package ar.edu.itba.proxy.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import ar.edu.itba.proxy.PropertiesManager;
import ar.edu.itba.proxy.support.HTMLResources;

public class Engine {

	// Logger principal utilizado por todo el servidor proxy
	public static final Logger log = (Logger) LogManager.getLogger("Proxy");

	private static final int PROXY_PORT = Integer.parseInt(PropertiesManager.getProperty("proxyPort"));
	private static final int SELECTOR_TIMEOUT = 3000;

	private HTMLResources resources;
	private Selector selector;
	private ServerSocketChannel proxyChannel;
	private TCPProtocol protocol;
	private int timeoutCalls = 0;

	public Engine() throws IOException {
		this.resources = new HTMLResources();
		this.selector = Selector.open();
		this.proxyChannel = ServerSocketChannel.open();
		this.protocol = new HTTPSelectorProtocol(resources);

		proxyChannel.socket().bind(new InetSocketAddress(PROXY_PORT));
		proxyChannel.configureBlocking(false);
		proxyChannel.register(selector, SelectionKey.OP_ACCEPT);

	}

	public void start() throws IOException {
		while (true) {

			if (selector.select(SELECTOR_TIMEOUT) == 0) {
				checkTimeout(selector.keys(), 200);
				continue;
			}

			checkTimeout(selector.keys(), timeoutCalls);

			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
			while (keyIter.hasNext()) {
				SelectionKey key = keyIter.next();

				if (key.isValid() && key.isAcceptable()) {
					protocol.handleAccept(key);
				}

				if (key.isValid() && key.isConnectable()) {
					protocol.handleConnect(key);
				}

				if (key.isValid() && key.isReadable()) {
					protocol.handleRead(key);
				}

				if (key.isValid() && key.isWritable()) {
					protocol.handleWrite(key);
				}

				keyIter.remove();
			}
		}
	}

	private void checkTimeout(Set<SelectionKey> keys, int timeoutCalls) {
               
                int cantConnClosed = 0;
               
                if(timeoutCalls == 200) {
                       
                        for(SelectionKey key : keys) {
                                ProxyConnection con = (ProxyConnection) key.attachment();
                               
                                if(con != null && con.connectionTimeout()) {
                                        con.closeChannels();
                                        key.cancel();
                                        cantConnClosed++;
                                }
                               
                        }
                       
                        timeoutCalls = 0;
                }
                if(cantConnClosed > 0)
                       Engine.log.debug("Se han cerrado " + cantConnClosed + " debido al timeout");
               
                timeoutCalls++;
               
        }

}
