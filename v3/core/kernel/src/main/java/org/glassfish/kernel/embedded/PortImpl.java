/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.*;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.ActionReport;

import java.beans.PropertyVetoException;
import java.util.Properties;
import java.util.List;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;

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
    @Inject
    HttpService httpService;
    String listenerName;
    int number;
    String defaultVirtualServer = "server";

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
                    http.setDefaultVirtualServer(defaultVirtualServer);
                    http.setServerName("");
                    protocol.setHttp(http);
                    return protocol;
                }
            }, config.getProtocols());
            ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
                public Object run(NetworkListeners param) throws TransactionFailure {
                    final NetworkListener listener = param.createChild(NetworkListener.class);
                    listener.setName(listenerName);
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
            VirtualServer vs = httpService.getVirtualServerByName(defaultVirtualServer);
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
                public Object run(VirtualServer avs) throws PropertyVetoException {
                    String DELIM = ",";
                    String lss = avs.getNetworkListeners();
                    boolean listenerShouldBeAdded = true;
                    if (lss == null || lss.length() == 0) {
                        lss = listenerName; //the only listener in the list
                    } else if (!lss.contains(listenerName)) { //listener does not already exist
                        if (!lss.endsWith(DELIM)) {
                            lss += DELIM;
                        }
                        lss += listenerName;
                    } else { //listener already exists in the list, do nothing
                        listenerShouldBeAdded = false;
                    }
                    if (listenerShouldBeAdded) {
                        avs.setNetworkListeners(lss);
                    }    
                    return avs;
                }
            }, vs);
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

    private boolean existsListener(String lName) {
        for (NetworkListener nl : config.getNetworkListeners().getNetworkListener()) {
            if (nl.getName().equals(lName)) {
                return true;
            }
        }
        return false;
    }

    public void close() {
        try {
            ConfigSupport.apply(new ConfigCode() {
                public Object run(ConfigBeanProxy[] params) throws PropertyVetoException, TransactionFailure {
                    final NetworkListeners nt = (NetworkListeners) params[0];
                    final VirtualServer vs = (VirtualServer) params[1];
                    final Protocols protocols = (Protocols) params[2];

                    List<Protocol> protos = protocols.getProtocol();
                    for (Protocol proto : protos) {
                        if (proto.getName().equals(listenerName))  {
                            protos.remove(proto);
                            break;
                        }
                    }

                    final List<NetworkListener> list = nt.getNetworkListener();
                    for (NetworkListener listener : list) {
                        if (listener.getName().equals(listenerName)) {
                            list.remove(listener);
                            break;
                        }
                    }
                    String regex = listenerName + ",?";
                    String lss = vs.getNetworkListeners();
                    vs.setNetworkListeners(lss.replaceAll(regex, ""));
                    return null;
                }
            }, config.getNetworkListeners(),
               httpService.getVirtualServerByName(defaultVirtualServer),
               config.getProtocols());
        } catch(TransactionFailure tf) {
            tf.printStackTrace();
            throw new RuntimeException(tf);
        }

        ports.remove(this);
    }

    public int getPortNumber() {
        return number;
    }
}
