

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.tomcat.util.net.jsse;

import org.apache.tomcat.util.net.SSLSupport;
import java.io.*;
import java.net.*;
import java.util.Vector;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLException;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;
import java.security.cert.CertificateFactory;
// START SJSAS 6439313
import javax.net.ssl.SSLEngine;
// END SJSAS 6439313

/* JSSESupport

   Concrete implementation class for JSSE
   Support classes.

   This will only work with JDK 1.2 and up since it
   depends on JDK 1.2's certificate support

   @author EKR
   @author Craig R. McClanahan
   Parts cribbed from JSSECertCompat       
   Parts cribbed from CertificatesValve
*/

class JSSE14Support extends JSSESupport {

    private static com.sun.org.apache.commons.logging.Log logger =
        com.sun.org.apache.commons.logging.LogFactory.getLog(JSSE14Support.class);

    Listener listener = new Listener();

    public JSSE14Support(SSLSocket sock){
        super(sock);
        sock.addHandshakeCompletedListener(listener);
    }

    // START SJSAS 6439313
    public JSSE14Support(SSLEngine sslEngine){
        super(sslEngine);
    }
    // END SJSAS 6439313
    
    protected void handShake() throws IOException {
        ssl.setNeedClientAuth(true);
        synchronousHandshake(ssl);        
    }

    /**
     * JSSE in JDK 1.4 has an issue/feature that requires us to do a
     * read() to get the client-cert.  As suggested by Andreas
     * Sterbenz
     */
    private  void synchronousHandshake(SSLSocket socket) 
        throws IOException {
        InputStream in = socket.getInputStream();
        int oldTimeout = socket.getSoTimeout();
        socket.setSoTimeout(1000);
        byte[] b = new byte[0];
        listener.reset();
        socket.startHandshake();
        int maxTries = 60; // 60 * 1000 = example 1 minute time out
        for (int i = 0; i < maxTries; i++) {
	    if(logger.isTraceEnabled())
		logger.trace("Reading for try #" +i);
            try {
                int x = in.read(b);
            } catch(SSLException sslex) {
                logger.info("SSL Error getting client Certs",sslex);
                throw sslex;
            } catch (IOException e) {
                // ignore - presumably the timeout
            }
            if (listener.completed) {
                break;
            }
        }
        socket.setSoTimeout(oldTimeout);
        if (listener.completed == false) {
            throw new SocketException("SSL Cert handshake timeout");
        }
    }

    /** Return the X509certificates or null if we can't get them.
     *  XXX We should allow unverified certificates 
     */ 
    protected X509Certificate [] getX509Certificates(SSLSession session) 
	throws IOException 
    {
        Certificate [] certs=null;
        try {
	    certs = session.getPeerCertificates();
        } catch( Throwable t ) {
            logger.debug("Error getting client certs",t);
            return null;
        }
        if( certs==null ) return null;
        
        X509Certificate [] x509Certs = new X509Certificate[certs.length];
	for(int i=0; i < certs.length; i++) {
	    if( certs[i] instanceof X509Certificate ) {
		// always currently true with the JSSE 1.1.x
		x509Certs[i] = (X509Certificate)certs[i];
	    } else {
		try {
		    byte [] buffer = certs[i].getEncoded();
		    CertificateFactory cf =
			CertificateFactory.getInstance("X.509");
		    ByteArrayInputStream stream =
			new ByteArrayInputStream(buffer);
		    x509Certs[i] = (X509Certificate)
			cf.generateCertificate(stream);
		} catch(Exception ex) { 
		    logger.info("Error translating cert " + certs[i], ex);
		    return null;
		}
	    }
	    if(logger.isTraceEnabled())
		logger.trace("Cert #" + i + " = " + x509Certs[i]);
	}
	if(x509Certs.length < 1)
	    return null;
	return x509Certs;
    }


    private static class Listener implements HandshakeCompletedListener {
        volatile boolean completed = false;
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            completed = true;
        }
        void reset() {
            completed = false;
        }
    }

}

