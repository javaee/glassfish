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
package org.jvnet.hk2.generator.internal;

import java.io.File;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.external.org.objectweb.asm.AnnotationVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.MethodVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.Type;

/**
 * @author jwells
 *
 */
public class ClassVisitorImpl extends AbstractClassVisitorImpl {
    private final static String SERVICE_CLASS_FORM = "Lorg/jvnet/hk2/annotations/Service;";
    private final static String CONTRACTS_PROVIDED_CLASS_FORM = "Lorg/jvnet/hk2/annotations/ContractsProvided;";
    private final static String RANK_CLASS_FORM = "Lorg/glassfish/hk2/api/Rank;";
    private final static String CONFIGURED_CLASS_FORM = "Lorg/jvnet/hk2/config/Configured;";
    private final static String DECORATE_CLASS_FORM = "Lorg/jvnet/hk2/annotations/Decorate;";
    private final static String USE_PROXY_CLASS_FORM = "Lorg/glassfish/hk2/api/UseProxy;";
    private final static String VISIBILITY_CLASS_FORM = "Lorg/glassfish/hk2/api/Visibility;";
    private final static String NAME = "name";
    private final static String METADATA = "metadata";
    private final static String VALUE = "value";
    private final static String PROVIDE = "provide";
    private final static String LOCAL = "LOCAL";
    private final static String ANALYZER = "analyzer";
    
    /**
     * Must be the same value as from the GenerateServiceFromMethod value
     */
    private final static String METHOD_ACTUAL = "MethodListActual";
    
    /**
     * Must be the same value as from the GenerateServiceFromMethod value
     */
    public final static String METHOD_NAME = "MethodName";
    
    /**
     * Must be the same value as from the GenerateServiceFromMethod value
     */
    public final static String PARENT_CONFIGURED = "ParentConfigured";
    
    private final boolean verbose;
    private final File searchHere;
    private final Utilities utilities;
    
    private String implName;
    private final LinkedHashSet<String> iFaces = new LinkedHashSet<String>();
    private LinkedHashSet<String> providedContracts;
    private String scopeClass;
    private final LinkedList<String> qualifiers = new LinkedList<String>();
    private boolean isAService = false;
    private boolean isConfigured = false;
    private NamedAnnotationVisitor baseName;
    private String metadataString = null;
    private Integer rank = null;
    private Boolean useProxy = null;
    private DescriptorVisibility visibility = DescriptorVisibility.NORMAL;
    private final Map<String, List<String>> metadata = new HashMap<String, List<String>>();
    private String classAnalyzer = ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME;
    
    private final LinkedList<DescriptorImpl> generatedDescriptors = new LinkedList<DescriptorImpl>();
    private boolean isFactory = false;
    private boolean factoryMethodFound = false;
    private DecorateData decorateData;
    private final Map<String, GenerateMethodAnnotationData> classLevelGenerators =
            new HashMap<String, GenerateMethodAnnotationData>();
    
    /**
     * Creates this with the config to add to if this is a service
     * 
     * @param utilities The utilities class to use for this visitor (preserves cache)
     * @param verbose true if we should print out any service we are binding
     * @param searchHere if we cannot classload something directly, search for it here
     */
    public ClassVisitorImpl(Utilities utilities, boolean verbose, File searchHere) {
        this.utilities = utilities;
        this.verbose = verbose;
        this.searchHere = searchHere;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public void visit(int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces) {
        implName = name.replace("/", ".");
        
        iFaces.addAll(utilities.getAssociatedContracts(searchHere, implName));
        
        if (iFaces.contains(Factory.class.getName())) {
            isFactory = true;
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (!visible) return null;
        
        if (SERVICE_CLASS_FORM.equals(desc)) {
            isAService = true;
            
            return new ServiceAnnotationVisitor();
        }
        
        if (CONTRACTS_PROVIDED_CLASS_FORM.equals(desc)) {
            providedContracts = new LinkedHashSet<String>();
            
            return new ContractsProvidedAnnotationVisitor();
        }
        
        if (RANK_CLASS_FORM.equals(desc)) {
            return new RankAnnotationVisitor();
        }
        
        if (CONFIGURED_CLASS_FORM.equals(desc)) {
            isConfigured = true;
        }
        
        if (DECORATE_CLASS_FORM.equals(desc)) {
            return new DecorateAnnotationVisitor();
        }
        
        if (USE_PROXY_CLASS_FORM.equals(desc)) {
            return new UseProxyAnnotationVisitor();
        }
        
        if (VISIBILITY_CLASS_FORM.equals(desc)) {
            return new VisibilityAnnotationVisitor();
        }
        
        if (!desc.startsWith("L")) return null;
            
        String loadQualifierName = desc.substring(1, desc.length() -1).replace("/", ".");
        if (utilities.isClassAScope(searchHere, loadQualifierName)) {
            if (scopeClass != null) {
                throw new AssertionError("A service with implementation " + implName + " has at least two scopes: " +
                  scopeClass + " and " + loadQualifierName);
            }
            scopeClass = loadQualifierName;
            
            return new MetadataAnnotationVisitor(loadQualifierName);
        }
        
        if (utilities.isClassAQualifier(searchHere, loadQualifierName)) {
            qualifiers.add(loadQualifierName);
            
            if (Named.class.getName().equals(loadQualifierName)) {
                baseName = new NamedAnnotationVisitor(getDefaultName(), null);
                return baseName;
            }
            
            return new MetadataAnnotationVisitor(loadQualifierName);
        }
        
        GenerateMethodAnnotationData gmad = utilities.isClassAGenerator(searchHere, loadQualifierName);
        if (gmad != null) {
            gmad = new GenerateMethodAnnotationData(gmad);
            classLevelGenerators.put(loadQualifierName, gmad);
            
            if (gmad.getNameMethodName() != null) {
                AnnotationVisitor retVal = new GeneratedNameMethodFinderVisitor(gmad);
                return retVal;
            }
        }
        
        return null;
    }
    
    private String getDefaultName() {
        if (implName == null) return "";
        int index = implName.lastIndexOf('.');
        if (index <= 0) return implName;
        
        return implName.substring(index + 1);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        if (!isAService) {
            if (decorateData != null) {
                String with = decorateData.getWith();
                
                GenerateMethodAnnotationData gbad = classLevelGenerators.get(with);
                
                if (gbad != null) {
                    DescriptorImpl generatedDescriptor = new DescriptorImpl();
                    generatedDescriptor.setImplementation(gbad.getImplementation());
                    
                    for (String contract : gbad.getContracts()) {
                        generatedDescriptor.addAdvertisedContract(contract);
                    }
                    
                    if (gbad.getName() != null) {
                        generatedDescriptor.setName(gbad.getName());
                    }
                    
                    generatedDescriptor.addMetadata(METHOD_ACTUAL, implName);
                    generatedDescriptor.addMetadata(METHOD_NAME, decorateData.getMethodName());
                    generatedDescriptor.addMetadata(PARENT_CONFIGURED, decorateData.getTargetType());
                    
                    if (verbose) {
                        System.out.println("Generated Descriptor for class-level GenerateServiceFromMethod annotation: " +
                            generatedDescriptor);
                    };
                    
                    generatedDescriptors.add(generatedDescriptor);
                    
                    return;
                }
                
            }
            
            if (verbose) {
                System.out.println("Class " + implName + " is not annotated with @Service");
            }
            return;
        }
        
        DescriptorImpl generatedDescriptor = new DescriptorImpl();
        generatedDescriptor.setImplementation(implName);
        if (scopeClass == null) {
            // The default for classes with Service is Singleton
            generatedDescriptor.setScope(Singleton.class.getName());
        }
        else {
            generatedDescriptor.setScope(scopeClass);
        }
        
        if (providedContracts != null) {
            for (String providedContract : providedContracts) {
                generatedDescriptor.addAdvertisedContract(providedContract);
            }
            
        }
        else {
            generatedDescriptor.addAdvertisedContract(implName);
            for (String iFace : iFaces) {
                generatedDescriptor.addAdvertisedContract(iFace);
            }
        }
        
        for (String qualifier : qualifiers) {
            generatedDescriptor.addQualifier(qualifier);
        }
        
        if (baseName != null) {
            generatedDescriptor.setName(baseName.getName());
        }
        
        generatedDescriptor.setClassAnalysisName(classAnalyzer);
        if (metadataString != null) {
            Map<String, List<String>> serviceMetadata = new HashMap<String, List<String>>();
            
            ReflectionHelper.parseServiceMetadataString(metadataString, serviceMetadata);
            
            generatedDescriptor.addMetadata(serviceMetadata);
        }
        
        if (rank != null) {
            generatedDescriptor.setRanking(rank.intValue());
        }
        
        if (useProxy != null) {
            generatedDescriptor.setProxiable(useProxy);
        }
        
        generatedDescriptor.setDescriptorVisibility(visibility);
        
        if (!metadata.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : metadata.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                
                for (String value : values) {
                    generatedDescriptor.addMetadata(key, value);
                }
            }
        }
        
        if (verbose) {
            System.out.println("Generated Descriptor: " + generatedDescriptor);
        }
        
        generatedDescriptors.add(generatedDescriptor);
    }
    
    private MethodVisitor visitConfiguredMethod(int access, String name, String desc, String signature, String[] exceptions) {
        String methodListActual = Utilities.getListActualType(signature);
        if (methodListActual == null) {
            if (signature != null) return null;
            
            methodListActual = Utilities.getFirstParameterType(desc);
            if (methodListActual == null) return null;
        }
        
        // OK, well, we have a reasonable candidate now, lets check its annotations
        return new ConfiguredMethodVisitor(name, methodListActual, implName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        if (isConfigured) return visitConfiguredMethod(access, name, desc, signature, exceptions);
        
        if (!isAService) return null;
        if (!isFactory) return null;
        if (!PROVIDE.equals(name)) return null;
        if (!desc.startsWith("()")) return null;
        if (factoryMethodFound) return null;
        factoryMethodFound = true;
        
        DescriptorImpl asAFactory = new DescriptorImpl();
        generatedDescriptors.add(asAFactory);
        
        asAFactory.setImplementation(implName);
        asAFactory.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        
        String factoryType = desc.substring(2);
        if (factoryType.charAt(0) == '[') {
            // Array type, may not be of an object!
            asAFactory.addAdvertisedContract(factoryType);  // Just the array of whatever type
        }
        else {
            if (factoryType.charAt(0) != 'L') {
                throw new AssertionError("Unable to handle provide descriptor " + desc);
            }
            
            int endIndex = factoryType.indexOf(';');
            if (endIndex < 0) {
                throw new AssertionError("Unable to find end of class return type in descriptor " + desc);
            }
            
            String trueFactoryClass = factoryType.substring(1, endIndex);
            
            // This might be parametererized, strip of the parameters
            trueFactoryClass = trueFactoryClass.replace('/', '.');
            
            Set<String> associatedContracts = utilities.getAssociatedContracts(
                    searchHere, trueFactoryClass);
            
            for (String contract : associatedContracts) {
                asAFactory.addAdvertisedContract(contract);
            }
        }
        
        return new MethodVisitorImpl(asAFactory);
    }
    
    private class ServiceAnnotationVisitor extends AbstractAnnotationVisitorImpl {

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
         */
        @Override
        public void visit(String annotationName, Object value) {
            if (annotationName.equals(NAME)) {
                baseName = new NamedAnnotationVisitor(null, (String) value);
            }
            else if (annotationName.equals(METADATA)) {
                metadataString = (String) value;
            }
            else if (annotationName.equals(ANALYZER)) {
                classAnalyzer = (String) value;
            }
        }        
    }
    
    private static class NamedAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        private final String defaultName;
        private boolean nameSet = false;
        private String name;
        
        public NamedAnnotationVisitor(String defaultName, String name) {
            this.defaultName = defaultName;
            this.name = name;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
         */
        @Override
        public void visit(String annotationName, Object value) {
            if (annotationName.equals(VALUE)) {
                name = (String) value;
                nameSet = true;
            }
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            if (nameSet) return;
            
            name = defaultName;
        }
        
        private String getName() {
            return name;
        }
        
    }
    
    private class MethodVisitorImpl extends AbstractMethodVisitorImpl {
        private final DescriptorImpl asAFactoryDI;
        private NamedAnnotationVisitor factoryName;
        
        private MethodVisitorImpl(DescriptorImpl asAFactoryDI) {
            this.asAFactoryDI = asAFactoryDI; 
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.MethodVisitor#visitAnnotation(java.lang.String, boolean)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (!desc.startsWith("L")) return null;
            
            String loadQualifierName = desc.substring(1, desc.length() -1).replace("/", ".");
            if (utilities.isClassAScope(searchHere, loadQualifierName)) {
                asAFactoryDI.setScope(loadQualifierName);
            }
            else if (utilities.isClassAQualifier(searchHere, loadQualifierName)) {
                asAFactoryDI.addQualifier(loadQualifierName);
                
                if (Named.class.getName().equals(loadQualifierName)) {
                    factoryName = new NamedAnnotationVisitor(getDefaultName(), null);
                    return factoryName;
                }
            }
            else if (desc.equals(RANK_CLASS_FORM)) {
                return new MethodRankAnnotationVisitor(asAFactoryDI);
            }
            else if (desc.equals(USE_PROXY_CLASS_FORM)) {
                return new MethodUseProxyAnnotationVisitor(asAFactoryDI);
            }
            else if (desc.equals(VISIBILITY_CLASS_FORM)) {
                return new MethodVisibilityAnnotationVisitor(asAFactoryDI);
            }
            
            return null;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.MethodVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            if (factoryName != null && factoryName.getName() != null) {
                asAFactoryDI.setName(factoryName.getName());
            }
            
            if (verbose) {
                System.out.println("Adding a factory descriptor: " + asAFactoryDI);
            }
        }        
    }
    
    private class ContractsProvidedAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public void visit(String name, Object value) {
            if (value == null) return;
            if (!(value instanceof Type)) return;
            
            providedContracts.add(((Type) value).getClassName());
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
         */
        @Override
        public AnnotationVisitor visitArray(String name) {
            if (!VALUE.equals(name)) {
                return null;
            }
            
            return this;
        }        
    }
    
    private class RankAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public void visit(String name, Object value) {
            rank = (Integer) value;
        }      
    }
    
    private static class MethodRankAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        private final DescriptorImpl di;
        
        private MethodRankAnnotationVisitor(DescriptorImpl di) {
            this.di = di;
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public void visit(String name, Object value) {
            di.setRanking(((Integer) value).intValue());
        }      
    }
    
    private class MetadataAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        private final String scopeOrQualifierName;
        private final String arrayName;
        
        private MetadataAnnotationVisitor(String scopeOrQualifierName) {
            this(scopeOrQualifierName, null);
        }
        
        private MetadataAnnotationVisitor(String scopeOrQualifierName, String arrayName) {
            this.scopeOrQualifierName = scopeOrQualifierName;
            this.arrayName = arrayName;
        }
        
        @Override
        public void visit(String name, Object value) {
            if (name == null) name = arrayName;
            String metadataKey = utilities.getMetadataKey(scopeOrQualifierName, name);
            
            if (metadataKey != null) {
                String valueString;
                if (value instanceof Type) {
                    Type type = (Type) value;
                    
                    valueString = type.getClassName();
                }
                else {
                    String valueClassName = value.getClass().getName();
                    if (valueClassName.startsWith("[")) {
                        int length = Array.getLength(value);
                        for (int lcv = 0; lcv < length; lcv++) {
                            Object member = Array.get(value, lcv);
                            
                            ReflectionHelper.addMetadata(metadata, metadataKey, member.toString());
                        }
                        
                        return;
                    }
                    
                    valueString = value.toString();
                }
                
                ReflectionHelper.addMetadata(metadata, metadataKey, valueString);
            }
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
         */
        @Override
        public AnnotationVisitor visitArray(String name) {
            return new MetadataAnnotationVisitor(scopeOrQualifierName, name);
        }
    }
    
    private class ConfiguredMethodVisitor extends AbstractMethodVisitorImpl {
        private final String methodName;
        private final String actualType;
        private final String parentConfigured;
        
        private final List<GenerateMethodAnnotationData> allAnnotationDataToAdd =
                new LinkedList<GenerateMethodAnnotationData>();
        
        private ConfiguredMethodVisitor(String methodName, String actualType, String parentConfigured) {
            this.methodName = methodName;
            this.actualType = actualType;
            this.parentConfigured = parentConfigured;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.MethodVisitor#visitAnnotation(java.lang.String, boolean)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            String loadAnnotationName = desc.substring(1, desc.length() -1).replace("/", ".");
            GenerateMethodAnnotationData generateData = utilities.isClassAGenerator(searchHere, loadAnnotationName);
            
            if (generateData == null) return null;
            
            allAnnotationDataToAdd.add(generateData);
            
            if (generateData.getNameMethodName() == null) {
                return null;
            }
            
            return new GeneratedNameMethodFinderVisitor(generateData);
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.MethodVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            for (GenerateMethodAnnotationData methodGenerated : allAnnotationDataToAdd) {
                DescriptorImpl di = new DescriptorImpl();
                di.setImplementation(methodGenerated.getImplementation());
                for (String contract : methodGenerated.getContracts()) {
                    di.addAdvertisedContract(contract);
                }
                di.setScope(methodGenerated.getScope());
                if (methodGenerated.getName() != null) {
                    di.setName(methodGenerated.getName());
                }
                
                di.addMetadata(METHOD_ACTUAL, actualType);
                di.addMetadata(METHOD_NAME, methodName);
                di.addMetadata(PARENT_CONFIGURED, parentConfigured);
                
                if (verbose) {
                    System.out.println("Generated Descriptor for GenerateServiceFromMethod annotation: " + di);
                }
                
                generatedDescriptors.add(di);
            }
            
        }
        
    }
    
    private static class GeneratedNameMethodFinderVisitor extends AbstractAnnotationVisitorImpl {
        private final GenerateMethodAnnotationData annoData;
        
        private GeneratedNameMethodFinderVisitor(GenerateMethodAnnotationData annoData) {
            this.annoData = annoData;
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public void visit(String name, Object value) {
            if (name == null || value == null) return;
            
            if (annoData.getNameMethodName().equals(name)) {
                if (!(value instanceof String)) return;
                
                annoData.setName((String) value);
            }
        }
        
    }
    
    private final static String DECORATE_TARGET_TYPE = "targetType";
    private final static String DECORATE_METHOD_NAME = "methodName";
    private final static String DECORATE_WITH = "with";  // tinsel
    
    private class DecorateAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        private String targetType;
        private String methodName;
        private String with;
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public void visit(String name, Object value) {
            if (DECORATE_TARGET_TYPE.equals(name)) {
                Type t = (Type) value;
                
                targetType = t.getClassName();
            }
            else if (DECORATE_METHOD_NAME.equals(name)) {
                methodName = (String) value;
            }
            else if (DECORATE_WITH.equals(name)) {
                Type t = (Type) value;
                
                with = t.getClassName();
            }
        }
        
        @Override
        public void visitEnd() {
            decorateData = new DecorateData(targetType, methodName, with);
        }
        
    }
    
    private class UseProxyAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        @Override
        public void visit(String name, Object value) {
            useProxy = (Boolean) value;
        }
        
        @Override
        public void visitEnd() {
            if (useProxy == null) useProxy = Boolean.TRUE;
        }
        
    }
    
    private class VisibilityAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        @Override
        public void visitEnum(String name, String v0, String v1) {
            if (v1 != null && LOCAL.equals(v1)) {
                visibility = DescriptorVisibility.LOCAL;
            }
        }
        
    }
    
    private static class MethodUseProxyAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        private final DescriptorImpl desc;
        
        private MethodUseProxyAnnotationVisitor(DescriptorImpl desc) {
            this.desc = desc;
        }
        
        @Override
        public void visit(String name, Object value) {
            desc.setProxiable((Boolean) value);
        }
        
        @Override
        public void visitEnd() {
            if (desc.isProxiable() == null) desc.setProxiable(Boolean.TRUE);
        }
        
    }
    
    private static class MethodVisibilityAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        private final DescriptorImpl desc;
        
        private MethodVisibilityAnnotationVisitor(DescriptorImpl desc) {
            this.desc = desc;
        }
        
        @Override
        public void visitEnum(String name, String v0, String v1) {
            if (v1 != null && LOCAL.equals(v1)) {
                desc.setDescriptorVisibility(DescriptorVisibility.LOCAL);
            }
        }
    }
    
    private static class DecorateData {
        private final String targetType;
        private final String methodName;
        private final String with;
        
        private DecorateData(String targetType, String methodName, String with) {
            this.targetType = targetType;
            this.methodName = methodName;
            this.with = with;
        }
        
        private String getTargetType() {
            return targetType;
        }
        
        private String getMethodName() {
            return methodName;
        }
        
        private String getWith() {
            return with;
        }
        
        public String toString() {
            return "DecorateData(" + targetType + "," + methodName + "," + with + "," +
                System.identityHashCode(this) + ")";
        }
    }
    
    /**
     * Gets the generated descriptor created by this visitor
     * 
     * @return The descriptor generated by this visitor, or null if the
     * class was not annotated with &#64;Service
     */
    public List<DescriptorImpl> getGeneratedDescriptor() {
        return generatedDescriptors;
    }
}
