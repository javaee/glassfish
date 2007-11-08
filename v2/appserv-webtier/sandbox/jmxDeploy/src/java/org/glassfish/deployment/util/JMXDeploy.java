/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.deployment.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 * Small Util to reload/restart a WebModule using JMX.
 *
 * @author Jeanfrancois Arcand
 */
public class JMXDeploy{

    private final static String mBeanName = "com.sun.appserv:" 
             + "J2EEApplication=null,J2EEServer=server," 
             + "j2eeType=WebModule,name=//server";
    
    private static URLConnection conn = null;
    private static URL url;
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;  
    private static String adminUser = null;
    private static String adminPassword = null;
    private static int adminPort = 4848;
    private static String contextRoot= "";
    private static String host = "localhost";
    
    public static void main(String args[]) throws Exception{
        try{
            String command = args[0];
            host = args[1];
            contextRoot = "/" + args[2];
            adminUser = args[3];
            adminPassword = args[4];
            adminPort = Integer.parseInt(args[5]);

            if ( command.equals("--restart")){
                restart(mBeanName + contextRoot);
            } else if (command.equals("--reload")){
                reload(mBeanName + contextRoot);
            }
            System.out.println("DEPLOYMENT SUCCESS");
        } catch(Throwable ex){
            System.out.println("Usage\n");
            System.out.println("jmxReload --reload|restart");
            System.out.print(" [contextRoot]");
            ex.printStackTrace();
            System.out.println("DEPLOYMENT FAILED");
        }
    }

    
    private static void restart(String oName) throws Throwable{
        ObjectName appMBean = new ObjectName(oName);
        
        try {
            restartHttp(appMBean);
        } catch (Throwable t){
            restartHttps(appMBean);
        }
    }


    private static void reload(String oName) throws Throwable{
        ObjectName appMBean = new ObjectName(oName);

        try {
            reloadHttp(appMBean);
        } catch (Throwable t){
            t.printStackTrace();
            reloadHttps(appMBean);
        }
     }


    /**
     * Reload the Context using JMX and HTTP.
     */
    private static void reloadHttp(ObjectName appMBean) throws Throwable {

        Object[] params = new Object[0];
        String[] signature = new String[0];
        System.out.println("Reload Context: " + contextRoot);

        Object o= getMBeanServerConnection().
            invoke(appMBean, "reload", params, signature);
    }


    /**
     * Reload the Context using JMX and HTTP.
     */
    private static void restartHttp(ObjectName appMBean) throws Throwable {
        Object[] params = new Object[0];
        String[] signature = new String[0];
        System.out.println("Stopping Context: " + contextRoot);

        Object o= getMBeanServerConnection().
            invoke(appMBean, "stop", params, signature);

        System.out.println("Starting Context: " + contextRoot);

        o= getMBeanServerConnection()
            .invoke(appMBean, "start", params, signature);
    }

    
    /**
     * Local the MBeanServer.
     */
    private static MBeanServerConnection getMBeanServerConnection() 
                                                            throws Throwable{
        return getMBeanServerConnection(host,adminPort,adminUser,adminPassword);
    }


    /**
     * Get an Server Connection.
     */
    private static MBeanServerConnection getMBeanServerConnection
                                            (String host, 
                                             int port,
                                             String user, 
                                             String password) throws Throwable{

        final JMXServiceURL url = 
            new JMXServiceURL("service:jmx:s1ashttp://" + host + ":" + port);
        final Map env = new HashMap();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";
        
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(ADMIN_USER_ENV_PROPERTY_NAME, user );
        env.put( ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);
        env.put(HTTP_AUTH_PROPERTY_NAME, DEFAULT_HTTP_AUTH_SCHEME);
        final JMXConnector conn = JMXConnectorFactory.connect(url, env);
        return conn.getMBeanServerConnection();
    }


    /**
     * Reload the Context using JMX and HTTPs
     */
    private static void reloadHttps(ObjectName appMBean) throws Throwable {
        Object[] params = new Object[0];
        String[] signature = new String[0];
        System.out.println("Reloading Context: " + contextRoot);

        Object o= getSecureMBeanServerConnection()
                    .invoke(appMBean, "reload", params, signature);

    }


    /**
     * Reload the Context using JMX and HTTPs
     */
    private static void restartHttps(ObjectName appMBean) throws Throwable {
        Object[] params = new Object[0];
        String[] signature = new String[0];
        System.out.println("Stopping Context: " + contextRoot);

        Object o= getSecureMBeanServerConnection()
                    .invoke(appMBean, "stop", params, signature);

        System.out.println("Starting Context: " + contextRoot);

        o= getSecureMBeanServerConnection().invoke(
            appMBean, "start", params, signature);
    }

    
    /**
     * Get a secure JMX connection.
     */
    private static MBeanServerConnection getSecureMBeanServerConnection() 
                                                            throws Throwable{
       return getSecureMBeanServerConnection(host,adminPort,adminUser,
                adminPassword); 
    }


    /**
     * Get a secure JMX connection.
     */
    private static MBeanServerConnection 
        getSecureMBeanServerConnection(String host, 
                                       int port,
                                       String user, 
                                       String password) throws Throwable{

        final JMXServiceURL url = new JMXServiceURL("service:jmx:s1ashttps://" +
                                  host + ":" + port);
        final Map env = new HashMap();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";
                                                                            
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(ADMIN_USER_ENV_PROPERTY_NAME, user );
        env.put( ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);
        env.put(HTTP_AUTH_PROPERTY_NAME, DIGEST_HTTP_AUTH_SCHEME);
        final JMXConnector conn = JMXConnectorFactory.connect(url, env);
        return conn.getMBeanServerConnection();
    }
    
// --------------------------------------------------------------- AMX related


    public static final String ADMIN_USER_ENV_PROPERTY_NAME = "USER";
    public static final String ADMIN_PASSWORD_ENV_PROPERTY_NAME = "PASSWORD";
    public static final String TRUST_MANAGER_PROPERTY_NAME = "TRUST_MANAGER_KEY";
    public static final String KEY_MANAGER_PROPERTY_NAME= "KEYMANAGER_KEY";
    public static final String SSL_SOCKET_FACTORY = "SSL_SOCKET_FACTORY";
    public static final String HOSTNAME_VERIFIER_PROPERTY_NAME = "HOSTNAME_VERIFIER_KEY";
    public static final String STRING_MANAGER_CLASS_NAME = "STRING_MANAGER_CLASS_KEY";
    public static final String DEFAULT_TRUST_MANAGER  = "com.sun.enterprise.security.trustmanager.SunOneBasicX509TrustManager";
    public static final String SERVLET_CONTEXT_PROPERTY_NAME = "com.sun.enterprise.as.context.root";
    public static final String HTTP_AUTH_PROPERTY_NAME = "com.sun.enterprise.as.http.auth";
    public static final String DEFAULT_SERVLET_CONTEXT_ROOT = "/web1/remotejmx"; /* This is to be in sync with the web.xml */
    public static final String DEFAULT_HTTP_AUTH_SCHEME = "BASIC";
    public static final String DIGEST_HTTP_AUTH_SCHEME = "Digest";
    
    public static final String S1_HTTP_PROTOCOL = "s1ashttp";
    public static final String S1_HTTPS_PROTOCOL = "s1ashttps";
}
