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
package org.jvnet.hk2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.glassfish.hk2.Binder;
import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.Module;
import org.glassfish.hk2.NamedBinder;
import org.glassfish.hk2.ResolvedBinder;
import org.glassfish.hk2.api.Scope;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ExtendedProvider;
import org.glassfish.hk2.api.scopes.PerLookup;
import org.glassfish.hk2.scopes.Singleton;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * This implementation of the {@link Module} interface
 * delegates to the Services API and the Habitat to do
 * all the heavy lifting
 * 
 * @author jwells
 *
 */
public class ServicesModuleImpl implements Module {
  private final ServiceLocatorImpl locator;
  private final List<ExtendedProviderImpl<?>> allProviders;
  private final HashMap<String, org.glassfish.hk2.Scope> proxyMap = new HashMap<String, org.glassfish.hk2.Scope>();
  
  /* package */ ServicesModuleImpl(
      ServiceLocatorImpl sli,
      List<ExtendedProviderImpl<?>> allProviders) {
    locator = sli;
    this.allProviders = allProviders;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void bindScope(BinderFactory binder, Scope scope) {
    final OldScopeImpl osi = new OldScopeImpl(scope);
    
    org.glassfish.hk2.Scope retVal = (org.glassfish.hk2.Scope) Proxy.newProxyInstance(scope.getClass().getClassLoader(),
        new Class<?>[] { org.glassfish.hk2.Scope.class },
        new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args)
              throws Throwable {
            if (method.getName().equals("current")) {
              return osi.current();
            }
            else if (method.getName().equals("toString")) {
              return "Scope proxy backed by: " + osi.toString();
            }
            
            throw new AssertionError("Method " + method + " not implemented");
          }
      
    });
    
    proxyMap.put(scope.getClass().getName(), retVal);
    
    // Must also register this guy with the binder
    binder.bind((Class) retVal.getClass()).toInstance(retVal);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void addOneDescriptor(BinderFactory binderFactory, ExtendedProviderImpl<?> epi) {
    Descriptor newDescriptor = epi.getDescriptor();
    
    Set<String> contracts = newDescriptor.getContracts();
    
    NamedBinder<?> namedBinder;
    if (contracts.size() > 0) {
      Binder<?> binder = binderFactory.bind(contracts.toArray(new String[contracts.size()]));
      
      Set<String> names = newDescriptor.getNames();
      if (names.size() > 0) {
        binder.named(Utilities.getFirstElement(names));
      }
      
      namedBinder = binder;
    }
    else {
      namedBinder = binderFactory.bind();
    }
    
    for (String qualifier : newDescriptor.getQualifiers()) {
      Class<? extends Annotation> myAnno = (Class<? extends Annotation>) Utilities.loadMyClass(qualifier);
      if (myAnno == null) continue;
      
      namedBinder = namedBinder.annotatedWith(myAnno);
    }
    
    if (epi.getSingleton() != null) {
      Object singleton = epi.getSingleton();
      
      ((NamedBinder<Object>) namedBinder).toInstance(singleton);
      
      return;
    }
    
    boolean hasScope = false;
    if (!newDescriptor.getScopes().isEmpty()) {
      hasScope = true;
    }
    
    ResolvedBinder rb;
    String implementationClass = Utilities.getFirstElement(newDescriptor.getImplementations());
    if (hasScope) {
      // Due to a... uh... "shortcoming" in hk2 if you have a scope you MUST use the class version of
      // the "to" method, rather than the string version.  Weird, but true
      
      rb = namedBinder.to((Class) Utilities.loadMyClass(implementationClass));
    }
    else {
      // It is assumed that we are binding to specific implementation
      rb = namedBinder.to(Utilities.getFirstElement(newDescriptor.getImplementations()));
    }
    
    if (hasScope) {
      String addScope = Utilities.getFirstElement(newDescriptor.getScopes());
      
      org.glassfish.hk2.Scope oldScope = proxyMap.get(addScope);
      if (oldScope == null) {
          if (addScope.equals(PerLookup.class.getName())) {
              // PerLookup handled as special case
              rb.in(org.glassfish.hk2.scopes.PerLookup.class);
              return;
          }
          
          if (addScope.equals(Singleton.class.getName())) {
              // Singleton handled as special case
              rb.in(org.glassfish.hk2.scopes.Singleton.class);
              return;
              
          }
        throw new AssertionError("Did not find a registered scope of type " + addScope);
      }
      
      rb.in(oldScope.getClass());
    }

  }
  
  /* (non-Javadoc)
   * @see org.glassfish.hk2.Module#configure(org.glassfish.hk2.BinderFactory)
   */
  @Override
  public void configure(BinderFactory binderFactory) {
    // We need to generate the proxy scopes prior to others
    // doing their binds, so we do the bindings of all the
    // scopes first
    List<ExtendedProvider<Object>> allScopes = locator.getAllServiceProviders(BuilderHelper.link().withContract(Scope.class).build());
    for (ExtendedProvider<Object> scope : allScopes) {
      ExtendedProviderImpl<Object> epi = (ExtendedProviderImpl<Object>) scope;
      
      bindScope(binderFactory, (Scope) epi.getSingleton());
    }
    
    for (ExtendedProviderImpl<?> newProvider : allProviders) {
      addOneDescriptor(binderFactory, newProvider);
    }
  }

}
