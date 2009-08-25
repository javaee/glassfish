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

package org.glassfish.maven;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.embedded.ScatteredArchive;
import org.glassfish.api.embedded.ScatteredArchive.Builder;



/**
 * @goal runscatteredarchive
 */

public class RunScatteredArchive extends AbstractMojo
{

/**
 * @parameter expression="${serverID}" default-value="maven"
*/

    protected String serverID;

/**
 * @parameter expression="${port}" default-value="8080"
*/
    
    protected int port;
/**
 * @parameter expression="${name}" default-value="test"
 */
    protected String name;

/**
 * @parameter expression="${contextroot}" default-value="test"
 */
    protected String contextRoot;

/**
 * @parameter expression="${rootdirectory}"
 */
    protected String rootdirectory;
/**
 * @parameter expression="${resources}"
 */
    protected String resources;

/**
 * @parameter expression="${precompilejsp}" default-value="false"
 */
    protected Boolean precompilejsp;

/**
 * @parameter expression="${virtualservers}" default-value="virtualservers"
 */
    protected String virtualservers;

/**
 * @parameter expression="${classpath}"
 */
    protected ArrayList<String> classpath = new ArrayList();

/**
 * @parameter expression="${metadata}"
 */
    protected HashMap<String, File> metadata = new HashMap();

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            Server server = new Server.Builder(serverID).build();
            server.createPort(port);
            System.out.println("Starting Web " + server);
            ContainerBuilder b = server.getConfig(ContainerBuilder.Type.web);
            server.addContainer(b);
            EmbeddedDeployer deployer = server.getDeployer();

            File f = new File(rootdirectory);
            ScatteredArchive.Builder builder = new ScatteredArchive.Builder("sampleweb", f);
            System.out.println("rootdir = " + f);
            if (resources == null) 
                resources = rootdirectory;
            builder.setResources(new File(resources));
            if (classpath.isEmpty())
                classpath.add(rootdirectory);
            for (String cp : classpath) {
                builder.addClassPath(new File(cp).toURL());
            }
            for (Map.Entry<String, File> entry : metadata.entrySet()) {
                String key = entry.getKey();
                File value = entry.getValue();
                builder.addMetadata(key, value);
            }

            DeployCommandParameters dp = new DeployCommandParameters(f);
            dp.name = name;
            dp.contextroot = contextRoot;

            while(true) {
                deployer.deploy(builder.buildWar(), dp);

                System.out.println("Deployed Application " + name
                        + " contextroot is " + contextRoot);
                System.out.println("");
                System.out.println("Hit ENTER to redeploy " + name
                        + " <Ctrl-C> to exit");
                // wait for enter
                new BufferedReader(new InputStreamReader(System.in)).readLine();
                deployer.undeploy(name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

