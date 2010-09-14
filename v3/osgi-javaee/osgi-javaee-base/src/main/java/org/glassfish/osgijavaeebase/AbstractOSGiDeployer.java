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

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.api.ActionReport;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class AbstractOSGiDeployer implements OSGiDeployer {

    /**
     * Various request processing states
     */
    public static enum State {
        DEPLOYING,
        DEPLOYED,
        FAILED,
        UNDEPLOYING,
        UNDEPLOYED
    }

    private static final Logger logger =
            Logger.getLogger(AbstractOSGiDeployer.class.getPackage().getName());

    private BundleContext bundleContext;
    private ServiceRegistration serviceReg;
    private int rank;

    private Deployment deployer = Globals.get(Deployment.class);
    private ArchiveFactory archiveFactory = Globals.get(ArchiveFactory.class);
    private ServerEnvironmentImpl env = Globals.get(ServerEnvironmentImpl.class);

    protected AbstractOSGiDeployer(BundleContext bundleContext, int rank) {
        this.bundleContext = bundleContext;
        this.rank = rank;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * Registers this as an OSGi service
     */
    public void register() {
        Properties properties = new Properties();
        properties.put(org.osgi.framework.Constants.SERVICE_RANKING, rank);
        serviceReg = bundleContext.registerService(OSGiDeployer.class.getName(), this, properties);
    }

    /**
     * Unregisters itself from OSGi service registry
     * Before it unregisters itself, it first undeploys all applications that were deployed using itself.
     */
    public void unregister() {
        /*
         * Why do we undeployAll while unregistering ourselves, but not deployAll during registering ourselves?
         * That's because, if we first unregister and rely on serviceRemoved() method to notify the OSGiContainer
         * to undeploy apps, OSGiContainer can't undeploy, because we are no longer available.  
         */
        undeployAll();
        serviceReg.unregister();
    }

    public OSGiApplicationInfo deploy(Bundle b) throws DeploymentException {
        raiseEvent(State.DEPLOYING, b, null);
        ActionReport report = getReport();
        OSGiDeploymentRequest request = createOSGiDeploymentRequest(deployer, archiveFactory, env, report, b);
        OSGiApplicationInfo osgiAppInfo = request.execute();
        if (osgiAppInfo == null) {
            final Throwable throwable = report.getFailureCause();
            raiseEvent(State.FAILED, b, throwable);
            throw new DeploymentException("Deployment of " + b + " failed because of following reason: " + report.getMessage(),
                    throwable);
        }
        raiseEvent(State.DEPLOYED, b, null);
        return osgiAppInfo;
    }

    public void undeploy(OSGiApplicationInfo osgiAppInfo) throws DeploymentException {
        final Bundle b = osgiAppInfo.getBundle();
        raiseEvent(State.UNDEPLOYING, b, null);
        ActionReport report = getReport();
        OSGiUndeploymentRequest request = createOSGiUndeploymentRequest(deployer, env, report, osgiAppInfo);
        request.execute();
        raiseEvent(State.UNDEPLOYED, b, null); // raise event even if something went wrong
        if (report.getActionExitCode() == ActionReport.ExitCode.FAILURE) {
            throw new DeploymentException("Undeployment of " + b + " failed because of following reason: " + report.getMessage(),
                    report.getFailureCause());
        }
    }

    protected ActionReport getReport() {
        return Globals.getDefaultHabitat().getComponent(ActionReport.class,
                "plain");
    }

    /**
     * Undeploys all bundles which have been deployed using this deployer
     */
    public void undeployAll() {
        ServiceTracker st = new ServiceTracker(bundleContext, OSGiContainer.class.getName(), null);
        st.open();
        try {
            OSGiContainer c = (OSGiContainer) st.getService();
            if (c == null) return;
            ServiceReference deployerRef = serviceReg.getReference();
            for(OSGiApplicationInfo app : c.getDeployedApps()) {
                if (app.getDeployer() == deployerRef) {
                    try {
                        c.undeploy(app.getBundle());
                    } catch (Exception e) {
                        logger.logp(Level.WARNING, "WebExtender", "undeployAll", "Failed to undeploy bundle " + app.getBundle(), e);
                    }
                }
            }
        } finally {
            st.close();
        }
    }

    protected abstract OSGiDeploymentRequest createOSGiDeploymentRequest(Deployment deployer,
                                                      ArchiveFactory archiveFactory,
                                                      ServerEnvironmentImpl env,
                                                      ActionReport reporter,
                                                      Bundle b);

    protected abstract OSGiUndeploymentRequest createOSGiUndeploymentRequest(Deployment deployer,
                                                          ServerEnvironmentImpl env,
                                                          ActionReport reporter,
                                                          OSGiApplicationInfo osgiAppInfo);

    /**
     * Integration with Event Admin Service happens here.
     */
    protected void raiseEvent(State state, Bundle appBundle, Throwable throwable) {
    }

}
