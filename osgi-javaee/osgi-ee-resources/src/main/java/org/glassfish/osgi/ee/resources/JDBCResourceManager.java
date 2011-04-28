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

import com.sun.enterprise.config.serverbeans.*;
import org.jvnet.hk2.component.Habitat;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.framework.BundleContext;

import java.util.*;

/**
 * Resource-Manager to export JDBC resources in GlassFish to OSGi's service-registry
 *
 * @author Jagadish Ramu
 */
public class JDBCResourceManager extends BaseResourceManager implements ResourceManager {

    public JDBCResourceManager(Habitat habitat){
        super(habitat);
    }

    public void registerResources(BundleContext context) {
        registerJdbcResources(context);
    }

    /**
     * Iterates through all of the configured jdbc-resources <br>
     * Exposes them as OSGi service by contract "javax.sql.DataSource"
     */
    private void registerJdbcResources(BundleContext context) {
        Resources resources = getHabitat().getComponent(Domain.class).getResources();
        Collection<JdbcResource> jdbcResources = resources.getResources(JdbcResource.class);
        for (JdbcResource resource : jdbcResources) {
            ResourceRef resRef = getResourceHelper().getResourceRef(resource.getJndiName());
            registerJdbcResource(resource, resRef, context);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void registerResource(BindableResource resource, ResourceRef resRef, BundleContext bundleContext) {
        registerJdbcResource((JdbcResource) resource, resRef, bundleContext);
    }

    /**
     * Retrieves driver-class-name information so as to register
     * the service with parameter <i>osgi.jdbc.driver.class</i><br>
     *
     * @param resource jdbc-resource
     * @param resRef   resource-ref
     */
    private void registerJdbcResource(JdbcResource resource, ResourceRef resRef, BundleContext bundleContext) {
        if (resource.getEnabled().equalsIgnoreCase("true")) {
            if (resRef != null && resRef.getEnabled().equalsIgnoreCase("true")) {
                String poolName = resource.getPoolName();
                JdbcConnectionPool pool = (JdbcConnectionPool)
                        getResources().getResourceByName(JdbcConnectionPool.class, poolName);
                String className = pool.getDatasourceClassname();
                // no need to use res-type to get driver/datasource-classname
                // as either datasource-classname or driver-classname must be not null.
                if(className == null){
                    className = pool.getDriverClassname();
                }
                Class[] intf = new Class[]{javax.sql.DataSource.class, Invalidate.class};
                Object proxy = getProxy(resource.getJndiName(), intf, getClassLoader());
                Dictionary properties = new Hashtable();
                properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, className);
                properties.put(Constants.JNDI_NAME, resource.getJndiName());

                registerResourceAsService(bundleContext, resource, Constants.DS,
                        properties, proxy);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean handlesResource(BindableResource resource) {
        return resource instanceof JdbcResource;
    }
}
