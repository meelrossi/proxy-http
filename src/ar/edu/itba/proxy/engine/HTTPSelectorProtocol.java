package ar.edu.itba.proxy.engine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ar.edu.itba.protocol.metrics.Metrics;
import ar.edu.itba.proxy.support.HTMLResources;

public class HTTPSelectorProtocol implements TCPProtocol {

	private HTMLResources resources;

	public HTTPSelectorProtocol(HTMLResources resources) {
		this.resources = resources;
	}

	public void handleAccept(SelectionKey key) {

		try {
			SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();

			channel.configureBlocking(false);

			ProxyConnection proxyConnection = new ProxyConnection(channel);

			proxyConnection.updateLastUsed();

			proxyConnection.setClientConnected(true);

			channel.register(key.selector(), SelectionKey.OP_READ, proxyConnection);

		} catch (IOException e) {
			Engine.log.error("No se pudo aceptar la conexion entrante del cliente");
		}

		Metrics.getInstance().addClientConnection();
		Engine.log.info("La conexion del cliente fue aceptada");

	}

	public void handleConnect(SelectionKey key) {

		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();
		SocketChannel serverChannel = (SocketChannel) key.channel();

		try {
			if (key.isConnectable() && serverChannel.finishConnect()) {

				serverChannel.register(key.selector(), SelectionKey.OP_WRITE, proxyConnection);

				proxyConnection.setServerConnected(true);
				Engine.log.info("Se ha establecido conexión con el host " + proxyConnection.getHost());
				Metrics.getInstance().addServerConnection();

			}

		} catch (IOException io) {
			Engine.log.error("No se pudo establecer conexion con el servidor");
			proxyConnection.closeChannels();
			return;
		}
		proxyConnection.updateLastUsed();

	}

	public void handleRead(SelectionKey key) {

		SocketChannel channel = (SocketChannel) key.channel();

		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		ByteBuffer counterpartBuffer = null;

		HTTPDecipher decipher = null;

		if (proxyConnection.isClient(channel)) {
			counterpartBuffer = proxyConnection.getServerBuffer();
			decipher = proxyConnection.getClientDecipher();
		} else {
			counterpartBuffer = proxyConnection.getClientBuffer();
			decipher = proxyConnection.getServerDecipher();
		}

		long bytesRead = 0;

		try {
			bytesRead = channel.read(counterpartBuffer);
		} catch (IOException e) {
			Engine.log.error("No se pudo leer del buffer");
			proxyConnection.closeChannels();
		}

		if (bytesRead == -1) {
			if (proxyConnection.isClient(channel))
				try {
					key.channel().close();
					if (proxyConnection.getServerChannel() != null && proxyConnection.isServerConnected())
						ConnectionPool.getInstance().addConnection(proxyConnection.getHost(),
								proxyConnection.getServerChannel(), key);
					return;
				} catch (IOException e) {
					Engine.log.error("No se han podido cerrar los canales.");
				}
			else
				try {
					key.channel().close();
					proxyConnection.setServerConnected(false);
					return;
				} catch (IOException e) {
					Engine.log.error("No se han podido cerrar los canales.");
				}
		}

		if (bytesRead > 0) {

			decipher.setKey(key);

			if (proxyConnection.getClientBuffer() == counterpartBuffer) {
				Metrics.getInstance().addServerTransferedBytes(bytesRead);
			} else {
				Metrics.getInstance().addClientTransferedBytes(bytesRead);
			}

			int position = counterpartBuffer.position();
			int limit = counterpartBuffer.limit();

			if (!proxyConnection.isClient(channel) && proxyConnection.getLastPositionRead() != 0) {
				counterpartBuffer.position(proxyConnection.getLastPositionRead());
			} else if (proxyConnection.isClient(channel) && proxyConnection.getLastPositionReadCli() != 0) {
				counterpartBuffer.position(proxyConnection.getLastPositionReadCli());
			} else {
				counterpartBuffer.flip();
			}

			decipher.consume(counterpartBuffer);

			counterpartBuffer.limit(limit);
			counterpartBuffer.position(position);

			if (!proxyConnection.isClient(channel) && !proxyConnection.isResponseFinished()
					&& position != counterpartBuffer.capacity()) {
				proxyConnection.setLastPositionRead(position);
			} else if (proxyConnection.isClient(channel) && !proxyConnection.isResponseFinished()
					&& position != counterpartBuffer.capacity()) {
				proxyConnection.setLastPositionReadCli(position);
			}

		}
		if (proxyConnection.isClient(channel))
			handleClientRead(key, bytesRead);
		else
			handleServerRead(key, bytesRead);

		if (key.isValid() && key.isReadable() && counterpartBuffer.capacity() == counterpartBuffer.position()
				&& !proxyConnection.isRequestFinished() && !proxyConnection.isResponseFinished()) {
			try {
				key.interestOps(key.interestOps() - SelectionKey.OP_READ);
			} catch (Exception e) {
				System.out.println("No se pudo desubscribir el canal de lectura.");
			}

		}

		proxyConnection.updateLastUsed();
	}

	public void handleWrite(SelectionKey key) {

		SocketChannel channel = (SocketChannel) key.channel();

		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		ByteBuffer buffer = null;

		if (proxyConnection == null) {
			return;
		}

		if (proxyConnection.getStatusCode() != null) {
			buffer = resources.getProxyResponse(proxyConnection.getStatusCode());
		} else {
			if (proxyConnection.isClient(channel)) {
				buffer = proxyConnection.getClientBuffer();
			} else {
				buffer = proxyConnection.getServerBuffer();
			}
		}

		buffer.flip();

		long bytesWritten = 0;
		try {
			while (buffer.hasRemaining()) {
				int bytes = channel.write(buffer);
				if (bytes == 0) {
					break;
				}
				bytesWritten += bytes;
			}
		} catch (IOException e) {
			Engine.log.error("No se pudo escribir en el buffer");
			proxyConnection.closeChannels();
			return;
		}

		if (proxyConnection.isClient(channel)) {
			Metrics.getInstance().addClientTransferedBytes(bytesWritten);
			handleClientWrite(key, bytesWritten);
		} else {
			Metrics.getInstance().addServerTransferedBytes(bytesWritten);
			handleServerWrite(key, bytesWritten);
		}

		proxyConnection.updateLastUsed();

	}

	private void handleClientRead(SelectionKey key, long bytesRead) {

		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		if (proxyConnection.isRequestFinished()) {
			key.interestOps(key.interestOps() - SelectionKey.OP_READ);
		}

		if (proxyConnection.isServerConnected()) { // ya esta subscrito en
													// cuando se hace el
													// connect, pero puede
													// servir para persistentes.
			SelectionKey serverKey = proxyConnection.getServerChannel().keyFor(key.selector());
			if (serverKey.isValid()) {
				serverKey.interestOps(SelectionKey.OP_WRITE);
			}
		}

	}

	private void handleServerRead(SelectionKey key, long bytesRead) {

		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		if (bytesRead > 0) {
			SelectionKey clientKey = proxyConnection.getClientChannel().keyFor(key.selector());
			if (clientKey != null && clientKey.isValid()) {
				clientKey.interestOps(SelectionKey.OP_WRITE);
			}
		}

		if (proxyConnection.getStatusCode() != null) {
			try {
				key.channel().close();
				proxyConnection.setServerConnected(false);
				return;
			} catch (IOException e) {
				Engine.log.error("Error closing server channel");
			}
		}

		if (!proxyConnection.isClientConnected()
				|| (proxyConnection.isResponseFinished() && !proxyConnection.isPersistent())) {
			ConnectionPool.getInstance().addConnection(proxyConnection.getHost(), proxyConnection.getServerChannel(),
					key);
			proxyConnection.setServerConnected(false);
			return;
		}

		if (proxyConnection.isResponseFinished() && proxyConnection.isPersistent() && key.isReadable()) {
			key.interestOps(key.interestOps() - SelectionKey.OP_READ);
		}
	}

	private void handleClientWrite(SelectionKey key, long bytesWritten) {

		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		if (bytesWritten == -1 || proxyConnection.getStatusCode() != null) {
			try {
				key.channel().close();
				if (proxyConnection.getServerChannel() != null && proxyConnection.isServerConnected())
					ConnectionPool.getInstance().addConnection(proxyConnection.getHost(),
							proxyConnection.getServerChannel(), key);
				return;
			} catch (IOException e) {
				Engine.log.error("No se han podido cerrar los canales.");
			}
			return;
		}

		SelectionKey serverKey = proxyConnection.getServerChannel().keyFor(key.selector());
		if (serverKey != null && serverKey.isValid() && !proxyConnection.isResponseFinished()
				&& proxyConnection.isServerConnected()) {
			serverKey.interestOps(SelectionKey.OP_READ);
			key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
		}

		if (!proxyConnection.isServerConnected()
				|| (proxyConnection.isResponseFinished() && !proxyConnection.isPersistent())) {
			try {
				key.channel().close();
				proxyConnection.getServerChannel().close();
			} catch (IOException e) {
				Engine.log.error("No se han podido cerrar los canales.");
			}
		}

		if (proxyConnection.isServerConnected() && proxyConnection.isPersistent()
				&& proxyConnection.isResponseFinished()) {
			key.interestOps(SelectionKey.OP_READ);
			proxyConnection.setResponseFinished(false);
			proxyConnection.setRequestFinished(false);
			proxyConnection.getServerBuffer().clear();
		}

		if (bytesWritten == 0 && key.isValid() && key.isWritable()) {
			key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
		}

		proxyConnection.setLastPositionRead(0);
		proxyConnection.getClientBuffer().compact();
	}

	private void handleServerWrite(SelectionKey key, long bytesWritten) {
		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		if (proxyConnection.isRequestFinished() || bytesWritten == 0) {
			key.interestOps(SelectionKey.OP_READ);
		}

		proxyConnection.getServerBuffer().compact();
		proxyConnection.setLastPositionReadCli(0);

	}

}
