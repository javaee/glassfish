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
 * GMSFailEvent.java
 *
 */

package com.sun.enterprise.ee.selfmanagement.events;

import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.core.GMSFactory;
import com.sun.enterprise.ee.cms.core.FailureNotificationActionFactory;
import com.sun.enterprise.ee.cms.core.FailureNotificationAction;
import com.sun.enterprise.ee.cms.core.ActionException;
import com.sun.enterprise.ee.cms.core.GMSException;
import com.sun.enterprise.ee.cms.core.Action;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.server.core.AdminService;


/**
 *
 * @author sankara rao bhogi
 */
public class GMSFailEvent implements FailureNotificationActionFactory,
        FailureNotificationAction {
    
    /** Creates a new instance of GMSEvent */
    private  GMSFailEvent(GMSEventProxy proxy) {
        this.proxy = proxy;
    }
    
    static synchronized GMSFailEvent getInstance(GMSEventProxy proxy) {
        if (_instance != null)
            return _instance;
        else {
            _instance = new GMSFailEvent(proxy);
            String currentServer = ApplicationServer.getServerContext().getInstanceName();
            ConfigContext context  =  AdminService.getAdminService().getAdminContext().getAdminConfigContext();
            if(AdminService.getAdminService().isDas()) {
                try {
                    Cluster[] clusters = (Cluster[]) ClusterHelper.getClustersInDomain(context);
                    for (int i = 0; i < clusters.length; i++) {
                        String clusterName = clusters[i].getName();
                        if(clusterName != null) {
                            GroupManagementService gms = GMSFactory.getGMSModule(clusterName);
                            gms.addActionFactory(_instance);
                        }
                    }
                } catch (ConfigException ex) {
                } catch (GMSException ex) {
                }
            } else {
                try {
                    if( currentServer != null) {
                        String clusterName = ((Cluster)(ClusterHelper.getClusterForInstance(context,currentServer))).getName();
                        if(clusterName != null) {
                            GroupManagementService gms = GMSFactory.getGMSModule(clusterName);
                            gms.addActionFactory(_instance);
                        }
                    }
                } catch (ConfigException ex) {
                } catch (GMSException ex) {
                }
            }
            return _instance;
        }
    }
    
    
    public void consumeSignal(Signal s) throws ActionException { 
        String memberToken = s.getMemberToken();
        if (proxy != null)
            proxy.eventOccurred(memberToken,GMSEvent.FAIL_EVENT);
    }
    
    public Action produceAction() {
        return _instance;
    }
    
    private static volatile GMSFailEvent _instance = null;
    private final GMSEventProxy proxy;
    
}
