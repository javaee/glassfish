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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InjectionPoint;
import org.jvnet.tiger_types.Types;

/**
 * InjectInjectionResolver, handles all Inject annotations
 */
public class InjectInjectionResolver extends InjectionResolver<Inject> {

    public static final boolean MANAGED_ENABLED = Habitat.MANAGED_INJECTION_POINTS_ENABLED;
  
    final Habitat habitat;

    public InjectInjectionResolver(Habitat habitat) {
        super(Inject.class);
        this.habitat = habitat;
    }
  
    public boolean isOptional(AnnotatedElement element, Inject annotation) {
        return annotation.optional();
    }

    /**
     * Obtains the value to inject, based on the type and {@link Inject} annotation.
     */
    @Override
    public <V> V getValue(final Object component,
                final Inhabitant<?> onBehalfOf,
                final AnnotatedElement target,
                final Type genericType,
                final Class<V> type) throws ComponentException {
      final Inject inject = target.getAnnotation(Inject.class);
      final Callable<V> callable = new Callable<V>() {
        @Override
        public V call() throws ComponentException {
          V result;

          if (type.isArray()) {
            result = getArrayInjectValue(habitat, component, onBehalfOf, target, genericType, type);
          } else {
            if (Types.isSubClassOf(type, Holder.class)){
              result = getHolderInjectValue(habitat, component, onBehalfOf, target, genericType, type, inject);
            } else {
              if (habitat.isContract(type)) {
                  result = getServiceInjectValue(habitat, component, onBehalfOf, target, genericType, type, inject);
              } else {
                  result = getComponentInjectValue(habitat, component, onBehalfOf, target, genericType, type, inject);
              }
            }
          }
      
          return validate(component, onBehalfOf, result);
        }
      };

      try {
        if (habitat.isContextualFactoriesPresent()) {
          InjectionPoint ip = new InjectionPointImpl(component, target, type, inject, onBehalfOf);
          return Hk2ThreadContext.captureIPandRun(ip, callable);
        } else {
          return callable.call();
        }
      } catch (Exception e) {
        if (e instanceof ComponentException) throw ComponentException.class.cast(e);
        throw new ComponentException(e);
      }
    }

    protected <V> V getArrayInjectValue(Habitat habitat,
              Object component,
              Inhabitant<?> onBehalfOf,
              AnnotatedElement target,
              Type genericType,
              Class<V> type) {
        V result;
        Class<?> ct = type.getComponentType();
  
        Collection<?> instances;
        if (habitat.isContract(ct)) {
            instances = getAllByContract(onBehalfOf, habitat, ct);
        } else {
            instances = getAllByType(onBehalfOf, habitat, ct);
        }
        
        result = type.cast(instances.toArray((Object[]) Array.newInstance(ct, instances.size())));
        // TODO: validate() here too
        return result;
    }

    protected <V> V getHolderInjectValue(final Habitat habitat,
              final Object component,
              final Inhabitant<?> onBehalfOf,
              final AnnotatedElement target,
              final Type genericType,
              final Class<V> type,
              final Inject inject) throws ComponentException {
      final Type t = Types.getTypeArgument(((java.lang.reflect.Field) target).getGenericType(), 0);
      final Class<?> finalType = Types.erasure(t);

      if (habitat.isContract(finalType)) {
          final Inhabitant<?> i = manage(onBehalfOf, habitat.getInhabitant(finalType, inject.name()));
          return type.cast(i);
      }
      
      try {
          if (finalType.cast(component)!=null) {
              return type.cast(onBehalfOf);
          }
      } catch (ClassCastException e) {
          // ignore
      }
      
      V result = type.cast(getInhabitantByType(onBehalfOf, habitat, finalType));
      // TODO: validate() here too
      return result;
    }

    protected <V> V getServiceInjectValue(Habitat habitat,
              Object component,
              Inhabitant<?> onBehalfOf,
              AnnotatedElement target,
              Type genericType,
              Class<V> type,
              Inject inject) throws ComponentException {
      V result = null;
      Inhabitant<?> i = manage(onBehalfOf, habitat.getInhabitant(type, inject.name()));
      if (null != i) {
        Object service = i.get();
        try {
            result = type.cast(service);
        } catch (ClassCastException e) {
            Logger.getAnonymousLogger().severe("ClassCastException between contract " + type + " and service " + service);
            Logger.getAnonymousLogger().severe("Contract class loader " + type.getClassLoader());
            Logger.getAnonymousLogger().severe("Service class loader " + service.getClass().getClassLoader());
            i.release();
            throw e;
        }
      }
      return result;
    }

    @SuppressWarnings("unchecked")
    protected <V> V getComponentInjectValue(Habitat habitat,
              Object component,
              Inhabitant<?> onBehalfOf,
              AnnotatedElement target,
              Type genericType,
              Class<V> type,
              Inject inject) throws ComponentException {
        // ideally we should check if type has @Service or @Configured
      Inhabitant<?> i = manage(onBehalfOf, habitat.getInhabitantByType(type));
      if (null != i) {
        return (V)i.get();
      }
      return null;
    }

    /**
     * Verifies the injection does not violate any integrity rules.
     * 
     * @param component the target component to be injected
     * @param toBeInjected the injected value
     */
    protected <V> V validate(Object component, Inhabitant<?> onBehalfOf, V toBeInjected) {
      Inhabitants.validate(component, toBeInjected); // will toss exception if there is a problem
      return toBeInjected;
    }

    protected Inhabitant<?> manage(Inhabitant<?> onBehalfOf, Inhabitant<?> inhabitant) {
      if (null == inhabitant || null == onBehalfOf || !MANAGED_ENABLED) {
        return inhabitant;
      }
      Inhabitant<?> scopedClone = inhabitant.scopedClone();
      onBehalfOf.manage(scopedClone);
      return scopedClone;
    }

    @SuppressWarnings("unchecked")
    protected <V> Collection<V> manage(Inhabitant<?> onBehalfOf, Iterable<?> inhabitants) {
      if (null == inhabitants) {
        return null;
      }

      final ArrayList<V> managed = new ArrayList<V>();
      for (Object iObj : inhabitants) {
        Inhabitant<V> i = (Inhabitant<V>)iObj;
        
        if (MANAGED_ENABLED) {
          managed.add((V)manage(onBehalfOf, i).get());
        } else {
          managed.add(i.get());
        }
      }
      
      return managed;
    }
    
    protected Inhabitant<?> getInhabitantByType(Inhabitant<?> onBehalfOf, Habitat habitat, Class<?> finalType) {
//      return habitat.getInhabitantByType(finalType);
      return manage(onBehalfOf, habitat.getInhabitantByType(finalType));
    }

    protected <V> Collection<V> getInhabitants(Inhabitant<?> onBehalfOf, Habitat habitat, Class<?> finalType, String name) {
      return manage(onBehalfOf, habitat.getInhabitants(finalType, name));
    }
    
    @SuppressWarnings("unchecked")
    protected <V> Collection<V> getAllByType(Inhabitant<?> onBehalfOf, Habitat habitat, Class<V> ct) {
//      return habitat.getAllByType(ct);
      return (Collection<V>) manage(onBehalfOf, habitat.getAllInhabitantsByType(ct));
    }

    @SuppressWarnings("unchecked")
    protected <V> Collection<V> getAllByContract(Inhabitant<?> onBehalfOf, Habitat habitat, Class<V> ct) {
//      return habitat.getAllByContract(ct);
      return (Collection<V>) manage(onBehalfOf, habitat.getAllInhabitantsByContract(ct.getName()));
    }
    
}
