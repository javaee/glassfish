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
import java.text.MessageFormat;


/**
 * Implementation of a Admin local string manager as Admin-specific wrapper around LocalStringsManager;
 * Provides access to i18n messages for classes that need them.
 * This particular implementation presents the following resources organization:
 *<ul> 
 * <li>1. Resource files share locations with sources (same directories);</li>
 * <li>2. Base directory is  "com.sun.enterprise.admin";</li>
 * <li>3. Search ONLY in first resource bundle, which found on the way up to basePackage( No additional hiearchical search of value if it is not found in that file)</li>
 *</ul> 
 */

public class AdminLocalStringsManager extends LocalStringsManager
{
    static final String ADMIN_BASE_PACKAGE            = "com.sun.enterprise.admin";
    static final String DEFAULT_PROPERTY_FILE_NAME    = "LocalStrings";

    /** <b>Messages</b> Property File Type (file name prefix) */
    public static final String MESSAGES          = "Messages"; 
    /** <b>Decriptions</b> Property File Type (file name prefix) */
    public static final String DESCRIPTIONS      = "MBeansInfo";

    /**
     * Create a string manager that looks for default type properties(MESSAGES) in
     * the package of the callerObject or parent Admin packages.
     * @param callerObject the caller object (defines starting package for searching)  
     */
    public AdminLocalStringsManager(Object callerObject)
    {
        this(callerObject, DEFAULT_PROPERTY_FILE_NAME);
    }

    /**
     * Create a string manager that looks for -propertyFileName-.properties in
     * the package of the callerObject or parent Admin packages;
     * <br>It's supposed that propertyFileName should be one of well-known names, 
     * defined by constants: MESSAGES/DESCRIPTIONS/NOTIFICATIONS etc.
     * @param callerObject The caller object (defines starting package for searching).
     * @param propertyFileName property file name prefix (e.g. "myStrings");
     */
    public AdminLocalStringsManager(Object callerObject, String propertyFileName)
    {
        super(ADMIN_BASE_PACKAGE, propertyFileName);
        String callerClassPath;
        if(callerObject==null)
            callerClassPath = ADMIN_BASE_PACKAGE; //reffer to base
        else
        {
            callerClassPath = callerObject.getClass().getName();
            callerClassPath = callerClassPath.substring(0, callerClassPath.lastIndexOf('.'));
        }
        setFixedResourceBundle(callerClassPath);
    }

    /**
     * Convenient Factory class for obtaining a Admin string manager for message type strings.
     * @param callerObject The caller object (defines starting package for searching).
     */
    public static AdminLocalStringsManager createMessagesManager(Object callerObject)
    {
       return new AdminLocalStringsManager(callerObject, MESSAGES);
    }
    /**
     * Convenient Factory class for obtaining a Admin string manager for description type strings.
     * @param callerObject The caller object (defines starting package for searching).
     */
    public static AdminLocalStringsManager createDescriptionsManager(Object callerObject)
    {
       return new AdminLocalStringsManager(callerObject, DESCRIPTIONS);
    }
    
    
    /**
     * Get a localized string.
     * @param key The name of the resource to fetch
     * @param defaultValue The default return value if not found
     * @return The localized string
     */
    public String getString( String key, String defaultValue)
    {
        return  super.getString(key, defaultValue);
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

