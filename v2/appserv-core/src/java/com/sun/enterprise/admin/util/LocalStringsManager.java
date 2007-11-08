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
 * Implementation of a local string manager.
 * Provides access to i18n messages for classes that need them.
 * Now emulates the proper  sun.enterprise.util.LocalStringManagerImpl
 * with few essential differences:
 * <br>1. It uses package hierarchy, but not classes hierarchy for resource files searching(it allows to place resources separatly from source files)
 * <br>2. You can choose different resource file names (not only LocalName.properties);
 * <br>3. New optional mode - with fixedResourceBundle allows to exclude bundle file search repeating during the subsequent getString() calls;
 */

public class LocalStringsManager
{
    static final String DEFAULT_PROPERTY_FILE_NAME    = "LocalStrings";

    private String          m_propertyFileName;
    private String          m_basePackage;
    private ResourceBundle  m_FixedResourceBundle;
    
    /**
     * Create a string manager that looks for LocalStrings.properties in
     * the package of the basePackage.
     * @param basePackage Most high level package Class path with localized strings
     */
    public LocalStringsManager(String basePackage, String propertyFileName)
    {
        m_basePackage       = basePackage;
        m_propertyFileName  = propertyFileName;
    }
    
    public LocalStringsManager(String basePackage)
    {
        this(basePackage, DEFAULT_PROPERTY_FILE_NAME);
    }

    /**
     * Get a localized string.
     * @param startPackage The package name which defines the starting directory for resource bundle serach.
     * @param key The name of the resource to fetch
     * @param defaultValue The default return value if not found
     * @return The localized value for the resource
     */
    public String getString(String startPackage, String key, String defaultValue)
    {
        if(m_FixedResourceBundle!=null)
        {
            String value = m_FixedResourceBundle.getString(key);
            if(value!=null)
               return value;
            return defaultValue;
        }
               
        
        String stopPackage = m_basePackage;
        if(startPackage==null)
            startPackage = stopPackage;
        else
            if(!startPackage.startsWith(m_basePackage))
               stopPackage = startPackage; //maybe  = ""
        ResourceBundle resources  = null;

        while (startPackage.length()>0 && startPackage.length()>=stopPackage.length())
        {
            try
            {
                String resFileName = startPackage+"."+m_propertyFileName;
                startPackage = startPackage.substring(0, startPackage.lastIndexOf("."));
                resources = ResourceBundle.getBundle(resFileName);
                if ( resources != null )
                {
                    String value = resources.getString(key);
                    if ( value != null )
                        return value;
                }
            } catch (Exception ex)
            {
            }
        }
        
        // Look for a global resource (defined by defaultClass)
        if ( !stopPackage.equals(m_basePackage) )
        {
            return getString(null, key, defaultValue);
        }

        return defaultValue;
    }
    
    /**
     * Get a localized string from the base package.
     * @param key The name of the resource to fetch
     * @param defaultValue The default return value if not found
     * @return The localized string
     */
    public String getString(String key, String defaultValue)
    {
        return getString(null, key, defaultValue);
    }
    
    /**
     * Get a local string for the caller and format the arguments accordingly.
     * @param startPackage The package name which defines the starting directory for resource bundle serach.
     * @param key The key to the local format string
     * @param fmt The default format if not found in the resources
     * @param arguments The set of arguments to provide to the formatter
     * @return A formatted localized string
     */
    
    public String getString(
    String startPackage,
    String key,
    String defaultFormat,
    Object arguments[]
    )
    {
        MessageFormat f = new MessageFormat(
        getString(startPackage, key, defaultFormat));
        for (int i = 0; i < arguments.length; i++)
        {
            if ( arguments[i] == null )
            {
                arguments[i] = "null";
            } else if  ( !(arguments[i] instanceof String) &&
            !(arguments[i] instanceof Number) &&
            !(arguments[i] instanceof java.util.Date))
            {
                arguments[i] = arguments[i].toString();
            }
        }
        return f.format(arguments);
    }
    
    /**
     * Get a local string from the base package  and
     * format the arguments accordingly.
     * @param key The key to the local format string
     * @param defaultFormat The default format if not found in the resources
     * @param arguments The set of arguments to provide to the formatter
     * @return A formatted localized string
     */
    public String getString(
    String key,
    String defaultFormat,
    Object arguments[]
    )
    {
        return getString(null, key, defaultFormat, arguments);
    }

    protected void setFixedResourceBundle(String startPackage)
    {
        if(startPackage==null)
        {
            m_FixedResourceBundle = null;
            return;
        }
        
        String stopPackage = m_basePackage;
        if(startPackage==null)
            startPackage = stopPackage;
        else
            if(!startPackage.startsWith(m_basePackage))
               stopPackage = startPackage; //maybe  = ""
        ResourceBundle resources  = null;

        while (startPackage.length()>0 && startPackage.length()>=stopPackage.length())
        {
            try
            {
                String resFileName = startPackage+"."+m_propertyFileName;
                startPackage = startPackage.substring(0, startPackage.lastIndexOf("."));
                resources = ResourceBundle.getBundle(resFileName);
                if ( resources != null )
                {
                   m_FixedResourceBundle = resources;
                   return;
                }
            } catch (Exception ex)
            {
            }
        }
        
        // Look for a global resource (defined by defaultClass)
        if ( !stopPackage.equals(m_basePackage) )
        {
            setFixedResourceBundle(m_basePackage);
        }

    }
}

