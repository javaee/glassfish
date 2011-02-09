/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package org.jvnet.hk2.config.provider.internal;

import java.lang.reflect.Constructor;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import com.sun.hk2.component.ConstructorCreator;
import com.sun.hk2.component.InjectInjectionResolver;
import com.sun.hk2.component.InjectionResolver;
import com.sun.hk2.component.InjectionResolverQuery;

/**
 * 
 * @author Jeff Trent
 */
/*public*/ class ConfigByCreator<T> extends ConstructorCreator<T> {
  
  private final Object configBean;
  private final InjectionResolverQuery txnContextResolver;

  /*public*/ ConfigByCreator(InjectionResolverQuery txnContextResolver, Object configBean, Class<T> type, Habitat habitat, MultiMap<String, String> metadata) {
    super(type, habitat, metadata);
    this.txnContextResolver = txnContextResolver;
    this.configBean = configBean;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T create(Inhabitant onBehalfOf) throws ComponentException {
    Class<?> clazz = configBean.getClass();
    
    try {
      T obj = null;
      
      // first look for single arg ctor
      for (Constructor<?> ctor : type().getDeclaredConstructors()) {
        Class<?>[] paramTypes = ctor.getParameterTypes();
        if (1 == paramTypes.length && paramTypes[0].isAssignableFrom(clazz)) {
          ctor.setAccessible(true);
          obj = (T) ctor.newInstance(configBean);
          return obj;
        }
      }

      // then look for no arg
      return super.create(onBehalfOf);
    } catch (Exception e) {
      throw new ComponentException("unable to find appropriate ctor for: " + clazz, 
          new ComponentException(e.getMessage(), e));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected InjectionResolver[] getInjectionResolvers(Habitat h) {
    InjectionResolver[] resolvers = super.getInjectionResolvers(h);
    if (null != resolvers) {
      // replace the inject injection resolver with one that has a view into the transaction
      for (int i = 0; i < resolvers.length; i++) {
        if (InjectInjectionResolver.class.isInstance(resolvers[i])) {
          resolvers[i] = new ConfigInjectInjectionResolver(txnContextResolver, InjectInjectionResolver.class.cast(resolvers[i]));
        }
      }
    }
    
    return resolvers;
  }
  
}
