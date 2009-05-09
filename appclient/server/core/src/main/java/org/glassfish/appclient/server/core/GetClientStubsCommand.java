/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
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

package org.glassfish.appclient.server.core;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.universal.i18n.LocalStrings;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.deployment.admin.DeployCommand;
import org.glassfish.deployment.common.DownloadableArtifacts;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 *
 * @author tjquinn
 */
@Service(name="get-client-stubs")
@I18n("get.client.stubs")
@Scoped(PerLookup.class)
public class GetClientStubsCommand implements AdminCommand {

    private final static String APPNAME = "appname";

    private final static LocalStringManager localStrings =
            new LocalStringManagerImpl(GetClientStubsCommand.class);

    @Inject
    private DownloadableArtifacts downloadableArtifacts;

    @Inject
    private Applications apps;

    @Param(name = APPNAME, optional=false)
    private String appname = null;

    @Param(primary=true)
    private String localDir;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        Application matchingApp = null;
        for (Application app : apps.getApplications()) {
            if (app.getName().equals(appname)) {
                matchingApp = app;
                break;
            }
        }
        if (matchingApp == null) {
            report.failure(logger, localStrings.getLocalString(
                getClass(),
                "get-client-stubs.noSuchApp",
                "Application {0} was not found",
                new Object[] {appname}));
            return;
        }
        try {
            DeployCommand.retrieveArtifacts(context, appname, localDir, downloadableArtifacts);
        } catch (Exception e) {
            report.setFailureCause(e);
            report.failure(logger, localStrings.getLocalString(
                    getClass(),
                    "get-client-stubs.errorPrepDownloadedFiles",
                    "Error preparing for download"), e);
            return;
        }
    }
}
