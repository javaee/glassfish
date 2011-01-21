/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.configuration.introspection.anyreally;


import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.util.ResourceLocator;
import org.junit.Assert; 

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Simple test to use the locator pattern.
 */
public class LocatorTest {

    public void testLocator() throws Exception {

        final String classAsResource = "org/glassfish/hk2/tests/configuration/introspection/anyreally/LocatorTest.class";


        URL url = this.getClass().getClassLoader().getResource(classAsResource);
        if (url==null) {
            throw new RuntimeException("Cannot determine test directory");
        }
        String path = url.getPath().substring(0, url.getPath().length() - classAsResource.length());
        File modelDir = new File(path);
        //File modelDir = new File(userDir, "target" + File.separator + "classes");

        ParsingContext.Builder builder = new ParsingContext.Builder();
        builder.locator(new ResourceLocator() {
            @Override
            public URL getResource(String name) {
                return getClass().getClassLoader().getResource(name);
            }
        });
        ParsingContext pc = builder.build();
        Parser parser = new Parser(pc);

        parser.parse(modelDir, null);
        Exception[] exceptions = parser.awaitTermination(100, TimeUnit.SECONDS);
        if (exceptions!=null) {
            for (Exception e : exceptions) {
                System.out.println("Found Exception ! : " +e);
            }
            Assert.assertTrue("Exceptions returned", exceptions.length==0);
        }

        AnnotationType at = pc.getTypes().getBy(AnnotationType.class, "org.jvnet.hk2.annotations.Contract");
        for (AnnotatedElement ae : at.allAnnotatedTypes()) {
            System.out.println(ae.getName() + " is a contract ");
            if (ae instanceof InterfaceModel) {
                InterfaceModel im = (InterfaceModel) ae;
                for (ClassModel cm : im.allImplementations()) {
                    if (cm.getAnnotation("org.jvnet.hk2.annotations.Service")!=null) {
                        System.out.println("And  " + cm.getName() + " is a service provider ");
                    }
                }
            }
        }
    }
}
