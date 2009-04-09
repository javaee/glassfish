/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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

package org.glassfish.javaee.core.deployment;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.Param;
import org.glassfish.api.I18n;
import org.glassfish.internal.deployment.Deployment;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
import org.jvnet.hk2.annotations.Service;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import java.util.List;
import java.util.ArrayList;

/**
 * list-sub-components command
 */
@Service(name="list-sub-components")
@I18n("list.sub.components")
@Scoped(PerLookup.class)
public class ListSubComponentsCommand implements AdminCommand {

    @Param(primary=true)
    private String appName = null;

    @Inject
    public ApplicationRegistry appRegistry;

    @Inject
    public Deployment deployment;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListSubComponentsCommand.class);    

    public void execute(AdminCommandContext context) {
        
        final ActionReport report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();        

        if (!deployment.isRegistered(appName)) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", appName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }

        Named module = ConfigBeansUtilities.getModule(appName);
  
        Application application = null;
        if (module instanceof Application) {
            application = (Application) module;
        }

        ApplicationInfo appInfo = appRegistry.get(appName);
        com.sun.enterprise.deployment.Application app = appInfo.getMetaData(com.sun.enterprise.deployment.Application.class);

        // TODO: we need to make output conform to v2 output
        // the impl now is only to provide support for JSR88
         
        if (Boolean.valueOf(application.getDeployProperties(
            ).getProperty("isComposite"))) {
            for (String modInfo : getSubModulesForEar(app)) {
                ActionReport.MessagePart childPart = part.addChild();
                childPart.setMessage(modInfo);
            }
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    // list sub components for ear
    private List<String> getSubModulesForEar(com.sun.enterprise.deployment.Application application) {
        List<String> moduleInfoList = new ArrayList<String>();
        for (ModuleDescriptor moduleDesc : application.getModules()) { 
            String moduleInfo = moduleDesc.getArchiveUri() + ":" + 
                moduleDesc.getModuleType(); 
             if (moduleDesc.getModuleType().equals(XModuleType.WAR)) {
                 moduleInfo = moduleInfo + ":" + moduleDesc.getContextRoot(); 
             }
             moduleInfoList.add(moduleInfo);
        }
        return moduleInfoList;
    }
}
