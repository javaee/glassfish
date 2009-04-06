package org.glassfish.enterprise.iiop.impl;

import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.enterprise.iiop.api.GlassFishORBLifeCycleListener;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.omg.CORBA.ORB;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;

import java.util.Collection;
import java.util.List;

/**
 * @author Mahesh Kannan
 *         Date: Jan 15, 2009
 */
@Service
public class IIOPUtils implements PostConstruct {

    private static IIOPUtils _me;


    @Inject
    private Habitat habitat;

    @Inject
    private ClassLoaderHierarchy clHierarchy;

    @Inject
    private ProcessEnvironment processEnv;

    private ProcessType processType;

    // The following info is only available for ProcessType.Server
    private Collection<ThreadPool> threadPools;
    private IiopService iiopService;
    private Collection<ServerRef> serverRefs;
    private Configs configs;

    // Set during init
    private ORB defaultORB;

    private GlassFishORBManager gfORBMgr;

    public void postConstruct() {
        _me = this;

        processType = processEnv.getProcessType();

        if( processEnv.getProcessType() == ProcessType.Server) {

            iiopService = habitat.getComponent(IiopService.class);
            threadPools = habitat.getAllByContract(ThreadPool.class);
            serverRefs  = habitat.getAllByContract(ServerRef.class);
            configs     = habitat.getComponent(Configs.class);
        }

    }



    public static IIOPUtils getInstance() {
        return _me;
    }

    void setGlassFishORBManager(GlassFishORBManager orbMgr) {
        gfORBMgr = orbMgr;
    }

    GlassFishORBManager getGlassFishORBManager() {
        return gfORBMgr;
    }

    public ClassLoader getCommonClassLoader() {
        return clHierarchy.getCommonClassLoader();
    }

    private void assertServer() {
        if ( processType != processType.Server ) {
            throw new IllegalStateException("Only available in Server mode");
        }
    }

    public IiopService getIiopService() {
        assertServer();
        return iiopService;
    }

    public Collection<ThreadPool> getAllThreadPools() {
        assertServer();
        return threadPools;
    }

    public Collection<ServerRef> getServerRefs() {
        assertServer();
        return serverRefs;
    }

    public List<IiopListener> getIiopListeners() {
        assertServer();
        return iiopService.getIiopListener();
    }

    public Collection<IIOPInterceptorFactory> getAllIIOPInterceptrFactories() {
        return habitat.getAllByContract(IIOPInterceptorFactory.class);
    }

    public Collection<GlassFishORBLifeCycleListener> getGlassFishORBLifeCycleListeners() {
        return habitat.getAllByContract(GlassFishORBLifeCycleListener.class);
    }

    public void setORB(ORB orb) {
        defaultORB = orb;
    }

    // For internal use only.  All other modules should use orb-connector
    // GlassFishORBHelper to acquire default ORB.
    public ORB getORB() {
        return defaultORB;
    }
}