/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package prober;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
// Super simple output -- it just writes to stdout -- which will go into the server logfile.
// The point is just to demonstrate how to listen to GlassFish probes

public class ConnectionProbeListener {
    private static final String ACCEPT = "glassfish:kernel:connection-queue:connectionAcceptedEvent";

    // this is what ProberServlet shows at runtime:
    // glassfish:kernel:connection-queue:connectionAcceptedEvent (java.lang.String listenerName,
    // int connection, java.lang.String address)
    @ProbeListener(ACCEPT)
    public void accepted(
            //java.lang.String listenerName, int connection, java.lang.String address
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionNumber,
            @ProbeParam("address") String address) {

        // you can do anything you like in here.
        System.out.printf("Connection Accepted!  name=%s, connection: %d, address: %s\n",
                listenerName, connectionNumber, address);
    }

    // this is what ProberServlet shows at runtime:
    //glassfish:web:http-service:requestStartEvent (java.lang.String appName,
    // java.lang.String hostName, java.lang.String serverName,
    //int serverPort, java.lang.String contextPath, java.lang.String servletPath)
    private static final String REQUEST = "glassfish:web:http-service:requestStartEvent";
    @ProbeListener(REQUEST)
    public void startEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName,
            @ProbeParam("serverName") String serverName,
            @ProbeParam("serverPort") int serverPort,
            @ProbeParam("contextPath") String contextPath,
            @ProbeParam("servletPath") String servletPath) {

        // you can do anything you like in here.
        System.out.printf("Web Start Event with these params:%s, %s, %s, %d, %s, %s\n",
                appName, hostName, serverName, serverPort, contextPath, servletPath);
    }
}
