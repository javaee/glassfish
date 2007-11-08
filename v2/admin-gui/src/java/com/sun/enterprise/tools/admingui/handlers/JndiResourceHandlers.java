
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
 * JndiResourceHandlers.java
 *
 * Created on August 30, 2006, 10:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author anilam
 */

package com.sun.enterprise.tools.admingui.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.TargetUtil;

import com.sun.appserv.management.config.JNDIResourceConfig;
import com.sun.appserv.management.config.AdminObjectResourceConfig;
import com.sun.appserv.management.config.JDBCResourceConfig;
import com.sun.appserv.management.config.ConnectorResourceConfig;
import com.sun.appserv.management.config.CustomResourceConfig;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.config.Enabled;

public class JndiResourceHandlers {
    /** Creates a new instance of JndiResourceHandlers */
    public JndiResourceHandlers() {
    }
    
    /**
     *	<p> This handler returns the values for all the attributes of the JNDI Resource or Custom Resource
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJndiResourceInfo",
        input={
            @HandlerInput(name="jndiName", type=String.class, required=true),
            @HandlerInput(name="type", type=String.class, required=true),
            @HandlerInput(name="edit", type=Boolean.class, required=true)},
        output={
            @HandlerOutput(name="resType", type=String.class),
            @HandlerOutput(name="factoryClass", type=String.class),
            @HandlerOutput(name="jndiLookupName", type=String.class),
            @HandlerOutput(name="description", type=String.class),
            @HandlerOutput(name="enabledString", type=String.class),
            @HandlerOutput(name="enabled", type=Boolean.class),
            @HandlerOutput(name="Properties", type=Map.class)
	} )
    public static void getJndiResourceInfo(HandlerContext handlerCtx) {
        
            if (!(Boolean) handlerCtx.getInputValue("edit")){
                handlerCtx.setOutputValue("enabled", Boolean.TRUE);
                handlerCtx.setOutputValue("Properties", new HashMap());
                return;
            }
	    String jndiName = (String) handlerCtx.getInputValue("jndiName");
	    String type = (String) handlerCtx.getInputValue("type");
	    String resType, factoryClass, description;
	    if (type.equals("custom")){
		CustomResourceConfig customRes = AMXUtil.getDomainConfig().getCustomResourceConfigMap().get(jndiName);
		if (customRes == null) {
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchCustomResource"));
		    return;
		}
		resType = customRes.getResType();
		factoryClass = customRes.getFactoryClass();
		description = customRes.getDescription();
                if(AMXUtil.isEE())
                    handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(customRes, false));
                else
                    handlerCtx.setOutputValue("enabled", TargetUtil.isResourceEnabled(customRes, "server" ));
                Map<String, String> props = customRes.getProperties();
                handlerCtx.setOutputValue("Properties", props);
	    } else{
		JNDIResourceConfig jndiRes = AMXUtil.getDomainConfig().getJNDIResourceConfigMap().get(jndiName);
		if (jndiRes == null) {
		    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchExternalResource"));
		    return;
		}
		resType = jndiRes.getResType();
		factoryClass = jndiRes.getFactoryClass();
		description = jndiRes.getDescription();
		handlerCtx.setOutputValue("jndiLookupName", jndiRes.getJNDILookupName());
                if(AMXUtil.isEE())
                    handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(jndiRes, false));
                else
                    handlerCtx.setOutputValue("enabled", TargetUtil.isResourceEnabled(jndiRes, "server" ));
                Map<String, String> props = jndiRes.getProperties();
                handlerCtx.setOutputValue("Properties", props);
	    }

	handlerCtx.setOutputValue("resType", resType);
        handlerCtx.setOutputValue("factoryClass", factoryClass);
        handlerCtx.setOutputValue("description", description);
    }
    
    /**
     *	<p> This handler saves the values for all the attributes of the JNDI Resource or Custom Resource
     */

    @Handler(id="saveJndiResource",
        input={
            @HandlerInput(name="edit", type=Boolean.class, required=true),
            @HandlerInput(name="jndiName", type=String.class, required=true),
            @HandlerInput(name="type", type=String.class, required=true),
            @HandlerInput(name="resType", type=String.class, required=true),
            @HandlerInput(name="factoryClass", type=String.class, required=true),
            @HandlerInput(name="jndiLookupName", type=String.class, required=true),
            @HandlerInput(name="description", type=String.class),
            @HandlerInput(name="enabled", type=Boolean.class),
            @HandlerInput(name="AddProps",    type=Map.class),
            @HandlerInput(name="RemoveProps", type=ArrayList.class),
            @HandlerInput(name="targets", type=String[].class )
        })
        public static void saveJndiResource(HandlerContext handlerCtx) {
            try{
                Boolean edit = (Boolean) handlerCtx.getInputValue("edit");
                if (!edit){
                    createJndiResource(handlerCtx);
                    return;
                }
                String jndiName = (String) handlerCtx.getInputValue("jndiName");
                String type = (String) handlerCtx.getInputValue("type");
                ResourceConfig resource = null;
                if (type.equals("custom")){
                    CustomResourceConfig custom;
                    resource = custom = AMXUtil.getDomainConfig().getCustomResourceConfigMap().get(jndiName);
                    if (resource == null){
                        GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchResource"));
                        return;
                    }
                    custom.setResType((String)handlerCtx.getInputValue("resType"));
                    custom.setFactoryClass((String)handlerCtx.getInputValue("factoryClass"));
                }else{
                    JNDIResourceConfig jndi = null;
                    resource = jndi = AMXUtil.getDomainConfig().getJNDIResourceConfigMap().get(jndiName);
                    if (resource == null){
                        GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchResource"));
                        return;
                    }
                    jndi.setJNDILookupName((String)handlerCtx.getInputValue("jndiLookupName"));
                    jndi.setResType((String)handlerCtx.getInputValue("resType"));
                    jndi.setFactoryClass((String)handlerCtx.getInputValue("factoryClass"));
                }
                resource.setDescription((String)handlerCtx.getInputValue("description"));
                AMXUtil.editProperties(handlerCtx, resource);
                if(! AMXUtil.isEE()){
                    Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
                    TargetUtil.setResourceEnabled(resource, "server", enabled); 
                }
                GuiUtil.prepareSuccessful(handlerCtx);
            }catch (Exception ex){
                 GuiUtil.handleException(handlerCtx, ex);
        }
    }
   
    private static void createJndiResource(HandlerContext handlerCtx){
        try{
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            String type = (String) handlerCtx.getInputValue("type");
            String resType = (String) handlerCtx.getInputValue("resType");
            String factoryClass = (String) handlerCtx.getInputValue("factoryClass");
            ResourceConfig resource = null;
            Map optionalMap = AMXUtil.convertToPropertiesOptionMap((Map)handlerCtx.getInputValue("AddProps"), null); 
            if (type.equals("custom")){
                resource = AMXUtil.getDomainConfig().createCustomResourceConfig(jndiName, resType, factoryClass, optionalMap);
            }else{
                resource = AMXUtil.getDomainConfig().createJNDIResourceConfig(jndiName, (String)handlerCtx.getInputValue("jndiLookupName"), resType, factoryClass, optionalMap);
            }
            JavaMailHandlers.createNewTargets(handlerCtx, jndiName);
            resource.setDescription((String)handlerCtx.getInputValue("description"));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
   
  
}
        
 
