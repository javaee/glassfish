/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import com.sun.hk2.component.ExistingSingletonInhabitant;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.resource.spi.ManagedConnectionFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;

import org.glassfish.api.admin.*;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.component.Habitat;

/**
 * An object factory to handle creation of Connection Factories
 *
 * @author Tony Ng
 */
public class ConnectorObjectFactory implements ObjectFactory {

    private ConnectorRuntime runtime ;

    private static Logger _logger = LogDomains.getLogger(ConnectorObjectFactory.class, LogDomains.JNDI_LOGGER);
    protected final static StringManager localStrings =
            StringManager.getManager(ConnectorRuntime.class);

    public ConnectorObjectFactory() {
    }
    
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable env) throws Exception {

        Reference ref = (Reference) obj;
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"ConnectorObjectFactory: " + ref +
                " Name:" + name);
        }
            String poolName = (String) ref.get(0).getContent();
            String moduleName  = (String) ref.get(1).getContent();


        if (getRuntime().isACCRuntime() || getRuntime().isNonACCRuntime()) {
            ConnectorDescriptor connectorDescriptor = null;

            String descriptorJNDIName = ConnectorAdminServiceUtils.
                    getReservePrefixedJNDINameForDescriptor(moduleName);
            Context ic = new InitialContext(env);
            connectorDescriptor = (ConnectorDescriptor) ic.lookup(descriptorJNDIName);
            try {
                getRuntime().createActiveResourceAdapter(connectorDescriptor, moduleName, null);
            } catch (ConnectorRuntimeException e) {
                _logger.log(Level.FINE, "Failed to look up ConnectorDescriptor from JNDI", moduleName);
                NamingException ne = new NamingException("Failed to look up ConnectorDescriptor from JNDI");
                ne.setRootCause(e);
                throw ne;
            }
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (!getRuntime().checkAccessibility(moduleName, loader)) {
            String msg = localStrings.getString("cof.no_access_to_embedded_rar", moduleName);
            throw new NamingException(msg);
        }

        Object cf = null;
        try {
            ManagedConnectionFactory mcf = getRuntime().obtainManagedConnectionFactory(poolName, env);
            if (mcf == null) {
                _logger.log(Level.FINE, "Failed to create MCF ", poolName);
                throw new ConnectorRuntimeException("Failed to create MCF");
            }

            boolean forceNoLazyAssoc = false;

            String jndiName = name.toString();
            if (jndiName.endsWith(ConnectorConstants.PM_JNDI_SUFFIX)) {
                forceNoLazyAssoc = true;
            }

            String derivedJndiName = ConnectorsUtil.deriveJndiName(jndiName, env);
            ConnectionManagerImpl mgr = (ConnectionManagerImpl)
                    getRuntime().obtainConnectionManager(poolName, forceNoLazyAssoc);
            mgr.setJndiName(derivedJndiName);
            mgr.setRarName(moduleName);

            String logicalName = (String)env.get(GlassfishNamingManager.LOGICAL_NAME);
            if(logicalName != null){
                mgr.setLogicalName(logicalName);
            }
            
            mgr.initialize();

            cf = mcf.createConnectionFactory(mgr);
            if (cf == null) {
                String msg = localStrings.getString("cof.no.resource.adapter");
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
        if (runtime == null) {
            runtime = ConnectorNamingUtils.getRuntime();
        }
        return runtime;
    }
}
