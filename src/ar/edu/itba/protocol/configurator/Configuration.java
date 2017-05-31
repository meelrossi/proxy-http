package ar.edu.itba.protocol.configurator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class Configuration {
	
	private final static Configuration configurationInstance = new Configuration();
	private boolean textTransformation = false;
	private Level loggerLevel = Level.INFO;

	private Configuration() {
	}

	public static Configuration getConfiguration() {
		return configurationInstance;
	}

	public boolean isTextTransformation() {
		return textTransformation;
	}

	public void setTextTransformation(boolean textTransformation) {
		this.textTransformation = textTransformation;
	}

	public void setLoggerLevel(Level level) {
		((Logger)LogManager.getLogger("Metrics")).setLevel(level);
		((Logger)LogManager.getLogger("Configurator")).setLevel(level);
		((Logger)LogManager.getLogger("Proxy")).setLevel(level);
	}
	
	public Level getLoggerLevel() {
		return loggerLevel;
	}
	
	public String toString() {
		String configurationString = "L33T ";
		if (textTransformation) {
			configurationString += "ON\r\n";
		} else {
			configurationString += "OFF\r\n";
		}
		configurationString += "LOGGER " + loggerLevel.name() + "\r\n";
		return configurationString;
	}

}
