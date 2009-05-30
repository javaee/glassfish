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
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import java.io.*;
import java.util.Date;
import com.sun.enterprise.util.SystemPropertyConstants;

public class CreateServiceCommand extends AbstractCommand {
    @Override
    public void runCommand() throws CommandException, CommandValidationException
    {
        // note: all of the calls to File.getPath() are guaranteed to return good
        // solid absolute paths because SmartFile is used for processing all
        // File objects in validateOptions()
        try {
            validateOptions();
            boolean dry_run = getBooleanOption("dry-run");
            String type = "das"; //getOption(TYPE);  TODO
            final Service service = ServiceFactory.getService();
                //configure service
            service.setDate(new Date().toString());
            service.setName(serviceName);
            service.setDryRun(dry_run);
            service.setLocation(serverDir.getPath());
            service.setType(type.equals("das") ?
                            AppserverServiceType.Domain
                            : AppserverServiceType.NodeAgent);
            service.setFQSN();
            service.setOSUser();
            service.setAsadminPath(asadminScript.getPath());
            
            if(passwordFile != null)
                service.setPasswordFilePath(passwordFile.getPath());

            service.setServiceProperties(getOption(SERVICE_PROPERTIES));
            service.isConfigValid();
            service.setTrace(CLILogger.isDebug());
            service.createService(service.tokensAndValues());
            CLILogger.getInstance().printMessage(service.getSuccessMessage());
            logger.printDetailMessage(getLocalizedString("CommandSuccessful", new Object[] {name}));
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

        // The order that you make these calls matters!!
        validateServerDir();
        validateName();
        validateAsadmin();
        validatePasswordFile();
        
        return true;
    }

    // TODO TODO TODO
    // Allow the default domain!!!!
    // TODO TODO TODO
    private void validateServerDir() throws CommandValidationException{
        String op = (String)getOperands().get(0);

        if(!ok(op)) {
            throw new CommandValidationException(strings.get("create.service.NoServerDirOperand"));
        }

        File f = SmartFile.sanitize(new File(op));

        if(!f.isDirectory()) {
            throw new CommandValidationException(strings.get("create.service.BadServerDir", f));
        }

        File serverDirParent = new File(f, "..");

        if(!serverDirParent.isDirectory()) {
            throw new CommandValidationException(strings.get("create.service.BadServerDirParent", serverDirParent));
        }
        String serverName = f.getName();

        if(!ok(serverName)) {
            // impossible
            throw new CommandValidationException(strings.get("create.service.BadServerDir", f));
        }

        serverDir = f;
    }

    private void validateName() {
       serviceName = getOption(NAME);

       if(!ok(serviceName))
           serviceName = serverDir.getName();

       logger.printDebugMessage("service name = " + serviceName);
    }

    private void validatePasswordFile() throws CommandValidationException {
        String passwordFileName = getOption(PASSWORDFILE);

        if (!ok(passwordFileName)) {
            return; // no password file specified -- that is allowed...
        }

        // they specified a password file...
        // TODO look inside the file and make sure it is kosher...
        passwordFile = SmartFile.sanitize(new File(passwordFileName));
        
        if(!passwordFile.isFile()) {
            final String msg = strings.get("create.service.NoSuchFile", passwordFileName);
            throw new CommandValidationException(msg);
        }
    }

    private void validateAsadmin() throws CommandValidationException{
        String s = SystemPropertyConstants.getAsAdminScriptLocation();

        if(!ok(s))
            throw new CommandValidationException(strings.get("internal.error", "Can't get Asadmin script location"));

        asadminScript = SmartFile.sanitize(new File(s));

        if(!asadminScript.isFile()) {
            throw new CommandValidationException(strings.get("create.service.noAsadminScript", asadminScript));
        }
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private final static String TYPE = "type";
    private final static String NAME = "name";
    private final static String SERVICE_PROPERTIES = "serviceproperties";
    private final static String VALID_TYPES = "das|node-agent";
    private final static String DAS_TYPE = "das";
    private final static LocalStringsImpl strings = new LocalStringsImpl(CreateServiceCommand.class);
    private final static CLILogger logger = CLILogger.getInstance();

    private File    serverDir;
    private String  serviceName;
    private File    passwordFile;
    private File    asadminScript;
}