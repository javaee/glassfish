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

import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListeners;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.Protocol;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;

import java.util.List;

/**
 * Delete Network Listener command
 * 
 */
@Service(name="delete-network-listener")
@Scoped(PerLookup.class)
@I18n("delete.network.listener")
public class DeleteNetworkListener implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(DeleteNetworkListener.class);

    @Param(name="networkListenerName", primary=true)
    String networkListenerName;

    NetworkListener listenerToBeRemoved = null;
    
    @Inject
    Configs configs;
    @Inject
    Habitat habitat;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        List <Config> configList = configs.getConfig();
        Config config = configList.get(0);
        NetworkConfig networkConfig = config.getNetworkConfig();
        NetworkListeners networkListeners = networkConfig.getNetworkListeners();

        try {
            for (NetworkListener nl : networkListeners.getNetworkListener()) {
                if (networkListenerName.equalsIgnoreCase(nl.getName())) {
                    listenerToBeRemoved = nl;
                }
            }

            if (listenerToBeRemoved == null) {
                report.setMessage(localStrings.getLocalString("delete.network.listener.notexists",
                    "{0} Network Listener doesn't exist", networkListenerName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
                public Object run(NetworkListeners param) throws PropertyVetoException, TransactionFailure {
                    param.getNetworkListener().remove(listenerToBeRemoved);
                    updateVirtualServer(listenerToBeRemoved);
                    return listenerToBeRemoved;
                }
            }, networkListeners);
            
        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("delete.networkListener.fail",
                "Deletion of NetworkListener {0} failed", networkListenerName) + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void updateVirtualServer(final NetworkListener listener) throws TransactionFailure {
        final Protocol prot = listener.findHttpProtocol();
        if (prot != null) {
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
                public Object run(final VirtualServer param) throws PropertyVetoException, TransactionFailure {
                    param.removeNetworkListener(listener.getName());
                    return null;
                }
            }, habitat.getComponent(VirtualServer.class, prot.getHttp().getDefaultVirtualServer()));
        }
    }

}