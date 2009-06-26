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
 * InstanceHandler.java
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

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.GuiUtil;

import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;

import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.intf.config.AMXConfigHelper;
import org.glassfish.admin.amx.intf.config.ConfigTools;

import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;

import java.util.StringTokenizer;
import javax.faces.context.ExternalContext;

import javax.servlet.http.HttpServletRequest;
import org.glassfish.admingui.common.util.V3AMX;

public class InstanceHandler {

    /** Creates a new instance of InstanceHandler */
    public InstanceHandler() {
    }

    /**
     *	<p> This handler returns the values for all the attributes in the
     *      PE Server Instance General Page.</p>
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     *	<p> Output value: "hostName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "httpPorts" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "iiopPorts" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "version" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "configDir" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "debugPort" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getInstanceGeneralAttributes",
    input = {
        @HandlerInput(name = "instanceName", type = String.class, required = true)
    },
    output = {
        @HandlerOutput(name = "hostName", type = String.class),
        @HandlerOutput(name = "httpPorts", type = String.class),
        @HandlerOutput(name = "iiopPorts", type = String.class),
        @HandlerOutput(name = "version", type = String.class),
        @HandlerOutput(name = "configDir", type = String.class),
        @HandlerOutput(name = "debugPort", type = String.class)
    })
    public static void getInstanceGeneralAttributes(HandlerContext handlerCtx) {

        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        if (GuiUtil.isEmpty(instanceName)) {
            //TODO log:
            System.out.println("instanceName is not provided, set to \"server\"");
            instanceName = "server";
        }

        AMXRoot amxRoot = AMXRoot.getInstance();
        String configName = amxRoot.getConfigName(instanceName);

        // get host Name (for PE only.  For EE, we just display the name of the server instance).
        //TODO: once we can test if we are running in PE or EE environment, we should do accordingly.
        //      for now, assume it is PE.
        ExternalContext extContext = handlerCtx.getFacesContext().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) extContext.getRequest();   //we only deal with servlet, not Portlet
        String hostName = request.getServerName();
        handlerCtx.setOutputValue("hostName", hostName);

        handlerCtx.setOutputValue("httpPorts", V3AMX.getHttpPortNumber( hostName, configName));

        //iiop ports
        IIOPServiceConfig iiopService = amxRoot.getConfig(configName).getIIOPServiceConfig();
        Map<String, IIOPListenerConfig> iiopListeners = iiopService.getIIOPListenerConfigMap();
        StringBuffer iports = new StringBuffer();
        for (String key : iiopListeners.keySet()) {
            String iport = iiopListeners.get(key).getPort();
            if (iport.startsWith("$")) {
                iport = resolveToken((iport.substring(2, iport.length() - 1)), instanceName);
            }
            iports.append("," + iport);
        }
        iports.deleteCharAt(0);  //remove the first ','
        handlerCtx.setOutputValue("iiopPorts", iports.toString());


        String configDir = amxRoot.getDomainRoot().getConfigDir();
        String version = amxRoot.getDomainRoot().getApplicationServerFullVersion();
        String debugPort = "";
        String debugOption = amxRoot.getConfig(configName).getJavaConfig().getDebugOptions();
        StringTokenizer tokens = new StringTokenizer(debugOption, ",");
        String doption = "";
        while (tokens.hasMoreTokens()) {
            doption = tokens.nextToken().trim();
            if (doption.startsWith("address")) {
                int pos = doption.indexOf("=");
                if (pos >= 0) {
                    debugPort = doption.substring(pos + 1).trim();
                    break;
                }
            }
        }
        String debugEnabled = amxRoot.getConfig(configName).getJavaConfig().getDebugEnabled();
        String msg = ("true".equals(debugEnabled)) ?
            GuiUtil.getMessage("inst.debugEnabled") + debugPort :
            GuiUtil.getMessage("inst.notEnabled");
        handlerCtx.setOutputValue("debugPort", msg);
        handlerCtx.setOutputValue("configDir", configDir);
        handlerCtx.setOutputValue("version", version);
    }

    private static String resolveToken(String pn, String serverName) {
        StandaloneServerConfig ss = AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().get(serverName);
        if (ss != null) {
            if (ss.getSystemPropertyConfigMap().containsKey(pn)) {
                return ss.getSystemPropertyConfigMap().get(pn).getValue();
            }
        }
        ClusteredServerConfig cs = AMXRoot.getInstance().getServersConfig().getClusteredServerConfigMap().get(serverName);
        if (cs != null) {
            if (cs.getSystemPropertyConfigMap().containsKey(pn)) {
                return cs.getSystemPropertyConfigMap().get(pn).getValue();
            }
        }
        return "";
    }

@Handler(id="getProfilerAttrs",
    output={
        @HandlerOutput(name="objectName",        type=String.class),
        @HandlerOutput(name="edit",        type=Boolean.class)})

        public static void getProfilerAttrs(HandlerContext handlerCtx) {
        ObjectName objName = null;
        Boolean edit = false; 
        try{
            AMXProxy amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName("v3:pp=/domain/configs/config[server-config],type=java-config"));
            objName = (ObjectName) amx.attributesMap().get("Profiler");
            if (objName != null) {
                edit = true;
            }
            handlerCtx.setOutputValue("edit", edit);
            handlerCtx.setOutputValue("objectName", objName);
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("edit", false); 
        }
    }    

 @Handler(id="getJvmOptionsValues",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="result", type=java.util.List.class)})

        public static void getJvmOptionsValues(HandlerContext handlerCtx) {
        try{
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            AMXProxy  amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            final String[] options = (String[])amx.attributesMap().get("JvmOptions");
            handlerCtx.setOutputValue("result", GuiUtil.convertArrayToListOfMap(options, "Value"));
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("result", new HashMap());
        }
    }   
 
     @Handler(id="saveJvmOptionValues",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true),
        @HandlerInput(name="options",   type=List.class)} )
       public static void saveJvmOptionValues(HandlerContext handlerCtx) {
        List newList = new ArrayList();
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            ObjectName objectName = new ObjectName(objectNameStr);
            List<Map<String, String>> options = (List) handlerCtx.getInputValue("options");
            for (Map<String, String> oneRow : options) {
                String value = oneRow.get(PROPERTY_VALUE);
                if (!GuiUtil.isEmpty(value)) {
                    newList.add(value);
                }
            }
            V3AMX.setAttribute(objectName, new Attribute("JvmOptions", (String[]) newList.toArray(new String[0])));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler stops DAS immediately.</p>
     */
    @Handler(id = "stopDAS")
    public static void stopDAS(HandlerContext handlerCtx) {
        AMXRoot.getInstance().getDomainRoot().executeREST(DomainRoot.STOP_DOMAIN);
    }
    
    private static final String PROPERTY_VALUE = "Value";
}
        
 
