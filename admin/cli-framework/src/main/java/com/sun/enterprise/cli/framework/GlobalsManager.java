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
	Some preexisting portions Copyright 2001 Netscape
	Communications Corp. All rights reserved.
 */

/*
 * Portions Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
	$Id: GlobalsManager.java,v 1.4 2007/04/04 16:33:17 pa100654 Exp $
 */

package com.sun.enterprise.cli.framework;

import java.util.ResourceBundle;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;

/**
    A utility class which sets and gets the command environment,
    user input information.
 */
public class GlobalsManager
{
    private static  GlobalsManager          sGlobalsMgr                 = null;
    private         ICommandEnvironment     mGlobalCommandEnvironment   = null;
    
    private static HashMap resourceBundles = new HashMap();
    
    private static String COMMANDS_BASE_PACKAGE = null;
    private final static String FRAMEWORK_BASE_PACKAGE = GlobalsManager.class.getPackage().getName();
    private static String FRAMEWORK_PROPERTY_FILE_NAME = "LocalStrings";
    private static String COMMANDS_PROPERTY_FILE_NAME = "LocalStrings";

    /**
        A constructor which is accessed by it's sub classes only.
     */

    protected GlobalsManager() {
		this(new CommandEnvironment());
	}
  
    protected GlobalsManager(ICommandEnvironment env){
	  mGlobalCommandEnvironment = env;
	  resourceBundles = new HashMap();
    }

    /**
        Sets the GlobalsManager object.
        @param    globalsMgr is the GlobalsManager object to be set.
    */
    public synchronized static void setInstance( GlobalsManager globalsMgr )
    {
        if ( sGlobalsMgr != globalsMgr )
        {
            sGlobalsMgr = globalsMgr;
        }
    }

    /**
        Returns the instance of the GlobalsManager through this object,
        the caller can access all the methods in this class.
    */
    public synchronized static GlobalsManager getInstance()
    {
        if( sGlobalsMgr == null )
        {
            sGlobalsMgr = new GlobalsManager();
        }
        return sGlobalsMgr;
    }

    /**
        Returns the command environment that is set in in GlobalsManager.
    */
    public ICommandEnvironment getGlobalsEnv()
    {
        return mGlobalCommandEnvironment;
    }

    /**
        Sets the global command environment. (global means the set of options
        which are base/common options to all the subcommands).
        @param    env is the command environment to be set.
    */
    public void setGlobalsEnv( ICommandEnvironment env )
    {
        mGlobalCommandEnvironment = env;
    }

    /**
        Returns the option value that is set in GlobalsManager.
        @param  optionName is the name of the option.
     */
    public String getOption( String optionName )
    {
        String optionValue = (String)mGlobalCommandEnvironment.getEnvironmentValue(optionName );
        return optionValue;
    }

    /**
        Sets the Option object in the GlobalsManager.
        @param   option is the Option object to be set.
     */
    public void setOption( String name, String value )
    {
        mGlobalCommandEnvironment.setEnvironment(name, value);
    }

    /** 
     *Removes the option from the options list
     */
    public void removeOption( String optionName)
    {
        mGlobalCommandEnvironment.removeEnvironment(optionName);
    }

    /*
     *  Returns the Localized string from the commands properties file
     */
    public static String getString(String key) throws CommandException
    {
        return getString(key, COMMANDS_BASE_PACKAGE, 
                            COMMANDS_PROPERTY_FILE_NAME);
    }

    /*
     *  Return the Localized string from the commands properties file 
     *  with the inserted values
     */
    public static String getString(String key, Object[] toInsert) throws CommandException
    {
        return getString(key, toInsert, COMMANDS_BASE_PACKAGE, 
                            COMMANDS_PROPERTY_FILE_NAME);
    }
    
    /*
     *  Returns the Localized string from the framework properties file
     */
    public static String getFrameworkString(String key) throws CommandException
    {
        return getString(key, FRAMEWORK_BASE_PACKAGE, 
                            FRAMEWORK_PROPERTY_FILE_NAME);
    }

    /*
     *  Return the Localized string from the commands properties file 
     *  with the inserted values
     */
    public static String getFrameworkString(String key, Object[] toInsert) 
                            throws CommandException
    {
        return getString(key, toInsert, FRAMEWORK_BASE_PACKAGE, 
                            FRAMEWORK_PROPERTY_FILE_NAME);
    }
    
    /*
     *  Returns the Localized string from the commands properties file
     */
    private static String getString(String key, String basePkg, String propertyFile) 
                    throws CommandException
    {
        return getResourceBundle(basePkg, propertyFile).getString(key);
    }

    /*
     *  Return the Localized string with the inserted values
     */
    private static String getString(String key, Object[] toInsert, 
                                    String basePkg, String propertyFile) 
                                    throws CommandException
    {
        String fmtStr = null;
        try
        {
            ResourceBundle resBundle = getResourceBundle(basePkg, propertyFile);
            MessageFormat msgFormat = 
                        new MessageFormat(resBundle.getString(key));
            fmtStr = msgFormat.format(toInsert);
        }
        catch(Exception e)
        {
            throw new CommandException(e.getLocalizedMessage());
        }
        return fmtStr;
    }
    
    private static ResourceBundle getResourceBundle(String packageName, 
                                                   String propertyFile) 
                                                    throws CommandException
    {
        ResourceBundle resBundle = (ResourceBundle) resourceBundles.get(packageName);
        if (resBundle == null)
        {
            try
            {
                resBundle = ResourceBundle.getBundle(
                                packageName + "." + propertyFile);
                resourceBundles.put(packageName, resBundle);
            }
            catch(java.util.MissingResourceException mre)
            {
                throw new CommandException(mre.getLocalizedMessage());
            }
        }
        return resBundle;
    }
    
    /*
     *  Sets the base package for the commands module
     */
    public static void setBasePackage(String basePkg)
    {
        COMMANDS_BASE_PACKAGE = basePkg;
    }
    
    /*
     *  Sets the property file for the commands module
     */
    public static void setPropertyFile(String fileName)
    {
        COMMANDS_PROPERTY_FILE_NAME = fileName;
    }
    
    public static void main(String[] args)
    {
        try
        {
            GlobalsManager globalsMgr = GlobalsManager.getInstance();
            System.out.println(globalsMgr.getFrameworkString("junk", new Object[]{"junk", "prashanth"}));
            globalsMgr.setBasePackage("com.sun.enterprise.cli.commands");
            System.out.println(globalsMgr.getString("junk", new Object[]{"junk", "prashanth"}));
        }
        catch(CommandException ce)
        {
            ce.printStackTrace();
        }
    }
   
}

