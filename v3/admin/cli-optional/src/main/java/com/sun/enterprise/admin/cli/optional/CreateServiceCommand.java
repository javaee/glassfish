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

package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.admin.servermgmt.services.ServiceFactory;
import com.sun.enterprise.admin.servermgmt.services.Service;
import com.sun.enterprise.admin.servermgmt.services.AppserverServiceType;
import java.io.*;
import java.util.Date;
import com.sun.enterprise.util.SystemPropertyConstants;

public class CreateServiceCommand extends AbstractCommand {

    @Override
    public void runCommand() throws CommandException, CommandValidationException
    {
        try {
            validateOptions();
            String passwordFile = getOption(PASSWORDFILE);
            boolean dry_run = getBooleanOption("dry-run");
            String type = "das"; //getOption(TYPE);  TODO
            String typeDir = (String) getOperands().get(0);
            final Service service = ServiceFactory.getService();
                //configure service
            service.setDate(new Date().toString());
            final StringBuilder ap = new StringBuilder();
            service.setName(getName(typeDir, ap));
            service.setDryRun(dry_run);
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
        catch (CommandValidationException e) {
            throw e;
        }
        catch (Exception e) {
            throw new CommandValidationException(e);
        }
    }

    @Override
    public boolean validateOptions() throws CommandValidationException
    {
        super.validateOptions();

        // 1.  They *must* have a password file
        String passwordFile = getOption(PASSWORDFILE);

        if (! new File(passwordFile).isFile())
        {
            final String msg = getLocalizedString("FileDoesNotExist",
                    new Object[] {passwordFile});
            throw new CommandValidationException(msg);
        }
        
    	return true;
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
           String aName = f.getName();
           absolutePath.append(f.getAbsolutePath());
           final String nameFromOption = getOption(NAME);
           if (nameFromOption != null)
               aName = nameFromOption;
           CLILogger.getInstance().printDebugMessage("service name = " + aName);
           return ( aName );
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

    private final static String TYPE = "type";
    private final static String NAME = "name";
    private final static String SERVICE_PROPERTIES = "serviceproperties";
    private final static String VALID_TYPES = "das|node-agent";
    private final static String DAS_TYPE = "das";


}