/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.cluster.ssh.connect;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.admin.SSHCommandExecutionException;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.StringUtils;

import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import java.io.ByteArrayOutputStream;

public class NodeRunner  {

    private static final String NL = System.getProperty("line.separator");

    private  Habitat habitat;
    private Logger logger;

    private String lastCommandRun = null;

    private int commandStatus;

    private SSHLauncher sshL = null;

    public NodeRunner(Habitat habitat, Logger logger) {
        this.logger = logger;
        this.habitat = habitat;
    }

    public String getLastCommandRun() {
        return lastCommandRun;
    }

    public boolean isSshNode(Node node) {

        if (node == null) {
            throw new IllegalArgumentException();
        }
        return node.getType().equals("SSH");
    }

    /**
     * Run an asadmin command on a Node. The node may be local or remote. If
     * it is remote then SSH is used to execute the command on the node.
     * The args list is all parameters passed to "asadmin", but not
     * "asadmin" itself. So an example args is:
     * 
     * "--host", "mydashost.com", "start-local-instance", "--node", "n1", "i1"
     *
     * @param node  The node to run the asadmin command on
     * @param output    A StringBuilder to hold the command's output in. Both
     *                  stdout and stderr are placed in output. null if you
     *                  don't want the output.
     * @param args  The arguments to the asadmin command. This includes
     *              parameters for asadmin (like --host) as well as the
     *              command (like start-local-instance) as well as an
     *              parameters for the command. It does not include the
     *              string "asadmin" itself.
     * @return      The status of the asadmin command. Typically 0 if the
     *              command was successful else 1.
     *
     * @throws SSHCommandExecutionException There was an error executing the
     *                                      command via SSH.
     * @throws ProcessManagerException      There was an error executing the
     *                                      command locally.
     * @throws UnsupportedOperationException The command needs to be run on
     *                                       a remote node, but the node is not
     *                                       of type SSH.
     * @throws IllegalArgumentException     The passed node is malformed.
     */
    public int runAdminCommandOnNode(Node node, StringBuilder output,
                                     List<String> args) throws
        SSHCommandExecutionException,
        ProcessManagerException,
        UnsupportedOperationException,
        IllegalArgumentException {

        if (node.isLocal()) {
            return runAdminCommandOnLocalNode(node, output, args);
        } else {
            return runAdminCommandOnRemoteNode(node, output, args);
        }
    }

    private int runAdminCommandOnLocalNode(Node node, StringBuilder output,
                                           List<String> args) throws
            ProcessManagerException {

        List<String> fullcommand = new ArrayList<String>();
        String installDir = node.getInstallDirUnixStyle();
        if (!StringUtils.ok(installDir)) {
            throw new IllegalArgumentException("Node does not have an installDir");
        }

        File asadmin = new File(SystemPropertyConstants.getAsAdminScriptLocation(installDir));
        fullcommand.add(asadmin.getAbsolutePath());
        fullcommand.addAll(args);

        if (!asadmin.canExecute())
            throw new ProcessManagerException("asadmin is not executable!");

        lastCommandRun = commandListToString(fullcommand);

        trace("Running command locally: " + lastCommandRun);
        ProcessManager pm = new ProcessManager(fullcommand);

        // XXX should not need this after fix for 12777
        //pm.waitForReaderThreads(waitForReaderThreads);
        pm.execute();  // blocks until command is complete

        String stdout = pm.getStdout();
        String stderr = pm.getStderr();

        if (output != null) {
            if (StringUtils.ok(stdout)) {
                output.append(stdout);
            }

            if (StringUtils.ok(stderr)) {
                if (output.length() > 0) {
                    output.append(NL);
                }
                output.append(stderr);
            }
        }
        return pm.getExitValue();
    }

    private int runAdminCommandOnRemoteNode(Node node, StringBuilder output,
                                       List<String> args) throws
            SSHCommandExecutionException, IllegalArgumentException,
            UnsupportedOperationException {

        if (! isSshNode(node)) {
            throw new UnsupportedOperationException(
                    "Node is not of type SSH");
        }

        String installDir = node.getInstallDirUnixStyle();
        if (!StringUtils.ok(installDir)) {
            throw new IllegalArgumentException("Node does not have an installDir");
        }

        // Since we pass the command as a string to SSHLauncher we must
        // make sure to escape any spaces in the installDir with backslashes
        // XXX need a more general solution to this problem
        installDir = encodeSpaces(installDir);

        List<String> fullcommand = new ArrayList<String>();

        // We can just use "asadmin" even on Windows since the SSHD provider
        // will locate the command (.exe or .bat) for us
        fullcommand.add(installDir + "/bin/asadmin");
        fullcommand.addAll(args);

        try{
            lastCommandRun = commandListToString(fullcommand);
            trace("Running command on " + node.getNodeHost() + ": " +
                    lastCommandRun);
            sshL=habitat.getComponent(SSHLauncher.class);
            sshL.init(node, logger);

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            commandStatus = sshL.runCommand(lastCommandRun, outStream);
            String results = outStream.toString();
            output.append(outStream.toString());
            return commandStatus;              

        }catch (IOException ex) {
            String m1 = " Command execution failed. " +ex.getMessage();
            String m2 = "";
            Throwable e2 = ex.getCause();
            if(e2 != null) {
                m2 = e2.getMessage();
            }
            logger.severe("Command execution failed for "+ lastCommandRun);
            SSHCommandExecutionException cee = new SSHCommandExecutionException(StringUtils.cat(":",
                                            m1));
            cee.setSSHSettings(sshL.toString());
            cee.setCommandRun(lastCommandRun);
            throw cee;
            
        } catch (java.lang.InterruptedException ei){
            ei.printStackTrace();
            String m1 = ei.getMessage();
            String m2 = "";
            Throwable e2 = ei.getCause();
            if(e2 != null) {
                m2 = e2.getMessage();
            }
            logger.severe("Command interrupted "+ lastCommandRun);
            SSHCommandExecutionException cee = new SSHCommandExecutionException(StringUtils.cat(":",
                                             m1, m2));
            cee.setSSHSettings(sshL.toString());
            cee.setCommandRun(lastCommandRun);
            throw cee;
        }
    }

    private void trace(String s) {
        logger.fine(String.format("%s: %s", this.getClass().getSimpleName(), s));
    }

    /**
     * Escape all spaces in the string with a backslash.
     */
    private String encodeSpaces(String s) {
        // This replaces all spaces with a backslash-space
        return s.replaceAll(" ", "\\\\ ");
    }

    private String commandListToString(List<String> command) {
        StringBuilder fullCommand = new StringBuilder();

        for (String s : command) {
            fullCommand.append(" ");
            fullCommand.append(s);
        }

        return fullCommand.toString();
    }

}