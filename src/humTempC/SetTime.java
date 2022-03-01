package humTempC;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JSeparator;

public class SetTime extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5929235661206170580L;
	private final JPanel contentPanel = new JPanel();
	//private CalibrationData calData; // May be needed later, if methods other than the constructor are added.
	private JLabel lblManSet;
	private JButton btnManSet, btnCurSet;
	private JSpinner manDate;
	private JCheckBox autoSet;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SetTime dialog = new SetTime(new CalibrationData());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SetTime(CalibrationData calData) {
		//this.calData = calData;
		setBounds(100, 100, 450, 150);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			autoSet = new JCheckBox("Set time automatically");
			autoSet.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JCheckBox sAut = (JCheckBox) e.getSource();
					if (sAut.isSelected()) {
						lblManSet.setEnabled(false);
						btnManSet.setEnabled(false);
						btnCurSet.setEnabled(false);
						manDate.setEnabled(false);
						//calData.timeAutoSet(true);
					} else {
						lblManSet.setEnabled(true);
						btnManSet.setEnabled(true);
						btnCurSet.setEnabled(true);
						manDate.setEnabled(true);
						//calData.timeAutoSet(false);
					}
				}
			});
			autoSet.setSelected(calData.timeAutoSet());
			autoSet.setEnabled(false);
			// TODO Enable this when the function has been implemented.
			autoSet.setToolTipText("Automatically update the Arduino RTC every hour if its \ntime deviates from local time by more than 5 seconds.\n(Currently not implemented)");
			// At some point it may be beneficial to be able to set how large an offset is acceptable, and maybe how often it is tested
			GridBagConstraints gbc_autoSet = new GridBagConstraints();
			gbc_autoSet.gridwidth = 2;
			gbc_autoSet.insets = new Insets(0, 0, 5, 5);
			gbc_autoSet.gridx = 0;
			gbc_autoSet.gridy = 0;
			contentPanel.add(autoSet, gbc_autoSet);
		}
		{
			btnCurSet = new JButton("Set current time");
			btnCurSet.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Calendar now = Calendar.getInstance();
					System.out.println(now.getTime());
//					System.out.println("Day: "+now.get(Calendar.DAY_OF_MONTH));
//					System.out.println("Month: "+(now.get(Calendar.MONTH)+1));
//					System.out.println("Year: "+now.get(Calendar.YEAR));
//					System.out.println("Hour: "+now.get(Calendar.HOUR_OF_DAY));
//					System.out.println("Minute: "+now.get(Calendar.MINUTE));
//					System.out.println("Second: "+now.get(Calendar.SECOND));
					System.out.println("Sending command: "+"s"+Long.toString(now.getTime().getTime()));
//					System.out.println("Integer.MAX_VALUE command: "+"s"+Long.toString(Integer.MAX_VALUE));
					Communications comms = calData.getComms();
					comms.sendCommand("s");// + Integer.MAX_VALUE);//Long.toString(now.getTime().getTime()));
					comms.sendLong((now.getTime().getTime()+TimeZone.getDefault().getOffset(now.getTime().getTime()))/1000+1); 
					// Add the time zone offset
					//now.getTime().getTime());//Long.toString(now.getTime().getTime()));
					//comms.sendCommand("\n");
					//System.out.println(now.toString());
				}
			});
			btnCurSet.setToolTipText("Set Arduino RTC to current local time");
			GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
			gbc_btnNewButton.anchor = GridBagConstraints.EAST;
			gbc_btnNewButton.gridwidth = 2;
			gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
			gbc_btnNewButton.gridx = 2;
			gbc_btnNewButton.gridy = 0;
			contentPanel.add(btnCurSet, gbc_btnNewButton);
		}
		{
			JSeparator separator = new JSeparator();
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.gridwidth = 4;
			gbc_separator.insets = new Insets(0, 0, 5, 5);
			gbc_separator.gridx = 0;
			gbc_separator.gridy = 1;
			contentPanel.add(separator, gbc_separator);
		}
		{
			lblManSet = new JLabel("Set time manually");
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.gridwidth = 2;
			gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 2;
			contentPanel.add(lblManSet, gbc_lblNewLabel);
		}
		{
			manDate = new JSpinner(new SpinnerDateModel());
			GridBagConstraints gbc_manDate = new GridBagConstraints();
			gbc_manDate.insets = new Insets(0, 0, 0, 5);
			gbc_manDate.fill = GridBagConstraints.HORIZONTAL;
			gbc_manDate.gridx = 2;
			gbc_manDate.gridy = 2;
			contentPanel.add(manDate, gbc_manDate);
		}
		{
			btnManSet = new JButton("Set time");
			btnManSet.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//DONE Dig out the selected date and time and send it
					Date manSetDate = (Date) manDate.getValue();
					System.out.println("Manual time to set: "+manSetDate.getTime());
					System.out.println("Sending command: "+"s"+
					(manSetDate.getTime()+TimeZone.getDefault().getOffset(manSetDate.getTime()))/1000+1);
//					System.out.println("Integer.MAX_VALUE command: "+"s"+Long.toString(Integer.MAX_VALUE));
					Communications comms = calData.getComms();
					comms.sendCommand("s");// + Integer.MAX_VALUE);//Long.toString(now.getTime().getTime()));
					comms.sendLong((manSetDate.getTime()+TimeZone.getDefault().getOffset(manSetDate.getTime()))/1000+1); 
					
				}
			});
			btnManSet.setToolTipText("Set Arduion RTC to manually selected date and time");
			GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
			gbc_btnNewButton_1.gridx = 3;
			gbc_btnNewButton_1.gridy = 2;
			contentPanel.add(btnManSet, gbc_btnNewButton_1);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ( autoSet.isSelected() ) {
							calData.timeAutoSet(autoSet.isSelected());
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
//						if ( autoSet.isSelected() ) {
//							autoSet.setSelected(false);
//						}
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
