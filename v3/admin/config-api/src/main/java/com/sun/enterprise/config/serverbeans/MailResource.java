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
import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.io.Serializable;
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "description",
    "property"
}) */
@Configured
public class MailResource extends ConfigBean implements Resource, Serializable {

    final transient private VetoableChangeSupport support = new VetoableChangeSupport(this);
    
    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String jndiName;
    @Attribute

    protected String storeProtocol;
    @Attribute

    protected String storeProtocolClass;
    @Attribute

    protected String transportProtocol;
    @Attribute

    protected String transportProtocolClass;
    @Attribute(required = true)

    protected String host;
    @Attribute(required = true)

    protected String user;
    @Attribute(required = true)

    protected String from;
    @Attribute

    protected String debug;
    @Attribute

    protected String objectType;
    @Attribute

    protected String enabled;
    protected String description;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the jndiName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * Sets the value of the jndiName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJndiName(String value) throws PropertyVetoException {
        support.fireVetoableChange("jndiName", this.jndiName, value);

        this.jndiName = value;
    }

    /**
     * Gets the value of the storeProtocol property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getStoreProtocol() {
        if (storeProtocol == null) {
            return "imap";
        } else {
            return storeProtocol;
        }
    }

    /**
     * Sets the value of the storeProtocol property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStoreProtocol(String value) throws PropertyVetoException {
        support.fireVetoableChange("storeProtocol", this.storeProtocol, value);

        this.storeProtocol = value;
    }

    /**
     * Gets the value of the storeProtocolClass property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getStoreProtocolClass() {
        if (storeProtocolClass == null) {
            return "com.sun.mail.imap.IMAPStore";
        } else {
            return storeProtocolClass;
        }
    }

    /**
     * Sets the value of the storeProtocolClass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStoreProtocolClass(String value) throws PropertyVetoException {
        support.fireVetoableChange("storeProtocolClass", this.storeProtocolClass, value);

        this.storeProtocolClass = value;
    }

    /**
     * Gets the value of the transportProtocol property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getTransportProtocol() {
        if (transportProtocol == null) {
            return "smtp";
        } else {
            return transportProtocol;
        }
    }

    /**
     * Sets the value of the transportProtocol property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTransportProtocol(String value) throws PropertyVetoException {
        support.fireVetoableChange("transportProtocol", this.transportProtocol, value);

        this.transportProtocol = value;
    }

    /**
     * Gets the value of the transportProtocolClass property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getTransportProtocolClass() {
        if (transportProtocolClass == null) {
            return "com.sun.mail.smtp.SMTPTransport";
        } else {
            return transportProtocolClass;
        }
    }

    /**
     * Sets the value of the transportProtocolClass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTransportProtocolClass(String value) throws PropertyVetoException {
        support.fireVetoableChange("transportProtocolClass", this.transportProtocolClass, value);

        this.transportProtocolClass = value;
    }

    /**
     * Gets the value of the host property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHost(String value) throws PropertyVetoException {
        support.fireVetoableChange("host", this.host, value);

        this.host = value;
    }

    /**
     * Gets the value of the user property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUser(String value) throws PropertyVetoException {
        support.fireVetoableChange("user", this.user, value);

        this.user = value;
    }

    /**
     * Gets the value of the from property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFrom(String value) throws PropertyVetoException {
        support.fireVetoableChange("from", this.from, value);

        this.from = value;
    }

    /**
     * Gets the value of the debug property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDebug() {
        if (debug == null) {
            return "false";
        } else {
            return debug;
        }
    }

    /**
     * Sets the value of the debug property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDebug(String value) throws PropertyVetoException {
        support.fireVetoableChange("debug", this.debug, value);

        this.debug = value;
    }

    /**
     * Gets the value of the objectType property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getObjectType() {
        if (objectType == null) {
            return "user";
        } else {
            return objectType;
        }
    }

    /**
     * Sets the value of the objectType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setObjectType(String value) throws PropertyVetoException {
        support.fireVetoableChange("objectType", this.objectType, value);

        this.objectType = value;
    }

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getEnabled() {
        if (enabled == null) {
            return "true";
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("enabled", this.enabled, value);

        this.enabled = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) throws PropertyVetoException {
        support.fireVetoableChange("description", this.description, value);

        this.description = value;
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
