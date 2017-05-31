package ar.edu.itba.protocol.configurator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class ConfiguratorConnectionHandler implements ConnectionHandler {

	private static final int BUFSIZE = 50;

	private static String endResponse = "\r\n";
	
	private static final Logger log = (Logger) LogManager
			.getLogger("Configurator");

	public void handle(Socket s) throws IOException {
		boolean endOfLine = false;
		ByteBuffer lineBuffer = ByteBuffer.allocate(BUFSIZE);
		InputStream in = s.getInputStream();
		OutputStream out = s.getOutputStream();
		ConfiguratorConnectionDecoder decoder = new ConfiguratorConnectionDecoder();
		int lineSize = 0;
		String welcomeString = "Welcome to the proxy configuration. \"Help\" for more info.\r\n";
		int recvMsgSize = 0; 
		byte[] receiveBuf = new byte[BUFSIZE];
		
		try {
			out.write(welcomeString.getBytes());
		} catch (SocketException e) {
			log.info("Client closed connection.");
			return;
		}
		
		while(s.isConnected()) {
			while (!endOfLine && (recvMsgSize = in.read(receiveBuf)) != -1) {
				lineSize += recvMsgSize;
				lineBuffer.put(receiveBuf, 0, recvMsgSize);
				String data = new String(receiveBuf);
				endOfLine = data.contains("\n");
			}
			
			if(recvMsgSize == -1) {
				s.close();
				log.info("Closing connection due to end of transmission");
				return;
			}
			
			log.debug("Received line from client: " + new String(lineBuffer.array()).substring(0,lineSize));
			String answer = decoder.decode(new String(lineBuffer.array()).substring(0,lineSize));
			log.debug("Answer obtained: " + answer);
			
			answer += endResponse;
			try {
				out.write(answer.getBytes());
			} catch (SocketException e) {
				log.info("Client closed connection.");
				return;
			}
			if(decoder.shouldQuit()) {
				decoder.setShouldQuit(false);
				s.close();
				log.info("EXIT: Closing connection.");
				return;
			}
			
			lineBuffer.clear();
			endOfLine = false;
			lineSize = 0;
		}
		log.info("Client close connection. Closing socket.");
		s.close();
	}
	

}
