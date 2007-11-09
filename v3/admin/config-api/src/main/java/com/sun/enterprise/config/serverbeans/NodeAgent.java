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
    "jmxConnector",
    "authRealm",
    "logService",
    "property"
}) */
@Configured
public class NodeAgent
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String name;
    @Attribute

    protected String systemJmxConnectorName;
    @Attribute

    protected String startServersInStartup;
    @Element
    protected JmxConnector jmxConnector;
    @Element
    protected AuthRealm authRealm;
    @Element(required=true)
    protected LogService logService;
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
     * Gets the value of the systemJmxConnectorName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSystemJmxConnectorName() {
        return systemJmxConnectorName;
    }

    /**
     * Sets the value of the systemJmxConnectorName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSystemJmxConnectorName(String value) throws PropertyVetoException {
        support.fireVetoableChange("systemJmxConnectorName", this.systemJmxConnectorName, value);

        this.systemJmxConnectorName = value;
    }

    /**
     * Gets the value of the startServersInStartup property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getStartServersInStartup() {
        if (startServersInStartup == null) {
            return "true";
        } else {
            return startServersInStartup;
        }
    }

    /**
     * Sets the value of the startServersInStartup property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStartServersInStartup(String value) throws PropertyVetoException {
        support.fireVetoableChange("startServersInStartup", this.startServersInStartup, value);

        this.startServersInStartup = value;
    }

    /**
     * Gets the value of the jmxConnector property.
     *
     * @return possible object is
     *         {@link JmxConnector }
     */
    public JmxConnector getJmxConnector() {
        return jmxConnector;
    }

    /**
     * Sets the value of the jmxConnector property.
     *
     * @param value allowed object is
     *              {@link JmxConnector }
     */
    public void setJmxConnector(JmxConnector value) throws PropertyVetoException {
        support.fireVetoableChange("jmxConnector", this.jmxConnector, value);

        this.jmxConnector = value;
    }

    /**
     * Gets the value of the authRealm property.
     *
     * @return possible object is
     *         {@link AuthRealm }
     */
    public AuthRealm getAuthRealm() {
        return authRealm;
    }

    /**
     * Sets the value of the authRealm property.
     *
     * @param value allowed object is
     *              {@link AuthRealm }
     */
    public void setAuthRealm(AuthRealm value) throws PropertyVetoException {
        support.fireVetoableChange("authRealm", this.authRealm, value);

        this.authRealm = value;
    }

    /**
     * Gets the value of the logService property.
     *
     * @return possible object is
     *         {@link LogService }
     */
    public LogService getLogService() {
        return logService;
    }

    /**
     * Sets the value of the logService property.
     *
     * @param value allowed object is
     *              {@link LogService }
     */
    public void setLogService(LogService value) throws PropertyVetoException {
        support.fireVetoableChange("logService", this.logService, value);

        this.logService = value;
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
