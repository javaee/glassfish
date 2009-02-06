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
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;

import org.glassfish.quality.ToDo;

import org.glassfish.api.amx.AMXConfigInfo;

/* @XmlType(name = "", propOrder = {
    "jmsHost",
    "property"
}) */
@AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.JMSServiceConfig", singleton=true)
@Configured
public interface JmsService extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the initTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="60")
    public String getInitTimeoutInSeconds();

    /**
     * Sets the value of the initTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInitTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the type property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getType();

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the startArgs property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getStartArgs();

    /**
     * Sets the value of the startArgs property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStartArgs(String value) throws PropertyVetoException;

    /**
     * Gets the value of the defaultJmsHost property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getDefaultJmsHost();

    /**
     * Sets the value of the defaultJmsHost property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultJmsHost(String value) throws PropertyVetoException;

    /**
     * Gets the value of the reconnectIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="5")
    public String getReconnectIntervalInSeconds();

    /**
     * Sets the value of the reconnectIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReconnectIntervalInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the reconnectAttempts property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="3")
    public String getReconnectAttempts();

    /**
     * Sets the value of the reconnectAttempts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReconnectAttempts(String value) throws PropertyVetoException;

    /**
     * Gets the value of the reconnectEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true")
    public String getReconnectEnabled();

    /**
     * Sets the value of the reconnectEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReconnectEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the addresslistBehavior property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="random")
    public String getAddresslistBehavior();

    /**
     * Sets the value of the addresslistBehavior property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAddresslistBehavior(String value) throws PropertyVetoException;

    /**
     * Gets the value of the addresslistIterations property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="3")
    public String getAddresslistIterations();

    /**
     * Sets the value of the addresslistIterations property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAddresslistIterations(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mqScheme property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getMqScheme();

    /**
     * Sets the value of the mqScheme property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMqScheme(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mqService property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getMqService();

    /**
     * Sets the value of the mqService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMqService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jmsHost property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jmsHost property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJmsHost().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link JmsHost }
     */
    @Element
    public List<JmsHost> getJmsHost();
    
     /**
        Properties.
     */
@PropertiesDesc(
    props={
        @PropertyDesc(name="instance-name", defaultValue="imqbroker",
            description="The full Sun GlassFish Message Queue broker instance name"),
            
        @PropertyDesc(name="instance-name-suffix", defaultValue="xxxxxxxxxxxxxxxxxx",
            description="A suffix to add to the full Message Queue broker instance name. The suffix is separated " +
                "from the instance name by an underscore character (_). For example, if the instance name is 'imqbroker', " +
                "appending the suffix 'xyz' changes the instance name to 'imqbroker_xyz'"),
            
        @PropertyDesc(name="append-version", defaultValue="",
            description="If true, appends the major and minor version numbers, preceded by underscore characters (_), " +
                "to the full Message Queue broker instance name. For example, if the instance name is 'imqbroker', " +
                "appending the version numbers changes the instance name to imqbroker_8_0"),
            
        @PropertyDesc(name="user-name", defaultValue="xxxxxxxxxxxxxxxxxx",
            description="Specifies the user name for creating the JMS connection. Needed only if the default " +
                "username/password of guest/guest is not available in the broker"),
            
        @PropertyDesc(name="password", defaultValue="xxxxxxxxxxxxxxxxxx",
            description="Specifies the password for creating the JMS connection. Needed only if the default " +
                "username/password of guest/guest is not available in the broker")
    }
    )
    @Element
    List<Property> getProperty();
    
    }
