/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.hk2.component;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.PreDestroy;

/**
 * Enables lifecycle controls surrounding and inhabitant's get() and release().
 * 
 * <p/>
 * Resembles LazyInhabitant is some ways, but varies by caching the result of get()
 * and keeping it cached while ref count >= 1.
 * 
 * @author Jeff Trent
 * @since 3.1
 */
@SuppressWarnings("unchecked")
public class ReferenceCountedLazyInhabitant<T> extends EventPublishingInhabitant<T> {

  private volatile WeakReference<T> ref;
  private volatile Inhabitant<?> onBehalfOf;
  private final AtomicInteger refCount;
  
  public ReferenceCountedLazyInhabitant(Inhabitant<?> delegate) {
    this(delegate, 0);
  }
  
  public ReferenceCountedLazyInhabitant(Inhabitant<?> delegate, int startingRefCount) {
    super(delegate);
    ref = new WeakReference(null);
    refCount = new AtomicInteger(startingRefCount);
  }
  
  @Override
  protected void finalize() throws Throwable {
    releaseFinal();
    super.finalize();
  }
  
  int getRefCount() {
    return refCount.get();
  }

  @Override
  public T get(Inhabitant onBehalfOf) {
    T object = null; 
    int val = refCount.incrementAndGet();
    if (1 == val || null == this.onBehalfOf) {
      try {
        object = super.get(onBehalfOf);
        ref = new WeakReference(object);
      } catch (RuntimeException e) {
        logger.log(Level.FINE, "error encountered", new ComponentException(e));
        refCount.decrementAndGet(); // ignore result
        throw e;
      }
      if (null != object) {
        this.onBehalfOf = onBehalfOf;
      }
    } else {
      object = ref.get();
    }
    assert (onBehalfOf == this.onBehalfOf || null == this.onBehalfOf) : "wrong onBehalfOf context";
    return object;
  }

  @Override
  public void release() {
    int val = refCount.decrementAndGet();
    assert (val >= 0) : "too many releases";
    
    if (0 == val) {
      try {
        releaseFinal();
      } catch (RuntimeException e) {
        logger.log(Level.FINE, "error encountered", new ComponentException(e));
        throw e;
      } finally {
        this.ref = new WeakReference(null);
        this.onBehalfOf = null;
        super.release();
      }
    }
  }

  private void releaseFinal() {
    Object object = ref.get();
    if (PreDestroy.class.isInstance(object)) {
      PreDestroy.class.cast(object).preDestroy();
    }
  }
  
  @Override
  public Inhabitant<T> scopedClone() {
    if (null == real) {
      fetch();
    }
    Inhabitant<T> real = this.real;
    return real.scopedClone();
  }
}
