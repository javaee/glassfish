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
 * CallFlowHandlers.java
 *
 * Created on Sept 21, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.handlers;

//import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.MSVValidator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.handlers.NavigationHandlers;
import com.sun.jsftemplating.component.ComponentUtil;

import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.monitor.CallFlowMonitor;

import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;

import javax.faces.component.UIComponent;
import com.sun.webui.jsf.component.Tree;
import com.sun.webui.jsf.component.TreeNode;

/**
 *
 * @author Anissa Lam
 */
public class CallFlowHandlers {
    
    private static String SUCCESS="success";
    private static String FAILED="failed";
    private static String[] ADMIN_APP_PREFIX = 
        {"uri:/asadmin/",
         "uri:/admingui/",
         "uri:/theme/",
         "uri:/docroot/",
         "uri:/images/",
         "uri:/js/",
         "uri:/redirect.html",
         "uri:/com_sun_web_ui/",
         "uri:/favicon.ico",
         "uri:/web1/",
         "uri:/admin-jsf/",
         "uri:/resource/libs/",
         "uri:/theme/com/sun/webui/jsf/",
         "uri:/commonTask/",
         "uri:/clusterCommonTask/",
         "uri:/peTree.jsf",
         "uri:/header.jsf",
         "uri:/index.jsf",
         "uri:/login.jsf",
         "uri:/clusterProfileTree.jsf",
         "uri:/appServer",
        "uri:/clusterCommonTask/",
        "uri:/peTree.jsf/",
        "uri:/header.jsf/",
        "uri:/login.jsf/",
        "uri:/clusterProfileTree.jsf/",
        "uri:/appServer/",
        "uri:/resourceNode/",
        "uri:/resource/images/",
        "uri:/resource/js/",
        "uri:/applications/",
        "uri:/cluster/",
        "uri:/configuration/",
        "uri:/domain/",
        "uri:/mgmtRules/",
        "uri:/nodeAgent/",
        "uri:/standalone/",
        "uri:/webService/",
        "uri:/admin-jsf/",
        "uri:/resource/com_sun_faces_ajax.js",
        "uri:/resources/",
         "uri:/glue.js"
         
         };
            
    private static final String LOCALHOST="127.0.0.1";
    private static final String END_TIMESTAMP_KEY = "end_timestamp";
   
    
    
    /** Creates a new instance of CallFlowHandlers */
    public CallFlowHandlers() {
    }
    
    /**
     *	<p> This handler returns the call flow config mbean info
     *
     *  <p> Input value: "instanceName" -- Type: <code>java.lang.String</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getCallFlowConfigInfo",
	input={
	    @HandlerInput(name="instanceName", type=String.class)},
	output={
            @HandlerOutput(name="host", type=String.class),
            @HandlerOutput(name="user", type=String.class),
            @HandlerOutput(name="enabled", type=Boolean.class),
            @HandlerOutput(name="hasMonitor", type=Boolean.class) }
    )
        public static void getCallFlowConfigInfo(HandlerContext handlerCtx) {
	    String instanceName = (String) handlerCtx.getInputValue("instanceName");
	    CallFlowMonitor cm = getCallFlowMonitor((String) handlerCtx.getInputValue("instanceName"));
            if(cm == null){
                handlerCtx.setOutputValue("hasMonitor", Boolean.FALSE);
                handlerCtx.setOutputValue("host", "");
                handlerCtx.setOutputValue("user", "");
                handlerCtx.setOutputValue("enabled", Boolean.FALSE);
            }else{
                handlerCtx.setOutputValue("hasMonitor", Boolean.TRUE);
                handlerCtx.setOutputValue("host", cm.getCallerIPFilter());
                handlerCtx.setOutputValue("user", cm.getCallerPrincipalFilter());
                handlerCtx.setOutputValue("enabled", cm.getEnabled());
            }
    }
    
    /**
     *	<p> This handler saves the call flow config mbean info
     *
     *  <p> Input value: "instanceName" -- Type: <code>java.lang.String</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveCallFlowConfigInfo",
	input={
	    @HandlerInput(name="instanceName", type=String.class),
            @HandlerInput(name="host", type=String.class),
            @HandlerInput(name="user", type=String.class),
            @HandlerInput(name="enabled", type=Boolean.class)}
        )
    public static void saveCallFlowConfigInfo(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        try{
	    CallFlowMonitor cm = getCallFlowMonitor((String) handlerCtx.getInputValue("instanceName"));
            
            cm.setCallerIPFilter((String) handlerCtx.getInputValue("host"));
            cm.setCallerPrincipalFilter ((String) handlerCtx.getInputValue("user"));
            cm.setEnabled((Boolean) handlerCtx.getInputValue("enabled"));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx,ex);
        }
    }
    
    
    /**
     *	<p> This handler returns the call flow monitor mbean
     *
     *  <p> Input value: "instanceName" -- Type: <code>java.lang.String</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="clearCallFlowData",
	input={
	    @HandlerInput(name="instanceName", type=String.class)}
    )
    public static void clearCallFlowData(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        CallFlowMonitor cm = getCallFlowMonitor((String) handlerCtx.getInputValue("instanceName"));
        if (cm == null)
            return;
        try{
            cm.clearData() ;
        }catch (Exception ex){
            GuiUtil.prepareException(handlerCtx, ex);
        }
    }
   
     /**
     *	<p> This handler returns the list of call flow data for populating the table.
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "filterValue" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getCallFlowDataList",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true),
            @HandlerInput(name="filterValue", type=String.class),
            @HandlerInput(name="demo", type=Boolean.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getCallFlowDataList(HandlerContext handlerCtx){
    
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        String filterValue = (String) handlerCtx.getInputValue("filterValue");
        Boolean demo = (Boolean) handlerCtx.getInputValue("demo");
        
        List<Map> result = new ArrayList();
        
        try {
            CallFlowMonitor cfm = getCallFlowMonitor(instanceName);
            
            if (cfm == null){
                handlerCtx.setOutputValue("result", result);
                return;
            }
            
            List<Map<String,String>> listOfMap = cfm.queryRequestInformation ();
            if (demo != null && demo){
                listOfMap = queryDemoRequestInformation();
            }else{
                if (listOfMap == null || listOfMap.isEmpty()){
                        handlerCtx.setOutputValue("result", result);
                        return;
                }
            }
            
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, GuiUtil.getLocale());
            for(Map oneRow : listOfMap){
                if( GuiUtil.isEmpty(filterValue) || includeRequest(oneRow, filterValue)){
                    Map converted =  populateOneRow(oneRow, dateFormat);
                    if (converted != null)
                        result.add(converted);
                }
            }
            handlerCtx.setOutputValue("result", result);
            return;
            
        } catch (Exception ex) {
            //backend shouldn't throw exception.
            handlerCtx.setOutputValue("result", result);
            return;
        }
    }
    
    static private Map populateOneRow(Map oneRow, DateFormat dateFormat){
        
        String app = (String) oneRow.get(CallFlowMonitor.APPLICATION_NAME_KEY);
        if (!GuiUtil.isEmpty(app)){
            for(int i = 0; i < ADMIN_APP_PREFIX.length; i++){
                if (app.toLowerCase().startsWith(ADMIN_APP_PREFIX[i].toLowerCase()))
                    return null;
            }
        }
        
        Map map = new HashMap();
        
        String ms =(String) oneRow.get(CallFlowMonitor.TIME_STAMP_MILLIS_KEY);
        if (!GuiUtil.isEmpty(ms)){
            map.put("timeStamp", new Long(ms));
            Date date = new Date (Long.parseLong (ms));
            String formattedTime = dateFormat.format(date);
            map.put("timeStampFormatted", formattedTime);
        }
        map.put("requestId", oneRow.get(CallFlowMonitor.REQUEST_ID_KEY));
        String clientHost = (String) oneRow.get(CallFlowMonitor.CLIENT_HOST_KEY);
        if (LOCALHOST.equals(clientHost))
            clientHost = GuiUtil.getMessage("callFlow.localhost");
        map.put("clientHost", clientHost);
        map.put("user", oneRow.get(CallFlowMonitor.USER_KEY));
        map.put("application", oneRow.get(CallFlowMonitor.APPLICATION_NAME_KEY));
        map.put("startContainer", GuiUtil.getMessage((String)oneRow.get(CallFlowMonitor.REQUEST_TYPE_KEY)));
        String responseTime = (String)oneRow.get(CallFlowMonitor.RESPONSE_TIME_KEY);
        String resp = convertNanoToMs(responseTime);
        map.put("responseTime", resp);
        map.put("hiddenResponseTime", resp);
        String status = getStatus(oneRow);
        if (SUCCESS.equals(status))
            map.put("response", GuiUtil.getMessage("common.Success") );
        else
            map.put("response", GuiUtil.getMessage("common.Failed"));
        map.put("selected", false);
        return map;
    }
    
    
     /**
     *	<p> This handler takes in selected rows, and delete the request.
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteCallFlowRequest",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="instanceName", type=String.class, required=true)}
    )
    public static void deleteCallFlowRequest(HandlerContext handlerCtx) {
        
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        if (obj == null || obj.size() == 0)
            return;
        String[] requests = new String[obj.size()];
        
        try{
            int i=0;
            for(Map oneRow : selectedRows){
                String requestId = (String) oneRow.get("requestId");
                requests[i++] = requestId;
            }
            CallFlowMonitor cfm = getCallFlowMonitor(instanceName);
            cfm.deleteRequestIDs(requests);
        }catch(Exception ex){
            GuiUtil.prepareException(handlerCtx, ex);
        }
    }
    
    
     /**
     *	<p> This handler takes in selected rows, and delete the request.
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getCallFlowFilters",
    output={
        @HandlerOutput(name="filterListValue", type=java.util.List.class),
        @HandlerOutput(name="filterListLabel", type=java.util.List.class)}
    )
    public static void getCallFlowFilters(HandlerContext handlerCtx) {
        List label=new ArrayList();
        label.add(GuiUtil.getMessage("common.showAll"));
        label.add(GuiUtil.getMessage("callFlow.Success"));
        label.add(GuiUtil.getMessage("callFlow.Failed"));
        label.add(GuiUtil.getMessage("callFlow.REMOTE_WEB"));
        label.add(GuiUtil.getMessage("callFlow.REMOTE_WEB_SERVICE"));
        label.add(GuiUtil.getMessage("callFlow.REMOTE_EJB"));
        label.add(GuiUtil.getMessage("callFlow.TIMER_EJB"));
        label.add(GuiUtil.getMessage("callFlow.REMOTE_ASYNC_MESSAGE"));
        
        List value = new ArrayList();
        value.add("");
        value.add(SUCCESS);
        value.add(FAILED);
        value.add(CallFlowMonitor.REMOTE_WEB);
        value.add(CallFlowMonitor.REMOTE_WEB_SERVICE);
        value.add(CallFlowMonitor.REMOTE_EJB);
        value.add(CallFlowMonitor.TIMER_EJB);
        value.add(CallFlowMonitor.REMOTE_ASYNC_MESSAGE);
        
        handlerCtx.setOutputValue("filterListValue", value);
        handlerCtx.setOutputValue("filterListLabel", label);
    }
    
    
    /**
     *	<p> This handler returns all the info necessary to display the call flow detailpage
     *	@param	context	The HandlerContext.
     *
     */
    @Handler(id="getCallFlowDetail",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true),
            @HandlerInput(name="requestId", type=String.class, required=true),
            @HandlerInput(name="user", type=String.class),
            @HandlerInput(name="responseTime", type=String.class),
            @HandlerInput(name="doCharting", type=Boolean.class, required=false),
            @HandlerInput(name="demo", type=Boolean.class)},
        output={
            @HandlerOutput(name="detailInfo", type=java.util.Map.class),
            @HandlerOutput(name="chartInfo", type=java.util.Map.class),
            @HandlerOutput(name="callFlowStackMap", type=java.util.List.class),
            @HandlerOutput(name="hasCallFlowChart", type=Boolean.class)}
     )
    public static void getCallFlowDetail(HandlerContext handlerCtx) {
    
    String requestId = (String) handlerCtx.getInputValue("requestId");
    String instanceName = (String) handlerCtx.getInputValue("instanceName");
    Boolean demo = (Boolean) handlerCtx.getInputValue("demo");
    if (demo == null) demo = false;
    
    Map infoMap = new HashMap();
    List stackList = new ArrayList();
    
    CallFlowMonitor cfm = getCallFlowMonitor(instanceName);
    if (!demo && (cfm == null || requestId==null)){
        handlerCtx.setOutputValue("detailInfo", infoMap);
        handlerCtx.setOutputValue("callFlowStackMap", stackList);
        handlerCtx.setOutputValue("hasCallFlowChart", false);
         return;
    }
    try {
        List listOfMap = null;
        /*
        System.out.println("List returned by queryCallStackForRequest()");
        System.out.println(listOfMap);
        for(int i=0; i<listOfMap.size(); i++){
            System.out.println(" Map # " + i);
            System.out.println(listOfMap.get(i));
        }
         */
        if (demo != null && demo){
            listOfMap = getDemoCallFlowStack(requestId);
        }else{
            listOfMap = cfm.queryCallStackForRequest (requestId);
        }
        infoMap.put("user", handlerCtx.getInputValue("user"));
        infoMap.put("responseTime", handlerCtx.getInputValue("responseTime"));
        Map oneRow = getRow( CallFlowMonitor.CALL_STACK_REQUEST_START, listOfMap);
        if(oneRow == null) return;  //shouldn't happen
        String ms =(String) oneRow.get(CallFlowMonitor.TIME_STAMP_MILLIS_KEY);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, GuiUtil.getLocale());
        Date date = new Date (Long.parseLong(ms));
        String formattedTime = dateFormat.format(date);
        infoMap.put("timeStamp", formattedTime);

        infoMap.put("startContainer", oneRow.get(CallFlowMonitor.REQUEST_TYPE_KEY));

        //extract info from METHOD_START
        oneRow = getRow( CallFlowMonitor.CALL_STACK_METHOD_START, listOfMap);
        if (oneRow == null) return;  //shouldn't happen
        infoMap.put("application", oneRow.get(CallFlowMonitor.APPLICATION_NAME_KEY));
        //handlerCtx.setOutputValue("user", oneRow.get(CallFlowMonitor.USER_KEY));

        oneRow = getLastRow( CallFlowMonitor.CALL_STACK_METHOD_END, listOfMap);
        if(oneRow == null) return;
        String except = (String) oneRow.get(CallFlowMonitor.EXCEPTION_KEY);
        infoMap.put("exception", except);
        if (GuiUtil.isEmpty(except)){
            infoMap.put("response", GuiUtil.getMessage("common.Success") );
            infoMap.put("hasException", false );
        }else{
            infoMap.put("response", GuiUtil.getMessage("common.Failed"));
            infoMap.put("hasException", true );
        }
        
        Map timeSpendMap = null;
        if (demo != null && demo ){
            timeSpendMap = getDemoTimeSpendMap(requestId);
        }else{
            timeSpendMap = (Map<String,String>) cfm.queryPieInformation(requestId);
        }
        //By default, charting info will be generated. 
        Boolean doCharting = (Boolean )handlerCtx.getInputValue("doCharting");
        boolean hasChartData = getTimeSpendInfo(infoMap, timeSpendMap, doCharting);
        
        handlerCtx.setOutputValue("detailInfo", infoMap);
        handlerCtx.setOutputValue("callFlowStackMap", convertToDetailStackList(listOfMap)); 
        handlerCtx.setOutputValue("hasCallFlowChart", hasChartData);
        
    } catch (Exception ex) {
        ex.printStackTrace();
        handlerCtx.setOutputValue("detailInfo", infoMap);
        handlerCtx.setOutputValue("hasCallFlowChart", false);
    }
    
    }
    
    
    private static Map getRow(String callStackType, List listOfMap){
    
        Map oneRow = null;
        for(int i=0; i< listOfMap.size(); i++){
            oneRow = (Map) listOfMap.get(i);
            String type = (String) oneRow.get(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY);
            if (callStackType.equals(type))
                break;
        }
        return oneRow;
    }
    
    
    private static Map getLastRow(String callStackType, List listOfMap){
        Map oneRow = null;
        for(int i= listOfMap.size()-1; i>=0;  i--){
            oneRow = (Map) listOfMap.get(i);
            String type = (String) oneRow.get(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY);
            if (callStackType.equals(type))
                break;
        }
        return oneRow;
    }
    
    
     static private List convertToDetailStackList(List listOfMap) {
        
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, GuiUtil.getLocale());
       
            List convertedList = new ArrayList();
            if (listOfMap == null || listOfMap.isEmpty())
                return convertedList;
        try { 
            ArrayList <Map>  tmpList = new ArrayList<Map> ();
            for(int i=0; i < listOfMap.size(); i++){
                Map oneRow = (Map) listOfMap.get(i);
                String type = (String) oneRow.get(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY);
                if (CallFlowMonitor.CALL_STACK_METHOD_START.equals(type)){
                    tmpList.add(oneRow);
                }
                if (CallFlowMonitor.CALL_STACK_METHOD_END.equals(type)){
                    int lastIndex = tmpList.size()-1;
                    Map methodStart = tmpList.get(lastIndex);
                    methodStart.put(END_TIMESTAMP_KEY, oneRow.get(CallFlowMonitor.TIME_STAMP_MILLIS_KEY) );
                    tmpList.remove(lastIndex);
                }
            }
            
            int sequence = 1;
            for(int i=0; i< listOfMap.size(); i++){
                Map oneRow = (Map) listOfMap.get(i);
                String type = (String) oneRow.get(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY);
                if (CallFlowMonitor.CALL_STACK_METHOD_START.equals(type)){
                    convertedList.add(populateOneDetailRow( oneRow, dateFormat, sequence++));
                }
            }
            
        } catch (Exception ex) {
            //TODO:  Log Exception
        }
        return convertedList;
    }
    
     static private Map populateOneDetailRow(Map oneRow, DateFormat dateFormat, int sequence){

        /*** Commented out for now, refer to bug# 6365274 
//        String startTime =(String) oneRow.get(CallFlowMonitor.TIME_STAMP_MILLIS_KEY);
//        String endTime = (String) oneRow.get(END_TIMESTAMP_KEY);
//        if (!Util.isEmpty(startTime) && !Util.isEmpty(endTime)){
//            String duration = String.valueOf(Long.parseLong(endTime) - Long.parseLong(startTime));
//            model.setValue("duration", convertNanoToMs(duration));
//        }
         */
            Map converted = new HashMap();
            converted.put("container", oneRow.get(CallFlowMonitor.CONTAINER_TYPE_KEY));
            converted.put("module", oneRow.get(CallFlowMonitor.MODULE_NAME_KEY));
            converted.put("method", oneRow.get(CallFlowMonitor.METHOD_NAME_KEY));
            converted.put("component", oneRow.get(CallFlowMonitor.COMPONENT_NAME_KEY));
            converted.put("sequence", sequence);
            //converted.put("transaction", oneRow.get(CallFlowMonitor.TRANSACTION_ID_KEY));
            //converted.put("thread", oneRow.get(CallFlowMonitor.THREAD_ID_KEY));
            return converted;
    }
    
     static private boolean getTimeSpendInfo(Map infoMap,  Map<String,String> timeSpendMap, Boolean doCharting) {
         
         if (timeSpendMap == null || timeSpendMap.size() <=0)
             return false ;
         float total = 0;
         for(String key : timeSpendMap.keySet()){
             total += Float.parseFloat(timeSpendMap.get(key));
         }
         NumberFormat nf = NumberFormat.getInstance(GuiUtil.getLocale());
         nf.setMinimumFractionDigits(1);
         
         String ms = null;
         float percent = 0;
         String percentStr = "";
         
         List labelList = new ArrayList();
         
         if (timeSpendMap.containsKey("WEB_APPLICATION")){
             infoMap.put("hasWebApp", true);
             ms = timeSpendMap.get("WEB_APPLICATION");
             percent =  Float.parseFloat(ms)/total * 100;
             percentStr = nf.format(percent);
             infoMap.put("webApp", formatStr("callFlowDetail.percentMs", percentStr, ms));
             labelList.add(chartLabelMap("callFlowDetail.chart.webApp", percent, percentStr, ms));
         }else{
             infoMap.put("hasWebApp", false);
             labelList.add(emptyMap("callFlowDetail.chart.webApp"));
         }
         
         if (timeSpendMap.containsKey("WEB_CONTAINER")){
             infoMap.put("hasWebContainer", true);
             ms = timeSpendMap.get("WEB_CONTAINER");
             percent =  Float.parseFloat(ms)/total * 100;
             percentStr = nf.format(percent);
             infoMap.put("webContainer", formatStr("callFlowDetail.percentMs", percentStr, ms));
             labelList.add(chartLabelMap("callFlowDetail.chart.webCont", percent, percentStr, ms));
         }else{
             infoMap.put("hasWebContainer", false);
             labelList.add(emptyMap("callFlowDetail.chart.webCont"));
         }
         
         if (timeSpendMap.containsKey("EJB_APPLICATION")){
             infoMap.put("hasEjbApp", true);
             ms = timeSpendMap.get("EJB_APPLICATION");
             percent =  Float.parseFloat(ms)/total * 100;
             percentStr = nf.format(percent);
             infoMap.put("ejbApp", formatStr("callFlowDetail.percentMs", percentStr, ms));
             labelList.add(chartLabelMap("callFlowDetail.chart.ejbApp", percent, percentStr, ms));
         }else{
             infoMap.put("hasEjbApp", false);
             labelList.add(emptyMap("callFlowDetail.chart.ejbApp"));
         }
         
         if (timeSpendMap.containsKey("EJB_CONTAINER")){
             infoMap.put("hasEjb", true);
             ms = timeSpendMap.get("EJB_CONTAINER");
             percent =  Float.parseFloat(ms)/total * 100;
             percentStr = nf.format(percent);
             infoMap.put("ejbContainer", formatStr("callFlowDetail.percentMs", percentStr, ms));
             labelList.add(chartLabelMap("callFlowDetail.chart.ejbCont", percent, percentStr, ms));
         }else{
             infoMap.put("hasEjb", false);
             labelList.add(emptyMap("callFlowDetail.chart.ejbCont"));
         }
         
         if (timeSpendMap.containsKey("ORB_CONTAINER")){
             infoMap.put("hasOrbContainer", true);
             ms = timeSpendMap.get("ORB_CONTAINER");
             percent =  Float.parseFloat(ms)/total * 100;
             percentStr = nf.format(percent);
             infoMap.put("orbContainer", formatStr("callFlowDetail.percentMs", percentStr, ms));
             labelList.add(chartLabelMap("callFlowDetail.chart.orbCont", percent, percentStr, ms));
         }else{
             infoMap.put("hasOrbContainer", false);
             labelList.add(emptyMap("callFlowDetail.chart.orbCont"));
         }
         
         /* TODO:  if we can recreate the jmaki-chart component in the .jsf file, then 
          * we can optimize this code to NOT to generate the charting info.
          
        if (doCharting != null && !doCharting){
            return false;
        }
          
         */
         
         Collections.sort(labelList, new TimeSpentComparator());
         infoMap.put("xLabels", labelList);
         
         // create the value list that corresponds to the Labels
         /* The following will be converted into something like
            {color: 'green',    values : [95,5,0,0,0] }
          */
         List valueList = new ArrayList();
         for(int i=0;  i < labelList.size() ;  i++){
             Float ff = (Float) ((Map)labelList.get(i)).get("compValue");
             valueList.add( Math.round(ff));
         }
         Map vMap = new HashMap();
         vMap.put("color", "green");
         vMap.put("values", valueList);
         
         List vList = new ArrayList();
         vList.add(vMap);
         infoMap.put("valueList", vList);
         // return true;
         return doCharting;   //TODO should return true if jmaki:charting component can be re-created in .jsf
     }
     
     
     private static Map emptyMap(String key){
         Map aMap = new HashMap();
         aMap.put("title", "0%");
         aMap.put("label", GuiUtil.getMessage(key));
         aMap.put("compValue", Float.valueOf(0));
         return aMap;
     }
     
     private static Map chartLabelMap(String key, float percent, String percentStr, String ms ){
        Map aMap = new HashMap();
        aMap.put("label", GuiUtil.getMessage(key));
        aMap.put("title", formatStr(key+"DD", percentStr, ms));
        aMap.put("compValue", Float.valueOf(percent));
        return aMap;
     }

     private static String formatStr(String key, String percent, String ms){
         
         return GuiUtil.getMessage(key, new Object[]{percent, convertNanoToMs(ms)});
     }
     
    
    static CallFlowMonitor getCallFlowMonitor(String instanceName){
        ServerRootMonitor serverRootMonitor = AMXUtil.getServerRootMonitor(instanceName);
        if (serverRootMonitor == null)
            return null;
        CallFlowMonitor cfm = serverRootMonitor.getCallFlowMonitor();
        return cfm;
   }
    
    static private boolean includeRequest(Map oneRow, String filter){
        if (filter == null || "".equals(filter))
            return true;
        String status = getStatus(oneRow);
        String container = (String) oneRow.get(CallFlowMonitor.REQUEST_TYPE_KEY);
        
        if (filter.equalsIgnoreCase(status) ||  filter.equalsIgnoreCase(container))
            return true;
        else
            return false;
    }
    
     static private String getStatus(Map oneRow){
        String error = (String) oneRow.get(CallFlowMonitor.EXCEPTION_KEY);
        return GuiUtil.isEmpty(error) ? SUCCESS : FAILED ;
    }
     
     static private String convertFromNano(String ms, DateFormat dateFormat){
        long ns = Long.parseLong(ms);
        long ns1 = ns/1000000;
        Date dd = new Date(ns1);
        String formattedTime = dateFormat.format(dd);
        return formattedTime;
    }
    
    static private String convertNanoToMs(String nano){
        
        float ns = Float.parseFloat(nano);
        float ns1 = ns / 1000000;
        NumberFormat numberformat = NumberFormat.getInstance(GuiUtil.getLocale());
        numberformat.setMinimumFractionDigits(2);
        String str = numberformat.format(ns1);
        return str;
    }
     
    private static List queryDemoRequestInformation(){
        List listOfMap = new ArrayList();
        
        HashMap hashMap = new HashMap();
        hashMap.put(CallFlowMonitor.REQUEST_ID_KEY, "23458989" );
        hashMap.put(CallFlowMonitor.TIME_STAMP_MILLIS_KEY, ""+java.lang.System.nanoTime());
        hashMap.put(CallFlowMonitor.CLIENT_HOST_KEY, "138.243.150.122");
        hashMap.put(CallFlowMonitor.USER_KEY, "Mary");
        hashMap.put(CallFlowMonitor.REQUEST_TYPE_KEY, "REMOTE_WEB");
        hashMap.put(CallFlowMonitor.STATUS_KEY, "true");
        hashMap.put(CallFlowMonitor.RESPONSE_TIME_KEY, "34");
        hashMap.put(CallFlowMonitor.APPLICATION_NAME_KEY, "testApp");
        listOfMap.add(hashMap);
        
        HashMap hashMap2 = new HashMap();
        hashMap2.put(CallFlowMonitor.REQUEST_ID_KEY, "28881999" );
        hashMap2.put(CallFlowMonitor.TIME_STAMP_MILLIS_KEY, ""+java.lang.System.nanoTime());
        hashMap2.put(CallFlowMonitor.CLIENT_HOST_KEY, "138.243.140.111");
        hashMap2.put(CallFlowMonitor.USER_KEY, "Peter");
        hashMap2.put(CallFlowMonitor.REQUEST_TYPE_KEY, "REMOTE_EJB");
        hashMap2.put(CallFlowMonitor.STATUS_KEY, "true");
        hashMap2.put(CallFlowMonitor.RESPONSE_TIME_KEY, "69");
        hashMap2.put(CallFlowMonitor.APPLICATION_NAME_KEY, "testApp");
        listOfMap.add(hashMap2);
        
        HashMap hashMap3 = new HashMap();
        hashMap3.put(CallFlowMonitor.REQUEST_ID_KEY, "55551111" );
        hashMap3.put(CallFlowMonitor.TIME_STAMP_MILLIS_KEY, ""+java.lang.System.nanoTime());        
        hashMap3.put(CallFlowMonitor.CLIENT_HOST_KEY, "299.288.277.266");
        hashMap3.put(CallFlowMonitor.USER_KEY, "another-user");
        hashMap3.put(CallFlowMonitor.REQUEST_TYPE_KEY, "iiop");
        hashMap3.put(CallFlowMonitor.STATUS_KEY, "false");
        hashMap3.put(CallFlowMonitor.RESPONSE_TIME_KEY, "44");
        hashMap3.put(CallFlowMonitor.APPLICATION_NAME_KEY, "another-app");
        listOfMap.add(hashMap3);
        
        HashMap hashMap4 = new HashMap();
        hashMap4.put(CallFlowMonitor.REQUEST_ID_KEY, "38881999" );
        hashMap4.put(CallFlowMonitor.TIME_STAMP_MILLIS_KEY, ""+java.lang.System.nanoTime());
        hashMap4.put(CallFlowMonitor.CLIENT_HOST_KEY, "178.244.140.111");
        hashMap4.put(CallFlowMonitor.USER_KEY, "admin");
        hashMap4.put(CallFlowMonitor.REQUEST_TYPE_KEY, "REMOTE_EJB");
        hashMap4.put(CallFlowMonitor.STATUS_KEY, "false");
        hashMap4.put(CallFlowMonitor.RESPONSE_TIME_KEY, "20");
        hashMap4.put(CallFlowMonitor.APPLICATION_NAME_KEY, "testApp");
        listOfMap.add(hashMap4);
        
        return listOfMap;
    }
    
    private static List getDemoCallFlowStack(String requestId){
        List listOfMap = new ArrayList();
        
        HashMap hMap = new HashMap();
        hMap.put("RequestID", "RequestID_1");
        hMap.put("RequestType", "REMOTE_EJB");
        hMap.put("TimeStampMillis", "10");
        hMap.put("CallStackRowType", "RequestStart");
        listOfMap.add(hMap);

        hMap = new HashMap();
        hMap.put("Status", "false");
        hMap.put("ModuleName", "Module_Name_1");
        hMap.put("MethodName", "Method_Name_1");
        hMap.put("Exception", "");
        hMap.put("ComponentName", "Component_Name_1");
        hMap.put("RequestID", "RequestID_1");
        hMap.put("ApplicationName", "APP_NAME");
        hMap.put("ContainerType", "SERVLET");
        hMap.put("TimeStampMillis", "11");
        hMap.put("CallStackRowType", "MethodStart");
        listOfMap.add(hMap);


        hMap = new HashMap();
        hMap.put("Status", "false");
        hMap.put("ModuleName", "Module_Name_2");
        hMap.put("MethodName", "Method_Name_2");
        hMap.put("Exception", "");
        hMap.put("ComponentName", "Component_Name_2");
        hMap.put("RequestID", "RequestID_1");
        hMap.put("ApplicationName", "APP_NAME");
        hMap.put("ContainerType", "SERVLET");
        hMap.put("TimeStampMillis", "12");
        hMap.put("CallStackRowType", "MethodStart");
        listOfMap.add(hMap);

        
        hMap = new HashMap();
        hMap.put("Status", "false");
        hMap.put("Exception", "exe_1");
        hMap.put("RequestID", "RequestID_1");
        hMap.put("TimeStampMillis", "13");
        hMap.put("CallStackRowType", "MethodEnd");
        listOfMap.add(hMap);

        hMap = new HashMap();
        hMap.put("Status", "false");
        hMap.put("Exception", "exe_1");
        hMap.put("RequestID", "RequestID_1");
        hMap.put("TimeStampMillis", "14");
        hMap.put("CallStackRowType", "MethodEnd");
        listOfMap.add(hMap);

        hMap = new HashMap();
        hMap.put("RequestID", "RequestID_1");
        hMap.put("TimeStampMillis", "15");
        hMap.put("CallStackRowType", "RequestEnd");
        listOfMap.add(hMap);

        return listOfMap;
    }
    
    static int democount = 0;

    static private Map getDemoTimeSpendMap(String id){
        
        HashMap hashMap = new HashMap();
        
        if (democount == 0){
        hashMap.put("WEB_CONTAINER", ""+10);
        hashMap.put("WEB_APPLICATION", ""+30);
        hashMap.put("EJB_CONTAINER", ""+15);
        hashMap.put("EJB_APPLICATION", ""+40);
        hashMap.put("ORB_CONTAINER", ""+5);
        }else
        if (democount == 1){
            hashMap.put("WEB_CONTAINER", ""+20);
            hashMap.put("WEB_APPLICATION", ""+10);
            hashMap.put("EJB_CONTAINER", ""+15);
            hashMap.put("EJB_APPLICATION", ""+30);
        }else
        if (democount == 2){
            hashMap.put("WEB_CONTAINER", ""+20);
            hashMap.put("WEB_APPLICATION", ""+10);
            hashMap.put("EJB_CONTAINER", ""+15);
        }
        else 
        {
            hashMap.put("WEB_CONTAINER", ""+20);
            hashMap.put("WEB_APPLICATION", ""+35);
        }
        if (democount++ >=3)
            democount=0;
        
        return hashMap;
    }
   
        
    /**
     *	<p> This handler is written for our JSF-based framework -- note the
     *	    HandlerContext package.  This method retreives the CallFlow stack
     *	    Maps.</p>
     *
     *	<p> This handler uses the following input:</p>
     *
     *	<ul><li><b>requestId</b> - The requestId of the CallFlow stack.</li>
     *	    <li><b>instanceName</b> - The server instance name.</li></ul>
     *
     *	<p> This handler returns the following output:</p>
     *
     *	<ul><li><b>callStackMap</b> - The Map of Maps representing the request
     *		information.</li></ul>
     *
     *	@param	handlerCtx  The JSF-based HandlerContext
     */
    
     @Handler(id="getCallFlowStackMaps",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true),
            @HandlerInput(name="requestId", type=String.class, required=true),
            @HandlerInput(name="demo", type=Boolean.class)},
        output={
            @HandlerOutput(name="callStackMap", type=java.util.List.class)}
     )
    public void getCallFlowStackMaps(HandlerContext handlerCtx) {

	String requestId = (String) handlerCtx.getInputValue("requestId");
	String instanceName = (String) handlerCtx.getInputValue("instanceName");
        Boolean demo = (Boolean) handlerCtx.getInputValue("demo");

	CallFlowMonitor cfm = getCallFlowMonitor(instanceName);
	if (cfm == null) {
	    return;
	}
	try {
            List listOfMap = null;
            if (demo != null && demo){
                listOfMap = getDemoCallFlowStack(requestId);
            }else{
                listOfMap = cfm.queryCallStackForRequest(requestId);
            }
	    handlerCtx.setOutputValue("callStackMap", listOfMap);
	} catch (Exception ex) {
	    GuiUtil.handleException(handlerCtx, ex);
            ex.printStackTrace();
	}
    }
    
     
    /**
     *	<p> This handler is written for our JSF-based framework -- note the
     *	    HandlerContext package.  This method converts the CallFlow stack
     *	    List of Maps to tree and adds it as a child to the given parent
     *	    UIComponent.</p>
     *
     *	<p> This handler uses the following input:</p>
     *
     *	<ul><li><b>parent</b> -
     *		The <code>com.sun.web.ui.component.Tree</code>.</li>
     *	    <li><b>content</b> - The List of Maps.</li></ul>
     *
     *	<p> This handler returns the following output:</p>
     *
     *	<ul><li><b>tree</b> - The populated Tree.</li></ul>
     *
     *	@param	handlerCtx  The JSF-based HandlerContext
     */
     @Handler(id="createCallFlowStackTree",
        input={
            @HandlerInput(name="parent", type=javax.faces.component.UIComponent.class, required=true),
            @HandlerInput(name="content", type=List.class, required=true)},
        output={
            @HandlerOutput(name="tree", type=com.sun.webui.jsf.component.Tree.class)}
     )
    public void createCallFlowStackTree(HandlerContext handlerCtx) {
	// Get the inputs
	UIComponent parent = (UIComponent) handlerCtx.getInputValue("parent");
	List content = (List) handlerCtx.getInputValue("content");

	// Create a Tree dataSource...
	TreeDataSource dataSource = new CallFlowStackTreeDS(content);

	// Fill the Tree
	Tree tree = dataSource.createJSFTree(parent);

	// Set the output
	handlerCtx.setOutputValue("tree", tree);
    }

    private interface TreeDataSource {
	/**
	 *  <p>	This method is reponsible for creating, filling and adding a Tree
	 *	to the given parent UIComponent.  It will use the information
	 *	contained in this TreeDataSource.</p>
	 */
	public Tree createJSFTree(UIComponent parent);
    }

    private class CallFlowStackTreeDS implements TreeDataSource {
	public CallFlowStackTreeDS(List<Map> maps) {
	    if (maps != null) {
		_maps = maps;
	    }
	}

	/**
	 *  <p>	This method is reponsible for creating, filling and adding a Tree
	 *	to the given parent UIComponent.  It will use the information
	 *	contained in this TreeDataSource.</p>
	 */
	public Tree createJSFTree(UIComponent parent) {
	    Map nodeMap = null;
	    UIComponent child = null;
	    String type = null;
	    String methodName = null;
	    Tree tree = null;

	    // Get the interator...
	    Iterator<Map> it = _maps.iterator();
	    if (!it.hasNext()) {
		return null;
	    }
            
            //Get the application name from the methodStart row.
            String application = "";
            for(int i=0; i<_maps.size(); i++){
                Map ms = _maps.get(i);
                String rowType = (String) ms.get(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY);
                if(rowType.equals(CallFlowMonitor.CALL_STACK_METHOD_START )){
                    application = (String) ms.get(CallFlowMonitor.APPLICATION_NAME_KEY );
                    if(! GuiUtil.isEmpty(application))
                        break;
                }
            }
                    
	    // Process first Map (should be RequestStart)
	    nodeMap = (Map) it.next();
	    if (!((String) nodeMap.get(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY)).
		    equals(CallFlowMonitor.CALL_STACK_REQUEST_START)) {
		throw new RuntimeException("CallFlow stack should begin with "
			+ "RequestStart, instead got: '"
			+ nodeMap.get(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY) + "'.");
	    }
	    Properties props = new Properties();
	    props.put("expanded", Boolean.TRUE);
            //ensure application is not null
            if (application == null) application = "";
	    props.put("text", application);
	    //props.put("imageURL", ...);
	    child = ComponentUtil.getChild(parent, "callFlowTree",
		    "com.sun.jsftemplating.component.factory.sun.TreeFactory",
		    props);
	    parent.getChildren().add(child);
	    tree = (Tree) child;
	    tree.setClientSide(false);

	    int idx = 0;
	    while (it.hasNext()) {
		nodeMap = (Map) it.next();
		type = (String) nodeMap.get(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY);
		if (type.equals(CallFlowMonitor.CALL_STACK_METHOD_START)) {
		    parent = child;
		    methodName = (String) nodeMap.get(CallFlowMonitor.METHOD_NAME_KEY);
		    // Don't share properties...
		    props = (Properties) props.clone();
		    props.setProperty("text", methodName);
		    child = ComponentUtil.getChild(
			parent, "node" + (++idx),
			"com.sun.jsftemplating.component.factory.sun.TreeNodeFactory",
			props);
		    parent.getChildren().add(child);
		} else if (type.equals(CallFlowMonitor.CALL_STACK_METHOD_END)) {
		    // Set the child -- parent points to child on next pass
		    child = child.getParent();
		} else if (type.equals(CallFlowMonitor.CALL_STACK_REQUEST_END)) {
		    break;
		}
	    }

	    return tree;
	}

	/**
	 *  The List of Maps
	 */
	private List<Map> _maps = new ArrayList<Map>();
    }


    /*
        Compare Time Spent Maps (for sorting). 
     */
    private final static class TimeSpentComparator implements java.util.Comparator
    {
        public int compare( Object o1, Object o2 )
        {
            Float f1 = (Float) ((Map)o1).get("compValue");
            Float f2 = (Float) ((Map)o2).get("compValue");
            return( f2.compareTo(f1) ); 
        }

        public boolean  equals( Object other )
        {
            return( other instanceof TimeSpentComparator );
        }
    }
}
