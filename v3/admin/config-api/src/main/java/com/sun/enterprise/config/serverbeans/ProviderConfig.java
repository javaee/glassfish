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
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.List;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "requestPolicy",
    "responsePolicy",
    "property"
}) */
@org.glassfish.api.amx.AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.ProviderConfig")
@Configured
public interface ProviderConfig extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the providerId property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true, key=true)
    public String getProviderId();

    /**
     * Sets the value of the providerId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProviderId(String value) throws PropertyVetoException;

    /**
     * Gets the value of the providerType property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getProviderType();

    /**
     * Sets the value of the providerType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProviderType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the className property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getClassName();

    /**
     * Sets the value of the className property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClassName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the requestPolicy property.
     *
     * @return possible object is
     *         {@link RequestPolicy }
     */
    @Element
    public RequestPolicy getRequestPolicy();

    /**
     * Sets the value of the requestPolicy property.
     *
     * @param value allowed object is
     *              {@link RequestPolicy }
     */
    public void setRequestPolicy(RequestPolicy value) throws PropertyVetoException;

    /**
     * Gets the value of the responsePolicy property.
     *
     * @return possible object is
     *         {@link ResponsePolicy }
     */
    @Element
    public ResponsePolicy getResponsePolicy();

    /**
     * Sets the value of the responsePolicy property.
     *
     * @param value allowed object is
     *              {@link ResponsePolicy }
     */
    public void setResponsePolicy(ResponsePolicy value) throws PropertyVetoException;
    

  /**
        Properties.
     */
@PropertiesDesc(
    props={
        @PropertyDesc(name="security.config", defaultValue="${com.sun.aas.instanceRoot}/config/wss-server-config-1.0.xml",
            description="Specifies the location of the message security configuration file"),
            
        @PropertyDesc(name="debug", defaultValue="false", dataType=Boolean.class,
            description="Enables dumping of server provider debug messages to the server log"),
            
        @PropertyDesc(name="dynamic.username.password", defaultValue="false", dataType=Boolean.class,
            description="Signals the provider runtime to collect the user name and password from the " +
                "CallbackHandler for each request. If false, the user name and password for wsse:UsernameToken(s) is " +
                "collected once, during module initialization. Applicable only for a ClientAuthModule"),
            
        @PropertyDesc(name="encrypencryption.key.alias", defaultValue="s1as",
            description="Specifies the encryption key used by the provider. The key is identified by its keystore alias"),
            
        @PropertyDesc(name="signature.key.alaias", defaultValue="s1as",
            description="Specifies the signature key used by the provider. The key is identified by its keystore alias")
    }
    )
    @Override
    @Element
    List<Property> getProperty();
}
