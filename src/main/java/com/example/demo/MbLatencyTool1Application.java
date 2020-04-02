package com.example.demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import Utils.Constants;
import dao.JDBCDaoImpl;
import entity.LatencyDashboard;

@SpringBootApplication
public class MbLatencyTool1Application {
	private static long lastRowPos = 0;

	private static String date = "";
	private static String sessionID = "";
	private static String serviceName = "";
	private static String channelTrxRef = "";
	private static String requestId = "";
	private static String type = "";

	private static final String ID_TAG = "sId:";
	private static final String END_SEPARATOR = "]";
	private static final String END_TAG_SEPARATOR = "<";
	private static final String FIRST_ROW_INDECATOR = "DEBUG";
	private static final String SERVICE_NAME_TAG = "<ns2:";
	private static final String LOGIN_REQUEST_TAG = "<ns2:LoginRequest>";
	private static final String LOGIN_RRESPONSE_TAG = "<ns2:LoginResponse>";
	private static final String CHANNEL_TRX_REF_TAG = "ChannelTrxRef>";
	private static final String REQUEST_TAG = "<requestId>";
	private static final String END_ENVELOPE_TAG = "</soapenv:Envelope>";

	private static final String MIDDLE_WARE = "MW";
	private static final String INTERNET_BANKING = "Internet banking";

	private static Constants Constants = new Constants();

	private static Logger logger = Logger.getLogger(MbLatencyTool1Application.class);

	public static void main(String[] args) {
		logger.info("Start Java Application");
		repeatEachXSecond(getRepeatedTime());
	}

	private static long getRepeatedTime() {
		return Long.parseLong(Constants.REPEAT_TIME);
	}

	private static void repeatEachXSecond(long rTime) {
		Timer timer = new Timer();
		TimerTask myTask = new TimerTask() {
			@Override
			public void run() {
				readFormFile(getFilePath());
			}
		};

		timer.schedule(myTask, 0, rTime);
	}

	private static void readFormFile(String path) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));

			br.skip(getStartPos());

			String contentLine = br.readLine();
			while (contentLine != null) {
				incLastPos(contentLine.length());
				validate(contentLine.trim());
				contentLine = br.readLine();
			}

		} catch (FileNotFoundException e) {
			logger.error("File not found");
		} catch (IOException ioe) {
			logger.error("Error while Reading file" + ioe.getMessage());
			ioe.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				logger.error("Error in closing the BufferedReader" + ioe.getMessage());
			}
		}
	}

	private static void incLastPos(long charNum) {
		lastRowPos = lastRowPos + charNum;
	}

	private static long getStartPos() {
		return lastRowPos;
	}

	private static void validate(String line) {
		if (line.contains(FIRST_ROW_INDECATOR)) {
			date = getDate(line);
			sessionID = getSessionID(line);
		} else if (line.contains(LOGIN_REQUEST_TAG) || line.contains(LOGIN_RRESPONSE_TAG)) {
			return;
		} else if (line.contains(SERVICE_NAME_TAG)) {
			serviceName = getServiceName(line);
		} else if (line.contains(CHANNEL_TRX_REF_TAG)) {
			type = MIDDLE_WARE;
			channelTrxRef = getChannelTrxRef(line);
		} else if (line.contains(REQUEST_TAG)) {
			type = INTERNET_BANKING;
			requestId = getRequestID(line);
		} else if (line.contains(END_ENVELOPE_TAG)) {
		    insertRecord(getRecord());
		}
	}

	private static void insertRecord(LatencyDashboard latencyDashboard) {
		try {
			JDBCDaoImpl.insertLatencyRecord(latencyDashboard);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		resetParameter();
	}

	private static String getRequestID(String line) {
		int startPos = line.indexOf(REQUEST_TAG) + REQUEST_TAG.length();
		int endPos = line.indexOf(END_TAG_SEPARATOR, startPos);

		return line.substring(startPos, endPos);
	}

	private static String getChannelTrxRef(String line) {
		int startPos = line.indexOf(CHANNEL_TRX_REF_TAG) + CHANNEL_TRX_REF_TAG.length();
		int endPos = line.indexOf(END_TAG_SEPARATOR, startPos);

		return line.substring(startPos, endPos);
	}

	private static String getServiceName(String line) {
		return line.substring(SERVICE_NAME_TAG.length(), line.length());
	}

	private static String getSessionID(String line) {
		int startPos = line.indexOf(ID_TAG) + ID_TAG.length();
		int endPos = line.indexOf(END_SEPARATOR, startPos);

		return line.substring(startPos, endPos);
	}

	private static String getDate(String line) {
		return line.substring(0, 28);
	}

	private static void resetParameter() {
		date = "";
		sessionID = "";
		serviceName = "";
		channelTrxRef = "";
		requestId = "";
		type = "";
	}

	private static LatencyDashboard getRecord() {
		if (type.equals(MIDDLE_WARE)) {
			return new LatencyDashboard(date, sessionID, serviceName, "", type, channelTrxRef,
					getCurrentDate(Constants.DATE_PATTERN2));
		} else if (type.equals(INTERNET_BANKING)) {
			return new LatencyDashboard(date, sessionID, serviceName, "", type, requestId,
					getCurrentDate(Constants.DATE_PATTERN2));
		} else {
			return new LatencyDashboard(date, sessionID, serviceName, "", type, "",
					getCurrentDate(Constants.DATE_PATTERN2));
		}
	}

	private static String getFilePath() {
		String fileName = Constants.WEBSERVICE + "." + getCurrentDate(Constants.DATE_PATTERN) + "." + "0" + "."
				+ Constants.FILE_EXTENTION;
		
		String filePath = getFolderPath()+ fileName;

		return filePath;
	}

	private static String getFolderPath() {
		String fPath = Constants.FOLDER_PATH;
		
		if(fPath.substring(fPath.length() - 1).equals("\\")) {
			return fPath;
		}else {
			return fPath + "\\";
		}
	}
	
	private static String getCurrentDate(String pattern) {
		DateFormat dateFormat = new SimpleDateFormat(pattern);
		Date date = new Date();

		String currentDate = dateFormat.format(date);
		return currentDate;
	}

	/*
	 * private static boolean checkFilePattern() { String filePath =
	 * Constants.FILE_PATH;
	 * 
	 * String[] parts = filePath.split(Pattern.quote("."));
	 * 
	 * if (parts.length != 4) { return false; } if
	 * (!parts[0].equals(Constants.WEBSERVICE)) { return false; } if
	 * (!isValidFormat(Constants.DATE_PATTERN, parts[1])) { return false; } if
	 * (!parts[3].equals(Constants.FILE_EXTENTION)) { return false; }
	 * 
	 * return true; }
	 */

	public static boolean isValidFormat(String format, String value) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(value);
			if (!value.equals(sdf.format(date))) {
				date = null;
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return date != null;
	}
}
