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

import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.config.serverbeans.SshAuth;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.StringUtils;
import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.*;
import java.util.logging.Logger;

/**
 * Remote AdminCommand to update a config node.  This command is run only on DAS.
 *  Update the config node on DAS
 *
 * @author Carla Mott
 */
@Service(name = "_update-node")
@I18n("update.node")
@Scoped(PerLookup.class)
@ExecuteOn({RuntimeType.DAS})
public class UpdateNodeCommand implements AdminCommand {

    @Inject
    Nodes nodes;

    @Inject
    Domain domain;

    @Param(name="name", primary = true)
    String name;

    @Param(name="nodedir", optional=true)
    String nodedir;

    @Param(name="nodehost", optional=true)
    String nodehost;

    @Param(name = "installdir", optional=true)
    String installdir;

    @Param(name="sshport", optional=true)
    String sshport;

    @Param(name="sshuser", optional=true)
    String sshuser;

    @Param(name="sshnodehost", optional=true)
    String sshnodehost;

    @Param(name="sshkeyfile", optional=true)
    String sshkeyfile;

    @Param(name = "sshpassword", optional = true, password=true)
     String sshpassword;

    @Param(name = "sshkeypassphrase", optional = true, password=true)
     String sshkeypassphrase;

    @Param(name = "type", optional=true)
     String type;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger= context.logger;

        Node node= nodes.getNode(name);
        if (node == null) {
            //node doesn't exist
            String msg = Strings.get("noSuchNode", name);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        // If the node is in use then we can't change certain attributes
        // like the install directory or node directory.
        if (node.nodeInUse()) {
            String badparam = null;
            if (StringUtils.ok(nodedir))  {
                badparam = "nodedir";
            }
            if (StringUtils.ok(installdir))  {
                badparam = "installdir";
            }

            if (StringUtils.ok(badparam)) {
                String msg = Strings.get("noUpdate.nodeInUse", name, badparam);
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        }

        try {
            updateNodeElement(name);
        } catch(TransactionFailure e) {
            logger.warning("failed.to.update.node " + name);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
        }

        
    }


    public void updateNodeElement(final String nodeName) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode() {
            @Override
            public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {
                // get the transaction
                Transaction t = Transaction.getTransaction(param);
                if (t!=null) {
                   Nodes nodes = ((Domain)param).getNodes();
                    Node node = nodes.getNode(nodeName);
                    Node writeableNode = t.enroll(node);
                    if (nodedir != null)
                        writeableNode.setNodeDir(nodedir);
                    if (nodehost != null)
                        writeableNode.setNodeHost(nodehost);
                    if (installdir != null)
                        writeableNode.setInstallDir(installdir);
                    if (type != null)
                        writeableNode.setType(type);
                    if (sshport != null || sshnodehost != null ||sshuser != null || sshkeyfile != null){
                        SshConnector sshC = writeableNode.getSshConnector();
                        if (sshC == null)  {
                            sshC =writeableNode.createChild(SshConnector.class);
                        }else
                            sshC = t.enroll(sshC);

                        if (sshport != null)
                            sshC.setSshPort(sshport);
                        if(sshnodehost != null)
                            sshC.setSshHost(sshnodehost);

                        if (sshuser != null || sshkeyfile != null || sshpassword != null || sshkeypassphrase != null ) {
                            SshAuth sshA = sshC.getSshAuth();
                            if (sshA == null) {
                               sshA = sshC.createChild(SshAuth.class);
                            } else
                                sshA = t.enroll(sshA);

                            if (sshuser != null)
                                sshA.setUserName(sshuser);
                            if (sshkeyfile != null)
                                sshA.setKeyfile(sshkeyfile);
                            if(sshpassword != null)
                                sshA.setPassword(sshpassword);
                            if(sshkeypassphrase != null)
                                sshA.setKeyPassphrase(sshkeypassphrase);
                            sshC.setSshAuth(sshA);
                        }
                        writeableNode.setSshConnector(sshC);
                        
                    }
                   
                }
                return Boolean.TRUE;
            }

        }, domain);
    }
            
}
