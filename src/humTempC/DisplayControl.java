package humTempC;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.Date;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JCheckBox;
//import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerDateModel;
//import javax.swing.JToggleButton;
//import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
//import java.io.IOException;
//import javax.swing.SpinnerModel;

public class DisplayControl extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8703498757450907348L;	
	private final JPanel contentPanel = new JPanel();
	private JTextPane hiPerc;
	private JTextPane miPerc;
	private JTextPane loPerc;
	private JSlider hiSlider;
	private JSlider miSlider;
	private JSlider loSlider;
	private int origLoVal;
	private int origMiVal;
	private int origHiVal; 
	private JCheckBox chckbxUseDaytimeOnly;
//	private JCheckBoxMenuItem menuCheckBox;
	private JSpinner daylight;
	private JSpinner evening;
	private JSpinner nightTime;
	private boolean selOff;
//	private JToggleButton dispOn;
//	private JTextPane timedOut;
//	private JCheckBoxMenuItem chckbxmntmUseDisplayTimeout;

	/**
	 * Create the dialog.
	 */
	
	public DisplayControl(CalibrationData calData) {
		super();
		origLoVal = calData.getLoBr();
		origMiVal = calData.getMiBr();
		origHiVal = calData.getHiBr();
		selOff 	  = calData.isDispOn();//true;
		runDisplayControl(calData);
	}
	
//	public DisplayControl(CalibrationData calData, JToggleButton dispOn, JTextPane timedOut, JCheckBoxMenuItem chckbxmntmUseDisplayTimeout) {
//		super();
//		origLoVal = calData.getLoBr();
//		origMiVal = calData.getMiBr();
//		origHiVal = calData.getHiBr();
//		selOff = dispOn.isSelected();
////		this.dispOn = dispOn;
////		this.timedOut = timedOut;
////		this.chckbxmntmUseDisplayTimeout = chckbxmntmUseDisplayTimeout;
//		runDisplayControl(calData);
//	}
	
	public void runDisplayControl(CalibrationData calData) {
		
//		origLoVal = calData.getLoBr();
//		origMiVal = calData.getMiBr();
//		origHiVal = calData.getHiBr();
//		selOff	  = dispOn.isSelected();
//		menuCheckBox = chckbxmntmUseDisplayTimeout;
		
		setBounds(100, 100, 578, 212);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			hiSlider = new JSlider(0,255,origHiVal);
			hiSlider.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					calData.getComms().sendCommand("\n");
				}
			});
			hiSlider.setToolTipText("Set the daytime brightness");
			hiSlider.setBounds(171, 47, 190, 29);
			hiSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					hiPerc.setText(Math.round(Double.valueOf(hiSlider.getValue())/255*100)+"%");
					//System.out.println(Math.round(Double.valueOf(hiSlider.getValue())/255*100)+"%");
					//System.out.println("l"+hiSlider.getValue()+"\n");
					calData.getComms().sendCommand("h"+hiSlider.getValue()); // +"\n"
				}
			});
			contentPanel.setLayout(null);
			contentPanel.add(hiSlider);
		}

		{
			hiPerc = new JTextPane();
			hiPerc.setEditable(false);
			hiPerc.setBounds(366, 53, 40, 16);
			hiPerc.setBackground(UIManager.getColor("InternalFrame.background"));
			hiPerc.setText(Math.round(Double.valueOf(hiSlider.getValue())/255*100)+"%");
			contentPanel.add(hiPerc);
		}
		
		{
			JTextPane txtpnHighBrightnessLevel = new JTextPane();
			txtpnHighBrightnessLevel.setEditable(false);
			txtpnHighBrightnessLevel.setBounds(24, 53, 140, 16);
			txtpnHighBrightnessLevel.setBackground(UIManager.getColor("InternalFrame.background"));
			txtpnHighBrightnessLevel.setText("Daytime brightness");
			contentPanel.add(txtpnHighBrightnessLevel);

			
		}
		
		{
			JTextPane txtpnMidBrightnessLevel = new JTextPane();
			txtpnMidBrightnessLevel.setEditable(false);
			txtpnMidBrightnessLevel.setBounds(24, 87, 140, 16);
			txtpnMidBrightnessLevel.setBackground(UIManager.getColor("InternalFrame.background"));
			txtpnMidBrightnessLevel.setText("Evening brightness");
			contentPanel.add(txtpnMidBrightnessLevel);
		}
		{
			miSlider = new JSlider(0,255,origMiVal);
			miSlider.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					calData.getComms().sendCommand("\n");
				}
			});
			miSlider.setToolTipText("Set the evening brightness");
			miSlider.setBounds(171, 81, 190, 29);
			miSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					miPerc.setText(Math.round(Double.valueOf(miSlider.getValue())/255*100)+"%");
					//System.out.println(Math.round(Double.valueOf(loSlider.getValue())/255*100)+"%");
					//System.out.println("l"+loSlider.getValue()+"\n");
					calData.getComms().sendCommand("m"+miSlider.getValue()); // +"\n"
				}
			});
			miSlider.setEnabled(!calData.useDayOnly());
			contentPanel.add(miSlider);
		}
		{
			miPerc = new JTextPane();
			miPerc.setEditable(false);
			miPerc.setBounds(366, 87, 40, 16);
			miPerc.setBackground(UIManager.getColor("InternalFrame.background"));
			miPerc.setText(Math.round(Double.valueOf(miSlider.getValue())/255*100)+"%");
			miPerc.setEnabled(!calData.useDayOnly());
			contentPanel.add(miPerc);
		}
		
		int hFh = calData.getHiFromH(); // = 2.5;
//		int hTh = calData.getHiToH();
		int hFm = calData.getHiFromM();
//		int hTm = calData.getHiToM();
		
		int mFh = calData.getMiFromH(); // = 2.5;
//		int mTh = calData.getMiToH();
		int mFm = calData.getMiFromM();
//		int mTm = calData.getMiToM();
		
		int lFh = calData.getLoFromH(); // = 2.5;
//		int lTh = calData.getLoToH();
		int lFm = calData.getLoFromM();
//		int lTm = calData.getLoToM();
		
		
		Calendar dCal = Calendar.getInstance();
		dCal.set(Calendar.HOUR_OF_DAY,hFh);
		dCal.set(Calendar.MINUTE,hFm);
		System.out.println("Setting start time for daylight: " + hFh + ":" + hFm + " (note, leading 0s stripped)");
		System.out.print("dCal.getTime() gives this: ");
		System.out.println(dCal.getTime());
		
		daylight = new JSpinner(new SpinnerDateModel()); // SpinnerNumberModel(hFh,0,23,1)
		//daylight.setEnabled(false);
		daylight.setEditor(new JSpinner.DateEditor(daylight,"HH:mm"));
		daylight.setValue(dCal.getTime());
		daylight.setToolTipText("Start daylight brightness at this time");
		daylight.setBounds(418, 50, 70, 26);
		daylight.setEnabled(!calData.useDayOnly());
		// TODO Implement the scheduling in Arduino before setting to visible.
		daylight.setVisible(false);
		contentPanel.add(daylight);
		
		JTextPane txtpnDisplayDaylight = new JTextPane();
		txtpnDisplayDaylight.setEditable(false);
		txtpnDisplayDaylight.setBackground(UIManager.getColor("InternalFrame.background"));
		txtpnDisplayDaylight.setText("Change to this brightness \nat these times");
		txtpnDisplayDaylight.setBounds(396, 6, 217, 29);
		// TODO Implement the scheduling in Arduino before setting to visible.
		txtpnDisplayDaylight.setVisible(false);
		contentPanel.add(txtpnDisplayDaylight);
		
		chckbxUseDaytimeOnly = new JCheckBox("Daytime brightness only");
		chckbxUseDaytimeOnly.setToolTipText("Set brightness to permanently be held at daytime setting");
		chckbxUseDaytimeOnly.setBounds(171, 12, 190, 23);
		chckbxUseDaytimeOnly.setSelected(calData.useDayOnly());
		chckbxUseDaytimeOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox dto = (JCheckBox) e.getSource();
				System.out.println(dto.isSelected() ? "Use only daytime brightness":"Use schedule");
				daylight.setEnabled(!dto.isSelected());
				evening.setEnabled(!dto.isSelected());
				nightTime.setEnabled(!dto.isSelected());
				miSlider.setEnabled(!dto.isSelected());
				miPerc.setEnabled(!dto.isSelected());
				loSlider.setEnabled(!dto.isSelected());
				loPerc.setEnabled(!dto.isSelected());
				calData.setDayOnly(dto.isSelected());
				if (dto.isSelected())
					calData.getComms().sendCommand("d2\n");
				else
					calData.getComms().sendCommand("d3\n");
				// Potentially send command here as well. Think about it
			}
		});
//		chckbxUseDaytimeOnly.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				//loPerc.setText(Math.round(Double.valueOf(loSlider.getValue())/255*100)+"%");
//				//System.out.println(e.get"");
//				//System.out.println("l"+loSlider.getValue()+"\n");
//				calData.getComms().sendCommand("l"+loSlider.getValue()+"\n");
//			}
//		});
		contentPanel.add(chckbxUseDaytimeOnly);
		
		JTextPane txtpnLowBrightnessLevel = new JTextPane();
		txtpnLowBrightnessLevel.setText("Nighttime brightness");
		txtpnLowBrightnessLevel.setEditable(false);
		txtpnLowBrightnessLevel.setBackground(SystemColor.window);
		txtpnLowBrightnessLevel.setBounds(24, 121, 140, 16);
		contentPanel.add(txtpnLowBrightnessLevel);
		
		loSlider = new JSlider(0, 255, origLoVal);
		loSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				calData.getComms().sendCommand("\n");
			}
		});
		loSlider.setToolTipText("Set the nighttime brightness");
		loSlider.setBounds(171, 115, 190, 29);
		loSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				loPerc.setText(Math.round(Double.valueOf(loSlider.getValue())/255*100)+"%");
				//System.out.println(Math.round(Double.valueOf(loSlider.getValue())/255*100)+"%");
				//System.out.println("l"+loSlider.getValue()+"\n");
				calData.getComms().sendCommand("l"+loSlider.getValue()); // +"\n"
			}
		});
		loSlider.setEnabled(!calData.useDayOnly());
		System.out.println("loSlider enabled "+loSlider.isEnabled());
		contentPanel.add(loSlider);
		
		loPerc = new JTextPane();
		loPerc.setText(Math.round(Double.valueOf(loSlider.getValue())/255*100)+"%");
		loPerc.setEditable(false);
		loPerc.setBackground(SystemColor.window);
		loPerc.setBounds(366, 121, 40, 16);
		loPerc.setEnabled(!calData.useDayOnly());
		contentPanel.add(loPerc);
		
		evening = new JSpinner(new SpinnerDateModel()); // SpinnerNumberModel(hFh,0,23,1)
		evening.setEditor(new JSpinner.DateEditor(evening,"HH:mm"));
		Calendar eCal = Calendar.getInstance();
		System.out.println("Evening spinner has this time delivered: "+mFh+":"+mFm);
		System.out.println(eCal.getTime());
		eCal.set(Calendar.HOUR_OF_DAY,mFh);
		eCal.set(Calendar.MINUTE,mFm);
		System.out.println(eCal.getTime());
		evening.setValue(eCal.getTime());
		evening.setToolTipText("Start evening brightness at this time");
		evening.setBounds(418, 84, 70, 26);
		evening.setEnabled(!calData.useDayOnly());
		// TODO Implement the scheduling in Arduino before setting to visible.
		evening.setVisible(false);
		contentPanel.add(evening);
		
		nightTime = new JSpinner(new SpinnerDateModel());
		nightTime.setEditor(new JSpinner.DateEditor(nightTime,"HH:mm"));
		Calendar nCal = Calendar.getInstance();
		nCal.set(Calendar.HOUR_OF_DAY,lFh);
		nCal.set(Calendar.MINUTE,lFm);
		nightTime.setValue(nCal.getTime());
		nightTime.setToolTipText("Start night brightness at this time");
		nightTime.setBounds(418, 115, 70, 26);
		nightTime.setEnabled(!calData.useDayOnly());
		// TODO Implement the scheduling in Arduino before setting to visible.
		nightTime.setVisible(false);
		contentPanel.add(nightTime);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(!chckbxUseDaytimeOnly.isSelected()) {
							
							System.out.println("Daytime only selected: "+chckbxUseDaytimeOnly.isSelected());
							calData.setDayOnly(chckbxUseDaytimeOnly.isSelected());
							System.out.print("Daytime start time (full String): ");
							String dSstr = daylight.getValue().toString();
							System.out.println(dSstr);
							System.out.print("Daytime start time (time String): ");
							System.out.println(dSstr.substring(11,16));
							//* Daytime settings subdivided into hours and minutes *//
							int testint = Integer.parseInt(dSstr.substring(11,13));
							System.out.print(testint);
							testint = Integer.parseInt(dSstr.substring(14,16));
							System.out.println(":"+testint);
							calData.setHiFromH(Integer.parseInt(dSstr.substring(11,13)));
							calData.setHiFromM(Integer.parseInt(dSstr.substring(14,16)));
							calData.setLoToH(Integer.parseInt(dSstr.substring(11,13)));
							calData.setLoToM(Integer.parseInt(dSstr.substring(14,16)));

							//* Evening settings subdivided into hours and minutes *//
							String eSstr = evening.getValue().toString();
							calData.setMiFromH(Integer.parseInt(eSstr.substring(11,13)));
							calData.setMiFromM(Integer.parseInt(eSstr.substring(14,16)));
							calData.setHiToH(Integer.parseInt(eSstr.substring(11,13)));
							calData.setHiToM(Integer.parseInt(eSstr.substring(14,16)));

							//* Night settings subdivided into hours and minutes *//
							String nSstr = nightTime.getValue().toString();
							calData.setLoFromH(Integer.parseInt(nSstr.substring(11,13)));
							calData.setLoFromM(Integer.parseInt(nSstr.substring(14,16)));
							calData.setMiToH(Integer.parseInt(nSstr.substring(11,13)));
							calData.setMiToM(Integer.parseInt(nSstr.substring(14,16)));
							
							System.out.println("OK selected, final commands sent:");
							System.out.print("h"+Integer.toString(hiSlider.getValue())+" ");
							calData.setHiBr(hiSlider.getValue());						
							try {
								calData.getComms().sendCommand("h"+hiSlider.getValue()+"\n");
								Thread.sleep(450);
							} catch (InterruptedException e1) {
								System.out.println("Interrupted while trying to sleep for a while");
								e1.printStackTrace();
							}
							System.out.print("m"+Integer.toString(miSlider.getValue())+" ");
							calData.setMiBr(miSlider.getValue());
							try {
								calData.getComms().sendCommand("m"+miSlider.getValue()+"\n");
								Thread.sleep(450);
							} catch (InterruptedException e1) {
								System.out.println("Interrupted while trying to sleep for a while");
								e1.printStackTrace();
							}
							System.out.print("l"+Integer.toString(loSlider.getValue())+"\n");
							calData.setLoBr(loSlider.getValue());
							try {
								calData.getComms().sendCommand("l"+loSlider.getValue()+"\n");
								Thread.sleep(450);
							} catch (InterruptedException e1) {
								System.out.println("Interrupted while trying to sleep for a while");
								e1.printStackTrace();
							}
							
							if (selOff) {
								System.out.println("Entered selOff check on Display menu OK");
								System.out.println("selOff == "+selOff);
								calData.getComms().sendCommand("d1\n");
							} else {
								System.out.println("Entered selOff check on Display menu OK");
								System.out.println("selOff == "+selOff);
								calData.getComms().sendCommand("d0\n");
							}

//							* These settings moved up to be performed before communication section  *
//							* to enable sending of scheduling times together with intensity values. *
//							
//							System.out.println("Daytime only selected: "+chckbxUseDaytimeOnly.isSelected());
//							calData.setDayOnly(chckbxUseDaytimeOnly.isSelected());
//							System.out.print("Daytime start time (full String): ");
//							String dSstr = daylight.getValue().toString();
//							System.out.println(dSstr);
//							System.out.print("Daytime start time (time String): ");
//							System.out.println(dSstr.substring(11,16));
//							//* Daytime settings subdivided into hours and minutes *//
//							int testint = Integer.parseInt(dSstr.substring(11,13));
//							System.out.print(testint);
//							testint = Integer.parseInt(dSstr.substring(14,16));
//							System.out.println(":"+testint);
//							calData.setHiFromH(Integer.parseInt(dSstr.substring(11,13)));
//							calData.setHiFromM(Integer.parseInt(dSstr.substring(14,16)));
//							calData.setLoToH(Integer.parseInt(dSstr.substring(11,13)));
//							calData.setLoToM(Integer.parseInt(dSstr.substring(14,16)));
//
//							//* Evening settings subdivided into hours and minutes *//
//							String eSstr = evening.getValue().toString();
//							calData.setMiFromH(Integer.parseInt(eSstr.substring(11,13)));
//							calData.setMiFromM(Integer.parseInt(eSstr.substring(14,16)));
//							calData.setHiToH(Integer.parseInt(eSstr.substring(11,13)));
//							calData.setHiToM(Integer.parseInt(eSstr.substring(14,16)));
//
//							//* Night settings subdivided into hours and minutes *//
//							String nSstr = nightTime.getValue().toString();
//							calData.setLoFromH(Integer.parseInt(nSstr.substring(11,13)));
//							calData.setLoFromM(Integer.parseInt(nSstr.substring(14,16)));
//							calData.setMiToH(Integer.parseInt(nSstr.substring(11,13)));
//							calData.setMiToM(Integer.parseInt(nSstr.substring(14,16)));

							//System.out.println(dSstr.substring(11,12));
							//*System.out.println(dSstr.substring(11,13));
							//*System.out.println(dSstr.substring(14,16));
							//System.out.println(dSstr.substring(15,16));
							//* This bit below could be implemented elsewhere – confirm effectiveness. *// 
							//						if (chckbxUseDaytimeOnly.isSelected()) {
							//							System.out.print("Command sent: ");
							//							System.out.println("t"+Math.round((double)daylight.getValue()*1000));
							//							calData.setTimeout(Math.round((double)daylight.getValue()*1000));
							//							calData.getComms().sendCommand("t"+Math.round((double)daylight.getValue()*1000)+"\n");
							//							calData.setOldTo(Math.round((double)daylight.getValue()*1000));
							//						} else {
							//							calData.setTimeout(0);
							//							calData.getComms().sendCommand("t0\n");
							//						}
							//						if (selOff) {
							//							System.out.println("Entered selOff check on Display menu OK");
							//							System.out.println("selOff == "+selOff);
							//							calData.getComms().sendCommand("d0\n");
							//						}
							//						System.out.println("--------");
							//						System.out.println("Testing calData");
							//						System.out.println("Timeout set: "+calData.useTimeout());
							//						System.out.println("Set timeout: "+calData.getTimeout());
						}
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(!chckbxUseDaytimeOnly.isSelected()) {
							try {
								calData.getComms().sendCommand("h"+Integer.toString(origHiVal)+"\n");
								Thread.sleep(450);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								System.out.println("Interrupted while trying to sleep for a while");
								e1.printStackTrace();
							}
							try {
								calData.getComms().sendCommand("m"+Integer.toString(origMiVal)+"\n");
								Thread.sleep(450);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								System.out.println("Interrupted while trying to sleep for a while");
								e1.printStackTrace();
							}
							try {
								calData.getComms().sendCommand("l"+Integer.toString(origLoVal)+"\n");
								Thread.sleep(450);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								System.out.println("Interrupted while trying to sleep for a while");
								e1.printStackTrace();
							}
							if (selOff) {
								System.out.println("Entered selOff check on Display menu OK");
								System.out.println("selOff == "+selOff);
								calData.getComms().sendCommand("d1\n");
							} else {
								System.out.println("Entered selOff check on Display menu OK");
								System.out.println("selOff == "+selOff);
								calData.getComms().sendCommand("d0\n");
							}
							System.out.println("Cancel selected, reverting to original values with commands:");
							System.out.print("h"+Integer.toString(origHiVal)+" ");
							System.out.print("m"+Integer.toString(origMiVal)+" ");
							System.out.print("l"+Integer.toString(origLoVal)+"\n");							
						}
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
