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
import com.sun.appserv.management.config.PropertyConfig;
import com.sun.appserv.management.config.SessionConfig;
import com.sun.appserv.management.config.SessionManagerConfig;
import com.sun.appserv.management.config.SessionPropertiesConfig;
import com.sun.appserv.management.config.StorePropertiesConfig;
import com.sun.appserv.management.config.WebContainerConfig;
import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.AMXUtil;
import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  
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
     *	@param	handlerCtx	The HandlerContext.
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
        handlerCtx.setOutputValue("Properties", webConfig.getPropertyConfigMap());
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Web Container - Session Props </p>
     *	<p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "SessionTimeout"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	handlerCtx	The HandlerContext.
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
        Map<String, PropertyConfig> props = new HashMap();
        if(sessionConfig != null){
            SessionPropertiesConfig ssPropConfig = sessionConfig.getSessionPropertiesConfig();
            if(ssPropConfig != null){
                String sessTimeout =ssPropConfig.getTimeoutInSeconds();
                handlerCtx.setOutputValue("SessionTimeout", sessTimeout);
                props = ssPropConfig.getPropertyConfigMap();
            }
        }
        handlerCtx.setOutputValue("Properties", props);
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Web Container - General Settings page </p>
     *	<p> Input value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "newProps"        -- Type: <code>java.util.ArrayList</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="saveWebContainerGeneralProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),    
        @HandlerInput(name="newProps",   type=Map.class)})
        
        public static void saveWebContainerGeneralProps(HandlerContext handlerCtx) {
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
            WebContainerConfig webConfig = config.getWebContainerConfig();
            AMXUtil.updateProperties( webConfig, (Map)handlerCtx.getInputValue("newProps"));
    }
    
    /**
     *	<p> This handler sets the values for the attributes in 
     *      Web Container - Session Props </p>
     *	<p> Input value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "SessionTimeout"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "newProps"           -- Type: <code>java.util.Map</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="saveWebSessionProps",
    input={
        @HandlerInput(name="ConfigName",        type=String.class, required=true),    
        @HandlerInput(name="SessionTimeout",    type=String.class),
        @HandlerInput(name="newProps",          type=Map.class)})
        
        public static void saveWebSessionValues(HandlerContext handlerCtx) {
        ConfigConfig config = AMXRoot.getInstance().getConfig((String)handlerCtx.getInputValue("ConfigName"));
        String sessTimeout = (String)handlerCtx.getInputValue("SessionTimeout");    
        SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();
        SessionPropertiesConfig sPropConfig = null;
        try{
            if((sessionConfig != null) && (sessionConfig.getSessionPropertiesConfig() != null)) {
                sPropConfig = sessionConfig.getSessionPropertiesConfig();
            }else {
                if(sessionConfig == null)
                    sessionConfig = config.getWebContainerConfig().createSessionConfig();
                sPropConfig = sessionConfig.getSessionPropertiesConfig();
                if (sPropConfig == null)
                    sPropConfig = sessionConfig.createSessionPropertiesConfig(new HashMap());
            }
            sPropConfig.setTimeoutInSeconds(sessTimeout);
            AMXUtil.updateProperties( sPropConfig, (Map)handlerCtx.getInputValue("newProps"));
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
     *	@param	handlerCtx	The HandlerContext.
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
        Map<String, PropertyConfig> props = new HashMap();
        if(sessionConfig != null){
            SessionManagerConfig sessMgrConfig = sessionConfig.getSessionManagerConfig();
            if(sessMgrConfig != null){
                ManagerPropertiesConfig mgrPropConfig = sessMgrConfig.getManagerPropertiesConfig();
                if(mgrPropConfig != null){
                    String reapInterval = mgrPropConfig.getReapIntervalInSeconds();
                    String maxSessions = mgrPropConfig.getMaxSessions();
                    String sessFileName = mgrPropConfig.getSessionFileName();
                    String sessionIdGen = mgrPropConfig.getSessionIdGeneratorClassname();
                    props = mgrPropConfig.getPropertyConfigMap();
                    
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
     *  <p> Input value: "newProps"           -- Type: <code>java.util.Map</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="saveWebManagerProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="ReapInterval",       type=String.class),
        @HandlerInput(name="MaxSessions",        type=String.class),
        @HandlerInput(name="SessFileName",       type=String.class),
        @HandlerInput(name="SessionIdGen",       type=String.class),
        @HandlerInput(name="newProps",           type=Map.class)})

        public static void saveWebManagerProps(HandlerContext handlerCtx) {
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
            SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();
        
            try{
                String reapInterval = (String)handlerCtx.getInputValue("ReapInterval");
                String maxSessions = (String)handlerCtx.getInputValue("MaxSessions");
                String sessFileName = (String)handlerCtx.getInputValue("SessFileName");
                String sessionIdgen = (String)handlerCtx.getInputValue("SessionIdGen");
                ManagerPropertiesConfig mgrPropConfig = null;
                if((sessionConfig != null) && (sessionConfig.getSessionManagerConfig() != null)
                    && (sessionConfig.getSessionManagerConfig().getManagerPropertiesConfig() != null)) {
                    mgrPropConfig = sessionConfig.getSessionManagerConfig().getManagerPropertiesConfig();
                }else{
                    if(sessionConfig == null)
                        sessionConfig = config.getWebContainerConfig().createSessionConfig();
                    SessionManagerConfig mgrConfig = sessionConfig.getSessionManagerConfig();
                    if(mgrConfig == null)
                        mgrConfig = sessionConfig.createSessionManagerConfig();
                    mgrPropConfig = mgrConfig.getManagerPropertiesConfig();
                    if (mgrPropConfig == null)
                        mgrPropConfig = mgrConfig.createManagerPropertiesConfig(new HashMap());
                }
                mgrPropConfig.setReapIntervalInSeconds(reapInterval);
                mgrPropConfig.setMaxSessions(maxSessions);
                mgrPropConfig.setSessionFileName(sessFileName);
                mgrPropConfig.setSessionIdGeneratorClassname(sessionIdgen);
                AMXUtil.updateProperties( mgrPropConfig, (Map)handlerCtx.getInputValue("newProps"));
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
     *	@param	handlerCtx	The HandlerContext.
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
        Map <String, PropertyConfig> props = new HashMap();
        if(sessionConfig != null){
            SessionManagerConfig sessMgrConfig = sessionConfig.getSessionManagerConfig();
            if(sessMgrConfig != null){
                StorePropertiesConfig storePropConfig = sessMgrConfig.getStorePropertiesConfig();
                if(storePropConfig != null){
                    String reapInterval = storePropConfig.getReapIntervalInSeconds();
                    String directory = storePropConfig.getDirectory();
                    props = storePropConfig.getPropertyConfigMap();
                    
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
     *  <p> Input value: "newProps"         -- Type: <code>java.util.Map</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="saveWebStoreProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="ReapInterval",     type=String.class),
        @HandlerInput(name="Directory",        type=String.class),
        @HandlerInput(name="newProps",         type=Map.class) })

        public static void saveWebStoreProps(HandlerContext handlerCtx) {
            ConfigConfig config = AMXRoot.getInstance().getConfig((String) handlerCtx.getInputValue("ConfigName"));
            SessionConfig sessionConfig = config.getWebContainerConfig().getSessionConfig();
            StorePropertiesConfig storePropConfig = null;
            String reapInterval = (String)handlerCtx.getInputValue("ReapInterval");
            String directory = (String)handlerCtx.getInputValue("Directory");
            try{
                if((sessionConfig != null) && (sessionConfig.getSessionManagerConfig() != null)
                    && (sessionConfig.getSessionManagerConfig().getStorePropertiesConfig() != null)) {
                    storePropConfig = sessionConfig.getSessionManagerConfig().getStorePropertiesConfig();
                }else{
                    if(sessionConfig == null)
                        sessionConfig = config.getWebContainerConfig().createSessionConfig();
                    SessionManagerConfig mgrConfig = sessionConfig.getSessionManagerConfig();
                    if(mgrConfig == null)
                        mgrConfig = sessionConfig.createSessionManagerConfig();
                    storePropConfig = mgrConfig.getStorePropertiesConfig();
                    if (storePropConfig == null)
                        storePropConfig = mgrConfig.createStorePropertiesConfig(new HashMap());
                }
                storePropConfig.setReapIntervalInSeconds(reapInterval);
                storePropConfig.setDirectory(directory);
                AMXUtil.updateProperties( storePropConfig, (Map)handlerCtx.getInputValue("newProps"));
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
