/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.embed;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collections;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Level.INFO;

/**
 * Launches a mock-up HK2 environment that doesn't provide
 * any classloader isolation. Instead, the whole thing is loaded
 * from the single classloader.
 *
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) throws Exception {
        AppServer.setLogLevel(INFO);

         AppServer glassfish = new AppServer(9999);
        //if you want to use your own domain.xml
        //Server glassfish = new Server(new File("domain.xml").toURI().toURL());
        //if you want to use your own default-web.xml file
        //glassfish.setDefaultWebXml(new File("default-web.xml".toURI().toURL()));

        // deploy(new File("./simple.war"),habitat);
        // deploy(new File("./JSPWiki.war"),habitat);

//        GFApplication app = glassfish.deploy(new File("./hudson.war"));
System.out.println("YYYYYYYY");

        while (true) {
            //File killerApp = new File("C:/gf/v3/embedded/api/killer-app");
            File killerApp = new File("killer-app");
            ScatteredWar war = new ScatteredWar(
                "killer-app",
                new File(killerApp,"web"),
                new File(killerApp,"web.xml"),
                Collections.singleton(
                    new File(killerApp,"target/classes").toURI().toURL())
            );
            //GFApplication app = glassfish.deploy(new File("simple.war"));
            App app = glassfish.deploy(war);
            // if you want to use another context root for example "/"
            // GFApplication app = glassfish.deployWar(war, "/");
            // if you want to use the default context root but another virtual server
            // GFApplication app = glassfish.deployWar(war, null, "myServerId");     

            System.out.println("Ready!");

            // wait for enter
            new BufferedReader(new InputStreamReader(System.in)).readLine();

            app.undeploy();
        }

//        glassfish.stop();
    }
}
