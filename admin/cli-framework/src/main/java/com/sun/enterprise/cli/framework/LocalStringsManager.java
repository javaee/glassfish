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

/*
 * LocalStringsManager.java
 *
 * Created on July 29, 2003, 4:26 PM
 */

package com.sun.enterprise.cli.framework;

//imports
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Vector;
import java.util.Properties;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 *
 * @author  pa100654
 */
public class LocalStringsManager {
    
    private final String          packageName;
    private final String          propertyFile;
    private final Vector<ResourceBundle> resourceBundles = new Vector<ResourceBundle>();
    public static final String   DEFAULT_STRING_VALUE = "Key not found";

    /** Creates a new instance of LocalStringsManager */
    public LocalStringsManager(String packageNameIn, String propertyFileIn) 
    {
        this.packageName = packageNameIn;
        this.propertyFile = propertyFileIn;
        ResourceBundle resourceBundle = 
                ResourceBundle.getBundle(packageNameIn + "." + propertyFile);
        resourceBundles.add(resourceBundle);
    }
    
    /** Creates a new instance of LocalStringsManager from the 
        properties Vector 
     */
    public LocalStringsManager(Vector<Properties> localizePropertiesList) 
    {
        this.packageName    = null;
        this.propertyFile   = null;
        
        for (int i = 0; i < localizePropertiesList.size(); i++)
        {
            Properties properties = localizePropertiesList.get(i);
            String packageNameStr = (String) properties.get("base-package");
            String propertyFileStr = (String) properties.get("property-file-name");
            ResourceBundle resourceBundle = 
                ResourceBundle.getBundle(packageNameStr + "." + propertyFileStr);
            resourceBundles.add(resourceBundle);
        }
    }

        
    /** Creates a new instance of LocalStringsManager from the 
        properties Vector of Strings and locale 
     */
    public LocalStringsManager(Vector<String> localizeStringList, Locale locale) 
    {
        this.packageName    = null;
        this.propertyFile   = null;
        
        for (int i = 0; i < localizeStringList.size(); i++)
        {
            ResourceBundle resourceBundle = 
                ResourceBundle.getBundle(localizeStringList.get(i),
                                         locale);
            resourceBundles.add(resourceBundle);
        }
    }

    
    /*
     *returns the property file name
     */
    public String getPropertiesFile()
    {
        return propertyFile;
    }
    
    /*
     * returns the base package name
     */
    public String getPackageName()
    {
        return packageName;
    }

    
    /*
     *returns resourceBundles
     */
    public Vector<ResourceBundle> getResourceBundles()
    {
        return resourceBundles;
    }
    
    
    /*
     *  Returns the Localized string properties file
     */
    public String getString(String key)
    {
        Iterator resourcesIter = resourceBundles.iterator();
        String value = DEFAULT_STRING_VALUE + " (" + key + ")";
        while (resourcesIter.hasNext())
        {
            ResourceBundle resourceBundle = (ResourceBundle)resourcesIter.next();
            try
            {
                value = resourceBundle.getString(key);
                break;
            }
            catch (MissingResourceException mre)
            {
                // if not found, try next resource bundle in the iterator
            }
        }
        return value;
    }

    /*
     *  Return the Localized string with the inserted values
     */
    public String getString(String key, Object[] toInsert) 
                    throws CommandValidationException
    {
        String fmtStr = null;
        try
        {
            MessageFormat msgFormat = 
                            new MessageFormat(getString(key));
            fmtStr = msgFormat.format(toInsert);
        }
        catch(Exception e)
        {
            throw new CommandValidationException(e);
        }
        return fmtStr;
    }
}
