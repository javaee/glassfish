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
    "ejbTimerService",
    "property"
}) */
@Configured
public class EjbContainer
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String steadyPoolSize;
    @Attribute

    protected String poolResizeQuantity;
    @Attribute

    protected String maxPoolSize;
    @Attribute

    protected String cacheResizeQuantity;
    @Attribute

    protected String maxCacheSize;
    @Attribute

    protected String poolIdleTimeoutInSeconds;
    @Attribute

    protected String cacheIdleTimeoutInSeconds;
    @Attribute

    protected String removalTimeoutInSeconds;
    @Attribute

    protected String victimSelectionPolicy;
    @Attribute

    protected String commitOption;
    @Attribute

    protected String sessionStore;
    @Element
    protected EjbTimerService ejbTimerService;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the steadyPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSteadyPoolSize() {
        if (steadyPoolSize == null) {
            return "32";
        } else {
            return steadyPoolSize;
        }
    }

    /**
     * Sets the value of the steadyPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteadyPoolSize(String value) throws PropertyVetoException {
        support.fireVetoableChange("steadyPoolSize", this.steadyPoolSize, value);

        this.steadyPoolSize = value;
    }

    /**
     * Gets the value of the poolResizeQuantity property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPoolResizeQuantity() {
        if (poolResizeQuantity == null) {
            return "16";
        } else {
            return poolResizeQuantity;
        }
    }

    /**
     * Sets the value of the poolResizeQuantity property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPoolResizeQuantity(String value) throws PropertyVetoException {
        support.fireVetoableChange("poolResizeQuantity", this.poolResizeQuantity, value);

        this.poolResizeQuantity = value;
    }

    /**
     * Gets the value of the maxPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMaxPoolSize() {
        if (maxPoolSize == null) {
            return "64";
        } else {
            return maxPoolSize;
        }
    }

    /**
     * Sets the value of the maxPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxPoolSize(String value) throws PropertyVetoException {
        support.fireVetoableChange("maxPoolSize", this.maxPoolSize, value);

        this.maxPoolSize = value;
    }

    /**
     * Gets the value of the cacheResizeQuantity property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCacheResizeQuantity() {
        if (cacheResizeQuantity == null) {
            return "32";
        } else {
            return cacheResizeQuantity;
        }
    }

    /**
     * Sets the value of the cacheResizeQuantity property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCacheResizeQuantity(String value) throws PropertyVetoException {
        support.fireVetoableChange("cacheResizeQuantity", this.cacheResizeQuantity, value);

        this.cacheResizeQuantity = value;
    }

    /**
     * Gets the value of the maxCacheSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMaxCacheSize() {
        if (maxCacheSize == null) {
            return "512";
        } else {
            return maxCacheSize;
        }
    }

    /**
     * Sets the value of the maxCacheSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxCacheSize(String value) throws PropertyVetoException {
        support.fireVetoableChange("maxCacheSize", this.maxCacheSize, value);

        this.maxCacheSize = value;
    }

    /**
     * Gets the value of the poolIdleTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPoolIdleTimeoutInSeconds() {
        if (poolIdleTimeoutInSeconds == null) {
            return "600";
        } else {
            return poolIdleTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the poolIdleTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPoolIdleTimeoutInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("poolIdleTimeoutInSeconds", this.poolIdleTimeoutInSeconds, value);

        this.poolIdleTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the cacheIdleTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCacheIdleTimeoutInSeconds() {
        if (cacheIdleTimeoutInSeconds == null) {
            return "600";
        } else {
            return cacheIdleTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the cacheIdleTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCacheIdleTimeoutInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("cacheIdleTimeoutInSeconds", this.cacheIdleTimeoutInSeconds, value);

        this.cacheIdleTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the removalTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getRemovalTimeoutInSeconds() {
        if (removalTimeoutInSeconds == null) {
            return "5400";
        } else {
            return removalTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the removalTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRemovalTimeoutInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("removalTimeoutInSeconds", this.removalTimeoutInSeconds, value);

        this.removalTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the victimSelectionPolicy property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getVictimSelectionPolicy() {
        if (victimSelectionPolicy == null) {
            return "nru";
        } else {
            return victimSelectionPolicy;
        }
    }

    /**
     * Sets the value of the victimSelectionPolicy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVictimSelectionPolicy(String value) throws PropertyVetoException {
        support.fireVetoableChange("victimSelectionPolicy", this.victimSelectionPolicy, value);

        this.victimSelectionPolicy = value;
    }

    /**
     * Gets the value of the commitOption property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCommitOption() {
        if (commitOption == null) {
            return "B";
        } else {
            return commitOption;
        }
    }

    /**
     * Sets the value of the commitOption property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCommitOption(String value) throws PropertyVetoException {
        support.fireVetoableChange("commitOption", this.commitOption, value);

        this.commitOption = value;
    }

    /**
     * Gets the value of the sessionStore property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSessionStore() {
        return sessionStore;
    }

    /**
     * Sets the value of the sessionStore property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSessionStore(String value) throws PropertyVetoException {
        support.fireVetoableChange("sessionStore", this.sessionStore, value);

        this.sessionStore = value;
    }

    /**
     * Gets the value of the ejbTimerService property.
     *
     * @return possible object is
     *         {@link EjbTimerService }
     */
    public EjbTimerService getEjbTimerService() {
        return ejbTimerService;
    }

    /**
     * Sets the value of the ejbTimerService property.
     *
     * @param value allowed object is
     *              {@link EjbTimerService }
     */
    public void setEjbTimerService(EjbTimerService value) throws PropertyVetoException {
        support.fireVetoableChange("ejbTimerService", this.ejbTimerService, value);

        this.ejbTimerService = value;
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
