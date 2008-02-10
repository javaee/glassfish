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
package com.sun.enterprise.naming.factory;

import java.util.*;
import javax.naming.*;
import javax.naming.spi.*;

import java.util.Hashtable;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import com.sun.enterprise.*;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.connectors.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;

import java.util.logging.*;
import com.sun.logging.*;

/**
 * An object factory to handle creation of Connection Factories
 * 
 * @author Tony Ng
 *
 */
public class ConnectorObjectFactory implements ObjectFactory {

    static Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);

    private static LocalStringManagerImpl localStrings = 
    new LocalStringManagerImpl(ConnectorObjectFactory.class);
    private ConnectorRuntime runtime = ConnectorRuntime.getRuntime();

    public ConnectorObjectFactory() {
    }

    public Object getObjectInstance(Object obj, 
				    Name name, 
				    Context nameCtx,
				    Hashtable env) throws Exception 
    {
	Reference ref = (Reference) obj;
	if(_logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.FINE,"ConnectorObjectFactory: " + ref +
			" Name:" + name);
	}
        String poolName = (String) ref.get(0).getContent();
        String moduleName  = (String) ref.get(1).getContent();

        Switch sw = Switch.getSwitch();

        if(runtime.getEnviron() == ConnectorRuntime.CLIENT) {
            ConnectorDescriptor connectorDescriptor = null; 
            try {	     
    	        Context ic = new InitialContext();        		
                String descriptorJNDIName = ConnectorAdminServiceUtils.
                    getReservePrefixedJNDINameForDescriptor(moduleName);
        		connectorDescriptor = (ConnectorDescriptor)ic.lookup(descriptorJNDIName); 
            }
            catch(NamingException ne) {
                _logger.log(Level.FINE,
			    "Failed to look up ConnectorDescriptor from JNDI", 
			    moduleName); 
                throw new ConnectorRuntimeException(
			    "Failed to look up ConnectorDescriptor from JNDI");
            }
            runtime.createActiveResourceAdapter(connectorDescriptor,
						moduleName,null);
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (runtime.checkAccessibility(moduleName, loader) == false) {
	    throw new NamingException(
		      "Only the application that has the embedded resource" + 
		      "adapter can access the resource adapter");
	}
	

        ManagedConnectionFactory mcf = runtime.obtainManagedConnectionFactory(poolName);
        if(mcf == null) {
                _logger.log(Level.FINE,"Failed to create MCF ",poolName);
                throw new ConnectorRuntimeException("Failed to create MCF");
        }

        String jndiName = name.toString();
        boolean forceNoLazyAssoc = false;
        if ( jndiName.endsWith( ConnectorConstants.PM_JNDI_SUFFIX ) ) {
            forceNoLazyAssoc = true;
        }
        ConnectionManagerImpl mgr = (ConnectionManagerImpl) 
            runtime.obtainConnectionManager(poolName, forceNoLazyAssoc);
        mgr.setJndiName(deriveJndiName(jndiName, env));
        mgr.setRarName( moduleName );
        mgr.initialize();

        Object cf = mcf.createConnectionFactory(mgr);
        if (cf == null) {
            String msg = localStrings.getLocalString
                ("no.resource.adapter", "");
            throw new ConfigurationException(msg);
        }
	if(_logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.FINE,"Connection Factory:" + cf);
	}

	return cf;
    }

    private String deriveJndiName(String name, Hashtable env) {
        String suffix = (String) env.get(ConnectorConstants.JNDI_SUFFIX_PROPERTY);
        if (runtime.isValidJndiSuffix(suffix)) {
            _logger.log(Level.FINE, "JNDI name will be suffixed with :" + suffix);
            return name + suffix;
        }
        return name;
    }

}
