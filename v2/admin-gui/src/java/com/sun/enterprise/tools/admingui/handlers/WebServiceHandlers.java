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
 * WebServiceHandlers.java
 *
 * Created on September 13, 2006, 12:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.handlers;

import com.sun.appserv.management.config.EJBModuleConfig;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.RegistryLocationConfig;
import com.sun.appserv.management.config.TransformationRuleConfig;
import com.sun.appserv.management.config.WebModuleConfig;
import com.sun.appserv.management.config.WebServiceEndpointConfig;
import com.sun.appserv.management.config.WebServiceEndpointConfigCR;
import com.sun.appserv.management.ext.wsmgmt.MessageTrace;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfoImpl;
import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.appserv.management.j2ee.WebServiceEndpoint;
import com.sun.appserv.management.monitor.CallFlowMonitor;
import com.sun.appserv.management.monitor.WebServiceEndpointMonitor;
import com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats;
import com.sun.enterprise.tools.admingui.bean.WebServiceBean;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.TargetUtil;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.util.LogUtil;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jennifer Chou
 */
public class WebServiceHandlers {
    
    /** Creates a new instance of WebServiceHandlers */
    public WebServiceHandlers() {
    }
    
    /**
     *	<p> Returns the WeServiceBean</p>
     *
     *  <p> Output value: "WebServiceBean" -- Type: <code>com.sun.enterprise.tools.admingui.bean.WebServiceBean</code>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebServiceBean",
         output={
            @HandlerOutput(name="WebServiceBean", type=WebServiceBean.class)})
    public static void getWebServiceHandlers(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("WebServiceBean", new WebServiceBean());
    }
    
    /**
     *	<p> Returns the AMX WebServiceEndpointInfo MBean object</p>
     *
     *  <p> Input value: "WebServiceKey" -- Type: <code>String</code> 
     *          - fully qualified name of web service
     *  <p> Output value: "WebServiceEndpointInfoMBean" -- Type: <code>com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo</code></p>
     *          - AMX WebServiceEndpointInfo MBean object
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebServiceEndpointInfoMBean",
        input={
            @HandlerInput(name="WebServiceKey", type=Object.class, required=true)},
        output={
            @HandlerOutput(name="WebServiceEndpointInfoMBean", type=WebServiceEndpointInfo.class)})
    public static void getWebServiceEndpointInfoMBean(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue(
            "WebServiceEndpointInfoMBean",
            AMXUtil.getWebServiceEndpointInfo(
                (Object)handlerCtx.getInputValue("WebServiceKey")));
    }

    /**
     *	<p> Returns the Map representation of WebServiceEndpointInfo</p>
     *
     *  <p> Input value: "WebServiceEndpointInfoMBean" -- Type: 
     *          <code>com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo</code> 
     *  <p> Output value: "WebServiceInfoMap" -- Type: <code>java.util.Map</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebServiceInfoMap",
        input={
            @HandlerInput(name="WebServiceEndpointInfoMBean", 
                type=WebServiceEndpointInfo.class, required=true)},
        output={
            @HandlerOutput(name="WebServiceInfoMap", type=Map.class)})
    public static void getWebServiceInfoMap(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("WebServiceInfoMap", 
            ((WebServiceEndpointInfo)handlerCtx.getInputValue(
                "WebServiceEndpointInfoMBean")).asMap());
    }     
    
    /**
     *	<p> Detemines whether Web Service Test button should enabled or disabled</p>
     *
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.Object</code>
     *  <p> Output value: "DisableTest" -- Type: <code>java.lang.Boolean</code></p>
     *
     *	@param	context	The HandlerContext.
     */
    @Handler(id="disableTest",
        input={
            @HandlerInput(name="WebServiceKey",type=Object.class, required=true)},
        output={
            @HandlerOutput(name="DisableTest", type=Boolean.class)})
    public static void disableTest(HandlerContext handlerCtx) {
        Object webServiceKey = (Object)handlerCtx.getInputValue("WebServiceKey");
        WebServiceEndpointInfo wsInfo = AMXUtil.getWebServiceEndpointInfo(webServiceKey);
        boolean disableTest = false;
        if (wsInfo.isSecure()) {
            disableTest = true;
        } else {
            Enabled app = null;
            if (!wsInfo.isAppStandaloneModule()) {
                app = AMXUtil.getDomainConfig().getJ2EEApplicationConfigMap().get(wsInfo.getAppID());
            } else {
                if (wsInfo.getServiceImplType().equals(WebServiceEndpointInfo.SERVLET_IMPL)) {
                    app = AMXUtil.getDomainConfig().getWebModuleConfigMap().get(wsInfo.getAppID());
                } else {
                    app = AMXUtil.getDomainConfig().getEJBModuleConfigMap().get(wsInfo.getAppID());
                }
            }
            String status = TargetUtil.getEnabledStatus(app, true);
            if (AMXUtil.supportCluster()){
                if (!status.equals(GuiUtil.getMessage("deploy.allEnabled"))) {
                    disableTest = true;
                }
            } else {
                if (status.equals(Boolean.toString(false))) {
                    disableTest = true;
                }
            }
        }
        handlerCtx.setOutputValue("DisableTest", disableTest);
    }
    
    /**
     *	<p> If the EndpointUri has an extra '/' prepended, remove it. </p>
     *
     *  <p> Input value: "Uri" -- Type: <code>java.lang.String</code> 
     *  <p> Output value: "NewUri" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="pruneEndpointUri",
    	input={
	    @HandlerInput(name="Uri", type=String.class, required=true)},
        output={
            @HandlerOutput(name="NewUri", type=String.class)})
    public static void pruneEndpointUri(HandlerContext handlerCtx) {
        String uri = (String) handlerCtx.getInputValue("Uri");
        String newUri = uri;
        if (uri.startsWith("//")) {
            newUri = uri.substring(1);
        }
        handlerCtx.setOutputValue("NewUri", newUri);        
    }
    
    /**
     *	<p> Returns the List of all WebServiceEndpointInfo maps</p>
     * 
     *  <p> Output value: "WebServicesData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebServicesData",
        output={
            @HandlerOutput(name="WebServicesData", type=List.class)})
    public static void getWebServicesData(HandlerContext handlerCtx) {
        Map<Object, String> wsKeyMap = getWSKeys();
        List dataList = new ArrayList();
        for (Iterator iter = wsKeyMap.keySet().iterator(); iter.hasNext();) {
            Object wsKey = iter.next();
            WebServiceEndpointInfo wsInfo = AMXUtil.getWebServiceEndpointInfo(wsKey);
            Map wsInfoMap = wsInfo.asMap();
            
            String wsdlFileName = (new File((String)wsInfoMap.get(
                WebServiceEndpointInfo.WSDL_FILE_LOCATION_KEY))).getName();
            wsInfoMap.put("WSDLFileName", wsdlFileName);
            
            String wsKeyEncoded = (String)wsKey;
            try {
                wsKeyEncoded = URLEncoder.encode(wsKeyEncoded, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                LogUtil.severe("Error encoding " + wsKey, uee);
                wsKeyEncoded = URLEncoder.encode(wsKeyEncoded);
            }
            wsInfoMap.put("WebServiceKeyEncoded", wsKeyEncoded);

            String port = ApplicationHandlers.getPortForApplication(wsInfo.getAppID());
            wsInfoMap.put("Port", port);
            String endpointURI = wsInfo.getEndpointURI();
            String endpointURIPruned = endpointURI;
            if (endpointURI.startsWith("/")) {
                endpointURIPruned = endpointURI.substring(1);
            }
            wsInfoMap.put("EndpointURI", endpointURIPruned);
            
            dataList.add(wsInfoMap);
        }
        handlerCtx.setOutputValue("WebServicesData", dataList);
    }
    
     /**
     *	<p> Returns the last time the Reset button was pushed.</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Server" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Time" -- Type: <code>java.lang.String<code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWsLastResetTime",
        input={
            @HandlerInput(name="WebServiceKey", type=String.class, required=true),
            @HandlerInput(name="Server", type=String.class, required=true)},
        output={
            @HandlerOutput(name="Time", type=String.class)})
    public void getWsLastResetTime(HandlerContext handlerCtx) {
	String webServiceKey = (String) handlerCtx.getInputValue("WebServiceKey");
        String server = (String) handlerCtx.getInputValue("Server");
        Set<WebServiceEndpoint> wsSet 
                = AMXUtil.getWebServiceMgr().getWebServiceEndpointSet(webServiceKey, server);
        
        if (!wsSet.isEmpty()){  // should be only one.
            Iterator it = wsSet.iterator();
            WebServiceEndpoint ws = (WebServiceEndpoint) it.next();
            if (ws != null){
                long lastResetTime = ws.getLastResetTime();
                Date date = new Date(lastResetTime);
                DateFormat dateFormat = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.MEDIUM, 
                    handlerCtx.getFacesContext().getViewRoot().getLocale());
                handlerCtx.setOutputValue("Time", dateFormat.format(date));
            } else {
                handlerCtx.setOutputValue("Time", "");
            }
        } else {
            handlerCtx.setOutputValue("Time", "");
        }
            
    }
    
    /**
     *	<p> Returns the </p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SelectedServer" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DisplayServer" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ServerRootList" -- Type: <code>java.util.ArrayList</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWSServerList",
        input={
            @HandlerInput(name="WebServiceKey", type=String.class, required=true),
            @HandlerInput(name="Server", type=String.class)},
        output={
            @HandlerOutput(name="DisplayServer", type=String.class),
            @HandlerOutput(name="ServerRootList", type=java.util.ArrayList.class)})
    public void getWSServerList(HandlerContext handlerCtx) {
        String webServiceKey = (String) handlerCtx.getInputValue("WebServiceKey");
        String server = (String) handlerCtx.getInputValue("Server");
	ArrayList serverRootList = new ArrayList();
        Set<WebServiceEndpoint> wsSet 
                = AMXUtil.getWebServiceMgr().getWebServiceEndpointSet(webServiceKey, server);
        Iterator it = wsSet.iterator();
        while (it.hasNext()){
            WebServiceEndpoint wsEndpoint =  (WebServiceEndpoint) it.next();
            WebServiceEndpointMonitor wsMonitor = (WebServiceEndpointMonitor)wsEndpoint.getMonitoringPeer(); 
            if (wsMonitor != null){
                String serverRoot = wsMonitor.getServerRootMonitor().getName();
                serverRootList.add(serverRoot);
            }
        }
        if (serverRootList.size() != 0){
            handlerCtx.setOutputValue("DisplayServer", (String)serverRootList.get(0));
        }
        handlerCtx.setOutputValue("ServerRootList", serverRootList);
    }
    
    /**
     *	<p> Returns the </p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SelectedServer" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DisplayServer" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "WSMonitorsList" -- Type: <code>java.util.ArrayList<code></p>
     *  <p> Output value: "ServerRootList" -- Type: <code>java.util.ArrayList</code></p>
     *  <p> Output value: "HasMonitors" -- Type: <code>java.lang.Boolean</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWsMonitorsList",
        input={
            @HandlerInput(name="WebServiceKey", type=String.class, required=true),
            @HandlerInput(name="SelectedServer", type=String.class)},
        output={
            @HandlerOutput(name="DisplayServer", type=String.class),
            @HandlerOutput(name="WSMonitorsList", type=java.util.ArrayList.class),
            @HandlerOutput(name="ServerRootList", type=java.util.ArrayList.class),
            @HandlerOutput(name="HasMonitors", type=java.lang.Boolean.class)})
    public void getWsMonitorsList(HandlerContext handlerCtx) {
	String webServiceKey = (String) handlerCtx.getInputValue("WebServiceKey");
        String selectedServer = (String) handlerCtx.getInputValue("SelectedServer");
        ArrayList wsMonitorsList = new ArrayList();
        ArrayList serverRootList = new ArrayList();
        Set<WebServiceEndpoint> wsSet 
                = AMXUtil.getWebServiceMgr().getWebServiceEndpointSet(webServiceKey, selectedServer);
        Iterator it = wsSet.iterator();
        while (it.hasNext()){
            WebServiceEndpoint wsEndpoint =  (WebServiceEndpoint) it.next();
            WebServiceEndpointMonitor wsMonitor = (WebServiceEndpointMonitor)wsEndpoint.getMonitoringPeer(); 
            if (wsMonitor != null){
                String serverRoot = wsMonitor.getServerRootMonitor().getName();
                wsMonitorsList.add(wsMonitor);
                serverRootList.add(serverRoot);
            }
        }
        
        if (wsMonitorsList.size() != 0){
            handlerCtx.setOutputValue("DisplayServer", 
                    (selectedServer==null) ? (String)serverRootList.get(0) : selectedServer);
        }
        handlerCtx.setOutputValue("WSMonitorsList", wsMonitorsList);
       // String[] test =  (String[])(serverRootList.toArray(new String[serverRootList.size()]));
        handlerCtx.setOutputValue("ServerRootList", serverRootList);
        handlerCtx.setOutputValue("HasMonitors", 
                wsMonitorsList.size()==0 ? new Boolean("false") : new Boolean("true"));
        
    }
    
    /**
     *	<p> Returns the WebServiceEndpointAggregateStatsMBean</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Server" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "WebServiceEndpointAggregateStatsMBean" -- Type: <code>com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebServiceEndpointAggregateStatsMBean",
        input={
            @HandlerInput(name="WebServiceKey", type=String.class, required=true),
            @HandlerInput(name="Server", type=String.class, required=true)},
        output={
            @HandlerOutput(name="WebServiceEndpointAggregateStatsMBean", type=WebServiceEndpointAggregateStats.class)})
    public static void getWebServiceEndpointAggregateStatsMBean(HandlerContext handlerCtx) {
        String webServiceKey =(String)handlerCtx.getInputValue("WebServiceKey");
        String server = (String)handlerCtx.getInputValue("Server");
        Set<WebServiceEndpoint> wsSet 
                = AMXUtil.getWebServiceMgr().getWebServiceEndpointSet(webServiceKey, server);
        WebServiceEndpointAggregateStats statsMbean = null;
        if (!wsSet.isEmpty()){  // should be only one.
            Iterator it = wsSet.iterator();
            WebServiceEndpoint ws = (WebServiceEndpoint) it.next();
            WebServiceEndpointMonitor wsMonitor = (WebServiceEndpointMonitor)ws.getMonitoringPeer(); 
            if (wsMonitor != null){
                statsMbean = wsMonitor.getWebServiceEndpointAggregateStats();
            }
        }
        
        handlerCtx.setOutputValue("WebServiceEndpointAggregateStatsMBean", statsMbean);
    }

    
    /**
     *	<p> Returns the List of all messsages of a web service</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Server" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "WebServiceMessageData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMessageTraceMBean",
        input={
            @HandlerInput(name="WebServiceKey", type=String.class, required=true),
            @HandlerInput(name="Server", type=String.class, required=true),
            @HandlerInput(name="MessageID", type=String.class, required=true)},
        output={
            @HandlerOutput(name="MessageTraceMBean", type=MessageTrace.class)})
    public static void getMessageTraceMBean(HandlerContext handlerCtx) {
        String webServiceKey =(String)handlerCtx.getInputValue("WebServiceKey");
        String server = (String)handlerCtx.getInputValue("Server");
        String msgID = (String)handlerCtx.getInputValue("MessageID");
        Set<WebServiceEndpoint> wsSet 
                = AMXUtil.getWebServiceMgr().getWebServiceEndpointSet(webServiceKey, server);
        MessageTrace[] msgs = null;
        if (!wsSet.isEmpty()){  // should be only one.
            Iterator it = wsSet.iterator();
            WebServiceEndpoint ws = (WebServiceEndpoint) it.next();
            msgs = ws.getMessagesInHistory();
        }
        
        MessageTrace mt = null;
        if (msgs != null) {
            for (int i=0; i < msgs.length; i++) {
                if (msgID.equals(msgs[i].getMessageID())) {
                    mt = msgs[i];
                }
            }
        }
        handlerCtx.setOutputValue("MessageTraceMBean", mt);
    }
    
    /**
     *	<p> Returns the List of all messsages of a web service</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Server" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "FilterValue" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "WebServiceMessageData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebServiceMessageData",
        input={
            @HandlerInput(name="WebServiceKey", type=String.class, required=true),
            @HandlerInput(name="Server", type=String.class, required=true),
            @HandlerInput(name="FilterValue", type=String.class)},
        output={
            @HandlerOutput(name="WebServiceMessageData", type=List.class)})
    public static void getWebServiceMessageData(HandlerContext handlerCtx) {
        String webServiceKey =(String)handlerCtx.getInputValue("WebServiceKey");
        String server = (String)handlerCtx.getInputValue("Server");
        String filterValue =(String)handlerCtx.getInputValue("FilterValue");
        Set<WebServiceEndpoint> wsSet 
                = AMXUtil.getWebServiceMgr().getWebServiceEndpointSet(webServiceKey, server);
        MessageTrace[] msgs = null;
        if (!wsSet.isEmpty()){  // should be only one.
            Iterator it = wsSet.iterator();
            WebServiceEndpoint ws = (WebServiceEndpoint) it.next();
            msgs = ws.getMessagesInHistory();
        }

        List dataList = new ArrayList();
        if (msgs != null) {
            for (int i=0; i < msgs.length; i++) {
                Map msgMap = msgs[i].asMap();                
                String code = msgs[i].getFaultCode();
                
                if (filterValue == null
                || code == null && filterValue.equals(GuiUtil.getMessage("common.Success"))   //match the value in the table xml
                || code != null && filterValue.equals(GuiUtil.getMessage("common.Failed"))
                || filterValue.equals("")) {
                    
                    // format timestamp
                    Long L = new Long(msgs[i].getTimestamp());
                    long l = L.longValue();
                    Date date = new Date(l);
                    msgMap.put("TimeStampFormatted",date.toString());
                    
                    // format size
                    int reqSize = msgs[i].getRequestSize();
                    int resSize = msgs[i].getResponseSize();
                    String reqResSize = reqSize + "b / " + resSize + "b";
                    msgMap.put("ReqResSize", reqResSize);
                    
                    //format success/failed
                    if (code == null) {
                        msgMap.put("Response", GuiUtil.getMessage("common.Success"));
                        msgMap.put("FaultCode", "");
                    } else {
                        msgMap.put("Response", GuiUtil.getMessage("common.Failed"));
                    }
                    if (msgs[i].getFaultActor() == null) {
                        msgMap.put("FaultActor", "");
                    }
                    if (msgMap.get("FaultString") == null) {
                        msgMap.put("FaultString", "");
                    }
                    
                    // url encode HTTP request/response headers
                    String reqHeaders = msgs[i].getHTTPRequestHeaders();
                    String resHeaders = msgs[i].getHTTPResponseHeaders();
                    String reqHeadersEnc = "";
                    String resHeadersEnc = "";
                    try {
                        if (reqHeaders != null) {
                            reqHeadersEnc = URLEncoder.encode(reqHeaders, "UTF-8");
                        }
                        if (resHeaders != null) {
                            resHeadersEnc = URLEncoder.encode(resHeaders, "UTF-8");
                        }
                    } catch (UnsupportedEncodingException uee) {
                        LogUtil.severe("Error encoding HTTP Request/Response Headers", uee);
                        if (reqHeaders != null) reqHeadersEnc = URLEncoder.encode(reqHeaders);
                        if (resHeaders != null)resHeadersEnc = URLEncoder.encode(resHeaders);
                    }
                    msgMap.put("RequestHeadersEncoded", reqHeadersEnc);
                    msgMap.put("ResponseHeadersEncoded", resHeadersEnc);
                    
                    if (msgMap.get("PrincipalName") == null) {
                        msgMap.put("PrincipalName", "");
                    }
                                        
                    if (msgs[i].isCallFlowEnabled()) {
                        CallFlowMonitor cfm = CallFlowHandlers.getCallFlowMonitor(server);
                        List list = cfm.queryCallStackForRequest(msgs[i].getMessageID());
                        if (list == null || list.isEmpty()) {
                            msgMap.put("IsCallFlowAvailable", Boolean.FALSE);
                        } else {
                            msgMap.put("IsCallFlowAvailable", Boolean.TRUE);
                        }
                    } else {
                        msgMap.put("IsCallFlowAvailable", Boolean.FALSE);
                    }
                    
                    dataList.add(msgMap);
                }
            }
        }
        handlerCtx.setOutputValue("WebServiceMessageData", dataList);
    }
    
    /**
     *	<p> Returns the filter list for Messages Table.
     *  <p> Output  value: "FilterListValue" -- Type: <code>java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWSMessageFilters",
    output={
        @HandlerOutput(name="FilterListValue", type=java.util.List.class),
        @HandlerOutput(name="FilterListLabel", type=java.util.List.class)}
    )
    public static void getWSMessageFilters(HandlerContext handlerCtx) {
        List label=new ArrayList();
        label.add(GuiUtil.getMessage("common.AllItems"));
        label.add(GuiUtil.getMessage("common.Success"));
        label.add(GuiUtil.getMessage("common.Failed"));

        List value = new ArrayList();
        value.add("");
        value.add(GuiUtil.getMessage("common.Success"));
        value.add(GuiUtil.getMessage("common.Failed"));
        
        handlerCtx.setOutputValue("FilterListValue", value);
        handlerCtx.setOutputValue("FilterListLabel", label);
    }
    
   /**
     *	<p> Get the monitoring level and max message history size of a Web Service</p>
     *
     *  <p> Input value: "WebServiceName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AppName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Type" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "IsStandalone" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Level" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "History" -- Type: <code>java.lang.String</code></p>
     * 
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWSConfig",
        input={
            @HandlerInput(name="WebServiceName", type=String.class, required=true),
            @HandlerInput(name="AppName", type=String.class, required=true),
            @HandlerInput(name="BundleName", type=String.class, required=true),
            @HandlerInput(name="Type", type=String.class, required=true),
            @HandlerInput(name="IsStandalone", type=Boolean.class, required=true)},
        output={
            @HandlerOutput(name="Level", type=String.class),
            @HandlerOutput(name="History", type=String.class)})
    public static void getWSConfig(HandlerContext handlerCtx) {
        String wsName = (String)handlerCtx.getInputValue("WebServiceName");
        String appName = (String)handlerCtx.getInputValue("AppName");
        String bundleName = (String)handlerCtx.getInputValue("BundleName");
        String type = (String)handlerCtx.getInputValue("Type");
        Boolean isStandalone = (Boolean)handlerCtx.getInputValue("IsStandalone");
        WebServiceEndpointConfig wsConfig 
            = getWebServiceEndpointConfig(appName, bundleName, wsName, type, isStandalone);
        
        String level;
        String history;
        if (wsConfig == null) {
            level = "OFF";
            history = "25";
        } else {
            level = wsConfig.getMonitoringLevel();
            history = wsConfig.getMaxHistorySize();
        }
       
        handlerCtx.setOutputValue("Level", level);
        handlerCtx.setOutputValue("History", history);
    }
    
    /**
     *	<p> Edit/Save the WebServiceEndpointConfig MBean</p>
     *
     *  <p> Input value: "WebServiceName" -- Type: <code>java.lang.String</code>/p>
     *  <p> Input value: "AppName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Type" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "IsStandalone" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Level" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "History" -- Type: <code>java.lang.String</code></p>
     * 
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveWSConfig",
        input={
            @HandlerInput(name="WebServiceName", type=String.class, required=true),
            @HandlerInput(name="AppName", type=String.class, required=true),
            @HandlerInput(name="BundleName", type=String.class, required=true),
            @HandlerInput(name="Type", type=String.class, required=true),
            @HandlerInput(name="IsStandalone", type=Boolean.class, required=true),
            @HandlerInput(name="Level", type=String.class),
            @HandlerInput(name="History", type=String.class)})
    public static void saveWSConfig(HandlerContext handlerCtx) {
        String wsName = (String)handlerCtx.getInputValue("WebServiceName");
        String appName = (String)handlerCtx.getInputValue("AppName");
        String bundleName = (String)handlerCtx.getInputValue("BundleName");
        String type = (String)handlerCtx.getInputValue("Type");
        Boolean isStandalone = (Boolean)handlerCtx.getInputValue("IsStandalone");
        String level = (String)handlerCtx.getInputValue("Level");
        String history = (String)handlerCtx.getInputValue("History");
        try{
            WebServiceEndpointConfig wsConfig 
                = getWebServiceEndpointConfig(appName, bundleName, wsName, type, isStandalone);
            if (wsConfig == null) {
                try {
                    wsConfig = createWebServiceEndpointConfig(appName, bundleName, wsName, type, isStandalone);
                } catch(Exception ex) {
                    GuiUtil.handleException(handlerCtx, ex);
                }
            }
            if (wsConfig != null) {
                wsConfig.setMonitoringLevel(level);
                wsConfig.setMaxHistorySize(history);
            } 
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> Resets monitoring statistics on WebServiceEndpoint MBean</p>
     *
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code> 
     *  <p> Input value: "Server" -- Type: <code>java.lang.String</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="resetWsStats",
        input={
            @HandlerInput(name="WebServiceKey", type=String.class, required=true),
            @HandlerInput(name="Server", type=String.class)})
    public void resetWsStats(HandlerContext handlerCtx) {
	String webServiceKey = (String) handlerCtx.getInputValue("WebServiceKey");
        String server = (String) handlerCtx.getInputValue("Server");   
        try{
            WebServiceMgr wsm = AMXUtil.getWebServiceMgr();
            Set endpointSet = wsm.getWebServiceEndpointSet(webServiceKey, server);
            if (!endpointSet.isEmpty()){  // should be only one.
                Iterator it = endpointSet.iterator();
                WebServiceEndpoint endpoint = (WebServiceEndpoint) it.next();
                endpoint.resetStats();
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> Returns the List of all registries the web service is published to.</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "WSPublishedRegisitriesData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWSPublishedRegistriesData",
        input={
            @HandlerInput(name="WebServiceKey", type=Object.class, required=true)},
        output={
            @HandlerOutput(name="WSPublishedRegistriesData", type=List.class)})
    public void getWSPublishedRegistriesData(HandlerContext handlerCtx) {
        Object webServiceKey = (Object)handlerCtx.getInputValue("WebServiceKey");  
        Map registryConfigMap = getRegistryLocationConfigMap(webServiceKey);
        List dataList = new ArrayList();
        if (registryConfigMap != null && !registryConfigMap.isEmpty()) {
            for (Iterator iter = registryConfigMap.values().iterator(); iter.hasNext();) {
                RegistryLocationConfig registry = (RegistryLocationConfig)iter.next();
                Map registryMap = new HashMap();
                registryMap.put("RegistryName", registry.getConnectorResourceJNDIName());
                registryMap.put("selected", false);
                dataList.add(registryMap);
            }
        }
        handlerCtx.setOutputValue("WSPublishedRegistriesData", dataList);
    }
    
    /**
     *	<p> Returns the List of all available registries.</p>
     * 
     *  <p> Output value: "AvailableRegistriesList" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAvailableRegistriesList",
        output={
            @HandlerOutput(name="AvailableRegistriesList", type=List.class)})
    public void getAvailableRegistriesList(HandlerContext handlerCtx) {
        WebServiceMgr wsm = (WebServiceMgr)AMXUtil.getDomainRoot().getWebServiceMgr();
        String[] registries = wsm.listRegistryLocations();
        List registryList = new ArrayList();
        if (registries != null) {
            for (int i=0; i < registries.length; i++) {
                registryList.add(registries[i]);
            }
        }
        handlerCtx.setOutputValue("AvailableRegistriesList", registryList);       
    }
    
    /**
     *	<p> Publishes the web service to the selected registreis.</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.Object</code></p>
     *  <p> Input value: "RegistriesSelected" -- Type: <code>java.lang.String[]</code></p>
     *  <p> Input value: "LBHost" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "LBPort" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "LBSSLPort" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Categories" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Description" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Organization" -- Type: <code>java.lang.String</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="publishToRegistry",
        input={
            @HandlerInput(name="WebServiceKey", type=Object.class, required=true),
            @HandlerInput(name="RegistriesSelected", type=String[].class, required=true),
            @HandlerInput(name="LBHost", type=String.class),
            @HandlerInput(name="LBPort", type=String.class),
            @HandlerInput(name="LBSSLPort", type=String.class),
            @HandlerInput(name="Categories", type=String.class),
            @HandlerInput(name="Description", type=String.class),
            @HandlerInput(name="Organization", type=String.class)})
    public void publishToRegistry(HandlerContext handlerCtx) {
        Object webServiceKey = (Object)handlerCtx.getInputValue("WebServiceKey");
        String[] registries = (String[])handlerCtx.getInputValue("RegistriesSelected");
        if (registries !=  null && registries.length > 0) {
            HashMap map = new HashMap();
            map.put(WebServiceMgr.LB_HOST_KEY, (String)handlerCtx.getInputValue("LBHost"));
            map.put(WebServiceMgr.LB_PORT_KEY, (String)handlerCtx.getInputValue("LBPort"));
            map.put(WebServiceMgr.LB_SECURE_PORT, (String)handlerCtx.getInputValue("LBSSLPort"));
            map.put(WebServiceMgr.CATEGORIES_KEY, (String)handlerCtx.getInputValue("Categories"));
            map.put(WebServiceMgr.DESCRIPTION_KEY, (String)handlerCtx.getInputValue("Description"));
            map.put(WebServiceMgr.ORGANIZATION_KEY, (String)handlerCtx.getInputValue("Organization"));
            WebServiceMgr wsm = (WebServiceMgr)AMXUtil.getDomainRoot().getWebServiceMgr();
            try {
                wsm.publishToRegistry(registries, webServiceKey, map);
            } catch (Exception ex) {
                GuiUtil.handleException(handlerCtx, ex);
            }
        }
    }
    
    /**
     *	<p> Publishes the web service to the selected registries.</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RegistriesSelectedList" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="unpublishFromRegistry",
        input={
            @HandlerInput(name="WebServiceKey", type=Object.class, required=true),
            @HandlerInput(name="RegistriesSelectedList", type=List.class, required=true)})
    public void unpublishFromRegistry(HandlerContext handlerCtx) {
        Object webServiceKey = (Object)handlerCtx.getInputValue("WebServiceKey");
        List obj = (List)handlerCtx.getInputValue("RegistriesSelectedList");
        List<Map> registriesSelected = (List) obj;
        if (obj == null || obj.size() == 0)
            return;
        String[] registries = new String[obj.size()];
        try{
            int i=0;
            for(Map registry : registriesSelected){
                String registryName = (String) registry.get("RegistryName");
                registries[i++] = registryName;
            }
            WebServiceMgr wsm = (WebServiceMgr)AMXUtil.getDomainRoot().getWebServiceMgr();
            wsm.unpublishFromRegistry(registries, webServiceKey);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> Returns the List of all available registries.</p>
     * 
     *  <p> Output value: "WSRegistriesData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWSRegistriesData",
        output={
            @HandlerOutput(name="RegistriesData", type=List.class)})
    public void getWSRegistriesData(HandlerContext handlerCtx) {
        List dataList = new ArrayList();
        try{
            WebServiceMgr wsm = (WebServiceMgr)AMXUtil.getDomainRoot().getWebServiceMgr();
            String[] registries = wsm.listRegistryLocations();
            if (registries != null) {
                for (int i=0; i < registries.length; i++) {
                    Map registryMap = new HashMap();
                    registryMap.put("RegistryName", registries[i]);
                    registryMap.put("selected", false);
                    dataList.add(registryMap);
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("RegistriesData", dataList);
    }
    
    /**
     *	<p> Publishes the web service to the selected registreis.</p>
     * 
     *  <p> Input value: "JNDIName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RegistryType" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "PublishURL" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "QueryURL" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "UserName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Password" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Description" -- Type: <code>java.lang.String</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="addRegistry",
        input={
            @HandlerInput(name="JNDIName", type=String.class, required=true),
            @HandlerInput(name="RegistryType", type=String.class, required=true),
            @HandlerInput(name="PublishURL", type=String.class),
            @HandlerInput(name="QueryURL", type=String.class),
            @HandlerInput(name="UserName", type=String.class),
            @HandlerInput(name="Password", type=String.class),
            @HandlerInput(name="Description", type=String.class)})
    public void addRegistry(HandlerContext handlerCtx) {
        String jndiName = (String)handlerCtx.getInputValue("JNDIName");
        String registryType = (String)handlerCtx.getInputValue("RegistryType");
        String publishURL = (String)handlerCtx.getInputValue("PublishURL");
        String queryURL = (String)handlerCtx.getInputValue("QueryURL");
        String userName = (String)handlerCtx.getInputValue("UserName");
        String password = (String)handlerCtx.getInputValue("Password");
        String description = (String)handlerCtx.getInputValue("Description");
        Map map = new HashMap();
	String type = WebServiceMgr.EBXML_KEY;
	if (registryType.equalsIgnoreCase (WebServiceMgr.UDDI_KEY)){
		type = WebServiceMgr.UDDI_KEY;
	} else {
		type = WebServiceMgr.EBXML_KEY;
	} 
        map.put(WebServiceMgr.PUBLISH_URL_KEY,  publishURL);
        if (!GuiUtil.isEmpty(queryURL)) {
            map.put(WebServiceMgr.QUERY_URL_KEY, queryURL);
        }
        if (!GuiUtil.isEmpty(userName)) {
            map.put(WebServiceMgr.USERNAME_KEY, userName);
        }
        if (!GuiUtil.isEmpty(password)) {
            map.put(WebServiceMgr.PASSWORD_KEY, password);
        }
        WebServiceMgr wsm = (WebServiceMgr)AMXUtil.getDomainRoot().getWebServiceMgr();
        try {
            wsm.addRegistryConnectionResources(jndiName, description, type, map);
        } catch (Exception e) {
            GuiUtil.handleException(handlerCtx, e);
        }
    }
    
    /**
     *	<p> Removes selected registries</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RegistriesSelectedList" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="removeRegistries",
        input={
            @HandlerInput(name="RegistriesList", type=List.class, required=true)})
    public void removeRegistries(HandlerContext handlerCtx) {
        List obj = (List)handlerCtx.getInputValue("RegistriesList");
        List<Map> registriesSelected = (List) obj;
        if (obj == null || obj.size() == 0)
            return;
        try{
            WebServiceMgr wsm = (WebServiceMgr)AMXUtil.getDomainRoot().getWebServiceMgr();
            int i=0;
            for(Map registry : registriesSelected){
                String registryName = (String) registry.get("RegistryName");
                wsm.removeRegistryConnectionResources(registryName);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> Returns the List of all transformation rules for the web service.</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "TransformationRulesData" -- Type: <code>java.util.List</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWSTransformationRulesData",
        input={
            @HandlerInput(name="WebServiceKey", type=Object.class, required=true)},
        output={
            @HandlerOutput(name="TransformationRulesData", type=List.class)})
    public void getWSTransformationRulesData(HandlerContext handlerCtx) {
        String webServiceKey = (String)handlerCtx.getInputValue("WebServiceKey");
        List dataList = new ArrayList();
        List ruleList = getTransformationRuleConfigList(webServiceKey);
        try{
            if (ruleList != null && !ruleList.isEmpty()) {
                for (Iterator iter = ruleList.iterator(); iter.hasNext();) {
                    TransformationRuleConfig ruleConfig = (TransformationRuleConfig)iter.next();
                    Map ruleMap = new HashMap();
                    ruleMap.put("RuleName", ruleConfig.getName());
                    if (ruleConfig.getEnabled()) {
                        ruleMap.put("RuleStatus", GuiUtil.getMessage("common.Enabled"));
                    } else {
                        ruleMap.put("RuleStatus", GuiUtil.getMessage("common.Disabled"));
                    }
                    ruleMap.put("RuleFile", ruleConfig.getRuleFileLocation());
                    ruleMap.put("ApplyTo", ruleConfig.getApplyTo());
                    ruleMap.put("selected", false);
                    dataList.add(ruleMap);
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("TransformationRulesData", dataList);
    }
    
    /**
     *	<p> Enables the selected transformation rules for the web service.</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RulesSelectedList" -- Type: <code>java.util.List</code></p>
     *  <p> Input value: "Action" -- Type: <code>java.lang.String</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="applyTransformationRuleTableAction",
        input={
            @HandlerInput(name="WebServiceKey", type=Object.class, required=true),
            @HandlerInput(name="RulesSelectedList", type=List.class, required=true),
            @HandlerInput(name="Action", type=String.class, required=true)})
    public void applyTransformationRuleTableAction(HandlerContext handlerCtx) {
        String webServiceKey = (String)handlerCtx.getInputValue("WebServiceKey");
        List obj = (List)handlerCtx.getInputValue("RulesSelectedList");
        String action = (String)handlerCtx.getInputValue("Action");
        List<Map> rulesSelected = (List) obj;
        if (obj == null || obj.size() == 0)
            return;
        String[] rules = new String[obj.size()];
        try{
            int i=0;
            for(Map rule : rulesSelected){
                String ruleName = (String) rule.get("RuleName");
                rules[i++] = ruleName;
            }
            WebServiceEndpointConfig endpointConfig = getWebServiceEndpointConfig(webServiceKey);
            if (endpointConfig != null) {
                Map ruleMap = endpointConfig.getTransformationRuleConfigMap();
                for (int j=0; j < rules.length; j++) {
                    TransformationRuleConfig rule = (TransformationRuleConfig)ruleMap.get(rules[j]);
                    if (action.equals("enable")) rule.setEnabled(true);
                    if (action.equals("disable")) rule.setEnabled(false);
                    if (action.equals("remove")) endpointConfig.removeTransformationRuleConfig(rules[j]);
                }
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }        
    }
    
    /**
     *	<p> Adds a transformation rule to the web service.</p>
     * 
     *  <p> Input value: "WebServiceKey" -- Type: <code>java.lang.Object</code></p>
     *  <p> Input value: "RuleName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Enabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "ApplyTo" -- Type: <code>java.lang.String</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="addTransformationRule",
        input={
            @HandlerInput(name="WebServiceKey", type=Object.class, required=true),
            @HandlerInput(name="RuleName", type=String.class, required=true),
            @HandlerInput(name="Enabled", type=Boolean.class, required=true),
            @HandlerInput(name="ApplyTo", type=String.class, required=true),
            @HandlerInput(name="UploadDir", type=String.class, required=true)})
    public void addTransformationRule(HandlerContext handlerCtx) {
        Object webServiceKey = (Object)handlerCtx.getInputValue("WebServiceKey");
        String name = (String)handlerCtx.getInputValue("RuleName");
        boolean enabled = ((Boolean)(handlerCtx.getInputValue("Enabled"))).booleanValue();
        String applyTo = (String)handlerCtx.getInputValue("ApplyTo");
        String uploadDir = (String)handlerCtx.getInputValue("UploadDir");
        File tmpFile = null;
        if(uploadDir != null) {
            try {
                WebServiceMgr wsm = (WebServiceMgr)AMXUtil.getDomainRoot().getWebServiceMgr();
                WebServiceHandlers.createTransformationRuleConfig(
                        webServiceKey, name, enabled, applyTo, uploadDir, null);
            } catch (Exception ex) {
                GuiUtil.handleException(handlerCtx, ex);
            }
        }
    }
    
    private static RegistryLocationConfig getRegistryLocationConfig(String webServiceKey, String registryName) {
        return (RegistryLocationConfig)getRegistryLocationConfigMap(webServiceKey).get(registryName);
    }
    
    private static Map getRegistryLocationConfigMap(Object webServiceKey) {
        WebServiceEndpointConfig wseConfig = getWebServiceEndpointConfig(webServiceKey);
        return wseConfig == null ? null : wseConfig.getRegistryLocationConfigMap();
    }
    
    private static TransformationRuleConfig createTransformationRuleConfig(Object webServiceKey, String ruleName, boolean enabled, String applyTo, String ruleFileLocation, Map map) {
        WebServiceEndpointConfig wseConfig = getWebServiceEndpointConfig(webServiceKey);
	if (wseConfig == null) {
            wseConfig = createWebServiceEndpointConfig(webServiceKey);
        }        
        return wseConfig.createTransformationRuleConfig(ruleName, ruleFileLocation, enabled, applyTo, map);
    }
    
    private static List getTransformationRuleConfigList(String webServiceKey) {
        WebServiceEndpointConfig wseConfig = getWebServiceEndpointConfig(webServiceKey);
        return wseConfig == null ? null : wseConfig.getTransformationRuleConfigList();
    }
    
    private static WebServiceEndpointConfig createWebServiceEndpointConfig(String appName, String bundleName, String wsName, String type, Boolean isStandalone) {
        WebServiceEndpointConfig wsConfig = null;
        String fullQualName = buildFullyQualifiedWSName(bundleName, wsName, isStandalone);
        if (!isStandalone) {
            J2EEApplicationConfig appConfig
                =  AMXUtil.getDomainConfig().getJ2EEApplicationConfigMap().get(appName);
            wsConfig = appConfig.createWebServiceEndpointConfig(fullQualName, null);
        } else {
            if (type.equals(WebServiceEndpointInfo.SERVLET_IMPL)) {
                WebModuleConfig webConfig
                    = AMXUtil.getDomainConfig().getWebModuleConfigMap().get(appName);
                wsConfig = webConfig.createWebServiceEndpointConfig(fullQualName, null);
            } else {
                EJBModuleConfig ejbConfig
                    = AMXUtil.getDomainConfig().getEJBModuleConfigMap().get(appName);
                wsConfig = ejbConfig.createWebServiceEndpointConfig(fullQualName, null);
            }
        }
        return wsConfig;
    }
    
    private static WebServiceEndpointConfig createWebServiceEndpointConfig(Object webServiceKey) {
        WebServiceEndpointInfo wsInfo = AMXUtil.getWebServiceEndpointInfo(webServiceKey);
        String appName = wsInfo.getAppID();
        String wsName = wsInfo.getName();
        String bundleName = wsInfo.getBundleName();
        String type = wsInfo.getServiceImplType();
        boolean isStandalone = wsInfo.isAppStandaloneModule();
        WebServiceEndpointConfig wsConfig = null;
        String fullQualName = buildFullyQualifiedWSName(bundleName, wsName, isStandalone);
        if (!isStandalone) {
            J2EEApplicationConfig appConfig
                =  AMXUtil.getDomainConfig().getJ2EEApplicationConfigMap().get(appName);
            wsConfig = appConfig.createWebServiceEndpointConfig(fullQualName, null);
        } else {
            if (type.equals(WebServiceEndpointInfo.SERVLET_IMPL)) {
                WebModuleConfig webConfig
                    = AMXUtil.getDomainConfig().getWebModuleConfigMap().get(appName);
                wsConfig = webConfig.createWebServiceEndpointConfig(fullQualName, null);
            } else {
                EJBModuleConfig ejbConfig
                    = AMXUtil.getDomainConfig().getEJBModuleConfigMap().get(appName);
                wsConfig = ejbConfig.createWebServiceEndpointConfig(fullQualName, null);
            }
        }
        return wsConfig;
    }
    
    private static WebServiceEndpointConfig getWebServiceEndpointConfig(Object webServiceKey) {
        WebServiceEndpointInfo wsInfo = AMXUtil.getWebServiceEndpointInfo(webServiceKey);
        String appName = wsInfo.getAppID();
        String bundleName = wsInfo.getBundleName();
        String wsName = wsInfo.getName();
        String type = wsInfo.getServiceImplType();
        boolean isStandalone = wsInfo.isAppStandaloneModule();
        WebServiceEndpointConfig wsConfig = null;
        Map<String,WebServiceEndpointConfig> map = null;
        if (!isStandalone) {
            J2EEApplicationConfig appConfig
                =  AMXUtil.getDomainConfig().getJ2EEApplicationConfigMap().get(appName);
            map = appConfig.getWebServiceEndpointConfigMap();
        } else {
            if (type.equals(WebServiceEndpointInfo.SERVLET_IMPL)) {
                WebModuleConfig webConfig
                    = AMXUtil.getDomainConfig().getWebModuleConfigMap().get(appName);
                map = webConfig.getWebServiceEndpointConfigMap();
            } else {
                EJBModuleConfig ejbConfig
                    = AMXUtil.getDomainConfig().getEJBModuleConfigMap().get(appName);
                map = ejbConfig.getWebServiceEndpointConfigMap();
            }
        }
        String fullQualName = buildFullyQualifiedWSName(bundleName, wsName, isStandalone);
        wsConfig = map.get(fullQualName);
        return wsConfig;
    }  
      
    private static WebServiceEndpointConfig getWebServiceEndpointConfig(String appName, String bundleName, String wsName, String type, Boolean isStandalone) {
        WebServiceEndpointConfig wsConfig = null;
        Map<String,WebServiceEndpointConfig> map = null;
        if (!isStandalone) {
            J2EEApplicationConfig appConfig
                =  AMXUtil.getDomainConfig().getJ2EEApplicationConfigMap().get(appName);
            map = appConfig.getWebServiceEndpointConfigMap();
        } else {
            if (type.equals(WebServiceEndpointInfo.SERVLET_IMPL)) {
                WebModuleConfig webConfig
                    = AMXUtil.getDomainConfig().getWebModuleConfigMap().get(appName);
                map = webConfig.getWebServiceEndpointConfigMap();
            } else {
                EJBModuleConfig ejbConfig
                    = AMXUtil.getDomainConfig().getEJBModuleConfigMap().get(appName);
                map = ejbConfig.getWebServiceEndpointConfigMap();
            }
        }
        String fullQualName = buildFullyQualifiedWSName(bundleName, wsName, isStandalone);
        wsConfig = map.get(fullQualName);
        return wsConfig;
    }
    
    private static Map<Object, String> getWSKeys() {
        return AMXUtil.getWebServiceMgr().getWebServiceEndpointKeys();
    }
    
    private static String buildFullyQualifiedWSName(String bundleName, String wsName, Boolean isStandalone) {
        String fullQualName = "";
        if (isStandalone) {
            fullQualName = wsName;
        } else {
            fullQualName = bundleName + "#" + wsName;
        }
        return fullQualName;
    }
}
