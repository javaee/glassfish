/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

package embedded.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void zipInstanceDirectory() {
        zipInstanceDirectory("tmp");
    }
    public static void zipInstanceDirectory(String name) {
        try {
            ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( new File(name + "-instance-dir.zip") ) );
            File directory = new File(System.getProperty("com.sun.aas.instanceRoot"));
            zip( directory, directory.getParentFile(), zos );
            zos.close();
        } catch (IOException e) {
            System.out.println("ERROR preserving the directory:");
            e.printStackTrace();
        }
    }
   
    private static final void zip(File directory, File base, ZipOutputStream zos) throws IOException {
        File[] files = directory.listFiles();
        byte[] buffer = new byte[8192];
        int read = 0;
        for (int i = 0, n = files.length; i < n; i++) {
            if (files[i].getCanonicalPath().indexOf("osgi") != -1 ||
                    files[i].getCanonicalPath().indexOf("ejbtimer") != -1 ) {
                continue;
            } else {
                ZipEntry entry = new ZipEntry(files[i].getPath().substring(
                      base.getPath().length() + 1));
                zos.putNextEntry(entry);
                if (files[i].isDirectory()) {
                    zip(files[i], base, zos);
                } else {
                    FileInputStream in = new FileInputStream(files[i]);
                    while (-1 != (read = in.read(buffer))) {
                        zos.write(buffer, 0, read);
                    }
                    in.close();
                }
            }
        }
    }
}
