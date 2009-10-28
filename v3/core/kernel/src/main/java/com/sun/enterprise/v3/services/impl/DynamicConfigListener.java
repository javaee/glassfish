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

import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.grizzly.config.dom.FileCache;
import java.util.ArrayList;

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

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {

        final UnprocessedChangeEvents unp = ConfigSupport.sortAndDispatch(
            events, new Changed() {
                @Override
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
                    } else if (t instanceof FileCache) {
                        return processProtocol(type, (Protocol) ((FileCache) t).getParent().getParent());
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
                    } else if (t instanceof VirtualServer && !grizzlyService.hasMapperUpdateListener()){
                        return processVirtualServer(type, (VirtualServer)t);
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

    private NotProcessed processVirtualServer(Changed.TYPE type, VirtualServer vs) {
        NotProcessed notProcessed = null;
        String list = vs.getNetworkListeners();
        Collection<NetworkListener> nls = grizzlyService.getHabitat().getAllByType(NetworkListener.class);
        ArrayList<String> as = GrizzlyProxy.toArray(list,",");

        for (String s: as){
            for(NetworkListener n: nls){
                if (n.getName().equals(s)){
                    notProcessed = processNetworkListener(type, n);
                }
            }
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
