package org.glassfish.enterprise.iiop.impl;

import org.glassfish.enterprise.iiop.api.GlassFishORBFactory;
import org.glassfish.enterprise.iiop.impl.GlassFishORBManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.omg.CORBA.ORB;

import java.util.Properties;

/**
 * @author Mahesh Kannan
 *         Date: Jan 15, 2009
 */
@Service
public class GlassFishORBFactoryImpl
        implements GlassFishORBFactory {

    @Inject
    Habitat habitat;

    public ORB createORB(Properties props) {
        // TODO change this to a create call
       return GlassFishORBManager.getORB(props, habitat);
    }


    public String getORBHost(ORB orb) {

        return ((com.sun.corba.ee.spi.orb.ORB) orb).getORBData().getORBInitialHost();

    }

    public int getORBPort(ORB orb) {

        return ((com.sun.corba.ee.spi.orb.ORB) orb).getORBData().getORBInitialPort();

    }
    
}