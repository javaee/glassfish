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
import com.sun.appserv.management.config.EJBContainerConfig;
import com.sun.appserv.management.config.EJBTimerServiceConfig;
import com.sun.appserv.management.config.MDBContainerConfig;
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
import java.util.Properties;
import javax.faces.model.SelectItem;
import javax.management.Attribute;
import javax.management.AttributeList;

/**
 *
 * @author Nitya Doraisamy
 */
public class ContainerHandlers {
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      EJB Container - EJB Settings </p>
     *	<p> Input value: "cName"               -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "SessionStore"       -- Type: <code>java.lang.String</code></p>
     * 	<p> Output value: "CommitOption"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MinSize"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxSize"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PoolResize"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"            -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "MaxCache"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "CacheResize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RemTimout"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RemPolicy"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "CacheIdle"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getEjbSettings",
    input={
        @HandlerInput(name="cName", type=String.class, required=true)   },    
    output={
        @HandlerOutput(name="SessionStore",    type=String.class),
        @HandlerOutput(name="CommitOption",    type=String.class),
        @HandlerOutput(name="MinSize",         type=String.class),
        @HandlerOutput(name="MaxSize",         type=String.class),
        @HandlerOutput(name="PoolResize",      type=String.class),
        @HandlerOutput(name="Timeout",         type=String.class),
        @HandlerOutput(name="MaxCache",        type=String.class),
        @HandlerOutput(name="CacheResize",     type=String.class),
        @HandlerOutput(name="RemTimout",       type=String.class),
        @HandlerOutput(name="RemPolicy",       type=String.class),
        @HandlerOutput(name="CacheIdle",       type=String.class),
        @HandlerOutput(name="Properties",      type=Map.class)})
        
        public static void getEjbSettings(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("cName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        EJBContainerConfig ejbContainer = config.getEJBContainerConfig();
        
        String sessionStore = ejbContainer.getSessionStore();
        String commitOpt = ejbContainer.getCommitOption();
        String minSize = ejbContainer.getSteadyPoolSize();
        String maxSize = ejbContainer.getMaxPoolSize();
        String poolResize = ejbContainer.getPoolResizeQuantity();
        String timeout = ejbContainer.getPoolIdleTimeoutInSeconds();
        String maxCache = ejbContainer.getMaxCacheSize();
        String cacheResize = ejbContainer.getCacheResizeQuantity();
        String removalTimeout = ejbContainer.getRemovalTimeoutInSeconds();
        String removalPolicy = ejbContainer.getVictimSelectionPolicy();
        String cacheIdle = ejbContainer.getCacheIdleTimeoutInSeconds();
        Map<String, String> props = ejbContainer.getProperties();
        
        handlerCtx.setOutputValue("SessionStore", sessionStore);
        handlerCtx.setOutputValue("CommitOption", commitOpt);
        handlerCtx.setOutputValue("MinSize", minSize);
        handlerCtx.setOutputValue("MaxSize", maxSize);
        handlerCtx.setOutputValue("PoolResize", poolResize);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("MaxCache", maxCache);
        handlerCtx.setOutputValue("CacheResize", cacheResize);
        handlerCtx.setOutputValue("RemTimout", removalTimeout);
        handlerCtx.setOutputValue("RemPolicy", removalPolicy);
        handlerCtx.setOutputValue("CacheIdle", cacheIdle);        
        handlerCtx.setOutputValue("Properties", props);        
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      EJB Container - EJB Settings </p>
     *	<p> Input value: "cName"               -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "SessionStore"       -- Type: <code>java.lang.String</code></p>
     * 	<p> Output value: "CommitOption"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MinSize"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxSize"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PoolResize"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"            -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "MaxCache"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "CacheResize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RemTimout"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RemPolicy"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "CacheIdle"          -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultEjbSettings",
    output={
        @HandlerOutput(name="SessionStore",    type=String.class),
        @HandlerOutput(name="CommitOption",    type=String.class),
        @HandlerOutput(name="MinSize",         type=String.class),
        @HandlerOutput(name="MaxSize",         type=String.class),
        @HandlerOutput(name="PoolResize",      type=String.class),
        @HandlerOutput(name="Timeout",         type=String.class),
        @HandlerOutput(name="MaxCache",        type=String.class),
        @HandlerOutput(name="CacheResize",     type=String.class),
        @HandlerOutput(name="RemTimout",       type=String.class),
        @HandlerOutput(name="RemPolicy",       type=String.class),
        @HandlerOutput(name="CacheIdle",       type=String.class) })
        
        public static void getDefaultEjbSettings(HandlerContext handlerCtx) {
        Map<String, String> attrMap = AMXRoot.getInstance().getDomainConfig().getDefaultAttributeValues(EJBContainerConfig.J2EE_TYPE);
        
        handlerCtx.setOutputValue("SessionStore", attrMap.get("session-store"));
        handlerCtx.setOutputValue("CommitOption", attrMap.get("commit-option"));
        handlerCtx.setOutputValue("MinSize", attrMap.get("steady-pool-size"));
        handlerCtx.setOutputValue("MaxSize", attrMap.get("max-pool-size"));
        handlerCtx.setOutputValue("PoolResize", attrMap.get("pool-resize-quantity"));
        handlerCtx.setOutputValue("Timeout", attrMap.get("pool-idle-timeout-in-seconds"));
        handlerCtx.setOutputValue("MaxCache", attrMap.get("max-cache-size"));
        handlerCtx.setOutputValue("CacheResize", attrMap.get("cache-resize-quantity"));
        handlerCtx.setOutputValue("RemTimout", attrMap.get("removal-timeout-in-seconds"));
        handlerCtx.setOutputValue("RemPolicy", attrMap.get("victim-selection-policy"));
        handlerCtx.setOutputValue("CacheIdle", attrMap.get("cache-idle-timeout-in-seconds"));
    }
    
    /**
     *	<p> This handler returns the values for the Removal Selection Policy in 
     *      EJB Container - EJB Settings </p>
     *	<p> Input value: "cName"               -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Policies"           -- Type: <code>SelectItem.class</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getRemovalPolicies",
    input={
        @HandlerInput(name="cName", type=String.class, required=true)},    
    output={
        @HandlerOutput(name="Policies",        type=SelectItem[].class) })
        
        public static void getRemovalPolicies(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("cName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        
        String[] policyLabels = {"Not Recently Used (nru)", "First In First Out (fifo)", "Least Recently Used (lru)"};
        String[] policyItems = {"nru", "fifo", "lru"};
        //TODO-V3
        //SelectItem[] options = getOptions(policyItems, policyLabels);
        SelectItem[] options = new SelectItem[0];
        
        handlerCtx.setOutputValue("Policies", options);
     }
    
  
     /**
     *	<p> This handler sets the values for all the attributes in 
     *      EJB Container - EJB Settings </p>
     *  <p> Input  value: "cName              -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "SessionStore"       -- Type: <code>java.lang.String</code></p>
     * 	<p> Input value: "CommitOption"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MinSize"            -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MaxSize"            -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "PoolResize"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Timeout"            -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "MaxCache"           -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "CacheResize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RemTimout"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RemPolicy"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "CacheIdle"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"           -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"        -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveEjbSettings",
    input={
        @HandlerInput(name="cName", type=String.class, required=true),
        @HandlerInput(name="SessionStore",      type=String.class),
        @HandlerInput(name="CommitOption",      type=String.class),
        @HandlerInput(name="MinSize",           type=String.class),
        @HandlerInput(name="MaxSize",           type=String.class),
        @HandlerInput(name="PoolResize",        type=String.class),
        @HandlerInput(name="Timeout",           type=String.class),
        @HandlerInput(name="MaxCache",          type=String.class),
        @HandlerInput(name="CacheResize",       type=String.class),
        @HandlerInput(name="RemTimout",         type=String.class),
        @HandlerInput(name="RemPolicy",         type=String.class),
        @HandlerInput(name="CacheIdle",         type=String.class),
        @HandlerInput(name="AddProps",          type=Map.class),
        @HandlerInput(name="RemoveProps",       type=ArrayList.class)})
        
        public static void saveEjbSettings(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("cName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        EJBContainerConfig ejbContainer = config.getEJBContainerConfig();
        try{
            ejbContainer.setSessionStore((String)handlerCtx.getInputValue("SessionStore"));
            ejbContainer.setCommitOption(((String)handlerCtx.getInputValue("CommitOption")));
            ejbContainer.setSteadyPoolSize((String)handlerCtx.getInputValue("MinSize"));
            ejbContainer.setMaxPoolSize((String)handlerCtx.getInputValue("MaxSize"));
            ejbContainer.setPoolResizeQuantity((String)handlerCtx.getInputValue("PoolResize"));
            ejbContainer.setPoolIdleTimeoutInSeconds((String)handlerCtx.getInputValue("Timeout"));
            ejbContainer.setMaxCacheSize((String)handlerCtx.getInputValue("MaxCache"));
            ejbContainer.setCacheResizeQuantity((String)handlerCtx.getInputValue("CacheResize"));
            ejbContainer.setRemovalTimeoutInSeconds((String)handlerCtx.getInputValue("RemTimout"));
            ejbContainer.setVictimSelectionPolicy((String)handlerCtx.getInputValue("RemPolicy"));
            ejbContainer.setCacheIdleTimeoutInSeconds((String)handlerCtx.getInputValue("CacheIdle"));       
            AMXRoot.getInstance().editProperties(handlerCtx, ejbContainer);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      EJB Container - MDB Settings </p>
     *	<p> Input value: "ConfigName"    -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "MinSize"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxSize"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PoolResize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"   -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getEjbMdbSettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },    
    output={
        @HandlerOutput(name="MinSize",     type=String.class),
        @HandlerOutput(name="MaxSize",     type=String.class),
        @HandlerOutput(name="PoolResize",  type=String.class),
        @HandlerOutput(name="Timeout",     type=String.class),
        @HandlerOutput(name="Properties",  type=Map.class)})
        
        public static void getEjbMdbSettings(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        MDBContainerConfig mdbConfig = config.getMDBContainerConfig();
        
        String minSize = mdbConfig.getSteadyPoolSize();
        String maxSize = mdbConfig.getMaxPoolSize();
        String poolResize = mdbConfig.getPoolResizeQuantity();
        String timeout = mdbConfig.getIdleTimeoutInSeconds();
        Map<String, String> props = mdbConfig.getProperties();
        handlerCtx.setOutputValue("MinSize", minSize);
        handlerCtx.setOutputValue("MaxSize", maxSize);
        handlerCtx.setOutputValue("PoolResize", poolResize);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Properties", props);
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      EJB Container - MDB Settings </p>
     *	<p> Input value: "ConfigName"   -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "MinSize"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxSize"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PoolResize"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultEjbMdbSettings",
    output={
        @HandlerOutput(name="MinSize",     type=String.class),
        @HandlerOutput(name="MaxSize",     type=String.class),
        @HandlerOutput(name="PoolResize",  type=String.class),
        @HandlerOutput(name="Timeout",     type=String.class) })
        
        public static void getDefaultEjbMdbSettings(HandlerContext handlerCtx) {
        Map<String, String> attrMap = AMXRoot.getInstance().getDomainConfig().getDefaultAttributeValues(MDBContainerConfig.J2EE_TYPE);
        
        handlerCtx.setOutputValue("MinSize", attrMap.get("steady-pool-size"));
        handlerCtx.setOutputValue("MaxSize", attrMap.get("max-pool-size"));
        handlerCtx.setOutputValue("PoolResize", attrMap.get("pool-resize-quantity"));
        handlerCtx.setOutputValue("Timeout", attrMap.get("idle-timeout-in-seconds"));
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      EJB Container - MDB Settings </p>
     *	<p> Input value: "ConfigName"     -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "MinSize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MaxSize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "PoolResize"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Timeout"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"       -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"    -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveEjbMdbSettings",
    input={
        @HandlerInput(name="ConfigName",  type=String.class, required=true),
        @HandlerInput(name="MinSize",     type=String.class),
        @HandlerInput(name="MaxSize",     type=String.class),
        @HandlerInput(name="PoolResize",  type=String.class),
        @HandlerInput(name="Timeout",     type=String.class),
        @HandlerInput(name="AddProps",          type=Map.class),
        @HandlerInput(name="RemoveProps",       type=ArrayList.class)})
        
        public static void saveEjbMdbSettings(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        try{
            MDBContainerConfig mdbConfig = config.getMDBContainerConfig();
            mdbConfig.setSteadyPoolSize((String)handlerCtx.getInputValue("MinSize"));
            mdbConfig.setMaxPoolSize((String)handlerCtx.getInputValue("MaxSize"));
            mdbConfig.setPoolResizeQuantity((String)handlerCtx.getInputValue("PoolResize"));
            mdbConfig.setIdleTimeoutInSeconds((String)handlerCtx.getInputValue("Timeout"));        
            AMXRoot.getInstance().editProperties(handlerCtx, mdbConfig);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      EJB Container - EJB Timer Service </p>
     *	<p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "MinDelivery"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxRedelivery"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RedeliveryIntr"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "TimerDatasource"    -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getEjbTimerServiceValues",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },    
    output={
        @HandlerOutput(name="MinDelivery",       type=String.class),
        @HandlerOutput(name="MaxRedelivery",     type=String.class),
        @HandlerOutput(name="RedeliveryIntr",    type=String.class),
        @HandlerOutput(name="TimerDatasource",   type=String.class) })
        
        public static void getEjbTimerSettings(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        EJBTimerServiceConfig ejbTimerSrv = config.getEJBContainerConfig().getEJBTimerServiceConfig();
        
        String minDelivery = ejbTimerSrv.getMinimumDeliveryIntervalInMillis();
        String maxRedelivery = ejbTimerSrv.getMaxRedeliveries();
        String redeliveryIntr = ejbTimerSrv.getRedeliveryIntervalInternalInMillis();
        String timerDatasource = ejbTimerSrv.getTimerDatasource();
        
        handlerCtx.setOutputValue("MinDelivery", minDelivery);
        handlerCtx.setOutputValue("MaxRedelivery", maxRedelivery);
        handlerCtx.setOutputValue("RedeliveryIntr", redeliveryIntr);
        handlerCtx.setOutputValue("TimerDatasource", timerDatasource);
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      EJB Container - EJB Timer Service </p>
     *	<p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "MinDelivery"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxRedelivery"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RedeliveryIntr"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "TimerDatasource"    -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultEjbTimerServiceValues",
    output={
        @HandlerOutput(name="MinDelivery",       type=String.class),
        @HandlerOutput(name="MaxRedelivery",     type=String.class),
        @HandlerOutput(name="RedeliveryIntr",    type=String.class),
        @HandlerOutput(name="TimerDatasource",   type=String.class) })
        
        public static void getDefaultEjbTimerServiceValues(HandlerContext handlerCtx) {
        Map<String, String> attrMap = AMXRoot.getInstance().getDomainConfig().getDefaultAttributeValues(EJBTimerServiceConfig.J2EE_TYPE);
        
        handlerCtx.setOutputValue("MinDelivery", attrMap.get("minimum-delivery-interval-in-millis"));
        handlerCtx.setOutputValue("MaxRedelivery", attrMap.get("max-redeliveries"));
        handlerCtx.setOutputValue("RedeliveryIntr", attrMap.get("redelivery-interval-internal-in-millis"));
        handlerCtx.setOutputValue("TimerDatasource", attrMap.get("timer-datasource"));
      
    }
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      EJB Container - EJB Timer Service </p>
     *	<p> Input value: "ConfigName"         -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Input value: "MinDelivery"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MaxRedelivery"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RedeliveryIntr"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "TimerDatasource"    -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveEjbTimerServiceValues",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),    
        @HandlerInput(name="MinDelivery",       type=String.class),
        @HandlerInput(name="MaxRedelivery",     type=String.class),
        @HandlerInput(name="RedeliveryIntr",    type=String.class),
        @HandlerInput(name="TimerDatasource",   type=String.class) })
        
        public static void saveEjbTimerServiceValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        try{
            EJBTimerServiceConfig ejbTimerSrv = config.getEJBContainerConfig().getEJBTimerServiceConfig();
            ejbTimerSrv.setMinimumDeliveryIntervalInMillis((String)handlerCtx.getInputValue("MinDelivery"));
            ejbTimerSrv.setMaxRedeliveries((String)handlerCtx.getInputValue("MaxRedelivery"));
            ejbTimerSrv.setRedeliveryIntervalInternalInMillis((String)handlerCtx.getInputValue("RedeliveryIntr"));
            ejbTimerSrv.setTimerDatasource((String)handlerCtx.getInputValue("TimerDatasource"));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
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
                String sessTimeout = "AMX Exception" ; //ssPropConfig.getTimeoutInSeconds();
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
                    String reapInterval = "AMX Expection" ; //mgrPropConfig.getReapIntervalInSeconds();
                    String maxSessions = "AMX Exception";   //mgrPropConfig.getMaxSessions();
                    String sessFileName = "AMX Exception";  //mgrPropConfig.getSessionFileName();
                    String sessionIdGen = "AMX Exception";  //mgrPropConfig.getSessionIdGeneratorClassname();
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
                    String reapInterval = "AMX Exception";  //storePropConfig.getReapIntervalInSeconds();
                    String directory = "AMX Exception";   //storePropConfig.getDirectory();
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
