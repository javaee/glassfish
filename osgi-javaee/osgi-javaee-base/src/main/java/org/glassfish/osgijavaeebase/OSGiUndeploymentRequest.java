/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgijavaeebase;

import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;
import org.osgi.framework.Bundle;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a stateful service. It is responsible for undeployment of
 * the artifact from JavaEE runtime.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class OSGiUndeploymentRequest
{
    private static final Logger logger =
            Logger.getLogger(OSGiUndeploymentRequest.class.getPackage().getName());

    private Deployment deployer;
    private ServerEnvironmentImpl env;
    private ActionReport reporter;
    /**
     * Application being undeployed
     */
    private OSGiApplicationInfo osgiAppInfo;

    public OSGiUndeploymentRequest(Deployment deployer,
                                     ServerEnvironmentImpl env,
                                     ActionReport reporter,
                                     OSGiApplicationInfo osgiAppInfo)
    {
        this.deployer = deployer;
        this.env = env;
        this.reporter = reporter;
        this.osgiAppInfo = osgiAppInfo;
    }

    protected void preUndeploy() {}

    protected void postUndeploy() {}

    public void execute()
    {
        preUndeploy();
        // TODO(Sahoo): There may be side effect of creating a deployment context
        // as that leads to creation of class loaders again.
        OSGiDeploymentContext dc;
        try
        {
            dc = getDeploymentContextImpl(
                    reporter,
                    logger,
                    osgiAppInfo.getAppInfo().getSource(),
                    getUndeployParams(osgiAppInfo),
                    env,
                    osgiAppInfo.getBundle());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
        }

        final ApplicationInfo appInfo = osgiAppInfo.getAppInfo();
        appInfo.stop(dc, logger);
        appInfo.unload(dc);
        deployer.undeploy(appInfo.getName(), dc);
        if (!osgiAppInfo.isDirectoryDeployment())
        {
            // We can always assume dc.getSourceDir will return a valid file
            // because we would have expanded the app during deployment.
            cleanup(dc.getSourceDir());
        }
        postUndeploy();
    }

    protected abstract OSGiDeploymentContext getDeploymentContextImpl(ActionReport reporter, Logger logger, ReadableArchive source, UndeployCommandParameters undeployParams, ServerEnvironmentImpl env, Bundle bundle) throws Exception;

    private void cleanup(File dir)
    {
        assert (dir.isDirectory() && dir.exists());
        FileUtils.whack(dir);
        logger.logp(Level.INFO, "OSGiUndeploymentRequest", "cleanup",
                "Deleted {0}", new Object[]{dir});
    }

    protected UndeployCommandParameters getUndeployParams(
            OSGiApplicationInfo osgiAppInfo)
    {
        UndeployCommandParameters parameters =
                new UndeployCommandParameters();
        parameters.name = osgiAppInfo.getAppInfo().getName();
        parameters.origin = DeployCommandParameters.Origin.undeploy;
        return parameters;
    }

    protected OSGiApplicationInfo getOsgiAppInfo() {
        return osgiAppInfo;
    }
}
