/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility methods
 *
 * @author tjquinn
 */
public class Util {

    private static Class thisClass = Util.class;
    private final static LocalStringManager localStrings = new LocalStringManagerImpl(thisClass);

    /**
     * Returns a File for the specified path if the file exists and is readable.
     * @param filePath
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static File verifyFilePath(final String filePath) throws FileNotFoundException, IOException {
        File f = new File(filePath);
        if ( ! f.exists()) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        if ( ! f.canRead()) {
            String msg = localStrings.getLocalString(thisClass,
                    "appclient.notReadable",
                    "{0} is not a readable file",
                    new Object[] {f.getAbsolutePath()});
            throw new IOException(msg);
        }
        return f;
    }

    /**
     * Returns a File object for the specified path if the file is otherwise
     * valid (exists and is readable) and is not a directory.
     * @param filePath
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    static File verifyNonDirectoryFilePath(final String filePath) throws FileNotFoundException, IOException {
        File f = verifyFilePath(filePath);
        if ( ! f.isFile()) {
            String msg = localStrings.getLocalString(thisClass,
                    "appclient.isDir",
                    "{0} is a directory; it must be a readable non-directory file",
                    new Object[] {f.getAbsolutePath()});
            throw new IOException(msg);
        }
        return f;
    }

    public static ArchiveFactory getArchiveFactory() {
        return ACCModulesManager.getComponent(ArchiveFactory.class);
    }

    public static ArchivistFactory getArchivistFactory() {
        return ACCModulesManager.getComponent(ArchivistFactory.class);
    }

    public static URI getURI(final String s) throws URISyntaxException {
        return getURI(new File(s));
    }

    public static URI getURI(final File f) throws URISyntaxException {
        URI uri = f.toURI();
        if (f.isFile() && (f.getName().endsWith(".jar") ||
                           f.getName().endsWith(".ear"))) {
            uri = new URI("jar:" + uri.getSchemeSpecificPart());
        }
        return uri;
    }
}
