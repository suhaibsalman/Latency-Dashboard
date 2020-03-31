package com.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

import dao.JDBCDaoImpl;
import entity.LatencyDashboard;

@SpringBootApplication
public class MbLatencyTool1Application {

	private static String date = null;
	private static String sessionID = null;
	private static String serviceName = null;
	private static String channelTrxRef = null;
	private static String requestId = null;
	private static String type = null;
	
	private static Scanner myReader = null;

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

	private static final String filePath = "classpath:mb_log.txt";
	
	public static void main(String[] args) {
		readFormFile(filePath);
	}
	
	private static void readFormFile(String path) {
		try {
			File myObj = ResourceUtils.getFile(path);

			myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String line = myReader.nextLine().trim();
				validate(line);
			}

		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception occured " + e.getMessage());
		} finally {
			myReader.close();
		}
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
		date = null;
		sessionID = null;
		serviceName = null;
		channelTrxRef = null;
		requestId = null;
		type = null;
	}
	
	private static boolean isMiddleWare() {
		if(type.equals(MIDDLE_WARE)) {
			return true;
		}else {
			return false;
		}
	}
	
	private static LatencyDashboard getRecord() {
		if(isMiddleWare()) {
			return new LatencyDashboard(date, sessionID, serviceName, "", type, channelTrxRef);
		}else {
			return new LatencyDashboard(date, sessionID, serviceName, "", type, requestId);
		}
	}

}
