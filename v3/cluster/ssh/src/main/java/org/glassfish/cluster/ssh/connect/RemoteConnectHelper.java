/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.cluster.ssh.connect;

import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.config.serverbeans.SshAuth;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Node;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import java.io.ByteArrayOutputStream;

public class RemoteConnectHelper  {

    private Habitat habitat;

    private HashMap<String, Node> nodeMap;

    private Logger logger;

    public RemoteConnectHelper(Habitat habitat, Node[] nodes, Logger logger) {
        this.logger = logger;
        this.habitat = habitat;
        nodeMap = new HashMap<String, Node>();

        for (int i=0;i<nodes.length;i++) {
            Node n  =  nodes[i];
            nodeMap.put(n.getName(), n);
        }        

    }

    public boolean isRemoteConnectRequired(String nodeRef) {

        Node node = nodeMap.get(nodeRef);
        if (node != null){
            SshConnector sshC = node.getSshConnector();
            if ( sshC != null)
                return true;
            else
                return false;
                        
        }   else {
            logger.warning("invalid node ref "+ nodeRef);
            return false;

        }


    }
    // need to get the command options that were specified too

    public void runCommand(String noderef, String cmd, String instanceName ){

        //get the node ref and see if ssh connection is setup if so use it
        try{

        Node node = nodeMap.get(noderef);
        if (node == null){
            logger.severe("remote.connect.noSuchNodeRef"+ noderef);
            return;
        }
            String nodeHome = node.getNodeHome();
            if( nodeHome == null)   // what can we assume here?
                    return;
            SshConnector connector = node.getSshConnector();
            if ( connector != null)  {
                SSHLauncher sshL=habitat.getComponent(SSHLauncher.class);
                sshL.init(noderef);

                // create command and params
                String command = "start-local-instance "+ instanceName;
                String prefix = nodeHome +"/bin/asadmin ";

                String fullCommand = prefix + command;

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();

                sshL.runCommand(fullCommand, outStream);
                System.out.println(outStream);
            } else {
                return;

            }
        }catch (IOException ex) {
        } catch (java.lang.InterruptedException ei){
        }


    }


}