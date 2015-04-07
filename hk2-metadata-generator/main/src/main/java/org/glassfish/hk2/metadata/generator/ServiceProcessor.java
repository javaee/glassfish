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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * The entry point for service &#64;Service annotations
 * 
 * @author jwells
 *
 */
@SupportedAnnotationTypes("org.jvnet.hk2.annotations.Service")
@SupportedOptions("org.glassfish.hk2.metadata.location")
public class ServiceProcessor extends AbstractProcessor {
    private static final String LOCATION_OPTION = "org.glassfish.hk2.metadata.location";
    private static final String LOCATION_DEFAULT = "META-INF/hk2-locator/default";
    
    private final TreeSet<DescriptorImpl> allDescriptors = new TreeSet<DescriptorImpl>(new DescriptorComparitor());
    private final ArrayList<Element> originators = new ArrayList<Element>();

    /* (non-Javadoc)
     * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        MultiException collectedExceptions = null;
        for (TypeElement annotation : annotations) {
            Set<? extends Element> clazzes = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (Element clazzElement : clazzes) {
                if (!(clazzElement instanceof TypeElement)) continue;
                
                TypeElement clazz = (TypeElement) clazzElement;
                
                List<DescriptorImpl> descriptors;
                try {
                    descriptors = ServiceUtilities.getDescriptorsFromClass(clazz, processingEnv);
                }
                catch (Throwable th) {
                    if (collectedExceptions == null) {
                        collectedExceptions = new MultiException(th);
                    }
                    else {
                        collectedExceptions.addError(th);
                    }
                    
                    continue;
                }
                
                allDescriptors.addAll(descriptors);
                if (!descriptors.isEmpty()) {
                    originators.add(clazzElement);
                }
            }
        }
        
        if (collectedExceptions != null) {
            processingEnv.getMessager().printMessage(Kind.ERROR, collectedExceptions.getMessage());
            collectedExceptions.printStackTrace();
            return true;
        }
        
        if (allDescriptors.isEmpty()) return true;
        if (!roundEnv.processingOver()) return true;
        
        try {
            createFile();
        }
        catch (IOException e) {
            processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    private void createFile() throws IOException {
        String location = processingEnv.getOptions().get(LOCATION_OPTION);
        if (location == null) location = LOCATION_DEFAULT;
        location = location.trim();
        
        if (location.startsWith("/")) {
            throw new IOException("The " + LOCATION_OPTION +
                    " option to hk2-inhabitant-locator must be a relative path, it was " + location);
        }
        
        Filer filer = processingEnv.getFiler();
        FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT,
                "", location, 
                originators.toArray(new Element[originators.size()]));
        
        Writer fileWriter = fileObject.openWriter();
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(fileWriter);
            
            printWriter.println("#");
            printWriter.println("# Generated by hk2-metadata-generator");
            printWriter.println("#");
            printWriter.println();
            
            for (DescriptorImpl di : allDescriptors) {
                di.writeObject(printWriter);
            }
            
        }
        finally {
            if (printWriter != null) {
                printWriter.close();
            }
            
            fileWriter.close();
        }
        
    }
    
    /**
     * This is a comparator making things that don't really compare, compare.
     * It is done to ensure that given the same set of descriptors we always
     * return the set in the same order, which will ensure that the output
     * of the generator is not different from run to run
     * 
     * @author jwells
     *
     */
    private static class DescriptorComparitor implements Comparator<DescriptorImpl> {
        private static <T> int safeCompare(Comparable<T> a, T b) {
            if (a == null && b == null) return 0;
            if (a == null) return -1;
            if (b == null) return 1;
            
            return a.compareTo(b);
        }
        
        private static int compareStringMaps(Set<String> s1, Set<String> s2) {
            int size1 = s1.size();
            int size2 = s2.size();
            
            if (size1 != size2) return (size1 - size2);
            
            TreeSet<String> s1sorted = new TreeSet<String>(s1);
            TreeSet<String> s2sorted = new TreeSet<String>(s2);
            
            StringBuffer s1b = new StringBuffer();
            for (String s1sv : s1sorted) {
                s1b.append(s1sv);
            }
            
            StringBuffer s2b = new StringBuffer();
            for (String s2sv : s2sorted) {
                s2b.append(s2sv);
            }
            
            return safeCompare(s1b.toString(), s2b.toString());
        }

        @Override
        public int compare(DescriptorImpl o1, DescriptorImpl o2) {
            int retVal = o2.getRanking() - o1.getRanking();
            if (retVal != 0) return retVal;
            
            retVal = safeCompare(o1.getImplementation(), o2.getImplementation());
            if (retVal != 0) return retVal;
            
            retVal = safeCompare(o1.getName(), o2.getName());
            if (retVal != 0) return retVal;
            
            retVal = safeCompare(o1.getScope(), o2.getScope());
            if (retVal != 0) return retVal;
            
            retVal = compareStringMaps(o1.getAdvertisedContracts(), o2.getAdvertisedContracts());
            if (retVal != 0) return retVal;
            
            retVal = compareStringMaps(o1.getQualifiers(), o2.getQualifiers());
            if (retVal != 0) return retVal;
            
            retVal = o1.getDescriptorType().compareTo(o2.getDescriptorType());
            if (retVal != 0) return retVal;
            
            retVal = o1.getDescriptorVisibility().compareTo(o2.getDescriptorVisibility());
            if (retVal != 0) return retVal;
            
            return 0;
        }
        
    }
}
