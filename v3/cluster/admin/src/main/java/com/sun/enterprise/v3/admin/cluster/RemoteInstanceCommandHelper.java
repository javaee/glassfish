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
import org.glassfish.api.admin.ServerEnvironment;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.grizzly.config.dom.NetworkListener;
import java.util.*;

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

    RemoteInstanceCommandHelper(ServerEnvironment env0, Servers servers0, Configs configs0) {
        // get rid of the annoying extra level of indirection...
        // callers may have a Servers object or the may happen to have a List<Server> object
        // we have a ctor for both!
        this(env0, servers0.getServer(), configs0);
    }

    RemoteInstanceCommandHelper(ServerEnvironment env0, List<Server> servers0, Configs configs0) {
        env = env0;
        configs = configs0.getConfig();
        servers = servers0;
    }

    final boolean isDas() {
        return env.isDas();
    }

    final boolean isInstance() {
        return env.isInstance();
    }

    final int getAdminPort(final String serverName) {
        return getAdminPort(getServer(serverName));
    }

    final String getHost(final String serverName) {
        return getHost(getServer(serverName));
    }

    final String getHost(final Server server) {

        if (server == null)
            return null;

        String host = server.getNode();

        if (StringUtils.ok(host))
            return host;
        else
            return Strings.get("noNode");
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

    final String getNode(final Server server) {

        if (server == null)
            return null;
        String node = server.getNode();

        if (StringUtils.ok(node))
            return node;
        else
            return Strings.get("noNodeRef");
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
                    return translatePort(listener.getPort(), server, config);
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
    private String translatePort(String portString, Server server, Config config) {
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
    final private ServerEnvironment env;
    final private List<Server> servers;
    final private List<Config> configs;
}
