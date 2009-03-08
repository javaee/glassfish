package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;
import org.omg.CORBA.ORB;
import org.glassfish.enterprise.iiop.spi.EjbService;

import javax.naming.InitialContext;
import org.glassfish.api.naming.GlassfishNamingManager;

import java.util.Properties;
import java.util.Hashtable;

/**
 * @author Mahesh Kannan
 *         Date: Jan 17, 2009
 */
@Service
public class GlassFishORBHelper {

    private static final Properties EMPTY_PROPERTIES = new Properties();

    @Inject
    Habitat habitat;

    private volatile ORB orb;

    private volatile ProtocolManager protocolManager;

    // @@@ Need a way to figure out if we're in server mode or not
    // Right now only do server-specific processing when getProtocolManager
    // is called (should only be called by ejb container

    public ORB getORB() {

        if (orb == null) {

            synchronized (this) {

                if (orb == null) {

                    try {

                        GlassFishORBFactory factory = habitat.getByContract(GlassFishORBFactory.class);

                        Properties props = new Properties();
                        ORB tempOrb = factory.createORB(props);

                        orb = tempOrb;

                    } catch(Exception e) {
                        throw new RuntimeException("Orb initialization erorr", e);    
                    }

                }
            }
        }

        return orb;
    }

    public ProtocolManager getProtocolManager() {


        if (protocolManager == null) {

            synchronized (this) {

                if (protocolManager == null) {

                    getORB();

                    try {

                        GlassFishORBFactory factory = habitat.getByContract(GlassFishORBFactory.class);

                        Properties props = new Properties();
                        ORB tempOrb = factory.createORB(props);


                        ProtocolManager tempProtocolManager =
			                habitat.getByContract(ProtocolManager.class);

                        tempProtocolManager.initialize(orb);

                        tempProtocolManager.initializeNaming();

                        tempProtocolManager.initializePOAs();

                        GlassfishNamingManager namingManager =
                            habitat.getByContract(GlassfishNamingManager.class);

                        namingManager.initializeRemoteNamingSupport(orb);

                        protocolManager = tempProtocolManager;


                    } catch(Exception e) {
                        throw new RuntimeException("ProtocolManager initialization erorr", e);    
                    }

                }
            }
        }

        return protocolManager;
    }
    
}
