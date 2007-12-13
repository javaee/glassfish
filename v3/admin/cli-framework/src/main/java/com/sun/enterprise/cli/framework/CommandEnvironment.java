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
 * $Id: CommandEnvironment.java,v 1.4 2005/12/25 03:46:56 tcfujii Exp $
 */


package com.sun.enterprise.cli.framework;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

/**
   This class creates an environment for the options
 */

public class CommandEnvironment implements ICommandEnvironment
{
    //static instance of itself
    private static CommandEnvironment commandEnvironment;

    //Environment Prefix
    //this value is temporary here -- will move this to CLIDescriptor.xml
    private static final String ENVIRONMENT_PREFIX = "AS_ADMIN_";


    private HashMap environments;

    /**
	Default constructor.
    */
    protected CommandEnvironment()
    {
	    environments = (HashMap)getSystemEnvironment();
    }


    /** This method gets environment from os system environment
     *  @param strArray - the string array to convert
     *  @return 
     */
    private Map getSystemEnvironmentNative()
    {
        HashMap hashMap = new HashMap();
        try 
        {
            CLIDescriptorsReader cdr = CLIDescriptorsReader.getInstance();
            String envPrefix = cdr.getEnvironmentPrefix();
            //if not specified on descriptor file, then use the default value
            envPrefix = envPrefix==null?ENVIRONMENT_PREFIX:envPrefix;

            //get Global Env
            final String[] strArray = new CliUtil().getEnv(envPrefix);

            for (int ii=0; ii<strArray.length; ii++)
            {
                final int index = strArray[ii].indexOf("=");
                // need to break up the string in strArray
                // since the format is in name=value
                final String sOptionName = strArray[ii].substring(envPrefix.length(),
                                                                  index).toLowerCase();
                hashMap.put( sOptionName.replace('_', '-'),
                             strArray[ii].substring(index+1));
            }
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            CLILogger.getInstance().printDebugMessage(e.getLocalizedMessage());
            // Emit the warning message
            CLILogger.getInstance().printWarning(getLocalizedString("UnableToReadEnv"));
        }
        return hashMap;
    }


    /**
     *  This method uses System.getenv() (resurrected in jdk 1.5) to gets os environments.
     *  @return Map of the system environment.
     */
    private Map getSystemEnvironment()
    {
        //get environment prefix from CLI Descriptor file.
        final CLIDescriptorsReader cdr = CLIDescriptorsReader.getInstance();
        String sPrefix = cdr.getEnvironmentPrefix();
        //if not specified in descriptor file, then use the default value
        sPrefix = sPrefix==null?ENVIRONMENT_PREFIX:sPrefix;

        final Map<String, String> mEnv = System.getenv();
        HashMap<String, String> hmEnv = new HashMap<String, String>();
        final Iterator iterEnv = mEnv.keySet().iterator();

        while( iterEnv.hasNext() )
        {
            final String sEnvKey = ( String )iterEnv.next();
            if (sEnvKey.startsWith(sPrefix)) {
                    //extract the option name and convert it to lower case.
                final String sOptionName = sEnvKey.substring(sPrefix.length()).toLowerCase();
                    //replace occurrences of '_' to '-' since option name can only contain '-'
                    //this is a CLIP requirement.
                hmEnv.put(sOptionName.replace('_','-'), (String)mEnv.get(sEnvKey));
            }
        }
        return hmEnv;
    }
    


    /**
     * returns the instance of the CommandEnvironment
     */
    public static CommandEnvironment getInstance()
    {
        if (commandEnvironment == null)
        {
            commandEnvironment = new CommandEnvironment();
        }
        return commandEnvironment;
    }


    /**
     * This method adds a name and value to the environment.
     * @param name - option name 
     *  @param value - option value
     */
    public void setEnvironment(String name, String value)
    {
        if ( environments.containsKey(name) )
        {
            environments.remove( name );
        }
        environments.put(name, value );
    }


    /**
     *  This method removes environment
     *  from the environment.
     *  @param name is the name of the environment to be removed.
    */
    public Object removeEnvironment( String name )
    {
        return environments.remove(name);
    }

    /**
     *  returns the envrionment value by the given key
     */
    public Object getEnvironmentValue(String key)
    {
        return environments.get(key);
    }

    /**
	Returns an iterator over collection of Option objects.
    */
    public HashMap getEnvironments()
    {
        return environments;
    }


    /**
       This method returns a String format for list of environments in the
       environment with its argument values.
       @return  A String object.
    */
    public String toString()
    {
        String description = "";
        final Iterator environIter = environments.keySet().iterator();
        while( environIter.hasNext() )
        {
            String environKey = ( String )environIter.next();
            description   +=  " " + environKey + " = " + 
                                    (String)environments.get(environKey);
        }
        return description;
    }


    /**
	Returns the number of environments
    */
    public int getNumEnvironments()
    {
        return environments.size();
    }

    /**  returns the localized string from framework's LocalStrings.properties.
     *  Calls the LocalStringsManagerFactory.getFrameworkLocalStringsManager()
     *  method, returns "Key not found" if it cannot find the key
     *  @param key, the string to be localized
     */
    private String getLocalizedString(String key)
    {
        try
        {
            final LocalStringsManager lsm = 
            LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            return lsm.getString(key);
        }
        catch (CommandValidationException cve)
        {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
        }
    }

}

