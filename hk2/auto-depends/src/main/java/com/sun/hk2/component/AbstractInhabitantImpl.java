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
import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

/**
 * Partial implementation of {@link Inhabitant} that defines methods whose
 * semantics is fixed by {@link Habitat}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("unchecked")
public abstract class AbstractInhabitantImpl<T> implements Inhabitant<T>  {
    private Collection<Inhabitant> companions;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + System.identityHashCode(this) + 
            "(" + typeName() + ")";
    }
    
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

    public final <T> T getSerializedMetadata(Class<T> type) {
        return getSerializedMetadata(type,type.getName());
    }

    public Inhabitant lead() {
        return null;
    }

    public final Collection<Inhabitant> companions() {
        if(companions==null)    return Collections.emptyList();
        else                    return companions;
    }

    public final void setCompanions(Collection<Inhabitant> companions) {
        this.companions = companions;
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
