
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
 * GmsHandler.java
 *
 * Created on January 10, 2006, 11:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author anilam
 */

package com.sun.enterprise.tools.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.TargetUtil;

import com.sun.appserv.management.config.GroupManagementServiceConfig;
import com.sun.appserv.management.config.ConfigConfig;

public class GmsHandler {
    /** Creates a new instance of JdbcHandler */
    public GmsHandler() {
    }
    
   /**
     *	<p> This handler returns the values for all the attributes of GMS
     */
    @Handler(id="getGmsInfo",
        input={
            @HandlerInput(name="configName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="fdMax", type=String.class),
            @HandlerOutput(name="fdTimeout", type=String.class),
            @HandlerOutput(name="mergeMax", type=String.class),
            @HandlerOutput(name="mergeMin", type=String.class),
            @HandlerOutput(name="pingTimeout", type=String.class),
            @HandlerOutput(name="vsTimeout", type=String.class),
            @HandlerOutput(name="Properties",  type=Map.class)}
    )
    public static void getGmsInfo(HandlerContext handlerCtx) {
        
        GroupManagementServiceConfig gms = null;
        String configName = (String) handlerCtx.getInputValue("configName");
        ConfigConfig config = AMXUtil.getDomainConfig().getConfigConfigMap().get(configName);
        if (config != null){
            gms = config.getGroupManagementServiceConfig();
        }
        if (gms == null){
            handlerCtx.setOutputValue("Properties", new HashMap());
            return;
        }
        handlerCtx.setOutputValue("fdMax", gms.getFDProtocolMaxTries());
        handlerCtx.setOutputValue("fdTimeout", gms.getFDProtocolTimeoutMillis());
        handlerCtx.setOutputValue("mergeMax", gms.getMergeProtocolMaxIntervalMillis());
        handlerCtx.setOutputValue("mergeMin", gms.getMergeProtocolMinIntervalMillis());
        handlerCtx.setOutputValue("pingTimeout", gms.getPingProtocolTimeoutMillis());
        handlerCtx.setOutputValue("vsTimeout", gms.getVSProtocolTimeoutMillis());
        handlerCtx.setOutputValue("Properties", gms.getProperties());
    }
   
    
    /**
     *	<p> This handler saves the attributes of GMS
     */
    @Handler(id="saveGmsSettings",
        input={
            @HandlerInput(name="configName", type=String.class, required=true),
            @HandlerInput(name="fdMax", type=String.class),
            @HandlerInput(name="fdTimeout", type=String.class),
            @HandlerInput(name="mergeMax", type=String.class),
            @HandlerInput(name="mergeMin", type=String.class),
            @HandlerInput(name="pingTimeout", type=String.class),
            @HandlerInput(name="vsTimeout", type=String.class),
            @HandlerInput(name="AddProps",    type=Map.class),
            @HandlerInput(name="RemoveProps", type=ArrayList.class)
        })
    public static void saveGmsSettings(HandlerContext handlerCtx) {
        GroupManagementServiceConfig gms = null;
        String configName = (String) handlerCtx.getInputValue("configName");
        try{
            ConfigConfig config = AMXUtil.getDomainConfig().getConfigConfigMap().get(configName);
            if (config != null){
                gms = config.getGroupManagementServiceConfig();
            }
            if (gms == null){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoAMXGmsSupport"));
                return;
            }
            gms.setFDProtocolMaxTries(getAttrString(handlerCtx, "fdMax"));
            gms.setFDProtocolTimeoutMillis(getAttrString(handlerCtx, "fdTimeout"));
            gms.setMergeProtocolMaxIntervalMillis(getAttrString(handlerCtx, "mergeMax"));
            gms.setMergeProtocolMinIntervalMillis(getAttrString(handlerCtx, "mergeMin"));
            gms.setPingProtocolTimeoutMillis(getAttrString(handlerCtx, "pingTimeout"));
            gms.setVSProtocolTimeoutMillis(getAttrString(handlerCtx, "vsTimeout"));
            AMXUtil.editProperties(handlerCtx, gms);
            
        }catch (Exception ex){
	    GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    private static String getAttrString(HandlerContext handlerCtx, String nm){
        String value = (String) handlerCtx.getInputValue(nm);
        return (value == null) ? "" : value;
    }
    
     
    /**
     * <p> This handler returns the default values for the advance attributes of GMS
     */
    @Handler(id="getGmsDefaultSettings",
    output={
        @HandlerOutput(name="fdMax", type=String.class),
        @HandlerOutput(name="fdTimeout", type=String.class),
        @HandlerOutput(name="mergeMax", type=String.class),
        @HandlerOutput(name="mergeMin", type=String.class),
        @HandlerOutput(name="pingTimeout", type=String.class),
        @HandlerOutput(name="vsTimeout", type=String.class)}
    )
    public static void getGmsDefaultSettings(HandlerContext handlerCtx) {

        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(GroupManagementServiceConfig.J2EE_TYPE);
        handlerCtx.setOutputValue("fdMax", (String) defaultMap.get("fd-protocol-max-tries"));
        handlerCtx.setOutputValue("fdTimeout", (String) defaultMap.get("fd-protocol-timeout-in-millis"));
        handlerCtx.setOutputValue("mergeMax", (String) defaultMap.get("merge-protocol-max-interval-in-millis"));
        handlerCtx.setOutputValue("mergeMin", (String) defaultMap.get("merge-protocol-min-interval-in-millis"));
        handlerCtx.setOutputValue("pingTimeout", (String) defaultMap.get("ping-protocol-timeout-in-millis"));
        handlerCtx.setOutputValue("vsTimeout", (String) defaultMap.get("vs-protocol-timeout-in-millis"));
    }
}
        
 
