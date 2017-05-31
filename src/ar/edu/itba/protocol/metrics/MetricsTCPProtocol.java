package ar.edu.itba.protocol.metrics;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface MetricsTCPProtocol {
	
	void handleAccept(SelectionKey key) throws IOException;

	void handleRead(SelectionKey key) throws IOException;

	void handleWrite(SelectionKey key) throws IOException;
}