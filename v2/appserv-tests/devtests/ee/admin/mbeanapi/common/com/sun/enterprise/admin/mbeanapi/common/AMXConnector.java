/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.enterprise.admin.mbeanapi.common;

import java.io.File;
import java.io.IOException;

import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;
import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.TrustStoreTrustManager;

import com.sun.appserv.management.DomainRoot;

/**
 * Creates a connection to a specified DAS instance
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Aug 23, 2004
 * @version $Revision: 1.2 $
 */
public class AMXConnector {
    private final DomainRoot			mDomainRoot;
    private final AppserverConnectionSource	mAppserverConnectionSource;
    private HandshakeCompletedListenerImpl	mHandshakeCompletedListener;

    private final static String	TRUST_STORE_FILE		= "./keystore";
    private final static String	TRUST_STORE_PASSWORD	= "changeme";

    // currently it defaults to using rmi transport. 
    public AMXConnector(
        final String	host,
        final int		port,
        final String	user,
        final String	password,
        final boolean	useTLS )
        throws IOException
    {
        mHandshakeCompletedListener	= null;

        TLSParams	tlsParams	= null;

        if ( useTLS )
        {
            tlsParams	= createTLSParams( );
        }

        final String info = "host=" + host + ", port=" + port +
            ", user=" + user + ", password=" + password +
            ", tls=" + useTLS;

        info( "Connecting: " + info );

        mAppserverConnectionSource	=
            new AppserverConnectionSource( AppserverConnectionSource.PROTOCOL_RMI,
            host, port, user, password, tlsParams, null);


        mDomainRoot	= mAppserverConnectionSource.getDomainRoot();

        info( "Connection established successfully: " + info );
        if ( useTLS )
        {
            assert( mHandshakeCompletedListener.getLastEvent() != null );
            info( "HandshakeCompletedEvent: " + mHandshakeCompletedListener.getLastEvent() );
        }
    }

    private TLSParams createTLSParams()
    {
        final File trustStore	= new File( TRUST_STORE_FILE );
        final char[] trustStorePassword	= TRUST_STORE_PASSWORD.toCharArray();

        mHandshakeCompletedListener	= new HandshakeCompletedListenerImpl();
        final TrustStoreTrustManager trustMgr =
            new TrustStoreTrustManager( trustStore, trustStorePassword);
        trustMgr.setPrompt( true );

        final TLSParams	tlsParams = new TLSParams( trustMgr, mHandshakeCompletedListener );

        return( tlsParams );
    }

    private static void info( final Object o )
    {
        System.out.println( o.toString() );
    }

    public DomainRoot   getDomainRoot()
    {
        return( mDomainRoot );
    }


    public AppserverConnectionSource   getAppserverConnectionSource()
    {
        return( mAppserverConnectionSource );
    }
}
