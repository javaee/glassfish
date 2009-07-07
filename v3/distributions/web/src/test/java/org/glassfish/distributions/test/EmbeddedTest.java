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
package org.glassfish.distributions.test;


import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.distributions.test.ejb.SampleEjb;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EmbeddedTest {

    static Port http=null;
    static Server server = null;

    @BeforeClass
    public static void setup() {
        Server.Builder builder = new Server.Builder("build");

        server = builder.build();
        http = server.createPort(8080);
    }

    @Test
    public void testEjb() throws LifecycleException {

        server.addContainer(server.createConfig(ContainerBuilder.Type.ejb));
        EmbeddedDeployer deployer = server.getDeployer();

        URL source = SampleEjb.class.getClassLoader().getResource("org/glassfish/distributions/test/ejb/SampleEjb.class");
        String p = source.getPath().substring(0, source.getPath().length()-"org/glassfish/distributions/test/ejb/SimpleEjb.class".length());

        File path = new File(p);
        DeployCommandParameters dp = new DeployCommandParameters(path);
        dp.name="sample";
        String appName = deployer.deploy(path, dp);

        // ok now let's look up the EJB...
        try {
            InitialContext ic = new InitialContext();
            SampleEjb ejb = (SampleEjb) ic.lookup("java:global/test-classes/SampleEjb");
            if (ejb!=null) {
                try {
                    System.out.println(ejb.saySomething());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        deployer.undeploy(appName);
        System.out.println("Done with EJB");
    }

    //@Test
    public void testWeb() throws Exception {
        System.out.println("Starting Web " + server);
        ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
        System.out.println("builder is " + b);
        server.addContainer(b);
        EmbeddedDeployer deployer = server.getDeployer();
        System.out.println("Added Web");

        URL source = SampleEjb.class.getClassLoader().getResource("org/glassfish/distributions/test/web/WebHello.class");
        String p = source.getPath().substring(0, source.getPath().length()-"org/glassfish/distributions/test/web/WebHello.class".length());

        System.out.println("Deploying " + p);
        File path = new File(p);
        DeployCommandParameters dp = new DeployCommandParameters(path);
        dp.name="sampleweb";
        String appName = null;
        try {
            appName = deployer.deploy(path, dp);

            try {
                URL servlet = new URL("http://localhost:8080/sampleweb/hello");
                URLConnection yc = servlet.openConnection();
                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(
                                        yc.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);
                in.close();
            } catch(Exception e) {
                e.printStackTrace();
                throw e;
            }
        } catch(Exception e) {
            // mask exceptions for now
            // e.printStackTrace();
        }
        if (appName!=null)
            deployer.undeploy(appName);

    }

    @AfterClass
    public static void  close() throws LifecycleException {
        if (http!=null) {
            http.unbind();
            http=null;
        }
        System.out.println("Stopping server");
        if (server!=null) {
            server.stop();
            server=null;
        }
    }
}