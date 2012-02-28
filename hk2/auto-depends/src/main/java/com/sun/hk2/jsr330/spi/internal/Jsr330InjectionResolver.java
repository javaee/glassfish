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
package com.sun.hk2.jsr330.spi.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;

import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Constants;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.tiger_types.Types;

import com.sun.hk2.component.InjectionResolver;
import com.sun.hk2.jsr330.spi.BasicProviderImpl;

/**
 * Jsr-330 Injection Resolver
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
@SuppressWarnings("unchecked")
public class Jsr330InjectionResolver extends
    InjectionResolver<javax.inject.Inject> {

  @Inject
  private Habitat habitat;

  public Jsr330InjectionResolver() {
    super(javax.inject.Inject.class);
  }

  public Jsr330InjectionResolver(Habitat h) {
    super(javax.inject.Inject.class);
    this.habitat = h;
  }

    @Override
    public <V> V getValue(Object component, Inhabitant<?> onBehalfOf,
            AnnotatedElement target, Type gtype, Class<V> type)
            throws ComponentException {
        Object result;
        if (type.isArray()) {
            result = getValueForInjectedArray(onBehalfOf, gtype, type);
        }
        else if (Types.isSubClassOf(type, Provider.class)) {
            gtype = getGenericType(gtype, target);
            result = getHolderInjectValue(habitat, onBehalfOf, target,gtype);
        }
        else {
            result = get(habitat, onBehalfOf, target, type);
        }

        return (V) result;
    }

    private <V> Object getValueForInjectedArray(Inhabitant<?> onBehalfOf,
            Type gtype, Class<V> type) {
        Object result;
        Class<V> baseType = (Class<V>) type.getComponentType();
        
        if (Types.isSubClassOf(baseType, Provider.class)) {

            if (!GenericArrayType.class.isInstance(gtype)) {
                throw new ComponentException("unknown type: "
                        + gtype.toString());
            }

            Type baseComponentType = ((GenericArrayType) gtype)
                    .getGenericComponentType();
            if (null == baseComponentType) {
                throw new ComponentException(gtype.toString() + " on "
                        + onBehalfOf);
            }
            if (!ParameterizedType.class.isInstance(baseComponentType)) {
                throw new ComponentException("unknown type: "
                        + baseComponentType.toString());
            }

            Type[] types = ((ParameterizedType) baseComponentType)
                    .getActualTypeArguments();
            if (null == types || 1 != types.length) {
                throw new ComponentException(baseComponentType.toString()
                        + " on " + onBehalfOf);
            }

            Class<V> paramClass = Types.erasure(types[0]);
            Collection<Inhabitant<? extends V>> answer = habitat
                    .getInhabitants(paramClass);

            Collection providers = new ArrayList();
            for (Inhabitant<? extends V> inh : answer) {
                providers.add(new BasicProviderImpl(inh));
            }

            result = providers.toArray(new Provider[0]);
        }
        else { // Not a Provider.
            Collection<V> answer = (habitat.isContract(baseType)) ? habitat
                    .getAllByContract(baseType) : habitat
                    .getAllByType(baseType);

            result = type.cast(answer.toArray((V[]) Array.newInstance(
                    type.getComponentType(), answer.size())));
        }
        return result;
    }

  protected Type getGenericType(Type gtype, AnnotatedElement target) {
    if (null != gtype) {
      return gtype;
    }
    
    if (target instanceof Field) {
      return ((Field)target).getGenericType();
    } else if (target instanceof Method) {
      Type gpTypes[] = ((Method)target).getGenericParameterTypes();
      if (1 == gpTypes.length) {
        return gpTypes[0];
      }
    }
    
    throw new ComponentException("unknown type: " + target);
  }

  protected static <V> Provider<V> getHolderInjectValue(
      Habitat habitat,
      Object onBehalfOf,
      AnnotatedElement target, Type paramType) throws ComponentException {
    if (!ParameterizedType.class.isInstance(paramType)) {
      throw new ComponentException("unknown type: " + paramType.toString());
    }

    Type [] types = ((ParameterizedType)paramType).getActualTypeArguments();
    if (null == types || 1 != types.length) {
      throw new ComponentException(paramType.toString() + " on " + onBehalfOf);
    }
 
    Named named = target != null ? target.getAnnotation(Named.class) : null;
    String name = (null == named) ? null : named.value();
 
    
    Class<V> paramClass = Types.erasure(types[0]);
    final Inhabitant<V> inhabitant = getInhabitant(habitat, onBehalfOf, paramClass, name);
    
    return new BasicProviderImpl(inhabitant);
  }

    protected static <V> Inhabitant<V> getInhabitant(Habitat habitat,
            Object onBehalfOf, Class<V> type) {
        return getInhabitant(habitat, onBehalfOf, type, null);
    }
  
  protected static <V> Inhabitant<V> getInhabitant(
      Habitat habitat,
      Object onBehalfOf,
      Class<V> type, String name) {
    Inhabitant<V> result = habitat.getInhabitant(type, name);
    if (null == result) {
      result = habitat.getInhabitantByType(type);
    }
    
    return result;
  }

  protected static <V> V get(
      Habitat habitat,
      Object onBehalfOf,
      AnnotatedElement target,
      Class<V> type) {
    Annotation annotations[] = qualifiers(target.getAnnotations());
    
    Named named = target.getAnnotation(Named.class);
    String name = (null == named) ? null : named.value();
    
    V result = null;
    
    if (null != name) {
      result = getServiceInjectValue(habitat, type, name);
    }
    
    if (null == result) {
      if (null != annotations) {
        result = getServiceInjectValue(habitat, type, annotations);
      }
    }
    
    if (null == result) {
      result = getComponentInjectValue(habitat, type);
    }

    if (null == result) {
      if (null == name && null == annotations) {
        result = getServiceInjectValue(habitat, type, (String)null);
      }
    }
    
    return result;
  }

  private static Annotation[] qualifiers(Annotation[] annotations) {
    if (null == annotations || 0 == annotations.length) {
      return null;
    }
    
    ArrayList<Annotation> result = null;
    for (Annotation ann : annotations) {
      if (null != ann.annotationType().getAnnotation(Qualifier.class)) {
        if (null == result) {
          result = new ArrayList<Annotation>();
        }
        result.add(ann);
      }
    }
    
    return (null == result) ? null : result.toArray(annotations);
  }

  protected static <V> V getServiceInjectValue(Habitat habitat,
        Class<V> type,
        String name) throws ComponentException {
    V result = habitat.getComponent(type, name);
    return result;
  }

  protected static <V> V getServiceInjectValue(Habitat habitat,
      Class<V> type,
      Annotation[] annotations) throws ComponentException {
    Collection<Inhabitant<?>> candidates = habitat.getInhabitantsByContract(type.getName());
    for (Inhabitant<?> candidate : candidates) {
      if (matched(annotations, candidate.metadata())) {
        return (V) candidate.get();
      }
    }
    return null;
  }
  
  protected static boolean matched(Annotation[] annotations,
      MultiMap<String, String> candidateMd) {
    for (Annotation ann : annotations) {
      if (null != ann) {
        String name = ann.annotationType().getName();
        if (!candidateMd.contains(Constants.QUALIFIER, name)) {
          return false;
        }
      }
    }
    return true;
  }

  protected static <V> V getComponentInjectValue(
      Habitat habitat,
      Class<V> type) throws ComponentException {
    V result = habitat.getByType(type);
    return result;
  }

  @Override
  public boolean isOptional(AnnotatedElement annotated, Inject annotation) {
    return annotated.isAnnotationPresent(Optional.class);
  }

}
