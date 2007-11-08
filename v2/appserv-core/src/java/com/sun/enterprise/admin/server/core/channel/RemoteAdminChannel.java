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
package com.sun.enterprise.admin.server.core.channel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventResult;

/**
 * Remote interface for admin channel
 */
public interface RemoteAdminChannel extends Remote {

    // WARNING - Please make sure that the first parameter to all methods in
    // this interface is byte[] key. This is used in validating access. Not
    // having byte[] key will compromise security. Please see the implementation
    // class (AdminChannelServer) for details on how byte[] key is used.

    /**
     * Send a event notification.
     * @param event the event
     * @param key shared secret
     */
    public AdminEventResult sendNotification(byte[] key, AdminEvent event)
            throws RemoteException;

    /**
     * Ping server.
     */
    public boolean pingServer(byte[] key) throws RemoteException;

    /**
     * Get server status code. This method will return one of the following
     * constants from class <code>com.sun.enterprise.admin.common.Status</code>
     * -- <code>kInstanceStartingCode, kInstanceRunningCode or
     * kInstanceStoppingCode</code> representing starting, running and stopping
     * condition for the instance. 
     * @param key shared secret
     * @returns server status code denoting whether server is starting,
     *     running or stopping.
     */
    public int getServerStatusCode(byte[] key) throws RemoteException;

    /**
     * Is restart needed to use persistent server configuration. After a
     * notification, the server may be in inconsistenet state with respect
     * to persistent configuration because all changes to configuration can
     * not be handled dynamically - A restart is needed in such cases to
     * synchronize server with persistent configuration.
     * @param key shared secret
     * @return true if restart is required, false otherwise.
     */
    public boolean isRestartNeeded(byte[] key) throws RemoteException;

    /**
     * Set restart needed status on server instance.
     * @param key shared secret
     * @param needRestart true if the instance should be restarted to use
     *     changes in persistent configuration.
     */
    public void setRestartNeeded(byte[] key, boolean needRestart)
            throws RemoteException;

    /**
     * Obtain the port number that caused the port conflict. This should
     * be invoked only if Status is kInstaceFailedCode
     * @param key shared secret
     * @return port number.
     */
    public int getConflictedPort(byte[] key) throws RemoteException;

    /**
     *  Trigger server exit now. This is applicable only for 
     *  AS Socket Service based startup now.
     *  @param key shared secret
     */
    public void triggerServerExit(byte[] key) throws RemoteException ;

}
