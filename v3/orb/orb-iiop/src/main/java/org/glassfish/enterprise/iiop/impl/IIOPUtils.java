package org.glassfish.enterprise.iiop.impl;

import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.enterprise.iiop.api.GlassFishORBLifeCycleListener;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.omg.CORBA.ORB;

import java.util.Collection;
import java.util.List;

/**
 * @author Mahesh Kannan
 *         Date: Jan 15, 2009
 */
public class IIOPUtils
        implements PostConstruct {

    private static IIOPUtils _me;

    @Inject
    ThreadPool[] threadPools;

    @Inject
    Habitat habitat;

    @Inject
    ClassLoaderHierarchy clHierarchy;

    @Inject
    IiopService iiopService;

    @Inject
    Clusters clusters;

    @Inject
    Cluster cluster;

    @Inject
    ServerRef[] serverRefs;

    @Inject
    Configs configs;

    private volatile ORB gfORB;

    public void postConstruct() {
        _me = this;
    }

    public ThreadPool[] getAllThreadPools() {
        return threadPools;
    }

    public static IIOPUtils getInstance() {
        return _me;
    }

    public ClassLoader getCommonClassLoader() {
        return clHierarchy.getCommonClassLoader();
    }

    public IiopService getIiopService() {
        return iiopService;
    }

    public Clusters getClusters() {
        return clusters;
    }

    public Cluster getMyCluster() {
        return cluster;
    }

    public ServerRef[] getServerRefs() {
        return serverRefs;
    }

    public List<IiopListener> getIiopListeners() {
        return iiopService.getIiopListener();
    }

    //TODO
    public boolean isAppClientContainer() {
        return false;
    }

    public Collection<IIOPInterceptorFactory> getAllIIOPInterceptrFactories() {
        return habitat.getAllByContract(IIOPInterceptorFactory.class);
    }

    public Collection<GlassFishORBLifeCycleListener> getGlassFishORBLifeCycleListeners() {
        return habitat.getAllByContract(GlassFishORBLifeCycleListener.class);
    }

    public ORB getORB() {
        if (gfORB == null) {
            synchronized (this) {
                //GlassFishORBManager.getORB();
            }
        }

        return gfORB;
    }
}