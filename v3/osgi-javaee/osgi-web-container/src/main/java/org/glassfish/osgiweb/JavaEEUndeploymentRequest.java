/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgiweb;

import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a stateful service. It is responsible for undeployment of
 * the artifact from JavaEE runtime.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JavaEEUndeploymentRequest
{
    private static final Logger logger =
            Logger.getLogger(JavaEEUndeploymentRequest.class.getPackage().getName());

    private Deployment deployer;
    private ServerEnvironmentImpl env;
    private ActionReport reporter;
    /**
     * Application being undeployed
     */
    private OSGiWebContainer.OSGiApplicationInfo osgiAppInfo;
    private OSGiDeploymentContextImpl dc;

    public JavaEEUndeploymentRequest(Deployment deployer,
                                     ServerEnvironmentImpl env,
                                     ActionReport reporter,
                                     OSGiWebContainer.OSGiApplicationInfo osgiAppInfo)
    {
        this.deployer = deployer;
        this.env = env;
        this.reporter = reporter;
        this.osgiAppInfo = osgiAppInfo;
    }

    public void execute()
    {
        // TODO(Sahoo): There may be side effect of creating a deployment context
        // as that leads to creation of class loaders again.
        try
        {
            dc = new OSGiDeploymentContextImpl(
                    reporter,
                    logger,
                    osgiAppInfo.appInfo.getSource(),
                    getUndeployParams(osgiAppInfo),
                    env,
                    osgiAppInfo.bundle);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
        }

        deployer.undeploy(osgiAppInfo.appInfo.getName(), dc);
        if (!osgiAppInfo.isDirectoryDeployment)
        {
            // We can always assume dc.getSourceDir will return a valid file
            // because we would have expanded the app during deployment.
            cleanup(dc.getSourceDir());
        }
        logger.logp(Level.INFO, "JavaEEUndeploymentRequest", "undeploy",
                "Undeployed bundle {0} from {1}", new Object[]{osgiAppInfo.bundle,
                dc.getSource().getURI()});
    }

    private void cleanup(File dir)
    {
        assert (dir.isDirectory() && dir.exists());
        FileUtils.whack(dir);
        logger.logp(Level.INFO, "JavaEEUndeploymentRequest", "cleanup",
                "Deleted {0}", new Object[]{dir});
    }

    private UndeployCommandParameters getUndeployParams(
            OSGiWebContainer.OSGiApplicationInfo osgiAppInfo)
    {
        UndeployCommandParameters parameters =
                new UndeployCommandParameters();
        parameters.name = osgiAppInfo.appInfo.getName();
        parameters.origin = DeployCommandParameters.Origin.undeploy;
        return parameters;
    }

}
