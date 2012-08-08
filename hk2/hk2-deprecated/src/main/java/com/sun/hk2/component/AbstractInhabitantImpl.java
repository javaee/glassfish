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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.deprecated.internal.Utilities;

/**
 * Partial implementation of {@link Inhabitant} that defines methods whose
 * semantics is fixed by {@link org.glassfish.hk2.api.ServiceLocator}.
 *
 * @author Kohsuke Kawaguchi
 */
@Deprecated
public abstract class AbstractInhabitantImpl<T> extends DescriptorImpl implements Inhabitant<T> {
    protected static final Logger logger = Logger.getLogger(AbstractInhabitantImpl.class.getName());
    
    public AbstractInhabitantImpl(Descriptor descriptorOfSelf) {
        super((descriptorOfSelf == null) ? new DescriptorImpl() : descriptorOfSelf);
    }
    
    public boolean matches(Descriptor matchTo) {
        if (null == matchTo) {
            return true;
        }
        
        // TODO: JRW It is really hard to say what matches what.  For now, we are just gonna
        // match on Implementation
        return Utilities.safeEquals(getImplementation(), matchTo.getImplementation());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName() + "-" + System.identityHashCode(this) + "(");
        
        DescriptorImpl.pretty(sb, this);
        
        sb.append(")\n");
        
        return sb.toString();
    }
    
    @Override
    public final T get() {
        try {
            return get(this);
        } catch (Exception e) {
            // we are a holder, so we need to allow for {@link RunLevelService} constraints
            // not properly being met --- in such cases return null
            logger.log(Level.FINER, "swallowing error", e);
            return null;
        }
    }

    @Override
    public <U> U getByType(Class<U> type) {
        return (U) get();
    }

    public void dispose(T object) {
      if (object instanceof PreDestroy) {
          logger.log(Level.FINER, "calling PreDestroy on {0}", object);
          ((PreDestroy)object).preDestroy();
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

    @Override
    public Collection<Annotation> getAnnotations() {
        // TODO:
        throw new UnsupportedOperationException();
    }

}
