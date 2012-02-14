/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.internal;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.glassfish.hk2.TypeLiteral;
import org.glassfish.hk2.api.Configurator;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorFilter;
import org.glassfish.hk2.api.OrFilter;
import org.glassfish.hk2.api.ExtendedProvider;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Scope;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeListener;
import org.glassfish.hk2.api.scopes.PerLookup;
import org.glassfish.hk2.api.scopes.Singleton;
import org.glassfish.hk2.internal.DescriptorImpl;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * @author jwells
 *
 */
@SuppressWarnings("unchecked")
public class ServiceLocatorImpl implements ServiceLocator, Configurator {
	private final Object lock = new Object();
	private final HashMap<Long, LocatorData> completeBindingMap = new HashMap<Long, LocatorData>();
	private final HashMap<String, List<LocatorData>> byContractOrImpl = new HashMap<String, List<LocatorData>>();
	
	private final String name;
	
	/* package */ ServiceLocatorImpl(String name) {
	  this.name = name;
	}
	
	private static <T> ExtendedProvider<T> getBestProvider(List<ExtendedProvider<T>> providerList) {
		if (providerList.isEmpty()) return null;
		
		return providerList.get(0);
	}
	
	private static <T> T getBestService(List<ExtendedProvider<T>> providerList) {
		ExtendedProvider<T> bestProvider = getBestProvider(providerList);
		if (bestProvider == null) return null;
		
		return bestProvider.get();
	}
	
	private static <T> List<T> convertProvidersToServices(List<ExtendedProvider<T>> providerList) {
		LinkedList<T> retVal = new LinkedList<T>();
		
		for (ExtendedProvider<T> provider : providerList) {
			retVal.add(provider.get());
		}
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getService(java.lang.Class)
	 */
	
	@Override
	public <T> T getService(Class<T> contractOrImpl) {
		return (T) getBestService(
				getAllServiceProviders(
						BuilderHelper.link().withContract(contractOrImpl).build()));
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getAllServices(java.lang.Class)
	 */
	@Override
	public <T> List<T> getAllServices(Class<T> contractOrImpl) {
		return (List<T>) convertProvidersToServices(
				getAllServiceProviders(
						BuilderHelper.link().withContract(contractOrImpl).build()));
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getService(java.lang.Class, java.lang.String)
	 */
	@Override
	public <T> T getService(Class<T> contractOrImpl, String name) {
		return (T) getBestService(
				getAllServiceProviders(
						BuilderHelper.link().withContract(contractOrImpl).named(name).build()));
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getService(java.lang.String)
	 */
	@Override
	public <T> T getService(String contractOrImpl) {
		return (T) getBestService(
				getAllServiceProviders(
						BuilderHelper.link().withContract(contractOrImpl).build()));
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getAllServices(java.lang.String)
	 */
	@Override
	public <T> List<T> getAllServices(String contractOrImpl) {
		return (List<T>) convertProvidersToServices(
				getAllServiceProviders(
						BuilderHelper.link().withContract(contractOrImpl).build()));
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getService(java.lang.String, java.lang.String)
	 */
	@Override
	public <T> T getService(String contractOrImpl, String name) {
		return (T) getBestService(
				getAllServiceProviders(
						BuilderHelper.link().withContract(contractOrImpl).named(name).build()));
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getService(org.glassfish.hk2.Filter)
	 */
	@Override
	public <T> T getService(Filter<Descriptor> searchCriteria) {
		return (T) getBestService(
				getAllServiceProviders(searchCriteria));
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getAllServices(org.glassfish.hk2.Filter)
	 */
	@Override
	public <T> List<T> getAllServices(Filter<Descriptor> searchCriteria) {
		return (List<T>) convertProvidersToServices(
				getAllServiceProviders(searchCriteria));
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getServiceProvider(org.glassfish.hk2.Filter)
	 */
	@Override
	public <T> ExtendedProvider<T> getServiceProvider(Filter<Descriptor> searchCriteria) {
		return (ExtendedProvider<T>) getBestProvider(getAllServiceProviders(searchCriteria));
	}
	
	private <T> List<ExtendedProvider<T>> getAllServiceProvidersByDescriptorFilter(
			DescriptorFilter filter) {
		LinkedList<String> fastKeys = new LinkedList<String>();
		fastKeys.addAll(filter.getImplementations());
		fastKeys.addAll(filter.getContracts());
		
		if (fastKeys.isEmpty()) {
			return noShortcutSearch(filter);
		}
		
		synchronized (lock) {
			LinkedList<LocatorData> currentKnownList = null;
			
			for (String key : fastKeys) {
				List<LocatorData> foundList = byContractOrImpl.get(key);
				
				if (foundList == null) {
					// Nothing found, nothing found
					return new LinkedList<ExtendedProvider<T>>();
				}
				
				if (currentKnownList == null) {
			    currentKnownList = new LinkedList<LocatorData>();
						
				  for (LocatorData binding : foundList) {
				    Descriptor bindingDescriptor = binding.getDescriptor();
						if (filter.matches(bindingDescriptor)) {
							currentKnownList.add(binding);
						}
					}
				}
				else {
				    List<LocatorData> anotherList = new LinkedList<LocatorData>();
				    
				    for (LocatorData binding : foundList) {
						  if (filter.matches(binding.getDescriptor())) {
							  anotherList.add(binding);
						  }
					  }
				    
				    currentKnownList.retainAll(anotherList);
				}
			}
			
			List<ExtendedProvider<T>> retVal = new LinkedList<ExtendedProvider<T>>();
			for (LocatorData addMe : currentKnownList) {
				// TODO:  How does context work on getProvider?
			  ExtendedProvider<T> toAdd = (ExtendedProvider<T>) addMe.getProvider();
				retVal.add(toAdd);
			}
			
			return retVal;
		}
		
	}
	
	private <T> List<ExtendedProvider<T>> getAllServiceProvidersByDescriptorOrFilter(
			OrFilter<Descriptor> filter) {
		LinkedList<ExtendedProvider<T>> retVal = new LinkedList<ExtendedProvider<T>>();
		
		synchronized(lock) {
			for (Filter<Descriptor> dFilter : filter.getFilters()) {
			  List<ExtendedProvider<T>> orResults = null;
			  if (dFilter instanceof DescriptorFilter) {
				  orResults = getAllServiceProvidersByDescriptorFilter(
				      (DescriptorFilter) dFilter);
			  }
			  else {
			    orResults = noShortcutSearch(dFilter);
			  }
			  
				for (ExtendedProvider<T> orResult : orResults) {
					retVal.add(orResult);
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * @param searchCriteria
	 * @return
	 */
	private <T> List<ExtendedProvider<T>> noShortcutSearch(Filter<Descriptor> searchCriteria) {
		LinkedList<ExtendedProvider<T>> retVal = new LinkedList<ExtendedProvider<T>>();
		synchronized (lock) {
			for (LocatorData binding : completeBindingMap.values()) {
				if (searchCriteria.matches(binding.getDescriptor())) {
					// TODO:  Figure out how to pass in a context here???
					retVal.add((ExtendedProvider<T>) binding.getProvider());
				}
			}
			
		}
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.ServiceLocator#getAllServiceProviders(org.glassfish.hk2.Filter)
	 * 
	 * Note this is the method that does all of the heavy lifting
	 */
	@Override
	public <T> List<ExtendedProvider<T>> getAllServiceProviders(
			Filter<Descriptor> searchCriteria) {
		if (searchCriteria == null) throw new IllegalArgumentException();
		
		if (searchCriteria instanceof DescriptorFilter) {
			return getAllServiceProvidersByDescriptorFilter((DescriptorFilter) searchCriteria);
		}
		
		if (searchCriteria instanceof OrFilter) {
			return getAllServiceProvidersByDescriptorOrFilter((OrFilter<Descriptor>) searchCriteria);
		}
		
		// bummer, linear search through the entire DB
		return noShortcutSearch(searchCriteria);
	}
	
	/* (non-Javadoc)
   * @see org.glassfish.hk2.api.Configurator#bind(org.glassfish.hk2.api.Descriptor, java.lang.Object)
   */
  @Override
  public Descriptor bind(Descriptor keys, Object instance) {
    if (keys == null || instance == null) {
      throw new IllegalArgumentException();
    }
    
    String descriptorScope = Utilities.getFirstElement(keys.getScopes());
    if (descriptorScope != null) {
      if (!descriptorScope.equals(Singleton.class.getName())) {
        throw new IllegalArgumentException();
      }
    }
    
    String theImplClass = Utilities.getFirstElement(keys.getImplementations());
    if (theImplClass != null) {
      if (!theImplClass.equals(instance.getClass().getName())) {
        throw new IllegalArgumentException();
      }
    }
    
    Set<String> impls = new HashSet<String>();
    impls.add(instance.getClass().getName());
    
    Set<String> scopes = new HashSet<String>();
    scopes.add(Singleton.class.getName());
    
    keys = new DescriptorImpl(
        keys.getContracts(),
        keys.getNames(),
        scopes,
        impls,
        keys.getMetadata(),
        keys.getQualifiers(),
        null);
    
    return internalBind(keys, instance);
  }
  
  @Override
  public Descriptor bind(Descriptor keys) {
    return internalBind(keys, null);
  }
  
  private static Set<String> defaultScopes(Set<String> keyScopes) {
    String theScope = Utilities.getFirstElement(keyScopes);
    if (theScope != null) return keyScopes;
    
    HashSet<String> retVal = new HashSet<String>();
    retVal.add(PerLookup.class.getName());
    return retVal;
  }

	private Descriptor internalBind(Descriptor keys, Object instance) {
		if (keys == null || keys.getImplementations().isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		Long majorKey = SystemIDs.getSystemIds().getNextId();
		
		Descriptor systemKey = new DescriptorImpl(
		    keys.getContracts(),
		    keys.getNames(),
		    defaultScopes(keys.getScopes()),
		    keys.getImplementations(),
		    keys.getMetadata(),
		    keys.getQualifiers(),
		    majorKey);
		
		LocatorData ld = new LocatorData();
		ld.setDescriptor(systemKey);
		ld.setProvider(new ExtendedProviderImpl<Object>(systemKey, instance));
		
		LinkedList<String> implOrContractList = new LinkedList<String>(systemKey.getImplementations());
    implOrContractList.addAll(systemKey.getContracts());
		
		synchronized (lock) {
		  completeBindingMap.put(majorKey, ld);
		  
		  for (String implementation : implOrContractList) {
		    List<LocatorData> byImplList = byContractOrImpl.get(implementation);
		    if (byImplList == null) {
		      byImplList = new LinkedList<LocatorData>();
		      byContractOrImpl.put(implementation, byImplList);
		    }
		    
		    byImplList.add(ld);
		  }
		}
		
		return systemKey;
	}

	@Override
	public List<Descriptor> unbind(Filter<Descriptor> key) {
		// TODO Auto-generated method stub
		return null;
	}

  /* (non-Javadoc)
   * @see org.glassfish.hk2.api.ServiceLocator#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.api.ServiceLocator#shutdown()
   */
  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.api.Configurator#bindScope(org.glassfish.hk2.api.Scope)
   */
  @Override
  public void bindScope(Class<? extends Annotation> scopeAnno, Scope scope) {
    if (scope == null) throw new IllegalArgumentException();
    
    // In the implemenation over hk2 the scopeAnno gets sadly lost
    Descriptor d = BuilderHelper.link(scope.getClass()).
        withContract(Scope.class).
        in(Singleton.class).
        build();
    
    bind(d, scope);
  }

/* (non-Javadoc)
 * @see org.glassfish.hk2.api.Configurator#bindListener(org.glassfish.hk2.api.Filter, org.glassfish.hk2.api.TypeListener)
 */
@Override
public void bindListener(Filter<? super TypeLiteral<?>> matcher,
        TypeListener listener) {
    // TODO Auto-generated method stub
    
}
}
