/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.internal;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.api.ChildIterable;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * @author jwells
 *
 */
public class ChildIterableImpl<T> implements ChildIterable<T> {
    private final ServiceLocator locator;
    private final Type childType;
    private final String prefix;
    
    private final ChildFilter baseFilter;
    
    /* package */ ChildIterableImpl(ServiceLocator locator, Type childType, String prefix) {
        this.locator = locator;
        this.childType = childType;
        this.prefix = prefix;
        
        baseFilter = new ChildFilter(childType, prefix);
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        List<?> matches = locator.getAllServices(baseFilter);
        List<T> tMatches = (List<T>) matches;
        
        return tMatches.iterator();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.api.ChildIterable#byKey(java.lang.String)
     */
    @Override
    public T byKey(String key) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.api.ChildIterable#handleIterator()
     */
    @Override
    public Iterable<ServiceHandle<T>> handleIterator() {
        List<ServiceHandle<?>> matches = locator.getAllServiceHandles(baseFilter);
        final List<ServiceHandle<T>> tMatches = ReflectionHelper.<List<ServiceHandle<T>>>cast(matches);
        
        return new Iterable<ServiceHandle<T>>() {

            @Override
            public Iterator<ServiceHandle<T>> iterator() {
                return tMatches.iterator();
            }
            
        };
    }

}
