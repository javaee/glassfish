/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.jts.admin.cli;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.transaction.api.ResourceRecoveryManager;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.i18n.StringManager;

@Service(name = "recover-transactions")
@Scoped(PerLookup.class)
@I18n("recover.transactions")
public class RecoverTransactions implements AdminCommand {

    private static StringManager localStrings =
            StringManager.getManager(RecoverTransactions.class);

    @Param(name = "transactionlogdir", optional = true)
    String transactionLogDir;

    @Param(name = "destination", optional = true)
    String destinationServer;

    @Param(name = "server_name", primary = true)
    String serverToRecover;

    @Inject
    ResourceRecoveryManager recoveryManager;

    @Inject
    ServerContext _serverContext;

    @Inject
    Domain domain;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (domain.getServerNamed(serverToRecover) == null) {
            report.setMessage(localStrings.getString("recover.transactions.serverBeRecoveredIsNotKnown",
                    serverToRecover));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        if (isServerRunning(serverToRecover)) {
            if (destinationServer != null && !serverToRecover.equals(destinationServer)) {
                report.setMessage(localStrings.getString(
                        "recover.transactions.runningServerBeRecoveredFromAnotherServer",
                        serverToRecover, destinationServer));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
            if (transactionLogDir != null) {
                report.setMessage(localStrings.getString(
                        "recover.transactions.logDirShouldNotBeSpecifiedForSelfRecovery"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
        } else if (destinationServer == null) {
            report.setMessage(localStrings.getString("recover.transactions.noDestinationServer"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);

        } else if (!isServerRunning(destinationServer)) {
            report.setMessage(localStrings.getString("recover.transactions.destinationServerIsNotAlive",
                    serverToRecover));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);

        } else if (transactionLogDir == null) {
            report.setMessage(localStrings.getString("recover.transactions.logDirNotSpecifiedForDelegatedRecovery"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        if (destinationServer == null) {
            destinationServer = serverToRecover;
        }

        try {
            boolean result;
            if (!(destinationServer.equals(_serverContext.getInstanceName()))) {
                result = recoveryManager.recoverIncompleteTx(true, transactionLogDir);
            } else {
                result = recoveryManager.recoverIncompleteTx(false, null);
            }
            if (result)
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            else
                throw new IllegalStateException();
        } catch (Exception e) {
            report.setMessage(localStrings.getString("recover.transactions.failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    // Implementation note: This has to be redone when clustering is supported in V3. Currently this implementation
    // returns true by default as it gets executed only if DAS is running.
    private boolean isServerRunning(String serverName) {
        return true;
    }
}
