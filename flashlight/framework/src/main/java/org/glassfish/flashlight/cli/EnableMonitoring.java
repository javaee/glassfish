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

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport.ExitCode;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.tools.attach.VirtualMachine;
import org.glassfish.server.ServerEnvironmentImpl;
import java.io.File;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import com.sun.enterprise.config.serverbeans.MonitoringService;
import com.sun.enterprise.universal.process.ProcessUtils;
import org.glassfish.flashlight.impl.client.FlashlightProbeClientMediator;

/**
 * @author Sreenivas Munnangi
 */
@Service(name="enable-monitoring")
@Scoped(PerLookup.class)
@I18n("enable.monitoring")
public class EnableMonitoring implements AdminCommand {

    @Inject
    MonitoringService ms;

    @Param(optional=true)
    private String pid;

    @Param(optional=true)
    private String options;

    @Param(optional=true)
    private String modules;

    @Param(optional=true)
    private Boolean mbean;

    @Param(optional=true)
    private Boolean dtrace;

    final private LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(EnableMonitoring.class);

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        // attach agent using given options
        // TODO: allow for user defined port
        if (!FlashlightProbeClientMediator.isAgentAttached()) {
            if (! isValidString(pid)) {
                int i = ProcessUtils.getPid();
                if (i == -1) {
                    ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(localStrings.getLocalString("attach.agent.exception",
                        "invalid pid, pl. provide the application server's pid using --pid option, you may get pid using jps command"));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                } else {
                    pid = String.valueOf(i);
                }
            }
            if (isValidString(pid)) {
                FlashlightProbeClientMediator.setAgentInitialized(false);
                attachAgent(report);
            }
        }

        // following ordering is deliberate to facilitate config change
        // event handling by monitoring infrastructure
        // for ex. by the time monitoring-enabled is true all its constituent
        // elements should have been set to avoid multiple processing

        // module monitoring levels
        if ((modules != null) && (modules.length() > 0)) {
            String[] strArr = modules.split(":"); 
            String[] nvArr = null;
            for (String nv: strArr) { 
                if (nv.length() > 0) {
                    nvArr = nv.split("=");
                    if (nvArr.length > 1) {
                        if (isValidString(nvArr[1])) {
                            setModuleMonitoringLevel(nvArr[0], nvArr[1], report);
                        }
                    } else {
                        if (isValidString(nvArr[0])) {
                            setModuleMonitoringLevel(nvArr[0], "HIGH", report);
                        }
                    }
                }
            } 
        }

        // mbean-enabled
        if (mbean != null) {
            MonitoringConfig.setMBeanEnabled(ms, mbean.toString(), report);
        }

        // dtrace-enabled
        if (dtrace != null) {
            MonitoringConfig.setDTraceEnabled(ms, dtrace.toString(), report);
        }

        // check and set monitoring-enabled to true
        MonitoringConfig.setMonitoringEnabled(ms, "true", report);
    }

    private void attachAgent(ActionReport report) {
        ActionReport.MessagePart part = report.getTopMessagePart().addChild();
        if (options == null) {
            options = "unsafe=true,noServer=true";
        } else {
            options = "unsafe=true,noServer=true"+","+options;
        }
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            String ir = System.getProperty(INSTALL_ROOT_PROPERTY);
            File dir = new File(ir, "lib" + File.separator + "monitor");
            if (dir.isDirectory()) {
                File agentJar = new File(dir, "btrace-agent.jar");
                if (agentJar.isFile()) {
                    if (options == null) {
                        vm.loadAgent(agentJar.getPath());
                    } else {
                        vm.loadAgent(agentJar.getPath(), options);
                    }
                    part.setMessage(localStrings.getLocalString("attach.agent.suucess",
                        "btrace agent attached"));
                    report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                } else {
                    part.setMessage(localStrings.getLocalString("attach.agent.exception",
                        "btrace-agent.jar does not exist under {0}", dir));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                }
            } else {
                part.setMessage(localStrings.getLocalString("attach.agent.exception",
                    "btrace-agent.jar directory {0} does not exist", dir));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
        } catch (Exception e) {
            part.setMessage(localStrings.getLocalString("attach.agent.exception",
                "Encountered exception during agent attach {0}", e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

    private void setModuleMonitoringLevel(String moduleName, String level, ActionReport report) {
        ActionReport.MessagePart part = report.getTopMessagePart().addChild();

        if (! isValidString(moduleName)) {
            part.setMessage(localStrings.getLocalString("enable.monitoring.invalid",
                "Invalid module name {0}", moduleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        if ((!isValidString(level)) || (!isValidLevel(level))) {
            part.setMessage(localStrings.getLocalString("enable.monitoring.invalid",
                "Invalid level {0} for module name {1}", level, moduleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        MonitoringConfig.setMonitoringLevel(ms, moduleName, level, report);
    }

    private boolean isValidString(String str) {
        return (str!=null && str.length()>0);
    }

    private boolean isValidLevel(String level) {
        return ((level.equals("OFF")) || (level.equals("HIGH")) || (level.equals("LOW")));
    }
}
