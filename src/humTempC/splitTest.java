package humTempC;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
import java.util.Calendar;
import java.util.TimeZone;
import com.fazecast.jSerialComm.SerialPort;

public class splitTest {

	private static SerialPort[] ports;

	public splitTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	public static void main(String[] args) {
//		String logPathStr = "/Users/erik/Mjukvara/Eclipse-Java/Application Support/testwriting.txt";
//		File logPath = new File(logPathStr);
		String timeBuffer = "[14/12/2021 13:03:14]";
		Calendar eCal = Calendar.getInstance();
		Double tValue = 32.10, hValue = 65.43;

		System.out.println(Long.toString(eCal.getTime().getTime() / 1000));
		System.out.println(Long.toString(eCal.getTime().getTime()));
		System.out.println(System.currentTimeMillis());
		System.out.println(TimeZone.getTimeZone("Europe/Reykjavik").getOffset(eCal.getTime().getTime()));

		System.out.println("-------------------------------------------------------");
		System.out.println("Proceeding to test requesting a list of serial ports...");
		try {
			ports = SerialPort.getCommPorts();
			System.out.print("Got a list: ");
			System.out.println(ports.length);
			for (int i = 0; i < ports.length; i++) {
				System.out.println(ports[i]);
			}
			System.out.println(System.getProperty("user.home"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		try {
//			FileWriter lfw = new FileWriter(logPath,true);
//			//bfw = new BufferedWriter(lfw);
//			PrintWriter logWriter = new PrintWriter(new BufferedWriter(lfw));
//			System.out.print("Attempting to write to log: ");
//			System.out.println(eCal.getTime().toString()+"\t"+Double.toString(tValue)+" "+Double.toString(hValue));
//			logWriter.println(eCal.getTime().toString()+"\t"+Double.toString(tValue)+" "+Double.toString(hValue));
//			logWriter.flush();
//			logWriter.close();
//		} catch (IOException e) {
//			// Auto-generated catch block
//			System.out.println("Could not create a FileWriter");
//			//e.printStackTrace();
//		}
//		
//		for(String dateSplit : (timeBuffer).split("\\s|/|:|\\[|\\]")) {
//			System.out.println(dateSplit);
//		}
//		timeBuffer = timeBuffer.replaceAll("\\[|\\]","");
//		//timeBuffer = timeBuffer.replaceFirst("\\s","");
//		timeBuffer = timeBuffer.replaceAll("\\s\\s"," ");
//		timeBuffer = timeBuffer.trim();
//		
//		System.out.println(timeBuffer);
//		String[] dateSplit = timeBuffer.split("\\[|\\s|/|:|\\]");
//		System.out.println("dateSplit length: "+dateSplit.length);
//		int ctr = 0;
//		for(int j = 0; j < dateSplit.length; j++) { //String dateSplit : (timeBuffer).split("\\[|\\s|/|:|\\]")
////			System.out.println(j + " ("+ (dateSplit.length-1-j) + ") " + (dateSplit[dateSplit.length-j]));
////			ctr++;
//			if ((dateSplit[j]).equals("")) // (dateSplit[dateSplit.length-j]).equals("")
//				System.out.println(j + " Space");
//			else
//				System.out.println(j + " ("+ (dateSplit.length-1-j) + ") " + (dateSplit[j]));
////				System.out.println(j + " ("+ (dateSplit.length-1-j) + ") " + (dateSplit[dateSplit.length-j]));
//		}

//		for(int j = dateSplit.length-1; j > 0; j--) { //String dateSplit : (timeBuffer).split("\\[|\\s|/|:|\\]")
//			if (!(dateSplit[dateSplit.length-j].equals("")))
//				System.out.println(j + " ("+ (dateSplit.length-1-j) + ") " + (dateSplit[dateSplit.length-j]));
//		}

//		for(int j = 0; j < dateSplit.length; j++) { //String dateSplit : (timeBuffer).split("\\[|\\s|/|:|\\]")
//			if (!(dateSplit[j].equals("")))
//				System.out.println(j + " ("+ (dateSplit.length-1-j) + ") " + (dateSplit[j]));
//		}

	}

}
