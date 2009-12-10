/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.ant.embedded.tasks;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.ScatteredArchive;

import java.io.*;
import java.util.*;


public class DeployScatteredTask extends Task {

    protected int port;
    protected String name, rootdirectory, resources, contextroot;
    protected ArrayList<String> classpath = new ArrayList();
    protected HashMap<String, File> metadata = new HashMap();

    String serverID = Constants.DEFAULT_SERVER_ID;

    DeployCommandParameters cmdParams = new DeployCommandParameters();

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public void setRootDirectory(String rootdirectory) {
        this.rootdirectory = rootdirectory;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setContextroot(String contextroot) {
        this.contextroot = contextroot;
    }

    public void setWebxml(String path) {
        metadata.put("web.xml", new File(path));
    }

    public void setSunWebxml(String path) {
        metadata.put("sun-web.xml", new File(path));
    }

    public void setResourcesDirectory(String path) {
        resources = path;
    }
    
    public void setClasspath(String[] classpathArray) {
        for (int i =0; i < classpathArray.length; i++)
            this.classpath.add(classpathArray[i]);
    }

    public void execute() throws BuildException {
        log("deploying " + rootdirectory);

        try {
            File f = new File(rootdirectory);
            ScatteredArchive.Builder builder = new ScatteredArchive.Builder(name, f);
            log("rootdir = " + f);
            if (resources == null)
                resources = rootdirectory;
            builder.resources(new File(resources));

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
            Server server = Server.getServer(serverID);
            EmbeddedDeployer deployer = server.getDeployer();
            DeployCommandParameters dp = new DeployCommandParameters(f);
            dp.name = name;
            dp.contextroot = this.contextroot == null? name : this.contextroot;
            deployer.deploy(builder.buildWar(), dp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
