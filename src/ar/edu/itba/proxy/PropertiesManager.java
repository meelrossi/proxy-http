package ar.edu.itba.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class PropertiesManager {

	private static final Logger propLogger = (Logger)LogManager.getLogger("Proxy");
	
	public static String getProperty(String property) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = ClassLoader.getSystemResourceAsStream("conf.properties");
			properties.load(input);
		} catch (IOException e) {
			propLogger.error("Problem reading properties file");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					propLogger.error("Problem closing properties file");
				}
			}
		}
		return properties.getProperty(property);
	}
}
