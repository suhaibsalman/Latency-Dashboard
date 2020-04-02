package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	
	private static final String START_ENVELOP = "<soapenv:Envelope";
	private static final String END_ENVELOP = "</soapenv:Envelope>";

	private static final String MIDDLE_WARE = "MW";
	private static final String INTERNET_BANKING = "Internet banking";

	private static Constants Constants = new Constants();

	private static Logger logger = Logger.getLogger(MbLatencyTool1Application.class);

	public static void main(String[] args) {
		logger.info("Start Java Application");
		
		repeatEachXSecond(getRepeatedTime());
        
		//readFormXMLFile(getFilePath());
	}

	private static long getRepeatedTime() {
		return Long.parseLong(Constants.REPEAT_TIME);
	}

	private static void repeatEachXSecond(long rTime) {
		Timer timer = new Timer();
		TimerTask myTask = new TimerTask() {
			@Override
			public void run() {
				readFormXMLFile(getFilePath());
			}
		};

		timer.schedule(myTask, 0, rTime);
	}
	
	private static void readFormXMLFile(String path) {
		BufferedReader br = null;
		String result = "";
		try {
			br = new BufferedReader(new FileReader(path));

			if(getStartPos() == getFileLength()) {
				return;
			}
			br.skip(getStartPos());
	
			modifyLastPos(getFileLength());
			
			String contentLine = br.readLine();
			while (contentLine != null) {
				result = result + contentLine;
				contentLine = br.readLine();
			}
		
			SplitRequest(result);
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
	
	private static void convertStringToXmlDoc(String request) {

		try {
			String[] parts = request.split(Pattern.quote(START_ENVELOP));
			
			String xml = START_ENVELOP + parts[1];
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new InputSource(new StringReader(xml)));
		
			date = getDate( parts[0]);
			sessionID = getSessionID(parts[0]);
			serviceName = getServiceName(xml);
			channelTrxRef = getChannelTrxRef(doc);
			requestId = getRequestID(doc);
			
			if (!requestId.isEmpty()) {
				type = INTERNET_BANKING;
			} else if (!channelTrxRef.isEmpty()) {
				type = MIDDLE_WARE;
			} else {
				type = "";
			}
			
			insertRecord(getRecord());
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
	
	private static String getServiceName(String xml) {
		int endPos = xml.indexOf(" ", xml.indexOf("<ns2:"));
		String sName = xml.substring(xml.indexOf("<ns2:")+5,endPos);
		
		return sName;
	}

	private static String getChannelTrxRef(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("ns3:Channel");
		
		if (nodeList.getLength() > 0) {
			type = MIDDLE_WARE;
			Element node = (Element) nodeList.item(0);
			return node.getElementsByTagName("ns3:ChannelTrxRef").item(0).getTextContent();
		} else {
			return "";
		}
	}
	
	private static String getRequestID(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("RequestHeader");
		
		if (nodeList.getLength() > 0) {
			Element node = (Element) nodeList.item(0);
			return node.getElementsByTagName("requestId").item(0).getTextContent();
		} else {
			return "";
		}
	}

	private static void SplitRequest(String fContent) {
		String[] parts = fContent.split(Pattern.quote(END_ENVELOP));
		
		for(int i=0; i<parts.length; i++) {
			if(!parts[i].trim().isEmpty()) {
				convertStringToXmlDoc(parts[i]+ END_ENVELOP);
			}
		
		}
		
	}
	
	private static void modifyLastPos(long charNum) {
		lastRowPos = charNum;
	}

	private static long getStartPos() {
		return lastRowPos;
	}

	private static void insertRecord(LatencyDashboard latencyDashboard) {
		try {
			JDBCDaoImpl.insertLatencyRecord(latencyDashboard);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		resetParameter();
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
	
	private static long getFileLength() {
        File f = new File(getFilePath()); 
        
        return f.length();
	}
	
}
