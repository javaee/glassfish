/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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






