package org.glassfish.api.embedded;

import org.jvnet.hk2.annotations.Contract;

import java.io.IOException;
import java.util.Collection;

/**
 * Management interfaces for all embedded ports
 *
 * @author Jerome Dochez
 */
@Contract
public interface Ports {


    /**
     * Creates a port, binds it to a port number and returns it
     * @param number the port number
     * @return the bound port to the port number
     * @throws IOException if the port is already taken or another network exception occurs
     */
    Port createPort(int number) throws IOException;


    /**
     * Returns the list of allocated ports
     *
     * @return the allocated ports
     */
    Collection<Port> getPorts();
}
