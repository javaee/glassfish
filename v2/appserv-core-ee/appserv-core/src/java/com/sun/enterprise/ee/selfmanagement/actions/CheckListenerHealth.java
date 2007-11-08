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

/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package com.sun.enterprise.ee.selfmanagement.actions;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.config.serverbeans.PropertyResolver;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.security.SSLUtils;
import com.sun.logging.LogDomains;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.MalformedURLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.KeyManager;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.Callable;
import java.lang.reflect.Method;

public class CheckListenerHealth implements Callable<Boolean> {

    /** ANY address for server socket to listen to */
    static final String ANY_ADDR = "0.0.0.0" ;
    
    /** Http protocol */
    static final String HTTP_PROTOCOL = "http://";
            
    /** HTTPS protocol */
    static final String HTTPS_PROTOCOL = "https://";
    
    /** Type key name for object name */
    static final String TYPE_KEY_NAME = "type";
    
    /** Name key for name of object name */
    static final String NAME_KEY = "name";
    
    /** Type for selector */
    static final String HTTP_SELECTOR = "Selector";
    
    /** Http selector statistic API */
    static final String BUSY_PTHREADS_NAME = "currentBusyProcessorThreads";
    
    /** Http selector statistic API */
    static final String MAX_THREAD_NAME = "maxThreads";
    
    /** Name of the server whose listener is being checked */
    private Server server = null;
    
    /** Listener to check */
    private HttpListener listener = null;
    
    /** Timeout in seconds */
    private int timeoutInSeconds;
    
    /** Connect timeout */
    private int connectTimeout;
    
    /** Listener port, on which the selector works */
    private int listenerPort;
    
    /** Listeners no. of acceptor threads */
    private int countAcceptorThreads;
  
    /** isHealthy */
    boolean isHealthy = false;
    
    /** Logger for self management service */
    private static Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    /**
     * Initializes the instance of CheckListenerHealth
     * 
     * @param httpListener Server listener to check
     * @timeout Timeout in seconds, upon elapse of which listener
     *          would be consider non-responsive
     */
    public CheckListenerHealth(Server inst, HttpListener httpListener, int timeout)
               throws ConfigException {
        try {
            server = inst;
            listener = httpListener;
            timeoutInSeconds = timeout;
            String val  = httpListener.getAcceptorThreads();
            countAcceptorThreads = Integer.parseInt(val);
        
            val = httpListener.getPort();            
            PropertyResolver pr = new PropertyResolver(InstanceHangAction.configCtx,
                                                       inst.getName());
            String resolvedPort = pr.resolve(val);
            listenerPort = Integer.parseInt(resolvedPort);
            /* the default value used by LB; which has been factored in from
             *proxy config - 5 seconds
             */
            connectTimeout = 5 * 1000;
        } catch (ConfigException ex) {
            throw ex;
        }
    }
    
    /**
     * Create a trust manager that does not validate certificate chains
     *
     * @return Trust Manager trust all certificates 
     */
    private TrustManager[] setupAllTrust() {
        TrustManager[] trustAllCerts = null;
        try {
            trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };                  
        } catch (Exception ex) {
            //nop
        }
        return trustAllCerts;
    }

    /**
     * Reflects on InstanceRegistry class static method to
     * The method is used for obtaining MBeanServerConnection
     * to a instance JMX service.
     *
     * @return Method to invoke
     */
    private Method reflectInstanceRegistry() {
        Method mthd = null;

        try {
            final String INSTANCEREGISTRYCLASS = "com.sun.enterprise.ee.admin.clientreg.InstanceRegistry";
            final String OPNAME = "getInstanceConnection";

            Class reflec = Class.forName(INSTANCEREGISTRYCLASS);
            Class[] types = new Class[]{Class.forName("java.lang.String")};
            mthd = reflec.getDeclaredMethod(OPNAME,types);
        } catch (Exception ex) {
            //nop, shouldn't be the case - placeholder.
        }

        return mthd;
    }
    
    /**
     * Queries the instance statistic on http listener selector
     * to determine if the selector is unresponsive due to max'd
     * out on the threads in processor pipeline
     *
     * @return true if listener mx'd out, false otherwise.
     */
    boolean checkInstanceStatistics(String listener) {
        MBeanServerConnection mbsConn = null;
        String domain = ApplicationServer.getServerContext().getDefaultDomainName();
        String name = "http"+listenerPort;
        Hashtable<String,String> props = new Hashtable(2);
        
        /* get the MBeanserver connection to instances remote connector
         * Failure to communicate even over rmi path would further point
         * towards hang/unresponsive condition
         */
        try {
            props.put(TYPE_KEY_NAME,HTTP_SELECTOR);
            props.put(NAME_KEY,name);
            ObjectName objName = new ObjectName(domain,props);
            
            //mbsConn = InstanceRegistry.getInstanceConnection(server.getName());
            Method refMethod = reflectInstanceRegistry();
            if (refMethod == null) {

                /* shouldn't be the case, though cannot further verify instance status
                 * let it proceed with first level non-responsive check done -timeout.
                 */
                throw new Exception();
            }

            Object[] params = new Object[]{server.getName()};
            mbsConn = (MBeanServerConnection)refMethod.invoke(null,params);
            Integer val = (Integer)mbsConn.getAttribute(objName,MAX_THREAD_NAME);
            int maxThreads = val;
            val = (Integer) mbsConn.getAttribute(objName,BUSY_PTHREADS_NAME);
            int busyCount = val;
            
            if ( busyCount == countAcceptorThreads * maxThreads) {
                //the listener has max'd out
                _logger.log(Level.INFO,"sgmt.instancehang_listenermaxd",
                            new Object[]{server.getName(),listener,val.toString()});
                return false;
            } else {
                //still scope to process more processor tasks
                _logger.log(Level.INFO,"sgmt.instancehang_listenersnotmaxd",
                            new Object[]{server.getName(),listener,val});
                return true;
            }
        } catch (Exception ex) {
            // IO exception 
            return false;
        }
    }
    
    /**
     * Checks for non-responsiveness of the listener
     *
     * @return true If listener is responsive, false otherwise
     */
    public Boolean call() throws Exception {
        boolean isHealthy = true;
        
        String address = listener.getAddress();
        JMXConnectorConfig jmxCfg = null;
        String urlString = null;
        ConfigContext configCtx = InstanceHangAction.configCtx;
        
        try {
            if (address.equals(ANY_ADDR)) {
                jmxCfg = ServerHelper.getJMXConnectorInfo(configCtx,server.getName());
                address = jmxCfg.getHost();
            }
        
            boolean sslEnabled = listener.isSecurityEnabled();
            if (sslEnabled) {
                // ssl connection    
                urlString = HTTPS_PROTOCOL + address + ":" + listenerPort + "/";
                URL url = new URL(urlString);
                
                _logger.log(Level.INFO,"sgmt.instancehang_startlistcheck",
                             new Object[]{server.getName(),urlString});
                             
                TrustManager[] trustAllCerts = setupAllTrust();
                SSLContext sc = SSLContext.getInstance("SSL");
                KeyManager[] keyMgr = SSLUtils.getKeyManagers();
                sc.init(keyMgr, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection urlCon = (HttpsURLConnection)url.openConnection();
                urlCon.setConnectTimeout(connectTimeout);
                urlCon.setReadTimeout(timeoutInSeconds*1000);
                urlCon.connect();
                InputStream buffer = urlCon.getInputStream();
                buffer.read();
            } else {
                //non-ssl connection
                urlString = HTTP_PROTOCOL + address + ":" + listenerPort + "/";
                URL url = new URL(urlString);
                
                _logger.log(Level.INFO,"sgmt.instancehang_startlistcheck",
                             new Object[]{server.getName(),urlString});

                HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
                urlCon.setConnectTimeout(connectTimeout);
                urlCon.setReadTimeout(timeoutInSeconds*1000);
                urlCon.connect();
                InputStream buffer = urlCon.getInputStream();
                buffer.read();
            } 
        } catch (MalformedURLException ex) {
            //nop
        } catch (IOException ex) {
            //either connect or read failure. Server not healthy
            _logger.log(Level.WARNING,"sgmt.instancehang_listener_notresponding",
                        new Object[]{server.getName(),urlString,timeoutInSeconds});
            isHealthy = checkInstanceStatistics(urlString);
            return isHealthy;
        } catch (IllegalArgumentException ex) {
            //nop
        } catch (Exception ex) {
            //nop - config, ssl, keymanager
        }
        
        _logger.log(Level.INFO,"sgmt.instancehang_listenerresp",
                    new Object[]{server.getName(),urlString});
        return isHealthy;
    }
}
