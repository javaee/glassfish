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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import com.sun.hk2.jsr330.BasicBinding;
import com.sun.hk2.jsr330.BindingFactory;
import com.sun.hk2.jsr330.Jsr330Binding;
import com.sun.hk2.jsr330.Jsr330Bindings;
import com.sun.hk2.jsr330.spi.Jsr330BindingLocator;

/**
 * Default implementation for the Jsr330BindingLocator.
 * 
 * It builds a list by first calling all {@link Jsr330Bindings},
 * and then finding individual {@link Jsr330Binding}(s) in the system.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
// TODO: need to implement service ranking in Hk2
@Service(metadata=org.jvnet.hk2.component.Constants.SERVICE_RANKING + "=" + Integer.MIN_VALUE)
public class Jsr330BindingLocatorImpl implements Jsr330BindingLocator, BindingFactory {

  @Inject
  private Habitat h;
  
  /**
   * Will return a list populated with zero or more Jsr330Binding objects. 
   */
  @Override
  public List<Jsr330Binding> getAllBindings() {
    ArrayList<Jsr330Binding> bindings = new ArrayList<Jsr330Binding>();

    Collection<Jsr330Bindings> producers = h.getAllByContract(Jsr330Bindings.class);
    for (Jsr330Bindings producer : producers) {
      ArrayList<Jsr330Binding> subBindings = new ArrayList<Jsr330Binding>();
      producer.getBindings(subBindings, this);
      bindings.addAll(subBindings);
    }
    
    Collection<Jsr330Binding> floaters = h.getAllByContract(Jsr330Binding.class);
    for (Jsr330Binding binding : floaters) {
      bindings.add(binding);
    }
    
    return bindings;
  }

  @Override
  public BasicBinding newBinding(String serviceName) {
    return new BasicBinding(serviceName);
  }

  @Override
  public BasicBinding newBinding(Class<?> serviceClass) {
    return new BasicBinding(serviceClass);
  }

}
