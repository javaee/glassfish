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

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleState;

import java.net.URI;

/**
 * List the modules available to this instance and their status
 */
@Service(name="list-modules")
@I18n("list.modules.command")
public class ListModulesCommand implements AdminCommand {

    @Inject
    ModulesRegistry modulesRegistry;

    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();
        report.setActionDescription("List of modules");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("List Of Modules");
        top.setChildrenType("Module");
        for(Module module : modulesRegistry.getModules()) {

            ActionReport.MessagePart childPart = top.addChild();
            String version = module.getModuleDefinition().getVersion();
            if (version==null) {
                version = "any";

            }
            
            childPart.setMessage(module.getModuleDefinition().getName() +
                ":" + version);

            childPart.addProperty("State", module.getState().toString());
            if (module.isSticky()) {
                childPart.addProperty("Sticky", "true");
            }
            if (!module.isShared()) {
                childPart.addProperty("visibility", "private");
            } else {
                childPart.addProperty("visibility", "public");
            }
            
            if (module.getState().equals(ModuleState.READY)) {
                childPart.setChildrenType("Module Characteristics");
                ActionReport.MessagePart provides = childPart.addChild();
                provides.setMessage("Provides to following services");
                provides.setChildrenType("Provides");

                /*for (URL info : module.getServiceProviders().getDescriptors()) {
                    provides.addChild().setMessage(info.toString());
                } */

                ActionReport.MessagePart imports = childPart.addChild();
                imports.setMessage("List of imported modules");
                imports.setChildrenType("Imports");
                for (Module i : module.getImports()) {
                    String importVersion = i.getModuleDefinition().getVersion();
                    if (importVersion==null) {
                        importVersion="any";
                    }
                    imports.addChild().setMessage(i.getModuleDefinition().getName() + ":" + importVersion);
                }

                ActionReport.MessagePart implementations = childPart.addChild();
                implementations.setMessage("List of Jars implementing the module");
                implementations.setChildrenType("Jar");
                for (URI location : module.getModuleDefinition().getLocations()) {
                    implementations.addChild().setMessage(location.toString());
                }
            }
        }
    }
}
