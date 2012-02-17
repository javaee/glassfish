/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package prober;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

// this is what ProberServlet shows at runtime:
//glassfish:kernel:connection-queue:connectionAcceptedEvent (java.lang.String listenerName,
// int connection, java.lang.String address)
public class ConnectionProbeListener {
    @ProbeListener(ACCEPT)
    public void probe(
            //java.lang.String listenerName, int connection, java.lang.String address
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection")   int connectionNumber,
            @ProbeParam("address")      String address) {
        System.out.printf("ConnectionProbeListener HERE.  name=%s, connection: %d, address: %s\n",
                listenerName, connectionNumber, address);
    }
    private static final String ACCEPT = "glassfish:kernel:connection-queue:connectionAcceptedEvent";
}
