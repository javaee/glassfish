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
 * LocalStringsManagerFactory.java
 *
 * Created on July 29, 2003, 4:48 PM
 */

package com.sun.enterprise.cli.framework;

//imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Properties;

/**
 *
 *  This is a factory class that creates the LocalStringsManager
 * @author  pa100654
 */
public class LocalStringsManagerFactory {
    
    private static HashMap stringManagers = new HashMap();
    private static String PROPERTY_FILE = "LocalStrings";
    private final static String FRAMEWORK_BASE_PACKAGE = LocalStringsManagerFactory.class.getPackage().getName();
    private static LocalStringsManager commandLocalStringsManager = null;
    
    /** Creates a new instance of LocalStringsManagerFactory */
    protected LocalStringsManagerFactory() 
    {
    }
    
    /**
        Sets the LocalStringsManager object.
        @param packageName name of the package used by the LocalStringsManager.
        @param stringsManager LocalStringsManager object
    */
    public static void setInstance(String packageName, 
                                    LocalStringsManager stringsManager)
    {
        stringManagers.put(packageName, stringsManager);
    }

    /**
        Returns the instance of the LocalStringsManager object,
        the caller can access all the methods in this class.
        @param packageName name of the package
    */
    public static LocalStringsManager getLocalStringsManager(
                                            String packageName, 
                                            String propertiesFileName)
            throws CommandValidationException
    {
        LocalStringsManager stringsManager = 
                    (LocalStringsManager) stringManagers.get(packageName);
        if ((stringsManager == null) || 
            (!stringsManager.getPropertiesFile().equals(propertiesFileName)))
        {
            try
            {
                stringsManager = new LocalStringsManager(packageName, 
                                                            propertiesFileName);
                stringManagers.put(packageName, stringsManager);
            }
            catch(java.util.MissingResourceException mre)
            {
                //throw new CommandException(mre.getLocalizedMessage());
                throw new CommandValidationException(mre.getLocalizedMessage(), mre);
            }
        }
        return stringsManager;
    }


    /**
     *  Returns the instance of the LocalStringsManager object for 
     *	the CLI framework module.
     *  the caller can access all the methods in this class.
     *   @param packageName name of the package
     */
    public static LocalStringsManager getFrameworkLocalStringsManager()
            throws CommandValidationException
    {
        return getLocalStringsManager(FRAMEWORK_BASE_PACKAGE, PROPERTY_FILE);
    }

    /**
     *  sets the list of base_package and property_file_name properties for 
     *	the CLI Commands module.
     *   @param properties vector of java.util.Properties (name/value pairs of 
                            base-package and property-file-name) objects
     */
    public static void setCommandLocalStringsManagerProperties(
                                                Iterator propertiesIter)
            throws CommandValidationException
    {
        Vector localizePropertiesList = new Vector();
        while (propertiesIter.hasNext())
        {
		    Properties localizeProperties = new Properties();
            Properties properties = (Properties) propertiesIter.next();
            String basePackage = properties.getProperty("base-package");
            String propertyFile = properties.getProperty("property-file-name",
                                    LocalStringsManagerFactory.PROPERTY_FILE);
            if ((basePackage != null) && (!basePackage.equals("")))
            {
		CLILogger.getInstance().printDebugMessage("basePackage: " + basePackage);
		CLILogger.getInstance().printDebugMessage("propertyFile: " + propertyFile);
                localizeProperties.setProperty("base-package", basePackage);
                localizeProperties.setProperty("property-file-name", propertyFile);
                localizePropertiesList.add(localizeProperties);
            }
        }
        if (localizePropertiesList.size() > 0)
        {
            commandLocalStringsManager = 
                               new LocalStringsManager(localizePropertiesList);
        }
    }

    /**
     *  Returns the instance of the LocalStringsManager object for 
     *	the CLI Commands module.
     *  the caller can access all the methods in this class.
     */
    public static LocalStringsManager getCommandLocalStringsManager()
            throws CommandValidationException
    {
        if (commandLocalStringsManager == null)
        {
            LocalStringsManager lsm = 
                LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            throw new CommandValidationException(
                        lsm.getString("CouldNotFindLocalStringsProperties"));
        }
        return commandLocalStringsManager;
    }

}
