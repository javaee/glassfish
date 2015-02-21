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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;
import org.glassfish.hk2.xml.jaxb.internal.XmlElementImpl;

/**
 * @author jwells
 *
 */
public class AnnotationAltAnnotationImpl implements AltAnnotation {
    private final static HashSet<String> DO_NOT_HANDLE_METHODS = new HashSet<String>();
    static {
        DO_NOT_HANDLE_METHODS.add("hashCode");
        DO_NOT_HANDLE_METHODS.add("equals");
        DO_NOT_HANDLE_METHODS.add("toString");
        DO_NOT_HANDLE_METHODS.add("annotationType");
    }
    
    private final Annotation annotation;
    private Map<String, Object> values;
    
    public AnnotationAltAnnotationImpl(Annotation annotation) {
        this.annotation = annotation;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltAnnotation#annotationType()
     */
    @Override
    public String annotationType() {
        return annotation.annotationType().getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltAnnotation#getStringValue(java.lang.String)
     */
    @Override
    public synchronized String getStringValue(String methodName) {
        if (values == null) getAnnotationValues();
        
        if (XmlElementImpl.class.equals(annotation.getClass()) &&
                "getTypeByName".equals(methodName)) {
            XmlElementImpl xei = (XmlElementImpl) annotation;
            return xei.getTypeByName();
        }
        
        return (String) values.get(methodName);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltAnnotation#getBooleanValue(java.lang.String)
     */
    @Override
    public synchronized boolean getBooleanValue(String methodName) {
        if (values == null) getAnnotationValues();
        
        return (Boolean) values.get(methodName);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltAnnotation#getAnnotationValues()
     */
    @Override
    public synchronized Map<String, Object> getAnnotationValues() {
        if (values != null) return values;
        
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        for (Method javaAnnotationMethod : annotation.annotationType().getMethods()) {
            if (javaAnnotationMethod.getParameterTypes().length != 0) continue;
            if (DO_NOT_HANDLE_METHODS.contains(javaAnnotationMethod.getName())) continue;
            
            String key = javaAnnotationMethod.getName();
            
            Object value;
            try {
                value = ReflectionHelper.invoke(annotation, javaAnnotationMethod, new Object[0], false);
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Throwable th) {
                throw new RuntimeException(th);
            }
            
            retVal.put(key, value);
        }
        
        values = Collections.unmodifiableMap(retVal);
        return values;
    }
    
    @Override
    public String toString() {
        return "AnnotationAltAnnotationImpl(" + annotation + "," + System.identityHashCode(this) + ")";
    }
}
