package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ServerRequestInfo;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;


import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.internal.grizzly.LazyServiceInitializer;
import org.glassfish.internal.api.ORBLocator;


import java.util.Properties;
import java.rmi.Remote;
import java.nio.channels.SelectableChannel;

import org.jvnet.hk2.component.PostConstruct;

/**
 * This class exposes any orb/iiop functionality needed by modules in the app server.
 * This prevents modules from needing any direct dependencies on the orb-iiop module.
 * @author Mahesh Kannan
 *         Date: Jan 17, 2009
 */
@Service
public class GlassFishORBHelper implements PostConstruct, ORBLocator {

    @Inject
    private Habitat habitat;

    @Inject
    private ProcessEnvironment processEnv;

    private GlassFishORBFactory orbFactory;

    private volatile ORB orb = null ;

    private ProtocolManager protocolManager = null ;

    private ORBLazyServiceInitializer lazyServiceInitializer;

    private SelectableChannelDelegate selectableChannelDelegate;

    public void postConstruct() {
        orbFactory = habitat.getByContract(GlassFishORBFactory.class);
    }

    public synchronized void setORB( ORB orb ) {
        this.orb = orb ;
    }

    /**
     * Get or create the default orb.  This can be called for any process type.  However,
     * protocol manager and CosNaming initialization only take place for the Server.
     */
    public ORB getORB() {
        // Use a volatile double-checked locking idiom here so that we can publish
        // a partly-initialized ORB early, so that lazy init can come into getORB() 
        // and allow an invocation to the transport to complete.
        if (orb == null) {

            synchronized( this ) {
                if (orb == null) {
                    try {
                        Properties props = new Properties();

                        // Create orb and make it visible.  This will allow
                        // loopback calls to getORB() from
                        // portable interceptors activated as a side-effect of the
                        // remaining initialization. If it's a
                        // server, there's a small time window during which the
                        // ProtocolManager won't be available.  Any callbacks that
                        // result from the protocol manager initialization itself
                        // cannot depend on having access to the protocol manager.
                        orb = orbFactory.createORB(props);

                        if( processEnv.getProcessType().isServer()) {
                            if (protocolManager == null) {
                                ProtocolManager tempProtocolManager =
                                                habitat.getByContract(ProtocolManager.class);

                                tempProtocolManager.initialize(orb);
                                tempProtocolManager.initializeNaming();
                                tempProtocolManager.initializePOAs();

                                // Now make protocol manager visible.
                                protocolManager = tempProtocolManager;
                                
                                GlassfishNamingManager namingManager =
                                    habitat.getByContract(GlassfishNamingManager.class);

                                Remote remoteSerialProvider =
                                    namingManager.initializeRemoteNamingSupport(orb);

                                protocolManager.initializeRemoteNaming(remoteSerialProvider);
                            }
                        }
                    } catch(Exception e) {
                        orb = null;
                        protocolManager = null;
                        throw new RuntimeException("Orb initialization erorr", e);    
                    }
                }
            }
        }

        return orb;
    }


    public void setSelectableChannelDelegate(SelectableChannelDelegate d) {
        selectableChannelDelegate = d;
    }

    public SelectableChannelDelegate getSelectableChannelDelegate() {
        return this.selectableChannelDelegate;
    }

    public static interface SelectableChannelDelegate {

        public void handleRequest(SelectableChannel channel);

    }
    

    /**
     * Get a protocol manager for creating remote references. ProtocolManager is only
     * available in the server.  Otherwise, this method returns null.
     *
     * If it's the server and the orb hasn't been already created, calling
     * this method has the side effect of creating the orb.
     */
    public ProtocolManager getProtocolManager() {

        if( !processEnv.getProcessType().isServer() ) {
            return null;
        }

        if (protocolManager == null) {
            getORB();
        }

        return protocolManager;
    }

    public boolean isORBInitialized() {
	return (orb != null);
    }

    public int getOTSPolicyType() {
        return orbFactory.getOTSPolicyType();    
    }

    public int getCSIv2PolicyType() {
        return orbFactory.getCSIv2PolicyType();    
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
      
    public boolean isEjbCall(ServerRequestInfo sri) {
        return orbFactory.isEjbCall(sri);
    }
      
    
}
