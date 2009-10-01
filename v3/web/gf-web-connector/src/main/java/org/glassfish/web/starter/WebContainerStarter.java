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

package org.glassfish.web.starter;

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
import org.glassfish.api.Startup;
import org.glassfish.internal.data.ContainerRegistry;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.web.sniffer.WebSniffer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;
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
public class WebContainerStarter implements Startup, PostConstruct {

    private static final Logger logger = LogDomains.getLogger(
        WebContainerStarter.class, LogDomains.WEB_LOGGER);

    private static final String AUTH_PASSTHROUGH_ENABLED =
        "authPassthroughEnabled";

    private static final String PROXY_HANDLER = "proxyHandler";

    @Inject
    Domain domain;

    @Inject
    ContainerRegistry containerRegistry;

    @Inject
    ContainerStarter containerStarter;

    @Inject
    ModulesRegistry modulesRegistry;

    /**
     * Scans the domain.xml to see if it specifies any configuration
     * that can be handled only by the web container, and if so, starts
     * the web container
     */ 
    public void postConstruct() {
        boolean startNeeded = false;
        List<Config> configs = domain.getConfigs().getConfig();
        for (Config config : configs) {
            HttpService httpService = config.getHttpService();
            if (ConfigBeansUtilities.toBoolean(
                        httpService.getAccessLoggingEnabled()) ||
                    ConfigBeansUtilities.toBoolean(
                        httpService.getSsoEnabled())) {
                startNeeded = true;
            }

            if (!startNeeded) {
                List<Property> props = httpService.getProperty();
                if (props != null) {
                    for (Property prop : props) {
                        String propName = prop.getName();
                        String propValue = prop.getValue();
                        if (AUTH_PASSTHROUGH_ENABLED.equals(propName)) {
                            startNeeded = ConfigBeansUtilities.toBoolean(
                                    propValue);
                            if (startNeeded) break;
                        } else if (PROXY_HANDLER.equals(propName)) {
                            startNeeded = true;
                            break;
                        }
                    }
                }
            }

            if (!startNeeded) {
                List<VirtualServer> hosts = httpService.getVirtualServer();
                if (hosts != null) {
                    for (VirtualServer host : hosts) {
                        if (ConfigBeansUtilities.toBoolean(
                                    host.getAccessLoggingEnabled()) ||
                                ConfigBeansUtilities.toBoolean(
                                    host.getSsoEnabled())) {
                            startNeeded = true;
                        }
                    }
                }
            }

            if (startNeeded) {
                startWebContainer();
            }
        }
    }

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server,
        // hence SERVER
        return Lifecycle.SERVER;
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        // TODO
        return null;
    }

    /**
     * Starts the web container
     */
    private void startWebContainer() {
        WebSniffer sniffer = new WebSniffer();
        if (containerRegistry.getContainer(
                    sniffer.getContainersNames()[0]) != null) {
            containerRegistry.getContainer(
                    sniffer.getContainersNames()[0]).getContainer();
        } else {
            Module snifferModule = modulesRegistry.find(sniffer.getClass());
            try {
                Collection<EngineInfo> containersInfo =
                    containerStarter.startContainer(sniffer, snifferModule);
                if (containersInfo != null && !containersInfo.isEmpty()) {
                    // Start each container
                    for (EngineInfo info : containersInfo) {
                        info.getContainer();
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("Done with starting " +
                                sniffer.getModuleType() + " container");
                        }
                    }
                } else {
                    logger.severe(
                        "Unable to start container (no exception provided)");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to start container " +
                    sniffer.getContainersNames()[0], e);
            }
        }
    }
}
