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
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import com.sun.enterprise.admin.cli.util.*;
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

    private final static long WAIT_FOR_DAS_TIME_MS = 60000;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StopDomainCommand.class);

    /**
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, "domaindir", '\0', "STRING", false, null);
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "domain_name";
        operandType = "STRING";
        operandMin = 0;
        operandMax = 1;

        processProgramOptions();
    }

    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        int adminPort = getAdminPort(getDomainXml());
        programOpts.setPort(adminPort);

        // Verify that the DAS is running and reachable
        if (!DASUtils.pingDASQuietly(programOpts, env)) {
            // by definition this is not an error
            // https://glassfish.dev.java.net/issues/show_bug.cgi?id=8387
            logger.printWarning(strings.get("StopDomain.dasNotRunning"));
            return 0;
        }

        // run the remote stop-domain command and throw away the output
        RemoteCommand cmd = new RemoteCommand("stop-domain", programOpts, env);
        cmd.executeAndReturnOutput("stop-domain");
        waitForDeath(adminPort);
        return 0;
    }

    private void waitForDeath(int adminPort) throws CommandException {
        // 1) it's impossible to use the logger to print anything without \n
        // 2) The Logger is set to WARNING right now to kill the version
        //    messages
        // that's why I'm writing to stderr

        long startWait = System.currentTimeMillis();
        System.err.print(strings.get("StopDomain.WaitDASDeath") + " ");
        boolean alive = true;

        while (!timedOut(startWait)) {
            if (!isRunning(adminPort)) {
                alive = false;
                break;
            }
            try {
                Thread.sleep(100);
                System.err.print(".");
            } catch (InterruptedException ex) {
                // don't care
            }
        }

        System.err.println();

        if (alive) {
            throw new CommandException(strings.get("StopDomain.DASNotDead",
                    (WAIT_FOR_DAS_TIME_MS / 1000)));
        }
    }

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }
}
