package net.munnangi.addon;

import org.jvnet.hk2.annotations.CompanionOf;
import com.sun.enterprise.config.serverbeans.Config;
import org.jvnet.hk2.annotations.Lead;

@CompanionOf(Config.class)
public class ConfigCompanion implements Runtime {

    @Lead
    Config cfg;

    public String getName() {
        System.out.println("ConfigCompanion: getName: M S Reddy ...");
        System.out.println("AddonCompanion: Config = " + cfg);
        return null;
    }

    public Object getParent() {return null;}
}
