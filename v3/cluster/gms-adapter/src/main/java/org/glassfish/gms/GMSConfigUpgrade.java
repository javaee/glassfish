/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.gms;

import com.sun.enterprise.config.serverbeans.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigSupport;
import org.glassfish.api.admin.config.ConfigurationUpgrade ;
import org.jvnet.hk2.config.types.Property;

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

    @Inject
    Configs configs;


    public void postConstruct() {
        //This will upgrade all the cluster elements in the domain.xml
        upgradeClusterElements();

        // this will upgrade all the group-management-service elements in domain.xml
        upgradeGroupManagementServiceElements();
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

    private void upgradeGroupManagementServiceElements() {
        try {
            List<Config> lconfigs = configs.getConfig();
            for (Config c : lconfigs) {
                //Logger.getAnonymousLogger().log(Level.FINE, "Upgrade config " + c.getName());
                ConfigSupport.apply(new GroupManagementServiceConfigCode(), c);
            }
        } catch (Throwable t) {
            Logger.getAnonymousLogger().log(Level.SEVERE,"Failure while upgrading cluster data from V2 to V3", t);
            throw new RuntimeException(t);
        }
    }

    private class ClusterConfigCode implements SingleConfigCode<Cluster> {
        public Object run(Cluster cluster) throws PropertyVetoException, TransactionFailure {
            //set gms-enabled (default is true incase it may not appear in upgraded
            //domain.xml)
            cluster.setGmsEnabled(cluster.getHeartbeatEnabled());
            cluster.setHeartbeatEnabled(null);

            //set gms-multicast-address the value obtained from heartbeat-address
            cluster.setGmsMulticastAddress(cluster.getHeartbeatAddress());
            cluster.setHeartbeatAddress(null);

            //set gms-multicast-port the value of heartbeat-port
            cluster.setGmsMulticastPort(cluster.getHeartbeatPort());
            cluster.setHeartbeatPort(null);

            //gms-bind-interface is an attribute of cluster in 3.1
            Property prop  = cluster.getProperty("gms-bind-interface-address");
            if (prop != null && prop.getValue() != null) {
                cluster.setGmsBindInterfaceAddress(prop.getValue());
                List<Property> props = cluster.getProperty();
                props.remove(prop);
            } else {
                cluster.setGmsBindInterfaceAddress(String.format(
                        "${GMS-BIND-INTERFACE-ADDRESS-%s}",
                        cluster.getName()));
            }
            return cluster;
        }
    }

    private class GroupManagementServiceConfigCode implements SingleConfigCode<Config> {
        public Object run(Config config) throws PropertyVetoException, TransactionFailure {
            GroupManagementService gms = config.getGroupManagementService();
            Transaction t = Transaction.getTransaction(config);
            t.enroll(gms);
            String value = gms.getPingProtocolTimeoutInMillis();
            if (value != null) {
                gms.setGroupDiscoveryTimeoutInMillis(value);
            } // else null for server-config
            // workaround gf it 1303
            // gms.setPingProtocolTimeoutInMillis(null);

            FailureDetection fd = gms.getFailureDetection();
            t.enroll(fd);
            value = gms.getFdProtocolTimeoutInMillis();
            if (value != null){
                fd.setHeartbeatFrequencyInMillis(value);
            } // else  null for server-config
            // workaround gf it 1303
            //gms.setFdProtocolTimeoutInMillis(null);

            value = gms.getFdProtocolMaxTries();
            if (value != null) {
                fd.setMaxMissedHeartbeats(value);
            } // else null for server config
            // workaround gf it 1303
            //gms.setFdProtocolMaxTries(null);

            value = gms.getVsProtocolTimeoutInMillis();
            if (value != null) {
                fd.setVerifyFailureWaittimeInMillis(value);
            } // else null for server-config
            //gms.setVsProtocolTimeoutInMillis(null);

            Property prop = gms.getProperty("failure-detection-tcp-retransmit-timeout");
            if (prop != null && prop.getValue() != null ) {
                fd.setVerifyFailureConnectTimeoutInMillis(prop.getValue().trim());
                List<Property> props = gms.getProperty();
                props.remove(prop);
            } //else v3.1 default value for VerifyFailureConnectTimeoutInMillis is sufficient.

            // remove v2.1 attributes that are no longer needed.  No info to transfer to v3.1 gms config.
            // workaround gf it 1303
            //gms.setMergeProtocolMaxIntervalInMillis(null);
            //gms.setMergeProtocolMinIntervalInMillis(null);

            return config;
        }
    }
}
