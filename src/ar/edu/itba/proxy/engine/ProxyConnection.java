package ar.edu.itba.proxy.engine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

public class ProxyConnection {

	public static final int BUFF_SIZE = 4 * 1024;

	private static final int DEFAULT_TIMEOUT = 10 * 1000;

	private static final int KEEP_ALIVE_TIMEOUT = 15 * 1000;

	private ByteBuffer serverBuffer = ByteBuffer.allocate(BUFF_SIZE);

	private ByteBuffer clientBuffer = ByteBuffer.allocate(BUFF_SIZE);

	private SocketChannel clientChannel;

	private SocketChannel serverChannel;

	private HTTPDecipher clientDecipher;

	private HTTPDecipher serverDecipher = new HTTPDecipher();

	private long lastUsed;

	private String host = "";

	private boolean responseFinished = false;

	private boolean requestFinished = false;

	private int lastPositionRead = 0;

	private int lastPositionReadCli = 0;

	private boolean isServerConnected = false;

	private boolean isClientConnected = false;

	private boolean isPersistent = false;

	private HTTPStatusCode statusCode;

	public ProxyConnection(SocketChannel clientChannel) {
		this.clientChannel = clientChannel;
		this.clientDecipher = new HTTPDecipher();
	}

	public SocketChannel getServerChannel() {
		return serverChannel;
	}

	public void setServerChannel(SocketChannel serverChannel) {
		this.serverChannel = serverChannel;
	}

	public void updateLastUsed() {
		this.lastUsed = System.currentTimeMillis();
	}

	public HTTPDecipher getServerDecipher() {
		return serverDecipher;
	}

	public boolean isConnected() {
		if (serverChannel != null && clientChannel != null)
			return serverChannel.isConnected() && clientChannel.isConnected();
		return false;
	}

	public ByteBuffer getClientBuffer() {
		return clientBuffer;
	}

	public ByteBuffer getServerBuffer() {
		return serverBuffer;
	}

	public SocketChannel getClientChannel() {
		return clientChannel;
	}

	public boolean connectionTimeout() {
		if (isPersistent)
			return (System.currentTimeMillis() - lastUsed) >= KEEP_ALIVE_TIMEOUT;
		else
			return (System.currentTimeMillis() - lastUsed) >= DEFAULT_TIMEOUT;
	}

	public ByteBuffer getBufferFor(SocketChannel channel) {
		if (channel == serverChannel)
			return serverBuffer;
		else
			return clientBuffer;
	}

	public HTTPDecipher getClientDecipher() {
		return clientDecipher;
	}

	public void closeChannels() {

		if (serverChannel != null) {
			try {
				serverChannel.close();
			} catch (IOException e) {
				Engine.log.error("No se pudo cerrar el 'serverChannel'");
				e.printStackTrace();
			}
		}

		if (clientChannel != null) {
			try {
				clientChannel.close();
			} catch (IOException e) {
				Engine.log.error("No se pudo cerrar el 'clientChannel'");
				e.printStackTrace();
			}
		}

	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isPersistent() {
		return this.isPersistent;
	}
	
	public void setPersistent() {
		this.isPersistent = true;
	}

	public boolean isClient(SocketChannel channel) {
		return channel == clientChannel;
	}

	public String getHost() {
		return this.host;
	}

	public void setClientChannel(SocketChannel channel) {
		this.clientChannel = channel;
	}

	public void setClientDecipher(HTTPDecipher clientDecipher) {
		this.clientDecipher = clientDecipher;
	}

	public void setServerDecipher(HTTPDecipher serverDecipher) {
		this.serverDecipher = serverDecipher;
	}

	public long getLastUsed() {
		return lastUsed;
	}

	public void setServerBuffer(ByteBuffer buffer) {
		this.serverBuffer = buffer;
	}

	public boolean isResponseFinished() {
		return responseFinished;
	}

	public void setResponseFinished(boolean responseFinished) {
		this.responseFinished = responseFinished;
	}

	public void closeServerConnection() {
		isServerConnected = false;

		if (serverChannel != null) {
			try {
				serverChannel.close();
			} catch (IOException e) {
				Engine.log.error("No se pudo cerrar el 'serverChannel'");
			}
		}
	}

	public void closeClientConnection() {

		isClientConnected = false;

		if (clientChannel != null) {
			try {
				clientChannel.close();
			} catch (IOException e) {
				Engine.log.error("No se pudo cerrar el 'clientChannel'");
			}
		}

		closeServerConnection();
	}
	
	public void setNotPersistent() {
		this.isPersistent = false;
	}

	public void setClientBuffer(ByteBuffer buffer) {
		this.clientBuffer = buffer;
	}

	public int getLastPositionReadCli() {
		return lastPositionReadCli;
	}

	public void setLastPositionReadCli(int lastPositionReadCli) {
		this.lastPositionReadCli = lastPositionReadCli;
	}

	public int getLastPositionRead() {
		return lastPositionRead;
	}

	public void setLastPositionRead(int lastPositionRead) {
		this.lastPositionRead = lastPositionRead;
	}

	public boolean isRequestFinished() {
		return requestFinished;
	}

	public void setRequestFinished(boolean requestFinished) {
		this.requestFinished = requestFinished;
	}

	public HTTPStatusCode getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(HTTPStatusCode statusCode) {
		this.statusCode = statusCode;
	}

	public boolean isServerConnected() {
		return isServerConnected;
	}

	public void setServerConnected(boolean isServerConnected) {
		this.isServerConnected = isServerConnected;
	}

	public boolean isClientConnected() {
		return isClientConnected;
	}

	public void setClientConnected(boolean isClientConnected) {
		this.isClientConnected = isClientConnected;
	}

}
