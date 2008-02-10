package com.sun.enterprise.connectors.module;

import com.sun.enterprise.v3.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.io.IOException;
import java.util.logging.Logger;

@Service(name = "connectors")
@Scoped(Singleton.class)
public class ConnectorSniffer extends GenericSniffer implements Sniffer {

    public ConnectorSniffer() {
        super("connectors", "META-INF/ra.xml", null);
    }

    final String[] containerNames = {"com.sun.enterprise.connectors.module.ConnectorContainer"};

    /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     *
     * @param containerHome is where the container implementation resides
     * @param logger        the logger to use
     * @throws java.io.IOException exception if something goes sour
     */
    @Override
    public void setup(String containerHome, Logger logger) throws IOException {
        // do nothing, we are embedded in GFv3 for now
    }

    /**
     * Returns the list of Containers that this Sniffer enables.
     * <p/>
     * The runtime will look up each container implementing
     * using the names provided in the habitat.
     *
     * @return list of container names known to the habitat for this sniffer
     */
    public String[] getContainersNames() {
        return containerNames;
    }

    /**
     * Returns the Module type
     *
     * @return the container name
     */
    public String getModuleType() {
        return "connectors";
    }
}
