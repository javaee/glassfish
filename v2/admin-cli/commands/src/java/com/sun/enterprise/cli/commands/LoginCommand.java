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

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.enterprise.cli.framework.*;

import java.io.IOException;
import javax.management.MBeanServerConnection;

public class LoginCommand extends S1ASCommand 
{

    /**
     *  A method that validates the options/operands 
     *  @return boolean returns true if success else returns false
     *  @throws CommandValidationException
     */
    public boolean validateOptions() throws CommandValidationException
    {
    	return super.validateOptions();
    }

    
    /**
     *  Method that Executes the command
     *  @throws CommandException, CommandValidationException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        validateOptions();
        String userName = getUser();
        String password = getPassword();
        final String host = getHost();
        final int port = getPort();
        authenticate(host, port, userName, password);
        saveLogin(host, port, userName, password);
    }
    
    protected String getUser() throws CommandValidationException
    {
        try {
            InputsAndOutputs.getInstance().getUserOutput().print(
                                        getLocalizedString("AdminUserPrompt"));
            return InputsAndOutputs.getInstance().getUserInput().getLine();
        }
        catch (IOException ioe)
        {
            throw new CommandValidationException(getLocalizedString("CannotReadOption",
                                                    new Object[]{"user"}));
        }
    }


    protected String getPassword() throws CommandValidationException
    {
        String passwordValue;
        
        try 
        {
            InputsAndOutputs.getInstance().getUserOutput().print(
                                getLocalizedString("AdminPasswordPrompt"));
            passwordValue = new CliUtil().getPassword();
        }
        catch (java.lang.NoClassDefFoundError e)
        {
            passwordValue = readInput();
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            passwordValue = readInput();
        }
        catch (Exception e)
        {
            throw new CommandValidationException(e);
        }
        return passwordValue;
    }
    
    
    private void authenticate(final String host, final int port, final String user, final String password)
    throws CommandException, CommandValidationException {
        try {
            final Object[] params = new Object[] {host, ""+port};
            CLILogger.getInstance().printMessage(getLocalizedString("AuthenticatingMsg", params));
            final MBeanServerConnection mbsc = super.getMBeanServerConnection(host, port, user, password);
            final String dd = mbsc.getDefaultDomain(); //calls a dummy method to make sure authentication is OK
            final String msg = "Authentication succeeded to: " + host + "and port: " + port;
            CLILogger.getInstance().printDebugMessage(msg);
        }
        catch(final IOException ioe) {
            throw new CommandException(ioe);
        }
    }
    
    
    private void saveLogin(final String host, final int port, final String user, final String password)
    throws CommandException {
        try {
            final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
            final LoginInfo login      = new LoginInfo(host, port, user, password);
            String msg                 = null;
            final boolean storeIt      = handleExists(store, login);
            if (storeIt) {
                store.store(login, true);
                final Object[] params = new Object[] {login.getUser(), login.getHost(), ""+login.getPort(), store.getName()};
                msg = getLocalizedString("LoginInfoStored", params);
            }
            else {
                final Object[] params = new Object[] {login.getHost(), ""+login.getPort()};
                msg = getLocalizedString("LoginInfoNotStored", params);
            }
            CLILogger.getInstance().printMessage(msg);
        }
        catch(final Exception e) {
            throw new CommandException(e);
        }
    }
    
    
    private boolean handleExists(final LoginInfoStore store, final LoginInfo login) throws Exception {
        boolean storeIt = true;
        if (store.exists(login.getHost(), login.getPort())) {
            storeIt = promptOnce(login);
        }
        return ( storeIt );
    }
    
    
    private boolean promptOnce(final LoginInfo login) throws Exception {
        boolean userPressedYes = false;
        final String YES = "y";
        final Object[] loginId = new Object[] {login.getHost(), ""+login.getPort()};
        final String prompt = getLocalizedString("ShouldOverwriteLogin", loginId);
        final InputsAndOutputs io = InputsAndOutputs.getInstance();
        io.getUserOutput().print(prompt);
        final String in = io.getUserInput().getLine();
        userPressedYes = YES.equalsIgnoreCase(in);
        return ( userPressedYes );
    }
}