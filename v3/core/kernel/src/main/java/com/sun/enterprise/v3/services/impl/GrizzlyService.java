/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.v3.services.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.util.Result;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.v3.services.impl.monitor.GrizzlyMonitoring;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.http.mapper.Mapper;
import com.sun.hk2.component.ConstructorWomb;
import org.glassfish.api.FutureProvider;
import org.glassfish.api.Startup;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * The Network Service is responsible for starting grizzly and register the
 * top level proxy. It is also providing a runtime service where other
 * services (like admin for instance) can register endpoints proxy to
 * particular context root.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class GrizzlyService implements Startup, RequestDispatcher, PostConstruct, PreDestroy, FutureProvider<Result<Thread>> {

    public static final int ALL_PORTS = Integer.MAX_VALUE;

    
    @Inject(name="server-config") // for now
    Config config;

    @Inject
    Logger logger;

    @Inject
    Habitat habitat;

    @Inject
    ProbeProviderFactory probeProviderFactory;

    List<NetworkProxy> proxies = new ArrayList<NetworkProxy>();

    List<Future<Result<Thread>>> futures;

    Collection<String> hosts = new ArrayList<String>();

    private final GrizzlyMonitoring monitoring;

    private final ConcurrentLinkedQueue<MapperUpdateListener> mapperUpdateListeners =
            new ConcurrentLinkedQueue<MapperUpdateListener>();

    private DynamicConfigListener configListener;

    public GrizzlyService() {
        futures = new ArrayList<Future<Result<Thread>>>();
        monitoring = new GrizzlyMonitoring();
    }
    
    /**
     * Add the new proxy to our list of proxies.
     * @param proxy new proxy to be added
     */
    public void addNetworkProxy(NetworkProxy proxy) {
        proxies.add(proxy);               
    }
    
    
    /**
     * Remove the new proxy from our list of proxies by port.
     * @param port number to be removed
     * @return <tt>true</tt>, if proxy on specified port was removed,
     *         <tt>false</tt> if no proxy was associated with the port.
     */    
    public boolean removeNetworkProxy(int port) {
        NetworkProxy proxy = null;
        for (NetworkProxy p : proxies) {
            if (p.getPort() == port) {
                proxy = p;
                break;
            }
        }
        if (proxy != null) {
            proxy.stop();
            proxy.destroy();
            proxies.remove(proxy);
            return true;
        }

        return false;
    }

    
    /**
     * Remove the new proxy from our list of proxies by id.
     * @return <tt>true</tt>, if proxy on specified port was removed,
     *         <tt>false</tt> if no proxy was associated with the port.
     */
    public boolean removeNetworkProxy(String id) {
        NetworkProxy proxy = null;
        for (NetworkProxy p : proxies) {
            if (p instanceof GrizzlyProxy) {
                GrizzlyProxy grizzlyProxy = (GrizzlyProxy) p;
                if (grizzlyProxy.networkListener != null &&
                        grizzlyProxy.networkListener.getName() != null &&
                        grizzlyProxy.networkListener.getName().equals(id)) {
                    proxy = p;
                    break;
                }
            }
        }
        
        if (proxy != null) {
            proxy.stop();
            proxy.destroy();
            proxies.remove(proxy);
            return true;
        }

        return false;
    }

    /**
     * Is there any {@link MapperUpdateListener} registered?
     */
    public boolean hasMapperUpdateListener(){
        return (mapperUpdateListeners.size() > 0? true:false);
    }

    /**
     * Adds {@link MapperUpdateListener} to listeners queue.
     * 
     * @param listener the listener to be added.
     * @return <tt>true</tt>, if listener was successfully added,
     * or <tt>false</tt> otherwise.
     */
    public boolean addMapperUpdateListener(MapperUpdateListener listener) {
        return mapperUpdateListeners.add(listener);
    }

    /**
     * Removes {@link MapperUpdateListener} to listeners queue.
     *
     * @param listener the listener to be removed.
     * @return <tt>true</tt>, if listener was successfully removed,
     * or <tt>false</tt> otherwise.
     */
    public boolean removeMapperUpdateListener(MapperUpdateListener listener) {
        return mapperUpdateListeners.remove(listener);
    }

    /**
     * Notify all {@link MapperUpdateListener}s about update happened.
     * 
     * @param networkListener {@link NetworkListener}, which {@link Mapper} got changed
     * @param mapper new {@link Mapper} value
     */
    public void notifyMapperUpdateListeners(NetworkListener networkListener,
            Mapper mapper) {
        final HttpService httpService = config.getHttpService();
        for(MapperUpdateListener listener : mapperUpdateListeners) {
            listener.update(httpService, networkListener, mapper);
        }
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */   
    public Logger getLogger() {
        return logger;
    }


    /**
     * Gets the habitat.
     *
     * @return the habitat
     */   
    public Habitat getHabitat() {
        return habitat;
    }

    public GrizzlyMonitoring getMonitoring() {
        return monitoring;
    }

    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    @Override
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;                
    }


    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    @Override
    public void postConstruct() {
        NetworkConfig networkConfig = config.getNetworkConfig();

        configListener = new DynamicConfigListener();
        
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(networkConfig.getNetworkListeners());
        bean.addListener(configListener);
        bean = (ObservableBean) ConfigSupport.getImpl(config.getHttpService());
        bean.addListener(configListener);

        configListener.setGrizzlyService(this);
        configListener.setLogger(logger);

        try {
            futures = new ArrayList<Future<Result<Thread>>>();
            for (NetworkListener listener : networkConfig.getNetworkListeners().getNetworkListener()) {
                createNetworkProxy(listener);
            }
            
            /*
             * Ideally (and ultimately), all services that need lazy Init will add a network-listener element
             * in the domain.xml with name = "light-weight-listener". And a LWL instance would have been created
             * by the above loop. But for v3-FCS, IIOP and JMS listener will not
             * be able to reach that stage - hence we create a dummy network listener object here and use that
             * to create proxies etc. Whenever, IIOP and JMS listeners move to use network-listener elements,
             * then this code can be removed
             */
            List<IiopListener> iiopListenerList = config.getIiopService().getIiopListener();
            for(IiopListener oneListener : iiopListenerList) {
                if(Boolean.valueOf(oneListener.getEnabled()) && Boolean.valueOf(oneListener.getLazyInit())) {
                    NetworkListener dummy = new DummyNetworkListener();
                    dummy.setPort(oneListener.getPort());
                    dummy.setAddress(oneListener.getAddress());
                    dummy.setProtocol("light-weight-listener");
                    dummy.setTransport("tcp");
                    dummy.setName("iiop-service");
                    createNetworkProxy(dummy);
                }
            }
            /*
             * Do the same as above for JMS listeners also but only for MQ's EMBEDDED MODE
             */
            if("EMBEDDED".equalsIgnoreCase(config.getJmsService().getType())) {
                List<JmsHost> jmsHosts = config.getJmsService().getJmsHost();
                for(JmsHost oneHost : jmsHosts) {
                    if( Boolean.valueOf(oneHost.getLazyInit())) {
                        NetworkListener dummy = new DummyNetworkListener();
                        dummy.setPort(oneHost.getPort());
                        dummy.setAddress(oneHost.getHost());
                        dummy.setProtocol("light-weight-listener");
                        dummy.setTransport("tcp");
                        dummy.setName("mq-service");
                        createNetworkProxy(dummy);
                    }
                }
            }

            registerNetworkProxy(); 
        } catch(RuntimeException e) { // So far postConstruct can not throw any other exception type
            logger.log(Level.SEVERE, "Unable to start v3. Closing all ports",e);
            for(NetworkProxy proxy : proxies) {
                try {
                    proxy.stop();
                } catch(Exception proxyStopException) {
                    logger.log(Level.SEVERE, "Exception closing port: " 
                            + proxy.getPort() , proxyStopException);
                }
            }
            
            throw e;
        }
    }

    @Override
    public List<Future<Result<Thread>>> getFutures() {
        return futures;
    }

    /*
     * Creates a new NetworkProxy for a particular HttpListner
     * @param listener NetworkListener
     * @param networkConfig HttpService
     */
    public synchronized Future<Result<Thread>> createNetworkProxy(NetworkListener listener) {

        if (ConfigBeansUtilities.toBoolean(listener.getJkEnabled())) {
            return null;
        }

        if (!Boolean.valueOf(listener.getEnabled())) {
            logger.info("Network listener " + listener.getName() +
                    " on port " + listener.getPort() +
                    " disabled per domain.xml");
            return null;
        }

        // create the proxy for the port.
        GrizzlyProxy proxy = new GrizzlyProxy(this, listener);
        if(!("light-weight-listener".equals(listener.getProtocol()))) {
            final NetworkConfig networkConfig = listener.getParent(NetworkListeners.class).getParent(NetworkConfig.class);
            // attach all virtual servers to this port
            for (VirtualServer vs : networkConfig.getParent(Config.class).getHttpService().getVirtualServer()) {
                List<String> vsListeners = 
                    StringUtils.parseStringList(vs.getNetworkListeners(), " ,");
                if (vsListeners == null || vsListeners.isEmpty() ||
                        vsListeners.contains(listener.getName())) {
                    if (!hosts.contains(vs.getId())){
                        hosts.add(vs.getId());
                    }
                }            
            }
            addChangeListener(listener);
            addChangeListener(listener.findThreadPool());
            addChangeListener(listener.findTransport());
            final Protocol protocol = listener.findHttpProtocol();
            addChangeListener(protocol);
            addChangeListener(protocol.getHttp());
            addChangeListener(protocol.getHttp().getFileCache());
            addChangeListener(protocol.getSsl());
        }

        Future<Result<Thread>> future =  proxy.start();
        // add the new proxy to our list of proxies.
        proxies.add(proxy);
        futures.add(future);

        return future;
    }

    private void addChangeListener(ConfigBeanProxy bean) {
        if(bean != null) {
            ((ObservableBean) ConfigSupport.getImpl(bean)).addListener(configListener);
        }
    }

    /*
    * Registers all proxies
    */
    public void registerNetworkProxy() {
        registerNetworkProxy(ALL_PORTS);
    }

    /*
     * Registers all proxies
     */
    public void registerNetworkProxy(int port) {
        for (org.glassfish.api.container.Adapter subAdapter :
            habitat.getAllByContract(org.glassfish.api.container.Adapter.class)) {
            //@TODO change EndportRegistrationException processing if required
            try {
                if (!subAdapter.isRegistered()) {
                    registerAdapter(subAdapter);
                    subAdapter.setRegistered(true);
                }
            } catch(EndpointRegistrationException e) {
                logger.log(Level.WARNING, 
                        "GrizzlyService endpoint registration problem", e);
            }
        }
    }
    
    
    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {
        for (NetworkProxy proxy : proxies) {
            proxy.stop();
        }
    }

    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    @Override
    public void registerEndpoint(String contextRoot, Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException {

        registerEndpoint(contextRoot, endpointAdapter, container, null);
    }

    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     * @param application container
     * @param virtualServers comma separated list of the virtual servers
     */
    @Override
    public void registerEndpoint(String contextRoot, Adapter endpointAdapter,
        ApplicationContainer container, String virtualServers) throws EndpointRegistrationException {
        List<String> virtualServerList = new ArrayList<String>();
        if (virtualServers == null) {
            virtualServerList = 
                config.getHttpService().getNonAdminVirtualServerList();
        } else{
            virtualServerList = 
                StringUtils.parseStringList(virtualServers, ",");
        }
        registerEndpoint(contextRoot, virtualServerList, endpointAdapter, container);
    }


    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    @Override
    public void registerEndpoint(String contextRoot, Collection<String> vsServers,
            Adapter endpointAdapter,
            ApplicationContainer container) throws EndpointRegistrationException {
            
        Collection<String> ports = getPortsFromVirtualServers(vsServers);
        for (String portStr : ports) {
            int port = Integer.parseInt(portStr);
            registerEndpoint(contextRoot, port, vsServers, endpointAdapter, container);
        }
    }


    /**
     * Registers a new endpoint for the given context root at the given port
     * number.
     */
    @Override
    public void registerEndpoint(String contextRoot,
                                 int port,
                                 Collection<String> vsServers,
                                 Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException {
        for (NetworkProxy proxy : proxies) {
            if (port == ALL_PORTS || proxy.getPort() == port) {
                proxy.registerEndpoint(contextRoot, vsServers,
                                       endpointAdapter, container);
            }
        }
    }


    /**
     * Removes the contex-root from our list of endpoints.
     */
    @Override
    public void unregisterEndpoint(String contextRoot) throws EndpointRegistrationException {
        unregisterEndpoint(contextRoot, null);
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    @Override
    public void unregisterEndpoint(String contextRoot, 
            ApplicationContainer app) throws EndpointRegistrationException {
        for (NetworkProxy proxy : proxies) {
            proxy.unregisterEndpoint(contextRoot, app);
        }
    }


    /**
     * Probe provider that implements each probe provider method as a 
     * no-op.
     */
    public static class NoopInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            // Deliberate no-op
            return null;
        }
    }

    private void registerAdapter(org.glassfish.api.container.Adapter a) throws EndpointRegistrationException {
        int port        = a.getListenPort();
        List<String> vs = a.getVirtualServers();
        String cr       = a.getContextRoot();
        this.registerEndpoint(cr, port, vs, a, null);
    }

    // get the ports from the http listeners that are associated with 
    // the virtual servers
    private List<String> getPortsFromVirtualServers(Collection<String> virtualServers) {
        List<String> ports = new ArrayList<String>();
        List<NetworkListener> networkListenerList = config.getNetworkConfig().getNetworkListeners().getNetworkListener();

        for (String vs : virtualServers) {
            VirtualServer virtualServer = 
                config.getHttpService().getVirtualServerByName(vs);
            if (virtualServer == null) {
                // non-existent virtual server
                logger.warning("Skip registering endpoint with non existent virtual server: " + vs);
                continue;
            }
            String vsNetworkListeners = virtualServer.getNetworkListeners();
            List<String> vsNetworkListenerList =
                StringUtils.parseStringList(vsNetworkListeners, ",");
            for (String vsNetworkListener : vsNetworkListenerList) {
                for (NetworkListener networkListener : networkListenerList) {
                    if (networkListener.getName().equals(vsNetworkListener) && 
                        Boolean.valueOf(networkListener.getEnabled())) {
                        ports.add(networkListener.getPort());
                        break;
                    }
                }
            }
        } 
        return ports;
    }
}
