
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
package org.glassfish.admingui.common.handlers;

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
import javax.management.Attribute;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.intf.config.Application;
import org.glassfish.admin.amx.intf.config.ApplicationRef;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.AppUtil;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.V3AMXUtil;
import org.glassfish.admin.amx.intf.config.Property;
import org.glassfish.admin.amx.intf.config.VirtualServer;
import org.glassfish.admingui.common.util.DeployUtil;
import org.glassfish.deployment.client.DFDeploymentProperties;

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
        String isEar = V3AMX.getPropValue(V3AMX.objectNameToProxy(objectNameStr), DFDeploymentProperties.IS_COMPOSITE);
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
        Map<String, AMXProxy> modules = V3AMX.getInstance().getApplication(appName).childrenMap("module");
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
        Map<String, AMXProxy> application = V3AMX.getInstance().getApplications().childrenMap("application");
        for (AMXProxy oneApp : application.values()) {
            if(V3AMX.getPropValue(oneApp, DFDeploymentProperties.IS_LIFECYCLE) != null){
                continue;   //we don't want to display lifecycle.
            }
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


    /*
     * Get the application type for the specified appName.
     * If there isComposite property is true, the appType will be returned as 'ear'
     * Otherwise, depends on the sniffer engine
     */
    @Handler(id = "getApplicationType",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "appType", type = String.class)})
    public static void getApplicationType(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        Application application = V3AMX.getInstance().getApplication(appName);
        String appType = V3AMX.getPropValue(application, "isComposite");
        if ("true".equals(appType)){
            appType = "ear";
        }else{
            List sniffersList = AppUtil.getAllSniffers(application);
            if (sniffersList.contains("connector")){
                appType="rar";
            }else
            if (sniffersList.contains("web")){
                appType="war";
            }else
            if (sniffersList.contains("appclient")){
                appType="appclient";
            }else
                //For the case like "jruby", "jython", "ejb jar" etc.  Those should only have 1 sniffer engine.
                appType = (String) sniffersList.get(0);
        }
        handlerCtx.setOutputValue("appType", appType);
    }

    /**
     *	<p> This handler returns the list of lifecycles for populating the table.
     *  <p> Input  value: "serverName" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getLifecyclesInfo",
        input = {
            @HandlerInput(name = "serverName", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)})
    public static void getLifecyclesInfo(HandlerContext handlerCtx) {
        List result = new ArrayList();
        Map<String, AMXProxy> application = V3AMX.getInstance().getApplications().childrenMap("application");
        for (AMXProxy oneApp : application.values()) {
            if(V3AMX.getPropValue(oneApp, DFDeploymentProperties.IS_LIFECYCLE) == null){
                continue;   //we only want lifecycle.
            }
            HashMap oneRow = new HashMap();
            oneRow.put("Name", oneApp.getName());
            oneRow.put("selected", false);
            boolean enable = AppUtil.isApplicationEnabled(oneApp);
            String enableURL= (enable)? "/resource/images/enabled.png" : "/resource/images/disabled.png";
            oneRow.put("enableURL", enableURL);
            final String className = V3AMX.getPropValue(oneApp, DFDeploymentProperties.CLASS_NAME);
            oneRow.put("className", (className == null) ? "" : className);
            final String order = V3AMX.getPropValue(oneApp, DFDeploymentProperties.LOAD_ORDER);
            oneRow.put("loadOrder", (order == null) ? "" : order);
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
    }

     /**
     *	<p> This handler returns the list of lifecycles for populating the table.
     *  <p> Input  value: "serverName" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getLifecycleAttrEdit",
    input = {
        @HandlerInput(name = "Name", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "attrMap", type = Map.class),
        @HandlerOutput(name = "properties", type = List.class)})
    public static void getLifecycleAttrEdit(HandlerContext handlerCtx) {

        String appName = (String) handlerCtx.getInputValue("Name");
        Application app = (Application) V3AMX.getInstance().getApplication(appName);
        Map<String, Property> origProps = app.getProperty();

        Map attrMap = new HashMap();
        attrMap.put("Name", appName);
        attrMap.put(DFDeploymentProperties.CLASS_NAME, getA(origProps, DFDeploymentProperties.CLASS_NAME));
        attrMap.put(DFDeploymentProperties.CLASSPATH, getA(origProps, DFDeploymentProperties.CLASSPATH));
        attrMap.put(DFDeploymentProperties.LOAD_ORDER, getA(origProps, DFDeploymentProperties.LOAD_ORDER));
        String fatal = getA(origProps, DFDeploymentProperties.IS_FAILURE_FATAL);
        attrMap.put(DFDeploymentProperties.IS_FAILURE_FATAL, (fatal.equals("") ? "false" : "true") );

        List props = V3AMX.getChildrenMapForTableList(app, "property",  skipLifecyclePropsList);
        handlerCtx.setOutputValue("attrMap", attrMap);
        handlerCtx.setOutputValue("properties", props);
    }

    @Handler(id = "saveLifecycle",
    input = {
        @HandlerInput(name = "attrMap", type = Map.class, required = true),
        @HandlerInput(name = "attrMap2", type = Map.class, required = true),
        @HandlerInput(name = "propList", type = List.class, required = true),
        @HandlerInput(name = "edit", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "attrMap", type = Map.class),
        @HandlerOutput(name = "properties", type = List.class)})
    public static void saveLifecycle(HandlerContext handlerCtx) {

        String edit = (String) handlerCtx.getInputValue("edit");
        Map attrMap = (Map) handlerCtx.getInputValue("attrMap");
        Map attrMap2 = (Map) handlerCtx.getInputValue("attrMap2");
        List propList = (List) handlerCtx.getInputValue("propList");
        String name = (String) attrMap.get("Name");
        Map aMap = new HashMap();
        aMap.put("Name", name);
        aMap.put("Enabled", (attrMap2.get("Enabled") == null)? "false" : "true");
        aMap.put("Description", attrMap.get("Description"));

        attrMap.put(DFDeploymentProperties.IS_LIFECYCLE, "true");
        SecurityHandler.putOptional(attrMap, propList, DFDeploymentProperties.IS_LIFECYCLE, DFDeploymentProperties.IS_LIFECYCLE);
        SecurityHandler.putOptional(attrMap, propList, DFDeploymentProperties.CLASS_NAME, DFDeploymentProperties.CLASS_NAME);
        SecurityHandler.putOptional(attrMap, propList, DFDeploymentProperties.CLASSPATH, DFDeploymentProperties.CLASSPATH);
        SecurityHandler.putOptional(attrMap, propList, DFDeploymentProperties.LOAD_ORDER, DFDeploymentProperties.LOAD_ORDER);
        if (attrMap2.get(DFDeploymentProperties.IS_FAILURE_FATAL) != null){
            SecurityHandler.putOptional(attrMap, propList, DFDeploymentProperties.IS_FAILURE_FATAL, DFDeploymentProperties.IS_FAILURE_FATAL);
        }

        Attribute vsAttr = null;
        //do not send VS if user didn't specify, refer to bug#6542276
        String[] vs = (String[]) attrMap2.get("selectedVS");
        if (vs != null && vs.length > 0 && (!vs[0].equals(""))) {
            if (!GuiUtil.isEmpty(vs[0])) {
                String vsTargets = GuiUtil.arrayToString(vs, ",");
                vsAttr = new Attribute("VirtualServers", vsTargets);
            }
        }else{
            Set<AMXProxy> vsSet = V3AMX.getInstance().getDomainRoot().getQueryMgr().queryType("virtual-server");
            List vsList = new ArrayList();
            for(AMXProxy oneVs: vsSet){
                vsList.add(oneVs.getName());
            }
            vsList.remove("__asadmin");
            vsAttr = new Attribute("VirtualServers", GuiUtil.listToString(vsList, ","));
        }


        if (edit.equals("true")){
            AMXConfigProxy app = V3AMX.getInstance().getApplication(name);
            V3AMX.setAttributes(app.objectName(), aMap);
            V3AMX.setProperties(app.objectName().toString(), propList, false);
            AMXConfigProxy appRef = V3AMX.getInstance().getApplicationRef("server", name);
            V3AMX.setAttribute(appRef.objectName(), vsAttr);
            String enable = (String)attrMap2.get("Enabled");
            V3AMX.setAttribute(appRef.objectName(), new Attribute("Enabled", (enable==null)? "false" : "true"));

        }else{
            AMXConfigProxy apps = (AMXConfigProxy) V3AMX.getInstance().getApplications();
            List pList = V3AMX.verifyPropertyList(propList);
            Map[] propMaps = (Map[])pList.toArray(new Map[pList.size()]);
            aMap.put("object-type", "user");
            aMap.put(Util.deduceType(Property.class), propMaps);
            apps.createChild("application", aMap);
            AMXConfigProxy server = V3AMX.getInstance().getDomain().getServers().getServer().get("server");
            attrMap2.put("Name", name);
            if (attrMap2.get("Enabled") == null){
                attrMap2.put("Enabled", "false");
            }
            attrMap2.remove("selectedVS");
            attrMap2.put("VirtualServers", vsAttr.getValue());
            server.createChild("application-ref", attrMap2);
            //don't want to change the attrMap2 value, in case there is error and the create page need to be refreshed.
            attrMap2.put("selectedVS" , vs);
        }
    }


    @Handler(id = "changeLifecycleStatus",
    input = {
        @HandlerInput(name = "selectedRows", type = List.class, required = true),
        @HandlerInput(name = "enabled", type = Boolean.class, required = true)
    })
    public static void changeLifecycleStatus(HandlerContext handlerCtx) {

        List obj = (List) handlerCtx.getInputValue("selectedRows");
        boolean enabled = ((Boolean) handlerCtx.getInputValue("enabled")).booleanValue();
        String status = (enabled) ? "true" : "false";
        List selectedRows = (List) obj;
        try {
            for (int i = 0; i < selectedRows.size(); i++) {
                Map oneRow = (Map) selectedRows.get(i);
                String appName = (String) oneRow.get("Name");
                V3AMX.getInstance().getApplicationRef("server", appName).setEnabled(status);
                V3AMX.getInstance().getApplication(appName).setEnabled(status);
                String msg = GuiUtil.getMessage((enabled) ? "msg.enableSuccessfulLifecyce" : "msg.disableSuccessfulLifecycle");
                GuiUtil.prepareAlert(handlerCtx, "success", msg, null);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }




    private static String getA(Map<String, Property> attrs, String key) {
        Property val = attrs.get(key);
        return (val == null) ? "" :  val.getValue();
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
    public static void restartApplication(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        try{
            DeployUtil.restartApplication(appName);
            if (V3AMX.getInstance().isEE()) {
                GuiUtil.prepareAlert(handlerCtx, "success", GuiUtil.getMessage("org.glassfish.web.admingui.Strings", "restart.success"), null);
            } else {
                GuiUtil.prepareAlert(handlerCtx, "success", GuiUtil.getMessage("org.glassfish.web.admingui.Strings", "restart.successPE"), null);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }

    }

   //This is called when user change the default web module of a VS.
   //Need to ensure this VS is in the application-ref virtual server list. If not add it and restart the app for
   //change to take into effect.  refer to issue#8671
   @Handler(id = "EnsureDefaultWebModule",
        input = {
            @HandlerInput(name = "vsObjStr", type = String.class, required = true),
            @HandlerInput(name = "vsName", type = String.class, required = true)
        })
    public static void EnsureDefaultWebModule(HandlerContext handlerCtx) {
        String vsObjStr = (String) handlerCtx.getInputValue("vsObjStr");
        String webModule= (String) V3AMX.getAttribute(vsObjStr, "DefaultWebModule");
        String vsName = (String) V3AMX.getAttribute(vsObjStr, "Name");
        if (GuiUtil.isEmpty(webModule))
            return;
        String appName = webModule;
        int index = webModule.indexOf("#");
        if (index != -1){
            appName=webModule.substring(0, index);
        }
        ApplicationRef appRef = V3AMX.getInstance().getApplicationRef("server", appName);
        String vsStr = appRef.getVirtualServers();
        List vsList = GuiUtil.parseStringList(vsStr, ",");
        if (vsList.contains(vsName)){
            return;   //the default web module app is already deployed to this vs, no action needed
        }
        //Add to the vs list of this application-ref, then restart the app.
        vsStr=vsStr+","+vsName;
        appRef.setVirtualServers(vsStr);
        try{
            DeployUtil.restartApplication(appName);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
   }

   //getVsForDeployment(result="#{pageSession.vsList}");
   @Handler(id = "getVsForDeployment",
        output = {
        @HandlerOutput(name = "result", type = List.class)})
    public static void getVsForDeployment(HandlerContext handlerCtx) {
        Set vsSet = V3AMX.getInstance().getConfig("server-config").getHttpService().getVirtualServer().keySet();
        vsSet.remove("__asadmin");
        handlerCtx.setOutputValue("result", new ArrayList(vsSet));
   }

   //This handler is called after user deleted one more more VS from the VS table.
   //We need to go through all the application-ref to see if the VS specified still exist.  If it doesn't, we need to
   //remove that from the vs list.
   @Handler(id = "checkVsOfAppRef")
    public static void checkVsOfAppRef(HandlerContext handlerCtx) {
       Map<String, ApplicationRef> appRefMap = V3AMX.getInstance().getServer("server").getApplicationRef();
       Map<String, VirtualServer> vsMap = V3AMX.getInstance().getConfig("server-config").getHttpService().getVirtualServer();
       for(ApplicationRef appRef: appRefMap.values()){
           String vsStr = appRef.getVirtualServers();
           List<String> vsList = GuiUtil.parseStringList(vsStr, ",");
           boolean changed = false;
           String newVS = "";
           for(String oneVs: vsList ){
               if (! vsMap.containsKey(oneVs)){
                   changed = true;
                   continue;
               }
               newVS = newVS+","+oneVs;
           }
           if (changed){
               newVS = newVS.substring(1);
               appRef.setVirtualServers(newVS);
           }
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

   private static List skipLifecyclePropsList = new ArrayList();
    static {
        skipLifecyclePropsList.add(DFDeploymentProperties.CLASS_NAME);
        skipLifecyclePropsList.add(DFDeploymentProperties.CLASSPATH);
        skipLifecyclePropsList.add(DFDeploymentProperties.LOAD_ORDER);
        skipLifecyclePropsList.add(DFDeploymentProperties.IS_FAILURE_FATAL);
        skipLifecyclePropsList.add(DFDeploymentProperties.IS_LIFECYCLE);
    }
  
}
