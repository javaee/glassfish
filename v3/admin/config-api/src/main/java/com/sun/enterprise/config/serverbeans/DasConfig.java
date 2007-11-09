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
public class DasConfig
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String dynamicReloadEnabled;
    @Attribute

    protected String dynamicReloadPollIntervalInSeconds;
    @Attribute

    protected String autodeployEnabled;
    @Attribute

    protected String autodeployPollingIntervalInSeconds;
    @Attribute

    protected String autodeployDir;
    @Attribute

    protected String autodeployVerifierEnabled;
    @Attribute

    protected String autodeployJspPrecompilationEnabled;
    @Attribute

    protected String deployXmlValidation;
    @Attribute

    protected String adminSessionTimeoutInMinutes;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the dynamicReloadEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDynamicReloadEnabled() {
        if (dynamicReloadEnabled == null) {
            return "false";
        } else {
            return dynamicReloadEnabled;
        }
    }

    /**
     * Sets the value of the dynamicReloadEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDynamicReloadEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("dynamicReloadEnabled", this.dynamicReloadEnabled, value);

        this.dynamicReloadEnabled = value;
    }

    /**
     * Gets the value of the dynamicReloadPollIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDynamicReloadPollIntervalInSeconds() {
        if (dynamicReloadPollIntervalInSeconds == null) {
            return "2";
        } else {
            return dynamicReloadPollIntervalInSeconds;
        }
    }

    /**
     * Sets the value of the dynamicReloadPollIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDynamicReloadPollIntervalInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("dynamicReloadPollIntervalInSeconds", this.dynamicReloadPollIntervalInSeconds, value);

        this.dynamicReloadPollIntervalInSeconds = value;
    }

    /**
     * Gets the value of the autodeployEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAutodeployEnabled() {
        if (autodeployEnabled == null) {
            return "false";
        } else {
            return autodeployEnabled;
        }
    }

    /**
     * Sets the value of the autodeployEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAutodeployEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("autodeployEnabled", this.autodeployEnabled, value);

        this.autodeployEnabled = value;
    }

    /**
     * Gets the value of the autodeployPollingIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAutodeployPollingIntervalInSeconds() {
        if (autodeployPollingIntervalInSeconds == null) {
            return "2";
        } else {
            return autodeployPollingIntervalInSeconds;
        }
    }

    /**
     * Sets the value of the autodeployPollingIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAutodeployPollingIntervalInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("autodeployPollingIntervalInSeconds", this.autodeployPollingIntervalInSeconds, value);

        this.autodeployPollingIntervalInSeconds = value;
    }

    /**
     * Gets the value of the autodeployDir property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAutodeployDir() {
        if (autodeployDir == null) {
            return "autodeploy";
        } else {
            return autodeployDir;
        }
    }

    /**
     * Sets the value of the autodeployDir property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAutodeployDir(String value) throws PropertyVetoException {
        support.fireVetoableChange("autodeployDir", this.autodeployDir, value);

        this.autodeployDir = value;
    }

    /**
     * Gets the value of the autodeployVerifierEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAutodeployVerifierEnabled() {
        if (autodeployVerifierEnabled == null) {
            return "false";
        } else {
            return autodeployVerifierEnabled;
        }
    }

    /**
     * Sets the value of the autodeployVerifierEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAutodeployVerifierEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("autodeployVerifierEnabled", this.autodeployVerifierEnabled, value);

        this.autodeployVerifierEnabled = value;
    }

    /**
     * Gets the value of the autodeployJspPrecompilationEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAutodeployJspPrecompilationEnabled() {
        if (autodeployJspPrecompilationEnabled == null) {
            return "false";
        } else {
            return autodeployJspPrecompilationEnabled;
        }
    }

    /**
     * Sets the value of the autodeployJspPrecompilationEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAutodeployJspPrecompilationEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("autodeployJspPrecompilationEnabled", this.autodeployJspPrecompilationEnabled, value);

        this.autodeployJspPrecompilationEnabled = value;
    }

    /**
     * Gets the value of the deployXmlValidation property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDeployXmlValidation() {
        if (deployXmlValidation == null) {
            return "full";
        } else {
            return deployXmlValidation;
        }
    }

    /**
     * Sets the value of the deployXmlValidation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDeployXmlValidation(String value) throws PropertyVetoException {
        support.fireVetoableChange("deployXmlValidation", this.deployXmlValidation, value);

        this.deployXmlValidation = value;
    }

    /**
     * Gets the value of the adminSessionTimeoutInMinutes property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAdminSessionTimeoutInMinutes() {
        if (adminSessionTimeoutInMinutes == null) {
            return "60";
        } else {
            return adminSessionTimeoutInMinutes;
        }
    }

    /**
     * Sets the value of the adminSessionTimeoutInMinutes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdminSessionTimeoutInMinutes(String value) throws PropertyVetoException {
        support.fireVetoableChange("adminSessionTimeoutInMinutes", this.adminSessionTimeoutInMinutes, value);

        this.adminSessionTimeoutInMinutes = value;
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
