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
package com.sun.enterprise.resource;

import com.sun.enterprise.repository.ResourceProperty;
import com.sun.enterprise.resource.beans.JavaEEResource;
import com.sun.enterprise.resource.beans.MailResource;
import com.sun.enterprise.resource.beans.PMFResource;
import com.sun.enterprise.resource.beans.ExternalJndiResource;
import com.sun.enterprise.resource.beans.CustomResource;
import org.glassfish.api.admin.config.Property;

import java.util.List;

/**
 * Config bean to runtime bean convertor for various resources
 */
public class ResourceConverter {

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
    public static JavaEEResource toMailJ2EEResource(
        com.sun.enterprise.config.serverbeans.MailResource rbean) {

        com.sun.enterprise.resource.beans.MailResource jr = new MailResource(rbean.getJndiName());

        //jr.setDescription(rbean.getDescription()); // FIXME: getting error
        //jr.setEnabled(rbean.isEnabled());TODO V3 setEnabled is not available ?
        jr.setStoreProtocol(rbean.getStoreProtocol());
        jr.setStoreProtocolClass(rbean.getStoreProtocolClass());
        jr.setTransportProtocol(rbean.getTransportProtocol());
        jr.setTransportProtocolClass(rbean.getTransportProtocolClass());
        jr.setMailHost(rbean.getHost());
        jr.setUsername(rbean.getUser());
        jr.setMailFrom(rbean.getFrom());
        //jr.setDebug(rbean.isDebug()); TODO V3 setDebug not available ?

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
    public static com.sun.enterprise.resource.beans.JavaEEResource toExternalJndiJ2EEResource(
            com.sun.enterprise.config.serverbeans.ExternalJndiResource rbean) {

        ExternalJndiResource jr = new com.sun.enterprise.resource.beans.ExternalJndiResource(rbean.getJndiName());

        //jr.setDescription( rbean.getDescription() ); // FIXME: getting error

        // sets the enable flag
        //TODO V3 handle later
        //jr.setEnabled( rbean.isEnabled() );

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

    /**
     * Returns a new instance of j2ee custom resource from the given
     * config bean.
     *
     * This method gets called from the custom resource deployer
     * to convert custom-resource config bean into custom j2ee resource.
     *
     * @param    rbean   custom-resource config bean
     *
     * @return   new instance of j2ee custom resource
     */
    public static JavaEEResource toCustomJ2EEResource(
            com.sun.enterprise.config.serverbeans.CustomResource rbean) {

        CustomResource jr = new com.sun.enterprise.resource.beans.CustomResource( rbean.getJndiName() );

        //jr.setDescription(rbean.getDescription()); // FIXME: getting error

        // sets the enable flag
        //TODO V3 setEnabled() not available ?
        // jr.setEnabled( rbean.isEnabled() );

        // sets the resource type
        jr.setResType( rbean.getResType() );

        // sets the factory class name
        jr.setFactoryClass( rbean.getFactoryClass() );

        // sets the properties
        List<Property> properties = rbean.getProperty();
        if (properties!= null) {
            for(Property property : properties) {
                ResourceProperty rp =
                    new ResourcePropertyImpl(property.getName(), property.getValue());
                jr.addProperty(rp);
            }
        }
        return jr;
    }


    /**
     * Returns a new instance of j2ee pmf resource from the given config bean.
     *
     * This method gets called from the Persistence Manager Factory Resource
     * deployer to convert persistence-manager-resource-factory config bean into
     * pmf j2ee resource.
     *
     * @param rbean persistence-manager-resource-factory config bean
     *
     * @return a new instance of j2ee pmf resource
     *
     */
    public static JavaEEResource toPMFJ2EEResource(
            com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource rbean) {
        com.sun.enterprise.resource.beans.PMFResource jr = new PMFResource(rbean.getJndiName());
        //TODO V3 setEnabled() not available ?
        //jr.setEnabled(rbean.isEnabled());
        jr.setFactoryClass(rbean.getFactoryClass());
        jr.setJdbcResourceJndiName(rbean.getJdbcResourceJndiName());

        List<Property> properties = rbean.getProperty();
        if (properties!= null) {
            for (Property property : properties) {
                ResourceProperty rp = new ResourcePropertyImpl(property.getName(), property.getValue());
                jr.addProperty(rp);
            }
        }
        //jr.setDescription(next.getDescription()); // FIXME add this

        return jr;
    }

}
