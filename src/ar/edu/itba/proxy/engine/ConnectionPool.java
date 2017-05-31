package ar.edu.itba.proxy.engine;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class ConnectionPool {
	static final ConnectionPool instance = new ConnectionPool();
	
	private Map<String, SocketChannel> pool = new HashMap<String, SocketChannel>();
	
	private ConnectionPool() {
		
	}
	
	public static ConnectionPool getInstance() {
		return instance;
	}
	
	public void addConnection(String host, SocketChannel channel, SelectionKey key) {
		if(channel.isConnectionPending()) {
			try {
				channel.close();
			} catch (IOException e) {
				Engine.log.error("No se pudo cerrar el canal con el servidor");
			}
		}
		if(pool.containsKey(host)) {
			try {
				pool.get(host).close();
			} catch (IOException e) {
				Engine.log.error("No se ha podido cerrar el canal con el host: " + host);
			}
			pool.remove(host);
		}
		try {
			channel.register(key.selector(), 0, null);
		} catch (ClosedChannelException e) {
			Engine.log.error("No se pudo registrar el canal");
			return;
		}
		pool.put(host, channel);
	}
	
	public void removeConnection(String host) {
		pool.remove(host);
	}
	
	public SocketChannel getConnection(String host) {
		SocketChannel channel = pool.get(host);
		pool.remove(host);
		return channel;
	}
}
