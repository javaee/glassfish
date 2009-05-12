/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.services.impl;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;

import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.Http;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.Ssl;
import com.sun.grizzly.config.dom.ThreadPool;
import com.sun.grizzly.config.dom.Transport;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Grizzly dynamic configuration handler
 *
 * @author Alexey Stashok
 */
public class DynamicConfigListener implements ConfigListener {
    private GrizzlyService grizzlyService;

    private Logger logger;

    public DynamicConfigListener() {
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        final UnprocessedChangeEvents unp = ConfigSupport.sortAndDispatch(
            events, new Changed() {
                public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type,
                    Class<T> tClass, T t) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "NetworkConfig changed " + type
                            + " " + tClass + " " + t);
                    }
                    if (t instanceof NetworkListener) {
                        return processNetworkListener(type, (NetworkListener) t);
                    } else if (t instanceof Http) {
                        return processProtocol(type, (Protocol) ((Http) t).getParent());
                    } else if (t instanceof Ssl) {
                        return processProtocol(type, (Protocol) ((Ssl) t).getParent());
                    } else if (t instanceof Protocol) {
                        return processProtocol(type, (Protocol) t);
                    } else if (t instanceof ThreadPool) {
                        ThreadPool pool = (ThreadPool) t;
                        NotProcessed notProcessed = null;
                        for (NetworkListener listener : (Collection<NetworkListener>) pool.findNetworkListeners()) {
                            notProcessed = processNetworkListener(type, listener);
                        }
                        return notProcessed;
                    } else if (t instanceof Transport) {
                        Transport transport = (Transport) t;
                        NotProcessed notProcessed = null;
                        for (NetworkListener listener : (Collection<NetworkListener>) transport.findNetworkListeners()) {
                            notProcessed = processNetworkListener(type, listener);
                        }
                        return notProcessed;
                    }
                    return null;
                }
            }, logger);
        return unp;
    }

    private <T extends ConfigBeanProxy> NotProcessed processNetworkListener(Changed.TYPE type,
        NetworkListener listener) {
        if (!"admin-listener".equals(listener.getName())) {
            int listenerPort = -1;
            try {
                listenerPort = Integer.parseInt(listener.getPort());
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Can not parse network-listener port number: " + listener.getPort());
            }
            if (type == Changed.TYPE.ADD) {
                grizzlyService.createNetworkProxy(listener);
                grizzlyService.registerNetworkProxy(listenerPort);
            } else if (type == Changed.TYPE.REMOVE) {
                grizzlyService.removeNetworkProxy(listenerPort);
            } else if (type == Changed.TYPE.CHANGE) {
                // Restart GrizzlyProxy on the port
                // Port number or id could be changed - so try to find
                // corresponding proxy both ways
                boolean isRemovedOld =
                    grizzlyService.removeNetworkProxy(listenerPort) ||
                        grizzlyService.removeNetworkProxy(listener.getName());
                grizzlyService.createNetworkProxy(listener);
                grizzlyService.registerNetworkProxy(listenerPort);
            }
        }
        return null;
    }

    private NotProcessed processProtocol(Changed.TYPE type, Protocol protocol) {
        NotProcessed notProcessed = null;
        for (NetworkListener listener : (Collection<NetworkListener>) protocol.findNetworkListeners()) {
            notProcessed = processNetworkListener(type, listener);
        }
        return notProcessed;
    }

    public void setGrizzlyService(GrizzlyService grizzlyService) {
        this.grizzlyService = grizzlyService;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
