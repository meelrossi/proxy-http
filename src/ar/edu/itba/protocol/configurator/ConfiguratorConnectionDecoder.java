package ar.edu.itba.protocol.configurator;

import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import ar.edu.itba.protocol.CommandsPattern;
import ar.edu.itba.protocol.ProtocolsStatusCode;
import ar.edu.itba.proxy.PropertiesManager;

public class ConfiguratorConnectionDecoder {
	private Configuration conf = Configuration.getConfiguration();
	private static final Logger log = (Logger) LogManager.getLogger("Configurator");
	private boolean logged = false;
	private boolean quit = false;
	
	public String decode(String s) {
		s = s.replace("\n", "").replace("\r", "").toUpperCase();
		if (commandMatchesPattern(CommandsPattern.AUTHENTICATION, s)) {
			String[] userAndPass = s.split(" ")[1].split(":");
			String user = userAndPass[0];
			String pass = userAndPass[1];
			if (user.equals(PropertiesManager.getProperty("user").toUpperCase())
					&& pass.equals(PropertiesManager.getProperty("password").toUpperCase())) {
				logged = true;
				log.info("User authenticated " + user + " " + pass);
				return ProtocolsStatusCode.AUTHENTIFICATION_SUCCESS.getDescription();
			}
			return ProtocolsStatusCode.AUTHENTIFICATION_FAIL.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.HELP, s)) {
			return ProtocolsStatusCode.OK.getDescription() + help();

		} else if (commandMatchesPattern(CommandsPattern.EXIT, s)) {
			quit = true;
			return ProtocolsStatusCode.EXIT.getDescription();

		} else if (!logged) {
			return ProtocolsStatusCode.NOT_AUTHENTICATED.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.LEET_ON, s)) {
			conf.setTextTransformation(true);
			log.info("L33t transformation ON");
			return ProtocolsStatusCode.LEET_ON.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.LEET_OFF, s)) {
			conf.setTextTransformation(false);
			log.info("L33t transformation OFF");
			return ProtocolsStatusCode.LEET_OFF.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.LOG_DEBUG, s)) {
			conf.setLoggerLevel(Level.DEBUG);
			return ProtocolsStatusCode.LOG_DEBUG.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.LOG_ERROR, s)) {
			conf.setLoggerLevel(Level.ERROR);
			return ProtocolsStatusCode.LOG_ERROR.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.LOG_INFO, s)) {
			conf.setLoggerLevel(Level.INFO);
			return ProtocolsStatusCode.LOG_INFO.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.LOG_FATAL, s)) {
			conf.setLoggerLevel(Level.FATAL);
			return ProtocolsStatusCode.LOG_FATAL.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.LOG_WARN, s)) {
			conf.setLoggerLevel(Level.WARN);
			return ProtocolsStatusCode.LOG_WARN.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.LOG_OFF, s)) {
			conf.setLoggerLevel(Level.OFF);
			return ProtocolsStatusCode.LOG_OFF.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.CONFIGURATION, s)) {
			return ProtocolsStatusCode.OK.getDescription() + Configuration.getConfiguration().toString();

		}
		return ProtocolsStatusCode.UNDECLARED_METHOD.getDescription();

	}
	
	public boolean shouldQuit() {
		return quit;
	}
	
	public void setShouldQuit(boolean shouldQuit) {
		this.quit = shouldQuit;
	}
	
	public String help() {
		return "USER $username :$password \r\n"
				+ "LOG INFO/DEBUG/TRACE/ERROR/FATAL/OFF\r\n" 
				+ "L33T ON/OFF\r\n" 
				+ "CONFIGURATION\r\n" 
				+ "HELP\r\n" 
				+ "EXIT\r\n";
	}

	public boolean commandMatchesPattern(Pattern command, String input) {
		return command.matcher(input).matches();
	}

}
