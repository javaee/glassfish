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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.Unqualified;
import org.glassfish.hk2.utilities.NamedImpl;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * @author jwells
 * 
 * @param <T> The type for this provider
 */
public class IterableProviderImpl<T> implements IterableProvider<T> {
    private final ServiceLocatorImpl locator;
    private final Type requiredType;
    private final Set<Annotation> requiredQualifiers;
    private final Unqualified unqualified;
    private final Injectee originalInjectee;
    private final boolean isIterable;
    
    /* package */ IterableProviderImpl(
            ServiceLocatorImpl locator,
            Type requiredType,
            Set<Annotation> requiredQualifiers,
            Unqualified unqualified,
            Injectee originalInjectee,
            boolean isIterable) {
        this.locator = locator;
        this.requiredType = requiredType;
        this.requiredQualifiers = Collections.unmodifiableSet(requiredQualifiers);
        this.unqualified = unqualified;
        this.originalInjectee = originalInjectee;
        this.isIterable = isIterable;
    }
    
    private void justInTime() {
        InjecteeImpl injectee = new InjecteeImpl(originalInjectee);
        injectee.setRequiredType(requiredType);
        injectee.setRequiredQualifiers(requiredQualifiers);
        if (unqualified != null) {
            injectee.setUnqualified(unqualified);
        }
        
        // This does nothing more than run the JIT resolvers
        locator.getInjecteeDescriptor(injectee);
    }

    /* (non-Javadoc)
     * @see javax.inject.Provider#get()
     */
    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        justInTime();
        
        // Must do this in this way to ensure that the generated item is properly associated with the root
        return (T) locator.getUnqualifiedService(requiredType, unqualified,
                isIterable, requiredQualifiers.toArray(new Annotation[requiredQualifiers.size()]));
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.IterableProvider#getHandle()
     */
    @SuppressWarnings("unchecked")
    @Override
    public ServiceHandle<T> getHandle() {
        justInTime();
        
        return (ServiceHandle<T>) locator.getUnqualifiedServiceHandle(requiredType, unqualified,
                isIterable, requiredQualifiers.toArray(new Annotation[requiredQualifiers.size()]));
    }
    

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        justInTime();
        
        List<ServiceHandle<T>> handles;
        handles = ReflectionHelper.<List<ServiceHandle<T>>>cast(locator.getAllUnqualifiedServiceHandles(requiredType,
                    unqualified, isIterable, requiredQualifiers.toArray(new Annotation[requiredQualifiers.size()])));
        
        return new MyIterator<T>(handles);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.IterableProvider#getSize()
     */
    @Override
    public int getSize() {
        justInTime();
        
        return locator.getAllUnqualifiedServiceHandles(requiredType, unqualified, isIterable,
                requiredQualifiers.toArray(new Annotation[requiredQualifiers.size()])).size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.IterableProvider#named(java.lang.String)
     */
    @Override
    public IterableProvider<T> named(String name) {
        return qualifiedWith(new NamedImpl(name));
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.IterableProvider#ofType(java.lang.reflect.Type)
     */
    @Override
    public <U> IterableProvider<U> ofType(Type type) {
        return new IterableProviderImpl<U>(locator, type, requiredQualifiers, unqualified, originalInjectee, isIterable);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.IterableProvider#qualifiedWith(java.lang.annotation.Annotation[])
     */
    @Override
    public IterableProvider<T> qualifiedWith(Annotation... qualifiers) {
        HashSet<Annotation> moreAnnotations = new HashSet<Annotation>(requiredQualifiers);
        for (Annotation qualifier : qualifiers) {
            moreAnnotations.add(qualifier);
        }
        
        return new IterableProviderImpl<T>(locator, requiredType, moreAnnotations, unqualified, originalInjectee, isIterable);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.IterableProvider#handleIterator()
     */
    @Override
    public Iterable<ServiceHandle<T>> handleIterator() {
        justInTime();
        
        List<ServiceHandle<T>> handles = ReflectionHelper.<List<ServiceHandle<T>>>cast(locator.getAllServiceHandles(requiredType,
                requiredQualifiers.toArray(new Annotation[requiredQualifiers.size()])));
        
        return new HandleIterable<T>(handles);
    }
    
    private static class MyIterator<U> implements Iterator<U> {
        private final LinkedList<ServiceHandle<U>> handles;
        
        private MyIterator(List<ServiceHandle<U>> handles) {
            this.handles = new LinkedList<ServiceHandle<U>>(handles);
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return !handles.isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public U next() {
            if (handles.isEmpty()) throw new NoSuchElementException();
            
            ServiceHandle<U> nextHandle = handles.removeFirst();
            
            return nextHandle.getService();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
            
        }
        
    }
    
    private static class HandleIterable<U> implements Iterable<ServiceHandle<U>> {
        private final List<ServiceHandle<U>> handles;
        
        private HandleIterable(List<ServiceHandle<U>> handles) {
            this.handles = new LinkedList<ServiceHandle<U>>(handles);
        }

        /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<ServiceHandle<U>> iterator() {
            return new MyHandleIterator<U>(handles);
        }
        
    }
    
    private static class MyHandleIterator<U> implements Iterator<ServiceHandle<U>> {
        private final LinkedList<ServiceHandle<U>> handles;
        
        private MyHandleIterator(List<ServiceHandle<U>> handles) {
            this.handles = new LinkedList<ServiceHandle<U>>(handles);
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return !handles.isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public ServiceHandle<U> next() {
            return handles.removeFirst();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
            
        }
        
    }

    public String toString() {
        return "IterableProviderImpl(" + Pretty.type(requiredType) + "," + Pretty.collection(requiredQualifiers) + "," +
            System.identityHashCode(this) + ")";
    }

    

    

}
