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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * This is an implementation of {@link Archive} which is used when container
 * returns some form of URL from which an InputStream in jar format can be
 * obtained. e.g. jar:file:/tmp/a_ear/b.war!/WEB-INF/lib/pu.jar
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JarInputStreamURLArchive implements Archive {
    private URL url;

    private List<String> entries = new ArrayList<String>();

    private Logger logger;

    public JarInputStreamURLArchive(URL url) throws IOException {
        this(url, Logger.global);
    }

    public JarInputStreamURLArchive(URL url, Logger logger) throws IOException {
        logger.entering("JarInputStreamURLArchive", "JarInputStreamURLArchive", // NOI18N
                new Object[]{url});
        this.logger = logger;
        this.url = url;
        init();
    }

    private void init() throws IOException {
        JarInputStream jis = new JarInputStream(
                new BufferedInputStream(url.openStream()));
        try {
            do {
                ZipEntry ze = jis.getNextEntry();
                if (ze == null) {
                    break;
                }
                if (!ze.isDirectory()) {
                    entries.add(ze.getName());
                }
            } while (true);
        } finally {
            jis.close();
        }
    }

    public Iterator<String> getEntries() {
        return entries.iterator();
    }

    public InputStream getEntry(String entryPath) throws IOException {
        if (!entries.contains(entryPath)) {
            return null;
        }
        JarInputStream jis = new JarInputStream(
                new BufferedInputStream(url.openStream()));
        do {
            ZipEntry ze = jis.getNextEntry();
            if (ze == null) {
                break;
            }
            if (ze.getName().equals(entryPath)) {
                return jis;
            }
        } while (true);

        // don't close the stream, as the caller has to read from it.

        assert(false); // should not reach here
        return null;
    }

    public URL getEntryAsURL(String entryPath) throws IOException {
        URL result = entries.contains(entryPath) ?
            result = new URL("jar:"+url+"!/"+entryPath) : null; // NOI18N
        return result;
    }

    public URL getRootURL() {
        return url;
    }
}
