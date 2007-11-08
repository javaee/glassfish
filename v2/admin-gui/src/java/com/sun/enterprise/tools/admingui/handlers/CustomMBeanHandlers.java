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
 * CustomMBeanHandlers.java
 *
 * Created on November 28, 2006, 1:15 AM
 *
 */

package com.sun.enterprise.tools.admingui.handlers;


import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.tools.admingui.util.FileUtil;
import com.sun.enterprise.tools.admingui.util.TargetUtil;
import com.sun.enterprise.tools.admingui.util.JarExtract;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.webui.jsf.model.UploadedFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import javax.management.Attribute;
import javax.management.AttributeList;
import com.sun.appserv.management.config.CustomMBeanConfig;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
/**
 *
 * @author Nitya Doraisamy
 */
public class CustomMBeanHandlers {
    
    private static String APPS_OBJNAME = "com.sun.appserv:type=applications,category=config";
    /**
     *	<p> This handler returns the list of Clusters and config info for populating the table.
     *	@param	context	The HandlerContext.
     *  <p> Input value: "SelectedRows"       -- Type: <code>java.util.List</code></p>
     *  <p> Output value: "Result"            -- Type: <code>java.util.List</code></p>
     */
    @Handler(id="getCustomMBeansList",
        input={
            @HandlerInput(name="SelectedRows", type=List.class)},
        output={
            @HandlerOutput(name="Result",      type=List.class)}
     )
     public static void getCustomMBeansList(HandlerContext handlerCtx){
        List result = new ArrayList();
        try{
            Map <String, CustomMBeanConfig> customMBeanMap = AMXUtil.getDomainConfig().getCustomMBeanConfigMap();
            List<Map> selectedList = (List)handlerCtx.getInputValue("SelectedRows");
            boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
            for(String key : customMBeanMap.keySet()){
                HashMap oneRow = new HashMap();
                CustomMBeanConfig mbeanConfig = customMBeanMap.get(key);
                String name = mbeanConfig.getName();
                String classname = mbeanConfig.getImplClassname();
                String status = TargetUtil.getEnabledStatus(mbeanConfig, true);
                boolean enabled = false;
                if(! AMXUtil.isEE()){
                    enabled = TargetUtil.isApplicationEnabled(mbeanConfig, "server");
                }        
                oneRow.put("name", (name == null) ? " ": name);
                oneRow.put("classname", (classname == null) ? " ": classname);
                oneRow.put("enable", enabled);
                oneRow.put("status", (status == null) ? " ": status);
                oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
                result.add(oneRow);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("Result", result);
    }
    
   /**
    *	<p> This handler returns the list of Clusters and config info for populating the table.
    *	@param	context	The HandlerContext.
    *  <p> Input value: "CustomMBeanName"       -- Type: <code>java.lang.String</code></p>
    *  <p> Input value: "Edit"                  -- Type: <code>java.lang.Boolean</code></p>
    *  <p> Input value: "ClassName"             -- Type: <code>java.lang.String</code></p>
    *  <p> Output value: "ImplClassName"        -- Type: <code>java.lang.String</code></p>
    *  <p> Output value: "ObjectName"           -- Type: <code>java.lang.String</code></p>
    *  <p> Output value: "Description"          -- Type: <code>java.lang.String</code></p>
    *  <p> Output value: "Status"               -- Type: <code>java.lang.Boolean</code></p>
    *  <p> Output value: "Result"               -- Type: <code>java.util.List</code></p>
    */
    @Handler(id="getCustomMBeanValues",
        input={
            @HandlerInput(name="CustomMBeanName", type=String.class, required=true),
            @HandlerInput(name="Edit",            type=Boolean.class, required=true),
            @HandlerInput(name="ClassName",       type=String.class, required=true)},
        output={
            @HandlerOutput(name="ImplClassName",  type=String.class),
            @HandlerOutput(name="ObjectName",     type=String.class),
            @HandlerOutput(name="Description",    type=String.class),
            @HandlerOutput(name="Status",         type=Boolean.class),
            @HandlerOutput(name="StatusString",   type=String.class),
            @HandlerOutput(name="Result",         type=List.class),
            @HandlerOutput(name="Properties",     type=Map.class) })
     public static void getCustomMBeanValues(HandlerContext handlerCtx){
        try{
            CustomMBeanConfig mbeanConfig = null;
            boolean edit = ((Boolean)handlerCtx.getInputValue("Edit")).booleanValue();
            String implClassName = "";
            Map<String, String> props = new HashMap();
            boolean status = true;
            if(edit){
                mbeanConfig = AMXUtil.getDomainConfig().getCustomMBeanConfigMap().get(handlerCtx.getInputValue("CustomMBeanName"));
                implClassName = mbeanConfig.getImplClassname();
                handlerCtx.setOutputValue("ObjectName", mbeanConfig.getObjectNameInConfig());
                handlerCtx.setOutputValue("Description", mbeanConfig.getDescription());
                props = mbeanConfig.getProperties();
                if(AMXUtil.isEE()){
                    handlerCtx.setOutputValue("StatusString", TargetUtil.getEnabledStatus(mbeanConfig, true));
                }else{
                    status = TargetUtil.isApplicationEnabled(mbeanConfig, "server");
                }
            }else{
                implClassName = (String)handlerCtx.getInputValue("ClassName");
            }
            List result = new ArrayList();
            Object[] params = {implClassName};
            String[] signature = {"java.lang.String"};
            MBeanInfo mbinfo = (MBeanInfo) JMXUtil.invoke(APPS_OBJNAME, "getMBeanInfo", params, signature);
            MBeanAttributeInfo[] attrArray = mbinfo.getAttributes();
            if (attrArray != null && attrArray.length > 0){
                AttributeList attrList = null;
                if(edit){
                    String objName = "com.sun.appserv:type=mbean,category=config,name=" + mbeanConfig.getName();
                    attrList = (AttributeList) JMXUtil.invoke(objName, "getProperties", null, null);
                }
                for(int i=0; i<attrArray.length; i++){
                    HashMap oneRow = new HashMap();
                    String name = attrArray[i].getName();
                    oneRow.put("name", name);
                    oneRow.put("type", attrArray[i].getType());
                    oneRow.put("value", "");
                    if(edit){
                        if(Arrays.asList(mbeanConfig.getPropertyNames()).contains(name)){
                            oneRow.put("value", mbeanConfig.getPropertyValue(name));
                        }
                    }
                    result.add(oneRow);
                }
            }

            handlerCtx.setOutputValue("Status", status);
            handlerCtx.setOutputValue("ImplClassName", implClassName);
            handlerCtx.setOutputValue("Result", result);
            handlerCtx.setOutputValue("Properties", props);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
    *	<p> This handler returns the list of Clusters and config info for populating the table.
    *	@param	context	The HandlerContext.
    *  <p> Input value: "CustomMBeanName"      -- Type: <code>java.lang.String</code></p>
    *  <p> Input value: "Edit"                 -- Type: <code>java.lang.Boolean</code></p>
    *  <p> Input value: "ImplClassName"        -- Type: <code>java.lang.String</code></p>
    *  <p> Input value: "ObjectName"           -- Type: <code>java.lang.String</code></p>
    *  <p> Input value: "Description"          -- Type: <code>java.lang.String</code></p>
    *  <p> Input value: "Status"               -- Type: <code>java.lang.Boolean</code></p>
    *  <p> Input value: "Targets"              -- Type: <code>java.util.Array</code></p>
    *  <p> Input value: "AddProps"             -- Type: <code>java.util.Map</code></p>
    *  <p> Input value: "RemoveProps"          -- Type: <code>java.util.List</code></p>
    */
    @Handler(id="saveCustomMBeanValues",
        input={
            @HandlerInput(name="CustomMBeanName", type=String.class, required=true),
            @HandlerInput(name="Edit",            type=Boolean.class, required=true),
            @HandlerInput(name="ImplClassName",   type=String.class, required=true),
            @HandlerInput(name="ObjectName",      type=String.class),
            @HandlerInput(name="Description",     type=String.class),
            @HandlerInput(name="Status",          type=Boolean.class),
            @HandlerInput(name="Targets",         type=String[].class),
            @HandlerInput(name="NewList",         type=List.class),
            @HandlerInput(name="OldList",         type=Map.class) })
     public static void saveCustomMBeanValues(HandlerContext handlerCtx){
        try{
            CustomMBeanConfig mbeanConfig = null;
            String beanName = (String)handlerCtx.getInputValue("CustomMBeanName");
            boolean edit = ((Boolean)handlerCtx.getInputValue("Edit")).booleanValue();
            
            String objNameProp = (String)handlerCtx.getInputValue("ObjectName");
            List newList = (List)handlerCtx.getInputValue("NewList");
            
            if(edit){
                mbeanConfig = AMXUtil.getDomainConfig().getCustomMBeanConfigMap().get(beanName);
                if(! AMXUtil.isEE()){
                    boolean enabled = ((Boolean)handlerCtx.getInputValue("Status")).booleanValue();
                    TargetUtil.setApplicationEnabled(mbeanConfig, "server", enabled);
                }
            }else{
                boolean enabled = ((Boolean)handlerCtx.getInputValue("Status")).booleanValue();
                String implClassName = (String)handlerCtx.getInputValue("ImplClassName");
                String[] targets = (String[])handlerCtx.getInputValue("Targets");
                Map propsMap = new HashMap();
                propsMap.put("name", beanName);
                propsMap.put("impl-class-name", implClassName);
                createCustomConfig(targets, propsMap, newList, enabled);
                mbeanConfig = AMXUtil.getDomainConfig().getCustomMBeanConfigMap().get(beanName);
            }    
            
            //seems some timing problem in Window platform during creation that the mbeanConfig
            //wasn't avaible right away.  change to use JMX to set description.
            String desc = (String)handlerCtx.getInputValue("Description");
            String objName = "com.sun.appserv:type=mbean,category=config,name=" + beanName;
            JMXUtil.setAttribute(objName, new Attribute("description", desc));
            
            //FIXME
            //Attribute attr = new Attribute("object-name", handlerCtx.getInputValue("ObjectName"));
            //JMXUtil.setAttribute(objName, attr);
        
            if(edit){
                ListIterator newListItr = newList.listIterator();
                Map<String, String> oldList = (Map)handlerCtx.getInputValue("OldList");
                String methodName = "setProperty";
                String[] signature = new String[]{"javax.management.Attribute"};
                if (oldList != null) {
                    String[] oldarray = (String[])oldList.keySet().toArray(new String[oldList.size()]);
                    for (int i = 0; i < oldarray.length; i++) {
                        String origPropName = oldarray[i];
                        boolean found = false;
                        while(newListItr.hasNext()) {
                            Map newProps = (Map)newListItr.next();
                            if (newProps.get("name").equals(origPropName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Attribute removeProp = new Attribute(origPropName, null);
                            Object[] params = new Object[]{removeProp};
                            JMXUtil.invoke(objName, methodName, params, signature);
                        }
                    }
                }
                while(newListItr.hasNext()) {
                    Map newProps = (Map)newListItr.next();
                    Attribute addAttr = new Attribute((String)newProps.get("name"), newProps.get("value"));
                    Object[] params = new Object[]{addAttr};
                    JMXUtil.invoke(objName, methodName, params, signature);
                }
                /*
                 * AMX removeProperty on mbeanConfig is causing exceptions in server log
                 * though it does remove the property too.
                if(removeProps != null){
                   String[] remove = (String[])removeProps.toArray(new String[removeProps.size()]);
                    for(int i=0; i<remove.length; i++){
                        mbeanConfig.removeProperty(remove[i]);
                    }
                }
                if(addProps != null ){
                    for(String key: addProps.keySet()){
                        String value = addProps.get(key);
                        mbeanConfig.setPropertyValue(PropertiesAccess.PROPERTY_PREFIX + key, value);
                    }
                }
                */
            } //if -- edit
            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("customData", new HashMap());
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
        
    /**
    *	<p> This handler deployes the Custom MBean jar
    *	@param	context	The HandlerContext.
    *  <p> Input value: "LocationGroup"    -- Type: <code>java.lang.String</code></p>
    *  <p> Input value: "UploadedFile"     -- Type: <code>java.lang.String</code></p>
    *  <p> Input value: "DirPath"          -- Type: <code>java.lang.String</code></p>
    *  <p> Input value: "ImplClassName"    -- Type: <code>java.lang.String</code></p>
    */
    @Handler(id="deployCustomMBean",
        input={
            @HandlerInput(name="LocationGroup",     type=String.class),
            @HandlerInput(name="UploadedJar",       type=UploadedFile.class),
            @HandlerInput(name="DirPath",           type=String.class),
            @HandlerInput(name="ImplClassName",     type=String.class) })
    public static void deployCustomMBean(HandlerContext handlerCtx){
        try{
            String locationType = (String)handlerCtx.getInputValue("LocationGroup");
            Map dataMap = new HashMap();
            if(locationType.equals("skip")){
                dataMap.put("fileName", "N/A");
            }else{
                String fileName = processCustomMBeanJar(locationType, handlerCtx);
                dataMap.put("fileName", fileName);
                dataMap.put("name", FileUtil.getFileName(fileName));
            }    
            
            String className = (String)handlerCtx.getInputValue("ImplClassName");
            dataMap.put("implClassName", className);
            
            Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
            sessionMap.put("customData", dataMap);
            
            Object[] params = {className};
            String[] types = {"java.lang.String"};
            JMXUtil.invoke(APPS_OBJNAME, "getMBeanInfo", params, types);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler takes in selected rows, and do the undeployment
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="undeployCustomMBean",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true) })
    public static void undeployCustomMBean(HandlerContext handlerCtx) {
        try{
            Object obj = handlerCtx.getInputValue("selectedRows");
            List selectedRows = (List) obj;
            for(int i=0; i< selectedRows.size(); i++){
                Map oneRow = (Map) selectedRows.get(i);
                String appName = (String) oneRow.get("name");
                CustomMBeanConfig mbeanConfig = AMXUtil.getDomainConfig().getCustomMBeanConfigMap().get(appName);
                List<String> targetList = TargetUtil.getDeployedTargets((AMX)mbeanConfig, true);
                if(targetList.size() == 0){
                    String operName = "removeMbeanByName";
                    Object[] params = new Object[]{appName};
                    String[] signature = new String[]{"java.lang.String"};
                    JMXUtil.invoke(APPS_OBJNAME, operName, params, signature);
                }else{
                    for(String target: targetList){
                        String operName = "deleteMBean";
                        Object[] params = new Object[]{target, appName};
                        String[] signature = new String[]{"java.lang.String", "java.lang.String"};
                        JMXUtil.invoke(APPS_OBJNAME, operName, params, signature);
                    }
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    private static String processCustomMBeanJar(String locationType, HandlerContext handlerCtx) throws Exception {
        String fileName = "";
        String jarToExtract = "";
        try{
            if(locationType.equals("client")){
                UploadedFile uploadJar = (UploadedFile)handlerCtx.getInputValue("UploadedJar");
                if(uploadJar != null) {
                    fileName = uploadJar.getOriginalName();
                    //see bug# 6498910, 6511392, for IE, getOriginalName() returns the full path, including the drive.
                    //for any other browser, it just returns the file name.
                    int lastIndex = fileName.lastIndexOf("\\");
                    if (lastIndex != -1){
                        fileName = fileName.substring(lastIndex+1, fileName.length());
                    }
                    File tmpFile = null;
                    String suffix = fileName.substring(fileName.indexOf("."));
                    String prefix = fileName.substring(0, fileName.indexOf("."));
                    try {
                        tmpFile = File.createTempFile(prefix, suffix);
                        uploadJar.write(tmpFile);
                        jarToExtract = tmpFile.getCanonicalPath();
                    } catch (IOException ioex) {
                        try {
                            jarToExtract = tmpFile.getAbsolutePath();
                        } catch (Exception ex) {
                            //Handle AbsolutePathException here
                        }
                    } catch (Exception ex) {
                        throw new Exception(ex);
                    }
                }
            }else if(locationType.equals("server")){
                jarToExtract = (String)handlerCtx.getInputValue("DirPath");
                fileName = jarToExtract;
            }
            extractMbeanJarFile(TargetUtil.getDomainRoot(), jarToExtract);
        }catch(Exception ex){
            throw new Exception(ex);
        }
        return fileName;
    }
    
    private static void extractMbeanJarFile(String domainRoot, String fileName) throws IOException {
            File f = new File(fileName);
            if(FileUtil.isJarFile(f)) {
               //We have to extract it into mbeans directory for the classloader to load it.
               JarExtract.extract(fileName, domainRoot+"/applications/mbeans");
            }
    } 
    
    private static CustomMBeanConfig getCustomConfig(String implClassName){
        CustomMBeanConfig mbeanConfig = null;
        Iterator iter = AMXUtil.getDomainConfig().getCustomMBeanConfigMap().values().iterator();
        if (iter != null){
            while(iter.hasNext()){
                mbeanConfig = (CustomMBeanConfig) iter.next();
                String className = mbeanConfig.getImplClassname();
                if(className.equals(implClassName)){
                    break;
                }
            }
        }
        return mbeanConfig;
    }
    
    private static CustomMBeanConfig createCustomConfig(String[] targets, Map propsMap, Object attributes, boolean enabled){
        CustomMBeanConfig mbeanConfig = null;
        if(targets == null || targets.length == 0) {
            //By default deploying to DAS to be in sync. with CLI.
            targets = new String[]{"server"};
        }
        String operName ="createMBean";
        String[] signature = {"java.lang.String", "java.util.Map", "java.util.Map"};
        
        for (int i = 0; i < targets.length; i++) {
            Map attrMap = null;
            if(attributes instanceof List){
                attrMap = createAttributesMap((List)attributes);
            }else{
                attrMap = (Map)attributes;
            }
            Object[] params = {targets[i], propsMap, attrMap};
            JMXUtil.invoke(APPS_OBJNAME, operName, params, signature);
            String beanObjName = "com.sun.appserv:type=application-ref,category=config,server=" + targets[i] + ",ref=" + propsMap.get("name");
            Attribute attr = new Attribute("enabled", enabled);
            JMXUtil.setAttribute(beanObjName, attr);
        }
        return mbeanConfig;
    }
    
    /**
     *	<p> This handler creates references for the given custommbean
     *  <p> Input value: "Name"      -- Type: <code>String</code>/</p>
     *  <p> Input value: "Targets"   -- Type: <code>String[]</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="createMBeanReferences",
        input={
        @HandlerInput(name="Name",      type=String.class, required=true),
        @HandlerInput(name="Targets",   type=String[].class, required=true )})
    public static void createMBeanReferences(HandlerContext handlerCtx) {
        String name = (String)handlerCtx.getInputValue("Name");
        String[] selTargets = (String[])handlerCtx.getInputValue("Targets");
        List<String> targets = Arrays.asList(selTargets);
        List<String> associatedTargets = TargetUtil.getDeployedTargets(name, true);
        try{
            Vector newTargets = new Vector();
            Map attrsMap = new HashMap();
            Map propsMap = new HashMap();
            for(String targetName:targets) {
                if(!(associatedTargets.contains(targetName))) {
                    CustomMBeanConfig mbeanConfig = AMXUtil.getDomainConfig().getCustomMBeanConfigMap().get(name);
                    String implClassName = mbeanConfig.getImplClassname();
                    attrsMap.putAll(mbeanConfig.getProperties());
                    propsMap.put("name", name);
                    propsMap.put("impl-class-name", implClassName);
                    newTargets.add(targetName);
                }               
            }
            if(newTargets.size() > 0){
                createCustomConfig((String[]) newTargets.toArray(new String[newTargets.size()]), propsMap, attrsMap, true);
            }    
            
            boolean inclServer = false;
            //removes the old application references
            for(String targetName:associatedTargets) {
                if(!(targets.contains(targetName))) {
                    ServerConfig target = (ServerConfig)AMXUtil.getDomainConfig().getStandaloneServerConfigMap().get(targetName);
                    if(!targetName.equals("server")){
                        if(target != null){
                            Object[] params = {name};
                            String[] removeSignatures = {"java.lang.String"};
                            String objName = "com.sun.appserv:type=server,name=" + targetName + ",category=config";
                            JMXUtil.invoke(objName, "removeApplicationRefByRef", params, removeSignatures);
                        }else{
                            String operName = "deleteMBean";
                            Object[] params = new Object[]{targetName, name};
                            String[] signature = new String[]{"java.lang.String", "java.lang.String"};
                            JMXUtil.invoke(APPS_OBJNAME, operName, params, signature);
                        }
                    }else{
                        inclServer = true;
                    }
                }
            }
            /*
             * If you remove the 'server' reference before the other targets,
             * then the deleteMBean will result in undeployment 
             * So remove the reference to server last.
             */
            if(inclServer){
                Object[] params = {name};
                String[] removeSignatures = {"java.lang.String"};
                String objName = "com.sun.appserv:type=server,name=server,category=config";
                JMXUtil.invoke(objName, "removeApplicationRefByRef", params, removeSignatures);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    private static String getMbeanPropertyValue(AttributeList attrList, String name){
        if( attrList == null) 
            return null;
        Iterator it = attrList.iterator();
        while (it.hasNext()) {
            Attribute att = (Attribute)it.next();
            if (att.getName().equals(name)) {
                return  (String) att.getValue();
            }
        }
        return null;
    }
    
    private static Map createAttributesMap(List attrList){
        Map<String, String> propsMap = new HashMap();
        if(attrList != null){
            ListIterator attrsItr= attrList.listIterator();
            while(attrsItr.hasNext()) {
                Map<String, String> newProps = (Map)attrsItr.next();
                String value = newProps.get("value");
                if((value != null) && (!value.equals(""))){
                    propsMap.put(newProps.get("name"), value);
                }
            }
        }
        return propsMap;
    }

}

