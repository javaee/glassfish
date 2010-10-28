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

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.api.ActionReport;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.osgijavaeebase.*;
import org.glassfish.server.ServerEnvironmentImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiWebDeployer extends AbstractOSGiDeployer {
    private static final Logger logger =
            Logger.getLogger(OSGiWebDeployer.class.getPackage().getName());

    public OSGiWebDeployer(BundleContext context) {
        super(context, Integer.MAX_VALUE);
    }

    public OSGiUndeploymentRequest createOSGiUndeploymentRequest(Deployment deployer, ServerEnvironmentImpl env, ActionReport reporter, OSGiApplicationInfo osgiAppInfo) {
        return new OSGiWebUndeploymentRequest(deployer, env, reporter, osgiAppInfo);
    }

    public OSGiDeploymentRequest createOSGiDeploymentRequest(Deployment deployer, ArchiveFactory archiveFactory, ServerEnvironmentImpl env, ActionReport reporter, Bundle b) {
        return new OSGiWebDeploymentRequest(deployer, archiveFactory, env, reporter, b);
    }

    public boolean handles(Bundle bundle) {
        return isWebBundle(bundle);
    }

    /**
     * Determines if a bundle represents a web application or not.
     * As per rfc #66, a web container extender recognizes a web application
     * bundle by looking for the presence of Web-contextPath manifest header
     *
     * @param b
     * @return
     */
    private boolean isWebBundle(Bundle b)
    {
        final Dictionary headers = b.getHeaders();
        return headers.get(Constants.WEB_CONTEXT_PATH) != null &&
                headers.get(org.osgi.framework.Constants.FRAGMENT_HOST) == null;
    }

    @Override
    protected void raiseEvent(State state, Bundle appBundle, Throwable e) {
        WABEventPublisher ep = new WABEventPublisher();
        ep.raiseEvent(state, appBundle, getBundleContext().getBundle(), e);
    }

}
