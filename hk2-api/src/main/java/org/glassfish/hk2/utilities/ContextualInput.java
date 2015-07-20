/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * Used by several contexts for keeping the inputs of
 * {@link org.glassfish.hk2.api.Context#findOrCreate(ActiveDescriptor, ServiceHandle)}.
 * May be used as the key in a HashMap, where the criteria for equality
 * is the equality of the Descriptor
 * 
 * @author jwells
 */
public class ContextualInput<T> {
    private final ActiveDescriptor<T> descriptor;
    private final ServiceHandle<?> root;
    
    /**
     * The inputs from the {@link org.glassfish.hk2.api.Context#findOrCreate(ActiveDescriptor, ServiceHandle)}
     * method
     * 
     * @param descriptor The non-null descriptor associated with a contextual creation
     * @param root The possibly null root associated with a contextual creation
     */
    public ContextualInput(ActiveDescriptor<T> descriptor, ServiceHandle<?> root) {
        this.descriptor = descriptor;
        this.root = root;
    }
    
    /**
     * Returns the descriptor associated with this contextual creation
     * @return The non-null descriptor associated with this creation
     */
    public ActiveDescriptor<T> getDescriptor() {
        return descriptor;
    }
    
    /**
     * Returns the {@link ServiceHandle} root associated with this
     * contextual creation
     * 
     * @return The possibly null root associated with this creation
     */
    public ServiceHandle<?> getRoot() {
        return root;
    }
    
    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof ContextualInput)) return false;
        
        ContextualInput<T> other = (ContextualInput<T>) o;
        
        return descriptor.equals(other.descriptor);
    }
    
    @Override
    public String toString() {
        return "ContextualInput(" + descriptor.getImplementation() + "," + root + "," + System.identityHashCode(this) + ")";
    }

}
