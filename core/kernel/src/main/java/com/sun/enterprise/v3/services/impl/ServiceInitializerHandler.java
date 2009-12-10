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
    private long timeout = 60000;

    public ServiceInitializerHandler(ServiceInitializerThread selectorThread) {
        this.selectorThread = selectorThread;
        initializerImplList = selectorThread.getHabitat().getAllByContract(LazyServiceInitializer.class);
        setSelectTimeout(timeout);
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

