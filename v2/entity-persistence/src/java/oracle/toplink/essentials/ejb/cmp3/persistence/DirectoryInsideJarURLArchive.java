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

import java.net.URL;
import java.net.JarURLConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is an implementation of {@link Archive} which is used when container
 * returns a jar: URL. e.g. jar:file:/tmp/a_ear/b.war!/WEB-INF/classes/
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DirectoryInsideJarURLArchive  implements Archive {
    /*
     * Implementation Note: This class does not have any dependency on TopLink
     * or GlassFish implementation classes. Please retain this searation.
     */

    private JarFile jarFile;

    // URL representation of this archive
    private URL rootURL;

    // distance from top of the JarFile to the entry
    private String relativeRootPath;

    private List<String> entries = new ArrayList<String>();

    private Logger logger;

    public DirectoryInsideJarURLArchive(URL url) throws IOException {
        this(url, Logger.global);
    }

    public DirectoryInsideJarURLArchive(URL url, Logger logger)
            throws IOException {
        logger.entering("DirectoryInsideJarURLArchive", "DirectoryInsideJarURLArchive",  // NOI18N
                new Object[]{url});
        this.logger = logger;
        assert(url.getProtocol().equals("jar")); // NOI18N
        rootURL = url;
        JarURLConnection conn =
                JarURLConnection.class.cast(url.openConnection());
        jarFile = conn.getJarFile();
        logger.logp(Level.FINER, "DirectoryInsideJarURLArchive",
                "DirectoryInsideJarURLArchive", "jarFile = {0}", jarFile);
        relativeRootPath = conn.getEntryName();
        init();
    }

    private void init() {
        for(Enumeration<JarEntry> jarEntries = jarFile.entries();
            jarEntries.hasMoreElements();) {
            JarEntry jarEntry = jarEntries.nextElement();
            if(jarEntry.isDirectory()) {
                continue;
            }
            String jarEntryName = jarEntry.getName();
            if (relativeRootPath==null) {
                entries.add(jarEntryName);
            } else if (jarEntryName.startsWith(relativeRootPath)) {
                entries.add(jarEntryName.substring(relativeRootPath.length()));
            }
        }
    }

    public Iterator<String> getEntries() {
        return entries.iterator();
    }

    public InputStream getEntry(String entryPath) throws IOException {
        InputStream is = entries.contains(entryPath) ?
            jarFile.getInputStream(jarFile.getEntry(relativeRootPath + entryPath)) : null;
        return is;
    }

    public URL getEntryAsURL(String entryPath) throws IOException {
        URL result = entries.contains(entryPath) ?
            new URL("jar:"+rootURL+"!/"+ relativeRootPath + entryPath) : null; // NOI18N
        return result;
    }

    public URL getRootURL() {
        return rootURL;
    }
}
