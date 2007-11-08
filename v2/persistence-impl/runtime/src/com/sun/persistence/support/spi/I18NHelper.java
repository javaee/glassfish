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

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.persistence.support.spi;

import java.util.*;
import java.text.MessageFormat;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.persistence.support.JDOFatalInternalException;

/** Helper class for constructing messages from bundles.  The intended usage
 * of this class is to construct a new instance bound to a bundle, as in
 * <P>
 * <code>I18NHelper msg = I18NHelper.getInstance("com.sun.persistence.support.Bundle");</code>
 * <P>
 * This call uses the class loader that loaded the I18NHelper class to find
 * the specified Bundle. The class provides two overloaded getInstance
 * methods allowing to specify a different class loader: 
 * {@link #getInstance(Class cls)} looks for a bundle
 * called "Bundle.properties" located in the package of the specified class 
 * object and {@link #getInstance(String bundleName,ClassLoader loader)} 
 * uses the specified class loader to find the bundle.
 * <P>
 * Subsequently, instance methods can be used to format message strings 
 * using the text from the bundle, as in 
 * <P>
 * <code>throw new JDOFatalInternalException (msg.msg("ERR_NoMetadata", cls.getName()));</code>
 * @since 1.0.1
 * @version 1.1
 */        
public class I18NHelper {

    /** Bundles that have already been loaded 
     */
    private static Hashtable    bundles = new Hashtable();
    
    /** Helper instances that have already been created 
     */
    private static Hashtable    helpers = new Hashtable();
    
    /** The default locale for this VM.
     */
    private static Locale       locale = Locale.getDefault();

    /** The name of the bundle used by this instance of the helper.
     */
    private final String        bundleName;

    /** The bundle used by this instance of the helper.
     */
    private ResourceBundle      bundle = null;

    /** Throwable if ResourceBundle couldn't be loaded
     */
    private Throwable           failure = null;

    /** The unqualified standard name of a bundle. */
    private static final String bundleSuffix = ".Bundle";    // NOI18N

    /** Constructor */
    private I18NHelper() {
        this.bundleName = null;
    }

    /** Constructor for an instance bound to a bundle.
     * @param bundleName the name of the resource bundle
     * @param loader the class loader from which to load the resource
     * bundle
     */
    private I18NHelper (String bundleName, ClassLoader loader) {
        this.bundleName = bundleName;
        try {
            bundle = loadBundle (bundleName, loader);
        }
        catch (Throwable e) {
            failure = e;
        }
    }
    
    /** An instance bound to a bundle. This method uses the current class 
     * loader to find the bundle.
     * @param bundleName the name of the bundle
     * @return the helper instance bound to the bundle
     */
    public static I18NHelper getInstance (String bundleName) {
        return getInstance (bundleName, I18NHelper.class.getClassLoader());
    }

    /** An instance bound to a bundle. This method figures out the bundle name
     * for the class object's package and uses the class' class loader to
     * find the bundle. Note, the specified class object must not be
     * <code>null</code>.
     * @param cls the class object from which to load the resource bundle
     * @return the helper instance bound to the bundle
     */
    public static I18NHelper getInstance (final Class cls) {
        ClassLoader classLoader = (ClassLoader) AccessController.doPrivileged (
            new PrivilegedAction () {
                public Object run () {
                    return cls.getClassLoader();
                }
            }
            );
        String bundle = getPackageName (cls.getName()) + bundleSuffix;
        return getInstance (bundle, classLoader);
    }

    /** An instance bound to a bundle. This method uses the specified class
     * loader to find the bundle. Note, the specified class loader must not
     * be <code>null</code>.
     * @param bundleName the name of the bundle
     * @param loader the class loader from which to load the resource
     * bundle
     * @return the helper instance bound to the bundle
     */
    public static I18NHelper getInstance (String bundleName, 
                                          ClassLoader loader) {
        I18NHelper helper = (I18NHelper) helpers.get (bundleName);
        if (helper != null) {
            return helper;
        }
        helper = new I18NHelper(bundleName, loader);
        helpers.put (bundleName, helper);
        // if two threads simultaneously create the same helper, return the first
        // one to be put into the Hashtable.  The other will be garbage collected.
        return (I18NHelper) helpers.get (bundleName);
    }

    /** Message formatter
     * @param messageKey the message key
     * @return the resolved message text
     */
    public String msg (String messageKey) {
        assertBundle (messageKey);
        return getMessage (bundle, messageKey);
    }

    /** Message formatter
     * @param messageKey the message key
     * @param arg1 the first argument
     * @return the resolved message text
     */
    public String msg (String messageKey, Object arg1) {
        assertBundle (messageKey);
        return getMessage (bundle, messageKey, arg1);
    }

    /** Message formatter
     * @param messageKey the message key
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the resolved message text
     */
    public String msg (String messageKey, Object arg1, Object arg2) {
        assertBundle (messageKey);
        return getMessage (bundle, messageKey, arg1, arg2);
    }

    /** Message formatter
     * @param messageKey the message key
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the resolved message text
     */
    public String msg (String messageKey, Object arg1, Object arg2, Object arg3) {
        assertBundle (messageKey);
        return getMessage (bundle, messageKey, arg1, arg2, arg3);
    }

    /** Message formatter
     * @param messageKey the message key
     * @param args the array of arguments
     * @return the resolved message text
     */
    public String msg (String messageKey, Object[] args) {
        assertBundle (messageKey);
        return getMessage (bundle, messageKey, args);
    }

    /** Message formatter
     * @param messageKey the message key
     * @param arg the argument
     * @return the resolved message text
     */
    public String msg (String messageKey, int arg) {
        assertBundle (messageKey);
        return getMessage(bundle, messageKey, arg);
    }
    
    /** Message formatter
     * @param messageKey the message key
     * @param arg the argument
     * @return the resolved message text
     */
    public String msg (String messageKey, boolean arg) {
        assertBundle (messageKey);
        return getMessage(bundle, messageKey, arg);
    }
    
    /** Returns the resource bundle used by this I18NHelper.
     * @return the associated resource bundle
     * @since 1.1
     */
    public ResourceBundle getResourceBundle () {
        assertBundle ();
        return bundle;
    }
    
    //========= Internal helper methods ==========

    /**
     * Load ResourceBundle by bundle name
     * @param bundleName the name of the bundle
     * @param loader the class loader from which to load the resource bundle
     * @return  the ResourceBundle
     */
    final private static ResourceBundle loadBundle(
        String bundleName, ClassLoader loader) {
        ResourceBundle messages = (ResourceBundle)bundles.get(bundleName);

        if (messages == null) //not found as loaded - add
        {
            messages = ResourceBundle.getBundle(bundleName, locale, loader);
            bundles.put(bundleName, messages);
        }
        return messages;
    }

    /** Assert resources available
     * @since 1.1
     * @throws JDOFatalInternalException if the resource bundle could not
     * be loaded during construction.
     */
    private void assertBundle () {
        if (failure != null)
            throw new JDOFatalInternalException (
                "No resources could be found for bundle:\"" + 
                bundle + "\" ", failure);
    }
    
    /** Assert resources available
     * @param key the message key 
     * @since 1.0.2
     * @throws JDOFatalInternalException if the resource bundle could not
     * be loaded during construction.
     */
    private void assertBundle (String key) {
        if (failure != null)
            throw new JDOFatalInternalException (
                "No resources could be found to annotate error message key:\"" + 
                key + "\"", failure);
    }

    /**
     * Returns message as <code>String</code>
     * @param messages the resource bundle
     * @param messageKey the message key
     * @return the resolved message text
     */
    final private static String getMessage(ResourceBundle messages, String messageKey) 
    {
        return messages.getString(messageKey);
    }

    /**
     * Formats message by adding array of arguments
     * @param messages the resource bundle
     * @param messageKey the message key
     * @param msgArgs an array of arguments to substitute into the message
     * @return the resolved message text
     */
    final private static String getMessage(ResourceBundle messages, String messageKey, Object msgArgs[]) 
    {
        for (int i=0; i<msgArgs.length; i++) {
            if (msgArgs[i] == null) msgArgs[i] = ""; // NOI18N
        }
        MessageFormat formatter = new MessageFormat(messages.getString(messageKey));
        return formatter.format(msgArgs);
    }
    
    /**
     * Formats message by adding an <code>Object</code> argument.
     * @param messages the resource bundle
     * @param messageKey the message key
     * @param arg the argument
     * @return the resolved message text
     */
    final private static String getMessage(ResourceBundle messages, String messageKey, Object arg) 
    {
        Object []args = {arg};
        return getMessage(messages, messageKey, args);
    }
    
    /**
     * Formats message by adding two <code>Object</code> arguments.
     * @param messages the resource bundle
     * @param messageKey the message key
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the resolved message text
     */
    final private static String getMessage(ResourceBundle messages, String messageKey, Object arg1,
                                   Object arg2) 
    {
        Object []args = {arg1, arg2};
        return getMessage(messages, messageKey, args);
    }
    
    /**
     * Formats message by adding three <code>Object</code> arguments.
     * @param messages the resource bundle
     * @param messageKey the message key
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the resolved message text
     */
    final private static String getMessage(ResourceBundle messages, String messageKey, Object arg1,
                                   Object arg2, Object arg3) 
    {
        Object []args = {arg1, arg2, arg3};
        return getMessage(messages, messageKey, args);
    }

    /**
     * Formats message by adding an <code>int</code> as an argument.
     * @param messages the resource bundle
     * @param messageKey the message key
     * @param arg the argument
     * @return the resolved message text
     */
    final private static String getMessage(ResourceBundle messages, String messageKey, int arg) 
    {
        Object []args = {new Integer(arg)};
        return getMessage(messages, messageKey, args);
    }
    
    /**
     * Formats message by adding a <code>boolean</code> as an argument.
     * @param messages the resource bundle
     * @param messageKey the message key
     * @param arg the argument
     * @return the resolved message text
     */
    final private static String getMessage(ResourceBundle messages, String messageKey, boolean arg) 
    {
        Object []args = {String.valueOf(arg)};
        return getMessage(messages, messageKey, args);
    }

    /**  
     * Returns the package portion of the specified class.
     * @param className the name of the class from which to extract the 
     * package 
     * @return package portion of the specified class
     */   
    final private static String getPackageName(final String className)
    { 
        final int index = className.lastIndexOf('.');
        return ((index != -1) ? className.substring(0, index) : ""); // NOI18N
    }
}
