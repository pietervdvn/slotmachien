package slotmachien.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;

/**
 * Low level IO with USB. For use with USBHandler only!
 * 
 * @author pietervdvn
 *
 */
public class UsbIO {

	private NXTConnection conn;
	private BufferedReader reader;
	private BufferedWriter writer;

	public UsbIO(NXTConnection conn) {
		this.conn = conn;
		DataInputStream in = conn.openDataInputStream();
		// Buffered reader is required for generating an OutOfMemoryException
		// #shitsfucked
		this.reader = new BufferedReader(new InputStreamReader(in));

		DataOutputStream out = conn.openDataOutputStream();
		this.writer = new BufferedWriter(new OutputStreamWriter(out));
	}

	/**
	 * Factory method
	 * 
	 * @return
	 */
	public static UsbIO waitForUsbIO() {
		NXTConnection conn = USB.waitForConnection();
		return new UsbIO(conn);
	}

	public void writeLine(String line) throws IOException {
		writer.write(line);
		writer.newLine();
		writer.flush();
	}

	/**
	 * Blocking read line. You get the read line, with newline char. Throws an
	 * exception when something went wrong
	 * 
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		char temp;
		char last = '0';
		String s = "";
		int sameChar = 0;
		// Read character per character
		do {
			temp = (char) reader.read();
			s = s + temp;
			
			// we count how much time we see the same character. If > 32, we reset the connection
			if(last == temp){
				sameChar ++;
			}
			last = temp;
			
			if(sameChar >= 16){
				return "";	// connection failed, let's return nothing. (Otherwise, at least one \n is given)
			}
			
		} while (temp != '\n');
		return s;

	}

	public void close() throws IOException {
		writer.close();
		reader.close();
		conn.close();
	}

}
