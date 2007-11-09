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
    "serverRef",
    "resourceRef",
    "applicationRef",
    "systemProperty",
    "property"
}) */
@Configured
public class Cluster
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String name;
    @Attribute(required = true)

    protected String configRef;
    @Attribute

    protected String heartbeatEnabled;
    @Attribute

    protected String heartbeatPort;
    @Attribute

    protected String heartbeatAddress;
    @Element
    protected List<ServerRef> serverRef = new ConstrainedList<ServerRef>(this, "serverRef", support);
    @Element
    protected List<ResourceRef> resourceRef = new ConstrainedList<ResourceRef>(this, "resourceRef", support);
    @Element
    protected List<ApplicationRef> applicationRef = new ConstrainedList<ApplicationRef>(this, "applicationRef", support);
    @Element
    protected List<SystemProperty> systemProperty = new ConstrainedList<SystemProperty>(this, "systemProperty", support);
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
     * Gets the value of the configRef property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConfigRef() {
        return configRef;
    }

    /**
     * Sets the value of the configRef property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConfigRef(String value) throws PropertyVetoException {
        support.fireVetoableChange("configRef", this.configRef, value);

        this.configRef = value;
    }

    /**
     * Gets the value of the heartbeatEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHeartbeatEnabled() {
        if (heartbeatEnabled == null) {
            return "true";
        } else {
            return heartbeatEnabled;
        }
    }

    /**
     * Sets the value of the heartbeatEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHeartbeatEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("heartbeatEnabled", this.heartbeatEnabled, value);

        this.heartbeatEnabled = value;
    }

    /**
     * Gets the value of the heartbeatPort property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHeartbeatPort() {
        return heartbeatPort;
    }

    /**
     * Sets the value of the heartbeatPort property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHeartbeatPort(String value) throws PropertyVetoException {
        support.fireVetoableChange("heartbeatPort", this.heartbeatPort, value);

        this.heartbeatPort = value;
    }

    /**
     * Gets the value of the heartbeatAddress property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHeartbeatAddress() {
        return heartbeatAddress;
    }

    /**
     * Sets the value of the heartbeatAddress property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHeartbeatAddress(String value) throws PropertyVetoException {
        support.fireVetoableChange("heartbeatAddress", this.heartbeatAddress, value);

        this.heartbeatAddress = value;
    }

    /**
     * Gets the value of the serverRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serverRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServerRef().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ServerRef }
     */
    public List<ServerRef> getServerRef() {
        return this.serverRef;
    }

    /**
     * Gets the value of the resourceRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceRef().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceRef }
     */
    public List<ResourceRef> getResourceRef() {
        return this.resourceRef;
    }

    /**
     * Gets the value of the applicationRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applicationRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplicationRef().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ApplicationRef }
     */
    public List<ApplicationRef> getApplicationRef() {
        return this.applicationRef;
    }

    /**
     * Gets the value of the systemProperty property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the systemProperty property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSystemProperty().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link SystemProperty }
     */
    public List<SystemProperty> getSystemProperty() {
        return this.systemProperty;
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
