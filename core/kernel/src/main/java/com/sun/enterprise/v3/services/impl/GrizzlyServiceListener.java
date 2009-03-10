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

import com.sun.grizzly.Controller;
import com.sun.grizzly.ControllerStateListener;
import com.sun.enterprise.util.Result;

import java.io.IOException;
import java.net.InetAddress;

/**
 * <p>The GrizzlyServiceListener is responsible of mapping incoming requests
 * to the proper Container or Grizzly extensions. Registered Containers can be
 * notified by Grizzly using three mode:</p>
 * <ul><li>At the transport level: Containers can be notified when TCP, TLS or UDP
 *                                 requests are mapped to them.</li>
 * <li>At the protocol level: Containers can be notified when protocols
 *                            (ex: SIP, HTTP) requests are mapped to them.</li>
 * </li>At the requests level: Containers can be notified when specific patterns
 *                             requests are mapped to them.</li><ul>
 *
 * @author Jeanfrancois Arcand
 */
public class GrizzlyServiceListener {
    private Controller controller;
    
    private int port;
    private InetAddress address;    
    private GrizzlyService grizzlyService;
    private boolean isEmbeddedHttpSecured;
    private GrizzlyEmbeddedHttp embeddedHttp;
    
    private String name;
    
    public GrizzlyServiceListener() {
    }
    
    public GrizzlyServiceListener(GrizzlyService grizzlyService) {
        this.grizzlyService = grizzlyService; 
        this.controller = grizzlyService.getController();
    }

    public void start(final GrizzlyProxy.GrizzlyFuture future) throws IOException, InstantiationException {
        final Thread t = Thread.currentThread();
        embeddedHttp.initEndpoint();
        embeddedHttp.getController().addStateListener(new ControllerStateListener() {
            public void onStarted() {
            }

            public void onReady() {
                future.setResult(new Result<Thread>(t));   
            }

            public void onStopped() {

            }

            public void onException(Throwable throwable) {
                future.setResult(new Result<Thread>(throwable));
            }
        });
        embeddedHttp.startEndpoint();
    }
    
    public void stop() {
        embeddedHttp.stopEndpoint();
    }
    
    public void initializeEmbeddedHttp(boolean isSecured) {
        this.isEmbeddedHttpSecured = isSecured;
        if (isSecured) {
            embeddedHttp = new GrizzlyEmbeddedHttps(grizzlyService);
        } else {
            embeddedHttp = new GrizzlyEmbeddedHttp(grizzlyService);
        }
        
        embeddedHttp.setPort(port);
        embeddedHttp.setAddress(address);
    }
    
    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public GrizzlyEmbeddedHttp getEmbeddedHttp() {
        return embeddedHttp;
    }

    public boolean isEmbeddedHttpSecured() {
        return isEmbeddedHttpSecured;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        
        if (embeddedHttp != null) {
            embeddedHttp.setPort(port);
        }
    }

    public void setAddress(InetAddress address) {
        this.address = address;
        
        if (embeddedHttp != null) {
            embeddedHttp.setAddress(address);
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
