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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;

import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ErrorService;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Metadata;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Proxiable;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;

/**
 * @author jwells
 *
 */
public class Utilities {
    private final static String DOT_CLASS = ".class";
    private final static String CONTRACT_WITH_SLASHES = "L" + Contract.class.getName().replace('.', '/') + ";";
    private final static String SCOPE_WITH_SLASHES = "L" + Scope.class.getName().replace('.', '/') + ";";
    private final static String QUALIFIER_WITH_SLASHES = "L" + Qualifier.class.getName().replace('.', '/') + ";";
    private final static String METHOD_GENERATOR_WITH_SLASHES = "Lorg/jvnet/hk2/config/GenerateServiceFromMethod;";
    private final static String GENERATOR_NAME_FIELD_WITH_SLASHES = "Lorg/jvnet/hk2/config/GeneratedServiceName;";
    
    private final static String METADATA_DESC = "Lorg/glassfish/hk2/api/Metadata;";
    
    private final Map<String, Boolean> ISA_CONTRACT = new HashMap<String, Boolean>();
    private final Map<String, Boolean> ISA_SCOPE = new HashMap<String, Boolean>();
    private final Map<String, Boolean> ISA_QUALIFIER = new HashMap<String, Boolean>();
    private final Map<String, String> FOUND_SUPERCLASS = new HashMap<String, String>();  // Terminal is null
    private final Map<String, Set<String>> FOUND_INTERFACES = new HashMap<String, Set<String>>();
    private final Map<String, GenerateMethodAnnotationData> FOUND_GENERATORS = new HashMap<String, GenerateMethodAnnotationData>();
    
    // Map from scope or qualifier name to method name to metadata key value
    private final Map<String, Map<String, String>> METADATA =
            new HashMap<String, Map<String, String>>();
    
    private final boolean verbose;
    private final String searchPath;
    
    private final static String CONFIGURED_CONTRACT = "org.jvnet.hk2.config.Configured";
    
    private final static List<KnownClassData> KNOWN_DATA = new LinkedList<KnownClassData>();
    
    static {
        Set<String> empty = Collections.emptySet();
        
        // Factory
        KNOWN_DATA.add(new KnownClassData(Factory.class.getName(),
                true, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Context
        KNOWN_DATA.add(new KnownClassData(Context.class.getName(),
                true, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // ErrorService
        KNOWN_DATA.add(new KnownClassData(ErrorService.class.getName(),
                true, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Singleton
        KNOWN_DATA.add(new KnownClassData(Singleton.class.getName(),
                false, // isa_contract
                true, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // PerLookup
        KNOWN_DATA.add(new KnownClassData(PerLookup.class.getName(),
                false, // isa_contract
                true, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Named
        KNOWN_DATA.add(new KnownClassData(Named.class.getName(),
                false, // isa_contract
                false, // isa_scope
                true, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Contract
        KNOWN_DATA.add(new KnownClassData(Contract.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // @Configured
        KNOWN_DATA.add(new KnownClassData(CONFIGURED_CONTRACT,
                true, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Scope
        KNOWN_DATA.add(new KnownClassData(Scope.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Target
        KNOWN_DATA.add(new KnownClassData(Target.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Retention
        KNOWN_DATA.add(new KnownClassData(Retention.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Proxiable
        KNOWN_DATA.add(new KnownClassData(Proxiable.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Annotation
        KNOWN_DATA.add(new KnownClassData(Annotation.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Qualifier
        KNOWN_DATA.add(new KnownClassData(Qualifier.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Documented
        KNOWN_DATA.add(new KnownClassData(Documented.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Inherited
        KNOWN_DATA.add(new KnownClassData(Inherited.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Inherited
        KNOWN_DATA.add(new KnownClassData(ContractsProvided.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Rank
        KNOWN_DATA.add(new KnownClassData(Rank.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
        
        // Metadata
        KNOWN_DATA.add(new KnownClassData(Metadata.class.getName(),
                false, // isa_contract
                false, // isa_scope
                false, // isa_qualifier
                null,  // superclass
                empty  // interfaces
                ));
                
    }
    
    /* package */ Utilities(boolean verbose, String searchPath) {
        this.verbose = verbose;
        this.searchPath = searchPath;
        
        // We can pre-load the cache with some obvious ones and thus reduce searching quite a bit
        for (KnownClassData kcd : KNOWN_DATA) {
            ISA_CONTRACT.put(kcd.getClazz(), kcd.isIsa_contract());
            ISA_SCOPE.put(kcd.getClazz(), kcd.isIsa_scope());
            ISA_QUALIFIER.put(kcd.getClazz(), kcd.isIsa_qualifier());
            FOUND_SUPERCLASS.put(kcd.getClazz(), kcd.getSuperclass());
            FOUND_INTERFACES.put(kcd.getClazz(), kcd.getiFaces());
            FOUND_GENERATORS.put(kcd.getClazz(), null);
        }
        
        if (verbose && searchPath != null) {
            System.out.println("The elements of the path that will be searched is:");
            StringTokenizer st = new StringTokenizer(searchPath, File.pathSeparator);
            int lcv = 1;
            while (st.hasMoreTokens()) {
                String pathElement = st.nextToken();
                
                System.out.println(" " + lcv++ + ". " + pathElement);
            }
            System.out.println("Finished printing search path elements");
            
        }
    }
    
    /**
     * Will look for a file on a last-ditch sort of effort using the searchHere thing (jar or directory)
     * and then also in the classpath
     * 
     * @param searchHere
     * @param dotDelimitedName
     * @param searchClassPath true if the classpath should be searched as well
     * @return an IOStream if the file could be located
     * @throws IOException
     */
    private InputStream findClass(File searchHere, String dotDelimitedName, boolean searchClassPath, String calledFrom) throws IOException {
        if (verbose && searchClassPath) {
            System.out.println("Looking for " + dotDelimitedName + " for discovery of " + calledFrom);
        }
        
        if (searchHere.isDirectory()) {
            String properPathName = dotDelimitedName.replace('.', File.separatorChar) + DOT_CLASS;
            
            File fullFile = new File(searchHere, properPathName);
            
            if (fullFile.exists()) {
                if (verbose) {
                    System.out.println("Found " + dotDelimitedName + " in " + searchHere.getAbsolutePath() + " for " + calledFrom);
                }
                return new FileInputStream(fullFile);
            }
        }
        else {
            JarFile jar = new JarFile(searchHere);
            
            String entryName = dotDelimitedName.replace('.', '/') + DOT_CLASS;
            ZipEntry entry = jar.getEntry(entryName);
            if (entry != null) {
                if (verbose) {
                    System.out.println("Found " + dotDelimitedName + " in jar " + searchHere.getAbsolutePath() + " for " + calledFrom);
                }
                
                return jar.getInputStream(entry);
            }
        }
        
        if (!searchClassPath) return null;
        
        // Handle classpath
        String classpath = searchPath;
        if (classpath == null) return null;
        
        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String pathElement = st.nextToken();
            
            File nextSearchGuy = new File(pathElement);
            InputStream is = findClass(nextSearchGuy, dotDelimitedName, false, calledFrom);
            if (is != null) {
                return is;
            }
        }
        
        if (verbose) {
            System.out.println("Did not find " + dotDelimitedName + " in classpath for " + calledFrom);
        }
        return null;
    }
    
    private void nullCaches(String dotDelimitedName) {
        ISA_CONTRACT.put(dotDelimitedName, false);
        ISA_SCOPE.put(dotDelimitedName, false);
        ISA_QUALIFIER.put(dotDelimitedName, false);
        FOUND_SUPERCLASS.put(dotDelimitedName, null);
        FOUND_GENERATORS.put(dotDelimitedName, null);
    }
    
    /**
     * Returns true if the given class is a contract
     * 
     * @param searchHere The file or jar to look in
     * @param dotDelimitedName The fully qualified class name to look for
     * @return true if this can determine that this is a contract
     */
    private boolean isClassAContract(File searchHere, String dotDelimitedName) {
        if (ISA_CONTRACT.containsKey(dotDelimitedName)) {
            return ISA_CONTRACT.get(dotDelimitedName);
        }
        
        InputStream is = null;
        try {
            is = findClass(searchHere, dotDelimitedName, true, "isaContract");
            if (is == null) {
                nullCaches(dotDelimitedName);
                
                return false;
            }
            
            ClassReader reader = new ClassReader(is);
            
            ContractClassVisitor ccv = new ContractClassVisitor(searchHere, CONTRACT_WITH_SLASHES, dotDelimitedName);
            
            reader.accept(ccv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            return ccv.isALookedForThing();
        }
        catch (IOException ioe) {
            nullCaches(dotDelimitedName);
            
            return false;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * Returns true if the given class is a contract
     * 
     * @param searchHere The file or jar to look in
     * @param dotDelimitedName The fully qualified class name to look for
     * @return The dot-delimited superclass name or null if this is terminal (is
     *   an interface or extends java.lang.Object)
     */
    private String getSuperclass(File searchHere, String dotDelimitedName) {
        if (FOUND_SUPERCLASS.containsKey(dotDelimitedName)) {
            return FOUND_SUPERCLASS.get(dotDelimitedName);
        }
        
        InputStream is = null;
        try {
            is = findClass(searchHere, dotDelimitedName, true, "superclass");
            if (is == null) {
                nullCaches(dotDelimitedName);
                
                return null;
            }
            
            ClassReader reader = new ClassReader(is);
            
            ContractClassVisitor ccv = new ContractClassVisitor(searchHere, null, dotDelimitedName);
            
            reader.accept(ccv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            return ccv.getDotDelimitedSuperclass();
        }
        catch (IOException ioe) {
            nullCaches(dotDelimitedName);
            
            return null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * Returns true if it can be determined that this class is a scope
     * 
     * @param searchHere
     * @param dotDelimitedName
     * @return true if this class is a scope
     */
    public boolean isClassAScope(File searchHere, String dotDelimitedName) {
        if (ISA_SCOPE.containsKey(dotDelimitedName)) {
            return ISA_SCOPE.get(dotDelimitedName);
        }
        
        InputStream is = null;
        try {
            is = findClass(searchHere, dotDelimitedName, true, "isascope");
            if (is == null) {
                nullCaches(dotDelimitedName);
                
                return false;
            }
            
            ClassReader reader = new ClassReader(is);
            
            ContractClassVisitor ccv = new ContractClassVisitor(searchHere, SCOPE_WITH_SLASHES, dotDelimitedName);
            
            reader.accept(ccv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            return ccv.isALookedForThing();
        }
        catch (IOException ioe) {
            // Error on the side of not a contract
            nullCaches(dotDelimitedName);
            
            return false;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * Returns true if it can be determined that this class is a qualifier
     * 
     * @param searchHere
     * @param dotDelimitedName
     * @return true if this class is a qualifier
     */
    public boolean isClassAQualifier(File searchHere, String dotDelimitedName) {
        if (ISA_QUALIFIER.containsKey(dotDelimitedName)) {
            return ISA_QUALIFIER.get(dotDelimitedName);
        }
        
        InputStream is = null;
        try {
            is = findClass(searchHere, dotDelimitedName, true, "isaQualifier");
            if (is == null) {
                nullCaches(dotDelimitedName);
                
                return false;
            }
            
            ClassReader reader = new ClassReader(is);
            
            ContractClassVisitor ccv = new ContractClassVisitor(searchHere, QUALIFIER_WITH_SLASHES, dotDelimitedName);
            
            reader.accept(ccv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            return ccv.isALookedForThing();
        }
        catch (IOException ioe) {
            // Error on the side of not a contract
            nullCaches(dotDelimitedName);
            
            return false;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * Returns true if it can be determined that this class is a generator
     * 
     * @param searchHere
     * @param dotDelimitedName
     * @return true if this class is a qualifier
     */
    public GenerateMethodAnnotationData isClassAGenerator(File searchHere, String dotDelimitedName) {
        if (FOUND_GENERATORS.containsKey(dotDelimitedName)) {
            return FOUND_GENERATORS.get(dotDelimitedName);
        }
        
        InputStream is = null;
        try {
            is = findClass(searchHere, dotDelimitedName, true, "isaMethodGenerator");
            if (is == null) {
                nullCaches(dotDelimitedName);
                
                return null;
            }
            
            ClassReader reader = new ClassReader(is);
            
            GeneratorClassVisitor gcv = new GeneratorClassVisitor(dotDelimitedName);
            
            reader.accept(gcv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            return gcv.getGenerateMethodAnnotationData();
        }
        catch (IOException ioe) {
            // Error on the side of not a contract
            nullCaches(dotDelimitedName);
            
            return null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    private void getAssociatedSuperclassContracts(File searchHere, String dotDelimitedName, Set<String> addToMe) {
        if (!addToMe.contains(dotDelimitedName) && isClassAContract(searchHere, dotDelimitedName)) {
            addToMe.add(dotDelimitedName);
        }
        
        String dotDelimitedSuperclass = getSuperclass(searchHere, dotDelimitedName);
        if (dotDelimitedSuperclass != null) {
            getAssociatedSuperclassContracts(searchHere, dotDelimitedSuperclass, addToMe);
        }
    }
    
    /**
     * Gets the contracts associated with the name passed in
     * @param searchHere
     * @param dotDelimitedName
     * @return The set of contracts associated with this dotDelimited name (ordered iterator)
     */
    public Set<String> getAssociatedContracts(File searchHere, String dotDelimitedName) {
        LinkedHashSet<String> retVal = new LinkedHashSet<String>();
        retVal.add(dotDelimitedName);
        
        getAssociatedSuperclassContracts(searchHere, dotDelimitedName, retVal);
        
        while (dotDelimitedName != null) {
            // getAssociatedSuperclassContracts is guaranteed to fill in the INTERFACES cache
            Set<String> allInterfaces = FOUND_INTERFACES.get(dotDelimitedName);
            if (allInterfaces == null) {
                dotDelimitedName = getSuperclass(searchHere, dotDelimitedName);
                
                continue;
            }
            
            for (String dotDelimitedInterface : allInterfaces) {
                if (isClassAContract(searchHere, dotDelimitedInterface)) {
                    retVal.add(dotDelimitedInterface);  
                }
                
                addSubInterface(searchHere, dotDelimitedInterface, retVal);
            }
            
            dotDelimitedName = getSuperclass(searchHere, dotDelimitedName);
        }
        
        return retVal;
    }
    
    private void addSubInterface(File searchHere, String dotDelimitedInterface, LinkedHashSet<String> retVal) {
        Set<String> subInterfaces = FOUND_INTERFACES.get(dotDelimitedInterface);
        if (subInterfaces == null) return;
        
        for (String dotDelimitedSubInterface : subInterfaces) {
            if (isClassAContract(searchHere, dotDelimitedSubInterface)) {
                retVal.add(dotDelimitedSubInterface);
            }
            
            addSubInterface(searchHere, dotDelimitedSubInterface, retVal);
        }
    }
    
    private class ContractClassVisitor extends AbstractClassVisitorImpl {
        private final String cacheKey;
        private final String lookForMe;
        private final File searchHere;
        
        private final Map<String, String> methodNameToMetadataKey =
                new HashMap<String, String>();
        
        private boolean isLookedFor = false;
        
        private boolean isContract = false;
        private boolean isScope = false;
        private boolean isQualifier = false;
        
        private String dotDelimitedSuperclass;
        
        private ContractClassVisitor(File searchHere, String lookForMe, String cacheKey) {
            this.searchHere = searchHere;
            this.lookForMe = lookForMe;
            this.cacheKey = cacheKey;
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
            if (!FOUND_INTERFACES.containsKey(cacheKey)) {
                LinkedHashSet<String> iFaces = new LinkedHashSet<String>();
                
                for (String iFace : interfaces) {
                    String iWithDots = iFace.replace('/', '.');
                    iFaces.add(iWithDots);
                    
                    // Fill in the cache with all sub interfaces
                    getSuperclass(searchHere, iWithDots);
                }
                
                FOUND_INTERFACES.put(cacheKey, iFaces);
            }
            
            if (superName == null) {
                FOUND_SUPERCLASS.put(cacheKey, null);
                
                return;
            }
            
            dotDelimitedSuperclass = superName.replace('/', '.');
            
            if (Object.class.getName().equals(dotDelimitedSuperclass)) {
                dotDelimitedSuperclass = null;
            }
            
            FOUND_SUPERCLASS.put(cacheKey, dotDelimitedSuperclass);
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean arg1) {
            if (lookForMe != null && desc.equals(lookForMe)) {
                isLookedFor = true;
            }
            
            if (desc.equals(CONTRACT_WITH_SLASHES)) {
                isContract = true;
            }
            
            if (desc.equals(SCOPE_WITH_SLASHES)) {
                isScope = true;
            }
            
            if (desc.equals(QUALIFIER_WITH_SLASHES)) {
                isQualifier = true;
            }
            
            if (desc.equals(QUALIFIER_WITH_SLASHES)) {
                isQualifier = true;
            }
            
            return null;
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
         */
        @Override
        public MethodVisitor visitMethod(int arg0, String name, String arg2,
                String arg3, String[] arg4) {
            return new ContractMethodVisitor(this, name);
        }
        
        public void visitEnd() {
            ISA_CONTRACT.put(cacheKey, isContract);
            ISA_SCOPE.put(cacheKey, isScope);
            ISA_QUALIFIER.put(cacheKey, isQualifier);
            
            if ((isScope || isQualifier) && !methodNameToMetadataKey.isEmpty()) {
                METADATA.put(cacheKey, methodNameToMetadataKey);
            }
        }
        
        private boolean isALookedForThing() {
            return isLookedFor;
        }
        
        private String getDotDelimitedSuperclass() {
            return dotDelimitedSuperclass;
        }
        
        private void associateMethodNameWithMetadataKey(
                String methodName,
                String metadataKey) {
            methodNameToMetadataKey.put(methodName, metadataKey);
        }
        
    }
    
    private static class ContractMethodVisitor extends AbstractMethodVisitorImpl {
        private final ContractClassVisitor parent;
        private final String methodName;
        private String metadataKey; 
        
        private ContractMethodVisitor(ContractClassVisitor parent, String methodName) {
            this.parent = parent;
            this.methodName = methodName;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.MethodVisitor#visitAnnotation(java.lang.String, boolean)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (!METADATA_DESC.equals(desc)) return null;
            
            return new MetadataAnnotationVisitor(this);
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.MethodVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            if (metadataKey != null) {
                parent.associateMethodNameWithMetadataKey(methodName, metadataKey); 
            }
            
        }
        
        private void setMetadataKey(String key) {
            metadataKey = key;
        }
    }
    
    private static class MetadataAnnotationVisitor extends AbstractAnnotationVisitorImpl {
        private final ContractMethodVisitor parent;
        
        private MetadataAnnotationVisitor(ContractMethodVisitor parent) {
            this.parent = parent;
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public void visit(String name, Object value) {
            parent.setMetadataKey((String) value);
        }
    }
    
    private class GeneratorClassVisitor extends AbstractClassVisitorImpl {
        private final String cacheKey;
        private GenerateMethodAnnotationData methodGenerateData = null;
        
        private GeneratorClassVisitor(String cacheKey) {
            this.cacheKey = cacheKey;
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean arg1) {
            if (desc.equals(METHOD_GENERATOR_WITH_SLASHES)) {
                return new GenerateServiceFromMethodVisitor(this);
            }
            
            return null;
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
         */
        @Override
        public MethodVisitor visitMethod(int access,
                String name,
                String desc,
                String signature,
                String[] exceptions) {
            if (methodGenerateData == null) {
                return null;
            }
            
            return new GeneratorNameFinderMethodVisitor(this, name);
        }
        
        private void setGenerateMethodAnnotationData(GenerateMethodAnnotationData methodGenerateData) {
            this.methodGenerateData = methodGenerateData;
            
        }
        
        private GenerateMethodAnnotationData getGenerateMethodAnnotationData() {
            return methodGenerateData;
        }
        
        private void setNameMethodName(String methodName) {
            methodGenerateData.setNameMethodName(methodName);
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.ClassVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            FOUND_GENERATORS.put(cacheKey, methodGenerateData);
        }
    }
    
    private final static String GENERATOR_IMPL_NAME = "implementation";
    private final static String GENERATOR_SCOPE_NAME = "scope";
    
    private static class GenerateServiceFromMethodVisitor extends AbstractAnnotationVisitorImpl {
        private final GeneratorClassVisitor parent;
        
        private String implementation;
        private String scope = PerLookup.class.getName();
        private final HashSet<String> contracts = new HashSet<String>();
        
        private GenerateServiceFromMethodVisitor(GeneratorClassVisitor parent) {
            this.parent = parent;
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public void visit(String name, Object value) {
            if (name == null) {
                // This is the contracts array
                contracts.add((String) value);
            }
            else if (GENERATOR_IMPL_NAME.equals(name)) {
                implementation = (String) value;
            }
            else if (GENERATOR_SCOPE_NAME.equals(name)) {
                scope = (String) value;
            }
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
         */
        @Override
        public AnnotationVisitor visitArray(String name) {
            return this;
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            parent.setGenerateMethodAnnotationData(
                    new GenerateMethodAnnotationData(implementation, contracts, scope));
        }
        
    }
    
    private static class GeneratorNameFinderMethodVisitor extends AbstractMethodVisitorImpl {
        private final String methodName;
        private final GeneratorClassVisitor parent;
        
        private GeneratorNameFinderMethodVisitor(GeneratorClassVisitor parent, String methodName) {
            this.parent = parent;
            this.methodName = methodName;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.MethodVisitor#visitAnnotation(java.lang.String, boolean)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (GENERATOR_NAME_FIELD_WITH_SLASHES.equals(desc)) {
                parent.setNameMethodName(methodName);
                
            }
            
            return null;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.MethodVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    private static class KnownClassData {
        private final String clazz;
        private final boolean isa_contract;
        private final boolean isa_scope;
        private final boolean isa_qualifier;
        private final String superclass;
        private final Set<String> iFaces;
        
        private KnownClassData(String clazz,
                boolean isa_contract,
                boolean isa_scope,
                boolean isa_qualifier,
                String superclass,
                Set<String> iFaces) {
            this.clazz = clazz;
            this.isa_contract = isa_contract;
            this.isa_scope = isa_scope;
            this.isa_qualifier = isa_qualifier;
            this.superclass = superclass;
            this.iFaces = iFaces;
        }

        /**
         * @return the clazz
         */
        String getClazz() {
            return clazz;
        }

        /**
         * @return the isa_contract
         */
        boolean isIsa_contract() {
            return isa_contract;
        }

        /**
         * @return the isa_scope
         */
        boolean isIsa_scope() {
            return isa_scope;
        }

        /**
         * @return the isa_qualifier
         */
        boolean isIsa_qualifier() {
            return isa_qualifier;
        }

        /**
         * @return the superclass
         */
        String getSuperclass() {
            return superclass;
        }

        /**
         * @return the iFaces
         */
        Set<String> getiFaces() {
            return iFaces;
        }
    }
    
    public String getMetadataKey(String scopeOrQualifier, String methodName) {
        Map<String, String> methodToKey = METADATA.get(scopeOrQualifier);
        if (methodToKey == null) return null;
        
        return methodToKey.get(methodName);
    }
    
    private final static String LIST_WITH_PTYPE = "Ljava/util/List<";
    
    /* package */ static String getListActualType(String signature) {
        if (signature == null) return null;
        if (!signature.contains(LIST_WITH_PTYPE)) return null;
        
        int ltIndex = signature.indexOf('<');
        int gtIndex = signature.indexOf('>');
        
        if (ltIndex < 0 || gtIndex < 0) return null;
        
        String pType = signature.substring(ltIndex + 1, gtIndex);
        if (!pType.startsWith("L")) return null;
        
        pType = pType.substring(1, pType.length() - 1);  // Remove L and ;
        
        return pType.replace('/', '.');
    }
    
    /* package */ static String getFirstParameterType(String desc) {
        if (desc == null) return null;
        if (!desc.startsWith("(L")) return null;
        int semiColonIndex = desc.indexOf(';');
        if (semiColonIndex < 0) return null;  // Badly formed desc?
        
        String postL = desc.substring(2, semiColonIndex);
        
        return postL.replace('/', '.');
    }
}
