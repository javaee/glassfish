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
package com.sun.enterprise.resource.monitor;

//removed unnecessary imports
import java.util.logging.Logger;

import javax.management.j2ee.statistics.Stats;

import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.connectors.work.monitor.ConnectorWorkMonitoringLevelListener;
import com.sun.logging.LogDomains;


/**
 * Provides an implementation of the MonitoringLevelListener interface to
 * receive callbacks from admin regarding change in the connector-service
 * monitoring level.
 * 
 * Though there are 3 monitoring levels defined by JSR77, we support
 * only 2 levels - OFF and ON (HIGH/LOW). So essentially, HIGH and LOW
 * for us is only ON
 * 
 * @author Sivakumar Thyagarajan
 * @since sjsas pe/se/ee 8.1
 */

public class ConnectorServiceMonitoringLevelListener 
                            implements MonitoringLevelListener {

    private ConnectorPoolMonitoringLevelListener ccpPoolMonitoringLevelListener_;
    private ConnectorWorkMonitoringLevelListener cwMonitoringLevelListener_;

    public ConnectorServiceMonitoringLevelListener(){
        ccpPoolMonitoringLevelListener_ = new ConnectorPoolMonitoringLevelListener();
        cwMonitoringLevelListener_ = new ConnectorWorkMonitoringLevelListener();
    }


    /**
     * @see com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener#setLevel(com.sun.enterprise.admin.monitor.registry.MonitoringLevel)
     * @deprecated
     */
    //remove after admin removes it
    public void setLevel( MonitoringLevel level ) {}
    
    /**
     * This is the callback invoked by the MonitoringRegistry
     * on change in monitoring level
     * 
     * @param from - the past level
     * @param to - the new level
     * @param handback - the Stats object this listener was registered for
     * @deprecated 
     */
    public void changeLevel( MonitoringLevel from, MonitoringLevel to,
        Stats handback ) {}

    public void changeLevel(MonitoringLevel from, MonitoringLevel to, 
            MonitoredObjectType type) {
        /*        
         * For backward compatability reasons: When connector connection pool or 
         * the JMS-Service module monitoring level is updated, the connector 
         * service's module monitoring level listener is updated and vice-versa.
         * 
         * Change levels in connector connection pools and
         * connector work management level listeners as well.
         */        
        ccpPoolMonitoringLevelListener_.changeLevel(from, to, type);
        cwMonitoringLevelListener_.changeLevel(from, to, type);
    }
}