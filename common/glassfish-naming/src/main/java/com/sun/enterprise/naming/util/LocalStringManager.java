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
package com.sun.enterprise.naming.util;

/**
 * A local string manager.
 * This interface describes the access to i18n messages for classes that need
 * them.
 */

public interface LocalStringManager {

    /**
     * Get a localized string.
     * Strings are stored in a single property file per package named
     * LocalStrings[_locale].properties. Starting from the class of the
     * caller, we walk up the class hierarchy until we find a package
     * resource bundle that provides a value for the requested key.
     * <p/>
     * <p>This simplifies access to resources, at the cost of checking for
     * the resource bundle of several classes upon each call. However, due
     * to the caching performed by <tt>ResourceBundle</tt> this seems
     * reasonable.
     * <p/>
     * <p>Due to that, sub-classes <strong>must</strong> make sure they don't
     * have conflicting resource naming.
     *
     * @param callerClass  The object making the call, to allow per-package
     *                     resource bundles
     * @param key          The name of the resource to fetch
     * @param defaultValue The default return value if not found
     * @return The localized value for the resource
     */
    public String getLocalString(
            Class callerClass,
            String key,
            String defaultValue
    );

    /**
     * Get a local string for the caller and format the arguments accordingly.
     *
     * @param callerClass   The caller (to walk through its class hierarchy)
     * @param key           The key to the local format string
     * @param defaultFormat The default format if not found in the resources
     * @param arguments     The set of arguments to provide to the formatter
     * @return A formatted localized string
     */
    public String getLocalString(
            Class callerClass,
            String key,
            String defaultFormat,
            Object arguments[]
    );
}
