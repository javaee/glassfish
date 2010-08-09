/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 */
package org.glassfish.gms;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigSupport;
import org.glassfish.api.admin.config.ConfigurationUpgrade ;

import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Cluster;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyVetoException;

/**
 * Startup service to upgrade cluster/gms elements in domain.xml
 * @author Bhakti Mehta
 *
 */

@Service
public class GMSConfigUpgrade implements ConfigurationUpgrade, PostConstruct {
    @Inject
    Clusters clusters;


    public void postConstruct() {
        //This will upgrade all the cluster elements in the domain.xml
        upgradeClusterElements();

    }

    private void upgradeClusterElements (){
        try {
            List<Cluster> clusterList = clusters.getCluster();
            for (Cluster cl :clusterList) {
                ConfigSupport.apply(new ClusterConfigCode(), cl);
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                    "Failure while upgrading cluster data from V2 to V3", e);
            throw new RuntimeException(e);
        }
    }

    private class ClusterConfigCode implements SingleConfigCode<Cluster> {
        public Object run(Cluster cluster) throws PropertyVetoException, TransactionFailure {
            //set gms-enabled (default is true incase it may not appear in upgraded
            //domain.xml)
            cluster.setGmsEnabled(cluster.getHeartbeatEnabled());

            //set gms-multicast-address the value obtained from heartbeat-address
            cluster.setGmsMulticastAddress(cluster.getHeartbeatAddress());

            //set gms-multicast-port the value of heartbeat-port
            cluster.setGmsMulticastPort(cluster.getHeartbeatPort());

            //gms-bind-interface is an attribute of cluster in 3.1
            cluster.setGmsBindInterfaceAddress(String.format(
                "${GMS-BIND-INTERFACE-ADDRESS-%s}",
                cluster.getName()));
           return cluster;
        }
    }
}
