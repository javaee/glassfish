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

package org.glassfish.ejb.admin.cli;

import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import org.glassfish.ejb.api.DistributedEJBTimerService;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;


@Service(name="list-timers")
@I18n("list.timers")

public class ListTimers implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListTimers.class);

    @Param(primary=true, optional=true)
    String target;

    @Inject
    DistributedEJBTimerService timerService;

    @Inject
    Servers servers;

    /**
     * Executes the command
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        //TODO Required to be modified when cluster is supported.
        if (target != null && !target.equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
            report.setMessage(localStrings.getLocalString("list.timers" +
                    ".invalid.target", "Invalid target specified as Operand"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        try {
            String[] timerList = timerService.listTimers
                    (new String[]{servers.getServer().get(0).getName()});

            for (String timer : timerList) {
                final ActionReport.MessagePart part =
                        report.getTopMessagePart().addChild();
                part.setMessage(timer);
            }

            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.timers" +
                    ".failed", "List Timers command failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }

    }

}
