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

/*
 * @(#) CustomResourceDeployer.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.resource;

import com.sun.enterprise.server.ResourceDeployer;
import com.sun.enterprise.config.serverbeans.Resources;

import com.sun.enterprise.Switch;
import com.sun.enterprise.NamingManager;
import com.sun.enterprise.repository.J2EEResource;
import com.sun.enterprise.repository.CustomResource;
import com.sun.enterprise.repository.IASJ2EEResourceFactoryImpl;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Handles custom resource events in the server instance. 
 *
 * The custom resource events from the admin instance are propagated 
 * to this object.
 *
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * <P> 
 * Note: Since a notification is not sent to the user of the custom 
 *       resources upon undeploy, it is possible that there would be 
 *       stale objects not being garbage collected. Future versions
 *       should take care of this problem.
 *
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class CustomResourceDeployer implements ResourceDeployer {

    /** Stringmanager for this deployer */
    private static final StringManager localStrings =
        StringManager.getManager("com.sun.enterprise.resource");

    /** logger for this deployer */
    private static Logger _logger=LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /**
     * Deploy the resource into the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void deployResource(Object resource) throws Exception {
        
        com.sun.enterprise.config.serverbeans.CustomResource customRes = 
            (com.sun.enterprise.config.serverbeans.CustomResource) resource;
        
        if (customRes.isEnabled()) {
            // converts the config data to j2ee resource
            J2EEResource j2eeResource = 
                IASJ2EEResourceFactoryImpl.toCustomJ2EEResource(customRes);

            // resource installer
            ResourceInstaller installer = 
                Switch.getSwitch().getResourceInstaller();

            // installs the resource
            installer.installCustomResource((CustomResource) j2eeResource);

            // adds the resource to the resource collection
            installer.addResource(j2eeResource);
        } else {
            _logger.log(Level.INFO, "core.resource_disabled", 
                new Object[] {customRes.getJndiName(), 
                              IASJ2EEResourceFactoryImpl.CUSTOM_RES_TYPE});
        }
    }

    /**
     * Undeploy the resource from the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void undeployResource(Object resource) 
            throws Exception {

        // naming manager - provides jndi support
        NamingManager namingMgr = Switch.getSwitch().getNamingManager();

        com.sun.enterprise.config.serverbeans.CustomResource customRes = 
            (com.sun.enterprise.config.serverbeans.CustomResource) resource;

        // converts the config data to j2ee resource
        J2EEResource j2eeResource = 
            IASJ2EEResourceFactoryImpl.toCustomJ2EEResource(customRes);

        // removes the resource from jndi naming
        namingMgr.unpublishObject( j2eeResource.getName() );

        // resource installer
        ResourceInstaller installer = Switch.getSwitch().getResourceInstaller();

        // removes the resource from the collection
        installer.removeResource(j2eeResource); 
    }

    /**
     * Redeploy the resource into the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void redeployResource(Object resource)
            throws Exception {

        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * Enable the resource in the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    /**
     * Disable the resource in the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }


	/**
	 * Utility method to find a resource from Resources beans and converte
	 * it to a resource object to be used by the implemented ResourceDeployer
	 *
 	 * @param     name      resource name (normally the jndi-name)
	 * @param     rbeans    Resources config-beans  
	 * @exception Exception thrown if fail
	 */
    public Object getResource(String name, Resources rbeans) throws Exception {

        Object res = rbeans.getCustomResourceByJndiName(name);

        if (res == null) {
            String msg = localStrings.getString(
                         "resource.no_resource",name);
            throw new Exception(msg);
        }

        return res;
    }
}
