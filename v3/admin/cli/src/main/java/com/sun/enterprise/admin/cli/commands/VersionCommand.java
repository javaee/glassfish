/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli.commands;

import java.util.*;
import java.util.logging.*;
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.cli.framework.ValidOption;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import static com.sun.enterprise.admin.cli.CLIConstants.EOL;

/**
 * A local version command.
 * Prints the version of the server, if running. Prints the version from locally
 * available Version class if server is not running or if the version could not
 * be obtained from a server for some reason. The idea is to get the version
 * of server software, the server process need not be running. This command
 * does not return the version of local server installation if its
 * options (host, port, user, passwordfile) identify a running server.
 * 
 * @author km@dev.java.net
 * @author Bill Shannon
 */
public class VersionCommand extends RemoteCommand {
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(VersionCommand.class);

    public VersionCommand(String name, ProgramOptions po,
            Environment env) throws CommandException {
        super(name, po, env);
    }

    @Override
    protected void fetchCommandMetadata() {
        /*
         * Don't fetch information from server.
         * We need to work even if server is down.
         */
        Set<ValidOption> opts = new HashSet<ValidOption>();
        commandOpts = Collections.unmodifiableSet(opts);
        operandType = "STRING";
        operandMin = 0;
        operandMax = 0;
    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            super.executeCommand();
        } catch (Exception e) {
            // suppress all output and infer that the server is not running
            printRemoteException(e);
            invokeLocal();
        }
        return 0;       // always succeeds
    }

    private void invokeLocal() {
        String fv = Version.getFullVersion();
        String cn = Version.class.getName();
        String msg = strings.get("version.local", cn, fv);
        logger.printMessage(msg);
    }

    private void printRemoteException(Exception e) {
        String host = po.getHost();
        String port = po.getPort() + "";
        String msg = strings.get("remote.version.failed", host, port);
        logger.printMessage(msg);
        logger.printDebugMessage(e.getMessage());        
    }
}
