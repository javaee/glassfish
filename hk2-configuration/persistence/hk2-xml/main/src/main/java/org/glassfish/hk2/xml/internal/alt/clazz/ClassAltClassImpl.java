/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2016 Oracle and/or its affiliates. All rights reserved.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;
import org.glassfish.hk2.xml.internal.alt.AltClass;
import org.glassfish.hk2.xml.internal.alt.AltMethod;

/**
 * @author jwells
 *
 */
public class ClassAltClassImpl implements AltClass {
    private static final ClassReflectionHelper SCALAR_HELPER = new ClassReflectionHelperImpl();
    public static final AltClass VOID = new ClassAltClassImpl(void.class, SCALAR_HELPER);
    public static final AltClass BOOLEAN = new ClassAltClassImpl(boolean.class, SCALAR_HELPER);
    public static final AltClass BYTE = new ClassAltClassImpl(byte.class, SCALAR_HELPER);
    public static final AltClass CHAR = new ClassAltClassImpl(char.class, SCALAR_HELPER);
    public static final AltClass SHORT = new ClassAltClassImpl(short.class, SCALAR_HELPER);
    public static final AltClass INT = new ClassAltClassImpl(int.class, SCALAR_HELPER);
    public static final AltClass LONG = new ClassAltClassImpl(long.class, SCALAR_HELPER);
    public static final AltClass FLOAT = new ClassAltClassImpl(float.class, SCALAR_HELPER);
    public static final AltClass DOUBLE = new ClassAltClassImpl(double.class, SCALAR_HELPER);
    public static final AltClass OBJECT = new ClassAltClassImpl(Object.class, SCALAR_HELPER);
    
    private final Class<?> clazz;
    private final ClassReflectionHelper helper;
    private List<AltMethod> methods;
    private List<AltAnnotation> annotations;
    
    public ClassAltClassImpl(Class<?> clazz, ClassReflectionHelper helper) {
        this.clazz = clazz;
        this.helper = helper;
    }
    
    public Class<?> getOriginalClass() {
        return clazz;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltClass#getName()
     */
    @Override
    public String getName() {
        return clazz.getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltClass#getSimpleName()
     */
    @Override
    public String getSimpleName() {
        return clazz.getSimpleName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltClass#getAnnotations()
     */
    @Override
    public synchronized List<AltAnnotation> getAnnotations() {
        if (annotations != null) return annotations;
        
        Annotation annotationz[] = clazz.getAnnotations();
        
        ArrayList<AltAnnotation> retVal = new ArrayList<AltAnnotation>(annotationz.length);
        for (Annotation annotation : annotationz) {
            retVal.add(new AnnotationAltAnnotationImpl(annotation, helper));
        }
        
        annotations = Collections.unmodifiableList(retVal);
        return annotations;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltClass#getMethods()
     */
    @Override
    public synchronized List<AltMethod> getMethods() {
        if (methods != null) return methods;
        
        Set<MethodWrapper> wrappers = helper.getAllMethods(clazz);
        ArrayList<AltMethod> retVal = new ArrayList<AltMethod>(wrappers.size());
        
        for (MethodWrapper method : wrappers) {
            retVal.add(new MethodAltMethodImpl(method.getMethod(), helper));
        }
        
        methods = Collections.unmodifiableList(retVal);
        return methods;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltClass#isInterface()
     */
    @Override
    public boolean isInterface() {
        return clazz.isInterface();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltClass#isArray()
     */
    @Override
    public boolean isArray() {
        return clazz.isArray();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltClass#getComponentType()
     */
    @Override
    public AltClass getComponentType() {
        Class<?> cType = clazz.getComponentType();
        if (cType == null) return null;
        
        return new ClassAltClassImpl(cType, helper);
    }
    
    @Override
    public int hashCode() {
        return clazz.getName().hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof AltClass)) return false;
        
        AltClass other = (AltClass) o;
        
        return clazz.getName().equals(other.getName());
    }

    @Override
    public String toString() {
        return "ClassAltClassImpl(" + clazz.getName() + "," + System.identityHashCode(this) + ")";
    }
}
