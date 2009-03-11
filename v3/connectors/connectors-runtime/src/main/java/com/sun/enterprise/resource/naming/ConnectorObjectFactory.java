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
package com.sun.enterprise.resource.naming;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectionManagerImpl;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.logging.LogDomains;
import org.glassfish.api.naming.NamingObjectProxy;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ConfigurationException;
import javax.naming.InitialContext;
import javax.resource.spi.ManagedConnectionFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An object factory to handle creation of Connection Factories
 *
 * @author Tony Ng
 */
public class ConnectorObjectFactory implements NamingObjectProxy {

    private ConnectorRuntime runtime;
    private String jndiName;
    private String poolName;
    private String moduleName;
    private String connectionFactoryName;

    private static Logger _logger = LogDomains.getLogger(ConnectorObjectFactory.class, LogDomains.JNDI_LOGGER);

    public ConnectorObjectFactory(String jndiName, String connectionFactoryName, String moduleName, String poolName) {
        this.jndiName = jndiName;
        this.moduleName = moduleName;
        this.poolName = poolName;
        this.connectionFactoryName = connectionFactoryName;
        runtime = ConnectorRuntime.getRuntime();
    }

    /**
     * Tells if the result of create() is cacheable. If so
     * the naming manager will replace this object factory with
     * the object itself.
     *
     * @return true if the result of create() can be cached
     */
    public boolean isCreateResultCacheable() {
        return false;
    }

    /**
     * Create ad return an object.
     *
     * @return an object
     */
    public Object create(Context ic) throws NamingException {

        /* TODO V3 handle client later
        if (runtime.getEnviron() == ConnectorRuntime.CLIENT) {
            ConnectorDescriptor connectorDescriptor = null;

                String descriptorJNDIName = ConnectorAdminServiceUtils.
                        getReservePrefixedJNDINameForDescriptor(moduleName);
                connectorDescriptor = (ConnectorDescriptor) ic.lookup(descriptorJNDIName);
            try {
                runtime.createActiveResourceAdapter(connectorDescriptor,  moduleName, null);
            } catch (ConnectorRuntimeException e) {
                _logger.log(Level.FINE, "Failed to look up ConnectorDescriptor from JNDI", moduleName);
                NamingException ne = new NamingException("Failed to look up ConnectorDescriptor from JNDI");
                ne.setRootCause(e);
                throw ne;
            }
        }
        */

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (runtime.checkAccessibility(moduleName, loader) == false) {
            throw new NamingException(
                    "Only the application that has the embedded resource" +
                            "adapter can access the resource adapter");
        }

        Object cf = null;
        try {
            ManagedConnectionFactory mcf = getRuntime().obtainManagedConnectionFactory(poolName);
            if (mcf == null) {
                _logger.log(Level.FINE, "Failed to create MCF ", poolName);
                throw new ConnectorRuntimeException("Failed to create MCF");
            }

            boolean forceNoLazyAssoc = false;

            if (jndiName.endsWith(ConnectorConstants.PM_JNDI_SUFFIX)) {
                forceNoLazyAssoc = true;
            }

            String derivedJndiName = ConnectorsUtil.deriveJndiName(jndiName, ic.getEnvironment());
            ConnectionManagerImpl mgr = (ConnectionManagerImpl)
                    getRuntime().obtainConnectionManager(poolName, forceNoLazyAssoc);
            mgr.setJndiName(derivedJndiName);
            mgr.setRarName(moduleName);
            mgr.initialize();

            cf = mcf.createConnectionFactory(mgr);
            if (cf == null) {
                /* TODO V3 handle later
                    String msg = localStrings.getLocalString
                        ("no.resource.adapter", "");
                */
                String msg = "No resource adapter found";
                throw new RuntimeException(new ConfigurationException(msg));
            }

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Connection Factory:" + cf);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cf;
    }

    private ConnectorRuntime getRuntime() {
        return runtime;
    }
}
