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

import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.TCPSelectorHandler;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.http.HttpProtocolChain;
import com.sun.grizzly.http.SelectorThread;
import org.jvnet.hk2.component.Habitat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Implementation of a generic listener for various
 * services started lazily by Grizzly
 *
 * @author Vijay Ramachandran
 */
public class ServiceInitializerThread extends SelectorThread {
    /**
     * The resource bundle containing the message strings for logger.
     */
    protected static final ResourceBundle _rb = logger.getResourceBundle();
    private GrizzlyListener service;
    private Habitat habitat;

    /**
     * Constructor
     *
     * @param grizzlyListener
     */
    public ServiceInitializerThread(GrizzlyListener grizzlyListener, Habitat h) {
        service = grizzlyListener;
        habitat = h;
        setClassLoader(getClass().getClassLoader());
    }

    public Habitat getHabitat() {
        return this.habitat;
    }

    public GrizzlyListener getGrizzlyListener () {
        return this.service;
    }
    
    /**
     * Set a dummy protocol chain and filter. The LWL should never have to come to this place
     * TBD : Do we need this ? Can this be set to null ?
     */
    @Override
    protected void initController() {
        super.initController();
        final DefaultProtocolChainInstanceHandler instanceHandler = new DefaultProtocolChainInstanceHandler() {
            private final ConcurrentLinkedQueue<ProtocolChain> chains =
                    new ConcurrentLinkedQueue<ProtocolChain>();

            /**
             * Always return instance of ProtocolChain.
             */
            @Override
            public ProtocolChain poll() {
                ProtocolChain protocolChain = chains.poll();
                if (protocolChain == null) {
                    protocolChain = new HttpProtocolChain();
                    configureFilters(protocolChain);
                }
                return protocolChain;
            }

            /**
             * Pool an instance of ProtocolChain.
             */
            @Override
            public boolean offer(ProtocolChain instance) {
                return chains.offer(instance);
            }
        };
        controller.setProtocolChainInstanceHandler(instanceHandler);
        controller.setReadThreadsCount(readThreadsCount);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopEndpoint();
            }
        });
    }

    public void configureFilters(ProtocolChain chain) {
        ServiceInitializerFilter readFilter = new ServiceInitializerFilter();
        chain.addFilter(readFilter);
    }
    
    protected TCPSelectorHandler createSelectorHandler() {
        return new ServiceInitializerHandler(this);
    }

    @Override
    public void stopEndpoint() {
        try {
            super.stopEndpoint();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Unable to stop properly", t);
        } finally {
            // Force the Selector(s) to be closed in case an unexpected
            // exception occured during shutdown.
            try {
                if (selectorHandler != null
                        && selectorHandler.getSelector() != null) {
                    selectorHandler.getSelector().close();
                }
            } catch (IOException ex) {
            }
        }

    }

    public void configure(NetworkListener networkListener) {
        setPort(Integer.parseInt(networkListener.getPort()));
        try {
            setAddress(InetAddress.getByName(networkListener.getAddress()));
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, "Invalid address for {0}: {1}",
                    new Object[]{
                            networkListener.getName(),
                            networkListener.getAddress()
                    });
        }
    }
}

