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

import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.admin.cli.LocalDomainCommand;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;

/**
 * The change-master-password command.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@Service(name = "change-master-password")
@Scoped(PerLookup.class)
@Cluster({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER})
public class ChangeMasterPasswordCommand extends LocalDomainCommand {
    @Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    private boolean savemp;

    @Param(name = "domain_name", primary = true, optional = true)
    private String domainName0;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ChangeMasterPasswordCommand.class);

    @Param(name = "target", optional = true, defaultValue =
        SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Override
    protected void validate()
            throws CommandException, CommandValidationException  {
        setDomainName(domainName0);
        super.validate();
    }

    @Override
    protected int executeCommand()
                    throws CommandException, CommandValidationException {
        try {
            if (super.isRunning(super.getAdminPort()))
                throw new CommandException(strings.get("domain.is.running",
                                                    getDomainName(), getDomainRootDir()));
            DomainConfig domainConfig = new DomainConfig(getDomainName(),
                getDomainsDir().getAbsolutePath());
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
            ParamModelData nmpo = new ParamModelData("New_Master_Password",
                String.class, false, null);
            nmpo.param._password = true;
            String nmp = super.getPassword(nmpo, null, true);
            if (nmp == null)
                throw new CommandException(strings.get("no.console"));
            domainConfig.put(DomainConfig.K_MASTER_PASSWORD, mp);
            domainConfig.put(DomainConfig.K_NEW_MASTER_PASSWORD, nmp);
            domainConfig.put(DomainConfig.K_SAVE_MASTER_PASSWORD, savemp);
            manager.changeMasterPassword(domainConfig);
            // Implementation note: Not sure if keys in domain-passwords
            // are reencrypted - TODO - km@dev.java.net
            return 0;
        } catch(Exception e) {
            throw new CommandException(e);
        }
    }
}
