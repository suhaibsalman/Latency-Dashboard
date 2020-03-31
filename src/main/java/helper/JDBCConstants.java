package helper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class JDBCConstants {
	//read pram from application.properties
	public String getPram(String key) {
		try {
			Properties prop = new Properties();
			String propFileName = "application.properties";

			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}

			return prop.getProperty(key);

		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
}
