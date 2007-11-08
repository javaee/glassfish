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
 *  Will list the names for the binding components, service engines, shared libraries,
 *  or service assemblies.
 *  @version  $Revision: 1.3 $
 */
public class JBIListCommands extends JBICommand
{
    private static final String LIST_BINDING_COMPONENTS    = "list-jbi-binding-components";
    private static final String LIST_SERVICE_ENGINES       = "list-jbi-service-engines";
    private static final String LIST_SHARED_LIBRARIES      = "list-jbi-shared-libraries";
    private static final String LIST_SERVICE_ASSEMBLIES    = "list-jbi-service-assemblies";

    /**
     *  A method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        String result = "";
        try {

            // Perform the pre run initialization
            if (preRunInit())
            {
                // Retrieve the options used for this command
                String  targetName     = getOption(TARGET_OPTION);
                String  libraryName    = getOption(LIBRARY_NAME_OPTION);                
                String  assemblyName   = getOption(ASSEMBLY_NAME_OPTION);  
                String  lifecycleState = getOption(LIFECYCLE_STATE_OPTION,validStates);                        
                String  componentName  = getOption(COMPONENT_NAME_OPTION);  

                // Using the command name, we'll determine how to process the command
                if (name.equals(LIST_BINDING_COMPONENTS)) {
                    result = ((JBIAdminCommands) mJbiAdminCommands).listBindingComponents(
                            lifecycleState,
                            libraryName,
                            assemblyName,
                            targetName);
                    processJBIAdminComponentListResult(result);
                }

                else if (name.equals(LIST_SERVICE_ENGINES)) {
                    result = ((JBIAdminCommands) mJbiAdminCommands).listServiceEngines(
                            lifecycleState,
                            libraryName,
                            assemblyName,
                            targetName);
                    processJBIAdminComponentListResult(result);
                }

                else if (name.equals(LIST_SHARED_LIBRARIES)) {
                    result = ((JBIAdminCommands) mJbiAdminCommands).listSharedLibraries(
                            componentName,
                            targetName);
                    processJBIAdminComponentListResult(result);
                }

                else if (name.equals(LIST_SERVICE_ASSEMBLIES)) {
                    result = ((JBIAdminCommands) mJbiAdminCommands).listServiceAssemblies(
                            lifecycleState,
                            componentName,
                            targetName);
                    processJBIAdminAsseblyListResult(result);
                }

                // Display the default success message
                CLILogger.getInstance().printDetailMessage (
                        getLocalizedString ("CommandSuccessful",new Object[] {name} ) );
            }
        }

        catch (Exception e) {
            processTaskException(e);
        }
    }
}
