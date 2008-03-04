package net.munnangi.addon;

import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;

@Service
public class RuntimeMBeanCageBuilder implements CageBuilder {
    public void onEntered(Inhabitant<?> i) {
        System.out.println("RuntimeMBeanCageBuilder: onEntered("+i.typeName()+"): time to create and register the mBean ...");

        Object o = i.get();
        if (o instanceof net.munnangi.addon.AddonCompanion) {
            System.out.println("instance of AddonCompanion ...");
            System.out.println("getname = " + ((net.munnangi.addon.AddonCompanion)o).getName());
        } else if (o instanceof net.munnangi.addon.ConfigCompanion) {
            System.out.println("instance of ConfigCompanion ...");
            System.out.println("getname = " + ((net.munnangi.addon.ConfigCompanion)o).getName());
        }

            System.out.println("list of companions size ..." + i.companions().size());
        for (Inhabitant c : i.companions()) {
            o = c.get();
            System.out.println("next companion = " + c.getClass().getName());
        }
    }
}
