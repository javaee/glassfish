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
import com.sun.enterprise.cli.framework.*;


/**
 *  This class is the unset command.
 *  It will unset the given environment variable in multimode.
 */
public class UnsetCommand extends S1ASCommand 
{

    private final static String ENVIRONMENT_PREFIX = "AS_ADMIN_";
    private final static String ENVIRONMENT_DELIMITER = "=";

    private CommandEnvironment commandEnvironment = CommandEnvironment.getInstance();

    /** Creates new UnsetCommand */
    public UnsetCommand() 
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
        
        for (int ii = 0; ii < operands.size(); ii++)
        {
	    updateEnvironment((String)operands.elementAt(ii));
        }
    }

    
    /**
     * this method check for prefix AS_ADMIN
     * returns the name without the prefix AS_ADMIN
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
	    throw new CommandException(getLocalizedString("CouldNotUnsetVariable",
							  new Object[] {name}));
	}
	return envName.toLowerCase();
    }

    
    /**
     *  this method updates the variables by removing them from the
     *  command environment (local and global).
     *  @param nameStr - the name of the environment to remove
     *  @throws CommandException if environment could not be removed.
     */
    private void updateEnvironment(String nameStr)
	throws CommandException
    {
	final String envName = checkForPrefix(nameStr);
	if (envName.equals("*"))
	{
            // Remove both the local and global environment variables
            removeAllEnvironments();
	}
	else
	{
            if (MultiProcessCommand.removeLocalEnvironment(envName) != null)
            {
                // Also remove the global environment if exists
                commandEnvironment.removeEnvironment(envName);
            }
            else if (commandEnvironment.removeEnvironment(envName) == null)
            {
		throw new CommandException(getLocalizedString("UnableToRemoveEnv",
					   new Object[] {nameStr}));
            }
	}
    }


    /**
     *  this method is called when AS_ADMIN_* is encountered.
     *  The wildcard means to remove all variables set in the environment.
     */
    private void removeAllEnvironments()
    {
        // remove the Local environments
        MultiProcessCommand.removeAllEnvironments();
        
        final java.util.HashMap envMap = new
           java.util.HashMap(commandEnvironment.getEnvironments());
        final java.util.Iterator envIter = envMap.keySet().iterator();

        while (envIter.hasNext())
        {
            final String envName = (String)envIter.next();
            if (commandEnvironment.removeEnvironment(envName) == null)
                CLILogger.getInstance().printWarning(getLocalizedString(
                                                    "UnableToRemoveEnv",
                                                    new Object[] {envName}));
        }
     }
}
