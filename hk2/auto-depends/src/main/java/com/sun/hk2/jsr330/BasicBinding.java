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
package com.sun.hk2.jsr330;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;

import org.jvnet.hk2.component.MultiMap;

/**
 * basic definition usable in most situations.  Simple
 * builder pattern over Jsr330BindingExt.
 * 
 * @author Jeff Trent
 *
 * @since 3.1
 */
public class BasicBinding implements Jsr330BindingExt, Cloneable {

  private String service;
  private List<String> contracts;
  private List<Named> names;
  private List<Class<? extends Annotation>> qualifiers;
  private Scope scope;
  private MultiMap<String, String> metadata;
  
  public BasicBinding() {
  }
  
  public BasicBinding(String service) {
    setServiceClassName(service);
  }
  
  public BasicBinding(Class<?> service) {
    setServiceClass(service);
  }
  
  public BasicBinding(Jsr330BindingExt another) {
    copy(another);
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + System.identityHashCode(this) + 
      "(" + getServiceClassName() + ", indicies: " + getContractClassNames() + 
      ", names: " + getServiceNames() + ", qualifiers: " + getServiceQualifiers() + ")";
  }
  
  public BasicBinding clone() {
    BasicBinding clone = new BasicBinding();
    clone.copy(this);
    return clone;
  }
  
  @SuppressWarnings("unchecked")
  protected synchronized void copy(Jsr330BindingExt another) {
    service = another.getServiceClassName();
    if (null == service || service.isEmpty()) throw new IllegalStateException();
    
    Collection<String> other = another.getContractClassNames();
    if (null != other) {
      if (null == contracts) {
        contracts = new ArrayList<String>();
      }
      contracts.addAll(other);
    }
    
    Collection otherNamed = another.getServiceNames();
    if (null != otherNamed) {
      if (null == names) {
        names = new ArrayList<Named>();
      }
      names.addAll(otherNamed);
    }

    Collection otherQualifiers = another.getServiceQualifiers();
    if (null != otherQualifiers) {
      if (null == qualifiers) {
        qualifiers = new ArrayList<Class<? extends Annotation>>();
      }
      qualifiers.addAll(otherQualifiers);
    }
    
    scope = another.getServiceScope();
    
    MultiMap<String, String> otherMd = another.getMetadata();
    if (null != otherMd) {
      metadata = metadata.clone();
    }
  }
  
  @Override
  public String getServiceClassName() {
    return service;
  }
  
  public BasicBinding setServiceClassName(String service) {
    if (null == service || service.isEmpty()) {
      throw new IllegalStateException();
    }
    this.service = service;
    return this;
  }

  public BasicBinding setServiceClass(Class<?> service) {
    if (null == service) {
      throw new IllegalStateException();
    }
    this.service = service.getName();
    return this;
  }
  
  @Override
  public Collection<String> getContractClassNames() {
    return (null == contracts) ? null : Collections.unmodifiableList(contracts);
  }
  
  public synchronized BasicBinding addContractClassName(String contract) {
    if (null == contract || contract.isEmpty()) {
      throw new IllegalStateException();
    }
    if (null == contracts) {
      contracts = new ArrayList<String>();
    }
    contracts.add(contract);
    return this;
  }

  public BasicBinding addContractClass(Class<?> contract) {
    if (null == contract) {
      throw new IllegalStateException();
    }
    return addContractClassName(contract.getName());
  }
  
  @Override
  public Collection<Named> getServiceNames() {
    return (null == names) ? null : Collections.unmodifiableList(names);
  }

  public synchronized BasicBinding addName(Named named) {
    if (null == named) {
      throw new IllegalStateException();
    }
    if (null == names) {
      names = new ArrayList<Named>();
    }
    names.add(named);
    return this;
  }
  
  public synchronized BasicBinding addName(final String name) {
    if (null == name) {
      throw new IllegalStateException();
    }
    if (null == names) {
      names = new ArrayList<Named>();
    }

    names.add(new Named() {
      @Override
      public String value() {
        return name;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return Named.class;
      }
      
      @Override
      public String toString() {
        return value();
      }
    });
    
    return this;
  }
  
  @Override
  public Collection<Class<? extends Annotation>> getServiceQualifiers() {
    return (null == qualifiers) ? null : Collections.unmodifiableList(qualifiers);
  }

  public BasicBinding addQualifier(Class<? extends Annotation> qc) {
    Qualifier q = qc.getAnnotation(Qualifier.class);
    if (null == q) {
      throw new IllegalArgumentException(qc.getName());
    }
    
    if (null == qualifiers) {
      qualifiers = new ArrayList<Class<? extends Annotation>>();
    }
    qualifiers.add(qc);

    return this;
  }
  
  @Override
  public Scope getServiceScope() {
    return scope;
  }

  public void setServiceScope(Scope scope) {
    this.scope = scope;
  }

  @Override
  public MultiMap<String, String> getMetadata() {
    return metadata;
  }
  
  public void setMetaData(MultiMap<String, String> metadata) {
    this.metadata = (null == metadata) ? null : metadata.clone();
  }
}
