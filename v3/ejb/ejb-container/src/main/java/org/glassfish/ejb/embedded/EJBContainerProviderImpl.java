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

package org.glassfish.ejb.embedded;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;

import com.sun.logging.LogDomains;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedDeployer;
import com.sun.ejb.containers.EjbContainerUtilImpl;

/**
 * GlassFish implementation of the EJBContainerProvider.
 *
 * @author Marina Vatkina
 */
public class EJBContainerProviderImpl implements EJBContainerProvider {

    private static final String GF_PROVIDER_NAME = EJBContainerProviderImpl.class.getName();

    // Use Bundle from another package
    private static final Logger _logger = 
            LogDomains.getLogger(EjbContainerUtilImpl.class, LogDomains.EJB_LOGGER);

    private static final Object lock = new Object();

    private static EJBContainerImpl container;

    public EJBContainerProviderImpl() {}

    public EJBContainer createEJBContainer(Map<?, ?> properties) throws EJBException {
        if (properties == null || properties.get(EJBContainer.PROVIDER) == null || 
                properties.get(EJBContainer.PROVIDER).equals(GF_PROVIDER_NAME)) {

            init();
            if (container.isOpen()) {
                throw new EJBException(_logger.getResourceBundle().getString(
                        "ejb.embedded.exception_exists_container"));
            }

            try {
                Set<File> modules = new HashSet<File>();
                addEJBModules(modules, properties);
                if (modules.isEmpty()) {
                    _logger.log(Level.SEVERE, "No EJB modules found");
                }

                container.deploy(properties, modules);

                return container;
            } catch (Throwable t) {
                // Can't throw an exception - only return null.
                _logger.log(Level.SEVERE, "ejb.embedded.exception_instantiating", t);
            }
        }

        return null;
    }

    private void init() {
        synchronized(lock) {
            if (container == null) {
                Server.Builder builder = new Server.Builder("GFEJBContainerProviderImpl");
                Server server = builder.build();
                EjbBuilder ejb = server.createConfig(EjbBuilder.class);
                EmbeddedEjbContainer ejbContainer = server.addContainer(ejb);
                EmbeddedDeployer deployer = server.getDeployer();

                container = new EJBContainerImpl(ejbContainer, deployer);
            }
        }
    }

    /**
     * Adds EJB modules for the property in the properties Map and in the future
     * from the System classpath
     */
    private void addEJBModules(Set<File> modules, Map<?, ?> properties) {
        Object obj = (properties == null)? null : properties.get(EJBContainer.MODULES);
        Set<File> expected = new HashSet<File>();
        if (obj != null) {
            if (obj instanceof File) {
                expected.add((File)obj);
            } else if (obj instanceof File[]) {
                File[] arr = (File[])obj;
                for (File f : arr) {
                    expected.add(f);
                }
/** TODO: these are module names to be used, not neccessarily file names
            } else if (obj instanceof String) {
                expected.add(new File((String)obj));
            } else if (obj instanceof String[]) {
                String[] arr = (String[])obj;
                for (String s : arr) {
                    expected.add(new File(s));
                }
**/
            }
        }

        Set<File> files = getFromClassPath();
        boolean fromClassPath = true;
        if (files.isEmpty()) {
            files = expected;
            fromClassPath = false;
        }

        for (File f : files) {
            if (f.exists() /** && isEJBModule(f) **/) {
                System.err.println("Found.... " + f.getName());
                if (!fromClassPath || expected.isEmpty() || expected.contains(f)) {
                    modules.add(f);
                    System.err.println("...Added.... ");
                }
            }
        }
    }

    /**
     * TODO
     */
    private Set<File> getFromClassPath() {
        Set<File> result = new HashSet<File>();
        // TODO
        return result;
    }
}
