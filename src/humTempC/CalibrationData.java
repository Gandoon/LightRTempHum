package humTempC;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

/**
 * A container holding most data needed to be carried between components of the control interface. <br>
 * It keeps track of current time and illumination option, as well as a list of the corresponding switching times.<br>
 * It further keeps track of the display state and log file paths. <br>
 * Finally, it also contains a communicator object, for interaction with an Arduino
 * @author Erik Hedlund
 *
 */

public class CalibrationData {
	// Position data
//	private int currPos;
//	private int gotoPos;
//	private int selectedPos;
	//private final double[] NDval = {0.04, 0.5, 1.0, 1.5, 2.0, 2.5};
	
	// A container for the communicator object
	private Communications comms;
	
	// A container for the log file writer
	private FileWriter logWriter;
	
	// Settings for display
	private int loBr;
	private int miBr;
	private int hiBr;
	private boolean dayOnly;
	//private long timeout;
	//private long oldTimeout;
	private boolean dispOn;
	private boolean saveLog;
	private boolean timeAutoSet;
	private int hFh;
	private int hTh;
	private int hFm;
	private int hTm;
	private int mFh;
	private int mTh;
	private int mFm;
	private int mTm;
	private int lFh;
	private int lTh;
	private int lFm;
	private int lTm;
	
	// Settings for automatic updating of RTC
	private int timeChInt;	// How often the test for RTC drift should be performed (in seconds)
	private int aDrift;		// Allowed minor drift before a set event will occur. Defaults to 29 seconds.
	
	// Locally stored preferences object
	private Properties prefs; 
	private File logPath;
	
	/**
	 * Create the CalibrationData container without a Communications object set
	 */
	public CalibrationData() {
//		currPos = 0;
//		setGotoPos(0);
		logPath = null;
		logWriter = null;
		setComms(null);
		//NDval[6] = {0.04, 0.5, 1.0, 1.5, 2.0, 2.5}; //new double[6]; 
		// Set standard values for brightness
		setLoBr(7);
		setMiBr(31);
		setHiBr(127);
		setDispOn(true);
		setDayOnly(false);
		setHiFromH(9);
		setHiToH(0);
		setHiFromM(0);
		setHiToM(0);
		setMiFromH(19);
		setMiToH(0);
		setMiFromM(0);
		setMiToM(0);
		setLoFromH(23);
		setLoToH(0);
		setLoFromM(0);
		setLoToM(0);
		timeCheckInterval(3600);
		allowedDrift(29);
		}

	/**
	 * @param Create the CalibrationData container with a Communications object set
	 */
	public CalibrationData(Communications comms) {
//		currPos = 0;
//		setGotoPos(0);
		logPath = null;
		logWriter = null;
		this.setComms(comms);
		// Set standard values for brightness
		setLoBr(7);
		setMiBr(31);
		setHiBr(127);
		setDispOn(true);
		setDayOnly(false);
		setHiFromH(9);
		setHiToH(0);
		setHiFromM(0);
		setHiToM(0);
		setMiFromH(19);
		setMiToH(0);
		setMiFromM(0);
		setMiToM(0);
		setLoFromH(23);
		setLoToH(0);
		setLoFromM(0);
		setLoToM(0);
		timeCheckInterval(3600);
		allowedDrift(29);
		//logPath = new File(); // Can not take an empty constructor
	}

	/**
	 * @return the Communications object
	 */
	public Communications getComms() {
		return comms;
	}

	/**
	 * @param comms the Communications object to set
	 */
	public void setComms(Communications comms) {
		this.comms = comms;
	}

//	/**
//	 * @return the currPos
//	 */
//	public int getCurrPos() {
//		return currPos;
//	}
//
//	/**
//	 * @param currPos the currPos to set
//	 */
//	public void setCurrPos(int currPos) {
//		this.currPos = currPos;
//	}
//	
//	/**
//	 * @return the gotoPos
//	 */
//	public int getGotoPos() {
//		return gotoPos;
//	}
//
//	/**
//	 * @param gotoPos the gotoPos to set
//	 */
//	public void setGotoPos(int gotoPos) {
//		this.gotoPos = gotoPos;
//	}
//
//	/**
//	 * @return the NDVal
//	 */
//	public double getNDVal() {
//		return NDval[currPos];
//	}
//
//	/**
//	 * @return the selectedPos
//	 */
//	public int getSelectedPos() {
//		return selectedPos;
//	}
//
//	/**
//	 * @param selectedPos the selectedPos to set
//	 */
//	public void setSelectedPos(int selectedPos) {
//		this.selectedPos = selectedPos;
//	}
//	
//	public void commitPos() {
//		currPos = selectedPos;
//	}

	/**
	 * @return the loBr
	 */
	public int getLoBr() {
		return loBr;
	}

	/**
	 * @param loBr the loBr to set
	 */
	public void setLoBr(int loBr) {
		this.loBr = loBr;
	}

	/**
	 * @return the miBr
	 */
	public int getMiBr() {
		return miBr;
	}
	
	/**
	 * @param miBr the miBr to set
	 */
	public void setMiBr(int miBr) {
		this.miBr = miBr;
		
	}
	
	/**
	 * @return the hiBr
	 */
	public int getHiBr() {
		return hiBr;
	}

	/**
	 * @param hiBr the hiBr to set
	 */
	public void setHiBr(int hiBr) {
		this.hiBr = hiBr;
	}

	/**
	 * @return the dispOn
	 */
	public boolean isDispOn() {
		return dispOn;
	}
	
	/**
	 * @param dispOn the dispOn to set
	 */
	public void setDispOn(boolean dispOn) {
		this.dispOn = dispOn;
	}
	
	// *************** //
	// High brightness //
	// *************** //
	
	/**
	 * @param hFh set the hour to start daylight setting 
	 */
	public void setHiFromH(int hFh) {
		this.hFh = hFh;
	}
	
	/**
	 * @return the hour to start daylight setting 
	 */
	public int getHiFromH() {
		return hFh;
	}
	
	/**
	 * @param hTh set the hour to end daylight setting 
	 */
	public void setHiToH(int hTh) {
		this.hTh = hTh;
	}
	
	/**
	 * @return the hour to end daylight setting 
	 */
	public int getHiToH() {
		return hTh;
	}
	
	// ****** //
	
	/**
	 * @param hFm set the minute to start daylight setting 
	 */
	public void setHiFromM(int hFm) {
		this.hFm = hFm;
	}
	
	/**
	 * @return the minute to start daylight setting 
	 */
	public int getHiFromM() {
		return hFm;
	}
	
	/**
	 * @param hTm set the minute to end daylight setting 
	 */
	public void setHiToM(int hTm) {
		this.hTm = hTm;
	}
	
	/**
	 * @return the minute to end daylight setting 
	 */
	public int getHiToM() {
		return hTm;
	}
	
	// ***************** //
	// Medium brightness //
	// ***************** //
	
	/**
	 * @param mFh set the hour to start evening setting 
	 */
	public void setMiFromH(int mFh) {
		this.mFh = mFh;
	}
	
	/**
	 * @return the hour to start evening setting 
	 */
	public int getMiFromH() {
		return mFh;
	}
	
	/**
	 * @param mTh set the hour to end evening setting 
	 */
	public void setMiToH(int mTh) {
		this.mTh = mTh;
	}
	
	/**
	 * @return the hour to end daylight setting 
	 */
	public int getMiToH() {
		return mTh;
	}
	
	// ****** //
	
	/**
	 * @param mFm set the minute to start evening setting 
	 */
	public void setMiFromM(int mFm) {
		this.mFm = mFm;
	}
	
	/**
	 * @return the minute to start evening setting 
	 */
	public int getMiFromM() {
		return mFm;
	}
	
	/**
	 * @param mTm set the minute to end evening setting 
	 */
	public void setMiToM(int mTm) {
		this.mTm = mTm;
	}
	
	/**
	 * @return the minute to end evening setting 
	 */
	public int getMiToM() {
		return mTm;
	}
	
	// ************** //
	// Low brightness //
	// ************** //

	/**
	 * @param lFh set the hour to start night setting 
	 */
	public void setLoFromH(int lFh) {
		this.lFh = lFh;
	}
	
	/**
	 * @return the hour to start night setting 
	 */
	public int getLoFromH() {
		return lFh;
	}
	
	/**
	 * @param lTh set the hour to end night setting 
	 */
	public void setLoToH(int lTh) {
		this.lTh = lTh;
	}
	
	/**
	 * @return the hour to end night setting 
	 */
	public int getLoToH() {
		return lTh;
	}
	
	// ****** //
	
	/**
	 * @param lFm set the minute to start night setting 
	 */
	public void setLoFromM(int lFm) {
		this.lFm = lFm;
	}
	
	/**
	 * @return the hour to start night setting 
	 */
	public int getLoFromM() {
		return lFm;
	}
	
	/**
	 * @param lTm set the minute to end night setting 
	 */
	public void setLoToM(int lTm) {
		this.lTm = lTm;
	}
	
	/**
	 * @return the minute to end night setting 
	 */
	public int getLoToM() {
		return lTm;
	}
	
	/**
	 * @return Should only daytime brightness be used? 
	 */
	public boolean useDayOnly() {
		return dayOnly;
	}
	
	/**
	 * @param lTm set the minute to end night setting 
	 */
	public void setDayOnly(boolean dayOnly) {
		this.dayOnly = dayOnly;
	}
	
	/**
	 * @param logPath set the path to the log file 
	 */
	public void setLogPath(File logPath) {
		this.logPath = logPath;
	}
	
	/**
	 * @return the path to the log file 
	 */
	public File getLogPath() {
		return logPath;
	}
	
	/**
	 * @param logPath set the path to the log file 
	 */
	public void saveLog(boolean saveLog) {
		this.saveLog = saveLog;
	}
	
	/**
	 * @return the path to the log file 
	 */
	public boolean saveLog() {
		return saveLog;
	}
	
	// ****** //
	
	/**
	 * @return is time to be automatically set?
	 */
	public boolean timeAutoSet() {
		return timeAutoSet;
	}

	/**
	 * @param timeAutoSet set if time is to be automatically set
	 */
	public void timeAutoSet(boolean timeAutoSet) {
		this.timeAutoSet = timeAutoSet;
	}
	
	/**
	 * @return how often the automatic RTC precision check should be performed, in seconds.
	 */
	public int timeCheckInterval() {
		return(timeChInt);
	}
	
	/**
	 * @param timeChInt : How often the automatic RTC precision check should be performed, in seconds. Defaults to one hour (3600 s).
	 */
	public void timeCheckInterval(int timeChInt) {
		this.timeChInt = timeChInt;
	}
	
	/**
	 * @return the allowed minor drift before a set event will occur.
	 */
	public int allowedDrift() {
		return(aDrift);
	}
	
	/**
	 * @param aDrift : Allowed minor drift before a set event will occur. Defaults to 29 seconds.
	 */
	public void allowedDrift(int aDrift) {
		this.aDrift = aDrift;
	}

	/**
	 * @return the logWriter
	 */
	public FileWriter getLogWriter() {
		return logWriter;
	}

	/**
	 * @param logWriter the logWriter to set
	 */
	public void setLogWriter(FileWriter logWriter) {
		this.logWriter = logWriter;
	}

	public void loadPrefs(Properties loadedPrefs, Properties defaults) {
		prefs = loadedPrefs;
		loBr    = Integer.parseInt(prefs.getProperty("loBr", defaults.getProperty("loBr", "7"))); // prefs.getProperty("loBr", "31")); // 
		miBr    = Integer.parseInt(prefs.getProperty("miBr", defaults.getProperty("miBr", "31")));
		hiBr    = Integer.parseInt(prefs.getProperty("hiBr", defaults.getProperty("hiBr", "127")));
//		currPos = Integer.parseInt(prefs.getProperty("currPos", defaults.getProperty("currPos", "0")));
//		gotoPos = currPos;
		//timeout = Long.parseLong(prefs.getProperty("timeout", defaults.getProperty("timeout", "0")));
		dispOn   = Boolean.parseBoolean(prefs.getProperty("dispOn", defaults.getProperty("dispOn", "true")));
		dayOnly  = Boolean.parseBoolean(prefs.getProperty("dayOnly", defaults.getProperty("dayOnly", "true")));
		//* Daytime settings *//
		hFh = Integer.parseInt(prefs.getProperty("hFh", defaults.getProperty("hFh", "09")));
		hFm = Integer.parseInt(prefs.getProperty("hFm", defaults.getProperty("hFm", "00")));
		System.out.println("Daytime read from file: " + hFh + ":" + hFm);
		hTh = Integer.parseInt(prefs.getProperty("hTh", defaults.getProperty("hTh", "19")));
		hTm = Integer.parseInt(prefs.getProperty("hTm", defaults.getProperty("hTm", "00")));
		//* Evening settings *//
		mFh = Integer.parseInt(prefs.getProperty("mFh", defaults.getProperty("mFh", "19")));
		mFm = Integer.parseInt(prefs.getProperty("mFm", defaults.getProperty("mFm", "00")));
		System.out.println("Evening read from file: " + mFh + ":" + mFm);
		mTh = Integer.parseInt(prefs.getProperty("mTh", defaults.getProperty("mTh", "22")));
		mTm = Integer.parseInt(prefs.getProperty("mTm", defaults.getProperty("mTm", "00")));
		//* Night settings *//
		lFh = Integer.parseInt(prefs.getProperty("lFh", defaults.getProperty("lFh", "22")));
		lFm = Integer.parseInt(prefs.getProperty("lFm", defaults.getProperty("lFm", "00")));
		System.out.println("Night time read from file: " + lFh + ":" + lFm);
		lTh = Integer.parseInt(prefs.getProperty("lTh", defaults.getProperty("lTh", "09")));
		lTm = Integer.parseInt(prefs.getProperty("lTm", defaults.getProperty("lTm", "00")));
		timeAutoSet = Boolean.parseBoolean(prefs.getProperty("timeAutoSet",defaults.getProperty("timeAutoSet","false")));
		timeChInt = Integer.parseInt(prefs.getProperty("timeChInt", defaults.getProperty("timeChInt", "60")));
		aDrift  = Integer.parseInt(prefs.getProperty("aDrift", defaults.getProperty("aDrift", "29")));
		saveLog = Boolean.parseBoolean(prefs.getProperty("saveLog",defaults.getProperty("saveLog","false")));
		logPath = new File(prefs.getProperty("logPath", defaults.getProperty("logPath", "")));
		
	}
	
	public void commitPrefs() {
		// TODO implement in Arduino software to accept input before sending commands
		// Update information on the Arduino 
		System.out.print("Comms configured (in calData): ");
		System.out.println(comms.isConfigured());

		String dispCmd;
		if (dispOn) {
			dispCmd = "d1\n";
		} else {
			dispCmd = "d0\n";
		}
		System.out.print("l"+prefs.getProperty("loBr", "7")+"\n" +
				"m"+prefs.getProperty("miBr", "31")+"\n" +
				"h"+prefs.getProperty("hiBr", "127")+"\n" + dispCmd);
//		comms.sendCommand("l"+prefs.getProperty("loBr", "31")+"\n" +
//				"h"+prefs.getProperty("hiBr",  "127")+"\n" +
//				"s"+prefs.getProperty("currPos", "0")+"\n" +
//				dispCmd + "t"+prefs.getProperty("timeout", "0")+"\n");
		
		
	}
	
	public void storePrefs(Properties prefs) {
		// Note the prefs may not be correctly stored
		this.prefs = prefs; //??
//		prefs.setProperty("currPos", Integer.toString(currPos));
		prefs.setProperty("loBr", Integer.toString(loBr));
		prefs.setProperty("miBr", Integer.toString(miBr));
		prefs.setProperty("hiBr", Integer.toString(hiBr));
		//prefs.setProperty("timeout", Long.toString(timeout));
		prefs.setProperty("dispOn", Boolean.toString(dispOn));
		prefs.setProperty("dayOnly", Boolean.toString(dayOnly));
		//* Daytime settings *//
		prefs.setProperty("hFh", Integer.toString(hFh));
		prefs.setProperty("hFm", Integer.toString(hFm));
		prefs.setProperty("hTh", Integer.toString(hTh));
		prefs.setProperty("hTm", Integer.toString(hTm));
		//* Evening settings *//
		prefs.setProperty("mFh", Integer.toString(mFh));
		prefs.setProperty("mFm", Integer.toString(mFm));
		prefs.setProperty("mTh", Integer.toString(mTh));
		prefs.setProperty("mTm", Integer.toString(mTm));
		//* Night settings *//
		prefs.setProperty("lFh", Integer.toString(lFh));
		prefs.setProperty("lFm", Integer.toString(lFm));
		prefs.setProperty("lTh", Integer.toString(lTh));
		prefs.setProperty("lTm", Integer.toString(lTm));	
		prefs.setProperty("saveLog", Boolean.toString(saveLog));
		prefs.setProperty("timeAutoSet", Boolean.toString(timeAutoSet));
		prefs.setProperty("aDrift", Integer.toString(aDrift));
		prefs.setProperty("timeChInt", Integer.toString(timeChInt));
		if (saveLog) {
			if (logPath != null) { 
				prefs.setProperty("logPath",logPath.toString());
				System.out.println("logPath as stored: "+logPath.toString());
			} else {
				prefs.setProperty("logPath","");
				System.out.println("logPath empty. Probably shouldn't be able to happen");
			}
		}
	}
}
