/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.util.io.DomainDirs;
import java.io.*;
import java.util.*;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.servermgmt.services.ServiceFactory;
import com.sun.enterprise.admin.servermgmt.services.Service;
import com.sun.enterprise.admin.servermgmt.services.AppserverServiceType;
import com.sun.enterprise.universal.StringUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.InstanceDirs;
import com.sun.enterprise.util.io.ServerDirs;

/**
 * Create a "service" in the operating system to start this domain
 * automatically.
 */
@org.jvnet.hk2.annotations.Service(name = "create-service")
@Scoped(PerLookup.class)
public final class CreateServiceCommand extends CLICommand {

    @Param(name = "name", optional = true)
    private String serviceName;
    @Param(name = "serviceproperties", optional = true)
    private String serviceProperties;
    @Param(name = "dry-run", optional = true, defaultValue = "false")
    private boolean dry_run;
    @Param(name = "force", optional = true, defaultValue = "false")
    private boolean force;
    @Param(name = "domaindir", optional = true)
    private File domainDirParent;
    @Param(name = "server_name", primary = true, optional = true, alias = "domain_name")
    private String serverName;
    @Param(name = "nodedir", optional = true, alias = "agentdir")
    protected String nodeDir;           // nodeDirRoot
    @Param(name = "node", optional = true, alias = "nodeagent")
    protected String node;
    private File domainDir;  // the directory of the domain itself
    private File asadminScript;
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CreateServiceCommand.class);
    private ServerDirs dirs;
    private InstanceDirs instanceDirs;
    private DomainDirs domainDirs;

    /**
     */
    @Override
    protected void validate()
            throws CommandException, CommandException {
        try {
            super.validate(); // pointless empty method but who knows what the future holds?

            // The order that you make these calls matters!!
            validateDomainOrInstance();
            validateDomainDir();
            validateName();
            validateAsadmin();
        }
        catch (CommandException e) {
            throw e;
        }
        catch (Exception e) {
            // plenty of RuntimeException possibilities!
            throw new CommandException(e.getMessage(), e);
        }
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandException {
        // note: all of the calls to File.getPath() are guaranteed to return
        // good solid absolute paths because SmartFile is used for processing
        // all File objects in validate()
        try {
            String type = "das"; // TODO - make it a command option
            final Service service = ServiceFactory.getService();
            // configure service
            service.setDate(new Date().toString());
            service.setName(serviceName);
            service.setDryRun(dry_run);
            service.setLocation(domainDir.getPath());
            service.setType(type.equals("das")
                    ? AppserverServiceType.Domain
                    : AppserverServiceType.NodeAgent);
            service.setFQSN();
            service.setOSUser();
            service.setAsadminPath(asadminScript.getPath());

            if (programOpts.getPasswordFile() != null)
                service.setPasswordFilePath(SmartFile.sanitize(
                        new File(programOpts.getPasswordFile()).getPath()));

            service.setServiceProperties(serviceProperties);
            service.isConfigValid();
            service.setTrace(CLILogger.isDebug());
            service.setForce(force);

            service.createService(service.tokensAndValues());

            // Why the messiness?  We don't want to talk about the help
            // file inside the help file thus the complications below...
            String help = service.getSuccessMessage();
            String tellUserAboutHelp = strings.get("create.service.runtimeHelp", help);
            logger.printMessage(tellUserAboutHelp);
            service.writeReadmeFile(help);

        }
        catch (Exception e) {
            // We only want to wrap the string -- not the Exception.
            // Otherwise the message that is printed out to the user will be like this:
            // java.lang.IllegalArgumentException: The passwordfile blah blah blah
            // What we want is:
            // The passwordfile blah blah blah
            // IT 8882

            String msg = e.getMessage();

            if (StringUtils.ok(msg))
                throw new CommandException(msg);
            else
                throw new CommandException(e);
        }
        return 0;
    }

    void validateDomainDir() throws CommandException {
        if (domainDirParent == null)
            domainDirParent = getDefaultDomainDirParent();
        else
            domainDirParent = SmartFile.sanitize(domainDirParent);

        // either the default or the given is set.  Make sure it is valid...
        if (!domainDirParent.isDirectory()) {
            throw new CommandException(
                    strings.get("create.service.BadDomainDirParent",
                    domainDirParent));
        }

        if (!ok(serverName)) {
            serverName = getTheOneAndOnlyDomain();
        }

        domainDir = SmartFile.sanitize(new File(domainDirParent, serverName));

        if (!domainDir.isDirectory())
            throw new CommandException(
                    strings.get("create.service.BadDomainDir", domainDir));
    }

    /**
     * make sure the parameters make sense for either an instance or a domain.
     */
    private void validateDomainOrInstance() throws CommandException, IOException {
        // case 1: since ddp is specified - it MUST be a domain
        if (domainDirParent != null) {
            domainDirs = new DomainDirs(domainDirParent, serverName);
        }
        //case 2: if either of these are set then it MUST be an instance
        else if (node != null || nodeDir != null) {
            instanceDirs = new InstanceDirs(nodeDir, node, serverName);
        }
        // case 3: nothing is specified -- use default domain as in v3.0
        else if (serverName == null) {
            domainDirs = new DomainDirs(domainDirParent, serverName);
        }
        // case 4: serverName is set and the other 3 are all null
        // we need to figure out if it's a DAS or an instance
        else {
            try {
                domainDirs = new DomainDirs(domainDirParent, serverName);
                return;
            }
            catch(IOException e) {
                // handled below
            }

            instanceDirs = new InstanceDirs(nodeDir, node, serverName);
        }
    }

    private File getDefaultDomainDirParent() {
        String ir =
                System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

        if (!ok(ir))
            throw new RuntimeException(
                    "Internal Error: System Property not set: "
                    + SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

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
    serverName = domainRootDir.getName();
    return;
    }

    // if they set domainParentDir -- use it.  o/w use the default dir
    if (domainParentDir == null) {
    domainParentDir = new File(installDir, DEFAULT_DOMAIN_PARENT_DIR);
    }

    // if they specified domain name -- use it.  o/w use the one and only
    // dir in the domain parent dir

    if (serverName == null) {
    serverName = getTheOneAndOnlyDomain();
    }

    domainRootDir = new File(domainParentDir, serverName);
    }
     */
    private void validateName() {
        if (!ok(serviceName))
            serviceName = domainDir.getName();

        logger.printDebugMessage("service name = " + serviceName);
    }

    private void validateAsadmin() throws CommandException {
        String s = SystemPropertyConstants.getAsAdminScriptLocation();

        if (!ok(s))
            throw new CommandException(
                    strings.get("internal.error",
                    "Can't get Asadmin script location"));

        asadminScript = SmartFile.sanitize(new File(s));

        if (!asadminScript.isFile()) {
            throw new CommandException(
                    strings.get("create.service.noAsadminScript", asadminScript));
        }
    }
}
