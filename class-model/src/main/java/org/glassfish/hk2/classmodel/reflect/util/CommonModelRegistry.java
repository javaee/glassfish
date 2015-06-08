/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.classmodel.reflect.util;

import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.external.org.objectweb.asm.ClassReader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Mahesh Kannan
 *
 */
public class CommonModelRegistry
    implements ResourceLocator {

    BundleContext ctx;

    PackageAdmin pkgAdmin;

    private static CommonModelRegistry _instance = new CommonModelRegistry();

    private CommonModelRegistry(){}

    public static CommonModelRegistry getInstance() {
        return _instance;
    }

    /*package*/
    void initialize(BundleContext ctx, PackageAdmin pkgAdmin) {
        this.ctx = ctx;
        this.pkgAdmin = pkgAdmin;
    }

    public boolean canLoadResources() {
        return pkgAdmin != null;
    }

    public void loadModel(ParsingContext ctx, String className) {
        int index = className.lastIndexOf('.');
        String packageName = index > 0 ? className.substring(0, index) : "";
        ExportedPackage pkg = pkgAdmin.getExportedPackage(packageName);

        if (pkg != null) {
            Bundle srcBundle = pkg.getExportingBundle();
            String resourceName = className.replace('.', '/');
            if (! resourceName.endsWith(".class"))
                resourceName += ".class";
            URL url = srcBundle.getResource(resourceName);
            byte[] data = null;
            if (url != null) {
                try {
                    InputStream is = url.openStream();
                    data = new byte[is.available()];
                    for (int remaining = data.length; remaining > 0; ) {
                        int read = is.read(data, data.length - remaining, remaining);
                        if (read > 0)
                            remaining -= read;
                    }

                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    ClassReader cr = new ClassReader(bis);
                    cr.accept(ctx.getClassVisitor(url.toURI(), className), ClassReader.SKIP_DEBUG);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
//                System.out.println("** CommonModelRegistry::loadModel(" + resourceName + ") ==> ***NOT FOUND** "
//                        + "; pkg : " + pkg.toString() + "; bnd : " + srcBundle);
            }
        } else {
//            System.out.println("** CommonModelRegistry::loadModel NULL PACKAGE for: " + className);
        }
    }

    @Override
    public InputStream openResourceStream(String className) throws IOException {
        int index = className.lastIndexOf('/');
        String packageName = index > 0 ? className.substring(0, index) : "";
        ExportedPackage pkg = pkgAdmin.getExportedPackage(packageName.replace('/', '.'));

//        System.out.println("** CommonModelRegistry::openResourceStream called  for: " + className);

        InputStream inputStream = null;
        if (pkg != null) {
            Bundle srcBundle = pkg.getExportingBundle();
//            String resourceName = className.replace('.', '/');
//            if (! resourceName.endsWith(".class"))
//                resourceName += ".class";
            URL url = srcBundle.getResource(className);
            byte[] data = null;
            if (url != null) {
                try {
                    InputStream is = url.openStream();

                    data = new byte[is.available()];
                    for (int remaining = data.length; remaining > 0; ) {
                        int read = is.read(data, data.length - remaining, remaining);
                        if (read > 0)
                            remaining -= read;
                    }

                    inputStream = new ByteArrayInputStream(data);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

//                System.out.println("** CommonModelRegistry::loadModel("
//                        + className + ") ==> "
//                        + (data == null ? -1 : data.length));
            } else {
//                System.out.println("** CommonModelRegistry::loadModel("
//                        + className + ") ==> ***NOT FOUND** "
//                        + "; pkg : " + pkg.toString()
//                        + "; bnd : " + srcBundle
//                );
            }
        } else {
//            System.out.println("** CommonModelRegistry::loadModel NULL PACKAGE for: " + className);
        }

        return inputStream;
    }

    @Override
    public URL getResource(String className) {
        int index = className.lastIndexOf('/');
        String packageName = index > 0 ? className.substring(0, index) : "";
        ExportedPackage pkg = pkgAdmin.getExportedPackage(packageName.replace('/', '.'));

//        System.out.println("** CommonModelRegistry::getResource called  for: " + className);

        InputStream inputStream = null;
        if (pkg != null) {
            Bundle srcBundle = pkg.getExportingBundle();
//            String resourceName = className.replace('.', '/');
//            if (! resourceName.endsWith(".class"))
//                resourceName += ".class";
            return srcBundle.getResource(className);
        }

        return null;
    }
}
