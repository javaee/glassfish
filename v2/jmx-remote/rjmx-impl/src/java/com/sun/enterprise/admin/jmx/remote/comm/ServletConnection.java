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

/* CVS information
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/comm/ServletConnection.java,v 1.7 2007/03/28 02:37:33 ne110415 Exp $
 * $Revision: 1.7 $
 * $Date: 2007/03/28 02:37:33 $
*/

package com.sun.enterprise.admin.jmx.remote.comm;

//jdk imports
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
/* BEGIN -- S1WS_MOD */
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
/* END -- S1WS_MOD */

import com.sun.appserv.management.client.RedirectException;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
/* BEGIN -- S1WS_MOD */
import com.sun.enterprise.admin.jmx.remote.streams.*;
/* END -- S1WS_MOD */

/** A packager private Class (implementing {@link IConnection} communicating 
 * with Servlet over HTTP. Internally it uses java.net.URLConnection,
 * for we may need to use it for both HTTP and HTTPS. In case of
 * java.net.HttpURLConnection, only HTTP can be used. The server side (servlet etc)
 * has to be configured as documented, for this connection to work.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */

class ServletConnection implements IConnection, Runnable {
	static final String         UNKNOWN_HOST            = "Unknown host : ";
	static final String         UNAUTHORIZED_ACCESS     =
	"Invalid user or password";
	
	private URLConnection        mConnection           = null;
	private ObjectOutputStream   mObjectOutStream      = null;
	private ObjectInputStream    mObjectInStream       = null;
        private HttpConnectorAddress hca;
        private URL uri;
        boolean isRedirectionEnabled = true; //default: redirection enabled
        
/* BEGIN -- S1WS_MOD */
    private static final Logger logger = Logger.getLogger(
        DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
        DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );*/

    private boolean response_received =false;
    private int streamSize = 0;
    private int reqSize = 0;
/* END -- S1WS_MOD */

    ServletConnection(HttpConnectorAddress a) throws IOException {
        try{
            String uri = a.getPath();
            if (uri == null || uri.trim().length() == 0)
                uri = DefaultConfiguration.DEFAULT_SERVLET_CONTEXT_ROOT;
            mConnection = a.openConnection(uri);
            this.hca = a;
            String flag = System.getProperty(
                    DefaultConfiguration.REDIRECTION_DETECTION);
            if (flag != null && flag.equalsIgnoreCase("false"))
                isRedirectionEnabled = false;            
        } catch (IOException ioe){
            handleException(ioe);
        }
    }

    /**
     Read an incoming Object.
     */
    public  Object receive(  ) 
    throws IOException, ClassNotFoundException, RedirectException {
	Object value = null;
        try {            
            if (isRedirectionEnabled) {
                int respCode = ((HttpURLConnection)mConnection).getResponseCode();
                if (respCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String redirectUrl = 
                        mConnection.getHeaderFields().get("Location").get(0);
                    throw new RedirectException(redirectUrl, 
                        "HTTP connection failed: 302 Moved Temporarily");
                }
            }
            InputStream in = mConnection.getInputStream();
            mObjectInStream = new ObjectInputStream(in);
            value = mObjectInStream.readObject();
            response_received = true;
            JMXInbandStream.setOutputStream(null, 0);
            StreamMBeanServerResponseMessage res = 
                (StreamMBeanServerResponseMessage) value;
            if (res.isStreamAvailable()) {
                JMXInbandStream.setIncomingStream(
                    new JMXChunkedInputStream(in));
            }           
        } catch (IOException ioe) {
            if (ioe instanceof RedirectException) throw ioe;
            else handleException(ioe);
        }
        return value;
    }
	
	/**
	 Write an object to the connection
	 */
	public  void	send(Serializable req)
            throws IOException {
        InputStream in = null;
        if (req instanceof StreamMBeanServerRequestMessage) {
            StreamMBeanServerRequestMessage reqm =
                    (StreamMBeanServerRequestMessage) req;
            JMXInbandStream.setIncomingStream(null);
            in = JMXInbandStream.getOutgoingStream();
            if (in != null) {
                reqm.setStreamAvailable(true);
                int len = (int) JMXInbandStream.getOutgoingStreamLength();

                ByteArrayOutputStream byO = new ByteArrayOutputStream();
                ObjectOutputStream oO = new ObjectOutputStream(byO);
                oO.writeObject(reqm);
                reqSize = byO.size();
                byO.reset();

                int chunks = (len/8192) + 2;
                streamSize = reqSize + len + (chunks * 4);
                ((HttpURLConnection)mConnection).setFixedLengthStreamingMode(streamSize);
                mConnection.setRequestProperty("Connection", "Close");
            }
        }
        sendReq(req);
        if (in != null)
            sendStream();
    }

    private void sendStream() {
/*        Thread thr = new Thread(this);
        thr.start();
*/
        run();
    }

    /**
     Returns the URL to which Servlet Connection has been established
     */
    public URL getURL() {
        return mConnection.getURL();
    }

    /**
     Returns the URL to which Servlet Connection has been established
     */
    HttpConnectorAddress getHttpConnectorAddress() {
        return hca;
    }

    public void run() {
        OutputStream out = null;
        try {
            out = new JMXChunkedOutputStream(mConnection.getOutputStream());
            InputStream in   = JMXInbandStream.getOutgoingStream();
            byte[] bytes = new byte[8192];
            int len = 0;
            int prLen = 0;
            int flBytes = 0;
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            JMXInbandStream.setOutputStream(null, 0);
            out.flush();
            ((JMXChunkedOutputStream)out).writeEOF(reqSize);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	private  void	sendReq( Serializable object ) throws IOException {
        response_received = false;
		try {
			mObjectOutStream = new ObjectOutputStream(
			new BufferedOutputStream(mConnection.getOutputStream()));
			mObjectOutStream.writeObject(object);
			mObjectOutStream.flush();
			//mObjectOutStream.close();
		}
		catch (IOException ioe) {
                        handleException(ioe);
		}
	}
	
	public void	close() {
		try {
			mObjectInStream.close();
			mObjectOutStream.close();
		}
		catch(Exception e) {
			throw new RuntimeException (e);
		}
	}
	
	
	private void handleException(IOException e) throws IOException {
		IOException exception = null;
		if (e instanceof java.net.UnknownHostException) {
			exception = new java.net.UnknownHostException(UNKNOWN_HOST +
			e.getMessage());
		}
		else if (e instanceof java.net.ConnectException) {
			exception = new java.net.ConnectException(createConnectExceptionMessage());
		}
		else {
			int responseCode =
			((HttpURLConnection)mConnection).getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				exception = new IOException(UNAUTHORIZED_ACCESS);
			}
			else {
				exception = e;
			}
		}
		throw exception;
	}
        
        private String createConnectExceptionMessage() {
            /* this is not internationalized yet -- this is because there is a 
               separate delivery of this code that cannot depend on other parts 
               of application server
            */
            final String h = hca.getHost();
            final int    p = hca.getPort();
            final String msg = "Unable to connect to admin-server at given host: [" + h + "] and port: [" + p + "].\nPlease check if this server is up and running and that the host and port provided are correct.";
            return ( msg );
        }
}
