/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/rmi/RMISSLServerSocketFactory.java,v 1.4 2004/07/26 23:03:48 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/07/26 23:03:48 $
 */
package com.sun.cli.jmxcmd.security.rmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;


public class RMISSLServerSocketFactory
    implements RMIServerSocketFactory
{
    private final SSLServerSocketFactory mFactory;
    
    	public
    RMISSLServerSocketFactory(
    	final File 		keystore,
    	final char[]	keystorePassword,
    	final char[]	keyPassword )
    	throws IOException
    {
    	mFactory	= createSocketFactory( "JKS", keystore, keystorePassword, keyPassword );
    }
    
		private final static SSLServerSocketFactory
	createSocketFactory(
		final String	keystoreType,
    	final File 		keystore,
    	final char[]	keystorePassword,
    	final char[]	keyPassword )
		throws IOException
	{
		SSLServerSocketFactory	factory	= null;
		FileInputStream			in	= new FileInputStream( keystore );
		
        try
        {
            final KeyStore ks = KeyStore.getInstance( keystoreType );
            
            ks.load( in, keystorePassword);
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keyPassword == null ? keystorePassword : keyPassword );
            
            final SSLContext ctx = SSLContext.getInstance( "TLSv1" );
            ctx.init( kmf.getKeyManagers(), null, null);
            
            factory = ctx.getServerSocketFactory();
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw (IOException) new IOException().initCause(e);
        }
        finally
        {
        	in.close();
        }
        
        return( factory );
	}
	
	

		public ServerSocket
    createServerSocket( final int port) throws IOException
    {
    	final SSLServerSocket	socket	=
    		(SSLServerSocket) mFactory.createServerSocket( port );
    	
    	socket.setEnabledProtocols( RMISSLClientSocketFactory.getSupportedProtocols() );
    	socket.setEnabledCipherSuites( RMISSLClientSocketFactory.getSupportedCipherSuites() );
    	
    	// we don't support it, so don't ask
		socket.setWantClientAuth( false );
		
    	// we don't need it, so turn it off
		socket.setUseClientMode( false );
		
		/*
        final String[]	suites	= socket.getEnabledCipherSuites();
        for( int i = 0; i < suites.length; ++i )
        {
       	 System.out.println( suites[ i ] );
        }
        
        final String[]	protocols	= socket.getEnabledProtocols();
        for( int i = 0; i < protocols.length; ++i )
        {
       	 System.out.println( protocols[ i ] );
        }
        */
        
        return socket;
    }
    
    	public String
    toString()
    {
    	final String	DELIM	= ", ";
    	
    	return( getClass().getName() + " with protocols " +
    		ArrayStringifier.stringify( RMISSLClientSocketFactory.getSupportedProtocols(), DELIM) +
    		" and cipher suites " +
    		ArrayStringifier.stringify( RMISSLClientSocketFactory.getSupportedCipherSuites(), DELIM) );
    }
}






