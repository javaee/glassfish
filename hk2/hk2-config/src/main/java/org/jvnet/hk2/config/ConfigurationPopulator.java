package org.jvnet.hk2.config;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.ConfigPopulator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import javax.inject.Inject;

@Service
public class ConfigurationPopulator
    implements ConfigPopulator {
    
    @Inject
    Habitat habitat;

    public void populateConfig(ServiceLocator serviceLocator) {
        for (Populator p : serviceLocator.<Populator>getAllServices(Populator.class)) {
            System.out.println("Found populator: " + p.getClass().getName());
            p.run(new ConfigParser(habitat));
        }
    }

}
