/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.v3.server;

import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;

import java.net.URI;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.jar.Manifest;
import java.io.IOException;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.common_impl.Tokenizer;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class ClassLoaderHierarchyImpl implements ClassLoaderHierarchy {
    @Inject APIClassLoaderServiceImpl apiCLS;

    @Inject CommonClassLoaderServiceImpl commonCLS;

    @Inject ConnectorClassLoaderServiceImpl connectorCLS;

    @Inject AppLibClassLoaderServiceImpl applibCLS;

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    Logger logger;

    @Inject
    Habitat habitat;



    public ClassLoader getAPIClassLoader() {
        return apiCLS.getAPIClassLoader();
    }

    public ClassLoader getCommonClassLoader() {
        return commonCLS.getCommonClassLoader();
    }

    public String getCommonClassPath() {
        return commonCLS.getCommonClassPath();
    }

    public DelegatingClassLoader getConnectorClassLoader(String application) {
        return connectorCLS.getConnectorClassLoader(application);
    }

    public ClassLoader getAppLibClassLoader(String application, List<URI> libURIs) throws MalformedURLException {
        return applibCLS.getAppLibClassLoader(application, libURIs);
    }

    /**
     * Sets up the parent class loader for the application class loader.
     * Application class loader are under the control of the ArchiveHandler since
     * a special archive file format will require a specific class loader.
     *
     * However GlassFish needs to be able to add capabilities to the application
     * like adding APIs accessibility, this is done through its parent class loader
     * which we create and maintain.
     *
     * @param parent the parent class loader
     * @param context deployment context
     * @return class loader capable of loading public APIs identified by the deployers
     * @throws ResolveError if one of the deployer's public API module is not found.
     */
    public ClassLoader createApplicationParentCL(ClassLoader parent, DeploymentContext context)
        throws ResolveError {

        final ReadableArchive source = context.getSource();
        List<ModuleDefinition> defs = new ArrayList<ModuleDefinition>();

        // now let's see if the application is requesting any module imports
        Manifest m=null;
        try {
            m = source.getManifest();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot load application's manifest file :", e.getMessage());
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
        }
        if (m!=null) {
            String importedBundles = m.getMainAttributes().getValue(ManifestConstants.BUNDLE_IMPORT_NAME);
            if (importedBundles!=null) {
                for( String token : new Tokenizer(importedBundles,",")) {
                    Collection<Module> modules = modulesRegistry.getModules(token);
                    if (modules.size() ==1) {
                        defs.add(modules.iterator().next().getModuleDefinition());
                    } else {
                        throw new ResolveError("Not able to locate a unique module by name " + token);
                    }
                }
            }
        }

        // Applications can also request to be wired to implementors of certain services.
        // That means that any module implementing the requested service will be accessible
        // by the parent class loader of the application.
        if (m!=null) {
            String requestedWiring = m.getMainAttributes().getValue(org.glassfish.api.ManifestConstants.GLASSFISH_REQUIRE_SERVICES);
            if (requestedWiring!=null) {
                for (String token : new Tokenizer(requestedWiring, ",")) {
                    for (Inhabitant<?> impl : habitat.getInhabitantsByContract(token)) {
                        Module wiredBundle = modulesRegistry.find(impl.get().getClass());
                        if (wiredBundle!=null) {
                            defs.add(wiredBundle.getModuleDefinition());
                        }
                    }
                }
            }
        }

        if (defs.isEmpty()) {
            return parent;
        }  else {
            return modulesRegistry.getModulesClassLoader(parent, defs);
        }

    }

}
