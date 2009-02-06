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


import com.sun.appserv.connectors.internal.api.ConnectorsUtil;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.InitialContextFactory;


/**
 * A proxy object factory for an external JNDI factory
 */
public class JndiProxyObjectFactory implements ObjectFactory {

    // for every external-jndi-resource there is an InitialContext
    // created from the factory and environment properties
    private static Hashtable contextMap = new Hashtable();

    public static Context removeInitialContext(String jndiName) {
        return (Context) contextMap.remove(jndiName);
    }

    /**
     * load the context factory
     */
    private Context loadInitialContext(String factoryClass, Hashtable env) {
	Object factory = ConnectorsUtil.loadObject(factoryClass);
        if (factory == null) {
        	System.err.println("Cannot load external-jndi-resource " +
                                   "factory-class '" + factoryClass + "'");
                return null;
        } else if (! (factory instanceof
                            javax.naming.spi.InitialContextFactory)) {

                System.err.println("external-jndi-resource factory-class '"
                                  + factoryClass + "' must be of type "
                                  + "javax.naming.spi.InitialContextFactory");
                return null;
        }

        Context context = null;
        try {
        	context = ((InitialContextFactory)factory).getInitialContext(env);
        } catch (NamingException ne) {
          	System.err.println("Exception thrown creating initial context " +
                                   "for external JNDI factory '" +
                                   factoryClass + "' " + ne.getMessage());
        }

	return context;
    }

    /**
    * create the object instance from the factory
    */
    public Object getObjectInstance(Object obj,
                    Name name, Context nameCtx, Hashtable environment)
                    throws NamingException {

        // name to lookup in the external factory
        String jndiLookupName = "";
        String jndiFactoryClass = null;
 	    String bindName = null;

        // get the target initial naming context and the lookup name
        Reference ref = (Reference) obj;
        Enumeration addrs = ref.getAll();
        while (addrs.hasMoreElements()) {
            RefAddr addr = (RefAddr) addrs.nextElement();

            String prop = addr.getType();
            if (prop.equals("jndiName")) {
                bindName = (String)addr.getContent();
            }
            else if (prop.equals("jndiLookupName")) {
                jndiLookupName = (String) addr.getContent();
            }
            else if (prop.equals("jndiFactoryClass")) {
                jndiFactoryClass = (String) addr.getContent();
            }
        }

        if (bindName == null) {
		    throw new NamingException("JndiProxyObjectFactory: no bindName context info");
	    }

	    com.sun.enterprise.resource.naming.ProxyRefAddr contextAddr =
                (com.sun.enterprise.resource.naming.ProxyRefAddr)ref.get(bindName);
	    Hashtable env = null;
	    if (contextAddr == null ||
            jndiFactoryClass == null ||
	        (env = (Hashtable)(contextAddr.getContent())) == null) {
		    throw new NamingException("JndiProxyObjectFactory: no info in the " +
                    "reference about the target context; contextAddr = " + contextAddr + " " +
                    "env = " + env + " factoryClass = " + jndiFactoryClass);
	}

        // Context of the external naming factory
        Context context = (Context)contextMap.get(bindName);
        if (context == null) {
            synchronized (contextMap) {
                context = (Context)contextMap.get(bindName);
                if (context == null) {
                    context = loadInitialContext(jndiFactoryClass, env);
                    contextMap.put(bindName, context);
                }
            }
        }

        if (context == null)
            throw new NamingException("JndiProxyObjectFactory no InitialContext" + jndiFactoryClass);

        // use the name to lookup in the external JNDI naming context
        try {
            return context.lookup(jndiLookupName);
        } catch (NameNotFoundException e) {
            throw new ExternalNameNotFoundException(e);
        }
    }
}
