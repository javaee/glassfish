/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.module.single;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.common_impl.ByteArrayInhabitantsDescriptor;
import com.sun.hk2.component.InhabitantsFile;

import java.net.*;
import java.util.*;
import java.util.jar.Manifest;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a ModuleDefinition backed up by a a single classloader
 *
 * @author Jerome Dochez
 */
public class ProxyModuleDefinition implements ModuleDefinition {

        private final ModuleMetadata metadata = new ModuleMetadata();
        private final Manifest manifest;

        public ProxyModuleDefinition(ClassLoader classLoader) throws IOException {
            this(classLoader, null, Collections.singleton("default"));
        }

        public ProxyModuleDefinition(ClassLoader classLoader, List<ManifestProxy.SeparatorMappings> mappings) throws IOException {
            this(classLoader, mappings, Collections.singleton("default"));
        }

        public ProxyModuleDefinition(ClassLoader classLoader, List<ManifestProxy.SeparatorMappings> mappings,
                                     Collection<String> habitatNames) throws IOException {
            manifest = new ManifestProxy(classLoader, mappings);
            for (String habitatName : habitatNames) {
                Enumeration<URL> inhabitants = classLoader.getResources(InhabitantsFile.PATH+'/'+habitatName);
                Set<File> processedFiles = new HashSet<File>();
                while (inhabitants.hasMoreElements()) {
                    URL url = inhabitants.nextElement();
                    if (url.getProtocol().equals("jar")) {
                        int index = url.getPath().indexOf("!");
                        if (index>0 && url.getPath().length()>5) {
                            String path = url.getPath().substring(5, url.getPath().indexOf("!"));
                            File f = new File(path);
                            if (f.exists()) {
                                f = f.getCanonicalFile();
                                if (processedFiles.contains(f)) {
                                    continue;
                                }
                                processedFiles.add(f);
                            }   
                        }
                    }
                    metadata.addHabitat(habitatName,
                        new ByteArrayInhabitantsDescriptor(url, readFully(url))
                    );
                }
            }
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlCL = (URLClassLoader) classLoader;
                for (URL url : urlCL.getURLs()) {
                    try {
                        uris.add(url.toURI());
                    } catch (URISyntaxException e) {
                        Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }

        private static byte[] readFully(URL url) throws IOException {
            DataInputStream dis=null;
            try {
                URLConnection con = url.openConnection();
                int len = con.getContentLength();
                InputStream in = con.getInputStream();
                dis = new DataInputStream(in);
                byte[] bytes = new byte[len];
                dis.readFully(bytes);
                return bytes;
            } catch (IOException e) {
                IOException x = new IOException("Failed to read " + url);
                x.initCause(e);
                throw x;
            } finally {
                if (dis!=null)
                    dis.close();
            }
        }

        public String getName() {
            return "Static Module";
        }

        public String[] getPublicInterfaces() {
            return new String[0];
        }

        public ModuleDependency[] getDependencies() {
            return EMPTY_MODULE_DEFINITIONS_ARRAY;
        }

        public URI[] getLocations() {
            return uris.toArray(new URI[uris.size()]);
        }

        public String getVersion() {
            return "1.0.0";
        }

        public String getImportPolicyClassName() {
            return null;
        }

        public String getLifecyclePolicyClassName() {
            return null;
        }

        public Manifest getManifest() {
            return manifest;
        }

        public ModuleMetadata getMetadata() {
            return metadata;
        }

        private static boolean ok(String s) {
            return s != null && s.length() > 0;
        }

        private static boolean ok(String[] ss) {
            return ss != null && ss.length > 0;
        }
        private static final ModuleDependency[] EMPTY_MODULE_DEFINITIONS_ARRAY = new ModuleDependency[0];
        private static final List<URI> uris = new ArrayList<URI>();

    static {
        // It is impossible to change java.class.path after the JVM starts --
        // so cache away a copy...

        String cp = System.getProperty("java.class.path");
        if(ok(cp)) {
            String[] paths = cp.split(System.getProperty("path.separator"));

            if(ok(paths)) {

                for(int i = 0; i < paths.length; i++) {
                    uris.add(new File(paths[i]).toURI());
                }
            }
        }
    }


    }
