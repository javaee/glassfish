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
package com.sun.enterprise.ee.synchronization.api;

import com.sun.enterprise.ee.synchronization.SynchronizationException;

/**
 * API for Application or Module synchronization. The APIs can be called
 * in a server instance to download bits from central repository. This 
 * API is intended for use during hot deployment - new deployment of 
 * application or module, redeployment of applications or module. The API
 * keeps record of internal states and will optimize synchronization
 * during re-deployment.
 *
 * <p> The API does not deal with file locking issues, i.e., if the 
 * bits are in use by runtime. The caller of this API must ensure that
 * the application is not running before calling this API.
 * 
 * <xmp>
 * Example: The following code snippet synchronizes bits for a J2EE application.
 *
 *   // new config context associated with the event
 *   ConfigContext configContext = event.getConfigContext();
 *
 *   // creates a synchronization context
 *   SynchronizationContext synchCtx = 
 *       SynchronizationFactory.createSynchronizationContext(configContext);
 *
 *   // applications synchronization manager
 *   ApplicationsMgr appSynchMgr = synchCtx.getApplicationsMgr();
 *
 *   // synchronizes an application
 *   appSynchMgr.synchronize(appName); 
 *
 * </xmp>
 *
 * @author Nazrul Islam
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public interface ApplicationsMgr {

    /**
     * Helper method. It synchronizes the given application or module.
     * This method finds the application or module in the configuration
     * and delegates to the correct method.
     *
     * @param  name  name of an application or module
     *
     * @throws  SynchronizationException   if synchronization fails
     */
    public void synchronize(String name) throws SynchronizationException;

    /**
     * Synchronizes a J2EE application from central repository.
     *
     * @param  name  name of the J2EE application
     *
     * @throws  SynchronizationException   if synchronization fails
     */
    public void synchronizeJ2EEApplication(String name) 
            throws SynchronizationException;

    /**
     * Synchronizes an appclient module from the central repository.
     *
     * @param  name  name of the appclient module
     *
     * @throws  SynchronizationException   if synchronization fails
     */
    public void synchronizeAppclientModule(String name) 
            throws SynchronizationException;

    /**
     * Synchronizes an EJB module from the central repository. 
     *
     * @param  name  name of the ejb module
     *
     * @throws  SynchronizationException   if synchronization fails
     */
    public void synchronizeEJBModule(String name) 
            throws SynchronizationException;

    /**
     * Synchronizes a lifecycle module from the central repository.
     *
     * @param  name  name of the lifecycle module
     *
     * @throws  SynchronizationException   if synchronization fails
     */
    public void synchronizeLifecycleModule(String name) 
            throws SynchronizationException;

    /**
     * Synchronizes a web module from the central repository.
     *
     * @param  name  name of the web module
     *
     * @throws  SynchronizationException   if synchronization fails
     */
    public void synchronizeWebModule(String name) 
            throws SynchronizationException;

    /**
     * Synchronizes a connector module from the central repository.
     *
     * @param  name  name of the connector module
     *
     * @throws  SynchronizationException   if synchronization fails
     */
    public void synchronizeConnectorModule(String name) 
            throws SynchronizationException;
}
