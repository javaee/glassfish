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

package com.sun.enterprise.cli.commands;

import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;
import com.sun.enterprise.cli.framework.*;

/**
 *  This class is the export command.
 *  It will set the given environment variable in multimode.
 */
public class ExportCommand extends S1ASCommand 
{

    private final static String ENVIRONMENT_PREFIX = "AS_ADMIN_";
    private final static String ENVIRONMENT_DELIMITER = "=";
    private final static String PASSWORD_STRING = "password";

    private CommandEnvironment commandEnvironment = 
                                    CommandEnvironment.getInstance();

    /** Creates new ExportCommand */
    public ExportCommand() 
    {
    }

    /**
     * Validates the Options for correctness
     * @return boolean returns true if validation is succesful else false
     */
    public boolean validateOptions() throws CommandValidationException 
    {
        return super.validateOptions();
    }
    
    /**
     * Executes the command
     * @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException 
    {
        validateOptions();
        
        Vector operands = getOperands();
        if (operands.size() == 0)
        {
            printAllEnvValues();
            return;
        }
        for (int ii = 0; ii < operands.size(); ii++)
        {
            updateEnvironment((String)operands.elementAt(ii));
        }
    }

    
    /**
     * this method check for prefix AS_ADMIN
     * retursn the name without the prefix AS_ADMIN
     */
    private String checkForPrefix(String name) throws CommandException
    {
        String envName = null;
        //check for prefix AS_ADMIN
        if (name.regionMatches(true, 0, ENVIRONMENT_PREFIX, 0, 
                               ENVIRONMENT_PREFIX.length())) 
        {
	        //extract AS_ADMIN from sOperandName
            envName =  name.substring(ENVIRONMENT_PREFIX.length());
        }
        else 
        {
            throw new CommandException(getLocalizedString("CouldNotSetVariable",
                                                          new Object[] {name}));
        }
        return envName.toLowerCase();
    }

    
    /**
     * this method prints the environment variable.
     */
    private void printAllEnvValues()
    {
        if ((commandEnvironment.getNumEnvironments() == 0) &&
                (MultiProcessCommand.getNumLocalEnvironments() == 0))
        {
            CLILogger.getInstance().printDetailMessage(
                                        getLocalizedString("NoEnvironment"));
        }
        else
        {
            HashMap allEnvironments = commandEnvironment.getEnvironments();
            // Add Local Environment values (Will contain only for passwords)
            allEnvironments.putAll(MultiProcessCommand.getLocalEnvironments());
            printEnvValues(allEnvironments);
        }
    }


    /**
     * Prints the name,value pairs of the local or global environments
     */
    private void printEnvValues(HashMap allEnvironments)
    {
        final Iterator envIterator = allEnvironments.keySet().iterator();

        while (envIterator.hasNext())
        {
            final String name = (String) envIterator.next();
            CLILogger.getInstance().printDebugMessage(
                                        commandEnvironment.toString());
            final Object value = allEnvironments.get(name);
            printEnvValue(name, value);
        }
    }

    
    /**
     *  print one environment
     *  @param name used in the environment
     *  @param value is the value of the given name in environment
     */
    private void printEnvValue(String name, Object value)
    {
        final String displayName = ENVIRONMENT_PREFIX.concat(name.toUpperCase());
        //Do not display the password value in clear text
        if (name.endsWith(PASSWORD_STRING))
        {
            CLILogger.getInstance().printMessage(displayName + " = ********");
        }
        else
        {
            CLILogger.getInstance().printMessage(displayName + " = " + value);
        }
    }
    

    /** 
     *  this method updates the variable in the CommandEnvironment object.
     *  @param nameStr - the name of the environment 
     */
    private void updateEnvironment(String nameStr)
	throws CommandException
    {
        final int nameend = nameStr.indexOf(ENVIRONMENT_DELIMITER);
        //remove the environment if command is "export AS_ADMIN_<name>="
        if (nameend == nameStr.length()-1) 
        {
            final String envName = checkForPrefix(nameStr.substring(0, nameend));
            if (MultiProcessCommand.removeLocalEnvironment(envName) != null)
            {
                // Also remove the global environment if exists
                commandEnvironment.removeEnvironment(envName);
            }
            else if (commandEnvironment.removeEnvironment(envName) == null)
            {
                 throw new CommandException(
                            getLocalizedString("UnableToRemoveEnv",
                              new Object[] {nameStr.subSequence(0, nameend)}));
            }
        }
        //print the value if the command is "export AS_ADMIN_<name>"
        else if (nameend == -1)
        {
            final String name = checkForPrefix(nameStr);
            if (MultiProcessCommand.getLocalEnvironmentValue(name) != null)
                printEnvValue(name, 
                            MultiProcessCommand.getLocalEnvironmentValue(name));
            else
                printEnvValue(name,
                            commandEnvironment.getEnvironmentValue(name));
        }
        //set the environment if the command is "export AS_ADMIN_<name>=<value>"
        else 
        {
            final String envName = checkForPrefix(nameStr.substring(0, nameend));
            //Set only the password options in local environment
            //to avoid printing the password warning message when in multimode
            if (envName.matches(NOT_DEPRECATED_PASSWORDFILE_OPTIONS))
                MultiProcessCommand.setLocalEnvironment(envName, 
                                              nameStr.substring(nameend+1));
            else
                commandEnvironment.setEnvironment(envName,
                                              nameStr.substring(nameend+1));
        }
    }
}
