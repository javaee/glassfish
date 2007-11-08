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

package com.sun.enterprise.admin.selfmanagement.event;
import javax.management.ObjectName;
import javax.management.NotificationFilter;
import javax.management.monitor.Monitor;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;


public class MonitorEvent implements Event {
    private static Logger _logger=LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    private static StringManager sm = StringManager.getManager(MonitorEvent.class);
    
    MonitorEvent(StatisticMonitor monitor, ObjectName objName, String description) {
        monitor.start();
        try {
            MBeanServerFactory.getMBeanServer().registerMBean(monitor,objName);
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"smgt.internal_error", ex);
        }
        
        this.sMonitor = monitor;
        this.objName = objName;
        if (description != null)
            this.description = description;
        else
            this.description = defaultDescription;
    }

    MonitorEvent(Monitor monitor, ObjectName objName, String description) {
        monitor.start();
        try {
            MBeanServerFactory.getMBeanServer().registerMBean(monitor,objName);
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"smgt.internal_error", ex);
        }

        this.monitor = monitor;
        this.objName = objName;
        if (description != null)
            this.description = description;
        else
            this.description = defaultDescription;
    }
    
    
    public ObjectName getObjectName() {
        return objName;
    }
    public String getType() {
        return "monitor";
    }
    public NotificationFilter getNotificationFilter() {
        return null;
    }
    public String getDescription() {
        return description;
    }
    public void destroy() {
        try {
        if (monitor != null)
            monitor.stop();
        if (sMonitor != null)
            sMonitor.stop();
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"smgt.internal_error", ex);
        }
        try {
            MBeanServerFactory.getMBeanServer().unregisterMBean(objName);
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"smgt.internal_error", ex);
        }
    }
    private ObjectName objName = null;
    private static String description = null;
    private static String defaultDescription = sm.getString("selfmgmt_event.monitor_event_description");
    private StatisticMonitor sMonitor = null;
    private Monitor monitor = null;
}
