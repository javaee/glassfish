package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.embedded.Ports;
import org.glassfish.api.embedded.Port;

import java.io.IOException;
import java.util.*;

import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;

/**
 * @author Jerome Dochez
 */
@Service
public class PortsImpl implements Ports {


    @Inject
    NetworkConfig network;

    @Inject
    Habitat habitat;

    final Map<Integer, Port> ports = new HashMap<Integer, Port>();

    public Port open(int number) throws IOException {
        return open(Integer.valueOf(number));
    }

    private Port open(Integer portNumber) throws IOException {

        for (NetworkListener nl : network.getNetworkListeners().getNetworkListener()) {
            if (nl.getPort().equals(portNumber.toString())) {
                throw new IOException("Port " + portNumber + " is already configured");
            }
        }
        for (Integer pn : ports.keySet()) {
            if (pn.equals(portNumber)) {
                throw new IOException("Port " + portNumber + " is alredy open");
            }
        }
        Port port = habitat.getComponent(Port.class);
        port.bind(portNumber);
        ports.put(portNumber, port);
        return port;    }

    public Collection<Port> getPorts() {
        return ports.values();
    }

    public void close(Port port) {
        port.unbind();
    }

    public void remove(Port port) {
        ports.remove(port.getPortNumber());
    }
}
