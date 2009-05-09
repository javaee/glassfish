
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
 * ConnectorHandlers.java
 *
 * Created on Sept 1, 2006, 8:32 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author anilam
 */
package org.glassfish.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.Properties;


import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.TargetUtil;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AdminObjectResourceConfig;
import com.sun.appserv.management.config.BackendPrincipalConfig;
import com.sun.appserv.management.config.ConnectorConnectionPoolConfig;
import com.sun.appserv.management.config.ConnectorResourceConfig;
import com.sun.appserv.management.config.JDBCResourceConfig;
import com.sun.appserv.management.config.JNDIResourceConfig;
import com.sun.appserv.management.config.CustomResourceConfig;
import com.sun.appserv.management.config.MailResourceConfig;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.SecurityMapConfig;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ResourcesConfig;
import com.sun.appserv.management.util.misc.GSetUtil;
import javax.management.Attribute;
import javax.management.AttributeList;

import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.model.Option;

import java.util.HashSet;
import javax.management.ObjectName;

public class ConnectorsHandlers {

    /** Creates a new instance of ConnectorsHandler */
    public ConnectorsHandlers() {
    }

    /**
     *	<p> This handler returns the values for all the attributes of the Connector Resource
     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
     *	<p> Output value: "jndiName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "poolName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getConnectorResourceInfo", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "edit", type = Boolean.class, required = true)
}, output = {
@HandlerOutput(name = "poolName", type = String.class),
@HandlerOutput(name = "description", type = String.class),
@HandlerOutput(name = "enabledString", type = String.class),
@HandlerOutput(name = "enabled", type = Boolean.class)
})
    public static void getConnectorResourceInfo(HandlerContext handlerCtx) {

        if (!(Boolean) handlerCtx.getInputValue("edit")) {
            handlerCtx.setOutputValue("enabled", Boolean.TRUE);
            return;
        }
        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        ConnectorResourceConfig resource = AMXRoot.getInstance().getResourcesConfig().getConnectorResourceConfigMap().get(jndiName);
        if (resource == null) {
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorResource"));
            return;
        }

        handlerCtx.setOutputValue("poolName", resource.getPoolName());
        handlerCtx.setOutputValue("description", resource.getDescription());
        if (AMXRoot.getInstance().isEE()) {
            handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(resource, false));
        } else {
            handlerCtx.setOutputValue("enabled", TargetUtil.isResourceEnabled(resource, "server"));
        }

    }

    /**
     *	<p> This handler returns the values for all the attributes of the Connector Resource
     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "edit" -- Type: <code> java.lang.Boolean</code></p>
     *	<p> Output value: "jndiName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "poolName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "saveConnectorResource", input = {
@HandlerInput(name = "edit", type = Boolean.class, required = true),
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "poolName", type = String.class, required = true),
@HandlerInput(name = "description", type = String.class),
@HandlerInput(name = "enabled", type = Boolean.class),
@HandlerInput(name = "targets", type = String[].class)
})
    public static void saveConnectorResource(HandlerContext handlerCtx) {

        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        String poolName = (String) handlerCtx.getInputValue("poolName");
        ConnectorResourceConfig resource = null;
        try {
            if (!(Boolean) handlerCtx.getInputValue("edit")) {
                resource = AMXRoot.getInstance().getResourcesConfig().createConnectorResourceConfig(jndiName, poolName, null);
                //Work around for bug#6519377.  It automatically creates a <resource-ref> for "server"
                if (AMXRoot.getInstance().isEE()) {
                    if (TargetUtil.getResourceRef(jndiName, "server") != null) {
                        TargetUtil.removeResourceRef(jndiName, "server");
                    }
                /* TODO-V3
                JavaMailHandlers.createNewTargets(handlerCtx, jndiName);
                 */
                } else {
                    Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
                    TargetUtil.setResourceEnabled(resource, "server", enabled);
                }
            //End of workaround.
                /*  Original code, should just call this:
             *  JavaMailHandlers.createNewTargets(handlerCtx, jndiName);
             */
            } else {
                GuiUtil.prepareSuccessful(handlerCtx);
                resource = AMXRoot.getInstance().getResourcesConfig().getConnectorResourceConfigMap().get(jndiName);
                if (resource == null) {
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoConnectResource"));
                }
                resource.setPoolName(poolName);
                if (!AMXRoot.getInstance().isEE()) {
                    Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
                    TargetUtil.setResourceEnabled(resource, "server", enabled);
                }
            }
            resource.setDescription((String) handlerCtx.getInputValue("description"));

        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns the list of Connector Connection Pools
     *  <p> Output value: "connectionPoolNames" -- Type: <code>java.util.List</code></p>
     */
    @Handler(id = "getConnectorConnectionPools", output = {
@HandlerOutput(name = "connectorConnectionPools", type = java.util.List.class)
})
    public static void getConnectorConnectionPools(HandlerContext handlerCtx) {
        Set keys = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().keySet();
        handlerCtx.setOutputValue("connectorConnectionPools", new ArrayList(keys));
    }

    /**
     *	<p> This handler returns the list of Connector Connection Pools
     *  <p> Output value: "result" -- Type: <code>java.util.List</code></p>
     */
    @Handler(id = "getConnectorConnectionPoolMaps", input = {
@HandlerInput(name = "selectedRows", type = List.class)
}, output = {
@HandlerOutput(name = "result", type = java.util.List.class)
})
    public static void getConnectorConnectionPoolMaps(HandlerContext handlerCtx) {

        List<Map> selectedList = (List) handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size() == 0) ? false : true;
        List result = new ArrayList();
        try {
            Iterator iter = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().values().iterator();
            if (iter != null) {
                while (iter.hasNext()) {
                    ConnectorConnectionPoolConfig res = (ConnectorConnectionPoolConfig) iter.next();
                    HashMap oneRow = new HashMap();
                    oneRow.put("name", res.getName());
                    oneRow.put("selected", (hasOrig) ? GuiUtil.isSelected(res.getName(), selectedList) : false);
                    oneRow.put("resInfo", res.getResourceAdapterName());
                    oneRow.put("extraInfo", res.getConnectionDefinitionName());
                    oneRow.put("description", GuiUtil.checkEmpty(res.getDescription()));
                    result.add(oneRow);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        handlerCtx.setOutputValue("result", result);
    }

    /*******  Connector Connection Pools  *************/
    /**
     *	<p> This handler returns the values for all the attributes of the Connector Connection Pool
     */
    @Handler(id = "getConnectorConnectionPoolInfo", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true)
}, output = {
@HandlerOutput(name = "resourceAdapterName", type = String.class),
@HandlerOutput(name = "connectionDefinitionName", type = String.class),
@HandlerOutput(name = "description", type = String.class),
@HandlerOutput(name = "steadyPoolSize", type = String.class),
@HandlerOutput(name = "maxPoolSize", type = String.class),
@HandlerOutput(name = "poolResizeQuantity", type = String.class),
@HandlerOutput(name = "idleTimeoutInSeconds", type = String.class),
@HandlerOutput(name = "maxWaitTimeInMillis", type = String.class),
@HandlerOutput(name = "failAllConnections", type = Boolean.class),
@HandlerOutput(name = "transactionSupport", type = String.class),
@HandlerOutput(name = "isConnectionValidationRequired", type = Boolean.class)
})
    public static void getConnectorConnectionPoolInfo(HandlerContext handlerCtx) {

        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
        if (pool == null) {
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorConnectionPool"));
            return;
        }
        handlerCtx.setOutputValue("resourceAdapterName", pool.getResourceAdapterName());
        handlerCtx.setOutputValue("connectionDefinitionName", pool.getConnectionDefinitionName());
        handlerCtx.setOutputValue("description", pool.getDescription());
        handlerCtx.setOutputValue("steadyPoolSize", pool.getSteadyPoolSize());
        handlerCtx.setOutputValue("maxPoolSize", pool.getMaxPoolSize());
        handlerCtx.setOutputValue("poolResizeQuantity", pool.getPoolResizeQuantity());
        handlerCtx.setOutputValue("idleTimeoutInSeconds", pool.getIdleTimeoutInSeconds());
        handlerCtx.setOutputValue("maxWaitTimeInMillis", pool.getMaxWaitTimeInMillis());
        handlerCtx.setOutputValue("isConnectionValidationRequired", pool.getConnectionValidationRequired());
        handlerCtx.setOutputValue("failAllConnections", pool.getFailAllConnections());
        handlerCtx.setOutputValue("transactionSupport", pool.getTransactionSupport());
    }

    /**
     *	<p> This handler returns the values for all the attributes of the Connector Connection Pool
     */
    @Handler(id = "getConnectorConnectionPoolProperty", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true)
}, output = {
@HandlerOutput(name = "properties", type = java.util.Map.class)
})
    public static void getConnectorConnectionPoolProperty(HandlerContext handlerCtx) {

        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
        if (pool == null) {
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorConnectionPool"));
            return;
        }
        handlerCtx.setOutputValue("properties", pool.getPropertyConfigMap());
    }

    /**
     *	<p> This handler saves the values for all the attributes of the Connector Connection Pool
     */
    @Handler(id = "saveConnectorConnectionPool", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "jmsFactory", type = Boolean.class),
@HandlerInput(name = "description", type = String.class),
@HandlerInput(name = "steadyPoolSize", type = String.class),
@HandlerInput(name = "maxPoolSize", type = String.class),
@HandlerInput(name = "poolResizeQuantity", type = String.class),
@HandlerInput(name = "idleTimeoutInSeconds", type = String.class),
@HandlerInput(name = "maxWaitTimeInMillis", type = String.class),
@HandlerInput(name = "isConnectionValidationRequired", type = String.class),
@HandlerInput(name = "failAllConnections", type = String.class),
@HandlerInput(name = "isIsolationLevelGuaranteed", type = Boolean.class),
@HandlerInput(name = "transactionSupport", type = String.class)
})
    public static void saveConnectorConnectionPool(HandlerContext handlerCtx) {

        try {
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
            if (pool == null) {
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorConnectionPool"));
                return;
            }
            //For JMS connection Factory Edit, we don't save the description.
            //description for JMS connectionFacatory applies to connector resource
            Boolean jmsFactory = ((Boolean) handlerCtx.getInputValue("jmsFactory"));
            if (jmsFactory == null || jmsFactory.booleanValue() == false) {
                pool.setDescription((String) handlerCtx.getInputValue("description"));
            }
            pool.setMaxPoolSize((String) handlerCtx.getInputValue("maxPoolSize"));
            pool.setSteadyPoolSize((String) handlerCtx.getInputValue("steadyPoolSize"));
            pool.setPoolResizeQuantity((String) handlerCtx.getInputValue("poolResizeQuantity"));
            pool.setIdleTimeoutInSeconds((String) handlerCtx.getInputValue("idleTimeoutInSeconds"));
            pool.setMaxWaitTimeInMillis((String) handlerCtx.getInputValue("maxWaitTimeInMillis"));
            pool.setConnectionValidationRequired((String) handlerCtx.getInputValue("isConnectionValidationRequired"));
            pool.setTransactionSupport((String) handlerCtx.getInputValue("transactionSupport"));
            pool.setFailAllConnections((String) handlerCtx.getInputValue("failAllConnections"));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler saves the properties of the Connector Connection Pool
     */
    @Handler(id = "saveConnectorConnectionPoolProperty", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "AddProps", type = Map.class),
@HandlerInput(name = "RemoveProps", type = ArrayList.class)
})
    public static void saveConnectorConnectionPoolProperty(HandlerContext handlerCtx) {

        try {
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
            if (pool == null) {
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorConnectionPool"));
                return;
            }
            AMXRoot.getInstance().editProperties(handlerCtx, pool);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns the values for all the attributes of the Connector Connection Pool
     */
    @Handler(id = "getConnectorConnectionPoolDefaultInfo", output = {
@HandlerOutput(name = "steadyPoolSize", type = String.class),
@HandlerOutput(name = "maxPoolSize", type = String.class),
@HandlerOutput(name = "poolResizeQuantity", type = String.class),
@HandlerOutput(name = "idleTimeoutInSeconds", type = String.class),
@HandlerOutput(name = "maxWaitTimeInMillis", type = String.class),
@HandlerOutput(name = "isConnectionValidationRequired", type = Boolean.class),
@HandlerOutput(name = "failAllConnections", type = Boolean.class),
@HandlerOutput(name = "transactionSupport", type = String.class)
})
    public static void getConnectorConnectionPoolDefaultInfo(HandlerContext handlerCtx) {
        Map defaultMap = AMXRoot.getInstance().getResourcesConfig().getDefaultValues(XTypes.CONNECTOR_CONNECTION_POOL_CONFIG, true);
        handlerCtx.setOutputValue("steadyPoolSize", defaultMap.get("SteadyPoolSize"));
        handlerCtx.setOutputValue("maxPoolSize", defaultMap.get("MaxPoolSize"));
        handlerCtx.setOutputValue("poolResizeQuantity", defaultMap.get("PoolResizeQuantity"));
        handlerCtx.setOutputValue("idleTimeoutInSeconds", defaultMap.get("IdleTimeoutInSeconds"));
        handlerCtx.setOutputValue("maxWaitTimeInMillis", defaultMap.get("MaxWaitTimeInMillis"));
        handlerCtx.setOutputValue("isConnectionValidationRequired", defaultMap.get("IsConnectionValidationRequired"));
        handlerCtx.setOutputValue("failAllConnections", defaultMap.get("FailAllConnections"));
        handlerCtx.setOutputValue("transactionSupport", defaultMap.get("TransactionSupport"));
    }

    /**
     *	<p> This handler returns the values for all the attributes of the Connector Connection Pool
     */
    @Handler(id = "getConnectorPoolAdvanceInfo", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true)
}, output = {
@HandlerOutput(name = "advance", type = Map.class)
})
    public static void getConnectorPoolAdvanceInfo(HandlerContext handlerCtx) {

        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
        if (pool == null) {
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchConnectorConnectionPool"));
            return;
        }
        Map advance = new HashMap();

        advance.put("validateAtMostOncePeriodInSeconds", pool.getValidateAtMostOncePeriodInSeconds());
        advance.put("connectionLeakTimeoutInSeconds", pool.getConnectionLeakTimeoutInSeconds());
        advance.put("connectionLeakReclaim", StringToBoolean(pool.getConnectionLeakReclaim()));
        advance.put("connectionCreationRetryAttempts", pool.getConnectionCreationRetryAttempts());
        advance.put("connectionCreationRetryIntervalInSeconds", pool.getConnectionCreationRetryIntervalInSeconds());
        advance.put("lazyConnectionEnlistment", StringToBoolean(pool.getLazyConnectionEnlistment()));
        advance.put("lazyConnectionAssociation", StringToBoolean(pool.getLazyConnectionAssociation()));
        advance.put("associateWithThread", StringToBoolean(pool.getAssociateWithThread()));
        advance.put("matchConnections", StringToBoolean(pool.getMatchConnections()));
        advance.put("maxConnectionUsageCount", pool.getMaxConnectionUsageCount());
        handlerCtx.setOutputValue("advance", advance);
    }

    /**
     *	<p> This handler returns the default values for the advance attributes of the Connector Connection Pool
     */
    @Handler(id = "getConnectorPoolAdvanceDefaultInfo", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true)
}, output = {
@HandlerOutput(name = "advance", type = Map.class)
})
    public static void getConnectorPoolAdvanceDefaultInfo(HandlerContext handlerCtx) {

        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
        if (pool == null) {
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchConnectorConnectionPool"));
            return;
        }
        Map advance = new HashMap();
        // TODO-V3
        //Map defaultMap = AMXRoot.getInstance().getDefaultAttributeValues(ConnectorConnectionPoolConfig.J2EE_TYPE);
        Map defaultMap = new HashMap();
        advance.put("validateAtMostOncePeriodInSeconds", defaultMap.get("validate-atmost-once-period-in-seconds"));
        advance.put("connectionLeakTimeoutInSeconds", defaultMap.get("connection-leak-timeout-in-seconds"));
        advance.put("connectionLeakReclaim", StringToBoolean( defaultMap.get("connection-leak-reclaim")));
        advance.put("connectionCreationRetryAttempts", defaultMap.get("connection-creation-retry-attempts"));
        advance.put("connectionCreationRetryIntervalInSeconds", defaultMap.get("connection-creation-retry-interval-in-seconds"));
        advance.put("lazyConnectionEnlistment",  StringToBoolean( defaultMap.get("lazy-connection-enlistment")));
        advance.put("lazyConnectionAssociation",  StringToBoolean( defaultMap.get("lazy-connection-association")));
        advance.put("associateWithThread",  StringToBoolean( defaultMap.get("associate-with-thread")));
        advance.put("matchConnections",  StringToBoolean( defaultMap.get("match-connections")));
        advance.put("maxConnectionUsageCount", defaultMap.get("max-connection-usage-count"));
        handlerCtx.setOutputValue("advance", advance);
    }

    /**
     *	<p> This handler saves the advance attributes of the Connector Connection Pool
     */
    @Handler(id = "saveConnectorPoolAdvanceInfo", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "advance", type = Map.class)
})
    public static void saveConnectorPoolAdvanceInfo(HandlerContext handlerCtx) {
        try {
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            Map advance = (Map) handlerCtx.getInputValue("advance");
        /* TODO-V3
        ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getConnectorConnectionPoolConfigMap().get(jndiName);
        if (pool == null){
        GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchConnectorConnectionPool"));
        return;
        }
        //uncomment the following with issue#1638 is fixed.
        pool.setValidateAtMostOncePeriodInSeconds((String) advance.get("validateAtMostOncePeriodInSeconds"));
        pool.setConnectionLeakTimeoutInSeconds((String) advance.get("connectionLeakTimeoutInSeconds"));
        pool.setConnectionLeakReclaim( BooleanToString(advance.get("connectionLeakReclaim")));
        pool.setConnectionCreationRetryAttempts((String) advance.get("connectionCreationRetryAttempts"));
        pool.setConnectionCreationRetryIntervalInSeconds((String) advance.get("connectionCreationRetryIntervalInSeconds"));
        pool.setLazyConnectionEnlistment(BooleanToString( advance.get("lazyConnectionEnlistment")));
        pool.setLazyConnectionAssociation(BooleanToString( advance.get("lazyConnectionAssociation")));
        pool.setAssociateWithThread(BooleanToString( advance.get("associateWithThread")));
        pool.setMatchConnections(BooleanToString( advance.get("matchConnections")));
        pool.setMaxConnectionUsageCount((String) advance.get("maxConnectionUsageCount"));
         */
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    private static String BooleanToString(Object test) {
        if (test == null) {
            return Boolean.FALSE.toString();
        }
        return test.toString();
    }

    private static Boolean StringToBoolean(Object test) {
        if (test == null) {
            return false;
        }
        if (test instanceof String) {
            return Boolean.valueOf((String) test);
        } else if (test instanceof Boolean) {
            return (Boolean) test;
        }
        return false;
    }

    /**
     *	<p> This handler creates a ConnectorConnection Pool to be used in the wizard
     */
    @Handler(id = "getConnectorConnectionPoolWizard", input = {
@HandlerInput(name = "fromStep2", type = Boolean.class),
@HandlerInput(name = "fromStep1", type = Boolean.class),
@HandlerInput(name = "poolName", type = String.class),
@HandlerInput(name = "resAdapter", type = String.class)
}, output = {
@HandlerOutput(name = "connectionDefinitions", type = List.class)
})
    public static void getConnectorConnectionPoolWizard(HandlerContext handlerCtx) {
        //We need to use 2 maps for Connector Connection Pool creation because there are extra info we need to keep track in 
        //the wizard, but cannot be passed to the creation API.

        Boolean fromStep2 = (Boolean) handlerCtx.getInputValue("fromStep2");
        Boolean fromStep1 = (Boolean) handlerCtx.getInputValue("fromStep1");
        if ((fromStep2 != null) && fromStep2) {
            //wizardPool is already in session map, we don't want to change anything.
            Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
            String resAdapter = (String) extra.get("resAdapter");
            List defs = getConnectionDefinitions(resAdapter);
            handlerCtx.setOutputValue("connectionDefinitions", defs);
        } else if ((fromStep1 != null) && fromStep1) {
            //this is from Step 1 where the page is navigated when changing the dropdown of resource adapter.
            //since the dropdown is immediate, the wizardPoolExtra map is not updated yet, we need
            //to update it manually and also set the connection definition map according to this resource adapter.
            String resAdapter = (String) handlerCtx.getInputValue("resAdapter");
            String poolName = (String) handlerCtx.getInputValue("poolName");
            if (GuiUtil.isEmpty(resAdapter)) {
                handlerCtx.setOutputValue("connectionDefinitions", new ArrayList());
            } else {
                Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
                extra.put("resAdapter", resAdapter);
                extra.put("name", poolName);
                List defs = getConnectionDefinitions(resAdapter);
                handlerCtx.setOutputValue("connectionDefinitions", defs);
            }
        } else {
            /* TODO-V3
            Map defaultMap = AMXRoot.getInstance().getDefaultAttributeValues(ConnectorConnectionPoolConfig.J2EE_TYPE);
            Map attrMap = new HashMap();
            attrMap.put("SteadyPoolSize", defaultMap.get("steady-pool-size"));
            attrMap.put("MaxPoolSize", defaultMap.get("max-pool-size"));
            attrMap.put("PoolResizeQuantity", defaultMap.get("pool-resize-quantity"));
            attrMap.put("IdleTimeoutInSeconds", defaultMap.get("idle-timeout-in-seconds"));
            attrMap.put("FailAllConnections", defaultMap.get("fail-all-connections"));
            attrMap.put("TransactionSupport", defaultMap.get("transaction-support"));
            attrMap.put("IsConnectionValidationRequired", defaultMap.get("is-connection-validation-required"));
            attrMap.put("MaxWaitTimeInMillis", defaultMap.get("max-wait-time-in-millis"));
             */

            Map extra = new HashMap();

            //handlerCtx.setOutputValue("wizardPool", attrMap);
            //handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPool", attrMap);
            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolExtra", extra);
            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolProperties", extra);
        }
    }

    /**
     *	<p> updates the wizard map
     */
    @Handler(id = "updateConnectorConnectionPoolWizard")
    public static void updateConnectorConnectionPoolWizard(HandlerContext handlerCtx) {
        Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");

        String resAdapter = (String) extra.get("resAdapter");
        String definition = (String) extra.get("connectionDefinition");

        String previousDefinition = (String) extra.get("previousDefinition");
        String previousResAdapter = (String) extra.get("previousResAdapter");

        if (definition.equals(previousDefinition) && resAdapter.equals(previousResAdapter)) {
        //User didn't change defintion and adapter, keep the properties table content the same.
        } else {
            if (!GuiUtil.isEmpty(definition) && !GuiUtil.isEmpty(resAdapter)) {
                Properties props = getConnectorConnectionPoolProps("getMCFConfigProps", resAdapter, "connection-definition-name", definition);
                handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolProperties", props);
            }
            extra.put("previousDefinition", definition);
            extra.put("previousResAdapter", resAdapter);
        }
    }

    /**
     *	<p> This handler creates a ConnectorConnection Pool in DomainConfig
     */
    @Handler(id = "createConnectorConnectionPool")
    public static void createConnectorConnectionPool(HandlerContext handlerCtx) {
        try {
            Map pool = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPool");
            Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
            Map propsMap = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolProperties");
            String name = (String) extra.get("name");
            String resAdapter = (String) extra.get("resAdapter");
            String connectionDef = (String) extra.get("connectionDefinition");

            Map allOptions = new HashMap(pool);
            allOptions = AMXUtil.convertToPropertiesOptionMap(propsMap, allOptions);

            ConnectorConnectionPoolConfig newPool = AMXRoot.getInstance().getResourcesConfig().createConnectorConnectionPoolConfig(name, resAdapter, connectionDef, allOptions);
            newPool.setDescription((String) extra.get("Description"));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    private static List getConnectionDefinitions(String resAdapter) {
        ArrayList defs = new ArrayList();
        if (GuiUtil.isEmpty(resAdapter)) {
            return defs;
        }
        /* TODO-V3
        Object[] params = {resAdapter};
        String[] types = {"java.lang.String"};
        String[] connectionDefinitions = (String[])JMXUtil.invoke(
        "com.sun.appserv:type=resources,category=config",
        "getConnectionDefinitionNames", params, types );
        if (connectionDefinitions != null){
        for(int i=0; i< connectionDefinitions.length; i++){
        defs.add(connectionDefinitions[i]);
        }
        }
         */
        return defs;
    }

    // Connector Connection Pool Security Maps
    /**
     *	<p> This handler returns the list of Security Maps of Connector Connection Pools
     *  <p> Output value: "result" -- Type: <code>java.util.List</code></p>
     */
    @Handler(id = "getConnectorSecurityMaps", input = {
@HandlerInput(name = "jndiName", type = String.class)
}, output = {
@HandlerOutput(name = "result", type = java.util.List.class)
})
    public static void getConnectorSecurityMaps(HandlerContext handlerCtx) {

        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
        if (pool == null) {
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorConnectionPool"));
            return;
        }
        Map<String, SecurityMapConfig> securityMaps = pool.getSecurityMapConfigMap();
        List result = new ArrayList();
        for (String securityMapName : securityMaps.keySet()) {
            HashMap oneRow = new HashMap();
            oneRow.put("name", securityMapName);
            oneRow.put("editLink", "/resourceNode/connectorSecurityMapEdit.jsf?poolName=" + jndiName + "&securityMapName=" + securityMapName);
            oneRow.put("selected", false);
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
    }

    /**
     *	<p> This handler takes in selected rows, and change the status of the app
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
     *  <p> Input  value: "isJmsConnectionFactory" -- Type: <code>Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "deleteConnectorSecurityMaps", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "selectedRows", type = List.class, required = true)
})
    public static void deleteConnectorSecurityMaps(HandlerContext handlerCtx) {

        String jndiName = (String) handlerCtx.getInputValue("jndiName");
    /* TODO-V3
    ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getConnectorConnectionPoolConfigMap().get(jndiName);
     * 
    if (pool == null){
    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorConnectionPool"));
    return;
    }
    List obj = (List) handlerCtx.getInputValue("selectedRows");
    List<Map> selectedRows = (List) obj;
    try{
    for(Map oneRow : selectedRows){
    pool.removeSecurityMapConfig((String)oneRow.get("name"));
    }
    }catch(Exception ex){
    GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"),ex.getMessage());
    }
     */
    }

    /**
     *	<p> This handler returns the info about the Security Maps of Connector Connection Pools
     */
    @Handler(id = "getConnectorSecurityMapInfo", input = {
@HandlerInput(name = "poolName", type = String.class),
@HandlerInput(name = "securityMapName", type = String.class)
}, output = {
@HandlerOutput(name = "userGroups", type = String.class),
@HandlerOutput(name = "principals", type = String.class),
@HandlerOutput(name = "userName", type = String.class),
@HandlerOutput(name = "password", type = String.class),
@HandlerOutput(name = "hasUserGroups", type = Boolean.class)
})
    public static void getConnectorSecurityMapInfo(HandlerContext handlerCtx) {

        String poolName = (String) handlerCtx.getInputValue("poolName");
        ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(poolName);
        if (pool == null) {
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorConnectionPool"));
            return;
        }
        String securityMapName = (String) handlerCtx.getInputValue("securityMapName");
        SecurityMapConfig securityMap = pool.getSecurityMapConfigMap().get(securityMapName);

        String[] groups = securityMap.getUserGroupNames();
        if (groups != null && groups.length > 0) {
            String userGroups = groups[0];
            for (int i = 1; i < groups.length; i++) {
                userGroups = userGroups.concat("," + groups[i]);
            }
            handlerCtx.setOutputValue("userGroups", userGroups);
            handlerCtx.setOutputValue("hasUserGroups", true);
        } else {
            handlerCtx.setOutputValue("hasUserGroups", false);
            handlerCtx.setOutputValue("userGroups", "");
        }

        String[] principalNames = securityMap.getPrincipalNames();
        if (principalNames != null && principalNames.length > 0) {
            String principals = principalNames[0];
            for (int i = 1; i < principalNames.length; i++) {
                principals = principals.concat("," + principalNames[i]);
            }
            handlerCtx.setOutputValue("principals", principals);
        } else {
            handlerCtx.setOutputValue("hasUserGroups", true);
            handlerCtx.setOutputValue("principals", "");
        }

        BackendPrincipalConfig bpc = securityMap.getBackendPrincipalConfig();
        if (bpc != null) {
            handlerCtx.setOutputValue("userName", bpc.getUserName());
            handlerCtx.setOutputValue("password", bpc.getPassword());
        }
    }

    /**
     *	<p> This handler returns the info about the Security Maps of Connector Connection Pools
     */
    @Handler(id = "saveConnectorSecurityMap", input = {
@HandlerInput(name = "poolName", type = String.class),
@HandlerInput(name = "securityMapName", type = String.class),
@HandlerInput(name = "usersOption", type = String.class),
@HandlerInput(name = "userGroups", type = String.class),
@HandlerInput(name = "principals", type = String.class),
@HandlerInput(name = "userName", type = String.class),
@HandlerInput(name = "password", type = String.class),
@HandlerInput(name = "edit", type = Boolean.class)
})
    public static void saveConnectorSecurityMap(HandlerContext handlerCtx) {

        String poolName = (String) handlerCtx.getInputValue("poolName");
        ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(poolName);
        if (pool == null) {
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchConnectorConnectionPool"));
            return;
        }
        String securityMapName = (String) handlerCtx.getInputValue("securityMapName");
        String userName = (String) handlerCtx.getInputValue("userName");
        String password = (String) handlerCtx.getInputValue("password");
        String option = (String) handlerCtx.getInputValue("usersOption");
        String userGroups = (String) handlerCtx.getInputValue("userGroups");
        String principals = (String) handlerCtx.getInputValue("principals");
        String value = null;
        String[] str = null;
        boolean usePrincipals = false;
        //Take either userGroups or Principals
        if (option.equals("users")) {
            value = userGroups;
            usePrincipals = false;
        } else {
            value = principals;
            usePrincipals = true;
        }
        /* boolean usePrincipals = false;
        if (GuiUtil.isEmpty(userGroups)){
        value = principals;
        usePrincipals = true;
        }else{
        value = userGroups;
        usePrincipals = false;
        }
         */
        if (value != null && value.indexOf(",") != -1) {
            str = GuiUtil.stringToArray(value, ",");
        } else {
            str = new String[1];
            str[0] = value;
        }
        try {
            if (!(Boolean) handlerCtx.getInputValue("edit")) {
                pool.createSecurityMapConfig(securityMapName, userName, password,
                        (usePrincipals) ? str : null, (usePrincipals) ? null : str);
                return;
            }

            SecurityMapConfig securityMap = pool.getSecurityMapConfigMap().get(securityMapName);

            //Remove all the old user groups and principals and add them back
            String[] oldGroups = securityMap.getUserGroupNames();
            if (oldGroups != null && oldGroups.length > 0) {
                for (int i = 0; i < oldGroups.length; i++) {
                    securityMap.removeUserGroup(oldGroups[i]);
                }
            }

            String[] oldPrincipals = securityMap.getPrincipalNames();
            if (oldPrincipals != null && oldPrincipals.length > 0) {
                for (int i = 0; i < oldPrincipals.length; i++) {
                    securityMap.removePrincipal(oldPrincipals[i]);
                }
            }

            if (usePrincipals) {
                for (int i = 0; i < str.length; i++) {
                    securityMap.createPrincipal(str[i]);
                }
            } else {
                for (int i = 0; i < str.length; i++) {
                    securityMap.createUserGroup(str[i]);
                }
            }

            BackendPrincipalConfig bpc = securityMap.getBackendPrincipalConfig();
            if (bpc != null) {
                bpc.setUserName(userName);
                bpc.setPassword(password);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }

    }

    /*******  Admin Object Resource *************/
    /**
     *	<p> This handler returns the values for all the attributes of the Admin Object Resource
     *	<p> Input value: "jndiName" -- Type: <code>java.lang.String</code></p>
     **	<p> Input value: "edit" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "resType" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "resAdapter" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getAdminObjectResourceInfo", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "edit", type = Boolean.class, required = true)
}, output = {
@HandlerOutput(name = "resType", type = String.class),
@HandlerOutput(name = "resAdapter", type = String.class),
@HandlerOutput(name = "description", type = String.class),
@HandlerOutput(name = "enabledString", type = String.class),
@HandlerOutput(name = "enabled", type = Boolean.class),
@HandlerOutput(name = "properties", type = java.util.Map.class)
})
    public static void getAdminObjectResourceInfo(HandlerContext handlerCtx) {

        if (!(Boolean) handlerCtx.getInputValue("edit")) {
            handlerCtx.setOutputValue("enabled", Boolean.TRUE);
            handlerCtx.setOutputValue("resAdapter", "");
            return;
        }
        String jndiName = (String) handlerCtx.getInputValue("jndiName");
    /* TODO-V3
    AdminObjectResourceConfig resource = AMXRoot.getInstance().getAdminObjectResourceConfigMap().get(jndiName);
    if (resource == null){
    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchAdminObjectResource"));
    return;
    }
    handlerCtx.setOutputValue("resType", resource.getResType());
    handlerCtx.setOutputValue("resAdapter", resource.getResAdapter());
    handlerCtx.setOutputValue("description", resource.getDescription());
    if(AMXRoot.getInstance().isEE()))
    handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(resource, false));
    else
    handlerCtx.setOutputValue("enabled", TargetUtil.isResourceEnabled(resource, "server" ));
    handlerCtx.setOutputValue("properties", resource.getPropertyConfigMap());
     */
    }

    /**
     *	<p> This handler saves the attributes of the AdminObject Resource
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "saveAdminObjectResource", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "edit", type = Boolean.class, required = true),
@HandlerInput(name = "resType", type = String.class, required = true),
@HandlerInput(name = "resAdapter", type = String.class, required = true),
@HandlerInput(name = "description", type = String.class),
@HandlerInput(name = "enabled", type = Boolean.class),
@HandlerInput(name = "AddProps", type = Map.class),
@HandlerInput(name = "RemoveProps", type = ArrayList.class)
})
    public static void saveAdminObjectResource(HandlerContext handlerCtx) {
        try {
            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            AdminObjectResourceConfig resource = AMXRoot.getInstance().getResourcesConfig().getAdminObjectResourceConfigMap().get(jndiName);
            if (resource == null) {
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchAdminObjectResource"));
                return;
            }
            resource.setResType((String) handlerCtx.getInputValue("resType"));
            resource.setResAdapter((String) handlerCtx.getInputValue("resAdapter"));
            resource.setDescription((String) handlerCtx.getInputValue("description"));
            if (!AMXRoot.getInstance().isEE()) {
                Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
                TargetUtil.setResourceEnabled(resource, "server", enabled);
            }
            AMXRoot.getInstance().editProperties(handlerCtx, resource);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns the list of Resource Adapter 
     *  <p> Output value: "poolNames" -- Type: <code>java.util.List</code></p>
     */
    @Handler(id = "getResourceAdapter", input = {
@HandlerInput(name = "forAdminObject", type = Boolean.class, required = true)
}, output = {
@HandlerOutput(name = "resAdapters", type = java.util.List.class)
})
    public static void getResourceAdapter(HandlerContext handlerCtx) {

        //List of deployed connectors
        //TODO-V3
        //Set keys = AMXRoot.getInstance().getResourcesConfig().getRARModuleConfigMap().keySet();
        Set keys = new HashSet();
        ArrayList total = new ArrayList(keys);
        total.add(0, "");

        //only jmsra is allowed for admin object, refer to bug#6477306
        Boolean forAdminObject = (Boolean) handlerCtx.getInputValue("forAdminObject");
        if (forAdminObject) {
            total.add("jmsra");
        } else {
        //List of system connectors
        /* TODO-V3
        String[] systemConnectors = 
        (String[]) JMXUtil.invoke("com.sun.appserv:type=resources,category=config", 
        "getSystemConnectorsAllowingPoolCreation", null, null);
        if (systemConnectors != null){
        for (int i=0; i< systemConnectors.length; i++)
        total.add(systemConnectors[i]);
        }
         */
        }
        String[] embeddedConnectors = null;
        /* TODO-V3
        //List of embedded Connectors
        String[] types = new String[]{"java.lang.String", "java.lang.String"};
        Object[] params = new Object[]{null, "domain" }; 
        String[] embeddedConnectors = (String[]) JMXUtil.invoke(
        "com.sun.appserv:type=applications,category=config", 
        "getEmbeddedConnectorNames", params, types);
         */

        if (embeddedConnectors != null) {
            for (int i = 0; i < embeddedConnectors.length; i++) {
                total.add(embeddedConnectors[i]);
            }
        }

        handlerCtx.setOutputValue("resAdapters", total);
    }

    /**
     *	<p> This handler returns the list of Resource Adapter 
     *  <p> Output value: "poolNames" -- Type: <code>java.util.List</code></p>
     */
    @Handler(id = "setConnectionDefDropdown", input = {
@HandlerInput(name = "dropDownComponent", type = com.sun.webui.jsf.component.DropDown.class, required = true),
@HandlerInput(name = "poolMap", type = java.util.Map.class)
})
    public static void setConnectionDefDropdown(HandlerContext handlerCtx) {
        DropDown dp = (DropDown) handlerCtx.getInputValue("dropDownComponent");
        Map poolMap = (Map) handlerCtx.getInputValue("poolMap");
        String resAdapter = (String) poolMap.get("resAdapter");
        if (GuiUtil.isEmpty(resAdapter)) {
            return;
        }
        String[] connectionDefinitions = null;
        /* TODO-V3
        Object[] params = {resAdapter};
        String[] types = {"java.lang.String"};
        String[] connectionDefinitions = (String[])JMXUtil.invoke(
        "com.sun.appserv:type=resources,category=config",
        "getConnectionDefinitionNames", params, types );
         */
        if (connectionDefinitions == null) {
            return;
        }
        ArrayList list = new ArrayList();
        for (int i = 0; i < connectionDefinitions.length; i++) {
            list.add(new Option(connectionDefinitions[i], connectionDefinitions[i]));
        }
        dp.setItems(list);
    }

    /**
     *	<p> This handler creates a Map be used in the wizard for creating Admin Object Resource
     */
    @Handler(id = "getAdminObjectWizard", input = {
@HandlerInput(name = "fromStep2", type = Boolean.class)
})
    public static void getAdminObjectWizard(HandlerContext handlerCtx) {

        Boolean fromStep2 = (Boolean) handlerCtx.getInputValue("fromStep2");
        Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
        if ((fromStep2 != null) && fromStep2 && (sessionMap.get("wizardPool") != null)) {
        //wizardPool is and should be already in session map
        } else {
            Map pool = new HashMap();
            pool.put("enabled", Boolean.TRUE);
            sessionMap.put("wizardPool", pool);
            sessionMap.put("wizardPoolProperties", new HashMap());
        }
    }

    /**
     *	<p> updates the wizard map
     */
    @Handler(id = "updateAdminObjectWizard")
    public static void updateAdminObjectWizard(HandlerContext handlerCtx) {
        Map pool = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPool");

        String resType = (String) pool.get("resType");
        String resAdapter = (String) pool.get("resAdapter");

        String previousResType = (String) pool.get("previousResType");
        String previousResAdapter = (String) pool.get("previousResAdapter");
        try {
            if (resType.equals(previousResType) && resAdapter.equals(previousResAdapter)) {
            //User didn't change type and adapter, keep the properties table content the same.
            } else {
                if (!GuiUtil.isEmpty(resType) && !GuiUtil.isEmpty(resAdapter)) {
                    Properties props = getConnectorConnectionPoolProps("getAdminObjectConfigProps", resAdapter, "admin-object-interface", resType);
                    handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolProperties", props);
                }
                pool.put("previousResType", resType);
                pool.put("previousResAdapter", resAdapter);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler creates an Admin Object Resource in DomainConfig
     */
    @Handler(id = "createAdminObjectResource", input = {
@HandlerInput(name = "enabled", type = Boolean.class),
@HandlerInput(name = "targets", type = String[].class)
})
    public static void createAdminObjectResource(HandlerContext handlerCtx) {
        Map pool = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPool");
        String name = (String) pool.get("name");
        String resType = (String) pool.get("resType");
        String resAdapter = (String) pool.get("resAdapter");
        String description = (String) pool.get("description");
        Boolean enabled = (Boolean) pool.get("enabled");
        //We need to use JMX to do the creation because AMX API doesn't do any verification

        AttributeList list = new AttributeList();
        list.add(new Attribute("jndi-name", name));
        list.add(new Attribute("res-type", resType));
        list.add(new Attribute("res-adapter", resAdapter));
        //list.add(new Attribute("enabled", enabled));
        list.add(new Attribute("description", description));

        Map propsMap = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolProperties");
        Properties properties = convertMapToProperties(propsMap);
    /* TODO-V3
    String[] types = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};
    Object[] params = new Object[]{list, properties, "domain"};
    try {
    Object obj =  JMXUtil.invoke(
    "com.sun.appserv:type=resources,category=config", 
    "createAdminObjectResource", params, types);
    JavaMailHandlers.createNewTargets(handlerCtx, name);
    }catch (Exception ex){
    GuiUtil.handleException(handlerCtx, ex);
    }
     */

    }

    /**
     *	<p> This handler returns the list of Resource Adapter 
     *  <p> Output value: "poolNames" -- Type: <code>java.util.List</code></p>
     */
    @Handler(id = "getJmsConnectionFactories", input = {
@HandlerInput(name = "selectedRows", type = List.class)
}, output = {
@HandlerOutput(name = "result", type = java.util.List.class)
})
    public static void getJmsConnectionFactories(HandlerContext handlerCtx) {
        ObjectName[] factories = null;
        /* TODO-V3
        //List of jms connection factories
        String[] types = new String[]{"java.lang.String"};
        Object[] params = new Object[]{""}; 
        ObjectName[] factories = (ObjectName[]) JMXUtil.invoke(
        "com.sun.appserv:type=resources,category=config", 
        "getJmsConnectionFactory", params, types);
         */

        List<Map> selectedList = (List) handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size() == 0) ? false : true;

        List result = new ArrayList();
        if (factories != null && factories.length > 0) {
            for (int i = 0; i < factories.length; i++) {
                HashMap oneRow = new HashMap();
                ObjectName one = factories[i];
                String name = one.getKeyProperty("jndi-name");
                oneRow.put("name", name);
                ConnectorResourceConfig resource = AMXRoot.getInstance().getResourcesConfig().getConnectorResourceConfigMap().get(name);
                oneRow.put("enabled", TargetUtil.getEnabledStatus(resource, false));
                oneRow.put("description", GuiUtil.checkEmpty(resource.getDescription()));
                oneRow.put("pool", resource.getPoolName());
                oneRow.put("selected", (hasOrig) ? GuiUtil.isSelected(name, selectedList) : false);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("result", result);
    }

    /**
     * createJmsConnectionFactory()
     */
    @Handler(id = "createJmsConnectionFactory", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "resType", type = String.class),
@HandlerInput(name = "description", type = String.class),
@HandlerInput(name = "steadyPoolSize", type = String.class),
@HandlerInput(name = "maxPoolSize", type = String.class),
@HandlerInput(name = "poolResizeQuantity", type = String.class),
@HandlerInput(name = "idleTimeoutInSeconds", type = String.class),
@HandlerInput(name = "maxWaitTimeInMillis", type = String.class),
@HandlerInput(name = "transactionSupport", type = String.class),
@HandlerInput(name = "enabled", type = Boolean.class),
@HandlerInput(name = "isConnectionValidationRequired", type = Boolean.class),
@HandlerInput(name = "failAllConnections", type = Boolean.class),
@HandlerInput(name = "properties", type = java.util.Map.class),
@HandlerInput(name = "targets", type = String[].class)
})
    public static void createJmsConnectionFactory(HandlerContext handlerCtx) {

        AttributeList list = new AttributeList();
        list.add(createAttr("jndi-name", "jndiName", handlerCtx));
        list.add(createAttr("res-type", "resType", handlerCtx));
        list.add(createAttr("description", "description", handlerCtx));
        list.add(createAttr("steady-pool-size", "steadyPoolSize", handlerCtx));
        list.add(createAttr("max-pool-size", "maxPoolSize", handlerCtx));
        list.add(createAttr("pool-resize-quantity", "poolResizeQuantity", handlerCtx));
        list.add(createAttr("idle-timeout-in-seconds", "idleTimeoutInSeconds", handlerCtx));
        list.add(createAttr("max-wait-time-in-millis", "maxWaitTimeInMillis", handlerCtx));
        list.add(createAttr("transaction-support", "transactionSupport", handlerCtx));
        list.add(createAttr("is-connection-validation-required", "isConnectionValidationRequired", handlerCtx));
        list.add(createAttr("fail-all-connections", "failAllConnections", handlerCtx));

        Properties props = convertMapToProperties(
                (Map) handlerCtx.getInputValue("properties"));
    /* TODO-V3
    String[] types = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};
    Object[] params = new Object[]{list, props, "domain"};
    try {
    JMXUtil.invoke(
    "com.sun.appserv:type=resources,category=config", 
    "createJmsResource", params, types);
    //work around a bug that is-connection-validation-required attribute was ignored during creation
    String jndiName = (String) handlerCtx.getInputValue("jndiName");
    ConnectorConnectionPoolConfig pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
    //for Window, sometimes we need to put in a little delay 
    int ix = 0;
    while( (pool == null) && ix < 10){
    Thread.sleep(1000);
    pool = AMXRoot.getInstance().getResourcesConfig().getConnectorConnectionPoolConfigMap().get(jndiName);
    ix++;
    }
    pool.setConnectionValidationRequired ((Boolean) handlerCtx.getInputValue("isConnectionValidationRequired"));
    JavaMailHandlers.createNewTargets(handlerCtx,  jndiName);
    }catch (Exception ex){
    GuiUtil.handleException(handlerCtx, ex);
    }*/
    }

    private static Properties convertMapToProperties(Map<String, String> inputMap) {
        Properties props = new Properties();
        for (String key : inputMap.keySet()) {
            if (!GuiUtil.isEmpty(inputMap.get(key))) {
                props.put(key, inputMap.get(key));
            }
        }
        return props;
    }

    /**
     * createJmsDestinationResource()
     */
    @Handler(id = "createJmsDestinationResource", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "name", type = String.class, required = true),
@HandlerInput(name = "resType", type = String.class, required = true),
@HandlerInput(name = "resAdapter", type = String.class, required = true),
@HandlerInput(name = "description", type = String.class),
@HandlerInput(name = "enabled", type = Boolean.class),
@HandlerInput(name = "properties", type = java.util.Map.class),
@HandlerInput(name = "targets", type = String[].class)
})
    public static void createJmsDestinationResource(HandlerContext handlerCtx) {

        AttributeList list = new AttributeList();
        list.add(createAttr("jndi-name", "jndiName", handlerCtx));
        list.add(createAttr("res-type", "resType", handlerCtx));
        list.add(createAttr("res-adapter", "resAdapter", handlerCtx));
        list.add(createAttr("description", "description", handlerCtx));
        list.add(new Attribute("enabled", "true"));

        Properties props = convertMapToProperties(
                (Map) handlerCtx.getInputValue("properties"));
        props.put("Name", handlerCtx.getInputValue("name"));
    /* TODO-V3
    String[] types = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};
    Object[] params = new Object[]{list, props, "domain"};
    try {
    JMXUtil.invoke(
    "com.sun.appserv:type=resources,category=config", 
    "createJmsResource", params, types);
    if (File.separatorChar == '\\'){
    //For Window, there is a timing issue that we need to put in some delay.
    Thread.sleep(2000);
    }
    String jndiName = (String)handlerCtx.getInputValue("jndiName");
    JavaMailHandlers.createNewTargets(handlerCtx,  jndiName);
    }catch (Exception ex){
    GuiUtil.handleException(handlerCtx, ex);
    }
     */
    }

    private static Attribute createAttr(String name, String key, HandlerContext handlerCtx) {
        Object value = handlerCtx.getInputValue(key);
        Attribute attr = new Attribute(name, (value == null) ? "" : value.toString());
        return attr;
    }

    /**
     *  <p>Gets the properties of JMS Connection Factories.  This is the same as getting the properties
     *  for Connector Connection Pool with resource adapter set to "jmsra" <p>
     */
    @Handler(id = "getJmsDestinationProperties", output = {
@HandlerOutput(name = "properties", type = java.util.Map.class)
})
    public static void getJmsDestinationProperties(HandlerContext handlerCtx) {
        Properties props = getConnectorConnectionPoolProps(
                "getAdminObjectConfigProps", "jmsra",
                "admin-object-interface", "javax.jms.Queue");
        props.remove("Name");
        handlerCtx.setOutputValue("properties", props);
    }

    /**
     *  <p>Gets the properties of JMS Connection Factories.  This is the same as getting the properties
     *  for Connector Connection Pool with resource adapter set to "jmsra" <p>
     */
    @Handler(id = "getJmsConnectionFactoriesProperties", output = {
@HandlerOutput(name = "properties", type = java.util.Map.class)
})
    public static void getJmsConnectionFactoriesProperties(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("properties",
                getConnectorConnectionPoolProps("getMCFConfigProps", "jmsra", "connection-definition-name", "javax.jms.TopicConnectionFactory"));
    }

    private static Properties getConnectorConnectionPoolProps(String getMethodName, String resourceAdapter, String attrName, String connectionDefinition) {

        AttributeList attrList = new AttributeList();
        attrList.add(new Attribute("resource-adapter-name", resourceAdapter));
        attrList.add(new Attribute(attrName, connectionDefinition));
        Object[] params = new Object[]{attrList};
        String[] types = new String[]{"javax.management.AttributeList"};
        Properties properties = null;
        /* TODO-V3
        Properties properties = (Properties)JMXUtil.invoke(
        "com.sun.appserv:type=resources,category=config",
        getMethodName,
        params, types );
         */

        return (properties == null) ? new Properties() : properties;
    }

    /**
     *	<p> This handler returns the list of targets for populating the target table.
     *  <p> Input  value: "appName" -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "appType" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getResourcesTargetTableList", input = {
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "resourceType", type = String.class, required = true)
}, output = {
@HandlerOutput(name = "result", type = java.util.List.class)
})
    public static void getResourcesTargetTableList(HandlerContext handlerCtx) {

        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        String resourceType = (String) handlerCtx.getInputValue("resourceType");
        List<String> targetList = TargetUtil.getDeployedTargets(jndiName, false);
        List result = new ArrayList();
        for (String target : targetList) {
            HashMap oneRow = new HashMap();
            oneRow.put("selected", false);
            oneRow.put("targetName", target);
            Enabled resourceConfig = getEnabledConfig(jndiName, resourceType);
            oneRow.put("enabled", Boolean.toString(TargetUtil.isResourceEnabled(resourceConfig, target)));
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
    }

    /**
     *	<p> This handler takes in selected rows, and change the status of the app
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
     *  <p> Input  value: "enabled" -- Type: <code>Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "changeResourceStatus", input = {
@HandlerInput(name = "selectedRows", type = List.class, required = true),
@HandlerInput(name = "resourceType", type = String.class, required = true),
@HandlerInput(name = "enabled", type = Boolean.class, required = true)
})
    public static void changeResourceStatus(HandlerContext handlerCtx) {

        List obj = (List) handlerCtx.getInputValue("selectedRows");
        boolean enabled = ((Boolean) handlerCtx.getInputValue("enabled")).booleanValue();

//        if(enabled){
//            GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), "Testing");
//            return;
//        }

        String resourceType = (String) handlerCtx.getInputValue("resourceType");
        List selectedRows = (List) obj;
        try {
            for (int i = 0; i < selectedRows.size(); i++) {
                Map oneRow = (Map) selectedRows.get(i);
                String resourceName = (String) oneRow.get("name");
                Enabled resourceConfig = getEnabledConfig(resourceName, resourceType);
                if (resourceConfig == null) {
                //Can't find the deployed app, don't do anything.
                //when the page refresh after the processing, it will be fine.
                } else {
                    List<String> targetList = TargetUtil.getDeployedTargets((AMX) resourceConfig, false);
                    for (String target : targetList) {
                        TargetUtil.setResourceEnabled(resourceConfig, target, enabled);
                    }
                }

                if (AMXRoot.getInstance().isEE()) {
                    String msg = GuiUtil.getMessage((enabled) ? "msg.enableResourceSuccessful" : "msg.disableResourceSuccessful");
                    GuiUtil.prepareAlert(handlerCtx, "success", msg, null);
                } else {
                    String msg = GuiUtil.getMessage((enabled) ? "msg.enableResourceSuccessfulPE" : "msg.disableResourceSuccessfulPE");
                    GuiUtil.prepareAlert(handlerCtx, "success", msg, null);
                }
            }
        } catch (Exception ex) {
            GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), ex.getMessage());
        }
    }

    /**
     *	<p> This handler takes in selected rows, and change the status of the app
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
     *  <p> Input  value: "enabled" -- Type: <code>Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "changeResourceTargetStatus", input = {
@HandlerInput(name = "selectedRows", type = List.class, required = true),
@HandlerInput(name = "resourceType", type = String.class, required = true),
@HandlerInput(name = "jndiName", type = String.class, required = true),
@HandlerInput(name = "enabled", type = Boolean.class, required = true)
})
    public static void changeResourceTargetStatus(HandlerContext handlerCtx) {

        String resourceType = (String) handlerCtx.getInputValue("resourceType");
        String jndiName = (String) handlerCtx.getInputValue("jndiName");
        Enabled resConfig = getEnabledConfig(jndiName, resourceType);
        if (resConfig == null) {
            //Can't find the resource, don't do anything, except maybe log it in server.log
            return;
        }

        List obj = (List) handlerCtx.getInputValue("selectedRows");
        boolean enabled = ((Boolean) handlerCtx.getInputValue("enabled")).booleanValue();

        List selectedRows = (List) obj;
        try {
            for (int i = 0; i < selectedRows.size(); i++) {
                Map oneRow = (Map) selectedRows.get(i);
                String target = (String) oneRow.get("targetName");
                TargetUtil.setResourceEnabled(resConfig, target, enabled);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    static private Enabled getEnabledConfig(String resourceName, String resourceType) {
        Enabled config = null;
        ResourcesConfig resourcesConfig = AMXRoot.getInstance().getResourcesConfig();
        if ("jdbcResource".equals(resourceType)) {
            config = resourcesConfig.getJDBCResourceConfigMap().get(resourceName);
        } else if ("adminObjectResource".equals(resourceType)) {
            config = resourcesConfig.getAdminObjectResourceConfigMap().get(resourceName);
        } else if ("connectorResource".equals(resourceType)) {
            config = resourcesConfig.getConnectorResourceConfigMap().get(resourceName);
        } else if ("javaMailSession".equals(resourceType)) {
            config = resourcesConfig.getMailResourceConfigMap().get(resourceName);
        } else if ("customResource".equals(resourceType)) {
            config = resourcesConfig.getCustomResourceConfigMap().get(resourceName);
        } else if ("externalResource".equals(resourceType)) {
            config = resourcesConfig.getJNDIResourceConfigMap().get(resourceName);
        }
        return config;
    }

    /**
     *	<p> This handler returns the list of specified resources for populating the table.
     *  <p> Input  value: "type" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getResourcesList", input = {
@HandlerInput(name = "type", type = String.class, required = true),
@HandlerInput(name = "selectedRows", type = List.class)
}, output = {
@HandlerOutput(name = "result", type = java.util.List.class)
})
    public static void getResourcesList(HandlerContext handlerCtx) {

        String type = (String) handlerCtx.getInputValue("type");
        boolean isJdbc = false;
        boolean isConnector = false;
        boolean isCustomResource = false;
        boolean isExternal = false;
        boolean isAdminObject = false;

        Iterator iter = null;
        ResourcesConfig resourcesConfig = AMXRoot.getInstance().getResourcesConfig();
        if ("jdbcResource".equals(type)) {
                iter = resourcesConfig.getJDBCResourceConfigMap().values().iterator();
                isJdbc = true;

        } else if ("connectorResource".equals(type)) {
            iter = resourcesConfig.getConnectorResourceConfigMap().values().iterator();
            isConnector = true;
        } else if ("jndiCustomResource".equals(type)) {
            iter = resourcesConfig.getCustomResourceConfigMap().values().iterator();
            isCustomResource = true;
        } else if ("jndiExternalResource".equals(type)) {
            iter = resourcesConfig.getJNDIResourceConfigMap().values().iterator();
            isExternal = true;
        } else if ("adminObjectResource".equals(type)) {
            iter = resourcesConfig.getAdminObjectResourceConfigMap().values().iterator();
            isAdminObject = true;
        } else if ("jmsDestResource".equals(type)) {
            iter = resourcesConfig.getAdminObjectResourceConfigMap().values().iterator();
            //jmsDestationResource is Admin Object Resource with 'jmsra' as res adapter.
            ArrayList jms = new ArrayList();
            while (iter.hasNext()) {
                AdminObjectResourceConfig aor = (AdminObjectResourceConfig) iter.next();
                if ("jmsra".equals(aor.getResAdapter())) {
                    jms.add(aor);
                }
            }
            iter = jms.iterator();
            isAdminObject = true;
        } else if ("javaMailSession".equals(type)) {
            iter = resourcesConfig.getMailResourceConfigMap().values().iterator();
        }
        /** uncomment the following lines if we want the redisplayed list show the previously selected rows.
         * if uncommented, will need to make the javascript smarter to enable the table action buttons where
         * there is at least one selected row. 
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
         */
        List<Map> selectedList = null;
        boolean hasOrig = false;

        List result = new ArrayList();
        if (iter != null) {
            while (iter.hasNext()) {
                try{
                    ResourceConfig resConfig = (ResourceConfig) iter.next();
                    HashMap oneRow = new HashMap();
                    String name = resConfig.getJNDIName();
                    oneRow.put("name", name);
                    oneRow.put("enabled", TargetUtil.getEnabledStatus(resConfig, false));
                    oneRow.put("selected", (hasOrig) ? GuiUtil.isSelected(name, selectedList) : false);
                    oneRow.put("description", GuiUtil.checkEmpty(resConfig.getDescription()));
                    if (isJdbc) {
                        oneRow.put("pool", ((JDBCResourceConfig) resConfig).getPoolName());
                    } else if (isConnector) {
                        oneRow.put("pool", ((ConnectorResourceConfig) resConfig).getPoolName());
                    } else if (isCustomResource) {
                        oneRow.put("resType", ((CustomResourceConfig) resConfig).getResType());
                    } else if (isExternal) {
                        oneRow.put("resType", ((JNDIResourceConfig) resConfig).getResType());
                    } else if (isAdminObject) {
                        oneRow.put("resType", ((AdminObjectResourceConfig) resConfig).getResType());
                    }
                    result.add(oneRow);
                 } catch (Exception ex) {
                        System.out.println("!!!! Catch exception when trying to iterate through resource list.  Resource Name ");
                        ex.printStackTrace();
                }
            }
        }
        handlerCtx.setOutputValue("result", result);
    }

    /**
     *	<p> This handler takes in selected rows, and change the status of the app
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
     *  <p> Input  value: "isJmsConnectionFactory" -- Type: <code>Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "deleteResource", input = {
        @HandlerInput(name = "selectedRows", type = List.class, required = true),
        @HandlerInput(name = "resourceType", type = String.class, required = true),
        @HandlerInput(name = "isJmsConnectionFactory", type = Boolean.class)
    })
    public static void deleteResource(HandlerContext handlerCtx) {


        String target = "server";
        ResourcesConfig resourcesConfig = AMXRoot.getInstance().getResourcesConfig();
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        String resourceType = (String) handlerCtx.getInputValue("resourceType");
        Boolean isJmsConnectionFactory = (Boolean) handlerCtx.getInputValue("isJmsConnectionFactory");
        boolean isJms = (isJmsConnectionFactory == null) ? false : isJmsConnectionFactory.booleanValue();
        List<Map> selectedRows = (List) obj;
        try {
            for (Map oneRow : selectedRows) {
                String resourceName = (String) oneRow.get("name");
                if ("jdbcResource".equals(resourceType)) {
                    resourcesConfig.removeJDBCResourceConfig(resourceName);
                    
                } else if("jdbcConnectionPool".equals(resourceType)){
                    //When deleting JDBCConnection Pool,  we will also delete the JDBC resource that uses this pool.
                    //This equivalent to casade=true in CLI. GUI has already warn user about this.
                    Iterator iter = resourcesConfig.getJDBCResourceConfigMap().values().iterator();
                    if (iter != null) {
                        while (iter.hasNext()) {
                            JDBCResourceConfig jdbc = (JDBCResourceConfig) iter.next();
                            if (jdbc.getPoolName().equals(resourceName))
                                resourcesConfig.removeJDBCResourceConfig(jdbc.getName());
                        }
                    }
                    resourcesConfig.removeJDBCConnectionPoolConfig(resourceName);
                }
                /*
                else if ("adminObjectResource".equals(resourceType)) {
                    resourcesConfig.removeAdminObjectResourceConfig(resourceName);
                } else if ("connectorResource".equals(resourceType) && !isJms) {
                    resourcesConfig.removeConnectorResourceConfig(resourceName);
                } else if ("javaMailSession".equals(resourceType)) {
                    resourcesConfig.removeMailResourceConfig(resourceName);
                } else if ("customResource".equals(resourceType)) {
                    resourcesConfig.removeCustomResourceConfig(resourceName);
                } else if ("externalResource".equals(resourceType)) {
                    resourcesConfig.removeJNDIResourceConfig(resourceName);
                } else if (isJms && "connectorResource".equals(resourceType)) {
                    //JMS Connection Factory.  Need to use JMX so both the connector connection pool
                    // and connector resource is deleted.
                    //Need to remove all the resource-ref first.

                    String defaultTarget = "server";
                    if (AMXRoot.getInstance().isEE()) {
                        List<String> targetList = TargetUtil.getDeployedTargets(resourceName, false);
                        for (String eachTarget : targetList) {
                            TargetUtil.removeResourceRef(resourceName, eachTarget);
                        }
                        defaultTarget = "domain";
                    }
                    Object[] params = {resourceName, defaultTarget};
                    String[] types = {"java.lang.String", "java.lang.String"};
                    JMXUtil.invoke( "com.sun.appserv:type=resources,category=config",
                    "deleteJmsDestinationResource", params, types ); 
                }
                 */
            }
            
            /* remove workaround as AMX will fix this.
             * refer to issue# 4622
            if (File.separatorChar == '\\') {
                //For Window, there is a timing issue that we need to put in some delay.
                //Otherwise, when we redisplay the resource table after deletion, there will be exception thrown
                //since it doesn't recognize that the resource has already been deleted
                Thread.sleep(3000);
            }
             */
        } catch (Exception ex) {
            ex.printStackTrace();
            GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), ex.getMessage());
        }
    }

    /**
     *	<p> This handler returns the list of resources deployed to the specified target.
     *     The target should be the name of a standalone server instance or cluster 
     *  <p> Input  value: "target" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getResourceRefListForTarget", input = {
        @HandlerInput(name = "target", type = String.class, required = true),
        @HandlerInput(name = "filterValue", type = String.class),
        @HandlerInput(name = "isServer", type = Boolean.class, required = true)
        }, output = {
        @HandlerOutput(name = "result", type = java.util.List.class)
    })
    public static void getResourceRefListForTarget(HandlerContext handlerCtx) {
        String target = (String) handlerCtx.getInputValue("target");
        String filterValue = (String) handlerCtx.getInputValue("filterValue");
        boolean isServer = ((Boolean) handlerCtx.getInputValue("isServer")).booleanValue();
        Collection<ResourceRefConfig> refs = new ArrayList();
        List result = new ArrayList();

        if (isServer) {
            StandaloneServerConfig server = AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().get(target);
            refs = server.getResourceRefConfigMap().values();
        } else {
            ClusterConfig cluster = AMXRoot.getInstance().getClustersConfig().getClusterConfigMap().get(target);
            refs = cluster.getResourceRefConfigMap().values();
        }

        for (ResourceRefConfig refObject : refs) {
            String resName = refObject.getName();
            String resType = getResourceType(resName);
            if (!GuiUtil.isEmpty(filterValue)) {
                if (!resType.equals(filterValue)) {
                    continue;
                }
            }
            Map oneRow = new HashMap();
            oneRow.put("enabled", refObject.getEnabled());
            oneRow.put("selected", false);
            oneRow.put("name", resName);
            oneRow.put("link", "/resourceNode/" + editMap.get(resType) + "?name=" + resName);
            oneRow.put("resType", typeMap.get(resType));
            ObjectName nn = com.sun.appserv.management.base.Util.getObjectName(refObject);
            oneRow.put("objectName", nn.toString());
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
    }

    /**
     *	<p> This handler sets the enabled status for a particular target.
     *  <p> Input  value: "target" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "changeEnableForTarget", input = {
@HandlerInput(name = "target", type = String.class, required = true),
@HandlerInput(name = "enabled", type = Boolean.class, required = true),
@HandlerInput(name = "isServer", type = Boolean.class, required = true),
@HandlerInput(name = "selectedRows", type = java.util.List.class)
})
    public static void changeEnableForTarget(HandlerContext handlerCtx) {
        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
        String enabled = ""+ handlerCtx.getInputValue("enabled");
        String target = (String) handlerCtx.getInputValue("target");

        List<Map<String, ResourceRefConfig>> allResourceRefs = TargetUtil.getAllResourceRefConfig(target);
        for (Map oneRow : selectedRows) {
            String name = (String) oneRow.get("name");
            for (Map<String, ResourceRefConfig> oneResourceMap : allResourceRefs) {
                ResourceRefConfig ref = oneResourceMap.get(name);
                ref.setEnabled(enabled);
            }
        }
    }

    /**
     *	<p> Returns the list of resources for filtering 
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getResourceFilterTypes", output = {
@HandlerOutput(name = "labels", type = java.util.List.class),
@HandlerOutput(name = "values", type = java.util.List.class)
})
    public static void getResourceFilterTypes(HandlerContext handlerCtx) {

        List labels = new ArrayList();
        labels.add(GuiUtil.getMessage("common.showAll"));
        labels.add(GuiUtil.getMessage("tree.jdbcResources"));
        labels.add(GuiUtil.getMessage("tree.javaMailSessions"));
        labels.add(GuiUtil.getMessage("tree.customResources"));
        labels.add(GuiUtil.getMessage("tree.externalResources"));
        labels.add(GuiUtil.getMessage("tree.connectorResources"));
        labels.add(GuiUtil.getMessage("tree.adminObjectResources"));

        List values = new ArrayList();
        values.add("");
        values.add(JDBCResourceConfig.J2EE_TYPE);
        values.add(MailResourceConfig.J2EE_TYPE);
        values.add(CustomResourceConfig.J2EE_TYPE);
        values.add(JNDIResourceConfig.J2EE_TYPE);
        values.add(ConnectorResourceConfig.J2EE_TYPE);
        values.add(AdminObjectResourceConfig.J2EE_TYPE);

        handlerCtx.setOutputValue("values", values);
        handlerCtx.setOutputValue("labels", labels);

    }

    private static String getResourceType(String name) {
        Set<ResourceConfig> resources = AMXRoot.getInstance().getQueryMgr().queryJ2EETypesSet(RESOURCE_TYPES);
        for (ResourceConfig res : resources) {
            if (res.getName().equals(name)) {
                return res.getJ2EEType();
            }
        }
        return "";
    }

    
    static final private Set<String> RESOURCE_TYPES = GSetUtil.newUnmodifiableStringSet(
            JDBCResourceConfig.J2EE_TYPE,
            MailResourceConfig.J2EE_TYPE,
            CustomResourceConfig.J2EE_TYPE,
            JNDIResourceConfig.J2EE_TYPE,
            ConnectorResourceConfig.J2EE_TYPE,
            AdminObjectResourceConfig.J2EE_TYPE);
    static private Map<String, String> editMap = new HashMap();
    static private Map<String, String> typeMap = new HashMap();

    static {
        editMap.put(JDBCResourceConfig.J2EE_TYPE, "jdbcResourceEdit.jsf");
        editMap.put(MailResourceConfig.J2EE_TYPE, "javaMailSessionEdit.jsf");
        editMap.put(CustomResourceConfig.J2EE_TYPE, "customResourceEdit.jsf");
        editMap.put(JNDIResourceConfig.J2EE_TYPE, "externalResourceEdit.jsf");
        editMap.put(ConnectorResourceConfig.J2EE_TYPE, "connectorResourceEdit.jsf");
        editMap.put(AdminObjectResourceConfig.J2EE_TYPE, "adminObjectEdit.jsf");

        typeMap.put(JDBCResourceConfig.J2EE_TYPE, GuiUtil.getMessage("tree.jdbcResources"));
        typeMap.put(MailResourceConfig.J2EE_TYPE, GuiUtil.getMessage("tree.javaMailSessions"));
        typeMap.put(CustomResourceConfig.J2EE_TYPE, GuiUtil.getMessage("tree.customResources"));
        typeMap.put(JNDIResourceConfig.J2EE_TYPE, GuiUtil.getMessage("tree.externalResources"));
        typeMap.put(ConnectorResourceConfig.J2EE_TYPE, GuiUtil.getMessage("tree.connectorResources"));
        typeMap.put(AdminObjectResourceConfig.J2EE_TYPE, GuiUtil.getMessage("tree.adminObjectResources"));
    }
}
 
