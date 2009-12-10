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


package org.glassfish.osgihttp;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of {@link HttpService} per bundle.
 * This is what a bundle gets when they look up the servuce in OSGi service
 * registry. This is needed so that we can unregister all the servlets
 * registered by a bundle when that bundle goes down without unregistering
 * the servlet or resource end points. This delegates to {@link GlassFishHttpService}
 * for implementing the actual service.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class HttpServiceWrapper implements HttpService {

    private GlassFishHttpService delegate;
    /**
     * The bundle which has looked up this service instance from registry.
     */
    private Bundle registeringBundle;

    /**
     * Aliases registered by the current bundle holding this service reference.
     */
    private Set<String> aliases = new HashSet<String>();

    public HttpServiceWrapper(GlassFishHttpService delegate,
                              Bundle registeringBundle) {
        this.delegate = delegate;
        this.registeringBundle = registeringBundle;
    }

    public HttpContext createDefaultHttpContext() {
        return new DefaultHttpContext(registeringBundle);
    }

    public void registerServlet(String alias, Servlet servlet, Dictionary initParams, HttpContext httpContext) throws ServletException, NamespaceException {
        if (httpContext == null) {
            httpContext = createDefaultHttpContext();
        }
        delegate.registerServlet(alias, servlet, initParams, httpContext);
        aliases.add(alias);
    }

    public void registerResources(String alias, String name, HttpContext httpContext) throws NamespaceException {
        if (httpContext == null) {
            httpContext = createDefaultHttpContext();
        }
        delegate.registerResources(alias, name, httpContext);
        aliases.add(alias);
    }

    public synchronized void unregister(String alias) {
        unregister(alias, true);
    }

    private void unregister(String alias, boolean callDestroy) {
        delegate.unregister(alias, callDestroy);
        aliases.remove(alias);
    }

    /**
     * Unregisters all the aliases without calling servlet.destroy (if any).
     */
    /* package */ void unregisterAll() {
        // take a copy of all registered aliases,
        // as the underlying list will change
        for (String alias : aliases.toArray(new String[0])) {
            unregister(alias, false); // don't call servlet.destry, hence false
        }
    }

    /**
     * This service factory is needed, because the spec requires the following:
     * If the bundle which performed the registration is stopped or
     * otherwise "unget"s the Http Service without calling
     * unregister(java.lang.String) then Http Service must automatically
     * unregister the registration. However, if the registration was for
     * a servlet, the destroy method of the servlet will not be called
     * in this case since the bundle may be stopped.
     * unregister(java.lang.String) must be explicitly called to cause the
     * destroy method of the servlet to be called.
     * This can be done in the BundleActivator.stop method of the
     * bundle registering the servlet.
     */
    public static class HttpServiceFactory implements ServiceFactory {
        private GlassFishHttpService delegate;

        public HttpServiceFactory(GlassFishHttpService delegate) {
            this.delegate = delegate;
        }

        public Object getService(Bundle bundle, ServiceRegistration registration) {
            return new HttpServiceWrapper(delegate, bundle);
        }

        public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
            HttpServiceWrapper.class.cast(service).unregisterAll();
        }
    }
}
