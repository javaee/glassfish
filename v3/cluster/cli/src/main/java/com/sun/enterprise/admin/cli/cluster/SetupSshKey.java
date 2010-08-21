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

package com.sun.enterprise.admin.cli.cluster;

import java.io.IOException;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.internal.api.Globals;
import com.sun.enterprise.admin.cli.CLICommand;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;

/**
 *  This is a local command that distributes the public key (RSA) to remote node(s)
 *
 */
@Service(name = "_setup-ssh-key")
@Scoped(PerLookup.class)
public final class SetupSshKey extends CLICommand {
    
    @Param(optional = false, password=true)
    private String sshpassword;

    @Param(optional = true)
    private String sshuser;

    @Param(optional=true)
    private int sshport;

    @Param(optional = true)
    private String sshkeyfile;

    @Param(optional = true)
    private String sshpublickeyfile;

    @Param(optional = false, primary = true, multiple = true)
    private String[] nodes;

    @Inject
    private Habitat habitat;

    /**
     */
    @Override
    protected void validate()
            throws CommandException {

        if(sshuser==null) {
            sshuser = System.getProperty("user.name");
        }

        if(sshport==0) {
            sshport=22;
        }
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {

        SSHLauncher sshL=habitat.getComponent(SSHLauncher.class);
        Globals.setDefaultHabitat(habitat);

        for (String node : nodes) {
            sshL.init(sshuser, node,  sshport, sshpassword, sshkeyfile, "", logger.getLogger());
            try {
                sshL.setupKey(node, sshpublickeyfile);
            } catch (IOException ce) {
                logger.printDebugMessage("SSH key setup failed: " + ce.getMessage());
                throw new CommandException("SSH key setup failed: " + ce.getMessage());
            } catch (Exception e) {
                //handle KeyStoreException
            }
            if (sshL.checkConnection())
                logger.printDebugMessage("Connection SUCCEEDED!");
        }
        return SUCCESS;
    }
}
