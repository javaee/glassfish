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

package org.glassfish.admingui.common.handlers;


import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import javax.management.Attribute;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  

import java.util.HashSet;
import java.util.Set;
import javax.management.ObjectName;
import org.glassfish.admin.amx.base.Query;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.intf.config.AMXConfigHelper;
import org.glassfish.admin.amx.intf.config.ConfigTools;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author Anissa Lam
 */
//TODO: Document these handlers
public class ProxyHandlers {
    @Handler(id="getChildrenTable",
        input={
            @HandlerInput(name="objectNameStr", type=String.class, required=true),
            @HandlerInput(name="childType", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getChildrenTable(HandlerContext handlerCtx){
        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        String childType = (String) handlerCtx.getInputValue("childType");
        List result = new ArrayList();

        AMXProxy amx = objectNameToProxy(objectNameStr);
        if (amx != null) {
            Map<String, AMXProxy> children = amx.childrenMap(childType);
            for(AMXProxy oneChild : children.values()){
                try{
                    AMXConfigHelper helper = new AMXConfigHelper((AMXConfigProxy) oneChild);
                    final Map<String,Object> attrs = helper.simpleAttributesMap();
                    HashMap oneRow = new HashMap();
                     oneRow.put("selected", false);
                    for(String attrName : attrs.keySet()){
                        oneRow.put(attrName, getA(attrs, attrName));
                        //String enableURL= (enabled.equals("true"))? "/resource/images/enabled.png" : "/resource/images/disabled.png";
                    }
                result.add(oneRow);
                }catch(Exception ex){
                    GuiUtil.handleException(handlerCtx, ex);
                }
            }
        }
        handlerCtx.setOutputValue("result", result);
    }

    private static String getA(Map<String, Object> attrs,  String key){
        Object val = attrs.get(key);
        return (val == null) ? "" : val.toString();
    }


     @Handler(id="deleteChildren",
        input={
            @HandlerInput(name="objectNameStr", type=String.class, required=true),
            @HandlerInput(name="type", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
     )
    public static void deleteChildren(HandlerContext handlerCtx){
         String type = (String) handlerCtx.getInputValue("type");
         String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
         AMXConfigProxy amx = (AMXConfigProxy) objectNameToProxy(objectNameStr);

         List obj = (List) handlerCtx.getInputValue("selectedRows");
         List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String Name = (String)oneRow.get("Name");
                amx.removeChild(type, Name);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


     public static AMXProxy objectNameToProxy(String objectNameStr){
         try {
            AMXProxy amx = V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            return amx;
         }catch(Exception ex){
             System.out.println("Cannot find object: " + objectNameStr);
             return null;
         }
     }


    /*  Get the simpleAttributes of the bean based on the objectNameString.
     *  simpleAttributes means those NOT of Element, ie all attributes.
     *  This requires the use of AMXConfigHelper, which thus causes the limitation that
     *  this mbean has to be AMXConfigProxy, not runtiime.
     *  For runtime mbeans, you need to use getRuntimeProxyAttrs.
     */
    @Handler(id="getProxyAttrs",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

        public static void getProxyAttrs(HandlerContext handlerCtx) {
        AMXProxy amx = null;
        try{
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            AMXConfigHelper helper = new AMXConfigHelper((AMXConfigProxy) amx);
            final Map<String,Object> attrs = helper.simpleAttributesMap();
            handlerCtx.setOutputValue("valueMap", attrs);
        }catch (Exception ex){
            if ( !(amx instanceof AMXConfigProxy) ){
                getRuntimeProxyAttrs(handlerCtx);
                return;
            }
            ex.printStackTrace();
            handlerCtx.setOutputValue("valueMap", new HashMap());
        }
    }


    @Handler(id="getRuntimeProxyAttrs",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

        public static void getRuntimeProxyAttrs(HandlerContext handlerCtx) {
        try{
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            AMXProxy  amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            final Map<String,Object> attrs = amx.attributesMap();
            handlerCtx.setOutputValue("valueMap", attrs);
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("valueMap", new HashMap());
        }
    }


    /*
     * Get the value of an attribute.  
     * If the attribute is an array, specifying an index will return an element in the array, otherwise, the entire array will be returned.
     */
    @Handler(id="getProxyAttribute",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true),
        @HandlerInput(name="attrName",   type=String.class, required=true),
        @HandlerInput(name="index",   type=String.class)},
    output={
        @HandlerOutput(name="value",        type=Object.class)})

    public static void getProxyAttribute(HandlerContext handlerCtx) {
        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        String attrName = (String) handlerCtx.getInputValue("attrName");
        Object result = "";
        try{
            AMXProxy  amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            Object val = amx.attributesMap().get(attrName);
            if (val instanceof Object[]){
                String index = (String) handlerCtx.getInputValue("index");
                if (index == null){
                    result = val;
                }else{
                    Object value = ((Object[])val)[Integer.parseInt(index)];
                    result = (value == null) ? "" : value.toString();
                }
            }else{
                result = (val == null) ? "" : val.toString();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        handlerCtx.setOutputValue("value", result);
    }


    @Handler(id="getDefaultProxyAttrs",
    input={
        @HandlerInput(name="parentObjectNameStr",   type=String.class, required=true),
        @HandlerInput(name="childType",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

    public static void getDefaultProxyAttrs(HandlerContext handlerCtx) {
        try{
            String parentName = (String) handlerCtx.getInputValue("parentObjectNameStr");
            String childType = (String) handlerCtx.getInputValue("childType");
            AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentName));
            Map valueMap = amx.getDefaultValues(childType, true);
            handlerCtx.setOutputValue("valueMap", valueMap);
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("valueMap", new HashMap());
        }

    }
    

    @Handler(id="proxyExist",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="exist",        type=Boolean.class)})

    public static void proxyExist(HandlerContext handlerCtx) {
        try{
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            handlerCtx.setOutputValue("exist", isProxyExist(objectNameStr));
        }catch(Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("exist", Boolean.FALSE);
        }
    }

    /*
     * Save the attributes of the proxy.   If the proxy doesn't exist, And forceCreate is true, a new
     * proxy will be created.
     */

    @Handler(id="saveBeanAttributes",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true),
        @HandlerInput(name="attrs",   type=Map.class),
        @HandlerInput(name="skipAttrs",   type=List.class),
        @HandlerInput(name="convertToFalse",   type=List.class),
        @HandlerInput(name="parentObjectNameStr",   type=String.class),
        @HandlerInput(name="forceCreate",   type=Boolean.class),
        @HandlerInput(name="childType",   type=String.class)} )
        public static void saveBeanAttributes(HandlerContext handlerCtx) {
        try{

            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");

            if (! isProxyExist(objectNameStr)){
                Boolean forceCreate = (Boolean) handlerCtx.getInputValue("forceCreate");
                if (forceCreate!=null && forceCreate.booleanValue()){
                    createProxy(handlerCtx);
                    return;
                }else{
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("error.noSuchProxy"));
                    return;
                }
            }

            Map attrs = (Map) handlerCtx.getInputValue("attrs");
            
            List<String> skipAttrs = (List) handlerCtx.getInputValue("skipAttrs");
            if (skipAttrs != null){
                for(String sk : skipAttrs){
                    if (attrs.keySet().contains(sk)){
                        attrs.remove(sk);
                    }
                }
            }

            List<String> convertToFalse = (List) handlerCtx.getInputValue("convertToFalse");
            if (convertToFalse != null){
                for(String sk : convertToFalse){
                    if (attrs.keySet().contains(sk)) {
                        if (attrs.get(sk) == null){
                            attrs.remove(sk);
                            attrs.put(sk, "false");
                        }
                    }
                }
            }
            V3AMX.setAttributes( objectNameStr, attrs);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
         @Handler(id="updateStatus",
        input={
            @HandlerInput(name="objectNameStr", type=String.class, required=true),
            @HandlerInput(name="enabled",   type=String.class),
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
     )
    public static void updateStatus(HandlerContext handlerCtx){
         String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
         AMXConfigProxy amx = (AMXConfigProxy) objectNameToProxy(objectNameStr);
         String status = (String) handlerCtx.getInputValue("enabled");
         List obj = (List) handlerCtx.getInputValue("selectedRows");
         List<Map> selectedRows = (List) obj;
         Attribute attr = null;
         
        try{
            for(Map oneRow : selectedRows){
                String Name = (String)oneRow.get("Name");
                System.out.println("object name is "+objectNameStr+Name);
                V3AMX.setAttribute(objectNameStr+Name, new Attribute("Enabled", status));
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    


    @Handler(id="createProxy",
    input={
        @HandlerInput(name="parentObjectNameStr",   type=String.class, required=true),
        @HandlerInput(name="childType",   type=String.class, required=true),
        @HandlerInput(name="attrs",   type=Map.class),
        @HandlerInput(name="skipAttrs",   type=List.class),
        @HandlerInput(name="onlyUseAttrs",   type=List.class),
        @HandlerInput(name="convertToFalse",   type=List.class)},
    output={
        @HandlerOutput(name="result",        type=String.class)})

        public static void createProxy(HandlerContext handlerCtx) {
        try{
            final String childType = (String) handlerCtx.getInputValue("childType");
            Map<String, Object> attrs = (Map) handlerCtx.getInputValue("attrs");
            String parentObjectNameStr = (String) handlerCtx.getInputValue("parentObjectNameStr");
            AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentObjectNameStr));

            List<String> convertToFalse = (List) handlerCtx.getInputValue("convertToFalse");
            if (convertToFalse != null){
                for(String sk : convertToFalse){
                    if (attrs.keySet().contains(sk)){
                        if (attrs.get(sk) == null){
                            attrs.put(sk, "false");
                        }
                    }
                }
            }

            //Should specify either skipAttrs or onlyUseAttrs
            List<String> skipAttrs = (List) handlerCtx.getInputValue("skipAttrs");
            V3AMX.removeSpecifiedAttr(attrs, skipAttrs);

            List<String> onlyUseAttrs = (List) handlerCtx.getInputValue("onlyUseAttrs");
            if (onlyUseAttrs != null){
                Map newAttrs = new HashMap();
                for(String key : onlyUseAttrs){
                    if (attrs.keySet().contains(key)){
                        newAttrs.put(key, attrs.get(key) );
                    }
                }
                attrs = newAttrs;
            }

//            System.out.println("========createChild========");
//            System.out.println(amx.toString());
//            System.out.println("childType = " + childType);
//            System.out.println(attrs);

            V3AMX.removeElement(attrs);
            AMXConfigProxy child = amx.createChild( childType,attrs);
            handlerCtx.setOutputValue("result", child.objectName().toString());
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }



    /*
     * This handler returns a list of children by its name.
     * Useful for creating dropdowns or listBox
     */
    @Handler(id="getChildrenByType",
    input={
        @HandlerInput(name="parentObjectNameStr",   type=String.class, required=true),
        @HandlerInput(name="type",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="result",        type=List.class)})

        public static void getChildrenByType(HandlerContext handlerCtx) {
        try{
            String type = (String) handlerCtx.getInputValue("type");
            String parentObjectNameStr = (String) handlerCtx.getInputValue("parentObjectNameStr");
            List result = new ArrayList();
            AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentObjectNameStr));
            Map<String, AMXProxy> childrenMap = amx.childrenMap(type);
            result.addAll(childrenMap.keySet());
            handlerCtx.setOutputValue("result",result);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    @Handler(id="getApplicationByType",
    input={
        @HandlerInput(name="type",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="result",        type=List.class)})

    public static void getApplicationByType(HandlerContext handlerCtx) {
        String type = (String) handlerCtx.getInputValue("type");
        AMXProxy amx = objectNameToProxy("v3:pp=/domain,type=applications");
        Map<String, AMXProxy> children = amx.childrenMap("application");
        List result = new ArrayList();
        for (AMXProxy oneChild : children.values()) {
            try {
                AMXConfigHelper helper = new AMXConfigHelper((AMXConfigProxy) oneChild);
                final Map<String, Object> attrs = helper.simpleAttributesMap();
                for (String attrName : attrs.keySet()) {
                    if (attrName.equals("Name")) {
                        String appName = getA(attrs, "Name");
                        Map<String, AMXProxy> module = objectNameToProxy("v3:pp=/domain/applications,type=application,name=" +appName ).childrenMap("module");

                        //The above 6 lines can be writen as
                        //Map <String, AMXProxy> module = oneChild.childrenMap("module");
                        //
                        for (AMXProxy oneModule : module.values()) {
                            AMXConfigHelper helperModule = new AMXConfigHelper((AMXConfigProxy) oneModule);
                            final Map<String, Object> modattrs = helperModule.simpleAttributesMap();
                            for (String modName : modattrs.keySet()) {
                                if (modName.equals("Children")) {
                                    ObjectName[] engines = (ObjectName[]) modattrs.get(modName);
                                    for (int i = 0; i < engines.length; i++) {
                                        String enginename = engines[i].getKeyProperty("name");
                                        if (enginename.equals(type)) {
                                            result.add(appName);
                                        }

                                    }
                                }

                            }

                        }
                    }
                }
            } catch (Exception ex) {
                GuiUtil.handleException(handlerCtx, ex);
            }
        }
        handlerCtx.setOutputValue("result", result);
    }




    /**
     *	<p> This handler goes through all the deployed application to search for all the application that has at least one module
     *  <p> with the specified sniffer.
     *
     *  <p> Input  value: "type" -- the name of the sniffer
     *  <p> Input  value: "fullName" -- boolean that indicates if the returned name should be presented as application#module name
     *  <p> or just the module name.   eg. myApp#myModule  vs  myModule
     *  <p> Input value: "result"  -- Type: <code> java.util.List</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getApplicationBySnifferType",
    input={
        @HandlerInput(name="type",   type=String.class, required=true),
        @HandlerInput(name="fullName",   type=Boolean.class)},
    output={
        @HandlerOutput(name="result",        type=List.class)})

    public static void getApplicationBySnifferType(HandlerContext handlerCtx) {
        String type = (String) handlerCtx.getInputValue("type");
        Boolean fullName = (Boolean) handlerCtx.getInputValue("fullName");
        if (fullName==null)
            fullName = false;

        AMXProxy amx = objectNameToProxy("v3:pp=/domain,type=applications");
        Map<String, AMXProxy> applications = amx.childrenMap("application");
        List result = new ArrayList();
        eachApp:  for (AMXProxy oneApp : applications.values()) {
            Map<String, AMXProxy> modules = oneApp.childrenMap("module");
            for(AMXProxy oneModule: modules.values()){
                Map<String, AMXProxy> engines = oneModule.childrenMap("engine");
                for(AMXProxy oneEngine: engines.values()){
                    if (oneEngine.getName().equals(type)){
                        String appName = oneApp.getName();
                        if (fullName){
                            AMXProxy earSniffer = oneApp.childrenMap("engine").get(SNIFFER_EAR);
                            result.add( (earSniffer == null)? appName :appName + "#" + oneModule.getName());
                            continue;
                        }else{
                            result.add(appName);
                            continue eachApp;
                        }
                    }
                }
            }
        }
        handlerCtx.setOutputValue("result", result);
    }

    @Handler(id="setProxyProperties",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true),
        @HandlerInput(name="systemProp",   type=Boolean.class),
        @HandlerInput(name="propertyList", type=List.class, required=true)})
        public static void setProxyProperties(HandlerContext handlerCtx) {
        try{
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            Boolean systemProp = (Boolean) handlerCtx.getInputValue("systemProp");
            ObjectName objectName = new ObjectName(objectNameStr);
            List<Map<String,String>> propertyList = (List)handlerCtx.getInputValue("propertyList");
            List newList = new ArrayList();
            Set propertyNames = new HashSet();
            final ConfigTools configTools = V3AMX.getInstance().getDomainRoot().getExt().child(ConfigTools.class);
            if (propertyList.size()==0){
                if ((systemProp != null) && (systemProp.booleanValue())){
                    configTools.clearSystemProperties(objectName);
                }else{
                    configTools.clearProperties(objectName);
                }
            }else{
                for(Map<String, String> oneRow : propertyList){
                    Map newRow = new HashMap();
                    final String  name = oneRow.get(PROPERTY_NAME);
                    if (GuiUtil.isEmpty(name)){
                        continue;
                    }
                    if (propertyNames.contains(name)){
                        GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.duplicatePropTableKey", new Object[]{name}));
                        return;
                    }else{
                        propertyNames.add(name);
                    }
                    
                    String value = oneRow.get(PROPERTY_VALUE);
                    if (GuiUtil.isEmpty(value)){
                        value = "";
                    }
                    newRow.put(PROPERTY_NAME,name);
                    newRow.put(PROPERTY_VALUE,value);
                    String desc = (String) oneRow.get(PROPERTY_DESC);
                    if (! GuiUtil.isEmpty(desc)){
                        newRow.put( PROPERTY_DESC,  desc);
                    }
                    newList.add(newRow);
                }
                if ((systemProp != null) && (systemProp.booleanValue())){
                    configTools.setSystemProperties(objectName, newList, true);
                }else{
                    configTools.setProperties(objectName, newList, true);
                }
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id="updateProxyProperties",
    input={
        @HandlerInput(name="propertyList", type=java.util.List.class, required=true)},
    output={
        @HandlerOutput(name="TableList", type=List.class)})
    public static void updateProxyProperties(HandlerContext handlerCtx) {
        try {
            List<Map<String, String>> propertyList = (List)handlerCtx.getInputValue("propertyList");
            List newList = new ArrayList();
            if (propertyList != null && propertyList.size() != 0) {
                for (Map<String, String> oneRow : propertyList) {
                    Map newRow = new HashMap();
                    newRow.put("selected", false);
                    final String name = oneRow.get(PROPERTY_NAME);
                    if (GuiUtil.isEmpty(name)) {
                        continue;
                    }
                    String value = oneRow.get(PROPERTY_VALUE);
                    if (GuiUtil.isEmpty(value)) {
                        value = "";
                    }
                    newRow.put(PROPERTY_NAME, name);
                    newRow.put(PROPERTY_VALUE, value);
                    String desc = (String) oneRow.get(PROPERTY_DESC);
                    if (!GuiUtil.isEmpty(desc)) {
                        desc = "";
                    }
                    newRow.put(PROPERTY_DESC, desc);
                    newList.add(newRow);
                }
            }
            handlerCtx.setOutputValue("TableList", newList);

        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    public static Map getDefaultProxyAttrsMap(String parentObjectNameStr, String childType) {
        try{
            String parentName = parentObjectNameStr;
            String child = childType;
            AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentName));
            Map valueMap = amx.getDefaultValues(child, true);
            return valueMap;
        }catch (Exception ex){
            ex.printStackTrace();
            return new HashMap();
        }
    }

    public static boolean isProxyExist(String objectNameStr){
        try{
            ObjectName objName = new ObjectName(objectNameStr);
            Query query = V3AMX.getInstance().getDomainRoot().getQueryMgr();
//            Set<ObjectName> result = query.queryTypeObjectNameSet(objName.getKeyProperty("type"));
            Set<ObjectName> result = query.queryAllObjectNameSet();
            return (result.contains(objName));
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }


    private static final String SNIFFER_EAR = "ear";
    //mbean Attribute Name
    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_VALUE = "Value";
    private static final String PROPERTY_DESC = "Description";
}
