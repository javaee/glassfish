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

package org.glassfish.osgi.ee.resources;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for resource-managers that export resources in GlassFish to OSGi's service-registry
 *
 * @author Jagadish Ramu
 */
public class BaseResourceManager {

    private Habitat habitat;

    protected List<ServiceRegistration> services = new ArrayList<ServiceRegistration>();
    protected static final Logger logger = Logger.getLogger(
            BaseResourceManager.class.getPackage().getName());
    protected ResourceHelper resourceHelper ;

    public BaseResourceManager(Habitat habitat){
        this.habitat = habitat;
        resourceHelper = new ResourceHelper(habitat);
    }

    protected void unRegisterResource(ServiceRegistration serviceRegistration, BundleContext context) {
        debug("unregistering resource [" + serviceRegistration.getReference().getProperty(Constants.JNDI_NAME) + "]");
        Invalidate proxy = (Invalidate) serviceRegistration.getReference().getBundle().
                getBundleContext().getService(serviceRegistration.getReference());
        serviceRegistration.unregister();
        proxy.invalidate();
    }

    public void unRegisterResource(BindableResource resource, ResourceRef resRef, BundleContext bundleContext) {
        String jndiName = resource.getJndiName();
        ServiceRegistration toRemove = null;
        for (ServiceRegistration serviceRegistration : services) {
            if (serviceRegistration.getReference().getProperty(Constants.JNDI_NAME).equals(jndiName)) {
                unRegisterResource(serviceRegistration, bundleContext);
                toRemove = serviceRegistration;
                break;
            }
        }
        if (toRemove != null) {
            services.remove(toRemove);
        }
    }

    public void unRegisterResources(BundleContext context) {
        Iterator it = services.iterator();
        while (it.hasNext()) {
            ServiceRegistration serviceRegistration = (ServiceRegistration) it.next();
            unRegisterResource(serviceRegistration, context);
            it.remove();
        }
    }

    protected Habitat getHabitat() {
        return habitat;
    }

    protected Resources getResources() {
        return habitat.getComponent(Domain.class).getResources();
    }

    protected ResourceHelper getResourceHelper() {
        return resourceHelper;
    }

    protected ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }

    protected void registerResourceAsService(BundleContext bundleContext, BindableResource bindableResource,
                                             String name, Dictionary properties, Object o) {
        ServiceRegistration service = bundleContext.registerService(name, o, properties);
        debug("registering resource [" + bindableResource.getJndiName() + "]");
        services.add(service);
    }

    /**
     * get proxy object for the resource types (interfaces) so as to delegate to actual objects<br>
     * @param jndiName jndi-name of resource
     * @param ifaces list of interfaces for which the proxy is needed
     * @param loader class-loader to define the proxy class
     * @return proxy object 
     */
    protected Object getProxy(String jndiName, Class[] ifaces, ClassLoader loader) {
        ResourceProxy proxy = new ResourceProxy(jndiName);
        return Proxy.newProxyInstance(loader, ifaces, proxy);
    }

    protected void debug(String s) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[osgi-ee-resources] : " + s);
        }
    }
}
