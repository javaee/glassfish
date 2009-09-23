/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;


/**
 * THe restart-domain command.
 * The local portion of this command is only used to block until:
 * <ul><li>the old server dies
 * <li>the new server starts
 * </ul>
 * Tactics:
 * <ul>
 * <li>Get the uptime for the current server
 * <li>start the remote Restart command
 * <li>Call uptime in a loop until the uptime number is less than
 * the original uptime
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "restart-domain")
@Scoped(PerLookup.class)
public class RestartDomainCommand extends StopDomainCommand {
    @Inject
    private Habitat habitat;

    private long uptimeOldServer;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(RestartDomainCommand.class);

    /**
     * Execute the restart-domain command.
     */
    @Override
    protected void doCommand()
            throws CommandException, CommandValidationException {
        // first, find out how long the server has been up
        uptimeOldServer = getUptime();  // may throw CommandException

        // run the remote restart-domain command and throw away the output
        RemoteCommand cmd =
            new RemoteCommand("restart-domain", programOpts, env);
        cmd.executeAndReturnOutput("restart-domain");
        waitForRestart();
        logger.printMessage(strings.get("restartDomain.success"));
    }

    /**
     * If the server isn't running, try to start it.
     */
    @Override
    protected int dasNotRunning()
            throws CommandException, CommandValidationException {
        logger.printWarning(strings.get("restart.dasNotRunning"));
        CLICommand cmd = habitat.getComponent(CLICommand.class, "start-domain");
        // XXX - assume start-domain accepts all the same options
        return cmd.execute(argv);
    }

    /**
     * Get uptime from the server.
     */
    private long getUptime()
            throws CommandException, CommandValidationException {
        RemoteCommand cmd = new RemoteCommand("uptime", programOpts, env);
        String up = cmd.executeAndReturnOutput("uptime");
        long up_ms = parseUptime(up);

        if (up_ms <= 0) {
            throw new CommandException(strings.get("restart.dasNotRunning"));
        }

        logger.printDebugMessage("server uptime: " + up_ms);
        return up_ms;
    }

    /**
     * The remote uptime command returns a string like:
     * Uptime: 10 minutes, 53 seconds, Total milliseconds: 653859\n
     * We find that last number and extract it.
     * XXX - this is pretty gross, and fragile
     */
    private long parseUptime(String up) {
        if (up == null || up.length() < 4)
            return 0;

        up = up.trim();
        int index = up.lastIndexOf(':');
        if (index < 0)
            return 0;

        if (up.length() - index < 3)
            return 0;

        try {
            return Long.parseLong(up.substring(index + 2));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Wait for the server to restart.
     */
    private void waitForRestart() throws CommandException {
        long end = CLIConstants.WAIT_FOR_DAS_TIME_MS +
                                        System.currentTimeMillis();

        while (System.currentTimeMillis() < end) {
            try {
                Thread.sleep(300);
                if (domainName != null) {
                    // local password will change when server restarts
                    initializeLocalPassword(domainRootDir);
                }
                long up = getUptime();

                if (up > 0 && up < uptimeOldServer)
                    return;
            } catch (Exception e) {
                // continue
            }
        }
        // if we get here -- we timed out
        throw new CommandException(strings.get("restartDomain.noGFStart"));
    }
}
