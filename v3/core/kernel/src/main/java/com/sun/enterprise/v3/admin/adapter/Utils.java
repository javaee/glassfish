/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
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
        InputStream is = Utils.class.getClassLoader().getResourceAsStream(file);
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] bytes = new byte[1024];
            int read;
            StringBuffer sb = new StringBuffer();
            while ((read = bis.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, read, "UTF-8"));
            }
            return ( sb.toString());
        } finally {
            is.close();
        }
    }
}
