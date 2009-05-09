/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.cli;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.cli.remote.CommandInvoker;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;

/** Prints the version of the server, if running. Prints the version from locally
 *  available Version class if server is not running or if the version could not
 *  be obtained from a server for some reason. The idea is to get the version
 *  of server software, the server process need not be running. This command
 *  does not return the version of local server installation if the server its
 *  options (host, port, user, passwordfile) identify a running server. This
 *  is a deficiency in the implemetation of this command, but that's the way it is.
 * 
 * @author km@dev.java.net
 * @since  GlassFish v3 Prelude
 */

public final class VersionCommand extends AbstractCommand {

    @Override
    public void runCommand() throws CommandException, CommandValidationException {
        super.validateOptions();
        try {
            invokeRemote();
        } catch(Exception e) {
            //suppress all output and infer that the server is not running
            printRemoteException(e);
            invokeLocal();
        }
    }
    
    private void invokeRemote() throws CommandException {
        CommandInvoker in = new CommandInvoker(super.getName());
        in.put(HOST, getOption(HOST));
        in.put(PORT, getOption(PORT));
        in.put(USER, getOption(USER));
        in.put(PASSWORDFILE, getOption(PASSWORDFILE));
        in.put(SECURE, getOption(SECURE));
        in.put(TERSE, getOption(TERSE));        
        in.invoke();
    }
    
    private void invokeLocal() {
        String fv = Version.getFullVersion();
        String cn = Version.class.getName();
        String msg = super.getLocalizedString("version.local", new String[]{cn, fv});
        CLILogger.getInstance().printMessage(msg);
    }
    private void printRemoteException(Exception e) {
        String host  = getOption(HOST) == null ? CLIConstants.DEFAULT_HOSTNAME : getOption(HOST);
        String ports = getOption(PORT) == null ? "" + CLIConstants.DEFAULT_ADMIN_PORT : getOption(PORT);
        String msg = super.getLocalizedString("remote.version.failed", new String[]{host, ports});
        CLILogger.getInstance().printMessage(msg);
        CLILogger.getInstance().printDebugMessage(e.getMessage());        
    }
}
