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
 * @(#) MailResourceDeployer.java
 *
 * Copyright 2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.resource.deployer;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.resource.beans.MailResource;
import com.sun.enterprise.resource.naming.SerializableObjectRefAddr;
import com.sun.appserv.connectors.internal.api.JavaEEResource;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.deployment.MailConfiguration;
import com.sun.enterprise.repository.ResourceProperty;
import com.sun.enterprise.container.common.impl.MailNamingObjectFactory;
import com.sun.appserv.connectors.internal.api.ResourcePropertyImpl;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;

/**
 * Handles mail resource events in the server instance.
 * <p/>
 * The mail resource events from the admin instance are propagated
 * to this object.
 * <p/>
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author James Kong
 * @since JDK1.4
 */
@Service
@Scoped(Singleton.class)
public class MailResourceDeployer extends GlobalResourceDeployer
        implements ResourceDeployer {


    @Inject
    private GlassfishNamingManager namingMgr;

        //TODO V3 log strings for the entire class
    // StringManager for this deployer
    private static final StringManager localStrings =
            StringManager.getManager(com.sun.enterprise.resource.deployer.MailResourceDeployer.class);

    // logger for this deployer
    private static Logger _logger = LogDomains.getLogger(MailResourceDeployer.class, LogDomains.CORE_LOGGER);

    /**
     * {@inheritDoc}
     */
    public synchronized void deployResource(Object resource) throws Exception {

        com.sun.enterprise.config.serverbeans.MailResource mailRes =
                (com.sun.enterprise.config.serverbeans.MailResource) resource;

        if (mailRes == null) {
            _logger.log(Level.INFO, "core.resourcedeploy_error");
        } else {
            if (ConnectorsUtil.parseBoolean(mailRes.getEnabled())) {
            //registers the jsr77 object for the mail resource deployed
            //TODO V3 MOM is not available ?
            /*ManagementObjectManager mgr =
                getAppServerSwitchObject().getManagementObjectManager();
            mgr.registerJavaMailResource(mailRes.getJndiName());*/
            installResource(mailRes);
            } else {
                _logger.log(Level.INFO, "core.resource_disabled",
                        new Object[] {mailRes.getJndiName(),
                        ConnectorConstants.RES_TYPE_MAIL});
            } 
        }
    }

    /**
     * Local method for calling the ResourceInstaller for installing
     * mail resource in runtime.
     *
     * @param mailResource The mail resource to be installed.
     * @throws Exception when not able to create a resource
     */
    void installResource(com.sun.enterprise.config.serverbeans.MailResource mailResource) throws Exception {
        // Converts the config data to j2ee resource ;
        // retieves the resource installer ; installs the resource ;
        // and adds it to a collection in the installer
        JavaEEResource j2eeRes = toMailJavaEEResource(mailResource);
        //ResourceInstaller installer = runtime.getResourceInstaller();
        installMailResource((MailResource) j2eeRes);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void undeployResource(Object resource)
            throws Exception {

        com.sun.enterprise.config.serverbeans.MailResource mailRes =
                (com.sun.enterprise.config.serverbeans.MailResource) resource;

        // converts the config data to j2ee resource
        JavaEEResource javaEEResource = toMailJavaEEResource(mailRes);

        // removes the resource from jndi naming
        namingMgr.unpublishObject(javaEEResource.getName());

        /* TODO V3 handle later
            ManagementObjectManager mgr =
                    getAppServerSwitchObject().getManagementObjectManager();
            mgr.unregisterJavaMailResource(mailRes.getJndiName());
        */
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
        return resource instanceof com.sun.enterprise.config.serverbeans.MailResource;
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
     * Installs the given mail resource. This method gets called during server
     * initialization and from mail resource deployer to handle resource events.
     *
     * @param mailRes mail resource
     */
    public void installMailResource(com.sun.enterprise.resource.beans.MailResource mailRes) {

        String bindName = null;

        try {
            bindName = mailRes.getName();

            MailConfiguration config = new MailConfiguration(mailRes);

            javax.naming.Reference ref = new javax.naming.Reference(javax.mail.Session.class.getName(),
                    MailNamingObjectFactory.class.getName(),null);
            SerializableObjectRefAddr serializableRefAddr = new SerializableObjectRefAddr("jndiName", config);
            ref.add(serializableRefAddr);

            // Publish the object
            namingMgr.publishObject(bindName, ref, true);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "mailrsrc.create_obj_error", bindName);
            _logger.log(Level.SEVERE, "mailrsrc.create_obj_error_excp", ex);
        }
    }

    /**
     * Returns a new instance of j2ee mail resource from the given config bean.
     *
     * This method gets called from the mail resource deployer to convert mail
     * config bean into mail j2ee resource.
     *
     * @param    rbean    mail-resource config bean
     *
     * @return   a new instance of j2ee mail resource
     *
     */
    public static JavaEEResource toMailJavaEEResource(
        com.sun.enterprise.config.serverbeans.MailResource rbean) {

        com.sun.enterprise.resource.beans.MailResource jr = new MailResource(rbean.getJndiName());

        //jr.setDescription(rbean.getDescription()); // FIXME: getting error
        jr.setEnabled(ConnectorsUtil.parseBoolean(rbean.getEnabled()));
        jr.setStoreProtocol(rbean.getStoreProtocol());
        jr.setStoreProtocolClass(rbean.getStoreProtocolClass());
        jr.setTransportProtocol(rbean.getTransportProtocol());
        jr.setTransportProtocolClass(rbean.getTransportProtocolClass());
        jr.setMailHost(rbean.getHost());
        jr.setUsername(rbean.getUser());
        jr.setMailFrom(rbean.getFrom());
        jr.setDebug(ConnectorsUtil.parseBoolean(rbean.getDebug()));

        // sets the properties
        List<Property> properties = rbean.getProperty();
        if (properties != null) {

            for(Property property : properties){
                ResourceProperty rp = new ResourcePropertyImpl(property.getName(), property.getValue());
                jr.addProperty(rp);
            }
        }
        return jr;
    }
}
