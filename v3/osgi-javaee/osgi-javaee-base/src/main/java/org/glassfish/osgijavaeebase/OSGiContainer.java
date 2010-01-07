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


package org.glassfish.osgijavaeebase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.api.Globals;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.ActionReport;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URI;

import com.sun.enterprise.deploy.shared.ArchiveFactory;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class OSGiContainer {

    // Context in which this object is operating.
    private BundleContext context;

    protected Map<Bundle, OSGiApplicationInfo> applications =
            new HashMap<Bundle, OSGiApplicationInfo>();
    protected Map<OSGiApplicationInfo, ServiceRegistration> regs = new HashMap<OSGiApplicationInfo, ServiceRegistration>();
    private static final Logger logger =
            Logger.getLogger(OSGiContainer.class.getPackage().getName());

    private Deployment deployer = Globals.get(Deployment.class);
    private ArchiveFactory archiveFactory = Globals.get(ArchiveFactory.class);
    private ServerEnvironmentImpl env = Globals.get(ServerEnvironmentImpl.class);

    protected OSGiContainer(BundleContext ctx) {
        this.context = ctx;
    }

    protected BundleContext getBundleContext() {
        return context;
    }
    /**
     * Deploys an application bundle in underlying application container in GlassFish.
     * This method is synchronized because we don't know if GlassFish
     * deployment framework can handle concurrent requests or not.
     *
     * @param b Bundle to be deployed.
     */
    public synchronized void deploy(Bundle b) throws Exception {
        preDeploy(b);
        OSGiApplicationInfo osgiAppInfo = applications.get(b);
        if (osgiAppInfo != null) {
            logger.logp(Level.WARNING, "OSGiContainer", "deploy",
                    "Bundle {0} is already deployed at {1} ", new Object[]{b,
                    osgiAppInfo.getAppInfo().getSource()});
            return;
        }
        // deploy the java ee artifacts
        ActionReport report = getReport();
        osgiAppInfo = deployJavaEEArtifacts(b, report);
        if (osgiAppInfo == null) {
            throw new Exception("Deployment of " + b + " failed because of following reason: " + report.getMessage());
        }
        ServiceRegistration reg = context.registerService(OSGiApplicationInfo.class.getName(), osgiAppInfo, new Properties());
        regs.put(osgiAppInfo, reg);
        postDeploy(osgiAppInfo);
    }

    /**
     * Undeploys a web application bundle.
     * This method is synchronized because we don't know if GlassFish
     * deployment framework can handle concurrent requests or not.
     *
     * @param b Bundle to be undeployed
     */
    public synchronized void undeploy(Bundle b) throws Exception
    {
        OSGiApplicationInfo osgiAppInfo = applications.get(b);
        if (osgiAppInfo == null)
        {
            throw new RuntimeException("No applications for bundle " + b);
        }
        regs.remove(osgiAppInfo).unregister();
        preUndeploy(osgiAppInfo);
        ActionReport report = getReport();
        undeployJavaEEArtifacts(osgiAppInfo, report);
        URI location = osgiAppInfo.getAppInfo().getSource().getURI();
        switch (report.getActionExitCode())
        {
            case FAILURE:
                logger.logp(Level.WARNING, "OSGiContainer", "undeploy",
                        "Failed to undeploy {0} from {1}. See previous messages for " +
                                "further information.", new Object[]{b, location});
                break;
            default:
                logger.logp(Level.INFO, "OSGiContainer", "undeploy",
                        "Undeployed bundle {0} from {1}", new Object[]{b, location});
                break;
        }
        applications.remove(b);
        postUndeploy(osgiAppInfo);
    }

    public void undeployAll()
    {
        // Take a copy of the entries as undeploy changes the underlying map.
        for (Bundle b : new HashSet<Bundle>(applications.keySet()))
        {
            try
            {
                undeploy(b);
            }
            catch (Exception e)
            {
                logger.logp(Level.SEVERE, "OSGiContainer", "undeployAll",
                        "Exception undeploying bundle {0}",
                        new Object[]{b.getLocation()});
                logger.logp(Level.SEVERE, "OSGiContainer", "undeployAll",
                        "Exception Stack Trace", e);
            }
        }
    }

    protected void preDeploy(Bundle b) throws Exception {}

    protected void postDeploy(OSGiApplicationInfo osgiApplicationInfo) throws Exception {}

    protected void preUndeploy(OSGiApplicationInfo osgiApplicationInfo) throws Exception {}

    protected void postUndeploy(OSGiApplicationInfo osgiApplicationInfo) throws Exception {}

    /**
     * Does necessary deployment in Java EE container
     * @param b
     * @param report
     * @return
     */
    private OSGiApplicationInfo deployJavaEEArtifacts(Bundle b, ActionReport report)
    {
        OSGiDeploymentRequest request = createOSGiDeploymentRequest(
                deployer, archiveFactory, env, report, b);
        return request.execute();
    }

    /**
     * Does necessary undeployment in Java EE container
     * @param osgiAppInfo
     * @param report
     * @return
     */
    protected ActionReport undeployJavaEEArtifacts(OSGiApplicationInfo osgiAppInfo,
                                                 ActionReport report)
    {
        OSGiUndeploymentRequest request = createOSGiUndeploymentRequest(
                deployer, env, report, osgiAppInfo);
        request.execute();
        return report;
    }

    protected ActionReport getReport()
    {
        return Globals.getDefaultHabitat().getComponent(ActionReport.class,
                "plain");
    }

    public boolean isDeployed(Bundle bundle)
    {
        return applications.containsKey(bundle);
    }

    protected abstract OSGiUndeploymentRequest createOSGiUndeploymentRequest(Deployment deployer, ServerEnvironmentImpl env, ActionReport reporter, OSGiApplicationInfo osgiAppInfo);

    protected abstract OSGiDeploymentRequest createOSGiDeploymentRequest(Deployment deployer, ArchiveFactory archiveFactory, ServerEnvironmentImpl env, ActionReport reporter, Bundle b);
}