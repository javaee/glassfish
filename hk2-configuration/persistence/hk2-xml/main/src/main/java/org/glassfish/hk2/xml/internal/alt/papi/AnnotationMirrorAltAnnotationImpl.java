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
package org.glassfish.hk2.xml.internal.alt.papi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.glassfish.hk2.xml.internal.Utilities;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;

/**
 * @author jwells
 *
 */
public class AnnotationMirrorAltAnnotationImpl implements AltAnnotation {
    private final AnnotationMirror annotation;
    private final ProcessingEnvironment processingEnv;
    private String type;
    private Map<String, Object> values;
    
    public AnnotationMirrorAltAnnotationImpl(AnnotationMirror annotation, ProcessingEnvironment processingEnv) {
        this.annotation = annotation;
        this.processingEnv = processingEnv;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltAnnotation#annotationType()
     */
    @Override
    public synchronized String annotationType() {
        if (type != null) return type;
        
        DeclaredType dt = annotation.getAnnotationType();
        TypeElement clazzType = (TypeElement) dt.asElement();
        type = Utilities.convertNameToString(clazzType.getQualifiedName());
        
        return type;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltAnnotation#getStringValue(java.lang.String)
     */
    @Override
    public String getStringValue(String methodName) {
        getAnnotationValues();
        
        return (String) values.get(methodName);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltAnnotation#getBooleanValue(java.lang.String)
     */
    @Override
    public boolean getBooleanValue(String methodName) {
        getAnnotationValues();
        
        return (Boolean) values.get(methodName);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.alt.AltAnnotation#getAnnotationValues()
     */
    @Override
    public synchronized Map<String, Object> getAnnotationValues() {
        if (values != null) return values;
        
        Map<? extends ExecutableElement, ? extends AnnotationValue> rawValues =
                processingEnv.getElementUtils().getElementValuesWithDefaults(annotation);
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : rawValues.entrySet()) {
            ExecutableElement annoMethod = entry.getKey();
            AnnotationValue annoValue = entry.getValue();
            
            String key = Utilities.convertNameToString(annoMethod.getSimpleName());
            Object value = annoValue.getValue();
            
            if (value instanceof TypeMirror) {
                value = Utilities.convertTypeMirror((TypeMirror) value, processingEnv);
            }
            else if (value instanceof VariableElement) {
                VariableElement variable = (VariableElement) value;
                
                throw new AssertionError("The annotation " + annotation + " key " + key + " has unimplemented enum");
            }
            else if (value instanceof AnnotationMirror) {
                throw new AssertionError("The annotation " + annotation + " key " + key + " has unimplemented type AnnotationMirror");
            }
            else if (value instanceof List) {
                throw new AssertionError("The annotation " + annotation + " key " + key + " is an unimplemented array");
            }
            
            retVal.put(key, value);
        }
        
        values = Collections.unmodifiableMap(retVal);
        return values;
    }

}
