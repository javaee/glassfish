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
 * Created on September 16, 2003
 */

package org.apache.tools.ant.taskdefs.optional.sun.appserv;

//imports
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Properties;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 *  This is a utility class for getting localized string.
 *  Strings are stored in a single property file per package named
 *  LocalStrings[_locale].properties.
 *
 */
public class LocalStringsManager {
    
    private String packageName = "org.apache.tools.ant.taskdefs.optional.sun.appserv";
    private String propertyFile = "LocalStrings";
    private ResourceBundle resourceBundle = null;


    /** Creates a new instance of LocalStringsManager */
    public LocalStringsManager()
    {
        this.resourceBundle = ResourceBundle.getBundle(packageName + "." + propertyFile);
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
     *  Returns the Localized string properties file
     */
    public String getString(String key)
    {
        String value = "";
        try
        {
            value = resourceBundle.getString(key);
        }
        catch (MissingResourceException mre)
        {
            // if not found, try next resource bundle in the iterator
        }
        return value;
    }


    /*
     *  Return the Localized string with the inserted values
     */
    public String getString(String key, Object[] toInsert) 
    {
        String fmtStr = "";
        try
        {
            MessageFormat msgFormat = 
                            new MessageFormat(getString(key));
            fmtStr = msgFormat.format(toInsert);
        }
        catch(Exception e)
        {
        }
        return fmtStr;
    }
}
