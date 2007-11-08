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

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.jbi.ui.common.JBIRemoteException;
import com.sun.jbi.ui.common.JBIAdminCommands;

/**
 *  Will start the JBI component on the specified target.
 *  @version  $Revision: 1.3 $
 */
public class JBIUninstallCommands extends JBICommand
{
    private static final String UNINSTALL_COMPONENT        = "uninstall-jbi-component";
    private static final String UNINSTALL_SHARED_LIBRARY   = "uninstall-jbi-shared-library";
    private static final String UNDEPLOY_SERVICE_ASSEMBLY  = "undeploy-jbi-service-assembly";

    /**
     *  A method that Executes the command
     *  @throws CommandException
     */

    public void runCommand() throws CommandException, CommandValidationException
    {
        String result = "";
        String successKey = "";
        try {

            // Perform the pre run initialization
            if (preRunInit())
            {
                // Retrieve the options
                String  targetName  = getOption(TARGET_OPTION);
                boolean force       = getBooleanOption(FORCE_OPTION);
                boolean keepArchive = getBooleanOption(KEEP_ARCHIVE_OPTION);

                // Retrieve the operand
                String  componentName = (String) getOperands().get(0);

                // Using the command name, we'll determine how to process the command
                if (name.equals(UNINSTALL_COMPONENT)) {
                    result = ((JBIAdminCommands) mJbiAdminCommands).uninstallComponent(
                            componentName,
                            force,
                            keepArchive,
                            targetName);
                    successKey = "JBISuccessUninstallComponent";
                }

                else if (name.equals(UNINSTALL_SHARED_LIBRARY)) {
                    result = ((JBIAdminCommands) mJbiAdminCommands).uninstallSharedLibrary(
                            componentName,
                            force,
                            keepArchive,
                            targetName);
                    successKey = "JBISuccessUninstallSharedLibrary";
                }

                else if (name.equals(UNDEPLOY_SERVICE_ASSEMBLY)) {
                    result = ((JBIAdminCommands) mJbiAdminCommands).undeployServiceAssembly(
                            componentName,
                            force,
                            keepArchive,
                            targetName);
                    successKey = "JBISuccessUndeployServiceAssembly";
                }

                // Display the success message
                CLILogger.getInstance().printDetailMessage (
                        getLocalizedString (successKey,new Object[] {componentName}));
            }
        }

        catch (Exception e) {
            processTaskException(e);
        }
    }
}

