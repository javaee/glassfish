package org.glassfish.webservices;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.internal.api.Globals;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the implementation class which will provide the implementation
 * to access the injected fields like the NamingManager , ComponentEnvManager
 */
@Service
public class WebServiceContractImpl implements WebServicesContract{

    @Inject
    private ComponentEnvManager compEnvManager;

    private  static WebServiceContractImpl wscImpl;

    private Logger logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    public ComponentEnvManager getComponentEnvManager() {
        return compEnvManager;  
    }

    public static WebServiceContractImpl getInstance() {
        // Create the instance first to access the logger.
        wscImpl = Globals.getDefaultHabitat().getComponent(
                WebServiceContractImpl.class);

        return wscImpl;
    }

    public Logger getLogger() {
        return logger;
    }
}
