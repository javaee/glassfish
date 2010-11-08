/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.enterprise.naming.impl;

import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.logging.LogDomains;
import java.util.ArrayList;
import org.glassfish.api.naming.NamingObjectsProvider;
import org.jvnet.hk2.component.Habitat;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ORBLocator;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;

public class SerialInitContextFactory implements InitialContextFactory {
    public static final String LOAD_BALANCING_PROPERTY =
            "com.sun.appserv.iiop.loadbalancingpolicy";

    public static final String IIOP_ENDPOINTS_PROPERTY =
            "com.sun.appserv.iiop.endpoints";

    public static final String IC_BASED_WEIGHTED = "ic-based-weighted";

    public static final String IC_BASED = "ic-based";

    public static final String IIOP_URL = "iiop:1.2@";

    public static final String CORBALOC = "corbaloc:";


    private static final RoundRobinPolicy rrPolicy ;

    private static final boolean useLB ;

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private static final Hashtable defaultEnv = new Hashtable() ;

    private static boolean propertyIsSet( String pname ) {
        String value = System.getProperty(pname) ;
        return value != null && !value.isEmpty() ;
    }

    private static List<String> splitOnComma( String arg ) {
        final List<String> result = new ArrayList<String>() ;
        if (arg != null) {
            final String[] splits = arg.split( "," ) ;
            if (splits != null) {
                for (String str : splits) {
                    result.add( str.trim() ) ;
                }
            }
        }

        return result ;
    }

    private static String getEnvSysProperty( Hashtable env, String pname ) {
        String value = (String)env.get( pname ) ;
        if (value == null) {
            value = System.getProperty( pname ) ;
        }
        return value ;
    }

    private static List<String> getEndpointList() {
        return getEndpointList( defaultEnv ) ;
    }

    private static List<String> getEndpointList( Hashtable env ) {
        final List<String> list = new ArrayList<String>() ;

        if (list.isEmpty()) {
            final String lbpv = System.getProperty( LOAD_BALANCING_PROPERTY);
            final List<String> lbList = splitOnComma(lbpv) ;
            if (lbList.size() > 0) {
                final String first = lbList.remove( 0 ) ;
                if (first.equals(IC_BASED) || first.equals(IC_BASED_WEIGHTED)) {
                    // XXX concurrency issue here:  possible race on global
                    System.setProperty(LOAD_BALANCING_PROPERTY, first );
                }
            }
            list.addAll( lbList ) ;
        }

        if (list.isEmpty()) {
            final String iepv = System.getProperty( IIOP_ENDPOINTS_PROPERTY);
            final List<String> epList = splitOnComma(iepv) ;
            list.addAll( epList ) ;
        }

        if (list.isEmpty()) {
            final String urlValue = (String)env.get(
                ORBLocator.JNDI_PROVIDER_URL_PROPERTY) ;
            list.addAll( rrPolicy.getEndpointForProviderURL( urlValue ) ) ;
        }

        if (list.isEmpty()) {
            String host = getEnvSysProperty( env,
                ORBLocator.OMG_ORB_INIT_HOST_PROPERTY) ;
            String port = getEnvSysProperty( env,
                ORBLocator.OMG_ORB_INIT_PORT_PROPERTY) ;

            if (host != null && port != null) {
                list.addAll(
                    rrPolicy.getAddressPortList(host, port) ) ;
                _logger.log(Level.WARNING, "no.endpoints.selected",
                        new Object[] {host, port});
            }
        }

        if (list.isEmpty()) {
            if (defaultHost != null && defaultPort != null) {
                list.add( defaultHost + ":" + defaultPort ) ;
            }
        }

        if (list.isEmpty()) {
            _logger.log(Level.SEVERE, "no.endpoints");
            throw new RuntimeException("Cannot Proceed. No Endpoints specified.");
        }

        return list ;
    }

    private static void doLog( Level level, String fmt, Object... args )  {
        if (_logger.isLoggable(level)) {
            _logger.log( level, fmt, args ) ;
        }
    }

    private static void fineLog( String fmt, Object... args ) {
        doLog( Level.FINE, fmt, args ) ;
    }

    static {
        useLB = propertyIsSet(IIOP_ENDPOINTS_PROPERTY)
            || propertyIsSet(LOAD_BALANCING_PROPERTY) ;

        if (useLB) {
            List<String> list = getEndpointList() ;
            rrPolicy = new RoundRobinPolicy(list);
        } else {
            rrPolicy = null ;
        }
    }

    protected static final Logger _logger = LogDomains.getLogger(
        SerialInitContextFactory.class, LogDomains.JNDI_LOGGER );

    private static String defaultHost = null ;

    private static String defaultPort = null ;

    private static Habitat defaultHabitat = null ;

    public static RoundRobinPolicy getRRPolicy() {
	return rrPolicy;
    }

    static void setDefaultHost(String host) {
        defaultHost = host;
    }

    static void setDefaultPort(String port) {
        defaultPort = port;
    }

    static void setDefaultHabitat(Habitat h) {
        defaultHabitat = h;

    }

    static Habitat getDefaultHabitat() {
        return defaultHabitat;
    }

    private static String getCorbalocURL( final List<String> list) {
        final StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for (String str : list) {
	    if (first) {
                first = false ;
                sb.append( CORBALOC ) ;
	    } else {
                sb.append( ',' ) ;
	    }

            sb.append( IIOP_URL ) ;
            sb.append( str.trim() ) ;
	}

	fineLog( "corbaloc url ==> {0}", sb.toString() );

	return sb.toString() ;
    }

    private final Habitat habitat ;

    private final GroupInfoServiceObserverImpl giso ;

    private /* should be final */ GroupInfoService gis = null ;

    public SerialInitContextFactory() {
        // Issue 14396
        Habitat temp = defaultHabitat ;
        if (temp == null) {
            temp = Globals.getDefaultHabitat() ;
        }
        if (temp == null) {
            // May need to initialize hk2 component model in standalone client
            temp = Globals.getStaticHabitat() ;
        }
        habitat = temp ;

        if (useLB) {
            try {
                //fix for bug 6527987
                // passing the first endpoint for ORBManager.getORB() to connect to
                //need to make NameService HA...what if first endpoint is down.
                //need to address this issue.
                /*  Properties props = new Properties();
                String hostPort = endpoint;
                // for IPv6 support, using lastIndex of ":"
                int lastIndex = hostPort.lastIndexOf(':');
                _logger.fine("hostPort = " + hostPort + " lastIndex = " + lastIndex);
                _logger.fine("hostPort.substring(0, lastIndex) = " + hostPort.substring(0, lastIndex));
                _logger.fine("hostPort.substring(lastIndex + 1) = " + hostPort.substring(lastIndex + 1));
                props.put("org.omg.CORBA.ORBInitialHost", hostPort.substring(0, lastIndex));
                props.put("org.omg.CORBA.ORBInitialPort", hostPort.substring(lastIndex + 1));
                */
                gis = (GroupInfoService) (getORB().resolve_initial_references(
                    ORBLocator.FOLB_CLIENT_GROUP_INFO_SERVICE));
            } catch (InvalidName ex) {
                doLog(Level.SEVERE,
                    "Exception in SerialInitContextFactory constructor {0}",
                        ex);
            }

            giso = new GroupInfoServiceObserverImpl( gis );

            gis.addObserver(giso);

            fineLog( "getGIS: rrPolicy = {0}", rrPolicy );
        }  else {
            giso = null ;
        }
    }

    private ORB getORB() {
        ORB result = null ;
        if (habitat != null) {
            ORBLocator orbLoc = habitat.getByContract(ORBLocator.class) ;
            if (orbLoc != null) {
                return orbLoc.getORB() ;
            }
        }

        throw new RuntimeException( "Could not get ORB" ) ;
    }

    /**
     * Create the InitialContext object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context getInitialContext(Hashtable env) throws NamingException {
        final Hashtable myEnv = env == null ? new Hashtable() : env ;

        // Use Atomic look to ensure only first thread does NamingObjectsProvider
        // initialization.  This cannot be static because we need the env
        // argument passed to getInitialContext.
        // TODO Note that right now the 2nd, 3rd. etc. threads will proceed
        // past here even if the first thread is still doing its getAllByContract
        // work.  Should probably change the way this works to eliminate that
        // time window where the objects registered by NamingObjectsProvider
        // aren't available.
        final boolean firstToInitialize = initialized.compareAndSet( false, true ) ;
        if (firstToInitialize) {
            // this should force the initialization of the resources providers
            if (habitat!=null) {
                for (NamingObjectsProvider provider :
                    habitat.getAllByContract(NamingObjectsProvider.class)) {
                    // no-op
                }
            }

            if (useLB) {
                final List<String> epList = getEndpointList( myEnv ) ;
                if (!epList.isEmpty()) {
                    rrPolicy.setClusterInstanceInfoFromString(epList);
                }

                giso.membershipChange();
            }
        }

        if (useLB) {
            Context ctx = SerialContext.getStickyContext() ;
            if (ctx != null) {
                return ctx ;
            }

            List<String> rrList = rrPolicy.getNextRotation();
            fineLog( "getInitialContext: rrPolicy = {0}", rrPolicy );

            String corbalocURL = getCorbalocURL(rrList);

            myEnv.put("com.sun.appserv.ee.iiop.endpointslist", corbalocURL);
            myEnv.put(ORBLocator.JNDI_CORBA_ORB_PROPERTY, getORB());
        } else {
            if (defaultHost != null) {
                myEnv.put( ORBLocator.OMG_ORB_INIT_HOST_PROPERTY, defaultHost ) ;
            }

            if (defaultPort != null) {
                myEnv.put( ORBLocator.OMG_ORB_INIT_PORT_PROPERTY, defaultPort ) ;
            }
        }

        return createInitialContext(myEnv);
    }

    private Context createInitialContext(Hashtable env) throws NamingException
    {
        SerialContext serialContext = new SerialContext(env, habitat);
        if (NamingManager.hasInitialContextFactoryBuilder()) {
            // When builder is used, JNDI does not go through
            // URL Context discovery anymore. To address that
            // we install a wrapper that first goes through
            // URL context discovery and then falls back to
            // serialContext.
            return new WrappedSerialContext(env, serialContext);
        } else {
            return serialContext ;
        }
    }
}
