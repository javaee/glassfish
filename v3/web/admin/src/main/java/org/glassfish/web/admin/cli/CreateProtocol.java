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
package org.glassfish.web.admin.cli;

import java.util.List;
import java.util.Properties;
import java.util.Map;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.Protocols;
import com.sun.grizzly.config.dom.Protocol;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;

/**
 * Command to create protocol element within network-config
 *
 * Sample Usage :
 *    create-protocol [--securityenabled true|false] protocol_name
 *
 * domain.xml element example
 * 
 * <protocol name="http-listener-1">
 *   <http max-connections="250" default-virtual-server="server" server-name="">
 *     <file-cache enabled="false" />
 *   </http>
 *   <ssl ssl3-enabled="false" cert-nickname="s1as" />
 * </protocol>
 *
 * @author Nandini Ektare
 */
@Service(name="create-protocol")
@Scoped(PerLookup.class)
@I18n("create.protocol")
public class CreateProtocol implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateProtocol.class);

    @Param(name="protocolname", primary=true)
    String protocolName;
             
    // TODO: 
    // After v3 release, incorporate changes to CRUD <http/>, <port-unification/>
    // and <protocol-chain-instance-handler/>. As each has considerable number
    // of config options and no specific ids to co-relate, we may need to choose
    // the way create-ssl has been done. Grizzly team concurs on this proposal

    @Param(name="securityenabled", optional=true, defaultValue = "false")
    Boolean securityEnabled = false;

    @Inject
    Configs configs;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // check for duplicates
        List <Config> configList = configs.getConfig();
        Config config = configList.get(0);
        NetworkConfig networkConfig = config.getNetworkConfig();
        Protocols protocols = networkConfig.getProtocols();
        for (Protocol protocol : protocols.getProtocol()) {
            if (protocolName != null &&
                    protocolName.equalsIgnoreCase(protocol.getName())) {
                report.setMessage(localStrings.getLocalString(
                    "create.protocol.fail.duplicate",
                    "{0} protocol already exists. " +
                    "Cannot add duplicate protocol"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        // Add to the <network-config>
        try {
            ConfigSupport.apply(new SingleConfigCode<Protocols>() {
                public Object run(Protocols param)
                throws PropertyVetoException, TransactionFailure {
                    boolean docrootAdded = false;
                    boolean accessLogAdded = false;
                    Protocol newProtocol = param.createChild(Protocol.class);
                    newProtocol.setName(protocolName);
                    newProtocol.setSecurityEnabled(securityEnabled == null ? null : securityEnabled.toString());
                    param.getProtocol().add(newProtocol);
                    return newProtocol;
                }
            }, protocols);
        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("create.protocol.fail",
                "Failed to create protocol {0} ", protocolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        } catch(Exception e) {
            report.setMessage(localStrings.getLocalString("create.protocol.fail",
                    "Failed to create protocol {0} ", protocolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

    }
}
