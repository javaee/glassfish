/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.web.reconfig;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Property; 
import com.sun.enterprise.web.WebContainer;

import org.apache.catalina.LifecycleException;

import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.annotations.Inject;


/**
 * Http service dynamic configuration
 *
 * @author amyroh
 */
public class HttpServiceConfigListener implements ConfigListener {

    @Inject
    public HttpService httpService;
    
    private WebContainer container;
    
    private Logger logger;

    volatile boolean received=false;
        
    /**
     * Set the Web Container for this ConfigListener.
     * Must be set in order to perform dynamic configuration 
     * @param container the container to be set
     */
    public void setContainer(WebContainer container) {
        this.container = container;
    } 
    
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    /**
     * Handles HttpService change events
     * @param events the PropertyChangeEvent
     */
    public synchronized void changed(PropertyChangeEvent[] events) {
        
        ConfigSupport.sortAndDispatch(events, new Changed() {
            public <T extends ConfigBeanProxy> void changed(TYPE type, Class<T> tClass, T t) {
                try {
                    if (t instanceof com.sun.enterprise.config.serverbeans.VirtualServer) {
                        // TODO enable once issue 4583 is resolved
                        /*
                        if (type==TYPE.ADD) {                           
                            container.createVirtualServer(
                            (com.sun.enterprise.config.serverbeans.VirtualServer)t,
                             httpService, null);
                        } else if (type==TYPE.REMOVE) {
                            container.deleteHost(httpService);
                        } else if (type==TYPE.CHANGE) {
                            container.updateHost(
                                    (com.sun.enterprise.config.serverbeans.VirtualServer)t, 
                                    httpService);
                        }
                         */
                    } else if (t instanceof HttpListener) {
                        if (type==TYPE.ADD) {
                            container.addConnector((HttpListener)t, httpService);
                        } else if (type==TYPE.REMOVE) {
                            container.deleteConnector((HttpListener)t, httpService);
                        } else if (type==TYPE.CHANGE) {
                            container.updateConnector((HttpListener)t, httpService);
                        }
                    }
                } catch (LifecycleException le) {
                    logger.log(Level.SEVERE, "Exception processing HttpService config change", le);
                }    
            }
        }
        , logger);
       
    }
    
}
