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


package org.glassfish.web.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An extender that listens to web application bundle's lifecycle
 * events and does the necessary deployment/undeployment.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WebExtender implements BundleActivator, SynchronousBundleListener
{
    private static final String WEB_DD = "WEB-INF/web.xml";
    private static final String WAR_EXT = ".war";
    private OSGiWebContainer wc;
    private static final String WEB_CONTEXT_PATH = "Web-ContextPath";
    private static final Logger logger =
            Logger.getLogger(WebExtender.class.getPackage().getName());
    private BundleContext context;

    public void start(BundleContext context) throws Exception
    {
        this.context = context;
        wc = new OSGiWebContainer();
        context.addBundleListener(this);

        // Web Container bundle can come into existence after
        // web application bundles, so we must go through existing bundles
        // to see if there are any web application bundles already started.
        for (Bundle b : context.getBundles())
        {
            if (((b.getState() & (Bundle.STARTING | Bundle.ACTIVE)) != 0) && isWebBundle(b))
            {
                deploy(b);
            }
        }
        addURLHandler();
    }

    public void stop(BundleContext context) throws Exception
    {
        context.removeBundleListener(this);
        wc.undeployAll();
    }

    public void bundleChanged(BundleEvent event)
    {
        Bundle bundle = event.getBundle();
        switch (event.getType())
        {
            case BundleEvent.STARTING:
                if (isWebBundle(bundle))
                {
                    deploy(bundle);
                }
                break;
            case BundleEvent.STOPPING:
                if (isWebBundle(bundle))
                {
                    undeploy(bundle);
                }
                break;
        }
    }

    /**
     * Determines if a bundle represents a web application or not.
     * As per rfc #66, a web container extender recognizes a web application
     * bundle by looking for the presence of one or more of the following:
     * Web-contextPath manifest header
     * Web.xml deployment descriptor
     * Bundle?s location has a .war extension
     * In addition to the above, we put a restriction that we don't allow
     * web applications to be from modules dir.
     *
     * @param b
     * @return
     */
    private boolean isWebBundle(Bundle b)
    {
        try
        {
            URI location = new URI(b.getLocation());
            if (location.isAbsolute())
            {
                // all our GF modules are installed with an Absolute URL.
                String installRoot = System.getProperty("com.sun.aas.installRoot");
                if (installRoot != null)
                {
                    location = new File(installRoot, "modules/").toURI().relativize(location);
                }
                if (!location.isAbsolute())
                {
                    return false;
                }
            }
        }
        catch (URISyntaxException e)
        {
            // All our modules installed from modules dir have a proper URI
            // as their location, so getting here means it is not a GF module.
        }
        return b.getHeaders().get(WEB_CONTEXT_PATH) != null ||
                b.getEntry(WEB_DD) != null ||
                b.getLocation().endsWith(WAR_EXT);
    }

    private void deploy(Bundle b)
    {
        try
        {
            wc.deploy(b);
        }
        catch (Exception e)
        {
            logger.logp(Level.SEVERE, "WebExtender", "deploy", "Exception deploying bundle {0}", new Object[]{b.getLocation()});
            logger.logp(Level.SEVERE, "WebExtender", "deploy", "Exception Stack Trace", e);
        }
    }

    private void undeploy(Bundle b)
    {
        try
        {
            wc.undeploy(b);
        }
        catch (Exception e)
        {
            logger.logp(Level.SEVERE, "WebExtender", "undeploy", "Exception undeploying bundle {0}", new Object[]{b.getLocation()});
            logger.logp(Level.SEVERE, "WebExtender", "undeploy", "Exception Stack Trace", e);
        }
    }

    private void addURLHandler()
    {
        Properties p = new Properties();
        p.put(URLConstants.URL_HANDLER_PROTOCOL,
                new String[]{WebBundleURLStreamHandlerService.WEB_BUNDLE_SCHEME});
        context.registerService(
                URLStreamHandlerService.class.getName(),
                new WebBundleURLStreamHandlerService(),
                p);
    }

}
