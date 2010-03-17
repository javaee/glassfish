/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License. You can obtain
 *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *  Sun designates this particular file as subject to the "Classpath" exception
 *  as provided by Sun in the GPL Version 2 section of the License file that
 *  accompanied this code.  If applicable, add the following below the License
 *  Header, with the fields enclosed by brackets [] replaced by your own
 *  identifying information: "Portions Copyrighted [year]
 *  [name of copyright owner]"
 *
 *  Contributor(s):
 *
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package org.glassfish.hk2.classmodel.reflect.util;

import org.glassfish.hk2.classmodel.reflect.ArchiveAdapter;

import java.io.*;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Directory base archive abstraction
 */
public class DirectoryArchive implements ArchiveAdapter {
    public final File directory;
    public final List<Entry> entries = new ArrayList<Entry>();

    public DirectoryArchive(File directory) {
        this.directory = directory;
        parse(directory);
    }

    @Override
    public String getName() {
        return directory.getName();
    }

    @Override
    public InputStream getInputStream(String entry) throws IOException {
        File source = new File(directory, unmangle(entry));
        if (source.exists()) {
           return Channels.newInputStream((new FileInputStream(source)).getChannel());   
        }
        throw new IOException("Cannot find entry getName " + entry);
    }

    @Override
    public Manifest getManifest() throws IOException {
        File manifest = new File(directory, JarFile.MANIFEST_NAME);
        if (manifest.exists()) {
            return new Manifest(new BufferedInputStream(new FileInputStream(manifest)));  
        }
        return null;
    }

    @Override
    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

    private void parse(File dir) {
        for (File f : dir.listFiles()) {
            entries.add(new Entry(mangle(f), f.length(), f.isDirectory()));
            if (f.isDirectory()) {
                parse(f);
            }
        }
    }

    private String mangle(File f) {
        String relativePath = f.getAbsolutePath().substring(directory.getAbsolutePath().length()+1);
        return relativePath.replace(File.separatorChar, '/');
    }

    private String unmangle(String name) {
        return name.replace('/', File.separatorChar);
    }
}
