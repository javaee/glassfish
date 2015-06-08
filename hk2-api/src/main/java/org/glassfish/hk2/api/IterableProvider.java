/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Provider;

/**
 * This object can be injected rather than Provider or ExtendedProvider when
 * it is desired to iterate over more than one returned instance of the type.
 * <p>
 * The iterator returned will be in ranked order (with DescriptorRank as
 * primary key, largest rank first and ServiceID as secondary key, smallest
 * id first)
 * 
 * @author jwells
 * @param <T> The type of this IterableProvider
 */
public interface IterableProvider<T> extends Provider<T>, Iterable<T> {
    /**
     * Rather than getting the service directly with get (in which
     * case the returned service cannot be disposed of) this method
     * will instead return a service handle for the current best service.
     * 
     * @return A ServiceHandle for the service, or null if there is
     * currently no service definition available
     */
    public ServiceHandle<T> getHandle();
    
    /**
     * Returns the size of the iterator that would be returned
     * 
     * @return the size of the iterator that would be chosen
     */
    public int getSize();
    
    /**
     * Returns an IterableProvider that is further qualified
     * with the given name
     * 
     * @param name The value field of the Named annotation parameter.  Must
     * not be null
     * @return An iterable provider further qualified with the given name
     */
    public IterableProvider<T> named(String name);
    
    /**
     * Returns an IterableProvider that is of the given type.  This type
     * must be one of the type safe contracts of the original iterator
     * 
     * @param type The type to restrict the returned iterator to
     * @return An iterator restricted to only providing the given type
     */
    public <U> IterableProvider<U> ofType(Type type);
    
    /**
     * A set of qualifiers to further restrict this iterator to.
     * 
     * @param qualifiers The qualifiers to further restrict this iterator to
     * @return An iterator restricted with the given qualifiers
     */
    public IterableProvider<T> qualifiedWith(Annotation... qualifiers);
    
    /**
     * This version of iterator returns an iterator of ServiceHandles rather
     * than returning the services (which then have no way to be properly
     * destroyed)
     * 
     * @return An iterator of ServiceHandles for the set of services
     * represtended by this IterableProvider
     */
    public Iterable<ServiceHandle<T>> handleIterator();

}
