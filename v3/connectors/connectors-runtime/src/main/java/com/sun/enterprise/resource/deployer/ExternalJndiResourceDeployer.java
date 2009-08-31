/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * @(#) ExternalJndiResourceDeployer.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.resource.deployer;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.JavaEEResource;
import com.sun.enterprise.resource.beans.ExternalJndiResource;
import com.sun.appserv.connectors.internal.api.ResourcePropertyImpl;
import com.sun.enterprise.resource.naming.JndiProxyObjectFactory;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.repository.ResourceProperty;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.api.naming.GlassfishNamingManager;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.InitialContextFactory;

/**
 * Handles external-jndi resource events in the server instance.
 *
 * The external-jndi resource events from the admin instance are propagated
 * to this object.
 *
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
@Service
@Scoped(Singleton.class)
public class ExternalJndiResourceDeployer implements ResourceDeployer {

    @Inject
    private GlassfishNamingManager namingMgr;

        //TODO V3 log strings for the entire class
    
    /** StringManager for this deployer */
    private static final StringManager localStrings =
        StringManager.getManager(ExternalJndiResourceDeployer.class);
    /** logger for this deployer */
    private static Logger _logger=LogDomains.getLogger(ExternalJndiResourceDeployer.class, LogDomains.CORE_LOGGER);

    /**
     * {@inheritDoc}
     */
	public synchronized void deployResource(Object resource) throws Exception {

        com.sun.enterprise.config.serverbeans.ExternalJndiResource jndiRes =
            (com.sun.enterprise.config.serverbeans.ExternalJndiResource) resource;

        if (ConnectorsUtil.parseBoolean(jndiRes.getEnabled())) {
            // converts the config data to j2ee resource
            JavaEEResource j2eeRes = toExternalJndiJavaEEResource(jndiRes);

            // installs the resource
            installExternalJndiResource(
                (com.sun.enterprise.resource.beans.ExternalJndiResource) j2eeRes);

        } else {
            _logger.log(Level.INFO, "core.resource_disabled",
                new Object[] {jndiRes.getJndiName(),
                              ConnectorConstants.EXT_JNDI_RES_TYPE});
        }

    }

    /**
     * {@inheritDoc}
     */
	public synchronized void undeployResource(Object resource)
            throws Exception {

        com.sun.enterprise.config.serverbeans.ExternalJndiResource jndiRes =
            (com.sun.enterprise.config.serverbeans.ExternalJndiResource) resource;

        // converts the config data to j2ee resource
        JavaEEResource j2eeResource = toExternalJndiJavaEEResource(jndiRes);

        // un-installs the resource
        uninstallExternalJndiResource(j2eeResource);
    }

    /**
     * {@inheritDoc}
     */
	public synchronized void redeployResource(Object resource)
            throws Exception {

        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(Object resource){
        return resource instanceof com.sun.enterprise.config.serverbeans.ExternalJndiResource;
    }


    /**
     * {@inheritDoc}
     */
	public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
	public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }

    /**
     * Installs the given external jndi resource. This method gets called
     * during server initialization and from external jndi resource
     * deployer to handle resource events.
     *
     * @param extJndiRes external jndi resource
     */
    public void installExternalJndiResource(com.sun.enterprise.resource.beans.ExternalJndiResource extJndiRes) {

        String bindName = null;

        try {
            bindName = extJndiRes.getName();

            // create the external JNDI factory, its initial context and
            // pass them as references.
            String factoryClass = extJndiRes.getFactoryClass();
            String jndiLookupName = extJndiRes.getJndiLookupName();

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "installExternalJndiResources jndiName "
                        + bindName + " factoryClass " + factoryClass
                        + " jndiLookupName = " + jndiLookupName);
            }


            Object factory = ConnectorsUtil.loadObject(factoryClass);
            if (factory == null) {
                _logger.log(Level.WARNING, "jndi.factory_load_error", factoryClass);
                return;

            } else if (!(factory instanceof javax.naming.spi.InitialContextFactory)) {
                _logger.log(Level.WARNING, "jndi.factory_class_unexpected", factoryClass);
                return;
            }

            // Get properties to create the initial naming context
            // for the target JNDI factory
            Hashtable env = new Hashtable();
            for (Iterator props = extJndiRes.getProperties().iterator();
                 props.hasNext();) {

                ResourceProperty prop = (ResourceProperty) props.next();
                env.put(prop.getName(), prop.getValue());
            }

            Context context = null;
            try {
                context =
                        ((InitialContextFactory) factory).getInitialContext(env);

            } catch (NamingException ne) {
                _logger.log(Level.SEVERE, "jndi.initial_context_error", factoryClass);
                _logger.log(Level.SEVERE, "jndi.initial_context_error_excp", ne.getMessage());
            }

            if (context == null) {
                _logger.log(Level.SEVERE, "jndi.factory_create_error", factoryClass);
                return;
            }

            // Bind a Reference to the proxy object factory; set the
            // initial context factory.
            //JndiProxyObjectFactory.setInitialContext(bindName, context);

            Reference ref = new Reference(extJndiRes.getResType(),
                    "com.sun.enterprise.resource.naming.JndiProxyObjectFactory",
                    null);

            // unique JNDI name within server runtime
            ref.add(new StringRefAddr("jndiName", bindName));

            // target JNDI name
            ref.add(new StringRefAddr("jndiLookupName", jndiLookupName));

            // target JNDI factory class
            ref.add(new StringRefAddr("jndiFactoryClass", factoryClass));

            // add Context info as a reference address
            ref.add(new com.sun.enterprise.resource.naming.ProxyRefAddr(bindName, env));

            // Publish the reference
            namingMgr.publishObject(bindName, ref, true);

        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "customrsrc.create_ref_error", bindName);
            _logger.log(Level.SEVERE, "customrsrc.create_ref_error_excp", ex);

        }
    }

    /**
     * Un-installs the external jndi resource.
     *
     * @param resource external jndi resource
     */
    public void uninstallExternalJndiResource(JavaEEResource resource) {

        // removes the jndi context from the factory cache
        JndiProxyObjectFactory.removeInitialContext(resource.getName());

        // removes the resource from jndi naming
        try {
            namingMgr.unpublishObject(resource.getName());
            /* TODO V3 handle jms later
            //START OF IASRI 4660565
            if (((ExternalJndiResource)resource).isJMSConnectionFactory()) {
                nm.unpublishObject(IASJmsUtil.getXAConnectionFactoryName(resource.getName()));
            }
            //END OF IASRI 4660565
            */
        } catch (javax.naming.NamingException e) {
            _logger.log(Level.FINE,
                    "Error while unpublishing resource: " + resource.getName(), e);
        }
    }


    /**
     * Returns a new instance of j2ee external jndi resource from the given
     * config bean.
     *
     * This method gets called from the external resource
     * deployer to convert external-jndi-resource config bean into
     * external-jndi  j2ee resource.
     *
     * @param    rbean    external-jndi-resource config bean
     *
     * @return   a new instance of j2ee external jndi resource
     *
     */
    public static JavaEEResource toExternalJndiJavaEEResource(
            com.sun.enterprise.config.serverbeans.ExternalJndiResource rbean) {

        ExternalJndiResource jr = new com.sun.enterprise.resource.beans.ExternalJndiResource(rbean.getJndiName());

        //jr.setDescription( rbean.getDescription() ); // FIXME: getting error

        // sets the enable flag
        jr.setEnabled( ConnectorsUtil.parseBoolean(rbean.getEnabled()) );

        // sets the jndi look up name
        jr.setJndiLookupName( rbean.getJndiLookupName() );

        // sets the resource type
        jr.setResType( rbean.getResType() );

        // sets the factory class name
        jr.setFactoryClass( rbean.getFactoryClass() );

        // sets the properties
        List<Property> properties = rbean.getProperty();
        if (properties!= null) {
            for(Property property : properties){
                ResourceProperty rp =
                    new ResourcePropertyImpl(property.getName(), property.getValue());
                jr.addProperty(rp);
            }
        }
        return jr;
    }
}
