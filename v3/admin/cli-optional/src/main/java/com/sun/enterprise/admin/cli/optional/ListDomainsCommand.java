/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.enterprise.util.io.ServerDirs;
import java.io.File;
import java.io.IOException;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.admin.*;
import org.glassfish.api.Param;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.io.DomainDirs;

/**
 *  This is a local command that lists the domains.
 */
@Service(name = "list-domains")
@Scoped(PerLookup.class)
public final class ListDomainsCommand extends LocalDomainCommand {

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ListDomainsCommand.class);
    private String domainsRoot = null;

    /*
     * We don't want the domain to be initialized since this command is not
     * for a specific domain.
     */
    @Override
    protected void initDomain() throws CommandException { }

    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        try {
            DomainConfig domainConfig = new DomainConfig(null, getDomainsRoot());
            DomainsManager manager = new PEDomainsManager();
            String[] domainsList = manager.listDomains(domainConfig);
            programOpts.setInteractive(false);  // no prompting for passwords
            if (domainsList.length > 0) {
                for (String dn : domainsList) {
                    String status = getStatus(dn);
                    String dname = strings.get("list.domains.Name");
                    logger.printMessage(dname + " " + dn + " " + status);
                }
            } else {
                logger.printDetailMessage(strings.get("NoDomainsToList"));
            }
        } catch (Exception ex) {
            throw new CommandException(ex.getLocalizedMessage());
        }
        return 0;
    }

    protected String getDomainsRoot() throws CommandException {
        if (domainsRoot != null) {
            return domainsRoot;
        }
        try {
            File domainsDirFile = ok(domainDirParam) ?
                new File(domainDirParam) : DomainDirs.getDefaultDomainsDir();
            if (!domainsDirFile.isDirectory()) {
                throw new CommandException(
                            strings.get("InvalidDomainPath",
                            domainsDirFile.toString()));
            }
            domainsRoot = SmartFile.sanitize(domainsDirFile.getAbsolutePath());
            return domainsRoot;
        }
        catch (IOException ioe) {
            throw new CommandException(strings.get("InvalidDomainPath",
                    ioe.getLocalizedMessage()));
        }
    }

    // Implementation note: This has to be redone - km@dev.java.net (Aug 2008)
    private String getStatus(String dn) throws IOException, CommandException {
        try {
            GFLauncher launcher = GFLauncherFactory.getInstance(
                RuntimeType.DAS);
            GFLauncherInfo li = launcher.getInfo();
            String parent = getDomainsRoot();
            
            if (parent != null)
                li.setDomainParentDir(parent);            

            li.setDomainName(dn);
            launcher.setup(); //admin ports are not available otherwise
            setServerDirs(new ServerDirs(li.getInstanceRootDir()));

            programOpts.setPort(li.getAnAdminPort());
            boolean status =
                isThisDAS(SmartFile.sanitize(li.getInstanceRootDir()));
            if (status) {
                try {
                    RemoteCommand cmd =
                        new RemoteCommand("_get-restart-required",
                                            programOpts, env);
                    String restartRequired =
                        cmd.executeAndReturnOutput("_get-restart-required");
                    if (Boolean.parseBoolean(restartRequired.trim()))
                        return strings.get("list.domains.StatusRestartRequired");
                } catch (Exception ex) {
                }
                return strings.get("list.domains.StatusRunning");
            } else
                return strings.get("list.domains.StatusNotRunning");
        } catch (GFLauncherException gf) {
            logger.printExceptionStackTrace(gf);
            return strings.get("list.domains.StatusUnknown");
        } catch (MiniXmlParserException me) {
            logger.printExceptionStackTrace(me);
            return strings.get("list.domains.StatusUnknown");
        }
    }
}
