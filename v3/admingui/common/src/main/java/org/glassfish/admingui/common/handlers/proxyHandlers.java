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

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  


import javax.management.ObjectName;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.intf.config.AMXConfigHelper;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author Anissa Lam
 */
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

        AMXProxy amx = objectNameToProxy(objectNameStr);
        Map<String, AMXProxy> children = amx.childrenMap(childType);
        List result = new ArrayList();
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
        handlerCtx.setOutputValue("result", result);
    }

    private static String getA(Map<String, Object> attrs,  String key){
        String res = "" + attrs.get(key);
        return res;
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
     
    @Handler(id="getProxyAttrs",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

        public static void getProxyAttrs(HandlerContext handlerCtx) {
        try{
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            AMXProxy  amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            handlerCtx.setOutputValue("valueMap", amx.attributesMap());
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("valueMap", new HashMap());
        }
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
//            boolean calltest=false;
//            if (calltest){
//                AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentName));
//
//                String type = "file-cache";
//                System.out.println("type = " + type);
//                System.out.println("objectName = " + parentName);
//                Map mm = amx.getDefaultValues(type, true);
//                System.out.println(mm);
//            }
            String childType = (String) handlerCtx.getInputValue("childType");
            AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentName));
            Map valueMap = amx.getDefaultValues(childType, true);
            handlerCtx.setOutputValue("valueMap", valueMap);
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("valueMap", new HashMap());
        }

    }


    @Handler(id="saveBeanAttributes",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true),
        @HandlerInput(name="attrs",   type=Map.class),
        @HandlerInput(name="skipAttrs",   type=List.class),
        @HandlerInput(name="convertToFalse",   type=List.class)} )
        public static void saveBeanAttributes(HandlerContext handlerCtx) {
        try{
            Map attrs = (Map) handlerCtx.getInputValue("attrs");
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
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
                    if (attrs.keySet().contains(sk)){
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


    //mbean Attribute Name
    private static List httpServiceSkipPropsList = new ArrayList();
    
    static {
        httpServiceSkipPropsList.add("accessLogBufferSize");
        httpServiceSkipPropsList.add("accessLogWriteInterval");
        httpServiceSkipPropsList.add("accessLoggingEnabled");
    }
}
