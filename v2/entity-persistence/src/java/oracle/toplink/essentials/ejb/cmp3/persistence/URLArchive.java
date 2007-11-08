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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

/**
 * This is an implementation of {@link Archive} when container returns a url
 * that is not one of the familiar URL types like file or jar URLs. So, we can
 * not recurssively walk thru' it's hierarchy. As a result {@link #getEntries()}
 * returns an empty collection.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class URLArchive implements Archive {
    /*
     * Implementation Note: This class does not have any dependency on TopLink
     * or GlassFish implementation classes. Please retain this searation.
     */

    /**
     * The URL representation of this archive.
     */
    private URL url;

    public URLArchive(URL url) {
        this.url = url;
    }

    public Iterator<String> getEntries() {
        return Collections.EMPTY_LIST.iterator();
    }

    public InputStream getEntry(String entryPath) throws IOException {
        URL subEntry = new URL(url, entryPath);
        InputStream is = null;
        try {
            is = subEntry.openStream();
        } catch (IOException ioe) {
            // we return null when entry does not exist
        }
        return is;
    }

    public URL getEntryAsURL(String entryPath) throws IOException {
        URL subEntry = new URL(url, entryPath);
        try {
            InputStream is = subEntry.openStream();
            is.close();
        } catch (IOException ioe) {
            return null; // return null when entry does not exist
        }
        return subEntry;
    }

    public URL getRootURL() {
        return url;
    }
}
