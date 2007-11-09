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
    "property"
}) */
@Configured
public class WebContainerAvailability
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String availabilityEnabled;
    @Attribute

    protected String persistenceType;
    @Attribute

    protected String persistenceFrequency;
    @Attribute

    protected String persistenceScope;
    @Attribute

    protected String persistenceStoreHealthCheckEnabled;
    @Attribute

    protected String ssoFailoverEnabled;
    @Attribute

    protected String httpSessionStorePoolName;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the availabilityEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAvailabilityEnabled() {
        return availabilityEnabled;
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
     * Gets the value of the persistenceType property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPersistenceType() {
        if (persistenceType == null) {
            return "memory";
        } else {
            return persistenceType;
        }
    }

    /**
     * Sets the value of the persistenceType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPersistenceType(String value) throws PropertyVetoException {
        support.fireVetoableChange("persistenceType", this.persistenceType, value);

        this.persistenceType = value;
    }

    /**
     * Gets the value of the persistenceFrequency property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPersistenceFrequency() {
        return persistenceFrequency;
    }

    /**
     * Sets the value of the persistenceFrequency property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPersistenceFrequency(String value) throws PropertyVetoException {
        support.fireVetoableChange("persistenceFrequency", this.persistenceFrequency, value);

        this.persistenceFrequency = value;
    }

    /**
     * Gets the value of the persistenceScope property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPersistenceScope() {
        return persistenceScope;
    }

    /**
     * Sets the value of the persistenceScope property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPersistenceScope(String value) throws PropertyVetoException {
        support.fireVetoableChange("persistenceScope", this.persistenceScope, value);

        this.persistenceScope = value;
    }

    /**
     * Gets the value of the persistenceStoreHealthCheckEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPersistenceStoreHealthCheckEnabled() {
        if (persistenceStoreHealthCheckEnabled == null) {
            return "false";
        } else {
            return persistenceStoreHealthCheckEnabled;
        }
    }

    /**
     * Sets the value of the persistenceStoreHealthCheckEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPersistenceStoreHealthCheckEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("persistenceStoreHealthCheckEnabled", this.persistenceStoreHealthCheckEnabled, value);

        this.persistenceStoreHealthCheckEnabled = value;
    }

    /**
     * Gets the value of the ssoFailoverEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSsoFailoverEnabled() {
        if (ssoFailoverEnabled == null) {
            return "false";
        } else {
            return ssoFailoverEnabled;
        }
    }

    /**
     * Sets the value of the ssoFailoverEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsoFailoverEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("ssoFailoverEnabled", this.ssoFailoverEnabled, value);

        this.ssoFailoverEnabled = value;
    }

    /**
     * Gets the value of the httpSessionStorePoolName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHttpSessionStorePoolName() {
        return httpSessionStorePoolName;
    }

    /**
     * Sets the value of the httpSessionStorePoolName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHttpSessionStorePoolName(String value) throws PropertyVetoException {
        support.fireVetoableChange("httpSessionStorePoolName", this.httpSessionStorePoolName, value);

        this.httpSessionStorePoolName = value;
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
