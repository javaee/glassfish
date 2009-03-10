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

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.Inject;
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
    @Inject
    public HttpService httpService;

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
                    logger.log(Level.FINE, "HttpService config changed " + type
                            + " " + tClass + " " + t);
                }

                if (t instanceof HttpListener) {
                    HttpListener listener = (HttpListener) t;
                    int listenerPort = -1;
                        try {
                            listenerPort = Integer.parseInt(
                                    listener.getPort());
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING,
                                    "Can not parse http-listener port number: " +
                                    listener.getPort());
                        }

                    if (type == TYPE.ADD) {
                        grizzlyService.createNetworkProxy(listener, httpService);
                        grizzlyService.registerNetworkProxy(listenerPort);
                    } else if (type == TYPE.REMOVE) {
                        grizzlyService.removeNetworkProxy(listenerPort);
                    } else if (type == TYPE.CHANGE) {
                        // Restart GrizzlyProxy on the port
                        // Port number or id could be changed - so try to find
                        // corresponding proxy both ways
                        boolean isRemovedOld =
                                grizzlyService.removeNetworkProxy(listenerPort) ||
                                grizzlyService.removeNetworkProxy(listener.getId());

                        grizzlyService.createNetworkProxy(listener,
                                httpService);
                        grizzlyService.registerNetworkProxy(listenerPort);
                    }
                    return null;
                }

                return null;
            }
        }, logger);
        return unp;
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