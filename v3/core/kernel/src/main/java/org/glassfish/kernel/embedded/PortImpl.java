package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.ActionReport;

import java.util.Properties;
import java.util.List;

import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.Http;
import com.sun.grizzly.config.dom.ThreadPool;
import com.sun.grizzly.config.dom.Transport;
import com.sun.grizzly.config.dom.Protocols;
import com.sun.grizzly.config.dom.NetworkListeners;

/**
 * Abstract to port creation and destruction
 */
@Service
@Scoped(PerLookup.class)
public class PortImpl implements Port {
    @Inject
    CommandRunner runner = null;
    @Inject(name = "plain")
    ActionReport report = null;
    @Inject
    PortsImpl ports;
    @Inject
    NetworkConfig config;
    String listenerName;
    int number;

    public void bind(final int portNumber) {
        number = portNumber;
        listenerName = getListenerName();
        try {
            ConfigSupport.apply(new SingleConfigCode<Protocols>() {
                public Object run(Protocols param) throws TransactionFailure {
                    final Protocol protocol = param.createChild(Protocol.class);
                    protocol.setName(listenerName);
                    param.getProtocol().add(protocol);
                    final Http http = protocol.createChild(Http.class);
                    http.setDefaultVirtualServer("server");
                    protocol.setHttp(http);
                    return protocol;
                }
            }, config.getProtocols());
                ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
                    public Object run(NetworkListeners param) throws TransactionFailure {
                        final NetworkListener listener = param.createChild(NetworkListener.class);
                        listener.setName(listenerName);
                        listener.setAddress("127.0.0.1");
                        listener.setPort(Integer.toString(portNumber));
                        listener.setProtocol(listenerName);
                        listener.setThreadPool("http-thread-pool");
                        if (listener.findThreadPool() == null) {
                            final ThreadPool pool = config.getNetworkListeners().createChild(ThreadPool.class);
                            pool.setName(listenerName);
                            listener.setThreadPool(listenerName);
                        }
                        listener.setTransport("tcp");
                        if (listener.findTransport() == null) {
                            final Transport transport = config.getTransports().createChild(Transport.class);
                            transport.setName(listenerName);
                            listener.setTransport(listenerName);
                        }
                        param.getNetworkListener().add(listener);
                        return listener;

                    }
                }, config.getNetworkListeners());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getListenerName() {
        int i = 1;
        String name = "embedded-listener";
        while (existsListener(name)) {
            name = "embedded-listener-" + i++;
        }
        return name;
    }

    private boolean existsListener(String listenerName) {
        // FIX this to check if listenerName exists
        return false;
    }

    public void unbind() {
        final List<NetworkListener> list = config.getNetworkListeners().getNetworkListener();
        for (NetworkListener listener : list) {
            if (listener.getName().equals(listenerName)) {
                list.remove(listener);
                break;
            }
        }
        ports.remove(this);
    }

    public int getPortNumber() {
        return number;
    }
}
