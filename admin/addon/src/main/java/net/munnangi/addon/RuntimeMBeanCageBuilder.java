package net.munnangi.addon;

import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;

@Service
public class RuntimeMBeanCageBuilder implements CageBuilder {
    public void onEntered(Inhabitant<?> i) {
        System.out.println("RuntimeMBeanCageBuilder: onEntered("+i.typeName()+"): time to create and register the mBean ...");
    }
}
