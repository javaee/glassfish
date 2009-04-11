
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
 * JdbcHandler.java
 *
 * Created on August 10, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author anilam
 */

package org.glassfish.admingui.handlers;

import com.sun.appserv.management.base.KitchenSink;
import com.sun.appserv.management.base.SystemStatus;
import com.sun.appserv.management.base.XTypes;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.TargetUtil;

import com.sun.appserv.management.config.JDBCConnectionPoolConfig;
import com.sun.appserv.management.config.JDBCResourceConfig;
import com.sun.appserv.management.base.SystemStatus;

import com.sun.webui.jsf.component.Field;


public class JdbcHandlers {
    /** Creates a new instance of JdbcHandler */
    public JdbcHandlers() {
    }
    
    /**
     *	<p> This handler returns the values for all the attributes of the Jdbc Resource
     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
     *	<p> Output value: "jndiName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "poolName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getJdbcInfo",
        input={
            @HandlerInput(name="jndiName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="poolName", type=String.class),
            @HandlerOutput(name="description", type=String.class),
            @HandlerOutput(name="enabledString", type=String.class),
            @HandlerOutput(name="enabled", type=Boolean.class)} )
    public static void getJdbcInfo(HandlerContext handlerCtx) {
        
        String jndiName = (String) handlerCtx.getInputValue("jndiName");

        if (GuiUtil.isEmpty(jndiName)){
            //for creating JDBC,this will be empty.
            handlerCtx.setOutputValue("enabled", Boolean.TRUE);
            return;
        }
        
	JDBCResourceConfig jdbc = AMXRoot.getInstance().getResourcesConfig().getJDBCResourceConfigMap().get(jndiName);
	if (jdbc == null){
	    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchJDBCResource"));
	}else{
	    handlerCtx.setOutputValue("poolName", jdbc.getPoolName());
	    handlerCtx.setOutputValue("description", jdbc.getDescription());
            if(AMXRoot.getInstance().isEE()) {
                handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(jdbc, false));
            }else{
                handlerCtx.setOutputValue("enabled", TargetUtil.isResourceEnabled(jdbc, "server" ));
            }
	}

    }
    
    
    /**
     *	<p> This handler returns the values for all the attributes of the Jdbc Resource
     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
     *	<p> Output value: "jndiName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "poolName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="saveJdbc",
        input={
            @HandlerInput(name="edit", type=Boolean.class, required=true),
            @HandlerInput(name="jndiName", type=String.class, required=true),
            @HandlerInput(name="poolName", type=String.class, required=true),
            @HandlerInput(name="description", type=String.class),
            @HandlerInput(name="enabled", type=Boolean.class),
            @HandlerInput(name="targets", type=String[].class )
        })
    public static void saveJdbc(HandlerContext handlerCtx) {
        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        String poolName = (String) handlerCtx.getInputValue("poolName");
        Boolean edit = (Boolean) handlerCtx.getInputValue("edit");
        
        JDBCResourceConfig jdbc = null;
        
        try{
            if (edit){
                 jdbc = AMXRoot.getInstance().getResourcesConfig().getJDBCResourceConfigMap().get(jndiName);
                if (jdbc == null){
		    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchJDBCResource"));
                    return;
                }
                jdbc.setPoolName(poolName);
                if(!AMXRoot.getInstance().isEE()){
                    Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
                    TargetUtil.setResourceEnabled(jdbc, "server", enabled); 
                }
                GuiUtil.prepareSuccessful(handlerCtx);
            }else{
                 Map optionalMap = new HashMap();
                 optionalMap.put("enabled", "true");
                 jdbc = AMXRoot.getInstance().getResourcesConfig().createJDBCResourceConfig(jndiName, poolName, optionalMap);
                 TargetUtil.createNewTargets(handlerCtx,  jndiName);
            }
            jdbc.setDescription((String)handlerCtx.getInputValue("description"));
            
        }catch (Exception ex){
	    GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    
    
    /**
     *	<p> This handler returns the list of JDBC Connection Pool
     *  <p> Output value: "connectionPoolNames" -- Type: <code>java.util.List</code></p>
     */
    @Handler(id="getJdbcConnectionPools",
        output={
            @HandlerOutput(name="jdbcConnectionPools", type=java.util.List.class)}
        )
    public static void getJdbcConnectionPools(HandlerContext handlerCtx) {
	Set keys = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().keySet();
	handlerCtx.setOutputValue("jdbcConnectionPools", new ArrayList(keys));
    }


    /**
     *	<p> This handler returns the values for all the attributes of the Jdbc Connection Pool
     */
    @Handler(id="getJdbcConnectionPoolInfo",
        input={
            @HandlerInput(name="jndiName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="datasourceClassname", type=String.class),
            @HandlerOutput(name="resType", type=String.class),
            @HandlerOutput(name="description", type=String.class),
            @HandlerOutput(name="steadyPoolSize", type=String.class),
            @HandlerOutput(name="maxPoolSize", type=String.class),
            @HandlerOutput(name="poolResizeQuantity", type=String.class),
            @HandlerOutput(name="idleTimeoutInSeconds", type=String.class),
            @HandlerOutput(name="maxWaitTimeInMillis", type=String.class),
            @HandlerOutput(name="isConnectionValidationRequired", type=Boolean.class),
            @HandlerOutput(name="connectionValidationMethod", type=String.class),
            @HandlerOutput(name="validationTableName", type=String.class),
            @HandlerOutput(name="failAllConnections", type=Boolean.class),
            @HandlerOutput(name="allowNonComponentCallers", type=Boolean.class),
            @HandlerOutput(name="nonTransactionalConnections", type=Boolean.class),
            @HandlerOutput(name="transactionIsolationLevel", type=String.class),
            @HandlerOutput(name="isIsolationLevelGuaranteed", type=Boolean.class)}
                )
        public static void getJdbcConnectionPoolInfo(HandlerContext handlerCtx) {
        
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            JDBCConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().get(jndiName);
            if (pool == null){
		GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchJDBCConnectionPool"));
            }
            handlerCtx.setOutputValue("datasourceClassname", pool.getDatasourceClassname());
            handlerCtx.setOutputValue("resType", pool.getResType());
            handlerCtx.setOutputValue("description", pool.getDescription());
            handlerCtx.setOutputValue("steadyPoolSize", pool.getSteadyPoolSize());
            handlerCtx.setOutputValue("maxPoolSize", pool.getMaxPoolSize());
            handlerCtx.setOutputValue("poolResizeQuantity", pool.getPoolResizeQuantity());
            handlerCtx.setOutputValue("idleTimeoutInSeconds", pool.getIdleTimeoutInSeconds());
            handlerCtx.setOutputValue("maxWaitTimeInMillis", pool.getMaxWaitTimeInMillis());
            handlerCtx.setOutputValue("isConnectionValidationRequired", pool.getIsConnectionValidationRequired());
            handlerCtx.setOutputValue("connectionValidationMethod", pool.getConnectionValidationMethod());
            handlerCtx.setOutputValue("validationTableName", pool.getValidationTableName());
            handlerCtx.setOutputValue("failAllConnections", pool.getFailAllConnections());
            handlerCtx.setOutputValue("allowNonComponentCallers", pool.getAllowNonComponentCallers());
            handlerCtx.setOutputValue("nonTransactionalConnections", pool.getNonTransactionalConnections());
            handlerCtx.setOutputValue("transactionIsolationLevel", pool.getTransactionIsolationLevel());
            handlerCtx.setOutputValue("isIsolationLevelGuaranteed", pool.getIsIsolationLevelGuaranteed());
        }

    
    /**
     *	<p> This handler returns the properties of the Jdbc Connection Pool
     */
    @Handler(id="getJdbcConnectionPoolProperty",
        input={
            @HandlerInput(name="jndiName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="properties",type=Map.class)}
                )
        public static void getJdbcConnectionPoolProperty(HandlerContext handlerCtx) {
        
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            JDBCConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().get(jndiName);
            if (pool == null){
		GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchJDBCConnectionPool"));
            }
            handlerCtx.setOutputValue("properties", pool.getPropertyConfigMap());
        }


    /**
     *	<p> This handler saves the values for all the attributes of the Jdbc Connection Pool
     */

    @Handler(id="saveJdbcConnectionPool",
        input={
		@HandlerInput(name="jndiName", type=String.class, required=true),
		@HandlerInput(name="datasourceClassname", type=String.class, required=true),
		@HandlerInput(name="resType", type=String.class),
		@HandlerInput(name="description", type=String.class),
		@HandlerInput(name="steadyPoolSize", type=String.class),
		@HandlerInput(name="maxPoolSize", type=String.class),
		@HandlerInput(name="poolResizeQuantity", type=String.class),
		@HandlerInput(name="idleTimeoutInSeconds", type=String.class),
		@HandlerInput(name="maxWaitTimeInMillis", type=String.class),
		@HandlerInput(name="isConnectionValidationRequired", type=String.class),
		@HandlerInput(name="connectionValidationMethod", type=String.class),
		@HandlerInput(name="validationTableName", type=String.class),
		@HandlerInput(name="failAllConnections", type=String.class),
		@HandlerInput(name="allowNonComponentCallers", type=String.class),
		@HandlerInput(name="nonTransactionalConnections", type=String.class),
		@HandlerInput(name="transactionIsolationLevel", type=String.class),
		@HandlerInput(name="isIsolationLevelGuaranteed", type=String.class)
        })
    public static void saveJdbcConnectionPool(HandlerContext handlerCtx) {

        try{
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            JDBCConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().get(jndiName);
                if (pool == null){
		    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchJDBCConnectionPool"));
                    return;
                }
            pool.setDatasourceClassname ((String) handlerCtx.getInputValue("datasourceClassname"));
            pool.setResType ((String) handlerCtx.getInputValue("resType"));
            pool.setDescription((String) handlerCtx.getInputValue("description"));
            pool.setMaxPoolSize ((String) handlerCtx.getInputValue("maxPoolSize"));
            pool.setSteadyPoolSize ((String) handlerCtx.getInputValue("steadyPoolSize"));
            pool.setPoolResizeQuantity ((String) handlerCtx.getInputValue("poolResizeQuantity"));
            pool.setIdleTimeoutInSeconds ((String) handlerCtx.getInputValue("idleTimeoutInSeconds"));
            pool.setMaxWaitTimeInMillis ((String) handlerCtx.getInputValue("maxWaitTimeInMillis"));
            pool.setIsConnectionValidationRequired ((String) handlerCtx.getInputValue("isConnectionValidationRequired"));
            String method = (String) handlerCtx.getInputValue("connectionValidationMethod");
            pool.setConnectionValidationMethod (method);
            if ("table".equals(method)){
                String table = (String) handlerCtx.getInputValue("validationTableName");
                if (! GuiUtil.isEmpty(table))
                    pool.setValidationTableName (table);
            }else{
                pool.setValidationTableName("");
            }
            pool.setFailAllConnections ((String) handlerCtx.getInputValue("failAllConnections"));
            pool.setAllowNonComponentCallers ((String) handlerCtx.getInputValue("allowNonComponentCallers"));
            pool.setNonTransactionalConnections ((String) handlerCtx.getInputValue("nonTransactionalConnections"));
            pool.setTransactionIsolationLevel ((String) handlerCtx.getInputValue("transactionIsolationLevel"));
            pool.setIsIsolationLevelGuaranteed ((String) handlerCtx.getInputValue("isIsolationLevelGuaranteed"));
            GuiUtil.prepareSuccessful(handlerCtx);
        }catch (Exception ex){
	    GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    
    /**
     *	<p> This handler saves the values for all the attributes of the Jdbc Connection Pool
     */

    @Handler(id="saveJdbcConnectionPoolProperty",
        input={
		@HandlerInput(name="jndiName", type=String.class, required=true),
                @HandlerInput(name="newProps",    type=Map.class)
        })
    public static void saveJdbcConnectionPoolProperty(HandlerContext handlerCtx) {
        try{
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            JDBCConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().get(jndiName);
            if (pool == null){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchJDBCConnectionPool"));
                return;
            }
            AMXUtil.updateProperties( pool, (Map)handlerCtx.getInputValue("newProps"));
        }catch (Exception ex){
	    GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the attributes of the Jdbc Connection Pool
     */
    @Handler(id="getJdbcConnectionPoolDefaultInfo",
        input={
            @HandlerInput(name="jndiName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="resType", type=String.class),
            @HandlerOutput(name="description", type=String.class),
            @HandlerOutput(name="steadyPoolSize", type=String.class),
            @HandlerOutput(name="maxPoolSize", type=String.class),
            @HandlerOutput(name="poolResizeQuantity", type=String.class),
            @HandlerOutput(name="idleTimeoutInSeconds", type=String.class),
            @HandlerOutput(name="maxWaitTimeInMillis", type=String.class),
            @HandlerOutput(name="isConnectionValidationRequired", type=Boolean.class),
            @HandlerOutput(name="connectionValidationMethod", type=String.class),
            @HandlerOutput(name="validationTableName", type=String.class),
            @HandlerOutput(name="failAllConnections", type=Boolean.class),
            @HandlerOutput(name="allowNonComponentCallers", type=Boolean.class),
            @HandlerOutput(name="nonTransactionalConnections", type=Boolean.class),
            @HandlerOutput(name="transactionIsolationLevel", type=String.class),
            @HandlerOutput(name="isIsolationLevelGuaranteed", type=Boolean.class)}
                )
        public static void getJdbcConnectionPoolDefaultInfo(HandlerContext handlerCtx) {
        
            Map <String,String> defaultMap = AMXRoot.getInstance().getResourcesConfig().getDefaultValues(XTypes.JDBC_CONNECTION_POOL_CONFIG, true); 
            handlerCtx.setOutputValue("steadyPoolSize", defaultMap.get("SteadyPoolSize"));
            handlerCtx.setOutputValue("maxPoolSize",defaultMap.get("MaxPoolSize"));
            handlerCtx.setOutputValue("poolResizeQuantity", defaultMap.get("PoolResizeQuantity"));
            handlerCtx.setOutputValue("idleTimeoutInSeconds", defaultMap.get("IdleTimeoutInSeconds"));
            handlerCtx.setOutputValue("maxWaitTimeInMillis", defaultMap.get("MaxWaitTimeInMillis"));
            handlerCtx.setOutputValue("isConnectionValidationRequired", StringToBoolean(defaultMap.get("IsConnectionValidationRequired")));
            handlerCtx.setOutputValue("connectionValidationMethod", defaultMap.get("ConnectionValidationMethod"));
            handlerCtx.setOutputValue("validationTableName", defaultMap.get("ValidationTableName"));
            handlerCtx.setOutputValue("failAllConnections", StringToBoolean(defaultMap.get("FailAllConnections")));
            handlerCtx.setOutputValue("allowNonComponentCallers", StringToBoolean(defaultMap.get("AllowNonComponentCallers")));
            handlerCtx.setOutputValue("nonTransactionalConnections", StringToBoolean(defaultMap.get("NonTransactionalConnections")));
            handlerCtx.setOutputValue("transactionIsolationLevel", defaultMap.get("TransactionIsolationLevel"));
            handlerCtx.setOutputValue("isIsolationLevelGuaranteed", StringToBoolean(defaultMap.get("IsIsolationLevelGuaranteed")));
        }
    
        /**
         *	<p> This handler returns the values for all the attributes of the Jdbc Connection Pool
         */
        @Handler(id="getPoolAdvanceInfo",
            input={
                @HandlerInput(name="jndiName", type=String.class, required=true)},
            output={
                @HandlerOutput(name="advance", type=Map.class)}
        )
        public static void getPoolAdvanceInfo(HandlerContext handlerCtx) {
        
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            JDBCConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().get(jndiName);
            if (pool == null){
		GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchJDBCConnectionPool"));
                return;
            }
            Map advance = new HashMap();
            advance.put("wrapJDBCObjects",  StringToBoolean( pool.getWrapJDBCObjects()));
            advance.put("statementTimeoutInSeconds", pool.getStatementTimeoutInSeconds());
            advance.put("validateAtMostOncePeriodInSeconds", pool.getValidateAtMostOncePeriodInSeconds());
            advance.put("connectionLeakTimeoutInSeconds", pool.getConnectionLeakTimeoutInSeconds());
            advance.put("connectionLeakReclaim",  StringToBoolean( pool.getConnectionLeakReclaim()));
            advance.put("connectionCreationRetryAttempts",  pool.getConnectionCreationRetryAttempts());
            advance.put("connectionCreationRetryIntervalInSeconds", pool.getConnectionCreationRetryIntervalInSeconds());
            advance.put("lazyConnectionEnlistment",  StringToBoolean( pool.getLazyConnectionEnlistment()));
            advance.put("lazyConnectionAssociation",  StringToBoolean( pool.getLazyConnectionAssociation()));
            advance.put("associateWithThread", StringToBoolean(  pool.getAssociateWithThread()));
            advance.put("matchConnections",  StringToBoolean( pool.getMatchConnections()));
            advance.put("maxConnectionUsageCount", pool.getMaxConnectionUsageCount());
            handlerCtx.setOutputValue("advance", advance);
        }
        
        /**
         *	<p> This handler returns the default values for the advance attributes of the Jdbc Connection Pool
         */
        @Handler(id="getPoolAdvanceDefaultInfo",
            input={
                @HandlerInput(name="jndiName", type=String.class, required=true)},
            output={
                @HandlerOutput(name="advance", type=Map.class)}
        )
        public static void getPoolAdvanceDefaultInfo(HandlerContext handlerCtx) {
        
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            JDBCConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().get(jndiName);
            if (pool == null){
		GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchJDBCConnectionPool"));
                return;
            }
            Map advance = new HashMap();
            Map <String,String> defaultMap = AMXRoot.getInstance().getResourcesConfig().getDefaultValues(XTypes.JDBC_CONNECTION_POOL_CONFIG, true);
            advance.put("wrapJDBCObjects", StringToBoolean( defaultMap.get("WrapJdbcObjects")));
            advance.put("statementTimeoutInSeconds", defaultMap.get("StatementTimeoutInSeconds"));
            advance.put("validateAtMostOncePeriodInSeconds", defaultMap.get("ValidateAtmostOncePeriodInSeconds"));
            advance.put("connectionLeakTimeoutInSeconds", defaultMap.get("ConnectionLeakTimeoutInSeconds"));
            advance.put("connectionLeakReclaim",  StringToBoolean( defaultMap.get("ConnectionLeakReclaim")));
            advance.put("connectionCreationRetryAttempts",  defaultMap.get("ConnectionCreationRetryAttempts"));
            advance.put("connectionCreationRetryIntervalInSeconds", defaultMap.get("ConnectionCreationRetryIntervalInSeconds"));
            advance.put("lazyConnectionEnlistment",  StringToBoolean( defaultMap.get("LazyConnectionEnlistment")));
            advance.put("lazyConnectionAssociation",  StringToBoolean( defaultMap.get("LazyConnectionAssociation")));
            advance.put("associateWithThread",  StringToBoolean( defaultMap.get("AssociateWithThread")));
            advance.put("matchConnections",  StringToBoolean( defaultMap.get("MatchConnections")));
            advance.put("maxConnectionUsageCount", defaultMap.get("MaxConnectionUsageCount"));
            handlerCtx.setOutputValue("advance", advance);
        }
    
        
    /**
     *	<p> This handler saves the advance attributes of the Jdbc Connection Pool
     */

    @Handler(id="savePoolAdvanceInfo",
        input={
		@HandlerInput(name="jndiName", type=String.class, required=true),
                @HandlerInput(name="advance", type=Map.class)}
      )
    public static void savePoolAdvanceInfo(HandlerContext handlerCtx) {
        try{
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            Map advance = (Map) handlerCtx.getInputValue("advance");
            JDBCConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().get(jndiName);
                if (pool == null){
		    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchJDBCConnectionPool"));
                    return;
                }
		pool.setWrapJDBCObjects(BooleanToString (advance.get("wrapJDBCObjects")));
		pool.setStatementTimeoutInSeconds((String) advance.get("statementTimeoutInSeconds"));
                pool.setValidateAtMostOncePeriodInSeconds((String) advance.get("validateAtMostOncePeriodInSeconds"));
                pool.setConnectionLeakTimeoutInSeconds((String) advance.get("connectionLeakTimeoutInSeconds"));
                pool.setConnectionLeakReclaim( BooleanToString( advance.get("connectionLeakReclaim")));
                pool.setConnectionCreationRetryAttempts((String) advance.get("connectionCreationRetryAttempts"));
                pool.setConnectionCreationRetryIntervalInSeconds((String) advance.get("connectionCreationRetryIntervalInSeconds"));
                pool.setLazyConnectionEnlistment( BooleanToString( advance.get("lazyConnectionEnlistment")));
                pool.setLazyConnectionAssociation( BooleanToString(  advance.get("lazyConnectionAssociation")));
                pool.setAssociateWithThread(BooleanToString(  advance.get("associateWithThread")));
                pool.setMatchConnections(BooleanToString(  advance.get("matchConnections")));
                pool.setMaxConnectionUsageCount((String) advance.get("maxConnectionUsageCount"));
        }catch (Exception ex){
             GuiUtil.handleException(handlerCtx, ex);
        }
    }
            
              
    private static String BooleanToString(Object test){
        if (test == null) return Boolean.FALSE.toString();
        return test.toString();
    }
    
    private static Boolean StringToBoolean(Object test){
        if (test == null) return false;
        if (test instanceof String)
            return Boolean.valueOf( (String) test);
        else
        if (test instanceof Boolean)
            return (Boolean) test;
        return false;
    }
    
            
        /**
         *	<p> This handler pings the  Jdbc Connection Pool
         */
        @Handler(id="pingJdbcConnectionPool",
            input={
                @HandlerInput(name="jndiName", type=String.class, required=true)})
        public static void pingJdbcConnectionPool(HandlerContext handlerCtx) {
            
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            JDBCConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().get(jndiName);
            try {
    
                SystemStatus ss = AMXRoot.getInstance().getDomainRoot().getSystemStatus();
                Map<String, Object> statusMap = ss.pingJDBCConnectionPool(jndiName);
                if ((Boolean) statusMap.get(SystemStatus.PING_SUCCEEDED_KEY)){
                    GuiUtil.prepareAlert(handlerCtx,"success", GuiUtil.getMessage("msg.PingSucceed"), null);
                }else{
                    GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), statusMap.get(SystemStatus.REASON_FAILED_KEY).toString() );
                }
                    
            }catch(Exception ex){
		GuiUtil.handleException(handlerCtx, ex);
            }
        }
     
        
        /**
         *	<p> This handler creates a JDBCConnection Pool to be used in the wizard
         */
        @Handler(id="getJDBCPoolWizard",
        input={
                @HandlerInput(name="fromStep2", type=Boolean.class)},
        output={
            @HandlerOutput(name="ResTypeList", type=java.util.List.class),
            @HandlerOutput(name="DBVendorList", type=java.util.List.class)
            } )
        public static void getJDBCPoolWizard(HandlerContext handlerCtx){
            //We need to use 2 maps for JDBC Connection Pool creation because there are extra info we need to keep track in 
            //the wizard, but cannot be passed to the creation API.
            
            Boolean fromStep2 = (Boolean) handlerCtx.getInputValue("fromStep2");
            if ((fromStep2!= null) && fromStep2){
                //wizardPool is already in session map
            }else{
                Map defaultMap = AMXRoot.getInstance().getResourcesConfig().getDefaultValues(XTypes.JDBC_CONNECTION_POOL_CONFIG, true); 
                Map attrMap = new HashMap();
                attrMap.put("SteadyPoolSize", defaultMap.get("SteadyPoolSize"));
                attrMap.put("MaxPoolSize", defaultMap.get("MaxPoolSize"));
                attrMap.put("MaxWaitTimeInMillis", defaultMap.get("MaxWaitTimeInMillis"));
                attrMap.put("PoolResizeQuantity", defaultMap.get("PoolResizeQuantity"));
                attrMap.put("IdleTimeoutInSeconds", defaultMap.get("IdleTimeoutInSeconds"));
                attrMap.put("IsIsolationLevelGuaranteed", defaultMap.get("IsIsolationLevelGuaranteed"));
                attrMap.put("IsConnectionValidationRequired", defaultMap.get("IsConnectionValidationRequired"));
                attrMap.put("ConnectionValidationMethod", defaultMap.get("ConnectionValidationMethod"));
                attrMap.put("FailAllConnections", defaultMap.get("FailAllConnections"));
                attrMap.put("NonTransactionalConnections", defaultMap.get("NonTransactionalConnections"));
                attrMap.put("AllowNonComponentCallers", defaultMap.get("AllowNonComponentCallers"));
                             
                Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
                sessionMap.put("wizardPool", attrMap);
                sessionMap.put("wizardPoolExtra", new HashMap());
                sessionMap.put("wizardPoolProperties", new HashMap());
            }
            handlerCtx.setOutputValue("ResTypeList", resTypeList);
            handlerCtx.setOutputValue("DBVendorList", dbVendorList);
        }
        
        /**
         *	<p> This handler creates a JDBCConnection Pool to be used in the wizard
         */
        @Handler(id="updateJDBCPoolWizard")
        public static void updateJDBCPoolWizard(HandlerContext handlerCtx){
                Map pool = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPool");
                Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
                
                String resType = (String) pool.get("ResType");
                String dbVendor = (String) extra.get("DBVendor");
                
                String previousResType = (String) extra.get("PreviousResType");
                String previousDB = (String) extra.get("PreviousDB");
                
                if (resType.equals(previousResType) && dbVendor.equals(previousDB) && 
                        !GuiUtil.isEmpty((String) extra.get("DatasourceClassname")) ){
                    //User didn't change type and DB, keep the datasource classname as the same.
                }else{
                    
                    if (!GuiUtil.isEmpty(resType) && !GuiUtil.isEmpty(dbVendor)){
                        String datasourceClassName = "";
                        if (resType.equals(DATA_SOURCE))
                            datasourceClassName =  dataSourceMap.get(dbVendor);
                        else
                        if (resType.equals(XADATA_SOURCE))
                            datasourceClassName =  XADataSourceMap.get(dbVendor);
                        else
                        if (resType.equals(CCDATA_SOURCE))
                            datasourceClassName =  CCDataSourceMap.get(dbVendor);
                        
                        if (datasourceClassName==null) 
                            datasourceClassName="";
                        extra.put("DatasourceClassname",  datasourceClassName);
                        
                        KitchenSink ks = AMXRoot.getInstance().getDomainRoot().getKitchenSink();
                        Map<String, Object> result = ks.getConnectionDefinitionPropertiesAndDefaults(datasourceClassName);
                        Map propsMap = (Map) result.get(KitchenSink.PROPERTY_MAP_KEY);
                        if (propsMap == null){
                            //TODO use logger
                            System.out.println( "!!!!!! JdbcHandlers:updateJDBCPoolWizard(), error getting property map");
                            System.out.println(result.get(KitchenSink.REASON_FAILED_KEY));
                        }
                        handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolProperties", propsMap);
                    }
                    
		    extra.put("PreviousResType", resType);
                    extra.put("PreviousDB", dbVendor);
                    
                }
        }

    /**
     *	<p> This handler creates a JDBCConnection Pool in DomainConfig
     */
    @Handler(id="createJdbcConnectionPool")
    
    public static void createJdbcConnectionPool(HandlerContext handlerCtx){
        try{
            Map pool = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPool");
            Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
            Map properties = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolProperties");
            String name = (String) extra.get("name");
            String datasourceClassname = (String) extra.get("DatasourceClassname");
            
            String method = (String) pool.get("ConnectionValidationMethod");
            if ("table".equals(method)){
                String table = (String) pool.get("ValidationTableName");
                if (! GuiUtil.isEmpty(table))
                    pool.put ("ValidationTableName", table);
            }else{
                pool.remove("ValidationTableName");
            }
            
            
            //TODO
            //There seems to be a bug in Woodstock 4.2 that dropdown selected cannot use '-',   selected="#{wizardPool.transaction-isolation-level}"
            //so we have to use the camelCase and then change it back before passing to backend.
            
            Map allOptions = new HashMap();
           
            allOptions.put("res-type", pool.get("ResType"));
            allOptions.put("connection-validation-method", pool.get("ConnectionValidationMethod"));
            allOptions.put("transaction-isolation-level", pool.get("TransactionIsolationLevel"));
            allOptions.put("steady-pool-size", pool.get("SteadyPoolSize"));
            allOptions.put("max-pool-size", pool.get("MaxPoolSize"));
            allOptions.put("max-wait-time-in-millis", pool.get("MaxWaitTimeInMillis"));
            allOptions.put("pool-resize-quantity", pool.get("PoolResizeQuantity"));
            allOptions.put("idle-timeout-in-seconds", pool.get("IdleTimeoutInSeconds"));
            allOptions.put("is-isolation-level-guaranteed", pool.get("IsIsolationLevelGuaranteed"));
            allOptions.put("fail-all-connections", pool.get("FailAllConnections"));
            allOptions.put("non-transactional-connections", pool.get("NonTransactionalConnections"));
            allOptions.put("allow-non-component-callers", pool.get("AllowNonComponentCallers"));
            allOptions.put("is-connection-validation-required", pool.get("IsConnectionValidationRequired"));
            allOptions.put("validation-table-name", pool.get("ValidationTableName"));
            
            allOptions = AMXUtil.convertToPropertiesOptionMap(properties, allOptions);
            
            //System.out.println("!!!! calling getResourcesConfig().createJDBCConnectionPoolConfig  ");
            //System.out.println("name="+name);
            //System.out.println("datasoruceClassname="+datasourceClassname);
            //System.out.println("allOption="+ allOptions);
            JDBCConnectionPoolConfig newPool = AMXRoot.getInstance().getResourcesConfig().createJDBCConnectionPoolConfig(name, datasourceClassname, allOptions);
            newPool.setDescription((String) extra.get("Description"));
        }catch (Exception ex){
	    GuiUtil.handleException(handlerCtx, ex);
        }
    }
        
  /**
     *	<p> This handler returns the list of Connector Connection Pools
     *  <p> Output value: "result" -- Type: <code>java.util.List</code></p>
     */
    @Handler(id="getJDBCConnectionPoolMaps",
        input={
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
        )
    public static void getJDBCConnectionPoolMaps(HandlerContext handlerCtx) {
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
        List result = new ArrayList();
	Iterator iter = AMXRoot.getInstance().getResourcesConfig().getJDBCConnectionPoolConfigMap().values().iterator();
        if(iter != null ){
            while(iter.hasNext()){
                try{
                    JDBCConnectionPoolConfig res = (JDBCConnectionPoolConfig)iter.next();
                    HashMap oneRow = new HashMap();
                    oneRow.put("name", res.getName());
                    oneRow.put("selected", (hasOrig)? GuiUtil.isSelected(res.getName(), selectedList): false);
                    oneRow.put("resInfo", GuiUtil.checkEmpty(res.getResType()));
                    oneRow.put("extraInfo", res.getDatasourceClassname());
                    oneRow.put("description", GuiUtil.checkEmpty(res.getDescription()));
                    result.add(oneRow);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        handlerCtx.setOutputValue("result", result);
        
    }
    
    /**
     *	<p> This handler enable or disable the table text field according to the method value.
     */
    @Handler(id="setDisableConnectionPoolTableField",
        input={
            @HandlerInput(name="tableField", type=com.sun.webui.jsf.component.Field.class),
            @HandlerInput(name="methodValue", type=String.class)}
        )
    public static void setDisableConnectionPoolTableField(HandlerContext handlerCtx) {
        String methodValue = (String)handlerCtx.getInputValue("methodValue");
        Field tableField = (Field)handlerCtx.getInputValue("tableField");
        if("table".equals(methodValue)){
            tableField.setDisabled(false);
        }else
            tableField.setDisabled(true);
    }
    

        static private final String DATA_SOURCE = "javax.sql.DataSource";
	static private final String XADATA_SOURCE = "javax.sql.XADataSource";
	static private final String CCDATA_SOURCE = "javax.sql.ConnectionPoolDataSource";

	static private final String JAVADB = "JavaDB";
	static private final String ORACLE = "Oracle";
	static private final String DERBY = "Derby";
	static private final String SYBASE = "Sybase";
	static private final String DB2 = "DB2";
	static private final String POINTBASE = "PointBase";
	static private final String POSTGRESQL = "PostgreSQL";
	static private final String INFORMIX = "Informix";
	static private final String CLOUDSCAPE = "Cloudscape";
	static private final String MSSQL = "Microsoft SQL Server";
	static private final String MYSQL = "MySQL";
        static private Map<String, String> dataSourceMap = new HashMap();
        static private Map<String,String>  XADataSourceMap = new HashMap();
        static private Map<String,String> CCDataSourceMap = new HashMap();
        static private List resTypeList = new ArrayList();
        static private List dbVendorList = new ArrayList();
        
    static {
	
        dataSourceMap.put(ORACLE, "oracle.jdbc.pool.OracleDataSource");
	dataSourceMap.put(DERBY , "org.apache.derby.jdbc.ClientDataSource");
	dataSourceMap.put(JAVADB, "org.apache.derby.jdbc.ClientDataSource");
	dataSourceMap.put(DB2 , "com.ibm.db2.jdbc.DB2DataSource");
	dataSourceMap.put(MSSQL , "com.ddtek.jdbcx.sqlserver.SQLServerDataSource");
	dataSourceMap.put(SYBASE , "com.sybase.jdbc2.jdbc.SybDataSource");
	dataSourceMap.put(POINTBASE , "com.pointbase.jdbc.jdbcDataSource");
	dataSourceMap.put(CLOUDSCAPE , "com.cloudscape.core.BasicDataSource");
	dataSourceMap.put(INFORMIX , "com.informix.jdbcx.IfxDataSource");
	dataSourceMap.put(MYSQL , "com.mysql.jdbc.jdbc2.optional.MysqlDataSource" );
	dataSourceMap.put(POSTGRESQL , "org.postgresql.ds.PGSimpleDataSource");

	
	XADataSourceMap.put(ORACLE,  "oracle.jdbc.xa.client.OracleXADataSource");
	XADataSourceMap.put(DERBY,  "org.apache.derby.jdbc.ClientXADataSource");
	XADataSourceMap.put(JAVADB,  "org.apache.derby.jdbc.ClientXADataSource");
	XADataSourceMap.put(DB2,  "com.ibm.db2.jdbc.DB2XADataSource");
	XADataSourceMap.put(MSSQL,  "com.ddtek.jdbcx.sqlserver.SQLServerDataSource");
	XADataSourceMap.put(SYBASE,  "com.sybase.jdbc2.jdbc.SybXADataSource");
	XADataSourceMap.put(POINTBASE,  "com.pointbase.xa.xaDataSource");
	XADataSourceMap.put(CLOUDSCAPE,  "com.cloudscape.core.XADataSource");
	XADataSourceMap.put(INFORMIX,  "com.informix.jdbcx.IfxXADataSource");
	XADataSourceMap.put(MYSQL,  "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource" );
	XADataSourceMap.put(POSTGRESQL,  "org.postgresql.xa.PGXADataSource");
	
	CCDataSourceMap.put(ORACLE, "oracle.jdbc.pool.OracleConnectionPoolDataSource");
	CCDataSourceMap.put(DB2, "com.ibm.db2.jdbc.DB2ConnectionPoolDataSource");
	CCDataSourceMap.put(SYBASE, "com.sybase.jdbc2.jdbc.SybConnectionPoolDataSource");
	CCDataSourceMap.put(DERBY, "org.apache.derby.jdbc.ClientConnectionPoolDataSource");
	CCDataSourceMap.put(JAVADB, "org.apache.derby.jdbc.ClientConnectionPoolDataSource");
	CCDataSourceMap.put(POSTGRESQL, "org.postgresql.ds.PGConnectionPoolDataSource");
        CCDataSourceMap.put(MYSQL, "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource");
        
        resTypeList.add("");
        resTypeList.add(DATA_SOURCE);
        resTypeList.add(XADATA_SOURCE);
        resTypeList.add(CCDATA_SOURCE);
        
        dbVendorList.add("");
        dbVendorList.add(JAVADB);
        dbVendorList.add(ORACLE);
        dbVendorList.add(DERBY);
        dbVendorList.add(SYBASE);
        dbVendorList.add(DB2);
        dbVendorList.add(POINTBASE);
        dbVendorList.add(POSTGRESQL);
        dbVendorList.add(INFORMIX);
        dbVendorList.add(CLOUDSCAPE);
        dbVendorList.add(MSSQL);
        dbVendorList.add(MYSQL);
    }

}
        
 
