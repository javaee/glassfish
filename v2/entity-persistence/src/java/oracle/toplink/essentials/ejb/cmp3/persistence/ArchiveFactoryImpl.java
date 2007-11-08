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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class is written to deal with various URLs that can be returned by
 * {@link javax.persistence.spi.PersistenceUnitInfo#getPersistenceUnitRootUrl()}
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ArchiveFactoryImpl {
    /*
     * Implementation Note: This class does not have any dependency on TopLink
     * or GlassFish implementation classes. Please retain this searation.
     */

    private Logger logger;

    public ArchiveFactoryImpl() {
        this(Logger.global);
    }

    public ArchiveFactoryImpl(Logger logger) {
        this.logger = logger;
    }

    public Archive createArchive(URL url) throws URISyntaxException, IOException {
        logger.entering("ArchiveFactoryImpl", "createArchive", new Object[]{url});
        Archive result;
        String protocol = url.getProtocol();
        logger.logp(Level.FINER, "ArchiveFactoryImpl", "createArchive", "protocol = {0}", protocol);
        
        if ("file".equals(protocol)) {
            URI uri = null;
            try {
                // Attempt to use url.toURI since it will deal with all urls 
                // without special characters and URISyntaxException allows us 
                // to catch issues with special characters. This will handle 
                // URLs that already have special characters replaced such as 
                // URLS derived from searches for persistence.xml on the Java 
                // System class loader
                uri = url.toURI();
            } catch (URISyntaxException exception) {
                // Use multi-argument constructor for URI since single-argument 
                // constructor and URL.toURI() do not deal with special 
                // characters in path
                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), null);
            }
            
            File f = new File(uri);
        
            if (f.isDirectory()) {
                // e.g. file:/tmp/a_ear/ejb_jar
                result = new DirectoryArchive(f);
            } else {
                // e.g. file:/tmp/a_ear/lib/pu.jar
                // It's not a directory. Then it must be a jar file.
                result = new JarFileArchive(new JarFile(f));
            }
        } else if ("jar".equals(protocol)) { // NOI18N
            JarURLConnection conn = JarURLConnection.class.cast(url.openConnection());
            JarEntry je = conn.getJarEntry();
            if (je == null) {
                // e.g. jar:file:/tmp/a_ear/lib/pu.jar!/
                // No entryName specified, hence URL points to a JAR file and
                // not to any entry inside it. Ideally this should have been
                // file:/tmp/a_ear/lib/pu.jar,
                // but containers (e.g.) WebLogic return this kind of URL,
                // so we better handle this in our code to imrove pluggability.
                // Read the entire jar file.
                result = new JarFileArchive(conn.getJarFile());
            } else if (je.isDirectory()) {
                // e.g. jar:file:/tmp/a_ear/b.war!/WEB-INF/classes/
                // entryName [je.getName()] is a directory
                result = new DirectoryInsideJarURLArchive(url);
            } else {
                // some URL (e.g.) jar:file:/tmp/a_ear/b.war!/WEB-INF/lib/pu.jar
                // entryName [je.getName()] is a file, so treat this URL as a
                // URL from which  a JAR format InputStream can be obtained.
                result = new JarInputStreamURLArchive(url);
            }
        } else if (isJarInputStream(url)){
            result = new JarInputStreamURLArchive(url);
        } else {
            result = new URLArchive(url);
        }
        logger.exiting("ArchiveFactoryImpl", "createArchive", result);
        return result;
    }

    /**
     * This method is called for a URL which has neither jar nor file protocol.
     * This attempts to find out if we can treat it as a URL from which a JAR
     * format InputStream can be obtained.
     * @param url
     */
    private boolean isJarInputStream(URL url) throws IOException {
        InputStream in = null;
        try {
        	in = url.openStream();
            if (in == null) { // for directories, we may get InputStream as null
            	return false;
            }
            JarInputStream jis = new JarInputStream(in);
            jis.close();
            return true; // we are successful in creating a Jar format IS
        } catch (IOException ioe) {
            if (in != null) {
            	in.close();
            }
            return false;
        }
    }
}
