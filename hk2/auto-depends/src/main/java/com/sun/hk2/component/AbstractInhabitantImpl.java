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

import org.jvnet.hk2.tracing.TracingThreadLocal;
import org.jvnet.hk2.tracing.TracingUtilities;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.PreDestroy;

import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Partial implementation of {@link Inhabitant} that defines methods whose
 * semantics is fixed by {@link Habitat}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("unchecked")
public abstract class AbstractInhabitantImpl<T> implements Inhabitant<T> {
    private static final boolean MANAGED_ENABLED = Habitat.MANAGED_INJECTION_POINTS_ENABLED;
    
    protected static final Logger logger = Logger.getLogger(ScopeInstance.class.getName());

    private Collection<Inhabitant> companions;

    private volatile Collection<Inhabitant<?>> managed;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + System.identityHashCode(this) + 
            "(" + typeName() + ")";
    }
    
    @Override
    public final T get() {
        try {
            if (TracingUtilities.isEnabled())
                TracingThreadLocal.get().push(this);
            return get(this);
        } finally {
            if (TracingUtilities.isEnabled())
                TracingThreadLocal.get().pop();
        }
    }

    @Override
    public <T> T getSerializedMetadata(final Class<T> type, String key) {
        String v = metadata().getOne(key);
        if(v==null)     return null;

        try {
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(new BASE64Decoder().decodeBuffer(v))) {
                final ClassLoader cl = type.getClassLoader();

                /**
                 * Use ClassLoader of the given type. Otherwise by default we end up using the classloader
                 * that loaded HK2, which won't be able to see most of the user classes.
                 */
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    String name = desc.getName();
                    try {
                        return Class.forName(name,false,cl);
                    } catch (ClassNotFoundException ex) {
                        return super.resolveClass(desc);
                    }
                }
            };

            return type.cast(is.readObject());
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Override
    public final <T> T getSerializedMetadata(Class<T> type) {
        return getSerializedMetadata(type,type.getName());
    }

    @Override
    public Inhabitant lead() {
        return null;
    }

    @Override
    public final Collection<Inhabitant> companions() {
        if(companions==null)    return Collections.emptyList();
        else                    return companions;
    }

    @Override
    public final void setCompanions(Collection<Inhabitant> companions) {
        this.companions = companions;
    }
    
    @Override
    public Inhabitant<T> scopedClone() {
      return new ReferenceCountedLazyInhabitant<T>(this);
    }

    public synchronized int getManagedCount() {
      if (null == managed) {
        return 0;
      }

      cleanManaged();
      return managed.size();
    }
    
    @Override
    public synchronized void manage(Inhabitant<?> managedInhabitant) {
      if (!MANAGED_ENABLED) {
        return;
      }
      
      assert(null != managedInhabitant);
      assert(this != managedInhabitant);
      if (null == managed) {
        managed = new ArrayList<Inhabitant<?>>();
      } else {
        cleanManaged();
      }
      
      managed.add(managedInhabitant);
    }

    /**
     * Clean out old, gc-collected managed inhabitants
     */
    private void cleanManaged() {
      Iterator<Inhabitant<?>> iter = managed.iterator();
      while (iter.hasNext()) {
        if (!iter.next().isInstantiated()) {
          iter.remove();
        }
      }
    }

    @Override
    public void release() {
      if (null != managed) {
        releaseManaged();
      }
    }

    protected final void dispose(T object) {
      if (object instanceof PreDestroy) {
          logger.log(Level.FINER, "calling PreDestroy on {0}", object);
          ((PreDestroy)object).preDestroy();
      }
    }
    
    protected synchronized void releaseManaged() {
      if (null != managed) {
        RuntimeException lastException = null;
        
        for (Inhabitant<?> i : managed) {
          logger.log(Level.FINER, "releasing {0} on behalf of {1}", new Object[] {i, this});
          try {
            i.release();
          } catch (RuntimeException e) {
            logger.log(Level.FINE, "error encountered", e);
            lastException = e;
          }
        }
        
        managed = null;
        
        if (null != lastException) {
          throw lastException;
        }
      }
    }
    
    public <V extends Annotation> V getAnnotation(Class<V> annotation) {
        return getAnnotation(type(), annotation, false);
    }
    
    /**
     * FOR INTERNAL USE TO HK2
     */
    public static <V extends Annotation> V getAnnotation(Class<?> annotated,
        Class<V> annotation,
        boolean walkParentChain) {
      V v = annotated.getAnnotation(annotation);
      if (null != v) {
          return v;
      }
      
      for (Annotation a : annotated.getAnnotations()) {
          v = a.annotationType().getAnnotation(annotation);
          if (null != v) {
              return v;
          }
      }
      
      if (walkParentChain) {
        annotated = annotated.getSuperclass();
        if (null != annotated) {
          return getAnnotation(annotated, annotation, true);
        }
      }
      
      return null;
    }

}
