package org.glassfish.enterprise.iiop.impl;

import org.glassfish.enterprise.iiop.api.GlassFishORBFactory;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.omg.CORBA.ORB;

import java.util.Properties;

/**
 * @author Mahesh Kannan
 *         Date: Jan 15, 2009
 */
@Service
public class GlassFishORBFactoryImpl
        implements GlassFishORBFactory, PostConstruct {

    @Inject
    private Habitat habitat;

    @Inject
    private IIOPUtils iiopUtils;

    private GlassFishORBManager gfORBManager;

    public void postConstruct() {
        gfORBManager = new GlassFishORBManager(habitat);
        iiopUtils.setGlassFishORBManager(gfORBManager);
    }

    public ORB createORB(Properties props) {
        // TODO change this to a create call
       return gfORBManager.getORB(props);
    }

    public Properties getCSIv2Props() {
        return gfORBManager.getCSIv2Props();
    }

    public void setCSIv2Prop(String name, String value) {
        gfORBManager.setCSIv2Prop(name, value);
    }

    public int getORBInitialPort() {
        return gfORBManager.getORBInitialPort();
    }

    public String getORBHost(ORB orb) {
        return ((com.sun.corba.ee.spi.orb.ORB) orb).getORBData().getORBInitialHost();
    }

    public int getORBPort(ORB orb) {
        return ((com.sun.corba.ee.spi.orb.ORB) orb).getORBData().getORBInitialPort();
    }


    
}