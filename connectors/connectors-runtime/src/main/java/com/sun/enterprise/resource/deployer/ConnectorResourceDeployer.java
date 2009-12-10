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


package com.sun.enterprise.resource.deployer;

import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;

/**
 * @author Srikanth P
 */

@Service
@Scoped(Singleton.class)
public class ConnectorResourceDeployer implements ResourceDeployer {

    @Inject
    private ConnectorRuntime runtime;
    private static Logger _logger = LogDomains.getLogger(ConnectorResourceDeployer.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    public void deployResource(Object resource) throws Exception {
        //deployResource is not synchronized as there is only one caller
        //ResourceProxy which is synchronized
        
        ConnectorResource domainResource = (ConnectorResource) resource;
        String jndiName = domainResource.getJndiName();
        String poolName = domainResource.getPoolName();
        _logger.log(Level.FINE, "Calling backend to add connector resource", jndiName);

        runtime.createConnectorResource(jndiName, poolName, null);
        _logger.log(Level.FINE, "Added connector resource in backend", jndiName);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void undeployResource(Object resource)
            throws Exception {
        ConnectorResource domainResource = (ConnectorResource) resource;
        String jndiName = domainResource.getJndiName();
        runtime.deleteConnectorResource(jndiName);

        //Since 8.1 PE/SE/EE - if no more resource-ref to the pool
        //of this resource in this server instance, remove pool from connector
        //runtime
        // TODO V3, we can't destroy the pool as we dont get default call from naming any more.
        // probably, delete the pool and recreate the proxy ?
        // checkAndDeletePool(domainResource);

    }

    /**
     * {@inheritDoc}
     */
    public void redeployResource(Object resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void disableResource(Object resource)
                  throws Exception {
        undeployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(Object resource){
        return resource instanceof ConnectorResource;
    }
}
