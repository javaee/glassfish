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

import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.client.DeploymentFacility;
import com.sun.enterprise.deployment.client.ServerConnectionIdentifier;
import com.sun.enterprise.deployment.client.DeploymentFacilityFactory;
import com.sun.enterprise.deployment.client.JESTarget;
import com.sun.enterprise.deployment.client.JESProgressObject;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import javax.enterprise.deploy.spi.Target;
import com.sun.enterprise.server.Constants;

// jdk imports
import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 *  Undeploy command
 *  @version  $Revision: 1.4 $
 */
public class UndeployCommand extends S1ASCommand
{
    private static final String CASCADE_OPTION = "cascade";
    private static final String DROPTABLES_OPTION = "droptables";
    private static final String TARGET_OPTION = "target";

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

        //prepare data
        //Target[] targets = new JESTarget[1];
        final String targetName = getOption(TARGET_OPTION);
        //targets[0] = new JESTarget(targetName, null);
        Map deployOptions = createDeploymentProperties();

        JESProgressObject progressObject = null;
        
        try
        {
            if (df.isConnected())
            {
                CLILogger.getInstance().printDebugMessage("Calling the undeploy with DeployOptions");
                Target[] targets = df.createTargets(new String[]{targetName});
                if (targets == null)
                {
                    //CLILogger.getInstance().printError(getLocalizedString("InvalidTarget"));
                    throw new CommandException(getLocalizedString("InvalidTarget", new Object[] {targetName}));
                }

                progressObject = df.undeploy(targets, getComponentName(), deployOptions);
            } else
            {
                CLILogger.getInstance().printError(
                                   getLocalizedString("CouldNotConnectToDAS"));
            }
        }
        catch (Exception e)
        {
            if (e.getLocalizedMessage() != null)
                CLILogger.getInstance().printDetailMessage(
                    e.getLocalizedMessage());

            throw new CommandException(getLocalizedString(
                "CommandUnSuccessful", new Object[] {name} ), e);
        }

        DeploymentStatus status = df.waitFor(progressObject);
        final String statusString = status.getStageStatusMessage();


        if (status != null &&
            status.getStatus() == DeploymentStatus.FAILURE) {
            throw new CommandException(getLocalizedString(
                "CommandUnSuccessfulWithMsg", new Object[] {name,
                statusString} ));
        } else if (status != null &&
            status.getStatus() == DeploymentStatus.WARNING) {
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                "CommandSuccessfulWithMsg",
                new Object[] {name, statusString}));
        } else {
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                "CommandSuccessful", new Object[] {name} ));
        }

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
        return super.validateOptions();
    }


    /**
     * this method returns the componet_name operand
     * @return component_name
     */
    private String getComponentName()
    {
        return (String) getOperands().get(0);
    }


    /**
     *  creates the DeployProperties which is used as a parameter to the
     *  deploy operation.
     */
    private Map createDeploymentProperties()
    {
        Properties props = new Properties();
        final String cascadeOption   = getOption(CASCADE_OPTION);
        final String dropTablesOption = getOption(DROPTABLES_OPTION);
        final String target = getOption(TARGET_OPTION);                
                
        if (props != null) 
            props.put(DeploymentProperties.TARGET, target);        

     	props.put(DeploymentProperties.NAME, getComponentName() );

        if (cascadeOption != null)
            props.put(DeploymentProperties.CASCADE, cascadeOption);
        if (dropTablesOption != null)
            props.put(Constants.CMP_DROP_TABLES, dropTablesOption);
        return props;
    }


}
