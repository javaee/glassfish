/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.remote.ServerRemoteAdminCommand;
import java.io.Console;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import org.glassfish.api.Param;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;

/**
 * The change-admin-password command.
 * The remote command implementation presents a different
 * interface (set of options) than the local command.
 * This special local implementation adapts the local
 * interface to the requirements of the remote command.
 *
 * @author Bill Shannon
 */
@Service(name = "change-admin-password")
@Scoped(PerLookup.class)
@ExecuteOn({RuntimeType.DAS})
public class ChangeAdminPasswordCommand extends CLICommand {
    private ParameterMap params;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ChangeAdminPasswordCommand.class);

    /**
     * Require the user to actually type the passwords.
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        /*
         * If --user wasn't specified as a program option,
         * we treat it as a required option and prompt for it
         * if possible.
         */
        if (programOpts.getUser() == null) {
            // prompt for it (if interactive)
            Console cons = System.console();
            if (cons != null && programOpts.isInteractive()) {
                cons.printf("%s", strings.get("AdminUserDefaultPrompt",
                    SystemPropertyConstants.DEFAULT_ADMIN_USER));
                String val = cons.readLine();
                if (ok(val))
                    programOpts.setUser(val);
                else
                    programOpts.setUser(
                                    SystemPropertyConstants.DEFAULT_ADMIN_USER);
            } else {
                //logger.info(strings.get("AdminUserRequired"));
                throw new CommandValidationException(
                    strings.get("AdminUserRequired"));
            }
        }

        // now, prompt for the passwords
        try {
            String password = getPasswords();
            programOpts.setPassword(password,
                ProgramOptions.PasswordLocation.USER);
        } catch (CommandValidationException cve) {
            throw new CommandException(cve);
        }

        /*
         * Now that the user-supplied parameters have been validated,
         * we set the parameter values for the remote command.
         */
        params = new ParameterMap();
        params.set("DEFAULT", programOpts.getUser());
        params.set(Environment.AS_ADMIN_ENV_PREFIX + "PASSWORD",
                passwords.get(Environment.AS_ADMIN_ENV_PREFIX + "PASSWORD"));
        params.set(Environment.AS_ADMIN_ENV_PREFIX + "NEWPASSWORD",
                passwords.get(Environment.AS_ADMIN_ENV_PREFIX + "NEWPASSWORD"));
    }

    /**
     * Execute the remote command using the parameters we've collected.
     */
    @Override
    protected int executeCommand() throws CommandException {
        RemoteAdminCommand rac = new RemoteAdminCommand(name,
            programOpts.getHost(), programOpts.getPort(),
            programOpts.isSecure(), programOpts.getUser(),
            programOpts.getPassword(), logger);
        rac.executeCommand(params);
        return SUCCESS;
    }

    /**
     * Prompt for all the passwords needed by this command.
     * Return the old password.
     */
    private String getPasswords() throws CommandValidationException {
        String oldpassword = readPassword(strings.get("AdminPasswordPrompt"));
        String newpassword =
                readPassword(strings.get("AdminNewPasswordPrompt"));
        String newpasswordAgain =
                readPassword(strings.get("AdminNewPasswordConfirmationPrompt"));
        if (!newpassword.equals(newpasswordAgain)) {
            throw new CommandValidationException(
                strings.get("OptionsDoNotMatch", "Admin Password"));
        }

        passwords.put(Environment.AS_ADMIN_ENV_PREFIX + "PASSWORD",
                                oldpassword);
        passwords.put(Environment.AS_ADMIN_ENV_PREFIX + "NEWPASSWORD",
                                newpassword);
        return oldpassword;
    }
}
