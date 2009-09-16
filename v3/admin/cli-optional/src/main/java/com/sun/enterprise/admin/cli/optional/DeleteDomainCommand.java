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

import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.StoreException;

/**
 *  This is a local command that deletes a domain.
 */
@Service(name = "delete-domain")
@Scoped(PerLookup.class)
public final class DeleteDomainCommand extends LocalDomainCommand {

    private static final String DOMAINDIR = "domaindir";

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(DeleteDomainCommand.class);

    private int adminPort;  //this is single threaded code, deliberately avoiding volatile/atomic
    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, DOMAINDIR, '\0', "STRING", false, null);
        // not a remote command so have to process --terse and --echo ourselves
        addOption(opts, "terse", '\0', "BOOLEAN", false, "false");
        addOption(opts, "echo", '\0', "BOOLEAN", false, "false");
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "domain_name";
        operandType = "STRING";
        operandMin = 1;
        operandMax = 1;
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
        super.validate();

        // if --terse or -echo are supplied, copy them over to program options
        if (options.containsKey("echo"))
            programOpts.setEcho(getBooleanOption("echo"));
        if (options.containsKey("terse"))
            programOpts.setTerse(getBooleanOption("terse"));
        initializeLogger();     // in case program options changed
        adminPort = super.getAdminPort(super.getDomainXml());
    }
 
    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        try {            
            DomainConfig domainConfig =
                new DomainConfig(domainName, domainsDir.getPath());
            checkRunning();
            DomainsManager manager = new PEDomainsManager();
            manager.deleteDomain(domainConfig);
            //By default, do as what v2 does -- don't delete the entry - might need a revisit (Kedar: 09/16/2009)
            //deleteLoginInfo();
        } catch (Exception e) {
	        throw new CommandException(e.getLocalizedMessage());
        }

	logger.printDetailMessage(strings.get("DomainDeleted", domainName));
        return 0;
    }

    private void checkRunning() throws CommandException {
        if (super.isRunning(adminPort)) {
            String msg = strings.get("domain.is.running", super.domainName, super.domainRootDir);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * This method will delete the entry in the .asadminpass file if exists
     */
    private void deleteLoginInfo() throws CommandException, StoreException {
        LoginInfoStore store = LoginInfoStoreFactory.getDefaultStore();
        store.remove("localhost", adminPort);  //the host is always "localhost" in this case
    }
}
