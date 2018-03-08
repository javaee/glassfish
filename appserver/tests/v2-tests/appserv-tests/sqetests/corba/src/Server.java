/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package corba;

import org.omg.CORBA.ORB;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
public class Server {

    private static SimpleReporterAdapter status =
		 new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        final String suiteId = "CORBA";
        final String testId = "rmiiiop test";

        try {

            status.addDescription("To test registration, resolution and " +
                                  "remote invocation on a rmiiiop server");

            ORB orb = ORB.init(args, System.getProperties());
            System.out.println("ORB Initialized.");

            POA root = POAHelper.narrow(
                orb.resolve_initial_references("RootPOA"));

            Policy[] policy = new Policy[2];
            policy[0] = root.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
            policy[1] = root.create_servant_retention_policy(
                ServantRetentionPolicyValue.NON_RETAIN);
            POA slpoa = root.create_POA("NR_USM_POA", null, policy);
            slpoa.set_servant_manager(new MyServantLocator());

            Servant servant = getServant();
            String intf = servant._all_interfaces(slpoa, null)[0];
            byte[] oid = "abcd".getBytes();
            org.omg.CORBA.Object ref =
                slpoa.create_reference_with_id(oid, intf);

            root.the_POAManager().activate();
            slpoa.the_POAManager().activate();

            NamingContext namingContext = NamingContextHelper.narrow(
                orb.resolve_initial_references("NameService"));
            System.out.println(namingContext);

            System.out.println("Resolved NameService");


            NameComponent[] names = { new NameComponent("RemoteTest", "") };
            namingContext.rebind(names, ref);
	    System.out.println("rebind nameservice");

            ORB clientORB = ORB.init(args, System.getProperties());
            System.out.println(args[0]);
            RemoteTest testRef = (RemoteTest) PortableRemoteObject.narrow(
                clientORB.string_to_object(args[0]), RemoteTest.class);
            System.out.println("Pinging... ");
            testRef.ping();

            status.addStatus(testId, status.PASS);
        } catch (Throwable e) {
            status.addStatus(testId, status.FAIL);
            System.out.println("If You're using Mainstream build, "+
                               "please check the LD_LIBRARY_PATH for "+
                               "solaris and PATH for windows.\n"+
                               "Include /usr/lib/mps in the LD_LIBRARY_PATH "+
                               "for solaris");
            e.printStackTrace();
        } 
          status.printSummary("corbaID");
          System.out.println("Test done. exiting");
          System.exit(0);
    }

    public static Servant getServant() {
        Servant servant = null;
        try {
            RemoteTestImpl remoteTest = new RemoteTestImpl();
            servant =  (Servant) javax.rmi.CORBA.Util.getTie(remoteTest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servant;
    }
}

class MyServantLocator extends LocalObject implements ServantLocator {

    public Servant preinvoke(byte[] oid, POA adapter, String name,
                             CookieHolder cookie)
    throws ForwardRequest {
        return Server.getServant();
    }

    public void postinvoke(byte[] oid, POA adapter, String name,
                           java.lang.Object obj, Servant servant)
    {

    }

}
