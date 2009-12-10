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
import org.glassfish.api.admin.config.ApplicationName;
import org.glassfish.api.Param;
import org.glassfish.api.I18n;
import org.glassfish.internal.deployment.Deployment;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EjbEntityDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
import org.jvnet.hk2.annotations.Service;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * list-sub-components command
 */
@Service(name="list-sub-components")
@I18n("list.sub.components")
@Scoped(PerLookup.class)
public class ListSubComponentsCommand implements AdminCommand {

    @Param(primary=true)
    private String modulename = null;

    @Param(optional=true)
    private String appname = null;

    @Param(optional=true)
    private String type = null;

    @Inject
    public ApplicationRegistry appRegistry;

    @Inject
    public Deployment deployment;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListSubComponentsCommand.class);    

    public void execute(AdminCommandContext context) {
        
        final ActionReport report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();        

        String applicationName = modulename; 
        if (appname != null) {
            applicationName = appname;
        }

        if (!deployment.isRegistered(applicationName)) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", applicationName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }

        ApplicationName module = ConfigBeansUtilities.getModule(applicationName);
  
        Application application = null;
        if (module instanceof Application) {
            application = (Application) module;
        }

        ApplicationInfo appInfo = appRegistry.get(applicationName);
        if (appInfo == null) {
            report.setMessage(localStrings.getLocalString("application.not.enabled","Application {0} is not in an enabled state", applicationName));
            return;
        }

        com.sun.enterprise.deployment.Application app = appInfo.getMetaData(com.sun.enterprise.deployment.Application.class);

        List<String> subComponents = new ArrayList<String>();    

        if (appname == null) {
            subComponents = getAppLevelComponents(app, type);
        } else {
           subComponents = getModuleLevelComponents(
               app.getModuleByUri(modulename), type);
        }
        
        // the type param can only have values "ejbs" and "servlets"
        if (type != null)  {
            if (!type.equals("servlets") && !type.equals("ejbs")) {
                report.setMessage(localStrings.getLocalString("listsubcomponents.invalidtype", "The type option has invalid value {0}. It should have a value of servlets or ejbs.", type));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        List<String> subModuleInfos = new ArrayList<String>();    
        if (!app.isVirtual()) {
            subModuleInfos = getSubModulesForEar(app);
        }

        for (int i = 0; i < subComponents.size(); i++) {
            ActionReport.MessagePart childPart = part.addChild();
            childPart.setMessage(subComponents.get(i));
            if (appname == null && !app.isVirtual()) {
                // we use the property mechanism to provide 
                // support for JSR88 client
                if (subModuleInfos.get(i) != null) {
                    childPart.addProperty("moduleInfo", 
                        subModuleInfos.get(i));
                }
            }
        }

        if (subComponents.size() == 0) {
            part.setMessage(localStrings.getLocalString("listsubcomponents.no.elements.to.list", "Nothing to List."));
        }

        // now this is the normal output for the list-sub-components command
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

    private List<String> getAppLevelComponents(com.sun.enterprise.deployment.Application application, String type) {
        List<String> subComponentList = new ArrayList<String>(); 
        if (application.isVirtual()) {
            // for standalone module, get servlets or ejbs
            BundleDescriptor bundleDescriptor = 
                application.getStandaloneBundleDescriptor();
            subComponentList = getModuleLevelComponents(bundleDescriptor, type);
        } else {
            // for ear case, get modules
            Collection<ModuleDescriptor<BundleDescriptor>> modules = 
                new ArrayList<ModuleDescriptor<BundleDescriptor>>();
            if (type == null) {
                modules = application.getModules();
            } else if (type.equals("servlets")) {
                modules = application.getModuleDescriptorsByType(
                    XModuleType.WAR);
            } else if (type.equals("ejbs")) {    
                modules = application.getModuleDescriptorsByType(
                    XModuleType.EJB);
                // ejb in war case
                Collection<ModuleDescriptor<BundleDescriptor>> webModules = 
                    application.getModuleDescriptorsByType(XModuleType.WAR);
                for (ModuleDescriptor webModule : webModules) {
                    if (webModule.getDescriptor().getExtensionsDescriptors(EjbBundleDescriptor.class).size() > 0) {
                        modules.add(webModule);
                    }
                }
            }
 
            for (ModuleDescriptor module : modules) {
                StringBuffer sb = new StringBuffer();    
                sb.append(module.getArchiveUri()); 
                sb.append(" <"); 
                sb.append(getModuleType(module));
                sb.append(">"); 
                subComponentList.add(sb.toString());    
            }
        }
        return subComponentList;
    }

    private List<String> getModuleLevelComponents(BundleDescriptor bundle, 
        String type) {
        List<String> moduleSubComponentList = new ArrayList<String>(); 
        if (bundle instanceof WebBundleDescriptor) {
            WebBundleDescriptor wbd = (WebBundleDescriptor)bundle;
            // look at ejb in war case
            Collection<EjbBundleDescriptor> ejbBundleDescs = 
                wbd.getExtensionsDescriptors(EjbBundleDescriptor.class);
            if (ejbBundleDescs.size() > 0) {
                EjbBundleDescriptor ejbBundle = 
                        ejbBundleDescs.iterator().next();
                moduleSubComponentList.addAll(getModuleLevelComponents(
                        ejbBundle, type));
            }

            if (type != null && type.equals("ejbs")) {    
                return moduleSubComponentList;
            }
            for (WebComponentDescriptor wcd : 
                    wbd.getWebComponentDescriptors()) {
                StringBuffer sb = new StringBuffer();    
                sb.append(wcd.getCanonicalName()); 
                sb.append(" <"); 
                String wcdType = (wcd.isServlet() ? "Servlet" : "JSP");
                sb.append(wcdType);
                sb.append(">"); 
                moduleSubComponentList.add(sb.toString());
            }
        } else if (bundle instanceof EjbBundleDescriptor)  {
            if (type != null && type.equals("servlets")) {    
                return moduleSubComponentList;
            }
            EjbBundleDescriptor ebd = (EjbBundleDescriptor)bundle;
            for (EjbDescriptor ejbDesc : ebd.getEjbs()) {
                StringBuffer sb = new StringBuffer();    
                sb.append(ejbDesc.getName()); 
                sb.append(" <"); 
                sb.append(getEjbType(ejbDesc));
                sb.append(">"); 
                moduleSubComponentList.add(sb.toString());
            }
        }

        return moduleSubComponentList;
    }


    private String getEjbType(EjbDescriptor ejbDesc) {
        String type = null;
        if (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE)) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor)ejbDesc;
            if (sessionDesc.isStateful()) {
                type = "StatefulSessionBean";
            } else if (sessionDesc.isStateless()) {
                type = "StatelessSessionBean";
            } else if (sessionDesc.isSingleton()) {
                type = "SingletonSessionBean";
            }
        } else if (ejbDesc.getType().equals(EjbMessageBeanDescriptor.TYPE)) {
            type = "MessageDrivenBean";
        } else if (ejbDesc.getType().equals(EjbEntityDescriptor.TYPE)) {
            type = "EntityBean";
        }

        return type;
    }

    private String getModuleType(ModuleDescriptor modDesc) {
        String type = null;
        if (modDesc.getModuleType().equals(XModuleType.EJB)) {
            type = "EJBModule";
        } else if (modDesc.getModuleType().equals(XModuleType.WAR)) {
            type = "WebModule";
        } else if (modDesc.getModuleType().equals(XModuleType.CAR)) {
            type = "AppClientModule";
        } else if (modDesc.getModuleType().equals(XModuleType.CAR)) {
            type = "ConnectorModule";
        }

        return type;
    }
}
