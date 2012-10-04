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

import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.component.Inhabitant;

/**
 * An inhabitant that implements InhabitantEventPublisher, and maintains a list
 * of listeners to notify for interesting changes of the underlying delegate.
 * 
 * @author Jeff Trent
 */
@Deprecated
public class EventPublishingInhabitant<T> extends AbstractInhabitantImpl<T> {

  /**
   * Real {@link Inhabitant} object.
   */
  protected volatile Inhabitant<T> real;
  
  /**
   * For use in getting services
   */
  private final ServiceLocator serviceLocator;
  
  public EventPublishingInhabitant(ServiceLocator serviceLocator, Descriptor descriptor) {
      super(descriptor);
      this.serviceLocator = serviceLocator;
      this.real = null;
  }

  @SuppressWarnings("unchecked")
  public EventPublishingInhabitant(ServiceLocator serviceLocator, Inhabitant<?> delegate) {
      super(delegate);
      this.real = (Inhabitant<T>) delegate;
      this.serviceLocator = serviceLocator;
  }

  @Override
  public void release() {
    isActive();
    if (null != real) {
      real.release();
    }
  }

  @Override
  public boolean isActive() {
    return (null != real && real.isActive());
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get(Inhabitant onBehalfOf) {
    if (null == real) {
      fetch();
    }
    ActiveDescriptor<T> activeDescriptor;
    if (real != null && (real instanceof ActiveDescriptor)) {
        activeDescriptor = (ActiveDescriptor<T>) real;
    }
    else {
        activeDescriptor = (ActiveDescriptor<T>) serviceLocator.getBestDescriptor(
                BuilderHelper.createNameAndContractFilter(getImplementation(), getName()));
    }
    
    if (activeDescriptor == null) {
        return null;
    }
    
    ServiceHandle<T> handle = serviceLocator.getServiceHandle(activeDescriptor);
    
    handle.isActive();
    T result = handle.getService();
    
    return result;
  }

  protected void fetch() {
    throw new IllegalStateException();  // responsibility on derived classes
  }
  
  public ServiceLocator getServiceLocator() {
      return serviceLocator;
  }
  
  public int hashCode() {
      return System.identityHashCode(this);
  }
  
  public boolean equals(Object o) {
      return this == o;
  }
  
    @Override
    public Class<?> getImplementationClass() {
        ActiveDescriptor<?> ad = serviceLocator.reifyDescriptor(this);
        return (Class<? extends T>) ad.getImplementationClass();
    }

    @Override
    public T create(ServiceHandle<?> root) {
        return get(null);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + System.identityHashCode(this) + 
            "(" + getImplementation() + ", active: " + real + ")";
    }
}
