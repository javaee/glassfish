/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.adapter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Package-private class to provide utilities.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352; (km@dev.java.net)
 * @since GlassFish V3
 */
final class Utils {

    /** Reads the given file in this package and returns it as a String.
     *  If there is any problem in reading an IOException is thrown.
     * @param name representing just the complete name of file to be read, e.g. foo.html
     * @return String
     * @throws IOException
     */
    static String packageResource2String(String name) throws IOException {
        String file = Utils.class.getPackage().getName().replace('.', '/') + "/" + name;
        InputStream is=null;
        try {
            is = new BufferedInputStream(Utils.class.getClassLoader().getResourceAsStream(file));
            byte[] bytes = new byte[1024];
            int read;
            StringBuilder sb = new StringBuilder();
            while ((read = is.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, read, "UTF-8"));
            }
            return ( sb.toString());
        } finally {
            if (is!=null) {
                try {
                    is.close();
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }
    }
}
