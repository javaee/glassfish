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

import com.sun.enterprise.v3.services.impl.monitor.GrizzlyMonitoring;
import com.sun.enterprise.v3.services.impl.monitor.MonitorableServiceListener;
import com.sun.grizzly.Controller;
import com.sun.grizzly.config.dom.NetworkListener;
import org.jvnet.hk2.component.Habitat;

import java.io.IOException;

/**
 * This class extends Grizzly's GrizzlyServiceListener class to customize it for GlassFish
 * and enable a single listener do both lazy service initialization as well as init of HTTP
 * and admin listeners
 * @author Vijay Ramachandran
 */
public class GrizzlyListener extends MonitorableServiceListener {
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
    public void configure(NetworkListener networkListener, Habitat habitat) {
        this.listener = networkListener;
        if("light-weight-listener".equals(networkListener.getProtocol())) {
            isGenericListener = true;
        }

        if(!isGenericListener) {
            super.configure(networkListener, habitat);
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

    @Override
    public void start() throws IOException, InstantiationException {
        if(isGenericListener) {
            serviceInitializer.initController();
            serviceInitializer.startEndpoint();
        } else {
            getEmbeddedHttp().initEndpoint();
            getEmbeddedHttp().startEndpoint();
        }
    }

    @Override
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

