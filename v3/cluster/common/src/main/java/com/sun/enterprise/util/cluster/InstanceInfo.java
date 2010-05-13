/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.util.cluster;

import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import java.util.logging.Logger;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;

/**
 * Used to format instance state info in a standard way.
 * It also does internet work
 * @author byron Nevins
 */
public class InstanceInfo {
    public InstanceInfo(String name0, int port0, String host0, Logger logger0) {
        name = name0;
        port = port0;
        host = host0;
        logger = logger0;
        running = pingInstance();
    }

    @Override
    public String toString() {
        return "name: " + getName()
                + ", host: " + getHost()
                + ", port: " + getPort()
                + ", state: " + running;
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
     * @return the host
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name0) {
        name = name0;
    }

    // TODO what about security????
    private String pingInstance() {
        // TODO force a shorter timeout if instance isn't running -- currently an unacceptable 10-15 seconds
        try {
            RemoteAdminCommand rac = new RemoteAdminCommand("uptime", host, port, false, "admin", null, logger);
            ParameterMap map = new ParameterMap();
            return rac.executeCommand(map);
        }
        catch (CommandException ex) {
            return "Not Running";
        }
    }
    private String host;
    private int port;
    private String name;
    private String running;
    private Logger logger;
}
/** delete this stuff after May 30, 2010
private boolean simplePortTest() {
return !NetUtils.isPortFree(host, port);
}

private boolean advancedPortTest() {
Socket socket = NetUtils.getClientSocket(host, port, 1000);
BufferedReader reader = null  ;

try {
reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
return true;
}
catch (IOException ex) {
return false;
}
finally {
try {
if(reader != null)
reader.close();
}
catch(Exception e) {
// ignore
}
}
}
 */
