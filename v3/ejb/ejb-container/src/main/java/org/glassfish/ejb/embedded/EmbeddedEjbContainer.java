package org.glassfish.ejb.embedded;

import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.beans.PropertyVetoException;

import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.Config;

/**
 * @author Jerome Dochez
 */
public class EmbeddedEjbContainer implements EmbeddedContainer {

    final Habitat habitat;
    

    EmbeddedEjbContainer(EjbBuilder builder) {
        this.habitat = builder.habitat;
    }

    public void bind(Port port, String protocol) {

    }

    public List<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<Sniffer>();
        addSniffer(sniffers, "Ejb");
        addSniffer(sniffers, "Security");
        addSniffer(sniffers, "jpa");
        addSniffer(sniffers, "jpaCompositeSniffer");
        addSniffer(sniffers, "ear");
        return sniffers;

    }

    private void addSniffer(List<Sniffer> sniffers, String name) {
        Sniffer sniffer = habitat.getComponent(Sniffer.class, name);
        if (sniffer != null) {
            sniffers.add(sniffer);
        }
    }

    public void start() throws LifecycleException {
    }

    public void stop() throws LifecycleException {

    }

}
