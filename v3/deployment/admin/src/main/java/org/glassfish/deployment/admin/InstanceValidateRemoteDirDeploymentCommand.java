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

import java.io.File;
import java.util.logging.Level;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * Instance-only command which makes sure that a deployment directory seems to
 * be the same when viewed from this instance as when viewed from the DAS.
 * <p>
 * The DAS computes a checksum for the deployment directory as it sees it and
 * passes it as a parameter to this command.  This command (on each instance)
 * computes a checksum for the path passed to it.  If the checksums agree
 * then we conclude that the DAS and this instance saw the same files in the
 * directory and this command reports success; otherwise this command reports
 * failure.
 *
 * @author Tim Quinn
 */
@Service(name="_instanceValidateRemoteDirDeployment")
@Scoped(PerLookup.class)
@Cluster(value={RuntimeType.INSTANCE})
public class InstanceValidateRemoteDirDeploymentCommand implements AdminCommand {

    @Param(primary=true)
    private File path;

    @Param
    private String checksum;

    @Override
    public void execute(AdminCommandContext context) {
        context.getLogger().log(Level.FINE,
                "Running _instanceValidateRemoteDirDeployment with {0} and checksum {1}",
                new Object[]{path.getAbsolutePath(), checksum});
        final long myChecksum = DeploymentUtils.checksum(path);
        final ActionReport report = context.getActionReport();
        report.setActionExitCode(
                (Long.parseLong(checksum) == myChecksum
                  ? ActionReport.ExitCode.SUCCESS
                  : ActionReport.ExitCode.FAILURE));
    }
}
