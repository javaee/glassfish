package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

@Service
public class ConfigurationPopulator {
    
    public void populateHabitat(Habitat habitat) {
        for (Populator p : habitat.<Populator>getAllServices(Populator.class)) {
            System.out.println("Found populator: " + p.getClass().getName());
            p.run(new ConfigParser(habitat));
        }
    }

}
