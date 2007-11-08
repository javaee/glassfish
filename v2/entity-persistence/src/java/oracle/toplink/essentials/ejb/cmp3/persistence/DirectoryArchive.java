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

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This is an implementation of {@link Archive} when container returns a file:
 * url that refers to a directory that contains an exploded jar file.
 * e.g. file:/tmp/a_ear/ejb_jar
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DirectoryArchive implements Archive {
    /*
     * Implementation Note: This class does not have any dependency on TopLink
     * or GlassFish implementation classes. Please retain this searation.
     */

    /**
     * The directory this archive represents.
     */
    private File directory;

    /**
     * The URL representation of this archive.
     */
    private URL rootURL;

    /**
     * The file entries that this archive contains.
     */
    private List<String> entries = new ArrayList<String>();

    private Logger logger;

    public DirectoryArchive(File directory) throws MalformedURLException {
        this(directory, Logger.global);
    }

    public DirectoryArchive(File directory, Logger logger)
            throws MalformedURLException {
        logger.entering("DirectoryArchive", "DirectoryArchive",
                        new Object[]{directory});
        this.logger = logger;
        if (!directory.isDirectory()) {
            // should never reach here, hence the msg is not internationalized.
            throw new IllegalArgumentException(directory +
                    " is not a directory." + // NOI18N
                    "If it is a jar file, then use JarFileArchive."); // NOI18N
        }
        this.directory = directory;
        rootURL = directory.toURI().toURL();
        logger.logp(Level.FINER, "DirectoryArchive", "DirectoryArchive",
                "rootURL = {0}", rootURL);
        init(this.directory, this.directory); // initialize entries
    }

    private void init(File top, File directory) {
        File[] dirFiles = directory.listFiles();
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                continue; // exclude dir entries
            }

            // add only the relative path from the top.
            // note: we use unix style path
            String entryName = file.getPath().replace(File.separator, "/") // NOI18N
                    .substring(top.getPath().length() + 1);
            entries.add(entryName);
        }
        File[] subDirs = directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File subDir : subDirs) {
            init(top, subDir); // recursion
        }
    }

    public Iterator<String> getEntries() {
        return entries.iterator();
    }

    public InputStream getEntry(String entryPath) throws IOException {
        File f = getFile(entryPath);
        InputStream is = f.exists() ? new FileInputStream(f) : null;
        return is;
    }

    public URL getEntryAsURL(String entryPath) throws IOException {
        File f = getFile(entryPath);
        URL url = f.exists() ? f.toURI().toURL() : null;
        return url;
    }

    public URL getRootURL() {
        return rootURL;
    }

    private File getFile(String entryPath) {
        File f = new File(directory, entryPath);
        return f;
    }

}

