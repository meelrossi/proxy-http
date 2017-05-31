package ar.edu.itba.protocol.configurator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class ConfiguratorImplementation implements Runnable {
	final ConfiguratorConnectionHandler handler = new ConfiguratorConnectionHandler();
	private int port;
	
	public ConfiguratorImplementation(int port) {
		this.port = port;
	}
	
	@SuppressWarnings("resource")
	public void run() {
		Logger log = (Logger) LogManager.getLogger("Configurator");
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			log.info("Configurator listening in port :" + server.getLocalPort());
		} catch (IOException e) {
			log.error("Could not start configurator");
			return;
		}

		while (true) {
			Socket socket = null;
			try {
				socket = server.accept();
				String s = socket.getRemoteSocketAddress().toString();
				log.info("Connected to" + s + "\n");
				handler.handle(socket);
			} catch (IOException e) {
				log.error("Not able to connect");
				return;
			}
		}
	}
}
