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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * $Id: JTSMonitorMBean.java,v 1.5 2006/03/14 11:11:44 sankara Exp $
 */
package com.sun.enterprise.transaction.monitor;

import java.util.Map;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.RuntimeOperationsException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import javax.transaction.Transaction;
import javax.transaction.SystemException;

import com.sun.enterprise.admin.monitor.BaseMonitorMBean;
import com.sun.enterprise.admin.monitor.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.types.Counter;
import com.sun.enterprise.admin.monitor.types.MonitoredAttributeType;
import com.sun.enterprise.admin.monitor.types.StringMonitoredAttributeType;

import com.sun.enterprise.J2EETransactionManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.transaction.TransactionAdminBean;
import com.sun.enterprise.resource.ResourceInstaller;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import com.sun.enterprise.util.i18n.StringManager;

//jsr 77 support
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext; 
import com.sun.enterprise.admin.monitor.registry.*;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.TransactionService;
import com.sun.enterprise.config.serverbeans.ElementProperty;



/**
 * MBean implementation  to monitor Transaction Manager.
 */
public class JTSMonitorMBean extends BaseMonitorMBean {

    static final String NUM_TRANSACTIONS_COMPLETED = "total-tx-completed";
    static final String NUM_TRANSACTIONS_ROLLEDBACK = "total-tx-rolled-back";
    static final String NUM_TRANSACTIONS_INFLIGHT = "total-tx-inflight";
    static final String IS_FROZEN = "isFrozen";
    static final String INFLIGHT_TRANSACTIONS = "inflight-tx";
    static final String ROLLBACK = "rollbackList";
    static final String FREEZE = "freeze";
    static final int COLUMN_LENGTH = 25;

    public static final String TRANSACTION_ID = "TransactionId";
    public static final String STATE = "TransactionState";
    public static final String ELAPSED_TIME = "ElapsedTime";
    public static final String COMPONENT_NAME = "ComponentName";
    public static final String RESOURCE_NAMES = "ResourceNames";


	// Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(JTSMonitorMBean.class);

 /**
 	Logger to log transaction messages
 */ 
 	static Logger _logger = LogDomains.getLogger(LogDomains.JTA_LOGGER);


    /**
    * A 2-d array initialized to attribute names and their types
    */
    private static Object[][] attrNameTypeArray = {
        {NUM_TRANSACTIONS_COMPLETED, Counter.INTEGER},
	{NUM_TRANSACTIONS_ROLLEDBACK, Counter.INTEGER},
	{NUM_TRANSACTIONS_INFLIGHT, Counter.INTEGER},
        {IS_FROZEN, StringMonitoredAttributeType.DEFAULT},
        {INFLIGHT_TRANSACTIONS, StringMonitoredAttributeType.DEFAULT}
       };

    private static MBeanOperationInfo[] operationInfoArray =
        new MBeanOperationInfo[2];

    /**
    * JTSAdminClient to get the monitor data from, and to invoke user actions
    */
    private J2EETransactionManager txnMgr;
    private Hashtable txnTable = null;
    private MonitoredObjectType type = MonitoredObjectType.TXNMGR;
    private boolean monitorOn = false;
    private long startTime = 0;

    /**
     * Map of attribute names and their types
     */
    private static Map attrNameTypeMap;

    /**
     * Info on this MBean
     */
    private static MBeanInfo mBeanInfo;

    static {
        attrNameTypeMap = createAttrNameTypeMap(attrNameTypeArray);
        operationInfoArray[0] = new MBeanOperationInfo(ROLLBACK,
                                    "rollback(String txnId): Marks the transaction for rollback",
                                    null, "void", MBeanOperationInfo.ACTION);
        operationInfoArray[1] = new MBeanOperationInfo(FREEZE,
                                    "freeze(): Freezes the transactions",
                                    null, "void", MBeanOperationInfo.ACTION);
        mBeanInfo = createMBeanInfo(attrNameTypeMap, operationInfoArray);
    }

    /**
     * Creates a new instance of JTSMonitorMBean
     */
    public JTSMonitorMBean() {
           txnMgr = Switch.getSwitch().getTransactionManager();
           ServerContext sCtx = ApplicationServer.getServerContext();
           if (sCtx != null) {
               try {
                   ConfigContext ctx = sCtx.getConfigContext();
                   Config cfg = ServerBeansFactory.getConfigBean(ctx);
                   String lvl = cfg.getMonitoringService().getModuleMonitoringLevels().getTransactionService();
                   MonitoringLevel l = MonitoringLevel.instance(lvl);
                   if (l != MonitoringLevel.OFF) {
                       startMonitoring();
                   }
                   MonitoringRegistry registry = sCtx.getMonitoringRegistry();
                   JTAStatsImpl.createInstance(this);
                   JTAStatsImpl statImpl = JTAStatsImpl.getInstance();
                   registry.registerJTAStats(statImpl, statImpl);
                   _logger.log(Level.FINE,"JTAStats monitoring registration completed");
                   TransactionService txnService = ServerBeansFactory.getTransactionServiceBean(ctx);
                   ElementProperty[] eprops = txnService.getElementProperty();
                   for (int index = 0; index < eprops.length; index++) {
                       if ("pending-txn-cleanup-interval".equals(eprops[index].getName())) {
                           int interval = 60;
                           if (eprops[index].getValue() != null)
                               interval = Integer.parseInt(eprops[index].getValue());
                           new RecoveryHelperThread(interval).start();
                           if (_logger.isLoggable(Level.FINE))
                               _logger.log(Level.FINE,"Asynchronous thread for incomplete tx is enabled with interval " + interval);
                    }
                }

               } catch (MonitoringRegistrationException mex) {
                   _logger.log(Level.WARNING,"transaction.monitor.registration_failed", mex);
               } catch (ConfigException e) {
                   _logger.log(Level.WARNING,"transaction.monitor.registration_failed", e);
               }
           }
           else {
               _logger.log(Level.FINE,"JTSMonitorMBean: ServerContext is null: monitoring is not enabled");
           }
    }

    public List<Map<String, String>> listActiveTransactions() {
        ArrayList aList = txnMgr.getActiveTransactions();
        if (aList.isEmpty()) 
            return new ArrayList<Map<String, String>>(0);
        txnTable = new Hashtable();
        List<Map<String, String>> activeTxnList = new ArrayList<Map<String, String>>();
        Map<String, String> txnListEntry = null;
        for (int i=0; i < aList.size(); i++) {
            TransactionAdminBean txnBean = (TransactionAdminBean)aList.get(i);
            Transaction j2eeTxn = (Transaction) txnBean.getIdentifier();
            String txnId = txnBean.getId();
            txnTable.put(txnId, j2eeTxn);
            txnListEntry = new HashMap<String, String>(5);
            txnListEntry.put(TRANSACTION_ID, txnId);
            txnListEntry.put(ELAPSED_TIME, String.valueOf(txnBean.getElapsedTime()));
            txnListEntry.put(COMPONENT_NAME, txnBean.getComponentName());
            ArrayList<String> resourceList = txnBean.getResourceNames();
            StringBuffer strBuf = new StringBuffer(" ");
            if (resourceList != null) {
                for (int k = 0; k < resourceList.size(); k++) {
                    strBuf.append(resourceList.get(k));
                    strBuf.append(",");
                }
            }
            txnListEntry.put(RESOURCE_NAMES, strBuf.toString());
            txnListEntry.put(STATE, txnBean.getStatus());
            activeTxnList.add(txnListEntry);
        }
        return activeTxnList;
    }

    /**
     * Obtains the value of a specific monitored attribute.
     * @param attribute The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws AttributeNotFoundException if attribute name is not valid
     */
    public Object getAttribute(String attribute) throws AttributeNotFoundException{
        if (attribute == null) {
			/**
         throw new RuntimeOperationsException(
             new IllegalArgumentException("Attribute name cannot be null"));
			**/
         throw new RuntimeOperationsException(
             new IllegalArgumentException(sm.getString("transaction.monitor.attribute_is_null")));
        }
        // Call the corresponding getter for a recognized attribute_name
        if (attribute.equals(NUM_TRANSACTIONS_COMPLETED)) {
            return new Integer(txnMgr.getNumberOfTransactionsCommitted());
        }
        if (attribute.equals(NUM_TRANSACTIONS_ROLLEDBACK)) {
            return new Integer(txnMgr.getNumberOfTransactionsRolledBack());
        }
        if (attribute.equals(NUM_TRANSACTIONS_INFLIGHT)) {
            return new Integer(txnMgr.getNumberOfActiveTransactions());
        }
        if (attribute.equals(IS_FROZEN)) {
            if (txnMgr.isFrozen())
                return "True";
            else
                return "False";
        }        
        if (attribute.equals(INFLIGHT_TRANSACTIONS)) {
            ArrayList aList = txnMgr.getActiveTransactions();
            // if (aList.isEmpty()) return "No active transaction found.";
            if (aList.isEmpty()) return "";
            StringBuffer strBuf = new StringBuffer(1024);
            txnTable = new Hashtable();
        
            //Set the headings for the tabular output
            if (aList.size() > 0) {
                String colName = "Transaction Id";
                strBuf.append("\n\n");
                strBuf.append(colName);
                for (int i=colName.length(); i<COLUMN_LENGTH+15; i++){
                    strBuf.append(" ");
                }
                colName = "Status";
                strBuf.append(colName);
                for (int i=colName.length(); i<COLUMN_LENGTH; i++){
                    strBuf.append(" ");
                }
                colName = "ElapsedTime(ms)";
                strBuf.append(colName);
                for (int i=colName.length(); i<COLUMN_LENGTH; i++){
                    strBuf.append(" ");
                }
                colName = "ComponentName";
                strBuf.append(colName);
                for (int i=colName.length(); i<COLUMN_LENGTH; i++){
                    strBuf.append(" ");
                }
                strBuf.append("ResourceNames\n");
            }

            for (int i=0; i < aList.size(); i++) {
                TransactionAdminBean txnBean = (TransactionAdminBean)aList.get(i);
                Transaction j2eeTxn = (Transaction) txnBean.getIdentifier();
                String txnId = txnBean.getId();
                txnTable.put(txnId, j2eeTxn);

                strBuf.append("\n");
                strBuf.append(txnId);
                for (int j=txnId.length(); j<COLUMN_LENGTH+15; j++){
                    strBuf.append(" ");
                }
                strBuf.append(txnBean.getStatus());
                for (int j=txnBean.getStatus().length(); j<COLUMN_LENGTH; j++){
                    strBuf.append(" ");
                }
                strBuf.append(String.valueOf(txnBean.getElapsedTime()));
                for (int j=(String.valueOf(txnBean.getElapsedTime()).length()); j<COLUMN_LENGTH; j++){
                    strBuf.append(" ");
                }

                strBuf.append(txnBean.getComponentName());
                for (int j=txnBean.getComponentName().length(); j<COLUMN_LENGTH; j++){
                    strBuf.append(" ");
                }
                ArrayList<String> resourceList = txnBean.getResourceNames();
                if (resourceList != null) {
                    for (int k = 0; k < resourceList.size(); k++) {
                        strBuf.append(resourceList.get(k));
                        strBuf.append(",");
                    }
                }
            }

            return strBuf.toString();
        }

         // If attribute_name has not been recognized
         // throw(new AttributeNotFoundException("Cannot find " + attribute + " attribute" ));
         throw(new AttributeNotFoundException(sm.getString("transaction.monitor.attribute_not_found",attribute ) ));
    }

    /**
     * Get the values of several attributes of the monitoring MBean.
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     */
    public AttributeList getAttributes(String[] attributeNames) {
	// Check attributeNames to avoid NullPointerException later on
        if (attributeNames == null) {
			/**
            throw new RuntimeOperationsException(
                new IllegalArgumentException(
                    "attributeNames[] cannot be null"));
			**/
            throw new RuntimeOperationsException(
                new IllegalArgumentException(
                    sm.getString("transaction.monitor.attributes_not_null")));
        }
        AttributeList resultList = new AttributeList();
        // if attributeNames is empty, return an empty result list
        if (attributeNames.length == 0)
             return resultList;
             // build the result attribute list
        for (int i=0 ; i<attributeNames.length ; i++){
         try {
             Object value = getAttribute((String) attributeNames[i]);
             resultList.add(new Attribute(attributeNames[i],value));
         } catch (Exception e) {
             // print debug info but continue processing list
			 _logger.log(Level.WARNING,"transaction.monitor.error_while_getting_monitor_attr",e);
         }
        }
        return(resultList);
    }

    public Object invoke(String operationName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        if (operationName == null || operationName.equals("")) {
			/**
            throw new RuntimeOperationsException(
                new IllegalArgumentException("operationName cannot be null"));
			**/
            throw new RuntimeOperationsException(
                new IllegalArgumentException(sm.getString("transaction.monitor.operation_name_is_null")));
        }

        if (params == null)
            return null;

        AttributeList resultList = new AttributeList();
        if (operationName.equals(ROLLBACK)) {
            for (int i=0; i<params.length; i++) {
                String txnId = (String) params[i];
                Object value = setRollback(txnId);
                resultList.add(new Attribute(txnId, value));
            }
        } else if (operationName.equals(FREEZE)) {
            if (params[0].equals("true")) {
		txnMgr.freeze();
	    	resultList.add(new Attribute("freeze", "Successful"));
            } else {
		txnMgr.unfreeze();
	    	resultList.add(new Attribute("unfreeze", "Successful"));
            }
        } else {
            throw new UnsupportedOperationException(UNSUPPORTED_ERRMSG);
        }

        return resultList;
    }

    public void freeze() {
        txnMgr.freeze();
    }
    public void unfreeze() {
	txnMgr.unfreeze();
    }
	
    public String[] rollback(String[] txnIds) {
        if (txnIds == null || txnIds.length == 0)
            return new String[0];
        String result[] = new String[txnIds.length];
        for (int i = 0; i < txnIds.length; i++) {
            result[i] = (String) setRollback(txnIds[i]);
        }
        return result;
	}
	
    public Object setRollback(String txnId) throws IllegalStateException {
        // Lookup the transaction Array for the txnid
        String result = "";
        if (txnTable == null || txnTable.get(txnId) == null) {
            result = sm.getString("transaction.monitor.rollback_invalid_id");
            throw new  IllegalStateException(result);
        }
        else {
            // Call the TransactionManager to rollback
            try {
                txnMgr.forceRollback((Transaction)txnTable.get(txnId));
                result = sm.getString("transaction.monitor.rollback_sucessful");
            } catch (IllegalStateException e) {
                // current thread is not associated with the transaction
                result = sm.getString("transaction.monitor.rollback_unsuccessful_not_associated");
                IllegalStateException ex = new IllegalStateException(result);
                ex.initCause(e);
                throw ex;
                
            } catch (SecurityException e) {
                // Thread is not allowed to rollback the transaction
                result = sm.getString("transaction.monitor.rollback_unsuccessful_security_exception");
                SecurityException ex = new SecurityException(result);
                ex.initCause(e);
                throw ex;
            } catch (SystemException e) {
                // Transaction Manager encountered unexpected error condition
                result = sm.getString("transaction.monitor.rollback_unsuccessful_unexpected_exception");
                IllegalStateException ex = new IllegalStateException(result);
                ex.initCause(e);
                throw ex;
            }
        }

	return result;
    }


    /**
     * Start monitoring on this component. This will be called when monitoring
     * is enabled on this component (or the group containing this component)
     * through user interface.
     * @see stopMonitoring
     */
    public void startMonitoring() {
	txnMgr.setMonitoringEnabled(true);
        monitorOn = true;
        startTime = System.currentTimeMillis();
    }

    /**
     * Stop monitoring on this component. Called when monitoring is disabled on
     * user interface.
     */
    public void stopMonitoring() {
	txnMgr.setMonitoringEnabled(false);
        monitorOn = false;
        startTime = 0;
    }


   public long getStartTime() {
       return startTime;
   }

    /**
     * Provides the exposed attributes and actions of the monitoring MBean using
     * an MBeanInfo object.
     * @return An instance of MBeanInfo with all attributes and actions exposed
     *         by this monitoring MBean.
     */
    public MBeanInfo getMBeanInfo() {
        return mBeanInfo;
    }

    /**
     * Get a map of monitored attribute names and their types. The keys in
     * the map are names of the attribute and the values are their types. The
     * type value are instances of class
     * com.iplanet.ias.monitor.type.MonitoredAttributeType (or its sub-classes)
     *
     * @return map of names and types of all monitored attributes
     */
    public Map getMonitoringMetaData() {
        return attrNameTypeMap;
    }

    /**
     * Get type of the specified monitored attribute.
     */
    public MonitoredAttributeType getAttributeType(String attrName) {
        MonitoredAttributeType type = null;
        if (attrNameTypeMap != null && attrName != null) {
            type = (MonitoredAttributeType)attrNameTypeMap.get(attrName);
        }
        return type;
    }

    public MonitoredObjectType getMonitoredObjectType() {
        return type;
    }

    public static void recover(boolean delegated, String txLogDir) throws Exception {
        ResourceInstaller resInstaller = Switch.getSwitch().getResourceInstaller();
        boolean result = true;
        if (resInstaller == null) {
            throw new IllegalStateException();
        }
        if (!delegated) { // own recovery
            result = resInstaller.recoverIncompleteTx(false, null);
        }
        else { // delegated recovery
            result = resInstaller.recoverIncompleteTx(true, txLogDir);
        }
        if (!result)
            throw new IllegalStateException();
    }

    class RecoveryHelperThread extends Thread {
        private int interval;
        RecoveryHelperThread(int interval) {
            setName("Recovery Helper Thread");
            setDaemon(true);
            this.interval = interval;
        }
        public void run() {
            try {
                while(true) {
                    Thread.sleep(interval*1000);
                    Switch.getSwitch().getResourceInstaller().recoverIncompleteTx(false, null);
                }
            } catch (Exception ex) {
                if (JTSMonitorMBean._logger.isLoggable(Level.FINE))
                    JTSMonitorMBean._logger.log(Level.FINE, " Exception occurred in recoverInCompleteTx ");
            }
        }
    }
}

