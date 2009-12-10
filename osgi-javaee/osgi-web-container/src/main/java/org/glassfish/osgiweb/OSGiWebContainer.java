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

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.web.WebApplication;
import com.sun.enterprise.web.WebContainer;
import com.sun.enterprise.web.WebModule;
import org.glassfish.api.ActionReport;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.EngineRef;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

import javax.servlet.ServletContext;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiWebContainer
{
    // TODO(Sahoo): Integration wh event admin service

    static class OSGiApplicationInfo
    {
        ApplicationInfo appInfo;
        boolean isDirectoryDeployment;
        Bundle bundle;
    }
    private Map<Bundle, OSGiApplicationInfo> applications =
            new HashMap<Bundle, OSGiApplicationInfo>();

    private static final Logger logger =
            Logger.getLogger(OSGiWebContainer.class.getPackage().getName());

    private Deployment deployer = Globals.get(Deployment.class);
    private ArchiveFactory archiveFactory = Globals.get(ArchiveFactory.class);
    private ServerEnvironmentImpl env = Globals.get(ServerEnvironmentImpl.class);

    // Set the current bundle context in a thread local for use during web module decoration
    private ThreadLocal<BundleContext> currentBundleContext = new ThreadLocal<BundleContext>();


    /**
     * Deploys a web application bundle in GlassFish Web container.
     * This method is synchronized because we don't know if GlassFish
     * deployment framework can handle concurrent requests or not.
     *
     * @param b Web Application Bundle to be deployed.
     */
    public synchronized void deploy(final Bundle b) throws Exception
    {
        currentBundleContext.set(b.getBundleContext());
        OSGiApplicationInfo osgiAppInfo = applications.get(b);
        if (osgiAppInfo != null) {
            logger.logp(Level.WARNING, "OSGiWebContainer", "deploy",
                    "Bundle {0} is already deployed at {1} ", new Object[]{b,
                    osgiAppInfo.appInfo.getSource()});
            return;
        }
        // deploy the java ee artifacts
        ActionReport report = getReport();
        osgiAppInfo = deployJavaEEArtifacts(b, report);
        if (osgiAppInfo != null)
        {
            try {
                ServletContext sc = getServletContext(osgiAppInfo.appInfo);
                assert(sc.getAttribute(Constants.BUNDLE_CONTEXT_ATTR) == osgiAppInfo.bundle.getBundleContext());
                registerService(b, sc);
                applications.put(b, osgiAppInfo);
                logger.logp(Level.INFO, "OSGiWebContainer", "deploy",
                        "deployed bundle {0} at {1}",
                        new Object[]{b, osgiAppInfo.appInfo.getSource().getURI()});
            } catch (Exception e) {
                logger.logp(Level.WARNING, "OSGiWebContainer", "deploy",
                        "Rolling back deployment as exception occured", e);
                undeployJavaEEArtifacts(osgiAppInfo, report);
            }
        }
        else
        {
            logger.logp(Level.WARNING, "OSGiWebContainer", "deploy",
                    "could not deploy bundle {0}. See previous messages for " +
                            "further information",
                    new Object[]{b});
        }
    }

    /**
     * Does necessary deployment in Java EE container
     * @param b
     * @param report
     * @return
     */
    private OSGiApplicationInfo deployJavaEEArtifacts(Bundle b, ActionReport report)
    {
        JavaEEDeploymentRequest request = new JavaEEDeploymentRequest(
                deployer, archiveFactory, env, report, b);
        return request.execute();
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
        ActionReport report = getReport();
        undeployJavaEEArtifacts(osgiAppInfo, report);
        URI location = osgiAppInfo.appInfo.getSource().getURI();
        switch (report.getActionExitCode())
        {
            case FAILURE:
                logger.logp(Level.WARNING, "OSGiWebContainer", "undeploy",
                        "Failed to undeploy {0} from {1}. See previous messages for " +
                                "further information.", new Object[]{b, location});
                break;
            default:
                logger.logp(Level.INFO, "OSGiWebContainer", "undeploy",
                        "Undeployed bundle {0} from {1}", new Object[]{b, location});
                break;
        }
        applications.remove(b);
    }

    /**
     * Does necessary undeployment in Java EE container
     * @param osgiAppInfo
     * @param report
     * @return
     */
    private ActionReport undeployJavaEEArtifacts(OSGiApplicationInfo osgiAppInfo,
                                                 ActionReport report)
    {
        JavaEEUndeploymentRequest request = new JavaEEUndeploymentRequest(
                deployer, env, report, osgiAppInfo);
        request.execute();
        return report;
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
                logger.logp(Level.SEVERE, "OSGiWebContainer", "undeployAll",
                        "Exception undeploying bundle {0}",
                        new Object[]{b.getLocation()});
                logger.logp(Level.SEVERE, "OSGiWebContainer", "undeployAll",
                        "Exception Stack Trace", e);
            }
        }
    }

    private ActionReport getReport()
    {
        return Globals.getDefaultHabitat().getComponent(ActionReport.class,
                "plain");
    }


    private ServletContext setServletContextAttr(OSGiApplicationInfo osgiAppInfo)
    {
        ServletContext sc = getServletContext(osgiAppInfo.appInfo);
        assert (sc != null);
        sc.setAttribute(Constants.BUNDLE_CONTEXT_ATTR,
                osgiAppInfo.bundle.getBundleContext());
        return sc;
    }

    private ServletContext getServletContext(ApplicationInfo appInfo)
    {
        if (appInfo.getModuleInfos().size() == 1)
        {
            ModuleInfo m = appInfo.getModuleInfos().iterator().next();
            EngineRef e = m.getEngineRefForContainer(WebContainer.class);
            assert (e != null);
            WebApplication a = (WebApplication) e.getApplicationContainer();
            Set<WebModule> wms = a.getWebModules();
            assert (wms.size() == 1); // we only deploy to default virtual server
            if (wms.size() == 1)
            {
                return wms.iterator().next().getServletContext();
            }
        }
        return null;
    }

    private void registerService(Bundle b, ServletContext sc)
    {
        Properties props = new Properties();
        props.setProperty(Constants.OSGI_WEB_SYMBOLIC_NAME, b.getSymbolicName());
        String cpath = (String) b.getHeaders().get(Constants.WEB_CONTEXT_PATH);
        props.setProperty(Constants.OSGI_WEB_CONTEXTPATH, cpath);
        String version = (String) b.getHeaders().get(BUNDLE_VERSION);
        if (version != null)
        {
            props.setProperty(Constants.OSGI_WEB_VERSION, version);
        }
        BundleContext bctx = b.getBundleContext();
        if (bctx != null) {
            // This null check is required until we upgrade to Felix 1.8.1.
            // Felix 1.8.0 returns null when bundle is in starting state.
            bctx.registerService(
                    ServletContext.class.getName(),
                    sc, props);
            logger.logp(Level.INFO, "OSGiWebContainer", "registerService",
                    "Registered ServletContext as a service with properties: {0} ",
                    new Object[]{props});
        } else {
            logger.logp(Level.WARNING, "OSGiWebContainer", "registerService",
                    "Not able to register ServletContext as a service as bctx is null");
        }
    }

    /* package */ BundleContext getCurrentBundleContext() {
        return currentBundleContext.get();
    }

    public boolean isDeployed(Bundle bundle)
    {
        return applications.containsKey(bundle);
    }

}
