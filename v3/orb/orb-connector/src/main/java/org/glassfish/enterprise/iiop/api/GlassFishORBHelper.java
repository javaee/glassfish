package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.omg.CORBA.ORB;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;


import org.glassfish.api.naming.GlassfishNamingManager;

import java.util.Properties;
import java.rmi.Remote;

import org.jvnet.hk2.component.PostConstruct;

/**
 * This class exposes any orb/iiop functionality needed by modules in the app server.
 * This prevents modules from needing any direct dependencies on the orb-iiop module.
 * @author Mahesh Kannan
 *         Date: Jan 17, 2009
 */
@Service
public class GlassFishORBHelper implements PostConstruct {

    public static final String JNDI_CORBA_ORB_PROPERTY = "java.naming.corba.orb";
    public static final String OMG_ORB_INIT_HOST_PROPERTY = "org.omg.CORBA.ORBInitialHost";
    public static final String OMG_ORB_INIT_PORT_PROPERTY = "org.omg.CORBA.ORBInitialPort";

    public static final String DEFAULT_ORB_INIT_HOST = "localhost";
    public static final String DEFAULT_ORB_INIT_PORT = "3700";

 
    // This property is true if SSL is required to be used by
    // non-EJB CORBA objects in the server.
    public static final String ORB_SSL_SERVER_REQUIRED =
            "com.sun.CSIV2.ssl.server.required";
    //
    // This property is true if client authentication is required by
    // non-EJB CORBA objects in the server.
    public static final String ORB_CLIENT_AUTH_REQUIRED =
            "com.sun.CSIV2.client.auth.required";

     // This property is true (in appclient Main)
    // if SSL is required to be used by clients.
    public static final String ORB_SSL_CLIENT_REQUIRED =
            "com.sun.CSIV2.ssl.client.required";

    @Inject
    private Habitat habitat;

    @Inject
    private ProcessEnvironment processEnv;

    private GlassFishORBFactory orbFactory;

    private volatile ORB orb;

    private volatile ProtocolManager protocolManager;

    public void postConstruct() {
        orbFactory = habitat.getByContract(GlassFishORBFactory.class);   
    }

    /**
     * Get or create the default orb.  This can be called for any process type.  However,
     * protocol manager and CosNaming initialization only take place for the Server.
     */
    public ORB getORB() {

        if (orb == null) {

            synchronized (this) {

                if (orb == null) {

                    try {

                        Properties props = new Properties();
                        ORB tempOrb = orbFactory.createORB(props);

                        if( processEnv.getProcessType() == ProcessType.Server) {

                            ProtocolManager tempProtocolManager =
			                    habitat.getByContract(ProtocolManager.class);

                            tempProtocolManager.initialize(tempOrb);

                            tempProtocolManager.initializeNaming();

                            tempProtocolManager.initializePOAs();

                            GlassfishNamingManager namingManager =
                                habitat.getByContract(GlassfishNamingManager.class);

                            Remote remoteSerialProvider =
                                namingManager.initializeRemoteNamingSupport(tempOrb);

                            tempProtocolManager.initializeRemoteNaming(remoteSerialProvider);

                            protocolManager = tempProtocolManager;

                        }

                        orb = tempOrb;

                    } catch(Exception e) {
                        throw new RuntimeException("Orb initialization erorr", e);    
                    }

                }
            }
        }

        return orb;
    }

    /**
     * Get a protocol manager for creating remote references. ProtocolManager is only
     * available in the server.  Otherwise, this method returns null.
     *
     * If it's the server and the orb hasn't been already created, calling
     * this method has the side effect of creating the orb.
     */
    public ProtocolManager getProtocolManager() {

        if( processEnv.getProcessType() != ProcessType.Server ) {
            return null;
        }

        if (protocolManager == null) {
            getORB();
        }

        return protocolManager;
    }


    public Properties getCSIv2Props() {
        return orbFactory.getCSIv2Props();
    }

    public void setCSIv2Prop(String name, String value) {
        orbFactory.setCSIv2Prop(name, value);
    }

    public int getORBInitialPort() {
        return orbFactory.getORBInitialPort();
    }

    public String getORBHost(ORB orb) {
        return orbFactory.getORBHost(orb);
    }

    public int getORBPort(ORB orb) {
        return orbFactory.getORBPort(orb);
    }
      
    
}
