/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.bootstrap.*;
import com.sun.enterprise.module.*;
import com.sun.enterprise.module.single.SingleModulesRegistry;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import static com.sun.enterprise.glassfish.bootstrap.ASMainHelper.getLastModified;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.InhabitantsParserDecorator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.util.jar.Manifest;
import java.util.jar.JarFile;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitants;
import org.kohsuke.MetaInfServices;

/**
 * Main class for static invocation, no OSGi mode, all classes loaded
 * by a single class loader.
 */
@MetaInfServices(PlatformMain.class)
public class ASMainStatic extends ASMainNonOSGi {

    private File out;
    private Habitat habitat;
    
    protected String getPreferedCacheDir() {
        return "static-cache/gf/";
    }

    public String getName() {
        return ASMain.Platform.Static.toString();
    }


    @Override
    public <T> T getStartedService(Class<T> serviceType) {
        if (habitat !=null)
            return habitat.getComponent(serviceType);
        else
            return null;
    }

    @Override
    protected void setUpCache(File sourceDir, File cacheDir) throws IOException {
        // I take care of this when running...
    }

    public void run(final Logger logger, String... args) throws Exception {

        super.run(logger, args);

        StartupContext sc = getContext(StartupContext.class);
        if (sc==null) {
            Properties p = ArgumentManager.argsToMap(args);
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("-upgrade") && i+1<args.length && !args[i+1].equals("false"))  {
                    p.put(StartupContext.STARTUP_MODULESTARTUP_NAME, "upgrade" );
                }
            }
            sc = new StartupContext(findBootstrapFile().getParentFile().getParentFile(), p);

        }
        super.setContext(sc);
        
        final File modulesDir = new File(sc.getRootDirectory() , "modules");
        final StartupContext startupContext = sc;

        setSystemProperties();

        // create our masking class loader
        final ClassLoader parent = getClass().getClassLoader();
        ClassLoader maskingClassLoade = getMaskingClassLoader(parent, sc.getRootDirectory(), logger);

        // our unique class loader.
        ClassLoader singleClassLoader = createTmpClassLoader(maskingClassLoade, modulesDir);

        // set up the cache.
/*        final File cacheDir = (System.getProperty("glassfish.static.cache.dir") != null)?new File(System.getProperty("glassfish.static.cache.dir"), getPreferedCacheDir()):new File(domainDir, getPreferedCacheDir());
        out = new File(cacheDir, "glassfish.jar");
        final long lastModified = getLastModified(modulesDir, 0);
        Thread cacheThread = null;
        if (isCacheOutdated(lastModified, cacheDir)) {
            logger.info("Cache not present, will revert to less efficient algorithm");
            cacheThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        flushAndCreate(cacheDir, lastModified);
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, "Failed setting up the cache, aborting", e);
                    }                    
                }
            });
            try {
                singleClassLoader = createTmpClassLoader(modulesDir);
            } catch (Exception e) {
                throw new BootException(e);
            }

        } else {
            try {
                singleClassLoader = createClassLoader(cacheDir);
            } catch (Exception e) {
                throw new BootException(e);
            }


        }
*/

        if (singleClassLoader==null) {
            throw new BootException("Could not create single class loader from the cache");
        }

        // ANONYMOUS CLASS HERE!!
        final ModulesRegistryImpl modulesRegistry = new SingleModulesRegistry(singleClassLoader);
        modulesRegistry.setParentClassLoader(singleClassLoader);
        final ClassLoader cl = singleClassLoader;

        Thread launcherThread = new Thread(new Runnable(){
            public void run() {
                Main main = new Main() {
                    protected Habitat createHabitat(ModulesRegistry registry, StartupContext context) throws BootException {
                        Habitat habitat = registry.newHabitat();

                        for (Object c : getContexts()) {
                            habitat.add(Inhabitants.create(c));
                        }
                        // the root registry must be added as other components sometimes inject it
                        habitat.add(new ExistingSingletonInhabitant(ModulesRegistry.class, registry));
                        habitat.add(new ExistingSingletonInhabitant(Logger.class, logger));
                        registry.createHabitat("default", createInhabitantsParser(habitat));

                        ASMainStatic.this.habitat = habitat;
                        return habitat;
                    }

                    @Override
                    protected InhabitantsParser createInhabitantsParser(Habitat habitat) {
                        InhabitantsParser parser = super.createInhabitantsParser(habitat);


                        ServiceLoader<InhabitantsParserDecorator> decorators = ServiceLoader.load(InhabitantsParserDecorator.class, cl);
                        for (InhabitantsParserDecorator decorator : decorators) {
                            //if (decorator.getName().equalsIgnoreCase(getName()))
                                decorator.decorate(parser);
                        }

                        return parser;
                    }
                };
                try {
                    main.launch(modulesRegistry, startupContext);
                } catch (BootException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        },"Static Framework Launcher");

        launcherThread.setContextClassLoader(singleClassLoader);
        launcherThread.setDaemon(true);
        launcherThread.start();

        // Wait for framework to be started, otherwise the VM would exit since there is no
        // non-daemon thread started yet. The first non-daemon thread is started
        // when our hk2 osgi-adapter is started.
        try {
            launcherThread.join();
        } catch (InterruptedException e) {
            logger.warning("main thread interrupted");
        }
        logger.fine("Framework successfully started");
/*        if (cacheThread!=null) {
            logger.info("Started cache creation");
            cacheThread.start();
            try {
                cacheThread.join();
            } catch (InterruptedException e) {
            }
            logger.info("Finished cache creation");
        }

*/        
    }

    protected static ClassLoader getMaskingClassLoader(ClassLoader parent, File root,
            Logger logger) {
        return getMaskingClassLoader(parent, root, logger, true);
    }

    public static ClassLoader getMaskingClassLoader(ClassLoader parent, File root,
            Logger logger, boolean useExplicitSystemClassLoaderCalls) {
        File f = new File(root, ASMainFelix.GF_FELIX_HOME);
        f = new File(f, ASMainFelix.CONFIG_PROPERTIES);
        if (!f.exists()) {
            return parent;
        }
        Properties props = new Properties();
        FileReader reader = null;
        try {
            reader = new FileReader(f);
            props.load(reader);
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Cannot load " + f.getAbsolutePath());
            return parent;
        } finally {
            try {
                if (reader!=null) {
                    reader.close();
                }
            } catch(IOException e) {
                // ignore.
            }
        }

        return getMaskingClassLoader(parent, props, useExplicitSystemClassLoaderCalls);
    }

    public static ClassLoader getMaskingClassLoader(final ClassLoader parent,
            final Properties props) {
        return getMaskingClassLoader(parent, props, true);
    }

    /**
     * Returns a masking class loader with the specified parent and
     * configured using the indicated properties.
     * <p>
     * Used both from the instance getMaskingClassLoader method and also from
     * the Java Web Start-aware ACC so it can property support the endorsed
     * JARs provided with GlassFish.
     * 
     * @param parent parent class loader for the new MaskingClassLoader
     * @param props configuration specifying what packages to let the
     * parent class loader attempt to load
     * @return
     */
    public static ClassLoader getMaskingClassLoader(final ClassLoader parent,
            final Properties props,
            final boolean useExplicitSystemClassLoaderCalls) {
    
    
        String punchins = props.getProperty("jre-1.6");
        StringTokenizer st = new StringTokenizer(punchins, ",");
        List<String> p = new ArrayList<String>();
        List<String> multiples = new ArrayList<String>();
        multiples.add("org.jvnet");
        multiples.add("org.glassfish");
        while (st.hasMoreTokens()) {
            String tk = st.nextToken();
            if (tk.contains(";")) {
                tk = tk.substring(0, tk.indexOf(";"));
            }
            p.add(tk.trim());
        }
        return new MaskingClassLoader(parent, p, multiples, useExplicitSystemClassLoaderCalls);
    }
    

    @Override
    Logger getLogger() {
        return logger;
    }

    @Override
    boolean createCache(File cacheDir) throws IOException {
        cacheDir.mkdirs();
        Rejar rejar = new Rejar();
        rejar.rejar(out, bootstrapFile.getParentFile());
        return true;
    }

    public ClassLoader createClassLoader(File cacheDir) throws Exception {
        if (out!=null) {
            List<URL> urls = new ArrayList<URL>();
            urls.add(out.toURI().toURL());
            findDerbyClient(urls);
            return new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
        }
        return null;
    }

    public ClassLoader createTmpClassLoader(ClassLoader parent, File moduleDir) throws Exception {
        List<URL> urls = new ArrayList<URL>();
        Set<ModuleInfo> modules = getAlreadyLoadedModules();
        insertURLs(moduleDir, modules, urls);
        findDerbyClient(urls);
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);

    }
    private final class ModuleInfo {
        final String bundleName;
        final String bundleVersion;

        private ModuleInfo(String bundleName, String bundleVersion) {
            this.bundleName = bundleName;
            this.bundleVersion = bundleVersion;
        }

        public boolean equals(Object other) {
            if (!(other instanceof ModuleInfo)) {
                return false;
            }
            ModuleInfo o = (ModuleInfo) other;
            return this.bundleName.equals(o.bundleName) && this.bundleVersion.equals(o.bundleVersion);
        }
    }

    private URL getJarFileURL(URL url) throws MalformedURLException {
        final String urlString = url.toExternalForm();
        return new URL(urlString.substring("jar:".length(), urlString.length()-
                JarFile.MANIFEST_NAME.length()));
   }
    

    private Set<ModuleInfo> getAlreadyLoadedModules() throws IOException {
        ClassLoader cl = getClass().getClassLoader();
        Set<ModuleInfo> modules = new HashSet<ModuleInfo>();
        Enumeration<URL> urls = cl.getResources(JarFile.MANIFEST_NAME);
        while (urls.hasMoreElements()) {
            InputStream is = null;
            URL url = urls.nextElement();
            try {
                is = url.openStream();
                Manifest m = new Manifest(is);
                if (m.getMainAttributes().getValue(ManifestConstants.BUNDLE_NAME)!=null) {
                    modules.add(new ModuleInfo(
                            m.getMainAttributes().getValue(ManifestConstants.BUNDLE_NAME),
                            m.getMainAttributes().getValue("Bundle-Version")
                    ));
                }
            } finally {
                try {
                    if (is!=null) {
                        is.close();
                    }
                } catch(IOException e) {
                    // ignore
                }
            }
        }
        return modules;
    }

    private void insertURLs(File directory, Set<ModuleInfo> modules, List<URL> result) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isFile() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {
                processFile(file, modules, result);
            } else if (file.isDirectory()) {
                insertURLs(file,modules, result);
            }
        }
    }

    private void processFile(File file, Set<ModuleInfo> modules, List<URL> result) throws IOException {
        JarFile jarFile = new JarFile(file);
        Manifest m = jarFile.getManifest();
        String bundleName = m.getMainAttributes().getValue(ManifestConstants.BUNDLE_NAME);
        if (bundleName != null) {
            String version = m.getMainAttributes().getValue(ManifestConstants.BUNDLE_VERSION);
            ModuleInfo info = new ModuleInfo(bundleName, version);
            if (!modules.contains(info)) {
                result.add(file.toURI().toURL());
                modules.add(info);
            }
        } else {
            result.add(file.toURI().toURL());
        }
        // claaspath processing
        String classpath = m.getMainAttributes().getValue("Class-Path");
        if (classpath!=null) {
            StringTokenizer st = new StringTokenizer(classpath);
            while (st.hasMoreTokens()) {
                String cpe =  st.nextToken();
                
                
            }
        }
    }

    private void findDerbyClient(List<URL> urls) throws IOException {
        String derbyHome = System.getProperty("AS_DERBY_INSTALL");
        File derbyLib = null;
        if (derbyHome != null) {
            derbyLib = new File(derbyHome, "lib");
        }
        if (derbyLib == null || !derbyLib.exists()) {
            // maybe the jdk...
            if (System.getProperty("java.version").compareTo("1.6") > 0) {
                File jdkHome = new File(System.getProperty("java.home"));
                derbyLib = new File(jdkHome, "../db/lib");
            }
        }
        if (!derbyLib.exists()) {
            logger.fine("Cannot find javadb client jar file, jdbc driver not available");
            return;
        }
        // Add all derby jars, as embedded driver is one jar and network driver
        // is in another.
        urls.add(new File(derbyLib, "derby.jar").toURI().toURL());
        urls.add(new File(derbyLib, "derbyclient.jar").toURI().toURL());

    }
}
