package org.glassfish.enterprise.iiop.impl;

import org.glassfish.enterprise.iiop.api.GlassFishORBFactory;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ServerRequestInfo;

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
        IIOPUtils.setInstance(iiopUtils);
        //iiopUtils.setGlassFishORBManager(gfORBManager);
    }

    public int getOTSPolicyType() {
        return POARemoteReferenceFactory.OTS_POLICY_TYPE;
    }

    public int getCSIv2PolicyType() {
        return POARemoteReferenceFactory.CSIv2_POLICY_TYPE;
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

    /**
     * Returns true, if the incoming call is a EJB method call.
     * This checks for is_a calls and ignores those calls. In callflow analysis
     * when a component looks up another component, this lookup should be
     * considered part of the same call coming in.
     * Since a lookup triggers the iiop codebase, it will fire a new request start.
     * With this check, we consider the calls that are only new incoming ejb
     * method calls as new request starts.
     */
    public boolean isEjbCall (ServerRequestInfo sri) {
        return (gfORBManager.isEjbAdapterName(sri.adapter_name()) &&
                (!gfORBManager.isIsACall(sri.operation())));
    }

    
}
