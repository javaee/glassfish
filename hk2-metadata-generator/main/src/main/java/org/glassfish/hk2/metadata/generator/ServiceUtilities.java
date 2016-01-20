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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Metadata;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ProxyForSameScope;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.UseProxy;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

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
        
        Elements elements = processingEnvironment.getElementUtils();
        
        Set<Modifier> modifiers = clazz.getModifiers();
        if (modifiers.contains(Modifier.ABSTRACT)) {
            throw new IllegalArgumentException("The class " + elements.getBinaryName(clazz) +
                    " is abstract.  @Service may only be put on concrete classes");
        }
        
        Set<String> contracts = getAllContracts(clazz, processingEnvironment);
        
        if (contracts.contains(Factory.class.getName())) {
            // Not yet implemented
            return getDescriptorFromFactoryClass(clazz, contracts, processingEnvironment);
        }
        
        DescriptorImpl retVal = new DescriptorImpl();
        retVal.setImplementation(nameToString(elements.getBinaryName(clazz)));
        
        generateFromClass(retVal, clazz, contracts, processingEnvironment);
        
        return Collections.singletonList(retVal);
    }
    
    private static List<DescriptorImpl> getDescriptorFromFactoryClass(TypeElement clazz, Set<String> contracts, ProcessingEnvironment processingEnvironment) {
        LinkedList<DescriptorImpl> retVal = new LinkedList<DescriptorImpl>();
        
        Elements elements = processingEnvironment.getElementUtils();
        
        DescriptorImpl factoryItself = new DescriptorImpl();
        factoryItself.setImplementation(nameToString(elements.getBinaryName(clazz)));
        
        generateFromClass(factoryItself, clazz, contracts, processingEnvironment);
        retVal.add(factoryItself);
        
        // Now find the provideMethod
        ExecutableElement provideMethodEE = null;
        for(Element enclosedElement : processingEnvironment.getElementUtils().getAllMembers(clazz)) {
            if (!ElementKind.METHOD.equals(enclosedElement.getKind())) continue;
            
            ExecutableElement executable = (ExecutableElement) enclosedElement;
            if (!"provide".equals(nameToString(executable.getSimpleName()))) continue;
            
            provideMethodEE = executable;
            break;
        }
        
        if (provideMethodEE == null) return retVal;
        
        TypeMirror methodReturnMirror = provideMethodEE.getReturnType();
        Element methodReturnElementRaw = processingEnvironment.getTypeUtils().asElement(methodReturnMirror);
        TypeElement methodReturnElement;
        if (methodReturnElementRaw instanceof TypeElement) {
            methodReturnElement = (TypeElement) methodReturnElementRaw;
        }
        else if (methodReturnElementRaw instanceof TypeParameterElement) {
            methodReturnElement = findFactory(clazz, clazz, null, null, processingEnvironment);
        }
        else {
            throw new AssertionError("Unknown type for provide method: " + methodReturnElementRaw);
        }
        
        DescriptorImpl provideDescriptor = new DescriptorImpl();
        provideDescriptor.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        provideDescriptor.setImplementation(nameToString(elements.getBinaryName(clazz)));
        
        Set<String> methodContracts = getAllContracts(methodReturnElement, processingEnvironment);
        for (String methodContract : methodContracts) {
            provideDescriptor.addAdvertisedContract(methodContract);
        }
        
        generateFromClass(provideDescriptor, provideMethodEE, methodContracts, processingEnvironment);
        
        retVal.add(provideDescriptor);
        
        return retVal;
    }
    
    private static TypeElement findFactory(
            TypeElement originalClazz,
            TypeElement clazz,
            List<? extends TypeMirror> hardenedClassTypes,
            Map<Name, TypeMirror> classTypeMap,
            ProcessingEnvironment environment) {
        Elements elements = environment.getElementUtils();
        
        for (TypeMirror iFace : clazz.getInterfaces()) {
            TypeElement iFaceElement = (TypeElement) environment.getTypeUtils().asElement(iFace);
            String iFaceQualifiedName = nameToString(elements.getBinaryName(iFaceElement));
            if (!Factory.class.getName().equals(iFaceQualifiedName)) continue;
            
            DeclaredType dtIFace = (DeclaredType) iFace;
            
            for (TypeMirror paramElement : dtIFace.getTypeArguments()) {
                Element retVal = environment.getTypeUtils().asElement(paramElement);
                
                if (retVal instanceof TypeElement) {
                    // Hard-coded type in the superclass
                    return (TypeElement) retVal;
                }
                
                if (hardenedClassTypes == null) {
                    throw new AssertionError("Error analyzing " + originalClazz +
                            ": Unspecified generic type of Factory in " + clazz + " interface " + iFace);
                }
                
                if (!(retVal instanceof TypeParameterElement)) {
                    throw new AssertionError("Error analyzing " + originalClazz +
                            ": Unknown generic type of Factory: " + retVal.getKind() + " of element " + retVal);
                }
                
                TypeParameterElement tpe = (TypeParameterElement) retVal;
                
                boolean found = false;
                int count = 0;
                for (TypeParameterElement candidate : clazz.getTypeParameters()) {
                    if (tpe.equals(candidate)) {
                        found = true;
                        break;
                    }
                    count++;
                }
                
                if (!found) {
                    throw new AssertionError("Error analyzing " + originalClazz +
                            ":  Internal error: mismatch between candidates (" + clazz.getTypeParameters() + ") and subclass (" +
                            hardenedClassTypes + ")");
                }
                
                TypeMirror transposedMirror = hardenedClassTypes.get(count);
                Element transposedElement = environment.getTypeUtils().asElement(transposedMirror);
                
                if (!(transposedElement instanceof TypeElement)) {
                    throw new AssertionError("Error analyzing " + originalClazz +
                            ": Factory type not specified fully, cannot analyze " + clazz + " interface " + iFace + " type " + transposedElement);
                }
                
                return (TypeElement) transposedElement;
            }
        }
        
        // If we get here, Factory was not directly implemented by the class.  Must now check
        // the superclass
        
        DeclaredType superClassDeclaredType = (DeclaredType) clazz.getSuperclass();
        TypeElement superClazz = (TypeElement) environment.getTypeUtils().asElement(superClassDeclaredType);
        
        List<? extends TypeMirror> superClassDeclaredTypes = superClassDeclaredType.getTypeArguments();
        
        List<? extends TypeMirror> hardenedList = superClassDeclaredTypes;
        if (hardenedClassTypes != null) {
            List<TypeMirror> translatedList = new ArrayList<TypeMirror>(superClassDeclaredTypes.size());
            
            for (TypeMirror scTPE : superClassDeclaredType.getTypeArguments()) {
                Name scTPEName = environment.getTypeUtils().asElement(scTPE).getSimpleName();
                TypeMirror replacement = classTypeMap.get(scTPEName);
                if (replacement == null) {
                    translatedList.add(scTPE);
                }
                else {
                    translatedList.add(replacement);
                }
            }
            
            hardenedList = (List<? extends TypeMirror>) translatedList;
        }
        
        Map<Name, TypeMirror> typeMap = new LinkedHashMap<Name, TypeMirror>();
       
        int position = 0;
        for (TypeParameterElement tpe : superClazz.getTypeParameters()) {
            typeMap.put(tpe.getSimpleName(), hardenedList.get(position));
            position++;
        }
        
        return findFactory(originalClazz, superClazz, hardenedList, typeMap, environment);
    }
    
    private static void generateFromClass(
            DescriptorImpl retVal,
            Element clazz,
            Set<String> contracts,
            ProcessingEnvironment processingEnvironment) {
        for (String contract : contracts) {
            retVal.addAdvertisedContract(contract);
        }
        
        LinkedHashMap<String, List<String>> metadata = new LinkedHashMap<String, List<String>>();
        
        retVal.setScope(getScope(clazz, metadata, processingEnvironment));
        String name = getName(clazz, processingEnvironment);
        if (name != null) {
            retVal.setName(name);
        }
        
        for (String qualifier : getAllQualifiers(clazz, metadata, processingEnvironment)) {
            retVal.addQualifier(qualifier);
        }
        
        retVal.setDescriptorVisibility(getVisibility(clazz, processingEnvironment));
        retVal.setRanking(getRank(clazz, processingEnvironment));
        retVal.setProxiable(getUseProxy(clazz, processingEnvironment));
        retVal.setProxyForSameScope(getProxyForSameScope(clazz, processingEnvironment));
        
        String analyzer = getAnalyzer(clazz, processingEnvironment);
        if (analyzer != null) {
            retVal.setClassAnalysisName(analyzer);
        }
        
        getServiceMetadata(clazz, metadata, processingEnvironment);
        
        retVal.setMetadata(metadata);
    }
    
    private static String getScope(Element clazz, LinkedHashMap<String, List<String>> metadata, ProcessingEnvironment processingEnv) {
        List<? extends AnnotationMirror> annotationMirrors = clazz.getAnnotationMirrors();
        
        Elements elements = processingEnv.getElementUtils();
        
        String foundScope = null;
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            DeclaredType dt = annotationMirror.getAnnotationType();
            TypeElement annotationType = (TypeElement) dt.asElement();
            
            if (annotationType.getAnnotation(Scope.class) != null) {
                if (foundScope != null) {
                    String qName = "provide";
                    if (clazz instanceof TypeElement) {
                        qName = nameToString(elements.getBinaryName((TypeElement) clazz));
                    }
                    throw new AssertionError("A service with implementation " + qName +
                            " has at least two scopes: " +
                            foundScope + " and " + elements.getBinaryName(annotationType));
                }
                
                foundScope = nameToString(elements.getBinaryName(annotationType));
                getMetadataFromAnnotation(annotationMirror, annotationType, metadata, processingEnv);
            }
        }
        
        if (foundScope != null) return foundScope;
        
        if (clazz instanceof ExecutableElement) {
            return PerLookup.class.getName();
        }
        
        return Singleton.class.getName();
    }
    
    private static String getName(Element clazz, ProcessingEnvironment processingEnvironment) {
        if (clazz.getAnnotation(Named.class) != null) {
            // Named wins
            AnnotationMirror namedMirror = getAnnotation(clazz, Named.class.getName(), processingEnvironment);
            AnnotationValue namedValue = getValueFromAnnotation(namedMirror, processingEnvironment);
            
            String value = (String) namedValue.getValue();
            if ("".equals(value)) {
                if (clazz instanceof ExecutableElement) {
                    throw new AssertionError("A provide method is annotated with @Named but in Factory "
                        + clazz.getEnclosingElement());   
                }
                
                return nameToString(clazz.getSimpleName());
            }
            
            return value;
        }
        
        AnnotationMirror serviceMirror = getAnnotation(clazz, Service.class.getName(), processingEnvironment);
        if (serviceMirror == null) return null;
        
        AnnotationValue serviceValue = getValueFromAnnotation(serviceMirror, "name", processingEnvironment);
        
        String value = (String) serviceValue.getValue();
        if ("".equals(value)) return null;
        return value;
    }
    
    private static Set<String> getAllQualifiers(Element clazz, LinkedHashMap<String, List<String>> metadata, ProcessingEnvironment processingEnv) {
        LinkedHashSet<String> retVal = new LinkedHashSet<String>();
        
        Elements elements = processingEnv.getElementUtils();
        
        List<? extends AnnotationMirror> annotations = clazz.getAnnotationMirrors();
        for (AnnotationMirror annoMirror : annotations) {
            DeclaredType dt = annoMirror.getAnnotationType();
            TypeElement dtElement = (TypeElement) dt.asElement();
            
            if (dtElement.getAnnotation(Qualifier.class) != null) {
                retVal.add(nameToString(elements.getBinaryName(dtElement)));
                
                getMetadataFromAnnotation(annoMirror, dtElement, metadata, processingEnv);
            }
        }
        
        return retVal;
    }
    
    private static DescriptorVisibility getVisibility(Element clazz, ProcessingEnvironment processingEnv) {
        AnnotationMirror mirror = getAnnotation(clazz, Visibility.class.getName(), processingEnv);
        if (mirror == null) return DescriptorVisibility.NORMAL;
        
        AnnotationValue annoValue = getValueFromAnnotation(mirror, processingEnv);
        if (annoValue == null) return DescriptorVisibility.NORMAL;
        
        VariableElement enumValue = (VariableElement) annoValue.getValue();
        String simpleName = nameToString(enumValue.getSimpleName());
        if (simpleName == null) return DescriptorVisibility.NORMAL;
        if ("LOCAL".equals(simpleName)) return DescriptorVisibility.LOCAL;
        
        return DescriptorVisibility.NORMAL;
    }
    
    private static int getRank(Element clazz, ProcessingEnvironment processingEnv) {
        AnnotationMirror mirror = getAnnotation(clazz, Rank.class.getName(), processingEnv);
        if (mirror == null) return 0;
        
        AnnotationValue annoValue = getValueFromAnnotation(mirror, processingEnv);
        if (annoValue == null) return 0;
        
        Integer r = (Integer) annoValue.getValue();
        return r;
    }
    
    private static Boolean getUseProxy(Element clazz, ProcessingEnvironment processingEnv) {
        AnnotationMirror mirror = getAnnotation(clazz, UseProxy.class.getName(), processingEnv);
        if (mirror == null) return null;
        
        AnnotationValue annoValue = getValueFromAnnotation(mirror, processingEnv);
        if (annoValue == null) return null;
        
        return (Boolean) annoValue.getValue();
    }
    
    private static Boolean getProxyForSameScope(Element clazz, ProcessingEnvironment processingEnv) {
        AnnotationMirror mirror = getAnnotation(clazz, ProxyForSameScope.class.getName(), processingEnv);
        if (mirror == null) return null;
        
        AnnotationValue annoValue = getValueFromAnnotation(mirror, processingEnv);
        if (annoValue == null) return null;
        
        return (Boolean) annoValue.getValue();
    }
    
    private static String getAnalyzer(Element clazz, ProcessingEnvironment processingEnv) {
        AnnotationMirror mirror = getAnnotation(clazz, Service.class.getName(), processingEnv);
        if (mirror == null) return null;
        
        AnnotationValue annoValue = getValueFromAnnotation(mirror, "analyzer", processingEnv);
        if (annoValue == null) return null;
        
        String retVal = (String) annoValue.getValue();
        if (ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME.equals(retVal)) return null;
        
        return retVal;
    }
    
    private static void getServiceMetadata(Element clazz, Map<String, List<String>> metadata, ProcessingEnvironment processingEnv) {
        AnnotationMirror mirror = getAnnotation(clazz, Service.class.getName(), processingEnv);
        if (mirror == null) return;
        
        AnnotationValue annoValue = getValueFromAnnotation(mirror, "metadata", processingEnv);
        if (annoValue == null) return;
        
        String retVal = (String) annoValue.getValue();
        if ("".equals(retVal)) return;
        
        ReflectionHelper.parseServiceMetadataString(retVal, metadata);
    }
    
    @SuppressWarnings("unchecked")
    private static Set<String> getAllContracts(TypeElement clazz, ProcessingEnvironment processingEnvironment) {
        if (clazz == null) return Collections.emptySet();
        
        Elements elements = processingEnvironment.getElementUtils();
        
        ContractsProvided provided = clazz.getAnnotation(ContractsProvided.class);
        if (provided != null) {
            LinkedHashSet<String> retVal = new LinkedHashSet<String>();
            
            AnnotationMirror contractsProvided = getAnnotation(clazz, ContractsProvided.class.getName(), processingEnvironment);
            AnnotationValue annoValue = getValueFromAnnotation(contractsProvided, processingEnvironment);
            
            List<? extends AnnotationValue> arrayValues = (List<? extends AnnotationValue>) annoValue.getValue();
            for (AnnotationValue providedValues : arrayValues) {
                TypeMirror providedMirror = (TypeMirror) providedValues.getValue();
                        
                TypeElement providedElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(providedMirror);
                         
                retVal.add(nameToString(elements.getBinaryName(providedElement)));
            }
            
            return retVal;
        }
        
        LinkedHashSet<String> retVal = new LinkedHashSet<String>();
        
        retVal.add(nameToString(elements.getBinaryName(clazz)));
        
        getAllSubContracts(clazz, processingEnvironment, retVal, new LinkedHashSet<String>());
        
        return retVal;
    }
    
    private static void getAllSubContracts(TypeElement clazz,
            ProcessingEnvironment processingEnvironment,
            LinkedHashSet<String> contracts,
            LinkedHashSet<String> cycleDetector) {
        Elements elements = processingEnvironment.getElementUtils();
        
        if (clazz == null || nameToString(elements.getBinaryName(clazz)).equals(Object.class.getName())) return;
        
        if (cycleDetector.contains(nameToString(elements.getBinaryName(clazz)))) return;
        cycleDetector.add(nameToString(elements.getBinaryName(clazz)));
        
        List<? extends TypeMirror> interfaceMirrors = clazz.getInterfaces();
        for (TypeMirror mirror : interfaceMirrors) {
            TypeElement iFace = (TypeElement) processingEnvironment.getTypeUtils().asElement(mirror);
            
            if (isAContract(iFace)) {
                contracts.add(nameToString(elements.getBinaryName(iFace)));
            }
            
            getAllSubContracts(iFace, processingEnvironment, contracts, cycleDetector);
        }
        
        TypeMirror superClazzMirror = clazz.getSuperclass();
        if (superClazzMirror != null) {
            TypeElement superClass = (TypeElement) processingEnvironment.getTypeUtils().asElement(superClazzMirror);
            
            if (isAContract(superClass)) {
                contracts.add(nameToString(elements.getBinaryName(superClass)));
            }
            
            getAllSubContracts(superClass, processingEnvironment, contracts, cycleDetector);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void getMetadataFromAnnotation(AnnotationMirror annotation,
            TypeElement annotationType,
            LinkedHashMap<String, List<String>> metadata,
            ProcessingEnvironment processingEnvironment) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> methods = annotation.getElementValues();
        
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : methods.entrySet()) {
            ExecutableElement ee = entry.getKey();
            AnnotationMirror metadataMirror = getAnnotation(ee, Metadata.class.getName(), processingEnvironment);
            if (metadataMirror == null) continue;
            
            AnnotationValue metadataAnnotationKey = getValueFromAnnotation(metadataMirror, processingEnvironment);
            if (metadataAnnotationKey == null) continue;
            
            String metadataKey = (String) metadataAnnotationKey.getValue();
            if (metadataKey == null) continue;
            
            AnnotationValue methodAnnotationValue = entry.getValue();
            Object methodObjectValue = methodAnnotationValue.getValue();
            
            if (methodObjectValue instanceof List) {
                List<? extends AnnotationValue> annoList = (List<? extends AnnotationValue>) methodObjectValue;
                
                for (AnnotationValue annoListValue : annoList) {
                    Object aValue = annoListValue.getValue();
                    
                    addToMetadataMap(metadataKey, aValue.toString(), metadata);
                }
            }
            else {
                addToMetadataMap(metadataKey, methodObjectValue.toString(), metadata);
            }
        }
        
    }
    
    private static void addToMetadataMap(String key, String value, Map<String, List<String>> metadata) {
        List<String> values = metadata.get(key);
        if (values == null) {
            values = new LinkedList<String>();
            metadata.put(key, values);
        }
        
        values.add(value);
    }
    
    private static boolean isAContract(TypeElement element) {
        if (element == null) return false;
        
        Contract contract = element.getAnnotation(Contract.class);
        if (contract == null) return false;
        return true;
    }
    
    public static String nameToString(Name name) {
        if (name == null) return null;
        return name.toString();
    }
    
    private static AnnotationMirror getAnnotation(Element clazz, String clazzName, ProcessingEnvironment processingEnv) {
        Elements elements = processingEnv.getElementUtils();
        
        List<? extends AnnotationMirror> annotationMirrors = clazz.getAnnotationMirrors();
        for (AnnotationMirror annoMirror : annotationMirrors) {
            DeclaredType dt = annoMirror.getAnnotationType();
            TypeElement dtElement = (TypeElement) dt.asElement();
            
            if (clazzName.equals(nameToString(elements.getBinaryName(dtElement)))) {
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
