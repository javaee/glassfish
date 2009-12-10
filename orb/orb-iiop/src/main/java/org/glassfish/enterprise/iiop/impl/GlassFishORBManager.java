/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager;
import com.sun.corba.ee.spi.osgi.ORBFactory;
import com.sun.corba.ee.spi.orbutil.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.transport.CorbaTransportManager;
import com.sun.corba.ee.spi.transport.TransportDefault;
import com.sun.corba.ee.spi.transport.CorbaAcceptor;

import com.sun.logging.LogDomains;

import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.Orb;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.SslClientConfig;

import com.sun.grizzly.config.dom.Ssl;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.enterprise.iiop.api.GlassFishORBLifeCycleListener;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.enterprise.iiop.util.IIOPUtils;

import com.sun.enterprise.util.Utility;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashSet;
import java.util.Set;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.types.Property;

/**
 * This class initializes the ORB with a list of (standard) properties
 * and provides a few convenience methods to get the ORB etc.
 */

public final class GlassFishORBManager {
    static Logger logger = LogDomains.getLogger(GlassFishORBManager.class, LogDomains.UTIL_LOGGER);



    private static final boolean debug = true;

    // Various pluggable classes defined in the app server that are used
    // by the ORB.
    private static final String ORB_CLASS =
            "com.sun.corba.ee.impl.orb.ORBImpl";
    private static final String ORB_SINGLETON_CLASS =
            "com.sun.corba.ee.impl.orb.ORBSingleton";

    private static final String ORB_SE_CLASS =
            "com.sun.corba.se.impl.orb.ORBImpl";
    private static final String ORB_SE_SINGLETON_CLASS =
            "com.sun.corba.se.impl.orb.ORBSingleton";

    private static final String PEORB_CONFIG_CLASS =
            "org.glassfish.enterprise.iiop.impl.PEORBConfigurator";
    private static final String IIOP_SSL_SOCKET_FACTORY_CLASS =
            "org.glassfish.enterprise.iiop.impl.IIOPSSLSocketFactory";
    private static final String RMI_UTIL_CLASS =
            "com.sun.corba.ee.impl.javax.rmi.CORBA.Util";
    private static final String RMI_STUB_CLASS =
            "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl";
    private static final String RMI_PRO_CLASS =
            "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject";

    // JNDI constants
    public static final String JNDI_PROVIDER_URL_PROPERTY =
            "java.naming.provider.url";
    public static final String JNDI_CORBA_ORB_PROPERTY =
            "java.naming.corba.orb";

    // RMI-IIOP delegate constants
    public static final String ORB_UTIL_CLASS_PROPERTY =
            "javax.rmi.CORBA.UtilClass";
    public static final String RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY =
            "javax.rmi.CORBA.StubClass";
    public static final String RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY =
            "javax.rmi.CORBA.PortableRemoteObjectClass";

    // ORB constants: OMG standard
    public static final String OMG_ORB_CLASS_PROPERTY =
            "org.omg.CORBA.ORBClass";
    public static final String OMG_ORB_SINGLETON_CLASS_PROPERTY =
            "org.omg.CORBA.ORBSingletonClass";
    public static final String OMG_ORB_INIT_HOST_PROPERTY =
            ORBConstants.INITIAL_HOST_PROPERTY;
    public static final String OMG_ORB_INIT_PORT_PROPERTY =
            ORBConstants.INITIAL_PORT_PROPERTY;
    private static final String PI_ORB_INITIALIZER_CLASS_PREFIX =
            ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX;

    // ORB constants: Sun specific
    public static final String SUN_USER_CONFIGURATOR_PREFIX =
            ORBConstants.USER_CONFIGURATOR_PREFIX;
    public static final String SUN_ORB_ID_PROPERTY =
            ORBConstants.ORB_ID_PROPERTY;
    public static final String SUN_ORB_SERVER_HOST_PROPERTY =
            ORBConstants.SERVER_HOST_PROPERTY;
    public static final String SUN_ORB_SERVER_PORT_PROPERTY =
            ORBConstants.SERVER_PORT_PROPERTY;
    public static final String SUN_ORB_SOCKET_FACTORY_CLASS_PROPERTY =
            ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY;
    public static final String SUN_ORB_IOR_TO_SOCKETINFO_CLASS_PROPERTY =
            ORBConstants.IOR_TO_SOCKET_INFO_CLASS_PROPERTY;
    public static final String SUN_MAX_CONNECTIONS_PROPERTY =
            ORBConstants.HIGH_WATER_MARK_PROPERTY;
    public static final String ORB_LISTEN_SOCKET_PROPERTY =
            ORBConstants.LISTEN_SOCKET_PROPERTY;
    //
    // XXX The following constants do not appear to be used in the ORB
    public static final String ORB_DISABLED_PORTS_PROPERTY =
            "com.sun.CORBA.connection.ORBDisabledListenPorts";
    private static final String SUN_LISTEN_ADDR_ANY_ADDRESS =
            "com.sun.CORBA.orb.AddrAnyAddress";
    private static final String ORB_IOR_ADDR_ANY_INITIALIZER =
            "com.sun.enterprise.iiop.IORAddrAnyInitializer";

    // ORB configuration constants
    private static final String DEFAULT_SERVER_ID = "100";
    private static final String ACC_DEFAULT_SERVER_ID = "101";
    private static final String USER_DEFINED_ORB_SERVER_ID_PROPERTY = "org.glassfish.orb.iiop.orbserverid";

    private static final String DEFAULT_MAX_CONNECTIONS = "1024";
    private static final String GLASSFISH_INITIALIZER =
            "org.glassfish.enterprise.iiop.impl.GlassFishORBInitializer";

    private static final String SUN_GIOP_DEFAULT_FRAGMENT_SIZE = "1024";
    private static final String SUN_GIOP_DEFAULT_BUFFER_SIZE = "1024";

    private static final String IIOP_CLEAR_TEXT_CONNECTION =
            "IIOP_CLEAR_TEXT";
    public static final String DEFAULT_ORB_INIT_HOST = "localhost";

    // This will only apply for stand-alone java clients, since
    // in the server the orb port comes from domain.xml, and in an appclient
    // the port is set from the sun-acc.xml.  It's set to the same 
    // value as the default orb port in domain.xml as a convenience.
    // That way the code only needs to do a "new InitialContext()"
    // without setting any jvm properties and the naming service will be
    // found.  Of course, if the port was changed in domain.xml for some
    // reason the code will still have to set org.omg.CORBA.ORBInitialPort.
    public static final String DEFAULT_ORB_INIT_PORT = "3700";

    // CSIv2 config
    private static final String SSL = "SSL";
    private static final String SSL_MUTUALAUTH = "SSL_MUTUALAUTH";
    private static final String ORB_SSL_CERTDB_PATH =
            "com.sun.CSIV2.ssl.CertDB";
    private static final String ORB_SSL_CERTDB_PASSWORD =
            "com.sun.CSIV2.ssl.CertDBPassword";
    public static final String SUN_GIOP_FRAGMENT_SIZE_PROPERTY =
            "com.sun.CORBA.giop.ORBFragmentSize";
    public static final String SUN_GIOP_BUFFER_SIZE_PROPERTY =
            "com.sun.CORBA.giop.ORBBufferSize";

     // We need this to get the ORB monitoring set up correctly
    public static final String S1AS_ORB_ID = "S1AS-ORB";

    // Set in constructor
    private Habitat habitat;
    private IIOPUtils iiopUtils;


    // the ORB instance
    private ORB orb = null;

    // The ReferenceFactoryManager from the orb.
    private ReferenceFactoryManager rfm = null;

    private int orbInitialPort = -1;


    private IiopListener[] iiopListenerBeans = null;
    private Orb orbBean = null;
    private IiopService iiopServiceBean = null;

    private Properties csiv2Props = new Properties();

    private ProcessType processType;

    private static final Properties EMPTY_PROPERTIES = new Properties();



    /**
     * Keep this class private to the package.  Eventually we need to
     * move all public statics or change them to package private.
     * All external orb/iiop access should go through orb-connector module
     */
    GlassFishORBManager(Habitat h) {

        habitat = h;

        iiopUtils = habitat.getComponent(IIOPUtils.class);

        ProcessEnvironment processEnv = habitat.getComponent(ProcessEnvironment.class);

        processType = processEnv.getProcessType();

        initProperties();

    }

    /**
     * Returns whether an adapterName (from ServerRequestInfo.adapter_name)
     * represents an EJB or not.
     */
    public boolean isEjbAdapterName(String[] adapterName) {
        boolean result = false;
        if (rfm != null)
            result = rfm.isRfmName(adapterName);

        return result;
    }

    /**
     * Returns whether the operationName corresponds to an "is_a" call
     * or not (used to implement PortableRemoteObject.narrow.
     */
    boolean isIsACall(String operationName) {
        return operationName.equals("_is_a");
    }

    /**
     * Return the shared ORB instance for the app server.
     * If the ORB is not already initialized, it is created
     * with the standard server properties, which can be
     * overridden by Properties passed in the props argument.
     */
    synchronized ORB getORB(Properties props) {

        try {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "GlassFishORBManager.getORB->: " + orb);
            }

            if (orb == null) {
                initORB(props);
            }

            iiopUtils.setORB(orb);

            return orb;
        } finally {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "GlassFishORBManager.getORB<-: " + orb);
            }
        }
    }

    Properties getCSIv2Props() {
        // Return a copy of the CSIv2Props
        return new Properties(csiv2Props);
    }

    void setCSIv2Prop(String name, String value) {
        csiv2Props.setProperty(name, value);
    }

    int getORBInitialPort() {
        return orbInitialPort;
    }

    private void initProperties() {


        if( (processType == ProcessType.ACC) || (processType == ProcessType.Other) ) {

            // No access to domain.xml.  Just init properties.
            // In this case iiopListener beans will be null.

            checkORBInitialPort(EMPTY_PROPERTIES);

        } else {

            iiopServiceBean = iiopUtils.getIiopService();

            iiopListenerBeans = (IiopListener[]) iiopServiceBean.getIiopListener().toArray(new IiopListener[0]);
            assert (iiopListenerBeans != null && iiopListenerBeans.length > 0);

            // checkORBInitialPort looks at iiopListenerBeans, if present
            checkORBInitialPort(EMPTY_PROPERTIES);

            orbBean = iiopServiceBean.getOrb();
            assert (orbBean != null);

            // Initialize IOR security config for non-EJB CORBA objects
            //iiopServiceBean.isClientAuthenticationRequired()));
            csiv2Props.put(GlassFishORBHelper.ORB_CLIENT_AUTH_REQUIRED, String.valueOf(
                    iiopServiceBean.getClientAuthenticationRequired()));
            boolean corbaSSLRequired = true;

            // If there is at least one non-SSL listener, then it means
            // SSL is not required for CORBA objects.
            for (int i = 0; i < iiopListenerBeans.length; i++) {
                if (iiopListenerBeans[i].getSsl() == null) {
                    corbaSSLRequired = false;
                    break;
                }
            }

            csiv2Props.put(GlassFishORBHelper.ORB_SSL_SERVER_REQUIRED, String.valueOf(
                    corbaSSLRequired));
        }

    }

    /**
     * Set ORB-related system properties that are required in case
     * user code in the app server or app client container creates a
     * new ORB instance.  The default result of calling
     * ORB.init( String[], Properties ) must be a fully usuable, consistent
     * ORB.  This avoids difficulties with having the ORB class set
     * to a different ORB than the RMI-IIOP delegates.
     */
    private void setORBSystemProperties() {

        java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public java.lang.Object run() {
                        if (System.getProperty(OMG_ORB_CLASS_PROPERTY) == null) {
                            // set ORB based on JVM vendor
                            if (System.getProperty("java.vendor").equals("Sun Microsystems Inc.")) {
                                System.setProperty(OMG_ORB_CLASS_PROPERTY, ORB_SE_CLASS);
                            } else {
                                // if not Sun, then set to EE class
                                System.setProperty(OMG_ORB_CLASS_PROPERTY, ORB_CLASS);
                            }
                        }

                        if (System.getProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY) == null) {
                            // set ORBSingleton based on JVM vendor
                            if (System.getProperty("java.vendor").equals("Sun Microsystems Inc.")) {
                                System.setProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY, ORB_SE_SINGLETON_CLASS);
                            } else {
                                // if not Sun, then set to EE class
                                System.setProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY, ORB_SINGLETON_CLASS);
                            }
                        }

                        System.setProperty(ORB_UTIL_CLASS_PROPERTY,
                                RMI_UTIL_CLASS);

                        System.setProperty(RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY,
                                RMI_STUB_CLASS);

                        System.setProperty(RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY,
                                RMI_PRO_CLASS);

                        return null;
                    }
                }
        );
    }

    /**
     * Set the ORB properties for IIOP failover and load balancing.
     */
    private void setFOLBProperties(Properties orbInitProperties) {

        orbInitProperties.put(ORBConstants.RFM_PROPERTY, "dummy");

        orbInitProperties.put(SUN_ORB_SOCKET_FACTORY_CLASS_PROPERTY,
                IIOP_SSL_SOCKET_FACTORY_CLASS);

        // ClientGroupManager.
        // Registers itself as
        //   ORBInitializer (that registers ClientRequestInterceptor)
        //   IIOPPrimaryToContactInfo
        //   IORToSocketInfo
        orbInitProperties.setProperty(
                ORBConstants.USER_CONFIGURATOR_PREFIX
                        + "com.sun.corba.ee.impl.folb.ClientGroupManager",
                "dummy");
         
        // This configurator registers the CSIv2SSLTaggedComponentHandler
        orbInitProperties.setProperty(
                ORBConstants.USER_CONFIGURATOR_PREFIX
                        + CSIv2SSLTaggedComponentHandlerImpl.class.getName(),"dummy");
       

        /** TODO enable this (needs clustering support)
        if (ASORBUtilities.isGMSAvailableAndClusterHeartbeatEnabled()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "GMS available and enabled - doing EE initialization");
            }

            // Register ServerGroupManager.
            // Causes it to register itself as an ORBInitializer
            // that then registers it as
            // IOR and ServerRequest Interceptors.
            orbInitProperties.setProperty(
                    ORBConstants.USER_CONFIGURATOR_PREFIX
                            + "com.sun.corba.ee.impl.folb.ServerGroupManager",
                    "dummy");

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Did EE property initialization");
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Doing PE initialization");
            }


            orbInitProperties.put(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX
                    + FailoverIORInterceptor.class.getName(), "dummy");

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Did PE property initialization");
            }
        }

        */
    }

    private void initORB(Properties props) {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, ".initORB->: ");
            }

            setORBSystemProperties();

            Properties orbInitProperties = new Properties();
            orbInitProperties.putAll(props);

            orbInitProperties.put(ORBConstants.APPSERVER_MODE, "true");

            // The main configurator.
            orbInitProperties.put(SUN_USER_CONFIGURATOR_PREFIX
                    + PEORB_CONFIG_CLASS, "dummy");

            setFOLBProperties(orbInitProperties);

            // Standard OMG Properties.
            String orbDefaultServerId = DEFAULT_SERVER_ID;
            if (!processType.isServer() && !processType.isStandaloneServer()) {
                orbDefaultServerId = ACC_DEFAULT_SERVER_ID;
            }

            orbDefaultServerId = System.getProperty(USER_DEFINED_ORB_SERVER_ID_PROPERTY, orbDefaultServerId);

            orbInitProperties.put(ORBConstants.ORB_SERVER_ID_PROPERTY,
                    orbDefaultServerId);

            orbInitProperties.put(OMG_ORB_CLASS_PROPERTY, ORB_CLASS);

            orbInitProperties.put(
                    PI_ORB_INITIALIZER_CLASS_PREFIX + GLASSFISH_INITIALIZER, "");                   

            orbInitProperties.put(ORBConstants.ALLOW_LOCAL_OPTIMIZATION,
                    "true");

            orbInitProperties.put(ORBConstants.GET_SERVICE_CONTEXT_RETURNS_NULL, "true");

            orbInitProperties.put(SUN_ORB_ID_PROPERTY, S1AS_ORB_ID);
            orbInitProperties.put(ORBConstants.SHOW_INFO_MESSAGES, "true");

            // Do this even if propertiesInitialized, since props may override
            // ORBInitialHost and port.
            String initialPort = checkORBInitialPort(orbInitProperties);

            String orbInitialHost = checkORBInitialHost(orbInitProperties);
            String[] orbInitRefArgs;
            if (System.getProperty(IIOP_ENDPOINTS_PROPERTY) != null &&
                    !System.getProperty(IIOP_ENDPOINTS_PROPERTY).equals("")) {
                orbInitRefArgs = getORBInitRef(System.getProperty(IIOP_ENDPOINTS_PROPERTY));
            } else {
                // Add -ORBInitRef for INS to work
                orbInitRefArgs = getORBInitRef(orbInitialHost, initialPort);
            }

            // In a server, don't configure any default acceptors so that lazy init
            // can be used.  Actual lazy init setup takes place in PEORBConfigurator
            if (processType.isServer()) {
                validateIiopListeners();
                orbInitProperties.put(ORBConstants.NO_DEFAULT_ACCEPTORS, "true");
            }

            checkConnectionSettings(orbInitProperties);
            checkMessageFragmentSize(orbInitProperties);
            checkServerSSLOutboundSettings(orbInitProperties);
            checkForOrbPropertyValues(orbInitProperties);

            Collection<GlassFishORBLifeCycleListener> lcListeners =
                    iiopUtils.getGlassFishORBLifeCycleListeners();

            List<String> argsList = new ArrayList<String>();
            for (String a : orbInitRefArgs) {
                argsList.add(a);
            }
            for (GlassFishORBLifeCycleListener listener : lcListeners) {
                listener.initializeORBInitProperties(argsList, orbInitProperties);
            }

            String[] args = argsList.toArray(new String[argsList.size()]);

            // The following is done only on the Server Side to set the
            // ThreadPoolManager in the ORB. ThreadPoolManager on the server
            // is initialized based on configuration parameters found in
            // domain.xml. On the client side this is not done

            if (processType.isServer()) {
                PEORBConfigurator.setThreadPoolManager();
            }

            // orb MUST be set before calling getFVDCodeBaseIOR, or we can
            // recurse back into initORB due to interceptors that run
            // when the TOA supporting the FVD is created!
            // DO NOT MODIFY initORB to return ORB!!!

            /**
             * we can't create object adapters inside the ORB init path, or else we'll get this same problem
             * in slightly different ways. (address in use exception)
             * Having an IORInterceptor (TxSecIORInterceptor) get called during ORB init always results in a
             * nested ORB.init call because of the call to getORB in the IORInterceptor.
             */
                
            // TODO Right now we need to explicitly set useOSGI flag.  If it's set to
            // OSGI mode and we're not in OSGI mode, orb initialization fails.  
            boolean useOSGI = false;

            final ClassLoader prevCL = Utility.getClassLoader();
            try {
                Utility.setContextClassLoader(GlassFishORBManager.class.getClassLoader());

                if( processType.isServer()) {

                    Module corbaOrbModule = null;

                    // start glassfish-corba-orb bundle
                    ModulesRegistry modulesRegistry = habitat.getComponent(ModulesRegistry.class);

                    for(Module m : modulesRegistry.getModules()) {
                        if( m.getName().equals("glassfish-corba-orb") ) {
                            corbaOrbModule = m;
                            break;
                        }
                    }

                    if( corbaOrbModule != null) {
                        useOSGI = true;
                        corbaOrbModule.start();
                    }
                }
            } finally {
                Utility.setContextClassLoader(prevCL);
            }

            // Can't run with GlassFishORBManager.class.getClassLoader() as the context ClassLoader
            orb = ORBFactory.create() ;
            ORBFactory.initialize( orb, args, orbInitProperties, useOSGI);

            // Done to indicate this is a server and
            // needs to create listen ports.
            try {
                org.omg.CORBA.Object obj =
                        orb.resolve_initial_references("RootPOA");
            } catch (org.omg.CORBA.ORBPackage.InvalidName in) {
                logger.log(Level.SEVERE, "enterprise.orb_reference_exception", in);
            }


            if( processType.isServer() ) {
                // J2EEServer's persistent server port is same as ORBInitialPort.
                orbInitialPort = getORBInitialPort();

                for (GlassFishORBLifeCycleListener listener : lcListeners) {
                    listener.orbCreated(orb);
                }

                //TODO: The following two statements can be moved to some GlassFishORBLifeCycleListeners

                rfm = (ReferenceFactoryManager) orb.resolve_initial_references(
                        ORBConstants.REFERENCE_FACTORY_MANAGER);
            }

            /** TODO
            ASORBUtilities.initGIS(orb);            
            **/

            // SeeBeyond fix for 6325988: needs testing.
            // Still do not know why this might make any difference.
            // Invoke this for its side-effects: ignore returned IOR.
            orb.getFVDCodeBaseIOR();

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "enterprise_util.excep_in_createorb", ex);
            throw new RuntimeException(ex);
        } finally {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, ".initORB<-: ");
            }
        }
    }

    private String checkForAddrAny(Properties props, String orbInitialHost) {
        if ((orbInitialHost.equals("0.0.0.0")) || (orbInitialHost.equals("::"))
                || (orbInitialHost.equals("::ffff:0.0.0.0"))) {
            /* FIXME -DHIRU
               props.setProperty(SUN_LISTEN_ADDR_ANY_ADDRESS, orbInitialHost);
               props.put(PI_ORB_INITIALIZER_CLASS_PREFIX + ORB_IOR_ADDR_ANY_INITIALIZER, "");
           */
            try {
                String localAddress = java.net.InetAddress.getLocalHost().getHostAddress();
                return localAddress;
            } catch (java.net.UnknownHostException uhe) {
                logger.log(Level.WARNING, "Unknown host exception - Setting host to localhost");
                return DEFAULT_ORB_INIT_HOST;
            }
        } else {
            // Set com.sun.CORBA.ORBServerHost only if it's not one of "0.0.0.0",
            // "::" or "::ffff:0.0.0.0"
            props.setProperty(SUN_ORB_SERVER_HOST_PROPERTY, orbInitialHost);
            return orbInitialHost;
        }
    }

    private String checkORBInitialHost(Properties props) {
        // Host setting in system properties always takes precedence.
        String orbInitialHost = System.getProperty(OMG_ORB_INIT_HOST_PROPERTY);
        if (orbInitialHost == null)
            orbInitialHost = props.getProperty(OMG_ORB_INIT_HOST_PROPERTY);
        if (orbInitialHost == null) {
            if( iiopListenerBeans != null ) {
                orbInitialHost = iiopListenerBeans[0].getAddress();
                orbInitialHost = checkForAddrAny(props, orbInitialHost);
            }
        }
        if (orbInitialHost == null)
            orbInitialHost = DEFAULT_ORB_INIT_HOST;

        props.setProperty(OMG_ORB_INIT_HOST_PROPERTY, orbInitialHost);

        if (debug) {
            if (logger.isLoggable(Level.FINE))
                logger.log(Level.FINE, "Setting orb initial host to " + orbInitialHost);
        }
        return orbInitialHost;
    }

    private String checkORBInitialPort(Properties props) {
        // Port setting in system properties always takes precedence.
        String initialPort = System.getProperty(OMG_ORB_INIT_PORT_PROPERTY);
        if (initialPort == null)
            initialPort = props.getProperty(OMG_ORB_INIT_PORT_PROPERTY);

        if (initialPort == null) {
            if( iiopListenerBeans != null ) {
                initialPort = iiopListenerBeans[0].getPort();
                if (!Boolean.valueOf(iiopListenerBeans[0].getEnabled())) {
                    props.setProperty(ORB_DISABLED_PORTS_PROPERTY, initialPort);
                }
            }
        }

        if (initialPort == null)
            initialPort = DEFAULT_ORB_INIT_PORT;

        // Make sure we set initial port in System properties so that
        // any instantiations of com.sun.jndi.cosnaming.CNCtxFactory
        // use same port.
        props.setProperty(OMG_ORB_INIT_PORT_PROPERTY, initialPort);


        // Done to initialize the Persistent Server Port, before any
        // POAs are created. This was earlier done in POAEJBORB
        // Do it only in the appserver, not on appclient.  
        if ( processType.isServer() ) {
            props.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                    initialPort);
        }
       

        if (debug) {
            if (logger.isLoggable(Level.FINE))
                logger.log(Level.FINE, "Setting orb initial port to " + initialPort);
        }

        orbInitialPort = new Integer(initialPort).intValue();

        return initialPort;
    }

    private void validateIiopListeners() {
        if (iiopListenerBeans != null) {
            int lazyCount = 0 ;
		    for (IiopListener ilb : iiopListenerBeans) {
                boolean securityEnabled = Boolean.valueOf( ilb.getSecurityEnabled() ) ;
                boolean isLazy = Boolean.valueOf( ilb.getLazyInit() ) ;
                if( isLazy ) {
                    lazyCount++;
                }
                if (lazyCount > 1) {
                    throw new IllegalStateException("Invalid iiop-listener " + ilb.getId() +
                            ". Only one iiop-listener can be configured with lazy-init=true");
                }

                if (securityEnabled || ilb.getSsl() == null) {
                    // no-op
                } else {
                    if (isLazy) {
                        throw new IllegalStateException("Invalid iiop-listener " + ilb.getId() +
                                ". Lazy-init not supported for SSL iiop-listeners");
                    }
                    Ssl sslBean = ilb.getSsl() ;
                    assert sslBean != null ;

		        }
		    }
	    }
    }

    private void checkConnectionSettings(Properties props) {
        if (orbBean != null) {
            String maxConnections;

            try {
                maxConnections = orbBean.getMaxConnections();

                // Validate number formats
                Integer.parseInt(maxConnections);
            } catch (NumberFormatException nfe) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "enterprise_util.excep_orbmgr_numfmt", nfe);
                }

                maxConnections = DEFAULT_MAX_CONNECTIONS;
            }

            props.setProperty(SUN_MAX_CONNECTIONS_PROPERTY, maxConnections);
        }
        return;
    }

    private void checkMessageFragmentSize(Properties props) {
        if (orbBean != null) {
            String fragmentSize, bufferSize;
            try {
                int fsize = ((Integer.parseInt(orbBean.getMessageFragmentSize().trim())) / 8) * 8;
                if (fsize < 32) {
                    fragmentSize = "32";
                    logger.log(Level.INFO, "Setting ORB Message Fragment size to " + fragmentSize);
                } else {
                    fragmentSize = String.valueOf(fsize);
                }
                bufferSize = fragmentSize;
            } catch (NumberFormatException nfe) {
                // Print stack trace and use default values
                logger.log(Level.WARNING, "enterprise_util.excep_in_reading_fragment_size", nfe);
                logger.log(Level.INFO, "Setting ORB Message Fragment size to Default " +
                        SUN_GIOP_DEFAULT_FRAGMENT_SIZE);
                fragmentSize = SUN_GIOP_DEFAULT_FRAGMENT_SIZE;
                bufferSize = SUN_GIOP_DEFAULT_BUFFER_SIZE;
            }
            props.setProperty(SUN_GIOP_FRAGMENT_SIZE_PROPERTY, fragmentSize);
            props.setProperty(SUN_GIOP_BUFFER_SIZE_PROPERTY, bufferSize);
        }
    }

    private void checkServerSSLOutboundSettings(Properties props) {
        if (iiopServiceBean != null) {
            SslClientConfig sslClientConfigBean = iiopServiceBean.getSslClientConfig();
            if (sslClientConfigBean != null) {
                Ssl ssl = sslClientConfigBean.getSsl();
                assert (ssl != null);
            }
        }
    }

    private void checkForOrbPropertyValues(Properties props) {
        if (orbBean != null) {
            List<Property> orbBeanProps = orbBean.getProperty();
            if (orbBeanProps != null) {
                for (int i = 0; i < orbBeanProps.size(); i++) {
                    props.setProperty(orbBeanProps.get(i).getName(), orbBeanProps.get(i).getValue());
                }
            }
        }
    }

    private String[] getORBInitRef(String orbInitialHost,
                                          String initialPort) {
        // Add -ORBInitRef NameService=....
        // This ensures that INS will be used to talk with the NameService.
        String[] newArgs = new String[]{
                "-ORBInitRef",
                "NameService=corbaloc:iiop:1.2@"
                        + orbInitialHost + ":"
                        + initialPort + "/NameService"
        };

        return newArgs;
    }

    private String[] getORBInitRef(String endpoints) {

        String[] list = (String[]) endpoints.split(",");
        String corbalocURL = getCorbalocURL(list);
        logger.fine("GlassFishORBManager.getORBInitRef = " + corbalocURL);

        // Add -ORBInitRef NameService=....
        // This ensures that INS will be used to talk with the NameService.
        String[] newArgs = new String[]{
                "-ORBInitRef",
                "NameService=corbaloc:" + corbalocURL + "/NameService"
        };

        return newArgs;
    }

    // TODO : Move this to naming  NOT needed for V3 FCS

    public static final String IIOP_ENDPOINTS_PROPERTY =
            "com.sun.appserv.iiop.endpoints";

    private static final String IIOP_URL = "iiop:1.2@";

    private String getCorbalocURL(Object[] list) {

        String corbalocURL = "";
        //convert list into corbaloc url
        for (int i = 0; i < list.length; i++) {
            logger.info("list[i] ==> " + list[i]);
            if (corbalocURL.equals("")) {
                corbalocURL = IIOP_URL + ((String) list[i]).trim();
            } else {
                corbalocURL = corbalocURL + "," +
                        IIOP_URL + ((String) list[i]).trim();
            }
        }
        logger.info("corbaloc url ==> " + corbalocURL);
        return corbalocURL;
    }
}
