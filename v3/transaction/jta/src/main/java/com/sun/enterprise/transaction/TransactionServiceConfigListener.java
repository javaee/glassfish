
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
package com.sun.enterprise.transaction;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.beans.PropertyChangeEvent;

import com.sun.logging.LogDomains;

import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.annotations.Inject;

/*
 * Listener for TransactionService dynamic configuration.
 */
public class TransactionServiceConfigListener implements ConfigListener
{
     // Logger to log transaction messages
    static Logger _logger = LogDomains.getLogger(LogDomains.JTA_LOGGER);

    @Inject private JavaEETransactionManagerSimplified tm;

    public synchronized void changed(PropertyChangeEvent[] events) {

        ConfigSupport.sortAndDispatch(events, new Changed() {
            public <T extends ConfigBeanProxy> void changed(TYPE type, Class<T> tClass, T t) {
               System.err.println("XXXX tClass ======= " + tClass);
               if (type==TYPE.ADD || type==TYPE.REMOVE) {
                   // XXX throw new RuntimeException(???)
               } else if (type==TYPE.CHANGE) {
                   System.err.println("======CHANGE======");
               }
            }
        }
        , _logger);

        for (PropertyChangeEvent event : events) {
            System.err.println("XXX event ======== "+event.getSource()+ " "+event.getPropertyName()+" "+
                    event.getOldValue()+" "+event.getNewValue());
        }
    }

/** XXX
    public String xPath = ServerXPathHelper.XPATH_TRANSACTION_SERVICE;

    private static ConfigChangeCategory category = new ConfigChangeCategory(
            "jts", ServerXPathHelper.REGEX_XPATH_CONFIG
                    + ServerXPathHelper.XPATH_SEPARATOR
                    + ServerXPathHelper.REGEX_ONE_PLUS 
                    + ServerTags.TRANSACTION_SERVICE + ".*"); 
    public static ConfigChangeCategory getCategory() { 
        return category; 
    }

    public void handleCreate(JTSEvent event) throws AdminEventListenerException {
        throw new AdminEventListenerException("handleCreate is not valid for JTSEvent");
    }

    public void handleDelete(JTSEvent event) throws AdminEventListenerException {
        throw new AdminEventListenerException("handleDelete is not valid for JTSEvent");
    }

    public void handleUpdate(JTSEvent event) throws AdminEventListenerException {
        //Bug Id: 4666390 Handle no event in event list case
        if(event==null){
            //do nothing
            return;
        }
        ArrayList configChangeList = event.getConfigChangeList();
        if(configChangeList==null){
            //do nothing
            return;
        }
        //Bug Id: 4666390 End

        ConfigUpdate configUpdate = null;
        boolean match = false;
        for (int i = 0; i < configChangeList.size(); i++) {
            configUpdate = (ConfigUpdate) configChangeList.get(i);
             if (configUpdate.getXPath() != null &&
                configUpdate.getXPath().endsWith(ServerTags.TRANSACTION_SERVICE)) {
                if (xPath.equals(configUpdate.getXPath())) {
                    match = true;
                    break;
                }
             }
        }
        if (match) { // TransactionService has been changed



            Set attributeSet = configUpdate.getAttributeSet();
            String next = null;
            for (Iterator iter = attributeSet.iterator(); iter.hasNext();) {
                next = (String) iter.next();
                if (next.equals(ServerTags.TIMEOUT_IN_SECONDS)) {
                    _logger.log(Level.FINE," Transaction Timeout interval event occurred");
                    String oldTimeout = configUpdate.getOldValue(ServerTags.TIMEOUT_IN_SECONDS);
                    String newTimeout = configUpdate.getNewValue(ServerTags.TIMEOUT_IN_SECONDS);
                    if (oldTimeout.equals(newTimeout)) {
                    }
                    else {
                        try {
                            Switch.getSwitch().getTransactionManager().setDefaultTransactionTimeout(
                                                                        Integer.parseInt(newTimeout,10));
                        } catch (Exception ex) {
                            _logger.log(Level.WARNING,"transaction.reconfig_txn_timeout_failed",ex);
                        }
                    } // timeout-in-seconds
                }else if (next.equals(ServerTags.KEYPOINT_INTERVAL)) {
                    _logger.log(Level.FINE,"Keypoint interval event occurred");
                    String oldKeyPoint = configUpdate.getOldValue(ServerTags.KEYPOINT_INTERVAL);
                    String newKeyPoint = configUpdate.getNewValue(ServerTags.KEYPOINT_INTERVAL);
                    if (oldKeyPoint.equals(newKeyPoint)) {
                    }
                    else {
                        Configuration.setKeypointTrigger(Integer.parseInt(newKeyPoint,10));
                    }
                }else if (next.equals(ServerTags.RETRY_TIMEOUT_IN_SECONDS)) {
                    String oldRetryTiemout = configUpdate.getOldValue(ServerTags.RETRY_TIMEOUT_IN_SECONDS);
                    String newRetryTiemout = configUpdate.getNewValue(ServerTags.RETRY_TIMEOUT_IN_SECONDS);
                    _logger.log(Level.FINE,"retry_timeout_in_seconds reconfig event occurred " + newRetryTiemout);
                    if (oldRetryTiemout.equals(newRetryTiemout)) {
                    }
                    else {
                        Configuration.setCommitRetryVar(newRetryTiemout);
                    }
                }
                else {
                    // Not handled dynamically. Restart is required.
                    AdminEventMulticaster.notifyFailure(event, AdminEventResult.RESTART_NEEDED);
                }
** XX **/
                
                /*
        //This feature is currently dropped as it's not implemented totally
        else if (next.equals("commit-retries")) {
                    String oldCommitRetries = configUpdate.getOldValue("commit-retries");
                    String newCommitRetries = configUpdate.getNewValue("commit-retries");
                    if (oldCommitRetries.equals(newCommitRetries)) {
                    }
                    else {
                        Configuration.setCommitRetryVar(newCommitRetries);
                    }
                } // commit-retries
                */

/** XX **
            }
        }

    }
** XX **/
}
