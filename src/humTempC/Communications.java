package humTempC;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
//import java.io.PrintWriter;
import java.util.Scanner;

//import com.fazecast.jSerialComm.*;
import com.fazecast.jSerialComm.SerialPort;

/**
 * @author Erik Hedlund
 *
 */
public class Communications {
	private SerialPort[] ports;
	private SerialPort selectedPort;
	private SerialPort openPort;
	private boolean configured;
	private Thread t;
	private static PrintStream output;
	private Scanner scanner;
	private String indata;
	private boolean hasNewLine;

	/**
	 * Configure Communications automatically
	 */
	public Communications() {
		super();
		ports = SerialPort.getCommPorts();
		configured = false;
		hasNewLine = false;
		// this.setPorts(ports);
	}

	/**
	 * Configure Communications with predefined serial ports
	 * 
	 * @param ports
	 */
	public Communications(SerialPort[] ports) {
		super();
		this.setPorts(ports);
		configured = false;
		hasNewLine = false;
	}

	public SerialPort getSelectedPort() {
		return selectedPort;
	}

	public void setSelectedPort(SerialPort selectedPort) {
		this.selectedPort = selectedPort;
	}

	/**
	 * @return the ports
	 */
	public SerialPort[] getPorts() {
		return ports;
	}

	/**
	 * @param ports the ports to set
	 */
	public void setPorts(SerialPort[] ports) {
		this.ports = ports;
	}

	public SerialPort getOpenPort() {
		if (configured) {
			return (openPort);
		} else {
			return (null);
		}
	}

	public void setOpenPort(SerialPort openPort) {
		this.openPort = openPort;
		configured = true;
	}

	public boolean isConfigured() {
		return (configured);
	}

	public synchronized String latestString() {
		hasNewLine = false;
		return (indata);
	}

	public synchronized boolean hasNewLine() { //
		return hasNewLine;
	}

	public void initialise() {
		// Call to start a communication thread
		t = new Thread() {
			@Override
			public void run() {
				// Wait after connection
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					// Silently ignore
				}

				// int readNo = 0;
				output = new PrintStream(new BufferedOutputStream(getOpenPort().getOutputStream()));

				scanner = new Scanner(getOpenPort().getInputStream());
				System.out.println("Scanner started...");
				indata = readSerialData(scanner);
				System.out.println("First String from scanner: " + indata + " * Read index 0"); // Integer.toString(readNo)
				while (configured) {
					/*
					 * while(scanner.hasNextLine()) { //
					 * //System.out.println("Entered the scanning section..."); String line =
					 * scanner.nextLine(); System.out.println(line);
					 * //try{Thread.sleep(10);}catch(InterruptedException ex) {}; }
					 */
					// readNo++;
					indata = readSerialData(scanner);
					System.out.println(indata);
					// +" * Read index: "+Integer.toString(readNo)+" New line available:
					// "+Boolean.toString(hasNewLine)
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// scanner.close();
			}
		};
		t.start();
	}

	public boolean closeOpenPort() {
		System.out.println("Attempting to close open port: " + openPort.getSystemPortName());
		configured = false;
		try {
			t.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		output.close();
		scanner.close();

		// Start a new thread to flush residual garbage from the serial interface
		// initialise();
		// immediately close things down again...
		// output.close();
		// scanner.close();
//		try {
//			t.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		// ...and close the port afterwards.
		if (openPort.closePort()) {
			System.out.println("Successfully closed port");
			openPort = null;
			// configured = false;
			return true;
		} else {
			configured = true;
			return false;
		}
	}

	public void setUnconfigured() {
		openPort = null;
		configured = false;
	}

	public void sendCommand(String cmd) {
		output.print(cmd);
		output.flush();
	}

	public void sendLong(long cmd) {
		output.println(cmd);
		output.flush();
	}

	private String readSerialData(Scanner scanner) {
		String line = "** data not read **";
		if (scanner.hasNextLine()) { //
			// System.out.println("Entered the scanning section...");
			line = scanner.nextLine();
			hasNewLine = true;
			// System.out.println(line);
			// try{Thread.sleep(10);}catch(InterruptedException ex) {};
		} else if (hasNewLine) {
			// hasNewLine = false;
			line = indata;
		}
		return line;
	}

}
