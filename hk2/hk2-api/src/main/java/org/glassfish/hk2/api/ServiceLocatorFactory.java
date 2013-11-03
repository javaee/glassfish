/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import org.glassfish.hk2.extension.ServiceLocatorGenerator;
import org.glassfish.hk2.internal.ServiceLocatorFactoryImpl;

/**
 * This factory can be used to create new named ServiceLocators
 * 
 * @author jwells
 */
public abstract class ServiceLocatorFactory {
  private static ServiceLocatorFactory INSTANCE = new ServiceLocatorFactoryImpl();
  
  /**
   * This will return a factory where the ServiceLocatorGenerator
   * is discovered from the META-INF/services of the process
   * 
   * @return The factory to use to create service locators
   */
  public static ServiceLocatorFactory getInstance() {
    return INSTANCE;
  }
  
  /**
   * Creates (or finds) a ServiceLocator.
   * <p>
   * If there is already a ServiceLocator with the given
   * name then this method will return that locator.
   * 
   * @param name The name of this service locator.  Passing a null
   * name will result in a newly created service locator with a
   * generated name and that will not be tracked by the system
   * @return The created or found named ServiceLocator
   */
  public abstract ServiceLocator create(String name);
  
  /**
   * Creates or finds a ServiceLocator.
   * <p>
   * If there is already a ServiceLocator with the given
   * name then this method will return that ServiceLocator.  The
   * parent argument will be ignored in that case
   * 
   * @param name The name of this service locator.  Passing a null
   * name will result in a newly created service locator with a
   * generated name and that will not be tracked by the system
   * @param parent The parent of this ServiceLocator.  Services can
   * be found in the parent (and all grand-parents).  May be null
   * if the returned ServiceLocator should not be parented
   * @return The created or found named ServiceLocator
   */
  public abstract ServiceLocator create(String name,
          ServiceLocator parent);
  
  /**
   * Creates or finds a ServiceLocator.
   * <p>
   * If there is already a ServiceLocator with the given
   * name then this method will return that ServiceLocator.  The
   * parent argument will be ignored in that case.
   * If a null name is given then a new ServiceLocator with a
   * generated name will be returned.
   * 
   * @param name The name of this service locator.  Passing a null
   * name will result in a newly created service locator with a
   * generated name and that will not be tracked by the system
   * @param parent The parent of this ServiceLocator.  Services can
   * be found in the parent (and all grand-parents).  May be null
   * if the returned ServiceLocator should not be parented
   * @param generator An implementation of the generator interface that
   * can be used to provide an implementation of ServiceLocator.  If
   * null then the generator used will be discovered from the OSGi
   * service registry or from META-INF/services
   * @return The created or found named ServiceLocator
   */
  public abstract ServiceLocator create(String name,
          ServiceLocator parent,
          ServiceLocatorGenerator generator);
  
  /**
   * Finds the ServiceLocator with this name
   * 
   * @param name May not be null, is the name of the ServiceLocator to find
   * @return The ServiceLocator with the given name, or null if there
   *   is no ServiceLocator with that name
   */
  public abstract ServiceLocator find(String name);
  
  /**
   * Removes the ServiceLocator with this name
   * <p>
   * All services associated with this ServiceLocator will be shutdown
   * 
   * @param name The name of the ServiceLocator to destroy
   */
  public abstract void destroy(String name);
  
  /**
   * Removes the given ServiceLocator
   * <p>
   * All services associated with this ServiceLocator will be shutdown
   * 
   * @param locator The ServiceLocator to destroy.  If null this will do nothing.
   * If the ServiceLocator given was already destroyed this will do nothing
   */
  public abstract void destroy(ServiceLocator locator);
  
  /**
   * Adds a service listener to the unordered set of listeners that
   * will be notified when named listeners are added or removed
   * from the system.  If this listener is already registered
   * this method does nothing
   * 
   * @param listener The non-null listener to add to the system
   */
  public abstract void addListener(ServiceLocatorListener listener);
  
  /**
   * Removes a service listener from the set of listeners that
   * are notified when named listeners are added or removed
   * from the system
   * 
   * @param listener The non-null listener to remove from the system
   */
  public abstract void removeListener(ServiceLocatorListener listener);

}
