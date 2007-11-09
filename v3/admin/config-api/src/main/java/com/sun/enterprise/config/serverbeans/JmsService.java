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
import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "jmsHost",
    "property"
}) */
@Configured
public class JmsService
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String initTimeoutInSeconds;
    @Attribute(required = true)

    protected String type;
    @Attribute

    protected String startArgs;
    @Attribute

    protected String defaultJmsHost;
    @Attribute

    protected String reconnectIntervalInSeconds;
    @Attribute

    protected String reconnectAttempts;
    @Attribute

    protected String reconnectEnabled;
    @Attribute

    protected String addresslistBehavior;
    @Attribute

    protected String addresslistIterations;
    @Attribute

    protected String mqScheme;
    @Attribute

    protected String mqService;
    @Element
    protected List<JmsHost> jmsHost = new ConstrainedList<JmsHost>(this, "jmsHost", support);
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the initTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getInitTimeoutInSeconds() {
        if (initTimeoutInSeconds == null) {
            return "60";
        } else {
            return initTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the initTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInitTimeoutInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("initTimeoutInSeconds", this.initTimeoutInSeconds, value);

        this.initTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setType(String value) throws PropertyVetoException {
        support.fireVetoableChange("type", this.type, value);

        this.type = value;
    }

    /**
     * Gets the value of the startArgs property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getStartArgs() {
        return startArgs;
    }

    /**
     * Sets the value of the startArgs property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStartArgs(String value) throws PropertyVetoException {
        support.fireVetoableChange("startArgs", this.startArgs, value);

        this.startArgs = value;
    }

    /**
     * Gets the value of the defaultJmsHost property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDefaultJmsHost() {
        return defaultJmsHost;
    }

    /**
     * Sets the value of the defaultJmsHost property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultJmsHost(String value) throws PropertyVetoException {
        support.fireVetoableChange("defaultJmsHost", this.defaultJmsHost, value);

        this.defaultJmsHost = value;
    }

    /**
     * Gets the value of the reconnectIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getReconnectIntervalInSeconds() {
        if (reconnectIntervalInSeconds == null) {
            return "5";
        } else {
            return reconnectIntervalInSeconds;
        }
    }

    /**
     * Sets the value of the reconnectIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReconnectIntervalInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("reconnectIntervalInSeconds", this.reconnectIntervalInSeconds, value);

        this.reconnectIntervalInSeconds = value;
    }

    /**
     * Gets the value of the reconnectAttempts property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getReconnectAttempts() {
        if (reconnectAttempts == null) {
            return "3";
        } else {
            return reconnectAttempts;
        }
    }

    /**
     * Sets the value of the reconnectAttempts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReconnectAttempts(String value) throws PropertyVetoException {
        support.fireVetoableChange("reconnectAttempts", this.reconnectAttempts, value);

        this.reconnectAttempts = value;
    }

    /**
     * Gets the value of the reconnectEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getReconnectEnabled() {
        if (reconnectEnabled == null) {
            return "true";
        } else {
            return reconnectEnabled;
        }
    }

    /**
     * Sets the value of the reconnectEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReconnectEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("reconnectEnabled", this.reconnectEnabled, value);

        this.reconnectEnabled = value;
    }

    /**
     * Gets the value of the addresslistBehavior property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAddresslistBehavior() {
        if (addresslistBehavior == null) {
            return "random";
        } else {
            return addresslistBehavior;
        }
    }

    /**
     * Sets the value of the addresslistBehavior property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAddresslistBehavior(String value) throws PropertyVetoException {
        support.fireVetoableChange("addresslistBehavior", this.addresslistBehavior, value);

        this.addresslistBehavior = value;
    }

    /**
     * Gets the value of the addresslistIterations property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAddresslistIterations() {
        if (addresslistIterations == null) {
            return "3";
        } else {
            return addresslistIterations;
        }
    }

    /**
     * Sets the value of the addresslistIterations property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAddresslistIterations(String value) throws PropertyVetoException {
        support.fireVetoableChange("addresslistIterations", this.addresslistIterations, value);

        this.addresslistIterations = value;
    }

    /**
     * Gets the value of the mqScheme property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMqScheme() {
        return mqScheme;
    }

    /**
     * Sets the value of the mqScheme property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMqScheme(String value) throws PropertyVetoException {
        support.fireVetoableChange("mqScheme", this.mqScheme, value);

        this.mqScheme = value;
    }

    /**
     * Gets the value of the mqService property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMqService() {
        return mqService;
    }

    /**
     * Sets the value of the mqService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMqService(String value) throws PropertyVetoException {
        support.fireVetoableChange("mqService", this.mqService, value);

        this.mqService = value;
    }

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
    public List<JmsHost> getJmsHost() {
        return this.jmsHost;
    }

    /**
     * Gets the value of the property property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     */
    public List<Property> getProperty() {
        return this.property;
    }



}
