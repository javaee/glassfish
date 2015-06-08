/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.hk2.classmodel.reflect.util.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * scans all glassfish jar, glassfish must be installed in ~/glassfish
 */
@Ignore
public class GFInstallationTest {

    @Test
    public void foo() throws IOException {
        List<File> files = new ArrayList<File>();
        long startTime = System.currentTimeMillis();
        File home = new File(System.getProperty("user.home"));
        File gf = new File(home,"glassfish/modules");
        Assert.assertTrue(gf.exists());
        long start = System.currentTimeMillis();
        for (File f : gf.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        })) {
            files.add(f);
        }

        ParsingContext.Builder builder = new ParsingContext.Builder();
        builder.archiveSelector(new ArchiveSelector() {

            @Override
            public boolean selects(ArchiveAdapter adapter) {
                Manifest manifest = null;
                try {
                    manifest = adapter.getManifest();
                } catch (IOException e) {
                    return true;
                }
                String bundleName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
                if (bundleName.contains("auto-depends")) {
                    return true;
                }
                // if it is not auto-depends, must import it...
                String imports = manifest.getMainAttributes().getValue("Import-Package");
                if (imports!=null && imports.indexOf("hk2")==-1) {
                    //System.out.println("Ignoring service-less " + adapter.getName());
                    return false;
                }
                return true;
            }
        });

        builder.logger().setLevel(Level.FINE);
        final Set<String> annotations = new HashSet<String>();
        annotations.add("org.jvnet.hk2.annotations.Service");
        builder.config(new ParsingConfig() {
            final Set<String> empty = Collections.emptySet();

            @Override
            public Set<String> getAnnotationsOfInterest() {
                return annotations;
            }

            @Override
            public Set<String> getTypesOfInterest() {
                return empty;
            }

            @Override
            public boolean modelUnAnnotatedMembers() {
                return true;
            }
        });
        ParsingContext context = builder.build();
        Parser parser = new Parser(context);

        
        for (final File f: files) {
            parser.parse(f, new Runnable() {
                @Override
                public void run() {
                    System.out.println("Finished parsing " + f.getName());
                }
            });
        }
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
        /*
        AnnotationTypeImpl service = TypesImpl.all.annotations.getElement("Lorg/jvnet/hk2/annotations/Service;");
        for (RefType type : service.allAnnotatedTypes()) {
            System.out.println("My services are " + type.getName);
        }
        */
        
    }

    public static void main(String[] args) {
        try {
            (new GFInstallationTest()).foo();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}
