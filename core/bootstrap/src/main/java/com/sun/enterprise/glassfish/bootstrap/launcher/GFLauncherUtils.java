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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.glassfish.bootstrap.launcher;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Package private static utility methods
 * @author bnevins
 */
class GFLauncherUtils {

    private GFLauncherUtils() {
    // all static methods
    }

    static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    static boolean safeExists(File f) {
        return f != null && f.exists();
    }

    static boolean safeIsDirectory(File f) {
        return f != null && f.isDirectory();
    }

    static File absolutize(File f) {
        if (f == null) {
            return null;
        }

        try {
            return f.getCanonicalFile();
        }
        catch (Exception e) {
            return f.getAbsoluteFile();
        }
    }

    static File getInstallDir() {
        String resourceName = GFLauncherUtils.class.getName().replace(".", "/") + ".class";
        URL resource = GFLauncherUtils.class.getClassLoader().getResource(resourceName);

        if (resource == null) {
            return null;
        }

        if (!resource.getProtocol().equals("jar")) {
            return null;
        }

        try {
            JarURLConnection c = (JarURLConnection) resource.openConnection();
            URL jarFile = c.getJarFileURL();
            File f = new File(jarFile.toURI());

            f = f.getParentFile();  // <install>/modules

            if (f == null) {
                return null;
            }

            f = f.getParentFile(); // <install>/

            return absolutize(f);
        }
        catch (Exception e) {
            return null;
        }
    }
}

