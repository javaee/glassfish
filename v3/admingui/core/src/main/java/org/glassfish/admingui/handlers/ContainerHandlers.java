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
 * ContainerHandlers.java
 *
 * Created on September 8, 2006, 12:47 AM
 *
 */

package org.glassfish.admingui.handlers;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ManagerPropertiesConfig;
import com.sun.appserv.management.config.SessionConfig;
import com.sun.appserv.management.config.SessionManagerConfig;
import com.sun.appserv.management.config.SessionPropertiesConfig;
import com.sun.appserv.management.config.StorePropertiesConfig;
import com.sun.appserv.management.config.WebContainerConfig;
import org.glassfish.admingui.util.AMXRoot;
import org.glassfish.admingui.util.GuiUtil;
import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Nitya Doraisamy
 * @author Anissa Lam
 */
public class ContainerHandlers {
 
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Web Container - General Settings page </p>
     *	<p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebContainerGeneralProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },    
    output={
        @HandlerOutput(name="Properties",      type=Map.class)})
        
        public static void getWebContainerGeneralProps(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        WebContainerConfig webConfig = config.getWebContainerConfig();
        Map<String, String> props = webConfig.getProperties();
        handlerCtx.setOutputValue("Properties", props);
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Web Container - Session Props </p>
     *	<p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "SessionTimeout"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebSessionProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },    
    output={
        @HandlerOutput(name="SessionTimeout",       type=String.class),
        @HandlerOutput(name="Properties",           type=Map.class)})
        
        public static void getWebSessionProps(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();  
        Map<String, String> props = new HashMap();
        if(sessionConfig != null){
            SessionPropertiesConfig ssPropConfig = sessionConfig.getSessionPropertiesConfig();
            if(ssPropConfig != null){
                String sessTimeout =ssPropConfig.getTimeoutInSeconds();
                handlerCtx.setOutputValue("SessionTimeout", sessTimeout);
                props = ssPropConfig.getProperties();
            }
        }
        handlerCtx.setOutputValue("Properties", props);
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Web Container - General Settings page </p>
     *	<p> Input value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"           -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"        -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveWebContainerGeneralProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),    
        @HandlerInput(name="AddProps",          type=Map.class),
        @HandlerInput(name="RemoveProps",       type=ArrayList.class)})
        
        public static void saveWebContainerGeneralProps(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        WebContainerConfig webConfig = config.getWebContainerConfig();
        AMXRoot.getInstance().editProperties(handlerCtx, webConfig);
    }
    
    /**
     *	<p> This handler sets the values for the attributes in 
     *      Web Container - Session Props </p>
     *	<p> Input value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "SessionTimeout"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"           -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"        -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveWebSessionProps",
    input={
        @HandlerInput(name="ConfigName",        type=String.class, required=true),    
        @HandlerInput(name="SessionTimeout",    type=String.class),
        @HandlerInput(name="AddProps",          type=Map.class),
        @HandlerInput(name="RemoveProps",       type=ArrayList.class)})
        
        public static void saveWebSessionValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();
        try{
            String sessTimeout = (String)handlerCtx.getInputValue("SessionTimeout");
            if((sessionConfig != null) && (sessionConfig.getSessionPropertiesConfig() != null)) {
                sessionConfig.getSessionPropertiesConfig().setTimeoutInSeconds(sessTimeout);
            }else {
                /** In V2
                String objName = "com.sun.appserv:type=configs,category=config";
                String opername = "createSessionProperties";
                String[] signature = {"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};

                AttributeList attrList = new AttributeList();
                attrList.add(new Attribute("timeout-in-seconds", sessTimeout));
                Properties props = new Properties();
                Object[] params = {attrList, props, configName};
                JMXUtil.invoke(objName, opername, params, signature);
                 */
                sessionConfig = config.getWebContainerConfig().getSessionConfig();           
                if(sessionConfig == null)
                    sessionConfig = config.getWebContainerConfig().createSessionConfig();
                Map props = new HashMap();
                props.put("TimeoutInSeconds", sessTimeout);
                SessionPropertiesConfig propConfig = sessionConfig.createSessionPropertiesConfig(props);
                propConfig.setTimeoutInSeconds(sessTimeout);
            }    
            AMXRoot.getInstance().editProperties(handlerCtx, sessionConfig.getSessionPropertiesConfig());
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Web Container - Manager Props </p>
     *	<p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "ReapInterval"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "MaxSessions"        -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "SessFileName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "SessionIdGen"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebManagerProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },    
    output={
        @HandlerOutput(name="ReapInterval",       type=String.class),
        @HandlerOutput(name="MaxSessions",        type=String.class),
        @HandlerOutput(name="SessFileName",       type=String.class),
        @HandlerOutput(name="SessionIdGen",       type=String.class),
        @HandlerOutput(name="Properties",         type=Map.class)})
        
        public static void getWebManagerProps(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();
        Map<String, String> props = new HashMap();
        if(sessionConfig != null){
            SessionManagerConfig sessMgrConfig = sessionConfig.getSessionManagerConfig();
            if(sessMgrConfig != null){
                ManagerPropertiesConfig mgrPropConfig = sessMgrConfig.getManagerPropertiesConfig();
                if(mgrPropConfig != null){
                    String reapInterval = mgrPropConfig.getReapIntervalInSeconds();
                    String maxSessions = mgrPropConfig.getMaxSessions();
                    String sessFileName = mgrPropConfig.getSessionFileName();
                    String sessionIdGen = mgrPropConfig.getSessionIdGeneratorClassname();
                    props = mgrPropConfig.getProperties();
                    
                    handlerCtx.setOutputValue("ReapInterval", reapInterval);
                    handlerCtx.setOutputValue("MaxSessions", maxSessions);
                    handlerCtx.setOutputValue("SessFileName", sessFileName);
                    handlerCtx.setOutputValue("SessionIdGen", sessionIdGen);
                }
            }
        }
        handlerCtx.setOutputValue("Properties", props);
    }
    
    /**
     *	<p> This handler saves the values for the attributes in
     *      Web Container - Manager Props </p>
     *	<p> Input value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "ReapInterval"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "MaxSessions"        -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "SessFileName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "SessionIdGen"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"           -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"        -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveWebManagerProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="ReapInterval",       type=String.class),
        @HandlerInput(name="MaxSessions",        type=String.class),
        @HandlerInput(name="SessFileName",       type=String.class),
        @HandlerInput(name="SessionIdGen",       type=String.class),
        @HandlerInput(name="AddProps",           type=Map.class),
        @HandlerInput(name="RemoveProps",        type=ArrayList.class)})

        public static void saveWebManagerProps(HandlerContext handlerCtx) {
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
            SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();
        
            try{
                String reapInterval = (String)handlerCtx.getInputValue("ReapInterval");
                String maxSessions = (String)handlerCtx.getInputValue("MaxSessions");
                String sessFileName = (String)handlerCtx.getInputValue("SessFileName");
                String sessionIdgen = (String)handlerCtx.getInputValue("SessionIdGen");
                if((sessionConfig != null) && (sessionConfig.getSessionManagerConfig() != null)
                    && (sessionConfig.getSessionManagerConfig().getManagerPropertiesConfig() != null)) {
                    ManagerPropertiesConfig mgrPropConfig = sessionConfig.getSessionManagerConfig().getManagerPropertiesConfig();
                    mgrPropConfig.setReapIntervalInSeconds(reapInterval);
                    mgrPropConfig.setMaxSessions(maxSessions);
                    mgrPropConfig.setSessionFileName(sessFileName);
                    mgrPropConfig.setSessionIdGeneratorClassname(sessionIdgen);
                    AMXRoot.getInstance().editProperties(handlerCtx, mgrPropConfig);
                }else{
                    
                    /* in V2
                    String objName = "com.sun.appserv:type=configs,category=config";
                    String opername = "createManagerProperties";
                    String[] signature = {"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};

                    AttributeList attrList = new AttributeList();
                    attrList.add(new Attribute("reap-interval-in-seconds", reapInterval));
                    attrList.add(new Attribute("max-sessions", maxSessions));
                    attrList.add(new Attribute("session-file-name", sessFileName));
                    attrList.add(new Attribute("session-id-generator-classname", sessionIdgen));

                    Properties props = new Properties();
                    Object[] params = {attrList, props, configName};
                    JMXUtil.invoke(objName, opername, params, signature);

                    sessionConfig = config.getWebContainerConfig().getSessionConfig();
                    ManagerPropertiesConfig mgrPropConfig = sessionConfig.getSessionManagerConfig().getManagerPropertiesConfig();
                    AMXRoot.getInstance().editProperties(handlerCtx, mgrPropConfig);
                    */
                    if(sessionConfig == null)
                        sessionConfig = config.getWebContainerConfig().createSessionConfig();
                    SessionManagerConfig mgrConfig = sessionConfig.getSessionManagerConfig();
                    if(mgrConfig == null)
                        mgrConfig = sessionConfig.createSessionManagerConfig();
                    ManagerPropertiesConfig mgrPropConfig = mgrConfig.createManagerPropertiesConfig(new HashMap());
                    mgrPropConfig.setReapIntervalInSeconds(reapInterval);
                    mgrPropConfig.setMaxSessions(maxSessions);
                    mgrPropConfig.setSessionFileName(sessFileName);
                    mgrPropConfig.setSessionIdGeneratorClassname(sessionIdgen);
                }
            }catch(Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
     }

    /**
     *	<p> This handler returns the values for the attributes in 
     *      Web Container - Store Props </p>
     *	<p> Input value: "ConfigName"        -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "ReapInterval"     -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Directory"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"       -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebStoreProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },    
    output={
        @HandlerOutput(name="ReapInterval",     type=String.class),
        @HandlerOutput(name="Directory",        type=String.class),
        @HandlerOutput(name="Properties",       type=Map.class) })
        
        public static void getWebStoreProps(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();
        Map <String, String> props = new HashMap();
        if(sessionConfig != null){
            SessionManagerConfig sessMgrConfig = sessionConfig.getSessionManagerConfig();
            if(sessMgrConfig != null){
                StorePropertiesConfig storePropConfig = sessMgrConfig.getStorePropertiesConfig();
                if(storePropConfig != null){
                    String reapInterval = storePropConfig.getReapIntervalInSeconds();
                    String directory = storePropConfig.getDirectory();
                    props = storePropConfig.getProperties();
                    
                    handlerCtx.setOutputValue("ReapInterval", reapInterval);
                    handlerCtx.setOutputValue("Directory", directory);
                }
            }
        }
        handlerCtx.setOutputValue("Properties", props);
    }
    
    /**
     *	<p> This handler saves the values for the attributes in
     *      Web Container - Store Props </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "ReapInterval"     -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Directory"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"         -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"      -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveWebStoreProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="ReapInterval",     type=String.class),
        @HandlerInput(name="Directory",        type=String.class),
        @HandlerInput(name="AddProps",         type=Map.class),
        @HandlerInput(name="RemoveProps",      type=ArrayList.class) })

        public static void saveWebStoreProps(HandlerContext handlerCtx) {
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
            SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();
            try{
                String reapInterval = (String)handlerCtx.getInputValue("ReapInterval");
                String directory = (String)handlerCtx.getInputValue("Directory");
                if((sessionConfig != null) && (sessionConfig.getSessionManagerConfig() != null)
                    && (sessionConfig.getSessionManagerConfig().getStorePropertiesConfig() != null)) {
                        StorePropertiesConfig storePropConfig = sessionConfig.getSessionManagerConfig().getStorePropertiesConfig();
                        storePropConfig.setReapIntervalInSeconds(reapInterval);
                        storePropConfig.setDirectory(directory);
                        AMXRoot.getInstance().editProperties(handlerCtx, storePropConfig);
                }else{
                    
                    /*
                    String objName = "com.sun.appserv:type=configs,category=config";
                    String opername = "createStoreProperties";
                    String[] signature = {"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};

                    AttributeList attrList = new AttributeList();
                    attrList.add(new Attribute("reap-interval-in-seconds", reapInterval));
                    attrList.add(new Attribute("directory", directory));

                    Properties props = new Properties();
                    Object[] params = {attrList, props, configName};
                    JMXUtil.invoke(objName, opername, params, signature);
                    sessionConfig = config.getWebContainerConfig().getSessionConfig();
                    StorePropertiesConfig storePropConfig = sessionConfig.getSessionManagerConfig().getStorePropertiesConfig();
                    AMXRoot.getInstance().editProperties(handlerCtx, storePropConfig);
                    */
                    
                    
                    if(sessionConfig == null)
                    sessionConfig = config.getWebContainerConfig().createSessionConfig();
                    SessionManagerConfig mgrConfig = sessionConfig.getSessionManagerConfig();
                    if(mgrConfig == null)
                        mgrConfig = sessionConfig.createSessionManagerConfig();
                    StorePropertiesConfig storePropConfig = mgrConfig.createStorePropertiesConfig(new HashMap());
                    storePropConfig.setReapIntervalInSeconds(reapInterval);
                    storePropConfig.setDirectory(directory);
                    
                }
            }catch(Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
                
                //AMX API CAlls for the same - Not working currently
                /*
                if(sessionConfig == null)
                    sessionConfig = config.getWebContainerConfig().createSessionConfig();
                SessionManagerConfig mgrConfig = sessionConfig.getSessionManagerConfig();
                if(mgrConfig == null)
                    mgrConfig = sessionConfig.createSessionManagerConfig();
                StorePropertiesConfig storePropConfig = mgrConfig.createStorePropertiesConfig(new HashMap());
                storePropConfig.setReapIntervalInSeconds(reapInterval);
                storePropConfig.setDirectory(directory);
                */
            }
    
}
