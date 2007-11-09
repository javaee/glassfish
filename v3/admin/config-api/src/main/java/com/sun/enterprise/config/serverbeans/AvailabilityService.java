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
    "webContainerAvailability",
    "ejbContainerAvailability",
    "jmsAvailability",
    "property"
}) */
@Configured
public class AvailabilityService
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String availabilityEnabled;
    @Attribute

    protected String haAgentHosts;
    @Attribute

    protected String haAgentPort;
    @Attribute

    protected String haAgentPassword;
    @Attribute

    protected String haStoreName;
    @Attribute

    protected String autoManageHaStore;
    @Attribute

    protected String storePoolName;
    @Attribute

    protected String haStoreHealthcheckEnabled;
    @Attribute

    protected String haStoreHealthcheckIntervalInSeconds;
    @Element
    protected WebContainerAvailability webContainerAvailability;
    @Element
    protected EjbContainerAvailability ejbContainerAvailability;
    @Element
    protected JmsAvailability jmsAvailability;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the availabilityEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAvailabilityEnabled() {
        if (availabilityEnabled == null) {
            return "true";
        } else {
            return availabilityEnabled;
        }
    }

    /**
     * Sets the value of the availabilityEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAvailabilityEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("availabilityEnabled", this.availabilityEnabled, value);

        this.availabilityEnabled = value;
    }

    /**
     * Gets the value of the haAgentHosts property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHaAgentHosts() {
        return haAgentHosts;
    }

    /**
     * Sets the value of the haAgentHosts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHaAgentHosts(String value) throws PropertyVetoException {
        support.fireVetoableChange("haAgentHosts", this.haAgentHosts, value);

        this.haAgentHosts = value;
    }

    /**
     * Gets the value of the haAgentPort property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHaAgentPort() {
        return haAgentPort;
    }

    /**
     * Sets the value of the haAgentPort property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHaAgentPort(String value) throws PropertyVetoException {
        support.fireVetoableChange("haAgentPort", this.haAgentPort, value);

        this.haAgentPort = value;
    }

    /**
     * Gets the value of the haAgentPassword property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHaAgentPassword() {
        return haAgentPassword;
    }

    /**
     * Sets the value of the haAgentPassword property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHaAgentPassword(String value) throws PropertyVetoException {
        support.fireVetoableChange("haAgentPassword", this.haAgentPassword, value);

        this.haAgentPassword = value;
    }

    /**
     * Gets the value of the haStoreName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHaStoreName() {
        return haStoreName;
    }

    /**
     * Sets the value of the haStoreName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHaStoreName(String value) throws PropertyVetoException {
        support.fireVetoableChange("haStoreName", this.haStoreName, value);

        this.haStoreName = value;
    }

    /**
     * Gets the value of the autoManageHaStore property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAutoManageHaStore() {
        if (autoManageHaStore == null) {
            return "false";
        } else {
            return autoManageHaStore;
        }
    }

    /**
     * Sets the value of the autoManageHaStore property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAutoManageHaStore(String value) throws PropertyVetoException {
        support.fireVetoableChange("autoManageHaStore", this.autoManageHaStore, value);

        this.autoManageHaStore = value;
    }

    /**
     * Gets the value of the storePoolName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getStorePoolName() {
        return storePoolName;
    }

    /**
     * Sets the value of the storePoolName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStorePoolName(String value) throws PropertyVetoException {
        support.fireVetoableChange("storePoolName", this.storePoolName, value);

        this.storePoolName = value;
    }

    /**
     * Gets the value of the haStoreHealthcheckEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHaStoreHealthcheckEnabled() {
        if (haStoreHealthcheckEnabled == null) {
            return "false";
        } else {
            return haStoreHealthcheckEnabled;
        }
    }

    /**
     * Sets the value of the haStoreHealthcheckEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHaStoreHealthcheckEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("haStoreHealthcheckEnabled", this.haStoreHealthcheckEnabled, value);

        this.haStoreHealthcheckEnabled = value;
    }

    /**
     * Gets the value of the haStoreHealthcheckIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHaStoreHealthcheckIntervalInSeconds() {
        if (haStoreHealthcheckIntervalInSeconds == null) {
            return "5";
        } else {
            return haStoreHealthcheckIntervalInSeconds;
        }
    }

    /**
     * Sets the value of the haStoreHealthcheckIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHaStoreHealthcheckIntervalInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("haStoreHealthcheckIntervalInSeconds", this.haStoreHealthcheckIntervalInSeconds, value);

        this.haStoreHealthcheckIntervalInSeconds = value;
    }

    /**
     * Gets the value of the webContainerAvailability property.
     *
     * @return possible object is
     *         {@link WebContainerAvailability }
     */
    public WebContainerAvailability getWebContainerAvailability() {
        return webContainerAvailability;
    }

    /**
     * Sets the value of the webContainerAvailability property.
     *
     * @param value allowed object is
     *              {@link WebContainerAvailability }
     */
    public void setWebContainerAvailability(WebContainerAvailability value) throws PropertyVetoException {
        support.fireVetoableChange("webContainerAvailability", this.webContainerAvailability, value);

        this.webContainerAvailability = value;
    }

    /**
     * Gets the value of the ejbContainerAvailability property.
     *
     * @return possible object is
     *         {@link EjbContainerAvailability }
     */
    public EjbContainerAvailability getEjbContainerAvailability() {
        return ejbContainerAvailability;
    }

    /**
     * Sets the value of the ejbContainerAvailability property.
     *
     * @param value allowed object is
     *              {@link EjbContainerAvailability }
     */
    public void setEjbContainerAvailability(EjbContainerAvailability value) throws PropertyVetoException {
        support.fireVetoableChange("ejbContainerAvailability", this.ejbContainerAvailability, value);

        this.ejbContainerAvailability = value;
    }

    /**
     * Gets the value of the jmsAvailability property.
     *
     * @return possible object is
     *         {@link JmsAvailability }
     */
    public JmsAvailability getJmsAvailability() {
        return jmsAvailability;
    }

    /**
     * Sets the value of the jmsAvailability property.
     *
     * @param value allowed object is
     *              {@link JmsAvailability }
     */
    public void setJmsAvailability(JmsAvailability value) throws PropertyVetoException {
        support.fireVetoableChange("jmsAvailability", this.jmsAvailability, value);

        this.jmsAvailability = value;
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
