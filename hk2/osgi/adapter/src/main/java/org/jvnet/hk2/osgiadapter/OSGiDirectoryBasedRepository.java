/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2012 Oracle and/or its affiliates. All rights reserved.
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
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

    private Map<URI, ModuleDefinition> cachedData = new HashMap<URI, ModuleDefinition>();
    private boolean cacheInvalidated = true;

    public OSGiDirectoryBasedRepository(String name, File repository) {
        this(name, repository, true);
    }

    public OSGiDirectoryBasedRepository(String name, File repository, boolean isTimerThreadDaemon) {
        super(name, repository, isTimerThreadDaemon);
    }

    @Override
    public void initialize() throws IOException {
        //TODO: caching to be revisited
//        try {
//            loadCachedData();
//        } catch (Exception e) {
//            logger.logp(Level.WARNING, "OSGiDirectoryBasedRepository", "initialize", "Cache disabled because of exception: ", e);
//        }
        super.initialize();
//        if (cacheInvalidated) {
//            saveCache();
//        }
    }

    /**
     * Loads the inhabitants metadata from the cache. metadata is saved in a file
     * called inhabitants
     *
     * @throws Exception if the file cannot be read correctly
     */
    private void loadCachedData() throws Exception {
        String cacheLocation = getProperty(org.jvnet.hk2.osgiadapter.Constants.HK2_CACHE_DIR);
        if (cacheLocation == null) {
            return;
        }
        File io = new File(cacheLocation, org.jvnet.hk2.osgiadapter.Constants.INHABITANTS_CACHE);
        if (!io.exists()) return;
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.INFO, "OSGiDirectoryBasedRepository", "loadCachedData", "HK2 cache file = {0}", new Object[]{io});
        }
        ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(io),
                getBufferSize()));
        this.cachedData = (Map<URI, ModuleDefinition>) stream.readObject();
        stream.close();
        cacheInvalidated = false;
    }

    /**
     * Saves the inhabitants metadata to the cache in a file called inhabitants
     * @throws IOException if the file cannot be saved successfully
     */
    private void saveCache() throws IOException {
        String cacheLocation = getProperty(org.jvnet.hk2.osgiadapter.Constants.HK2_CACHE_DIR);
        if (cacheLocation == null) {
            return;
        }
        File io = new File(cacheLocation, org.jvnet.hk2.osgiadapter.Constants.INHABITANTS_CACHE);
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.INFO, "OSGiDirectoryBasedRepository", "saveCache", "HK2 cache file = {0}", new Object[]{io});
        }
        if (io.exists()) io.delete();
        io.createNewFile();
        Map<URI, ModuleDefinition> data = new HashMap<URI, ModuleDefinition>();
        for (ModuleDefinition md : findAll()) {
            data.put(md.getLocations()[0], md);
        }
        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(io), getBufferSize()));
        os.writeObject(data);
        os.close();
    }

    private void deleteCache() {
        String cacheLocation = getProperty(org.jvnet.hk2.osgiadapter.Constants.HK2_CACHE_DIR);
        if (cacheLocation == null) {
            return;
        }
        File io = new File(cacheLocation, org.jvnet.hk2.osgiadapter.Constants.INHABITANTS_CACHE);
        if (io.exists()) {
            if (io.delete()) {
                logger.logp(Level.FINE, "OSGiDirectoryBasedRepository",
                        "deleteCache", "deleted = {0}", new Object[]{io});
            } else {
                logger.logp(Level.WARNING, "OSGiDirectoryBasedRepository",
                        "deleteCache", "failed to delete = {0}", new Object[]{io});
            }
        }
    }

    private int getBufferSize() {
        int bufsize = org.jvnet.hk2.osgiadapter.Constants.DEFAULT_BUFFER_SIZE;
        try {
            bufsize = Integer.valueOf(getProperty(org.jvnet.hk2.osgiadapter.Constants.HK2_CACHE_IO_BUFFER_SIZE));
        } catch (Exception e) {
        }
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "OSGiDirectoryBasedRepository", "getBufferSize", "bufsize = {0}", new Object[]{bufsize});
        }
        return bufsize;
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
        ModuleDefinition md = cachedData.get(jar.toURI());
        if (md != null) {
            if(logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINER, "OSGiDirectoryBasedRepository", "loadJar", "Found in mdCache for {0}", new Object[]{jar});
            }
            return md;
        }
        cacheInvalidated = true;
        Manifest m = new JarFile(jar).getManifest();
        if (m != null) {
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
    
    protected String getProperty(String property) {
        BundleContext bctx = null;
        try {
            bctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
        } catch (Exception e) {
        }
        String value = bctx != null ? bctx.getProperty(property) : null;
        return value != null ? value : System.getProperty(property);
    }

}
