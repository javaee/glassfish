
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
 * ApplicationsHandler.java
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

//import com.sun.appserv.management.config.AppClientModuleConfig;
//import com.sun.appserv.management.config.ApplicationConfig;
//import com.sun.appserv.management.config.ClusterConfig;
//import com.sun.appserv.management.config.ClusteredServerConfig;
//import com.sun.appserv.management.config.CustomMBeanConfig;
//import com.sun.appserv.management.config.DeployedItemRefConfig;
//import com.sun.appserv.management.config.DeployedItemRefConfigCR;
//import com.sun.appserv.management.config.EJBModuleConfig;
//import com.sun.appserv.management.config.Enabled;
//import com.sun.appserv.management.config.HTTPListenerConfig;
//import com.sun.appserv.management.config.HTTPServiceConfig;
//import com.sun.appserv.management.config.J2EEApplicationConfig;
//import com.sun.appserv.management.config.LifecycleModuleConfig;
//import com.sun.appserv.management.config.AbstractModuleConfig;
//import com.sun.appserv.management.config.ObjectType;
//import com.sun.appserv.management.config.ObjectTypeValues;
//import com.sun.appserv.management.config.RARModuleConfig;
//import com.sun.appserv.management.config.StandaloneServerConfig;
//import com.sun.appserv.management.config.VirtualServerConfig;
//import com.sun.appserv.management.config.WebModuleConfig;
//import com.sun.appserv.management.j2ee.J2EEServer;
//import com.sun.appserv.management.j2ee.StateManageable;
//
//import com.sun.jsftemplating.annotation.HandlerInput;
//import com.sun.jsftemplating.annotation.HandlerOutput;
//import com.sun.jsftemplating.annotation.Handler;
//import com.sun.jsftemplating.component.dataprovider.MultipleListDataProvider;
//import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
//
//import com.sun.webui.jsf.component.TableRowGroup;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.StringTokenizer;
//
//import javax.management.Attribute;
//import javax.management.AttributeList;
//import javax.management.ObjectName;
//
//import org.glassfish.admingui.common.util.AMXRoot;
//import org.glassfish.admingui.common.util.AMXUtil;
//import org.glassfish.admingui.common.util.GuiUtil;
//import org.glassfish.admingui.common.util.TargetUtil;
//
//
public class ApplicationHandlers {
    /** Creates a new instance of ApplicationsHandler */
    public ApplicationHandlers() {
    }
    
//    /**
//     *	<p> This handler returns the values for all the attributes of the Application
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
//     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getApplicationInfo",
//        input={
//            @HandlerInput(name="name", type=String.class, required=true),
//            @HandlerInput(name="appType", type=String.class, required=true)},
//        output={
//            @HandlerOutput(name="location", type=String.class),
//            @HandlerOutput(name="libraries", type=String.class),
//            @HandlerOutput(name="contextRoot", type=String.class),
//            @HandlerOutput(name="vs", type=String.class),
//            @HandlerOutput(name="description", type=String.class),
//            @HandlerOutput(name="threadPool", type=String.class),
//            @HandlerOutput(name="availEnabled", type=Boolean.class),
//            @HandlerOutput(name="javaWebStart", type=Boolean.class),
//            @HandlerOutput(name="enabledString", type=String.class),
//            @HandlerOutput(name="enabled", type=Boolean.class)} )
//    public static void getApplicationInfo(HandlerContext handlerCtx) {
//        
//        String name = (String) handlerCtx.getInputValue("name");
//        String appType = (String) handlerCtx.getInputValue("appType");
//	AbstractModuleConfig module = null;
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        
//        /*
//         * TODO-V3  need to revisit when more than web app is supported.
//         * 
//	if ("application".equals(appType)){
//	    module = amxRoot.getApplicationsConfig().getJ2EEApplicationConfigMap().get(name);
//            if (module != null){
//                J2EEApplicationConfig j2eeApp = (J2EEApplicationConfig) module;
//		//handlerCtx.setOutputValue("javaWebStart", j2eeApp.getJavaWebStartEnabled());
//		//handlerCtx.setOutputValue("availEnabled", j2eeApp.getAvailabilityEnabled());
//		if(!amxRoot.supportCluster()) {
//			//We need this only for PE, so hard code it "server"
//			handlerCtx.setOutputValue("vs", TargetUtil.getAssociatedVS(name, "server"));
//		}
//                handlerCtx.setOutputValue("contextRoot", j2eeApp.getContextRoot());
//	    }
//	}else
//	if ("ejbModule".equals(appType)){
//            module = amxRoot.getApplicationsConfig().getEJBModuleConfigMap().get(name);
//            if (module != null){
//                EJBModuleConfig ejbModule = (EJBModuleConfig) module;
//		handlerCtx.setOutputValue("availEnabled", ejbModule.getAvailabilityEnabled());
//	    }
//	}else
//	if ("webApp".equals(appType)){
//            module = amxRoot.getApplicationsConfig().getWebModuleConfigMap().get(name);
//	    if (module != null){
//                WebModuleConfig webModule = (WebModuleConfig) module;
//		handlerCtx.setOutputValue("contextRoot", webModule.getContextRoot());
//		handlerCtx.setOutputValue("availEnabled", webModule.getAvailabilityEnabled());
//		if(!amxRoot.supportCluster()) {
//			//We need this only for PE, so hard code it "server"
//			handlerCtx.setOutputValue("vs", TargetUtil.getAssociatedVS(name, "server"));
//		}
//	    }
//	}else
//	if ("connector".equals(appType)){
//            module = amxRoot.getApplicationsConfig().getRARModuleConfigMap().get(name);
//	    if (module != null){
//		ResourceAdapterConfig adapter = amxRoot.getApplicationsConfig().getResourceAdapterConfigMap().get(name);
//		if (adapter != null)
//		    handlerCtx.setOutputValue("threadPool", adapter.getThreadPoolIDs());
//	    }
//	}
//	if (module == null){
//	    //TODO: log error
//	    return;
//	}
//        */
//        
//        //No need to test the type for TP2
//        ApplicationConfig appConfig = amxRoot.getApplicationsConfig().getApplicationConfigMap().get(name);
//        handlerCtx.setOutputValue("contextRoot", appConfig.getContextRoot());
//	//handlerCtx.setOutputValue("availEnabled", appConfig.getAvailabilityEnabled());
//        if(!amxRoot.supportCluster()) {
//            //We need this only for PE, so hard code it "server"
//            handlerCtx.setOutputValue("vs", TargetUtil.getAssociatedVS(name, "server"));
//        }
//
//        //TODo-V3 revisit.  was using module instead of appConfig in v2
//	handlerCtx.setOutputValue("location", appConfig.getLocation());
//	handlerCtx.setOutputValue("description", appConfig.getDescription());
//	
//        if(amxRoot.isEE())
//            handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(appConfig, true));
//        else
//            handlerCtx.setOutputValue("enabled", TargetUtil.isApplicationEnabled(appConfig, "server" ));
//        
//        if (!"connector".equals(appType)){
//            //String[] libArray = (String[]) ((Libraries)appConfig).getLibraries();
//            String[] libArray = new String[1];
//            libArray[0]="AMX-EXCEPTION";
//            
//            
//            if (libArray != null){
//                StringBuffer libs = new StringBuffer();
//                for(int i=0; i< libArray.length; i++){
//                    libs.append("<br/>");
//                    libs.append(libArray[i]);
//                }
//                if (libs.length() > 1){
//                    handlerCtx.setOutputValue("libraries", libs.substring(5));
//                }
//            }
//        }
//    }
//    
//    
//    
//    /**
//     *	<p> This handler save  the values for all the attributes of the Application
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    /*
//    @Handler(id="saveApplicationInfoV2",
//        input={
//            @HandlerInput(name="name", type=String.class, required=true),
//            @HandlerInput(name="appType", type=String.class, required=true),
//            @HandlerInput(name="description", type=String.class),
//            @HandlerInput(name="contextRoot", type=String.class),
//            @HandlerInput(name="vs", type=String.class),
//            @HandlerInput(name="javaWebStart", type=Boolean.class),
//            @HandlerInput(name="threadPool", type=String.class),
//            @HandlerInput(name="enabled", type=Boolean.class),
//            @HandlerInput(name="availEnabled", type=Boolean.class)
//        })
//    public static void saveApplicationInfoV2(HandlerContext handlerCtx) {
//
//        String target = "server";   //TODO: Fix for EE
//        String name = (String) handlerCtx.getInputValue("name");
//        String appType = (String) handlerCtx.getInputValue("appType");
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        
//        try{
//	AbstractModuleConfig module = null;
//	if ("application".equals(appType)){
//	    module = amxRoot.getApplicationsConfig().getJ2EEApplicationConfigMap().get(name);
//            if (module != null){
//                J2EEApplicationConfig j2eeApp = (J2EEApplicationConfig) module;
//		j2eeApp.setJavaWebStartEnabled((Boolean)handlerCtx.getInputValue("javaWebStart"));
//                if (amxRoot.isEE()){
//                    Boolean ae = (Boolean)handlerCtx.getInputValue("availEnabled");
//                    if (ae != null)
//                        j2eeApp.setAvailabilityEnabled(ae);
//                }
//		else {
//			String vs = (String)handlerCtx.getInputValue("vs");
//			//only for PE, so hard-code to 'server'
//			TargetUtil.setVirtualServers(name, "server", vs);
//		}
//                    
//	    }
//	}else
//	if ("ejbModule".equals(appType)){
//	    module = amxRoot.getApplicationsConfig().getEJBModuleConfigMap().get(name);
//            if (module != null){
//                EJBModuleConfig ejbModule = (EJBModuleConfig) module;
//                if (amxRoot.isEE()){
//                    Boolean ae = (Boolean)handlerCtx.getInputValue("availEnabled");
//                    if (ae != null)
//                        ejbModule.setAvailabilityEnabled(ae);
//                }
//	    }
//	}else
//	if ("webApp".equals(appType)){
//	    module = amxRoot.getApplicationsConfig().getWebModuleConfigMap().get(name);
//	    if (module != null){
//                WebModuleConfig webModule = (WebModuleConfig) module;
//		webModule.setContextRoot((String)handlerCtx.getInputValue("contextRoot"));
//                if (amxRoot.isEE()){
//                    Boolean ae = (Boolean)handlerCtx.getInputValue("availEnabled");
//                    if (ae != null)
//                        webModule.setAvailabilityEnabled(ae);
//                }
//		else {
//			String vs = (String)handlerCtx.getInputValue("vs");
//			//only for PE, so hard-code to 'server'
//			TargetUtil.setVirtualServers(name, "server", vs);
//		}
//	    }
//	}else
//	if ("connector".equals(appType)){
//	    module = amxRoot.getApplicationsConfig().getRARModuleConfigMap().get(name);
//	    if (module != null){
//                ResourceAdapterConfig adapter = amxRoot.getApplicationsConfig().getResourceAdapterConfigMap().get(name);
//		if (adapter == null)
//		    adapter = amxRoot.getResourcesConfig().createResourceAdapterConfig(name, null);
//                adapter.setThreadPoolIDs((String) handlerCtx.getInputValue("threadPool"));
//	    }
//	}
//	if (module == null){
//	    //TODO: log error
//	    return;
//	}
//
//        module.setDescription((String)handlerCtx.getInputValue("description"));
//        if(! amxRoot.isEE()){
//            Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
//            TargetUtil.setApplicationEnabled(module, "server", enabled); 
//        }
//        }catch(Exception ex){
//            ex.printStackTrace();
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }
//    
//    */
//
//    /**
//     *	<p> This handler save  the values for all the attributes of the Application
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="saveApplicationInfo",
//        input={
//            @HandlerInput(name="name", type=String.class, required=true),
//            @HandlerInput(name="appType", type=String.class, required=true),
//            @HandlerInput(name="description", type=String.class),
//            @HandlerInput(name="contextRoot", type=String.class),
//            @HandlerInput(name="vs", type=String.class),
//            @HandlerInput(name="javaWebStart", type=Boolean.class),
//            @HandlerInput(name="threadPool", type=String.class),
//            @HandlerInput(name="enabled", type=Boolean.class),
//            @HandlerInput(name="availEnabled", type=String.class)
//        })
//    public static void saveApplicationInfo(HandlerContext handlerCtx) {
//
//        String target = "server";   //TODO: Fix for EE
//        String name = (String) handlerCtx.getInputValue("name");
//        String appType = (String) handlerCtx.getInputValue("appType");
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        
//        try{
//	
//	    ApplicationConfig appConfig = amxRoot.getApplicationsConfig().getApplicationConfigMap().get(name);
//	    if (appConfig != null){
//		appConfig.setContextRoot((String)handlerCtx.getInputValue("contextRoot"));
//                if (amxRoot.isEE()){
//                    appConfig.setAvailabilityEnabled((String) handlerCtx.getInputValue("availEnabled"));
//                }
//		else {
//                    String vs = (String)handlerCtx.getInputValue("vs");
//                    //only for PE, so hard-code to 'server'
//                    TargetUtil.setVirtualServers(name, "server", vs);
//		}
//	    }
//        
//        appConfig.setDescription((String)handlerCtx.getInputValue("description"));
//        if(! amxRoot.isEE()){
//            Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
//            TargetUtil.setApplicationEnabled(appConfig, "server", enabled); 
//        }
//        }catch(Exception ex){
//            ex.printStackTrace();
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }
//
//
//    /**
//     *	<p> This handler returns the values for all the attributes of the AppClient
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
//     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    /*
//    @Handler(id="getAppClientInfo",
//        input={
//            @HandlerInput(name="name", type=String.class, required=true) },
//        output={
//            @HandlerOutput(name="location", type=String.class),
//            @HandlerOutput(name="description", type=String.class),
//            @HandlerOutput(name="javaWebStart", type=Boolean.class)
//	})
//    public static void getAppClientInfo(HandlerContext handlerCtx) {
//        
//        String name = (String) handlerCtx.getInputValue("name");
//	AppClientModuleConfig module = AMXRoot.getInstance().getApplicationsConfig().getAppClientModuleConfigMap().get(name);
//	if (module == null){
//            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchAppclient"));
//            return;
//	}
//	handlerCtx.setOutputValue("location", module.getLocation());
//	handlerCtx.setOutputValue("description", module.getDescription());
//	handlerCtx.setOutputValue("javaWebStart", module.getJavaWebStartEnabled());
//    }
//     * 
//     */
//    
//    /**
//     *	<p> This handler save  the values for all the attributes of the AppClient
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    /*
//    @Handler(id="saveAppClientInfo",
//        input={
//            @HandlerInput(name="name", type=String.class, required=true),
//            @HandlerInput(name="description", type=String.class),
//            @HandlerInput(name="javaWebStart", type=Boolean.class)
//        })
//    public static void saveAppClientInfo(HandlerContext handlerCtx) {
//        String name = (String) handlerCtx.getInputValue("name");
//	AppClientModuleConfig module = AMXRoot.getInstance().getApplicationsConfig().getAppClientModuleConfigMap().get(name);
//	if (module == null){
//	    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchAppclient"));
//	    return;
//	}
//	module.setJavaWebStartEnabled((Boolean)handlerCtx.getInputValue("javaWebStart"));
//        module.setDescription((String)handlerCtx.getInputValue("description"));
//    }
//     */
//
//
//    /**
//     *	<p> This handler returns the values for all the attributes of the lifecycle Module
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
//     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getLifecycleModuleInfo",
//        input={
//            @HandlerInput(name="name", type=String.class, required=true) },
//        output={
//            @HandlerOutput(name="classname", type=String.class),
//            @HandlerOutput(name="classpath", type=String.class),
//            @HandlerOutput(name="description", type=String.class),
//            @HandlerOutput(name="loadOrder", type=String.class),
//            @HandlerOutput(name="enabledString", type=String.class),
//            @HandlerOutput(name="isFailureFatal", type=Boolean.class),
//            @HandlerOutput(name="enabled", type=Boolean.class),
//            @HandlerOutput(name="properties", type=java.util.Map.class)
//	})
//    public static void getLifecycleModuleInfo(HandlerContext handlerCtx) {
//        
//        String name = (String) handlerCtx.getInputValue("name");
//	LifecycleModuleConfig module = AMXRoot.getInstance().getApplicationsConfig().getLifecycleModuleConfigMap().get(name);
//	if (module == null){
//	    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchLifecycle"));
//	    return;
//	}
//	handlerCtx.setOutputValue("classname", module.getClassname());
//        handlerCtx.setOutputValue("classpath", module.getClasspath());
//        handlerCtx.setOutputValue("loadOrder", module.getLoadOrder());
//	handlerCtx.setOutputValue("description", module.getDescription());
//	handlerCtx.setOutputValue("isFailureFatal", module.getIsFailureFatal());
//        if(AMXRoot.getInstance().isEE())
//            handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(module, true));
//        else
//            handlerCtx.setOutputValue("enabled", TargetUtil.isApplicationEnabled(module, "server" ));
//        handlerCtx.setOutputValue("properties", module.getPropertyConfigMap());     
//    }
//    
//    
//    /**
//     *	<p> This handler save  the values for all the attributes of the LifecycleModule
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="saveLifecycleModuleInfo",
//        input={
//            @HandlerInput(name="name", type=String.class, required=true),
//            @HandlerInput(name="description", type=String.class),
//            @HandlerInput(name="classname", type=String.class),
//            @HandlerInput(name="classpath", type=String.class),
//            @HandlerInput(name="loadOrder", type=String.class),
//            @HandlerInput(name="enabled", type=Boolean.class),
//            @HandlerInput(name="isFailureFatal", type=String.class),
//            @HandlerInput(name="AddProps",    type=Map.class),
//            @HandlerInput(name="RemoveProps", type=ArrayList.class)
//        })
//    public static void saveLifecycleModuleInfo(HandlerContext handlerCtx) {
//        String name = (String) handlerCtx.getInputValue("name");
//        AMXRoot amxRoot = AMXRoot.getInstance();
//	LifecycleModuleConfig module = amxRoot.getApplicationsConfig().getLifecycleModuleConfigMap().get(name);
//	
//        if (module == null){
//            GuiUtil.handleError(handlerCtx,  GuiUtil.getMessage("msg.NoSuchLifecycle"));
//	}
//        
//        module.setDescription((String)handlerCtx.getInputValue("description"));
//        module.setClassname((String)handlerCtx.getInputValue("classname"));
//        module.setClasspath((String)handlerCtx.getInputValue("classpath"));
//        module.setLoadOrder((String)handlerCtx.getInputValue("loadOrder"));
//        module.setIsFailureFatal((String)handlerCtx.getInputValue("isFailureFatal"));
//        if (! amxRoot.isEE()){
//            Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
//            TargetUtil.setApplicationEnabled(module, "server", enabled); 
//        }
//        amxRoot.editProperties(handlerCtx, module);
//    }
//    
//    /**
//     *	<p> This handler save  the values for all the attributes of the LifecycleModule
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="createLifecycleModule",
//        input={
//            @HandlerInput(name="name", type=String.class, required=true),
//            @HandlerInput(name="description", type=String.class),
//            @HandlerInput(name="classname", type=String.class),
//            @HandlerInput(name="classpath", type=String.class),
//            @HandlerInput(name="loadOrder", type=String.class),
//            @HandlerInput(name="enabled", type=Boolean.class),
//            @HandlerInput(name="isFailureFatal", type=Boolean.class),
//            @HandlerInput(name="targets", type=String[].class)
//        })
//        
//        public static void createLifecycleModule(HandlerContext handlerCtx) {
//        
//            /*  need to user JMX to create the lifecycle because AMX has a bug that throws an Exception, thus
//             * prevents creating the application-ref for the lifecycle.
//             * using JMX, the createLifecycleModule() method already creates the application-ref for 
//             * "server".
//             */
//            String name = (String)handlerCtx.getInputValue("name");
//            Boolean isFailureFatal = ((Boolean)handlerCtx.getInputValue("isFailureFatal"));
//            String fatal = (isFailureFatal == null) ? "false" : isFailureFatal.toString();
//            Boolean enabledB = ((Boolean)handlerCtx.getInputValue("enabled"));
//            String enabledStr = (enabledB == null) ? "false": enabledB.toString();
//            
//            AttributeList list = new AttributeList();
//            list.add(new Attribute("name", name));
//            list.add(new Attribute("class-name", (String)handlerCtx.getInputValue("classname")));
//            list.add(new Attribute("classpath", (String)handlerCtx.getInputValue("classpath")));
//            list.add(new Attribute("description", (String)handlerCtx.getInputValue("description")));
//            list.add(new Attribute("load-order", (String)handlerCtx.getInputValue("loadOrder")));
//            list.add(new Attribute("is-failure-fatal", fatal));
//            list.add(new Attribute("enabled", enabledStr));
//            
//        String[] types = new String[]{"javax.management.AttributeList", "java.lang.String"};
//        Object[] params = new Object[]{list, (AMXRoot.getInstance().isEE()? "domain": "server")};
//        try {
//            
//        /* TODO-V3
//         Object obj =  JMXUtil.invoke(
//            "com.sun.appserv:type=applications,category=config", 
//            "createLifecycleModule", params, types);
//         
//        String[] selTargets = (String[])handlerCtx.getInputValue("targets"); 
//        DeploymentHandler.handleAppRefs(name, selTargets, handlerCtx, true, Boolean.valueOf(enabledStr));
//        */
//            
//         /* was commented out in V2 alread. 
//         Enabled appConfig = getModuleConfig(name, "lifecycle");
//         
//         if (selTargets == null){
//             //for PE
//            TargetUtil.setApplicationEnabled(appConfig, "server", enabled);
//         }else{
//             for(int i=0; i < selTargets.length; i++){
//                 String targetName = selTargets[i];
//                 if (!targetName.equals("server"))
//                    TargetUtil.createDeployedItemRefObject(name, targetName);
//                 TargetUtil.setApplicationEnabled(appConfig, targetName, enabled);
//             }
//         }
//         
//          */
//         
//        }catch(Exception ex){
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }
//        
//    /*
//    public static void createLifecycleModuleWithAMX(HandlerContext handlerCtx) {
//        try {
//        String name = (String)handlerCtx.getInputValue("name");
//        Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
//        Boolean isFailureFatal = (Boolean) handlerCtx.getInputValue("isFailureFatal");
//        if (enabled == null) enabled = false;
//        if (isFailureFatal == null) isFailureFatal = false;
//	LifecycleModuleConfig module = AMXRoot.getInstance().getApplicationsConfig().createLifecycleModuleConfig(
//                name,
//                (String)handlerCtx.getInputValue("description"),
//                (String)handlerCtx.getInputValue("classname"),
//                (String)handlerCtx.getInputValue("classpath"),
//                (String)handlerCtx.getInputValue("loadOrder"),
//                isFailureFatal,
//                enabled,
//                null
//        );
//        TargetUtil.createDeployedItemRefObject(name, target);   //This causes an exception in AMX.
//        TargetUtil.setApplicationEnabled(module, target, enabled);
//        }catch(Exception ex){
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }
//     */
//    
//     /**
//     *	<p> This handler takes in selected rows, and do the deletion of lifecycle
//     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="deleteLifecycleModules",
//    input={
//        @HandlerInput(name="selectedRows", type=List.class, required=true)
//    })
//        
//    public static void deleteLifecycleModules(HandlerContext handlerCtx) {
//        Object obj = handlerCtx.getInputValue("selectedRows");
//        List selectedRows = (List) obj;
//        try{
//            String[] types = new String[]{"java.lang.String", "java.lang.String"};
//            String defaultTarget = (AMXRoot.getInstance().isEE() ? "domain": "server");
//            for(int i=0; i< selectedRows.size(); i++){
//                Map oneRow = (Map) selectedRows.get(i);
//                String name = (String)oneRow.get("name");
//                LifecycleModuleConfig module = AMXRoot.getInstance().getApplicationsConfig().getLifecycleModuleConfigMap().get(name);
//                deleteLifecycleReferences(module);
//                Object[] params = new Object[]{ name , defaultTarget};
//                
//                /* TODO-V3
//                JMXUtil.invoke(
//                    "com.sun.appserv:type=applications,category=config",
//                    "removeLifecycleModuleByName", 
//                    params, types);
//                 
//                //To ensure that AMX sync up with config mbeans so that it won't return the objec that has been deleted
//                 Thread.sleep(1000);
//                 * 
//                 */
//            }
//        }catch(Exception ex){
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }
//    
//    
//     private static void deleteLifecycleReferences(LifecycleModuleConfig lm){
//        if (!AMXRoot.getInstance().isEE()) { return; }
//        String appName = lm.getName();
//        List<String>  targetList = TargetUtil.getDeployedTargets(lm, true);
//        /* TODO-V3
//        for(String target:  targetList){
//            JMXUtil.invoke(
//                "com.sun.appserv:type=applications,category=config",
//                "removeLifecycleModuleReference", 
//                new Object[]{appName, target},
//                new String[]{"java.lang.String", "java.lang.String"});
//        }
//        */
//    }
//    
//    /**
//     *	<p> This handler returns a list of Maps for populating the sub component table.
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *  <p> Input  value: "appType" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getSubComponents",
//        input={
//            @HandlerInput(name="appName", type=String.class, required=true),
//            @HandlerInput(name="appType", type=String.class, required=true)},
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//    public static void getSubComponents(HandlerContext handlerCtx){
//        
//        handlerCtx.setOutputValue("result", new ArrayList());
//            return;
//        /** TODO-V3    
//        
//        String appName = (String) handlerCtx.getInputValue("appName");
//        String appType = (String) handlerCtx.getInputValue("appType");
//        boolean hasAction = false;
//        List result = new ArrayList();
//        if ("connector".equals(appType)){
//            handlerCtx.setOutputValue("result", result);
//            return;
//        }else
//        if ("application".equals(appType)){
//            hasAction = true;
//        }
//        try {
//            String[] types = new String[]{"java.lang.String"};
//            Object[] params = new Object[]{appName}; 
//	    String[] modules= (String[]) JMXUtil.invoke(
//            "com.sun.appserv:type=applications,category=config", 
//            "getModuleComponents", params, types);
//        
//            for(int i = 0; i < modules.length; i++) {
//                HashMap oneRow = new HashMap();
//                ObjectName on = new ObjectName(modules[i]);
//                //Get the display field names from XML file
//                oneRow.put("componentName", on.getKeyProperty("name"));
//                oneRow.put("componentType", on.getKeyProperty("j2eeType"));
//                if(hasAction){
//                    if ("AppClientModule".equals(on.getKeyProperty("j2eeType"))){
//                        oneRow.put("downloadText", GuiUtil.getMessage("ComponentTable.downloadClientStub"));
//                        J2EEApplicationConfig appConfig = AMXRoot.getInstance().getApplicationsConfig().getJ2EEApplicationConfigMap().get(appName);
//                        boolean javaWebStart = appConfig.getJavaWebStartEnabled();
//                        oneRow.put("javaWebStart", javaWebStart);
//                    }else{
//                        oneRow.put("javaWebStart", false);
//                        oneRow.put("downloadText", " ");
//                    }
//                }
//                result.add(oneRow);
//            }
//            handlerCtx.setOutputValue("result", result);
//	} catch (Exception ex) {
//	    GuiUtil.handleException(handlerCtx, ex);
//	}
//         */
//    }
//    
//    
//    /**
//     *	<p> This handler returns the list of enterprise applications for populating the table.
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getDeployedAppInfo",
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//    public static void getDeployedAppInfo(HandlerContext handlerCtx){
//        
//        Iterator<J2EEApplicationConfig> iter = AMXRoot.getInstance().getApplicationsConfig().getJ2EEApplicationConfigMap().values().iterator();
//        List result = new ArrayList();
//        while(iter.hasNext()){
//            J2EEApplicationConfig appConfig = iter.next();
//            if (ObjectTypeValues.USER.equals(appConfig.getObjectType())){
//                HashMap oneRow = new HashMap();
//                oneRow.put("name", appConfig.getName());
//                oneRow.put("enabled", TargetUtil.getEnabledStatus(appConfig, true));
//                oneRow.put("location", appConfig.getLocation());
//                oneRow.put("selected", false);
//                oneRow.put("hasLaunch", false);
//                result.add(oneRow);
//            }
//        }
//        handlerCtx.setOutputValue("result", result);
//    }
//    
//    /**
//     *	<p> This handler returns the list of web applications for populating the table.
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getDeployedWebInfo",
//        input={
//            @HandlerInput(name="serverName", type=String.class, required=true)},
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//    public static void getDeployedWebInfo(HandlerContext handlerCtx){
//        
//        String serverName = (String) handlerCtx.getInputValue("serverName");
//        Map<String, ApplicationConfig> webAppsConfig = AMXUtil.getApplicationConfigByType("web");
//        List result = new ArrayList();
//        for(ApplicationConfig appConfig : webAppsConfig.values()){
//            if (ObjectTypeValues.USER.equals(appConfig.getObjectType())){
//                HashMap oneRow = new HashMap();
//                String protocol = "http" ;
//                String enable =  TargetUtil.getEnabledStatus(appConfig, true);
//                oneRow.put("name", appConfig.getName());
//                oneRow.put("enabled", enable);
//                String contextRoot = appConfig.getContextRoot();
//                oneRow.put("contextRoot", contextRoot);
//                String port = getPortForApplication(appConfig.getName());
//                if (port.startsWith("-") ){
//                    protocol="https";
//                    port = port.substring(1);
//                }
//                oneRow.put("port", port);
//                if(AMXRoot.getInstance().isEE()){
//                    if (enable.equals(GuiUtil.getMessage("deploy.allDisabled")) ||
//                            enable.equals(GuiUtil.getMessage("deploy.noTarget")))
//                        oneRow.put("hasLaunch", false);
//                    else
//                        oneRow.put("hasLaunch", true);
//                }else{
//                    oneRow.put("hasLaunch", Boolean.parseBoolean(enable) );
//                    String ctxRoot = calContextRoot(contextRoot);
//                    oneRow.put("launchLink", protocol+"://"+serverName+":"+ port + ctxRoot);
//                }
//                oneRow.put("selected", false);
//                //List<String> targets = TargetUtil.getDeployedTargets(appConfig, true);
//                result.add(oneRow);
//            }
//        }
//        handlerCtx.setOutputValue("result", result);
//    }
//    
//    /**
//     *	<p> This handler returns the list of Ejb Module for populating the table.
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getDeployedEJBModuleInfo",
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//    public static void getDeployedEJBModuleInfo(HandlerContext handlerCtx){
//        
//        Iterator<EJBModuleConfig> iter = AMXRoot.getInstance().getApplicationsConfig().getEJBModuleConfigMap().values().iterator();
//        List result = new ArrayList();
//        while(iter.hasNext()){
//            EJBModuleConfig appConfig = iter.next();
//            if (ObjectTypeValues.USER.equals(appConfig.getObjectType())){
//                HashMap oneRow = new HashMap();
//                oneRow.put("name", appConfig.getName());
//                oneRow.put("enabled", TargetUtil.getEnabledStatus(appConfig, true));
//                oneRow.put("location", appConfig.getLocation());
//                oneRow.put("selected", false);
//                oneRow.put("hasLaunch", false);
//                result.add(oneRow);
//            }
//        }
//        handlerCtx.setOutputValue("result", result);
//    }
//    
//    
//        /**
//     *	<p> This handler returns the list of Connector Module for populating the table.
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getDeployedConnectorModuleInfo",
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//    public static void getDeployedConnectorModuleInfo(HandlerContext handlerCtx){
//        
//        Iterator<RARModuleConfig> iter = AMXRoot.getInstance().getApplicationsConfig().getRARModuleConfigMap().values().iterator();
//        List result = new ArrayList();
//        while(iter.hasNext()){
//            RARModuleConfig appConfig = iter.next();
//            if (ObjectTypeValues.USER.equals(appConfig.getObjectType())){
//                HashMap oneRow = new HashMap();
//                oneRow.put("name", appConfig.getName());
//                oneRow.put("enabled", TargetUtil.getEnabledStatus(appConfig, true));
//                oneRow.put("location", appConfig.getLocation());
//                oneRow.put("selected", false);
//                oneRow.put("hasLaunch", false);
//                result.add(oneRow);
//            }
//        }
//        handlerCtx.setOutputValue("result", result);
//    }
//    
//    
//    /**
//     *	<p> This handler returns the list of Connector Module for populating the table.
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getDeployedLifecycleInfo",
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//    public static void getDeployedLifecycleInfo(HandlerContext handlerCtx){
//        
//        Iterator<LifecycleModuleConfig> iter = AMXRoot.getInstance().getApplicationsConfig().getLifecycleModuleConfigMap().values().iterator();
//        List result = new ArrayList();
//        while(iter.hasNext()){
//            LifecycleModuleConfig appConfig = iter.next();
//            try{
//              if (ObjectTypeValues.USER.equals(appConfig.getObjectType())){
//                HashMap oneRow = new HashMap();
//                oneRow.put("name", appConfig.getName());
//                oneRow.put("enabled", TargetUtil.getEnabledStatus(appConfig, true));
//                oneRow.put("loadOrder", GuiUtil.notNull(appConfig.getLoadOrder()));
//                oneRow.put("selected", false);
//                oneRow.put("hasLaunch", false);
//                result.add(oneRow);
//              }
//            }catch(Exception ex){
//                //TODO log exception at FINE level
//            }
//        }
//        handlerCtx.setOutputValue("result", result);
//    }
//    
//    /**
//     *	<p> This handler returns the list of AppClient Module for populating the table.
//     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getDeployedAppClientInfo",
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//    public static void getDeployedAppClientInfo(HandlerContext handlerCtx){
//        
//        Iterator<AppClientModuleConfig> iter = AMXRoot.getInstance().getApplicationsConfig().getAppClientModuleConfigMap().values().iterator();
//        List result = new ArrayList();
//        while(iter.hasNext()){
//            AppClientModuleConfig appConfig = iter.next();
//            HashMap oneRow = new HashMap();
//            oneRow.put("name", appConfig.getName());
//            oneRow.put("selected",false);
//            boolean javaWebStart = "true".equalsIgnoreCase( appConfig.getJavaWebStartEnabled());
//            if (javaWebStart){
//                oneRow.put("javaWebStart", "true");
//                oneRow.put("hasLaunch", true);
//            }else{
//                oneRow.put("javaWebStart", "false");
//                oneRow.put("hasLaunch", false);
//            }
//            result.add(oneRow);
//        }
//        handlerCtx.setOutputValue("result", result);
//    }
//    
//    
//   /**
//     *	<p> This handler returns the list of ClusteredInstances for populating the table.
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getTargetStatus",
//           input={
//            @HandlerInput(name="appName", type=String.class, required=true),
//            @HandlerInput(name="forApp", type=Boolean.class, required=true)},
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//     public static void getAppTargetStatus(HandlerContext handlerCtx){
//        
//        String appName = (String) handlerCtx.getInputValue("appName");
//        Boolean forApp = (Boolean) handlerCtx.getInputValue("forApp");
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        List result = new ArrayList();
//        try{
//            List<String>targetList = TargetUtil.getDeployedTargets(appName, forApp.booleanValue());
//            for(String target : targetList){
//                HashMap oneRow = new HashMap();
//                StandaloneServerConfig server = amxRoot.getServersConfig().getStandaloneServerConfigMap().get(target);
//                if (server != null){
//                    oneRow.put("target", target);
//                    oneRow.put("image", amxRoot.getStatusForDisplay(
//                            amxRoot.getJ2EEDomain().getJ2EEServerMap().get(target) , false));
//                    oneRow.put("targetURL", "/standalone/standaloneInstanceGeneral.jsf?instanceName="+target);
//                }else{
//                    //for cluster, we show the running symbol as long as there is one instance in that cluster
//                    // is running.
//                    oneRow.put("target", target);
//                    oneRow.put("image", getClusterStatus(target));
//                    oneRow.put("targetURL", "/cluster/clusterGeneral.jsf?clusterName="+target);
//                }
//                result.add(oneRow);
//            }
//            handlerCtx.setOutputValue("result", result);
//        }catch(Exception ex){
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }
//    
//    /* returns the status image for the cluster.  The image will show running as long as there
//     * is one instance running in that cluster.
//     */
//    private static String getClusterStatus(String clusterName){
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        ClusterConfig cluster = amxRoot.getClustersConfig().getClusterConfigMap().get(clusterName);
//        if (cluster == null) return "";
//        Map<String,ClusteredServerConfig> serverMap = cluster.getClusteredServerConfigMap();
//        if (serverMap.size() == 0) return "";
//        for(String instance : serverMap.keySet()){
//            J2EEServer j2eeServer = amxRoot.getJ2EEDomain().getJ2EEServerMap().get(instance);
//            if(j2eeServer != null){
//                int state = j2eeServer.getstate();
//                if (state == StateManageable.STATE_RUNNING){
//                    return amxRoot.getStatusForDisplay(j2eeServer, false);
//                }
//            }
//        }
//        return amxRoot.getStatusImage(StateManageable.STATE_STOPPED);
//    }
//   
//    /**
//     *	<p> This handler returns the list of targets for populating the target table.
//     *  <p> Input  value: "appName" -- Type: <code> java.lang.String</code></p>
//     *  <p> Input  value: "appType" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getApplicationTargetTableList",
//        input={
//            @HandlerInput(name="appName", type=String.class, required=true),
//            @HandlerInput(name="appType", type=String.class, required=true)},
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//    public static void getApplicationTargetTableList(HandlerContext handlerCtx){
//        
//        String appName = (String)handlerCtx.getInputValue("appName");
//        String appType = (String)handlerCtx.getInputValue("appType");
//        List<String> targetList = TargetUtil.getDeployedTargets(appName, true);
//        List result = new ArrayList();
//        for(String target:  targetList){
//            HashMap oneRow = new HashMap();
//            oneRow.put("selected", false);
//            oneRow.put("name", appName);
//            oneRow.put("targetName",target);
//            Enabled module = getModuleConfig(appName, appType);
//            if(module != null){  //appclients do not have enabled/lb-enabled attribute
//                oneRow.put("enabled", Boolean.toString(TargetUtil.isApplicationEnabled(module, target, false)));
//                oneRow.put("lbEnabled", Boolean.toString(TargetUtil.isApplicationEnabled(module, target, true)));
//                oneRow.put("vsLinkArgs", "?appName="+appName+"&targetName="+target);
//            }
//            result.add(oneRow);
//        }
//        handlerCtx.setOutputValue("result", result);
//    }
//    
//    /**
//     *	<p> This handler returns the list of targets and their status for 
//     *  populating the table.
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getTargetsTableList",
//           input={
//            @HandlerInput(name="appName", type=String.class, required=true),
//            @HandlerInput(name="forApp", type=Boolean.class, required=true),
//            @HandlerInput(name="appType", type=String.class, required=true)},
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class)}
//     )
//     public static void getTargetsTableList(HandlerContext handlerCtx){
//        String appName = (String) handlerCtx.getInputValue("appName");
//        Boolean forApp = (Boolean) handlerCtx.getInputValue("forApp");
//        String appType = (String)handlerCtx.getInputValue("appType");
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        List result = new ArrayList();
//        try{
//            List<String>targetList = TargetUtil.getDeployedTargets(appName, forApp.booleanValue());
//            for(String target : targetList){
//                HashMap oneRow = new HashMap();
//                StandaloneServerConfig server = amxRoot.getServersConfig().getStandaloneServerConfigMap().get(target);
//                Enabled module = getModuleConfig(appName, appType);
//                oneRow.put("selected", false);
//                oneRow.put("enabled", Boolean.toString(TargetUtil.isApplicationEnabled(module, target)));
//                if (server != null){
//                    oneRow.put("targetName", target);
//                    oneRow.put("image", amxRoot.getStatusForDisplay(
//                            amxRoot.getJ2EEDomain().getJ2EEServerMap().get(target) , false));
//                    oneRow.put("targetURL", "/standalone/standaloneInstanceGeneral.jsf?instanceName="+target);
//                }else{
//                    //for cluster, we show the running symbol as long as there is one instance in that cluster
//                    // is running.
//                    oneRow.put("targetName", target);
//                    oneRow.put("image", getClusterStatus(target));
//                    oneRow.put("targetURL", "/cluster/clusterGeneral.jsf?clusterName="+target);
//                }
//                    
//                result.add(oneRow);
//            }
//            handlerCtx.setOutputValue("result", result);
//        }catch(Exception ex){
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }
//    
//    /**
//     *	<p> This handler takes in selected rows, and change the status of the app
//     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
//     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
//     *  <p> Input  value: "enabled" -- Type: <code>Boolean</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="changeTargetStatus",
//    input={
//        @HandlerInput(name="selectedRows", type=List.class, required=true),
//        @HandlerInput(name="appType", type=String.class, required=true),
//        @HandlerInput(name="appName", type=String.class, required=true),
//        @HandlerInput(name="LB", type=Boolean.class),
//        @HandlerInput(name="enabled", type=Boolean.class, required=true)})
//        
//    public static void changeTargetStatus(HandlerContext handlerCtx) {
//        
//        //appType can be one of the following: application,webApp,ejbModule,connector
//        String appType = (String)handlerCtx.getInputValue("appType");
//        String appName = (String)handlerCtx.getInputValue("appName");
//        Enabled appConfig = getModuleConfig(appName, appType);
//        if(appConfig == null){
//            //Can't find the deployed app, don't do anything, except maybe log it in server.log
//            return;
//        }
//        
//        List obj = (List) handlerCtx.getInputValue("selectedRows");
//        boolean enabled = ((Boolean)handlerCtx.getInputValue("enabled")).booleanValue();
//        Boolean LB = (Boolean)handlerCtx.getInputValue("LB");
//        boolean forLB = (LB == null) ? false : LB.booleanValue();
//        
//        List selectedRows = (List) obj;
//        try{
//            for(int i=0; i< selectedRows.size(); i++){
//                Map oneRow = (Map) selectedRows.get(i);
//                String target = (String) oneRow.get("targetName");
//                TargetUtil.setApplicationEnabled(appConfig, target, enabled, forLB);
//            }
//        }catch(Exception ex){
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }
//    
//     /**
//     *	<p> This handler returns the list of application deployed to the specified target.
//     *     The target should be the name of a standalone server instance or cluster 
//     *  <p> Input  value: "target" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getAppRefListForTarget",
//        input={
//            @HandlerInput(name="target", type=String.class, required=true),
//            @HandlerInput(name="filterValue", type=String.class),
//            @HandlerInput(name="isServer", type=Boolean.class, required=true)},
//        output={
//            @HandlerOutput(name="result", type=java.util.List.class),
//            @HandlerOutput(name="hasLB", type=Boolean.class)}
//     )
//    public static void getAppRefListForTarget(HandlerContext handlerCtx){
//        String target = (String)handlerCtx.getInputValue("target");
//        String filterValue = (String)handlerCtx.getInputValue("filterValue");
//        boolean isServer = ((Boolean)handlerCtx.getInputValue("isServer")).booleanValue();
//        Collection <DeployedItemRefConfig> refs = new ArrayList();
//        List result = new ArrayList();
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        
//        if (isServer){
//            StandaloneServerConfig server = amxRoot.getServersConfig().getStandaloneServerConfigMap().get(target);
//            refs = server.getDeployedItemRefConfigMap().values();
//            //TODO-V3
//            //Map lbMap =  amxRoot.getLBConfigHelper().getLoadBalancers(target, false);
//            //handlerCtx.setOutputValue("hasLB", !lbMap.isEmpty());
//        }else{
//            ClusterConfig cluster = amxRoot.getClustersConfig().getClusterConfigMap().get(target);
//            refs = cluster.getDeployedItemRefConfigMap().values();
//            //TODO-V3
//            //Map lbMap =  amxRoot.getLBConfigHelper().getLoadBalancers(target, true);
//            //handlerCtx.setOutputValue("hasLB", !lbMap.isEmpty());
//        }
//        
//        for(DeployedItemRefConfig refObject : refs){
//            String appName = refObject.getName();
//            String appType = amxRoot.getAppType(appName);
//            
//            if(includeAppRef(appName, appType, filterValue)){
//                Map oneRow = new HashMap();
//                if(appType.equals(AppClientModuleConfig.J2EE_TYPE)){
//                    oneRow.put("enabled", "");
//                    oneRow.put("lbEnabled", "");
//                    oneRow.put("timeout", "");
//                }else{
//                    oneRow.put("enabled", refObject.getEnabled());
//                    oneRow.put("lbEnabled", refObject.getLBEnabled());
//                    oneRow.put("timeout", refObject.getDisableTimeoutInMinutes());
//                }
//                oneRow.put("name", appName);
//                oneRow.put("selected", false);
//                oneRow.put("link", "/applications/"+editMap.get(appType)+"?appName="+appName);
//                oneRow.put("appType",typeMap.get(appType));
//                oneRow.put("objectName", ""+com.sun.appserv.management.base.Util.getObjectName(refObject));
//                result.add(oneRow);
//            }
//        }
//            
//        handlerCtx.setOutputValue("result", result);
//    }
//    
//    
//    public static String getNumberLBAppsByTarget(String target){
//        Collection <DeployedItemRefConfig> refs = new ArrayList();
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        
//        if (TargetUtil.isCluster(target)){
//            ClusterConfig cluster = amxRoot.getClustersConfig().getClusterConfigMap().get(target);
//            refs = cluster.getDeployedItemRefConfigMap().values();
//        }else {
//            StandaloneServerConfig server = amxRoot.getServersConfig().getStandaloneServerConfigMap().get(target);
//            refs= server.getDeployedItemRefConfigMap().values();
//        }
//        
//        int totalEnabled = 0;
//        int totalCount = 0;
//        for(DeployedItemRefConfig refObject : refs){
//            String appName = refObject.getName();
//            String appType = amxRoot.getAppType(appName);
//            
//            if(includeAppRef(appName, appType, null)){
//                if ("true".equalsIgnoreCase(refObject.getLBEnabled()))
//                    totalEnabled++;
//                totalCount++;
//            }
//        }
//        
//        if (totalCount == 0) return GuiUtil.getMessage("loadBalancer.noDeployedApp");
//        return GuiUtil.getMessage("loadBalancer.numLBEnabled", new Object[]{""+totalEnabled, ""+totalCount});
//    }
//    
//    
//    /**
//     *	<p> This handler sets the enabled status for a particular target.
//     *  <p> Input  value: "target" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="changeAppEnableForTarget",
//        input={
//            @HandlerInput(name="enabled", type=Boolean.class, required=true),
//            @HandlerInput(name="LB", type=Boolean.class, required=true),
//            @HandlerInput(name="selectedRows", type=java.util.List.class)}
//     )
//    public static void changeAppEnableForTarget(HandlerContext handlerCtx){
//        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
//        String enabled = "" + handlerCtx.getInputValue("enabled");
//        boolean LB = ((Boolean)handlerCtx.getInputValue("LB")).booleanValue();
//        try{
//            for(Map oneRow: selectedRows){
//                String appType = (String)oneRow.get("appType");
//                if (appType.equals(GuiUtil.getMessage("tree.appclientModules")))
//                    continue;
//                ObjectName objName = new ObjectName((String)oneRow.get("objectName"));
//                Set<DeployedItemRefConfig> appRefs = AMXRoot.getInstance().getQueryMgr().queryPatternSet(objName);
//                for(DeployedItemRefConfig ref : appRefs){
//                    //TODO V3
////                    if (LB)  //should only be 1 in the set.
////                        ref.setLBEnabled(enabled);
////                    else
//                        ref.setEnabled(enabled); 
//                }
//            }
//        }catch(Exception ex){
//            //TODO: log exception
//            GuiUtil.prepareException(handlerCtx, ex);
//        }
//    }
//    
//    
//     /**
//     *	<p> This handler sets the enabled status for a particular target.
//     *  <p> Input  value: "target" -- Type: <code> java.lang.String</code></p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="removeAppTarget",
//        input={
//            @HandlerInput(name="selectedRows", type=java.util.List.class)}
//     )
//    public static void removeAppTarget(HandlerContext handlerCtx){
//        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
//        try{
//            for(Map oneRow: selectedRows){
//                ObjectName objName = new ObjectName((String)oneRow.get("objectName"));
//                Set<DeployedItemRefConfig> appRefs = AMXRoot.getInstance().getQueryMgr().queryPatternSet(objName);
//                for(DeployedItemRefConfig ref : appRefs){ //should only be 1 in the set.
//                    DeployedItemRefConfigCR container = (DeployedItemRefConfigCR)ref.getContainer();
//                    container.removeDeployedItemRefConfig( ref.getName() );  
//                }
//            }
//        }catch(Exception ex){
//            //TODO: log exception
//            GuiUtil.prepareException(handlerCtx, ex);
//        }
//    }
//    
//      /**
//     *	<p> This handler saves the disable timeout of application ref. 
//     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
//     */
//    @Handler(id="saveDisableTimeout",
//        input={
//            @HandlerInput(name="TableRowGroup", type=TableRowGroup.class, required=true)}
//        )
//    
//    public static void saveDisableTimeout(HandlerContext handlerCtx) {
//        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("TableRowGroup");
//        try{
//            MultipleListDataProvider dp = (MultipleListDataProvider)trg.getSourceData();
//            List<List<Object>> data = dp.getLists();
//            for(List inner : data){
//                List<Map> innerMap = inner;
//                for(Map oneRow: innerMap){
//                    String appType = (String)oneRow.get("appType");
//                    if (appType.equals(GuiUtil.getMessage("tree.appclientModules")))
//                        continue;
//                    String timeout = (String)oneRow.get("timeout");
//                    ObjectName objName = new ObjectName((String)oneRow.get("objectName"));
//                    Set<DeployedItemRefConfig> appRefs = AMXRoot.getInstance().getQueryMgr().queryPatternSet(objName);
//                    for(DeployedItemRefConfig ref : appRefs){
//                        ref.setDisableTimeoutInMinutes(timeout);
//                    }
//                }
//            }
//        }catch(Exception ex){
//            GuiUtil.handleException(handlerCtx, ex);
//        }            
//    }    
//    
//    
//    
//    /**
//     *	<p> Returns the list of resources for filtering 
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getAppFilterTypes",
//        output={
//            @HandlerOutput(name="labels", type=java.util.List.class),
//            @HandlerOutput(name="values", type=java.util.List.class)}
//     )
//    public static void getAppFilterTypes(HandlerContext handlerCtx){
//
//        List labels = new ArrayList();
//        labels.add(GuiUtil.getMessage("common.showAll"));
//        labels.add(GuiUtil.getMessage("tree.enterpriseApps"));
//        labels.add(GuiUtil.getMessage("tree.webApps"));
//        labels.add(GuiUtil.getMessage("tree.ejbModules"));
//        labels.add(GuiUtil.getMessage("tree.lifecycleModules"));
//        labels.add(GuiUtil.getMessage("tree.connectorResources"));
//        labels.add(GuiUtil.getMessage("tree.appclientModules"));
//        labels.add(GuiUtil.getMessage("tree.customMBeans"));
//  
//        List values = new ArrayList();
//        values.add("");
//        values.add(J2EEApplicationConfig.J2EE_TYPE);
//        values.add(WebModuleConfig.J2EE_TYPE);
//        values.add(EJBModuleConfig.J2EE_TYPE);
//        values.add(LifecycleModuleConfig.J2EE_TYPE);
//        values.add(RARModuleConfig.J2EE_TYPE);
//        values.add(AppClientModuleConfig.J2EE_TYPE);
//        values.add(CustomMBeanConfig.J2EE_TYPE);
//        
//        handlerCtx.setOutputValue("values", values);
//        handlerCtx.setOutputValue("labels", labels);
//        
//    }
//    
//    /**
//     *	<p> Returns the app type for displaying in the redeploy page
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getAppTypeForDisplay",
//        input={
//            @HandlerInput(name="appType", type=String.class)},
//        output={
//            @HandlerOutput(name="displayType", type=String.class)}
//     )
//    public static void getAppTypeForDisplay(HandlerContext handlerCtx){
//        String appType = (String) handlerCtx.getInputValue("appType");
//        handlerCtx.setOutputValue("displayType", displayMap.get(appType));
//    }
//    
//    /** 
//     * <p> Handler to set the viewKey which is used to decide if user wants summary or detail view.
//     */
//    @Handler(id="setAppViewKey",
//        input={
//            @HandlerInput(name="key", type=String.class, required=true),
//            @HandlerInput(name="selectedValue", type=String.class)}
//     )
//    public static void setAppViewKey(HandlerContext handlerCtx){
//            String key = (String) handlerCtx.getInputValue("key");
//            String selectedValue = (String) handlerCtx.getInputValue("selectedValue");
//            if (selectedValue.equals("summary"))
//                handlerCtx.getFacesContext().getExternalContext().getSessionMap().put(key, true);
//            else
//                handlerCtx.getFacesContext().getExternalContext().getSessionMap().put(key, false);
//    }
//    
//    /**
//     *	<p> This handler uses the given AppID to find all the possible
//     *	server/port combinations available for accessing the application client.</p>
//     * 
//     *  <p> Input value: "AppID" -- Type: <code>java.lang.String</code></p>.
//     *  <p> Input value: "AppendURL" -- Type: <code>java.lang.String</code></p
//     *  <p> Output value: "URLList" -- Type: <code>java.util.List</code></p>
//     *          
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getAppClientLinks",
//        input={
//            @HandlerInput(name="AppID", type=String.class, required=true),
//            @HandlerInput(name="AppendURL", type=String.class)},
//        output={
//            @HandlerOutput(name="AppClientLinks", type=List.class),
//            @HandlerOutput(name="SelectedLink", type=String.class)})    
//    public void getAppClientLinks(HandlerContext handlerCtx) {
//	// First get the application id
//	String id = (String)handlerCtx.getInputValue("AppID");
//        String appendUrl = (String)handlerCtx.getInputValue("AppendURL");
//        if (appendUrl != null) {
//            appendUrl = ((appendUrl.startsWith("/") ? appendUrl.substring(1) : appendUrl));
//        }
//	// Get all the URLs
//	List urls = getURLs(id, getWebAppReferencees(id));
//        String[] values = new String[urls.size()];
//        List list = new ArrayList();
//        for(int i=0; i< urls.size(); i++){
//            values[i] = (String) urls.get(i)+ "/" + appendUrl;
//            list.add(values[i]);
//        }
//        handlerCtx.setOutputValue("AppClientLinks", list);
//        handlerCtx.setOutputValue("SelectedLink", !list.isEmpty() ? (String)list.get(0) : "");
//     }
//    
//    /**
//     *	<p> This handler uses the given AppID to find all the possible
//     *	server/port combinations available for accessing the application.</p>
//     * 
//     *  <p> Input value: "AppID" -- Type: <code>java.lang.Object</code></p>
//     *  <p> Output value: "URLList" -- Type: <code>java.util.List</code></p>
//     *          
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getTargetURLList",
//        input={
//            @HandlerInput(name="AppID", type=String.class, required=true),
//            @HandlerInput(name="contextRoot", type=String.class)},
//        output={
//            @HandlerOutput(name="URLList", type=List.class)})
//    public void getTargetURLList(HandlerContext handlerCtx) {
//	String id = (String)handlerCtx.getInputValue("AppID");
//        String contextRoot = (String)handlerCtx.getInputValue("contextRoot");
//        String ctxRoot = calContextRoot(contextRoot);
//    
//	List urls = getURLs(id, getWebAppReferencees(id));
//	Iterator it = urls.iterator();
//	String url = null;
//        ArrayList list = new ArrayList();
//	while (it.hasNext()) {
//	    url = (String)it.next();       
//            HashMap m = new HashMap();
//            m.put("url", url + ctxRoot);
//            list.add(m);
//	}
//        handlerCtx.setOutputValue("URLList", list);
//    }
//    
//     /**
//     *	This method takes an array of object name.  These object names may
//     *	either be server instances or cluster instances.  It will get the
//     *	cluster's instances and recurse.  It will pass the server instances to
//     *	getURLs(ObjectName).
//     *
//     *	@param	serverInstance	An array of ObjectName, server instances
//     *				and/or clusters
//     */
//    protected List getURLs(String appId, ObjectName serverInstances[]) {
//	// Iterate over all the supplied server instance ObjectNames
//	List urls = new ArrayList();
//        /* TODO-V3
//	for (int count=0; count<serverInstances.length; count++) {
//	    if (serverInstances[count].getKeyProperty("type").equals("cluster")) {
//		// We found a cluster, get the server instances in the cluster
//		// listServerInstances returns: com.sun.appserv:type=server,name=Instance1A,category=config
//		urls.addAll(getURLs(appId, (ObjectName[])JMXUtil.invoke(
//		    serverInstances[count],
//		    "listServerInstances", null, null)));
//	    } else {
//		// We have a Server Instance, just get the urls
//		urls.addAll(getURLs(appId, serverInstances[count]));
//	    }
//	}
//         */
//
//	// Return the URLs
//	return urls;
//    }
//
//    /**
//     *	This method expects a ObjectName to a server instance (not a cluster).
//     *	It will obtain its configuration and determine its concrete port
//     *	number(s).
//     */
//    protected List getURLs(String appId, ObjectName serverInstance) {
//
//        return new ArrayList(0);
//        /* TODO-V3
//         * 
//	// Get the config ref name, this is used in a couple places below
//	String configRef = (String)JMXUtil.getAttribute(serverInstance, "config-ref");
//
//	// First get the application-ref ObjectName
//	String name = (String)JMXUtil.getAttribute(serverInstance, "name");
//	String appObj = "com.sun.appserv:type=application-ref,ref="+appId+",server="+name+",category=config";
//
//	// Next get the virtual server(s)
//	String strVirServers = (String)JMXUtil.getAttribute(appObj, "virtual-servers");
//	if ((strVirServers == null) || strVirServers.equals("")) {
//	    // No virtual server was specified, we must look at the listeners,
//	    // find the first *enabled* listener, then look at the listener's
//	    // *default* virtual server's listeners (backwards I know... but
//	    // this is how it is).
//
//	    // First get all the listeners for this instance
//	    ObjectName listeners[] = (ObjectName[])JMXUtil.invoke(
//		"com.sun.appserv:type=http-service,config="+configRef+
//		",category=config", "getHttpListener", null, null);
//
//	    // Find the first enabled listener
//	    int count=0;
//	    while (count<listeners.length) {
//		if (((String)JMXUtil.getAttribute(listeners[count],
//		    "enabled")).equals("true")) {
//		    break;
//		}
//		count++;
//	    }
//	    if (count == listeners.length) {
//		// There are no enabled listeners, return an empty List
//		return new ArrayList(0);
//	    }
//
//	    // Now pull off the default-virutal-server
//	    strVirServers = (String)JMXUtil.getAttribute(listeners[count],
//		"default-virtual-server");
//	}
//
//	// Next get the http listeners from the virtual servers
//	String virServer;
//	String listeners;
//	Iterator listIt;
//	String listener;
//	List listenerList = new ArrayList();
//	Iterator it = GuiUtil.parseStringList(strVirServers, " ,").iterator();
//	while (it.hasNext()) {
//	    // Get each virtual server's list of listeners
//	    virServer = (String)it.next();
//	    listeners = (String)JMXUtil.getAttribute(
//		"com.sun.appserv:type=virtual-server,id="+virServer+
//		    ",config="+configRef+",category=config",
//		"http-listeners");
//         * \
//	    listIt = GuiUtil.parseStringList(listeners, " ,").iterator();
//
//	    // Add each unique listener
//	    while (listIt.hasNext()) {
//		listener = (String)listIt.next();
//		if (!listenerList.contains(listener)) {
//		    // Only add unique listeners
//		    listenerList.add(listener);
//		}
//	    }
//	}
//
//	// There will be 1 URL per listener
//	List urls = new ArrayList(listenerList.size());
//	StringBuffer url = null;
//	String listenerObj;
//	it = listenerList.iterator();
//	while (it.hasNext()) {
//	    // Get the next listener
//	    listenerObj = "com.sun.appserv:type=http-listener,id="+it.next()
//		+",config="+configRef+",category=config";
//
//	    // Start the URL
//	    url = new StringBuffer("http");
//	    if (JMXUtil.getAttribute(listenerObj, "security-enabled").toString().equals("true")) {
//		// Security is enabled on this port, add an 's'
//		url.append("s");
//	    }
//	    url.append("://");
//
//	    // Host
//	    url.append(getHost(serverInstance));
//	    url.append(":");
//
//	    // Port
//	    url.append(replaceTokens(serverInstance, (String)JMXUtil.getAttribute(listenerObj, "port")));
////	    url.append("/");
//
//	    // Add the URL to the List
//	    urls.add(url.toString());
//	}
//
//	return urls;
//         */
//    }
//    
//        /**
//     *	This method determines the hostname of the given serverInstance
//     *	ObjectName to the best of its ability.  It will attempt to obtain the node-agent....
//     *
//     *	@param	serverInstance	The ObjectName to use to determine the hostname
//     */
//    protected String getHost(ObjectName serverInstance) {
//        
//        return "";
//        /* TODO-V3
//         * 
//	// Find the node agent (if there is one)
//	String nodeAgentRef = (String)JMXUtil.getAttribute(serverInstance, "node-agent-ref");
//	if ((nodeAgentRef == null) || nodeAgentRef.equals("")) {
//	    return getDefaultHostName();
//	}
//
//	// Get the JMX connector for the node agent
//	ObjectName jmxConnector = (ObjectName)JMXUtil.invoke(
//		"com.sun.appserv:type=node-agent,name="+nodeAgentRef+
//		    ",category=config",
//		"getJmxConnector", null, null);
//	if (jmxConnector == null) {
//	    return getDefaultHostName();
//	}
//	
//	// Try to get the hostname
//	// Get "client-hostname" from the properties (use this way instead
//	// of getProperty to avoid exception
//	AttributeList properties = (AttributeList)JMXUtil.invoke(
//		jmxConnector, "getProperties", null, null);
//	Attribute att;
//	String hostName = null;
//	Iterator it = properties.iterator();
//	while (it.hasNext()) {
//	    att = (Attribute)it.next();
//	    if (att.getName().equals("client-hostname")) {
//		hostName = (String)att.getValue();
//		break;
//	    }
//	}
//
//	// Get default host name
//	if ((hostName == null) || hostName.equals("") || hostName.equals("0.0.0.0")) {
//	    return getDefaultHostName();
//	}
//
//	// We found the hostname!!
//	return hostName;
//         */
//    }
//
//
//    /**
//     *
//     *	@param	src		The String which may contain tokens for
//     *				substitution.
//     *
//     *	@param	serverInstance	The serverInstance which has the system
//     *				properties.
//     */
//    protected String replaceTokens(ObjectName serverInstance, String src) {
//
//        return src;
//        
//        /* TODO-V3
//         * 
//	int idx = src.lastIndexOf("${");
//	if (idx < 0) {
//	    // Nothing to do
//	    return src;
//	}
//
//	// Get the Properties
//	Properties sysProp = (Properties)
//	    JMXUtil.invoke(serverInstance, "listSystemProperties",
//		new Object[]{ new Boolean("true")}, new String[]{"boolean"});
//	int endIdx;
//	String newString;
//	while (idx > -1) {
//	    endIdx = src.indexOf("}", idx+2);
//	    newString =
//		src.substring(0, idx)+
//		sysProp.getProperty(src.substring(idx+2, endIdx)) +
//		src.substring(endIdx+1);
//	    src = newString;
//	    idx = src.lastIndexOf("${");
//	}
//
//	// Return the result after the substitution(s)
//	return src;
//         */
//    }
//    
//    /**
//     *	This method is used as a fallback when no Hostname is provided.
//     */
//    public static String getDefaultHostName() {
//        String defaultHostName = "localhost";
//	try {
//	    InetAddress host = InetAddress.getLocalHost();
//	    defaultHostName = host.getCanonicalHostName();
//	} catch(UnknownHostException uhe) {
////	    sLogger.log(Level.FINEST, "mbean.get_local_host_error", uhe);
////	    sLogger.log(Level.INFO, "mbean.use_default_host");
//	}
//	return defaultHostName;
//    }
//    
//    /**
//     *	This method finds all the webapps associated w/ appId
//     *
//     *	@param	appId	The application
//     *
//     *	@return	The ObjectName[] of servers / clusters
//     */
//    protected ObjectName[] getWebAppReferencees(String appId) {
//        return new ObjectName[0];
//        /** TODO-V3
//         * 
//	return (ObjectName[])JMXUtil.invoke(
//	    "com.sun.appserv:type=applications,category=config",
//	    "listReferencees",
//	    new Object[] { appId },
//	    new String[] { "java.lang.String" });
//         */
//    }
//
//    /**
//     *	<p> This handler finds the launch URL path for the given JWS enabled application
//     *      client.</p>
//     * 
//     *  <p> Input value: "AppClientName" -- Type: <code>java.lang.String</code></p>
//     *  <p> Input value: "AppName" -- Type: <code>java.lang.String</code></p>
//     *  <p> Output value: "Path" -- Type: <code>java.lang.String</code></p>
//     *  <p> Output value: "IsEnabled" -- Type: <code>java.lang.Boolean</code></p>
//     *          
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getLaunchURLPath",
//        input={
//            @HandlerInput(name="AppClientName", type=String.class),
//            @HandlerInput(name="AppName", type=String.class)},
//        output={
//            @HandlerOutput(name="Path", type=String.class),
//            @HandlerOutput(name="IsEnabled", type=Boolean.class)})
//    public static void getLaunchURLPath(HandlerContext handlerCtx) {
//        
//        /* TODO-V3
//         * 
//        String appclientName = (String)handlerCtx.getInputValue("AppClientName");
//        String applicationName = (String)handlerCtx.getInputValue("AppName");
//        if (GuiUtil.isEmpty(appclientName)) {
// //           throw new Exception("Deployment.getLaunchURLPath(), appclientName is NULL");
//        }
//        try {
//            String jwsEnabled ;
//            if (GuiUtil.isEmpty(applicationName)) {
//                jwsEnabled = (String) JMXUtil.getAttribute("com.sun.appserv:type=appclient-module,category=config,name="+appclientName, "java-web-start-enabled");
//            } else {
//                jwsEnabled = (String) JMXUtil.getAttribute("com.sun.appserv:type=j2ee-application,category=config,name="+applicationName, "java-web-start-enabled");
//            }
//            if ( "false".equals(jwsEnabled)){
//                handlerCtx.setOutputValue("IsEnabled", Boolean.FALSE);
//                return;
//            }
//                
//            NamingConventions nc = new NamingConventions();
//            String path = (GuiUtil.isEmpty(applicationName)) ? nc.getLaunchURLPath(appclientName) :
//                           nc.getLaunchURLPath(applicationName, appclientName); 
//            if (!GuiUtil.isEmpty(path)){
//                handlerCtx.setOutputValue("Path", path);
//                handlerCtx.setOutputValue("IsEnabled", Boolean.TRUE);
//            } else {
//                handlerCtx.setOutputValue("IsEnabled", Boolean.FALSE);
//            }
//        }catch (Exception ex){
//            //NamingConvention may throw ConfigException. log it and assume null.
//     //       GuiUtil.logFINE(ex);
//            ex.printStackTrace();
//            handlerCtx.setOutputValue("IsEnabled", Boolean.FALSE);
//        }
//         * 
//         */ 
//     }
//    
//    /**
//     *	<p> This handler uses the given AppID to find port number on which
//     *      the application could be executed.</p>
//     * 
//     *  <p> Input value: "AppID" -- Type: <code>java.lang.Object</code></p>
//     *  <p> Output value: "Port" -- Type: <code>java.lang.String</code></p>
//     *          
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getPortForApplication",
//        input={
//            @HandlerInput(name="AppID", type=String.class, required=true)},
//        output={
//            @HandlerOutput(name="Port", type=String.class),
//            @HandlerOutput(name="secure", type=Boolean.class)})
//    public static void getPortForApplication(HandlerContext ctx) {
//        String appName = (String)ctx.getInputValue("AppID");
//        String port = getPortForApplication(appName);
////        if (port == null){
//              ctx.setOutputValue("Port", "");
//              ctx.setOutputValue("secure", false);
//          }
//              
//        if (port.startsWith("-") ){
//            ctx.setOutputValue("Port", port.substring(1));
//            ctx.setOutputValue("secure", true);
//        }else{
//            ctx.setOutputValue("Port", port);
//            ctx.setOutputValue("secure", false);
//        }
//        
//    }
//    
//    /* returns the port number on which appName could be executed 
//     * will try to get a port number that is not secured.  But if it can't find one, a
//     * secured port will be returned, prepanded with '-'
//     */
//    static String getPortForApplication(String appName) {
//        
//        DeployedItemRefConfig appRef = TargetUtil.getDeployedItemRefObject(appName, "server");
//        String vsId = null;
//        if (appRef == null) { // no ref found for this application
//            vsId = getNonAdminVirtualServer();
//        } else {
//            vsId = TargetUtil.getAssociatedVS(appName, "server");
//            if (vsId == null || vsId.length() ==0) { // no vs found for this application
//                vsId = getNonAdminVirtualServer();
//            } else {
//                if (vsId.indexOf(",") > 0) {
//                    vsId = vsId.substring(0, vsId.indexOf(","));
//                }
//            }
//        }
//        if (vsId == null)
//            return ""; // no vs found for this app..
//
//        String port = null;
//        Boolean secure = false;
//        try{
//            final HTTPServiceConfig httpServiceConfig = AMXRoot.getInstance().getConfig("server-config").getHTTPServiceConfig();
//             VirtualServerConfig vsConfig = httpServiceConfig.getVirtualServerConfigMap().get(vsId);
//             if (vsConfig != null) {
//                String listeners = vsConfig.getHTTPListeners();
//                if (!GuiUtil.isEmpty(listeners)) {
//                    StringTokenizer tok = new StringTokenizer(listeners, ",");
//                    String listener = "";
//                    while (tok.hasMoreTokens()) {
//                        listener = tok.nextToken();
//                        HTTPListenerConfig hConfig = httpServiceConfig.getHTTPListenerConfigMap().get(listener);
//                        secure = Boolean.valueOf(hConfig.getSecurityEnabled());
//                        port = hConfig.getPort();
//                        if (! secure) break;
//                    }
//                }
//            }
//            return (secure) ? "-" + port : port;
//        }catch(Exception ex){
//            //Maybe the vitrual server is not found, maybe there is no http listener
//            //this can be the case due to user error during deployment. refer to issue#2807.
//            //TODO: use logger
//            ex.printStackTrace();
//            return "";
//        }
//        
//    }
//    
//    // returns 'first' nonadmin virtual server -
//    private static String getNonAdminVirtualServer() {
//        
//        Map<String, VirtualServerConfig> vsMap = AMXRoot.getInstance().getConfig("server-config").getHTTPServiceConfig().getVirtualServerConfigMap();
//        for(String vsName : vsMap.keySet()){
//            if (! vsName.equals("__asadmin")){
//                return vsName;
//            }
//        }
//        return "";
//    }
//    
//    static private boolean includeAppRef( String appName, String appType, String filterValue ){ 
//        //for non-j2ee type apps, eg SIP, appType may be empty. Ignore it since we don't have code to handle non-standard j2ee apps
//        if (GuiUtil.isEmpty(appType)){
//            return false;
//        }
//        if (!GuiUtil.isEmpty(filterValue)){
//            if (!appType.equals(filterValue))
//                return false;
//        }
//        
//        if (AppClientModuleConfig.J2EE_TYPE.equals(appType))
//            return true;
//        
//        Set configs = AMXRoot.getInstance().getQueryMgr().queryJ2EETypeNameSet(appType, appName);
//        ObjectType appConfig = (ObjectType) configs.iterator().next();
//        String objectType = appConfig.getObjectType();
//        return (ObjectTypeValues.USER.equalsIgnoreCase(objectType)) ? true: false;
//    }
//    
//    
//    private static String calContextRoot(String contextRoot){
//        //If context root is not specified or if the context root is "/", ensure that we don't show two // at the end.
//        //refer to issue#2853
//        String ctxRoot = "";
//        if ((contextRoot == null) || contextRoot.equals("") || contextRoot.equals("/"))
//            ctxRoot = "/";
//        else
//        if (contextRoot.startsWith("/"))
//            ctxRoot = contextRoot;
//        else
//            ctxRoot = "/" + contextRoot;
//        return ctxRoot;
//    }
//    
//    private static Enabled getModuleConfig(String appName, String appType){
//        Enabled module=null;
//        AMXRoot amxRoot = AMXRoot.getInstance();
//        if ("application".equals(appType)){
//	    module = amxRoot.getApplicationsConfig().getApplicationConfigMap().get(appName);
//        }else
//        if ("ejbModule".equals(appType)){
//            module = amxRoot.getApplicationsConfig().getEJBModuleConfigMap().get(appName);
//        }else
//        if ("webApp".equals(appType)){
//            //V3: TP2 was using J2EEApplicaitonConfigMap() to be the web App
//            //module = amxRoot.getApplicationsConfig().getWebModuleConfigMap().get(appName);
//            
//            module = amxRoot.getApplicationsConfig().getApplicationConfigMap().get(appName);
//        }else
//        if ("connector".equals(appType)){
//            module = amxRoot.getApplicationsConfig().getRARModuleConfigMap().get(appName);
//        }else
//        if ("lifecycle".equals(appType)){
//            module = amxRoot.getApplicationsConfig().getLifecycleModuleConfigMap().get(appName);
//        } 
//         if ("mbean".equals(appType)){
//            module = amxRoot.getApplicationsConfig().getCustomMBeanConfigMap().get(appName);
//        } 
//        return module;
//    }
//    
//      static private Map<String, String> displayMap = new HashMap();
//      static private Map<String, String> editMap = new HashMap();
//      static private Map<String, String> typeMap = new HashMap();
//      static{
//        editMap.put(J2EEApplicationConfig.J2EE_TYPE, "enterpriseApplicationsEdit.jsf");
//        editMap.put(WebModuleConfig.J2EE_TYPE, "webApplicationsEdit.jsf");
//        editMap.put(EJBModuleConfig.J2EE_TYPE, "ejbModulesEdit.jsf");
//        editMap.put(LifecycleModuleConfig.J2EE_TYPE, "lifecycleModulesEdit.jsf");
//        editMap.put(RARModuleConfig.J2EE_TYPE, "connectorModulesEdit.jsf");
//        editMap.put(AppClientModuleConfig.J2EE_TYPE, "appclientModulesEdit.jsf");
//        editMap.put(CustomMBeanConfig.J2EE_TYPE, "customMBeansEdit.jsf");
//        
//        typeMap.put(J2EEApplicationConfig.J2EE_TYPE, GuiUtil.getMessage("tree.enterpriseApps"));
//        typeMap.put(WebModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.webApps"));
//        typeMap.put(EJBModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.ejbModules"));
//        typeMap.put(LifecycleModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.lifecycleModules"));
//        typeMap.put(RARModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.connectorModules"));
//        typeMap.put(AppClientModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.appclientModules"));
//        typeMap.put(CustomMBeanConfig.J2EE_TYPE, GuiUtil.getMessage("tree.customMBeans"));
//        
//        displayMap.put("application", GuiUtil.getMessage("deploy.ear"));
//        displayMap.put("webApp", GuiUtil.getMessage("deploy.war"));
//        displayMap.put("ejbModule", GuiUtil.getMessage("deploy.ejb"));
//        displayMap.put("appclient", GuiUtil.getMessage("deploy.appClient"));
//        displayMap.put("connector", GuiUtil.getMessage("deploy.rar"));
//      }
//   
//   
}
