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

import java.io.IOException;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;


import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.base.QueryMgr;

import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.stringifier.StringifierRegistryIniterImpl;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;


/**
 * This test uses com.sun.enterprise.admin.mbeanapi.common.AMXConnector.java to get a connection to the MBean Server
 * and then gets a bunch of ObjectNames and lists them.
 * <b>Usage: java -DHOST=hostname -DPORT=port -DADMIN_USER=username
 *  -D ADMIN_PASSWORD=passwd -DUSE_TLS=false/true
 *  -classpath <should include mbean api jars and impls, jmxri and jmxremote.jar>
 *  com.sun.enterprise.admin.mbeanapi.common.AMXConnectionVerifier</B>
 *
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Aug 23, 2004
 * @version $Revision: 1.2 $
 */
public class AMXConnectionVerifier {
    private final DomainRoot	mDomainRoot;

    private DomainConfig   getDomainConfig()
    {
        return( mDomainRoot.getDomainConfig() );
    }

    private QueryMgr  getQueryMgr()
    {
        return( mDomainRoot.getQueryMgr() );
    }

    public void   println( Object o )
    {
        System.out.println( toString( o ) );
    }

    public String  toString( Object o )
    {
        return( SmartStringifier.toString( o ) );
    }

    private void  listMap(final String	msg, final Map 	m)
    {
        println( msg + ": " + toString( m.keySet() ) );
    }

    private void list()
    {
        final DomainConfig	dcp	= getDomainConfig();

        // Top-level items
        println( "\n--- Top-level --- \n" );

        listMap( "ConfigConfig", dcp.getConfigConfigMap() );

        listMap( "ServerConfig", dcp.getServerConfigMap() );

        listMap( "StandaloneServerConfig", dcp.getStandaloneServerConfigMap() );

        listMap( "ClusteredServerConfig", dcp.getClusteredServerConfigMap() );

        listMap( "ClusterConfig", dcp.getClusterConfigMap() );


        // deployed items
        println( "\n--- DeployedItems --- \n" );
        listMap( "J2EEApplicationConfig", dcp.getJ2EEApplicationConfigMap() );
        listMap( "EJBModuleConfig", dcp.getEJBModuleConfigMap() );
        listMap( "WebModuleConfig", dcp.getWebModuleConfigMap() );
        //listMap( "RARModuleConfig", dcp.getRARModuleMap() );
        listMap( "AppClientModuleConfig", dcp.getAppClientModuleConfigMap() );
        listMap( "LifecycleModuleConfig", dcp.getLifecycleModuleConfigMap() );


        // resources
        println( "\n--- Resources --- \n" );

        listMap( "CustomResourceConfig", dcp.getCustomResourceConfigMap() );
        listMap( "PersistenceManagerFactoryResourceConfig",
            dcp.getPersistenceManagerFactoryResourceConfigMap() );
        listMap( "JNDIResourceConfig", dcp.getJNDIResourceConfigMap() );
        listMap( "JMSResourceConfig", dcp.getJMSResourceConfigMap() );
        listMap( "JDBCResourceConfig", dcp.getJDBCResourceConfigMap() );
        listMap( "ConnectorResourceConfig", dcp.getConnectorResourceConfigMap() );
        listMap( "JDBCConnectionPoolConfig", dcp.getJDBCConnectionPoolConfigMap() );
        listMap( "PersistenceManagerFactoryResourceConfig",
            dcp.getPersistenceManagerFactoryResourceConfigMap() );
        listMap( "ConnectorConnectionPoolResourceConfig",
            dcp.getConnectorConnectionPoolConfigMap() );
        listMap( "AdminObjectResourceConfig", dcp.getAdminObjectResourceConfigMap() );
        listMap( "ResourceAdapterConfig", dcp.getResourceAdapterConfigMap() );
        listMap( "MailResourceConfig", dcp.getMailResourceConfigMap() );


        // get a ConfigConfig
        final ConfigConfig	config	=
            (ConfigConfig)dcp.getConfigConfigMap().get( "server-config" );


        // HTTPService
        println( "\n--- HTTPService --- \n" );

        final HTTPServiceConfig httpService = config.getHTTPServiceConfig();
        listMap( "HTTPListeners", httpService.getHTTPListenerConfigMap() );
        listMap( "VirtualServers", httpService.getVirtualServerConfigMap() );
    }

    /**
        List all MBeans that have j2eeType=*ResourceConfig
     */
    private void   queryWild(final String name, final String value)
    {
        final String[]	propNames	= new String[ 1 ];
        propNames[ 0 ]	= name;

        final String[]	propValues	= new String[ 1 ];
        propValues[ 0 ]	= value;

        final Set	items	= getQueryMgr().queryWildSet( propNames, propValues );

        println( "\n--- Queried for " + propNames[ 0 ] + "=" + propValues[ 0 ] + " ---" );
        final Iterator	iter	= items.iterator();
        while ( iter.hasNext() )
        {
            final AMX	item	= (AMX)iter.next();

            println( "j2eeType=" + item.getJ2EEType() + "," + "name=" + item.getName() );
        }
    }

    /**
        List all MBeans that have j2eeType=<j2eeType>
     */
    private void queryForJ2EEType( final String j2eeType )
    {
        final String	prop	= Util.makeJ2EETypeProp( j2eeType );
        final Set		items	= getQueryMgr().queryPropsSet( prop );

        println( "\n--- Queried for " + prop + " ---" );

        final Iterator	iter	= items.iterator();
        while ( iter.hasNext() )
        {
            final AMX	item	= (AMX)iter.next();

            // they may or may not have unique names, so show ObjectNames
            println( item.getFullType() );
        }
    }

    private void proveConnection()
    {
        println("Listing.....");
        list( );
        /*println("querying Wild......");
        queryWild( AMX.J2EE_TYPE_KEY, "*ResourceConfig" );*/
        println("querying for j2ee type.....");
        queryForJ2EEType( XTypes.SSL_CONFIG );
    }

    public AMXConnectionVerifier(final String host, 
                                   final int port, 
                                   final String adminUser, 
                                   final String adminPassword,
                                   final boolean useTLS)
                                    throws IOException
    {
        final AMXConnector ct	= new AMXConnector( host, port, adminUser, adminPassword, useTLS );

        mDomainRoot	= ct.getDomainRoot();

        proveConnection( );
    }


    public static void   main( final String[] args )
    {
        new StringifierRegistryIniterImpl( StringifierRegistryImpl.DEFAULT );

        try
        {
            new AMXConnectionVerifier(System.getProperty("HOST", "localhost"),
                                        Integer.parseInt(System.getProperty("PORT","8686")),
                                        System.getProperty("ADMIN_USER", "admin"),
                                        System.getProperty("ADMIN_PASSWORD", "adminadmin"),
                                        Boolean.valueOf(System.getProperty("USE_TLS","false")).booleanValue());
        }
        catch( Throwable t )
        {
            ExceptionUtil.getRootCause( t ).printStackTrace();
        }
    }
}
