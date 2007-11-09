package com.sun.enterprise.v3.rails;

import com.sun.enterprise.v3.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 * JRuby sniffer
 */
@Service(name="rails")
@Scoped(Singleton.class)
public class RailsSniffer extends GenericSniffer implements Sniffer {

    public RailsSniffer() {
        super("jruby", "app/controllers/application.rb", null);
    }
    

    final String[] deployers = { "com.sun.enterprise.rails.RailsDeployer" };
        
    public String[] getDeployersNames() {
        return deployers;
    }    
}
