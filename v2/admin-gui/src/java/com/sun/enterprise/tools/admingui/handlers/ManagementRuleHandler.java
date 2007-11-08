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
 * ManagementRuleHandler.java
 *
 * Created on November 9, 2006, 6:55 PM
 *
 */

package com.sun.enterprise.tools.admingui.handlers;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.EventConfig;
import com.sun.appserv.management.config.ManagementRuleConfig;
import com.sun.appserv.management.config.ManagementRulesConfig;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.faces.model.SelectItem;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.OptionGroup;
/**
 *
 * @author Nitya Doraisamy
 */
public class ManagementRuleHandler {
    
    /**
     *	<p> This handler returns the list of rules for populating 
     *  <p> the table in Management Rules page
     *  <p> Input  value: "ConfigName"   -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "selectedRows" -- Type: <code> java.util.List</code></p>
     *  <p> Output  value: "Result"      -- Type: <code> java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMgmtRulesList",
        input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="Result", type=java.util.List.class)}
    )
    public static void getMgmtRulesList(HandlerContext handlerCtx){
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        
        Iterator iter = config.getManagementRulesConfig().getManagementRuleConfigMap().values().iterator();
        
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
        
        List result = new ArrayList();
        if (iter != null){
            while(iter.hasNext()){
                ManagementRuleConfig mgRuleConfig = (ManagementRuleConfig) iter.next();
                HashMap oneRow = new HashMap();
                String name = mgRuleConfig.getName();
                oneRow.put("name", name); //NOI18N
                oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false); //NOI18N
                boolean rulesEnabled = mgRuleConfig.getEnabled();
                String rulesStatus = null;
                if(rulesEnabled) {
                    rulesStatus = "Enabled"; //NOI18N
                } else {
                    rulesStatus = "Disabled"; //NOI18N
                }   
                String eventType = mgRuleConfig.getEventConfig().getType();
                oneRow.put("ruleStatus", (rulesStatus == null) ? " ": rulesStatus); //NOI18N
                oneRow.put("eventType", (eventType == null) ? " ": eventType); //NOI18N
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("Result", result); //NOI18N
    }
        
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Status"      -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "RuleDesc"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RecordEvent" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "LogLevel"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "EventDesc"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getManagementRuleValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true) },
    output={
        @HandlerOutput(name="Status",         type=Boolean.class),
        @HandlerOutput(name="RuleDesc",       type=String.class),
        @HandlerOutput(name="RecordEvent",    type=Boolean.class),
        @HandlerOutput(name="LogLevel",       type=String.class),
        @HandlerOutput(name="EventDesc",      type=String.class) })
        
        public static void getManagementRuleValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String ruleName = (String) handlerCtx.getInputValue("RuleName");
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            handlerCtx.setOutputValue("Status", mgRuleConfig.getEnabled());
            handlerCtx.setOutputValue("RuleDesc", mgRuleConfig.getDescription());
            handlerCtx.setOutputValue("RecordEvent", mgRuleConfig.getEventConfig().getRecordEvent());
            handlerCtx.setOutputValue("LogLevel", mgRuleConfig.getEventConfig().getLevel());
            handlerCtx.setOutputValue("EventDesc", mgRuleConfig.getEventConfig().getDescription());
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns the values for all the LifeCycle, Trace event types attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Event"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Action"          -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getCommonEventValues",
    input={
        @HandlerInput(name="ConfigName",        type=String.class, required=true),
        @HandlerInput(name="RuleName",          type=String.class, required=true) },
    output={
        @HandlerOutput(name="Event",            type=String.class),
        @HandlerOutput(name="Action",           type=String.class) })
        
        public static void getCommonEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            SelectItem[] events = null;
            String type = mgRuleConfig.getEventConfig().getType();
            if(type.equals("lifecycle")){
                events = ConfigurationHandlers.getOptions(lifeCycleEvents);
            }else if(type.equals("trace")){
                events = ConfigurationHandlers.getOptions(traceEvents);
            }
            if(mgRuleConfig.getActionConfig() != null){
                handlerCtx.setOutputValue("Action", mgRuleConfig.getActionConfig().getActionMBeanName()); //NOI18N
            }
            handlerCtx.setOutputValue("Event", propMap.get("event")); //NOI18N
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the LifeCycle, Trace event types attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Status"           -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "RuleDesc"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RecordEvent"      -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "LogLevel"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "EventDesc"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Event"            -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Action"           -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveCommonEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true),
        @HandlerInput(name="Status",          type=Boolean.class),
        @HandlerInput(name="RuleDesc",        type=String.class),
        @HandlerInput(name="RecordEvent",     type=Boolean.class),
        @HandlerInput(name="LogLevel",        type=String.class),
        @HandlerInput(name="EventDesc",       type=String.class),
        @HandlerInput(name="Event",           type=String.class),
        @HandlerInput(name="Action",          type=String.class)})
        
        public static void saveCommonEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            String type = mgRuleConfig.getEventConfig().getType();
            mgRuleConfig.setEnabled(((Boolean)handlerCtx.getInputValue("Status")).booleanValue());
            mgRuleConfig.setDescription((String)handlerCtx.getInputValue("RuleDesc"));
            mgRuleConfig.getEventConfig().setRecordEvent(((Boolean)handlerCtx.getInputValue("RecordEvent")).booleanValue());
            mgRuleConfig.getEventConfig().setLevel((String)handlerCtx.getInputValue("LogLevel"));
            mgRuleConfig.getEventConfig().setDescription((String)handlerCtx.getInputValue("EventDesc"));
            if(type.equals("lifecycle") || type.equals("trace")) { //NOI18N
                mgRuleConfig.getEventConfig().setPropertyValue("event", (String) handlerCtx.getInputValue("Event"));
            }
            saveActionProperty(mgRuleConfig, handlerCtx);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the Notification type attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SrcGroup"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "CustomMbeanName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "CustomMbeanList" -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "ObjName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NotifType"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Action"          -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getNotifEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true) },
    output={
        @HandlerOutput(name="SrcGroup",         type=String.class),
        @HandlerOutput(name="CustomMbeanName",  type=String.class),
        @HandlerOutput(name="CustomMbeanList",  type=SelectItem[].class),
        @HandlerOutput(name="ObjName",          type=String.class),
        @HandlerOutput(name="NotifType",        type=String.class),
        @HandlerOutput(name="Action",           type=String.class) })
        
        public static void getNotifEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            String mbeanName = propMap.get("sourceobjectname"); //NOI18N
            String[] beanNames = getSourceMbeansNames("notification", configName);
            if(Arrays.asList(beanNames).contains(mbeanName)){
                handlerCtx.setOutputValue("SrcGroup", "1"); //NOI18N
                handlerCtx.setOutputValue("CustomMbeanName", mbeanName);
            }else{
                handlerCtx.setOutputValue("SrcGroup", "2"); //NOI18N
                handlerCtx.setOutputValue("ObjName", mbeanName);
            }
            handlerCtx.setOutputValue("CustomMbeanList", getSourceMbeans("notification", configName)); //NOI18N
            handlerCtx.setOutputValue("NotifType", propMap.get("type")); //NOI18N
            if(mgRuleConfig.getActionConfig() != null){
                handlerCtx.setOutputValue("Action", mgRuleConfig.getActionConfig().getActionMBeanName()); //NOI18N
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      first page of the New Management Rule Wizard </p>
     *  <p> Input value: "ConfigName"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "FromStep2"      -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Status"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "EventType"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "EventTypeList" -- Type: <code>java.util.array</code></p>
     *  <p> Output value: "RecordEvent"   -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMgmtRuleWizard1",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="SupportCluster",  type=Boolean.class, required=true),
        @HandlerInput(name="FromStep2",       type=Boolean.class, required=true) },
    output={
        @HandlerOutput(name="Status",         type=Boolean.class),
        @HandlerOutput(name="EventTypeList",  type=SelectItem[].class),
        @HandlerOutput(name="RecordEvent",    type=Boolean.class) })
        
        public static void getMgmtRuleWizard1(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            Boolean fromStep2 = (Boolean) handlerCtx.getInputValue("FromStep2"); //NOI18N
            Boolean supportCluster = (Boolean) handlerCtx.getInputValue("SupportCluster"); //NOI18N
            if ((fromStep2 == null) || (! fromStep2)){
                ConfigConfig config = AMXUtil.getConfig(configName);
                Map defaultRuleMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(ManagementRuleConfig.J2EE_TYPE);         
                Map defaultEventMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(EventConfig.J2EE_TYPE);         
                Map wizardMgmtRuleMap = new HashMap();
                Map wizardRuleTypesMap = new HashMap();
                Object statusVal = defaultRuleMap.get("enabled"); //NOI18N
                Object recordEventVal = defaultEventMap.get("record-event"); //NOI18N
                wizardMgmtRuleMap.put("configName", configName);
                wizardMgmtRuleMap.put("status", statusVal);
                wizardMgmtRuleMap.put("logLevel", "INFO");
                wizardRuleTypesMap.put("recordEvent", recordEventVal);
                handlerCtx.setOutputValue("Status", statusVal); //NOI18N
                handlerCtx.setOutputValue("RecordEvent", recordEventVal); //NOI18N
                Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
                sessionMap.put("wizardMgmtRule", wizardMgmtRuleMap);
                sessionMap.put("wizardRuleTypes", wizardRuleTypesMap);
            }else{
                Map mgmtRule = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardMgmtRule");
                Map ruleTypes = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardRuleTypes");
                handlerCtx.setOutputValue("Status", mgmtRule.get("status")); //NOI18N
                handlerCtx.setOutputValue("RecordEvent", ruleTypes.get("recordEvent")); //NOI18N
            }
            SelectItem[] options = (SelectItem []) getMgmtRuleEventTypes(supportCluster);
            handlerCtx.setOutputValue("EventTypeList", options); //NOI18N
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values in the Management Rule wizard first page
     *   in the HandlerContext's session map
     *  <p> Input value: "Status"           -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "RecordEvent"      -- Type: <code>java.lang.Boolean</code></p> 
     */
    @Handler(id="updateMgmtRulesWizard1",
    input={
        @HandlerInput(name="Status",         type=Boolean.class),
        @HandlerInput(name="RecordEvent",    type=Boolean.class) })
    public static void updateMgmtRulesWizard1(HandlerContext handlerCtx){
        Map mgmtRule = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardMgmtRule");
        Map ruleTypes = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardRuleTypes");
        mgmtRule.put("status", handlerCtx.getInputValue("Status"));
        ruleTypes.put("recordEvent", handlerCtx.getInputValue("RecordEvent"));
        String type = (String) mgmtRule.get("eventType");
        ruleTypes.put("isCommon", false);
        if(type.equals("lifecycle")) {
            ruleTypes.put("isCommon", true);
        }else if(type.equals("trace")) {
            ruleTypes.put("isCommon", true);
        }          
    }
    
    /**
     *	<p> This handler returns sets the seesion attr values for the wizards of the
     *   Management Rules Page </p>
     *  <p> Input value: "FromStep2"    -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "EventType"    -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "EventDesc"    -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Action"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SrcGroup"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "CustomMbean"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ObjectName"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "NotifType"    -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Event"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "DateString"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Pattern"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Period"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "NoOccurences" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Message"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Logger"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Level"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="setMgmtRulesSessionValues",
    input={
        @HandlerInput(name="FromStep2",     type=Boolean.class, required=true), 
        @HandlerInput(name="EventType",     type=String.class),
        @HandlerInput(name="EventDesc",     type=String.class),
        @HandlerInput(name="Action",        type=String.class),
        @HandlerInput(name="SrcGroup",      type=String.class),
        @HandlerInput(name="CustomMbean",   type=String.class), 
        @HandlerInput(name="ObjectName",    type=String.class),
        @HandlerInput(name="NotifType",     type=String.class),
        @HandlerInput(name="Event",         type=String.class),
        @HandlerInput(name="DateString",    type=String.class),
        @HandlerInput(name="Pattern",       type=String.class),
        @HandlerInput(name="Period",        type=String.class),
        @HandlerInput(name="NoOccurences",  type=String.class),
        @HandlerInput(name="Message",       type=String.class),
        @HandlerInput(name="Logger",        type=String.class),
        @HandlerInput(name="Level",         type=String.class),
        @HandlerInput(name="ObservedMbean", type=String.class), 
        @HandlerInput(name="ObservedAttr",  type=String.class),
        @HandlerInput(name="Granularity",   type=String.class),
        @HandlerInput(name="MonitorType",   type=String.class),
        @HandlerInput(name="DiffMode",      type=Boolean.class),
        @HandlerInput(name="DiffModeGg",    type=Boolean.class),
        @HandlerInput(name="NumType",       type=String.class),
        @HandlerInput(name="NumTypeGg",     type=String.class),
        @HandlerInput(name="InitThreshold", type=String.class),
        @HandlerInput(name="Offset",        type=String.class),
        @HandlerInput(name="Modulus",       type=String.class),
        @HandlerInput(name="LowThresh",     type=String.class),
        @HandlerInput(name="HighThresh",    type=String.class),
        @HandlerInput(name="Trigger",       type=String.class),
        @HandlerInput(name="ValueProp",     type=String.class),
        @HandlerInput(name="ServerName",    type=String.class)})  
        
        public static void setMgmtRulesSessionValues(HandlerContext handlerCtx) {
        try{
            Map mgmtRule = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardMgmtRule");
            Map ruleTypes = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardRuleTypes");
            
            // Common Props
            String eventType = (String)handlerCtx.getInputValue("EventType");
            ruleTypes.put("eventType", eventType);
            ruleTypes.put("eventDesc", (String)handlerCtx.getInputValue("EventDesc"));
            ruleTypes.put("action", (String)handlerCtx.getInputValue("Action"));
            if(eventType.equals("notification")){
                String custMbean = (String)handlerCtx.getInputValue("CustomMbean");
                String objName = (String)handlerCtx.getInputValue("ObjectName");
                String notifType = (String)handlerCtx.getInputValue("NotifType");
                ruleTypes.put("srcGroup", (String)handlerCtx.getInputValue("SrcGroup"));
                ruleTypes.put("custMbean", custMbean);
                ruleTypes.put("objName", objName);
                ruleTypes.put("notifType", notifType);
            }
            if(eventType.equals("lifecycle") || eventType.equals("trace")){
                String event = (String)handlerCtx.getInputValue("Event");
                ruleTypes.put("event", event);
            }
            if(eventType.equals("timer")){
                String dtString = (String)handlerCtx.getInputValue("DateString");
                String pattern = (String)handlerCtx.getInputValue("Pattern");
                String period = (String)handlerCtx.getInputValue("Period");
                String noOccurences = (String)handlerCtx.getInputValue("NoOccurences");
                String message = (String)handlerCtx.getInputValue("Message");
                if (dtString != null)
                    ruleTypes.put("datestring", dtString);
                ruleTypes.put("pattern", pattern);
                ruleTypes.put("period", period);
                ruleTypes.put("noOccurences", noOccurences);
                ruleTypes.put("message", message);
            }
            if(eventType.equals("log")){
                String logger = (String)handlerCtx.getInputValue("Logger");
                String level = (String)handlerCtx.getInputValue("Level");
                ruleTypes.put("logger", logger);
                ruleTypes.put("level", level);
            }
            if(eventType.equals("monitor")){
                ruleTypes.put("obsMBean", (String)handlerCtx.getInputValue("ObservedMbean"));
                ruleTypes.put("obsAttr", (String)handlerCtx.getInputValue("ObservedAttr"));
                ruleTypes.put("granularity", (String)handlerCtx.getInputValue("Granularity"));
                String monitorType = (String)handlerCtx.getInputValue("MonitorType");
                ruleTypes.put("monitorType", monitorType);
                if((monitorType != null) && (! monitorType.equals(""))){
                    if(monitorType.equals("countermonitor")){
                        ruleTypes.put("diffMode", (Boolean)handlerCtx.getInputValue("DiffMode"));
                        ruleTypes.put("numType", (String)handlerCtx.getInputValue("NumType"));
                        ruleTypes.put("initThresh", (String)handlerCtx.getInputValue("InitThreshold"));
                        ruleTypes.put("offset", (String)handlerCtx.getInputValue("Offset"));
                        ruleTypes.put("modulus", (String)handlerCtx.getInputValue("Modulus"));
                    }else if(monitorType.equals("gaugemonitor")){
                        ruleTypes.put("diffModeGg", (Boolean)handlerCtx.getInputValue("DiffModeGg"));
                        ruleTypes.put("numTypeGg", (String)handlerCtx.getInputValue("NumTypeGg"));
                        ruleTypes.put("lowThresh", (String)handlerCtx.getInputValue("LowThresh"));
                        ruleTypes.put("highThresh", (String)handlerCtx.getInputValue("HighThresh"));
                    }else if(monitorType.equals("stringmonitor")){
                        ruleTypes.put("trigger", (String)handlerCtx.getInputValue("Trigger"));
                        ruleTypes.put("valueProp", (String)handlerCtx.getInputValue("ValueProp"));
                    }
                }
            }
            if(eventType.equals("cluster")){
                String event = (String)handlerCtx.getInputValue("Event");
                String serverName = (String)handlerCtx.getInputValue("ServerName");
                ruleTypes.put("event", event);
                ruleTypes.put("serverName", serverName);
            }
            if(! (Boolean)handlerCtx.getInputValue("FromStep2")){
                createMgmtRule(handlerCtx);
            }    
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for the element in the lists page of
     *      Management Rule </p>
     *  <p> Input value: "ConfigName"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AllRules"         -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMgmtRuleValues",
    input={
        @HandlerInput(name="ConfigName",         type=String.class) },
    output={
        @HandlerOutput(name="AllRules",          type=Boolean.class) })
        
        public static void getMgmtRuleValues(HandlerContext handlerCtx) {
        try{
            ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
            boolean allRules = config.getManagementRulesConfig().getEnabled();
            handlerCtx.setOutputValue("AllRules", allRules); //NOI18N
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the for the element in the lists page of 
     *      Management Rule </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AllRules"         -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveMgmtRuleValues",
    input={
        @HandlerInput(name="ConfigName",         type=String.class), 
        @HandlerInput(name="AllRules",          type=Boolean.class) })
        
        public static void saveMgmtRuleValues(HandlerContext handlerCtx) {
        try{
            ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
            config.getManagementRulesConfig().setEnabled(((Boolean)handlerCtx.getInputValue("AllRules")).booleanValue());
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for the elements that are lists in the
     *      Management Rule Wizard </p>
     *  <p> Input value: "EventType"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ActionsList"     -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "CustomMbeanList" -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "EventsList"      -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "LoggerList"      -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMgmtRuleLists",
    input={
        @HandlerInput(name="EventType",         type=String.class) },
    output={
        @HandlerOutput(name="ActionsList",      type=SelectItem[].class),
        @HandlerOutput(name="CustomMbeanList",  type=SelectItem[].class),
        @HandlerOutput(name="EventsList",       type=SelectItem[].class),
        @HandlerOutput(name="NumTypes",         type=SelectItem[].class),
        @HandlerOutput(name="TriggerTypes",     type=SelectItem[].class),
        @HandlerOutput(name="LoggerList",       type=SelectItem[].class),
        @HandlerOutput(name="ServerNamesList",       type=SelectItem[].class) })
        
        public static void getMgmtRuleLists(HandlerContext handlerCtx) {
        try{
            Map mgmtRule = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardMgmtRule");
            String configName = (String)mgmtRule.get("configName");
            String type = (String) handlerCtx.getInputValue("EventType"); //NOI18N 
            SelectItem[] eventsList = null;
            if(type.equals("lifecycle")) { //NOI18N
                eventsList = ConfigurationHandlers.getOptions(lifeCycleEvents);
            }else if(type.equals("trace")) { //NOI18N
                eventsList = ConfigurationHandlers.getOptions(traceEvents);
            }else if(type.equals("cluster")) { //NOI18N
                eventsList = ConfigurationHandlers.getOptions(clusterEvents);
            }
            String[] loggers = getLoggerNames();
            SelectItem[] loggerList = ConfigurationHandlers.getOptions(loggers);
            SelectItem[] numtypesList = ConfigurationHandlers.getOptions(numTypes);
            SelectItem[] triggerList = ConfigurationHandlers.getOptions(triggerValues, triggerTypes);  
            SelectItem[] serverNamesList = ConfigurationHandlers.getOptions(getTargetNames());          
            handlerCtx.setOutputValue("NumTypes", numtypesList);
            handlerCtx.setOutputValue("TriggerTypes", triggerList);
            handlerCtx.setOutputValue("ActionsList", getActions(configName)); //NOI18N
            handlerCtx.setOutputValue("CustomMbeanList", getSourceMbeans(type, configName)); //NOI18N
            handlerCtx.setOutputValue("EventsList", eventsList); //NOI18N
            handlerCtx.setOutputValue("LoggerList", loggerList); //NOI18N
            handlerCtx.setOutputValue("ServerNamesList", serverNamesList); //NOI18N
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler creates a Management Rule for use in the New wizard
     *  <p> Output value: "FromStep2"    -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "EventDesc"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Action"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "CustomMbean"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ObjectName"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NotifType"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Event"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DateString"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Pattern"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NoOccurences" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Message"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Logger"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Level"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ServerName"   -- Type: <code>java.lang.String</code></p>
     */
    @Handler(id="updateStep2MgmtRulesWizard",
    input={
        @HandlerInput(name="FromStep2",     type=Boolean.class) },
    output={
        @HandlerOutput(name="EventType",     type=String.class),
        @HandlerOutput(name="EventDesc",     type=String.class),
        @HandlerOutput(name="Action",        type=String.class),
        @HandlerOutput(name="SrcGroup",      type=String.class), 
        @HandlerOutput(name="CustomMbean",   type=String.class), 
        @HandlerOutput(name="ObjectName",    type=String.class),
        @HandlerOutput(name="NotifType",     type=String.class),
        @HandlerOutput(name="Event",         type=String.class),
        @HandlerOutput(name="DateString",    type=String.class),
        @HandlerOutput(name="Pattern",       type=String.class),
        @HandlerOutput(name="Period",        type=String.class),
        @HandlerOutput(name="NoOccurences",  type=String.class),
        @HandlerOutput(name="Message",       type=String.class),
        @HandlerOutput(name="Logger",        type=String.class),
        @HandlerOutput(name="Level",         type=String.class),
        @HandlerOutput(name="ObservedMbean", type=String.class), 
        @HandlerOutput(name="ObservedAttr",  type=String.class),
        @HandlerOutput(name="Granularity",   type=String.class),
        @HandlerOutput(name="MonitorType",   type=String.class),
        @HandlerOutput(name="DiffMode",      type=Boolean.class),
        @HandlerOutput(name="DiffModeGg",    type=Boolean.class),
        @HandlerOutput(name="NumType",       type=String.class),
        @HandlerOutput(name="NumTypeGg",     type=String.class),
        @HandlerOutput(name="InitThreshold", type=String.class),
        @HandlerOutput(name="Offset",        type=String.class),
        @HandlerOutput(name="Modulus",       type=String.class),
        @HandlerOutput(name="LowThresh",     type=String.class),
        @HandlerOutput(name="HighThresh",    type=String.class),
        @HandlerOutput(name="Trigger",       type=String.class),
        @HandlerOutput(name="ValueProp",     type=String.class),
        @HandlerOutput(name="ServerName",    type=String.class) })  
    public static void updateStep2MgmtRulesWizard(HandlerContext handlerCtx){
        try{
            Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
            Map mgmtRule = (Map) sessionMap.get("wizardMgmtRule");
            Map ruleTypes = (Map) sessionMap.get("wizardRuleTypes");
            Boolean fromStep2 = (Boolean)handlerCtx.getInputValue("FromStep2"); //NOI18N
            String eventType = (String)mgmtRule.get("eventType"); //NOI18N
            String prevEventType = (String)ruleTypes.get("eventType"); //NOI18N
            if(prevEventType == null){
                handlerCtx.setOutputValue("SrcGroup", "1");
                handlerCtx.setOutputValue("DiffMode", false);
                handlerCtx.setOutputValue("DiffModeGg", false);
            }
            if(! eventType.equals(prevEventType)){
                return;
            }
            if((fromStep2 == null) || (! fromStep2))   {
                // Common Props
                handlerCtx.setOutputValue("EventDesc", ruleTypes.get("eventDesc"));
                handlerCtx.setOutputValue("Action", ruleTypes.get("action"));
                if(eventType.equals("notification")){
                    handlerCtx.setOutputValue("SrcGroup", ruleTypes.get("srcGroup"));
                    handlerCtx.setOutputValue("CustomMbean", ruleTypes.get("custMbean"));
                    handlerCtx.setOutputValue("ObjectName", ruleTypes.get("objName"));
                    handlerCtx.setOutputValue("NotifType", ruleTypes.get("notifType"));
                }
                if(eventType.equals("lifecycle") || eventType.equals("trace")){
                    handlerCtx.setOutputValue("Event", ruleTypes.get("event"));
                }
                if(eventType.equals("timer")){
                    handlerCtx.setOutputValue("DateString", ruleTypes.get("datestring"));
                    handlerCtx.setOutputValue("Pattern", ruleTypes.get("pattern"));
                    handlerCtx.setOutputValue("Period", ruleTypes.get("period"));
                    handlerCtx.setOutputValue("NoOccurences", ruleTypes.get("noOccurences"));
                    handlerCtx.setOutputValue("Message", ruleTypes.get("message"));
                }
                if(eventType.equals("log")){
                    handlerCtx.setOutputValue("Logger", ruleTypes.get("logger"));
                    handlerCtx.setOutputValue("Level", ruleTypes.get("level"));
                }
                if(eventType.equals("monitor")){
                    handlerCtx.setOutputValue("ObservedMbean", ruleTypes.get("obsMBean"));
                    handlerCtx.setOutputValue("ObservedAttr", ruleTypes.get("obsAttr"));
                    handlerCtx.setOutputValue("Granularity", ruleTypes.get("granularity"));
                    String monitorType = (String)ruleTypes.get("monitorType");
                    handlerCtx.setOutputValue("MonitorType", monitorType);
                    if((monitorType != null) && (! monitorType.equals(""))){
                        if(monitorType.equals("countermonitor")){
                            handlerCtx.setOutputValue("DiffMode", (Boolean)ruleTypes.get("diffMode"));
                            handlerCtx.setOutputValue("NumType", ruleTypes.get("numType"));
                            handlerCtx.setOutputValue("InitThreshold", ruleTypes.get("initThresh"));
                            handlerCtx.setOutputValue("Offset", ruleTypes.get("offset"));
                            handlerCtx.setOutputValue("Modulus", ruleTypes.get("modulus"));
                        }else if(monitorType.equals("gaugemonitor")){
                            handlerCtx.setOutputValue("DiffModeGg", (Boolean)ruleTypes.get("diffModeGg"));
                            handlerCtx.setOutputValue("NumTypeGg", ruleTypes.get("numTypeGg"));
                            handlerCtx.setOutputValue("LowThresh", ruleTypes.get("lowThresh"));
                            handlerCtx.setOutputValue("HighThresh", ruleTypes.get("highThresh"));
                        }else if(monitorType.equals("stringmonitor")){
                            handlerCtx.setOutputValue("Trigger", ruleTypes.get("trigger"));
                            handlerCtx.setOutputValue("ValueProp", ruleTypes.get("valueProp"));
                        }
                    }
                }
                if(eventType.equals("cluster")){
                    handlerCtx.setOutputValue("Event", ruleTypes.get("event"));
                    handlerCtx.setOutputValue("ServerName", ruleTypes.get("serverName"));
                }
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns dropdown values for the Log Levels dropdown list
     *      in the Management Rules Page.</p>
     *  <p> Output value: "DefaultLevel" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogLevels"    -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLoglevels",
    output={
        @HandlerOutput(name="LogLevels",    type=SelectItem[].class)})
        public static void getLoglevels(HandlerContext handlerCtx) {
        SelectItem[] levels = ConfigurationHandlers.getOptions(logLevels); 
        handlerCtx.setOutputValue("LogLevels", levels);
    }
    
    /**
     *	<p> This handler returns the values for all the Monitor type attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ObservedMbean"  -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "ObservedAttr"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Granularity"    -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "MonitorType"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DiffMode"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "NumType"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "InitThreshold"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Offset"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Modulus"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NumTypes"      -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMonitorEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true) },
    output={
        @HandlerOutput(name="isCounter",          type=Boolean.class),
        @HandlerOutput(name="isGauge",            type=Boolean.class),
        @HandlerOutput(name="isString",           type=Boolean.class),
        @HandlerOutput(name="ObservedMbean",      type=String.class),
        @HandlerOutput(name="ObservedAttr",       type=String.class),
        @HandlerOutput(name="Granularity",        type=String.class),
        @HandlerOutput(name="MonitorType",        type=String.class),
        @HandlerOutput(name="DiffMode",           type=Boolean.class), 
        @HandlerOutput(name="NumType",            type=String.class),
        @HandlerOutput(name="InitThreshold",      type=String.class),
        @HandlerOutput(name="Offset",             type=String.class),
        @HandlerOutput(name="Modulus",            type=String.class),
        @HandlerOutput(name="NumTypesList",       type=SelectItem[].class),
        @HandlerOutput(name="LowThresh",          type=String.class),
        @HandlerOutput(name="HighThresh",         type=String.class),
        @HandlerOutput(name="Trigger",            type=String.class),
        @HandlerOutput(name="TriggersList",       type=SelectItem[].class),
        @HandlerOutput(name="ValueProp",          type=String.class),
        @HandlerOutput(name="Action",             type=String.class) })
        public static void getMonitorEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String ruleName = (String) handlerCtx.getInputValue("RuleName");
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            SelectItem[] numtypes = ConfigurationHandlers.getOptions(numTypes);
            String monitorType = propMap.get("monitortype");
            
            handlerCtx.setOutputValue("ObservedMbean", propMap.get("observedobject"));
            handlerCtx.setOutputValue("ObservedAttr", propMap.get("observedattribute"));
            handlerCtx.setOutputValue("Granularity", propMap.get("granularityperiod"));
                        
            if(monitorType.equals("countermonitor") || monitorType.equals("gaugemonitor")){
                handlerCtx.setOutputValue("isString", false);
                handlerCtx.setOutputValue("DiffMode", propMap.get("differencemode"));
                handlerCtx.setOutputValue("NumType", propMap.get("numbertype"));
                handlerCtx.setOutputValue("NumTypesList", numtypes);
                if(monitorType.equals("countermonitor")){
                    handlerCtx.setOutputValue("isCounter", true);
                    handlerCtx.setOutputValue("isGauge", false);
                    handlerCtx.setOutputValue("MonitorType", "Counter");
                    handlerCtx.setOutputValue("InitThreshold", propMap.get("initthreshold"));
                    handlerCtx.setOutputValue("Offset", propMap.get("offset"));
                    handlerCtx.setOutputValue("Modulus", propMap.get("modulus"));
                }
                if(monitorType.equals("gaugemonitor")){
                    handlerCtx.setOutputValue("isGauge", true);
                    handlerCtx.setOutputValue("isCounter", false);
                    handlerCtx.setOutputValue("MonitorType", "Gauge");
                    handlerCtx.setOutputValue("LowThresh", propMap.get("lowthreshold"));
                    handlerCtx.setOutputValue("HighThresh", propMap.get("highthreshold"));
                }
            }
            if(monitorType.equals("stringmonitor")){
                handlerCtx.setOutputValue("isString", true);
                handlerCtx.setOutputValue("isCounter", false);
                handlerCtx.setOutputValue("isGauge", false);
                handlerCtx.setOutputValue("MonitorType", "String");
                SelectItem[] triggersList = ConfigurationHandlers.getOptions(triggerValues, triggerTypes);  
                handlerCtx.setOutputValue("TriggersList", triggersList);
                handlerCtx.setOutputValue("Trigger", propMap.get("stringnotify"));
                handlerCtx.setOutputValue("ValueProp", propMap.get("stringtocompare"));
            }
            if(mgRuleConfig.getActionConfig() != null){
                handlerCtx.setOutputValue("Action", mgRuleConfig.getActionConfig().getActionMBeanName()); //NOI18N
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the Timer event types attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DateString"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Pattern"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Period"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NoOccurences"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Message"         -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getTimerEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true) },
    output={
        @HandlerOutput(name="DateString",       type=String.class),
        @HandlerOutput(name="Pattern",          type=String.class),
        @HandlerOutput(name="Period",           type=String.class),
        @HandlerOutput(name="NoOccurences",     type=String.class),
        @HandlerOutput(name="Message",          type=String.class),
        @HandlerOutput(name="Action",        type=String.class)})
        
        public static void getTimerEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            handlerCtx.setOutputValue("DateString", propMap.get("datestring")); //NOI18N
            handlerCtx.setOutputValue("Pattern", propMap.get("pattern")); //NOI18N
            handlerCtx.setOutputValue("Period", propMap.get("period")); //NOI18N
            handlerCtx.setOutputValue("NoOccurences", propMap.get("numberofoccurrences")); //NOI18N
            handlerCtx.setOutputValue("Message", propMap.get("message")); //NOI18N
            if(mgRuleConfig.getActionConfig() != null){
                handlerCtx.setOutputValue("Action", mgRuleConfig.getActionConfig().getActionMBeanName()); //NOI18N
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the Log event types attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Logger"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LoggerList"      -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "Level"           -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLogEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true) },
    output={
        @HandlerOutput(name="Logger",         type=String.class),
        @HandlerOutput(name="LoggerList",     type=SelectItem[].class),
        @HandlerOutput(name="Level",          type=String.class),
        @HandlerOutput(name="Action",           type=String.class) })
        
        public static void getLogEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            handlerCtx.setOutputValue("Level", propMap.get("level")); //NOI18N
            String[] loggers = getLoggerNames();
            SelectItem[] options = ConfigurationHandlers.getOptions(loggers);
            handlerCtx.setOutputValue("LoggerList", options);
            if(loggers.length > 0){
                handlerCtx.setOutputValue("Logger", propMap.get("loggernames")); //NOI18N
            }
            if(mgRuleConfig.getActionConfig() != null){
                handlerCtx.setOutputValue("Action", mgRuleConfig.getActionConfig().getActionMBeanName()); //NOI18N
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the Cluster event type attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Event"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ServerName"      -- Type: <code>java.util.String</code></p>
     *  <p> Output value: "ServerNamesList" -- Type: <code>java.lang.Array</code></p>
     *  <p> Output value: "Action"          -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusterEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true) },
    output={
        @HandlerOutput(name="Event",           type=String.class),
        @HandlerOutput(name="ServerName",      type=String.class),
        @HandlerOutput(name="ServerNamesList", type=SelectItem[].class),
        @HandlerOutput(name="Action",          type=String.class) })
        
        public static void getClusterEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            handlerCtx.setOutputValue("Event", propMap.get("name")); //NOI18N
            handlerCtx.setOutputValue("ServerName", propMap.get("servername")); //NOI18N
            handlerCtx.setOutputValue("ServerNamesList", ConfigurationHandlers.getOptions(getTargetNames())); //NOI18N
            if(mgRuleConfig.getActionConfig() != null){
                handlerCtx.setOutputValue("Action", mgRuleConfig.getActionConfig().getActionMBeanName()); //NOI18N
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Status"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "RuleDesc"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RecordEvent"  -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "LogLevel"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "EventDesc"    -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveManagementRuleValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true),
        @HandlerInput(name="Status",          type=Boolean.class),
        @HandlerInput(name="RuleDesc",        type=String.class),
        @HandlerInput(name="RecordEvent",     type=Boolean.class),
        @HandlerInput(name="LogLevel",        type=String.class),
        @HandlerInput(name="EventDesc",       type=String.class) })
        
        public static void saveManagementRuleValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String ruleName = (String) handlerCtx.getInputValue("RuleName");
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            
            mgRuleConfig.setEnabled(((Boolean)handlerCtx.getInputValue("Status")).booleanValue());
            mgRuleConfig.setDescription((String)handlerCtx.getInputValue("RuleDesc"));
            mgRuleConfig.getEventConfig().setRecordEvent(((Boolean)handlerCtx.getInputValue("RecordEvent")).booleanValue());
            mgRuleConfig.getEventConfig().setLevel((String)handlerCtx.getInputValue("LogLevel"));
            mgRuleConfig.getEventConfig().setDescription((String)handlerCtx.getInputValue("EventDesc"));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the Monitor type attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ObservedMbean"  -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "ObservedAttr"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Granularity"    -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "MonitorType"    -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "DiffMode"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "NumType"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "InitThreshold"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Offset"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Modulus"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "NumTypes"      -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveMonitorEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true), 
        @HandlerInput(name="Status",          type=Boolean.class),
        @HandlerInput(name="RuleDesc",        type=String.class),
        @HandlerInput(name="RecordEvent",     type=Boolean.class),
        @HandlerInput(name="LogLevel",        type=String.class),
        @HandlerInput(name="EventDesc",       type=String.class),
        @HandlerInput(name="ObservedMbean",      type=String.class),
        @HandlerInput(name="ObservedAttr",       type=String.class),
        @HandlerInput(name="Granularity",        type=String.class),
        @HandlerInput(name="MonitorType",        type=String.class),
        @HandlerInput(name="DiffMode",           type=Boolean.class), 
        @HandlerInput(name="NumType",            type=String.class),
        @HandlerInput(name="InitThreshold",      type=String.class, required=true),
        @HandlerInput(name="Offset",             type=String.class),
        @HandlerInput(name="Modulus",            type=String.class),
        @HandlerInput(name="LowThresh",          type=String.class),
        @HandlerInput(name="HighThresh",         type=String.class),
        @HandlerInput(name="Trigger",            type=String.class),
        @HandlerInput(name="ValueProp",          type=String.class),
        @HandlerInput(name="Action",             type=String.class) })
        
        public static void saveMonitorEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String ruleName = (String) handlerCtx.getInputValue("RuleName");
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            
            mgRuleConfig.setEnabled(((Boolean)handlerCtx.getInputValue("Status")).booleanValue());
            mgRuleConfig.setDescription((String)handlerCtx.getInputValue("RuleDesc"));
            mgRuleConfig.getEventConfig().setRecordEvent(((Boolean)handlerCtx.getInputValue("RecordEvent")).booleanValue());
            mgRuleConfig.getEventConfig().setLevel((String)handlerCtx.getInputValue("LogLevel"));
            mgRuleConfig.getEventConfig().setDescription((String)handlerCtx.getInputValue("EventDesc"));
            
            
            String obsMbean = (String)handlerCtx.getInputValue("ObservedMbean");
            String obsAttr = (String)handlerCtx.getInputValue("ObservedAttr");
            String granularity = (String)handlerCtx.getInputValue("Granularity");
            String monitorType = mgRuleConfig.getEventConfig().getProperties().get("monitortype");
            if(obsMbean != null){
                mgRuleConfig.getEventConfig().setPropertyValue("observedobject", obsMbean);
            }
            if(obsAttr != null){
                mgRuleConfig.getEventConfig().setPropertyValue("observedattribute", obsAttr);
            }    
            if(granularity != null){
                mgRuleConfig.getEventConfig().setPropertyValue("granularityperiod", granularity);
            }    
            if(monitorType.equals("countermonitor") || monitorType.equals("gaugemonitor")){
                Boolean diffMode = (Boolean)handlerCtx.getInputValue("DiffMode");
                if(diffMode != null){
                    mgRuleConfig.getEventConfig().setPropertyValue("differencemode", diffMode.toString());
                }
                String numType = (String)handlerCtx.getInputValue("NumType");
                if(numType != null){
                    mgRuleConfig.getEventConfig().setPropertyValue("numbertype", numType);
                }
                if(monitorType.equals("countermonitor")){
                    String initThresh = (String)handlerCtx.getInputValue("InitThreshold");
                    String offset = (String)handlerCtx.getInputValue("Offset");
                    String modulus = (String)handlerCtx.getInputValue("Modulus");
                    if(initThresh != null){
                        mgRuleConfig.getEventConfig().setPropertyValue("initthreshold", initThresh);
                    }
                    if(offset != null){
                        mgRuleConfig.getEventConfig().setPropertyValue("offset", offset);
                    }
                    if(modulus != null){
                        mgRuleConfig.getEventConfig().setPropertyValue("modulus", modulus);
                    }
                }
                if(monitorType.equals("gaugemonitor")){
                    String lowThresh = (String)handlerCtx.getInputValue("LowThresh");
                    String highThresh = (String)handlerCtx.getInputValue("HighThresh");
                    if(lowThresh != null){
                        mgRuleConfig.getEventConfig().setPropertyValue("lowthreshold", lowThresh);
                    }
                    if(highThresh != null){
                        mgRuleConfig.getEventConfig().setPropertyValue("highthreshold", highThresh);
                    }
                }
            }
            if(monitorType.equals("stringmonitor")){
                String trigger = (String)handlerCtx.getInputValue("Trigger");
                String valueProp = (String)handlerCtx.getInputValue("ValueProp");
                if(trigger != null){
                    mgRuleConfig.getEventConfig().setPropertyValue("stringnotify", trigger);
                }
                if(valueProp != null){
                    mgRuleConfig.getEventConfig().setPropertyValue("stringtocompare", valueProp);
                }
            }
            saveActionProperty(mgRuleConfig, handlerCtx);                
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the Notification type attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SrcGroup"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "CustomMbeanName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ObjName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NotifType"       -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveNotifEventValues",
    input={
        @HandlerInput(name="ConfigName",       type=String.class, required=true),
        @HandlerInput(name="RuleName",         type=String.class, required=true),
        @HandlerInput(name="Status",          type=Boolean.class),
        @HandlerInput(name="RuleDesc",        type=String.class),
        @HandlerInput(name="RecordEvent",     type=Boolean.class),
        @HandlerInput(name="LogLevel",        type=String.class),
        @HandlerInput(name="EventDesc",       type=String.class),
        @HandlerInput(name="SrcGroup",         type=String.class),
        @HandlerInput(name="CustomMbeanName",  type=String.class),
        @HandlerInput(name="ObjName",          type=String.class),
        @HandlerInput(name="NotifType",        type=String.class),
        @HandlerInput(name="Action",        type=String.class) })
        
        public static void saveNotifEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            
            String srcGroup = (String)handlerCtx.getInputValue("SrcGroup"); //NOI18N
            String notifType = (String)handlerCtx.getInputValue("NotifType"); //NOI18N
            String mbeanProp = null;
            if(srcGroup.equals("1")){ //NOI18N
                mbeanProp = (String)handlerCtx.getInputValue("CustomMbeanName"); //NOI18N
            }else if(srcGroup.equals("2")){ //NOI18N
                mbeanProp = (String)handlerCtx.getInputValue("ObjName"); //NOI18N
            }

            mgRuleConfig.setEnabled(((Boolean)handlerCtx.getInputValue("Status")).booleanValue());
            mgRuleConfig.setDescription((String)handlerCtx.getInputValue("RuleDesc"));
            mgRuleConfig.getEventConfig().setRecordEvent(((Boolean)handlerCtx.getInputValue("RecordEvent")).booleanValue());
            mgRuleConfig.getEventConfig().setLevel((String)handlerCtx.getInputValue("LogLevel"));
            mgRuleConfig.getEventConfig().setDescription((String)handlerCtx.getInputValue("EventDesc"));
            if(mbeanProp != null){
                mgRuleConfig.getEventConfig().setPropertyValue("sourceobjectname", mbeanProp); 
            }
            if(notifType != null){
                mgRuleConfig.getEventConfig().setPropertyValue("type", notifType);
            }    
            saveActionProperty(mgRuleConfig, handlerCtx);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the Timer event types attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "DateString"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Pattern"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Period"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "NoOccurences"    -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Message"         -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveTimerEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true), 
        @HandlerInput(name="Status",          type=Boolean.class),
        @HandlerInput(name="RuleDesc",        type=String.class),
        @HandlerInput(name="RecordEvent",     type=Boolean.class),
        @HandlerInput(name="LogLevel",        type=String.class),
        @HandlerInput(name="EventDesc",       type=String.class),
        @HandlerInput(name="DateString",      type=String.class, required=true),
        @HandlerInput(name="Pattern",         type=String.class),
        @HandlerInput(name="Period",          type=String.class),
        @HandlerInput(name="NoOccurences",    type=String.class),
        @HandlerInput(name="Message",         type=String.class),
        @HandlerInput(name="Action",        type=String.class)})
        
        public static void saveTimerEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            
            String dtString = (String)handlerCtx.getInputValue("DateString"); //NOI18N
            String pattern = (String)handlerCtx.getInputValue("Pattern"); //NOI18N
            String period = (String)handlerCtx.getInputValue("Period"); //NOI18N
            String numOccurences = (String)handlerCtx.getInputValue("NoOccurences"); //NOI18N
            String msg = (String)handlerCtx.getInputValue("Message"); //NOI18N
            mgRuleConfig.setEnabled(((Boolean)handlerCtx.getInputValue("Status")).booleanValue());
            mgRuleConfig.setDescription((String)handlerCtx.getInputValue("RuleDesc"));
            mgRuleConfig.getEventConfig().setRecordEvent(((Boolean)handlerCtx.getInputValue("RecordEvent")).booleanValue());
            mgRuleConfig.getEventConfig().setLevel((String)handlerCtx.getInputValue("LogLevel"));
            mgRuleConfig.getEventConfig().setDescription((String)handlerCtx.getInputValue("EventDesc"));
            
            if(dtString != null){
                mgRuleConfig.getEventConfig().setPropertyValue("datestring", dtString); 
            }
            if(pattern != null){
                mgRuleConfig.getEventConfig().setPropertyValue("pattern", pattern);
            }
            if(period != null){
                mgRuleConfig.getEventConfig().setPropertyValue("period", period);
            }
            if(numOccurences != null){
                mgRuleConfig.getEventConfig().setPropertyValue("numberofoccurrences", numOccurences);
            }
            if(msg != null){
                mgRuleConfig.getEventConfig().setPropertyValue("message", msg);
            }
            saveActionProperty(mgRuleConfig, handlerCtx);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the Cluster event type attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Status"           -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "RuleDesc"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RecordEvent"      -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "LogLevel"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "EventDesc"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Event"            -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ServerName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Action"           -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveClusterEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true), 
        @HandlerInput(name="Status",          type=Boolean.class),
        @HandlerInput(name="RuleDesc",        type=String.class),
        @HandlerInput(name="RecordEvent",     type=Boolean.class),
        @HandlerInput(name="LogLevel",        type=String.class),
        @HandlerInput(name="EventDesc",       type=String.class),
        @HandlerInput(name="Event",           type=String.class),
        @HandlerInput(name="ServerName",      type=String.class),
        @HandlerInput(name="Action",          type=String.class)})
        
        public static void saveClusterEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            mgRuleConfig.setEnabled(((Boolean)handlerCtx.getInputValue("Status")).booleanValue());
            mgRuleConfig.setDescription((String)handlerCtx.getInputValue("RuleDesc"));
            mgRuleConfig.getEventConfig().setRecordEvent(((Boolean)handlerCtx.getInputValue("RecordEvent")).booleanValue());
            mgRuleConfig.getEventConfig().setLevel((String)handlerCtx.getInputValue("LogLevel"));
            mgRuleConfig.getEventConfig().setDescription((String)handlerCtx.getInputValue("EventDesc"));
            String event = (String)handlerCtx.getInputValue("Event"); //NOI18N
            if(event != null){
                mgRuleConfig.getEventConfig().setPropertyValue("name", event); 
            }
            String serverName = (String)handlerCtx.getInputValue("ServerName"); //NOI18N
            if(serverName != null){
                mgRuleConfig.getEventConfig().setPropertyValue("servername", serverName); 
            }
            saveActionProperty(mgRuleConfig, handlerCtx);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the Log event types attributes in 
     *      Edit Management Rule Page </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Logger"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Level"           -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveLogEventValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true), 
        @HandlerInput(name="Status",          type=Boolean.class),
        @HandlerInput(name="RuleDesc",        type=String.class),
        @HandlerInput(name="RecordEvent",     type=Boolean.class),
        @HandlerInput(name="LogLevel",        type=String.class),
        @HandlerInput(name="EventDesc",       type=String.class),
        @HandlerInput(name="Logger",          type=String.class),
        @HandlerInput(name="Level",           type=String.class),
        @HandlerInput(name="Action",        type=String.class)})
        
        public static void saveLogEventValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            mgRuleConfig.setEnabled(((Boolean)handlerCtx.getInputValue("Status")).booleanValue());
            mgRuleConfig.setDescription((String)handlerCtx.getInputValue("RuleDesc"));
            mgRuleConfig.getEventConfig().setRecordEvent(((Boolean)handlerCtx.getInputValue("RecordEvent")).booleanValue());
            mgRuleConfig.getEventConfig().setLevel((String)handlerCtx.getInputValue("LogLevel"));
            mgRuleConfig.getEventConfig().setDescription((String)handlerCtx.getInputValue("EventDesc"));
            Object logList = handlerCtx.getInputValue("Logger"); //NOI18N
            if(logList instanceof String){
                String loggers = (String)logList;
                if(loggers != null){
                    mgRuleConfig.getEventConfig().setPropertyValue("loggernames", loggers); 
                }
            }else if(logList instanceof String[]){
                String[] lisVal = (String[])logList;
                String loggers = lisVal[0];
                if(loggers != null){
                    mgRuleConfig.getEventConfig().setPropertyValue("loggernames", loggers); 
                }
            }
            String level = (String)handlerCtx.getInputValue("Level"); //NOI18N
            if(level != null){
                mgRuleConfig.getEventConfig().setPropertyValue("level", level); 
            }
            saveActionProperty(mgRuleConfig, handlerCtx);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the type of Management Rule for 
     *      Edit Management Rule Page9s) </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Type"            -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getRuleType",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true) },
    output={
        @HandlerOutput(name="Type",         type=String.class) })
        
        public static void getRuleType(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName"); //NOI18N
            String ruleName = (String) handlerCtx.getInputValue("RuleName"); //NOI18N
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            handlerCtx.setOutputValue("Type", mgRuleConfig.getEventConfig().getType()); //NOI18N
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the Lists for the various types  
     *      of Management Rules </p>
     *  <p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RuleName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogLevelsList"   -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "ActionsList"     -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "EventsList"      -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getCommonLists",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="RuleName",        type=String.class, required=true) },
    output={
        @HandlerOutput(name="LogLevelsList",      type=SelectItem[].class),
        @HandlerOutput(name="EventsList",         type=SelectItem[].class),
        @HandlerOutput(name="ActionsList",        type=SelectItem[].class) })
        
        public static void getCommonLists(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String ruleName = (String) handlerCtx.getInputValue("RuleName");
            ConfigConfig config = AMXUtil.getConfig(configName);
            ManagementRuleConfig mgRuleConfig = config.getManagementRulesConfig().getManagementRuleConfigMap().get(ruleName);
            Map<String,String> propMap = mgRuleConfig.getEventConfig().getProperties();
            SelectItem[] levels = ConfigurationHandlers.getOptions(logLevels); 
            SelectItem[] events = null;
            String type = mgRuleConfig.getEventConfig().getType();
            if(type.equals("lifecycle")){
                events = ConfigurationHandlers.getOptions(lifeCycleEvents);
            }else if(type.equals("trace")){
                events = ConfigurationHandlers.getOptions(traceEvents);
            }else if(type.equals("cluster")) { //NOI18N
                events = ConfigurationHandlers.getOptions(clusterEvents);
            }
            handlerCtx.setOutputValue("LogLevelsList", levels);           
            handlerCtx.setOutputValue("ActionsList", getActions(configName)); //NOI18N
            handlerCtx.setOutputValue("EventsList", events); //NOI18N
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    private static void saveActionProperty(ManagementRuleConfig mgRuleConfig, HandlerContext handlerCtx) throws Exception {
        try{
            String action = (String)handlerCtx.getInputValue("Action");
            if((action != null) && (! action.equals(""))){
                if(mgRuleConfig.getActionConfig() != null){
                    mgRuleConfig.getActionConfig().setActionMBeanName(action);
                }else{
                    mgRuleConfig.createActionConfig(action);
                }
            }
        }catch(Exception ex){
            throw new Exception(ex);
        }
    }
    
    /**
     *	<p> This handler create a new Notification type Management Rule </p>
     *	@param	context	The HandlerContext.
     */
      public static void createMgmtRule(HandlerContext handlerCtx) throws Exception {
        try{
            Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
            Map mgmtRule = (Map) sessionMap.get("wizardMgmtRule");
            Map ruleTypes = (Map) sessionMap.get("wizardRuleTypes");
            String ruleName = (String)mgmtRule.get("ruleName"); //NOI18N
            String eventType = (String)mgmtRule.get("eventType"); //NOI18N
            Map optMap = new HashMap();
            optMap.put(ManagementRulesConfig.RULE_ENABLED_KEY, ((Boolean)mgmtRule.get("status")).toString()); 
            optMap.put(ManagementRulesConfig.RULE_DESCRIPTION_KEY, (String)mgmtRule.get("ruleDesc"));
            optMap.put(ManagementRulesConfig.EVENT_LOG_ENABLED_KEY, ((Boolean)ruleTypes.get("recordEvent")).toString());
            optMap.put(ManagementRulesConfig.EVENT_DESCRIPTION_KEY, (String)ruleTypes.get("eventDesc"));
            
            String logLevel = (String)mgmtRule.get("logLevel");
            if (! GuiUtil.isEmpty( logLevel))
                optMap.put(ManagementRulesConfig.EVENT_LEVEL_KEY, logLevel);
            
            //TODO Bug in AMX -- Classcast Exception on util.Properties because the optMap is defined 
            // as <String, String> in ConfigFactory though the create code expects Properties
            //optMap.put(ManagementRulesConfig.EVENT_PROPERTIES_KEY, getProperties(eventType, ruleTypes));
            ManagementRulesConfig mgRulesConfig = getManagementRulesConfig(mgmtRule);
            ManagementRuleConfig ruleConfig = mgRulesConfig.createManagementRuleConfig(ruleName,
                    eventType, null, optMap);
            setEventConfigProperties(ruleConfig, eventType, ruleTypes);
            String action = (String)ruleTypes.get("action");
            if((action != null) && (! action.equals(""))){
                ruleConfig.createActionConfig(action);
            }
        }catch (Exception ex){
            throw new Exception(ex);
        }
    }
    
    private static ManagementRulesConfig getManagementRulesConfig(Map mgmtRule){
        String configName = (String)mgmtRule.get("configName"); //NOI18N
        ConfigConfig config = AMXUtil.getConfig(configName);
        ManagementRulesConfig mgRulesConfig = config.getManagementRulesConfig();
        return mgRulesConfig;
    }
    
    private static Map getPropertiesMap(String eventType, Map ruleTypes){
        HashMap propsMap = new HashMap();
        if(eventType.equals("notification")){
            String beanName  = (String)ruleTypes.get("custMbean");
            if(beanName == null){
                beanName  = (String)ruleTypes.get("objName");
            }
            propsMap.put("sourceobjectname", beanName);
            propsMap.put("type", ruleTypes.get("notifType"));
        }else if(eventType.equals("lifecycle")){
            propsMap.put("event", ruleTypes.get("event"));
        }else if(eventType.equals("timer")){
            propsMap.put("datestring", ruleTypes.get("datestring"));
            propsMap.put("pattern", ruleTypes.get("pattern"));
            propsMap.put("period", ruleTypes.get("period"));
            propsMap.put("numberofoccurrences", ruleTypes.get("noOccurences"));
            propsMap.put("message", ruleTypes.get("message"));
        }else if(eventType.equals("log")){
            propsMap.put("loggernames", ruleTypes.get("logger"));
            propsMap.put("level", ruleTypes.get("level"));
        }else if(eventType.equals("trace")){
            propsMap.put("event", ruleTypes.get("event"));
        }
        return propsMap;
    }
    
    private static Properties getProperties(String eventType, Map ruleTypes){
        Properties props = new Properties();
        if(eventType.equals("notification")){
            String beanName  = (String)ruleTypes.get("custMbean");
            if(beanName == null){
                beanName  = (String)ruleTypes.get("objName");
            }
            props.put("sourceobjectname", beanName);
            props.put("type", ruleTypes.get("notifType"));
        }else if(eventType.equals("lifecycle")){
            props.put("event", ruleTypes.get("event"));
        }else if(eventType.equals("timer")){
            props.put("datestring", ruleTypes.get("datestring"));
            props.put("pattern", ruleTypes.get("pattern"));
            props.put("period", ruleTypes.get("period"));
            props.put("numberofoccurrences", ruleTypes.get("noOccurences"));
            props.put("message", ruleTypes.get("message"));
        }else if(eventType.equals("log")){
            props.put("loggernames", ruleTypes.get("logger"));
            props.put("level", ruleTypes.get("level"));
        }else if(eventType.equals("trace")){
            props.put("event", ruleTypes.get("event"));
        }
        return props;
    }
    
    private static void setEventConfigProperties(ManagementRuleConfig ruleConfig, String eventType, Map ruleTypes){
        ruleConfig.getEventConfig().setRecordEvent((Boolean)ruleTypes.get("recordEvent"));
        if(eventType.equals("notification")){
            String beanName  = (String)ruleTypes.get("custMbean");
            if(beanName == null){
                beanName  = (String)ruleTypes.get("objName");
            }
            if(beanName != null){
                ruleConfig.getEventConfig().setPropertyValue("sourceobjectname", beanName); 
            }
            String notifType = (String)ruleTypes.get("notifType");
            if(notifType != null){
                ruleConfig.getEventConfig().setPropertyValue("type", notifType);
            }
        }else if(eventType.equals("lifecycle")){
            String event = (String)ruleTypes.get("event");
            if(event != null){
                ruleConfig.getEventConfig().setPropertyValue("event", event);
            }
        }else if(eventType.equals("timer")){
            String dateString = (String)ruleTypes.get("datestring");
            if(dateString != null){
                ruleConfig.getEventConfig().setPropertyValue("datestring", dateString);
            }
            String pattern = (String)ruleTypes.get("pattern");
            if(pattern != null){
                ruleConfig.getEventConfig().setPropertyValue("pattern", pattern);
            }
            String period = (String)ruleTypes.get("period");
            if(period != null){
                ruleConfig.getEventConfig().setPropertyValue("period", period);
            }
            String numberofoccurrences = (String)ruleTypes.get("noOccurences");
            if(numberofoccurrences != null){
                ruleConfig.getEventConfig().setPropertyValue("numberofoccurrences", numberofoccurrences);
            }
            String message = (String)ruleTypes.get("message");
            if(message != null){
                ruleConfig.getEventConfig().setPropertyValue("message", message);
            }
        }else if(eventType.equals("log")){
            String loggernames = (String)ruleTypes.get("logger");
            if(loggernames != null){
                ruleConfig.getEventConfig().setPropertyValue("loggernames", loggernames);
            }
            String level = (String)ruleTypes.get("level");
            if(level != null){
                ruleConfig.getEventConfig().setPropertyValue("level", level);
            }
        }else if(eventType.equals("trace")){
            String event = (String)ruleTypes.get("event");
            if(event != null){
                ruleConfig.getEventConfig().setPropertyValue("event", event);
            }
        }else if(eventType.equals("cluster")){
            String event = (String)ruleTypes.get("event");
            if(event != null){
                ruleConfig.getEventConfig().setPropertyValue("name", event);
            }
            String servername = (String)ruleTypes.get("serverName");
            if(servername != null){
                ruleConfig.getEventConfig().setPropertyValue("servername", servername);
            }
        }else if(eventType.equals("monitor")){
            String obsMBean = (String)ruleTypes.get("obsMBean");
            String obsAttr = (String)ruleTypes.get("obsAttr");
            if(obsMBean == null || obsAttr == null || (obsMBean.equals("")) || (obsAttr.equals(""))){
                return;
            }
            ruleConfig.getEventConfig().setPropertyValue("observedobject", obsMBean);
            ruleConfig.getEventConfig().setPropertyValue("observedattribute", obsAttr);
            String granularity = (String)ruleTypes.get("granularity");
            if(granularity != null){
                ruleConfig.getEventConfig().setPropertyValue("granularityperiod", granularity);
            }
            String monitorType = (String)ruleTypes.get("monitorType");
            if((monitorType != null) && (! monitorType.equals(""))){
                if(monitorType.equals("countermonitor")){
                    ruleConfig.getEventConfig().setPropertyValue("monitortype", monitorType);
                    String diffMode = ((Boolean)ruleTypes.get("diffMode")).toString();
                    if(diffMode != null){
                        ruleConfig.getEventConfig().setPropertyValue("differencemode", diffMode);
                    }
                    String numType = (String)ruleTypes.get("numType");
                    if(numType != null){
                        ruleConfig.getEventConfig().setPropertyValue("numbertype", numType);
                    }
                    String initThresh = (String)ruleTypes.get("initThresh");
                    if(initThresh != null){
                        ruleConfig.getEventConfig().setPropertyValue("initthreshold", initThresh);
                    }
                    String offset = (String)ruleTypes.get("offset");
                    if(offset != null){
                        ruleConfig.getEventConfig().setPropertyValue("offset", offset);
                    }
                    String modulus = (String)ruleTypes.get("modulus");
                    if(modulus != null){
                        ruleConfig.getEventConfig().setPropertyValue("modulus", modulus);
                    }
                }else if(monitorType.equals("gaugemonitor")){
                    ruleConfig.getEventConfig().setPropertyValue("monitortype", monitorType);
                    String diffMode = ((Boolean)ruleTypes.get("diffModeGg")).toString();
                    if(diffMode != null){
                        ruleConfig.getEventConfig().setPropertyValue("differencemode", diffMode);
                    }
                    String numType = (String)ruleTypes.get("numTypeGg");
                    if(numType != null){
                        ruleConfig.getEventConfig().setPropertyValue("numbertype", numType);
                    }
                    String lowThresh = (String)ruleTypes.get("lowThresh");
                    if(lowThresh != null){
                        ruleConfig.getEventConfig().setPropertyValue("lowthreshold", lowThresh);
                    }
                    String highThresh = (String)ruleTypes.get("highThresh");
                    if(highThresh != null){
                        ruleConfig.getEventConfig().setPropertyValue("highthreshold", highThresh);
                    }
                }else if(monitorType.equals("stringmonitor")){
                    ruleConfig.getEventConfig().setPropertyValue("monitortype", monitorType);
                    String trigger = (String)ruleTypes.get("trigger");
                    if(trigger != null){
                        ruleConfig.getEventConfig().setPropertyValue("stringnotify", trigger);
                    }
                    String valueProp = (String)ruleTypes.get("valueProp");
                    if(valueProp != null){
                        ruleConfig.getEventConfig().setPropertyValue("stringtocompare", valueProp);
                    }
                }
            }
        }
    }
    
    private static Option[] getMgmtRuleEventTypes(boolean supportCluster){
        String[] grp1 = {"", "monitor"}; //NOI18N
        Option[] typeGrp1 = ConfigurationHandlers.getOptionsArray(grp1);
        OptionGroup sGroup1 = new OptionGroup();
        sGroup1.setLabel(""); //NOI18N
        sGroup1.setOptions(typeGrp1);
        
        String[] grp2 = {"notification"}; //NOI18N
        Option[] typeGrp2 = ConfigurationHandlers.getOptionsArray(grp2);
        OptionGroup sGroup2 = new OptionGroup();
        sGroup2.setLabel(""); //NOI18N
        sGroup2.setOptions(typeGrp2);
        
        String[] grp3 = null;
        if(supportCluster){
            grp3 = new String[]{"lifecycle", "log", "timer", "trace", "cluster"}; //NOI18N
        }else{
            grp3 = new String[]{"lifecycle", "log", "timer","trace"}; //NOI18N
        }    
        Option[] typeGrp3 = ConfigurationHandlers.getOptionsArray(grp3);
        OptionGroup sGroup3 = new OptionGroup();
        sGroup3.setLabel("System Events"); //NOI18N
        sGroup3.setOptions(typeGrp3);
        
        Option[] options = new Option[3];
        options[0] = sGroup1;
        options[1] = sGroup2;
        options[2] = sGroup3;
        return options;        
    }
    
    private static SelectItem[] getSourceMbeans(String eventType, String configName){
        String[] beanNames = getSourceMbeansNames(eventType, configName);
        SelectItem[] options = ConfigurationHandlers.getOptions(beanNames);
        return options;
    }
    
    private static String[] getSourceMbeansNames(String eventType, String configName){
        String mgmtRulesObjName = "com.sun.appserv:type=management-rules,config=" + configName + ",category=config";
        String[] signature = {"java.lang.String", "java.lang.String"};
        String[] arrList = null;
        if(eventType.equals("notification")){
            Object[] params = {eventType, "sourcembean"};
            List <String> list = (List)JMXUtil.invoke(mgmtRulesObjName, "getEventPropertyValues", params, signature);
            if(list != null){
                arrList = list.toArray(new String[list.size()]);
            }
        }
        if(arrList == null){
            arrList = new String[0];
        }
        return arrList;
    }
    
    private static SelectItem[] getActions(String configName){
        String mgmtRulesObjName = "com.sun.appserv:type=management-rules,config=" + configName + ",category=config";
        Object[] params = {Boolean.FALSE};
        String[] signature = {"boolean"};
        SelectItem[] options = null;
        List <String> list = (List)JMXUtil.invoke(mgmtRulesObjName, "getAllActionMBeans", params, signature);
        if(list != null){
            String[] arrList = list.toArray(new String[list.size()]);
            options = ConfigurationHandlers.getModOptions(arrList);
        }
        if(options == null){
            options = new SelectItem[0];
        }
        return options;
    }
    
    public static String[] getLoggerNames(){
        List logList = (List)JMXUtil.getAttribute("com.sun.appserv:name=logmanager,category=runtime,server=server",
                "LoggerNames");
        String[] loggers = (String[])logList.toArray(new String[logList.size()]);
        if(loggers != null){
            return loggers;
        }else{
            return new String[0];
        }  
    }
    
    private static String[] getTargetNames(){
        Set<String> standaloneSet = AMXUtil.getDomainConfig().getStandaloneServerConfigMap().keySet();
        Set<String> clusterSet = AMXUtil.getDomainConfig().getClusterConfigMap().keySet();
        
        Set<String> allTargets = new TreeSet<String>();
        allTargets.add("*");
        allTargets.addAll(standaloneSet);
        allTargets.addAll(clusterSet);
        String[] targets = (String[])allTargets.toArray(new String[allTargets.size()]);
        return targets;
    }
    
    private static String[] logLevels = {"FINEST", "FINER", "FINE", "CONFIG", "INFO", "WARNING", "SEVERE", "OFF"}; //NOI18N
    public static String[] lifeCycleEvents = {"ready", "shutdown", "termination"}; //NOI18N
    public static String[] traceEvents = {"ejb_component_method_entry", "ejb_component_method_exit", 
        "request_end", "request_start", "web_component_method_entry", "web_component_method_exit"}; //NOI18N
    public static String[] clusterEvents = {"fail", "start", "stop"}; //NOI18N
    private static String[] numTypes = {"byte", "double", "float", "int", "long", "short"}; //NOI18N
    private static String[] triggerTypes = {"Equals", "Differs From"}; //NOI18N
    private static String[] triggerValues = {"notifymatch", "notifydiffer"}; //NOI18N
    
}
