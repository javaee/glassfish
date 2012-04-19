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
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Proxiable;
import org.jvnet.hk2.annotations.Contract;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;

/**
 * @author jwells
 *
 */
public class Utilities {
    private final static String CLASS_PATH_PROPERTY = "java.class.path";
    private final static String DOT_CLASS = ".class";
    
    private final Map<String, Boolean> ISA_CONTRACT = new HashMap<String, Boolean>();
    private final Map<String, Boolean> ISA_SCOPE = new HashMap<String, Boolean>();
    private final Map<String, Boolean> ISA_QUALIFIER = new HashMap<String, Boolean>();
    private final Map<String, String> FOUND_SUPERCLASS = new HashMap<String, String>();  // Terminal is null
    
    private final String CONFIGURED_CONTRACT = "org.jvnet.hk2.config.Configured";
    
    /* package */ Utilities() {
        // We can pre-load the cache with some obvious ones and thus reduce searching quite a bit
        ISA_CONTRACT.put(Factory.class.getName(), true);
        ISA_CONTRACT.put(Context.class.getName(), true);
        ISA_CONTRACT.put(ErrorService.class.getName(), true);
        ISA_CONTRACT.put(Singleton.class.getName(), false);
        ISA_CONTRACT.put(PerLookup.class.getName(), false);
        ISA_CONTRACT.put(Named.class.getName(), false);
        ISA_CONTRACT.put(Contract.class.getName(), false);
        ISA_CONTRACT.put(CONFIGURED_CONTRACT, true);
        ISA_CONTRACT.put(Scope.class.getName(), false);
        ISA_CONTRACT.put(Target.class.getName(), false);
        ISA_CONTRACT.put(Retention.class.getName(), false);
        ISA_CONTRACT.put(Proxiable.class.getName(), false);
        ISA_CONTRACT.put(Annotation.class.getName(), false);
        
        ISA_SCOPE.put(Singleton.class.getName(), true);
        ISA_SCOPE.put(PerLookup.class.getName(), true);
        ISA_SCOPE.put(Factory.class.getName(), false);
        ISA_SCOPE.put(Context.class.getName(), false);
        ISA_SCOPE.put(ErrorService.class.getName(), false);
        ISA_SCOPE.put(Named.class.getName(), false);
        ISA_SCOPE.put(Contract.class.getName(), false);
        ISA_SCOPE.put(CONFIGURED_CONTRACT, false);
        ISA_SCOPE.put(Scope.class.getName(), false);
        ISA_SCOPE.put(Target.class.getName(), false);
        ISA_SCOPE.put(Retention.class.getName(), false);
        ISA_SCOPE.put(Proxiable.class.getName(), false);
        ISA_SCOPE.put(Annotation.class.getName(), false);
        
        ISA_QUALIFIER.put(Named.class.getName(), true);
        ISA_QUALIFIER.put(Singleton.class.getName(), false);
        ISA_QUALIFIER.put(PerLookup.class.getName(), false);
        ISA_QUALIFIER.put(Factory.class.getName(), false);
        ISA_QUALIFIER.put(Context.class.getName(), false);
        ISA_QUALIFIER.put(ErrorService.class.getName(), false);
        ISA_QUALIFIER.put(Contract.class.getName(), false);
        ISA_QUALIFIER.put(CONFIGURED_CONTRACT, false);
        ISA_QUALIFIER.put(Scope.class.getName(), false);
        ISA_QUALIFIER.put(Target.class.getName(), false);
        ISA_QUALIFIER.put(Retention.class.getName(), false);
        ISA_QUALIFIER.put(Proxiable.class.getName(), false);
        ISA_QUALIFIER.put(Annotation.class.getName(), false);
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
    private InputStream findClass(File searchHere, String dotDelimitedName, boolean searchClassPath) throws IOException {
        if (searchHere.isDirectory()) {
            String properPathName = dotDelimitedName.replace('.', File.separatorChar) + DOT_CLASS;
            
            File fullFile = new File(searchHere, properPathName);
            
            if (fullFile.exists()) {
                return new FileInputStream(fullFile);
            }
        }
        else {
            JarFile jar = new JarFile(searchHere);
            
            String entryName = dotDelimitedName.replace('.', '/') + DOT_CLASS;
            ZipEntry entry = jar.getEntry(entryName);
            if (entry != null) {
                return jar.getInputStream(entry);
            }
        }
        
        if (!searchClassPath) return null;
        
        // Handle classpath
        String classpath = System.getProperty(CLASS_PATH_PROPERTY);
        if (classpath == null) return null;
        
        
        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String pathElement = st.nextToken();
            
            File nextSearchGuy = new File(pathElement);
            InputStream is = findClass(nextSearchGuy, dotDelimitedName, false);
            if (is != null) {
                return is;
            }
        }
        
        return null;
    }
    
    /**
     * Returns true if the given class is a contract
     * 
     * @param searchHere The file or jar to look in
     * @param dotDelimitedName The fully qualified class name to look for
     * @return true if this can determine that this is a contract
     */
    public boolean isClassAContract(File searchHere, String dotDelimitedName) {
        if (ISA_CONTRACT.containsKey(dotDelimitedName)) {
            return ISA_CONTRACT.get(dotDelimitedName);
        }
        
        try {
            InputStream is = findClass(searchHere, dotDelimitedName, true);
            
            ClassReader reader = new ClassReader(is);
            
            ContractClassVisitor ccv = new ContractClassVisitor(Contract.class.getName().replace('.', '/'));
            
            reader.accept(ccv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            boolean retVal = ccv.isALookedForThing();
            
            // Record result
            ISA_CONTRACT.put(dotDelimitedName, retVal);
            
            return retVal;
        }
        catch (IOException ioe) {
            ISA_CONTRACT.put(dotDelimitedName, false);
            
            return false;
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
        
        try {
            InputStream is = findClass(searchHere, dotDelimitedName, true);
            
            ClassReader reader = new ClassReader(is);
            
            ContractClassVisitor ccv = new ContractClassVisitor(null);
            
            reader.accept(ccv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            String retVal = ccv.getDotDelimitedSuperclass();
            
            // Record result
            FOUND_SUPERCLASS.put(dotDelimitedName, retVal);
            
            return retVal;
        }
        catch (IOException ioe) {
            FOUND_SUPERCLASS.put(dotDelimitedName, null);
            
            return null;
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
        
        try {
            InputStream is = findClass(searchHere, dotDelimitedName, true);
            
            ClassReader reader = new ClassReader(is);
            
            ContractClassVisitor ccv = new ContractClassVisitor(Scope.class.getName().replace('.', '/'));
            
            reader.accept(ccv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            boolean retVal = ccv.isALookedForThing();
            
            ISA_SCOPE.put(dotDelimitedName, retVal);
            
            return retVal;
        }
        catch (IOException ioe) {
            // Error on the side of not a contract
            return false;
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
        
        try {
            InputStream is = findClass(searchHere, dotDelimitedName, true);
            
            ClassReader reader = new ClassReader(is);
            
            ContractClassVisitor ccv = new ContractClassVisitor(Qualifier.class.getName().replace('.', '/'));
            
            reader.accept(ccv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
            boolean retVal = ccv.isALookedForThing();
            
            ISA_QUALIFIER.put(dotDelimitedName, retVal);
            
            return retVal;
        }
        catch (IOException ioe) {
            // Error on the side of not a contract
            return false;
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
            try {
                InputStream is = findClass(searchHere, dotDelimitedName, true);
            
                ClassReader reader = new ClassReader(is);
            
                ContractFinderClassVisitor cfcv = new ContractFinderClassVisitor(this, searchHere);
            
                reader.accept(cfcv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            
                retVal.addAll(cfcv.getAllContracts());
            }
            catch (IOException ioe) {
              // Don't add anything to the return
            }
            
            dotDelimitedName = getSuperclass(searchHere, dotDelimitedName);
        }
        
        return retVal;
    }
    
    private static class ContractClassVisitor extends AbstractClassVisitorImpl {
        private boolean isContract = false;
        private final String lookForMe;
        private String dotDelimitedSuperclass;
        
        private ContractClassVisitor(String lookForMe) {
            this.lookForMe = lookForMe;
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
            if (superName == null) return;
            
            dotDelimitedSuperclass = superName.replace('/', '.');
            
            if (Object.class.getName().equals(dotDelimitedSuperclass)) {
                dotDelimitedSuperclass = null;
            }
            
        }
        
        /* (non-Javadoc)
         * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean arg1) {
            if (lookForMe != null && desc.contains(lookForMe)) {
                isContract = true;
            }
            
            return null;
        }
        
        private boolean isALookedForThing() {
            return isContract;
        }
        
        private String getDotDelimitedSuperclass() {
            return dotDelimitedSuperclass;
        }
        
    }
    
    private static class ContractFinderClassVisitor extends AbstractClassVisitorImpl {
        private final Set<String> allContracts = new LinkedHashSet<String>();
        private final File searchHere;
        private final Utilities utilities;
        
        private ContractFinderClassVisitor(Utilities utilities, File searchHere) {
            this.utilities = utilities;
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
            for (String iFace : interfaces) {
                String iWithDots = iFace.replace('/', '.');
                if (utilities.isClassAContract(searchHere, iWithDots)) {
                    allContracts.add(iWithDots);
                    
                }
            }

        }
        
        private Set<String> getAllContracts() {
            return allContracts;
        }
        
        
    }

}
