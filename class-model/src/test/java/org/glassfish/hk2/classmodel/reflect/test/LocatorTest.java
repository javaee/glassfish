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
package org.glassfish.hk2.classmodel.reflect.test;

import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.impl.AnnotationTypeImpl;
import org.glassfish.hk2.classmodel.reflect.util.ResourceLocator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Simple test to use the locator pattern.
 */
public class LocatorTest {

    @Test
    @Ignore
    public void testLocator() throws IOException {
        List<URL> files = new ArrayList<URL>();
        long startTime = System.currentTimeMillis();
        File home = new File(System.getProperty("user.home"));
        File gf = new File(home, "glassfish/modules");
        Assert.assertTrue(gf.exists());
        long start = System.currentTimeMillis();
        for (File f : gf.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // I am parsing kernel, so don't add it to the cp
                if (name.endsWith("kernel.jar")) return false;
                
                return name.endsWith(".jar");
            }
        })) {
            files.add(f.toURI().toURL());
        }

        final URLClassLoader cl = new URLClassLoader(files.toArray(new URL[files.size()]), this.getClass().getClassLoader());

        ParsingContext.Builder builder = new ParsingContext.Builder();
        builder.logger().setLevel(Level.FINE);
        builder.locator(new ResourceLocator() {
            @Override
            public URL getResource(String name) {
                if (name.indexOf(".")==-1) return null; // intrinsic types.
                if (name.startsWith("java/")) return null; // no jdk class parsing.
                return cl.getResource(name);
            }

            @Override
            public InputStream openResourceStream(String name)
                throws IOException {
              return cl.getResourceAsStream(name);
            }
        });
        ParsingContext context = builder.build();
        Parser parser = new Parser(context);

        parser.parse(new File(gf, "kernel.jar"), new Runnable() {
            @Override
            public void run() {
                System.out.println("Finished parsing kernel.jar ");
            }
        });
        try {
            Exception[] faults = parser.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("Found " + files.size() + " files in " + (System.currentTimeMillis() - start));
        for (Type t : context.getTypes().getAllTypes()) {
            if (t instanceof AnnotationTypeImpl) {
                System.out.println("Found annotation : " + ((AnnotationTypeImpl) t).getName() + " in " + t.getDefiningURIs());
            }
        }
        System.out.println("parsed " + files.size() + " in " + (System.currentTimeMillis() - startTime) + " ms");

        AnnotationType at = context.getTypes().getBy(AnnotationType.class, "org.jvnet.hk2.annotations.Contract");
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

    public static void main(String[] args) {
        try {
            (new LocatorTest()).testLocator();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
