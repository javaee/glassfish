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

package com.sun.enterprise.module.common_impl;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.ContextDuplicatePostProcessor;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.api.ServiceLocatorState;
import org.glassfish.hk2.bootstrap.HK2Populator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.*;

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
    protected final ModulesRegistry parent;
    protected final ConcurrentMap<ModuleId,Module> modules = new ConcurrentHashMap<ModuleId,Module>();

    protected final Map<Integer,Repository> repositories = new TreeMap<Integer,Repository>();

    private final ConcurrentMap<Class<?>, CopyOnWriteArrayList<?>> runningServices = 
      new ConcurrentHashMap<Class<?>,CopyOnWriteArrayList<?>>();

    /**
     * Service provider class names and which modules they are in.
     *
     * <p>
     * This is used for the classloader punch-in hack &mdash; to work nicely
     * with classic service loader implementation, we need to be able to allow
     * any modules to see these classes.
     */
    protected final Map<String,Module> providers = new HashMap<String,Module>();

    private Map<ServiceLocator, String> habitats = new Hashtable<ServiceLocator, String>();

    Map<Module, Map<ServiceLocator, List<ActiveDescriptor>>> moduleDescriptors = new ConcurrentHashMap<Module, Map<ServiceLocator, List<ActiveDescriptor>>>();
    
    protected AbstractModulesRegistryImpl(ModulesRegistry parent) {
        this.parent = parent;
    }

    /**
     * Creates an uninitialized {@link ServiceLocator}
     *
     */
    @Override
    public ServiceLocator newServiceLocator() throws MultiException {
    	return newServiceLocator(null);
    }
    
    /**
     * Create a new ServiceLocator optionally providing a parent Services 
     */
    @Override
	public ServiceLocator newServiceLocator(ServiceLocator parent) throws MultiException {
        // We intentionally create an unnamed service locator, because the caller is going to
        // manage its lifecycle.
    	ServiceLocator serviceLocator =  ServiceLocatorFactory.getInstance().create(null, parent);
 
    	initializeServiceLocator(serviceLocator);
        return serviceLocator;
    }

    protected void initializeServiceLocator(ServiceLocator serviceLocator) throws MultiException {
        DynamicConfigurationService dcs = serviceLocator
                .getService(DynamicConfigurationService.class);

        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.bind(BuilderHelper.createConstantDescriptor(Logger.getAnonymousLogger()));

        // default modules registry is the one that created the habitat
        config.bind(BuilderHelper.createConstantDescriptor(this));

        ContextDuplicatePostProcessor processor = serviceLocator.getService(ContextDuplicatePostProcessor.class);

		if (processor == null) {
		    config.addActiveDescriptor(ContextDuplicatePostProcessor.class);
		}

        config.commit();
    }

    /**
     * Creates a {@link ServiceLocator} from all the modules in this registry
     *
     * @param name
     *      Determines which descriptors are loaded.
     * @param postProcessors
     */
     public void populateServiceLocator(String name, ServiceLocator serviceLocator, List<PopulatorPostProcessor> postProcessors) throws MultiException {
         try {
             for (final Module module : getModules()) { 
            	   // TODO: should get the inhabitantsParser out of Main instead since
                 // this could have been overridden
             	List<ActiveDescriptor> allDescriptors =
             	        parseInhabitants(module, name, serviceLocator, postProcessors);
             	if (allDescriptors == null) continue;
             	if (allDescriptors.isEmpty()) continue;
             	
             	Map<ServiceLocator, List<ActiveDescriptor>> descriptorByServiceLocator = moduleDescriptors.get(module);
             	if (descriptorByServiceLocator == null) {
             	    descriptorByServiceLocator = new HashMap<ServiceLocator, List<ActiveDescriptor>>();
             	    
             	   moduleDescriptors.put(module, descriptorByServiceLocator);
             	}
             	
             	List<ActiveDescriptor> foundDs = descriptorByServiceLocator.get(serviceLocator);
             	if (foundDs == null) {
             	    foundDs = new LinkedList<ActiveDescriptor>();
             	    
             	    descriptorByServiceLocator.put(serviceLocator, foundDs);
             	}
             	
             	foundDs.addAll(allDescriptors);
             }
         } catch (Exception e) {
             throw new MultiException(e);
         }
         // From now on, we will keep this service registry up-to-date with module system state
         habitats.put(serviceLocator, name);
     }

    @Override
	public void populateConfig(ServiceLocator serviceLocator) {
    	try {
        HK2Populator.populateConfig(serviceLocator);
    	} catch (BootException be) {
    		throw new MultiException(be);
    	}
    }

    public ServiceLocator createServiceLocator(ServiceLocator parent, String name, List<PopulatorPostProcessor> postProcessors) throws MultiException {
        ServiceLocator serviceLocator = newServiceLocator(parent);
        populateServiceLocator(name, serviceLocator, postProcessors);
        return serviceLocator;
    }

	public ServiceLocator createServiceLocator(String name) throws MultiException {
    	return createServiceLocator(null, name, null);
    }

	public ServiceLocator createServiceLocator() throws MultiException {
    	return createServiceLocator("default");
    }

    protected abstract List<ActiveDescriptor> parseInhabitants(Module module,
                                                               String name, ServiceLocator serviceLocator, List<PopulatorPostProcessor> postProcessors)
            throws IOException, BootException;

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
        // check that we don't already have this repository
        for (Repository repo : repositories.values()) {
            if (repo.getLocation().equals(repository.getLocation())) {
                throw new RuntimeException("repository at " + repository.getLocation() + " already registered");
            }
        }
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
     * @param version the module version.
     * @return the module instance or null if none can be found
     * @throws ResolveError if the module dependencies cannot be resolved
     */
    public Module makeModuleFor(String name, String version) throws ResolveError {
        return makeModuleFor(name, version, true);
    }

    public Module makeModuleFor(String name, String version, boolean resolve) throws ResolveError {
        Module module;
        Logger.getAnonymousLogger().fine("this.makeModuleFor("+name+ ", " +
                version + ", " + resolve + ") called.");
        if(parent!=null) {
            module = parent.makeModuleFor(name,version, resolve);
            if(module!=null)        return module;
        }
        
        module = modules.get(AbstractFactory.getInstance().createModuleId(name, version));
        if (module == null && version == null) {
            Collection<Module> matchingModules = getModules(name);
            if (!matchingModules.isEmpty()) {
                module = matchingModules.iterator().next();
            }
        }
        if (module==null) {
            module = loadFromRepository(name, version);
            if (module!=null) {
                add(module);
            }
        }
        if (module!=null && resolve) {
            try {
                module.resolve();
            } catch(Throwable e) {
                module.uninstall();
                throw new ResolveError(e);
            }
        }
        Logger.getAnonymousLogger().info("this.makeModuleFor("+name+ ", " + version + ") returned " + module);
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
    protected void add(Module newModule) {
    	//if (Utils.isLoggable(Level.INFO)) {
        //    Utils.getDefaultLogger().info("New module " + newModule);
        //}
        assert newModule.getRegistry()==this;
        // see if this module is already added. This can happen
        // in case of OSGiModulesRegistryImpl which uses SynchronousBundleListener.
        ModuleId id = AbstractFactory.getInstance().createModuleId(
                newModule.getModuleDefinition());
        if (modules.get(id) != null) return;
        modules.put(id, newModule);

        // pick up providers from this module
        for( ModuleMetadata.Entry spi : newModule.getMetadata().getEntries() ) {
            for( String name : spi.providerNames )
                providers.put(name,newModule);
        }
            
        for (Map.Entry<ServiceLocator, String> entry : habitats.entrySet()) {
            String name = entry.getValue();
            ServiceLocator serviceLocator = entry.getKey();
            try
            {
                parseInhabitants(newModule, name, serviceLocator, new ArrayList<PopulatorPostProcessor>());
            }
            catch (Exception e)
            {
                throw new RuntimeException("Not able to parse inhabitants information");
            }
          
        }
    }
    
    private void removeShutdownLocators() {
        for (Map<ServiceLocator, List<ActiveDescriptor>> descriptorsByServiceLocator : moduleDescriptors.values()) {
            Set<ServiceLocator> keys = new HashSet<ServiceLocator>(descriptorsByServiceLocator.keySet());
            
            for (ServiceLocator key : keys) {
                if (!ServiceLocatorState.SHUTDOWN.equals(key.getState())) continue;
                
                descriptorsByServiceLocator.remove(key);
                
                habitats.remove(key);
            }
        }
   
        
    }
    
    /**
     * Removes a module from the registry. The module will not be accessible 
     * from this registry after this method returns.
     */
	public void remove(Module module) {
		// if (Utils.isLoggable(Level.INFO)) {
		// Utils.getDefaultLogger().info("Removed module " + module);
		// }
		assert module.getRegistry() == this;
		modules.remove(AbstractFactory.getInstance().createModuleId(
				module.getModuleDefinition()));
		
		// Removes any shutdown locators before we operate on them
		removeShutdownLocators();

		// TODO: modules comes right back when getModules() is called.
		// the modeling is incorrect

		Map<ServiceLocator, List<ActiveDescriptor>> descriptorsByServiceLocator = moduleDescriptors
				.get(module);

		if (descriptorsByServiceLocator != null) {
			for (Entry<ServiceLocator, List<ActiveDescriptor>> e : descriptorsByServiceLocator
					.entrySet()) {
				ServiceLocator sl = e.getKey();
				if (!sl.getState().equals(ServiceLocatorState.RUNNING)) continue;
				
				List<ActiveDescriptor> descriptors = e.getValue();

				for (Descriptor descriptor : descriptors) {
					ServiceLocatorUtilities.removeOneDescriptor(sl, descriptor);
				}
			}
			moduleDescriptors.remove(module);
		}
	}
	
	protected Set<ServiceLocator> getAllServiceLocators() {
	    removeShutdownLocators();
	    
	    return Collections.unmodifiableSet(habitats.keySet());
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
    public Collection<Module> getModules() {

        // make a copy to avoid synchronizing since this API can be called while
        // modules are added or removed by other threads.
        Map<Integer, Repository> repos = new TreeMap<Integer, Repository>();
        repos.putAll(repositories);

        // force repository extraction
        Set<Integer> keys = repos.keySet();
        TreeSet<Integer> sortedKeys = new TreeSet<Integer>();
        sortedKeys.addAll(keys);
        for (Integer key : sortedKeys) {
            Repository repo = repos.get(key);
            for (ModuleDefinition moduleDef : repo.findAll()) {
                if (modules.get(AbstractFactory.getInstance().createModuleId(moduleDef)) == null) {
                    Module newModule = newModule(moduleDef);
                    if (newModule != null) {
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

    public Collection<Module> getModules(String moduleName)
    {
        List<Module> result = new ArrayList<Module>();
        for (Module m : getModules()) {
            if (m.getName().equals(moduleName)) result.add(m);
        }
        return result;
    }

    /**
     * Modules can notify their registry that they have changed (classes, 
     * resources,etc...). Registries are requested to take appropriate action
     * to make the new module available.
     */
    public void changed(Module service) {
        
  
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
        return add(info, true);
    }

    public Module add(ModuleDefinition info, boolean resolve) throws ResolveError {
        // it may have already been created
        Module service = makeModuleFor(info.getName(), info.getVersion(), resolve);
        if (service!=null) {
        //    Utils.getDefaultLogger().info("Service " + info.getName()
        //       + " already registered");
        } else {
            // create the service instance
            service = newModule(info);
            if (service != null){
                add(service);
            }
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

    @Override
    public Module getProvidingModule(String providerClassName) {
        return providers.get(providerClassName);
    }

    public void dumpState(PrintStream writer) {

        StringBuilder sb = new StringBuilder("Registry Info:: Total repositories: " + repositories.size()
                + ", Total modules = " + modules.size() + "\n");
        for (Repository repo : repositories.values()) {
            sb.append("Attached repository: [" + repo + "]\n");
        }
        for (Module module : getModules()) {
            sb.append("Registered Module: [" + module + "]\n");
        }
        writer.println(sb);
    }


}
