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
 * RegistrySynchronizer.java
 *
 * Created on August 21, 2005, 8:00 PM
 *
 */

package com.sun.enterprise.ee.admin.clientreg;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;
import javax.management.Notification;
import javax.management.NotificationListener;

import com.sun.enterprise.ee.cms.logging.GMSLogDomain;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.util.i18n.StringManager;

public class RegistrySynchronizer implements NotificationListener {

    private static final StringManager _strMgr =
        StringManager.getManager(RegistrySynchronizer.class);
    
    private static final String[] types =  {
        "cluster.health.INSTANCE_STARTED_EVENT",
        "cluster.health.INSTANCE_STOPPED_EVENT",
        "cluster.health.INSTANCE_FAILED_EVENT"
    };

    private static Logger _logger = null;                

    private static Logger getLogger() {
        if (_logger == null) 
            _logger = Logger.getLogger(GMSLogDomain.GMS_LOGGER);
        return _logger;
    }

    public void handleNotification(
        final Notification notif, final Object handback) {

        try {
        final ObjectName objectName = (ObjectName) notif.getSource();
        final String serverName = (String) notif.getUserData();
        final String type = notif.getType();
        if (type.equals(types[0]))//cluster.health.INSTANCE_STARTED_EVENT
            instanceStarted(serverName);
        else if (type.equals(types[1]))//cluster.health.INSTANCE_STOPPED_EVENT
            instanceStopped(serverName);
        else if (type.equals(types[1]))//cluster.health.INSTANCE_FAILED_EVENT
            instanceFailed(serverName);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, 
                "RegistrySynchronizer : handleNotification failed");
        }
    }

    private void instanceStarted(String serverName) {
        try {
            InstanceRegistry instanceRegistry = 
                InstanceRegistry.getInstanceRegistry();
            instanceRegistry.getInstanceConnection(serverName);
        } catch (InstanceException ex) {
            getLogger().log(Level.WARNING, _strMgr.getString( 
                "gms.activeMngtFailedForServer", serverName));
        }
    }

    private void instanceStopped(String serverName) {
        //nothing for now
    }

    private void instanceFailed(String serverName) {
        try {
            InstanceRegistry instanceRegistry = 
                InstanceRegistry.getInstanceRegistry();
            instanceRegistry.removeInstanceConnection(serverName);
        } catch (InstanceException ex) {
            getLogger().log(Level.WARNING, 
                _strMgr.getString( "gms.activeMngtFailed", serverName));            
        }
    }    
}
