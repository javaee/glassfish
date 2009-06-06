/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * @author Nandini Ektare
 *
 */
package com.sun.enterprise.admin.cli.optional;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.enterprise.admin.cli.remote.CLIRemoteCommand;
import com.sun.enterprise.admin.cli.remote.CommandInvoker;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.SystemPropertyConstants;

public class AsadminLoginCommand extends BaseLifeCycleCommand {

    private String adminUser = null;
    private String adminPassword = null;
        
    @Override
    public void runCommand() 
    throws CommandException, CommandValidationException {
        
        super.validateOptions();

        // Step 1: Get admin username and password
        setOption(INTERACTIVE, "true");
        adminUser = getAdminUser();
        if (adminUser == null || adminUser.length() == 0) {
            adminUser = SystemPropertyConstants.DEFAULT_ADMIN_USER;
            adminPassword = SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD;
        } else {
            if (adminUser.equals(SystemPropertyConstants.DEFAULT_ADMIN_USER) 
                && (adminPassword == null || adminPassword.length() == 0)) {
                adminPassword = SystemPropertyConstants.DEFAULT_ADMIN_PASSWORD;
            } else {                            
                adminPassword = getAdminPassword();
            }
        }
        setOption(USER, adminUser);
        setOption(PASSWORD, adminPassword);
        
        // Step 2: Invoke version command to validate the authentication info
        if (!verifyAuth()) {
            // this would mean authentication failed.
            // so don't write into the .asadminpass file.
            throw new CommandException(getLocalizedString("InvalidCredentials",
                    new Object[]{adminUser}));            
        }

        // Step 3: Save in <userhomedir>/.asadminpass the string 
        // asadmin://<adminuser>@<adminhost>:<adminport><encrypted adminpassword>
        String portStr = getOption(PORT);
        int port = (portStr != null) ? Integer.parseInt(portStr): 4848;
        saveLogin(getOption(HOST), port, adminUser, adminPassword);
    }
    
    private boolean verifyAuth() {
        CommandInvoker invoker = new CommandInvoker(CLIRemoteCommand.RELIABLE_COMMAND); // version
        invoker.put(PORT, ""+getOption(PORT));
        invoker.put(USER, adminUser);
        invoker.put(PASSWORD, adminPassword);
        return (CLIRemoteCommand.pingDASWithAuth(invoker));
    }
        
    /**
     *  This methods returns the admin password. 
     *  The password can be passed in on the command line 
     *  @return admin password
     *  @throws CommandValidationException if adminpassword option can't be fetched 
     */
    private String getAdminPassword()
            throws CommandValidationException, CommandException {
        
        return getPassword(ADMIN_PASSWORD, "AdminPasswordPrompt", "", true, 
                           false, false, false, null, null, true, false, false, 
                           true);
    }

    /*
     * Saves the login information to the login store. Usually this is the file
     * ".asadminpass" in user's home directory.
     */
    private void saveLogin(String host, final int port, 
                           final String user, final String passwd) {
        final CLILogger logger = CLILogger.getInstance();
        LoginInfo login = null;
        try {
            // By definition, the host name will default to "localhost" and 
            // entry is overwritten
            final LoginInfoStore store = LoginInfoStoreFactory.getStore(null);
            if (host == null || host.equals(""))
                host = "localhost";
            login = new LoginInfo(host, port, user, passwd);
            if (store.exists(login.getHost(), login.getPort())) {
                // Let the user know that the user has chosen to overwrite the 
                // login information. This is non-interactive, on purpose
                final Object[] params = 
                    new Object[]{login.getHost(), "" + login.getPort()};
                final String msg = 
                    getLocalizedString("OverwriteLoginMsgCreateDomain", params);
                logger.printMessage(msg);
            }
            store.store(login, true);
            final Object[] params = 
                new String[]{user, login.getHost(), ""+port,store.getName()};
            final String msg = 
                getLocalizedString("LoginInfoStored", params);
            logger.printMessage(msg);
        } catch (final Exception e) {
            final Object[] params = new String[]{login.getHost(), ""+port};
            final String msg = 
                getLocalizedString("LoginInfoNotStored", params);
            logger.printWarning(msg);
            if (logger.isDebug()) {
                logger.printExceptionStackTrace(e);
            }
        }
    }
}
