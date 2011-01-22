/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.module.bootstrap;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.InhabitantsDescriptor;
import com.sun.enterprise.module.impl.HK2Factory;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.enterprise.module.common_impl.LogHelper;
import com.sun.hk2.component.ExistingSingletonInhabitant;

import com.sun.hk2.component.InhabitantParser;
import com.sun.hk2.component.InhabitantsParser;
import org.jvnet.hk2.component.*;

import java.io.File;
import java.io.IOException;
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
        try {
            return Which.jarFile(getClass());
        } catch (IOException e) {
            throw new BootException("Failed to get bootstrap path",e);
        }
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

        StartupContext context = new StartupContext(ArgumentManager.argsToMap(args));        
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
     * <p>
     * All the <tt>launch</tt> methods start additional threads and run GFv3,
     * then return from the method. 
     *
     * @param context
     *      startup context instance
     *
     * @return
     *      the entry point to all the components in this newly launched GlassFish.
     */
    public Habitat launch(ModulesRegistry registry, StartupContext context) throws BootException {
        return launch(registry, null, context);
    }

    private static final String HABITAT_NAME = "default"; // TODO: take this as a parameter

    public Habitat launch(ModulesRegistry registry, String mainModuleName, StartupContext context) throws BootException {
        Habitat habitat = createHabitat(registry, context);
        launch(registry, habitat,mainModuleName,context);
        return habitat;
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
     * @return The ModuleStartup service
     */
    public ModuleStartup launch(ModulesRegistry registry, Habitat habitat, String mainModuleName, StartupContext context) throws BootException {
        // now go figure out the start up service
        ModuleStartup startupCode = findStartupService(registry, habitat, mainModuleName, context);
        launch(startupCode, context);
        return startupCode;
    }

    /**
     * Return the ModuleStartup service configured to be used to start the system.
     * @param registry
     * @param habitat
     * @param mainModuleName
     * @param context
     * @return
     * @throws BootException
     */
    public ModuleStartup findStartupService(ModulesRegistry registry, Habitat habitat, String mainModuleName, StartupContext context) throws BootException {
        ModuleStartup startupCode=null;
        final Module mainModule;

        if(mainModuleName!=null) {
            // instantiate the main module, this is the entry point of the application
            // code. it is supposed to have 1 ModuleStartup implementation.
            Collection<Module> modules = registry.getModules(mainModuleName);
            if (modules.size() != 1) {
                if(registry.getModules().isEmpty())
                    throw new BootException("Registry has no module at all");
                else
                    throw new BootException("Cannot find main module " + mainModuleName+" : no such module");
            }
            mainModule = modules.iterator().next();
            String targetClassName = findModuleStartupClassName(mainModule,context.getPlatformMainServiceName(), HABITAT_NAME);
            if (targetClassName==null) {
                throw new BootException("Cannot find a ModuleStartup implementation in the META-INF/services/com.sun.enterprise.v3.ModuleStartup file, aborting");
            }

            Class<? extends ModuleStartup> targetClass=null;
            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(mainModule.getClassLoader());
            try {
                targetClass = mainModule.getClassLoader().loadClass(targetClassName).asSubclass(ModuleStartup.class);
                startupCode = habitat.getComponent(targetClass);
            } catch (ClassNotFoundException e) {
                throw new BootException("Unable to load component of type " + targetClassName,e);
            } catch (ComponentException e) {
                throw new BootException("Unable to load component of type " + targetClassName,e);
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        } else {
            Collection<Inhabitant<? extends ModuleStartup>> startups = habitat.getInhabitants(ModuleStartup.class);

            if(startups.isEmpty())
                throw new BootException("No module has a ModuleStartup implementation");
            if(startups.size()>1) {
                // maybe the user specified a main
                String mainServiceName = context.getPlatformMainServiceName();
                for (Inhabitant<? extends ModuleStartup> startup : startups) {
                    Collection<String> regNames = Inhabitants.getNamesFor(startup, ModuleStartup.class.getName());
                    if (regNames.isEmpty() && mainServiceName==null) {
                        startupCode = startup.get();
                    } else {
                        for (String regName : regNames) {
                            if (regName.equals(mainServiceName)) {
                                startupCode = startup.get();
                            }
                        }
                    }

                }
                if (startupCode==null) {
                    if (mainServiceName==null) {
                        Iterator<Inhabitant<? extends ModuleStartup>> itr = startups.iterator();
                        ModuleStartup a = itr.next().get();
                        ModuleStartup b = itr.next().get();
                        Module am = registry.find(a.getClass());
                        Module bm = registry.find(b.getClass());
                        throw new BootException(String.format("Multiple ModuleStartup found: %s from %s and %s from %s",a,am,b,bm));
                    } else {
                        throw new BootException(String.format("Cannot find %s ModuleStartup", mainServiceName));
                    }
                }
            } else {
                startupCode = startups.iterator().next().get();
            }
            mainModule = registry.find(startupCode.getClass());
        }

        habitat.addIndex(Inhabitants.create(startupCode),
                ModuleStartup.class.getName(), habitat.DEFAULT_NAME);
        mainModule.setSticky(true);
        return startupCode;
    }

    public Habitat createHabitat(ModulesRegistry registry, StartupContext context) throws BootException {
        // set the parent class loader before we start loading modules
        setParentClassLoader(context, registry);

        // create a habitat and initialize them
        Habitat habitat = registry.newHabitat();
        habitat.add(new ExistingSingletonInhabitant<StartupContext>(context));
        habitat.add(new ExistingSingletonInhabitant<Logger>(Logger.global));
        // the root registry must be added as other components sometimes inject it
        habitat.add(new ExistingSingletonInhabitant(ModulesRegistry.class, registry));
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            registry.createHabitat(HABITAT_NAME, createInhabitantsParser(habitat));
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
        return habitat;
    }

    /**
     * Creates {@link InhabitantsParser} to fill in {@link Habitat}.
     * Override for customizing the behavior.
     */
    protected InhabitantsParser createInhabitantsParser(Habitat habitat) {
        return new InhabitantsParser(habitat);
    }

    protected String findMainModuleName(File bootstrap) throws BootException {
        String targetModule;
        try {
            JarFile jarFile = new JarFile(bootstrap);
            Manifest manifest = jarFile.getManifest();
                          
            Attributes attr = manifest.getMainAttributes();
            targetModule = attr.getValue(ManifestConstants.MAIN_BUNDLE);
            if (targetModule==null) {
                LogHelper.getDefaultLogger().warning(
                        "No Main-Bundle module found in manifest of " +
                        bootstrap.getAbsoluteFile());
            }
        } catch(IOException ioe) {
            throw new BootException("Cannot get manifest from " + bootstrap.getAbsolutePath(), ioe);
        }
        return targetModule;
    }

    protected void launch(ModuleStartup startupCode, StartupContext context) throws BootException {
        startupCode.setStartupContext(context);
        startupCode.start();
    }

    /**
     * Finds {@link ModuleStartup} implementation class name to perform the launch.
     *
     * <p>
     * This implementation does so by looking it up from services.
     */
    protected String findModuleStartupClassName(Module mainModule, String serviceName, String habitatName) throws BootException {
        String index = (serviceName==null || serviceName.isEmpty()?ModuleStartup.class.getName():ModuleStartup.class.getName()+":"+serviceName);
        for(InhabitantsDescriptor d : mainModule.getMetadata().getHabitats(habitatName)) {
            try {
                for (InhabitantParser parser : d.createScanner()) {
                    for (String v : parser.getIndexes()) {
                        if(v.equals(index)) {
                            parser.rewind();
                            return parser.getImplName();
                        }
                    }
                }
            } catch (IOException e) {
                throw new BootException("Failed to parse "+d.getSystemId(),e);
            }
        }

        throw new BootException("No "+ModuleStartup.class.getName()+" in "+mainModule);
    }
}
