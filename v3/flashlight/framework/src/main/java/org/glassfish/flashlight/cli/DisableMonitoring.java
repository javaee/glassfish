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
package org.glassfish.flashlight.cli;

import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.beans.PropertyVetoException;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import com.sun.enterprise.config.serverbeans.MonitoringService;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.Dom;

/**
 * @author Sreenivas Munnangi
 */
@Service(name="disable-monitoring")
@I18n("disable.monitoring")
public class DisableMonitoring implements AdminCommand {

    // list of modules separated by comma
    @Param(optional=true)
    private String modules;

    @Inject
    private MonitoringService ms;

    final private LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(DisableMonitoring.class);

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        try {
            if ((modules == null) || (modules.length() < 1)) {
                // check if it is already false
                boolean enabled = Boolean.parseBoolean(ms.getMonitoringEnabled());
                // set overall monitoring-enabled to false
                if (enabled) {
                    MonitoringConfig.setMonitoringEnabled(ms, "false", report);
                } else {
                    report.setMessage(
                        localStrings.getLocalString("disable.monitoring.alreadyfalse",
                        "monitoring-enabled is already set to false"));
                }
            } else {
                // for each module set monitoring level to OFF
                String[] strArr = modules.split(":");
                for (String moduleName: strArr) {
                    if (moduleName.length() > 0) {
                        MonitoringConfig.setMonitoringLevel(ms, moduleName, ContainerMonitoring.LEVEL_OFF, report);
                    }
                }
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("disable.monitoring.exception",
                "Encountered exception during disabling monitoring {0}", e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

}
