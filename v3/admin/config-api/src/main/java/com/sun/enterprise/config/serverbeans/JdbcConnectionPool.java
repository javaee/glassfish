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
public class JdbcConnectionPool extends ConfigBean implements Resource, Serializable {

    final transient private VetoableChangeSupport support = new VetoableChangeSupport(this);
    
    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String name;
    @Attribute(required = true)

    protected String datasourceClassname;
    @Attribute

    protected String resType;
    @Attribute

    protected String steadyPoolSize;
    @Attribute

    protected String maxPoolSize;
    @Attribute

    protected String maxWaitTimeInMillis;
    @Attribute

    protected String poolResizeQuantity;
    @Attribute

    protected String idleTimeoutInSeconds;
    @Attribute

    protected String transactionIsolationLevel;
    @Attribute

    protected String isIsolationLevelGuaranteed;
    @Attribute

    protected String isConnectionValidationRequired;
    @Attribute

    protected String connectionValidationMethod;
    @Attribute

    protected String validationTableName;
    @Attribute

    protected String failAllConnections;
    @Attribute

    protected String nonTransactionalConnections;
    @Attribute

    protected String allowNonComponentCallers;
    @Attribute

    protected String validateAtmostOncePeriodInSeconds;
    @Attribute

    protected String connectionLeakTimeoutInSeconds;
    @Attribute

    protected String connectionLeakReclaim;
    @Attribute

    protected String connectionCreationRetryAttempts;
    @Attribute

    protected String connectionCreationRetryIntervalInSeconds;
    @Attribute

    protected String statementTimeoutInSeconds;
    @Attribute

    protected String lazyConnectionEnlistment;
    @Attribute

    protected String lazyConnectionAssociation;
    @Attribute

    protected String associateWithThread;
    @Attribute

    protected String matchConnections;
    @Attribute

    protected String maxConnectionUsageCount;
    @Attribute

    protected String wrapJdbcObjects;
    protected String description;
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
     * Gets the value of the datasourceClassname property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDatasourceClassname() {
        return datasourceClassname;
    }

    /**
     * Sets the value of the datasourceClassname property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDatasourceClassname(String value) throws PropertyVetoException {
        support.fireVetoableChange("datasourceClassname", this.datasourceClassname, value);

        this.datasourceClassname = value;
    }

    /**
     * Gets the value of the resType property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getResType() {
        return resType;
    }

    /**
     * Sets the value of the resType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResType(String value) throws PropertyVetoException {
        support.fireVetoableChange("resType", this.resType, value);

        this.resType = value;
    }

    /**
     * Gets the value of the steadyPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSteadyPoolSize() {
        if (steadyPoolSize == null) {
            return "8";
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
     * Gets the value of the maxPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMaxPoolSize() {
        if (maxPoolSize == null) {
            return "32";
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
     * Gets the value of the maxWaitTimeInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMaxWaitTimeInMillis() {
        if (maxWaitTimeInMillis == null) {
            return "60000";
        } else {
            return maxWaitTimeInMillis;
        }
    }

    /**
     * Sets the value of the maxWaitTimeInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxWaitTimeInMillis(String value) throws PropertyVetoException {
        support.fireVetoableChange("maxWaitTimeInMillis", this.maxWaitTimeInMillis, value);

        this.maxWaitTimeInMillis = value;
    }

    /**
     * Gets the value of the poolResizeQuantity property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPoolResizeQuantity() {
        if (poolResizeQuantity == null) {
            return "2";
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
     * Gets the value of the idleTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getIdleTimeoutInSeconds() {
        if (idleTimeoutInSeconds == null) {
            return "300";
        } else {
            return idleTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the idleTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIdleTimeoutInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("idleTimeoutInSeconds", this.idleTimeoutInSeconds, value);

        this.idleTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the transactionIsolationLevel property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getTransactionIsolationLevel() {
        return transactionIsolationLevel;
    }

    /**
     * Sets the value of the transactionIsolationLevel property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTransactionIsolationLevel(String value) throws PropertyVetoException {
        support.fireVetoableChange("transactionIsolationLevel", this.transactionIsolationLevel, value);

        this.transactionIsolationLevel = value;
    }

    /**
     * Gets the value of the isIsolationLevelGuaranteed property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getIsIsolationLevelGuaranteed() {
        if (isIsolationLevelGuaranteed == null) {
            return "true";
        } else {
            return isIsolationLevelGuaranteed;
        }
    }

    /**
     * Sets the value of the isIsolationLevelGuaranteed property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIsIsolationLevelGuaranteed(String value) throws PropertyVetoException {
        support.fireVetoableChange("isIsolationLevelGuaranteed", this.isIsolationLevelGuaranteed, value);

        this.isIsolationLevelGuaranteed = value;
    }

    /**
     * Gets the value of the isConnectionValidationRequired property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getIsConnectionValidationRequired() {
        if (isConnectionValidationRequired == null) {
            return "false";
        } else {
            return isConnectionValidationRequired;
        }
    }

    /**
     * Sets the value of the isConnectionValidationRequired property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIsConnectionValidationRequired(String value) throws PropertyVetoException {
        support.fireVetoableChange("isConnectionValidationRequired", this.isConnectionValidationRequired, value);

        this.isConnectionValidationRequired = value;
    }

    /**
     * Gets the value of the connectionValidationMethod property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConnectionValidationMethod() {
        if (connectionValidationMethod == null) {
            return "auto-commit";
        } else {
            return connectionValidationMethod;
        }
    }

    /**
     * Sets the value of the connectionValidationMethod property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionValidationMethod(String value) throws PropertyVetoException {
        support.fireVetoableChange("connectionValidationMethod", this.connectionValidationMethod, value);

        this.connectionValidationMethod = value;
    }

    /**
     * Gets the value of the validationTableName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getValidationTableName() {
        return validationTableName;
    }

    /**
     * Sets the value of the validationTableName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValidationTableName(String value) throws PropertyVetoException {
        support.fireVetoableChange("validationTableName", this.validationTableName, value);

        this.validationTableName = value;
    }

    /**
     * Gets the value of the failAllConnections property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFailAllConnections() {
        if (failAllConnections == null) {
            return "false";
        } else {
            return failAllConnections;
        }
    }

    /**
     * Sets the value of the failAllConnections property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFailAllConnections(String value) throws PropertyVetoException {
        support.fireVetoableChange("failAllConnections", this.failAllConnections, value);

        this.failAllConnections = value;
    }

    /**
     * Gets the value of the nonTransactionalConnections property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getNonTransactionalConnections() {
        if (nonTransactionalConnections == null) {
            return "false";
        } else {
            return nonTransactionalConnections;
        }
    }

    /**
     * Sets the value of the nonTransactionalConnections property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNonTransactionalConnections(String value) throws PropertyVetoException {
        support.fireVetoableChange("nonTransactionalConnections", this.nonTransactionalConnections, value);

        this.nonTransactionalConnections = value;
    }

    /**
     * Gets the value of the allowNonComponentCallers property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAllowNonComponentCallers() {
        if (allowNonComponentCallers == null) {
            return "false";
        } else {
            return allowNonComponentCallers;
        }
    }

    /**
     * Sets the value of the allowNonComponentCallers property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAllowNonComponentCallers(String value) throws PropertyVetoException {
        support.fireVetoableChange("allowNonComponentCallers", this.allowNonComponentCallers, value);

        this.allowNonComponentCallers = value;
    }

    /**
     * Gets the value of the validateAtmostOncePeriodInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getValidateAtmostOncePeriodInSeconds() {
        if (validateAtmostOncePeriodInSeconds == null) {
            return "0";
        } else {
            return validateAtmostOncePeriodInSeconds;
        }
    }

    /**
     * Sets the value of the validateAtmostOncePeriodInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValidateAtmostOncePeriodInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("validateAtmostOncePeriodInSeconds", this.validateAtmostOncePeriodInSeconds, value);

        this.validateAtmostOncePeriodInSeconds = value;
    }

    /**
     * Gets the value of the connectionLeakTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConnectionLeakTimeoutInSeconds() {
        if (connectionLeakTimeoutInSeconds == null) {
            return "0";
        } else {
            return connectionLeakTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the connectionLeakTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionLeakTimeoutInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("connectionLeakTimeoutInSeconds", this.connectionLeakTimeoutInSeconds, value);

        this.connectionLeakTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the connectionLeakReclaim property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConnectionLeakReclaim() {
        if (connectionLeakReclaim == null) {
            return "false";
        } else {
            return connectionLeakReclaim;
        }
    }

    /**
     * Sets the value of the connectionLeakReclaim property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionLeakReclaim(String value) throws PropertyVetoException {
        support.fireVetoableChange("connectionLeakReclaim", this.connectionLeakReclaim, value);

        this.connectionLeakReclaim = value;
    }

    /**
     * Gets the value of the connectionCreationRetryAttempts property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConnectionCreationRetryAttempts() {
        if (connectionCreationRetryAttempts == null) {
            return "0";
        } else {
            return connectionCreationRetryAttempts;
        }
    }

    /**
     * Sets the value of the connectionCreationRetryAttempts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionCreationRetryAttempts(String value) throws PropertyVetoException {
        support.fireVetoableChange("connectionCreationRetryAttempts", this.connectionCreationRetryAttempts, value);

        this.connectionCreationRetryAttempts = value;
    }

    /**
     * Gets the value of the connectionCreationRetryIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConnectionCreationRetryIntervalInSeconds() {
        if (connectionCreationRetryIntervalInSeconds == null) {
            return "10";
        } else {
            return connectionCreationRetryIntervalInSeconds;
        }
    }

    /**
     * Sets the value of the connectionCreationRetryIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionCreationRetryIntervalInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("connectionCreationRetryIntervalInSeconds", this.connectionCreationRetryIntervalInSeconds, value);

        this.connectionCreationRetryIntervalInSeconds = value;
    }

    /**
     * Gets the value of the statementTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getStatementTimeoutInSeconds() {
        if (statementTimeoutInSeconds == null) {
            return "-1";
        } else {
            return statementTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the statementTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatementTimeoutInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("statementTimeoutInSeconds", this.statementTimeoutInSeconds, value);

        this.statementTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the lazyConnectionEnlistment property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLazyConnectionEnlistment() {
        if (lazyConnectionEnlistment == null) {
            return "false";
        } else {
            return lazyConnectionEnlistment;
        }
    }

    /**
     * Sets the value of the lazyConnectionEnlistment property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLazyConnectionEnlistment(String value) throws PropertyVetoException {
        support.fireVetoableChange("lazyConnectionEnlistment", this.lazyConnectionEnlistment, value);

        this.lazyConnectionEnlistment = value;
    }

    /**
     * Gets the value of the lazyConnectionAssociation property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLazyConnectionAssociation() {
        if (lazyConnectionAssociation == null) {
            return "false";
        } else {
            return lazyConnectionAssociation;
        }
    }

    /**
     * Sets the value of the lazyConnectionAssociation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLazyConnectionAssociation(String value) throws PropertyVetoException {
        support.fireVetoableChange("lazyConnectionAssociation", this.lazyConnectionAssociation, value);

        this.lazyConnectionAssociation = value;
    }

    /**
     * Gets the value of the associateWithThread property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAssociateWithThread() {
        if (associateWithThread == null) {
            return "false";
        } else {
            return associateWithThread;
        }
    }

    /**
     * Sets the value of the associateWithThread property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAssociateWithThread(String value) throws PropertyVetoException {
        support.fireVetoableChange("associateWithThread", this.associateWithThread, value);

        this.associateWithThread = value;
    }

    /**
     * Gets the value of the matchConnections property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMatchConnections() {
        if (matchConnections == null) {
            return "false";
        } else {
            return matchConnections;
        }
    }

    /**
     * Sets the value of the matchConnections property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMatchConnections(String value) throws PropertyVetoException {
        support.fireVetoableChange("matchConnections", this.matchConnections, value);

        this.matchConnections = value;
    }

    /**
     * Gets the value of the maxConnectionUsageCount property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMaxConnectionUsageCount() {
        if (maxConnectionUsageCount == null) {
            return "0";
        } else {
            return maxConnectionUsageCount;
        }
    }

    /**
     * Sets the value of the maxConnectionUsageCount property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxConnectionUsageCount(String value) throws PropertyVetoException {
        support.fireVetoableChange("maxConnectionUsageCount", this.maxConnectionUsageCount, value);

        this.maxConnectionUsageCount = value;
    }

    /**
     * Gets the value of the wrapJdbcObjects property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getWrapJdbcObjects() {
        if (wrapJdbcObjects == null) {
            return "false";
        } else {
            return wrapJdbcObjects;
        }
    }

    /**
     * Sets the value of the wrapJdbcObjects property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWrapJdbcObjects(String value) throws PropertyVetoException {
        support.fireVetoableChange("wrapJdbcObjects", this.wrapJdbcObjects, value);

        this.wrapJdbcObjects = value;
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
