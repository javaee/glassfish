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


package org.glassfish.osgiejb;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.ServiceRegistration;
import static org.osgi.framework.Constants.ACTIVATION_LAZY;
import static org.osgi.framework.Constants.BUNDLE_ACTIVATIONPOLICY;
import org.glassfish.osgijavaeebase.Extender;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An extender that listens to web application bundle's lifecycle
 * events and does the necessary deployment/undeployment.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EJBExtender implements Extender, SynchronousBundleListener
{
    private OSGiEJBContainer c;
    private static final Logger logger =
            Logger.getLogger(EJBExtender.class.getPackage().getName());
    private BundleContext context;
    private AtomicBoolean started = new AtomicBoolean(false);
    private ServiceRegistration urlHandlerService;

    public EJBExtender(BundleContext context)
    {
        this.context = context;
    }

    public void start() {
        started.set(true);
        c = new OSGiEJBContainer(context);
        context.addBundleListener(this);

        // Web Container bundle can come into existence after
        // web application bundles, so we must go through existing bundles
        // to see if there are any web application bundles already started.
        for (Bundle b : context.getBundles())
        {
            if (((b.getState() & (Bundle.STARTING | Bundle.ACTIVE)) != 0) &&
                    isEJBBundle(b))
            {
                deploy(b);
            }
        }
    }

    public void stop() {
        if (started.getAndSet(false)) {
            context.removeBundleListener(this);
            if (c !=null) c.undeployAll();
        }
    }

    public void bundleChanged(BundleEvent event)
    {
        Bundle bundle = event.getBundle();
        switch (event.getType())
        {
            case BundleEvent.STARTING:
                if (!isLazy(bundle) && isEJBBundle(bundle))
                {
                    deploy(bundle);
                }
                break;
            case BundleEvent.LAZY_ACTIVATION:
                if (isEJBBundle(bundle))
                {
                    deploy(bundle);
                }
                break;
            case BundleEvent.STOPPED:
                // We undeploy in STOPPED event rather than STOPPING event
                // to make it symmetrical. Had we decided to undeploy in
                // STOPPING event, activator.stop() would have been called
                // after the app gets undeployed, where as activator.start()
                // would have been called after deployment. SO, activator
                // could not do meaningful cleanup using Java EE components in stop().
                //  Hence, we undeploy in STOPPED event.
                if (isEJBBundle(bundle) && c.isDeployed(bundle))
                {
                    undeploy(bundle);
                }
                break;
        }
    }

    private boolean isLazy(Bundle bundle)
    {
        return ACTIVATION_LAZY.equals(
                bundle.getHeaders().get(BUNDLE_ACTIVATIONPOLICY));
    }

    /**
     * Determines if a bundle represents a EJB application or not.
     * We determine this by looking at presence of Application-Type manifest header.
     *
     * @param b
     * @return
     */
    private boolean isEJBBundle(Bundle b)
    {
        return b.getHeaders().get(Constants.EJB_NAMESPACE) != null;
    }

    private void deploy(Bundle b)
    {
        try
        {
            c.deploy(b);
        }
        catch (Exception e)
        {
            logger.logp(Level.SEVERE, "EJBExtender", "deploy",
                    "Exception deploying bundle {0}",
                    new Object[]{b.getLocation()});
            logger.logp(Level.SEVERE, "EJBExtender", "deploy",
                    "Exception Stack Trace", e);
        }
    }

    private void undeploy(Bundle b)
    {
        try
        {
            c.undeploy(b);
        }
        catch (Exception e)
        {
            logger.logp(Level.SEVERE, "EJBExtender", "undeploy",
                    "Exception undeploying bundle {0}",
                    new Object[]{b.getLocation()});
            logger.logp(Level.SEVERE, "EJBExtender", "undeploy",
                    "Exception Stack Trace", e);
        }
    }

}