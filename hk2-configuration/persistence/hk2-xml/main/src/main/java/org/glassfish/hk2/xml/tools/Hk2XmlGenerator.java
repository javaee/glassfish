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
package org.glassfish.hk2.xml.tools;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.glassfish.hk2.xml.internal.Generator;
import org.glassfish.hk2.xml.internal.alt.papi.TypeElementAltClassImpl;

/**
 * @author jwells
 *
 */
@SupportedAnnotationTypes("org.glassfish.hk2.xml.api.annotations.Hk2XmlPreGenerate")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Hk2XmlGenerator extends AbstractProcessor {
    private final ClassPool defaultClassPool;
    private final CtClass superClazz;
    
    public Hk2XmlGenerator() {
        super();
        
        defaultClassPool = new ClassPool(true);
        
        ClassLoader localLoader = getClass().getClassLoader();
        if (!(localLoader instanceof URLClassLoader)) {
            throw new RuntimeException("Unknown classloader: " + localLoader);
        }
        
        @SuppressWarnings("resource")
        URLClassLoader urlLoader = (URLClassLoader) localLoader;
        
        for (URL url : urlLoader.getURLs()) {
            URI uri;
            try {
                uri = url.toURI();
            }
            catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            
            File asFile = new File(uri);
            try {
                defaultClassPool.appendClassPath(asFile.getAbsolutePath());
            }
            catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        
        try {
            superClazz = defaultClassPool.get("org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean");
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();
        
        for (TypeElement annotation : annotations) {
            Set<? extends Element> clazzes = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (Element clazzElement : clazzes) {
                if (!(clazzElement instanceof TypeElement)) continue;
                
                TypeElement clazz = (TypeElement) clazzElement;
                
                TypeElementAltClassImpl altClass = new TypeElementAltClassImpl(clazz, processingEnv);
                
                try {
                    CtClass ctClass = Generator.generate(altClass, superClazz, defaultClassPool);
                    
                    String ctClassName = ctClass.getName();
                    
                    JavaFileObject jfo = filer.createClassFile(ctClassName, clazzElement);
                    
                    OutputStream outputStream = jfo.openOutputStream();
                    DataOutputStream dataOutputStream = null;
                    try {
                        dataOutputStream = new DataOutputStream(outputStream);
                    
                        ctClass.toBytecode(dataOutputStream);
                    }
                    finally {
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                        
                        outputStream.close();
                    }
                }
                catch (Throwable e) {
                    String msg = e.getMessage();
                    if (msg == null) msg = "Exception of type " + e.getClass().getName();
                
                    processingEnv.getMessager().printMessage(Kind.ERROR, "While processing class: " + clazz.getQualifiedName() + " got exeption: " + msg);
                    e.printStackTrace();
                }
            }
        }
        
        return true;
    }

}
