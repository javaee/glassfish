/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.*;

import java.util.*;

/**
 * Proxy for types, used in place until the type can be properly instantiated.
 * Proxy type also holds all incoming reference to the type.
 *
 * @author Jerome Dochez
 */
public class TypeProxy<T extends Type> {

    private T value = null;
    private volatile boolean visited = false;
    private final String name;
    private final Notifier<T> notifier;
    private final List<Member> fieldRefs = new ArrayList<Member>();
    private final List<Type> subTypeRefs = new ArrayList<Type>();
    private final List<ClassModel> implementations = new ArrayList<ClassModel>();


    /**
     * Creates a new type proxy, this ctor is package private as many
     * other activities must be performed when a new proxy type is created.
     *
     * @param notifier notification handle to notify receiver the proxied
     * type has been resolved
     * @param name type name
     */
    TypeProxy(Notifier<T> notifier, String name) {
        this.notifier = notifier;
        this.name = name;
    }
    
    @Override
    public String toString() {
      return "TypeProxy:" + name;
    }

    public void set(T  value) {
        this.value = value;
        if (notifier!=null) {
            notifier.valueSet(value);
        }

    }

    public T get() {
        return value;
    }

    public String getName() {
        if (value!=null) return value.getName();
        return name;
    }
    
    public interface Notifier<T> {
        public void valueSet(T value);
    }

    public synchronized void addFieldRef(FieldModel field) {
        fieldRefs.add(field);
    }

    public List<Member> getRefs() {
        return Collections.unmodifiableList(fieldRefs);
    }

    public synchronized void addSubTypeRef(Type subType) {
        subTypeRefs.add(subType);
    }

    public List<Type> getSubTypeRefs() {
        return Collections.unmodifiableList(subTypeRefs);
    }

    public synchronized void addImplementation(ClassModel classModel) {
        implementations.add(classModel);
    }

    public List<ClassModel> getImplementations() {
        return Collections.unmodifiableList(implementations);
    }

    public static <U extends Type> Collection<U> adapter(final Collection<TypeProxy<U>> source) {
        return new AbstractCollection<U>() {

            @Override
            public Iterator<U> iterator() {
                final Iterator<TypeProxy<U>> itr = source.iterator();
                return new Iterator<U>() {
                    @Override
                    public boolean hasNext() {
                        return itr.hasNext();
                    }

                    @Override
                    public U next() {
                        TypeProxy<U> next = itr.next();
                        if (next!=null) {
                            return next.get();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return source.size();
            }
        };
    }

    public void visited() {
        visited=true;
    }

    public boolean isVisited() {
        return visited;
    }
}
