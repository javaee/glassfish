/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.grizzly.config.dom.NetworkListener;
import java.util.*;
import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.config.support.PropertyResolver;
import org.jvnet.hk2.component.Habitat;

/**
 * @author Byron Nevins
 * Everything is pkg-private or private in this class
 * 
 * Implementation Note:
 *
 * Ideally this class would be extended by AdminCommand's that need these
 * services.  The problem is getting the values out of the habitat.  The ctor
 * call would be TOO EARLY  in the derived classes.  The values are injected AFTER
 * construction.  We can't easily inject here -- because we don't want this class
 * to be a Service.
 * We could do it by having the derived class call a set method in here but that
 * gets very messy as we have to make sure we are in a valid state for every single
 * method call.
 *
 */
final class RemoteInstanceCommandHelper {

    RemoteInstanceCommandHelper(Habitat habitatIn) {

        try {
            habitat = habitatIn;
            configs = habitat.getByType(Configs.class).getConfig();
            servers = habitat.getByType(Servers.class).getServer();
            domain = habitat.getByType(Domain.class);
            nodes = habitat.getByType(Nodes.class);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final String getHost(final String serverName) {
        return getHost(getServer(serverName));
    }

    final String getHost(final Server server) {

        String hostName = null;

        if (server == null)
            return null;

        // For backwards compatibility we first check node-agent-ref
        // which in earlier builds contained the instances host name
        hostName = server.getNodeAgentRef();

        if (StringUtils.ok(hostName)) {
            return hostName;
        }

        if (nodes == null) {
            return null;
        }

        // No node-agent-ref. Get it from the node associated with the server
        String nodeName = server.getNode();
        if (StringUtils.ok(nodeName)) {
            Node node = nodes.getNode(nodeName);
            if (node != null) {
                hostName = node.getNodeHost();
            }
            // XXX Hack to get around the fact that the default localhost
            // node entry is malformed
            if (hostName == null && nodeName.equals("localhost")) {
                hostName = "localhost";
            }
        }

        if (StringUtils.ok(hostName)) {
            return hostName;
        } else {
            return null;
        }
    }

    final Server getServer(final String serverName) {
        for (Server server : servers) {
            final String name = server.getName();

            // ??? TODO is this crazy?
            if (serverName == null) {
                if (name == null) // they match!!
                    return server;
            }
            else if (serverName.equals(name))
                return server;
        }
        return null;
    }

    // bnevins: KLUDGE alert -- what is UP with the noNodeRef?!?
    // TODO TODO
    final String getNode(final Server server) {

        if (server == null)
            return null;

        String node = server.getNode();

        if (StringUtils.ok(node))
            return node;
        else
            return Strings.get("noNodeRef");
    }

    final int getAdminPort(final String serverName) {
        return getAdminPort(getServer(serverName));
    }

    final int getAdminPort(Server server) {
        String portString = getAdminPortString(server, getConfig(server));

        if (portString == null)
            return -1; // get out quick.  it is kosher to call with a null Server

        try {
            return Integer.parseInt(portString);
        }
        catch (Exception e) {
            // drop through...
        }
        // we might have something like "${SOME_PORT}" as the value of the port
        return -1;
    }

    ///////////////////////////////////////////////////////////////////////////
    //  All private below.  If you need something below in a derived class then
    // upgrade to pkg-private and move it above this line.  Change the keyword
    // private to final on the method
    ///////////////////////////////////////////////////////////////////////////
    private String getAdminPortString(Server server, Config config) {
        if (server == null || config == null)
            return null;

        try {
            List<NetworkListener> listeners = config.getNetworkConfig().getNetworkListeners().getNetworkListener();

            for (NetworkListener listener : listeners) {
                if ("admin-listener".equals(listener.getProtocol()))
                    return translatePort(listener, server);
            }
        }
        catch (Exception e) {
            // handled below...
        }
        return null;
    }

    private Config getConfig(final Server server) {
        // multiple returns makes this short method more readable...
        if (server == null)
            return null;

        String cfgName = server.getConfigRef();

        if (cfgName == null)
            return null;

        for (Config config : configs)
            if (cfgName.equals(config.getName()))
                return config;

        return null;
    }

    private String translatePort(NetworkListener adminListener, Server server) {
        NetworkListener adminListenerRaw = null;

        try {
            adminListenerRaw = GlassFishConfigBean.getRawView(adminListener);
            String portString = adminListenerRaw.getPort();

            if (!isToken(portString))
                return portString;

            PropertyResolver resolver = new PropertyResolver(domain, server.getName());
            return resolver.getPropertyValue(portString);
        }
        catch (ClassCastException e) {
            //jc: workaround for issue 12354
            // TODO severe error 
            return translatePortOld(adminListener.getPort(), server, getConfig(server));
        }
    }

    /**
     * The way the automatic translation works is that system-property
     * elements are used to resolve tokens.  But we are inside DAS.  We need to
     * resolve relative to an instance.  The values are NOT in System.getProperty() !!
     * There are potentially FOUR elements to check, in order of precedence high to low:
     * 1. server for the instance
     * 2. config for the instance
     * 3. cluster for the instance
     * 4. config for the cluster
     * @return the port number or -1 if there is an error.
     */
    private String translatePortOld(String portString, Server server, Config config) {
        if (!isToken(portString))
            return portString;

        // isToken returned true so we are NOT assuming anything below!
        String key = portString.substring(2, portString.length() - 1);

        // TODO TODO TODO
        // TODO TODO TODO
        // check cluster and the cluster's config if applicable
        // TODO TODO TODO
        // TODO TODO TODO
        // TODO TODO TODO

        SystemProperty prop = server.getSystemProperty(key);

        if (prop != null) {
            return prop.getValue();
        }

        prop = config.getSystemProperty(key);

        if (prop != null) {
            return prop.getValue();
        }

        return null;
    }

    private static boolean isToken(String s) {
        return s != null
                && s.startsWith("${")
                && s.endsWith("}")
                && s.length() > 3;
    }
    final private List<Server> servers;
    final private List<Config> configs;
    final private Nodes nodes;
    final private Habitat habitat;
    final private Domain domain;
}
