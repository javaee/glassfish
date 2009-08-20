/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.transaction.monitoring.config;

import org.glassfish.api.monitoring.MonitoringItem;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.config.serverbeans.MonitoringService;
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;

/**
 * Config upgrade for monitoring module
 *
 * @author Sreenivas Munnangi
 */
@Service
public class TransactionServiceConfigUpgrade implements ConfigurationUpgrade, PostConstruct {

    @Inject
    MonitoringService ms;

    private String level = MonitoringItem.LEVEL_OFF;
    private String name = MonitoringItem.TRANSACTION_SERVICE;

    public void postConstruct() {
        try {
            // get the level attribute from module-monitoring-levels
            if (ms == null) {
                Logger.getAnonymousLogger().log(Level.WARNING, 
                    "Failure while upgrading domain.xml, monitoring-service does not exist in domain.xml");
                return;
            }
            ModuleMonitoringLevels mmls = ms.getModuleMonitoringLevels();
            if (mmls != null) {
                level = mmls.getTransactionService();
            }

            // if monitoring-item exists set level 
            // otherwise create element and set level
            MonitoringItem mi = getMonitoringItem(ms);
            if (mi != null) {
                ConfigSupport.apply(new SingleConfigCode<MonitoringItem>() {
                    public Object run(MonitoringItem param) 
                    throws PropertyVetoException, TransactionFailure {
                        param.setLevel(level);
                        return param;
                    }
                }, mi);
            } else {
                ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {
                    public Object run(MonitoringService param) 
                    throws PropertyVetoException, TransactionFailure {
                        MonitoringItem newItem = param.createChild(
                            com.sun.enterprise.transaction.monitoring.config.TransactionServiceMI.class);
                        newItem.setName(name);
                        newItem.setLevel(level);
                        param.getMonitoringItems().add(newItem);
                        return newItem;
                    }
                }, ms);
            }
        } catch (TransactionFailure tf) {
            Logger.getAnonymousLogger().log(Level.SEVERE, 
                "Failure while upgrading domain.xml monitoring-service", tf);
            throw new RuntimeException(tf);
        }
    }

    private MonitoringItem getMonitoringItem(MonitoringService ms) {
        for (MonitoringItem mi : ms.getMonitoringItems()) {
            if (mi.getName().equals(name)) {
                return mi;
            }
        }
        return null;
    }
}

