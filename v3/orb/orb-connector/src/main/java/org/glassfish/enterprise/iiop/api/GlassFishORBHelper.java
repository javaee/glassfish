package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.omg.CORBA.ORB;
import org.glassfish.enterprise.iiop.spi.EjbService;

import java.util.Properties;

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

    public ORB getORB() {
        return getORB(EMPTY_PROPERTIES);
    }

    //Note: We really do not want this here
    public ORB getORB(Properties props) {
        if (orb == null) {
            synchronized (this) {
                if (orb == null) {
                    GlassFishORBFactory factory = habitat.getByContract(GlassFishORBFactory.class);
                    orb = factory.createORB(props);
                }
            }
        }

        return orb;
    }

    public ProtocolManager getProtocolManager(EjbService ejbService) {
        if (protocolManager == null) {
            synchronized (this) {
                if (protocolManager == null) {
                    ProtocolManager tempProtocolManager = 
			habitat.getByContract(ProtocolManager.class);
                    tempProtocolManager.initialize(getORB(), ejbService);
                    // Everything succeeded.  Now set protocol manager
                    protocolManager = tempProtocolManager;
                }
            }
        }

        return protocolManager;
    }
    
}
