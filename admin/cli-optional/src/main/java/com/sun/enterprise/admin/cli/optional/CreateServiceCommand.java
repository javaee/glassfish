/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.*;
import java.util.*;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.servermgmt.services.ServiceFactory;
import com.sun.enterprise.admin.servermgmt.services.Service;
import com.sun.enterprise.admin.servermgmt.services.AppserverServiceType;
import com.sun.enterprise.universal.StringUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * Create a "service" in the operating system to start this domain
 * automatically.
 */
@org.jvnet.hk2.annotations.Service(name = "create-service")
@Scoped(PerLookup.class)
public final class CreateServiceCommand extends CLICommand {

    private static final String DOMAIN_PARENT_DIR = "domaindir";
    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String SERVICE_PROPERTIES = "serviceproperties";
    private static final String DRY_RUN = "dry-run";
    private static final String VALID_TYPES = "das|node-agent";
    private static final String DAS_TYPE = "das";

    private File    domainDir;
    private String  serviceName;
    private File    asadminScript;
    private File    domainDirParent;
    private String  domainName;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CreateServiceCommand.class);

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, NAME, '\0', "STRING", false, null);
        addOption(opts, SERVICE_PROPERTIES, '\0', "STRING", false, null);
        addOption(opts, DRY_RUN, '\0', "BOOLEAN", false, "false");
        addOption(opts, DOMAIN_PARENT_DIR, '\0', "STRING", false, null);
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "domain_name";
        operandType = "STRING";
        operandMin = 0;
        operandMax = 1;

        processProgramOptions();
    }

    /**
     * The validate method validates that the type and quantity of
     * parameters and operands matches the requirements for this
     * command.  The validate method supplies missing options from
     * the environment.  It also supplies passwords from the password
     * file or prompts for them if interactive.
     */
    protected void validate()
            throws CommandException, CommandValidationException  {
        try {
            super.validate();

            // The order that you make these calls matters!!
            validateDomainDir();
            validateName();
            validateAsadmin();
        }
        catch(CommandException e) {
            throw e;
        }
        catch(CommandValidationException e) {
            throw e;
        }
        catch(Exception e) {
            // plenty of RuntimeException possibilities!
            throw new CommandValidationException(e.getMessage(), e);
        }
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        // note: all of the calls to File.getPath() are guaranteed to return
        // good solid absolute paths because SmartFile is used for processing
        // all File objects in validate()
        try {
            boolean dry_run = getBooleanOption(DRY_RUN);
            String type = "das"; //getOption(TYPE);  TODO
            final Service service = ServiceFactory.getService();
            // configure service
            service.setDate(new Date().toString());
            service.setName(serviceName);
            service.setDryRun(dry_run);
            service.setLocation(domainDir.getPath());
            service.setType(type.equals("das") ?
                            AppserverServiceType.Domain :
                            AppserverServiceType.NodeAgent);
            service.setFQSN();
            service.setOSUser();
            service.setAsadminPath(asadminScript.getPath());

            if (programOpts.getPasswordFile() != null)
                service.setPasswordFilePath(SmartFile.sanitize(
                    new File(programOpts.getPasswordFile()).getPath()));

            service.setServiceProperties(getOption(SERVICE_PROPERTIES));
            service.isConfigValid();
            service.setTrace(CLILogger.isDebug());
            service.createService(service.tokensAndValues());
            logger.printMessage(service.getSuccessMessage());
        } catch (Exception e) {
            // We only want to wrap the string -- not the Exception.
            // Otherwise the message that is printed out to the user will be like this:
            // java.lang.IllegalArgumentException: The passwordfile blah blah blah
            // What we want is:
            // The passwordfile blah blah blah
            // IT 8882

            String msg = e.getMessage();
            
            if(StringUtils.ok(msg))
                throw new CommandValidationException(msg);
            else
                throw new CommandValidationException(e);
        }
        return 0;
    }

    private void validateDomainDir() throws CommandValidationException{
        if (operands.size() >= 1)
            domainName = operands.get(0);

        String domainDirParentPath = getOption(DOMAIN_PARENT_DIR);

        if (!ok(domainDirParentPath))
            domainDirParent = getDefaultDomainDirParent();
        else
            domainDirParent = SmartFile.sanitize(new File(domainDirParentPath));

        // either the default or the given is set.  Make sure it is valid...
        if (!domainDirParent.isDirectory()) {
            throw new CommandValidationException(
                strings.get("create.service.BadDomainDirParent",
                            domainDirParent));
        }

        if (!ok(domainName)) {
            domainName = getTheOneAndOnlyDomain();
        }

        domainDir = SmartFile.sanitize(new File(domainDirParent, domainName));

        if (!domainDir.isDirectory())
            throw new CommandValidationException(
                strings.get("create.service.BadDomainDir", domainDir));
    }

    private File getDefaultDomainDirParent() {
        String ir =
            System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

        if (!ok(ir))
            throw new RuntimeException(
                "Internal Error: System Property not set: " +
                    SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

        return SmartFile.sanitize(new File(new File(ir), "domains"));
    }

    private String getTheOneAndOnlyDomain() {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = domainDirParent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f != null && f.isDirectory();
            }
        });

        if (files == null || files.length == 0) {
            throw new RuntimeException(
                strings.get("create.service.noDomainDirs", domainDirParent));
        }

        if (files.length > 1) {
            throw new RuntimeException(
                strings.get("create.service.tooManyDomainDirs",
                            domainDirParent));
        }

        return files[0].getName();
    }

/*
    private void setupDomainRootDir() throws GFLauncherException {
        // if they set domainrootdir -- it takes precedence
        if (domainRootDir != null) {
            domainParentDir = domainRootDir.getParentFile();
            domainName = domainRootDir.getName();
            return;
        }

        // if they set domainParentDir -- use it.  o/w use the default dir
        if (domainParentDir == null) {
            domainParentDir = new File(installDir, DEFAULT_DOMAIN_PARENT_DIR);
        }

        // if they specified domain name -- use it.  o/w use the one and only
        // dir in the domain parent dir

        if (domainName == null) {
            domainName = getTheOneAndOnlyDomain();
        }

        domainRootDir = new File(domainParentDir, domainName);
    }
 */

    private void validateName() {
       serviceName = getOption(NAME);

       if (!ok(serviceName))
           serviceName = domainDir.getName();

       logger.printDebugMessage("service name = " + serviceName);
    }

    private void validateAsadmin() throws CommandValidationException {
        String s = SystemPropertyConstants.getAsAdminScriptLocation();

        if (!ok(s))
            throw new CommandValidationException(
                strings.get("internal.error",
                            "Can't get Asadmin script location"));

        asadminScript = SmartFile.sanitize(new File(s));

        if (!asadminScript.isFile()) {
            throw new CommandValidationException(
                strings.get("create.service.noAsadminScript", asadminScript));
        }
    }
}
