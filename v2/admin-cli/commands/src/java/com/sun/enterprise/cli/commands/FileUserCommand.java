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

import com.sun.enterprise.cli.framework.*;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import java.io.IOException;

/**
   This class gets called when create-file-user command is invoked.
   This class will overwrite validOptions in GenericCommand to validate
   the userpassword option.  If the userpassword and passwordfile options
   are not entered in the command line and interactive is true, then
   CLI will prompt the user for the password.
 */
public class FileUserCommand extends S1ASCommand
{
    private static final String USER_PASSWORD  = "userpassword";
    private static final String FILE_USER_NAME = "fileuser";
    
    protected String getPasswordOptionName() {
        return USER_PASSWORD;
    }
    
    
    protected String getPasswordPrompt()
    {
        return "FileUserPasswordPrompt";
    }
    

    protected String getPasswordConfirmationPrompt()
    {
        return "FileUserPasswordConfirmationPrompt";
    }
    
    
    protected boolean confirmPassword() {
        return true;
    }

    protected String getUserOptionName() {
        return FILE_USER_NAME;
    }
    
    
    protected String getUserPrompt()
    {
        return "FileUserPrompt";
    }
    
    
    /**
     *  this methods returns the user/alias password. 
     *  @return user/alias password
     *  @throws CommandValidationException if could not get userpassword or
     *  aliaspassword option 
     */
    protected String getPasswordOption()     
        throws CommandValidationException, CommandException
    {
        //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, mgr, config,
        //promptUser, confirm, validate)
        return getPassword(getPasswordOptionName(), getPasswordPrompt(), 
                            getPasswordConfirmationPrompt(),
                            true, false, false, false, null, null,
                            true, confirmPassword(), false, true);
    }

    
    private String getUserName() throws CommandValidationException
    {
        String userName;
        if ((getOperands() != null) && (getOperands().size() != 0))
            userName = (String) getOperands().get(0);
        else //prompt for fileuser
        {
            try {
                InputsAndOutputs.getInstance().getUserOutput().print(
                                    getLocalizedString(this.getUserPrompt()));
                userName = InputsAndOutputs.getInstance().getUserInput().getLine();
            }
            catch (IOException ioe)
            {
                throw new CommandValidationException(
                            getLocalizedString("CannotReadOption", 
                                        new Object[]{getUserOptionName()}));
            }
        }   
        
        return userName;
    }
    
    
    /**
       Validate the userpassword option.
     */    
    public boolean validateOptions() throws CommandValidationException
    {
    	return super.validateOptions();
    }
    
    public void runCommand() throws CommandException, CommandValidationException
    {
        validateOptions();
        //use http connector
        MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                                              getUser(), getPassword());
        final String objectName = getObjectName();
        final Object[] params = getParamsInfo();
        final String operationName = getOperationName();
        final String[] types = getTypesInfo();

        //get fileuser and set it in the params
        params[0] = getUserName();
        //get fileuser password and set it in the params
        params[1] = getPasswordOption();
        try
        { 
	    //if (System.getProperty("Debug") != null) printDebug(mbsc, objectName);
            Object returnValue = mbsc.invoke(new ObjectName(objectName), 
					     operationName, params, types);
            handleReturnValue(returnValue);
	    CLILogger.getInstance().printDetailMessage(getLocalizedString(
						       "CommandSuccessful",
						       new Object[] {name}));
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }        
    }
}
