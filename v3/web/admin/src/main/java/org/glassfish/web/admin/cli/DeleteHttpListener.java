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

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.NetworkListeners;
import com.sun.grizzly.config.dom.Protocols;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Delete http listener command
 */
@Service(name = "delete-http-listener")
@Scoped(PerLookup.class)
@I18n("delete.http.listener")
public class DeleteHttpListener implements AdminCommand {
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteHttpListener.class);
    @Param(name = "listener_id", primary = true)
    String listenerId;
    @Param(name = "secure", optional = true)
    String secure;
    @Inject
    HttpService httpService;
    @Inject
    NetworkConfig networkConfig;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and
     * the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        if (!exists()) {
            report.setMessage(localStrings.getLocalString("delete.http.listener.notexists", "{0} doesn't exist",
                listenerId));
            report.setActionExitCode(ExitCode.FAILURE);
            return;
        }
        try {
            NetworkListener ls = networkConfig.getNetworkListener(listenerId);
            VirtualServer vs = httpService
                .getVirtualServerByName(ls.findProtocol().getHttp().getDefaultVirtualServer());
            ConfigSupport.apply(new Config(), networkConfig.getNetworkListeners());
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
                public Object run(VirtualServer avs) throws PropertyVetoException {
                    String lss = avs.getNetworkListeners();
                    if (lss != null && lss.contains(listenerId)) { //change only if needed
                        Pattern p = Pattern.compile(",");
                        String[] names = p.split(lss);
                        List<String> nl = new ArrayList<String>();
                        for (String name : names) {
                            if (!listenerId.equals(name)) {
                                nl.add(name);
                            }
                        }
                        //we removed the listenerId from lss and is captured in nl by now
                        lss = nl.toString();
                        lss = lss.substring(1, lss.length() - 1);
                        avs.setNetworkListeners(lss);
                    }
                    return avs;
                }
            }, vs);
            //remove the id from associated virtual-server's
            report.setActionExitCode(ExitCode.SUCCESS);

        } catch (TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("delete.http.listener.fail", "failed",
                listenerId));
            report.setActionExitCode(ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private boolean exists() {
        return networkConfig.getNetworkListener(listenerId) != null;
    }

    private void cleanUp(NetworkListener listener) throws TransactionFailure {
        final Protocol protocol = listener.findProtocol();
        boolean found = false;
        for (NetworkListener candidate : listener.getParent(NetworkListeners.class).getNetworkListener()) {
            found |= !listener.getName().equals(candidate.getName()) && candidate.getProtocol()
                .equals(protocol.getName());
        }
        if (!found) {
            ConfigSupport.apply(new SingleConfigCode<Protocols>() {
                @Override
                public Object run(Protocols param) throws PropertyVetoException, TransactionFailure {
                    List<Protocol> list = new ArrayList<Protocol>(param.getProtocol());
                    for (Protocol old : list) {
                        if (protocol.getName().equals(old.getName())) {
                            list.remove(old);
                            break;
                        }
                    }
                    param.getProtocol().clear();
                    param.getProtocol().addAll(list);
                    return null;
                }
            }, protocol.getParent(Protocols.class));
        }
    }

    private class Config implements SingleConfigCode<NetworkListeners> {
        public Object run(NetworkListeners param) throws PropertyVetoException, TransactionFailure {
            final List<NetworkListener> list = param/*.getNetworkListeners()*/.getNetworkListener();
            for (NetworkListener listener : list) {
                if (listener.getName().equals(listenerId)) {
                    list.remove(listener);
                    cleanUp(listener);
                    break;
                }
            }
            return list;
        }
    }
}
