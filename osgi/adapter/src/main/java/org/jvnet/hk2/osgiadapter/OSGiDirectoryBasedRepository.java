/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.jvnet.hk2.osgiadapter;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.enterprise.module.common_impl.ModuleId;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

import static org.jvnet.hk2.osgiadapter.Logger.logger;

/**
 * Only OSGi bundles are recognized as modules.
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiDirectoryBasedRepository extends DirectoryBasedRepository {

    ModuleDefinitionCacheSingleton cache = ModuleDefinitionCacheSingleton.getInstance();

    public OSGiDirectoryBasedRepository(String name, File repository) {
        this(name, repository, true);
    }

    public OSGiDirectoryBasedRepository(String name, File repository, boolean isTimerThreadDaemon) {
        super(name, repository, isTimerThreadDaemon);
    }

    @Override
    public void initialize() throws IOException {
        super.initialize();
    }

    /**
     * This class overrides this mthod, because we don't support the following cases:
     * 1. external manifest.mf file for a jar file
     * 2. jar file exploded as a directory.
     * Both the cases are supported in HK2, but not in OSGi.
     *
     * @param jar bundle jar
     * @return a ModuleDefinition for this bundle
     * @throws IOException
     */
    @Override
    protected ModuleDefinition loadJar(File jar) throws IOException {
        assert (jar.isFile()); // no support for exploded jar
        ModuleDefinition md = cache.get(jar.toURI());
        if (md != null) {
            if(logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINER, "OSGiDirectoryBasedRepository", "loadJar", "Found in mdCache for {0}", new Object[]{jar});
            }
            return md;
        }

        Manifest m = new JarFile(jar).getManifest();
        if (m != null) {
            cache.invalidate();

            // Needs to be added to the cache, cache needs to be saved (on shutdown?), but we want a BundleJar, not a Jar.Archive
            if (m.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME) != null) {
                Logger.logger.logp(Level.FINE, "OSGiDirectoryBasedRepository", "loadJar",
                        "{0} is an OSGi bundle", new Object[]{jar});
                return newModuleDefinition(jar, null);
            }
        }
        return null;
    }

    @Override
    protected ModuleDefinition newModuleDefinition(File jar, Attributes attr) throws IOException {
        return new OSGiModuleDefinition(jar);
    }

    @Override
    protected void loadModuleDefs(Map<ModuleId, ModuleDefinition> moduleDefs, List<URI> libraries) throws IOException {
        if (cache.isCacheInvalidated()) {
          super.loadModuleDefs(moduleDefs, libraries);
        }
    }
}
