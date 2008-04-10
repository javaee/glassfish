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
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.List;

/**
 * Delete virtual server command
 * 
 */
@Service(name="delete-virtual-server")
@Scoped(PerLookup.class)
@I18n("delete.virtual.server")
public class DeleteVirtualServer implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteVirtualServer.class);

    @Param(name="virtual_server_id", primary=true)
    String virtualServerId;

    @Inject
    HttpService httpService;

    //xxx
    
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        try {
            ConfigSupport.apply(new SingleConfigCode<HttpService>() {

                public Object run(HttpService param) throws PropertyVetoException, TransactionFailure {
                    List<VirtualServer> list = param.getVirtualServer();
                    Iterator iter = list.iterator();
                    while (iter.hasNext()) {
                        VirtualServer virtualServer = (VirtualServer)iter.next();
                        if (virtualServer.getId().equals(virtualServerId)) {
                            list.remove(virtualServer);
                        }
                    };
                    return list;
                }
            }, httpService);

            report.setMessage(localStrings.getLocalString("delete.virtual.server.success", "{0} deleted successfully", virtualServerId));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("delete.virtual.server.fail", "{0} delete failed ", virtualServerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
