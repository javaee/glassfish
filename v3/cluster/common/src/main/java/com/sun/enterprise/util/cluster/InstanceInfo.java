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
import java.util.logging.Logger;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;

/**
 * Used to format instance state info in a standard way.
 * It also does internet work
 * @author byron Nevins
 */
public class InstanceInfo {
    public InstanceInfo(String name0, int port0, String host0, Logger logger0) {
        name = name0;
        port = port0;
        host = host0;
        logger = logger0;
        running = pingInstance();
    }

    @Override
    public String toString() {
        return "name: " + getName()
                + ", host: " + getHost()
                + ", port: " + getPort()
                + ", state: " + running;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the host
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name0) {
        name = name0;
    }

    // TODO what about security????
    private String pingInstance() {
        try {
            RemoteAdminCommand rac = new RemoteAdminCommand("uptime", host, port, false, "admin", null, logger);
            // default timeout is 20,000 msec!!
            rac.setConnectTimeout(2000);
            ParameterMap map = new ParameterMap();
            return rac.executeCommand(map);
        }
        catch (CommandException ex) {
            return "Not Running";
        }
    }
    private String host;
    private int port;
    private String name;
    private String running;
    private Logger logger;
}
/** delete this stuff after May 30, 2010
private boolean simplePortTest() {
return !NetUtils.isPortFree(host, port);
}

private boolean advancedPortTest() {
Socket socket = NetUtils.getClientSocket(host, port, 1000);
BufferedReader reader = null  ;

try {
reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
return true;
}
catch (IOException ex) {
return false;
}
finally {
try {
if(reader != null)
reader.close();
}
catch(Exception e) {
// ignore
}
}
}
 */
