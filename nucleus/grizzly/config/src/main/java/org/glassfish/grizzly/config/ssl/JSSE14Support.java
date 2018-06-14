/*
 * Copyright (c) 2007-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.grizzly.config.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.glassfish.grizzly.Grizzly;
// START SJSAS 6439313
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

    /**
     * Default Logger.
     */
    private final static Logger logger = Grizzly.logger(JSSE14Support.class);

    final Listener listener = new Listener();

    public JSSE14Support(SSLSocket sock){
        super(sock);
        sock.addHandshakeCompletedListener(listener);
    }

    // START SJSAS 6439313
    public JSSE14Support(SSLEngine sslEngine){
        super(sslEngine);
    }
    // END SJSAS 6439313
    
    @Override
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
	    if(logger.isLoggable(Level.FINE)) 
                logger.log(Level.FINE, "Reading for try #{0}", i);
            try {
                final int bytesRead = in.read(b);
                assert bytesRead <= 0;
            } catch(SSLException sslex) {
                //logger.log(Level.SEVERE,"SSL Error getting client Certs",sslex);
                throw sslex;
            } catch (IOException e) {
                // ignore - presumably the timeout
            }
            if (listener.completed) {
                break;
            }
        }
        socket.setSoTimeout(oldTimeout);
        if (!listener.completed) {
            throw new SocketException("SSL Cert handshake timeout");
        }
    }

    /** Return the X509certificates or null if we can't get them.
     *  XXX We should allow unverified certificates 
     */ 
    @Override
    protected X509Certificate [] getX509Certificates(SSLSession session) 
	throws IOException 
    {
        Certificate [] certs;
        try {
	    certs = session.getPeerCertificates();
        } catch( Throwable t ) {
            if (logger.isLoggable(Level.FINEST))
                logger.log(Level.FINEST,"Error getting client certs",t);
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
		    logger.log(Level.SEVERE,"Error translating cert " + certs[i], ex);
		    return null;
		}
	    }
	    if(logger.isLoggable(Level.FINE)) 
                logger.log(Level.FINE, "Cert #{0} = {1}", new Object[]{i, x509Certs[i]});
	}
	if(x509Certs.length < 1)
	    return null;
	return x509Certs;
    }


    private static class Listener implements HandshakeCompletedListener {
        volatile boolean completed = false;
        @Override
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            completed = true;
        }
        void reset() {
            completed = false;
        }
    }

}

