package org.glassfish.webservices;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.PostConstruct;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import java.util.logging.Logger;


/**
 * This is the interface which will define the contract
 * to obtain ComponentInvocationManger etc 
 */
@Contract
public interface WebServicesContract {

    public ComponentEnvManager getComponentEnvManager();

    public Logger getLogger();
    
}
