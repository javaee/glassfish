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
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *  Will install a component, install a shared library or deploy a service assembly.
 *  or service assemblies.
 *  @version  $Revision: 1.7 $
 */
public class JBIInstallCommands extends JBICommand
{
    private static final String INSTALL_COMPONENT        = "install-jbi-component";
    private static final String INSTALL_SHARED_LIBRARY   = "install-jbi-shared-library";
    private static final String DEPLOY_SERVICE_ASSEMBLY  = "deploy-jbi-service-assembly";

    /**
     *  A method that Executes the command
     *  @throws CommandException
     *  @throws CommandValidationException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        String  result = "";
        String  successKey = "";
        String  errorKey = "";
        boolean installFromDomain = false;
        try {

            // Retrieve the upload boolean option value
            boolean isUpload = getBooleanOption(UPLOAD_OPTION);

            // Perform the pre run initialization
            if (preRunInit(isUpload))
            {
                // Retrieve the options
                String targetName = getOption(TARGET_OPTION);

                // Retrieve the operand
                String  operand = (String) getOperands().get(0);

                // Make sure the file specified is valid. If it is not a valid file, then
                // we should try to install/deploy the component/assembly from the domain.
                // If that should fail, then we will throw the original commandException,
                // saying that the filePath specified was not valid.
                try {
                    if (name.equals(DEPLOY_SERVICE_ASSEMBLY))
                    {
                        errorKey = "JBIDeloymentFileNotFound";
                    }
                    else
                    {
                        errorKey = "JBIInstallationFileNotFound";
                    }
                    operand = validateFilePath (errorKey,operand);
                }
                catch (CommandException ce) {
                    installFromDomain = true;

                    if (name.equals(INSTALL_COMPONENT)) {
                        result = ((JBIAdminCommands) mJbiAdminCommands).installComponentFromDomain(
                                operand,
                                targetName);
                        successKey = "JBISuccessInstallDomainComponent";
                    }

                    else if (name.equals(INSTALL_SHARED_LIBRARY)) {
                        result = ((JBIAdminCommands) mJbiAdminCommands).installSharedLibraryFromDomain(
                                operand,
                                targetName);
                        successKey = "JBISuccessInstallDomainSharedLibrary";
                    }

                    else if (name.equals(DEPLOY_SERVICE_ASSEMBLY)) {
                        result = ((JBIAdminCommands) mJbiAdminCommands).deployServiceAssemblyFromDomain(
                                operand,
                                targetName);
                        successKey = "JBISuccessDeployServiceAssembly";
                    }
                    processJBIAdminResult (result, successKey);
                }

                if (!(installFromDomain))
                {
                    // Using the command name, we'll determine how to process the command
                    if (name.equals(INSTALL_COMPONENT)) {

                        // When installing a component, two api's exist, one when installing
                        // a component with no associated properties, and one when installing
                        // a component with associated properties.
                        Properties properties = checkForProperties();
                        result = ((JBIAdminCommands) mJbiAdminCommands).installComponent(
                                operand,
                                properties,
                                targetName);
                        successKey = "JBISuccessInstallComponent";
                    }

                    else if (name.equals(INSTALL_SHARED_LIBRARY)) {
                        result = ((JBIAdminCommands) mJbiAdminCommands).installSharedLibrary(
                                operand,
                                targetName);
                        successKey = "JBISuccessInstallSharedLibrary";
                    }

                    else if (name.equals(DEPLOY_SERVICE_ASSEMBLY)) {
                        result = ((JBIAdminCommands) mJbiAdminCommands).deployServiceAssembly(
                                operand,
                                targetName);
                        successKey = "JBISuccessDeployServiceAssembly";
                    }
                    processJBIAdminResult (result, successKey);
                }
            }
        }

        catch (Exception e) {
            processTaskException(e);
        }
    }


    // Will first check the properties options for either a property file or 
    // properties specified on the command line.  If no properties option is
    // found, an empty properties object will be returned.
    private Properties checkForProperties() throws CommandException, 
                                                   CommandValidationException
 
    {
        String compProperties = getOption(COMPONENT_PROPERTIES);
        Properties properties = new Properties();
        if (compProperties != "")
        {
            try 
            {
                properties.load(new FileInputStream(compProperties));
            } 
            catch (IOException e) 
            {
                properties = createPropertiesParam(compProperties);
            }
        }
        return properties;
    }


}
