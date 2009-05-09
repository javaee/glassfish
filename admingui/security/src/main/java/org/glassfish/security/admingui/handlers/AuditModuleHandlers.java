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

package org.glassfish.security.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  
import com.sun.appserv.management.config.AuditModuleConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.PropertiesAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;
import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author anilam
 */
public class AuditModuleHandlers {
    
    
        /**
     *	<p> This handler returns the list of specified config elements for populating the table.
     *  <p> Input  value: "type" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getAuditModuleList",
        input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getAuditModuleList(HandlerContext handlerCtx){
        
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        Map<String, AuditModuleConfig> aMap = config.getSecurityServiceConfig().getAuditModuleConfigMap();
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        List result = new ArrayList();
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
        for (AuditModuleConfig  auditModule : aMap.values()){
                HashMap oneRow = new HashMap();
                String name=auditModule.getName();                
                oneRow.put("name", name);
                oneRow.put("selected", (hasOrig)? GuiUtil.isSelected(name, selectedList): false);
                String classname = auditModule.getClassname();
                oneRow.put("classname", (classname == null) ? " ": classname);
                String auditOn = AMXUtil.getPropertyValue(auditModule, "auditOn");
                if (GuiUtil.isEmpty(auditOn))
                    oneRow.put("auditOn", "false");
                else
                    oneRow.put("auditOn", auditOn);
                            
                result.add(oneRow);
            }
        handlerCtx.setOutputValue("result", result);
    }    

    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Audit Modules Page </p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getAuditModuleSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true) },        
    output={
        @HandlerOutput(name="Classname",   type=String.class),
        @HandlerOutput(name="auditOn",      type=Boolean.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
    public static void getAuditModuleSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if (!edit)
                return;
            Map<String,AuditModuleConfig>auditModules = config.getSecurityServiceConfig().getAuditModuleConfigMap();
            AuditModuleConfig module = (AuditModuleConfig)auditModules.get((String)handlerCtx.getInputValue("Name"));
            String classname = module.getClassname();
            
            handlerCtx.setOutputValue("Classname", classname);
            Map aMap = AMXUtil.getNonSkipPropertiesMap(module, auditSkipPropsList);
            handlerCtx.setOutputValue("Properties",  aMap);
            Boolean auditOnFlag = false;
            String auditOn = AMXUtil.getPropertyValue(module, "auditOn");
            if (! GuiUtil.isEmpty(auditOn))
                auditOnFlag = Boolean.valueOf(auditOn);
            handlerCtx.setOutputValue("auditOn", auditOnFlag);
            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }  
        
    /**
     *	<p> This handler saves the values for all the attributes in 
     *      Audit Module Page </p>
     */
    @Handler(id="saveAuditModuleSettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Classname",  type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true),
        @HandlerInput(name="auditOn",     type=Boolean.class),
        @HandlerInput(name="newProps", type=Map.class)
        })
        
    public static void saveAuditModuleSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        Map newProps = (Map)handlerCtx.getInputValue("newProps");
        Boolean auditOnB = (Boolean)handlerCtx.getInputValue("auditOn");
        boolean auditOn = (auditOnB == null) ? false : auditOnB.booleanValue();
        
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map convertedMap = AMXUtil.convertToPropertiesOptionMap(newProps, null);
                convertedMap.put( PropertiesAccess.PROPERTY_PREFIX + "auditOn", "" + auditOn );
                config.getSecurityServiceConfig().createAuditModuleConfig(
                        (String)handlerCtx.getInputValue("Name"),
                        (String)handlerCtx.getInputValue("Classname"),
                        convertedMap);
                return;
            }
            Map<String,AuditModuleConfig>modules = config.getSecurityServiceConfig().getAuditModuleConfigMap();
            AuditModuleConfig module = (AuditModuleConfig)modules.get((String)handlerCtx.getInputValue("Name"));
            module.setClassname((String) handlerCtx.getInputValue("Classname"));
            AMXUtil.updateProperties(module, newProps, auditSkipPropsList);
            AMXUtil.setPropertyValue(module, "auditOn", Boolean.toString(auditOn));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }   

    private static List auditSkipPropsList = new ArrayList();

     static {
        auditSkipPropsList.add("auditOn");
    }

}
