package org.glassfish.ejb.embedded;

import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;

/**
 * @author Jerome Dochez
 */
@Service
public class EjbEmbeddedContainer implements EmbeddedContainer {

    @Inject
    Habitat habitat;

    public Sniffer[] getSniffers() {
        Sniffer[] s = { habitat.getComponent(Sniffer.class, "Ejb") };
        return s;
    }

    public void start() {
    }

    public void stop() {

    }
}
