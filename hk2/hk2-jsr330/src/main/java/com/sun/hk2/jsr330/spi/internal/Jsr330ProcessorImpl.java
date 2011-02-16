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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Constants;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Scope;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.internal.runlevel.DefaultRunLevelService;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InjectionResolver;
import com.sun.hk2.jsr330.Jsr330Binding;
import com.sun.hk2.jsr330.Jsr330BindingExt;
import com.sun.hk2.jsr330.spi.Jsr330BindingLocator;
import com.sun.hk2.jsr330.spi.Jsr330Processor;

/**
 * Default Jsr330Processor for locating and installing all
 * Jsr330Bindings into the currently running habitat.
 * 
 * This implementation leverages the {@link RunLevelServicee}
 * to be immediately run as part of startup if this module/class
 * is in the classpath.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
//TODO: need to implement service ranking in Hk2
@Service(metadata=org.jvnet.hk2.component.Constants.SERVICE_RANKING + "=" + Integer.MIN_VALUE)
@RunLevel(DefaultRunLevelService.KERNEL_RUNLEVEL)
public class Jsr330ProcessorImpl implements Jsr330Processor, PostConstruct {

  @Inject
  private Habitat habitat;
  
  @Inject
  private Jsr330BindingLocator locator;

  private boolean installed;
  
  public Jsr330ProcessorImpl() {
  }
  
  public Jsr330ProcessorImpl(Habitat h) {
    this.habitat = h;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void postConstruct() {
    // TODO: this should not be necessary
    if (!installed) {
      habitat.add(new ExistingSingletonInhabitant<InjectionResolver>(InjectionResolver.class,
        new Jsr330InjectionResolver(habitat)));
      installed = true;
    }
  
    installAllBindings();
  }

  @Override
  public void installAllBindings() {
    Collection<? extends Jsr330Binding> bindings = locator.getAllBindings();
    assert(null != bindings);
    for (Jsr330Binding binding : bindings) {
      install(binding);
    }
  }

  public void install(Jsr330Binding binding) {
    String typeName = binding.getServiceClassName();
    
    Set<String> indicies = new HashSet<String>();
    Collection<String> ccns = binding.getContractClassNames();
    if (null != ccns) {
      indicies.addAll(ccns);
    }
    
    Collection<Class<? extends Annotation>> qualifiers = binding.getServiceQualifiers();
    if (null != qualifiers) {
      for (Class<? extends Annotation> ann : qualifiers) {
        if (null != ann) {
          indicies.add(ann.getName());
        }
      }
    }
    
    Collection<Named> named = binding.getServiceNames();
    Set<String> names = null;
    if (null != named) {
      names = new HashSet<String>();
      for (Named name : named) {
        if (null != name) {
          names.add(name.value());
        }
      }
    }
    
    Scope scope = getScope(binding);
    
    MultiMap<String, String> metadata = getMetadata(binding);

    Holder<ClassLoader> clh = new Holder.Impl<ClassLoader>(binding.getClass().getClassLoader());
    
    Inhabitant<?> i =  createInhabitant(clh, typeName, indicies, qualifiers, scope, metadata);
    InhabitantData data = createInhabitantData(i, binding, indicies, names, scope, metadata);
    add(data);
  }

  @SuppressWarnings("unchecked")
  protected Inhabitant<?> createInhabitant(Holder<ClassLoader> clh,
      String typeName,
      Set<String> indicies,
      Collection<Class<? extends Annotation>> annotations,
      Scope scope,
      MultiMap<String, String> metadata) {
    merge(metadata, annotations);
    Inhabitant<?> i = new Jsr330LazyInhabitant(habitat,
          clh,
          typeName,
          scope,
          metadata);
    return i;
  }

  protected void merge(MultiMap<String, String> metadata,
      Collection<Class<? extends Annotation>> annotations) {
    if (null != annotations) {
      for (Class<? extends Annotation> ann : annotations) {
        if (null != ann) {
          metadata.add(Constants.QUALIFIER, ann.getName());
        }
      }
    }
  }

  protected InhabitantData createInhabitantData(Inhabitant<?> i,
      Jsr330Binding binding, 
      Set<String> indicies,
      Set<String> names,
      Scope scope,
      MultiMap<String, String> metadata) {
    return new InhabitantData(i, indicies, names, scope, metadata);
  }

  /**
   * Returns the Hk2 scope giving the binding.
   *  
   * @param binding the binding
   * 
   * @return the Hk2 Scope
   */
  protected Scope getScope(Jsr330Binding binding) {
    javax.inject.Scope scope = binding.getServiceScope();
    if (null == scope) {
      return new PerLookup(); 
    }
    
    if (javax.inject.Singleton.class.isInstance(scope)) {
      throw new IllegalArgumentException(scope.toString());
    }
    
    return new Singleton();
  }

  /**
   * Returns the Hk2 metadata given the binding.
   * 
   * @param binding the binding
   * 
   * @return the Hk2 metadata
   */
  protected MultiMap<String, String> getMetadata(Jsr330Binding binding) {
    MultiMap<String, String> metadata = null;
    
    if (binding instanceof Jsr330BindingExt) {
      metadata = ((Jsr330BindingExt)binding).getMetadata();
    }
    
    return (null == metadata) ? new MultiMap<String, String>() : metadata;
  }

  protected void add(InhabitantData data) {
    habitat.add(data.i);

    Set<String> names = data.names;
    if (null == names) {
      names = Collections.singleton(null);
    }
    
    for (String v : data.indicies) {
      // register inhabitant to the index
      for (String name : names) {
        habitat.addIndex(data.i, v, name);
      }
    }
  }

}
