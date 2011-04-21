package org.glassfish.vmcluster.runtime;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages instances of virtual clusters
 */
@Service
public class VirtualClusters {

    final Domain domain;
    final Map<String, VirtualCluster> clusterMap = new HashMap<String, VirtualCluster>();

    public VirtualClusters(@Inject Domain domain) {
        this.domain = domain;
    }

    public void add(VirtualCluster vc) {
        clusterMap.put(vc.getConfig().getName(), vc);
    }

    public void remove(VirtualCluster vc) {
        clusterMap.remove(vc.getConfig().getName());
    }

    public VirtualCluster byName(String clusterName) {
        if (!clusterMap.containsKey(clusterName)) {
            Cluster cluster = domain.getClusterNamed(clusterName);
            if (cluster==null) {
                throw new InvalidParameterException(clusterName + " cluster not found");
            }
            synchronized (this) {
                if (!clusterMap.containsKey(clusterName)) {
                    clusterMap.put(clusterName, new VirtualCluster(cluster));
                }
            }
        }
        return clusterMap.get(clusterName);
    }

}
