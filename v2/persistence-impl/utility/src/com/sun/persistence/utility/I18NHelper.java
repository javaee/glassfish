/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.utility;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @deprecated
 */
public class I18NHelper {
    private static final String bundleSuffix = ".Bundle";	// NOI18N
    private static Hashtable bundles = new Hashtable();
    private static Locale locale = Locale.getDefault();

    /**
     * Constructor
     */
    public I18NHelper() {
    }

    /**
     * Load ResourceBundle by bundle name
     */
    public static ResourceBundle loadBundle(String bundleName) {
        return loadBundle(bundleName, I18NHelper.class.getClassLoader());
    }

    /**
     * Load ResourceBundle by bundle name and class loader
     */
    public static ResourceBundle loadBundle(String bundleName,
            ClassLoader loader) {
        ResourceBundle messages = (ResourceBundle) bundles.get(bundleName);

        if (messages == null) //not found as loaded - add
        {
            messages = ResourceBundle.getBundle(bundleName, locale, loader);
            bundles.put(bundleName, messages);
        }
        return messages;
    }

    /**
     * Load ResourceBundle by class object - figure out the bundle name for the
     * class object's package and use the class' class loader.
     */
    public static ResourceBundle loadBundle(Class classObject) {
        return loadBundle(
                JavaTypeHelper.getPackageName(classObject.getName())
                + bundleSuffix,
                classObject.getClassLoader());
    }

    /**
     * Returns message as String
     */
    final public static String getMessage(ResourceBundle messages,
            String messageKey) {
        return messages.getString(messageKey);
    }

    /**
     * Formats message by adding Array of arguments
     */
    final public static String getMessage(ResourceBundle messages,
            String messageKey, Object msgArgs[]) {
        for (int i = 0; i < msgArgs.length; i++) {
            if (msgArgs[i] == null) {
                msgArgs[i] = ""; // NOI18N
            }
        }
        MessageFormat formatter = new MessageFormat(
                messages.getString(messageKey));
        return formatter.format(msgArgs);
    }

    /**
     * Formats message by adding a String argument
     */
    final public static String getMessage(ResourceBundle messages,
            String messageKey, String arg) {
        Object[] args = {arg};
        return getMessage(messages, messageKey, args);
    }

    /**
     * Formats message by adding two String arguments
     */
    final public static String getMessage(ResourceBundle messages,
            String messageKey, String arg1, String arg2) {
        Object[] args = {arg1, arg2};
        return getMessage(messages, messageKey, args);
    }

    /**
     * Formats message by adding three String arguments
     */
    final public static String getMessage(ResourceBundle messages,
            String messageKey, String arg1, String arg2, String arg3) {
        Object[] args = {arg1, arg2, arg3};
        return getMessage(messages, messageKey, args);
    }

    /**
     * Formats message by adding an Object as an argument
     */
    final public static String getMessage(ResourceBundle messages,
            String messageKey, Object arg) {
        Object[] args = {arg};
        return getMessage(messages, messageKey, args);
    }

    /**
     * Formats message by adding an int as an argument
     */
    final public static String getMessage(ResourceBundle messages,
            String messageKey, int arg) {
        Object[] args = {new Integer(arg)};
        return getMessage(messages, messageKey, args);
    }

    /**
     * Formats message by adding a boolean as an argument
     */
    final public static String getMessage(ResourceBundle messages,
            String messageKey, boolean arg) {
        Object[] args = {String.valueOf(arg)};
        return getMessage(messages, messageKey, args);
    }

}
