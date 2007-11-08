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
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 *
 * $Id: PersistenceManagerFactoryResourceDeployer.java,v 1.4 2006/11/21 17:22:21 jr158900 Exp $
 */
package com.sun.enterprise.resource;

import com.sun.enterprise.server.ResourceDeployer;
import com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.ConfigBean;

import com.sun.enterprise.Switch;
import com.sun.enterprise.NamingManager;
import com.sun.enterprise.repository.IASJ2EEResourceFactoryImpl;
import com.sun.enterprise.repository.PMFResource;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import javax.naming.InitialContext;

/**
 * Handles PersistenceManagerFactory resource evnets in the server instance.
 * When user adds a pmf resource, the admin instance fires a resource event.
 * And the event is propagated to this object.
 * The methods may be invoked concurrently, therefore synchronized is used.
 *
 * @author Shing Wai Chan
 */

public class PersistenceManagerFactoryResourceDeployer implements ResourceDeployer {

    /** StringManager for this deployer */
    private static final StringManager localStrings =
        StringManager.getManager("com.sun.enterprise.resource");

    /** logger for this deployer */
    private static Logger _logger=LogDomains.getLogger(LogDomains.CORE_LOGGER);

    //---- begin implements ResourceDeployer ----
    
    public synchronized void deployResource(Object resource) throws Exception {
        PersistenceManagerFactoryResource configPMFRes = 
                (PersistenceManagerFactoryResource)resource;

        if (configPMFRes.isEnabled()) {
            // load associated jdbc resource with PMF
            loadJdbcResource(configPMFRes);

            PMFResource j2eeResource = (PMFResource)
                IASJ2EEResourceFactoryImpl.toPMFJ2EEResource(configPMFRes);

            ResourceInstaller installer = 
                Switch.getSwitch().getResourceInstaller();
            installer.installPersistenceManagerResource(j2eeResource);

            installer.addResource(j2eeResource);
        } else {
            _logger.log(Level.INFO, "core.resource_disabled", 
                new Object[] {configPMFRes.getJndiName(), 
                              IASJ2EEResourceFactoryImpl.PMF_RES_TYPE});
        }
    }

    public synchronized void undeployResource(Object resource)
            throws Exception {
        NamingManager namingMgr = Switch.getSwitch().getNamingManager();
        PersistenceManagerFactoryResource configPMFRes = 
                (PersistenceManagerFactoryResource)resource;
        namingMgr.unpublishObject(configPMFRes.getJndiName());

        ResourceInstaller installer = Switch.getSwitch().getResourceInstaller();
        installer.removeResource(
                IASJ2EEResourceFactoryImpl.toPMFJ2EEResource(configPMFRes));
    }

    public synchronized void redeployResource(Object resource)
            throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }

    /**
     * Utility method to find a resource from a Resource bean and convert
     * it to a resource object to be used by ResourceDeployer implementation
     *
     * @param     name      resource name (normally the jndi-name)
     * @param     rbeans    Resources config-beans  
     * @exception Exception thrown if fail
     */
    public Object getResource(String name, Resources rbeans) throws Exception {
        Object res = rbeans.getPersistenceManagerFactoryResourceByJndiName(name);
        if (res == null) {
            String msg = localStrings.getString("resource.no_resource",name);
            throw new Exception(msg);
        }
        return res;
    }
    
    private void loadJdbcResource(PersistenceManagerFactoryResource cr) 
                        throws Exception {

        String resName = cr.getJdbcResourceJndiName();
        Resources resources = (Resources) cr.parent();
        ConfigBean cb = resources.getJdbcResourceByJndiName(resName);
        if (cb != null) {
            try {
                InitialContext ic = new InitialContext();
                ic.lookup(resName);
            } catch (Exception e) {
                // resource is not loaded 
                JdbcResourceDeployer deployer =
                    new JdbcResourceDeployer();
                deployer.deployResource(cb);
            }
        }
    } 

    //---- end implements ResourceDeployer ----
}
