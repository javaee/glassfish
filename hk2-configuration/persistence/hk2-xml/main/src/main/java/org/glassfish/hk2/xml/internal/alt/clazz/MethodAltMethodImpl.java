/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.internal.alt.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;
import org.glassfish.hk2.xml.internal.alt.AltClass;
import org.glassfish.hk2.xml.internal.alt.AltMethod;

/**
 * @author jwells
 *
 */
public class MethodAltMethodImpl implements AltMethod {
    private final Method method;
    private final ClassReflectionHelper helper;
    private List<AltClass> parameterTypes;
    private List<AltAnnotation> altAnnotations;
    
    public MethodAltMethodImpl(Method method, ClassReflectionHelper helper) {
        this.method = method;
        this.helper = helper;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltMethod#getName()
     */
    @Override
    public String getName() {
        return method.getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltMethod#getReturnType()
     */
    @Override
    public AltClass getReturnType() {
        Class<?> retVal = method.getReturnType();
        if (retVal == null) retVal = void.class;
        
        return new ClassAltClassImpl(retVal, helper);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltMethod#getParameterTypes()
     */
    @Override
    public synchronized List<AltClass> getParameterTypes() {
        if (parameterTypes != null) return parameterTypes;
        
        Class<?> pTypes[] = method.getParameterTypes();
        List<AltClass> retVal = new ArrayList<AltClass>(pTypes.length);
        
        for (Class<?> pType : pTypes) {
            retVal.add(new ClassAltClassImpl(pType, helper));
        }
        
        parameterTypes = Collections.unmodifiableList(retVal);
        return parameterTypes;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltMethod#getFirstTypeArgument()
     */
    @Override
    public AltClass getFirstTypeArgument() {
        Type type = method.getGenericReturnType();
        if (type == null) return null;
        
        Type first = ReflectionHelper.getFirstTypeArgument(type);
        if (first == null) return null;
        
        Class<?> retVal = ReflectionHelper.getRawClass(first);
        if (retVal == null) return null;
        
        return new ClassAltClassImpl(retVal, helper);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltMethod#getFirstTypeArgumentOfParameter(int)
     */
    @Override
    public AltClass getFirstTypeArgumentOfParameter(int index) {
        Type pTypes[] = method.getGenericParameterTypes();
        Type pType = pTypes[index];
        
        Type first = ReflectionHelper.getFirstTypeArgument(pType);
        if (first == null) return null;
        
        Class<?> retVal = ReflectionHelper.getRawClass(first);
        if (retVal == null) return null;
        
        return new ClassAltClassImpl(retVal, helper);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltMethod#getAnnotation(java.lang.String)
     */
    @Override
    public AltAnnotation getAnnotation(String annotation) {
        if (annotation == null) return null;
        
        Annotation annotations[] = method.getAnnotations();
        
        for (Annotation anno : annotations) {
            if (annotation.equals(anno.annotationType().getName())) {
                return new AnnotationAltAnnotationImpl(anno);
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltMethod#getAnnotations()
     */
    @Override
    public synchronized List<AltAnnotation> getAnnotations() {
        if (altAnnotations != null) return altAnnotations;
        
        Annotation annotations[] = method.getAnnotations();
        List<AltAnnotation> retVal = new ArrayList<AltAnnotation>(annotations.length);
        
        for (Annotation annotation : annotations) {
            retVal.add(new AnnotationAltAnnotationImpl(annotation));
        }
        
        altAnnotations = Collections.unmodifiableList(retVal);
        return altAnnotations;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltMethod#isVarArgs()
     */
    @Override
    public boolean isVarArgs() {
        return method.isVarArgs();
    }

    @Override
    public String toString() {
        return "MethodAltMethodImpl(" + method + "," + System.identityHashCode(this) + ")";
    }
}
