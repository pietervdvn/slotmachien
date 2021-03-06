package slotmachien;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import observable.Mapper;
import observable.ObservableButton;
import observable.Observer;
import observable.PeriodicSignal;
import slotmachien.handlers.ButtonHandler;
import slotmachien.handlers.DelayedClose;
import slotmachien.handlers.MovedToMessage;
import slotmachien.handlers.SMMotorHandler;
import slotmachien.handlers.ScreenHandler;
import slotmachien.handlers.USBHandler;
import slotmachien.handlers.USBStatusToMessage;
import slotmachien.handlers.UsbParser;
import slotmachien.signals.ButtonSignal;
import slotmachien.signals.MessageSignal;
import slotmachien.signals.UsbStatusSignal;
import slotmachien.signals.UsbStatusSignal.UsbStatus;

public class NXTMain {

	public static void main(String[] args) {
		reactive();
	}
	
	public static void reactive() {
		System.out.println("Start Reactivity");

		final PeriodicSignal clock = new PeriodicSignal(100);
		
		final USBHandler usb = new USBHandler();

		final SMMotorHandler motors = new SMMotorHandler(clock, Motor.B, Motor.C);
		final ScreenHandler screen = new ScreenHandler();
		
		// write status updates to USB
		Mapper.pipe(motors, new MovedToMessage(), usb);
		// and to screen
		Mapper.pipe(motors, new MovedToMessage(), screen);
		
		// handle buttons
		ButtonHandler buttonHandler = new ButtonHandler();
		buttonHandler.addObserver(motors);

		// delayed closer
		DelayedClose delayedC = new DelayedClose(motors, new PeriodicSignal(1000), 10);
		// abort delayed close when something happens:
		motors.addObserverDangerous(delayedC.getCanceller());
		
		
		ObservableButton escape = new ObservableButton(Button.ESCAPE);
		
		new ObservableButton(Button.LEFT, Button.RIGHT)
		.addObserver(buttonHandler);
		new ObservableButton(Button.ENTER).addObserverDangerous(delayedC);
		escape
		.addObserver(new Observer<ButtonSignal>() {
			@Override
			public void notified(ButtonSignal signal){
				Sound.buzz();
				screen.clear();
			}
		});

		escape.addObserverDangerous(delayedC.getCanceller());
		

		// handle usb-input
		UsbParser parser = new UsbParser(motors, usb, screen);
		usb.addObserver(parser);
		// and write usb-failurs to screen
		Mapper<UsbStatusSignal, MessageSignal> conv = new USBStatusToMessage();
		usb.getStatusObservable().addObserver(conv);
		conv.addObserver(screen);
		// beep on usb connected/disconnected
		usb.getStatusObservable().addObserver(new Observer<UsbStatusSignal>() {

			@Override
			public void notified(UsbStatusSignal signal) {
				if (signal.status == UsbStatus.CONNECTED) {
					Sound.playNote(Sound.XYLOPHONE, 440, 250);
					Sound.playNote(Sound.XYLOPHONE, 880, 125);
					Sound.playNote(Sound.XYLOPHONE, 440, 250);
				} else {
					Sound.playNote(Sound.XYLOPHONE, 880, 500);
					Sound.playNote(Sound.XYLOPHONE, 440, 500);
					Sound.playNote(Sound.XYLOPHONE, 880, 500);
				}
			}

		});

		usb.getStatusObservable().addObserver(new Observer<UsbStatusSignal>() {

			@Override
			public void notified(UsbStatusSignal signal) {
				if (signal.status == UsbStatus.CONNECTED) {
					usb.notified("channel", "I'm back online!");
				}

			}
		});
		
	}
}
