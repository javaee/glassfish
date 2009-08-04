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

package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.*;

/* @XmlType(name = "", propOrder = {
    "jmxConnector",
    "dasConfig",
    "property"
}) */

@Configured
/**
 * Admin Service exists in every instance. It is the configuration for either
 * a normal server, DAS or PE instance
 */
public interface AdminService extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the type property.
     * An instance can either be of type
     * das
         Domain Administration Server in SE/EE or the PE instance
     * das-and-server
     *   same as das
     * server
     *   Any non-DAS instance in SE/EE. Not valid for PE.
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="server")
    String getType();

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the systemJmxConnectorName property.
     * The name of the internal jmx connector
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getSystemJmxConnectorName();

    /**
     * Sets the value of the systemJmxConnectorName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSystemJmxConnectorName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jmxConnector property.
     * The jmx-connector element defines the configuration of a JSR 160
     * compliant remote JMX Connector.
     * Objects of the following type(s) are allowed in the list
     * {@link JmxConnector }
     */
    @Element("jmx-connector")
    List<JmxConnector> getJmxConnector();

    /**
     * Gets the value of the dasConfig property.
     *
     * @return possible object is
     *         {@link DasConfig }
     */
    @Element("das-config")
    DasConfig getDasConfig();

    /**
     * Sets the value of the dasConfig property.
     *
     * @param value allowed object is
     *              {@link DasConfig }
     */
    void setDasConfig(DasConfig value) throws PropertyVetoException;
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

    @DuckTyped
    String getAdminRealmName();

    class Duck {
        public static String getAdminRealmName(AdminService as) {
            String def            = "admin-realm";  //this is the default propertyName for an AuthRealm instance to be used for admin
            String propertyName   = "administration-auth-realm-propertyName"; //propertyName of the property
            String adminRealmName = def; //same as def, by default;
            List<Property> props = as.getProperty();
            for(Property p : props) {
                if (propertyName.equals(p.getName())) {
                    adminRealmName = p.getValue();     //someone has configured it
                    break;
                }
            }
            //ensure that this realm exists
            List<AuthRealm> realms = ((Config)as.getParent()).getSecurityService().getAuthRealm(); //this assumes that <config> is parent of <admin-service>
            boolean exists = false;
            for (AuthRealm realm : realms) {
                if (realm.getName().equals(adminRealmName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                throw new RuntimeException ("The realm for administration named: " + adminRealmName + " does not exist in the configuration. Create the Realm first");
            }
            return adminRealmName;
        }
    }
}
