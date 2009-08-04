package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.ActionReport;

import java.util.Properties;

/**
 * Abstract to port creation and destruction
 */
@Service
@Scoped(PerLookup.class)
public class PortImpl implements Port {

    @Inject
    CommandRunner runner=null;

    @Inject(name="plain")
    ActionReport report=null;

    @Inject
    PortsImpl ports;

    String listenerName;
    int number;

    public void bind(int portNumber) {

        number = portNumber;
        Properties props = new Properties();
        props.put("listenerport", Integer.toString(portNumber));
        props.put("listeneraddress", "127.0.0.1");
        listenerName = getListenerName();
        props.put("listener_id", listenerName);
        props.put("servername", "");
        props.put("defaultvs", "server");
        runner.doCommand("create-http-listener", props, report);
    }

    private String getListenerName() {
        final String listenerNameBase = "embedded-listener";
        int i = 1;
        String listenerName = listenerNameBase;

        while (existsListener(listenerName)) {
            listenerName = "embedded-listener-" + i++;
        }
        return listenerName;
    }

    private boolean existsListener(String listenerName) {
        // FIX this to check if listenerName exists
        return false;
    }

    public void unbind() {
        Properties props = new Properties();
        props.put("listener_id", listenerName);
        runner.doCommand("delete-http-listener", props, report);
        ports.remove(this);
    }

    public int getPortNumber() {
        return number;
    }
}
