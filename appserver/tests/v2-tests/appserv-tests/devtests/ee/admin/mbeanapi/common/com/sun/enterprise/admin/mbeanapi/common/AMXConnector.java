/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
