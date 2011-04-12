/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
import org.osgi.framework.BundleContext;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import java.util.*;

/**
 * Resource-Manager to export jms-connection-factories (JMS-RA Connector-Resources)
 * in GlassFish to OSGi's service-registry
 *
 * @author Jagadish Ramu
 */
public class JMSResourceManager extends BaseResourceManager implements ResourceManager {

    public JMSResourceManager(Habitat habitat){
        super(habitat);
    }

    /**
     * {@inheritDoc}
     */
    public void registerResources(BundleContext context) {
        registerJmsResources(context);
    }

    /**
     * Iterates through all of the configured connector-resources of jms-ra<br>
     * Exposes them as OSGi service by appropriate contract which can be one of the following :<br>
     * <i>javax.jms.ConnectionFactory</i><br>
     * <i>javax.jms.QueueConnectionFactory</i><br>
     * <i>javax.jms.TopicConnectionFactory</i><br><br>
     * @param context bundle-context
     */
    public void registerJmsResources(BundleContext context) {
        Collection<ConnectorResource> connectorResources = getResources().getResources(ConnectorResource.class);
        for (ConnectorResource resource : connectorResources) {
            if (isJmsResource(resource)) {
                ResourceRef resRef = getResourceHelper().getResourceRef(resource.getJndiName());
                registerResource(resource, resRef, context);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void registerResource(BindableResource resource, ResourceRef resRef, BundleContext bundleContext) {
        ConnectorResource connectorResource = (ConnectorResource) resource;
        if (connectorResource.getEnabled().equalsIgnoreCase("true")) {
            if (resRef != null && resRef.getEnabled().equalsIgnoreCase("true")) {
                String poolName = connectorResource.getPoolName();
                ConnectorConnectionPool pool = (ConnectorConnectionPool)
                        getResources().getResourceByName(ConnectorConnectionPool.class, poolName);
                String defnName = pool.getConnectionDefinitionName();
                Class claz = null;
                Class intf[] = null;

                if (defnName.equals(Constants.QUEUE_CF)) {
                    claz = QueueConnectionFactory.class;
                    intf = new Class[]{QueueConnectionFactory.class, Invalidate.class};
                } else if (defnName.equals(Constants.TOPIC_CF)) {
                    claz = TopicConnectionFactory.class;
                    intf = new Class[]{TopicConnectionFactory.class, Invalidate.class};
                } else if (defnName.equals(Constants.UNIFIED_CF)) {
                    claz = ConnectionFactory.class;
                    intf = new Class[]{ConnectionFactory.class, Invalidate.class};
                } else {
                    throw new RuntimeException
                            ("Invalid connection-definition [ " + defnName + " ]" +
                                    " for jms-resource [ " + resource.getJndiName() + " ]");
                }
                Dictionary properties = new Hashtable();
                properties.put(Constants.JNDI_NAME, connectorResource.getJndiName());
                Object o = getProxy(connectorResource.getJndiName(), intf, getClassLoader());

                registerResourceAsService(bundleContext, connectorResource, claz.getName(), properties, o);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean handlesResource(BindableResource resource) {
        boolean result = false;
        if (resource instanceof ConnectorResource) {
            result = isJmsResource((ConnectorResource) resource);
        }
        return result;
    }

    /**
     * determines whether the resource is a JMS-RA's resource
     * @param resource connector-resource
     * @return boolean
     */
    private boolean isJmsResource(ConnectorResource resource) {
        boolean result = false;
        String poolName = resource.getPoolName();
        ConnectorConnectionPool pool = (ConnectorConnectionPool)
                getResources().getResourceByName(ConnectorConnectionPool.class, poolName);
        String raName = pool.getResourceAdapterName();
        if (raName.equals(Constants.DEFAULT_JMS_ADAPTER)) {
            result = true;
        }
        return result;
    }
}
