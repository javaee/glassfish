package org.glassfish.javaee.core.deployment;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.deployment.GenericSniffer;

/**
 * Ear sniffers snifs ear files.
 *
 * @author Jerome Dochez
 */
@Service(name="ear")
public class EarSniffer extends GenericSniffer {


    public EarSniffer() {
        super("ear", "META-INF/application.xml", null);
    }

    public String[] getContainersNames() {
        return new String[] { "org.glassfish.javaee.core.deployment.EarContainer"};
    }                                                                              
}

