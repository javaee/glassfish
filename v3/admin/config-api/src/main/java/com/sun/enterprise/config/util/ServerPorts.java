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

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.NetUtils;
import java.util.*;
import static com.sun.enterprise.config.util.PortConstants.PORTSLIST;

/**
 * Simple pkg-priv class for keeping the system-properties of a Server handy.
 * We add the Port Props in the correct order
 * Order of precedence from LOW to HIGH ==
 * (1) cluster
 * (2) config
 * (3) server
 * Just add the props in that same order and we're good to go!
 * Note that I'm VERY paranoid about NPE's
 *
 * @author Byron Nevins
 */
class ServerPorts {

    ServerPorts(Domain domain, Server theServer) {
        List<SystemProperty> propList;
        Cluster cluster = null;
        Config config = null;
        server = theServer;

        if (server.isInstance())
            cluster = domain.getClusterForInstance(server.getName());

        String configName = server.getConfigRef();

        if (StringUtils.ok(configName))
            config = domain.getConfigNamed(configName);

        // 1. cluster
        if (cluster != null) {
            propList = cluster.getSystemProperty();
            addAll(propList);
        }

        // 2. config
        if (config != null) {
            propList = config.getSystemProperty();
            addAll(propList);
        }

        // 3. server
        propList = server.getSystemProperty();
        addAll(propList);
    }

    private void addAll(List<SystemProperty> propList) {

        if (propList == null)
            return;

        for (SystemProperty sp : propList) {
            // we only care about
            // 1. the official Port Props that we support
            // 2. But only if they also have a value that is a legal port number

            String name = sp.getName();
            String value = sp.getValue();

            if (StringUtils.ok(name) && StringUtils.ok(value) && PORTSLIST.contains(name)) {
                try {
                    int port = Integer.parseInt(value);

                    if (NetUtils.isPortValid(port))
                        props.put(name, port);
                }
                catch (Exception e) {
                    // we're all done here!
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServerPorts Dump:").append('\n');
        sb.append("server: ").append(server.getName()).append(", ");
        sb.append("Properties: ").append(props).append('\n');
        return sb.toString();
    }
    private final Server server;
    private final Map<String, Integer> props = new HashMap<String, Integer>();
}
