package org.glassfish.enterprise.iiop.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.ThreadPool;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.enterprise.iiop.api.GlassFishORBLifeCycleListener;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.omg.CORBA.ORB;

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

    //private GlassFishORBManager gfORBMgr;

    public void postConstruct() {

        processType = processEnv.getProcessType();

        if( processEnv.getProcessType().isServer()) {

            iiopService = habitat.getComponent(IiopService.class);
            final Collection<ThreadPool> threadPool = habitat.getAllByContract(ThreadPool.class);
            final Collection<NetworkListener> listeners = habitat.getAllByContract(NetworkListener.class);
            final Set<String> names = new TreeSet<String>();
            threadPools = new ArrayList<ThreadPool>();
            for (NetworkListener listener : listeners) {
                names.add(listener.getThreadPool());
            }
            for (ThreadPool pool : threadPool) {
                if(!names.contains(pool.getName())) {
                    threadPools.add(pool);
                }
            }
            serverRefs  = habitat.getAllByContract(ServerRef.class);
            configs     = habitat.getComponent(Configs.class);
        }

        _me = this;

    }



    public static IIOPUtils getInstance() {
        return _me;
    }


    public static void setInstance(IIOPUtils utils) {
        _me = utils;
    }

    /*
    void setGlassFishORBManager(GlassFishORBManager orbMgr) {
        gfORBMgr = orbMgr;
    }

    GlassFishORBManager getGlassFishORBManager() {
        return gfORBMgr;
    }
    */

    public ClassLoader getCommonClassLoader() {
        return clHierarchy.getCommonClassLoader();
    }

    private void assertServer() {
        if ( !processType.isServer() ) {
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

    public ProcessType getProcessType() {
        return processType;
    }

    public Habitat getHabitat() {
        return habitat;
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
