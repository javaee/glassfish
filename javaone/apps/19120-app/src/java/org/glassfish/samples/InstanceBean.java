/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.samples;

import java.net.UnknownHostException;
import javax.inject.Named;

/**
 *
 * @author arun
 */
@Named
public class InstanceBean {
    public String getInstance() {
        return System.getProperty("com.sun.aas.instanceName");
    }

    public String getServer() throws UnknownHostException {
        return java.net.InetAddress.getLocalHost().getHostName();
    }
}
