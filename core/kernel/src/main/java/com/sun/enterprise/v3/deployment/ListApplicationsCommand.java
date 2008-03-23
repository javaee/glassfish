
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.deployment;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import com.sun.enterprise.v3.data.ContainerRegistry;
import com.sun.enterprise.v3.data.ContainerInfo;
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.impl.ModuleImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

/**
 *
 * @author Jerome Dochez
 */
@Service(name="list-applications")
public class ListApplicationsCommand implements AdminCommand {

    @Inject
    ContainerRegistry containerRegistry;

    public void execute(AdminCommandContext context) {
 
        ActionReport report = context.getActionReport();
        report.setActionDescription("List of Deployed Applications");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        StringBuffer buffer = new StringBuffer();
        ActionReport.MessagePart part = report.getTopMessagePart();
        part.setMessage("List of deployed applications per container");
        part.setChildrenType("ContractProvider");
        for (ContainerInfo containerInfo : containerRegistry.getContainers()) {
            ActionReport.MessagePart containerChild = part.addChild();
            containerChild.setMessage(containerInfo.getSniffer().getModuleType() + " ContractProvider");
            Module connectorModule = containerInfo.getMainModule();          
            containerChild.addProperty("Connector module", connectorModule.getModuleDefinition().getName());
            containerChild.addProperty("Sniffer module", containerInfo.getSniffer().getClass().toString());
            containerChild.setChildrenType("Application");
            Iterable<ApplicationInfo> apps  = containerInfo.getApplications();
            for (ApplicationInfo info : apps) {
                ActionReport.MessagePart appPart = containerChild.addChild();
                appPart.setMessage(info.getName());
                appPart.getProps().put("Source", info.getSource().getURI());
            }
        }
    }
    
}
