/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.server.core;

import java.util.*;

import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleImpl;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;

import com.sun.enterprise.server.ServerContext;

import java.util.logging.*;
import com.sun.logging.LogDomains;


/**
 * Lifecycle that manages the RMI server that communicates with clients.
 */
public class AdminChannelLifecycle extends ServerLifecycleImpl {

    private static final int NOTINITIALIZED = 0;
    private static final int INITIALIZED = 1;
    private static final int READY       = 2;
    private static final int SHUTDOWN    = 3;

    private static int status = NOTINITIALIZED;

    /**
     * Server is Inililize the AdminChannel.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onInitialization(ServerContext context)
        throws ServerLifecycleException {
        if (status < INITIALIZED) {
            status = INITIALIZED;
            AdminChannel.createRMIChannel();
            AdminChannel.createSharedSecret();
        }
    }

    /**
     * Server is ready.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onReady(ServerContext context)
        throws ServerLifecycleException {
        if (status < READY) {
            status = READY;
            AdminChannel.setRMIChannelReady();
        }
    }

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onShutdown()
        throws ServerLifecycleException {
        if (status < SHUTDOWN) {
            status = SHUTDOWN;
            AdminChannel.setRMIChannelStopping();
            AdminChannel.destroyRMIChannel();
        }
    }

    /**
     * Server startup has failed. This could be due to a port conflict exception.
     * In case of port conflict exception, port will be >0. Set the RMI channel server 
     * status to kInstanceFailed.
     * @param port portnumber 
     */
    public void onAbort(int port) 
        throws ServerLifecycleException {
        AdminChannel.setRMIChannelAborting(port);
    }

}

