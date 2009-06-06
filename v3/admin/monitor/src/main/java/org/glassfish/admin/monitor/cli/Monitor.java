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
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specContractic
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  Contract applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identContractying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Contract you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  Contract you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, Contract you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only Contract the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.admin.monitor.cli;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport.ExitCode;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * Return the version and build number
 *
 * @author Prashanth Abbagani
 */
@Service(name="monitor")
@I18n("monitor.command")
public class Monitor implements AdminCommand {

    @Param(optional=true)
    private String type;

    @Param(optional=true)
    private String filter;

    @Inject
    private Habitat habitat;

    final private LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(Monitor.class);


    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        MonitorContract mContract = null;
        for (MonitorContract m : habitat.getAllByContract(MonitorContract.class)) {
            if ((m.getName()).equals(type)) {
                mContract = m;
                break;
            }
        }
        if (mContract != null) {
            mContract.process(report, filter);
            return;
        }
        report.setMessage(localStrings.getLocalString("monitor.type.error", 
                "No type exists in habitat for the given monitor type {0}", type));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }
}
