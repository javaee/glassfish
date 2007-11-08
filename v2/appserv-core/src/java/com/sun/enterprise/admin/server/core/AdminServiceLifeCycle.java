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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.server.core;

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleImpl;
import com.sun.appserv.server.ServerLifecycleException;

import com.sun.enterprise.server.ServerContext;


/**
 * Lifecycle manager for admin service.
 */
public class AdminServiceLifeCycle extends ServerLifecycleImpl {

    /**
     * Constant denoting the status of admin service not started
     */
    public static final byte STATUS_NOT_STARTED = 0;

    /**
     * Constant denoting the status of admin service shutdown started
     */
    public static final byte STATUS_SHUTDOWN = 1;

    /**
     * Constant denoting the status of admin service initialized
     */
    public static final byte STATUS_INITED = 2;

    /**
     * Constant denoting the status of admin service started
     */
    public static final byte STATUS_STARTED = 4;

    /**
     * Constant denoting the status of admin service ready
     */
    public static final byte STATUS_READY = 8;

    /**
     * Constant denoting the status of admin service terminated
     */
    public static final byte STATUS_TERMINATED = 0;

    private byte serverStatus;

    /**
     * Default constructor
     */
    public AdminServiceLifeCycle() {
        serverStatus = STATUS_NOT_STARTED;
    }

    /**
     * Server is initializing admin server and setting up the runtime environment.
     * Prepare for the beginning of active use of the public methods of admin
     * server. This method is called before any of the public methods of 
     * admin server are utilized.  
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception IllegalStateException if this subsystem has already been
     *  started
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onInitialization(ServerContext sc)
            throws ServerLifecycleException {
        if ((serverStatus & STATUS_INITED) == STATUS_INITED) {
            throw new IllegalStateException("Admin Server already initialized");
        }
        AdminService srv = AdminService.createAdminService(sc);
        srv.init();
        serverStatus = STATUS_INITED;
    }

    /**
     * Server is starting up applications
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onStartup(ServerContext sc) 
            throws ServerLifecycleException {
        AdminService.getAdminService().start();
        serverStatus |= STATUS_STARTED;
    }

    /**
     * Server has completed loading the services and is ready to serve requests.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onReady(ServerContext sc) throws ServerLifecycleException {
        AdminService.getAdminService().ready();
        serverStatus |= STATUS_READY;
    }

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onShutdown() throws ServerLifecycleException {
        serverStatus |= STATUS_SHUTDOWN;
        AdminService.getAdminService().stop();
    }

    /**
     * Server is terminating the subsystems and the runtime environment.
     * Gracefully terminate the active use of the public methods of this
     * subsystem.  This method should be the last one called on a given
     * instance of this subsystem.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onTermination() throws ServerLifecycleException {
        AdminService.getAdminService().destroy();
        serverStatus = STATUS_TERMINATED;
    }

}
