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
import java.io.Serializable;
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "ssl",
    "property"
}) */
@Configured
public class HttpListener
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String id;
    @Attribute(required = true)

    protected String address;
    @Attribute(required = true)

    protected String port;
    @Attribute

    protected String externalPort;
    @Attribute

    protected String family;
    @Attribute

    protected String blockingEnabled;
    @Attribute

    protected String acceptorThreads;
    @Attribute

    protected String securityEnabled;
    @Attribute(required = true)

    protected String defaultVirtualServer;
    @Attribute(required = true)

    protected String serverName;
    @Attribute

    protected String redirectPort;
    @Attribute

    protected String xpoweredBy;
    @Attribute

    protected String enabled;
    protected Ssl ssl;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) throws PropertyVetoException {
        support.fireVetoableChange("id", this.id, value);

        this.id = value;
    }

    /**
     * Gets the value of the address property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAddress(String value) throws PropertyVetoException {
        support.fireVetoableChange("address", this.address, value);

        this.address = value;
    }

    /**
     * Gets the value of the port property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPort(String value) throws PropertyVetoException {
        support.fireVetoableChange("port", this.port, value);

        this.port = value;
    }

    /**
     * Gets the value of the externalPort property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getExternalPort() {
        return externalPort;
    }

    /**
     * Sets the value of the externalPort property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExternalPort(String value) throws PropertyVetoException {
        support.fireVetoableChange("externalPort", this.externalPort, value);

        this.externalPort = value;
    }

    /**
     * Gets the value of the family property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFamily() {
        if (family == null) {
            return "inet";
        } else {
            return family;
        }
    }

    /**
     * Sets the value of the family property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFamily(String value) throws PropertyVetoException {
        support.fireVetoableChange("family", this.family, value);

        this.family = value;
    }

    /**
     * Gets the value of the blockingEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getBlockingEnabled() {
        if (blockingEnabled == null) {
            return "false";
        } else {
            return blockingEnabled;
        }
    }

    /**
     * Sets the value of the blockingEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBlockingEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("blockingEnabled", this.blockingEnabled, value);

        this.blockingEnabled = value;
    }

    /**
     * Gets the value of the acceptorThreads property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAcceptorThreads() {
        if (acceptorThreads == null) {
            return "1";
        } else {
            return acceptorThreads;
        }
    }

    /**
     * Sets the value of the acceptorThreads property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAcceptorThreads(String value) throws PropertyVetoException {
        support.fireVetoableChange("acceptorThreads", this.acceptorThreads, value);

        this.acceptorThreads = value;
    }

    /**
     * Gets the value of the securityEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSecurityEnabled() {
        if (securityEnabled == null) {
            return "false";
        } else {
            return securityEnabled;
        }
    }

    /**
     * Sets the value of the securityEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSecurityEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("securityEnabled", this.securityEnabled, value);

        this.securityEnabled = value;
    }

    /**
     * Gets the value of the defaultVirtualServer property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDefaultVirtualServer() {
        return defaultVirtualServer;
    }

    /**
     * Sets the value of the defaultVirtualServer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultVirtualServer(String value) throws PropertyVetoException {
        support.fireVetoableChange("defaultVirtualServer", this.defaultVirtualServer, value);

        this.defaultVirtualServer = value;
    }

    /**
     * Gets the value of the serverName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Sets the value of the serverName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServerName(String value) throws PropertyVetoException {
        support.fireVetoableChange("serverName", this.serverName, value);

        this.serverName = value;
    }

    /**
     * Gets the value of the redirectPort property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getRedirectPort() {
        return redirectPort;
    }

    /**
     * Sets the value of the redirectPort property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRedirectPort(String value) throws PropertyVetoException {
        support.fireVetoableChange("redirectPort", this.redirectPort, value);

        this.redirectPort = value;
    }

    /**
     * Gets the value of the xpoweredBy property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getXpoweredBy() {
        if (xpoweredBy == null) {
            return "true";
        } else {
            return xpoweredBy;
        }
    }

    /**
     * Sets the value of the xpoweredBy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setXpoweredBy(String value) throws PropertyVetoException {
        support.fireVetoableChange("xpoweredBy", this.xpoweredBy, value);

        this.xpoweredBy = value;
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
     * Gets the value of the ssl property.
     *
     * @return possible object is
     *         {@link Ssl }
     */
    public Ssl getSsl() {
        return ssl;
    }

    /**
     * Sets the value of the ssl property.
     *
     * @param value allowed object is
     *              {@link Ssl }
     */
    public void setSsl(Ssl value) throws PropertyVetoException {
        support.fireVetoableChange("ssl", this.ssl, value);

        this.ssl = value;
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
