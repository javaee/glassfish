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
import java.beans.VetoableChangeSupport;
import java.io.Serializable;
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "description",
    "webServiceEndpoint",
    "property"
}) */
@Configured
public class EjbModule extends ConfigBean implements Module, Serializable {

    final transient private VetoableChangeSupport support = new VetoableChangeSupport(this);
    
    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String name;
    @Attribute(required = true)

    protected String location;
    @Attribute

    protected String objectType;
    @Attribute

    protected String enabled;
    @Attribute

    protected String libraries;
    @Attribute

    protected String availabilityEnabled;
    @Attribute

    protected String directoryDeployed;
    protected String description;
    @Element
    protected List<WebServiceEndpoint> webServiceEndpoint = new ConstrainedList<WebServiceEndpoint>(this, "webServiceEndpoint", support);
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException {
        support.fireVetoableChange("name", this.name, value);

        this.name = value;
    }

    /**
     * Gets the value of the location property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLocation(String value) throws PropertyVetoException {
        support.fireVetoableChange("location", this.location, value);

        this.location = value;
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
     * Gets the value of the libraries property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLibraries() {
        return libraries;
    }

    /**
     * Sets the value of the libraries property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLibraries(String value) throws PropertyVetoException {
        support.fireVetoableChange("libraries", this.libraries, value);

        this.libraries = value;
    }

    /**
     * Gets the value of the availabilityEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAvailabilityEnabled() {
        if (availabilityEnabled == null) {
            return "false";
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
     * Gets the value of the directoryDeployed property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDirectoryDeployed() {
        if (directoryDeployed == null) {
            return "false";
        } else {
            return directoryDeployed;
        }
    }

    /**
     * Sets the value of the directoryDeployed property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDirectoryDeployed(String value) throws PropertyVetoException {
        support.fireVetoableChange("directoryDeployed", this.directoryDeployed, value);

        this.directoryDeployed = value;
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
     * Gets the value of the webServiceEndpoint property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the webServiceEndpoint property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWebServiceEndpoint().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link WebServiceEndpoint }
     */
    public List<WebServiceEndpoint> getWebServiceEndpoint() {
        return this.webServiceEndpoint;
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
