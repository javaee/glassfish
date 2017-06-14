/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.enterprise.util.net;

import com.sun.enterprise.util.CULoggerInfo;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class JarURIPattern {
    private static final Logger logger = CULoggerInfo.getLogger();

    /**
     * This method is used to extract URI of jar entries that match
     * a given pattern.
     * @param uri
     * @param pattern
     */
    public static List<String> getJarEntries(URI uri, Pattern pattern) {
        List<String> results = new ArrayList<String>();   

        File file = null;
        try {
            file = new File(uri);
        } catch(Exception ex) {
            // ignore
        }
        if (file == null || file.isDirectory()) {
            return results;
        } 

        String fileName = file.getName();

        // only look at jar file
        if (fileName.endsWith(".jar")) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(new File(uri));
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry)entries.nextElement();
                    String entryName = entry.getName();
                    if (pattern.matcher(entryName).matches()) {
                        results.add(entryName);
                    }
                }
            } catch(Exception ex) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, 
                            CULoggerInfo.getString(CULoggerInfo.exceptionJarOpen, fileName), 
                            ex);
                }
                throw new RuntimeException(ex);
            } finally {
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (Throwable t) {
                        // Ignore
                    }
                }
            }
        }

        return results;
    }
    
}
