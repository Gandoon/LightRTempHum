package humTempC;

import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
//import javax.swing.JSlider;
import javax.swing.KeyStroke;
//import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
//import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialTextAnnotation;
//import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
//import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.chart.ui.GradientPaintTransformType;
import org.jfree.chart.ui.StandardGradientPaintTransformer;

import com.fazecast.jSerialComm.SerialPort;

//import humTempC.OsCheck;
import humTempC.OsCheck.OSType;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.awt.event.InputEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.SystemColor;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
//import javax.swing.SpringLayout;
import java.awt.Font;
import java.awt.GradientPaint;
//import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JButton;
import net.miginfocom.swing.MigLayout;

public class EnvironmentMonitor {

	private JFrame envFrame;
	private Communications comms;
	private ImageIcon wIcon;
	private CalibrationData calData;
	private boolean firstRun;
	private Thread publisher; // Publisher thread
	private String latestString;

	private Properties prefs, defaults;
	private File prefsPath;
	private PrintWriter logWriter;

	private JTextArea tempOut;
	private JTextArea humOut;
	private JTextArea txtrSerialConsole;
	private JLabel tSat, hSat;

	private DefaultValueDataset dialTemp, maxTemp, minTemp;
	private DefaultValueDataset dialHum, maxHum, minHum;
	private DialPlot tDial, hDial;

	//private XYSeries tSeries, hSeries;  // Optional way of storing the data, not used here.
	private TimeSeries tSeries, hSeries;
	
	// How often do we check for time drift on the RTC in minutes? Defaults to 60 minutes.
	// These values have been moved to the calData. Change default values by editing preference file.	
	// private int timeCheckInterval = 10;
	// private int allowedDrift = 0;        // Allowed minor drift before a set event will occur. Defaults to 29 seconds.
	
	// Last known check of the clock on the board was at this time
	// Moved to calData
	// private Calendar lastCheck = Calendar.getInstance(); // Give it some reasonable value to begin with

	private EnvironmentMonitor monitor;

	// 0.3	 - Added dials on the startup tab, added max/min indicators on the dials, and changed frame handling on primary tab to miglayout15.
	// 0.3.1 - Corrected DHT11 saturation level, removed unused library imports.
	// 0.3.2 - Fixed display control and a few other bugs found while performing less often used features
	// 0.3.3 - Added a container panel on the plot tab, in preparation for interval selection and plot clearing provisions. (branched)
	// 0.3.4 - Implemented the clear button logic and changed to a XYLineAndShapeRenderer for the humidity portion of the plot (the spline overshoots where a bit distracting
	// 		   when the change is minimal between data points).
	// 0.3.5 - Initial implementation of automatic time and date setting.
	private final String version = "0.3.5";
	private final String cYear = "2022";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EnvironmentMonitor window = new EnvironmentMonitor();
					window.envFrame.setTitle("LightR – Temperature and Humidity");
					window.envFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public EnvironmentMonitor() {
		firstRun = true;
		monitor = this;
		initialize();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	//@SuppressWarnings({ "deprecation", "unchecked" })
	@SuppressWarnings("deprecation")
	private void initialize() {
		comms = new Communications();
		calData = new CalibrationData(comms);
		prefs = new Properties();
		defaults = new Properties();
		OSType localOS = OsCheck.getOperatingSystemType();
		//System.out.println(localOS);

		// For Temperature
		dialTemp = new DefaultValueDataset(25D);
		maxTemp	 = new DefaultValueDataset(35D);
		minTemp	 = new DefaultValueDataset(15D);

		// For Humidity
		dialHum  = new DefaultValueDataset(50D);
		maxHum   = new DefaultValueDataset(70D);
		minHum   = new DefaultValueDataset(30D);

		String iconURL = "LightRLogo.png";
		BufferedImage bIm;
		try {
			bIm = ImageIO.read(ResourceLoader.load(iconURL));
			wIcon = new ImageIcon(bIm);
		} catch (Exception e) {
			System.out.println("Could not load icon image.");
		}

		// Read the defaults and previous settings
		try (InputStream pin = ResourceLoader.load("/EnvironmentDefaults.xml")) {					
			defaults.loadFromXML(pin); //FromXML
			pin.close();
			prefsPath = new File(defaults.getProperty("prefsPath"));
			System.out.println("Loaded prefsPath from Defaults: "+prefsPath);

		} catch (IOException e1) {
			System.out.println("No defaults found...");
			prefsPath = new File("EnvironmentPrefs.xml");
			//e1.printStackTrace();
		} catch (NullPointerException e2) {
			System.out.println("Totally failed to open defaults file, this normally should not happen...");
			e2.printStackTrace();
			//return;
		}
		try (FileInputStream pin = new FileInputStream(prefsPath)) {
			prefs.loadFromXML(pin); //FromXML
			pin.close();
			// Read preferences from FilterPrefs and store in calData, if not found use defaults.
			// If defaults are not found, use hardcoded standard values
			File prePrefsPath = new File(prefs.getProperty("prefsPath"));
			// File(prefs.getProperty("prefsPath")+ File.separator +"FilterPrefs.xml");
			System.out.print("prefsPath: ");
			System.out.println(prefsPath);
			System.out.print("prePrefsPath: ");
			System.out.println(prePrefsPath);
			//					System.out.print("prefsPath == prePrefsPath: ");
			//					System.out.println(prePrefsPath.equals(prefsPath));
			if (!prePrefsPath.equals(prefsPath)) {
				//prefsPath = prePrefsPath;
				try (FileInputStream pin2 = new FileInputStream(prePrefsPath)) {
					prefs.loadFromXML(pin2); //FromXML
					pin2.close();
					prefsPath = new File(prefs.getProperty("prefsPath")); // ,"FilterPrefs.xml"
					System.out.print("Read prefsPath: ");
					System.out.println(prefsPath);
				}
			} else {
				//						prefs.loadFromXML(pin); //FromXML
				//						pin.close();
			}
			calData.loadPrefs(prefs, defaults);
		} catch (IOException e1) {
			System.out.println("No preferences found...\nLoading defaults.");
			prefs = defaults;
		} catch (NullPointerException e2) {
			System.out.println("Totally failed to open prefs file, this should not normally happen...");
			e2.printStackTrace();
			return;
		}

		envFrame = new JFrame();
		envFrame.setBounds(100, 100, 1010, 520);
		//envFrame.set
		envFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		String portStringDefault, portString = "";
		if (localOS == OSType.MacOS) {
			//			if (localOS == OSType.MacOS) {
			//				portStringDefault = "cu.usbmodem142101";
			//			} else { // System identified as Linux
			//				portStringDefault = "ttyACM0";
			//			}
			//portStringDefault = defaults.getProperty("MacSerialPort", "cu.usbmodem141521");
			//portString = prefs.getProperty("SerialPort", portStringDefault);
			portStringDefault = defaults.getProperty("MacSerialPort", "cu.usbmodem142101");
			portString = prefs.getProperty("SerialPort", "");
			SerialPort port;
			if (portString.equals("")) {
				System.out.println("Selecting default port: "+portStringDefault);
				port = SerialPort.getCommPort(portStringDefault);			
			} else {
				port = SerialPort.getCommPort(portString);
				System.out.println("Selecting port from previous session: "+portString);
			}
			//port = SerialPort.getCommPort(portList.getSelectedItem().toString());
			System.out.println("Selected port: "+port.getSystemPortName());
			port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0); //TIMEOUT_NONBLOCKING
			if( !port.isOpen() ) { // false ) {//
				if(port.openPort()) {
					System.out.println("Successfully opened port " + portString);
					comms.setOpenPort(port);
					comms.initialise();
					System.out.println("Automatically selected, commiting selected port ("+comms.getOpenPort().getSystemPortName()+")");
					//					System.out.print("Comms configured: ");
					//					System.out.println(comms.isConfigured());
					//					comms.sendCommand("l"+prefs.getProperty("loBr", "31")+"\n");
					//					comms.sendCommand("h"+prefs.getProperty("hiBr", "127")+"\n");
					//					comms.sendCommand("s"+prefs.getProperty("currpos", "0")+"\n");
					//					comms.sendCommand("t"+prefs.getProperty("timeout", "0")+"\n");
					//					if ( Boolean.parseBoolean(prefs.getProperty("dispOn","true")) ) {
					//						comms.sendCommand("d1\n");
					//					} else {
					//						comms.sendCommand("d0\n");
					//					}
					//					calData.commitPrefs();
				} else {
					//					JOptionPane.showMessageDialog(frmFilterControl,"Could not open serial port " + portString,
					//							"Error",JOptionPane.ERROR_MESSAGE);
					SerialPortSelect dialog = new SerialPortSelect(comms,monitor);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
					dialog.setAlwaysOnTop(true);
				}
			} else {
				System.out.println("Port "+comms.getOpenPort().getSystemPortName()+" already open");						
			}
		} else if (localOS == OSType.Windows) {
			portStringDefault = defaults.getProperty("WinSerialPort", "COM5");
			portString = prefs.getProperty("SerialPort", portStringDefault);
			SerialPort port;
			if (portString.equals("")) {
				port = SerialPort.getCommPort(portStringDefault);
			} else {
				port = SerialPort.getCommPort(portString);
			}
			//port = SerialPort.getCommPort(portList.getSelectedItem().toString());
			System.out.println("Selected port: "+port.getSystemPortName());
			port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
			if( !port.isOpen() ) { // false ) {//
				if(port.openPort()) {
					System.out.println("Successfully opened port " + portString);
					comms.setOpenPort(port);
					comms.initialise();
					//calData.commitPrefs();
					System.out.println("Automatically selected, commiting selected port ("+comms.getOpenPort().getSystemPortName()+")");
				} else {
					//					JOptionPane.showMessageDialog(frmFilterControl,"Could not open serial port " + portString,
					//							"Error",JOptionPane.ERROR_MESSAGE);
					SerialPortSelect dialog = new SerialPortSelect(comms,monitor);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
					dialog.setAlwaysOnTop(true);
				}				
			} else {
				System.out.println("Port "+comms.getOpenPort().getSystemPortName()+" already open");						
			}
		} else {
			SerialPortSelect dialog = new SerialPortSelect(comms,monitor);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			dialog.setAlwaysOnTop(true);
		}

		JMenuBar menuBar = new JMenuBar();
		envFrame.setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("Environment");
		menuBar.add(mnNewMenu);

		JMenuItem mntmAboutEnv = new JMenuItem("About");
		mntmAboutEnv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (calData.getComms().isConfigured())
					calData.getComms().sendCommand("a\n");
				JOptionPane.showMessageDialog(envFrame,"Arduino coupled environment monitor\nCopyright \u00a9 "+cYear+" Erik G Hedlund\n"
						+ "LightR\nAll rights reserved\nversion "+version+" for "+localOS,"About",JOptionPane.PLAIN_MESSAGE, wIcon); // 
				if (calData.getComms().isConfigured())
					calData.getComms().sendCommand("b\n");
				System.out.println("This is executed after the about box is invoked...");
			}
		});
		mnNewMenu.add(mntmAboutEnv);

		JMenuItem mntmEnvQuit = new JMenuItem("Quit");
		mntmEnvQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// This bit saves preferences
				if (comms.isConfigured()) {
					prefs.setProperty("SerialPort",comms.getOpenPort().getSystemPortName());
				} else {
					prefs.setProperty("SerialPort","");
				}
				File repPath = new File("EnvironmentPrefs.xml");
				if (!prefsPath.equals(repPath)) {
					repPath = prefsPath;
				}
				prefs.setProperty("prefsPath", prefsPath.getAbsolutePath());
				System.out.print("repPath: ");
				System.out.println(repPath);
				System.out.print("prefsPath: ");
				System.out.println(prefsPath);
				//prefs.setProperty("MacSerialPort", comms.getOpenPort().getSystemPortName());
				//prefs.setProperty("WinSerialPort", "COM5");
				
				// For automatic time handling. We can edit the default settings by enabling these lines and setting the values as required.
				// This is only for checking internal functioning and should be removed later.
				// calData.timeCheckInterval(9);
				// calData.allowedDrift(0);
				
				calData.storePrefs(prefs);
				try {
					if (repPath.createNewFile()) 
						System.out.println("file not found.\nCreating file...");
					else
						System.out.println("file already exists");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("IO exception while trying to create file; "+repPath+"... \n (Should not happen.)");
					e1.printStackTrace();
				}
				try (FileOutputStream pOutx = new FileOutputStream(repPath)){  // ,"FilterPrefs.xml"
					//FileOutputStream pOut  = new FileOutputStream("FilterPrefs.conf");
					//prefs.store(pOut, "---Filter wheel defaults---");
					prefs.storeToXML(pOutx, "---Environment monitor state and settings---");
					//pOut.close();
					pOutx.close();
				} catch (IOException ex) {
					System.out.println("file could not be opened... This need to be fixed");

					ex.printStackTrace();
				}
				// try { publisher.join(); } catch (Exception exc) { System.out.println("Failed to stop publisher thread."); }

				if (calData.getComms().isConfigured()) {
					System.out.println("Attempting to close open serial port... ");
					if(comms.closeOpenPort()) {
						System.out.println("Succeded");
					} else {
						System.out.println("FAILED!!");
					}
				}

				if(calData.getLogWriter() != null) {
					System.out.println("Closing any open log file writers");
					try {
						logWriter.println("--- Application terminated ---");
						calData.getLogWriter().flush();
						calData.getLogWriter().close();
						//calData.setLogWriter(null);
					} catch (IOException e1) {
						// Weirdness while closing the logWriter
						System.out.println("Error encountered while flushing and closing the FileWriter");
						e1.printStackTrace();
					}
				}

				System.out.println("Termination command issued. I will now gracefully die...");
				System.exit(0);
			}
		});
		mntmEnvQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mnNewMenu.add(mntmEnvQuit);

		JMenu mnEnvMenu = new JMenu("Settings");
		menuBar.add(mnEnvMenu);

		JMenuItem mntmEnvSettings = new JMenuItem("Set time");
		mntmEnvSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SetTime timeSetter = new SetTime(calData);
				timeSetter.setLocationRelativeTo(envFrame);
				timeSetter.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				timeSetter.setVisible(true);
			}
		});
		mntmEnvSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		mnEnvMenu.add(mntmEnvSettings);

		JMenuItem mntmDispMenu = new JMenuItem("Display");
		mntmDispMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		mntmDispMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Display configuration selected...");
				try {
					DisplayControl dDialog = new DisplayControl(calData); //, dispOn, displayTimedOut, chckbxmntmUseDisplayTimeout);
					dDialog.setLocationRelativeTo(envFrame);
					dDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dDialog.setVisible(true);
					//System.out.println(calData.getCurrPos());
				} catch (Exception err) {
					System.out.println("Couldn't start the display configurator");
					err.printStackTrace();
				}
			}
		});
		mnEnvMenu.add(mntmDispMenu);

		JCheckBoxMenuItem chckbxmntmDisplay = new JCheckBoxMenuItem("Display on/off");
		chckbxmntmDisplay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxmntmDisplay.isSelected()) {
					calData.setDispOn(true);
					calData.getComms().sendCommand("d1\n");
				} else {
					calData.setDispOn(false);
					calData.getComms().sendCommand("d0\n");
				}
			}
		});
		chckbxmntmDisplay.setSelected(calData.isDispOn());
		chckbxmntmDisplay.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		mnEnvMenu.add(chckbxmntmDisplay);

		JMenuItem mntmLogLoc = new JMenuItem("Log settings");
		mntmLogLoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		mntmLogLoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Configure serial port selected...");
				try {
					LogSelection dialog = new LogSelection(calData, envFrame);
					dialog.setLocationRelativeTo(envFrame);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
					//System.out.println(calData.getCurrPos());
				} catch (Exception err) {
					err.printStackTrace();
				}
				// System.out.println(calData.getCurrPos());
				//				JOptionPane.showMessageDialog(mntmAbout,"Attenuator wheel control for 488 nm laser\n Copyright \u00a9 2018 Erik Hedlund\n "
				//						+ "KU Leuven\n all rights reserved","Calibration",JOptionPane.INFORMATION_MESSAGE, wIcon);
			}
		});
		mnEnvMenu.add(mntmLogLoc);


		JMenuItem mntmConfSerial = new JMenuItem("Configure serial port");
		mntmConfSerial.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		mntmConfSerial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Configure serial port selected...");
				try {
					SerialPortSelect dialog = new SerialPortSelect(comms,monitor);
					dialog.setLocationRelativeTo(envFrame);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
					//System.out.println(calData.getCurrPos());
				} catch (Exception err) {
					err.printStackTrace();
				}
				// System.out.println(calData.getCurrPos());
				//				JOptionPane.showMessageDialog(mntmAbout,"Attenuator wheel control for 488 nm laser\n Copyright \u00a9 2018 Erik Hedlund\n "
				//						+ "KU Leuven\n all rights reserved","Calibration",JOptionPane.INFORMATION_MESSAGE, wIcon);
			}
		});

		JMenuItem mntmSelectSettingsLocation = new JMenuItem("Select settings location");
		mntmSelectSettingsLocation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		mntmSelectSettingsLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//JFrame chooserFrame = new JFrame();
				JFileChooser chooser = new JFileChooser();
				//FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT files", "txt");
				//chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setFileHidingEnabled(false);
				//chooser.setCurrentDirectory(new File("").getAbsoluteFile());
				chooser.setCurrentDirectory(prefsPath);
				chooser.setDialogTitle("Select folder to save session settings to");
				chooser.setDialogType(JFileChooser.SAVE_DIALOG);
				int returnVal = chooser.showSaveDialog(envFrame);
				//				ArrayList<JPanel> jpanels = new ArrayList<JPanel>();
				//	            for(Component c : chooser.getComponents()){
				//	                if( c instanceof JPanel ){
				//	                    jpanels.add((JPanel)c);
				//	                }
				//	            }
				//	            jpanels.get(0).getComponent(0).setVisible(false);
				//	            File dir = chooser.getSelectedFile();
				//                if(!dir.exists()){
				//                    dir = dir.getParentFile();
				//                }
				//chooserFrame.getContentPane().add(chooser);
				//chooserFrame.pack();
				//chooserFrame.setVisible(true);

				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File altPrefsPath = new File(chooser.getSelectedFile(),"EnvironmentPrefs.xml");
					System.out.print("Original prefsPath: ");
					System.out.println(prefsPath);
					prefs.setProperty("prefsPath", altPrefsPath.getAbsolutePath());
					System.out.print("AltPrefsPath: ");
					System.out.println(altPrefsPath.getAbsolutePath());
					try {
						if (altPrefsPath.createNewFile()) 
							System.out.println("file not found.\nCreating file...");
						else
							System.out.println("file already exists");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						System.out.println("IO exception while trying to create file... \n (Should not happen.)");
						e1.printStackTrace();
					}
					try (FileOutputStream pOutx = new FileOutputStream(prefsPath)) { // new File(prefsPath,"FilterPrefs.xml")
						//FileOutputStream pOut  = new FileOutputStream("FilterPrefs.conf");
						//prefs.store(pOut, "---Filter wheel defaults---");
						prefs.storeToXML(pOutx, "---Environment monitor state and settings---");
						//pOut.close();
						pOutx.close();
					} catch (Exception ex) {
						System.out.println("Exception while saving preferences to standard file...");
						ex.printStackTrace();
					}
					//					System.out.println("You chose to save settings to this folder: " +
					//							chooser.getSelectedFile().getAbsolutePath());
					prefsPath = altPrefsPath; //new File (altPrefsPath,"FilterPrefs.xml");
					System.out.print("You chose to save settings to this file: ");
					System.out.println(prefsPath);
				} else if (returnVal == JFileChooser.CANCEL_OPTION) {
					System.out.println("Cancel selected, previous path still valid.");
				}
				//int returnVal = fc.showOpenDialog(aComponent);
			}
		});
		mnEnvMenu.add(mntmSelectSettingsLocation);
		mnEnvMenu.add(mntmConfSerial);



		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(new Color(220, 220, 220));
		envFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		txtrSerialConsole = new JTextArea("*** Serial console ***"); // 5,5);//
		txtrSerialConsole.setEditable(false);
		txtrSerialConsole.setLineWrap(true);
		txtrSerialConsole.setBackground(SystemColor.window);
		txtrSerialConsole.setWrapStyleWord(true);
		//txtrSerialConsole.setRows(16);
		//textArea.setCaretPosition(textArea.getDocument().getLength());
		DefaultCaret caret = (DefaultCaret) txtrSerialConsole.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel readoutPanel = new JPanel();		
		readoutPanel.setBackground(new Color(229, 229, 229));
		tabbedPane.addTab("Readings", null, readoutPanel, null);
		readoutPanel.setLayout(new MigLayout("", "[100px:400px:800px,grow][100px:400px:800px,grow][10px][133px]", "[16px][6px][30px][6px][16px][35px][16px][6px][30px][6px][16px][2px:192px:512px,grow][29px]"));

		JLabel tempLabel = new JLabel("Temperature");
		readoutPanel.add(tempLabel, "cell 3 0,alignx center,aligny bottom");

		tempOut = new JTextArea("        ");
		//sl_panel.putConstraint(SpringLayout.WEST, tempOut, -10, SpringLayout.EAST, panel);
		tempOut.setBackground(new Color(50, 205, 50));
		//tempOut.setColumns(7);
		tempOut.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		tempOut.setEditable(false);
		tempOut.setAutoscrolls(false);
		readoutPanel.add(tempOut, "cell 3 2,grow");

		tSat = new JLabel("* SATURATED *");
		tSat.setForeground(new Color(255, 0, 0));
		tSat.setVisible(false);
		readoutPanel.add(tSat, "cell 3 4,alignx center,aligny top");

		JLabel humLabel = new JLabel("Relative humidity");
		readoutPanel.add(humLabel, "cell 3 6,alignx center,aligny top");

		humOut = new JTextArea("        ");
		humOut.setEditable(false);
		humOut.setBackground(new Color(50, 205, 50));
		humOut.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		readoutPanel.add(humOut, "cell 3 8,grow");

		//********** Dial 1 START **********//

		tDial = new DialPlot();

		tDial.setView(0.0D, 0.0D, 1.0D, 1.0D);
		tDial.setDataset(0, dialTemp);
		tDial.setDataset(1, maxTemp);
		tDial.setDataset(2, minTemp);

		StandardDialFrame standarddialframe = new StandardDialFrame();
		standarddialframe.setBackgroundPaint(Color.lightGray);
		standarddialframe.setForegroundPaint(Color.darkGray);
		tDial.setDialFrame(standarddialframe);

		GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), new Color(170, 170, 220));
		DialBackground dialbackground = new DialBackground(gradientpaint);

		dialbackground.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
		tDial.setBackground(dialbackground);

		DialTextAnnotation dialtextannotation = new DialTextAnnotation("Temperature");
		dialtextannotation.setFont(new Font("Dialog", 1, 14));
		dialtextannotation.setRadius(0.69999999999999996D);
		tDial.addLayer(dialtextannotation);

		//        DialValueIndicator dialvalueindicator = new DialValueIndicator(0);
		//        dialvalueindicator.setFont(new Font("Dialog", 0, 10));
		//        dialvalueindicator.setOutlinePaint(Color.darkGray);
		//        dialvalueindicator.setRadius(0.59999999999999998D);
		//        dialvalueindicator.setAngle(-103D);
		//        tDial.addLayer(dialvalueindicator);
		//        
		//        DialValueIndicator dialvalueindicator1 = new DialValueIndicator(1);
		//        dialvalueindicator1.setFont(new Font("Dialog", 0, 10));
		//        dialvalueindicator1.setOutlinePaint(Color.red);
		//        dialvalueindicator1.setRadius(0.59999999999999998D);
		//        dialvalueindicator1.setAngle(-77D);
		//        tDial.addLayer(dialvalueindicator1);

		StandardDialScale standarddialscale = new StandardDialScale(0D, 50D, -120D, -300D, 10D, 9);
		standarddialscale.setTickRadius(0.88D);
		standarddialscale.setTickLabelOffset(0.14999999999999999D);
		standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		tDial.addScale(0, standarddialscale);

		StandardDialScale standarddialscale1 = new StandardDialScale(0.0D, 50D, -120D, -300D, 10D, 4);
		standarddialscale1.setTickRadius(0.5D);
		standarddialscale1.setTickLabelOffset(0.14999999999999999D);
		standarddialscale1.setTickLabelFont(new Font("Dialog", 0, 10));
		standarddialscale1.setMajorTickPaint(Color.red);
		standarddialscale1.setMinorTickPaint(Color.red);
		tDial.addScale(1, standarddialscale1);

		tDial.mapDatasetToScale(1, 1);

		StandardDialRange innerRing = new StandardDialRange(0D, 50D, Color.blue);
		innerRing.setScaleIndex(1);
		innerRing.setInnerRadius(0.52000000000000002D);
		innerRing.setOuterRadius(0.55000000000000004D);
		tDial.addLayer(innerRing);
		//        underSaturated.setScaleIndex(1);
		//        underSaturated.setInnerRadius(0.58999999999999997D);
		//        underSaturated.setOuterRadius(0.58999999999999997D);
		//        tDial.addLayer(underSaturated);

		org.jfree.chart.plot.dial.DialPointer.Pin tMinPin = new org.jfree.chart.plot.dial.DialPointer.Pin(2);
		tMinPin.setRadius(0.55000000000000004D);
		tMinPin.setPaint(Color.blue);
		tDial.addPointer(tMinPin);

		org.jfree.chart.plot.dial.DialPointer.Pin tMaxPin = new org.jfree.chart.plot.dial.DialPointer.Pin(1);
		tMaxPin.setRadius(0.55000000000000004D);
		tMaxPin.setPaint(Color.red);
		tDial.addPointer(tMaxPin);

		org.jfree.chart.plot.dial.DialPointer.Pointer pointer = new org.jfree.chart.plot.dial.DialPointer.Pointer(0);
		tDial.addPointer(pointer);

		DialCap dialcap = new DialCap();
		dialcap.setRadius(0.10000000000000001D);
		tDial.setCap(dialcap);

		//ChartFactory.create

		JFreeChart jfreechart = new JFreeChart(tDial);
		//jfreechart.setTitle("Environment monitor");
		ChartPanel chartpanel = new ChartPanel(jfreechart);
		chartpanel.setPreferredSize(new Dimension(400, 400));
		//        JPanel jpanel = new JPanel(new GridLayout(2, 2));
		//        jpanel.add(new JLabel("Outer Needle:"));
		//        jpanel.add(new JLabel("Inner Needle:"));
		//        slider1 = new JSlider(-40, 60);
		//        slider1.setMajorTickSpacing(20);
		//        slider1.setPaintTicks(true);
		//        slider1.setPaintLabels(true);
		//        slider1.addChangeListener(this);
		//        jpanel.add(slider1);
		//        jpanel.add(slider1);
		//        slider2 = new JSlider(0, 100);
		//        slider2.setMajorTickSpacing(20);
		//        slider2.setPaintTicks(true);
		//        slider2.setPaintLabels(true);
		//        slider2.addChangeListener(this);
		//        jpanel.add(slider2);
		readoutPanel.add(chartpanel, "cell 0 0 1 13,aligny top,grow");
		//        panel.add(jpanel, "South");

		//**********  Dial 1 END  **********//

		//********** Dial 2 START **********//

		hDial = new DialPlot();

		hDial.setView(0.0D, 0.0D, 1.0D, 1.0D);
		hDial.setDataset(0, dialHum);
		hDial.setDataset(1, maxHum);
		hDial.setDataset(2, minHum);

		StandardDialFrame hDialFrame = new StandardDialFrame();
		hDialFrame.setBackgroundPaint(Color.lightGray);
		hDialFrame.setForegroundPaint(Color.darkGray);
		hDial.setDialFrame(hDialFrame);

		GradientPaint gradientpaint1 = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), new Color(170, 170, 220));
		DialBackground dialbackground1 = new DialBackground(gradientpaint1);

		dialbackground1.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
		hDial.setBackground(dialbackground1);

		DialTextAnnotation dialtextannotation1 = new DialTextAnnotation("Humidity");
		dialtextannotation1.setFont(new Font("Dialog", 1, 14));
		dialtextannotation1.setRadius(0.69999999999999996D);
		hDial.addLayer(dialtextannotation1);

		//              DialValueIndicator dialvalueindicator = new DialValueIndicator(0);
		//              dialvalueindicator.setFont(new Font("Dialog", 0, 10));
		//              dialvalueindicator.setOutlinePaint(Color.darkGray);
		//              dialvalueindicator.setRadius(0.59999999999999998D);
		//              dialvalueindicator.setAngle(-103D);
		//              hDial.addLayer(dialvalueindicator);
		//              
		//              DialValueIndicator dialvalueindicator1 = new DialValueIndicator(1);
		//              dialvalueindicator1.setFont(new Font("Dialog", 0, 10));
		//              dialvalueindicator1.setOutlinePaint(Color.red);
		//              dialvalueindicator1.setRadius(0.59999999999999998D);
		//              dialvalueindicator1.setAngle(-77D);
		//              hDial.addLayer(dialvalueindicator1);

		StandardDialScale standarddialscale2 = new StandardDialScale(0D, 100D, -120D, -300D, 10D, 9);
		standarddialscale2.setTickRadius(0.88D);
		standarddialscale2.setTickLabelOffset(0.14999999999999999D);
		standarddialscale2.setTickLabelFont(new Font("Dialog", 0, 14));
		hDial.addScale(0, standarddialscale2);

		StandardDialScale standarddialscale3 = new StandardDialScale(0.0D, 100D, -120D, -300D, 10D, 4);
		standarddialscale3.setTickRadius(0.5D);
		standarddialscale3.setTickLabelOffset(0.14999999999999999D);
		standarddialscale3.setTickLabelFont(new Font("Dialog", 0, 10));
		standarddialscale3.setMajorTickPaint(Color.red);
		standarddialscale3.setMinorTickPaint(Color.red);
		hDial.addScale(1, standarddialscale3);

		hDial.mapDatasetToScale(1, 1);

		StandardDialRange goodRange = new StandardDialRange(20D, 90D, Color.green);
		StandardDialRange underSaturated = new StandardDialRange(0D, 20D, Color.blue);
		StandardDialRange saturated = new StandardDialRange(90D, 100D, Color.red);
		saturated.setScaleIndex(1);
		saturated.setInnerRadius(0.52000000000000002D);
		saturated.setOuterRadius(0.55000000000000004D);
		hDial.addLayer(saturated);
		underSaturated.setScaleIndex(1);
		underSaturated.setInnerRadius(0.52000000000000002D);
		underSaturated.setOuterRadius(0.55000000000000004D);
		hDial.addLayer(underSaturated);
		goodRange.setScaleIndex(1);
		goodRange.setInnerRadius(0.52000000000000002D);
		goodRange.setOuterRadius(0.55000000000000004D);
		hDial.addLayer(goodRange);

		org.jfree.chart.plot.dial.DialPointer.Pin hMaxPin = new org.jfree.chart.plot.dial.DialPointer.Pin(1);
		hMaxPin.setRadius(0.55000000000000004D);
		hMaxPin.setPaint(Color.red);
		hDial.addPointer(hMaxPin);

		org.jfree.chart.plot.dial.DialPointer.Pin hMinPin = new org.jfree.chart.plot.dial.DialPointer.Pin(2);
		hMinPin.setRadius(0.55000000000000004D);
		hMinPin.setPaint(Color.blue);
		hDial.addPointer(hMinPin);

		org.jfree.chart.plot.dial.DialPointer.Pointer hpointerm = new org.jfree.chart.plot.dial.DialPointer.Pointer(0);
		hDial.addPointer(hpointerm);

		DialCap hdialcap = new DialCap();
		hdialcap.setRadius(0.10000000000000001D);
		hDial.setCap(hdialcap);

		//ChartFactory.create

		JFreeChart hfreechart = new JFreeChart(hDial);
		//hfreechart.setTitle("Environment monitor");
		ChartPanel hchartpanel = new ChartPanel(hfreechart);
		hchartpanel.setPreferredSize(new Dimension(400, 400));
		//              JPanel jpanel = new JPanel(new GridLayout(2, 2));
		//              jpanel.add(new JLabel("Outer Needle:"));
		//              jpanel.add(new JLabel("Inner Needle:"));
		//              slider1 = new JSlider(-40, 60);
		//              slider1.setMajorTickSpacing(20);
		//              slider1.setPaintTicks(true);
		//              slider1.setPaintLabels(true);
		//              slider1.addChangeListener(this);
		//              jpanel.add(slider1);
		//              jpanel.add(slider1);
		//              slider2 = new JSlider(0, 100);
		//              slider2.setMajorTickSpacing(20);
		//              slider2.setPaintTicks(true);
		//              slider2.setPaintLabels(true);
		//              slider2.addChangeListener(this);
		//              jpanel.add(slider2);
		readoutPanel.add(hchartpanel, "cell 1 0 1 13,aligny top,grow");

		hSat = new JLabel("* SATURATED *");
		hSat.setForeground(new Color(255, 0, 0));
		hSat.setVisible(false);
		readoutPanel.add(hSat, "cell 3 10,alignx center,aligny top");

		//**********  Dial 2 END  **********//              

		JButton resetMaxMin = new JButton("Reset Max/Min");
		resetMaxMin.setToolTipText("Reset the Max/Min indicators on the dials.");
		resetMaxMin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				maxTemp.setValue(dialTemp.getValue().doubleValue());
				minTemp.setValue(dialTemp.getValue().doubleValue());
				maxHum.setValue(dialHum.getValue().doubleValue());
				minHum.setValue(dialHum.getValue().doubleValue());
			}
		});
		readoutPanel.add(resetMaxMin, "cell 3 12,alignx left,aligny bottom");
		//              panel.add(jpanel, "South");



		txtrSerialConsole.setAutoscrolls(true);
		JScrollPane scroll = new JScrollPane(txtrSerialConsole);
		new SmartScroller( scroll );
		scroll.setFocusable(false);
		scroll.setAutoscrolls(true);

		tSeries = new TimeSeries("Temperature Sensor Readings");
		hSeries = new TimeSeries("Humidity Sensor Readings");
		TimeSeriesCollection tDataset = new TimeSeriesCollection(tSeries);
		TimeSeriesCollection hDataset = new TimeSeriesCollection(hSeries);
		JFreeChart thChart = ChartFactory.createTimeSeriesChart("Temperature and humidity", "", "Temperature (°C)", tDataset,true,true,false);
		XYPlot plot = (XYPlot) thChart.getPlot();
		XYSplineRenderer splinerenderer0 = new XYSplineRenderer();
		XYLineAndShapeRenderer splinerenderer1 = new XYLineAndShapeRenderer();
		NumberAxis humAx = new NumberAxis("Rel. Humidity (%)");
		humAx.setAutoRangeIncludesZero(false);
		plot.setRangeAxis(1, humAx);
		plot.setDataset(1, hDataset);
		plot.setRenderer(0,splinerenderer0);
		plot.setRenderer(1,splinerenderer1);
		plot.mapDatasetToRangeAxis(1, 1);
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setDefaultToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
		//thChart.redpaint();
		//        if (renderer instanceof StandardXYItemRenderer) {
		//            StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
		////            rr.setPlotShapes(true);
		////            rr.setShapesFilled(true);
		//        }

		//        StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
		//        renderer2.setSeriesPaint(0, Color.black);
		////        renderer2.setPlotShapes(true);
		//        renderer.setDefaultToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
		//        plot.setRenderer(1, renderer2);

		JPanel plotPanel = new JPanel();
		plotPanel.setBackground(new Color(229, 229, 229));
		plotPanel.setLayout(new MigLayout("", "[100px:400px:1600px,grow][10px][133px]", "[16px][6px][30px][6px][16px][35px][16px][6px][30px][6px][16px][2px:192px:512px,grow][29px]"));
		// [100px:400px:800px,grow]
		ChartPanel plotFrame = new ChartPanel(thChart);
		plotPanel.add(plotFrame, "cell 0 0 1 13,aligny top,grow");

		JButton resetGraph = new JButton("Clear plot");
		resetGraph.setToolTipText("Clear the contents of the plot.");
		resetGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Add the clearing code here
				tSeries.clear();
				hSeries.clear();
			}
		});
		resetGraph.setEnabled(true); // button is now enabled
		plotPanel.add(resetGraph, "cell 2 12,alignx right,aligny bottom");

		tabbedPane.addTab("Plot", null, plotPanel, null);

		tabbedPane.addTab("Console", null, scroll, null);
		// ("Light Sensor Readings", "Time (seconds)", "ADC Reading", dataset);

		startPublisher();

	}

	public void startPublisher() {
		publisher = new Thread() {
			@Override public void run() {
				// Wait after connection
				String timeBuffer = null;
				Calendar eCal = Calendar.getInstance(); // Should be used in several places...
				
				if (firstRun) {calData.lastCheck(eCal);}
				while(comms.isConfigured()) {

					//boolean nlAv = comms.hasNewLine();
					//System.out.println("Comms has new line available: "+Boolean.toString(nlAv));
					if(comms.hasNewLine()) {
						if (latestString != null) {					
							//System.out.println(Character.toString(latestString.charAt(0))+Character.toString(latestString.charAt(20)));
							if (latestString.charAt(0) == '[' && latestString.charAt(20) == ']') {
								timeBuffer = latestString;
								//System.out.println("Time string "+timeBuffer+" saved for later...");
							}
						}
						latestString = comms.latestString();
						//System.out.println(latestString.substring(25,30));
						eCal = Calendar.getInstance();
						if(latestString.contains("Humidity: ")) {							
							//System.out.println(latestString.substring(25,30)+" length is: "+
							//Integer.toString((latestString.substring(25,30)+"°C").length()));
							String curTxt  = tempOut.getText();
							double tValue = Double.parseDouble(latestString.substring(25,30));
							if (tValue > 50) { // Saturated
								tempOut.setBackground(new Color(248,102,104));
								tSat.setVisible(true);
							} else if (tValue < 0) { // Under-saturated
								tempOut.setBackground(new Color(142,205,250));
								tSat.setVisible(true);
							} else { // Standard operating conditions
								tempOut.setBackground(new Color(50,205,50));
								tSat.setVisible(false);
							}
							//String lCurTxt = " has length: " + Integer.toString(curTxt.length());
							//System.out.println(curTxt+lCurTxt);
							tempOut.replaceRange(" "+latestString.substring(25,30)+"°C",0,curTxt.length());							
							System.out.println("Created Calendar content: "+eCal.getTime());
							
							// Time check seems to have been placed here, trying to move it, hopefully nothing breaks...
							
							//tempOut.append(" "+latestString.substring(25,30)+"°C"); //,0,(latestString.substring(25,30)+"°C").length()+1);
							tSeries.add(new TimeSeriesDataItem(new Second(eCal.getTime()),tValue));
							dialTemp.setValue(tValue);
							if (maxTemp.getValue().doubleValue() < tValue) {
								maxTemp.setValue(tValue);
							} 
							if (minTemp.getValue().doubleValue() > tValue) {
								minTemp.setValue(tValue);
							}

							curTxt  = humOut.getText();
							double hValue = Double.parseDouble(latestString.substring(10,15));
							hSeries.add(new TimeSeriesDataItem(new Second(eCal.getTime()),hValue));
							dialHum.setValue(hValue);
							if (maxHum.getValue().doubleValue() < hValue) {
								maxHum.setValue(hValue);
							} 
							if (minHum.getValue().doubleValue() > hValue) {
								minHum.setValue(hValue);
							}

							//envFrame.repaint();

							if (hValue > 90) { // Saturated
								humOut.setBackground(new Color(248,102,104));
								hSat.setVisible(true);
							} else if (hValue < 20) { // Under-saturated
								humOut.setBackground(new Color(142,205,250));
								hSat.setVisible(true);
							} else { // Standard operating conditions
								humOut.setBackground(new Color(50,205,50)); // Other good colour: 144,238,144
								hSat.setVisible(false);
							}
							//lCurTxt = " has length: " + Integer.toString(curTxt.length());
							// System.out.println("Humidity string: "+curTxt+lCurTxt);
							//humOut.replaceRange("",0,curTxt.length());
							//humOut.append(" "+latestString.substring(10,15)+" %");
							humOut.replaceRange(" "+latestString.substring(10,15)+" %",0,curTxt.length());

							//* This is to be run once, at a point where it is confirmed that the Arduino is in the loop *//
							//** Setting the state of daytime only operation **//
							if (firstRun) {
								System.out.println("First run setup...");
								maxTemp.setValue(tValue);
								minTemp.setValue(tValue);
								maxHum.setValue(hValue);
								minHum.setValue(hValue);

								try {
									System.out.println("Trying to send high brightess command");
									calData.getComms().sendCommand("h"+calData.getHiBr()+"\n");
									Thread.sleep(450);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									System.out.println("Interrupted while trying to sleep for a while");
									e1.printStackTrace();
								}
								try {
									System.out.println("Trying to send medium brightess command");
									calData.getComms().sendCommand("m"+calData.getMiBr()+"\n");
									Thread.sleep(450);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									System.out.println("Interrupted while trying to sleep for a while");
									e1.printStackTrace();
								}
								try {
									System.out.println("Trying to send low brightess command");
									calData.getComms().sendCommand("l"+calData.getLoBr()+"\n");
									Thread.sleep(450);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									System.out.println("Interrupted while trying to sleep for a while");
									e1.printStackTrace();
								}
								if (calData.isDispOn()) {
									System.out.println("Display on");
									// System.out.println("selOff == true");
									// calData.getComms().sendCommand("d1\n");
									if (calData.useDayOnly()) {
										System.out.println("Trying to send command \"d2\\n\"");
										calData.getComms().sendCommand("d2\n");
									} else {
										System.out.println("Trying to send command \"d3\\n\"");
										calData.getComms().sendCommand("d3\n"); 
									}
								} else {
									System.out.println("Display off");
									// System.out.println("selOff == false");
									calData.getComms().sendCommand("d0\n");
								}					
								firstRun = false; // Potentially wasted memory, could probably be implemented differently, but it should work...
							}
							FileWriter lfw = calData.getLogWriter();
							//BufferedWriter bfw;
							if (lfw != null && calData.saveLog()) {
								String logStr = eCal.getTime().toString()+"\t"+eCal.getTime().getTime()+" "+Double.toString(tValue)+" "+Double.toString(hValue);
								logWriter.println(logStr);//eCal.getTime().toString()+"\t"+Double.toString(tValue)+" "+Double.toString(hValue));
								System.out.print("lfw != null ; Attempting to write to log: ");
								System.out.println(eCal.getTime().toString()+"\t"+eCal.getTime().getTime()+" "+Double.toString(tValue)+" "+Double.toString(hValue));
							} else if (lfw == null && calData.saveLog()) {
								try {
									lfw = new FileWriter(calData.getLogPath(),true);
									//bfw = new BufferedWriter(lfw);
									logWriter = new PrintWriter(new BufferedWriter(lfw),true);
									System.out.print("Attempting to write to log: ");
									System.out.println(eCal.getTime().toString()+"\t"+eCal.getTime().getTime()+" "+Double.toString(hValue));
									logWriter.println("--- LightR Temperature and Humidity log file format: "
											+ "*Timestamp* *temp* (°C) *hum* (RH %) ---");
									logWriter.println(eCal.getTime().toString()+"\t"+eCal.getTime().getTime()+" "+Double.toString(tValue)+" "+Double.toString(hValue));
								} catch (IOException e) {
									// Auto-generated catch block
									System.out.println("Could not create a FileWriter");
									//e.printStackTrace();
								}
								calData.setLogWriter(lfw);
							} else { // calData.saveLog() == false
								// If this is the case, logging has been deselected and the writing should be closed
								System.out.println("Logging disabled");									
								if(calData.getLogWriter() != null) {
									System.out.println("Closing any open log file writers");
									try {
										logWriter.println("--- Logging disabled ---");
										calData.getLogWriter().flush();
										calData.getLogWriter().close();
										calData.setLogWriter(null);
										logWriter = null; // Maybe not necessary, but if it triggers garbage collection, so be it...
									} catch (IOException e1) {
										// Weirdness while closing the logWriter
										System.out.println("Error encountered while flushing and closing the FileWriter");
										e1.printStackTrace();
									}
								}
							}
						}
						
//						System.out.println("Time since last date check: "+(eCal.getTime().getTime()-calData.lastCheck().getTime().getTime())/1000 + " s");
//						System.out.println("Saved timeCheckInterval: "+calData.timeCheckInterval()+" s");
						if(((eCal.getTime().getTime()-calData.lastCheck().getTime().getTime())/1000 > calData.timeCheckInterval() || calData.immediateSync() )
								&& calData.timeAutoSet() && timeBuffer != null) {
//							System.out.println("Time since last date check: "+(eCal.getTime().getTime()-calData.lastCheck().getTime().getTime())/1000 + " s");
							System.out.println("Saved allowedDrift: "+calData.allowedDrift()+" s");
//							System.out.println("Internal allowedDrift: "+calData.allowedDrift()+" s");
//							// Removing additional formatting, brackets and additional spaces at the beginning of the string
//							// We can probably work around this, avoiding the following altogether.
//							// timeBuffer = timeBuffer.replaceAll("\\[|\\]","");
//							timeBuffer = timeBuffer.replaceAll("\\s\\s"," ");
//							// timeBuffer = timeBuffer.trim();
//							// System.out.println(timeBuffer);
//							// Technically four positions need to be checked for a time discrepancy:
//							// If the minutes differ, we need to update the time either way. If that number still holds, it
//							// could be beneficial to check for a change in hour, in case there has been a summer/winter time 
//							// changeover. If both those holds, a check for the date could be made, but it probably is unnecessary.
//							// So, we will go with first checking if the hours are off, and if they are set then check for minutes,
//							// and after that for seconds. We will need to decide upon a reasonable number of seconds drift that is 
//							// acceptable so this is not done excessively. This will also need to be kept down to a check every hour or so.
							if (eCal.get(Calendar.HOUR_OF_DAY) != Integer.parseInt(timeBuffer.substring(12,14).trim())) {
								System.out.print("HOUR_OF_DAY: ");
								System.out.println(eCal.get(Calendar.HOUR_OF_DAY));
								//System.out.println("timebuffer length: "+timeBuffer.length());
								//System.out.println(timeBuffer.substring(12,14));
								//if (calData.timeAutoSet()) {
									setTime(Calendar.getInstance()); // eCal substituted for a fresh instantiation of a Calendar
									System.out.print("Parsed hour: ");
									System.out.println(Integer.parseInt(timeBuffer.substring(12,14).trim()));	
								//}
							} else if (eCal.get(Calendar.MINUTE) != Integer.parseInt(timeBuffer.substring(15,17).trim())) {
								System.out.print("MINUTE: ");
								System.out.println(eCal.get(Calendar.MINUTE));
								//System.out.println("timebuffer length: "+timeBuffer.length());
								//System.out.println(timeBuffer.substring(12,14));
								//if (calData.timeAutoSet()) {
									setTime(Calendar.getInstance());
									System.out.print("Parsed minute: ");
									System.out.println(Integer.parseInt(timeBuffer.substring(15,17).trim()));		
								//}
							} else if (Math.abs(Calendar.getInstance().get(Calendar.SECOND) - Integer.parseInt(timeBuffer.substring(18,20).trim())) > calData.allowedDrift()) {
								// Using a fresh Calendar above to get the most accurate reading to avoid unnecessary writes
								System.out.print("SECOND: ");
								System.out.println(eCal.get(Calendar.SECOND));
								System.out.println("Time difference: " + Math.abs(eCal.get(Calendar.SECOND) - Integer.parseInt(timeBuffer.substring(18,20).trim())) + " s");
								//System.out.println("timebuffer length: "+timeBuffer.length());
								//System.out.println(timeBuffer.substring(12,14));
								//if (calData.timeAutoSet()) {
								
									setTime(Calendar.getInstance());
									System.out.print("Parsed second: ");
									System.out.println(Integer.parseInt(timeBuffer.substring(18,20).trim()));
								//}
							}
							calData.immediateSync(false);
							calData.lastCheck(Calendar.getInstance());

							
							
							

							//								String[] dateSplit = timeBuffer.split("\\[|\\s|/|:|\\]");
							//								eCal.set(Integer.parseInt(dateSplit[2]),Integer.parseInt(dateSplit[1])-1,
							//										Integer.parseInt(dateSplit[0]), Integer.parseInt(dateSplit[3]),
							//										Integer.parseInt(dateSplit[4]),Integer.parseInt(dateSplit[5]));
							//								eCal.set(Integer.parseInt(dateSplit[dateSplit.length-4]),Integer.parseInt(dateSplit[dateSplit.length-5]),
							//										Integer.parseInt(dateSplit[dateSplit.length-6]), Integer.parseInt(dateSplit[dateSplit.length-3]),
							//										Integer.parseInt(dateSplit[dateSplit.length-2]),Integer.parseInt(dateSplit[dateSplit.length-1]));
							//								for(int j = dateSplit.length-1; j > 0; j--) { //String dateSplit : (timeBuffer).split("\\[|\\s|/|:|\\]")
							//									if (!(dateSplit[dateSplit.length-j].equals("")))
							//										System.out.println(j + " ("+ (dateSplit.length-1-j) + ") " + (dateSplit[dateSplit.length-j]));
							//								}
							//								
							//								eCal.set(Calendar.SECOND, Integer.parseInt(timeBuffer.substring(18,20)));
							//								eCal.set(Calendar.MINUTE, Integer.parseInt(timeBuffer.substring(15,17)));
							//								System.out.println("Updated Calendar content: "+eCal.getTime());
						}
						
						txtrSerialConsole.append("\n"+latestString);
					}
					try {
						Thread.sleep(100);
					} catch (Exception e) {
						System.out.println("The publisher thread was interrupted while waiting for communications initialisation to stabilise.");
						System.out.println(e.getStackTrace());
						//Silently ignore
					}

					//				if (comms.isConfigured()) {
					//					calData.commitPrefs();
					//				}	
				}
			}

			// This is a local method to set the time on the Arduino. It uses the same basic routine the manual setting in SetTime uses.
			private void setTime(Calendar now) {
				// Reuse and refresh the content of the Calendar object
				now = Calendar.getInstance();
				System.out.println(now.getTime());
				System.out.println("Sending command: "+"s"+Long.toString(now.getTime().getTime()));
				// Scope unclear, will we have access to the main Communications object here
				// Communications comms = calData.getComms();
				comms.sendCommand("s");// + Integer.MAX_VALUE);//Long.toString(now.getTime().getTime()));
				comms.sendLong((now.getTime().getTime()+TimeZone.getDefault().getOffset(now.getTime().getTime()))/1000+1);
			}	
			// For testing purposes only...
//			private void dummySetTime(String outputThis) {
//				System.out.print(outputThis);
//			}	
		};
		System.out.println("Starting publisher thread...");
		publisher.start();	
	}
}
