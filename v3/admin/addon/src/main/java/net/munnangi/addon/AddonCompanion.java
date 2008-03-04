package net.munnangi.addon;

import org.jvnet.hk2.annotations.CompanionOf;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import org.jvnet.hk2.annotations.Lead;

@CompanionOf(JdbcResource.class)
public class AddonCompanion implements Runtime {

    @Lead
    JdbcResource jdbcres;

    public String getName() {
        System.out.println("AddonCompanion: getName: M S Reddy ...");
        System.out.println("AddonCompanion: jdbcres = " + jdbcres);
        return null;
    }

    public Object getParent() {return null;}
}
