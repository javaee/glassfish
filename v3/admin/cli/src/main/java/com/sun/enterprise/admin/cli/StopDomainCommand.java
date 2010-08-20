/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package com.sun.enterprise.admin.cli;

import java.io.*;
import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.admin.cli.remote.DASUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * The stop-domain command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "stop-domain")
@Scoped(PerLookup.class)
public class StopDomainCommand extends LocalDomainCommand {
    @Param(name = "domain_name", primary = true, optional = true)
    private String userArgDomainName;
    private static final long WAIT_FOR_DAS_TIME_MS = 60000; // 1 minute
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StopDomainCommand.class);

    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        setDomainName(userArgDomainName);
        super.validate(); // which calls initDomain()
    }

    /**
     * Override initDomain in LocalDomainCommand to only initialize
     * the local domain information (name, directory) in the local
     * case, when no --host has been specified.
     */
    @Override
    protected void initDomain() throws CommandException {
        // only initialize local domain information if it's a local operation
        if(programOpts.getHost().equals(CLIConstants.DEFAULT_HOSTNAME))
            super.initDomain();
        else if(userArgDomainName != null)   // remote case
            throw new CommandException(
                    strings.get("StopDomain.noDomainNameAllowed"));
    }

    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        boolean isLocal = getServerDirs().getServerName() != null;

        if(isLocal) {
            // if the local password isn't available, the domain isn't running
            // (localPassword is set by initDomain)
            if(getServerDirs().getLocalPassword() == null)
                return dasNotRunning();

            int adminPort = getAdminPort();
            programOpts.setPort(adminPort);
            logger.printDebugMessage("Stopping local domain on port "
                    + adminPort);

            /*
             * If we're using the local password, we don't want to prompt
             * for a new password.  If the local password doesn't work it
             * most likely means we're talking to the wrong server.
             */
            programOpts.setInteractive(false);
            // in the local case, make sure we're talking to the correct DAS

            if(!isThisDAS(getDomainRootDir()))
                return dasNotRunning();
            logger.printDebugMessage("It's the correct DAS");
        }
        else { // remote
            // Verify that the DAS is running and reachable
            if(!DASUtils.pingDASQuietly(programOpts, env))
                return dasNotRunning();

            logger.printDebugMessage("DAS is running");
            programOpts.setInteractive(false);
        }

        /*
         * At this point any options will have been prompted for, and
         * the password will have been prompted for by pingDASQuietly,
         * so even if the password is wrong we don't want any more
         * prompting here.
         */

        doCommand();
        return 0;
    }

    /**
     * Print message and return exit code when
     * we detect that the DAS is not running.
     */
    protected int dasNotRunning()
            throws CommandException, CommandValidationException {
        // by definition this is not an error
        // https://glassfish.dev.java.net/issues/show_bug.cgi?id=8387
        logger.printWarning(strings.get("StopDomain.dasNotRunning"));
        return 0;
    }

    /**
     * Execute the actual stop-domain command.
     */
    protected void doCommand()
            throws CommandException, CommandValidationException {
        // run the remote stop-domain command and throw away the output
        RemoteCommand cmd = new RemoteCommand(getName(), programOpts, env);
        cmd.executeAndReturnOutput("stop-domain");
        waitForDeath();
    }

    /**
     * Wait for the server to die.
     */
    protected void waitForDeath() throws CommandException {
        if(!programOpts.isTerse()) {
            // use stdout because logger always appends a newline
            System.out.print(strings.get("StopDomain.WaitDASDeath") + " ");
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
            throw new CommandException(strings.get("StopDomain.DASNotDead",
                    (WAIT_FOR_DAS_TIME_MS / 1000)));
        }
    }

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }
}
