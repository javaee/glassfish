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
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.services.impl;

import com.sun.grizzly.Context;
import com.sun.grizzly.TCPSelectorHandler;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.SelectorHandler;
import com.sun.grizzly.Controller;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import org.glassfish.internal.grizzly.LazyServiceInitializer;

/**
 * {@link SelectorHandler} implementation {@link SelectorThread} 
 * passes to {@link Controller}. It dumbs down
 * {@link TCPSelectorHandler}, to make it just good enough
 * for the light weight listener
 * 
 * @author Vijay Ramachandran
 */
public class ServiceInitializerHandler extends TCPSelectorHandler {
    private ServiceInitializerThread selectorThread;
    private LazyServiceInitializer targetInitializer = null;
    private Collection<LazyServiceInitializer> initializerImplList = null;
    protected static final Logger logger = Logger.getLogger(ServiceInitializerHandler.class.getName());
    private Object LOCK_OBJ = new Object();

    public ServiceInitializerHandler(ServiceInitializerThread selectorThread) {
        this.selectorThread = selectorThread;
        initializerImplList = selectorThread.getHabitat().getAllByContract(LazyServiceInitializer.class);
    }
    
    public void setSelectorThread(ServiceInitializerThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    @Override
    public boolean onAcceptInterest(SelectionKey key, Context ctx) throws IOException{
        if(initializerImplList == null) {
            logger.severe("NO Lazy Initialiser was found for port = " +
                selectorThread.getGrizzlyListener().getListener().getPort());
            return false;
        }
        SelectableChannel channel = acceptWithoutRegistration(key);
        if(targetInitializer == null) {
            synchronized(LOCK_OBJ) {
                if(targetInitializer == null) {
                    for(LazyServiceInitializer initializer : initializerImplList) {
                        String listenerName = selectorThread.getGrizzlyListener().getListener().getName();
                        if(listenerName.equalsIgnoreCase(initializer.getServiceName())) {
                            targetInitializer = initializer;
                            break;
                        }
                    }
                }
                if(targetInitializer == null) {
                    logger.severe("NO Lazy Initialiser implementation was found for port = " +
                            selectorThread.getGrizzlyListener().getListener().getPort());
                    return false;
                }
                if(!targetInitializer.initializeService()) {
                    targetInitializer = null;
                    logger.severe("Lazy Service initialization failed for port = " +
                            selectorThread.getGrizzlyListener().getListener().getPort());
                    return false;
                }
            }
        }
        if (channel != null) {
            targetInitializer.handleRequest(channel);
        }
        return false;
    }
}

