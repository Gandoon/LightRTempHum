/**
 * A modal window to select if and where to save a log to.
 */
package humTempC;

import java.awt.BorderLayout;
//import java.awt.Dialog;
import java.awt.FlowLayout;
//import java.awt.Frame;
//import java.awt.GraphicsConfiguration;
//import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
//import java.io.PrintWriter;
//import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

//import com.fazecast.jSerialComm.SerialPort;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

/**
 * @author Erik Hedlund
 *
 */
public class LogSelection extends JDialog {

	/**
	 * A modal window to select if and where to save a log to.
	 *
	 */
	private static final long serialVersionUID = -4038273990016529602L;
	private CalibrationData calData;
	private JButton btnLogPath;
	private JCheckBox chckbxSaveLogs;
	private final JPanel contentPanel;
	private File logPath, origPath;
	private boolean selected, neverSelected, checkboxChanged;
	//private PrintWriter logWriter;

	/**
	 * 
	 */
	public LogSelection(CalibrationData calData,  JFrame parent) {   // PrintWriter logWriter,
		// No longer stub: constructor
		super(parent,true);
		this.calData = calData;
		//this.logWriter = logWriter;
		contentPanel = new JPanel();
		//this.setLocationRelativeTo(parent);
		checkboxChanged = false;
		initialise();
	}
	
	private void initialise() {
		setBounds(100, 100, 502, 138);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.WEST);
		
		selected = calData.saveLog();
		chckbxSaveLogs = new JCheckBox("Save log file");
		chckbxSaveLogs.setSelected(selected);
		chckbxSaveLogs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox sLog = (JCheckBox) e.getSource();
				if(sLog.isSelected()) {
					btnLogPath.setVisible(true);
					try {
						if (calData.getLogPath().toString().length() == 0) {
							neverSelected = true;
							selected = false;
							//calData.saveLog(false);
						} else {
							selected = true;
						}
					} catch (Exception ex) {
						// This should normally only happen if no path has previously been selected
						System.out.println("logPath never set, exception generated in checkbox");
						//dispPath = "<never selected>";
						neverSelected = true;
						selected = false;
					}

				} else {
					btnLogPath.setVisible(false);
					selected = false;
					calData.saveLog(false);
				}
				checkboxChanged = !checkboxChanged;
			}
		});
		chckbxSaveLogs.setMnemonic('s');
		chckbxSaveLogs.setHorizontalAlignment(SwingConstants.LEFT);
		contentPanel.add(chckbxSaveLogs);
		{
			String dispPath;
			try {
				dispPath = calData.getLogPath().toString();
				System.out.println("logPath as read: "+dispPath+" with length: "+dispPath.length());
				if (dispPath.length() == 0) {
					dispPath = "<never selected>";
					neverSelected = true;
				}
			} catch (Exception ex) {
				// This should normally only happen if no path has previously been selected
				System.out.println("logPath never set, exception generated");
				dispPath = "<never selected (exc)>";
				neverSelected = true;
			}
			if (dispPath.length() > 38)
				dispPath = "..." + dispPath.substring(dispPath.length()-35,dispPath.length());
				
			btnLogPath = new JButton("Saving to: " + dispPath);
			btnLogPath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(calData.getLogPath()); // Already in java.io.File format
					//FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT files", "txt");
					//chooser.setFileFilter(filter);
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					chooser.setFileFilter(new FileNameExtensionFilter("TXT files","txt"));
					String defaultLogName;
					try{
						defaultLogName = calData.getLogPath().getName();
					} catch (NullPointerException ex) {
						System.out.println("No record of logfile name and location, using a default...");
						defaultLogName = "HumTempLog.txt";
					}
					if ( defaultLogName == "")
						defaultLogName = "HumTempLog.txt";
					chooser.setSelectedFile(new File(defaultLogName)); //
					chooser.setFileHidingEnabled(false);
					//chooser.setCurrentDirectory(new File("").getAbsoluteFile());
					logPath = calData.getLogPath();
					origPath = logPath;
					chooser.setCurrentDirectory(logPath);
					chooser.setDialogTitle("Select file to save log to");
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					int returnVal = chooser.showSaveDialog(contentPanel);
//					ArrayList<JPanel> jpanels = new ArrayList<JPanel>();
//		            for(Component c : chooser.getComponents()){
//		                if( c instanceof JPanel ){
//		                    jpanels.add((JPanel)c);
//		                }
//		            }
//		            jpanels.get(0).getComponent(0).setVisible(false);
//		            File dir = chooser.getSelectedFile();
//	                if(!dir.exists()){
//	                    dir = dir.getParentFile();
//	                }
	                //chooserFrame.getContentPane().add(chooser);
		            //chooserFrame.pack();
		            //chooserFrame.setVisible(true);
		            
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						System.out.println(chooser.getSelectedFile());
						//if (chooser.)
						File altLogPath = chooser.getSelectedFile(); // new File(chooser.getSelectedFile(),"HumTempLog.txt");
						System.out.print("Original prefsPath: ");
						System.out.println(logPath);
//						prefs.setProperty("prefsPath", altPrefsPath.getAbsolutePath());
//						try (FileOutputStream pOutx = new FileOutputStream(prefsPath)) { // new File(prefsPath,"FilterPrefs.xml")
//							//FileOutputStream pOut  = new FileOutputStream("FilterPrefs.conf");
//							//prefs.store(pOut, "---Filter wheel defaults---");
//							prefs.storeToXML(pOutx, "---Filter wheel state and settings---");
//							//pOut.close();
//							pOutx.close();
//						} catch (Exception ex) {
//							System.out.println("Exception while saving preferences to standard file...");
//							ex.printStackTrace();
//						}
//						System.out.println("You chose to save settings to this folder: " +
//								chooser.getSelectedFile().getAbsolutePath());
						
						// If the Chosen path differs from the previous path, the writer needs to be restarted
						if (!altLogPath.equals(logPath)) {
							if(calData.getLogWriter() != null) {
								try {
									calData.getLogWriter().flush();
									calData.getLogWriter().close();
									calData.setLogWriter(null);
								} catch (IOException e1) {
									// Weirdness while closing the logWriter
									System.out.println("Error encountered while flushing and closing the FileWriter");
									e1.printStackTrace();
								}
							}
						}
						
						logPath = altLogPath; //new File (altPrefsPath,"FilterPrefs.xml");
						System.out.print("You chose to save settings to this file: ");
						System.out.println(logPath);
						calData.setLogPath(logPath);
						String newBtnText = logPath.toString();
						if (newBtnText.length() > 38)
							newBtnText = "..." + newBtnText.substring(newBtnText.length()-35,newBtnText.length());
						btnLogPath.setText("Saving to: " + newBtnText);
						neverSelected = false;
						selected = true;
						calData.saveLog(chckbxSaveLogs.isSelected());
					} else if (returnVal == JFileChooser.CANCEL_OPTION) {
						System.out.println("Cancel selected, previously selected path, "+logPath+" still valid.");
					}
				}
			});
			btnLogPath.setVisible(calData.saveLog());
			contentPanel.add(btnLogPath);
		}
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if ( checkboxChanged ) {
						calData.setLogPath(origPath);
					}
					
//					if (neverSelected || !selected) {
//						calData.saveLog(false);
//					} else if ( !neverSelected && selected) {
//						calData.saveLog(true);
//					}
//					System.out.println("Close selected, disposing...");
//					if (!calData.saveLog()) {
//						if(calData.getLogWriter() != null) {
//							try {
//								calData.getLogWriter().flush();
//								calData.getLogWriter().close();
//								calData.setLogWriter(null);
//							} catch (IOException e1) {
//								// Weirdness while closing the logWriter
//								System.out.println("Error encountered while flushing and closing the FileWriter");
//								e1.printStackTrace();
//							}
//						}
//					}
					dispose();
				}
			});
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
			getRootPane().setDefaultButton(cancelButton);
		}

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//System.out.println("OK selected, disposing...");
				if (neverSelected || !selected) {
					calData.saveLog(false);
				} else if ( !neverSelected && selected) {
					calData.saveLog(true);
				}
				calData.setLogPath(logPath);
				
				if (!calData.saveLog()) {
					if(calData.getLogWriter() != null) {
						try {
							calData.getLogWriter().flush();
							calData.getLogWriter().close();
							calData.setLogWriter(null);
						} catch (IOException e1) {
							// Weirdness while closing the logWriter
							System.out.println("Error encountered while flushing and closing the FileWriter");
							e1.printStackTrace();
						}
					}
				}
				System.out.println("OK selected, disposing...");
				dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		
	}

//	/**
//	 * @param owner
//	 */
//	public LogSelection(Frame owner) {
//		super(owner);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 */
//	public LogSelection(Dialog owner) {
//		super(owner);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 */
//	public LogSelection(Window owner) {
//		super(owner);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param modal
//	 */
//	public LogSelection(Frame owner, boolean modal) {
//		super(owner, modal);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 */
//	public LogSelection(Frame owner, String title) {
//		super(owner, title);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param modal
//	 */
//	public LogSelection(Dialog owner, boolean modal) {
//		super(owner, modal);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 */
//	public LogSelection(Dialog owner, String title) {
//		super(owner, title);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param modalityType
//	 */
//	public LogSelection(Window owner, ModalityType modalityType) {
//		super(owner, modalityType);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 */
//	public LogSelection(Window owner, String title) {
//		super(owner, title);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 * @param modal
//	 */
//	public LogSelection(Frame owner, String title, boolean modal) {
//		super(owner, title, modal);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 * @param modal
//	 */
//	public LogSelection(Dialog owner, String title, boolean modal) {
//		super(owner, title, modal);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 * @param modalityType
//	 */
//	public LogSelection(Window owner, String title, ModalityType modalityType) {
//		super(owner, title, modalityType);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 * @param modal
//	 * @param gc
//	 */
//	public LogSelection(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
//		super(owner, title, modal, gc);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 * @param modal
//	 * @param gc
//	 */
//	public LogSelection(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
//		super(owner, title, modal, gc);
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param owner
//	 * @param title
//	 * @param modalityType
//	 * @param gc
//	 */
//	public LogSelection(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
//		super(owner, title, modalityType, gc);
//		// TODO Auto-generated constructor stub
//	}

}
