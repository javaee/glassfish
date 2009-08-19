/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.webservices.transport.tcp;

import com.sun.xml.ws.transport.tcp.server.IncomeMessageProcessor;
import com.sun.xml.ws.transport.tcp.server.TCPMessageListener;
import com.sun.xml.ws.transport.tcp.server.WSTCPConnector;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;

/**
 *
 * @author oleksiys
 */
public class Connector implements WSTCPConnector {
    private final String host;
    private final int port;
    private final TCPMessageListener listener;

    private final Properties properties;
    
    private final IncomeMessageProcessor processor;
    
    public Connector(String host, int port, TCPMessageListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
        
        properties = new Properties();

        processor = IncomeMessageProcessor.registerListener(port, listener, properties);
    }

    public void listen() throws IOException {
    }

    public void process(ByteBuffer buffer, SocketChannel channel) throws IOException {
        processor.process(buffer, channel);
    }

    public void notifyConnectionClosed(SocketChannel channel) {
        processor.notifyClosed(channel);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
    }

    public TCPMessageListener getListener() {
        return listener;
    }

    public void setListener(TCPMessageListener arg0) {
    }

    public void setFrameSize(int frameSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getFrameSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() {
        IncomeMessageProcessor.releaseListener(port);
    }
}
