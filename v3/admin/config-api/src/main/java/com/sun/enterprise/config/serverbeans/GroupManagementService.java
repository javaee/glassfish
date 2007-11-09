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
public class GroupManagementService
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String fdProtocolMaxTries;
    @Attribute

    protected String fdProtocolTimeoutInMillis;
    @Attribute

    protected String mergeProtocolMaxIntervalInMillis;
    @Attribute

    protected String mergeProtocolMinIntervalInMillis;
    @Attribute

    protected String pingProtocolTimeoutInMillis;
    @Attribute

    protected String vsProtocolTimeoutInMillis;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the fdProtocolMaxTries property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFdProtocolMaxTries() {
        if (fdProtocolMaxTries == null) {
            return "3";
        } else {
            return fdProtocolMaxTries;
        }
    }

    /**
     * Sets the value of the fdProtocolMaxTries property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFdProtocolMaxTries(String value) throws PropertyVetoException {
        support.fireVetoableChange("fdProtocolMaxTries", this.fdProtocolMaxTries, value);

        this.fdProtocolMaxTries = value;
    }

    /**
     * Gets the value of the fdProtocolTimeoutInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFdProtocolTimeoutInMillis() {
        if (fdProtocolTimeoutInMillis == null) {
            return "2000";
        } else {
            return fdProtocolTimeoutInMillis;
        }
    }

    /**
     * Sets the value of the fdProtocolTimeoutInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFdProtocolTimeoutInMillis(String value) throws PropertyVetoException {
        support.fireVetoableChange("fdProtocolTimeoutInMillis", this.fdProtocolTimeoutInMillis, value);

        this.fdProtocolTimeoutInMillis = value;
    }

    /**
     * Gets the value of the mergeProtocolMaxIntervalInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMergeProtocolMaxIntervalInMillis() {
        if (mergeProtocolMaxIntervalInMillis == null) {
            return "10000";
        } else {
            return mergeProtocolMaxIntervalInMillis;
        }
    }

    /**
     * Sets the value of the mergeProtocolMaxIntervalInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMergeProtocolMaxIntervalInMillis(String value) throws PropertyVetoException {
        support.fireVetoableChange("mergeProtocolMaxIntervalInMillis", this.mergeProtocolMaxIntervalInMillis, value);

        this.mergeProtocolMaxIntervalInMillis = value;
    }

    /**
     * Gets the value of the mergeProtocolMinIntervalInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMergeProtocolMinIntervalInMillis() {
        if (mergeProtocolMinIntervalInMillis == null) {
            return "5000";
        } else {
            return mergeProtocolMinIntervalInMillis;
        }
    }

    /**
     * Sets the value of the mergeProtocolMinIntervalInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMergeProtocolMinIntervalInMillis(String value) throws PropertyVetoException {
        support.fireVetoableChange("mergeProtocolMinIntervalInMillis", this.mergeProtocolMinIntervalInMillis, value);

        this.mergeProtocolMinIntervalInMillis = value;
    }

    /**
     * Gets the value of the pingProtocolTimeoutInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPingProtocolTimeoutInMillis() {
        if (pingProtocolTimeoutInMillis == null) {
            return "2000";
        } else {
            return pingProtocolTimeoutInMillis;
        }
    }

    /**
     * Sets the value of the pingProtocolTimeoutInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPingProtocolTimeoutInMillis(String value) throws PropertyVetoException {
        support.fireVetoableChange("pingProtocolTimeoutInMillis", this.pingProtocolTimeoutInMillis, value);

        this.pingProtocolTimeoutInMillis = value;
    }

    /**
     * Gets the value of the vsProtocolTimeoutInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getVsProtocolTimeoutInMillis() {
        if (vsProtocolTimeoutInMillis == null) {
            return "1500";
        } else {
            return vsProtocolTimeoutInMillis;
        }
    }

    /**
     * Sets the value of the vsProtocolTimeoutInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVsProtocolTimeoutInMillis(String value) throws PropertyVetoException {
        support.fireVetoableChange("vsProtocolTimeoutInMillis", this.vsProtocolTimeoutInMillis, value);

        this.vsProtocolTimeoutInMillis = value;
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
