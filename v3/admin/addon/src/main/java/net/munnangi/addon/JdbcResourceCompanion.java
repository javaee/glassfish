package net.munnangi.addon;

import org.jvnet.hk2.annotations.CompanionOf;
import com.sun.enterprise.config.serverbeans.JdbcResource;

@CompanionOf(JdbcResource.class)
public class JdbcResourceCompanion implements Runtime {

    public String getName() {
        System.out.println("JdbcResourceCompanion: getName: M S Reddy ...");
        return null;
    };

    public Object getParent() {return null;}
}
