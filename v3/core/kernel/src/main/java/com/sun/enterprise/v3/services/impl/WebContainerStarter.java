/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.v3.server.ContainerStarter;
import com.sun.logging.LogDomains;
import com.sun.hk2.component.*;
import org.glassfish.api.Startup;
import org.glassfish.api.container.*;
import org.glassfish.internal.data.ContainerRegistry;
import org.glassfish.internal.data.EngineInfo;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;

/**
 * Startup service for the web container.
 *
 * This service checks if any domain.xml configuration, or changes in
 * such configuration, that can be handled only by the web container
 * (e.g., access logging) have been specified, and if so, starts the
 * web container (unless already started).
 *
 * @author jluehe
 */
@Service
@Scoped(Singleton.class)
public class WebContainerStarter
        implements Startup, PostConstruct, ConfigListener {

    private static final Logger logger = LogDomains.getLogger(
        WebContainerStarter.class, LogDomains.WEB_LOGGER);

    private static final String AUTH_PASSTHROUGH_ENABLED_PROP =
        "authPassthroughEnabled";

    private static final String PROXY_HANDLER_PROP = "proxyHandler";

    private static final String TRACE_ENABLED_PROP = "traceEnabled";

    @Inject
    Domain domain;

    @Inject
    ContainerRegistry containerRegistry;

    @Inject
    ContainerStarter containerStarter;

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    public HttpService httpService;

    @Inject
    private Habitat habitat;


    /**
     * Scans the domain.xml to see if it specifies any configuration
     * that can be handled only by the web container, and if so, starts
     * the web container
     */ 
    public void postConstruct() {
        boolean isStartNeeded = false;
        List<Config> configs = domain.getConfigs().getConfig();
        for (Config config : configs) {
            HttpService httpService = config.getHttpService();
            if (isStartNeeded(httpService)) {
                isStartNeeded = true;
                break;
            }
            
            List<VirtualServer> hosts = httpService.getVirtualServer();
            if (hosts != null) {
                for (VirtualServer host : hosts) {
                    if (isStartNeeded(host)) {
                        isStartNeeded = true;
                        break;
                    }
                }
                if (isStartNeeded) {
                    break;
                }
            }
        }

        if (isStartNeeded) {
            startWebContainer();
        } else {
            ObservableBean httpServiceBean = (ObservableBean)
                ConfigSupport.getImpl(httpService);
            httpServiceBean.addListener(this);
        }
    }

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server,
        // hence SERVER
        return Lifecycle.SERVER;
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new Changed() {
            public <T extends ConfigBeanProxy> NotProcessed changed(
                    TYPE type, Class<T> tClass, T t) {
                if (t instanceof HttpService) {
                    if (type == TYPE.CHANGE) {
                        if (isStartNeeded((HttpService) t)) {
                            startWebContainer();
                        }
                    }
                } else if (t instanceof VirtualServer) {
                    if (type == TYPE.ADD || type == TYPE.CHANGE) {
                        if (isStartNeeded((VirtualServer) t)) {
                            startWebContainer();
                        }
                    }
                }
                return null;
            }
        }
        , logger);
    }

    /**
     * Starts the web container
     */
    private void startWebContainer() {
        Sniffer webSniffer = habitat.getComponent(Sniffer.class,"web");
        if (webSniffer==null) {
            logger.info("Web container not installed");
            return;
        }
        if (containerRegistry.getContainer(
                    webSniffer.getContainersNames()[0]) != null) {
            containerRegistry.getContainer(
                    webSniffer.getContainersNames()[0]).getContainer();
        } else {
            Module snifferModule = modulesRegistry.find(webSniffer.getClass());
            try {
                Collection<EngineInfo> containersInfo =
                    containerStarter.startContainer(webSniffer, snifferModule);
                if (containersInfo != null && !containersInfo.isEmpty()) {
                    // Start each container
                    for (EngineInfo info : containersInfo) {
                        info.getContainer();
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("Done with starting " +
                                webSniffer.getModuleType() + " container");
                        }
                    }
                } else {
                    logger.severe(
                        "Unable to start container (no exception provided)");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to start container " +
                    webSniffer.getContainersNames()[0], e);
            }
        }
    }

    /*
     * @return true if the given HttpService contains any configuration
     * that can be handled only by the web container and therefore requires
     * the web container to be started, false otherwise
     */
    private boolean isStartNeeded(HttpService httpService) {
        if (ConfigBeansUtilities.toBoolean(
                    httpService.getAccessLoggingEnabled()) ||
                ConfigBeansUtilities.toBoolean(
                    httpService.getSsoEnabled())) {
            return true;
        }

        List<Property> props = httpService.getProperty();
        if (props != null) {
            for (Property prop : props) {
                String propName = prop.getName();
                String propValue = prop.getValue();
                if (AUTH_PASSTHROUGH_ENABLED_PROP.equals(propName)) {
                    if (ConfigBeansUtilities.toBoolean(propValue)) {
                        return true;
                    }
                } else if (PROXY_HANDLER_PROP.equals(propName)) {
                    return true;
                } else if (TRACE_ENABLED_PROP.equals(propName)) {
                    if (!ConfigBeansUtilities.toBoolean(propValue)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /*
     * @return true if the given VirtualServer contains any configuration
     * that can be handled only by the web container and therefore requires
     * the web container to be started, false otherwise
     */
    private boolean isStartNeeded(VirtualServer host) {
        if (ConfigBeansUtilities.toBoolean(host.getAccessLoggingEnabled()) ||
                ConfigBeansUtilities.toBoolean(host.getSsoEnabled())) {
            return true;
        }

        String state = host.getState();
        if (state != null &&
                ("disabled".equals(state) ||
                    !ConfigBeansUtilities.toBoolean(state))) {
            return true;
        }
     
        List<Property> props = host.getProperty();
        if (props != null && !props.isEmpty()) {
            return true;
        }

        return false;
    }
}
