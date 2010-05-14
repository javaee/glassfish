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
package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.cli.remote.DASUtils;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import java.io.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * Stop a local server instance.
 * @author Bill Shannon
 * @author Byron Nevins
 *
 */
@Service(name = "stop-instance")
@Scoped(PerLookup.class)
public class StopLocalInstanceCommand extends LocalInstanceCommand {
    @Param(name = "instance_name", primary = true, optional = true)
    private String userArgInstanceName;
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StopLocalInstanceCommand.class);

    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        instanceName = userArgInstanceName;
        super.validate();
    }

    /**
     * Override initInstance in LocalInstanceCommand to only initialize
     * the local instance information (name, directory) in the local
     * case, when no --host has been specified.
     */
    @Override
    protected void initInstance() throws CommandException {
        // only initialize instance domain information if it's a local operation
        if(isLocalInstance())
            super.initInstance();
        else if(userArgInstanceName != null)   // remote case
            throw new CommandException(
                    strings.get("StopInstance.noInstanceNameAllowed"));
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        String serverName = getServerDirs().getServerName();
        boolean isLocal = ok(serverName);

        if(isLocal) {
            // if the local password isn't available, the instance isn't running
            // (localPassword is set by initInstance)
            if(getServerDirs().getLocalPassword() == null)
                return instanceNotRunning();

            int adminPort = getAdminPort(serverName);
            programOpts.setPort(adminPort);
            logger.printDebugMessage("StopInstance.stoppingMessage" +  adminPort);

            /*
             * If we're using the local password, we don't want to prompt
             * for a new password.  If the local password doesn't work it
             * most likely means we're talking to the wrong server.
             */
            programOpts.setInteractive(false);


            // in the local case, make sure we're talking to the correct DAS
            File serverDir = getServerDirs().getServerDir();

            if(!isThisServer(serverDir, "Instance-Root_value"))
                return instanceNotRunning();

            logger.printDebugMessage("It's the correct Instance");
        }
        else { // remote
            if(!DASUtils.pingDASQuietly(programOpts, env))
                return instanceNotRunning();

            logger.printDebugMessage("Instance is running remotely");
            programOpts.setInteractive(false);
        }
        return doRemoteCommand();
    }

    /**
     * Print message and return exit code when
     * we detect that the DAS is not running.
     */
    private int instanceNotRunning()
            throws CommandException, CommandValidationException {
        // by definition this is not an error
        // https://glassfish.dev.java.net/issues/show_bug.cgi?id=8387

        logger.printWarning(strings.get("StopInstance.instanceNotRunning"));
        return 0;
    }

    // TODO -- I don't think this will work in all cases!
    // e.g. localhost, vaio, vaio.sfbay, vaio.sfbay.sun.com should all
    // match on my machine
    
    private boolean isLocalInstance() {
        return programOpts.getHost().equals(CLIConstants.DEFAULT_HOSTNAME);
    }

    /**
     * Execute the actual stop-domain command.
     */
    private final int doRemoteCommand()
            throws CommandException, CommandValidationException {
        // run the remote stop-domain command and throw away the output
        RemoteCommand cmd = new RemoteCommand(getName(), programOpts, env);
        cmd.executeAndReturnOutput("stop-domain");
        waitForDeath();
        return 0;
    }

    /**
     * Wait for the server to die.
     */
    private void waitForDeath() throws CommandException {
        if(!programOpts.isTerse()) {
            // use stdout because logger always appends a newline
            System.out.print(strings.get("StopInstance.waitForDeath") + " ");
        }
        long startWait = System.currentTimeMillis();
        boolean alive = true;
        int count = 0;

        while(!timedOut(startWait)) {
            if(!isRunning()) {
                alive = false;
                break;
            }
            try {
                Thread.sleep(100);
                if(!programOpts.isTerse() && count++ % 10 == 0)
                    System.out.print(".");
            }
            catch (InterruptedException ex) {
                // don't care
            }
        }

        if(!programOpts.isTerse())
            System.out.println();

        if(alive) {
            throw new CommandException(strings.get("StopInstance.instanceNotDead",
                    (CLIConstants.DEATH_TIMEOUT_MS / 1000)));
        }
    }

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > CLIConstants.DEATH_TIMEOUT_MS;
    }
}
