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
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.impl.DirectoryBasedRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.ComponentManager;
import org.jvnet.hk2.component.ComponentException;

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
        Collection<ModuleStartup> startups = new ComponentManager(registry).getComponents(ModuleStartup.class);
        if(startups.isEmpty())
            throw new BootException("No module has ModuleStartup");
        if(startups.size()>1) {
            Iterator<ModuleStartup> itr = startups.iterator();
            throw new BootException("Multiple ModuleStartup found: "+itr.next()+" and "+itr.next());
        }

        ModuleStartup ms = startups.iterator().next();
        Module mainModule = Module.find(ms.getClass());
        StartupContext context = new StartupContext(root, mainModule, args);
        launch(ms,context, mainModule);
    }

    /**
     * Launches the module system and hand over the execution to the {@link ModuleStartup}
     * implementation of the main module.
     *
     * @param root
     *      This becomes {@link StartupContext#getRootDirectory()}
     * @param args
     *      This becomes {@link StartupContext#getArguments()}
     *
     */
    public void launch(ModulesRegistry registry, String mainModuleName, File root, String[] args) throws BootException {
        // instantiate the main module, this is the entry point of the application
        // code. it is supposed to have 1 ModuleStartup implementation.
        final Module mainModule = registry.makeModuleFor(mainModuleName, null);
        if (mainModule == null) {
            if(registry.getModules().isEmpty())
                throw new BootException("Registry has no module at all");
            else
                throw new BootException("Cannot find main module " + mainModuleName+" : no such module");
        }

        String targetClassName = findModuleStartup(mainModule);
        if (targetClassName==null) {
            throw new BootException("Cannot find a ModuleStartup implementation in the META-INF/services/com.sun.enterprise.v3.ModuleStartup file, aborting");
        }

        mainModule.setSticky(true);

        Class<? extends ModuleStartup> targetClass=null;
        ModuleStartup startupCode;
        ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mainModule.getClassLoader());
            try {
                targetClass = mainModule.getClassLoader().loadClass(targetClassName).asSubclass(ModuleStartup.class);
                startupCode = new ComponentManager(registry).getComponent(targetClass);
            } catch (ClassNotFoundException e) {
                throw new BootException("Unable to load "+targetClassName,e);
            } catch (ComponentException e) {
                throw new BootException("Unable to load "+targetClass,e);                
            }
            StartupContext context = new StartupContext(root, mainModule, args);

            launch(startupCode, context, mainModule);
        } finally {
            Thread.currentThread().setContextClassLoader(currentCL);
        }

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
            Thread thread = new Thread(startupCode);
            thread.setContextClassLoader(mainModule.getClassLoader());

            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
    protected String findModuleStartup(Module mainModule) {
        // so far, I only take the first implementation...
        ClassLoader mainModuleClassLoader = mainModule.getClassLoader();

        LineNumberReader fr=null;
        InputStream is=null;
        final String serviceIntf = ModuleStartup.class.getName();
        try {
            is = mainModuleClassLoader.getResourceAsStream("META-INF/services/"+serviceIntf);
            if (is==null) {
                System.err.println("no META-INF/services/"+ serviceIntf + " file found in " + mainModule);
                return null;
            }
            fr = new LineNumberReader(new InputStreamReader(is));
            return fr.readLine();
        } catch (IOException e) {
            System.err.println("Cannot read the META-INF/services/"+serviceIntf+" file : " +  e);
            return null;
        } finally {
            if (fr!=null) {
                try {
                    fr.close();
                } catch(Exception e) {
                    System.err.println("Cannot close file reader "  + e);

                }
            }
            if (is!=null) {
                try {
                    is.close();
                } catch(Exception e) {
                    System.err.println( "Cannot close input stream " + e);
                }
            }
        }
    }
}
