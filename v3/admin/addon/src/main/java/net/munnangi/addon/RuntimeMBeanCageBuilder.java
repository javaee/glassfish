package net.munnangi.addon;

import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;

public class RuntimeMBeanCageBuilder implements CageBuilder {
    public void onEntered(Inhabitant<?> i) {
        System.out.println("RuntimeMBeanCageBuilder: onEntered: time to create and register the mBean ...");
    }
}
