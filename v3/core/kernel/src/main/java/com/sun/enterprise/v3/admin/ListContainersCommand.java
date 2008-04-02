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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.v3.data.ContainerInfo;
import com.sun.enterprise.v3.data.ContainerRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.impl.ModuleImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

/**
 * This admin command list the containers currentely running within that
 * Glassfish instance
 */
@Service(name="list-containers")
@I18n("list.containers.command")
public class ListContainersCommand implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListContainersCommand.class);

    @Inject
    ContainerRegistry containerRegistry;

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    Habitat habitat;

    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();
        report.setActionDescription(localStrings.getLocalString("list.containers.command", "List of Containers"));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage(localStrings.getLocalString("list.containers.command", "List of Containers"));
        top.setChildrenType(localStrings.getLocalString("container", "Container"));

        Iterable<? extends Sniffer> sniffers = habitat.getAllByContract(Sniffer.class);
        if (sniffers ==null) {
            top.setMessage(localStrings.getLocalString("list.containers.nocontainer",
                    "No container currently configured"));
        } else {
            for (Sniffer sniffer : sniffers) {
                ActionReport.MessagePart container = top.addChild();
                container.setMessage(sniffer.getModuleType());
                container.addProperty(localStrings.getLocalString("contractprovider", "ContractProvider"),
                        sniffer.getModuleType());
                ContainerInfo containerInfo = containerRegistry.getContainer(sniffer.getModuleType());

                if (containerInfo != null) {
                    container.addProperty(
                            localStrings.getLocalString("status", "Status"),
                            localStrings.getLocalString("started", "Started"));
                    Module connectorModule = modulesRegistry.find(containerInfo.getSniffer().getClass());
                    container.addProperty(localStrings.getLocalString("connector", "Connector"),
                            connectorModule.getModuleDefinition().getName() +
                            ":" + connectorModule.getModuleDefinition().getVersion());
                    container.addProperty(localStrings.getLocalString("implementation", "Implementation"),
                            containerInfo.getContainer().getClass().toString());
                    Iterable<ApplicationInfo> apps = containerInfo.getApplications();
                    if (apps.iterator().hasNext()) {

                        container.setChildrenType(localStrings.getLocalString("list.containers.listapps",
                                "Applications deployed"));
                        for (ApplicationInfo info : apps) {
                            container.addChild().setMessage(info.getName());
                        }
                    } else {
                        container.addProperty("Status", "Not Started");
                    }
                }
            }
        }
    }
}
