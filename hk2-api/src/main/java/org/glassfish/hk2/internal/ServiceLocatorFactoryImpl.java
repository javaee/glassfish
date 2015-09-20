/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceConfigurationError;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.api.ServiceLocatorListener;
import org.glassfish.hk2.extension.ServiceLocatorGenerator;
import org.glassfish.hk2.osgiresourcelocator.ServiceLoader;
import org.glassfish.hk2.utilities.reflection.Logger;

/**
 * The implementation of the {@link ServiceLocatorFactory} that looks
 * in the OSGi service registry or the META-INF/services for the implementation
 * to use.  Failing those things, it uses the standard default locator
 * generator, which is found in auto-depends, which is the 99.9% case
 * 
 * @author jwells
 */
public class ServiceLocatorFactoryImpl extends ServiceLocatorFactory {
    private static final Object sLock = new Object();
    private static int name_count = 0;
    private static final String GENERATED_NAME_PREFIX = "__HK2_Generated_";
    
  private final ServiceLocatorGenerator defaultGenerator;
  private final Object lock = new Object();
  private final HashMap<String, ServiceLocator> serviceLocators = new HashMap<String, ServiceLocator>();
  private final HashSet<ServiceLocatorListener> listeners = new HashSet<ServiceLocatorListener>();

    /**
   * This will create a new set of name to locator mappings
   */
  public ServiceLocatorFactoryImpl() {
      defaultGenerator = AccessController.doPrivileged(new PrivilegedAction<ServiceLocatorGenerator>() {

        @Override
        public ServiceLocatorGenerator run() {
            try {
                return getGenerator();
            }
            catch (Throwable th) {
                Logger.getLogger().warning("Error finding implementation of hk2: " + th.getMessage());
                // th.printStackTrace();
                // Thread.dumpStack();
                return null;
            }
        }
          
      });
  }
  
  private static Iterable<? extends ServiceLocatorGenerator> getOSGiSafeGenerators() {
      try {
          return ServiceLoader.lookupProviderInstances(ServiceLocatorGenerator.class);
      }
      catch (Throwable th) {
          // The bundle providing ServiceLoader need not be on the classpath
          return null;
      }
  }
  
  private static ServiceLocatorGenerator getGenerator() {
      Iterable<? extends ServiceLocatorGenerator> generators = getOSGiSafeGenerators();
      if (generators != null) {
          // non-null indicates we are in OSGi environment
          // So, we will return whatever we find. If we don't find anything here, then we assume it is a
          // configuration error and return null.
          // Since org.glassfish.hk2.osgiresourcelocator.ServiceLoader never throws ServiceConfigurationError,
          // there is no need to catch it and try next item in the iterator.
          final Iterator<? extends ServiceLocatorGenerator> iterator = generators.iterator();
          return iterator.hasNext() ? iterator.next() : null;
      }
      
      // We are in non-OSGi environment, let's use JDK ServiceLoader instead.
      // Make sure we use our current loader to locate the service as opposed to some arbitrary TCL
      final ClassLoader classLoader = ServiceLocatorFactoryImpl.class.getClassLoader();
      Iterator<ServiceLocatorGenerator> providers = java.util.ServiceLoader.load(ServiceLocatorGenerator.class,
              classLoader).iterator();
      while (providers.hasNext()) {
          try {
              return providers.next();
          } catch (ServiceConfigurationError sce) {
              // This can happen. See the exception javadoc for more details.
              Logger.getLogger().debug("ServiceLocatorFactoryImpl", "getGenerator", sce);
                  // We will try the next one
          }
      }
      
      Logger.getLogger().warning("Cannot find a default implementation of the HK2 ServiceLocatorGenerator");
      return null;
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.api.ServiceLocatorFactory#create(java.lang.String, org.glassfish.hk2.api.Module)
   */
  @Override
  public ServiceLocator create(String name) {
      return create(name, null, null, CreatePolicy.RETURN);
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.api.ServiceLocatorFactory#find(java.lang.String)
   */
  @Override
  public ServiceLocator find(String name) {
    synchronized (lock) {
      return serviceLocators.get(name);
    }
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.api.ServiceLocatorFactory#destroy(java.lang.String)
   */
  @Override
  public void destroy(String name) {
      destroy(name, null);
  }
  
  private void destroy(String name, ServiceLocator locator) {
      ServiceLocator killMe = null;
    
      synchronized (lock) {
          if (name != null) {
              killMe = serviceLocators.remove(name);
          }
          
          if (killMe == null) {
              killMe = locator;
          }
          
          if (killMe != null) {
              for (ServiceLocatorListener listener : listeners) {
                  try {
                      listener.locatorDestroyed(killMe);
                  }
                  catch (Throwable th) {
                      Logger.getLogger().debug(getClass().getName(), "destroy " + listener, th);
                  }
              }
          }
      }
    
      if (killMe != null) {
          killMe.shutdown();
      }
  }
  
  public void destroy(ServiceLocator locator) {
      if (locator == null) return;
      
      destroy(locator.getName(), locator);
  }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocatorFactory#create(java.lang.String, org.glassfish.hk2.api.Module, org.glassfish.hk2.api.ServiceLocator)
     */
    @Override
    public ServiceLocator create(String name,
            ServiceLocator parent) {
        return create(name, parent, null, CreatePolicy.RETURN);
    }
    
    private static String getGeneratedName() {
        synchronized (sLock) {
            return GENERATED_NAME_PREFIX + name_count++;
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocatorFactory#create(java.lang.String, org.glassfish.hk2.api.ServiceLocator, org.glassfish.hk2.extension.ServiceLocatorGenerator)
     */
    @Override
    public ServiceLocator create(String name, ServiceLocator parent,
            ServiceLocatorGenerator generator) {
 
        return create(name, parent, generator, CreatePolicy.RETURN);
    }
    
    private void callListenerAdded(ServiceLocator added) {
        for (ServiceLocatorListener listener : listeners) {
            try {
                listener.locatorAdded(added);
            }
            catch (Throwable th) {
                Logger.getLogger().debug(getClass().getName(), "create " + listener, th);
            }
        }
    }
    
    @Override
    public ServiceLocator create(String name, ServiceLocator parent,
            ServiceLocatorGenerator generator, CreatePolicy policy) {
        synchronized (lock) {
            ServiceLocator retVal;

            if (name == null) {
                name = getGeneratedName();
                ServiceLocator added = internalCreate(name, parent, generator);
                callListenerAdded(added);
                return added;
            }

            retVal = serviceLocators.get(name);
            if (retVal != null) {
                if (policy == null || CreatePolicy.RETURN.equals(policy)) {
                    return retVal;
                }
                
                if (policy.equals(CreatePolicy.DESTROY)) {
                    destroy(retVal);
                }
                else {
                    throw new IllegalStateException(
                            "A ServiceLocator named " + name + " already exists");
                }
            }
            retVal = internalCreate(name, parent, generator);
            serviceLocators.put(name, retVal);
            
            callListenerAdded(retVal);

            return retVal;
        }
    }

    private ServiceLocator internalCreate(String name, ServiceLocator parent, ServiceLocatorGenerator generator) {
        if (generator == null) {
            if (defaultGenerator == null) {
                throw new IllegalStateException("No generator was provided and there is no default generator registered");
            }
            generator = defaultGenerator;
        }
        return generator.create(name, parent);
    }

    @Override
    public void addListener(ServiceLocatorListener listener) {
        if (listener == null) throw new IllegalArgumentException();
        
        synchronized (lock) {
            if (listeners.contains(listener)) return;
            
            try {
                HashSet<ServiceLocator> currentLocators = new HashSet<ServiceLocator>(serviceLocators.values());
                listener.initialize(Collections.unmodifiableSet(currentLocators));
            }
            catch (Throwable th) {
                // Not added to the set of listeners
                Logger.getLogger().debug(getClass().getName(), "addListener " + listener, th);
                return;
            }
            
            listeners.add(listener);
        }
        
    }

    @Override
    public void removeListener(ServiceLocatorListener listener) {
        if (listener == null) throw new IllegalArgumentException();
        
        synchronized (lock) {
            listeners.remove(listener);
        }
        
    }

    

}
