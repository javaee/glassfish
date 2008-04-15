/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.enterprise.module.ModuleMetadata.InhabitantsDescriptor;
import com.sun.enterprise.module.impl.HK2Factory;
import com.sun.enterprise.module.impl.Utils;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.enterprise.module.common_impl.AbstractFactory;
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
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Main() {
        HK2Factory.initialize();
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
        ModulesRegistry mr = AbstractFactory.getInstance().createModulesRegistry();
        Manifest mf;
        try {
            mf = (new JarFile(bootstrap)).getManifest();
        } catch (IOException e) {
            throw new BootException("Failed to read manifest from "+bootstrap);
        }

        createRepository(root,bootstrap,mf,mr);

        StartupContext context = new StartupContext(root, args);        
        launch(mr, targetModule, context);
    }


    protected void setParentClassLoader(StartupContext context, ModulesRegistry mr) throws BootException {
        mr.setParentClassLoader(this.getClass().getClassLoader());
    }
    
    /**
     * Creates repositories needed for the launch and 
     * adds the repositories to {@link ModulesRegistry}
     *
     * @param bootstrapJar
     *      The file from which manifest entries are loaded. Used for error reporting
     */
    protected void createRepository(File root, File bootstrapJar, Manifest mf, ModulesRegistry mr) throws BootException {
        String repos = mf.getMainAttributes().getValue(ManifestConstants.REPOSITORIES);
        if (repos!=null) {
            StringTokenizer st = new StringTokenizer(repos);
            while (st.hasMoreTokens()) {
                final String repoId = st.nextToken();
                final String repoKey = "HK2-Repository-"+repoId;
                final String repoInfo;
                try {
                    repoInfo = mf.getMainAttributes().getValue(repoKey);
                } catch (Exception e) {
                    throw new BootException("Invalid repository id " + repoId+" in "+bootstrapJar, e);
                }
                if (repoInfo!=null) {
                    addRepo(root, repoId, repoInfo, mr);
                }
            }
        } else {
            // by default, adding the boot archive directory
            addRepo(root, "lib", "uri=. type=directory", mr);
        }
    }

    private void addRepo(File root, String repoId, String repoInfo, ModulesRegistry mr) throws BootException {

        StringTokenizer st = new StringTokenizer(repoInfo);
        Properties props = new Properties();
        Pattern p = Pattern.compile("([^=]*)=(.*)");
        
        while(st.hasMoreTokens()) {
            Matcher m = p.matcher(st.nextToken());
            if (m.matches()) {
                props.put(m.group(1), m.group(2));
            }
        }
        
        String uri = props.getProperty("uri");
        if (uri==null) {
            uri = ".";
        }
        String type = props.getProperty("type");
        String weight = props.getProperty("weight");

        // need a plugability layer here...
        if ("directory".equalsIgnoreCase(type)) {

            File location = new File(uri);
            if (!location.isAbsolute()) {
                location = new File(root, uri);
            }
            if (!location.exists())
                throw new BootException("Non-existent directory: "+location);
        
            /* bnevins 3/21/08
             * location might be something like "/gf/modules/."
             * The "." can cause all sorts of trouble later, so sanitize
             * the name now!
             * here's an example of the trouble:            
             * new File("/foo/.").getParentFile().getPath() --> "/foo", not "/"
             * JDK treats the dot as a file in the foo directory!!
             */
            
            try {
                location = location.getCanonicalFile();
            }
            catch(Exception e) {
                // I've never seen this happen!
                location = location.getAbsoluteFile();
            }
            
            try {
                Repository repo = new DirectoryBasedRepository(repoId, location);
                addRepo(repo, mr, weight);
            } catch (IOException e) {
                throw new BootException("Exception while adding " + repoId + " repository", e);

            }
        } else {
            throw new BootException("Invalid attributes for modules repository " + repoId + " : " + repoInfo);
        }
    }

    protected void addRepo(Repository repo, ModulesRegistry mr, String weight)
        throws IOException {
        
        repo.initialize();
        int iWeight=50;
        if (weight!=null) {
            try {
                iWeight = Integer.parseInt(weight);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        mr.addRepository(repo, iWeight);
    }

    /**
     * Launches the module system and hand over the execution to the {@link ModuleStartup}
     * implementation of the main module.
     *
     * <p>
     * This version of the method auto-discoveres the main module.
     * If there's more than one {@link ModuleStartup} implementation, it is an error.
     *
     * @param context
     *      startup context instance
     *
     */
    public void launch(ModulesRegistry registry, StartupContext context) throws BootException {
        launch(registry,null, context);
    }

    /**
     * Launches the module system and hand over the execution to the {@link ModuleStartup}
     * implementation of the main module.
     *
     * @param mainModuleName
     *      The module that will provide {@link ModuleStartup}. If null,
     *      one will be auto-discovered.
     * @param context
     *      startup context instance
     *
     */
    public void launch(ModulesRegistry registry, String mainModuleName, StartupContext context) throws BootException {
        final String habitatName = "default"; // TODO: take this as a parameter

        // set the parent class loader before we start loading modules
        setParentClassLoader(context, registry);

        // create a habitat and initialize them
        Habitat mgr = registry.newHabitat();                        
        mgr.add(new ExistingSingletonInhabitant<StartupContext>(context));
        mgr.add(new ExistingSingletonInhabitant<Logger>(Logger.global));
        // the root registry must be added as other components sometimes inject it 
        mgr.add(new ExistingSingletonInhabitant(ModulesRegistry.class, registry));
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
                Module am = registry.find(a.getClass());
                Module bm = registry.find(b.getClass());
                throw new BootException(String.format("Multiple ModuleStartup found: %s from %s and %s from %s",a,am,b,bm));
            }

            startupCode = startups.iterator().next();
            mainModule = registry.find(startupCode.getClass());
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
            targetModule = attr.getValue(ManifestConstants.MAIN_BUNDLE);
            if (targetModule==null) {
                Utils.getDefaultLogger().warning(
                        "No Main-Bundle module found in manifest of " +
                        bootstrap.getAbsoluteFile());
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
