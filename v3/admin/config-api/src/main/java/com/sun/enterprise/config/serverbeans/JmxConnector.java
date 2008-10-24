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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;
import java.util.List;

import java.beans.PropertyVetoException;
import org.glassfish.config.support.datatypes.Port;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "ssl",
    "property"
}) */
@org.glassfish.api.amx.AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.JMXConnectorConfig")
@Configured
public interface JmxConnector extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true, key=true)
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the protocol property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="rmi_jrmp")
    public String getProtocol();

    /**
     * Sets the value of the protocol property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProtocol(String value) throws PropertyVetoException;

    /**
     * Gets the value of the address property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getAddress();

    /**
     * Sets the value of the address property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAddress(String value) throws PropertyVetoException;

    /**
     * Gets the value of the port property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (dataType=Port.class)
    public String getPort();

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPort(String value) throws PropertyVetoException;

    /**
     * Gets the value of the acceptAll property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getAcceptAll();

    /**
     * Sets the value of the acceptAll property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAcceptAll(String value) throws PropertyVetoException;

    /**
     * Gets the value of the authRealmName property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getAuthRealmName();

    /**
     * Sets the value of the authRealmName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAuthRealmName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the securityEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    public String getSecurityEnabled();

    /**
     * Sets the value of the securityEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSecurityEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ssl property.
     *
     * @return possible object is
     *         {@link Ssl }
     */
    @Element
    public Ssl getSsl();

    /**
     * Sets the value of the ssl property.
     *
     * @param value allowed object is
     *              {@link Ssl }
     */
    public void setSsl(Ssl value) throws PropertyVetoException;
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
