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

package com.sun.enterprise.transaction.monitor;

import com.sun.enterprise.admin.monitor.registry.*;
import com.sun.enterprise.admin.monitor.stats.*;
import com.sun.enterprise.admin.monitor.stats.JTAStats;
import com.sun.jts.CosTransactions.RecoveryManager;
import javax.management.j2ee.statistics.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.List;
import com.sun.logging.LogDomains;


public class JTAStatsImpl implements JTAStats, MonitoringLevelListener {

    private JTSMonitorMBean mBean = null;
    private GenericStatsImpl gStatsDelegate = null; 
    
    static Logger _logger = LogDomains.getLogger(LogDomains.JTA_LOGGER);
    private static JTAStatsImpl instance = null;

    // Will be instantiated by JTSMonitorMBean
    private JTAStatsImpl(JTSMonitorMBean mBean) {
        this.mBean = mBean;
        try {
            gStatsDelegate = new GenericStatsImpl("com.sun.enterprise.admin.monitor.stats.JTAStats",this);
        } catch (ClassNotFoundException clex) {
            _logger.log(Level.WARNING,"transaction.monitor.error_creating_jtastatsimpl",clex);
            // log and forget. Should not happen
        }
    }

    public static synchronized void createInstance(JTSMonitorMBean mBean) {
        if (instance == null)
            instance = new JTAStatsImpl(mBean);
    }
    public static synchronized JTAStatsImpl getInstance() {
        if (instance == null)
            throw new UnsupportedOperationException();
        return instance;
    }

    // Remove once it is deprecated
    public void setLevel(MonitoringLevel level) {
        if (level == MonitoringLevel.OFF) {
            mBean.stopMonitoring();
        }
        else if (level == MonitoringLevel.LOW || level == MonitoringLevel.HIGH) {
            mBean.startMonitoring();
        } 
    }

    // MonitoringLevelListener method
    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
                            javax.management.j2ee.statistics.Stats handback) {
        if (from != to) {
            _logger.log(Level.FINE,"JTAStats Monitoring level changed from " + from + "  to  " + to);
            if (to == MonitoringLevel.OFF) {
                mBean.stopMonitoring();
            }
            else if (to == MonitoringLevel.LOW || to == MonitoringLevel.HIGH) {
                mBean.startMonitoring();
            }
        }
    }

    public StringStatistic getActiveIds() {
        String activeStr = null;
        try {
            activeStr = (String)mBean.getAttribute(JTSMonitorMBean.INFLIGHT_TRANSACTIONS);
        }catch (javax.management.AttributeNotFoundException jmxex) {
           _logger.log(Level.WARNING,"transaction.monitor.attribute_not_found",jmxex);
        }
        return new StringStatisticImpl(activeStr, 
                                      "ActiveIds", 
                                       //"getActiveIds", 
                                       "List", 
                                       "List of inflight transactions", 
                                       mBean.getStartTime(),
                                       System.currentTimeMillis()); 
    }

    public StringStatistic getState() {
        String str = null;
        try {
            str = (String)mBean.getAttribute(JTSMonitorMBean.IS_FROZEN);
        }catch (javax.management.AttributeNotFoundException jmxex) {
           _logger.log(Level.WARNING,"transaction.monitor.attribute_not_found",jmxex);
          // log and forget. Should not happen
        }
        
        return new StringStatisticImpl(str, 
                                       "State", 
                                       //"getState", 
                                       "String", 
                                       "Transaction system state: frozen?", 
                                       mBean.getStartTime(),
                                       System.currentTimeMillis()); 
    }

    public CountStatistic getActiveCount() {
        Integer count = null; 
        try {
            count = (Integer)mBean.getAttribute(JTSMonitorMBean.NUM_TRANSACTIONS_INFLIGHT); 
        }catch (javax.management.AttributeNotFoundException jmxex) {
           _logger.log(Level.WARNING,"transaction.monitor.attribute_not_found",jmxex);
          // log and forget. Should not happen
        }
        return new CountStatisticImpl(count.longValue(), 
                                      "ActiveCount", 
                                      // "getActiveCount", 
                                      CountStatisticImpl.DEFAULT_UNIT, 
                                      "number of active transactions", 
                                       System.currentTimeMillis(), 
                                       mBean.getStartTime());
    }

    public CountStatistic getCommittedCount() {
        Integer count = null; 
        try {
            count = (Integer)mBean.getAttribute(JTSMonitorMBean.NUM_TRANSACTIONS_COMPLETED); 
        }catch (javax.management.AttributeNotFoundException jmxex) {
           _logger.log(Level.WARNING,"transaction.monitor.attribute_not_found",jmxex);
          // log and forget. Should not happen
        }
        return new CountStatisticImpl(count.longValue(), 
                                      "CommittedCount", 
                                      //"getCommittedCount", 
                                      CountStatisticImpl.DEFAULT_UNIT, 
                                      "number of committed transactions", 
                                       System.currentTimeMillis(), 
                                       mBean.getStartTime());
    }

    public CountStatistic getRolledbackCount() {
        Integer count = null; 
        try {
            count = (Integer)mBean.getAttribute(JTSMonitorMBean.NUM_TRANSACTIONS_ROLLEDBACK); 
        }catch (javax.management.AttributeNotFoundException jmxex) {
           _logger.log(Level.WARNING,"transaction.monitor.attribute_not_found",jmxex);
          // log and forget. Should not happen
        }
        return new CountStatisticImpl(count.longValue(), 
                                      "RolledbackCount", 
                                      //"getRolledbackCount", 
                                      CountStatisticImpl.DEFAULT_UNIT, 
                                      "number of rolled-back transactions", 
                                       System.currentTimeMillis(), 
                                       mBean.getStartTime());
    }

    public void freeze() {
        mBean.freeze();
    }

    public void unfreeze() {
        mBean.unfreeze();
    }

	/**
	 * method for rolling back a single transaction
	 * @param txnId String representing the Id of the transaction to be
	 *				roled back
	 */
	public String rollback(String txnId) {
        String  result = (String) mBean.setRollback(txnId);
        if (_logger.isLoggable(Level.FINE))
		    _logger.log(Level.FINE, result);
        return result;
	}
		
	
    /**
	 public String[] rollback(String[] txnIds) {
        return mBean.rollback(txnIds);    
    }
	 */


    public Statistic getStatistic(String statisticName) {
        return gStatsDelegate.getStatistic(statisticName);
    }

    public String[] getStatisticNames() {
        return gStatsDelegate.getStatisticNames();
    }

    public Statistic[] getStatistics() {
        return gStatsDelegate.getStatistics();
    }

	public void changeLevel(MonitoringLevel from, MonitoringLevel to, MonitoredObjectType type) {
        if (from != to) {
            _logger.log(Level.FINE,"JTAStats Monitoring level changed from " + from + "  to  " + to);
            if (to == MonitoringLevel.OFF) {
                mBean.stopMonitoring();
            }
            else if (to == MonitoringLevel.LOW || to == MonitoringLevel.HIGH) {
                mBean.startMonitoring();
            }
        }
	}	

    public List<Map<String, String>> listActiveTransactions() {
        return mBean.listActiveTransactions();
    }

    public Boolean isRecoveryRequired() {
        return RecoveryManager.isIncompleteTxRecoveryRequired();
    }
}
