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
 * $Id: BaseLifeCycleCommand.java,v 1.11 2007/05/01 05:36:54 ne110415 Exp $
 */
package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.cli.framework.*;


import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.logging.Logger;
import java.util.HashMap;
import java.io.IOException;
import com.sun.logging.LogDomains;

import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;

import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;

/**
 *Abstract base class for the Lifecycle commands
 *
 */
abstract public class BaseLifeCycleCommand extends S1ASCommand {

    protected static final String DOMAIN = "domain";
    protected static final String DOMAINDIR = "domaindir";
    protected static final String MASTER_PASSWORD = "masterpassword";
    protected static final String NEW_MASTER_PASSWORD = "newmasterpassword";
    protected static final String ADMIN_USER = "adminuser";
    protected static final String ADMIN_PASSWORD = "adminpassword";
    protected static final String PASSWORD = "password";
    protected static final String DEFAULT_MASTER_PASSWORD = RepositoryManager.DEFAULT_MASTER_PASSWORD;
    protected static final String ADMIN_PORT = "adminport";
    protected static final String SAVE_MASTER_PASSWORD = "savemasterpassword";
    protected final static char ESCAPE_CHAR = '\\';
    protected final static char EQUAL_SIGN = '=';
    protected final static String DELIMITER = ":";
    protected final static String KILL = "kill";

    /** Creates new BaseLifeCycleCommand */
    public BaseLifeCycleCommand() {
    }

    /**
     *  gets Logger from LogDomains
     *  @return Logger
     */
    protected Logger getLogger() {
        return LogDomains.getLogger(BaseLifeCycleCommand.class, LogDomains.CORE_LOGGER);
    }

    protected DomainConfig getDomainConfig(String domainName) throws CommandException {
        try {
            DomainConfig dc = new DomainConfig(domainName, getDomainsRoot());

            // add map entries for --verbose and --debug options to start-domain
            if (getBooleanOption("verbose")) {
                dc.put(DomainConfig.K_VERBOSE, Boolean.TRUE);
            }
            if (getBooleanOption("debug")) {
                dc.put(DomainConfig.K_DEBUG, Boolean.TRUE);
            }

            return dc;

        } catch (Exception e) {
            throw new CommandException(e);
        }
    }

    protected Boolean getSaveMasterPassword(String masterPassword) {
        Boolean saveMasterPassword = Boolean.valueOf(getBooleanOption(SAVE_MASTER_PASSWORD));
        if (masterPassword != null && masterPassword.equals(DEFAULT_MASTER_PASSWORD)) {
            saveMasterPassword = Boolean.TRUE;
        }
        return saveMasterPassword;
    }

    protected String getDomainsRoot() throws CommandException {
        String domainDir = getOption(DOMAINDIR);
        if (domainDir == null) {
            domainDir = getSystemProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
        }
        if (domainDir == null) {
            throw new CommandException(getLocalizedString("InvalidDomainPath",
                    new String[]{domainDir}));
        }
        return domainDir;
    }

    protected String getDomainName() throws CommandException {

        String domainName = null;
        if (operands.isEmpty()) {
            // need to also support domain option for backward compatibility
            if (getOption(DOMAIN) != null) {
                domainName = getOption(DOMAIN);
            } else {
                final String[] domains = getDomains();
                if (domains.length == 0) {
                    throw new CommandException(getLocalizedString("NoDomains",
                            new Object[]{
                        getDomainsRoot()
                    }));
                } else if (domains.length > 1) {
                    throw new CommandException(getLocalizedString("NoDefaultDomain",
                            new Object[]{
                        getDomainsRoot()
                    }));
                } else {
                    domainName = domains[0];
                }  //assign the only domain
            }
        } else {
            domainName = (String) operands.firstElement();
        }
        CLILogger.getInstance().printDebugMessage("domainName = " + domainName);
        return domainName;
    }

    /**
     *  this methods returns the admin user.
     *  first it checks if adminuser option is specified on command line.
     *  if not, then it'll try to get AS_ADMIN_ADMINUSER from .asadminprefs file.
     *  if that still does not exist then get AS_ADMIN_USER from ./asadminprefs file.
     *  if all else fails, an CommandValidationException is thrown.
     *  @return admin user.
     *  @throws CommandValidationException if could not get adminuser option 
     */
    protected String getAdminUser() throws CommandValidationException {
        String adminUserVal = getOption(USER);
        if (adminUserVal != null) {
            return adminUserVal;
        } else if (getBooleanOption(INTERACTIVE)) {
            //prompt for user
            try {
                InputsAndOutputs.getInstance().getUserOutput().print(
                        getLocalizedString("AdminUserPrompt"));
                return InputsAndOutputs.getInstance().getUserInput().getLine();
            } catch (IOException ioe) {
                throw new CommandValidationException(
                        getLocalizedString("CannotReadOption",
                        new Object[]{"ADMIN_USER"}));
            }
        } else {
            throw new CommandValidationException(getLocalizedString(
                    "OptionIsRequired",
                    new Object[]{USER}));
        }

    }

    /**
     *  this methods returns the master password and is used to get the master password
     *  for both create-domain and create-nodeagent commands. The password can be passed in
     *  on the command line (--masterpassword), environment variable (AS_ADMIN_MASTERPASSWORD), 
     *  or in the password file (--passwordfile).
     *  first it checks if masterpassword option is specified on command line.
     *  if not, then it'll try to get AS_ADMIN_MASTERPASSWORD from the password file.   
     *  if all else fails, then prompt the user for the password if interactive=true.
     *  @param confirmAndValidate - boolean whether to confirm and validate masterpassword
     *  @return admin password
     *  @throws CommandValidationException if could not get adminpassword option 
     */
    protected String getMasterPassword(boolean confirmAndValidate)
            throws CommandValidationException, CommandException {
        return getMasterPassword(confirmAndValidate, false);
    }

    /**
     *  this methods returns the master password and is used to get the master password
     *  for both create-domain and create-nodeagent commands. The password can be passed in
     *  on the command line (--masterpassword), environment variable (AS_ADMIN_MASTERPASSWORD), 
     *  or in the password file (--passwordfile).
     *  first it checks if masterpassword option is specified on command line.
     *  if not, then it'll try to get AS_ADMIN_MASTERPASSWORD from the password file.   
     *  if all else fails, then prompt the user for the password if interactive=true.
     *  @param confirmAndValidate - boolean whether to confirm and validate masterpassword
     *  @param alwaysPrompt - boolean whether to always prompt for masterpassword reguardless of adminuser/password
     *  @return admin password
     *  @throws CommandValidationException if could not get adminpassword option 
     */
    protected String getMasterPassword(boolean confirmAndValidate, boolean alwaysPrompt)
            throws CommandValidationException, CommandException {
        //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, mgr, config,
        //promptUser, confirm, validate)
        String adminPassword = getPassword(ADMIN_PASSWORD, true, false, false, false, null, null,
                false, false, false, false);

        if (adminPassword != null && !alwaysPrompt) {
            //If we got here, then the admin password was found in the command line, password file,
            //or environment variable
            //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, mgr, config,
            //promptUser, confirm, validate)
            String masterPassword = getPassword(MASTER_PASSWORD, false, false, false, false, null, null,
                    false, false, confirmAndValidate, false);
            if (masterPassword == null) {
                //If we got here, then the master password was not found in the command line or
                //password file, so we return the default master password rather than prompting.
                return DEFAULT_MASTER_PASSWORD;
            } else {
                return masterPassword;
            }
        }
        //If the admin user was not provided on the command line (i.e. the user was prompted), then
        //we can prompt for the master password.
        //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, mgr, config,
        //promptUser, confirm, validate)
        return getPassword(MASTER_PASSWORD, "MasterPasswordPrompt", "MasterPasswordConfirmationPrompt",
                false, false, false, false, null, null,
                true, confirmAndValidate, confirmAndValidate, false);
    }

    protected String getMasterPassword(RepositoryManager mgr, RepositoryConfig config)
            throws CommandValidationException, CommandException {
        //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, config,
        //promptUser, confirm, validate)
        return getPassword(MASTER_PASSWORD, "MasterPasswordPrompt", null,
                false, false, false, true, mgr, config,
                true, false, false, false);
    }

    protected String getNewMasterPassword()
            throws CommandValidationException, CommandException {
        return getPassword(NEW_MASTER_PASSWORD,
                "NewMasterPasswordPrompt", "NewMasterPasswordConfirmationPrompt",
                false, false, false, false, null, null,
                true, true, true, false);
    }

    protected HashMap<String, String> getExtraPasswords(String[] optionNames)
            throws CommandValidationException, CommandException {
        HashMap<String, String> result = new HashMap<String, String>();
        String password;
        String optionName;
        for (int i = 0; i < optionNames.length; i++) {
            optionName = optionNames[i];
            //Add the new option as a non-deprecated option, so a message will not be displayed.            
            NOT_DEPRECATED_PASSWORDFILE_OPTIONS += "|" + optionName;
            //getPassword(optionName, allowedOnCommandLine, readPrefsFile, readPasswordOptionFromPrefs, readMasterPasswordFile, config,
            //promptUser, confirm, validate)
            password = getPassword(optionName, "ExtraPasswordPrompt", null, false, false, false, false, null, null,
                    true, false, false, false);
            result.put(optionName, password);
        }
        return result;
    }

    protected String[] getDomains() throws CommandException {
        try {
            DomainsManager mgr = new PEDomainsManager();
            return mgr.listDomains(getDomainConfig(null));
        } catch (Exception e) {
            throw new CommandException(e.getLocalizedMessage());
        }
    }

    protected boolean isWindows() {
        final String osname = System.getProperty("os.name").toLowerCase();
        CLILogger.getInstance().printDebugMessage("osname = " + osname);
        return osname.indexOf("windows") != -1;
    }

    protected boolean isSpaceInPath(String path) {
        return path.indexOf(' ') != -1;
    }
}

