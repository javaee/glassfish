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

import java.util.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;


/**
 *  This is a local command that calls the primitive remote create-instance to add the
 *  entries in domain.xml and then the primitive local command _create-instance-filesystem
 *  to create the empty directory structure and das.properties
 *
 */
@Service(name = "create-local-instance")
@Scoped(PerLookup.class)
public final class CreateLocalInstanceCommand extends CreateLocalInstanceFilesystemCommand {

    @Param(name = "filesystemonly", optional = true, defaultValue = "false")
    private boolean filesystemOnly = false;

    @Param(name = "config", optional = true)
    private String configName;

    @Param(name = "cluster", optional = true)
    private String clusterName;

    @Param(name = "systemproperties", optional = true, separator = ':')
    private String systemProperties;     // XXX - should it be a Properties?

    public static final String RENDEZVOUS_PROPERTY_NAME = "rendezvousOccurred";

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CreateLocalInstanceCommand.class);

    /**
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException  {
        if (configName != null && clusterName != null)
            throw new CommandValidationException(
                                        strings.get("ConfigClusterConflict"));

        super.validate();
        
        if (!rendezvousWithDAS()) {
           throw new CommandValidationException(
                                        strings.get("Unable to rendezvous with DAS on host={0}, port={1}, protocol={2}", DASHost, DASPort, DASProtocol));
        }
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
            int exitCode = -1;
            int exitCodeRegister = -1;
            if (!this.filesystemOnly) {
               exitCodeRegister = registerToDAS();
            }
            if (exitCodeRegister == SUCCESS) {
                exitCode = super.executeCommand();
            } else {
                exitCode = exitCodeRegister;
            }

            return exitCode;
    }

    private boolean rendezvousWithDAS() {
        try {
            RemoteAdminCommand rac = new RemoteAdminCommand("uptime", DASHost, DASPort, dasIsSecure, "admin", null, logger.getLogger());
            rac.setConnectTimeout(10000);
            ParameterMap map = new ParameterMap();
            rac.executeCommand(map);
            return true;
        } catch (CommandException ex) {
            return false;
        }
    }

    private int registerToDAS() throws CommandException {
        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add(0, "create-instance");
        if (clusterName != null) {
            argsList.add("--cluster");
            argsList.add(clusterName);
        }
        if (configName != null) {
            argsList.add("--config");
            argsList.add(configName);
        }
        if (nodeAgent != null) {
            argsList.add("--nodeagent");
            argsList.add(nodeAgent);
        }
        if (systemProperties != null) {
            argsList.add("--systemproperties");
            argsList.add(systemProperties);
        }
        argsList.add(this.instanceName);

        String[] argsArray = new String[argsList.size()];
        argsArray = argsList.toArray(argsArray);

        RemoteCommand rc = new RemoteCommand("create-instance", this.programOpts, this.env);
        return rc.execute(argsArray);
    }
}
