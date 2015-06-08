/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.module.common_impl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipException;

import com.sun.enterprise.module.InhabitantsDescriptor;
import com.sun.enterprise.module.ModuleMetadata;

/**
 * Abstraction of {@link JarFile} so that we can handle
 * both a jar file and a directory image transparently.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Jar {
    protected Jar() {}

    /**
     * See {@link JarFile#getManifest()} for the contract.
     */
    public abstract Manifest getManifest() throws IOException;

    /**
     * Loads all <tt>META-INF/habitats</tt> entries and store them to the list.
     */
    public abstract void loadMetadata(ModuleMetadata result);

    /**
     * Gets the base name of the jar.
     *
     * <p>
     * For example, "bar" for "bar.jar".
     */
    public abstract String getBaseName();

    public static Jar create(File file) throws IOException {
        if(file.isDirectory())
            return new Directory(file);
        else
            return new Archive(file);
    }

    private static final class Directory extends Jar {
        private final File dir;

        public Directory(File dir) {
            this.dir = dir;
        }

        public Manifest getManifest() throws IOException {
            File mf = new File(dir,JarFile.MANIFEST_NAME);
            if(mf.exists()) {
                FileInputStream in = new FileInputStream(mf);
                try {
                    return new Manifest(in);
                } finally {
                    in.close();
                }
            } else {
                return null;
            }
        }

        private File[] fixNull(File[] f) {
            if(f==null) return new File[0];
            else        return f;
        }

        public void loadMetadata(ModuleMetadata result) {
        }

        public String getBaseName() {
            return dir.getName();
        }

        private byte[] readFully(File f) throws IOException {
            byte[] buf = new byte[(int)f.length()];
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            try {
                in.readFully(buf);
                return buf;
            } finally {
                in.close();
            }
        }
    }

    private static final class Archive extends Jar {
        private final JarFile jar;
        private final File file;

        public Archive(File jar) throws IOException {
            try {
                this.jar = new JarFile(jar);
                this.file = jar;
            } catch (ZipException e) {
                // ZipException doesn't include this crucial information, so rewrap
                IOException x = new IOException("Failed to open " + jar);
                x.initCause(e);
                throw x;
            }
        }

        public Manifest getManifest() throws IOException {
            return jar.getManifest();
        }

        public void loadMetadata(ModuleMetadata result) {
        }

        public String getBaseName() {
            String name = file.getName();
            int idx = name.lastIndexOf('.');
            if(idx>=0)
                name = name.substring(0,idx);
            return name;
        }
    }
}
