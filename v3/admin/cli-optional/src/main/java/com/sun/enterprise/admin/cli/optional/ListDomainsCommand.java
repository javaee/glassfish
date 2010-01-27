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

import java.io.File;
import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
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
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 *  This is a local command that lists the domains.
 */
@Service(name = "list-domains")
@Scoped(PerLookup.class)
public final class ListDomainsCommand extends LocalDomainCommand {

    private static final String DOMAINDIR = "domaindir";

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ListDomainsCommand.class);

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, DOMAINDIR, '\0', "STRING", false, null);
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandType = "STRING";
        operandMin = 0;
        operandMax = 0;

        processProgramOptions();
    }

    /**
     * Override superclass version to do nothing, since this command
     * doesn't operate on just a single domain.
     */
    protected void initDomain() {
    }
 
    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        try {
            DomainConfig domainConfig = new DomainConfig(null, 
                getDomainsRoot());
            DomainsManager manager = new PEDomainsManager();
            String[] domainsList = manager.listDomains(domainConfig);
            programOpts.setInteractive(false);  // no prompting for passwords
            if (domainsList.length > 0) {
                for (int i = 0; i < domainsList.length; i++) {
                    String dn = domainsList[i];
                    String status = getStatus(dn);
                    String name = strings.get("list.domains.Name");
                    logger.printMessage(name + " " + dn + " " + status);
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
        String domainDir = getOption(DOMAINDIR);
        if (domainDir == null) {
            domainDir = getSystemProperty(
                            SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
        }
        if (domainDir == null) {
            throw new CommandException(
                            strings.get("InvalidDomainPath", domainDir));
        }
        return domainDir;
    }

    // Implementation note: This has to be redone - km@dev.java.net (Aug 2008)
    private String getStatus(String dn) {
        try {
            GFLauncher launcher = GFLauncherFactory.getInstance(
                GFLauncherFactory.ServerType.domain);
            GFLauncherInfo li = launcher.getInfo();
            String parent = getOption(DOMAINDIR);
            if (parent != null)
                li.setDomainParentDir(parent);            
            li.setDomainName(dn);
            launcher.setup(); //admin ports are not available otherwise
            initializeLocalPassword(li.getInstanceRootDir());
            Set<Integer> adminPorts = li.getAdminPorts();
            programOpts.setPort(adminPorts.iterator().next());
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
