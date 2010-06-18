/*
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
package com.sun.enterprise.config.util;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.*;
import java.util.*;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Hiding place for the remarkably complex logic of assigning ports to instances
 * GUARANTEE -- the only thing thrown from here is TransactionFailure
 * @author Byron Nevins
 */
public final class PortManager {

    public PortManager(Server theDas, Domain theDomain, Server theNewServer) throws TransactionFailure {
        try {
            if (theNewServer == null || theDomain == null)
                throw new TransactionFailure(Strings.get("internal.error", "null argument in PortManager constructor"));

            dasName = theDas.getName();

            if (!StringUtils.ok(dasName))
                throw new TransactionFailure(Strings.get("internal.error", "null or empty DAS name."));

            newServer = theNewServer;
            this.domain = theDomain;
            serverName = newServer.getName();
            host = newServer.getNodeAgentRef();

            if (!StringUtils.ok(host))
                throw new TransactionFailure(Strings.get("PortManager.noHost", serverName));

            isLocal = NetUtils.IsThisHostLocal(host);
            allServers = domain.getServers().getServer();

            // why all this nonsense?  ConcurrentModificationException!!!
            for (Iterator<Server> it = allServers.iterator(); it.hasNext();) {
                Server curr = it.next();
                if (serverName.equals(curr.getName())) {
                    it.remove();
                }
            }
            serversOnHost = new ArrayList<ServerPorts>();
        }
        catch (TransactionFailure tf) {
            throw tf;
        }
        catch (Exception e) {
            // this Exception will not take just a Throwable.  I MUST give a string
            throw new TransactionFailure(e.toString(), e);
        }
    }

    public void justDoIt() throws TransactionFailure {
        try {
            // make sure user-supplied props are not flaky
            PortUtils.checkInternalConsistency(newServer);

            createServerList();
        }
        catch (TransactionFailure tf) {
            throw tf;
        }
        catch (Exception e) {
            throw new TransactionFailure(e.toString(), e);
        }
    }

    private void createServerList() {
        if (isLocal)
            createLocalServerList();
        else
            createRemoteServerList();

    }

    private void createLocalServerList() {
        for (Server server : allServers) {
            if (server.isDas())
                serversOnHost.add(new ServerPorts(domain, server));
            else if (NetUtils.IsThisHostLocal(server.getNodeAgentRef()))
                serversOnHost.add(new ServerPorts(domain, server));
        }
    }

    private void createRemoteServerList() {
        for (Server server : allServers) {
            // no DAS!
            if (server.isInstance() && sameHost(server))
                serversOnHost.add(new ServerPorts(domain, server));
        }
    }

    private boolean sameHost(Server server) {
        return NetUtils.isEqual(server.getNodeAgentRef(), host);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PortManager Dump:");

        for(ServerPorts sp : serversOnHost)
            sb.append(sp).append('\n');

        return sb.toString();
    }

    private final String serverName;
    private final Server newServer;
    private final String dasName;
    private final boolean isLocal;
    private final Domain domain;
    private final String host;
    private final List<Server> allServers;
    private final List<ServerPorts> serversOnHost;
}
