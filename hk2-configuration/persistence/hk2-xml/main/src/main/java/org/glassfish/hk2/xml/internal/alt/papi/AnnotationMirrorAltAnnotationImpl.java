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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.glassfish.hk2.xml.internal.Utilities;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;
import org.glassfish.hk2.xml.internal.alt.AltClass;
import org.glassfish.hk2.xml.internal.alt.AltEnum;

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
    
    @Override
    public synchronized String[] getStringArrayValue(String methodName) {
        getAnnotationValues();
        
        return (String[]) values.get(methodName);
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
                // The annotation method is a java.lang.Class
                value = Utilities.convertTypeMirror((TypeMirror) value, processingEnv);
            }
            else if (value instanceof VariableElement) {
                // The annotation method is an Enum
                VariableElement variable = (VariableElement) value;
                
                TypeElement enclosing = (TypeElement) variable.getEnclosingElement();
                
                String annoClassName = Utilities.convertNameToString(enclosing.getQualifiedName());
                String annoVal = Utilities.convertNameToString(variable.getSimpleName());
                
                value = new StringAltEnumImpl(annoClassName, annoVal);
            }
            else if (value instanceof AnnotationMirror) {
                throw new AssertionError("The annotation " + annotation + " key " + key + " has unimplemented type AnnotationMirror");
            }
            else if (value instanceof List) {
                // The annotation method returns an array of something
                ArrayType returnType = (ArrayType) annoMethod.getReturnType();
                TypeMirror arrayTypeMirror = returnType.getComponentType();
                TypeKind arrayTypeKind = arrayTypeMirror.getKind();
                
                @SuppressWarnings("unchecked")
                List<? extends AnnotationValue> array = ((List<? extends AnnotationValue>) value);
                
                if (TypeKind.INT.equals(arrayTypeMirror.getKind())) {
                    int[] iValue = new int[array.size()];
                    
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        iValue[lcv++] = (Integer) item.getValue();
                    }
                    
                    value = iValue;
                }
                else if (TypeKind.DECLARED.equals(arrayTypeMirror.getKind())) {
                    AltClass[] cValue = new AltClass[array.size()];
                    AltEnum[] eValue = new AltEnum[array.size()];
                    String[] sValue = new String[array.size()];
                    
                    boolean isClass = true;
                    boolean isEnum = true;
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        Object itemValue = item.getValue();
                        if (itemValue instanceof TypeMirror) {
                            isClass = true;
                            isEnum = false;
                            
                            cValue[lcv++] = Utilities.convertTypeMirror((TypeMirror) itemValue, processingEnv);
                        }
                        else if (itemValue instanceof VariableElement) {
                            isClass = false;
                            isEnum = true;
                            
                            VariableElement variable = (VariableElement) itemValue;
                            
                            TypeElement enclosing = (TypeElement) variable.getEnclosingElement();
                            
                            String annoClassName = Utilities.convertNameToString(enclosing.getQualifiedName());
                            String annoVal = Utilities.convertNameToString(variable.getSimpleName());
                            
                            eValue[lcv++] = new StringAltEnumImpl(annoClassName, annoVal);
                        }
                        else if (itemValue instanceof String) {
                            isClass = false;
                            isEnum = false;
                            
                            sValue[lcv++] = (String) itemValue;
                        }
                        else {
                            throw new AssertionError("Unknown declared type: " + itemValue.getClass().getName());
                        }
                    }
                    
                    if (isClass) {
                        value = cValue;
                    }
                    else if (isEnum) {
                        value = eValue;
                    }
                    else {
                        value = sValue;
                    }
                }
                else if (TypeKind.LONG.equals(arrayTypeMirror.getKind())) {
                    long[] iValue = new long[array.size()];
                    
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        iValue[lcv++] = (Long) item.getValue();
                    }
                    
                    value = iValue;
                }
                else if (TypeKind.SHORT.equals(arrayTypeMirror.getKind())) {
                    short[] iValue = new short[array.size()];
                    
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        iValue[lcv++] = (Short) item.getValue();
                    }
                    
                    value = iValue;
                }
                else if (TypeKind.CHAR.equals(arrayTypeMirror.getKind())) {
                    char[] iValue = new char[array.size()];
                    
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        iValue[lcv++] = (Character) item.getValue();
                    }
                    
                    value = iValue;
                }
                else if (TypeKind.FLOAT.equals(arrayTypeMirror.getKind())) {
                    float[] iValue = new float[array.size()];
                    
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        iValue[lcv++] = (Float) item.getValue();
                    }
                    
                    value = iValue;
                }
                else if (TypeKind.DOUBLE.equals(arrayTypeMirror.getKind())) {
                    double[] iValue = new double[array.size()];
                    
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        iValue[lcv++] = (Double) item.getValue();
                    }
                    
                    value = iValue;
                }
                else if (TypeKind.BOOLEAN.equals(arrayTypeMirror.getKind())) {
                    boolean[] iValue = new boolean[array.size()];
                    
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        iValue[lcv++] = (Boolean) item.getValue();
                    }
                    
                    value = iValue;
                }
                else if (TypeKind.BYTE.equals(arrayTypeMirror.getKind())) {
                    byte[] iValue = new byte[array.size()];
                    
                    int lcv = 0;
                    for (AnnotationValue item : array) {
                        iValue[lcv++] = (Byte) item.getValue();
                    }
                    
                    value = iValue;
                    
                }
                else {
                    throw new AssertionError("Array type " + arrayTypeKind + " is not implemented");
                }
            }
            
            retVal.put(key, value);
        }
        
        values = Collections.unmodifiableMap(retVal);
        return values;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(AltAnnotation o) {
        return annotationType().compareTo(o.annotationType());
    }
    
    @Override
    public int hashCode() {
        return annotationType().hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof AltAnnotation)) return false;
        AltAnnotation other = (AltAnnotation) o;
        
        return annotationType().equals(other.annotationType());
    }
    
    @Override
    public String toString() {
        return "AnnotationMirrorAltAnnotationImpl(" + annotationType() + ")";
    }

}
