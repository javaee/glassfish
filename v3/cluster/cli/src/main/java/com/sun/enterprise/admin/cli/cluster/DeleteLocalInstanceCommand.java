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

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.FileUtils;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;

import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;


// TODO TODO
// wipe out the tree if this is the last instance
// TODO TODO TODO

/**
 * Delete a local server instance.
 */
@Service(name = "delete-local-instance")
@Scoped(PerLookup.class)
public class DeleteLocalInstanceCommand extends LocalInstanceCommand {
    @Param(name = "instance_name", primary = true, optional = true)
    private String instanceName0;
    @Param(name = "filesystemonly", primary = false, optional = true, defaultValue = "false")
    private boolean localOnly;
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(DeleteLocalInstanceCommand.class);

    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        instanceName = instanceName0;
        super.validate();

        if(!StringUtils.ok(getServerDirs().getServerName()))
            throw new CommandException(strings.get("DeleteInstance.noInstanceName"));
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        if(isRunning()) {
            throw new CommandException(strings.get("DeleteInstance.running"));
        }

        if(!localOnly)
            doRemote();

        doLocal();
        return SUCCESS;
    }

    /**
     * Ask DAS to wipe it out from domain.xml
     */
    private void doRemote() throws CommandException {
        try {
            RemoteCommand rc = new RemoteCommand("_unregister-instance", programOpts, env);
            rc.execute("_unregister-instance",
                    "--nodeagent", getServerDirs().getServerParentDir().getName(),
                    //"--remote_only", "true",
                    getServerDirs().getServerName());
        }
        catch(CommandException ce) {
            // Let's add our $0.02 to this Exception!
            Throwable t = ce.getCause();
            String newString = strings.get("DeleteInstance.remoteError", 
                    ce.getLocalizedMessage());

            if(t != null)
                throw new CommandException(newString, t);
            else
                throw new CommandException(newString);
        }
    }

    private void doLocal() throws CommandException {
        File whackee = getServerDirs().getServerDir();

        if(whackee == null || !whackee.isDirectory()) {
            throw new CommandException(strings.get("DeleteInstance.noWhack",
                    whackee));
        }

        FileUtils.whack(whackee);

        if(whackee.isDirectory())
            throw new CommandException(strings.get("DeleteInstance.badWhack",
                    whackee));

    }
}
