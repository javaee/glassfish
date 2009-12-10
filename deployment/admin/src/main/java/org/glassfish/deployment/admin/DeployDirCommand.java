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

package org.glassfish.deployment.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.annotations.Scoped;


/**
 *
 * deploydir command
 *
 */
@Service(name="deploydir")
@Scoped(PerLookup.class)
@I18n("deploydir.command")
public class DeployDirCommand extends DeployCommand implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeployDirCommand.class);
    
    /**
     * Executes the command.
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        ActionReport.MessagePart msgPart = report.getTopMessagePart();
        msgPart.setChildrenType("WARNING");
        ActionReport.MessagePart childPart = msgPart.addChild();
        childPart.setMessage(localStrings.getLocalString("deploydir.command.deprecated", "{0} command deprecated.  Please use {1} command instead.", "deploydir", "deploy"));
        super.execute(context);
    }
}
