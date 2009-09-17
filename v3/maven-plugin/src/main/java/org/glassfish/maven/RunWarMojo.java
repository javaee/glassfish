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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.ContainerBuilder;


/**
 * @goal runweb
 */

public class RunWarMojo extends AbstractDeployMojo {

/**
 * @parameter expression="${webapp}"
 */
    protected String webapp;


    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            Server server = Util.getServer(serverID, installRoot, instanceRoot, configFile);
            if (port != -1)
                server.createPort(port);

            server.addContainer(ContainerBuilder.Type.web);

            EmbeddedDeployer deployer = server.getDeployer();
            DeployCommandParameters cmdParams = new DeployCommandParameters();
            configureDeployCommandParameters(cmdParams);

            while(true) {
                deployer.deploy(new File(webapp), cmdParams);
                System.out.println("Deployed Application " + name + "[" + webapp + "]"
                        + " contextroot is " + contextRoot);
                System.out.println("Hit ENTER to redeploy " + name + "[" + webapp + "]"
                        + " X to exit");
                String str = new BufferedReader(new InputStreamReader(System.in)).readLine();
                if (str.equalsIgnoreCase("X"))
                    break;
                deployer.undeploy(name, null);
            }
        } catch(Exception e) {
           throw new MojoExecutionException(e.getMessage(),e);
       }
    }

}
