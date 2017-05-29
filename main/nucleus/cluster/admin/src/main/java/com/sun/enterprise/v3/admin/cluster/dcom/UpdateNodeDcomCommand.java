/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.enterprise.v3.admin.cluster.dcom;

import com.sun.enterprise.util.cluster.RemoteType;
import com.sun.enterprise.v3.admin.cluster.*;

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.config.serverbeans.SshAuth;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.hk2.api.PerLookup;

import javax.inject.Inject;


import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;
import java.util.logging.Logger;

/**
 * Remote AdminCommand to update a DCOM node.
 *
 * @author Byron Nevins
 */
@Service(name = "update-node-dcom")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Node.class,
        opType=RestEndpoint.OpType.POST,
        path="update-node-dcom",
        description="Update Node DCOM",
        params={
            @RestParam(name="id", value="$parent")
        })
})

public class UpdateNodeDcomCommand extends UpdateNodeRemoteCommand  {
    @Param(name = "windowsuser", shortName = "w", optional = true, defaultValue = "${user.name}")
    private String windowsuser;
    @Param(name = "windowspassword", optional = true, password = true)
    private String windowspassword;
    @Param(name = "windowsdomain", shortName = "d", optional = true, defaultValue = "")
    private String windowsdomainInSubClass;

    @Override
    public void execute(AdminCommandContext context) {
        executeInternal(context);
    }

    @Override
    protected RemoteType getType() {
        return RemoteType.DCOM;
    }

    @Override
    protected String getDefaultPort() {
        return NodeUtils.NODE_DEFAULT_DCOM_PORT;
    }

    @Override
    protected void populateParameters() {
        remotePort = "135";
        remoteUser = windowsuser;
        sshkeyfile = null;
        remotepassword = windowspassword;
        sshkeypassphrase = null;
        windowsdomain = windowsdomainInSubClass;
    }
}
