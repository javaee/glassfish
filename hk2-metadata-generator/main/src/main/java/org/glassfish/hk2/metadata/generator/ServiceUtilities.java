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
package org.glassfish.hk2.metadata.generator;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractsProvided;

/**
 * @author jwells
 *
 */
public class ServiceUtilities {
    /**
     * Returns one descriptor if this is a normal service class or two
     * if this class is a factory.  It can return an empty list if the
     * class given us is an interface
     * 
     * @param clazz
     * @return
     */
    public static List<DescriptorImpl> getDescriptorsFromClass(TypeElement clazz, ProcessingEnvironment processingEnvironment) {
        if (clazz == null || !ElementKind.CLASS.equals(clazz.getKind())) return Collections.emptyList();
        
        Set<String> contracts = getAllContracts(clazz, processingEnvironment);
        
        if (contracts.contains(Factory.class.getName())) {
            // Not yet implemented
            return Collections.emptyList();
        }
        
        DescriptorImpl retVal = new DescriptorImpl();
        retVal.setImplementation(nameToString(clazz.getQualifiedName()));
        for (String contract : contracts) {
            retVal.addAdvertisedContract(contract);
        }
        
        return Collections.singletonList(retVal);
    }
    
    @SuppressWarnings("unchecked")
    private static Set<String> getAllContracts(TypeElement clazz, ProcessingEnvironment processingEnvironment) {
        if (clazz == null) return Collections.emptySet();
        
        ContractsProvided provided = clazz.getAnnotation(ContractsProvided.class);
        if (provided != null) {
            LinkedHashSet<String> retVal = new LinkedHashSet<String>();
            
            AnnotationMirror contractsProvided = getAnnotation(clazz, ContractsProvided.class.getName());
            AnnotationValue annoValue = getValueFromAnnotation(contractsProvided, processingEnvironment);
            
            List<? extends AnnotationValue> arrayValues = (List<? extends AnnotationValue>) annoValue.getValue();
            for (AnnotationValue providedValues : arrayValues) {
                TypeMirror providedMirror = (TypeMirror) providedValues.getValue();
                        
                TypeElement providedElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(providedMirror);
                        
                retVal.add(nameToString(providedElement.getQualifiedName()));
            }
            
            return retVal;
        }
        
        TreeSet<String> retVal = new TreeSet<String>();
        
        getAllContracts(clazz, processingEnvironment, retVal, new HashSet<String>());
        
        retVal.add(nameToString(clazz.getQualifiedName()));
        
        return retVal;
    }
    
    private static void getAllContracts(TypeElement clazz, ProcessingEnvironment processingEnvironment, TreeSet<String> contracts, HashSet<String> cycleDetector) {
        if (clazz == null || nameToString(clazz.getQualifiedName()).equals(Object.class.getName())) return;
        
        if (cycleDetector.contains(nameToString(clazz.getQualifiedName()))) return;
        cycleDetector.add(nameToString(clazz.getQualifiedName()));
        
        TypeMirror superClazzMirror = clazz.getSuperclass();
        if (superClazzMirror != null) {
            TypeElement superClass = (TypeElement) processingEnvironment.getTypeUtils().asElement(superClazzMirror);
            
            getAllContracts(superClass, processingEnvironment, contracts, cycleDetector);
            
            if (isAContract(superClass)) {
                contracts.add(nameToString(superClass.getQualifiedName()));
            }
        }
        
        List<? extends TypeMirror> interfaceMirrors = clazz.getInterfaces();
        for (TypeMirror mirror : interfaceMirrors) {
            TypeElement iFace = (TypeElement) processingEnvironment.getTypeUtils().asElement(mirror);
            
            getAllContracts(iFace, processingEnvironment, contracts, cycleDetector);
            
            if (isAContract(iFace)) {
                contracts.add(nameToString(iFace.getQualifiedName()));
            }
        }
        
    }
    
    private static boolean isAContract(TypeElement element) {
        if (element == null) return false;
        
        Contract contract = element.getAnnotation(Contract.class);
        if (contract == null) return false;
        return true;
    }
    
    private static String nameToString(Name name) {
        if (name == null) return null;
        return name.toString();
    }
    
    private static AnnotationMirror getAnnotation(TypeElement clazz, String clazzName) {
        List<? extends AnnotationMirror> annotationMirrors = clazz.getAnnotationMirrors();
        for (AnnotationMirror annoMirror : annotationMirrors) {
            DeclaredType dt = annoMirror.getAnnotationType();
            TypeElement dtElement = (TypeElement) dt.asElement();
            
            if (clazzName.equals(nameToString(dtElement.getQualifiedName()))) {
                return annoMirror;
            }
        }
        
        // Not found
        return null;
    }
    
    private static AnnotationValue getValueFromAnnotation(AnnotationMirror annotation, String methodName,
            ProcessingEnvironment processingEnv) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> values =
                processingEnv.getElementUtils().getElementValuesWithDefaults(annotation);
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entries : values.entrySet()) {
            ExecutableElement executable = entries.getKey();
            AnnotationValue annoValue = entries.getValue();
            
            if (methodName.equals(nameToString(executable.getSimpleName()))) {
                return annoValue;
            }
        }
        
        // not found
        return null;
    }
    
    private static AnnotationValue getValueFromAnnotation(AnnotationMirror annotation, ProcessingEnvironment processingEnv) {
        return getValueFromAnnotation(annotation, "value", processingEnv);
        
    }

}
