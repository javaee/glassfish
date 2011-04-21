package org.glassfish.vmcluster;

import org.glassfish.api.Startup;
import org.glassfish.vmcluster.spi.*;
import org.glassfish.vmcluster.config.GroupConfig;
import org.glassfish.vmcluster.config.Virtualizations;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.*;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Service the looks up the machines in the configured groups.
 *
 *
 */
@Service
public class GroupMembersPopulator implements Startup, PostConstruct, GroupManagement, ConfigListener {

    @Inject
    ShellExecutor shell;

    @Inject(optional=true)
    Virtualizations virtualizations = null;

    @Inject
    OsInterface os;

    @Inject
    Habitat habitat;

    private final Map<String, PhysicalGroup> groups = new HashMap<String, PhysicalGroup>();

    @Override
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    @Override
    public Iterator<PhysicalGroup> iterator() {
        return groups.values().iterator();
    }

    @Override
    public PhysicalGroup byName(String groupName) {
        return groups.get(groupName);
    }

    @Override
    public void postConstruct() {
        // first executeAndWait the fping command to populate our arp table.
        if (virtualizations==null) return;

        for (GroupConfig groupConfig : virtualizations.getGroupConfigs()) {
            try {
                PhysicalGroup group = processGroupConfig(groupConfig);
                System.out.println("I have a group " + group.getName());
                for (Machine machine : group.machines()) {
                    System.out.println("LibVirtMachine  " + machine.getName() + " is at " +  machine.getIpAddress() + " state is " + machine.getState());
                    if (machine.getState().equals(Machine.State.READY)) {
                        try {
                            System.out.println(machine.toString());
                        } catch (Exception e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    private PhysicalGroup processGroupConfig(GroupConfig groupConfig) {

        PhysicalGroup group = habitat.getComponent(PhysicalGroup.class, groupConfig.getVirtualization().getName());
        group.setConfig(groupConfig);
        synchronized (this) {
            groups.put(groupConfig.getName(), group);
        }
        return group;
    }

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        return ConfigSupport.sortAndDispatch(propertyChangeEvents, new Changed() {
            @Override
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> tClass, T t) {
                try {
                    GroupConfig groupConfig = GroupConfig.class.cast(t);
                    if (type.equals(TYPE.ADD)) {
                        processGroupConfig(groupConfig);
                    }
                    if (type.equals(TYPE.REMOVE)) {
                        synchronized (this) {
                            groups.remove(groupConfig.getName());
                        }
                    }
                } catch(ClassCastException e) {
                    // don't care
                }
                return null;
            }
        }, Logger.getAnonymousLogger());
    }
}
