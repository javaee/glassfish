/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Node;

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RuntimeType;
import org.jvnet.hk2.annotations.*;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.jvnet.hk2.component.*;
import java.util.logging.Logger;
import java.util.List;

@Service(name = "list-nodes")
@Scoped(PerLookup.class)
@I18n("list.nodes.command")
public class ListNodesCommand implements AdminCommand{

    @Inject
    private Domain domain;
    @Inject
    private ServerEnvironment env;
    @Inject
    private Nodes nodes;
    @Param(optional = true, defaultValue = "false")
    private boolean verbose;
    @Param(optional = true)
    private boolean terse;

    
    private ActionReport report;
    private ActionReport.MessagePart top;
    private static final String EOL = "\n";
    Logger logger;

    @Override
    public void execute(AdminCommandContext context) {

        report = context.getActionReport();
        top = report.getTopMessagePart();

        Logger logger = context.getLogger();

        StringBuilder sb = new StringBuilder();
        boolean firstNode = true;
        List<Node> nodeList=nodes.getNode();
        
        for (Node n : nodeList) {

            String name = n.getName();
            String type = n.getType();
            String host = n.getNodeHost();
            if (host == null)
                host = " ";

            if (firstNode)
                firstNode = false;
            else
                sb.append(EOL);

            if (terse)
                sb.append(name);
            else
                sb.append(name + "  "+ type + "  "+ host);

        }
        report.setMessage(sb.toString());
        report.setActionExitCode(ExitCode.SUCCESS);

    }
}
