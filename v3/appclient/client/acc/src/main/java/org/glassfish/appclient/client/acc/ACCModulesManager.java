/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.glassfish.bootstrap.ProxyModule;
import com.sun.enterprise.glassfish.bootstrap.ProxyModuleDefinition;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.impl.HK2Factory;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantsParser;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigParser;

/**
 * Encapsulates details of preparing the HK2 habitat while also providing
 * a "main" HK2 module the HK2 bootstrap logic can start.
 * <p>
 * The HK2 habitat must be initialized before any AppClientContainer can be
 * created, because each ACC is an HK2 service so it can use injection.  The
 * AppClientContainerConfigurator uses the habitat directly (without injection)
 * to create new ACCs. Part of initializing the habitat involves (at least
 * currently) finding and starting a "main HK2 module."  This class serves
 * that purpose, even though this class is not the main program.  To support
 * embedded ACCs we do not assume we provide the actual main program, but we
 * seem to need to offer a main module to HK2.  So this class implements
 * ModuleStartup even though it does little.
 *
 * @author tjquinn
 */
public class ACCModulesManager implements ModuleStartup {

    private static Habitat habitat = null;

    private StartupContext startupContext = null;

    public static void initialize(final Class c) throws BootException, URISyntaxException, IOException {
//        URI locURI = c.getProtectionDomain().getCodeSource().getLocation().toURI();
//
//        File locFile = new File(locURI);
//        JarFile jf = new JarFile(locFile);
//        Manifest mf = jf.getManifest();
//        Attributes mainAttrs = mf.getMainAttributes();
//        String classPath = locURI.toASCIIString() + " " + mainAttrs.getValue(Name.CLASS_PATH);

        habitat = prepareHabitat(
                Logger.getLogger("org.glassfish.appclient.client"));
//        habitat.addComponent("default", habitat);
//        habitat.getComponent(Globals.class);
    }

//    public synchronized static Habitat getHabitat(final ClassLoader classLoader, final Logger logger) throws
//        BootException, URISyntaxException {
//        if (habitat == null) {
//            habitat = prepareHabitat(classLoader, logger);
//        }
//        return habitat;
//    }

//    static Habitat getHabitat() {
//        return habitat;
//    }

    public static <T> T getComponent(Class<T> c) {
//        return Globals.get(c);
        return habitat.getComponent(c);
    }

    /**
     * Sets up the HK2 habitat.
     * <p>
     * Must be invoked at least once before an AppClientContainerConfigurator
     * returns a new AppClientContainer to the caller.
     * @param classLoader
     * @param logger
     * @throws com.sun.enterprise.module.bootstrap.BootException
     * @throws java.net.URISyntaxException
     */
    private static Habitat prepareHabitat(/*final String classPath, */
            final Logger logger) throws BootException, URISyntaxException, MalformedURLException {

        final URI bootstrapURI = findBootstrapURL().toURI();
//        final String[] classPathElements = classPath.split(" ");
//        final Set<URL> urls = new HashSet<URL>();
//        for (String elt : classPathElements) {
//            URI elementURI = bootstrapURI.resolve(elt);
//            urls.add(elementURI.toURL());
//        }
//        final URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]),
//                Thread.currentThread().getContextClassLoader());

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File modulesDir = new File(bootstrapURI).getParentFile();
        final File glassfishDir = modulesDir.getParentFile();

        Properties props = new Properties();
        URI domain1URI = bootstrapURI.resolve("../domains/domain1");
        File f = new File(domain1URI);
        props.setProperty("--domaindir", f.getAbsolutePath());
        final StartupContext startupContext = new StartupContext(modulesDir, props);
        final Module[] proxyMod = new Module[1];

        HK2Factory.initialize();
        final ModulesRegistryImpl modulesRegistry = new ModulesRegistryImpl(null) {
            @Override
            public Module find(Class clazz) {
                Module m = super.find(clazz);
                if (m == null)
                    return proxyMod[0];
                return m;
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
            @Override
            public Habitat createHabitat(String name, InhabitantsParser parser) throws ComponentException {
                try {
                    Habitat habitat = parser.habitat;

                    for (final Module module : getModules())
                        parseInhabitants(module, name,parser);

//                    ConfigParser configParser = new ConfigParser(habitat);
//                    for( Populator p : habitat.getAllByContract(Populator.class) )
//                        p.run(configParser);

                    return habitat;
                } catch (IOException e) {
                    throw new ComponentException("Failed to create a habitat",e);
                }
            }
        };

        modulesRegistry.setParentClassLoader(classLoader);

        ModuleDefinition moduleDef = null;
        try {
            moduleDef = new ProxyModuleDefinition(classLoader);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot load single module from cache", e);
            throw new BootException(e);
        }
        proxyMod[0] = new ProxyModule(modulesRegistry, moduleDef, classLoader);
        modulesRegistry.add(moduleDef);

//        Thread launcherThread = new Thread(new Runnable(){
//            public void run() {
//                Main main = new Main() {
//                    protected Habitat createHabitat(ModulesRegistry registry, StartupContext context) throws BootException {
//                        Habitat habitat = registry.newHabitat();
//                        habitat.add(new ExistingSingletonInhabitant<StartupContext>(context));
//                        // the root registry must be added as other components sometimes inject it
//                        habitat.add(new ExistingSingletonInhabitant(ModulesRegistry.class, registry));
//                        habitat.add(new ExistingSingletonInhabitant(Logger.class, logger));
//                        registry.createHabitat("default", createInhabitantsParser(habitat));
//                        return habitat;
//                    }
//                };
//                try {
//                    main.launch(modulesRegistry, startupContext);
//                } catch (BootException e) {
//                    logger.log(Level.SEVERE, e.getMessage(), e);
//                }
//            }
//        },"Static Framework Launcher");

        Habitat newHabitat = null;
        Main main = new Main() {
            @Override
            protected Habitat createHabitat(ModulesRegistry registry, StartupContext context) throws BootException {
                Habitat habitat = registry.newHabitat();
                habitat.add(new ExistingSingletonInhabitant<StartupContext>(context));
                // the root registry must be added as other components sometimes inject it
                habitat.add(new ExistingSingletonInhabitant(ModulesRegistry.class, registry));
                habitat.add(new ExistingSingletonInhabitant(Logger.class, logger));
                registry.createHabitat("default", createInhabitantsParser(habitat));
                return habitat;
            }

            @Override
            public Habitat launch(ModulesRegistry registry, StartupContext context) throws BootException {
                return createHabitat(registry, context);
            }

        };
        try {
            newHabitat = main.launch(modulesRegistry, startupContext);
        } catch (BootException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return newHabitat;
    }

    private static URL findBootstrapURL() {
        return AppClientCommand.class.getProtectionDomain().getCodeSource().getLocation();
    }

    public void setStartupContext(StartupContext startupContext) {
        this.startupContext = startupContext;
    }

    public void start() {
        //no-op
    }

    public void stop() {
        //no-op
    }

}
