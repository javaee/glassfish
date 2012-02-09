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
import java.util.List;
import java.util.Set;

import org.glassfish.hk2.Binder;
import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.Module;
import org.glassfish.hk2.NamedBinder;
import org.glassfish.hk2.ResolvedBinder;
import org.glassfish.hk2.Scope;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ExtendedProvider;

/**
 * This implementation of the {@link Module} interface
 * delegates to the Services API and the Habitat to do
 * all the heavy lifting
 * 
 * @author jwells
 *
 */
public class ServicesModuleImpl implements Module {
  private final List<ExtendedProvider<Object>> allProviders;
  
  /* package */ ServicesModuleImpl(List<ExtendedProvider<Object>> allProviders) {
    this.allProviders = allProviders;
    
  }

  @SuppressWarnings("unchecked")
  private static void addOneDescriptor(BinderFactory binderFactory, Descriptor newDescriptor) {
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
    
    // It is assumed that we are binding to specific implementation
    ResolvedBinder<?> rb = namedBinder.to(Utilities.getFirstElement(newDescriptor.getImplementations()));
    
    if (!newDescriptor.getScopes().isEmpty()) {
      String addScope = Utilities.getFirstElement(newDescriptor.getScopes());
      
      Class<? extends Scope> myScope = (Class<? extends Scope>) Utilities.loadMyClass(addScope);
      
      rb.in(myScope);
    }

  }
  
  /* (non-Javadoc)
   * @see org.glassfish.hk2.Module#configure(org.glassfish.hk2.BinderFactory)
   */
  @Override
  public void configure(BinderFactory binderFactory) {
    for (ExtendedProvider<Object> newDescriptor : allProviders) {
      addOneDescriptor(binderFactory, newDescriptor.getDescriptor());
      
    }
  }

}
