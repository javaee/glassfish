/*
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

package org.glassfish.deployment.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Domain;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.deployment.common.DeploymentUtils;

/**
 * List application ref command
 */
@Service(name="list-application-refs")
@I18n("list.application.refs")
@Cluster(value={RuntimeType.DAS})
@Scoped(PerLookup.class)
@TargetType(value={CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
public class ListApplicationRefsCommand implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListApplicationRefsCommand.class);

    @Param(primary=true, optional=true)
    String target = "server";

    @Param(optional=true, defaultValue="false", shortName="v")
    public Boolean verbose = false;

    @Inject
    Domain domain;

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        ActionReport.MessagePart part = report.getTopMessagePart();
        int numOfApplications = 0;

        for (ApplicationRef ref : domain.getApplicationRefsInTarget(target)) {
            ActionReport.MessagePart childPart = part.addChild();
            String message = ref.getRef();
            if( verbose ){
                message += getVerboseStatus(ref);
            }
            childPart.setMessage(message);
            numOfApplications++;
        }
        if (numOfApplications == 0) {
            part.setMessage(localStrings.getLocalString("list.components.no.elements.to.list", "Nothing to List."));
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private String getVerboseStatus(ApplicationRef ref) {
       String message = "";
       if (DeploymentUtils.isDomainTarget(target)) {
           // ignore --verbose for target domain
           return message;
       }
       boolean isVersionEnabled = domain.isAppRefEnabledInTarget(ref.getRef(), target);
       if ( isVersionEnabled ) {
           message = localStrings.getLocalString("list.applications.verbose.enabled", "(enabled)");
       } else {
           message = localStrings.getLocalString("list.applications.verbose.disabled", "(disabled)");
       }
       return message;
   }
}
