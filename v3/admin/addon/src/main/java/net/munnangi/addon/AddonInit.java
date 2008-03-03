package net.munnangi.addon;

import org.glassfish.internal.api.Init;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

@Service
public class AddonInit implements Init, PostConstruct {

    public void postConstruct() {
        System.out.println("MSR: addon init postConstruct() ...");
    }
}
