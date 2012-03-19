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

import org.glassfish.hk2.internal.ServiceLocatorFactoryImpl;

/**
 * This factory can be used to create new named ServiceLocators
 * 
 * @author jwells
 */
public abstract class ServiceLocatorFactory {
  private static ServiceLocatorFactory INSTANCE = new ServiceLocatorFactoryImpl();
  
  /**
   * This will return a factory that can be used
   * to create ServiceLocators.
   * 
   * @return The factory to use to create service locators
   */
  public static ServiceLocatorFactory getInstance() {
    return INSTANCE;
  }
  
  /**
   * Creates a ServiceLocator based on the users Module
   * which contains specific bindings and SPI implementations.
   * <p>
   * If there is already a ServiceLocator with the given
   * name then this method will return null.
   * 
   * @param name The name of this service locator.  May not be null
   * @param createFromThis The module that can be used to
   * configure this service locator
   * @param parent The parent of this ServiceLocator.  Services can
   * be found in the parent (and all grand-parents).
   * @return The newly created named ServiceLocator or null
   * if there was already a ServiceLocator with this name.
   */
  public abstract ServiceLocator create(String name,
          Module createFromThis,
          ServiceLocator parent);
  
  /**
   * Creates a ServiceLocator based on the users Module
   * which contains specific bindings and SPI implementations.
   * <p>
   * If there is already a ServiceLocator with the given
   * name then this method will return null.
   * 
   * @param name The name of this service locator.  May not be null
   * @param createFromThis The module that can be used to
   * configure this service locator
   * @return The newly created named ServiceLocator or null
   * if there was already a ServiceLocator with this name.
   */
  public abstract ServiceLocator create(String name, Module createFromThis);
  
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
   * @param name The name of the ServiceLocator to find
   * @return The ServiceLocator that was removed, or null if
   * the ServiceLocator with that name was not found
   */
  public abstract ServiceLocator destroy(String name);

}
