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

import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.GuiUtil;

import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.ext.logging.LogFileAccess;
import com.sun.appserv.management.monitor.ServerRootMonitor;

import java.util.StringTokenizer;
import javax.faces.context.ExternalContext;

import javax.servlet.http.HttpServletRequest;

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


        //V3 GRIZZLY TODO
        //http ports
        /* HTTPServiceConfig service = amxRoot.getConfig(configName).getHTTPServiceConfig();
        Map<String, HTTPListenerConfig> listeners = service.getHTTPListenerConfigMap();
        StringBuffer ports = new StringBuffer();
        for (String key : listeners.keySet()) {
            String port = listeners.get(key).getPort();
            if (port.startsWith("$")) {
                port = resolveToken((port.substring(2, port.length() - 1)), instanceName);
            }
            ports.append("," + port);
        }
        ports.deleteCharAt(0);  //remove the first ','
        handlerCtx.setOutputValue("httpPorts", ports.toString());
         */
        handlerCtx.setOutputValue("httpPorts", GuiUtil.getMessage("TBD"));


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

    /**
     *	<p> This handler rotate the log files of the server instance, instance should be running.
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     */
    @Handler(id = "rotateLogFile",
    input = {
        @HandlerInput(name = "instanceName", type = String.class, required = true)
    })
    public static void rotateLogFile(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        if (GuiUtil.isEmpty(instanceName)) {
            return;   //do nothing.
        }
        ServerRootMonitor monitor = AMXRoot.getInstance().getMonitoringRoot().getServerRootMonitorMap().get(instanceName);
        if (monitor == null) {
            //most likely, server is not running, shouldn't get here
            //TODO: log an internal error msg ?
        } else {
            monitor.getLogging().rotateLogFile(LogFileAccess.SERVER_KEY);
        }
    }

    /**
     *	<p> This handler returns the configuration Name of the instance or cluster</p>
     *  <p> Input  value: "target" -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "configName" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getConfigNameOfTarget",
    input = {
        @HandlerInput(name = "target", type = String.class, required = true)
    },
    output = {
        @HandlerOutput(name = "configName", type = String.class)
    })
    public static void getConfigNameOfTarget(HandlerContext handlerCtx) {

        String target = (String) handlerCtx.getInputValue("target");
        String configName = AMXRoot.getInstance().getConfigByInstanceOrClusterName(target).getName();
        handlerCtx.setOutputValue("configName", configName);
    }

    /**
     *	<p> This handler upgrade the profile from developer profile to cluster profile</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "upgradeProfile")
    public static void upgradeProfile(HandlerContext handlerCtx) {
        /* TODO-V3
        try{
        String[] params = {"cluster"};
        String[] signature = {"java.lang.String"};
        JMXUtil.invoke("com.sun.appserv:type=domain,category=config", "addClusteringSupportUsingProfile", params, signature);
        }catch(Exception ex){
        GuiUtil.handleException(handlerCtx, ex);
        }
         */
    }

    /**
     *	<p> This handler stops DAS immediately.</p>
     */
    @Handler(id = "stopDAS")
    public static void stopDAS(HandlerContext handlerCtx) {
        AMXRoot.getInstance().getDomainRoot().executeREST(DomainRoot.STOP_DOMAIN);
    }
}
        
 
