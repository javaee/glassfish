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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.Scope;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.Creator;
import org.jvnet.hk2.component.Creators;
import org.jvnet.hk2.component.internal.runlevel.RunLevelServices;

/**
 * Factory for Inhabitants.
 * 
 * @author Jeff Trent
 *
 * @since 3.1
 */
// TODO: Make all Inhabitant types package private (especially ctors)
// TODO: should probably use iface for InhabitantFactory instead of hardcoding them.
public class Inhabitants {

  private static final RunLevelServices runLevelServices = new RunLevelServices(); 
  
  @SuppressWarnings("unchecked")
  public static Inhabitant<?> createInhabitant(Habitat habitat,
      Holder<ClassLoader> classLoader,
      String typeName,
      MultiMap<String, String> metadata,
      Inhabitant<?> lead,
      Set<String> indicies) {
    AbstractInhabitantImpl<?> i = new LazyInhabitant(habitat, classLoader, typeName, metadata, lead);
    if (contains(indicies, RunLevel.class.getName())) {
      // this is a RunLevel service, we need to load type in order to get
      // more type information about it, namely it's environment and actual RunLevel id
      RunLevel rl = i.getAnnotation(RunLevel.class);
      assert(null != rl) : typeName + " is a problem; " + i + " has no RunLevel annotation";
      assert(!i.isInstantiated()) : "inhabitant should not be active: " + i;

      // get the appropriate RLS for this RunLevel
      RunLevelService<?> rls = runLevelServices.get(habitat, rl);
      InhabitantListener listener = InhabitantListener.class.isInstance(rls) ?
          InhabitantListener.class.cast(rls) : null;

      // wrap the inhabitant with a RunLevelInhabitant
      int runLevel = rl.value();
      
      // construct the runLevel inhabitant
      i = new RunLevelInhabitant(i, runLevel, rls.getState(), listener);
    }
    
    return i;
  }
  
  private static boolean contains(Set<String> indicies, String name) {
    if (null != indicies) {
      if (indicies.contains(name)) {
        return true;
      }
      
      // it could be a named inhabitant, in which case we need to iterate
      name += ":";
      for (String index : indicies) {
        if (index.startsWith(name)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Creates a singleton wrapper around existing object.
   */
  public static <T> Inhabitant<T> create(T instance) {
      return new ExistingSingletonInhabitant<T>(instance);
  }
  
  /**
   * Creates a {@link Inhabitant} by looking at annotations of the given type.
   */
  public static <T> Inhabitant<T> create(Class<T> c, Habitat habitat, MultiMap<String,String> metadata) {
      return wrapByScope(c, Creators.create(c,habitat,metadata), habitat);
  }

  /**
   * Creates a {@link Inhabitant} by wrapping {@link Creator} to handle scoping right.
   */
  public static <T> Inhabitant<T> wrapByScope(Class<T> c, Creator<T> creator, Habitat habitat) {
      Scoped scoped = c.getAnnotation(Scoped.class);
      if (scoped==null) {
          return new SingletonInhabitant<T>(creator); // treated as singleton
      }

      Class<? extends Scope> scopeClass = scoped.value();

      return wrapByScope(creator, habitat, scopeClass);
  }

  public static <T> Inhabitant<T> wrapByScope(Creator<T> creator, Habitat habitat,
      Class<? extends Scope> scopeClass) {
      // those two scopes are so common and different that they deserve
      // specialized code optimized for them.
      if (scopeClass== PerLookup.class) {
          return creator;
      }
      if (scopeClass== Singleton.class) {
          return new SingletonInhabitant<T>(creator);
      }
  
      // other general case
      Scope scope = habitat.getByType(scopeClass);
      if (scope==null) {
          throw new ComponentException("Failed to look up %s", scopeClass);
      }
  
      return new ScopedInhabitant<T>(creator,scope);
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
      return new ArrayList<String>(i.metadata().get(indexName));
  }

  /**
   * Performs basic validation of the injection.
   * 
   * @param target
   * @param injectedVal
   */
  static void validate(Object target, Object injectedVal) {
      if (null != injectedVal) {
          RunLevel targetRL = AbstractInhabitantImpl.getAnnotation(target.getClass(), RunLevel.class, false);
          RunLevel injectedValRL = AbstractInhabitantImpl.getAnnotation(injectedVal.getClass(), RunLevel.class, false);
          if (null == targetRL && null != injectedValRL) {
              throw new ComponentException("invalid dependency from a non-RunLevel instance " +
                  target + " to a RunLevel instance " + injectedVal);
        }
    }
  }

}
