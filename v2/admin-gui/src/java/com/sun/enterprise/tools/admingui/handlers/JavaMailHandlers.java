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
 * JavaMailHandlers.java
 *
 * Created on August 28, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author anilam
 */

package com.sun.enterprise.tools.admingui.handlers;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.TargetUtil;
import com.sun.appserv.management.config.MailResourceConfig;
import com.sun.appserv.management.config.MailResourceConfigKeys;
import com.sun.appserv.management.config.Enabled;

public class JavaMailHandlers {
    /** Creates a new instance of JavaMailHandlers */
    public JavaMailHandlers() {
    }
    
    /**
     *	<p> This handler returns the values for all the attributes of the JavaMail Resource
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJavaMailInfo",
        input={
            @HandlerInput(name="jndiName", type=String.class, required=true),
            @HandlerInput(name="edit", type=Boolean.class, required=true)},
        output={
            @HandlerOutput(name="host", type=String.class),
            @HandlerOutput(name="user", type=String.class),
            @HandlerOutput(name="from", type=String.class),
            @HandlerOutput(name="description", type=String.class),
            @HandlerOutput(name="enabledString", type=String.class),
            @HandlerOutput(name="enabled", type=Boolean.class),
            @HandlerOutput(name="storeProtocol", type=String.class),
            @HandlerOutput(name="storeProtocolClass", type=String.class),
            @HandlerOutput(name="transportProtocol", type=String.class),
            @HandlerOutput(name="transportProtocolClass", type=String.class),
            @HandlerOutput(name="debug", type=Boolean.class),
            @HandlerOutput(name="Properties", type=Map.class)
	} )
    public static void getJavaMailInfo(HandlerContext handlerCtx) {
        
        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        Boolean edit = (Boolean) handlerCtx.getInputValue("edit");
        if(!edit){
            getJavaMailDefaults(handlerCtx);
            handlerCtx.setOutputValue("Properties", new HashMap());
            return;
        }

        MailResourceConfig mailResource = AMXUtil.getDomainConfig().getMailResourceConfigMap().get(jndiName);
        if (mailResource == null){
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchMailResource"));
            return;
        }

	if(AMXUtil.isEE())
            handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(mailResource, false));
        else
            handlerCtx.setOutputValue("enabled", TargetUtil.isResourceEnabled(mailResource, "server" ));
        handlerCtx.setOutputValue("host", mailResource.getHost());
        handlerCtx.setOutputValue("user", mailResource.getUser());
	handlerCtx.setOutputValue("from", mailResource.getFrom());
        handlerCtx.setOutputValue("description", mailResource.getDescription());
	handlerCtx.setOutputValue("storeProtocol", mailResource.getStoreProtocol());
	handlerCtx.setOutputValue("storeProtocolClass", mailResource.getStoreProtocolClass());
	handlerCtx.setOutputValue("transportProtocol", mailResource.getTransportProtocol());
	handlerCtx.setOutputValue("transportProtocolClass", mailResource.getTransportProtocolClass());
	handlerCtx.setOutputValue("debug", mailResource.getDebug());
        
        Map<String, String> props = mailResource.getProperties();
        handlerCtx.setOutputValue("Properties", props);
    }
   
    
    /**
     *	<p> This handler saves the values for all the attributes of the Jdbc Connection Pool
     */

    @Handler(id="saveJavaMail",
        input={
            @HandlerInput(name="jndiName", type=String.class, required=true),
            @HandlerInput(name="edit", type=Boolean.class, required=true),
            @HandlerInput(name="host", type=String.class, required=true),
            @HandlerInput(name="user", type=String.class, required=true),
            @HandlerInput(name="from", type=String.class, required=true),
            @HandlerInput(name="description", type=String.class),
            @HandlerInput(name="enabled", type=Boolean.class),
            @HandlerInput(name="storeProtocol", type=String.class),
            @HandlerInput(name="storeProtocolClass", type=String.class),
            @HandlerInput(name="transportProtocol", type=String.class),
            @HandlerInput(name="transportProtocolClass", type=String.class),
            @HandlerInput(name="debug", type=Boolean.class),
            @HandlerInput(name="AddProps",    type=Map.class),
            @HandlerInput(name="RemoveProps", type=ArrayList.class),
            @HandlerInput(name="targets", type=String[].class )
        })
        public static void saveJavaMail(HandlerContext handlerCtx) {
        
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("edit");
            if(!edit){
                createJavaMail(handlerCtx);
                return;
            }
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            MailResourceConfig mailResource = AMXUtil.getDomainConfig().getMailResourceConfigMap().get(jndiName);
            if (mailResource == null){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchMailResource"));
                return;
            }
            mailResource.setHost((String)handlerCtx.getInputValue("host"));
            mailResource.setUser((String)handlerCtx.getInputValue("user"));
            mailResource.setFrom((String)handlerCtx.getInputValue("from"));
            mailResource.setDescription((String)handlerCtx.getInputValue("description"));
            if(! AMXUtil.isEE()){
                Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
                TargetUtil.setResourceEnabled(mailResource, "server", enabled); 
            }
            mailResource.setStoreProtocol((String)handlerCtx.getInputValue("storeProtocol"));
            mailResource.setStoreProtocolClass((String)handlerCtx.getInputValue("storeProtocolClass"));
            mailResource.setTransportProtocol((String)handlerCtx.getInputValue("transportProtocol"));
            mailResource.setTransportProtocolClass((String)handlerCtx.getInputValue("transportProtocolClass"));
            mailResource.setDebug((Boolean)handlerCtx.getInputValue("debug"));
            AMXUtil.editProperties(handlerCtx, mailResource);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    
    /**
     *	<p> This handler returns the values for all the attributes of the Jdbc Connection Pool
     */
    @Handler(id="getJavaMailDefaultInfo",
        input={
            @HandlerInput(name="jndiName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="storeProtocol", type=String.class),
            @HandlerOutput(name="storeProtocolClass", type=String.class),
            @HandlerOutput(name="transportProtocol", type=String.class),
            @HandlerOutput(name="transportProtocolClass", type=String.class),
            @HandlerOutput(name="enabled", type=Boolean.class),
            @HandlerOutput(name="debug", type=Boolean.class)}
            )
        public static void getJavaMailDefaultInfo(HandlerContext handlerCtx) {
            getJavaMailDefaults(handlerCtx);
        }
    
    
    private static void getJavaMailDefaults(HandlerContext handlerCtx){
        Map<String, String> attrMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(MailResourceConfig.J2EE_TYPE);
        handlerCtx.setOutputValue("enabled", Boolean.valueOf(attrMap.get("enabled")));
        handlerCtx.setOutputValue("debug", Boolean.valueOf(attrMap.get("debug")));
        handlerCtx.setOutputValue("storeProtocol", attrMap.get("store-protocol"));
        handlerCtx.setOutputValue("storeProtocolClass", attrMap.get("store-protocol-class"));
        handlerCtx.setOutputValue("transportProtocol", attrMap.get("transport-protocol"));
        handlerCtx.setOutputValue("transportProtocolClass", attrMap.get("transport-protocol-class"));
    }
    
    private static void createJavaMail(HandlerContext handlerCtx) {
        
        try{
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            String host = (String) handlerCtx.getInputValue("host");
            String user = (String) handlerCtx.getInputValue("user");
            String from = (String) handlerCtx.getInputValue("from");
            Map<String, String> options = new HashMap();

            addOption(options, handlerCtx, MailResourceConfigKeys.STORE_PROTOCOL_KEY, "storeProtocol");
            addOption(options, handlerCtx, MailResourceConfigKeys.STORE_PROTOCOL_CLASS_KEY, "storeProtocolClass");
            addOption(options, handlerCtx, MailResourceConfigKeys.TRANSPORT_PROTOCOL_KEY, "transportProtocol");
            addOption(options, handlerCtx, MailResourceConfigKeys.TRANSPORT_PROTOCOL_CLASS_KEY, "transportProtocolClass");

            Boolean debug = (Boolean) handlerCtx.getInputValue("debug");
            if (debug != null){
                options.put(MailResourceConfigKeys.DEBUG_KEY, debug.toString());
            }
            Map addProps = (Map)handlerCtx.getInputValue("AddProps");
            if (addProps != null)
                options = AMXUtil.convertToPropertiesOptionMap(addProps, options);
            if (options.isEmpty()) options = null;
            MailResourceConfig resource = AMXUtil.getDomainConfig().createMailResourceConfig(jndiName, host, user, from, options);
            resource.setDescription((String)handlerCtx.getInputValue("description"));
            createNewTargets(handlerCtx, jndiName);
            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    private static void addOption(Map options, HandlerContext handlerCtx, String key, String param){
        
        String value = (String) handlerCtx.getInputValue(param);
        if (! GuiUtil.isEmpty(value)){
            options.put(key, value);
        }
    }
    
    public static void createNewTargets(HandlerContext handlerCtx, String name) {
        Boolean enabled = (Boolean)handlerCtx.getInputValue("enabled");
        String[] selTargets = new String[] {"server"};
        if (AMXUtil.isEE())
            selTargets = (String[])handlerCtx.getInputValue("targets");
        List<String> targets = Arrays.asList(selTargets);
        for(String targetName: targets) {
            TargetUtil.createResourceRef(name, targetName, enabled);
        }
    }
}
        
 
