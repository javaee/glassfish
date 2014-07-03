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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.Unqualified;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * This is an implementation of Injectee that is used by the system.
 * 
 * @author jwells
 *
 */
public class SystemInjecteeImpl implements Injectee {
    private final Type requiredType;
    private final Set<Annotation> qualifiers;
    private final int position;
    private final Class<?> pClass;
    private final AnnotatedElement parent;
    private final boolean isOptional;
    private final boolean isSelf;
    private final Unqualified unqualified;
    private ActiveDescriptor<?> injecteeDescriptor;
    
    private final Object parentIdentifier;
    
    /* package */ SystemInjecteeImpl(
            Type requiredType,
            Set<Annotation> qualifiers,
            int position,
            AnnotatedElement parent,
            boolean isOptional,
            boolean isSelf,
            Unqualified unqualified,
            ActiveDescriptor<?> injecteeDescriptor) {
        this.requiredType = requiredType;
        this.position = position;
        this.parent = parent;
        this.qualifiers = Collections.unmodifiableSet(qualifiers);
        this.isOptional = isOptional;
        this.isSelf = isSelf;
        this.unqualified = unqualified;
        this.injecteeDescriptor = injecteeDescriptor;
        
        if (parent instanceof Field) {
            pClass = ((Field) parent).getDeclaringClass();
            parentIdentifier = ((Field) parent).getName();
        }
        else if (parent instanceof Constructor) {
            pClass = ((Constructor<?>) parent).getDeclaringClass();
            parentIdentifier = pClass;
        }
        else {
            pClass = ((Method) parent).getDeclaringClass();
            parentIdentifier = ReflectionHelper.createMethodWrapper((Method) parent);
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getRequiredType()
     */
    @Override
    public Type getRequiredType() {
        return requiredType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getRequiredQualifiers()
     */
    @Override
    public Set<Annotation> getRequiredQualifiers() {
        return qualifiers;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getPosition()
     */
    @Override
    public int getPosition() {
        return position;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getInjecteeClass()
     */
    @Override
    public Class<?> getInjecteeClass() {
        return pClass;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getParent()
     */
    @Override
    public AnnotatedElement getParent() {
        return parent;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#isOptional()
     */
    @Override
    public boolean isOptional() {
        return isOptional;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#isSelf()
     */
    @Override
    public boolean isSelf() {
        return isSelf;
    }
    
    @Override
    public Unqualified getUnqualified() {
        return unqualified;
    }
    
    @Override
    public ActiveDescriptor<?> getInjecteeDescriptor() {
        return injecteeDescriptor;
    }
    
    /* package */ void resetInjecteeDescriptor(ActiveDescriptor<?> injecteeDescriptor) {
        this.injecteeDescriptor = injecteeDescriptor;
    }
    
    @Override
    public int hashCode() {
        return position ^ parentIdentifier.hashCode() ^ pClass.hashCode() ;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof SystemInjecteeImpl)) return false;
        
        SystemInjecteeImpl other = (SystemInjecteeImpl) o;
        
        if (position != other.getPosition()) return false;
        if (!pClass.equals(other.getInjecteeClass())) return false;
        
        return parentIdentifier.equals(other.parentIdentifier);
    }
    
    @Override
    public String toString() {
        return "SystemInjecteeImpl(requiredType=" + Pretty.type(requiredType) +
                ",parent=" + Pretty.clazz(pClass) +
                ",qualifiers=" + Pretty.collection(qualifiers) +
                ",position=" + position +
                ",optional=" + isOptional +
                ",self=" + isSelf +
                ",unqualified=" + unqualified +
                "," + System.identityHashCode(this) + ")";
    }

    

    
}
