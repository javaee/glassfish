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

package com.sun.enterprise.module.common_impl;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.hk2.component.InhabitantsParser;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigParser;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * The Modules Registry maintains the registry of all available module.
 *
 * TODO: concurrency bug in the acess of the repositories field.
 *
 * @author Jerome Dochez
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class AbstractModulesRegistryImpl implements ModulesRegistry {
    /**
     * {@link ModulesRegistry} can form a tree structure by using this pointer.
     * It works in a way similar to the classloader tree. Modules defined in the parent
     * are visible to children. 
     */
    protected final AbstractModulesRegistryImpl parent;
    protected final ConcurrentMap<String,Module> modules = new ConcurrentHashMap<String,Module>();

    protected final Map<Integer,Repository> repositories = new TreeMap<Integer,Repository>();

    private final ConcurrentMap<Class, CopyOnWriteArrayList> runningServices = new ConcurrentHashMap<Class,CopyOnWriteArrayList>();

    /**
     * Service provider class names and which modules they are in.
     *
     * <p>
     * This is used for the classloader punch-in hack &mdash; to work nicely
     * with classic service loader implementation, we need to be able to allow
     * any modules to see these classes.
     */
    protected final Map<String,Module> providers = new HashMap<String,Module>();

    protected AbstractModulesRegistryImpl(AbstractModulesRegistryImpl parent) {
        this.parent = parent;
    }

    /**
     * Creates an uninitialized {@link Habitat}
     *
     */
    public Habitat newHabitat() throws ComponentException {
        return new Habitat();
    }

    /**
     * Creates a {@link Habitat} from all the modules in this registry
     *
     * @param name
     *      Determines which inhabitants descriptors are loaded.
     *      (so that different parallel habitats can be
     *      created over the same modules registry.)
     */
    public Habitat createHabitat(String name) throws ComponentException {
        Habitat h = newHabitat();
        return createHabitat(name, h);
    }

    @Override
    public Habitat createHabitat(String name, Habitat h) throws ComponentException {
        if (h==null)
            h = newHabitat();
        return createHabitat(name,new InhabitantsParser(h));
    }

    @Override
    public Habitat createHabitat(String name, InhabitantsParser parser) throws ComponentException {
        try {
            Habitat habitat = parser.habitat;

            for (final Module module : getModules())
                parseInhabitants(module, name,parser);

            ConfigParser configParser = new ConfigParser(habitat);
            for( Populator p : habitat.getAllByContract(Populator.class) )
                p.run(configParser);

            return habitat;
        } catch (IOException e) {
            throw new ComponentException("Failed to create a habitat",e);
        }
    }

    protected abstract void parseInhabitants(Module module,
                                  String name,
                                  InhabitantsParser inhabitantsParser)
            throws IOException;

    /**
     * Add a new <code>Repository</code> to this registry. From now on
     * the repository will be used to procure requested module not yet registered
     * in this registry instance. Repository can be searched in a particular 
     * order (to accomodate performance requirements like looking at local 
     * repositories first), a search order (1 to 100) can be specified when 
     * adding a repository to the registry (1 is highest priority). 
     * @param repository new repository to attach to this registry
     * @param weight int value from 1 to 100 to specify the search order
     */
    public synchronized void addRepository(Repository repository, int weight) {
        while (repositories.containsKey(weight)) {
            weight++;
        }
        repositories.put(weight, repository);
    }
    
    /**
     * Add a new <code>Repository</code> to this registry. From now on 
     * the repository will be used to procure requested nodule not 
     * registered in this instance.
     * @param repository new repository to attach to this registry
     */
    public synchronized void addRepository(Repository repository) {
        repositories.put(100+repositories.size(), repository);
    }
    
    /**
     * Remove a repository from the list of attached repositories to 
     * this instances. After this call, the <code>Repository</code>
     * name will not be used to procure missing modules any 
     * longer
     * @param name name of the repository to remove
     */
    public synchronized void removeRepository(String name) {
        for (Integer weight : repositories.keySet()) {
            Repository repo = repositories.get(weight);
            if (repo.getName().equals(name)) {
                repositories.remove(weight);
                return;
            }
        }
    }

    /**
     * Get a repository from the list of attached repositories
     * 
     * @param name name of the repository to return
     * @return the repository or null if not found
     */
    public synchronized Repository getRepository(String name) {
        for (Integer weight : repositories.keySet()) {
            Repository repo = repositories.get(weight);
            if (repo.getName().equals(name)) {
                return repo;
            }
        }
        return null;
    }

    /**
     * Returns the <code>Module</code> instance giving a name and version 
     * constraints.
     *
     * @param name the module name
     * @param version
     *      the module version. Can be null if the caller doesn't care about the version.
     * @return the module instance or null if none can be found
     * @throws ResolveError if the module dependencies cannot be resolved
     */
    public Module makeModuleFor(String name, String version) throws ResolveError {
        Module module;
        Logger.getAnonymousLogger().fine("this.makeModuleFor("+name+ ", " + version + ") called.");
        if(parent!=null) {
            module = parent.makeModuleFor(name,version);
            if(module!=null)        return module;
        }

        module = modules.get(name);
        if (module==null) {
            module = loadFromRepository(name, version);
            if (module!=null) {
                add(module);
            }
        }
        if (module!=null) {
            module.resolve();
        }
        Logger.getAnonymousLogger().fine("this.makeModuleFor("+name+ ", " + version + ") returned " + module);
        return module;
    }

    /**
     * Find and return a loaded Module that has the package name in its list
     * of exported interfaces.
     *
     * @param packageName the requested implementation package name.
     * @return the <code>Module</code> instance implementing the package
     * name or null if not found.
     * @throws ResolveError if the module dependencies cannot be resolved
     */
    public Module makeModuleFor(String packageName) throws ResolveError {
        if(parent!=null) {
            Module m = parent.makeModuleFor(packageName);
            if(m!=null)     return m;
        }

        for (Module module : modules.values()) {
            String[] exportedPkgs = module.getModuleDefinition().getPublicInterfaces();
            for (String exportedPkg : exportedPkgs) {
                if (exportedPkg.equals(packageName)) {
                    module.resolve();
                    return module;
                }
            }
        }
        return null;
    }

    protected Module loadFromRepository(String name, String version) {
        Set<Integer> keys = repositories.keySet();
        TreeSet<Integer> sortedKeys = new TreeSet<Integer>();
        sortedKeys.addAll(keys);
        for (Integer key : sortedKeys) {
            Repository repo = repositories.get(key);
            ModuleDefinition moduleDef = repo.find(name, version);
            if (moduleDef!=null) {
                return newModule(moduleDef);
            }
        }
        return null;
    }

    /**
     * Factory method for creating new instances of Module.
     * @param moduleDef module definition of the new module to be created
     * @return a new Module instance
     */
    protected abstract Module newModule(ModuleDefinition moduleDef);

    /**
     * Add a new module to this registry. Once added, the module will be 
     * available through one of the getServiceImplementor methods.
     * @param newModule the new module
     */
    private void add(Module newModule) {
        //if (Utils.isLoggable(Level.INFO)) {
        //    Utils.getDefaultLogger().info("New module " + newModule);
        //}
        assert newModule.getRegistry()==this;
        modules.put(newModule.getModuleDefinition().getName(), newModule);

        // pick up providers from this module
        for( ModuleMetadata.Entry spi : newModule.getMetadata().getEntries() ) {
            for( String name : spi.providerNames )
                providers.put(name,newModule);
        }
    }
    
    /**
     * Removes a module from the registry. The module will not be accessible 
     * from this registry after this method returns.
     */
    public void remove(Module module) {
        //if (Utils.isLoggable(Level.INFO)) {
        //    Utils.getDefaultLogger().info("Removed module " + module);
        //}
        assert module.getRegistry()==this;
        modules.remove(module.getModuleDefinition().getName());

        // TODO: modules comes right back when getModules() is called.
        // the modeling is incorrect
    }
    
    /** 
     * Returns the list of shared Modules registered in this instance.
     *
     * <p>
     * The returned list will not include the modules defined in the ancestor
     * {@link AbstractModulesRegistryImpl}s.
     *
     * @return an umodifiable list of loaded modules
     */
    public synchronized Collection<Module> getModules() {
        // force repository extraction
        Set<Integer> keys = repositories.keySet();
        TreeSet<Integer> sortedKeys = new TreeSet<Integer>();
        sortedKeys.addAll(keys);
        for (Integer key : sortedKeys) {
            Repository repo = repositories.get(key);
            for (ModuleDefinition moduleDef : repo.findAll()) {
                if (modules.get(moduleDef.getName())==null) {
                    Module newModule = newModule(moduleDef);
                    if (newModule!=null) {
                        // When some module can't get installed,
                        // don't halt proceeding, instead continue
                        add(newModule);
                        // don't resolve such modules, we just want to know about them
                    }
                }
            }
        }
        return modules.values();
    }

    /**
     * Modules can notify their registry that they have changed (classes, 
     * resources,etc...). Registries are requested to take appropriate action
     * to make the new module available.
     */
    public void changed(Module service) {
        
        System.out.println("I have received changed event from " + service);        
        // house keeping...
        remove(service);
        ModuleDefinition info = service.getModuleDefinition();
        
        Module newService = newModule(info);
        
        // store it
        add(newService);
    }   
    
    /**
     * Registers a new DefaultModuleDefinition in this registry. Using this module
     * definition, the registry will be capable of created shared and private
     * <code>Module</code> instances.
     */
    public synchronized Module add(ModuleDefinition info) throws ResolveError {

        // it may have already been created
        Module service = makeModuleFor(info.getName(), info.getVersion());
        if (service!=null) {
        //    Utils.getDefaultLogger().info("Service " + info.getName()
        //       + " already registered");
        } else {
            // create the service instance
            service = newModule(info);
            add(service);
        }
        return service;
    }

    /**
     * Print a Registry dump to the logger
     * @param logger the logger to dump on
     */
    public void print(Logger logger) {
        logger.info("Modules Registry information : " + modules.size() + " modules");
        for (Module module : modules.values()) {
            logger.info(module.getModuleDefinition().getName());
        }
    }

    public <T> Iterable<Class<? extends T>> getProvidersClass(final Class<T> serviceClass) {
        // oh boy, it really hurts not to have type inference.
        return new Iterable<Class<? extends T>>() {
            public Iterator<Class<? extends T>> iterator() {
                return new FlattenIterator<Class<? extends T>>(new AdapterIterator<Iterator<Class<? extends T>>,Module>(getModules().iterator()) {
                    protected Iterator<Class<? extends T>> adapt(Module module) {
                        return module.getProvidersClass(serviceClass).iterator();
                    }
                });
            }
        };
    }

    /**
     * Returns a collection of Module containing at least one implementation
     * of the passed service interface class.
     *
     * @param serviceClass the service interface class
     * @return a collection of module
     */
    public Iterable<Module> getModulesProvider(final Class serviceClass) {
        return new Iterable<Module>() {
            public Iterator<Module> iterator() {
                return new AdapterIterator<Module,Module>(getModules().iterator()) {
                    protected Module adapt(Module m) {
                        if(m.hasProvider(serviceClass))
                            return m;
                        else
                            return null;    // skip
                    }
                };
            }
        };
    }

    /**
     * Registers a running service, this is useful when other components need
     * to have access to a provider of a service without having to create
     * a new instance and initialize it.
     * @param serviceClass the service interface
     * @param provider the provider of that service.
     */
    public <T> void registerRunningService(Class<T> serviceClass, T provider) {
        CopyOnWriteArrayList rs = runningServices.get(serviceClass);
        if (rs==null) {
            rs = new CopyOnWriteArrayList<T>();
            CopyOnWriteArrayList existing = runningServices.putIfAbsent(serviceClass, rs);
            if(existing!=null)
                rs = existing;
        }
        rs.add(provider);
    }

    /**
     * Removes a running service, this is useful when a service instance is no longer
     * available as a provider of a service.
     */
    public <T> boolean unregisterRunningService(Class<T> serviceClass, T provider) {
        CopyOnWriteArrayList rs = runningServices.get(serviceClass);
        if (rs==null) {
            return false;
        }
        return rs.remove(provider);
    }

    /**
     * Returns all running services implementation of the passed service
     * interface
     * @param serviceClass the service interface
     * @return the list of providers of that service.
     */
    public <T> List<T> getRunningServices(Class<T> serviceClass) {
        List r = runningServices.get(serviceClass);
        if(r!=null)     return r;
        return Collections.emptyList();
    }

    /**
     * Gets the {@link Module} that provides the provider of the given name.
     */
    public Module getProvidingModule(String providerClassName) {
        return providers.get(providerClassName);
    }

    public void dumpState(PrintStream writer) {
        
        writer.println("Registry Info");
        for (Repository repo : repositories.values()) {
            writer.println("Attached repository : " + repo.getName());
            writer.println(repo.toString());
        }
        for (Module module : getModules()) {
            
            writer.println("Registered Module " + module.getModuleDefinition().getName());
            module.dumpState(writer);
        }
    }

}
