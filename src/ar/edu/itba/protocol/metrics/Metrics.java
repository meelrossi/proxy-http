package ar.edu.itba.protocol.metrics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Metrics {
	
	private final static Metrics metricsInstance = new Metrics();
	private long clientTransferedBytes = 0;
	private long serverTransferedBytes = 0;
	private long clientConnections = 0;
	private long serverConnections = 0;
	private long transformations = 0;
	
	private Metrics() {
		
	}
	
	public static Metrics getInstance() {
		return metricsInstance;
	}
	
	public long getClientTransferedBytes() {
		return clientTransferedBytes;
	}

	public void addClientTransferedBytes(long clientTransferedBytes) {
		this.clientTransferedBytes += clientTransferedBytes;
	}

	public long getServerTransferedBytes() {
		return serverTransferedBytes;
	}

	public void addServerTransferedBytes(long serverTransferedBytes) {
		this.serverTransferedBytes += serverTransferedBytes;
	}

	public long getClientConnections() {
		return clientConnections;
	}

	public void addClientConnection() {
		clientConnections ++;
	}

	public long getServerConnections() {
		return serverConnections;
	}

	public void addServerConnection() {
		serverConnections++;
	}

	public long getTransformations() {
		return transformations;
	}

	public void addTransformation() {
		transformations ++;;
	}
	
	public String getJson() {
		JsonObject metrics = new JsonObject();
		
        JsonObject bytes = new JsonObject();
        bytes.addProperty("server", serverTransferedBytes);
        bytes.addProperty("client", clientTransferedBytes);
        
        JsonObject connections = new JsonObject();
        connections.addProperty("server", serverConnections);
        connections.addProperty("client", clientConnections);
        
        metrics.addProperty("transformations", transformations);
        metrics.add("connections", connections);
        metrics.add("bytes", bytes);
        
        return new Gson().toJson(metrics) + "\r\n";
	}
	
	public String toString() {
		return "BYTES CLIENT " + clientTransferedBytes + "\r\n"
				+ "BYTES SERVER " +serverTransferedBytes + "\r\n"
				+ "CONNECTIONS SERVER " + serverConnections + "\r\n"
				+ "CONNECTIONS CLIENT " + clientConnections + "\r\n"
				+ "TRANSFORMATIONS " + transformations + "\r\n";
	}
	
	
	
}
