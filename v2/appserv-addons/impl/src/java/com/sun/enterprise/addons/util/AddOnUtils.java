/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.addons.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

/**
 * @since 9.1
 * @author sreenivas munnangi, msreddy@dev.java.net
 */
public class AddOnUtils {

    public static JarFile getAddonJarFile(
        String addonName, File jarDir, FilenameFilter filter) 
        throws Exception {

        if ((jarDir == null) || (addonName == null) || (filter == null)) {
            throw new Exception("invalid input to getAddonJarFile");
        }

        File[] jars = jarDir.listFiles(filter);
        String str;
        for (File jar:jars) {
            str = jar.getName().split(ConfigurableJarFileFilter.regex)[0];
            if (str.equals(addonName)) {
                return new JarFile(jar);
            }
        }
        return null;
    }

    public static String [] getDependencies(JarFile jar) 
        throws Exception {

        String [] strArr = null;

        if (jar == null) {
            throw new Exception("invalid input to getDependencies");
        }

        Manifest mf = jar.getManifest();
        if (mf == null) return strArr;

        Attributes attrs = mf.getMainAttributes();
        if (attrs == null) return strArr;

        Set<Map.Entry<Object, Object>> mfattrs = attrs.entrySet();
        String key, val;
        for (Map.Entry<Object,Object> entry : mfattrs) {
            key = ((Attributes.Name) entry.getKey()).toString();
            val = (String) entry.getValue();
            if (! key.equals("Add-On-Dependencies")) continue;
            if (val.contains(",")) {
                strArr = val.split(",");
            } else {
                strArr = new String[1];
                strArr[0] = val;
            }
        }
        return strArr;
    }
}
