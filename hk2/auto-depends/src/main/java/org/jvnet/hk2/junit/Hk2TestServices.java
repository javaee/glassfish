/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package org.jvnet.hk2.junit;

import com.sun.hk2.component.*;
import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Services available to junit tests running with the {@link Hk2Runner} runner.
 *
 * @author Jerome Dochez
 */
public class Hk2TestServices {

    private Habitat habitat;
    
    public Hk2TestServices() {
        System.out.println("Singleton created");
        String classPath = System.getProperty("surefire.test.class.path");
        if (classPath==null) {
            classPath = System.getProperty("java.class.path");
        }
        System.out.println("classpath is " + classPath);
        ParsingContext.Builder builder = new ParsingContext.Builder();
        final Set<String> annotations = new HashSet<String>();
        annotations.add("org.jvnet.hk2.annotations.Contract");
        annotations.add("org.jvnet.hk2.annotations.Service");

        builder.config(new ParsingConfig() {
            final Set<String> empty = Collections.emptySet();

            public Set<String> getInjectionTargetAnnotations() {
                return empty;
            }

            public Set<String> getInjectionTargetInterfaces() {
                return annotations;
            }

            public Set<String> getInjectionPointsAnnotations() {
                return empty;
            }
        });

        ParsingContext context = null;
        try {
            context = builder.build();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Parser parser = new Parser(context);

        final ClassLoader cLoader = this.getClass().getClassLoader();

        final Holder<ClassLoader> holder = new Holder<ClassLoader>() {
            public ClassLoader get() {
                return cLoader;
            }
        };
        
        habitat = new Habitat();

        List<InhabitantsScanner> metaInfScanners = new ArrayList<InhabitantsScanner>();

        final StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while(st.hasMoreElements()) {
            final String fileName = st.nextToken();
            File f = new File(fileName);
            if (f.exists()) {
                try {
                    System.out.println("Beginning parsing " + fileName);
                    if (f.isFile()) {
                        JarFile jarFile = new JarFile(f);
                        // TODO : add support for other habitat than default.
                        JarEntry entry = jarFile.getJarEntry(InhabitantsFile.PATH+"/default");
                        if (entry!=null) {
                            byte[] buf = new byte[(int) entry.getSize()];
                            DataInputStream in = new DataInputStream(jarFile.getInputStream(entry));
                            try {
                                in.readFully(buf);
                            } finally {
                                in.close();
                            }
                            System.out.println("Using meta-inf file for " + f.getPath());
                            metaInfScanners.add(new InhabitantsScanner(new ByteArrayInputStream(buf),
                                "jar:"+f.toURL()+"!/"+entry.getName()));
                        } else {
                            // it's a file but no inhabitant file...
                            parse(parser, f);
                        }
                    } else {
                        // directory, for now, always parse.
                        File inhabitantFile = new File(f, InhabitantsFile.PATH+File.separator+"default");
                        if (inhabitantFile.exists()) {
                            System.out.println("Using meta-inf file for " + f.getPath());
                            metaInfScanners.add(new InhabitantsScanner(new BufferedInputStream(
                                    new FileInputStream(inhabitantFile)),
                                    inhabitantFile.getPath()));
                        } else {
                            parse(parser, f);
                        }
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            parser.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Starting to introspect");
        final InhabitantsParser ip = new InhabitantsParser(habitat);
        IntrospectionScanner is = new IntrospectionScanner(context);
        try {
            ip.parse(is, holder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("finished introspecting");

        System.out.println("Starting to introspect");
        for (InhabitantsScanner scanner : metaInfScanners) {
            try {
                ip.parse(scanner, holder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("finished introspecting");

        Iterator<String> contracts = habitat.getAllContracts();
        while (contracts.hasNext()) {
            String contract = contracts.next();
            System.out.println("Found contract : " + contract);
            for (Inhabitant t : habitat.getInhabitantsByContract(contract)) {
                System.out.println(" --> " + t.typeName());
            }
        }
    }


    private void parse(Parser parser, final File f) throws IOException {
        Manifest manifest=null;
        if (f.isDirectory()) {
            File manifestFile = new File(f, JarFile.MANIFEST_NAME);
            if (manifestFile.exists()) {
                InputStream is = new BufferedInputStream(new FileInputStream(manifestFile));
                try {
                    manifest = new Manifest(is);
                } finally {
                    is.close();
                }
            }
        } else {
            JarFile jar = new JarFile(f);
            manifest = jar.getManifest();
        }
        if (manifest!=null) {
            String imports = manifest.getMainAttributes().getValue("Import-Package");
            if (imports==null || imports.indexOf("hk2")==-1) {
                System.out.println("Ignoring service-less " + f.getName());
                return;
            }
        }
        parser.parse(f, new Runnable() {
            public void run() {
                System.out.println("Finished introspecting " + f.getName());
            }
        });

    }
    public Habitat getHabitat() {
        return habitat;
    }
}
