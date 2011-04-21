package org.glassfish.vmcluster.runtime;

import com.sun.enterprise.config.serverbeans.Cluster;
import org.glassfish.vmcluster.spi.VirtualMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runtime representation of a virtual cluster.
 */
public class VirtualCluster {

    final Cluster config;
    final AtomicInteger token = new AtomicInteger();
    final List<VirtualMachine> vms = new ArrayList<VirtualMachine>();

    VirtualCluster(Cluster config) {
        this.config = config;
        token.set(config.getServerRef().size());
    }

    public Cluster getConfig() {
        return config;
    }

    public int allocateToken() {
        return token.addAndGet(1);
    }

    public synchronized void add(VirtualMachine vm) {
        vms.add(vm);
    }

    public synchronized void remove(VirtualMachine vm) {
        vms.remove(vm);
    }


}
