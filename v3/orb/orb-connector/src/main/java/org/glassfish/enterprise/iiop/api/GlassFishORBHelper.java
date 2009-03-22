package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;
import org.omg.CORBA.ORB;
import org.glassfish.enterprise.iiop.spi.EjbService;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;


import org.glassfish.api.naming.GlassfishNamingManager;

import java.util.Properties;
import java.rmi.Remote;

/**
 * @author Mahesh Kannan
 *         Date: Jan 17, 2009
 */
@Service
public class GlassFishORBHelper {

    public static final String JNDI_CORBA_ORB_PROPERTY = "java.naming.corba.orb";
    public static final String OMG_ORB_INIT_HOST_PROPERTY = "org.omg.CORBA.ORBInitialHost";
    public static final String OMG_ORB_INIT_PORT_PROPERTY = "org.omg.CORBA.ORBInitialPort";

    public static final String DEFAULT_ORB_INIT_HOST = "localhost";
    public static final String DEFAULT_ORB_INIT_PORT = "3700";

    @Inject
    private Habitat habitat;

    @Inject
    private ProcessEnvironment processEnv;

    private volatile ORB orb;

    private volatile ProtocolManager protocolManager;

    /**
     * Get the default orb for this habitat. Habitat must have been created
     * before this is called.
     * @return
     */
    public ORB getORB() {

        if (orb == null) {

            synchronized (this) {

                if (orb == null) {

                    try {

                        GlassFishORBFactory factory = habitat.getByContract(GlassFishORBFactory.class);

                        Properties props = new Properties();
                        ORB tempOrb = factory.createORB(props);

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
     * Get a protocol manager for creating remote references.  This should only be called
     * by the ejb container.
     */
    public ProtocolManager getProtocolManager() {

        if (protocolManager == null) {
            getORB();
        }

        return protocolManager;
    }
    
}
