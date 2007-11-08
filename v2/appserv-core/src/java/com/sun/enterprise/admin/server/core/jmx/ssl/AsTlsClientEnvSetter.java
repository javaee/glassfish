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

/*
 * AsTlsClientEnvSetter.java
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. No tabs are used, all spaces.
 * 2. In vi/vim -
 *      :set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *      1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *      2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = True.
 *      3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 * Unit Testing Information:
 * 0. Is Standard Unit Test Written (y/n): No
 * 1. Unit Test Location: (The instructions should be in the Unit Test Class itself).
 */

package com.sun.enterprise.admin.server.core.jmx.ssl;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.TrustManager;
/* from admin-core/mbeanapi -- for RMI/TLS communication */
import com.sun.enterprise.admin.jmx.remote.https.SunOneBasicX509TrustManager;
import com.sun.appserv.management.client.AdminRMISSLClientSocketFactory;
import com.sun.appserv.management.client.AdminRMISSLClientSocketFactoryEnvImpl;
import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;
//caution
//import com.sun.appserv.management.client.TrustAnyTrustManager;
/* from admin-core/mbeanapi -- for RMI/TLS communication */

/** This class is meant specifically for the setup of client side environment when
 * the server end of system jmx connector is started with TLS on. This means that
 * the custom RMI/SSL socket factory is going to be downloaded to the node agent
 * VM and then the client socket creation will happen. 
 * <p>
 * Really speaking RMI Custom Socket Factory provision has some loopholes in it in that if the factory stub is 
 * downloaded from server, then there is no way to "configure" it from the standpoint
 * of HandshakeCompletedListener and TrustManager provision. (Unless there were
 * some standard classes to do it).
 * <p>
 * Hence the downloading of stub does not work here in its true spirit, because the
 * stub class is actually going to be available to node agent in its system class 
 * path and hence the configuration of the same is possible.
 * <p>
 * What is not possible is connecting to RMI/TLS server end of system-jmx-connector
 * by a client that does not have appserver classses in class path. But node agent
 * is one of the internal clients to system-jmx-connector and hence there is
 * no problem as such. But since we are really not using the "downloaded" stub, the
 * true spirit of custom rmi client socket factories is not used.
 * <p>
 * The reason that this is a separate class is that the NodeAgent class that uses
 * this one is a really large class and I did not want to add more to it. Also, since
 * as of $Date: 2006/11/15 08:22:07 $ node agent runs the synchronization in a separate VM, this separate
 * VM also needs to set this environment. It is not sufficient to set this environment
 * only in Node agent. I am choosing this package for the lack of better one.
 * <p>
 * For the sake of uniformity, this class uses the {@link SunOneBasicX509TrustManager} which
 * knows how to check the "server" certificate by looking into .asadmintruststore.
 * <p>
 * @author  Kedar.Mhaswade@sun.com
 * @since Sun Java System Application Server 8.1ee
 */
public class AsTlsClientEnvSetter {
 
    final AdminRMISSLClientSocketFactoryEnvImpl env;

    public AsTlsClientEnvSetter() {
        this.env = AdminRMISSLClientSocketFactoryEnvImpl.getInstance();
        //System.out.println("Doing RMI/TLS Client setup in this VM");
    }
    public void setup() {
        //debugging is disabled
        //enableTrace();
        enableTrustManagement();
        enableHandshake();
    }
    private void enableTrace() {
        env.setTrace(true);
    }
    private void enableTrustManagement() {
        final TrustManager[] tms = this.getTrustManagers();
        env.setTrustManagers(tms);
    }
    private void enableHandshake() {
        env.setHandshakeCompletedListener(new HandshakeCompletedListenerImpl());
    }

    protected TrustManager[] getTrustManagers() {
        //return ( TrustAnyTrustManager.getInstanceArray() );
        return ( new TrustManager[]{new SunOneBasicX509TrustManager()} );
    }
}
