/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.naming.impl;

import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.logging.LogDomains;
import org.glassfish.api.naming.NamingObjectsProvider;
import org.jvnet.hk2.component.Habitat;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ORBLocator;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
/**
 * Implements the JNDI SPI InitialContextFactory interface used to create
 * the InitialContext objects. It creates an instance of the serial context.
 */

public class SerialInitContextFactory implements InitialContextFactory {
    public static final String LOAD_BALANCING_PROPERTY =
            "com.sun.appserv.iiop.loadbalancingpolicy";

    public static final String IIOP_ENDPOINTS_PROPERTY =
            "com.sun.appserv.iiop.endpoints";

    private static final String IIOP_URL = "iiop:1.2@";

    private static final String CORBALOC = "corbaloc:";

    public static final String IC_BASED_WEIGHTED = "ic-based-weighted";

    public static final String IC_BASED = "ic-based";

    protected static final Logger _logger = LogDomains.getLogger(
        SerialInitContextFactory.class, LogDomains.JNDI_LOGGER );

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    private static String defaultHost;
    private static String defaultPort;
    private static Habitat defaultHabitat;

    private final Hashtable defaultEnv = new Hashtable() ;
    private final RoundRobinPolicy rrPolicy;
    private final boolean useLB ;
    private final Habitat habitat;
    private final GroupInfoServiceObserverImpl giso;
    private /* should be final */ GroupInfoService gis;

    public RoundRobinPolicy getRRPolicy() {
	return rrPolicy;
    }

    public static String getCorbalocURL(Object[] list) {

	String corbalocURL = "";
	//convert list into corbaloc url
	for (int i = 0; i < list.length;i++) {
	    _logger.log(Level.INFO, "list[i] ==> {0}", list[i]);
	    if (corbalocURL.isEmpty()) {
	        corbalocURL = IIOP_URL + ((String)list[i]).trim();
	    } else {
	        corbalocURL = corbalocURL + "," +
		    IIOP_URL + ((String)list[i]).trim();
	    }
	}
	_logger.log(Level.INFO, "corbaloc url ==> {0}", corbalocURL);
	return corbalocURL;
    }

    private String[] getEndpointList() {
        String[] list ;
        String[] commaDelimitedValues = null;
        String policy = null;

        // Get the load balancing policy
        String propertyValue = System.getProperty( LOAD_BALANCING_PROPERTY);

        if (propertyValue != null) {
            commaDelimitedValues = propertyValue.split(",");

            if (commaDelimitedValues != null) {
                if (commaDelimitedValues[0].trim().equals(IC_BASED) ||
                    commaDelimitedValues[0].trim().equals(IC_BASED_WEIGHTED)) {
                    policy = commaDelimitedValues[0];
                }
            }

            if (policy != null) {
                System.setProperty(LOAD_BALANCING_PROPERTY, policy);
            }
        }

        propertyValue = System.getProperty( IIOP_ENDPOINTS_PROPERTY);

        if (propertyValue==null || propertyValue.length()==0) {
            //do not use the defaults here as then we are not giving a
            //chance to the <policy>,host:port(,host:port)* type of policy
            //specification
            list = null;
        } else {
            list = propertyValue.split(",");
        }

        //if the endpoints property was not specified, give a
        //chance to the <policy>,host:port(,host:port)* type of policy
        //specification

        if (list == null ) {
            if (commaDelimitedValues != null && commaDelimitedValues.length > 1) {
                list = new String[commaDelimitedValues.length-1];
                for (int i=0; i<list.length; i++) {
                    list[i] = commaDelimitedValues[i+1];
                }
            } else {
                String host = System.getProperty(
                    ORBLocator.OMG_ORB_INIT_HOST_PROPERTY ) ;
                String port = System.getProperty(
                    ORBLocator.OMG_ORB_INIT_PORT_PROPERTY ) ;
                if (host != null && port != null) {
                    list = new String[1];
                    list[0] = host + ":" + port;
                }
            }
        }

        return list ;
    }


    private boolean propertyIsSet( String pname ) {
        String value = System.getProperty(pname) ;
        return value != null && !value.isEmpty() ;
    }

    public SerialInitContextFactory() {
        habitat = (defaultHabitat == null) ? Globals.getDefaultHabitat() 
            : defaultHabitat;

        useLB = propertyIsSet(IIOP_ENDPOINTS_PROPERTY)
            || propertyIsSet(LOAD_BALANCING_PROPERTY) ;

        gis = null ;
        if (useLB) {
            String[] list = getEndpointList() ;

            rrPolicy = new RoundRobinPolicy(list);

            if (list != null && list.length > 0) {
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
                    _logger.log(Level.SEVERE, null, ex);
                }
            }
        } else {
            rrPolicy = null ;
        }

        if (gis != null) {
            giso = new GroupInfoServiceObserverImpl( gis, this );

            gis.addObserver(giso);

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "getGIS: rrPolicy = {0}", rrPolicy );
            }
        } else {
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

        // XXX what should we do if result is null?
        throw new RuntimeException( "Could not get ORB" ) ;
    }

    /**
     * Create the InitialContext object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context getInitialContext(Hashtable env) throws NamingException {
        if( (defaultHost != null) &&
            (env.get(ORBLocator.OMG_ORB_INIT_HOST_PROPERTY) == null)) {
            env.put(ORBLocator.OMG_ORB_INIT_HOST_PROPERTY, defaultHost);
        }

        if( (defaultPort != null) &&
            (env.get(ORBLocator.OMG_ORB_INIT_PORT_PROPERTY) == null)) {
            env.put(ORBLocator.OMG_ORB_INIT_PORT_PROPERTY, defaultPort);    
        }

        // Use Atomic look to ensure only first thread does NamingObjectsProvider
        // initialization.
        // TODO Note that right now the 2nd, 3rd. etc. threads will proceed
        // past here even if the first thread is still doing its getAllByContract
        // work.  Should probably change the way this works to eliminate that
        // time window where the objects registered by NamingObjectsProvider
        // aren't available.
        if( !initialized.get() ) {
            boolean firstToInitialize = initialized.compareAndSet(false, true);

            if (firstToInitialize) {
                // this should force the initialization of the resources providers
                if (habitat!=null) {
                    for (NamingObjectsProvider provider :
                            habitat.getAllByContract(NamingObjectsProvider.class)) {
                        // no-op
                    }
                }
            }

	    String policy = null;

            if (useLB) {
                if (env == null) {
                    env = defaultEnv;
                }

                Object [] list;

                Context ctx = SerialContext.getStickyContext() ;
                if (ctx != null) {
                    return ctx ;
                }

                //get next version of the randomized list using round robin algo
                list = rrPolicy.getNextRotation();

                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "getInitialContext: rrPolicy = {0}",
                         rrPolicy );
                }

                String corbalocURL = getCorbalocURL(list);

                env.put("com.sun.appserv.ee.iiop.endpointslist", CORBALOC + corbalocURL);
                env.put(ORBLocator.JNDI_CORBA_ORB_PROPERTY, getORB());

                //user can specify the load balancing policy and endpoints
                // via env. Hence the logic below.

                String propertyValue = (String) env.get(
                    LOAD_BALANCING_PROPERTY);
                String[] commaDelimitedValues = null;
                String host = null;
                String port = null;

                if (propertyValue != null) {
                    commaDelimitedValues = propertyValue.split(",");

                    if (commaDelimitedValues != null) {
                        if (commaDelimitedValues[0].trim().equals(IC_BASED) ||
                            commaDelimitedValues[0].trim().equals(IC_BASED_WEIGHTED)) {
                            policy = commaDelimitedValues[0];
                        }
                    }

                    if (policy != null) {
                        System.setProperty(LOAD_BALANCING_PROPERTY, policy);
                    }
                }

                propertyValue = (String) env.get(IIOP_ENDPOINTS_PROPERTY);

                String [] temp_list =
                    (propertyValue == null || propertyValue.length() == 0)
                    ? null
                    : propertyValue.split(",");

                if (temp_list == null || temp_list.length == 0) {
                    if (commaDelimitedValues != null) {
                        temp_list = new String[commaDelimitedValues.length - 1];

                        for (int i=0; i<temp_list.length; i++) {
                            temp_list[i] = commaDelimitedValues[i+1];
                        }
                    }
                }

                //if endpoints property is not set by commandline or via env
                // check for JNDI provider url
                // else use ORB host:port value
                if ((System.getProperty(IIOP_ENDPOINTS_PROPERTY) == null) &&
                    (temp_list == null || temp_list.length == 0)) {
                    if (env.get(ORBLocator.JNDI_PROVIDER_URL_PROPERTY) != null) {
                        temp_list = rrPolicy.getEndpointForProviderURL(
                            (String)env.get(ORBLocator.JNDI_PROVIDER_URL_PROPERTY));
                    }

                    if (temp_list == null || temp_list.length == 0) {
                        if (env.get( ORBLocator.OMG_ORB_INIT_HOST_PROPERTY) != null &&
                            env.get( ORBLocator.OMG_ORB_INIT_PORT_PROPERTY) != null) {
                            host = (String)env.get(
                                ORBLocator.OMG_ORB_INIT_HOST_PROPERTY);
                            port = (String)env.get(
                                ORBLocator.OMG_ORB_INIT_PORT_PROPERTY);
                        } else {
                            host = System.getProperty(
                                ORBLocator.OMG_ORB_INIT_HOST_PROPERTY);
                            port = System.getProperty(
                                ORBLocator.OMG_ORB_INIT_PORT_PROPERTY);
                        }

                        if (host != null && port != null) {
                            temp_list = rrPolicy.getAddressPortList(host, port);
                            _logger.log(Level.WARNING, "no.endpoints.selected",
                                    new Object[] {host, port});
                        } else {
                            _logger.log(Level.SEVERE, "no.endpoints");
                            throw new RuntimeException("Cannot Proceed. No Endpoints specified.");
                        }
                    }
                }

                if (giso == null) {
                    _logger.warning("Cannot obtain GroupInfoServiceObserverImpl");
                }

                //add the list after randomising it to the circular list in rrPolicy
                if (temp_list != null && temp_list.length > 0) {
                    rrPolicy.setClusterInstanceInfo(temp_list);
                }

                if (giso != null) {
                    //need to get the list of cluster instances the very first time a context is created
                    giso.membershipChange();
                } else {
                    _logger.warning("Cannot obtain GroupInfoServiceObserverImpl");
                }
            }
        }

        return createInitialContext(env != null ? env : defaultEnv);
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
}
