package Utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import dao.JDBCDaoImpl;


public class Constants {
	private static Logger logger = Logger.getLogger(JDBCDaoImpl.class);
	//read pram from application.properties
	public String getPram(String key) {
		try {
			Properties prop = new Properties();
			String propFileName = "application.properties";

			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				logger.error("property file '" + propFileName + "' not found in the classpath");
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}

			return prop.getProperty(key);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
}
