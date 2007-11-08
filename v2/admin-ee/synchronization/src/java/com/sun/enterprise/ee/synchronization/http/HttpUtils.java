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
package com.sun.enterprise.ee.synchronization.http;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import com.sun.enterprise.security.SSLUtils;
import sun.misc.BASE64Encoder;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.InstanceConfig;
import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.util.io.Utils;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.HttpListener;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import com.sun.enterprise.config.ConfigException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;


/**
 * Utility file to manage HTTP POST operation.
 *
 * @author Satish Viswanatham
 * @since  JDK1.5
 */
public class HttpUtils {

    /**
     * This method returns the full URL for the SynchronizationServlet
     * which is running on DAS.
     *
     * @param   ctx   config context
     * @return  synchronization url
     *
     * @throws  ConfigException  if a parsing error
     * @throws  IOException      if an i/o erro 
     */
    public static String getSynchronizationURL(ConfigContext ctx) 
            throws ConfigException, IOException  {

        String url = null;

        if (ctx != null) {
            DASPropertyReader dpr = new DASPropertyReader(new InstanceConfig());
            dpr.read();
            url = getSynchronizationURL(ctx, dpr);
        }

        return url;
    }


    /**
     * This method returns the full URL for the SynchronizationServlet
     * which is running on DAS.
     *
     * @param   ctx   config context
     * @param   dpr   DAS property reader
     *
     * @return  synchronization url
     *
     * @throws  ConfigException  if a parsing error
     * @throws  IOException      if an i/o erro 
     */
    public static String getSynchronizationURL(ConfigContext ctx, 
            DASPropertyReader dpr) throws ConfigException, IOException {

        String url = null;

        HttpListener listener = ServerHelper.getHttpListener(ctx, 
                SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME, 
                ServerHelper.ADMIN_HTTP_LISTNER_ID);

        String host = dpr.getHost();
        String port = listener.getPort();

        if ((host == null) || (port == null)) {
            String msg = 
                _localStrMgr.getString("synchronizationUrlError", host, port);
            throw new IllegalArgumentException(msg);
        }
            
        if (listener.isSecurityEnabled()) {
            url = "https://"+host+":"+port+SYNCHRONIZATION_URI;
        } else {
            url = "http://"+host+":"+port+SYNCHRONIZATION_URI;
        }

       return url;
    }

    /**
     * Returns true if given url resource is alive. It creates a HTTP 
     * connection and sends a GET request. It checks the returned status code.
     * If status code is HttpURLConnection.HTTP_OK, it returns true.
     *
     * @param  url   url to be pinged (example, Synchronization servlet url)
     *
     * @return  true if url resource is alive
     */
    public static boolean ping(String url) {

        boolean tf = false;

        try {
            HttpURLConnection conn = 
                (HttpURLConnection) getConnection(url, false);

            // get response code
            int response = conn.getResponseCode();

            // response code is OK
            if (response == HttpURLConnection.HTTP_OK) {
                tf = true;
            }
            conn.disconnect();
        } catch (Exception e) {
            // ignore
        }

        return tf;
    }

    /**
     * Returns a HTTP connection for the given URL.
     *
     * @param  url   url (synchronization url)
     * @param  post  true when the connection is using HTTP POST
     *
     * @return  a HTTP connection
     */
    public static URLConnection getConnection(String url, 
            boolean post) throws IOException, NoSuchAlgorithmException, 
            KeyManagementException, Exception {

        URL sUrl = new URL(url);
        URLConnection conn = sUrl.openConnection();
        if (post) {
            ((HttpURLConnection) conn).setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
        }

        if (conn instanceof HttpsURLConnection) {
            // installs the trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            TrustManager[] trust = new TrustManager[] {
                 new X509TrustManagerImpl()
            };
            sc.init(SSLUtils.getKeyManagers(), trust, new SecureRandom());
            SSLSocketFactory sf = sc.getSocketFactory();
            ((HttpsURLConnection)conn).setSSLSocketFactory(sf);
        }

        // get user and password
        String user = IdentityManager.getUser();
        String password = IdentityManager.getPassword();

        // encode user and password
        byte[] encodedPassword = (user + ":" + password).getBytes();
        BASE64Encoder encoder = new BASE64Encoder();
        String auth = encoder.encode(encodedPassword);
        conn.setRequestProperty("Authorization", "Basic " + auth);

        // create connection
        conn.connect();

        return conn;
    }

    /**
     * This method downloads the files that need to be synchronized from the 
     * SynchronizationServlet in the form of a zip file.
     *
     * @param   url  synchronization servlet url
     * @param   synReq  synchronization request
     *
     * @return  downloaded zip file
     *
     * @exception  IOException  if an error while copying the content
     */
    public static File downloadFile(String url, SynchronizationRequest synReq)
            throws IOException, NoSuchAlgorithmException, 
            KeyManagementException, Exception {

        // create connection to synchronization servlet
        URLConnection conn = getConnection(url, true);

        // send synchronization request
        OutputStreamWriter out = 
            new OutputStreamWriter(conn.getOutputStream());
        out.write(synReq.getPostData());
        out.write("\r\n");
        out.flush();
        out.close();

        // response from synchronization servlet
        BufferedInputStream bis = 
            new BufferedInputStream(conn.getInputStream());

        // write output stream to a temp zip file
        File zipFile = Utils.getTempZipFile();
        FileOutputStream fout = new FileOutputStream(zipFile);
        BufferedOutputStream bos=new BufferedOutputStream(fout);

        byte[] buffer=new byte[BUFFER_SIZE];//byte buffer
        int bytesRead=0;

        while (true) {
            //bytesRead returns the actual number of bytes read from
            //the stream. returns -1 when end of stream is detected
            bytesRead=bis.read(buffer,0,BUFFER_SIZE);

            if (bytesRead == -1) {
                break;            
            }
            bos.write(buffer,0,bytesRead);
        }
        bis.close();
        bos.close();
        ((HttpURLConnection) conn).disconnect();
        bis  = null;
        bos  = null;

        return zipFile;
    }

    // ---- VARIABLE(S) - PRIVATE ---------------------------------
    protected static final int BUFFER_SIZE           = 131072; // 128 kb
    private static final String SYNCHRONIZATION_URI  = 
                                        "/web1/SynchronizationServlet?";
    private static final StringManager _localStrMgr  = 
                            StringManager.getManager(HttpUtils.class);
    private static Logger _logger                    = 
                Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
}
