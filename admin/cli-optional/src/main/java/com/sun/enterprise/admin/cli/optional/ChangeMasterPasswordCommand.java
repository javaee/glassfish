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

import com.sun.enterprise.admin.cli.LocalDomainCommand;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.admin.cli.CommandException;
import com.sun.enterprise.admin.cli.CommandValidationException;
import com.sun.enterprise.admin.cli.ValidOption;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;

/** Class that implements the change-master-password command.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@Service(name = "change-master-password")
@Scoped(PerLookup.class)
public class ChangeMasterPasswordCommand extends LocalDomainCommand {
    private String savemp = "savemasterpassword";
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ChangeMasterPasswordCommand.class);
    @Override
    protected void prepare() throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, savemp, '\0', "BOOLEAN", false, "false");
        addOption(opts, "domaindir", '\0', "STRING", false, null);
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandType = "STRING";
        operandName = "domain_name";
        operandMin = 0;
        operandMax = 1;

        super.processProgramOptions();
    }

    @Override
    protected void validate() throws CommandException, CommandValidationException {
        super.validate();
    }
    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        try {
            if (super.isRunning(super.getAdminPort(super.getDomainXml())))
                throw new CommandException(strings.get("domain.is.running", super.domainName, super.domainRootDir));
            DomainConfig domainConfig = new DomainConfig(super.domainName,
                super.domainsDir.getAbsolutePath());
            PEDomainsManager manager = new PEDomainsManager();
            String mp = super.readFromMasterPasswordFile();
            if (mp == null) {
                mp = passwords.get("AS_ADMIN_MASTERPASSWORD");
                if (mp == null) {
                    mp = super.readPassword(strings.get("current.mp"));
                }
            }
            if (mp == null)
                throw new CommandException(strings.get("no.console"));
            if (!super.verifyMasterPassword(mp))
                throw new CommandException(strings.get("incorrect.mp"));
            ValidOption nmpo = new ValidOption("New_Master_Password", "PASSWORD", ValidOption.REQUIRED, null);
            String nmp = super.getPassword(nmpo, null, true);
            if (nmp == null)
                throw new CommandException(strings.get("no.console"));
            domainConfig.put(DomainConfig.K_MASTER_PASSWORD, mp);
            domainConfig.put(DomainConfig.K_NEW_MASTER_PASSWORD, nmp);
            domainConfig.put(DomainConfig.K_SAVE_MASTER_PASSWORD, saveIt());
            manager.changeMasterPassword(domainConfig);
            //Implementation note: Not sure if keys in domain-passwords are reencrypted - TODO - km@dev.java.net
            return 0;
        } catch(Exception e) {
            throw new CommandException(e);
        }
    }

    private Boolean saveIt() {
        String value = super.getOption(savemp);
        if (value == null)
            return Boolean.FALSE;
        else
            return Boolean.valueOf(value);
    }
}
