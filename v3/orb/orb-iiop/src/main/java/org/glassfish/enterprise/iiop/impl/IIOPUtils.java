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
public class IIOPUtils
        implements PostConstruct {

    private static IIOPUtils _me;

    /* TODO
    @Inject
    ThreadPool[] threadPools;
    */

    @Inject
    Habitat habitat;

    @Inject
    ClassLoaderHierarchy clHierarchy;

    @Inject
    ProcessEnvironment processEnv;

    /** TODO
    @Inject
    IiopService iiopService;
    **/

    /**
    @Inject
    ServerRef[] serverRefs;
    **/

    /**
    @Inject
    Configs configs;
    **/

    private volatile ORB gfORB;

    public void postConstruct() {
        _me = this;
    }

    public ThreadPool[] getAllThreadPools() {
        return null; // TODO threadPools;
    }

    public static IIOPUtils getInstance() {
        return _me;
    }

    public ClassLoader getCommonClassLoader() {
        return clHierarchy.getCommonClassLoader();
    }

    public IiopService getIiopService() {
        return null; // TODO iiopService;
    }

    public ServerRef[] getServerRefs() {
        return null; // TODO serverRefs;
    }

    public List<IiopListener> getIiopListeners() {
        return null; // TODO iiopService.getIiopListener();
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