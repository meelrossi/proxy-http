package ar.edu.itba.proxy.engine;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

import ar.edu.itba.proxy.http.HTTPHint;
import ar.edu.itba.proxy.http.HTTPRequest;
import ar.edu.itba.proxy.http.HTTPSkeleton;
import ar.edu.itba.proxy.http.HTTPTransfer;
import ar.edu.itba.proxy.http.syntax.HTTPMethod;
import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

public class HTTPDecipher extends HTTPSkeleton {

	private SelectionKey key;
	private HTTPHint hint = null;

	public HTTPDecipher() {
	}

	@Override
	public void onAcceptance(HTTPTransfer transfer) {
		Engine.log.debug("On Acceptance");

		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		proxyConnection.updateLastUsed();

		if (hint != null) {
			super.hint = hint;
			hint = null;
		}

		if (transfer.getMessage().isRequest()) {

			HTTPRequest request = (HTTPRequest) transfer.getMessage();

			if (request.getHost() != null) {
				removeHttp(proxyConnection, request);
			}

			if (request.getMethod().equals(HTTPMethod.HEAD)) {
				Engine.log.info("Se recibio un HEAD Request");
				proxyConnection.getServerDecipher().hint = HTTPHint.HEAD_RESPONSE;
			}

			Engine.log.info("Intentando acceder a " + request.getHost());

			if (proxyConnection.isPersistent()) {
				if (request.getHost().equals(proxyConnection.getHost()) && proxyConnection.isServerConnected()) {
					try {
						proxyConnection.getServerChannel().register(key.selector(), SelectionKey.OP_WRITE,
								proxyConnection);
					} catch (ClosedChannelException e) {
						Engine.log.info("El canal del servidor ha sido cerrado");
					}
					Engine.log.info("Se va a reusar una conexion existente");
					if (!request.isPersistent()) {
						proxyConnection.setNotPersistent();
					}
					return;
				} else {
					ConnectionPool.getInstance().addConnection(proxyConnection.getHost(),
							proxyConnection.getServerChannel(), key);
					proxyConnection.setServerConnected(false);
				}
			}
			SocketChannel serverConnection = ConnectionPool.getInstance().getConnection(request.getHost());
			if (serverConnection != null) {
				ByteBuffer buf = ByteBuffer.allocate(1);
				int read = 0;
				try {
					read = serverConnection.read(buf);
					if (read != -1) {
						proxyConnection.setServerChannel(serverConnection);
						proxyConnection.setHost(request.getHost());
						serverConnection.register(key.selector(), SelectionKey.OP_WRITE, proxyConnection);
						Engine.log.info("Se va a reusar una conexion con " + proxyConnection.getHost());
						return;
					}
				} catch (IOException e) {
					Engine.log.info("No se pudo asignar una conexion reusable con " + request.getHost()
							+ " el canal ha sido cerrado por el servidor");
				}
			}

			try {
				SocketChannel serverChannel;

				serverChannel = SocketChannel.open();
				serverChannel.configureBlocking(false);

				InetSocketAddress address = new InetSocketAddress(request.getHost(), request.getPort());
				boolean isConnected = serverChannel.connect(address);

				proxyConnection.setServerChannel(serverChannel);
				proxyConnection.setHost(request.getHost());

				if (request.isPersistent()) {
					Engine.log.info("Se creo una conexion persistente.");
					proxyConnection.setPersistent();
				}

				if (!isConnected) {
					serverChannel.register(key.selector(), SelectionKey.OP_CONNECT, proxyConnection);
				} else {
					Engine.log.info("Conexion instantanea hacia el servidor!");
					serverChannel.finishConnect();
					serverChannel.register(key.selector(), SelectionKey.OP_WRITE, proxyConnection);
				}

			} catch (UnresolvedAddressException e) {
				Engine.log.error("No se pudo resolver la direccion del host:" + proxyConnection.getHost());
			} catch (IOException e) {
				Engine.log.error("No se pudo establecer conexion con el servidor");
			} catch (CancelledKeyException e) {
				e.printStackTrace();
			}

		}
		super.onAcceptance(transfer);

	}

	public void onComplete(HTTPTransfer transfer) {
		Engine.log.debug("On Complete");
		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		if (!transfer.getMessage().isRequest()) {
			proxyConnection.setResponseFinished(true);
			Engine.log.debug("Se termino de leer la response");
		} else {
			proxyConnection.setRequestFinished(true);
		}
		if (transfer.getRemanent().hasRemaining()) {
		}

	}

	@Override
	public HTTPTransfer onError(HTTPTransfer transfer, HTTPStatusCode statusCode) {
		Engine.log.info("OnError ha ocurrido un error " + statusCode.getName() + " " + statusCode.getStatusCode());
		ProxyConnection proxyConnection = (ProxyConnection) key.attachment();

		proxyConnection.setStatusCode(statusCode);
		try {
			proxyConnection.getClientChannel().register(key.selector(), SelectionKey.OP_WRITE, proxyConnection);
		} catch (IOException e) {
			Engine.log.error("No se ha podido subscribir al cliente a lectura");
		}
		proxyConnection.setResponseFinished(true);
		return null;
	}

	public void setKey(SelectionKey key) {
		this.key = key;
	}

	public void removeHttp(ProxyConnection proxyConnection, HTTPRequest request) {

		String current = "";
		try {
			proxyConnection.getServerBuffer().flip();
			byte[] array = new byte[proxyConnection.getServerBuffer().remaining()];
			proxyConnection.getServerBuffer().get(array);
			current = new String(array, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Engine.log.error("No se ha podido decodificar el mensaje");
		}
		if(!current.contains("http://")) {
			return;
		}
		current = current.replace("http://", "").replaceFirst(request.getCompleteHost(), "");

		ByteBuffer newServerBuffer = ByteBuffer.allocate(ProxyConnection.BUFF_SIZE);
		try {
			newServerBuffer.put(current.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Engine.log.error("No se ha podido codificar el mensaje");
		}
		proxyConnection.setServerBuffer(newServerBuffer);
	}

}
