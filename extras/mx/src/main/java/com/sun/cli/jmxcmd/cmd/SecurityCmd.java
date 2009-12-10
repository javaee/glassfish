/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/SecurityCmd.java,v 1.7 2004/10/14 19:06:21 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2004/10/14 19:06:21 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import java.util.Set;
import java.util.HashSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;



import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import org.glassfish.admin.amx.util.stringifier.X509CertificateStringifier;


import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;



import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import com.sun.cli.jcmd.util.security.TrustAnyTrustManager;
import com.sun.cli.jcmd.util.security.HandshakeCompletedListenerImpl;
import org.glassfish.admin.amx.util.StringUtil;


/**
	Manages connections to MBeanServers.
 */
public class SecurityCmd extends JMXCmd
{
		public
	SecurityCmd( final CmdEnv env )
	{
		super( env );
	}
	

		public CmdHelp
	getHelp()
	{
		return( new SecurityCmdHelp() );
	}
	
	
	
	public final class SecurityCmdHelp extends CmdHelpImpl
	{
		public	SecurityCmdHelp()	{ super( getCmdInfos() ); }
		
		
		static final String	SYNOPSIS			= "manage security";
		
		private final static String	TEXT		=
		"TBD" +
		"";
		
		public String	getName()		{ return( OVERALL_NAME ); }
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TEXT ); }
	}
	
	final static String	OVERALL_NAME			= "security";
	final static String	ADD_CONNECTION_CERT		= "add-connection-cert";
	final static String	DUMP_KEYSTORE			= "dump-keystore";
	final static String	LIST_KEYSTORE_ALIASES	= "list-keystore-aliases";
	
	
	private final static OptionInfo HOST_OPTION		= new OptionInfoImpl( "host", "h", "host-or-ip");
	private final static OptionInfo PORT_OPTION		= new OptionInfoImpl( "port", "p", "port-number", true );

	
	static private final OptionInfo[]	ADD_CONNECTION_CERT_OPTIONS	=
	{
		HOST_OPTION,
		PORT_OPTION,
	};
	
	
	private final static CmdInfo	ADD_CONNECTION_CERT_INFO	=
		new CmdInfoImpl( ADD_CONNECTION_CERT,
				new OptionsInfoImpl( ADD_CONNECTION_CERT_OPTIONS ),
				new OperandsInfoImpl( "file[ file]", 1) );
				
	private final static CmdInfo	LIST_KEYSTORE_ALIASES_INFO	=
		new CmdInfoImpl( LIST_KEYSTORE_ALIASES,
				new OptionsInfoImpl( ),
				new OperandsInfoImpl( "file", 1) );
				
	private final static CmdInfo	DUMP_KEYSTORE_INFO	=
		new CmdInfoImpl( DUMP_KEYSTORE,
				new OptionsInfoImpl( ),
				new OperandsInfoImpl( "file", 1) );
		
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( ADD_CONNECTION_CERT_INFO,
				LIST_KEYSTORE_ALIASES_INFO, DUMP_KEYSTORE_INFO) );
	}

	
		private String
	getUniqueAlias( KeyStore ks )
		throws KeyStoreException
	{
		final String	nameBase	= "trustedKey";
		String	alias	= nameBase;
		
		int	i = 1;
		while ( ks.containsAlias( alias ) )
		{
			alias	= nameBase + i;
			++i;
		}
		
		return( alias );
	}
	
		boolean
	existsInKeyStore( KeyStore ks, Certificate cert )
		throws KeyStoreException
	{
		return( ks.getCertificateAlias( cert ) != null );
	}
	
		private KeyStore
	loadKeyStore( String fileName, final char[] password )
		throws Exception
	{
		final File		file	= new File( fileName );
		final KeyStore	ks	= KeyStore.getInstance( "JKS" );
		
		if ( file.exists() )
		{
			final FileInputStream	is	= new FileInputStream( file );
			try
			{
				ks.load( is, password );
			}
			catch( Exception e )
			{
				is.close();
				throw e;
			}
		}
		else
		{
			ks.load( null, password );
		}
		
		return( ks );
	}
	
		private boolean
	addToKeyStore( String fileName, Certificate cert, final char[] password )
		throws Exception
	{
		boolean	added	= false;
		
		final File		file	= new File( fileName );
		final KeyStore	ks	= loadKeyStore( fileName, password );
		
		if ( ! existsInKeyStore( ks, cert ) )
		{
			final String	alias	= getUniqueAlias( ks );
			ks.setCertificateEntry( alias, cert );
			assert( ks.isCertificateEntry( alias ) );
			added	= true;
			
			final FileOutputStream	os	= new FileOutputStream( file );
			try
			{
				ks.store( os, password );
			}
			catch( Exception e )
			{
				os.close();
				throw e;
			}
		}
		
		return( added );
	}
	
		char[]
	getPassword( String file )
		throws IOException
	{
		final char[]	passwordChars	=
			promptUserSecure( "\nPlease enter the password for the keystore " +
				StringUtil.quote( file ) + ": " );
		
		return( passwordChars );
	}
	
		private void
	handleAddConnectionCert( String host, int port, String[] files )
		throws Exception
	{
		// establish an SSL connection so that we can get the Certificate
		final SSLContext		sslContext	= SSLContext.getInstance( "TLSv1" );
		sslContext.init( null, new TrustManager[] { TrustAnyTrustManager.getInstance() }, null);
		final SSLSocketFactory	sslFactory	= sslContext.getSocketFactory();
		
		final Socket			rawSocket	= new Socket( host, port );
		final SSLSocket			sslSocket	= (SSLSocket)sslFactory.createSocket( rawSocket, host, port, true );
		
		final HandshakeCompletedListenerImpl	myListener	= new HandshakeCompletedListenerImpl();
		sslSocket.addHandshakeCompletedListener( myListener );
		
		sslSocket.setUseClientMode( true );
		sslSocket.startHandshake();
		while ( myListener.getLastEvent() == null )
		{
		}
		final HandshakeCompletedEvent	event	= myListener.getLastEvent();
		final Certificate				cert	= event.getPeerCertificates()[ 0 ];
				
		println( X509CertificateStringifier.stringify( (X509Certificate)cert ) );
		
		final String	response	= promptUser( "Save this certicate as a trusted certificate?" );
		if ( response.equalsIgnoreCase( "y" ) || response.equalsIgnoreCase( "yes" ) )
		{
			final char[]	passwordChars	= getPassword( files[ 0 ] );
			try
			{
				if ( ! addToKeyStore( files[ 0 ], cert, passwordChars ) )
				{
					println( "The certificate was already present in file " + quote( files[ 0 ] )  );
				}
				
				for ( int i = 1; i < files.length; ++i )
				{
					if ( ! addToKeyStore( files[ i ], cert, passwordChars ) )
					{
						println( "The certificate was already present in file " + quote( files[ i ] )  );
					}
				}
			}
			catch( Exception e )
			{
				for( int i = 0; i < passwordChars.length; ++i )
				{
					passwordChars[ i ]	= 'x';
				}
				throw e;
			}
		}
		else
		{
			println( "The certificate was NOT trusted.  No change was made." );
		}
	}
	
		
		private void
	getAliases( KeyStore ks, Set<String> trustedCertificateAliases, Set<String> keyAliases)
		throws KeyStoreException
	{
                for( final String alias : keyAliases )
		{
			if ( ks.isCertificateEntry( alias ) )
			{
				if ( trustedCertificateAliases != null )
				{
					trustedCertificateAliases.add( alias );
				}
			}
			else if ( ks.isKeyEntry( alias ) )
			{
				if ( keyAliases != null )
				{
					keyAliases.add( alias );
				}
			}
		}
	}
	
		private String[]
	getTrustedCertificateAliases( KeyStore ks )
		throws KeyStoreException
	{
		final Set<String>	trustedCertificateAliases	= new HashSet<String>();
		
		getAliases( ks, trustedCertificateAliases, null );
		return( (String[])
			trustedCertificateAliases.toArray( new String[ trustedCertificateAliases.size() ] ) );
	}
	
	
		private String[]
	getKeyAliases( KeyStore ks )
		throws KeyStoreException
	{
		final Set<String>	keyAliases	= new HashSet<String>();
		
		getAliases( ks, null, keyAliases );
		return( (String[])
			keyAliases.toArray( new String[ keyAliases.size() ] ) );
	}


		private void
	handleListAliases( String file )
		throws Exception
	{
		final char[]	passwordChars	= getPassword( file );
		final KeyStore	ks	= loadKeyStore( file, passwordChars );
		
		final String[]	trustedCertificateAliases	= getTrustedCertificateAliases( ks );
		final String[]	keyAliases	= getKeyAliases( ks );
		
		
		println( "Trusted certificate aliases:\n" +
			ArrayStringifier.stringify( trustedCertificateAliases, "\n" ) );
			
		println( "\nKey aliases:\n" +
			ArrayStringifier.stringify( keyAliases, "\n" ) );
		println( "" );
			
	}
	
		private void
	handleDumpKeyStore( String file )
		throws Exception
	{
		final char[]	passwordChars	= getPassword( file );
		final KeyStore	ks	= loadKeyStore( file, passwordChars );
		
		final String[]	certAliases	= getTrustedCertificateAliases( ks );
		for( int i = 0; i < certAliases.length; ++i )
		{
			final String	alias	= certAliases[ i ];
			final Certificate	cert	= ks.getCertificate( alias );
			
			println( alias + ":" );
			println( X509CertificateStringifier.stringify( (X509Certificate)cert ) );
			println( "");
		}
		
		
		final String[]	keyAliases	= getKeyAliases( ks );
		for( int i = 0; i < keyAliases.length; ++i )
		{
			final String	alias	= keyAliases[ i ];
			final Key	key	= ks.getKey( alias, passwordChars );
			
			println( alias + ":" );
			println( "Algorithm: " + key.getAlgorithm() );
			println( "");
		}
	}
	
		protected void
	establishProxy()
		throws Exception
	{
		// defeat default behavior; this is the 'connect' command after all
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd			= getSubCmdNameAsInvoked();
		final String [] operands	= getOperands();

		if ( cmd.equals( ADD_CONNECTION_CERT ) )
		{
			final String	host	= getString( HOST_OPTION.getShortName(), "localhost" );
			final int		port	= getInteger( PORT_OPTION.getShortName(), null ).intValue();
			handleAddConnectionCert( host, port, operands );
		}
		else if ( cmd.equals( LIST_KEYSTORE_ALIASES ) )
		{
			handleListAliases( operands[ 0 ] );
		}
		else if ( cmd.equals( DUMP_KEYSTORE ) )
		{
			handleDumpKeyStore( operands[ 0 ] );
		}
	}
	
}






