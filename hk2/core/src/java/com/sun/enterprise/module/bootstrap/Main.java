/*
 * Main.java
 *
 * Created on October 17, 2006, 11:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.module.bootstrap;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleMetadata.InhabitantsDescriptor;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.impl.DirectoryBasedRepository;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import static com.sun.hk2.component.InhabitantsFile.CLASS_KEY;
import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;
import com.sun.hk2.component.KeyValuePairParser;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;

/**
 * CLI entry point that will setup the module subsystem and delegate the
 * main execution to the first archive in its import list...
 *
 * TODO: reusability of this class needs to be improved.
 *
 * @author dochez
 */
public class Main {

    public static void main(final String[] args) {
        (new Main()).run(args);       
    }

    public void run(final String[] args) {
        try {
            final Main main = this;
            Thread thread = new Thread() {
                public void run() {
                    try {
                        main.start(args);
                    } catch(BootException e) {
                        e.printStackTrace();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     *  We need to determine which jar file has been used to load this class
     *  Using the getResourceURL we can get this information, after that, it
     *  is just a bit of detective work to get the file path for the jar file.
     *
     * @return
     *      the path to the jar file containing this class.
     *      always returns non-null.
     *
     * @throws BootException
     *      If failed to determine the bootstrap file name.
     */
    protected File getBootstrapFile() throws BootException {
        String resourceName = getClass().getName().replace(".","/")+".class";
        URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource==null) {
            throw new BootException("Cannot get bootstrap path from "
                    + resourceName + " class location, aborting");
        }

        if (resource.getProtocol().equals("jar")) {
            try {
                JarURLConnection c = (JarURLConnection) resource.openConnection();
                URL jarFile = c.getJarFileURL();
                File f = new File(jarFile.toURI());
                return f;
            } catch (IOException e) {
                throw new BootException("Cannot open bootstrap jar file", e);
            } catch (URISyntaxException e) {
                throw new BootException("Incorrect bootstrap class URI", e);
            }
        } else
            throw new BootException("Don't support packaging "+resource+" , please contribute !");
    }

    /**
     * Start the server from the command line
     * @param args the command line arguments
     */
    public void start(String[] args) throws BootException {
        
        File bootstrap = this.getBootstrapFile();
        File root = bootstrap.getAbsoluteFile().getParentFile();

        // root is the directory in which this bootstrap.jar is located
        // For most cases, this is the lib directory although this is completely
        // dependent on the usage of this facility.
        if (root==null) {
            throw new BootException("Cannot find root installation from "+bootstrap);
        }

        String targetModule = findMainModuleName(bootstrap);

        // get the ModuleStartup implementation.
        ModulesRegistry mr = ModulesRegistry.createRegistry();
        createRepository(root,mr);

        launch(mr, targetModule, root, args);
    }

    /**
     * Creates repositories needed for the launch and 
     * adds the repositories to {@link ModulesRegistry}
     */
    protected void createRepository(File root, ModulesRegistry mr) throws BootException {
        try {
            DirectoryBasedRepository lib = new DirectoryBasedRepository("lib", root);
            lib.initialize();
            mr.addRepository(lib);
            mr.setParentClassLoader(this.getClass().getClassLoader());
        } catch(IOException ioe) {
            throw new BootException("Error while initializing lib repository at : "+root, ioe);
        }
    }

    /**
     * Launches the module system and hand over the execution to the {@link ModuleStartup}
     * implementation of the main module.
     *
     * <p>
     * This version of the method auto-discoveres the main module.
     * If there's more than one {@link ModuleStartup} implementation, it is an error.
     *
     * @param root
     *      This becomes {@link StartupContext#getRootDirectory()}
     * @param args
     *      This becomes {@link StartupContext#getArguments()}
     *
     */
    public void launch(ModulesRegistry registry, File root, String[] args) throws BootException {
        launch(registry,null,root,args);
    }

    /**
     * Launches the module system and hand over the execution to the {@link ModuleStartup}
     * implementation of the main module.
     *
     * @param mainModuleName
     *      The module that will provide {@link ModuleStartup}. If null,
     *      one will be auto-discovered.
     * @param root
     *      This becomes {@link StartupContext#getRootDirectory()}
     * @param args
     *      This becomes {@link StartupContext#getArguments()}
     *
     */
    public void launch(ModulesRegistry registry, String mainModuleName, File root, String[] args) throws BootException {
        final String habitatName = "default"; // TODO: take this as a parameter

        // create a habitat and initialize them
        StartupContext context = new StartupContext(root, args);
        Habitat mgr = registry.newHabitat();
        mgr.add(new ExistingSingletonInhabitant<StartupContext>(context));
        mgr.add(new ExistingSingletonInhabitant<Logger>(Logger.global));
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            registry.createHabitat(habitatName, mgr);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }


        // now go figure out the start up module
        final ModuleStartup startupCode;
        final Module mainModule;

        if(mainModuleName!=null) {
            // instantiate the main module, this is the entry point of the application
            // code. it is supposed to have 1 ModuleStartup implementation.
            mainModule = registry.makeModuleFor(mainModuleName, null);
            if (mainModule == null) {
                if(registry.getModules().isEmpty())
                    throw new BootException("Registry has no module at all");
                else
                    throw new BootException("Cannot find main module " + mainModuleName+" : no such module");
            }

            String targetClassName = findModuleStartup(mainModule, habitatName);
            if (targetClassName==null) {
                throw new BootException("Cannot find a ModuleStartup implementation in the META-INF/services/com.sun.enterprise.v3.ModuleStartup file, aborting");
            }

            Class<? extends ModuleStartup> targetClass=null;
            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(mainModule.getClassLoader());
            try {
                targetClass = mainModule.getClassLoader().loadClass(targetClassName).asSubclass(ModuleStartup.class);
                startupCode = mgr.getComponent(targetClass);
            } catch (ClassNotFoundException e) {
                throw new BootException("Unable to load "+targetClassName,e);
            } catch (ComponentException e) {
                throw new BootException("Unable to load "+targetClass,e);
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        } else {
            Collection<ModuleStartup> startups = mgr.getAllByContract(ModuleStartup.class);
            if(startups.isEmpty())
                throw new BootException("No module has ModuleStartup");
            if(startups.size()>1) {
                Iterator<ModuleStartup> itr = startups.iterator();
                ModuleStartup a = itr.next();
                ModuleStartup b = itr.next();
                Module am = Module.find(a.getClass());
                Module bm = Module.find(b.getClass());
                throw new BootException(String.format("Multiple ModuleStartup found: %s from %s and %s from %s",a,am,b,bm));
            }

            startupCode = startups.iterator().next();
            mainModule = Module.find(startupCode.getClass());
        }

        mainModule.setSticky(true);
        launch(startupCode, context, mainModule);
    }

    protected String findMainModuleName(File bootstrap) throws BootException {
        String targetModule;
        try {
            JarFile jarFile = new JarFile(bootstrap);
            Manifest manifest = jarFile.getManifest();

            Attributes attr = manifest.getMainAttributes();
            targetModule = attr.getValue(ManifestConstants.BUNDLE_IMPORT_NAME);
            if (targetModule==null) {
                throw new BootException("No Import-Bundles module found in manifest of " + bootstrap.getAbsoluteFile());
            }
        } catch(IOException ioe) {
            throw new BootException("Cannot get manifest from " + bootstrap.getAbsolutePath(), ioe);
        }
        return targetModule;
    }

    protected void launch(ModuleStartup startupCode, StartupContext context, Module mainModule) throws BootException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            startupCode.setStartupContext(context);
            //Thread thread = new Thread(startupCode);
            //thread.setContextClassLoader(mainModule.getClassLoader());
            //
            //thread.start();
            //try {
            //    thread.join();
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}

            Thread.currentThread().setContextClassLoader(mainModule.getClassLoader());
            startupCode.run();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     * Finds {@link ModuleStartup} implementation class name to perform the launch.
     *
     * <p>
     * This implementation does so by looking it up from services.
     */
    protected String findModuleStartup(Module mainModule, String habitatName) throws BootException {
        for(InhabitantsDescriptor d : mainModule.getMetadata().getHabitats(habitatName)) {
            try {
                for (KeyValuePairParser kvpp : d.createScanner()) {
                    for (String v : kvpp.findAll(INDEX_KEY)) {
                        if(v.equals(ModuleStartup.class.getName())) {
                            kvpp.rewind();
                            return kvpp.find(CLASS_KEY);
                        }
                    }
                }
            } catch (IOException e) {
                throw new BootException("Failed to parse "+d.systemId,e);
            }
        }

        throw new BootException("No "+ModuleStartup.class.getName()+" in "+mainModule);
    }
}
