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

package com.sun.logging;

import java.util.*;
import java.util.logging.*;

/**
 *
 * @author bnevins
 */
class GFLogger extends Logger {

    @Override
    public ResourceBundle getResourceBundle() {
        setResourceBundle();
        return resourceBundle;
    }

    @Override
    public String getResourceBundleName() {
        return gfResourceBundleName;
    }

    GFLogger(String name, Class theClass) {
        super(name, null);
        loggerName = name;
        clazz = theClass;
        setResourceBundle();
    }

    private ResourceBundle setResourceBundle() {
        final ClassLoader cl = clazz.getClassLoader();
        String packageName = clazz.getPackage().getName();
        ResourceBundle bundle = null;

        // calculate a name but make sure it corresponds to a real bundle before returning...

        // try the com.sun.logging location first
        String bundleName = getLoggerResourceBundleName();
        String mainNameToTry = bundleName;  // squirrel away a copy for below...
        bundle = verifyBundle(bundleName, cl);

        if(bundle != null)
            return bundle;


        // see if the package or any of the "super-packages", in order, have
        // LogStrings.properties

        for( ; packageName != null; packageName = getParentPackageName(packageName)) {
            bundleName = packageName + "." + LogDomains.RESOURCE_BUNDLE;

            bundle = verifyBundle(bundleName, cl);

            if(bundle != null)
                return bundle;
        }

        // look in OUR ClassLoader for the properties
        bundle = verifyBundle(mainNameToTry, LogDomains.class.getClassLoader());
        return bundle; // might be null...
    }

    private ResourceBundle verifyBundle(final String name, final ClassLoader cl) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle(name, Locale.getDefault(), cl);
            gfResourceBundleName = name;
            return rb;
        }
        catch(MissingResourceException e) {
            return null;
        }
    }

    private static String getParentPackageName(String s) {
        int index = s.lastIndexOf('.');

        if(index < 0)
            return null;

        return s.substring(0, index);
    }

    private String getLoggerResourceBundleName() {
        String result = loggerName + "." + LogDomains.RESOURCE_BUNDLE;
       // System.out.println("looking for bundle "+ result.replaceFirst(DOMAIN_ROOT, PACKAGE_ROOT));
        return result.replaceFirst(LogDomains.DOMAIN_ROOT, LogDomains.PACKAGE_ROOT);
    }

    private String          gfResourceBundleName;
    private String          loggerName;
    private Class           clazz;
    private ResourceBundle  resourceBundle;
}
