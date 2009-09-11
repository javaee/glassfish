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

import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;

import java.util.Properties;
import java.util.List;
import java.util.logging.*;

import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;

/**
 * @author Srikanth P
 */

@Service
@Scoped(Singleton.class)
public class AdminObjectResourceDeployer extends GlobalResourceDeployer
        implements ResourceDeployer {

    @Inject
    ConnectorRuntime runtime;

    private static Logger _logger = LogDomains.getLogger(AdminObjectResourceDeployer.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    public synchronized void deployResource(Object resource) throws Exception {

        final AdminObjectResource aor = (AdminObjectResource) resource;

/* TODO V3 handle later MOM

        if (aor.isEnabled()) {
            //registers the jsr77 object for the mail resource deployed
            final ManagementObjectManager mgr =
                getAppServerSwitchObject().getManagementObjectManager();
            mgr.registerAdminObjectResource(aor.getJndiName(),
                aor.getResAdapter(), aor.getResType(),
                getPropNamesAsStrArr(aor.getElementProperty()),
                getPropValuesAsStrArr(aor.getElementProperty()));
        } else {
                _logger.log(Level.INFO, "core.resource_disabled",
                        new Object[] {aor.getJndiName(),
                        IASJ2EEResourceFactoryImpl.JMS_RES_TYPE});
        }
*/

        _logger.log(Level.FINE,
                "Calling backend to add adminObject", aor.getJndiName());
        runtime.addAdminObject(null, aor.getResAdapter(), aor.getJndiName(),
                aor.getResType(), aor.getClassName(),  transformProps(aor.getProperty()));
        _logger.log(Level.FINE,
                "Added adminObject in backend", aor.getJndiName());
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void undeployResource(Object resource)
            throws Exception {

        final AdminObjectResource aor = (AdminObjectResource) resource;

        _logger.log(Level.FINE,
                "Calling backend to delete adminObject", aor.getJndiName());
        runtime.deleteAdminObject(aor.getJndiName());
        _logger.log(Level.FINE,
                "Deleted adminObject in backend", aor.getJndiName());

        //unregister the managed object
/* TODO V3 handle once MOM is available ?
        final ManagementObjectManager mgr =
                getAppServerSwitchObject().getManagementObjectManager();
        mgr.unregisterAdminObjectResource(aor.getJndiName(), aor.getResType());
*/

    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(Object resource) {
        return resource instanceof AdminObjectResource;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void redeployResource(Object resource)
            throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void disableResource(Object resource)
            throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void enableResource(Object resource)
            throws Exception {
    }

    private Properties transformProps(List<Property> domainProps) {

        Properties props = new Properties();
        for (Property domainProp : domainProps) {
            props.setProperty(domainProp.getName(), domainProp.getValue());
        }
        return props;
    }
}
