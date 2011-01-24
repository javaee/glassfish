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

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InjectionManager;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.tiger_types.Types;

import com.sun.hk2.component.ConstructorWomb;

/**
 * Jsr30 requires injection resolution for ctor(s)
 * 
 * @author Jeff Trent
 *
 * @since 3.1
 */
@SuppressWarnings("unchecked")
public class Jsr330ConstructorWomb<T> extends ConstructorWomb<T> {

  public Jsr330ConstructorWomb(
      Class<T> type,
      Habitat habitat,
      MultiMap<String, String> metadata) {
    super(type, habitat, metadata);
  }

  @Override
  protected InjectionManager createInjectionManager() {
    return new Jsr330InjectionManager();
  }


  /**
   * First attempts to newInstance() as normal, then fails over to looking for @Inject ctor(s)
   */
  @Override
  public T create(Inhabitant onBehalfOf) throws ComponentException {
    try {
      return type.newInstance();
    } catch (IllegalAccessException e) {
      return createFromInjectedCtor();
    } catch (InstantiationException e) {
      return createFromInjectedCtor();
    } catch (Exception e) {
      throw new ComponentException("Failed to create " + type, e);
    }
  }

  protected T createFromInjectedCtor() {
    List<Object> params = null;
    try {
      Constructor<T> ctor = getConstructor();
      ctor.setAccessible(true);

      params = getParameters(ctor);
      return ctor.newInstance(params.toArray());
    } catch (Exception e) {
      e.printStackTrace();
      throw new ComponentException("Failed to create " + type, e);
    }
  }

  /**
   * Determine the constructor to use.
   */
  protected Constructor<T> getConstructor() throws Exception {
    for (Constructor ctor : type.getDeclaredConstructors()) {
      Inject inject = (Inject) ctor.getAnnotation(Inject.class);
      if (null != inject) {
        return ctor;
      }
    }
    
    Constructor ctor = type.getDeclaredConstructor((Class<?>[])null);
    if (null != ctor) {
      return ctor;
    }
    
    throw new ComponentException("no appropriate constructor");
  }

  /**
   * Resolve the parameters for the selected ctor
   */
  protected List<Object> getParameters(Constructor<T> ctor) {
    ArrayList<Object> params = new ArrayList<Object>();
    
    for (Type paramType : ctor.getGenericParameterTypes()) {
      Class<?> paramClass = Types.erasure(paramType);

      Object val;
      if (Types.isSubClassOf(paramClass, Provider.class)) {
        val = Jsr330InjectionResolver.getHolderInjectValue(habitat, ctor, paramType);
      } else {
        val = Jsr330InjectionResolver.get(habitat, ctor, ctor, paramClass);
      }
      
      params.add(val);
    }
    
    return params;
  }

  @Override
  public void initialize(final T t, final Inhabitant onBehalfOf) throws ComponentException {
    super.initialize(t, onBehalfOf);
  }

}
