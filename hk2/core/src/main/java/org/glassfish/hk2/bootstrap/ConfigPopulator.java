package org.glassfish.hk2.bootstrap;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ConfigPopulator {
    
    public void populateConfig(ServiceLocator serviceLocator);

}
