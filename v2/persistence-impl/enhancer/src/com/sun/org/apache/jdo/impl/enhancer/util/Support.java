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


package com.sun.org.apache.jdo.impl.enhancer.util;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Hashtable;
import java.text.MessageFormat;



class I18NHelper
{
    private static Hashtable	bundles = new Hashtable();
    private static Locale 	locale = Locale.getDefault();

    /**
     * Constructor
     */
    public I18NHelper()
    {}

    /**
     * Load ResourceBundle by bundle name
     */
    public static ResourceBundle loadBundle(String bundleName)
    {
        ResourceBundle messages = (ResourceBundle)bundles.get(bundleName);

        if (messages == null) //not found as loaded - add
        {
            messages = ResourceBundle.getBundle(bundleName, locale);
            bundles.put(bundleName, messages);
        }
        return messages;
    }

  
    /**
     * Returns message as String
     */
    final public static String getMessage(ResourceBundle messages, String messageKey) 
    {
        return messages.getString(messageKey);
    }

    /**
     * Formats message by adding Array of arguments
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, Object msgArgs[]) 
    {
        for (int i=0; i<msgArgs.length; i++) {
            if (msgArgs[i] == null) msgArgs[i] = ""; // NOI18N
        }
        MessageFormat formatter = new MessageFormat(messages.getString(messageKey));
        return formatter.format(msgArgs);
    }
    /**
     * Formats message by adding a String argument
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, String arg) 
    {
        Object []args = {arg};
        return getMessage(messages, messageKey, args);
    }
    /**
     * Formats message by adding two String arguments
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, String arg1,
                                          String arg2) 
    {
        Object []args = {arg1, arg2};
        return getMessage(messages, messageKey, args);
    }
    /**
     * Formats message by adding three String arguments
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, String arg1,
                                          String arg2, String arg3) 
    {
        Object []args = {arg1, arg2, arg3};
        return getMessage(messages, messageKey, args);
    }
    /**
     *
     * Formats message by adding an Object as an argument
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, Object arg) 
    {
        Object []args = {arg};
        return getMessage(messages, messageKey, args);
    }
    /**
     * Formats message by adding an int as an argument
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, int arg) 
    {
        Object []args = {new Integer(arg)};
        return getMessage(messages, messageKey, args);
    }
    /**
     * Formats message by adding a boolean as an argument
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, boolean arg) 
    {
        Object []args = {String.valueOf(arg)};
        return getMessage(messages, messageKey, args);
    }
}


/**
 * Basic support for enhancer implementation.
 */
public class Support
    extends Assertion
{
    //^olsen: hack
    static public Timer timer = new Timer();

    /**
     * I18N message handler
     */
    static private ResourceBundle MESSAGES;


    /**
     *
     */
    static
    {
        try
        {
            MESSAGES = I18NHelper.loadBundle("com.sun.org.apache.jdo.impl.enhancer.Bundle");
        }
        catch (java.util.MissingResourceException ex)
        {
            ex.printStackTrace ();
        }
    }

    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key)
    {
        return I18NHelper.getMessage(MESSAGES, key);
    }

    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key,
                                          String arg)
    {
        return I18NHelper.getMessage(MESSAGES, key, arg);
    }

    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key,
                                          String arg1,
                                          String arg2)
    {
        return I18NHelper.getMessage(MESSAGES, key, arg1, arg2);
    }

    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key,
                                          String arg1,
                                          String arg2,
                                          String arg3)
    {
        return I18NHelper.getMessage(MESSAGES, key, arg1, arg2, arg3);
    }

    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key,
                                          int arg1,
                                          String arg2)
    {
        return I18NHelper.getMessage(MESSAGES, key,
                                     new Object[]{new Integer(arg1), arg2});
    }

    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key,
                                          Object[] args)
    {
        return I18NHelper.getMessage(MESSAGES, key, args);
    }
}
