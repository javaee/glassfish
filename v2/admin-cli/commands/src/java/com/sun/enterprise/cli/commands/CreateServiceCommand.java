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
import com.sun.enterprise.admin.servermgmt.Service;
import com.sun.enterprise.admin.servermgmt.ServiceFactory;
import com.sun.enterprise.admin.servermgmt.AppserverServiceType;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import java.io.File;

import java.util.Date;

public class CreateServiceCommand extends S1ASCommand 
{
    private final static String TYPE = "type";
    private final static String NAME = "name";
    private final static String SERVICE_PROPERTIES = "serviceproperties";
    private final static String VALID_TYPES = "das|node-agent";
    private final static String DAS_TYPE = "das";

    /**
     *  A method that validates the options/operands 
     *  @return boolean returns true if success else returns false
     *  @throws CommandValidationException
     */
    public boolean validateOptions() throws CommandValidationException
    {
        super.validateOptions();
        String passwordFile = getOption(PASSWORDFILE);
        //check final the passwordfile is valid
        if (! new File(passwordFile).exists()) 
        {
            final String msg = getLocalizedString("FileDoesNotExist", 
                                                   new Object[] {passwordFile});
            throw new CommandValidationException(msg);
        }
        String typedirOperand = (String) getOperands().get(0);
        File typeDir = new File(typedirOperand);        
        String type = getOption(TYPE);
        if (!type.matches(VALID_TYPES))
        {
            throw new CommandValidationException(
                            getLocalizedString("InvalideServiceType"));
        }
        if (!typeDir.exists() || !typeDir.canWrite() || !typeDir.isDirectory()) 
        {
            final String msg = getLocalizedString("InvalidDirectory", 
                                                    new Object[] {typeDir});
            throw new CommandValidationException(msg);
        }
    	return true;
    }


    protected void validateType() throws CommandException
    {
        String typedirOperand = (String) getOperands().get(0);
        File typeDir = new File(typedirOperand);        
        String type = getOption(TYPE);
        //Validate the domain directory
        if (type.equals(DAS_TYPE))
        {
            String domainName = typeDir.getName();
            String domainRoot = typeDir.getParent();
            try {
                DomainConfig dc = new DomainConfig(domainName, domainRoot);
                RepositoryManager rm = new RepositoryManager();
                rm.checkRepository(dc, true);
            }catch (RepositoryException re){
                throw new CommandException(re.getLocalizedMessage());
            }
        }
        else // validate the node-agent directory
        {
            validateNodeAgent(typeDir);
        }
    }

    
    protected void validateNodeAgent(File typeDir) throws CommandException
    {
        throw new CommandException(getLocalizedString( "TypeNotSupported"));
    }
    
    
    /**
     *  Method that Executes the command
     *  @throws CommandException, CommandValidationException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        try {
            validateOptions();

            validateType();

            String passwordFile = getOption(PASSWORDFILE);
            String type = getOption(TYPE);
            String typeDir = (String) getOperands().get(0);
            final Service service = ServiceFactory.getService();
                //configure service
            service.setDate(new Date().toString());
            final StringBuilder ap = new StringBuilder();
            service.setName(getName(typeDir, ap));
            service.setLocation(ap.toString());
            service.setType(type.equals("das") ? 
                            AppserverServiceType.Domain
                            : AppserverServiceType.NodeAgent);
            service.setFQSN();
            service.setOSUser();
            service.setAsadminPath(SystemPropertyConstants.getAsAdminScriptLocation());
            service.setPasswordFilePath(passwordFile);
            service.setServiceProperties(getOption(SERVICE_PROPERTIES));
            service.isConfigValid(); 
            service.setTrace(CLILogger.isDebug());
            service.createService(service.tokensAndValues());
            printSuccess(service);
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                           "CommandSuccessful",
                                                           new Object[] {name}));
        }
        catch (Exception e) {
            displayExceptionMessage(e);
        }
    }

    
    /**
     *  Retrieves the domain/nodeagent name from the given directory 
     *  @return domain/nodeagent name
     *  @throws CommandValidationException
     */
    private String getName(String typeDir, final StringBuilder absolutePath) throws CommandException
    {
        String path = "";
        try
        {
            //Already checked for the valid directory in validateOptions()
           final File f = new File(typeDir);
           String name = f.getName();
           absolutePath.append(f.getAbsolutePath());
           final String nameFromOption = getOption(NAME);
           if (nameFromOption != null)
               name = nameFromOption;
           CLILogger.getInstance().printDebugMessage("service name = " + name);
           return ( name );
        }
        catch (Exception e)
        {
            throw new CommandException(e.getLocalizedMessage());
        }
    }
    private void printSuccess(final Service service) {
        final String[] params = new String[] {service.getName(), service.getType().toString(), service.getLocation(), service.getManifestFilePath()};
        final String msg = getLocalizedString("ServiceCreated", params);
        CLILogger.getInstance().printMessage(msg);
    }
}
