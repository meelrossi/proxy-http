package ar.edu.itba.protocol.metrics;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import ar.edu.itba.protocol.CommandsPattern;
import ar.edu.itba.protocol.ProtocolsStatusCode;
import ar.edu.itba.proxy.PropertiesManager;

public class MetricsDecoder {
	private static final int BUFSIZE = 1024;
	private String line = "";
	private final Metrics metrics = Metrics.getInstance();
	private ByteBuffer buffer;
	private boolean logged = false;
	private boolean quit = false;
	private static final Logger log = (Logger) LogManager.getLogger("Metrics");

	public MetricsDecoder() {
		this.buffer = ByteBuffer.allocate(BUFSIZE);
		String welcomeString = "Welcome to the proxy metrics. \"Help\" for more info.\r\n";
		buffer.put(welcomeString.getBytes());
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public boolean read() {
		line = line + new String(buffer.array(), 0, buffer.position());
		buffer.clear();
		if (line.lastIndexOf("\n") == (line.length() - 1)) {
			return true;
		}
		return false;
	}

	public String getReponse() {
		String s = line.replace("\n", "").replace("\r", "").toUpperCase();
		line = "";
		if (commandMatchesPattern(CommandsPattern.AUTHENTICATION, s)) {
			String[] userAndPass = s.split(" ")[1].split(":");
			String user = userAndPass[0];
			String pass = userAndPass[1];
			if (user.equals(PropertiesManager.getProperty("user").toUpperCase())
					&& pass.equals(PropertiesManager.getProperty("password").toUpperCase())) {
				logged = true;
				log.info("User autenthicated " + user + " " + pass);
				return ProtocolsStatusCode.AUTHENTIFICATION_SUCCESS.getDescription();
			}
			return ProtocolsStatusCode.AUTHENTIFICATION_FAIL.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.HELP, s)) {
			return ProtocolsStatusCode.OK.getDescription() + getHelpDescription();

		} else if (commandMatchesPattern(CommandsPattern.EXIT, s)) {
			quit = true;
			return ProtocolsStatusCode.EXIT.getDescription();
			
		} else if (!logged) {
			return ProtocolsStatusCode.NOT_AUTHENTICATED.getDescription();

		} else if (commandMatchesPattern(CommandsPattern.METRICS, s)) {
			return ProtocolsStatusCode.OK.getDescription() + metrics.toString();

		} else if ( commandMatchesPattern(CommandsPattern.METRICS_JSON, s)) {
			return ProtocolsStatusCode.OK.getDescription() + metrics.getJson();
			
		} else if (commandMatchesPattern(CommandsPattern.CONNECTIONS_CLIENT, s)) {
			return ProtocolsStatusCode.OK.getDescription() + metrics.getClientConnections() + "\r\n";

		} else if (commandMatchesPattern(CommandsPattern.CONNECTIONS_SERVER, s)) {
			return ProtocolsStatusCode.OK.getDescription() + metrics.getServerConnections() + "\r\n";

		} else if (commandMatchesPattern(CommandsPattern.BYTES_CLIENT, s)) {
			return ProtocolsStatusCode.OK.getDescription() + metrics.getClientTransferedBytes() + "\r\n";

		} else if (commandMatchesPattern(CommandsPattern.BYTES_SERVER, s)) {
			return ProtocolsStatusCode.OK.getDescription() + metrics.getServerTransferedBytes() + "\r\n";

		} else if (commandMatchesPattern(CommandsPattern.TRANSFORMATIONS, s)) {
			return ProtocolsStatusCode.OK.getDescription() + metrics.getTransformations() + "\r\n";

		}
		return ProtocolsStatusCode.UNDECLARED_METHOD.getDescription();
	}

	public boolean commandMatchesPattern(Pattern command, String input) {
		return command.matcher(input).matches();
	}

	public String getHelpDescription() {
		String help = "USER $username :$password \r\n"
				+ "HELP\r\n" 
				+ "BYTES SERVER/CLIENT\r\n" 
				+ "CONNECTIONS SERVER/CLIENT\r\n"
				+ "METRICS\r\n"
				+ "EXIT\r\n";
		return help;
	}
	
	public boolean shouldQuit() {
		return quit;
	}
}
