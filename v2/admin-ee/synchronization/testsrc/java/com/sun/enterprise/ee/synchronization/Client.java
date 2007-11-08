/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.ee.synchronization;

// java imports
//
import java.util.*;

import java.io.*;

import java.net.*;

// RI imports
//
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.Attribute;
import com.sun.enterprise.ee.util.zip.Unzipper;


/*
 *       Trivial application that displays a string
 */

/**
 * Class Client
 *
 */
public class Client 
{

    protected int _port;
    protected String _host;
    
    /**
     * Method usage
     *
     *
     */
    public static void usage() {
        System.out.println(
            "usage: remoteDirectory localDirectory <host> <port>");
        System.exit(1);
    }

    /**
     * Method main
     *
     *
     * @param args
     *
     */
    public static void main(String[] args) {

        try {
            String host = "localhost";
            int port = Server.DEFAULT_CONNECTOR_PORT;

            if ((args.length < 2) || (args.length > 4)) {
                usage();
            }

            String remotedir = args[0];
            String localdir = args[1];

            if (args.length == 3) {
                host = args[2];
            } else if (args.length == 4) {
                host = args[2];
                port = Integer.parseInt(args[3]);
            }

            Client client = new Client(host, port);
            MBeanServerConnection server = client.connect();            
            
            SynchronizationRequest[] requests = new SynchronizationRequest[1];

            //requests[0] = new SynchronizationRequest(remotedir, SynchronizationRequest.TYPE_DIRECTORY,
            //    ".", 0, SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME, null);
            //requests[0] = new SynchronizationRequest("c:\\Sun\\j2eesdk1.4_beta2\\domains\\domain1\\server\\applications", 
            //    SynchronizationRequest.TYPE_DIRECTORY,
            //    "./applications", 0, SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME, null);
            requests[0] = new SynchronizationRequest(
                "c:\\Sun\\j2eesdk1.4_beta2\\domains\\domain1\\server\\config\\backup\\domain.xml",
                "./config", 1051855384000L, SynchronizationRequest.TIMESTAMP_FILE,
                "c:\\Sun\\j2eesdk1.4_beta2\\domains\\domain1\\server\\config\\backup\\domain.xml.timestamp");
            ObjectName objname = new ObjectName(server.getDefaultDomain() + ":type=" + "Synchronization");    
            SynchronizationResponse result =
                (SynchronizationResponse)server.invoke(
                    objname, "synchronize", new Object[]{ requests },
                    new String[]{ "[Lcom.sun.enterprise.ee.synchronization.SynchronizationRequest;" });
            Unzipper uz = new Unzipper(localdir);
            uz.writeZipBytes(result.getZipBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Client (String host, int port) {
        _host = host;
        _port = port;
    }
    
    /**
     * Method Connect
     *
     *
     * @param host
     * @param port
     *
     * @return
     *
     * @throws IOException
     *
     */
    public MBeanServerConnection connect()
    throws IOException {
        final JMXServiceURL url = new JMXServiceURL("service:jmx:jmxmp://"
                                                    + _host + ":" + _port);

        System.out.println("connecting to " + url);

        JMXConnector conn = JMXConnectorFactory.connect(url);
        MBeanServerConnection server = conn.getMBeanServerConnection();
        return (server);
    }
}
;
