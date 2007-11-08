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
package com.sun.enterprise.admin.util;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Hashtable;
import java.text.MessageFormat;


/**
 * Implementation of a SOM local string manager as SOM-specific wrapper around LocalStringsManager;
 * Provides access to i18n messages for classes that need them.
 * This particular implementation presents the following resources organization:
 *<ul> 
 * <li>1. Resource files share locations with sources (same directories);</li>
 * <li>2. Base directory is  "com.sun.enterprise.admin";</li>
 * <li>3. Search ONLY in first resource bundle, which found on the way up to basePackage( No additional hiearchical search of value if it is not found in that file)</li>
 *</ul> 
 */

public class SOMLocalStringsManager extends LocalStringsManager
{
    private static final String DEFAULT_PROPERTY_FILE_NAME    = "bundle";
    private static final String DEFAULT_PACKAGE_NAME          = "com.sun.enterprise.admin";
    //private static final String NO_DEFAULT = "No local strings defined";

    /** cache for all the local string managers (per pkg) */
    private static Hashtable managers = new Hashtable();

    public SOMLocalStringsManager(String packageName)
    {
        super(packageName, DEFAULT_PROPERTY_FILE_NAME);
        setFixedResourceBundle(packageName);
    }

   /**
     * Returns a local string manager for the given package name.
     *
     * @param    packageName    name of the package of the src
     *
     * @return   a local string manager for the given package name
     */
    public synchronized static SOMLocalStringsManager getManager(String packageName) {

        SOMLocalStringsManager mgr = (SOMLocalStringsManager) managers.get(packageName);

        if (mgr == null) {
            mgr = new SOMLocalStringsManager(packageName);
            try {
                managers.put(packageName, mgr);
            } catch (Exception e) {
                // do nothing
            }
        }
        return mgr;
    }

    /**
     *
     * Returns a local string manager for the given package name.
     *
     * @param    callerClass    the object making the call
     *
     * @return   a local string manager for the given package name
     */
    public synchronized static SOMLocalStringsManager getManager(Class callerClass) {
    
        try {
            int dotIdx = callerClass.getName().lastIndexOf(".");
            if (dotIdx != -1) {
                String pkgName = callerClass.getName().substring(0, dotIdx);
                return getManager(pkgName);
            } else {
                // class does not belong to any pkg
                String pkgName = callerClass.getName();
                return getManager(pkgName);
            }
        } catch (Exception e) {
            // Return default manager, and not null, so as to avoid 
            // null pointer exception in the calling method.
            return getManager(DEFAULT_PACKAGE_NAME);
        }
    }
    
    /**
     * Get a localized string.
     * @param key The name of the resource to fetch
     * @return The localized string
     */
    public String getString( String key)
    {
        return  super.getString(key, key); // key itself will act as default value, 
                                           //instead of some string like "No local strings found"
    }

    /**
     * Get a localized string.
     * @param key The name of the resource to fetch
     * @param args The default return value if not found
     * @return The localized string
     */
    public String getString( String key, String defaultValue)
    {
        return  super.getString(key, defaultValue);
    }

    /**
     * Get a local string and format the arguments accordingly.
     * @param key The key to the local format string
     * @param arguments The set of arguments to provide to the formatter
     * @return A formatted localized string
     */
    public String getString( String key, Object[] args)
    {
        return  super.getString(key, key, args);
    }

    /**
     * Get a local string and format the arguments accordingly.
     * @param key The key to the local format string
     * @param defaultFormat The default format if not found in the resources
     * @param arguments The set of arguments to provide to the formatter
     * @return A formatted localized string
     */
    public String getString( String key, String defaultFormat, Object[] args)
    {
        return  super.getString(key, defaultFormat, args);
    }

    /**
     * Convenience method - getString() overriding for fixed number of formatting arguments.
     * @param key The key to the local format string
     * @param defaultFormat The default format if not found in the resources
     * @param arg1 The first argument to provide to the formatter
     * @return A formatted localized string
     */
    public String getString( String key, String defaultFormat, Object arg1)
    {
        return  getString(key, defaultFormat, (new Object[]{arg1}));
    }

    /**
     * Convenience method - getString() overriding for fixed number of formatting arguments.
     * @param key The key to the local format string
     * @param defaultFormat The default format if not found in the resources
     * @param arg1 The first argument to provide to the formatter
     * @param arg1 The second argument to provide to the formatter
     * @return A formatted localized string
     */
    public String getString( String key, String defaultFormat, Object arg1, Object arg2)
    {
        return  getString(key, defaultFormat, (new Object[]{arg1, arg2}));
    }

    /**
     * Convenience method - getString() overriding for fixed number of formatting arguments.
     * @param key The key to the local format string
     * @param defaultFormat The default format if not found in the resources
     * @param arg1 The first argument to provide to the formatter
     * @param arg2 The second argument to provide to the formatter
     * @param arg3 The third argument to provide to the formatter
     * @return A formatted localized string
     */
    public String getString( String key, String defaultFormat, Object arg1, Object arg2, Object arg3)
    {
        return  getString(key, defaultFormat, (new Object[]{arg1, arg2, arg3}));
    }
}