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
import com.sun.appserv.management.client.prefs.*;
import java.io.File;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;


/**
   This class gets called when change-admin-password command is invoked.
   CLI will prompt the user for the password.
 */
public class ChangeAdminPasswordCommand extends S1ASCommand
{
    private static final String ADMIN_PASSWORD = "adminpassword";
    private static final String PASSWORD = "password";
    private static final String INTERACTIVE = "interactive";
    private static final String OLD_ADMIN_PASSWORD = "oldadminpassword";
    private static final String NEW_ADMIN_PASSWORD = "newadminpassword";
    private String newPassword;
    private String oldPassword;
    
    
    private String getOldAdminPassword() 
        throws CommandValidationException, CommandException
    {
        return getPassword(OLD_ADMIN_PASSWORD, 
            "OldAdminPasswordPrompt", null, 
            false, false, false, false, null, null, 
            true, false, true, false);
    }

    
    private String getNewAdminPassword() 
        throws CommandValidationException, CommandException
    {
        return getPassword(NEW_ADMIN_PASSWORD, 
            "NewAdminPasswordPrompt", "NewAdminPasswordConfirmationPrompt", 
            false, false, false, false, null, null, 
            true, true, true, false);
    }

    
    /**
       This method prompts the user for the old and new adminpassword.
     */    
    public boolean validateOptions() throws CommandValidationException
    {
    	super.validateOptions();
        
        try {
            /**
             * set interactive to true so that passwords can be prompted.
             **/
            setOption(INTERACTIVE, "true");
            //if user is not entered on command line, prompt for the user name.
            final String sUser = getUser();
            setOption(USER, sUser);
                
            oldPassword = getOldAdminPassword();
            setOption(PASSWORD, oldPassword);
            newPassword = getNewAdminPassword();
            setOption(ADMIN_PASSWORD, newPassword);
        }
        catch (CommandException ce) {
            throw new CommandValidationException(ce.getLocalizedMessage());
        }                
        
        return true;
    }


    /**
     * executes the command
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        if (!validateOptions())
            throw new CommandValidationException("Validation is false");

        final String host = getHost();
        final int port = getPort();
        final String userName = getUser();
        
            //use http connector
        MBeanServerConnection mbsc = getMBeanServerConnection(host, port, userName, oldPassword);
        final String objectName = getObjectName();
        final Object[] params = getParamsInfo();
        final String operationName = getOperationName();
        final String[] types = getTypesInfo();

        try
        { 
	    //if (System.getProperty("Debug") != null) printDebug(mbsc, objectName);
            Object returnValue = mbsc.invoke(new ObjectName(objectName), 
					     operationName, params, types);
            handleReturnValue(returnValue);
            updateLoginFile(userName, host, port);
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                           "CommandSuccessful",
                                                           new Object[] {name}));
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }        
    }

    private void updateLoginFile(final String user, final String host, final int port)
    {
        try {
            final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
            if (store.exists(host, port))
            {
                store.store(new LoginInfo(host, port, user, newPassword), true);
                CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                               "SuccessfullyUpdatedLoginInfo"));
            }
        }
        catch(final Exception e) {
                //ignore message if unable to update .asadminpass
        }
    }
        
}
