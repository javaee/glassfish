/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * ConnectorHandlers.java
 *
 * Created on Sept 1, 2006, 8:32 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 */
package org.glassfish.jca.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;


import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;


public class ConnectorsHandlers {

    /** Creates a new instance of ConnectorsHandler */
    public ConnectorsHandlers() {
    }


    /**
     *	<p> This handler creates a ConnectorConnection Pool to be used in the wizard
     */
    @Handler(id = "gf.getConnectorConnectionPoolWizard", input = {
        @HandlerInput(name = "fromStep2", type = Boolean.class),
        @HandlerInput(name = "fromStep1", type = Boolean.class),
        @HandlerInput(name = "attrMap", type = Map.class),
        @HandlerInput(name = "poolName", type = String.class),
        @HandlerInput(name = "resAdapter", type = String.class)
        }, output = {
        @HandlerOutput(name = "resultAdapter", type = String.class)
    })
    public static void getConnectorConnectionPoolWizard(HandlerContext handlerCtx) {
        Boolean fromStep2 = (Boolean) handlerCtx.getInputValue("fromStep2");
        Boolean fromStep1 = (Boolean) handlerCtx.getInputValue("fromStep1");
        if ((fromStep2 != null) && fromStep2) {
            //wizardMap is already in session map, we don't want to change anything.
            Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
            String resAdapter = (String) extra.get("ResourceAdapterName");
            handlerCtx.setOutputValue("resultAdapter", resAdapter);
        } else if ((fromStep1 != null) && fromStep1) {
            //this is from Step 1 where the page is navigated when changing the dropdown of resource adapter.
            //since the dropdown is immediate, the wizardPoolExtra map is not updated yet, we need
            //to update it manually and also set the connection definition map according to this resource adapter.
            String resAdapter = (String) handlerCtx.getInputValue("resAdapter");
            if (resAdapter != null) {
                resAdapter = resAdapter.trim();
            }
            String poolName = (String) handlerCtx.getInputValue("poolName");
            if (poolName != null) {
                poolName = poolName.trim();
            }
            if (resAdapter != null && !(resAdapter.equals(""))) {               
                Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
                extra.put("ResourceAdapterName", resAdapter);
                extra.put("Name", poolName);                
                handlerCtx.setOutputValue("resultAdapter", resAdapter);
            }
        } else {
            Map extra = new HashMap();
            Map attrMap = (Map) handlerCtx.getInputValue("attrMap");
            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardMap", attrMap);
            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolExtra", extra);
        }
    }

    /**
     *	<p> updates the wizard map
     */
    @Handler(id = "gf.updateConnectorConnectionPoolWizard",
        input = {
            @HandlerInput(name = "props", type = Map.class),
            @HandlerInput(name = "currentAdapter", type = String.class),
            @HandlerInput(name = "currentDef", type = String.class)})
    public static void updateConnectorConnectionPoolWizard(HandlerContext handlerCtx) {
        Map<String, String> props = (Map<String, String>) handlerCtx.getInputValue("props");
        if (props != null) {
            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolProperties", GuiUtil.convertMapToListOfMap(props));
        } else {
            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolProperties", new ArrayList());
        }
        Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
        extra.put("previousDefinition", (String) handlerCtx.getInputValue("currentDef"));
        extra.put("previousResAdapter", (String) handlerCtx.getInputValue("currentAdapter"));
    }
    
    /**
     *	<p> updates the wizard map properties on step 2
     */
    @Handler(id = "updateConnectorConnectionPoolWizardStep2")
    public static void updateConnectorConnectionPoolWizardStep2(HandlerContext handlerCtx) {
        Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
        Map attrs = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardMap");

        String resAdapter = (String) extra.get("ResourceAdapterName");
        String definition = (String) extra.get("ConnectionDefinitionName");
        String name = (String) extra.get("Name");

        attrs.put("Name", name);
        attrs.put("ConnectionDefinitionName", definition);
        attrs.put("ResourceAdapterName", resAdapter);
    }
    
    /**
     *	<p> This handler determines whether usergroups or principals will be used and returns appropriate string array
     */
    @Handler(id="convertSecurityMapPropsToStringArray",
         input={
            @HandlerInput(name="usersOptions", type=String.class),
            @HandlerInput(name="edit", type=String.class),
            @HandlerInput(name="userGroups", type=String.class),
            @HandlerInput(name="principals", type=String.class)
            },
        output = {
            @HandlerOutput(name = "principalsSA", type = String[].class),
            @HandlerOutput(name = "usersSA", type = String[].class)
            })
    public static void convertSecurityMapPropsToStringArray(HandlerContext handlerCtx) {

        String option = (String) handlerCtx.getInputValue("usersOptions");
        String edit = (String) handlerCtx.getInputValue("edit");
        String userGroups = (String) handlerCtx.getInputValue("userGroups");
        String principals = (String) handlerCtx.getInputValue("principals");
        String value = null;
        String[] str = null;
        Object emptyVal = null;
        boolean usePrincipals = false;
        //Take either userGroups or Principals
        if(option != null){
             value = userGroups;
             usePrincipals = false;
        } else {
            value = principals;
            usePrincipals = true;
        }

        if (value != null && value.indexOf(",") != -1) {
            str = GuiUtil.stringToArray(value, ",");
        } 
        else {
            str = new String[1];
            str[0] = value;
        }
        
        if(edit.equals("true"))
            emptyVal = new String[0];
        handlerCtx.setOutputValue("principalsSA", (usePrincipals)? str : emptyVal);
        handlerCtx.setOutputValue("usersSA", (usePrincipals)? emptyVal : str);

    }

    /**
     *	<p> This handler creates a Admin Object Resource
     */
    @Handler(id = "gf.getAdminObjectResourceWizard", input = {
    @HandlerInput(name = "reload", type = Boolean.class),
    @HandlerInput(name = "attrMap", type = Map.class),
    @HandlerInput(name = "currentMap", type = Map.class)
    }, output = {
    @HandlerOutput(name = "valueMap", type = Map.class)
    })
    public static void getAdminObjectResourceWizard(HandlerContext handlerCtx) {
        Boolean reload = (Boolean) handlerCtx.getInputValue("reload");
        Map attrMap = (Map) handlerCtx.getInputValue("attrMap");
        Map currentMap = (Map) handlerCtx.getInputValue("currentMap");
        String name = null;
        String resAdapter = null;
        String resType = null;
        String className = null;
        if (attrMap == null) {
            attrMap = new HashMap();
        }
        if (((reload == null) || (!reload)) && (currentMap != null)) {
            name = (String) currentMap.get("name");
            resAdapter = (String) currentMap.get("resAdapter");
            resType = (String) currentMap.get("resType");
            className = (String) currentMap.get("className");
            attrMap.putAll(currentMap);
        } else {
            if (attrMap != null) {
                name = (String) attrMap.get("name");
                resAdapter = (String) attrMap.get("resAdapter");
                resType = (String) attrMap.get("resType");
                className = (String) attrMap.get("className");
            }
        }
        if (resAdapter != null) {
            resAdapter = resAdapter.trim();
        }
        if (resAdapter == null || resAdapter.equals("")) {
            resAdapter = "jmsra";
        }

        attrMap.put("name", name);
        attrMap.put("resType", resType);
        attrMap.put("resAdapter", resAdapter);
        attrMap.put("className", className);
        handlerCtx.setOutputValue("valueMap", attrMap);
    }
    
     @Handler(id = "createWorkSecurityMap",
        input = {
            @HandlerInput(name = "parentObjectNameStr", type = String.class, required = true),
            @HandlerInput(name = "childType", type = String.class, required = true),
            @HandlerInput(name = "usersOptionG", type = String.class),
            @HandlerInput(name = "usersOptionP", type = String.class),
            @HandlerInput(name = "attrs", type = Map.class),
            @HandlerInput(name = "mapping", type = Map.class),
            @HandlerInput(name = "convertToFalse", type = List.class)})
    public static void createWorkSecurityMap(HandlerContext handlerCtx) {
         String parentObjectNameStr = (String) handlerCtx.getInputValue("parentObjectNameStr");
         String childType = (String) handlerCtx.getInputValue("childType");
         String usersOptionG = (String) handlerCtx.getInputValue("usersOptionG");
         String usersOptionP = (String) handlerCtx.getInputValue("usersOptionP");
         Map<String, Object> attrs = (Map) handlerCtx.getInputValue("attrs");
         Map<String, Object> mappings = (Map) handlerCtx.getInputValue("mapping");

         try {
             AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getProxy(new ObjectName(parentObjectNameStr));
             List<String> convertToFalse = (List) handlerCtx.getInputValue("convertToFalse");

             if (convertToFalse != null) {
                 for (String sk : convertToFalse) {
                     if (attrs.keySet().contains(sk)) {
                         if (attrs.get(sk) == null) {
                             attrs.put(sk, "false");
                         }
                     }
                 }
             }

             V3AMX.removeElement(attrs);
             /* If user doesn't fill in anything, we need to remove it, otherwise, it is written out as "" in domain.xml and
              * user will not be able to get the default value.
              * Another reason is for attributes that is optional but is an enum, eg  transactionIsolationLevel in jdbc connection
              * pool, (read-uncommitted|read-committed|repeatable-read|serializable)  pass in "" will result in constraints
              * violation.
              */
             Set<Map.Entry<String, Object>> attrSet = attrs.entrySet();
             Iterator<Map.Entry<String, Object>> iter = attrSet.iterator();
             while (iter.hasNext()) {
                 Map.Entry<String, Object> oneEntry = iter.next();
                 Object val = oneEntry.getValue();
                 if ((val != null) && (val instanceof String) && (val.equals(""))) {
                     iter.remove();
                 }
             }

             AMXConfigProxy workSecurityMap = amx.createChild(childType, attrs);
             setWorkSecurityMapChildren(workSecurityMap, usersOptionG, usersOptionP, mappings);

         } catch (Exception ex) {
             GuiUtil.getLogger().severe("createWorkSecurityMap error");
             GuiUtil.handleException(handlerCtx, ex);
         }
    }


     private static void setWorkSecurityMapChildren(AMXConfigProxy amx, String usersOptionG, String usersOptionP, Map mappings){
         Map<String,String> mappingAttrs = new HashMap();
         if ((usersOptionG != null) && usersOptionG.equals("users")) {
             String groupsMapping = (String) mappings.get("GroupMapping");
             mappingAttrs = stringToMap(groupsMapping, ",");
             for(String key: mappingAttrs.keySet()){
                 Map<String, Object>  attrs = new HashMap();
                 attrs.put("EisGroup", key);
                 attrs.put("MappedGroup", mappingAttrs.get(key));
                 amx.createChild("group-map", attrs);
             }
         } else {
             String principalMapping = (String) mappings.get("PrincipalMapping");
             mappingAttrs = stringToMap(principalMapping, ",");
             for(String key: mappingAttrs.keySet()){
                 Map attrs = new HashMap();
                 attrs.put("EisPrincipal", key);
                 attrs.put("MappedPrincipal", mappingAttrs.get(key));
                 amx.createChild("principal-map", attrs);
             }
         }
     }


     @Handler(id = "saveWorkSecurityMap",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true),
            @HandlerInput(name = "valueMap", type = Map.class, required = true),
            @HandlerInput(name = "valueMap2", type = Map.class, required = true),
            @HandlerInput(name = "usersOptionG", type = String.class),
            @HandlerInput(name = "usersOptionP", type = String.class)})
        public static void saveWorkSecurityMap(HandlerContext handlerCtx) {

            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            Map valueMap = (Map) handlerCtx.getInputValue("valueMap");
            Map valueMap2 = (Map) handlerCtx.getInputValue("valueMap2");
            String usersOptionG = (String) handlerCtx.getInputValue("usersOptionG");
            String usersOptionP = (String) handlerCtx.getInputValue("usersOptionP");
            

            try{
                AttributeList attrList = new AttributeList();
                Attribute rarAttr = new Attribute("ResourceAdapterName", valueMap.get("ResourceAdapterName"));
                String desc = valueMap.get("Description") == null ? null : (String) valueMap.get("Description");
                Attribute descAttr = new Attribute("Description", desc);
                String enable = valueMap.get("Enabled") == null ? "false" : (String) valueMap.get("Enabled");
                Attribute enableAttr = new Attribute("Enabled", enable);
                attrList.add(rarAttr);
                attrList.add(descAttr);
                attrList.add(enableAttr);
                V3AMX.setAttributes( new ObjectName(objectNameStr), attrList);

                AMXConfigProxy workSecurityMap = (AMXConfigProxy)V3AMX.getInstance().getProxy(new ObjectName(objectNameStr));
                Map<String, AMXProxy> children = (Map<String, AMXProxy>)workSecurityMap.childrenMap("group-map");
                if (children.size() > 0){
                    for(String oneChild: children.keySet()){
                        workSecurityMap.removeChild("group-map", oneChild);
                    }
                }

                Map<String, AMXProxy> pchildren = (Map<String, AMXProxy>)workSecurityMap.childrenMap("principal-map");
                if (pchildren.size() > 0){
                    for(String oneChild: pchildren.keySet()){
                        workSecurityMap.removeChild("principal-map", oneChild);
                    }
                }
                setWorkSecurityMapChildren(workSecurityMap, usersOptionG, usersOptionP, valueMap2);
            }catch(Exception ex){
                GuiUtil.prepareException(handlerCtx, ex);
            }
     }


    @Handler(id = "getWorkSecurityMapInfo",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "valueMap2", type = Map.class),
            @HandlerOutput(name = "usersOptionG", type = String.class),
            @HandlerOutput(name = "usersOptionP", type = String.class)
    })
    public static void getWorkSecurityMap(HandlerContext handlerCtx) {
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            Map valueMap2 = new HashMap();
            AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getProxy(new ObjectName(objectNameStr));

            Map<String, AMXProxy> children = (Map<String, AMXProxy>)amx.childrenMap("group-map");
            if (children.size()> 0){
                String gp = "";
                for(AMXProxy oneGroup: children.values()){
                    ObjectName objN = oneGroup.objectName();
                    gp = gp + V3AMX.getAttribute(objN, "EisGroup") + "=" +  V3AMX.getAttribute(objN, "MappedGroup");
                    gp = gp + ",";
                }
                gp = gp.substring(0, gp.length()-1);
                valueMap2.put("GroupMapping", gp);
                handlerCtx.setOutputValue("usersOptionG", "users");
            }else{
                Map<String, AMXProxy> pChildren = (Map<String, AMXProxy>)amx.childrenMap("principal-map");
                if (pChildren.size() > 0){
                    String pM = "";
                    for(AMXProxy oneGroup: pChildren.values()){
                        ObjectName objN = oneGroup.objectName();
                        pM = pM + V3AMX.getAttribute(objN, "EisPrincipal") + "=" + V3AMX.getAttribute(objN, "MappedPrincipal");
                        pM = pM + ",";
                    }
                    pM = pM.substring(0, pM.length()-1);
                    valueMap2.put("PrincipalMapping", pM);
                    handlerCtx.setOutputValue("usersOptionP", "principals");
                }
            }
            handlerCtx.setOutputValue("valueMap2", valueMap2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *	<p> updates the wizard map properties on step 2
     */
    @Handler(id = "getResourceAdapterConfigNewInfo",
        input = {
            @HandlerInput(name = "name", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "valueMap", type = Map.class),
            @HandlerOutput(name = "tableList", type = java.util.List.class)
    })
    public static void getResourceAdapterConfigNewInfo(HandlerContext handlerCtx) {
        String rarName = (String) handlerCtx.getInputValue("name");
        if (GuiUtil.isEmpty(rarName)){
            rarName = "jmsra";
        }
        
        Map valueMap = new HashMap();
        valueMap.put("name", rarName);
        handlerCtx.setOutputValue("valueMap", valueMap);

        Map result = (Map) V3AMX.getInstance().getConnectorRuntime().getResourceAdapterConfigProps(rarName);
        if (result != null) {
            Map<String, String> props = (Map) result.get(RESOURCE_ADAPTER_CONFIG_PROPS_KEY);
            handlerCtx.setOutputValue("tableList", GuiUtil.convertMapToListOfMap(props));
        }else{
            List<Map<String, String>> noprops = new ArrayList<Map<String, String>>();
            handlerCtx.setOutputValue("tableList", noprops);
        }
    }



    /**
     *	<p> If the RAR is an embedded rar, we don't wan to show the .rar extension.
     *  <p> eg. myjca.ear containing myTest.rar  will be shown as myjca.ear#myTest
     */
    @Handler(id = "filterOutRarExtension",
        input = {
            @HandlerInput(name = "inList", type = java.util.List.class, required = true)},
        output = {
            @HandlerOutput(name = "convertedList", type = java.util.List.class)
    })
    public static void filterOutRarExtension(HandlerContext handlerCtx) {
        List<String> inList = (List) handlerCtx.getInputValue("inList");
        List<String> convertedList = new ArrayList();
        for(String one: inList){
            if( (one.indexOf("#") != -1) && one.endsWith(".rar")){
                convertedList.add( one.substring(0, one.length() - 4));
            }else{
                convertedList.add(one);
            }
        }
        handlerCtx.setOutputValue("convertedList", convertedList);
    }



   public static Map stringToMap(String str, String delimiter) {
        Map props = new HashMap();
         if ( str != null && delimiter != null) {
            StringTokenizer tokens = new StringTokenizer(str, delimiter);
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim();
                String values[] = token.split("=");
                if(values.length == 2) {
                    props.put(values[0], values[1]);
                }
            }
        }
        return props;
    }

    public static final String ADMINOBJECT_INTERFACES_KEY = "AdminObjectInterfacesKey";
    public static final String ADMINOBJECT_CLASSES_KEY = "AdminObjectClassesKey";
    public static final String CONNECTION_DEFINITION_NAMES_KEY = "ConnectionDefinitionNamesKey";
    public static final String MCF_CONFIG_PROPS_KEY = "McfConfigPropsKey";
    public static final String SYSTEM_CONNECTORS_KEY = "SystemConnectorsKey";
    public static final String ADMINOBJECT_CONFIG_PROPS_KEY = "AdminObjectConfigPropsKey";
    public static final String RESOURCE_ADAPTER_CONFIG_PROPS_KEY = "ResourceAdapterConfigPropsKey";


}
 
