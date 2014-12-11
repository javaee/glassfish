/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.internal;

import java.lang.annotation.Annotation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.jaxb.internal.XmlRootElementImpl;

/**
 * @author jwells
 *
 */
public class JAUtilities {

    private static void brainDump() throws Exception {
        /*
        ProxyFactory pf = new ProxyFactory();
        pf.setSuperclass(BaseHK2JAXBBean.class);
        pf.setInterfaces(new Class[] { Museum.class });
        
        Class<?> mfclass = pf.createClass();
        byte[] mfClassAsBytes = serialize(mfclass);
        
        
        ByteArrayClassPath ccp = new ByteArrayClassPath(mfclass.getName(), mfClassAsBytes);
        System.out.println("JRW(-01) dcp=" + defaultClassPool);
        defaultClassPool.insertClassPath(ccp);
        System.out.println("JRW(01) dcp=" + defaultClassPool);
        
        ClassPool defaultClassPool = ClassPool.getDefault();
        
        CtClass superClazz = defaultClassPool.get(BaseHK2JAXBBean.class.getName());
        // CtClass museumClazz = defaultClassPool.get(Museum.class.getName());
        
        
        // String museumClassName = Museum.class.getName() + "_$$_hk2_jaxb";
        
        // CtClass museumCtClass = defaultClassPool.makeClass(museumClassName);
        // ClassFile mcf = museumCtClass.getClassFile();
        
        // museumCtClass.setSuperclass(superClazz);
        // museumCtClass.addInterface(museumClazz);
        
        Annotation xmlRootElement = new XmlRootElementImpl("##default", "museum");
        // AnnotationsAttribute aa = (AnnotationsAttribute) mcf.getAttribute(AnnotationsAttribute.visibleTag);
        // aa.addAnnotation(xmlRootElement);
        
        // System.out.println("JRW(05) museumCtClass=" + museumCtClass);
        
        {
            CtMethod nameGetMethod =
                CtNewMethod.make("public java.lang.String getName() { return (java.lang.String) super._getProperty(\"name\"); }", museumCtClass);
            
            // nameGetMethod.setAttribute(name, data);
        
            // museumCtClass.addMethod(nameGetMethod);
        
            CtMethod nameSetMethod =
                CtNewMethod.make("public void setName(java.lang.String arg0) { super._setProperty(\"name\", arg0); }", museumCtClass);
        
            // museumCtClass.addMethod(nameSetMethod);
        }
        
        {
            CtMethod nameGetMethod =
                    CtNewMethod.make("public int getId() { java.lang.Integer i = (java.lang.Integer) super._getProperty(\"id\"); return i.intValue(); }", museumCtClass);
            
            // museumCtClass.addMethod(nameGetMethod);
            
            CtMethod nameSetMethod =
                    CtNewMethod.make("public void setId(int arg0) { super._setProperty(\"id\", new java.lang.Integer(arg0)); }", museumCtClass);
            
            // museumCtClass.addMethod(nameSetMethod);
        }
        
        {
            CtMethod nameGetMethod =
                    CtNewMethod.make("public int getAge() { java.lang.Integer i = (java.lang.Integer) super._getProperty(\"age\"); return i.intValue(); }", museumCtClass);
            
            museumCtClass.addMethod(nameGetMethod);
            
            CtMethod nameSetMethod =
                    CtNewMethod.make("public void setAge(int arg0) { super._setProperty(\"age\", new java.lang.Integer(arg0)); }", museumCtClass);
            
            museumCtClass.addMethod(nameSetMethod);
        }
        
        Class<?> proxy = museumCtClass.toClass(Museum.class.getClassLoader(), Museum.class.getProtectionDomain());
        
        System.out.println("JRW(10) who knows? proxy=" + proxy.getName());
        */
    }

}
