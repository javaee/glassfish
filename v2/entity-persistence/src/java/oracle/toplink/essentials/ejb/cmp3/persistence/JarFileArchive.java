/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
 * 
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

package oracle.toplink.essentials.ejb.cmp3.persistence;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This is an implementation of {@link Archive} when container returns a
 * file: url that refers to a jar file. e.g. file:/tmp/a_ear/lib/pu.jar
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JarFileArchive implements Archive {
    private JarFile jarFile;

    private URL rootURL;

    private Logger logger;

    public JarFileArchive(JarFile jarFile) throws MalformedURLException {
        this(jarFile, Logger.global);
    }

    public JarFileArchive(JarFile jarFile, Logger logger)
            throws MalformedURLException {
        logger.entering("JarFileArchive", "JarFileArchive", // NOI18N
                new Object[]{jarFile});
        this.logger = logger;
        this.jarFile = jarFile;
        rootURL = new File(jarFile.getName()).toURI().toURL();
        logger.logp(Level.FINER, "JarFileArchive", "JarFileArchive", // NOI18N
                "rootURL = {0}", rootURL); // NOI18N
    }

    public Iterator<String> getEntries() {
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        ArrayList<String> result = new ArrayList<String>();
        while (jarEntries.hasMoreElements()) {
            final JarEntry jarEntry = jarEntries.nextElement();
            if(!jarEntry.isDirectory()) { // exclude directory entries
                result.add(jarEntry.getName());
            }
        }
        return result.iterator();
    }

    public InputStream getEntry(String entryPath) throws IOException {
        InputStream is = null;
        final ZipEntry entry = jarFile.getEntry(entryPath);
        if (entry != null) {
            is = jarFile.getInputStream(entry);
        }
        return is;
    }

    public URL getEntryAsURL(String entryPath) throws IOException {
        return jarFile.getEntry(entryPath)!= null ?
                new URL("jar:"+rootURL+"!/"+entryPath) : null; // NOI18N
    }

    public URL getRootURL() {
        return rootURL;
    }
}

