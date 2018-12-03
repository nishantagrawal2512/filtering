/*
 * TITLE: Filtering Utility
 * DESCRIPTION: Reads the input I017 and I018 files. Reads the input exclusion list containing MSNs.
 * Where an MSN from I017/I018 is present in the exclusion list, removes the corresponding meterPoint tag from the input file.
 * -------------------------------------------
 * VERSION HISTORY
 * -------------------------------------------
 * VERSION: 0.1
 * DATE: 20-May-2018
 * DESCRIPTION: Revised logic after performance dip in EIP production system.
 * -------------------------------------------
 * 
 */

package filtering_utility;

import java.io.*;
import java.text.*;
import java.util.*;

public class FilteringUtility {
	static BufferedWriter outI017 = null;
	static BufferedWriter outI018 = null;
	static NumberFormat formatter = new DecimalFormat("#0.00000");
	static FileOutputStream logFile;
	static String fileDelimiter = "\r\n";
	static String i017MSNStartTag = "<meterSerialNo>";
	static String i017MSNEndTag = "</meterSerialNo>";
	static String i018MSNStartTag = "<meterSerialNo>";
	static String i018MSNEndTag = "</meterSerialNo>";
	static String logFilePath = getLogFilePath();
	static String exclusionListPath = getExclusionListPath();
	static String exclusionListFile = getExclusionListFile();
	static String i017SourcePath = getI017SourcePath();
	static String i017TargetPath = getI017TargetPath();
	static String i018SourcePath = getI018SourcePath();
	static String i018TargetPath = getI018TargetPath();

	public static void main(String[] args) throws SecurityException,
			IOException {
		// TODO Auto-generated method stub
		long startOfProgram = System.currentTimeMillis();

		// Read configuration from config.properties file
		Properties prop = new Properties();
		InputStream input = null;
		try {
			//input = new FileInputStream("C:\\FilterUtility\\config.properties");
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);
			// get the property values and set them
			setLogFilePath(prop.getProperty("logFilePath"));
			setExclusionListFile(prop.getProperty("exclusionListFile"));
			setExclusionListPath(prop.getProperty("exclusionListPath"));
			setI017SourcePath(prop.getProperty("i017SourcePath"));
			setI017TargetPath(prop.getProperty("i017TargetPath"));
			setI018SourcePath(prop.getProperty("i018SourcePath"));
			setI018TargetPath(prop.getProperty("i018TargetPath"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// create log file
		logFile = new FileOutputStream(logFilePath + "log"
				+ new SimpleDateFormat("ddMMyyyy.HHmmss").format(new Date())
				+ ".txt");
		System.setOut(new PrintStream(logFile));
		System.out.println("Filtering utility program starting at: "
				+ new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
						.format(new Date()) + "\n\n");

		String strExclusions = readExclusionList();
		if (checkI017Files()) {
			filterI017Files(strExclusions);
		}
		else
		{
			System.out.println("No I017 files to filter");
		}
		if (checkI018Files()) {
			filterI018Files(strExclusions);
		}
		else
		{
			System.out.println("No I018 files to filter");
		}
		
		long endOfProgram = System.currentTimeMillis();
		System.out.println("Total time to run filtering utility:"
				+ formatter.format((endOfProgram - startOfProgram) / 1000d)
				+ " seconds");
	}

	private static String readExclusionList() throws IOException {
		// Read Exclusion List Start
		long startExclusionFileLoad = System.currentTimeMillis();
		// Open the exclusion file
		String strExclusions = "";
		String exclusionFileLine;
		long numberOfMetersInExclusion = 0;
		FileInputStream fisExclusionListFile = new FileInputStream(
				exclusionListPath + exclusionListFile);
		DataInputStream in = new DataInputStream(fisExclusionListFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuffer sBuffer = new StringBuffer("");
		// Read exclusion file line by line
		while ((exclusionFileLine = br.readLine()) != null) {
			sBuffer.append(exclusionFileLine + ",");
			numberOfMetersInExclusion++;
		}
		// Transform the String Buffers to be Strings
		strExclusions = sBuffer.toString();
		// Close the Exclusions file
		in.close();
		long endExclusionFileLoad = System.currentTimeMillis();
		System.out.println("Number of meters in exclusion list: "
				+ numberOfMetersInExclusion);
		System.out
				.println("Total time to load the exclusion file:"
						+ formatter
								.format((endExclusionFileLoad - startExclusionFileLoad) / 1000d)
						+ " seconds");
		// System.out.println(strExclusions);
		// Read Exclusion List End
		return strExclusions;
	}

	private static boolean checkI017Files() throws IOException {
		// Check I017 files
		long startCheckI017Files = System.currentTimeMillis();
		File i017File = new File(i017SourcePath);
		File[] listOfI017Files = i017File.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.contains("I017") && name.endsWith("xml");
			}
		});
		int numberOfI017Files = 0;
		System.out.println("-------------------------------------------------");
		System.out.println("List of I017 files to be processed in this batch:");
		System.out.println("-------------------------------------------------");
		for (int i = 0; i < listOfI017Files.length; i++) {
			if (listOfI017Files[i].isFile()) {
				System.out.println(listOfI017Files[i].getName());
			}
			// else if (listOfI017Files[i].isDirectory()) {
			// System.out.println("Directory " + listOfI017Files[i].getName());
			// }
			numberOfI017Files++;
		}
		System.out.println("-------------------------------------------------");
		System.out.println("Total number of I017 files: " + numberOfI017Files);
		long endCheckI017Files = System.currentTimeMillis();
		System.out
				.println("Total time to check I017 files:"
						+ formatter
								.format((endCheckI017Files - startCheckI017Files) / 1000d)
						+ " seconds");
		System.out.println("-------------------------------------------------");
		if (numberOfI017Files > 0) {
			return true;
		}
		return false;
	}
	
	private static boolean checkI018Files() throws IOException {
		// Check I017 files
		long startCheckI018Files = System.currentTimeMillis();
		File i018File = new File(i018SourcePath);
		File[] listOfI018Files = i018File.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.contains("I018") && name.endsWith("xml");
			}
		});
		int numberOfI018Files = 0;
		System.out.println("-------------------------------------------------");
		System.out.println("List of I018 files to be processed in this batch:");
		System.out.println("-------------------------------------------------");
		for (int i = 0; i < listOfI018Files.length; i++) {
			if (listOfI018Files[i].isFile()) {
				System.out.println(listOfI018Files[i].getName());
			}
			// else if (listOfI017Files[i].isDirectory()) {
			// System.out.println("Directory " + listOfI017Files[i].getName());
			// }
			numberOfI018Files++;
		}
		System.out.println("-------------------------------------------------");
		System.out.println("Total number of I018 files: " + numberOfI018Files);
		long endCheckI018Files = System.currentTimeMillis();
		System.out
				.println("Total time to check I018 files:"
						+ formatter
								.format((endCheckI018Files - startCheckI018Files) / 1000d)
						+ " seconds");
		if (numberOfI018Files > 0) {
			return true;
		}
		return false;
	}

	private static int filterI017Files(String strExclusions) throws IOException {
		// Check I017 files
		long startFilterI017Files = System.currentTimeMillis();
		File i017File = new File(i017SourcePath);
		File[] listOfI017Files = i017File.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.contains("I017") && name.endsWith("xml");
			}
		});
		String currentI017File = "";
		FileInputStream fisCurrentI017File;
		DataInputStream dis;
		BufferedReader br;
		String i017Line;
		Boolean firstLine;
		String strMeterRecord = "", strMeterSerialNumber = "";
		long lNumberOfMeterPoints = 0, lNumberOfMeterPointsExcluded = 0;
		for (int i = 0; i < listOfI017Files.length; i++) {
			if (listOfI017Files[i].isFile()) {
				currentI017File = listOfI017Files[i].getName();
			}
			System.out.println("-------------------------------------------------");
			System.out.println("Current I017 file being filtered is: "
					+ currentI017File);
			fisCurrentI017File = new FileInputStream(i017SourcePath
					+ currentI017File);
			dis = new DataInputStream(fisCurrentI017File);
			br = new BufferedReader(new InputStreamReader(dis));

			if (outI017 == null) {
				outI017 = new BufferedWriter(new FileWriter(i017TargetPath
						+ currentI017File, false));
			}
			firstLine = true;
			// START
			while ((i017Line = br.readLine()) != null) {
				// If the start or end of the file then write directly to the
				// output file
				if (!firstLine) {
					// keep collecting string in record
					strMeterRecord = strMeterRecord + i017Line + fileDelimiter;
					// out of all these lines, if the line contains msn, get the
					// value
					if (i017Line.contains("<meterSerialNo")) {
						strMeterSerialNumber = i017Line.substring(
								i017Line.indexOf(i017MSNStartTag)
										+ i017MSNStartTag.length(),
								i017Line.indexOf(i017MSNEndTag,
										i017Line.indexOf(i017MSNStartTag)
												+ i017MSNStartTag.length()));
					} else if (i017Line.contains("</meterPoint>")) {
						lNumberOfMeterPoints++;
						if (strExclusions.contains(strMeterSerialNumber)) {
							System.out.println("MSN excluded: "
									+ strMeterSerialNumber);
							lNumberOfMeterPointsExcluded++;
						}
						// If the current MSN is in exclusion list, write
						// message to log file
						else {
							outI017.write(strMeterRecord + fileDelimiter);
						}
						// Clear the tracking variables
						strMeterRecord = "";
						strMeterSerialNumber = "";
					} else if (i017Line.contains("</registerReads")) {
						outI017.write(i017Line + fileDelimiter);
					}
				} else {
					if (i017Line.contains("<registerReads")) {
						outI017.write(i017Line + fileDelimiter);
						firstLine = false;
					}
				}

			}
			// Close the input file
			dis.close();
			// Close the output files if they are open
			if (!(outI017 == null)) {
				outI017.close();
				outI017 = null;
			}
			//Delete the I017 source file here
			if(listOfI017Files[i].delete()){
    			System.out.println(listOfI017Files[i].getName() + " is deleted.");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
		}
		System.out.println("-------------------------------------------------");
		System.out.println("Total number of Meters:" + lNumberOfMeterPoints);
		System.out.println("Number of Meters excluded:"
				+ lNumberOfMeterPointsExcluded);
		long endFilterI017Files = System.currentTimeMillis();
		System.out
				.println("Total time to filter I017 files:"
						+ formatter
								.format((endFilterI017Files - startFilterI017Files) / 1000d)
						+ " seconds");
		System.out.println("-------------------------------------------------");
		return 0;
	}

	private static int filterI018Files(String strExclusions) throws IOException {
		// Check I018 files
		long startFilterI018Files = System.currentTimeMillis();
		File i018File = new File(i018SourcePath);
		File[] listOfI018Files = i018File.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.contains("I018") && name.endsWith("xml");
			}
		});
		String currentI018File = "";
		FileInputStream fisCurrentI018File;
		DataInputStream dis;
		BufferedReader br;
		String i018Line;
		Boolean firstLine;
		String strMeterRecord = "", strMeterSerialNumber = "";
		long lNumberOfMeterPoints = 0, lNumberOfMeterPointsExcluded = 0;
		for (int i = 0; i < listOfI018Files.length; i++) {
			if (listOfI018Files[i].isFile()) {
				currentI018File = listOfI018Files[i].getName();
			}
			System.out.println("-------------------------------------------------");
			System.out.println("Current I018 file being filtered is: "
					+ currentI018File);
			fisCurrentI018File = new FileInputStream(i018SourcePath
					+ currentI018File);
			dis = new DataInputStream(fisCurrentI018File);
			br = new BufferedReader(new InputStreamReader(dis));

			if (outI018 == null) {
				outI018 = new BufferedWriter(new FileWriter(i018TargetPath
						+ currentI018File, false));
			}
			firstLine = true;
			// START
			while ((i018Line = br.readLine()) != null) {
				// If the start or end of the file then write directly to the
				// output file
				if (!firstLine) {
					// keep collecting string in record
					strMeterRecord = strMeterRecord + i018Line + fileDelimiter;
					// out of all these lines, if the line contains msn, get the
					// value
					if (i018Line.contains("<meterSerialNo")) {
						strMeterSerialNumber = i018Line.substring(
								i018Line.indexOf(i018MSNStartTag)
										+ i018MSNStartTag.length(),
								i018Line.indexOf(i018MSNEndTag,
										i018Line.indexOf(i018MSNStartTag)
												+ i018MSNStartTag.length()));
					} else if (i018Line.contains("</meterPoint>")) {
						lNumberOfMeterPoints++;
						if (strExclusions.contains(strMeterSerialNumber)) {
							System.out.println("MSN excluded: "
									+ strMeterSerialNumber);
							lNumberOfMeterPointsExcluded++;
						}
						// If the current MSN is in exclusion list, write
						// message to log file
						else {
							outI018.write(strMeterRecord + fileDelimiter);
						}
						// Clear the tracking variables
						strMeterRecord = "";
						strMeterSerialNumber = "";
					} else if (i018Line.contains("</profileReads")) {
						outI018.write(i018Line + fileDelimiter);
					}
				} else {
					if (i018Line.contains("<profileReads")) {
						outI018.write(i018Line + fileDelimiter);
						firstLine = false;
					}
				}

			}
			// Close the input file
			dis.close();
			// Close the output files if they are open
			if (!(outI018 == null)) {
				outI018.close();
				outI018 = null;
			}
			//Delete the I018 source file here
			if(listOfI018Files[i].delete()){
    			System.out.println(listOfI018Files[i].getName() + " is deleted.");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
		}
		System.out.println("-------------------------------------------------");
		System.out.println("Total number of Meters:" + lNumberOfMeterPoints);
		System.out.println("Number of Meters excluded:"
				+ lNumberOfMeterPointsExcluded);
		long endFilterI018Files = System.currentTimeMillis();
		System.out
				.println("Total time to filter I018 files:"
						+ formatter
								.format((endFilterI018Files - startFilterI018Files) / 1000d)
						+ " seconds");
		System.out.println("-------------------------------------------------");
		return 0;
	}
	
	public static String getLogFilePath() {
		return logFilePath;
	}

	public static void setLogFilePath(String logFilePath) {
		FilteringUtility.logFilePath = logFilePath;
	}

	public static String getExclusionListPath() {
		return exclusionListPath;
	}

	public static void setExclusionListPath(String exclusionListPath) {
		FilteringUtility.exclusionListPath = exclusionListPath;
	}

	public static String getExclusionListFile() {
		return exclusionListFile;
	}

	public static void setExclusionListFile(String exclusionListFile) {
		FilteringUtility.exclusionListFile = exclusionListFile;
	}

	public static String getI017SourcePath() {
		return i017SourcePath;
	}

	public static void setI017SourcePath(String i017SourcePath) {
		FilteringUtility.i017SourcePath = i017SourcePath;
	}

	public static String getI017TargetPath() {
		return i017TargetPath;
	}

	public static void setI017TargetPath(String i017TargetPath) {
		FilteringUtility.i017TargetPath = i017TargetPath;
	}

	public static String getI018SourcePath() {
		return i018SourcePath;
	}

	public static void setI018SourcePath(String i018SourcePath) {
		FilteringUtility.i018SourcePath = i018SourcePath;
	}

	public static String getI018TargetPath() {
		return i018TargetPath;
	}

	public static void setI018TargetPath(String i018TargetPath) {
		FilteringUtility.i018TargetPath = i018TargetPath;
	}
}