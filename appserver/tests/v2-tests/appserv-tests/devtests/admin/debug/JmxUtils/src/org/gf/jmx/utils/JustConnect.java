/*
 * JustConnect.java
 *
 * Created on July 19, 2006, 9:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.gf.jmx.utils;

import java.util.HashMap;
import java.util.Map;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author Administrator
 */
public class JustConnect {

    private JMXServiceURL url;
    private JustConnect(final String urls, final String user, final String pass) throws Exception {
        url = new JMXServiceURL(urls);
        final Map<String, Object> env = new HashMap<String, Object>();
        env.put("jmx.remote.credentials", new String[]{user, pass});
        JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
        System.out.println("Connected, to url: " + urls +  ", connection id = " + jmxc.getConnectionId());
    }
    
    public static void main(final String[] args) throws Exception {
        if (args == null || args.length != 3) {
            System.err.println("Usage: java org.jm.jmx.utils.JustConnect jmxserviceurl username password" +
                    "\n(username and password could be any string, if server is not authenticating)");
            System.exit(1);
        }
        new JustConnect(args[0], args[1], args[2]);
    }
}
