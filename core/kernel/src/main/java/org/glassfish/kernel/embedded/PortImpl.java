package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.ActionReport;

import java.util.Properties;

/**
 * Abstract to port creation and destruction
 */
@Service
public class PortImpl implements Port {

    @Inject
    CommandRunner runner=null;

    @Inject(name="plain")
    ActionReport report=null;
    
    public void bind(int portNumber) {

        Properties props = new Properties();
        props.put("port", Integer.toString(portNumber));
        props.put("host", "localhost");
        props.put("defaulvs", "server");
        runner.doCommand("create-http-listener", props, report);
    }

    public void unbind() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
