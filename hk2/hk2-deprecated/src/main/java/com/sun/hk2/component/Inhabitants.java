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
package com.sun.hk2.component;

import org.glassfish.hk2.Scope;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.deprecated.internal.HolderHK2LoaderImpl;

import java.util.*;

/**
 * Factory for Inhabitants.
 * 
 * @author Jeff Trent
 */
// TODO: Make all Inhabitant types package private (especially ctors)
@Deprecated
public class Inhabitants {

  /**
   * @deprecated
   */
  @SuppressWarnings("unchecked")
  public static Inhabitant<?> createInhabitant(ServiceLocator serviceLocator,
      Holder<ClassLoader> classLoader,
      String typeName,
      Map<String, List<String>> metadata,
      Set<String> indicies) {
    return createInhabitant(serviceLocator,
        classLoader, typeName, metadata, null, indicies);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Inhabitant<?> createInhabitant(ServiceLocator serviceLocator,
      Holder<ClassLoader> classLoader,
      String typeName,
      Map<String, List<String>> metadata,
      InhabitantStore store,
      Set<String> indicies) {
    AbstractInhabitantImpl<?> i = new LazyInhabitant(serviceLocator, new HolderHK2LoaderImpl(classLoader), typeName, metadata);
    return i;
  }
  
  /**
   * Creates a singleton wrapper around existing object.
   */
  public static <T> Inhabitant<T> create(T instance) {
      return null; //TODO [Check]  ==> new ExistingSingletonInhabitant<T>(instance);
  }
  
  /**
   * Creates a {@link org.jvnet.hk2.component.Inhabitant} by looking at annotations of the given type.
   */
  public static <T> Inhabitant<T> create(Class<T> c, ServiceLocator habitat, Map<String, List<String>> metadata) {
      return wrapByScope(c, Creators.create(c,habitat,metadata), habitat);
  }

  /**
   * Creates a {@link org.jvnet.hk2.component.Inhabitant} by wrapping {@link org.jvnet.hk2.component.Creator} to handle scoping right.
   */
  public static <T> Inhabitant<T> wrapByScope(Class<T> c, Creator<T> creator, ServiceLocator habitat) {
	  throw new UnsupportedOperationException("wrapByScope in Inhabitants");
  }

  public static <T> Inhabitant<T> wrapByScope(Creator<T> creator, ServiceLocator habitat,
      Class<? extends Scope> scopeClass) {
      // those two scopes are so common and different that they deserve
      // specialized code optimized for them.

/* TODO [Check] ==>
      if (scopeClass== PerLookup.class
              || scopeClass==org.jvnet.hk2.component.PerLookup.class) {
          return creator;
      }
      if (scopeClass==null || scopeClass== Singleton.class
              || scopeClass==org.jvnet.hk2.component.Singleton.class) {
          return new SingletonInhabitant<T>(creator);
      }
*/
      // other general case
      Scope scope = habitat.getService(scopeClass);
      if (scope==null) {
          throw new ComponentException("Failed to look up %s", scopeClass);
      }

      return null; //TODO [Check] ==> new ScopedInhabitant<T>(creator,scope);
  }

  /**
   * Returns the list of names the service implementation in known. Services in hk2 are
   * indexed by the contract name and an optional name. There can also be some aliasing
   * so the same service can be known under different names.
   *
   * @param i instance of inhabitant to obtain its registration name
   * @param indexName the contract name this service is implementing
   * @param <T> contract type, optional
   * @return a collection of names (usually there is only one) under which this service
   * is registered for the passed contract name
   */
  // TODO: Does this really belong here?
  public static <T> Collection<String> getNamesFor(Inhabitant<T> i, String indexName) {
    Map<String, List<String>> metadata = i.getMetadata();
	List<String> list = null;
	
	if (metadata != null) {
		list = metadata.get(indexName);
	}
	
	if (list == null) {
		list = new ArrayList<String>();
	}
	
	return new ArrayList<String>(list);
  }

    /**
     * Performs basic validation of the injection.
     *
     * @param target       the target of the injection
     * @param injectedVal  the value being injected
     *
     * @throws org.jvnet.hk2.component.ComponentException if the injection does not pass validation
     */
    static void validate(Object target, Object injectedVal) throws ComponentException {
        // not currently used
    }
}
