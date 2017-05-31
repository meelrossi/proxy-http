package ar.edu.itba.protocol.metrics;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class MetricsServerSelector implements Runnable {
	private static final int TIMEOUT = 3000;
	private Integer port;

	private static final Logger log = (Logger) LogManager.getLogger("Metrics");

	public MetricsServerSelector(Integer port) {
		this.port = port;
	}

	public void run() {
		Selector selector;
		try {
			selector = Selector.open();
			ServerSocketChannel listnChannel = ServerSocketChannel.open();
			listnChannel.socket().bind(new InetSocketAddress(port));
			listnChannel.configureBlocking(false);
			listnChannel.register(selector, SelectionKey.OP_ACCEPT);
			log.info("Metrics listening on port :" + port);
			MetricsTCPProtocol protocol = new MetricsSelectorProtocol();
			while (true) {
				if (selector.select(TIMEOUT) == 0) {
					continue;
				}
				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					if (key.isAcceptable()) {
						protocol.handleAccept(key);
					}
					if (key.isReadable()) {
						protocol.handleRead(key);
					}
					if (key.isValid() && key.isWritable()) {
						protocol.handleWrite(key);
					}
					keyIter.remove();
				}
			}
		} catch (IOException e) {
			log.error("Not able to connect to port : " + port);
		}

	}
}