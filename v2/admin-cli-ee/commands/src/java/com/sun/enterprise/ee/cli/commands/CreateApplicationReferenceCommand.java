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

package com.sun.enterprise.ee.cli.commands;

import com.sun.enterprise.cli.commands.S1ASCommand;
import com.sun.enterprise.cli.framework.*;

import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.client.DeploymentFacility;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.client.ServerConnectionIdentifier;
import com.sun.enterprise.deployment.client.DeploymentFacilityFactory;
import com.sun.enterprise.deployment.client.JESTarget;
import com.sun.enterprise.deployment.client.JESProgressObject;
import javax.enterprise.deploy.spi.Target;
import com.sun.enterprise.util.i18n.StringManager;

// jdk imports
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 *  This is the Delete Application Reference command
 *  @version  $Revision: 1.1.1.1 $
 */
public class CreateApplicationReferenceCommand extends S1ASCommand
{
    private static final String TARGET_OPTION = "target";
    private static final String ENABLED_OPTION = "enabled";
    private static final String VIRTUALSERVERS_OPTION = "virtualservers";
    private static final StringManager _strMgr =
        StringManager.getManager(ListApplicationReferenceCommand.class);

    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() 
        throws CommandException, CommandValidationException
    {
        validateOptions();

        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();       
        ServerConnectionIdentifier conn = createServerConnectionIdentifier(
            getHost(), getPort(), getUser(), getPassword());
        df.connect(conn);

        final String targetName = getOption(TARGET_OPTION);                
        //Target[] targets = df.createTargets(new String[]{targetName});
        final String moduleID = (String) getOperands().get(0);
        
        JESProgressObject progressObject = null;
	try
        {
            Map props = createProperties();
            if (df.isConnected()) 
            {
                Target[] targets = df.createTargets(new String[]{targetName});
                if (targets == null)
                {
                    //CLILogger.getInstance().printError(getLocalizedString("InvalidTarget"));
                    throw new CommandException(_strMgr.getString("InvalidTarget", new Object[] {targetName}));
                }

                progressObject = df.createAppRef(targets, moduleID, props);
            } else 
            {
                CLILogger.getInstance().printError(
                                   getLocalizedString("CouldNotConnectToDAS"));
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            if (e.getLocalizedMessage() != null)
                CLILogger.getInstance().printDetailMessage(
                    e.getLocalizedMessage());

            throw new CommandException(getLocalizedString(
                "CommandUnSuccessful", new Object[] {name} ), e);
        }
        
        DeploymentStatus status = df.waitFor(progressObject);
        final String statusString = status.getStageStatusMessage();

        if (status != null && 
                status.getStatus() == DeploymentStatus.WARNING) 
        {
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                "CommandSuccessfulWithMsg",
                new Object[] {name, statusString}));
        } 
        else if (status != null && 
               status.getStatus() == DeploymentStatus.FAILURE)
        {
            throw new CommandException(getLocalizedString(
                "CommandUnSuccessfulWithMsg", new Object[] {name,
                statusString} ));
        } 
        else
        {
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name} ) );
        }
    }


    /**
     *  creates the Properties which is used as a parameter to the
     *  deleteAppRef operation.
     *  @return Properties
     */
    private Map createProperties()
    {
        final String enabled = getOption(ENABLED_OPTION);                
        final String virtualServers = getOption(VIRTUALSERVERS_OPTION);                
        Properties props = new Properties();

        CLILogger.getInstance().printDebugMessage(DeploymentProperties.ENABLE);
        if (props != null) 
            props.put(DeploymentProperties.ENABLE, enabled);
        
        CLILogger.getInstance().printDebugMessage(DeploymentProperties.VIRTUAL_SERVERS);
        if (props != null && virtualServers != null) 
            props.put(DeploymentProperties.VIRTUAL_SERVERS, virtualServers);
        
        return props;
    }


    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of 
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException
    {
        super.validateOptions();
        return true;
    }


}
