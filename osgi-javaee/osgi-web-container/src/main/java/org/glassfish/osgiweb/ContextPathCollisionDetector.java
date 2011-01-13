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


package org.glassfish.osgiweb;

import org.glassfish.osgijavaeebase.OSGiContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Detects collision in Web-ContextPath
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class ContextPathCollisionDetector {
    private static ContextPathCollisionDetector _me = new ContextPathCollisionDetector();

    private static Logger logger = Logger.getLogger(ContextPathCollisionDetector.class.getPackage().getName());

    Map<String, List<Long>> contextPath2BundlesMap = new HashMap<String, List<Long>>();

    private ServiceTracker osgiContainerTracker = new ServiceTracker(getBundle().getBundleContext(),
            OSGiContainer.class.getName(), null);
    private boolean stopped;

    private ContextPathCollisionDetector() {
        osgiContainerTracker.open();
    }

    public static ContextPathCollisionDetector get() {
        return _me;
    }

    synchronized void stop() {
        osgiContainerTracker.close();
        stopped = true;
    }

    public synchronized void preDeploy(Bundle bundle) throws ContextPathCollisionException {
        if (stopped) return;
        String contextPath = (String) bundle.getHeaders().get(Constants.WEB_CONTEXT_PATH);
        List<Long> bundleIds = contextPath2BundlesMap.get(contextPath);
        if (bundleIds == null) {
            bundleIds = new ArrayList<Long>();
            contextPath2BundlesMap.put(contextPath, bundleIds);
        }
        if(!bundleIds.contains(bundle.getBundleId())) {
            // we can be here because of attempting to deploy from postUndeploy
            bundleIds.add(bundle.getBundleId());
        }
        if (bundleIds.size() > 1) {
            throw new ContextPathCollisionException(contextPath, bundleIds.toArray(new Long[0]));
        }
    }

    public synchronized void postUndeploy(Bundle bundle) {
        if (stopped) return;
        String contextPath = (String) bundle.getHeaders().get(Constants.WEB_CONTEXT_PATH);
        List<Long> bundleIds = contextPath2BundlesMap.get(contextPath);
        assert (bundleIds != null && bundleIds.size() >= 1);
        if (bundleIds == null || bundleIds.isEmpty()) return;
        int idx = bundleIds.indexOf(bundle.getBundleId());
        assert (idx != -1);
        if (idx == -1) {
            return;
        }
        bundleIds.remove(idx);
        if (bundleIds.isEmpty()) {
            return;
        } else {
            logger.logp(Level.INFO, "CollisionDetector", "postUndeploy",
                    "Attempting to deploy bundle {0} with context path {1} ", new Object[]{bundleIds.get(idx), contextPath});
            // attempt to deploy bundle with lowest bundle id having same context path
            Collections.sort(bundleIds);
            try {
                getOSGiContainer().deploy(getBundle().getBundleContext().getBundle(bundleIds.get(0)));
            } catch (Exception e) {
                logger.logp(Level.WARNING, "CollisionDetector", "postUndeploy", "e = {0}", new Object[]{e});
            }
        }
    }

    private OSGiContainer getOSGiContainer() {
        return (OSGiContainer) osgiContainerTracker.getService();
    }

    private Bundle getBundle() {
        return BundleReference.class.cast(getClass().getClassLoader()).getBundle();
    }

    public synchronized void cleanUp(Bundle bundle) {
        String contextPath = (String) bundle.getHeaders().get(Constants.WEB_CONTEXT_PATH);
        List<Long> bundleIds = contextPath2BundlesMap.get(contextPath);
        assert (bundleIds != null && bundleIds.size() >= 1);
        if (bundleIds == null || bundleIds.isEmpty()) return;
        int idx = bundleIds.indexOf(bundle.getBundleId());
        assert (idx != -1);
        if (idx == -1) {
            return;
        }
        Long bundleId= bundleIds.remove(idx);
        logger.logp(Level.INFO, "CollisionDetector", "cleanUp",
                "Removed bundle {0} against context path {1} ", new Object[]{bundleId, contextPath});

    }
}
