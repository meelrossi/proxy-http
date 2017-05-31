package ar.edu.itba.proxy.engine;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface TCPProtocol {
    
    public void handleAccept(SelectionKey key) throws IOException;
    
    public void handleRead(SelectionKey key) throws IOException;
    
    public void handleWrite(SelectionKey key) throws IOException;
    
    public void handleConnect(SelectionKey key)  throws IOException;
    
}
