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
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.client.DeploymentFacility;
import com.sun.enterprise.deployment.client.DeploymentFacilityFactory;
import com.sun.enterprise.deployment.client.JESProgressObject;
import com.sun.enterprise.deployment.client.JESTarget;
import com.sun.enterprise.deployment.client.ServerConnectionIdentifier;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.Archive;
import com.sun.enterprise.deployment.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.server.Constants;
import javax.enterprise.deploy.spi.Target;
import javax.management.ObjectName;

// jdk imports
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 *  This is the Deploy command
 *  @version  $Revision: 1.10 $
 */
public class DeployCommand extends S1ASCommand
{
    private static final String TARGET_OPTION = "target";
    private static final String CONTEXT_ROOT_OPTION = "contextroot";
    private static final String FORCE_OPTION = "force";    
    private static final String PRECOMPILE_JSP_OPTION = "precompilejsp";
    private static final String VERIFY_OPTION = "verify";
    private static final String UPLOAD_OPTION = "upload";
    private static final String ENABLED_OPTION = "enabled";
    private static final String COMPONENT_NAME = "name";
    private static final String RETRIEVE_DIR = "retrieve";
    private static final String VIRTUALSERVERS_OPTION = "virtualservers";
    private static final String DB_VENDOR_NAME_OPTION = "dbvendorname";
    private static final String CREATE_TABLES_OPTION = "createtables";
    private static final String DROP_AND_CREATE_TABLES_OPTION = "dropandcreatetables";
    private static final String UNIQUE_TABLENAMES_OPTION = "uniquetablenames";
    private static final String DEPLOYMENTPLAN_OPTION = "deploymentplan";
    private static final String AVAILABILITY_ENABLED_OPTION= "availabilityenabled";
    private static final String GENERATE_RMI_STUBS_OPTION = "generatermistubs";
    private static final String LIBRARIES_OPTION = "libraries";

    private String      filePath       = null;
    private String      componentName  = null;
    DeploymentFacility  df = null;

    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() 
        throws CommandException, CommandValidationException
    {   
        System.setProperty(DefaultConfiguration.REDIRECTION, "false");
        validateOptions();
        JESProgressObject progressObject = callDeploy();
        DeploymentStatus status = df.waitFor(progressObject);
        String statusString = status.getStageStatusMessage();

        if (statusString.indexOf("302")>-1) {
            setOption(SECURE, "true");
            progressObject = this.callDeploy();
            status = df.waitFor(progressObject);
            statusString = status.getStageStatusMessage();
        }

        if (status != null && status.getStatus() == DeploymentStatus.FAILURE) {
            checkDeployStatus(status, statusString);
        }

        final String retrievePath = getOption(RETRIEVE_DIR);
        if (retrievePath != null){
            try {
                CLILogger.getInstance().printDebugMessage("componentName = " + componentName + 
                                                  " retrievePath = " + retrievePath);
                final String fileName = df.downloadFile(new File(retrievePath), componentName, null);
                CLILogger.getInstance().printDebugMessage("downloaded stubs to  : " + fileName );
	    }
	    catch(Exception e){
	        throw new CommandException((getLocalizedString(
                    "InvalidValueInOption", new Object[] {RETRIEVE_DIR,
                    retrievePath})) + "\n"+ e.getLocalizedMessage());
	    }
	}

        if (status != null && status.getStatus() == DeploymentStatus.WARNING) {
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                "CommandSuccessfulWithMsg",
                new Object[] {name, statusString}));
        } else {
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name} ) );
        }
    }


    private JESProgressObject callDeploy()
        throws CommandException, CommandValidationException
    {
        df = DeploymentFacilityFactory.getDeploymentFacility();       
        ServerConnectionIdentifier conn = createServerConnectionIdentifier(
            getHost(), getPort(), getUser(), getPassword());
        df.connect(conn);

        //prepare data
        final String targetName = getOption(TARGET_OPTION);                
        
        final String deploymentPlan = getOption(DEPLOYMENTPLAN_OPTION);
        JESProgressObject progressObject = null;
        try {
            AbstractArchive arch = (new ArchiveFactory()).openArchive(filespecToJarURI(filePath));
            AbstractArchive plan = null;
            if (deploymentPlan != null) {
                plan = (new ArchiveFactory()).openArchive(filespecToJarURI(deploymentPlan));
            }
            //value of the map is String only
            Map deployOptions = createDeploymentProperties();

            if (df.isConnected()) 
            {
                Target[] targets = df.createTargets(new String[]{targetName});
                if (targets == null)
                {
                    //CLILogger.getInstance().printError(getLocalizedString("InvalidTarget"));
                    throw new CommandException(getLocalizedString("InvalidTarget", new Object[] {targetName}));
                }

                progressObject = df.deploy(targets, arch, plan, deployOptions);
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
        return progressObject;
    }
    

    /**
     *Convert a file spec into a jar URI.
     *@param filePath to be converted into a URI
     *@return URI with scheme=jar for the file
     */
    private URI filespecToJarURI(String fileSpec) throws URISyntaxException {
        File archiveFile = new File(fileSpec);
        String prefix = (archiveFile.isDirectory()) ? "file" : "jar";
        URI archiveFileURI = archiveFile.toURI();
        URI archiveJarURI = new URI(prefix, "" /* authority */, archiveFileURI.getSchemeSpecificPart(), null, null);
        return archiveJarURI;
    }

    /**
     *  creates the DeployProperties which is used as a parameter to the
     *  deploy operation.
     *  @return Properties
     */
    private Map createDeploymentProperties()
    {
        final String virtualServers = getOption(VIRTUALSERVERS_OPTION);
        final String contextRoot = getContextRoot();
        final String dbVendorName = getOption(DB_VENDOR_NAME_OPTION);
        final String createTable = getOption(CREATE_TABLES_OPTION);
        final String dropCreateTable = getOption(DROP_AND_CREATE_TABLES_OPTION);
        final String uniqueTableNames = getOption(UNIQUE_TABLENAMES_OPTION);
        final String target = getOption(TARGET_OPTION);  
        final String libraries = getOption(LIBRARIES_OPTION);
        final String upload = getOption(UPLOAD_OPTION);
        
        Properties props = new Properties();

        if (target != null) 
            props.put(DeploymentProperties.TARGET, target);
        
        if(filePath != null)
            props.put(DeploymentProperties.ARCHIVE_NAME, filePath);


        if(contextRoot != null)
            props.put(DeploymentProperties.CONTEXT_ROOT, contextRoot);

        if(virtualServers != null)
            props.put(DeploymentProperties.VIRTUAL_SERVERS, 
                              virtualServers);

        if(dbVendorName != null)
            props.put(Constants.CMP_DB_VENDOR_NAME, dbVendorName);

        if(createTable != null)
            props.put(Constants.CMP_CREATE_TABLES, createTable);
        
        if(dropCreateTable != null)
            props.put(Constants.CMP_DROP_AND_CREATE_TABLES, dropCreateTable);

        if(uniqueTableNames != null)
            props.put(Constants.CMP_UNIQUE_TABLE_NAMES, uniqueTableNames);

        if(libraries != null)
            props.put(DeploymentProperties.DEPLOY_OPTION_LIBRARIES_KEY, libraries);

        if(upload != null)
            props.put(DeploymentProperties.UPLOAD, upload);


            //the following properties is either a required option/operand
            //or it contains a default value in the CLIDescriptors.xml
            //so do not neet to check for "null"
        props.put(DeploymentProperties.NAME, componentName);

        props.put(DeploymentProperties.VERIFY, 
                          getOption(VERIFY_OPTION));

        props.put(DeploymentProperties.PRECOMPILE_JSP, 
                          getOption(PRECOMPILE_JSP_OPTION));

        props.put(DeploymentProperties.ENABLE, 
                          getOption(ENABLED_OPTION));
		
        props.put(DeploymentProperties.FORCE, 
                          getOption(FORCE_OPTION));
        
        props.put(DeploymentProperties.AVAILABILITY_ENABLED, 
                          getOption(AVAILABILITY_ENABLED_OPTION));
        
        props.put(DeploymentProperties.GENERATE_RMI_STUBS, 
                          getOption(GENERATE_RMI_STUBS_OPTION));
        
        final String retrievePath = getOption(RETRIEVE_DIR);
        if (retrievePath != null){
            props.put(DeploymentProperties.CLIENTJARREQUESTED,  "true");
        };

        return props;

    }


    /**
       Check the deployment status returned from the backend deployment.
       This method will iterate through the stages of the DeploymentStatus.
       If the first stage fails then it's a j2eec phase failure.
       CommandException will be thrown if j2eec phase failed.
       The next stages are the associate, start phases.  If the j2eec phase
       passed but start phase failed then the deployment was successfull but
       not loaded.  The backend message will be displayed.
       @param status - DeploymentStatus returned from the backend deployment.
       @throws CommandException if j2eec phase failed.
     */
    private void checkDeployStatus(DeploymentStatus status, 
        String statusString) throws  CommandException
    {
        if (status != null && status.getStatus() == DeploymentStatus.FAILURE) {
            throw new CommandException(getLocalizedString(
                "CommandUnSuccessfulWithMsg", new Object[] {name,
                statusString} ));
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
        super.validateOptions();
        if (getOption(CREATE_TABLES_OPTION) !=null &&
            getOption(DROP_AND_CREATE_TABLES_OPTION) != null)
            throw new CommandValidationException(getLocalizedString(
                                                     "MutuallyExclusiveOption",
                                                     new Object[] {CREATE_TABLES_OPTION,
                                                     DROP_AND_CREATE_TABLES_OPTION
                                                     }));

        filePath = (String) getOperands().get(0);
        componentName = getComponentName();
        return true;
    }


    /**
     *  get the context root from command option.  If context root is null
     *  then it will get from the filepath in the operand.
     *  @param returns context root
     */
    private String getContextRoot()
    {
        String contextRoot = getOption(CONTEXT_ROOT_OPTION);
        return contextRoot;
    }

    
    /**
     *  get the component name.  if component name is null then get it from 
     *  the file path in the operand
     *  @throws CommandValidationException if could not get component name
     */
    private String getComponentName() throws CommandValidationException
    {
        String name = getOption(COMPONENT_NAME);
        if (name == null)
        {
            name = getNameFromFilePath();
        }
            //if name is still null or empty then throw an exception
        if ((name == null) || (name.equals("")))
            throw new CommandValidationException(getLocalizedString("ComponentNameNull"));
        return name;
    }


    /**
     *  get the file name from the filepath
     *  @return file name
     */
    private String getNameFromFilePath()
    {
        final File file = new File(filePath);
        final String fileName = file.getName();
        
        if (file.isFile()) {
            int toIndex = fileName.lastIndexOf('.');
            if (toIndex > 1)
            {
                return fileName.substring(0, toIndex);
            }
        }
        return fileName;
    }
}
 
