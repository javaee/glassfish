package org.glassfish.vmcluster.virtmgt.impl;

import org.glassfish.vmcluster.config.Template;
import org.glassfish.vmcluster.runtime.VirtualCluster;
import org.glassfish.vmcluster.spi.PhysicalGroup;
import org.glassfish.vmcluster.spi.VirtException;
import org.glassfish.vmcluster.spi.VirtualMachine;
import org.glassfish.vmcluster.virtmgt.GroupAccess;
import org.jvnet.hk2.component.Injector;

import java.util.concurrent.Future;

/**
 * Defines a local group access. This mean this process is configured to be a group master
 * and this implementation allows the current process to interface with it.
 * @author Jerome Dochez
 */
public class LocalGroupAccess implements GroupAccess {

    final PhysicalGroup group;

    static LocalGroupAccess from(Injector injector, PhysicalGroup group) {
        LocalGroupAccess instance = new LocalGroupAccess(group);
        return injector.inject(instance);
    }

    private LocalGroupAccess(PhysicalGroup group) {
        this.group = group;
    }

    @Override
    public Iterable<Future<VirtualMachine>> allocate(Template template, VirtualCluster cluster, int number) throws VirtException {
        return group.allocate(template, cluster,  number);
    }

    @Override
    public String getVirtualizationName() {
        return group.getConfig().getVirtualization().getName();
    }
}
