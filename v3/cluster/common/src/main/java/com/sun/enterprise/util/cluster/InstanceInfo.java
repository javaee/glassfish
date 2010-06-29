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
package com.sun.enterprise.util.cluster;

import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.util.StringUtils;
import java.util.*;
import java.util.logging.Logger;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;

/**
 * Used to format instance state info in a standard way.
 * It also does internet work
 * @author byron Nevins
 */
public final class InstanceInfo {

    public InstanceInfo(String name0, int port0, String host0,
            Logger logger0, int timeout0) {
        this(name0, port0, host0, null, logger0, timeout0);
    }
    
    public InstanceInfo(String name0, int port0, String host0, String cluster0,
            Logger logger0, int timeout0) {
        if (name0 == null || host0 == null)
            throw new NullPointerException("null arguments");

        name = name0;
        port = port0;
        host = host0;
        logger = logger0;
        state = pingInstance();
        timeoutInMsec = timeout0;

        if (!StringUtils.ok(cluster0))
            cluster = null;
        else
            cluster = cluster0;
    }

    @Override
    public final String toString() {
        String cl = "";

        if (cluster != null)
            cl = ", cluster: " + getCluster();

        return "name: " + getName()
                + ", host: " + getHost()
                + ", port: " + getPort()
                + cl
                + ", state: " + state;
    }

    public final String getDisplayCluster() {
        return cluster == null ? NO_CLUSTER : cluster;
    }

    public final String getCluster() {
        return cluster;
    }

    public final String getHost() {
        return host;
    }

    public final int getPort() {
        return port;
    }

    public final String getName() {
        return name;
    }

    public final String getState() {
        return state;
    }

    public final boolean isRunning() {
        return running;
    }

    /////////////////////////////////////////////////////////////////////////
    ////////  static formatting stuff below   ///////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    public static String format(List<InstanceInfo> infos) {
        int longestName = NAME.length();
        int longestHost = HOST.length();
        int longestState = STATE.length();
        int longestPort = PORT.length();
        int longestCluster = CLUSTER.length();

        for (InstanceInfo info : infos) {
            int namel = info.getName().length();
            int hostl = info.getHost().length();
            int statel = info.getState().length();
            int clusterl = info.getDisplayCluster().length();

            if (namel > longestName)
                longestName = namel;
            if (hostl > longestHost)
                longestHost = hostl;
            if (statel > longestState)
                longestState = statel;
            if (clusterl > longestCluster)
                longestCluster = clusterl;
        }

        // we could truncate to fit in 80 characters -- but that gets very complex.
        // If user wants huge names -- he'll have to put up with it!

        longestName += 2;
        longestHost += 2;
        longestState += 2;
        longestPort += 2;
        longestCluster += 2;
        StringBuilder sb = new StringBuilder();

        String formattedLine =
                "%-" + longestName
                + "s %-" + longestHost
                + "s %-" + longestPort
                + "s %-" + longestCluster
                + "s %-" + longestState
                + "s";

        sb.append(String.format(formattedLine, NAME, HOST, PORT, CLUSTER, STATE));
        sb.append('\n');
        for (int i = 0; i < longestName; i++)
            sb.append('-');
        sb.append('|');
        for (int i = 0; i < longestHost; i++)
            sb.append('-');
        sb.append('|');
        for (int i = 0; i < longestPort; i++)
            sb.append('-');
        sb.append('|');
        for (int i = 0; i < longestCluster; i++)
            sb.append('-');
        sb.append('|');
        for (int i = 0; i < longestState; i++)
            sb.append('-');
        sb.append('|');
        sb.append('\n');

        // no linefeed at the end!!!
        boolean first = true;
        for (InstanceInfo info : infos) {
            if (first)
                first = false;
            else
                sb.append('\n');

            String portString = "   " + info.getPort();

            sb.append(String.format(formattedLine, info.getName(),
                    info.getHost(), portString, " " + info.getDisplayCluster(), info.getState()));
        }

        return sb.toString();
    }

    // TODO what about security????
    private String pingInstance() {
        // there could be more than one instance with the same admin port
        // let's get a positive ID!

        if (i9())
            return getUptime();
        else
            return NOT_RUNNING;
    }

    private String getUptime() {
        try {
            RemoteAdminCommand rac = new RemoteAdminCommand("uptime", host, port, false, "admin", null, logger);
            rac.setConnectTimeout(timeoutInMsec);
            ParameterMap map = new ParameterMap();
            map.set("type", "terse");
            running = true;
            return rac.executeCommand(map).trim();
        }
        catch (CommandException ex) {
            return NOT_RUNNING;
        }
    }

    private boolean i9() {
        // are you really the right server?
        // simple test -- is the server-name in the returned string?
        try {
            RemoteAdminCommand rac = new RemoteAdminCommand("__locations", host, port, false, "admin", null, logger);
            rac.setConnectTimeout(timeoutInMsec);
            ParameterMap map = new ParameterMap();
            map.set("type", "terse");
            String ret = rac.executeCommand(map).trim();

            if (ret != null && ret.endsWith("/" + name))
                return true;
        }
        catch (CommandException ex) {
            // handle below
        }
        return false;
    }

    private static String prepareFormatString() {
        // Probably not worth the effort but what the heck...

        return null;
    }
    private final String host;
    private final int port;
    private final String name;
    private final String state;
    private final String cluster;
    private final Logger logger;
    private final int timeoutInMsec;
    private boolean running;
    private static final String NOT_RUNNING = Strings.get("ListInstances.NotRunning");
    private static final String NAME = Strings.get("ListInstances.name");
    private static final String HOST = Strings.get("ListInstances.host");
    private static final String PORT = Strings.get("ListInstances.port");
    private static final String STATE = Strings.get("ListInstances.state");
    private static final String CLUSTER = Strings.get("ListInstances.cluster");
    private static final String NO_CLUSTER = "---";
}
