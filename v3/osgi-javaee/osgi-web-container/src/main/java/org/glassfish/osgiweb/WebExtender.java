/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-10-2010 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.web.WebModuleDecorator;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.glassfish.internal.api.Globals;
import org.glassfish.osgijavaeebase.Extender;
import org.jvnet.hk2.component.Inhabitant;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * An extender that listens to web application bundle's lifecycle
 * events and does the necessary deployment/undeployment.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WebExtender implements Extender {
    private static final Logger logger =
            Logger.getLogger(WebExtender.class.getPackage().getName());
    private BundleContext context;
    private ServiceRegistration urlHandlerService;
    private OSGiWebModuleDecorator wmd;
    private OSGiWebDeployer deployer;

    public WebExtender(BundleContext context) {
        this.context = context;
    }

    public synchronized void start() {
        registerWmd();
        registerDeployer();
        addURLHandler();
    }

    public synchronized void stop() {
        // Stop CollisionDetector first so that when we undeploy as part of shutting down, it won't try to deploy bundles
        ContextPathCollisionDetector.get().stop();
        removeURLHandler();
        unregisterDeployer();
        unregisterWmd();
    }

    private void registerDeployer() {
        deployer = new OSGiWebDeployer(context);
        deployer.register();
    }

    private void unregisterDeployer() {
        if (deployer != null) {
            deployer.unregister();
            deployer = null;
        }
    }

    private void addURLHandler() {
        Properties p = new Properties();
        p.put(URLConstants.URL_HANDLER_PROTOCOL,
                new String[]{Constants.WEB_BUNDLE_SCHEME});
        urlHandlerService = context.registerService(
                URLStreamHandlerService.class.getName(),
                new WebBundleURLStreamHandlerService(),
                p);
    }

    private void removeURLHandler() {
        if (urlHandlerService != null) {
            urlHandlerService.unregister();
        }
    }

    private void registerWmd() {
        wmd = new OSGiWebModuleDecorator();
        Inhabitant i = new ExistingSingletonInhabitant(wmd);
        String fqcn = WebModuleDecorator.class.getName();
        Globals.getDefaultHabitat().addIndex(i, fqcn, wmd.getClass().getSimpleName());
    }

    private void unregisterWmd() {
        if (wmd == null) return;
        // Since there is no public API to remove an inhabitant, we
        // nullify the fields and that ensures that this decorator
        // is as good as removed.
        wmd.deActivate();
    }

}

