package net.munnangi.addon;

import org.jvnet.hk2.annotations.CompanionOf;
import com.sun.enterprise.config.serverbeans.Config;

@CompanionOf(Config.class)
public class ConfigCompanion implements Runtime {

    public String getName() {
        System.out.println("ConfigCompanion: getName: M S Reddy ...");
        return null;
    }

    public Object getParent() {return null;}
}
