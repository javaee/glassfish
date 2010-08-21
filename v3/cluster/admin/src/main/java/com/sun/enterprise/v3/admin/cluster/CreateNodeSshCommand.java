/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.enterprise.v3.admin.cluster;

import java.io.*;

import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import java.util.logging.Logger;

/**
 * Remote AdminCommand to create and ssh node.  This command is run only on DAS.
 * Register the node with SSH info on DAS
 *
 * @author Carla Mott
 */
@Service(name = "create-node-ssh")
@I18n("create.node.ssh")
@Scoped(PerLookup.class)
@Cluster({RuntimeType.DAS})
public class CreateNodeSshCommand implements AdminCommand  {

    @Inject
    private CommandRunner cr;

    @Inject
    Habitat habitat;

    @Param(name="name", primary = true)
    private String name;

    @Param(name="nodehost")
    private String nodehost;

    @Param(name = "installdir", optional=true)
    private String installdir;

    @Param(name="nodedir", optional=true)
    private String nodedir;

    @Param(name="sshport", optional=true)
    private String sshport;

    @Param(name = "sshuser", optional = true)
    private String sshuser;

    @Param(name = "sshkeyfile", optional = true)
    private String sshkeyfile;

    @Param(optional = true)
    private String sshpublickeyfile;

    @Param(name = "sshpassword", optional = true, password = true)
    private String sshpassword;

    @Param(name = "sshkeypassphrase", optional = true, password=true)
    private String sshkeypassphrase;

    @Param(name = "force", optional = true, defaultValue = "false")
    private boolean force;

    private static final String NL = System.getProperty("line.separator");

    private Logger logger = null;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        StringBuilder msg = new StringBuilder();
        SSHLauncher sshL=habitat.getComponent(SSHLauncher.class);

        logger = context.getLogger();

        setDefaults();        

        /*
         * XXX Was requiring a password even if I already had a valid
         * SSH key setup
        sshL.init(sshuser, nodehost,  Integer.parseInt(sshport), sshpassword, sshkeyfile, sshkeypassphrase, logger);
        
        try {
            sshL.setupKey(nodehost, sshpublickeyfile);
        } catch (IOException ce) {
            logger.fine("SSH key setup failed: " + ce.getMessage());
            if(!force) {
                report.setMessage(ce.getMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        } catch (Exception e) {
            //handle KeyStoreException
        }
         */

        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", name);
        map.add(NodeUtils.PARAM_INSTALLDIR, installdir);
        map.add(NodeUtils.PARAM_NODEHOST, nodehost);
        map.add(NodeUtils.PARAM_NODEDIR, nodedir);
        map.add(NodeUtils.PARAM_SSHPORT, sshport);
        map.add(NodeUtils.PARAM_SSHUSER, sshuser);
        map.add(NodeUtils.PARAM_SSHKEYFILE, sshkeyfile);
        map.add(NodeUtils.PARAM_SSHPASSWORD, sshpassword);
        map.add(NodeUtils.PARAM_SSHKEYPASSPHRASE, sshkeypassphrase);
        map.add(NodeUtils.PARAM_TYPE,"SSH");

        try {
            NodeUtils nodeUtils = new NodeUtils(habitat, logger);
            nodeUtils.validate(map, sshL);
        } catch (CommandValidationException e) {
            String m1 = Strings.get("node.ssh.invalid.params");
            if (!force) {
                String m2 = Strings.get("create.node.ssh.not.created");
                msg.append(StringUtils.cat(NL, m1, m2, e.getMessage()));
                report.setMessage(msg.toString());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            } else {
                String m2 = Strings.get("create.node.ssh.continue.force");
                msg.append(StringUtils.cat(NL, m1, e.getMessage(), m2));
            }
        }

        CommandInvocation ci = cr.getCommandInvocation("_create-node", report);
        ci.parameters(map);
        ci.execute();

        if (StringUtils.ok(report.getMessage())) {
            if (msg.length() > 0) {
                msg.append(NL);
            }
            msg.append(report.getMessage());
        }

        report.setMessage(msg.toString());
    }

    private void setDefaults() {
        if (sshport == null) {
            sshport = NodeUtils.NODE_DEFAULT_SSH_PORT;
        }
        if (sshuser == null) {
            sshuser = NodeUtils.NODE_DEFAULT_SSH_USER;
        }
        if (installdir == null) {
            installdir = NodeUtils.NODE_DEFAULT_INSTALLDIR;
        }        
    }
}
