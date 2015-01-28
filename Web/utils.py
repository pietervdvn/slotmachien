from datetime import datetime as dt
from subprocess import Popen, PIPE
from threading import Thread
import threading
import json
import time
import signal
import sys

import requests

from app import app
from auth import has_slack_token, get_user
from models import LogAction


class Process:
    def __init__(self):
        self.process = None
        self.inputProcessing = None
        self.heartbeat = None
        self.write_lock = None
        self.stopped = False
        self.last_status = ''
        self.create()

    def create(self):
        self.clean_process()
        if app.config['DEBUG']:
            self.process = Popen(['python test.py'], stdin=PIPE, stdout=PIPE,
                                 shell=True)
        else:
            self.process = Popen(['cd ../SlotMachienPC/src && ' +
                                 'java -cp /opt/leJOS_NXT/lib/pc/pccomm.jar:.'
                                  + ' PCMain'], stdin=PIPE, stdout=PIPE,
                                 shell=True)

        print('SlotMachienPC pid: ' + str(self.process.pid))
        self.stopped = False
	self._write_command_("status;startup;")
        self.last_status = self.process.stdout.readline()

        # Create input processing thread
        self.inputProcessing = InputProcessingThread(self)
        self.inputProcessing.setDaemon(True)
        self.inputProcessing.start()

        # Create heartbeat thread
        self.heartbeat = HeartBeatThread(self)
        self.heartbeat.setDaemon(True)
        self.heartbeat.start()

        self.write_lock = threading.Lock()

    def clean_process(self):
        print("Started cleaning")
        if self.process and not self.process.poll():
            self.process.stdin.close()
            self.process.stdout.close()
            try:
                self.process.terminate()
            except OSError:
                print("No process to terminate")
            self.process = None

        if self.inputProcessing and self.inputProcessing.isAlive():
            self.inputProcessing = None
            # does not need to be stopped because stdin is closed

        if self.heartbeat and self.heartbeat.isAlive():
            self.heartbeat.stop = True
            self.heartbeat = None
        print("Closed all threads")

    def check_alive(self):
        if not self.process or self.process.poll() or self.stopped:
            print("DEAD")
            self.create()

    def stdin(self):
        self.check_alive()
        return self.process.stdin

    def stdout(self):
        self.check_alive()
        return self.process.stdout

    def send_command(self, command):
        self.check_alive()
        self._write_command_(command)
        time.sleep(1.5)  # wait for a couple of seconds to return
        return {'status': self.last_status.lower().strip()}

    def _write_command_(self, command):
        self.write_lock.acquire()
        self.process.stdin.write(command + '\n')
        self.process.stdin.flush()
        self.write_lock.release()


class InputProcessingThread(Thread):
    def __init__(self, process):
        super(InputProcessingThread, self).__init__()
        self.process = process

    def run(self):
        print('starting input processing')
        for line in iter(self.process.stdout().readline, ""):
            if len(line) > 1:
                self.process.last_status = line
                line = line.strip()
                print("received: " + line)
                # TODO: add logging
                self.webhooks(line)
                # TODO: do webhooks (in new thread)
        print('thread: input processing stopped')
        self.process.stopped = True

    def webhooks(self, text):
        js = json.dumps({'text': text})
        url = app.config['SLACK_WEBHOOK']
        if len(url) > 0:
            requests.post(url, data=js)


class HeartBeatThread(Thread):
    def __init__(self, process):
        super(HeartBeatThread, self).__init__()
        self.process = process
        self.stop = False

    def run(self):
        print('starting heartbeat')
        while not self.stop:
            self.process._write_command_('status;heartbeat;')
            self.process.check_alive()
            time.sleep(5)
        print('thread: heartbeat stopped')


def send_command(command):
    global process
    log_action(command)

    response = process.send_command(command)

    return response

process = Process()


# Add signal handler because SlotMachienPC cannot be closed by ctrl+c
def signal_handler(signal, frame):
        global process
        print("SIGINT called")
        process.stdin().close()
        process.heartbeat.join()
        sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)


def log_action(action):
    if action not in ["status"]:
        LogAction.create(auth_key=has_slack_token(), user=get_user(),
                         action=action, logged_on=dt.now())
