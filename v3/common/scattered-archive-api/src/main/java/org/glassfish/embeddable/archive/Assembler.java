/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

/**
 * @author bhavanishankar@java.net
 */

class Assembler {

    public URI assemble(ScatteredArchive archive) {
        try {
            return assemble(archive.name, archive.topDir, archive.classpaths,
                    archive.resourcespath, archive.metadatas, archive.type);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public URI assemble(ScatteredEnterpriseArchive archive) {
        try {
            return assemble(archive.name, null, archive.archives,
                    null, archive.metadatas, archive.type);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public URI assemble(String name, File topDir, List<File> classpaths, File resources,
                        Map<String, File> metadatas, String type) throws Exception {
        File archive = new File(System.getProperty("java.io.tmpdir"), name + "." + type);
//        archive.deleteOnExit();

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(archive));

        boolean isWar = "war".equals(type);
        boolean isEar = "ear".equals(type);
        String baseDir = isWar ? "WEB-INF/" : "";
        String classesDir = isWar ? "WEB-INF/classes/" : "";
        String libDir = isWar ? "WEB-INF/lib/" : "";

        transferDir(topDir, jos, classesDir);
        transferDir(resources, jos, baseDir);
        for (String key : metadatas.keySet()) {
            tranferFile(metadatas.get(key), jos, key, false);
        }
        for (File classpath : classpaths) {
            if (classpath.isDirectory()) {
                transferDir(classpath, jos, classesDir);
            } else {
                tranferFile(classpath, jos, libDir + classpath.getName(), !isWar && !isEar);
            }
        }
        jos.close();
        return archive.toURI();
    }

    void transferDir(File dir, JarOutputStream jos, String entryNamePrefix)
            throws Exception {
        transferDir(dir, dir, jos, entryNamePrefix);
    }

    void transferDir(File basedir, File dir, JarOutputStream jos, String entryNamePrefix)
            throws Exception {
        if (dir == null || jos == null) {
            return;
        }
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                String entryName = entryNamePrefix +
                        f.getPath().substring(basedir.getPath().length() + 1);
                tranferFile(f, jos, entryName, false);
            } else {
                transferDir(basedir, f, jos, entryNamePrefix);
            }
        }
    }

    void tranferFile(File file, JarOutputStream jos, String entryName, boolean explodeFile)
            throws Exception {
        if (explodeFile) {
            tranferEntries(file, jos);
        } else {
            transferFile(file, jos, entryName);
        }
    }

    void transferFile(File file, JarOutputStream jos, String entryName) throws Exception {
        if (file == null || jos == null || entryName == null) {
            return;
        }
        ZipEntry entry = new ZipEntry(entryName);
        try {
            jos.putNextEntry(entry);
        } catch (ZipException ex) {
            return;
        }
        FileInputStream fin = new FileInputStream(file);
        transferContents(fin, jos);
        jos.closeEntry();
    }

    void tranferEntries(File file, JarOutputStream jos) throws Exception {
        if (file == null || jos == null) {
            return;
        }
        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                InputStream in = jarFile.getInputStream(entry);
                try {
                    jos.putNextEntry(entry);
                } catch (ZipException ex) {
                    continue;
                }
                transferContents(in, jos);
                jos.closeEntry();
            }
        }
    }

    void transferContents(InputStream fin, JarOutputStream jos)
            throws Exception {
        if (fin == null || jos == null) {
            return;
        }
        int read = 0;
        byte[] buffer = new byte[8192];
        while ((read = fin.read(buffer, 0, buffer.length)) != -1) {
            jos.write(buffer, 0, read);
        }
        jos.flush();
    }

}
