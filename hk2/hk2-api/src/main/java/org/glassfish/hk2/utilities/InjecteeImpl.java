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
package org.glassfish.hk2.utilities;

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

/**
 * This is a writeable version of the Injectee interface.  Using this
 * implementation may make your code more portable, as new methods
 * added to the interface will be reflected in this class.
 * 
 * @author jwells
 *
 */
public class InjecteeImpl implements Injectee {
    private Type requiredType;
    private Set<Annotation> qualifiers;
    private int position;
    private Class<?> pClass;
    private AnnotatedElement parent;
    private boolean isOptional = false;
    private boolean isSelf = false;
    private Unqualified unqualified = null;
    private ActiveDescriptor<?> injecteeDescriptor;
    
    /**
     * None of the fields of the returned object will be set
     */
    public InjecteeImpl() {
    }
    
    /**
     * Only the requiredType field will be set
     * 
     * @param requiredType The possibly null required type
     */
    public InjecteeImpl(Type requiredType) {
        this.requiredType = requiredType;
    }
    
    /**
     * This is the copy constructor, which will copy all the values from the incoming Injectee
     * @param copyMe The non-null Injectee to copy the values from
     */
    public InjecteeImpl(Injectee copyMe) {
        requiredType = copyMe.getRequiredType();
        position = copyMe.getPosition();
        parent = copyMe.getParent();
        qualifiers = Collections.unmodifiableSet(copyMe.getRequiredQualifiers());
        isOptional = copyMe.isOptional();
        isSelf = copyMe.isSelf();
        injecteeDescriptor = copyMe.getInjecteeDescriptor();
        // unqualified = copyMe.getUnqualified();
        
        if (parent instanceof Field) {
            pClass = ((Field) parent).getDeclaringClass();
        }
        else if (parent instanceof Constructor) {
            pClass = ((Constructor<?>) parent).getDeclaringClass();
        }
        else if (parent instanceof Method) {
            pClass = ((Method) parent).getDeclaringClass();
        }
        else {
            throw new IllegalArgumentException("parent " + parent + " has an unknown type");
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getRequiredType()
     */
    @Override
    public Type getRequiredType() {
        return requiredType;
    }
    
    /**
     * Sets the required type of this Injectee
     * @param requiredType The required type of this injectee
     */
    public void setRequiredType(Type requiredType) {
        this.requiredType = requiredType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getRequiredQualifiers()
     */
    @Override
    public Set<Annotation> getRequiredQualifiers() {
        if (qualifiers == null) return Collections.emptySet();
        return qualifiers;
    }
    
    /**
     * Sets the required qualifiers for this Injectee
     * @param requiredQualifiers The non-null set of required qualifiers
     */
    public void setRequiredQualifiers(Set<Annotation> requiredQualifiers) {
        qualifiers = Collections.unmodifiableSet(requiredQualifiers);
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getPosition()
     */
    @Override
    public int getPosition() {
        return position;
    }
    
    /**
     * Sets the position of this Injectee.  The position represents the index of
     * the parameter, or -1 if this Injectee is describing a field.
     * 
     * @param position The index position of the parameter, or -1 if descrbing a field
     */
    public void setPosition(int position) {
        this.position = position;
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
    
    /**
     * This setter sets both the parent and the injecteeClass fields.
     * 
     * @param parent The parent (Field, Constructor or Method) which is
     * the parent of this Injectee
     */
    public void setParent(AnnotatedElement parent) {
        this.parent = parent;
        
        if (parent instanceof Field) {
            pClass = ((Field) parent).getDeclaringClass();
        }
        else if (parent instanceof Constructor) {
            pClass = ((Constructor<?>) parent).getDeclaringClass();
        }
        else if (parent instanceof Method) {
            pClass = ((Method) parent).getDeclaringClass();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#isOptional()
     */
    @Override
    public boolean isOptional() {
        return isOptional;
    }
    
    /**
     * Sets whether or not this Injectee should be considered optional
     * 
     * @param optional true if this injectee is optional, false if required
     */
    public void setOptional(boolean optional) {
        this.isOptional = optional;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#isSelf()
     */
    @Override
    public boolean isSelf() {
        return isSelf;
    }
    
    /**
     * Sets whether or not this is a self-referencing injectee
     * 
     * @param self true if this is a self-referencing Injectee, and false otherwise
     */
    public void setSelf(boolean self) {
        isSelf = self;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Injectee#getUnqualified()
     */
    @Override
    public Unqualified getUnqualified() {
        return unqualified;
    }
    
    /**
     * Sets the unqualified annotation to be associated with this injectee
     * 
     * @param unqualified The unqualified annotation to be associated with this injectee
     */
    public void setUnqualified(Unqualified unqualified) {
        this.unqualified = unqualified;
    }
    
    @Override
    public ActiveDescriptor<?> getInjecteeDescriptor() {
        return injecteeDescriptor;
    }
    
    /**
     * Sets the descriptor to be associated with this injectee
     * 
     * @param injecteeDescriptor The injectee to be associated with this injectee
     */
    public void setInjecteeDescriptor(ActiveDescriptor<?> injecteeDescriptor) {
        this.injecteeDescriptor = injecteeDescriptor;
    }

    public String toString() {
        return "InjecteeImpl(requiredType=" + Pretty.type(requiredType) +
                ",parent=" + Pretty.clazz(pClass) +
                ",qualifiers=" + Pretty.collection(qualifiers) +
                ",position=" + position +
                ",optional=" + isOptional +
                ",self=" + isSelf +
                ",unqualified=" + unqualified +
                "," + System.identityHashCode(this) + ")";
    }

    
}
