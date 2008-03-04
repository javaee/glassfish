package net.munnangi.addon;

import com.sun.enterprise.v3.server.Init;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.annotations.Service;

@Service
public class AddonInit implements Init, PostConstruct {

    public void postConstruct() {
        System.out.println("MSR: addon init postConstruct() ...");
    }
}
