package ar.edu.itba.protocol.metrics;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class MetricsSelectorProtocol implements MetricsTCPProtocol {
	private static final Logger log = (Logger) LogManager.getLogger("Metrics");
	private static String endResponse = "\r\n";
	
	public MetricsSelectorProtocol() {
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();

		log.info("Connected to " + clntChan.getRemoteAddress());
		
		clntChan.configureBlocking(false);
		clntChan.register(key.selector(), SelectionKey.OP_WRITE, new MetricsDecoder());
	}

	public void handleRead(SelectionKey key) throws IOException {
		SocketChannel clntChan = (SocketChannel) key.channel();
		MetricsDecoder decoder = (MetricsDecoder) key.attachment();
		ByteBuffer buf = decoder.getBuffer();
		long bytesRead = clntChan.read(buf);
		if (bytesRead == -1) {
			log.info("Client closed connection. Closing channel");
			clntChan.close();
		} else if (bytesRead > 0) {
			if(decoder.read()) {
				String response = decoder.getReponse();
				if(response.length() == 0) {
					clntChan.close();
					log.info("EXIT: Closing channel");
					return;
				}
				response += endResponse;
				buf.put(response.getBytes());
				key.interestOps(SelectionKey.OP_WRITE);
			} else {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		MetricsDecoder decoder = (MetricsDecoder) key.attachment();
		ByteBuffer buf = decoder.getBuffer();
		buf.flip();
		SocketChannel clntChan = (SocketChannel) key.channel();
		clntChan.write(buf);
		if(decoder.shouldQuit()) {
			clntChan.close();
			key.cancel();
			return;
		}
		if (!buf.hasRemaining()) {
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.clear();
	}
}