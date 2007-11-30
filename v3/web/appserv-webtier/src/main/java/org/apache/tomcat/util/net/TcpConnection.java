

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

package org.apache.tomcat.util.net;

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 */
public class TcpConnection  { // implements Endpoint {
    /**
     * Maxium number of times to clear the socket input buffer.
     */
    static  int MAX_SHUTDOWN_TRIES=20;

    public TcpConnection() {
    }

    // -------------------- Properties --------------------

    PoolTcpEndpoint endpoint;
    Socket socket;

    public static void setMaxShutdownTries(int mst) {
	MAX_SHUTDOWN_TRIES = mst;
    }
    public void setEndpoint(PoolTcpEndpoint endpoint) {
	this.endpoint = endpoint;
    }

    public PoolTcpEndpoint getEndpoint() {
	return endpoint;
    }

    public void setSocket(Socket socket) {
	this.socket=socket;
    }

    public Socket getSocket() {
	return socket;
    }

    public void recycle() {
        endpoint = null;
        socket = null;
    }

    // Another frequent repetition
    public static int readLine(InputStream in, byte[] b, int off, int len)
	throws IOException
    {
	if (len <= 0) {
	    return 0;
	}
	int count = 0, c;

	while ((c = in.read()) != -1) {
	    b[off++] = (byte)c;
	    count++;
	    if (c == '\n' || count == len) {
		break;
	    }
	}
	return count > 0 ? count : -1;
    }

    
    // Usefull stuff - avoid having it replicated everywhere
    public static void shutdownInput(Socket socket)
	throws IOException
    {
	try {
	    InputStream is = socket.getInputStream();
	    int available = is.available ();
	    int count=0;
	    
	    // XXX on JDK 1.3 just socket.shutdownInput () which
	    // was added just to deal with such issues.
	    
	    // skip any unread (bogus) bytes
	    while (available > 0 && count++ < MAX_SHUTDOWN_TRIES) {
		is.skip (available);
		available = is.available();
	    }
	}catch(NullPointerException npe) {
	    // do nothing - we are just cleaning up, this is
	    // a workaround for Netscape \n\r in POST - it is supposed
	    // to be ignored
	}
    }
}


