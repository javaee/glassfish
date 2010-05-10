/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.util.cluster;

import com.sun.enterprise.util.net.NetUtils;
import java.io.*;
import java.io.InputStreamReader;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to format instance state info in a standard way.
 * It also does internet work
 * @author byron Nevins
 */
public class InstanceInfo {
    public InstanceInfo(String name0, int port0, String nodeAgentRef0) {
        host = name0;
        port = port0;
        nodeAgentRef = nodeAgentRef0;
        running = simplePortTest(nodeAgentRef, port);    // always do this
    }

    @Override
    public String toString() {
        String runningString = running ? "Running" : "Not Running";
        return "host: " + getHost()
                + ", port: " + getPort()
                + ", nodeAgentRef: " + getNodeAgentRef()
                + ", state: " + runningString;
    }

    public void runAdvancedTest() {
        Socket socket = NetUtils.getClientSocket(host, port, 1000);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException ex) {
            running = false;
        }

    }
/**
     * @return the host
     */
    public String getHost() {
        return host;
    }
    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the nodeAgentRef
     */
    public String getNodeAgentRef() {
        return nodeAgentRef;
    }

    /**
     * @param nodeAgentRef the nodeAgentRef to set
     */
    public void setNodeAgentRef(String nodeAgentRef) {
        this.nodeAgentRef = nodeAgentRef;
    }

    private boolean simplePortTest(String host, int port) {
        return !NetUtils.isPortFree(host, port);
    }

    private String host;
    private int port;
    private String nodeAgentRef;
    private boolean running;

    
}

