/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.EngineRef;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.osgijavaeebase.OSGiDeploymentRequest;
import org.glassfish.osgijavaeebase.OSGiUndeploymentRequest;
import org.glassfish.osgijavaeebase.OSGiApplicationInfo;
import org.glassfish.osgijavaeebase.OSGiContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

import javax.servlet.ServletContext;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiWebContainer extends OSGiContainer
{
    // TODO(Sahoo): Integration with event admin service

    private static final Logger logger =
            Logger.getLogger(OSGiWebContainer.class.getPackage().getName());

    // Set the current bundle context in a thread local for use during web module decoration
    private ThreadLocal<BundleContext> currentBundleContext = new ThreadLocal<BundleContext>();

    public OSGiWebContainer(BundleContext ctx) {
        super(ctx);
    }

    protected void preDeploy(Bundle b) {
        currentBundleContext.set(b.getBundleContext());
    }

    protected void postDeploy(OSGiApplicationInfo osgiAppInfo) {
        assert(osgiAppInfo != null);
        try {
            ServletContext sc = getServletContext(osgiAppInfo.getAppInfo());
            assert(sc.getAttribute(Constants.BUNDLE_CONTEXT_ATTR) == osgiAppInfo.getBundle().getBundleContext());
            registerService(osgiAppInfo.getBundle(), sc);
            applications.put(osgiAppInfo.getBundle(), osgiAppInfo);
            logger.logp(Level.INFO, "OSGiWebContainer", "deploy",
                    "deployed bundle {0} at {1}",
                    new Object[]{osgiAppInfo.getBundle(), osgiAppInfo.getAppInfo().getSource().getURI()});
        } catch (Exception e) {
            logger.logp(Level.WARNING, "OSGiWebContainer", "deploy",
                    "Rolling back deployment as exception occured", e);
            undeployJavaEEArtifacts(osgiAppInfo, getReport());
        }
    }

    protected OSGiUndeploymentRequest createOSGiUndeploymentRequest(Deployment deployer, ServerEnvironmentImpl env, ActionReport reporter, OSGiApplicationInfo osgiAppInfo) {
        return new OSGiWebUndeploymentRequest(deployer, env, reporter, osgiAppInfo);
    }

    protected OSGiDeploymentRequest createOSGiDeploymentRequest(Deployment deployer, ArchiveFactory archiveFactory, ServerEnvironmentImpl env, ActionReport reporter, Bundle b) {
        return new OSGiWebDeploymentRequest(deployer, archiveFactory, env, reporter, b);
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

}
