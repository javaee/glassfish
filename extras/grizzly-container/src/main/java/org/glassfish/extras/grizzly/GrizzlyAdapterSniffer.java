package org.glassfish.extras.grizzly;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.container.Sniffer;
import org.glassfish.internal.deployment.GenericSniffer;

/**
 * Sniffs raw grizzly adapters in jar files
 *
 * @author Jerome Dochez
 */
@Service(name="grizzly")
public class GrizzlyAdapterSniffer extends GenericSniffer implements Sniffer {
    
    final static private String[] containerNames = { "org.glassfish.extras.grizzly.GrizzlyContainer" };

    public GrizzlyAdapterSniffer() {
        super("grizzly", "META-INF/grizzly.xml",null);
    }

    public String[] getContainersNames() {
        return containerNames;
    }
}
