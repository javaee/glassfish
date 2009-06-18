package org.glassfish.ejb.embedded;

import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Jerome Dochez
 */
@Service
public class EjbEmbeddedContainer implements EmbeddedContainer {

    @Inject
    Habitat habitat;

    public List<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<Sniffer>();
        sniffers.add(habitat.getComponent(Sniffer.class, "Ejb"));
        Sniffer security = habitat.getComponent(Sniffer.class, "Security");
        System.out.println("SEcurity is " + security);
        if (security!=null) {
            sniffers.add(security);
        }
        return sniffers;
    }

    public void start() {
    }

    public void stop() {

    }
}
