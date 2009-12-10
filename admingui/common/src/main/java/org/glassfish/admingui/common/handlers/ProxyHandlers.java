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
import java.util.logging.Level;
import javax.management.Attribute;
import java.util.Iterator;

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
import org.glassfish.admin.amx.intf.config.Property;
import org.glassfish.admin.amx.intf.config.Server;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author Anissa Lam
 */
//TODO: Document these handlers
public class ProxyHandlers {

    @Handler(id="getAmxProxy",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true)
        },
        output = {
            @HandlerOutput(name = "result", type = AMXProxy.class)
    })
    public static void getAmxProxy(HandlerContext handlerCtx) {
        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        AMXProxy amx = V3AMX.objectNameToProxy(objectNameStr);
        handlerCtx.setOutputValue("result", amx);
    }
    
    @Handler(id = "getChildrenTable",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true),
            @HandlerInput(name = "childType", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)})
    public static void getChildrenTable(HandlerContext handlerCtx) {
        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        String childType = (String) handlerCtx.getInputValue("childType");
        AMXProxy amx = V3AMX.objectNameToProxy(objectNameStr);
        List result = V3AMX.getChildrenMapForTableList(amx, childType, null);
        handlerCtx.setOutputValue("result", result);
    }

    @Handler(id = "filterTable",
        input = {
            @HandlerInput(name = "table", type = java.util.List.class, required = true),
            @HandlerInput(name = "key", type = java.lang.String.class, required = true),
            @HandlerInput(name = "value", type = java.lang.String.class, required = true),
            @HandlerInput(name = "keep", type = java.lang.Boolean.class, defaultValue="true")
        },
        output = {
            @HandlerOutput(name = "table", type = java.util.List.class)
    })
    public static void filterTable(HandlerContext handlerCtx) {
        List<Map> table = (List) handlerCtx.getInputValue("table");
        String key = (String) handlerCtx.getInputValue("key");
        String value = (String) handlerCtx.getInputValue("value");
        Boolean keep = (Boolean) handlerCtx.getInputValue("keep");
        if ((key == null) || ("".equals(key))) {
            GuiUtil.getLogger().info("'attr' must be non-null, and non-blank");
        }
        if ((value == null) || ("".equals(value))) {
            GuiUtil.getLogger().info("'value' must be non-null, and non-blank");
        }
        if (keep == null) {
            keep = Boolean.TRUE;
        }
        List<Map> results = new ArrayList<Map>();

        // If we're stripping keys we don't want, prep the results table with all of the
        // current values.  Those we don't want will be removed later.
        if (!keep) {
            results.addAll(table);
        }

        // Concurrent acces problems?
        for (Map child : table) {
            if (value.equals(child.get(key))) {
                if (keep) {
                    results.add(child);
                } else {
                    results.remove(child);
                }
            }
        }

        handlerCtx.setOutputValue("table", results);
    }

    private static String getA(Map<String, Object> attrs, String key) {
        Object val = attrs.get(key);
        return (val == null) ? "" : val.toString();
    }

    @Handler(id = "deleteChildren",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true),
            @HandlerInput(name = "type", type = String.class, required = true),
            @HandlerInput(name = "selectedRows", type = List.class, required = true),
            @HandlerInput(name = "key", type = String.class, defaultValue="Name", required = false)})
    public static void deleteChildren(HandlerContext handlerCtx) {
        String type = (String) handlerCtx.getInputValue("type");
        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        String key = (String) handlerCtx.getInputValue("key");
        AMXConfigProxy amx = (AMXConfigProxy) V3AMX.objectNameToProxy(objectNameStr);

        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
        try {
            for (Map oneRow : selectedRows) {
                String Name = (String) oneRow.get(key);
                amx.removeChild(type, Name);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

/*  deleteCascade handles delete for jdbc connection pool and connector connection pool
 *  The dependent resources jdbc resource and connector resource are deleted on deleting
 *  the pools
 *  
 */
  @Handler(id = "deleteCascade",
      input = {
          @HandlerInput(name = "objectNameStr", type = String.class, required = true),
          @HandlerInput(name = "type", type = String.class, required = true),
          @HandlerInput(name = "dependentType", type = String.class, required = true),
          @HandlerInput(name = "selectedRows", type = List.class, required = true)})
    public static void deleteCascade(HandlerContext handlerCtx) {
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            String type = (String) handlerCtx.getInputValue("type");
            String dependentType = (String) handlerCtx.getInputValue("dependentType");
            
            String dependentNameKey = "Name";
            if (type.equals(JDBC_CONNECTION_POOL)) {
                dependentNameKey = "JndiName";
            }
            
            if (dependentType != null) {
                AMXConfigProxy amx = (AMXConfigProxy) V3AMX.objectNameToProxy(objectNameStr);
                List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
                
                for (Map oneRow : selectedRows) {
                    String name = (String) oneRow.get("Name");
                    Map<String, AMXProxy> childrenMap = amx.childrenMap(dependentType);
                    Iterator itr = childrenMap.values().iterator();
                    
                    List dependencies = new ArrayList();
                    while (itr.hasNext()) {
                        AMXProxy obj = (AMXProxy) itr.next();
                        String resourceName = (String) obj.attributesMap().get(dependentNameKey);
                        String poolName = (String) obj.attributesMap().get("PoolName");
                        if (poolName.trim().equals(name.trim())) {
                            dependencies.add(resourceName);
                        }
                    }
                    //Remove dependent resources
                    for (int i = 0; i < dependencies.size(); i++) {
                        AMXConfigProxy refAmx = (AMXConfigProxy) V3AMX.objectNameToProxy("amx:pp=/domain/servers,type=server,name=server");
                        String refType = "resource-ref";
                        String dependentName = (String) dependencies.get(i);
                        amx.removeChild(dependentType, dependentName);
                        refAmx.removeChild(refType, dependentName);
                    } //for - dependency
                    amx.removeChild(type, name);
                } //for - pool
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "deleteChild",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true),
            @HandlerInput(name = "type", type = String.class, required = true),
            @HandlerInput(name = "name", type = String.class)})
    public static void deleteChild(HandlerContext handlerCtx) {
        String type = (String) handlerCtx.getInputValue("type");
        String name = (String) handlerCtx.getInputValue("name");
        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        AMXConfigProxy amx = (AMXConfigProxy) V3AMX.objectNameToProxy(objectNameStr);
        try {
                if (GuiUtil.isEmpty(name)){
                    amx.removeChild(type);
                }else{
                    amx.removeChild(type, name);
                }
       } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    /*  Get the simpleAttributes of the bean based on the objectNameString.
     *  simpleAttributes means those NOT of Element, ie all attributes.
     *  This requires the use of AMXConfigHelper, which thus causes the limitation that
     *  this mbean has to be AMXConfigProxy, not runtiime.
     *  For runtime mbeans, you need to use getRuntimeProxyAttrs.
     */
    @Handler(id = "getProxyAttrs",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "valueMap", type = Map.class)})
    public static void getProxyAttrs(HandlerContext handlerCtx) {
        AMXProxy amx = null;
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            AMXConfigHelper helper = new AMXConfigHelper((AMXConfigProxy) amx);
            final Map<String, Object> attrs = helper.simpleAttributesMap();
            handlerCtx.setOutputValue("valueMap", attrs);
        } catch (Exception ex) {
            if (!(amx instanceof AMXConfigProxy)) {
                getRuntimeProxyAttrs(handlerCtx);
                return;
            }
            ex.printStackTrace();
            handlerCtx.setOutputValue("valueMap", new HashMap());
        }
    }

    @Handler(id = "getRuntimeProxyAttrs",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "valueMap", type = Map.class)})
    public static void getRuntimeProxyAttrs(HandlerContext handlerCtx) {
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            AMXProxy amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            final Map<String, Object> attrs = amx.attributesMap();
            handlerCtx.setOutputValue("valueMap", attrs);
        } catch (Exception ex) {
            ex.printStackTrace();
            handlerCtx.setOutputValue("valueMap", new HashMap());
        }
    }


    /*
     * Get the value of an attribute.  
     * If the attribute is an array, specifying an index will return an element in the array, otherwise, the entire array will be returned.
     */
    @Handler(id = "getProxyAttribute",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true),
            @HandlerInput(name = "attrName", type = String.class, required = true),
            @HandlerInput(name = "index", type = String.class)},
        output = {
            @HandlerOutput(name = "value", type = Object.class)})
    public static void getProxyAttribute(HandlerContext handlerCtx) {
        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        String attrName = (String) handlerCtx.getInputValue("attrName");
        Object result = "";
        try {
            AMXProxy amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            Object val = amx.attributesMap().get(attrName);
            if (val instanceof Object[]) {
                
                String index = (String) handlerCtx.getInputValue("index");
                if (index == null) {
                    result = val;
                } else {
                    Object[] valArray = (Object[])val;
                    if (valArray.length <=0){
                        result = "";
                    }else{
                        Object value = valArray[Integer.parseInt(index)];
                        result = (value == null) ? "" : value.toString();
                    }
                }
            } else {
                result = (val == null) ? "" : val.toString();
            }
        } catch (Exception ex) {
            GuiUtil.getLogger().info("objectName=" + objectNameStr + ", attributeName=" + attrName);
            ex.printStackTrace();
        }
        handlerCtx.setOutputValue("value", result);
    }


    /*
     * Gets the default value of the specified bean.  If an orig Map is passed in, then only those fields in the orig Map that
     * has default value will be updated. This updated map will be the output.  This is so we don't overwrite any field that doesn't
     * have default value.
     * If no orig Map is specified, then the default values that AMX retuns will be the output.
     */
    @Handler(id = "getDefaultProxyAttrs",
        input = {
            @HandlerInput(name = "parentObjectNameStr", type = String.class, required = true),
            @HandlerInput(name = "childType", type = String.class, required = true),
            @HandlerInput(name = "orig", type = Map.class)},
        output = {
            @HandlerOutput(name = "valueMap", type = Map.class)})
    public static void getDefaultProxyAttrs(HandlerContext handlerCtx) {
        try {
            String parentName = (String) handlerCtx.getInputValue("parentObjectNameStr");
            String childType = (String) handlerCtx.getInputValue("childType");
            Map<String, String> orig = (Map) handlerCtx.getInputValue("orig");
            AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentName));
            Map<String, String> defaultMap = amx.getDefaultValues(childType, true);

            if (orig == null) {
                handlerCtx.setOutputValue("valueMap", defaultMap);
                return;
            }
            //we only want to fill in any default value that is available. Preserve all other fields user has entered.
            for (String origKey : orig.keySet()) {
                String defaultV = defaultMap.get(origKey);
                if (defaultV != null) {
                    orig.put(origKey, defaultV);
                }
            }
            handlerCtx.setOutputValue("valueMap", orig);
        } catch (Exception ex) {
            ex.printStackTrace();
            handlerCtx.setOutputValue("valueMap", new HashMap());
        }

    }

    @Handler(id = "proxyExist",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "exist", type = Boolean.class)})
    public static void proxyExist(HandlerContext handlerCtx) {
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            handlerCtx.setOutputValue("exist", doesProxyExist(objectNameStr));
        } catch (Exception ex) {
            ex.printStackTrace();
            handlerCtx.setOutputValue("exist", Boolean.FALSE);
        }
    }

    /*
     * Save the attributes of the proxy.  
     */
    @Handler(id = "saveBeanAttributes",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true),
            @HandlerInput(name = "attrs", type = Map.class),
            @HandlerInput(name = "skipAttrs", type = List.class),
            @HandlerInput(name = "convertToFalse", type = List.class),
            @HandlerInput(name = "onlyUseAttrs", type = List.class)})
    public static void saveBeanAttributes(HandlerContext handlerCtx) {
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");

            if (!doesProxyExist(objectNameStr)) {
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("error.noSuchProxy"));
                    return;
            }

            Map attrs = (Map) handlerCtx.getInputValue("attrs");

            List<String> skipAttrs = (List) handlerCtx.getInputValue("skipAttrs");
            if (skipAttrs != null) {
                for (String sk : skipAttrs) {
                    if (attrs.keySet().contains(sk)) {
                        attrs.remove(sk);
                    }
                }
            }

            List<String> onlyUseAttrs = (List) handlerCtx.getInputValue("onlyUseAttrs");
            if (onlyUseAttrs != null) {
                Map newAttrs = new HashMap();
                for (String key : onlyUseAttrs) {
                    if (attrs.keySet().contains(key)) {
                        newAttrs.put(key, attrs.get(key));
                    }
                }
                attrs = newAttrs;
            }


            List<String> convertToFalse = (List) handlerCtx.getInputValue("convertToFalse");
            if (convertToFalse != null) {
                for (String sk : convertToFalse) {
                    if (attrs.keySet().contains(sk)) {
                        if (attrs.get(sk) == null) {
                            attrs.remove(sk);
                            attrs.put(sk, "false");
                        }
                    }
                }
            }


           V3AMX.setAttributes(objectNameStr, attrs);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "updateStatus",
        input = {
            @HandlerInput(name = "resType", type = String.class, required = true),
            @HandlerInput(name = "enabled", type = String.class),
            @HandlerInput(name = "selectedRows", type = List.class, required = true)})
    public static void updateStatus(HandlerContext handlerCtx) {
        String resType = (String) handlerCtx.getInputValue("resType");
        String status = (String) handlerCtx.getInputValue("enabled");
        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
        Attribute attr = new Attribute("Enabled", status);
        try {
            for (Map oneRow : selectedRows) {
                String name = (String) oneRow.get("Name");
                Set<AMXProxy> resRefSet = V3AMX.getInstance().getDomainRoot().getQueryMgr().queryTypeName("resource-ref", name);
                for(AMXProxy ref : resRefSet){
                    V3AMX.setAttribute(ref.objectName(), attr);
                }

                //Ensure the resource itself is enabled.
                if ("true".equals(status)){
                    String resObjectName = V3AMX.getInstance().getResources().childrenMap(resType).get(name).objectName().toString();
                    if (V3AMX.getAttrsMap(resObjectName).containsKey("Enabled")) {
                        if (! V3AMX.getAttribute(resObjectName, "Enabled").equals("true")){
                            V3AMX.setAttribute(resObjectName, attr);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    @Handler(id = "createProxy",
        input = {
            @HandlerInput(name = "parentObjectNameStr", type = String.class, required = true),
            @HandlerInput(name = "childType", type = String.class, required = true),
            @HandlerInput(name = "attrs", type = Map.class),
            @HandlerInput(name = "skipAttrs", type = List.class),
            @HandlerInput(name = "onlyUseAttrs", type = List.class),
            @HandlerInput(name = "convertToFalse", type = List.class)},
        output = {
            @HandlerOutput(name = "result", type = String.class)})
    public static void createProxy(HandlerContext handlerCtx) {
        
            final String childType = (String) handlerCtx.getInputValue("childType");
            Map<String, Object> attrs = (Map) handlerCtx.getInputValue("attrs");
            if (attrs == null){
                attrs = new HashMap();
            }
            String parentObjectNameStr = (String) handlerCtx.getInputValue("parentObjectNameStr");
        try {
            AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentObjectNameStr));
        
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

            //Should specify either skipAttrs or onlyUseAttrs
            List<String> skipAttrs = (List) handlerCtx.getInputValue("skipAttrs");
            V3AMX.removeSpecifiedAttr(attrs, skipAttrs);

            List<String> onlyUseAttrs = (List) handlerCtx.getInputValue("onlyUseAttrs");
            if (onlyUseAttrs != null) {
                Map newAttrs = new HashMap();
                for (String key : onlyUseAttrs) {
                    if (attrs.keySet().contains(key)) {
                        newAttrs.put(key, attrs.get(key));
                    }
                }
                attrs = newAttrs;
            }

//            System.out.println("========createChild========");
//            System.out.println(amx.toString());
//            System.out.println("childType = " + childType);
//            System.out.println(attrs);

            V3AMX.removeElement(attrs);

            /* If user doesn't fill in anything, we need to remove it, otherwise, it is written out as "" in domain.xml and
             * user will not be able to get the default value.
             * Another reason is for attributes that is optional but is an enum, eg  transactionIsolationLevel in jdbc connection
             * pool, (read-uncommitted|read-committed|repeatable-read|serializable)  pass in "" will result in constraints
             * violation.
             */
            Set<Map.Entry <String, Object>> attrSet = attrs.entrySet();
            Iterator<Map.Entry<String, Object>> iter = attrSet.iterator();
            while (iter.hasNext()){
                 Map.Entry<String, Object> oneEntry = iter.next();
                 Object val = oneEntry.getValue();
                 if ((val != null) && (val instanceof String) && (val.equals(""))){
                    iter.remove();
                }
            }

            AMXConfigProxy child = amx.createChild(childType, attrs);
            handlerCtx.setOutputValue("result", child.objectName().toString());
        } catch (Exception ex) {
            GuiUtil.getLogger().severe("CreateProxy failed.  parent=" + parentObjectNameStr + "; childType=" + childType + "; attrs =" + attrs);
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /*
     * This handler returns a list of children by its name.
     * Useful for creating dropdowns or listBox
     */
    @Handler(id = "getChildrenByType",
        input = {
            @HandlerInput(name = "parentObjectNameStr", type = String.class, required = true),
            @HandlerInput(name = "type", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = List.class)})
    public static void getChildrenByType(HandlerContext handlerCtx) {
        try {
            String type = (String) handlerCtx.getInputValue("type");
            String parentObjectNameStr = (String) handlerCtx.getInputValue("parentObjectNameStr");
            List result = new ArrayList();
            AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentObjectNameStr));
            Map<String, AMXProxy> childrenMap = amx.childrenMap(type);
	    for (String key : childrenMap.keySet()) {
		result.add(com.sun.jsftemplating.util.Util.htmlEscape(key));
	    }
            handlerCtx.setOutputValue("result", result);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /*
     * This handler returns a list of names by type of mbean.
     * Useful for creating dropdowns or listBox
     */
    @Handler(id = "getProxyNamesByType",
        input = {
            @HandlerInput(name = "type", type = String.class, required = true),
            @HandlerInput(name = "end", type = Boolean.class)},
        output = {
            @HandlerOutput(name = "result", type = List.class),
            @HandlerOutput(name = "firstItem", type = String.class)})
    public static void getProxyNamesByType(HandlerContext handlerCtx) {
        try {
            String type = (String) handlerCtx.getInputValue("type");
            Boolean end = (Boolean) handlerCtx.getInputValue("end");
            List result = new ArrayList();
            Query query = V3AMX.getInstance().getDomainRoot().getQueryMgr();
            Set data = (Set) query.queryType(type);
            Iterator iter = data.iterator();
            String firstItem = "";
            String name = "";
            while (iter.hasNext()) {
                Map attr = ((AMXProxy) iter.next()).attributesMap();
                String obj = (String) attr.get("Name");
                if(end){
                    name = obj.substring(obj.lastIndexOf("/")+1, obj.length());

                } else {
                    name = obj.substring(obj.indexOf("/")+1, (obj.lastIndexOf("/") == obj.indexOf("/")) ? obj.length() : obj.lastIndexOf("/"));
                }
                if (GuiUtil.isEmpty(firstItem)) {
                    firstItem = name;
                }
                result.add(name);
            }
            handlerCtx.setOutputValue("result", result);
            handlerCtx.setOutputValue("firstItem", firstItem);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "getApplicationByType",
        input = {
            @HandlerInput(name = "type", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = List.class)})
    public static void getApplicationByType(HandlerContext handlerCtx) {
        String type = (String) handlerCtx.getInputValue("type");
        AMXProxy amx = V3AMX.getInstance().getApplications();
        Map<String, AMXProxy> children = amx.childrenMap("application");
        List result = new ArrayList();
        for (AMXProxy oneChild : children.values()) {
            try {
                AMXConfigHelper helper = new AMXConfigHelper((AMXConfigProxy) oneChild);
                final Map<String, Object> attrs = helper.simpleAttributesMap();
                for (String attrName : attrs.keySet()) {
                    if (attrName.equals("Name")) {
                        String appName = getA(attrs, "Name");
                        Map<String, AMXProxy> module = V3AMX.getInstance().getApplication(appName).childrenMap("module");

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
     *  <p> or just the module name.   eg. myApp#myModule  vs  myModule.  Default to "true"
     *  <p> Input value: "result"  -- Type: <code> java.util.List</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getApplicationBySnifferType",
        input = {
            @HandlerInput(name = "type", type = String.class, required = true),
            @HandlerInput(name = "fullName", type = Boolean.class)},
        output = {
            @HandlerOutput(name = "result", type = List.class)})
    public static void getApplicationBySnifferType(HandlerContext handlerCtx) {
        String type = (String) handlerCtx.getInputValue("type");
        Boolean fullName = (Boolean) handlerCtx.getInputValue("fullName");
        if (fullName == null) {
            fullName = true;
        }

        AMXProxy amx = V3AMX.getInstance().getApplications();
        Map<String, AMXProxy> applications = amx.childrenMap("application");
        List result = new ArrayList();
        eachApp:
        for (AMXProxy oneApp : applications.values()) {
            Map<String, AMXProxy> modules = oneApp.childrenMap("module");
            for (AMXProxy oneModule : modules.values()) {
                Map<String, AMXProxy> engines = oneModule.childrenMap("engine");
                for (AMXProxy oneEngine : engines.values()) {
                    if (oneEngine.getName().equals(type)) {
                        String appName = oneApp.getName();
                        if (fullName) {
                            AMXProxy earSniffer = oneApp.childrenMap("engine").get(SNIFFER_EAR);
                            result.add((earSniffer == null) ? appName : appName + "#" + oneModule.getName());
                            continue;
                        } else {
                            result.add(appName);
                            continue eachApp;
                        }
                    }
                }
            }
        }
        handlerCtx.setOutputValue("result", result);
    }

    /**
     *	<p> This handler saves the property name and value.  Any properyt row that doesn't
     *      have a Name OR Value will be ignored.  For creating a property with empty
     *      value, the value can be specified as '()', then it will be writen out to
     *      domain.xml as  ""
     *  </p>
     *
     *	<p> The following are the inputs are supported:</p>
     *	    <ul><li><b>objectName</b> - (required) This is the objectname for the
     *                 mbean.</li>
     *		<li><b>systemProp</b> - (optional) Boolean. If specified and it is
     *                 equal to TRUE, the property will be saved as system property
     *                 under server.  Otherwise, save as property for that objectname
     *              </li>
     *		<li><b>propertyList</b> - (required) Property list to be saved.</li></ul>
     */
    @Handler(id = "setProxyProperties",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true),
            @HandlerInput(name = "systemProp", type = Boolean.class),
            @HandlerInput(name = "propertyList", type = List.class, required = true)})
    public static void setProxyProperties(HandlerContext handlerCtx) {
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            Boolean systemProp = (Boolean) handlerCtx.getInputValue("systemProp");
            ObjectName objectName = new ObjectName(objectNameStr);
            List<Map<String, String>> propertyList = (List) handlerCtx.getInputValue("propertyList");
            List newList = new ArrayList();
            Set propertyNames = new HashSet();
            final ConfigTools configTools = V3AMX.getInstance().getDomainRoot().getExt().child(ConfigTools.class);
            if (propertyList.size() == 0) {
                if ((systemProp != null) && (systemProp.booleanValue())) {
                    configTools.clearSystemProperties(objectName);
                } else {
                    configTools.clearProperties(objectName);
                }
            } else {
                for (Map<String, String> oneRow : propertyList) {
                    Map newRow = new HashMap();
                    final String name = oneRow.get(PROPERTY_NAME);
                    String value = oneRow.get(PROPERTY_VALUE);
                    if (GuiUtil.isEmpty(name) || GuiUtil.isEmpty(value)) {
                        continue;
                    }

                    if (propertyNames.contains(name)) {
                        GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.duplicatePropTableKey", new Object[]{name}));
                        return;
                    } else {
                        propertyNames.add(name);
                    }
                    if (value.equals(V3AMX.GUI_TOKEN_FOR_EMPTY_PROPERTY_VALUE)){
                        value="";
                    }
                    newRow.put(PROPERTY_NAME, name);
                    newRow.put(PROPERTY_VALUE, value);
                    String desc = (String) oneRow.get(PROPERTY_DESC);
                    if (!GuiUtil.isEmpty(desc)) {
                        newRow.put(PROPERTY_DESC, desc);
                    }
                    newList.add(newRow);
                }
                if ((systemProp != null) && (systemProp.booleanValue())) {
                    configTools.setSystemProperties(objectName, newList, true);
                } else {
                    configTools.setProperties(objectName, newList, true);
                }
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "updateProxyProperties",
        input = {
            @HandlerInput(name = "propertyList", type = java.util.List.class, required = true)},
        output = {
            @HandlerOutput(name = "TableList", type = List.class)})
    public static void updateProxyProperties(HandlerContext handlerCtx) {
        try {
            List<Map<String, String>> propertyList = (List) handlerCtx.getInputValue("propertyList");
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


    /*
     * This handler returns a Map for the properties of the specified object.  Since this is a Map, the key
     * is the property name, and the value is the property value.   The description will not be available.
     */
    @Handler(id = "getProxyProperties",
        input = {
            @HandlerInput(name = "objectNameStr", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = Map.class)})
    public static void getProxyProperties(HandlerContext handlerCtx) {

        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        AMXProxy amx = V3AMX.objectNameToProxy(objectNameStr);
        Map<String, Property> children = amx.childrenMap(Property.class);
        Map result = new HashMap();
        for(Property oneChild : children.values()){
            result.put(oneChild.getName(), oneChild.getValue());
        }
        handlerCtx.setOutputValue("result", result);
    }

    /*
     * This handler converts a Map into a list of Properties.  If useOnly is specified, then only those will be included in the
     * list returned.
     */
    @Handler(id = "propMapToList",
        input = {
            @HandlerInput(name = "propMap", type = Map.class, required = true),
            @HandlerInput(name = "convertToFalse", type = List.class)},
        output = {
            @HandlerOutput(name = "propList", type = List.class)})
    public static void propMapToList(HandlerContext handlerCtx) {
        Map<String, String> propMap = (Map) handlerCtx.getInputValue("propMap");
        List convertToFalse = (List) handlerCtx.getInputValue("convertToFalse");
        if (convertToFalse == null){
            convertToFalse = new ArrayList();
        }
        List result = new ArrayList();

        for(String name: propMap.keySet()){
            String value = propMap.get(name);
            Map newRow = new HashMap();
            newRow.put(PROPERTY_NAME, name);
            if (convertToFalse.contains(name)){
                if (value == null){
                    value="false";
                }
                newRow.put(PROPERTY_VALUE, value);
                result.add(newRow);
            }else{
                if (! GuiUtil.isEmpty(value)){
                    newRow.put(PROPERTY_VALUE, value);
                    result.add(newRow);
                }
            }
        }
        handlerCtx.setOutputValue("propList", result);
    }


    /*
     * This handler takes in a list of rows, there should be 'Enabled' attribute in each row.
     * Get the resource-ref of this resource and do a logical And with this Enabled attribute
     * to get the real status
     */
    @Handler(id = "getResourceRealStatus",
        input = {
            @HandlerInput(name = "rows", type = java.util.List.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = List.class)})
    public static void getResourceRealStatus(HandlerContext handlerCtx) {
        List<Map> rows = (List) handlerCtx.getInputValue("rows");
        for (Map oneRow : rows) {
            String enabled = (String) oneRow.get("Enabled");
            String name = (String) oneRow.get("Name");
            if (enabled == null){
                continue;   //this should never happen.
            }
            Set<AMXProxy> resRefSet = V3AMX.getInstance().getDomainRoot().getQueryMgr().queryTypeName("resource-ref", name);
            for(AMXProxy ref : resRefSet){
                String refStatus = (String)ref.attributesMap().get("Enabled");
                if (refStatus.equals("true")){
                    oneRow.put("Enabled", enabled);   //depend on the resource itself.
                }else{
                    oneRow.put("Enabled", false);
                }
            }
        }
        handlerCtx.setOutputValue("result", rows);
    }




    @Handler(id = "createResourceRef",
        input = {
            @HandlerInput(name = "resourceName", type = String.class, required = true),
            @HandlerInput(name = "enabled", type=String.class)
        })
    public static void createResourceRef(HandlerContext handlerCtx) {
        String resourceName = (String) handlerCtx.getInputValue("resourceName");
        String enabled = (String) handlerCtx.getInputValue("enabled");
        if (enabled == null) {
            enabled = "true";
        }

        try {
            Server server = V3AMX.getInstance().getServer("server");
            Map<String, Object> attrs = new HashMap<String, Object>();
            attrs.put("Name", resourceName);
            attrs.put("Enabled", enabled);
            server.createChild("resource-ref", attrs);
        } catch (Exception ex) {
            GuiUtil.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public static Map getDefaultProxyAttrsMap(String parentObjectNameStr, String childType) {
        try {
            String parentName = parentObjectNameStr;
            String child = childType;
            AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentName));
            Map valueMap = amx.getDefaultValues(child, true);
            return valueMap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new HashMap();
        }
    }

    
    public static boolean doesProxyExist(String objectNameStr) {
        try {
            final ObjectName objName = new ObjectName(objectNameStr);
            return V3AMX.getInstance().getMbeanServerConnection().isRegistered(objName);
        } catch (Exception ex) {
            //ex.printStackTrace();
            return false;
        }
    }



    /**
     *
     */
    @Handler(id = "getAmxRoot",
        output = {
            @HandlerOutput(name = "amxRoot", type = V3AMX.class)
    })
    public static void getAmxRootInstance(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("amxRoot", V3AMX.getInstance());
    }
    private static final String SNIFFER_EAR = "ear";
    //mbean Attribute Name
    public static final String PROPERTY_NAME = "Name";
    public static final String PROPERTY_VALUE = "Value";
    public static final String PROPERTY_DESC = "Description";

    //TODO
    //Resources - can this obtained from AMX?
    public static final String JDBC_CONNECTION_POOL = "jdbc-connection-pool";
    public static final String CONNECTOR_CONNECTION_POOL = "connector-connection-pool";

}
