package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.*;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import com.sun.enterprise.module.impl.HK2Factory;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.Holder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.jvnet.hk2.component.Habitat;

/**
 * Main class for static invocation, no OSGi mode, all classes loaded
 * by a single class loader.
 */
public class ASMainStatic extends AbstractMain {

    private final Logger logger;
    private String args[];
    private File out;
    
    public ASMainStatic(Logger logger, String args[]) {
        this.logger = logger;
        this.args = args;
    }

    public void run() {
        try {
            start(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start(String args[]) throws BootException {

        final File modulesDir = findBootstrapFile().getParentFile();
        final File glassfishDir = modulesDir.getParentFile();

        final StartupContext startupContext = new StartupContext(modulesDir, args);

        ASMainHelper helper = new ASMainHelper(Logger.getAnonymousLogger());
        helper.parseAsEnv(glassfishDir);
        File domainDir = helper.getDomainRoot(startupContext);
        helper.verifyAndSetDomainRoot(domainDir);

        System.setProperty("com.sun.aas.installRoot",glassfishDir.getAbsolutePath());
        // crazy. we need to do better 
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        URI installRootURI = new File(installRoot).toURI();
        System.setProperty("com.sun.aas.installRootURI", installRootURI.toString());
        String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
        URI instanceRootURI = new File(instanceRoot).toURI();
        System.setProperty("com.sun.aas.instanceRootURI", instanceRootURI.toString());

        // our unique class loader.
        ClassLoader singleClassLoader = null;

        // initialize hk2
        HK2Factory.initialize();

        // set up the cache.
        final File cacheDir = (System.getProperty("glassfish.static.cache.dir") != null)?new File(System.getProperty("glassfish.static.cache.dir"), "static-cache/gf/"):new File(domainDir, "static-cache/gf/");
        out = new File(cacheDir, "glassfish.jar");
        final long lastModified = getLastModified(bootstrapFile.getParentFile(), 0);
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
                singleClassLoader = createTmpClassLoader(bootstrapFile.getParentFile());
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
        if (singleClassLoader==null) {
            throw new BootException("Could not create single class loader from the cache");
        }
        final Module[] proxyMod = new Module[1];

        // ANONYMOUS CLASS HERE!!
        final ModulesRegistryImpl modulesRegistry = new ModulesRegistryImpl(null) {
            public Module find(Class clazz) {
                Module m = super.find(clazz);
                if (m == null)
                    return proxyMod[0];
                return m;
            }

            @Override
            public Collection<Module> getModules(String moduleName) {
                // I could not care less about the modules names
                return getModules();
            }

            @Override
            public Collection<Module> getModules() {
                ArrayList<Module> list = new ArrayList<Module>();
                list.add(proxyMod[0]);
                return list;
            }

            @Override
            public Module makeModuleFor(String name, String version) throws ResolveError {
                return proxyMod[0];
            }

            @Override
            public void parseInhabitants(Module module, String name, InhabitantsParser inhabitantsParser)
                throws IOException {

                Holder<ClassLoader> holder = new Holder<ClassLoader>() {
                    public ClassLoader get() {
                        return proxyMod[0].getClassLoader();
                    }
                };

                for (ModuleMetadata.InhabitantsDescriptor d : proxyMod[0].getMetadata().getHabitats(name))
                    inhabitantsParser.parse(d.createScanner(),holder);
                }            
        };
        
        modulesRegistry.setParentClassLoader(singleClassLoader);

        ModuleDefinition moduleDef = null;
        try {
            moduleDef = new ProxyModuleDefinition(singleClassLoader);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot load single module from cache", e);
            throw new BootException(e);
        }
        proxyMod[0] = new ProxyModule(modulesRegistry, moduleDef, singleClassLoader);
        modulesRegistry.add(moduleDef);

        Thread launcherThread = new Thread(new Runnable(){
            public void run() {
                Main main = new Main() {
                    protected Habitat createHabitat(ModulesRegistry registry, StartupContext context) throws BootException {
                        Habitat habitat = registry.newHabitat();
                        habitat.add(new ExistingSingletonInhabitant<StartupContext>(context));
                        // the root registry must be added as other components sometimes inject it
                        habitat.add(new ExistingSingletonInhabitant(ModulesRegistry.class, registry));
                        habitat.add(new ExistingSingletonInhabitant(Logger.class, logger));
                        registry.createHabitat("default", createInhabitantsParser(habitat));
                        return habitat;
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
        if (cacheThread!=null) {
            logger.info("Started cache creation");
            cacheThread.start();
            try {
                cacheThread.join();
            } catch (InterruptedException e) {
            }
            logger.info("Finished cache creation");
        }
    }

    @Override
    Logger getLogger() {
        return logger;
    }

    @Override
    long getSettingsLastModification() {
        return 0;
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

    public ClassLoader createTmpClassLoader(File moduleDir) throws Exception {
        List<URL> urls = new ArrayList<URL>();
        insertURLs(moduleDir, urls);
        findDerbyClient(urls);
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());

    }

    private void insertURLs(File directory, List<URL> result) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                result.add(file.toURI().toURL());
            } else {
                insertURLs(file, result);
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
            logger.info("Cannot find javadb client jar file, jdbc driver not available");
            return;
        }
        // Add all derby jars, as embedded driver is one jar and network driver
        // is in another.
        urls.add(new File(derbyLib, "derby.jar").toURI().toURL());
        urls.add(new File(derbyLib, "derbyclient.jar").toURI().toURL());

    }
}
