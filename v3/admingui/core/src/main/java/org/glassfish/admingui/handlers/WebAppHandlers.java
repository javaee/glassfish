
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

/*
 * WebAppHandler.java
 *
 * Created on August 10, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author anilam
 */
package org.glassfish.admingui.handlers;

import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TreeSet;
import javax.enterprise.deploy.spi.Target;
import javax.management.Attribute;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.AppUtil;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.V3AMXUtil;
import org.glassfish.deployment.client.DeploymentFacility;

public class WebAppHandlers {

    /** Creates a new instance of ApplicationsHandler */
    public WebAppHandlers() {
    }


    @Handler(id = "getApplicationEnabled",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "enabled", type = String.class)})
    public static void getApplicationEnabled(HandlerContext handlerCtx) {

        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        boolean enable = AppUtil.isApplicationEnabled(objectNameStr);
        handlerCtx.setOutputValue("enabled", Boolean.toString(enable));
    }


    @Handler(id = "showContextRoot",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "value", type = Boolean.class)})
    public static void showContextRoot(HandlerContext handlerCtx) {

            // If this is a ear file,  or context-root is not specified, do not show the context root.
        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        String isEar = V3AMX.getPropValue(V3AMX.objectNameToProxy(objectNameStr), "isComposite" );
        if (isEar == null || !isEar.equals("true")){
            handlerCtx.setOutputValue("value", Boolean.TRUE);
        }else{
            handlerCtx.setOutputValue("value", Boolean.FALSE);
        }
    }



    /**
     *	<p> This handler save  the values for all the attributes of the Application
     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "saveApplicationInfo",
        input = {
            @HandlerInput(name = "appAttr", type = Map.class, required = true),
            @HandlerInput(name = "appRefAttr", type = Map.class, required = true),
            @HandlerInput(name = "appObjectName", type = String.class, required = true),
            @HandlerInput(name = "appRefObjectName", type = String.class, required = true)
        })
    public static void saveApplicationInfo(HandlerContext handlerCtx) {
        Map appAttr = (Map) handlerCtx.getInputValue("appAttr");
        Map appRefAttr = (Map) handlerCtx.getInputValue("appRefAttr");
        String appObjectName = (String) handlerCtx.getInputValue("appObjectName");
        String appRefObjectName = (String) handlerCtx.getInputValue("appRefObjectName");
        V3AMX.setAttribute(appObjectName, new Attribute("ContextRoot", appAttr.get("ContextRoot")));
        V3AMX.setAttribute(appObjectName, new Attribute("Description", appAttr.get("Description")));
        String enStr = (String) appAttr.get("Enabled");
        if (enStr == null )
            enStr = "false";
        V3AMX.setAttribute(appRefObjectName, new Attribute("Enabled", enStr));
        V3AMX.setAttribute(appRefObjectName, new Attribute("VirtualServers", appRefAttr.get("VirtualServers")));
    }


    /**
     *	<p> This handler returns the list of applications for populating the table.
     *  <p> Input  value: "serverName" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getSubComponents",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)})
    public static void getSubComponents(HandlerContext handlerCtx) {
        List result = new ArrayList();
        String appName = (String) handlerCtx.getInputValue("appName");
        AMXProxy applications = V3AMX.getInstance().getApplications();
        Map<String, AMXProxy> modules = applications.childrenMap("application").get(appName).childrenMap("module");
        for(AMXProxy oneModule: modules.values()){
            Map oneRow = new HashMap();
            List<String> snifferList = AppUtil.getSnifferListOfModule(oneModule);
            oneRow.put("componentName", oneModule.getName());
            oneRow.put("engines", snifferList.toString());
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
    }


    /**
     *	<p> This handler returns the list of applications for populating the table.
     *  <p> Input  value: "serverName" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getDeployedAppsInfo",
        input = {
            @HandlerInput(name = "serverName", type = String.class, required = true),
            @HandlerInput(name = "filterValue", type = String.class)},
        output = {
            @HandlerOutput(name = "filters", type = java.util.List.class),
            @HandlerOutput(name = "result", type = java.util.List.class)})
    public static void getDeployedAppsInfo(HandlerContext handlerCtx) {
        String serverName = (String) handlerCtx.getInputValue("serverName");
        String filterValue = (String) handlerCtx.getInputValue("filterValue");
        Set filters = new TreeSet();
        filters.add("");
        if (GuiUtil.isEmpty(filterValue))
            filterValue = null;
        List result = new ArrayList();
        AMXProxy applications = V3AMX.getInstance().getApplications();
        Map<String, AMXProxy> application = applications.childrenMap("application");
        eachApp:  for (AMXProxy oneApp : application.values()) {
            HashMap oneRow = new HashMap();
            oneRow.put("name", oneApp.getName());
            oneRow.put("selected", false);
            boolean enable = AppUtil.isApplicationEnabled(oneApp);
            String enableURL= (enable)? "/resource/images/enabled.png" : "/resource/images/disabled.png";
            oneRow.put("enableURL", enableURL);
            List sniffersList = AppUtil.getAllSniffers(oneApp);
            oneRow.put("sniffersList", sniffersList);
            oneRow.put("sniffers", sniffersList.toString());
            for(int ix=0; ix< sniffersList.size(); ix++)
                filters.add(sniffersList.get(ix));
            if (filterValue != null){
                if (! sniffersList.contains(filterValue))
                    continue;
            }
            getLaunchInfo(serverName, oneApp, oneRow);
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("filters", new ArrayList(filters));
    }

    
    private static void getLaunchInfo(String serverName, AMXProxy oneApp,  Map oneRow) {
        Map<String, Object> attrs = oneApp.attributesMap();
        String contextRoot = (String) attrs.get("ContextRoot");
        if (contextRoot == null){
            contextRoot = "";
        }
        boolean enabled = AppUtil.isApplicationEnabled(oneApp);
        oneRow.put("contextRoot", contextRoot);
        oneRow.put("hasLaunch", false);
        //for now, we only allow launch for enabled standalone war file with context root specified in domain.xml
        if ( !enabled || contextRoot.equals("")){
            return;
        }
        
        String protocol = "http";
        String port = V3AMXUtil.getPortForApplication( (String) attrs.get("Name"));
        if (port == null) {
            oneRow.put("port", "");
            oneRow.put("hasLaunch", false);
        } else {
            if (port.startsWith("-")) {
                protocol = "https";
                port = port.substring(1);
            }
            oneRow.put("port", port);
            oneRow.put("hasLaunch", true);
            String ctxRoot = calContextRoot(contextRoot);
            oneRow.put("launchLink", protocol + "://" + serverName + ":" + port + ctxRoot);
        }
    }

   @Handler(id = "restartApplication",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true)
        })
    // TODO: This may have issues with multiple VS/targets.  See DeploymentHandler.changeAppStatus()
    public static void restartApplication(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        String[] targetNames = new String[]{"server"};
        try {
            DeploymentFacility df = GuiUtil.getDeploymentFacility();
            Target[] targets = df.createTargets(targetNames);
            df.disable(targets, appName);
            df.enable(targets, appName);
            // Mimic behavior in DeploymentHandler.changeAppStatus
            if (V3AMX.getInstance().isEE()) {
                GuiUtil.prepareAlert(handlerCtx, "success", GuiUtil.getMessage("org.glassfish.web.admingui.Strings", "restart.success"), null);
            } else {
                GuiUtil.prepareAlert(handlerCtx, "success", GuiUtil.getMessage("org.glassfish.web.admingui.Strings", "restart.successPE"), null);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }

    }

    private static String calContextRoot(String contextRoot) {
        //If context root is not specified or if the context root is "/", ensure that we don't show two // at the end.
        //refer to issue#2853
        String ctxRoot = "";
        if ((contextRoot == null) || contextRoot.equals("") || contextRoot.equals("/")) {
            ctxRoot = "/";
        } else if (contextRoot.startsWith("/")) {
            ctxRoot = contextRoot;
        } else {
            ctxRoot = "/" + contextRoot;
        }
        return ctxRoot;
    }

  
}
