/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgiejb;

import org.osgi.framework.*;

import static org.osgi.framework.Constants.ACTIVATION_LAZY;
import static org.osgi.framework.Constants.BUNDLE_ACTIVATIONPOLICY;
import org.glassfish.osgijavaeebase.Extender;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Dictionary;

/**
 * An extender that listens to OSGi application bundle's lifecycle
 * events and does the necessary deployment/undeployment.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EJBExtender implements Extender, BundleListener
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

        // EJB Container bundle can come into existence after
        // ejb application bundles, so we must go through existing bundles
        // to see if there are any ejb application bundles is already in "ready" state.
        for (Bundle b : context.getBundles())
        {
            if (isEJBBundle(b) && isReady(b))
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
            case BundleEvent.STARTED:
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

    private boolean isReady(Bundle b) {
        final int state = b.getState();
        final boolean isActive = (state & Bundle.ACTIVE) != 0;
        final boolean isStarting = (state & Bundle.STARTING) != 0;
        final boolean isReady = isActive || (isLazy(b) && isStarting);
        return isReady;
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
        final Dictionary headers = b.getHeaders();
        return headers.get(Constants.EXPORT_EJB) != null &&
                headers.get(org.osgi.framework.Constants.FRAGMENT_HOST) == null;
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
