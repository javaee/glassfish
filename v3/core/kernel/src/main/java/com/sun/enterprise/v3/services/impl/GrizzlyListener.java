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

import com.sun.enterprise.v3.services.impl.monitor.GrizzlyMonitoring;
import com.sun.enterprise.v3.services.impl.monitor.MonitorableServiceListener;
import com.sun.grizzly.Controller;
import com.sun.grizzly.config.dom.NetworkListener;
import org.jvnet.hk2.component.Habitat;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class extends Grizzly's GrizzlyServiceListener class to customize it for GlassFish
 * and enable a single listener do both lazy service initialization as well as init of HTTP
 * and admin listeners
 * @author Vijay Ramachandran
 */
public class GrizzlyListener extends MonitorableServiceListener {
    /**
     * The logger to use for logging messages.
     */
    protected static final Logger logger = Logger.getLogger(GrizzlyListener.class.getName());

    private boolean isGenericListener = false;
    private ServiceInitializerThread serviceInitializer;
    private NetworkListener listener;

    public GrizzlyListener(GrizzlyMonitoring monitoring, Controller controller, String listenerName) {
        super(monitoring, controller, listenerName);
    }

    /*
    * Configures the given grizzlyListener.
    */
    @Override
    public void configure(NetworkListener networkListener, boolean isWebProfile, Habitat habitat) {
        this.listener = networkListener;
        if("light-weight-listener".equals(networkListener.getProtocol())) {
            isGenericListener = true;
        }

        if(!isGenericListener) {
            super.configure(networkListener, isWebProfile, habitat);
        } else {
            initializeListener(networkListener, habitat);
            setName(networkListener.getName());
        }
    }

    private void initializeListener(NetworkListener networkListener, Habitat habitat) {
        serviceInitializer = new ServiceInitializerThread(this, habitat);
        serviceInitializer.setController(this.getController());
        serviceInitializer.configure(networkListener);
    }

    public NetworkListener getListener() {
        return this.listener;
    }

    public void start() throws IOException, InstantiationException {
        if(isGenericListener) {
            serviceInitializer.initController();
            serviceInitializer.startEndpoint();
        } else {
            getEmbeddedHttp().initEndpoint();
            getEmbeddedHttp().startEndpoint();
        }
    }

    public void stop() {
        if(isGenericListener) {
            serviceInitializer.stopEndpoint();
        } else {
            getEmbeddedHttp().stopEndpoint();
        }
    }

    public void initEndpoint() throws IOException, InstantiationException {
        if(isGenericListener) {
            serviceInitializer.initEndpoint();
        } else {
            getEmbeddedHttp().initEndpoint();
        }
    }

    @Override
    public Controller getController() {
        if(isGenericListener) {
            return serviceInitializer.getController();
        } else {
            return getEmbeddedHttp().getController();
        }
    }

    public void startEndpoint() throws IOException, InstantiationException {
        if(isGenericListener) {
            serviceInitializer.startEndpoint();
        } else {
            getEmbeddedHttp().startEndpoint();
        }        
    }

    public boolean isGenericListener() {
        return isGenericListener;
    }

    @Override
    public int getPort() {
        if(isGenericListener) {
            return serviceInitializer.getPort();
        } else {
            return getEmbeddedHttp().getPort();
        }
    }
}

