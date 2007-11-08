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
 * ConnectionManager.java
 *
 * Created on July 30, 2005, 12:08 AM
 */

package com.sun.enterprise.ee.admin.lbadmin.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import java.net.HttpURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.sun.enterprise.ee.admin.lbadmin.writer.LbConfigWriter;
import com.sun.enterprise.security.SSLUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.ee.EELogDomains;

/**
 *
 * @author hr124446
 */
public class ConnectionManager {

    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";
    public static final String SSL = "SSL";
    
    /** Creates a new instance of ConnectionManager */
    public ConnectionManager(String lbHost,String lbPort,String lbProxyHost,    
                            String lbProxyPort,String lbName, boolean isSecure)
    {
        _lbHost = lbHost;
        _lbPort = lbPort;
        _lbProxyHost = lbProxyHost;
        _lbProxyPort = lbProxyPort;
        _lbName = lbName;
        _isSecure = isSecure;
    }
    
    /**
     * creates a connection to the loadbalancer
     * @param contextRoot context root that will be used in constructing the URL
     * @throws java.io.IOException 
     * @return either HTTP or HTTPS connection to the load balancer.
     */
    public HttpURLConnection getConnection(String contextRoot) throws IOException{
        if (_isSecure) {
            return getSecureConnection(contextRoot);
        } else {
            return getNonSecureConnection(contextRoot);
        }
    }

    /**
     * creates a connection to the loadbalancer
     * @param contextRoot context root that will be used in constructing the URL
     * @throws java.io.IOException 
     * @return either HTTP or HTTPS connection to the load balancer.
     */
    private HttpURLConnection getNonSecureConnection(String contextRoot) throws IOException{
        if(_lbHost == null || _lbPort==null) {
            String msg = _strMgr.getString("LbDeviceNotConfigured", _lbName);
            throw new IOException(msg);
        }
        
        HttpURLConnection conn =null;
        URL url = null;
        try {
            
            //---------------------------------
            url = new URL(HTTP_PROTOCOL,_lbHost,Integer.parseInt(_lbPort),contextRoot);
            if(_lbProxyHost!=null && _lbProxyPort !=null){
                Proxy proxy = new Proxy(Proxy.Type.HTTP, 
                        new InetSocketAddress(_lbProxyHost, Integer.parseInt(_lbProxyPort)));
                conn = (HttpURLConnection)url.openConnection(proxy);
            }else {
                conn = (HttpURLConnection)url.openConnection();
            }
        } catch(Exception e){
            if(_logger.isLoggable(Level.INFO))
                e.printStackTrace();
            throw new IOException(e.getMessage());
        }finally {
        }
        return conn;
    }    

    /**
     * creates a connection to the loadbalancer
     * @param contextRoot context root that will be used in constructing the URL
     * @throws java.io.IOException 
     * @return HTTPS connection to the load balancer.
     */
    private HttpsURLConnection getSecureConnection(String contextRoot) throws IOException{
        if(_lbHost == null || _lbPort==null) {
            String msg = _strMgr.getString("LbDeviceNotConfigured", _lbName);
            throw new IOException(msg);
        }
        
        HttpsURLConnection conn =null;
        URL url = null;
        try {
            //---------------------------------
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
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
            
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance(SSL);
            sc.init(SSLUtils.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
            
            //---------------------------------
            url = new URL(HTTPS_PROTOCOL,_lbHost,Integer.parseInt(_lbPort),contextRoot);
            if(_lbProxyHost!=null && _lbProxyPort !=null){
                Proxy proxy = new Proxy(Proxy.Type.HTTP, 
                        new InetSocketAddress(_lbProxyHost, Integer.parseInt(_lbProxyPort)));
                conn = (HttpsURLConnection)url.openConnection(proxy);
            }else {
                conn = (HttpsURLConnection)url.openConnection();
            }
            conn.setSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier hnv = new SSLHostNameVerifier();
            conn.setDefaultHostnameVerifier(hnv);
        } catch(Exception e){
            if(_logger.isLoggable(Level.INFO))
                e.printStackTrace();
            throw new IOException(e.getMessage());
        }finally {
        }
        return conn;
    }    

    private String _lbHost = null;
    private String _lbPort = null;
    private String _lbProxyHost = null;
    private String _lbProxyPort = null;
    private String _lbName = null;
    private boolean _isSecure = true;
    private static final StringManager _strMgr = 
        StringManager.getManager(LbConfigWriter.class);
    private static Logger _logger = Logger.getLogger(
			EELogDomains.EE_ADMIN_LOGGER);
    
}
