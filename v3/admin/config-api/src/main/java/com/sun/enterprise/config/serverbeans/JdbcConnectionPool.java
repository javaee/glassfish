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

import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;
import java.beans.PropertyVetoException;

import org.glassfish.config.support.datatypes.Port;
import org.glassfish.config.support.datatypes.PositiveInteger;
import org.glassfish.config.support.datatypes.NonNegativeInteger;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "description",
    "property"
}) */
@org.glassfish.api.amx.AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.JDBCConnectionPoolConfig")
@Configured
public interface JdbcConnectionPool extends ConfigBeanProxy, Injectable, Resource, ResourcePool, PropertyBag {

    @Attribute(required = true)
    public String getDatasourceClassname();

    /**
     * Sets the value of the datasourceClassname property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDatasourceClassname(String value) throws PropertyVetoException;

    /**
     * Gets the value of the resType property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getResType();

    /**
     * Sets the value of the resType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the steadyPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="8", dataType=PositiveInteger.class)
    public String getSteadyPoolSize();

    /**
     * Sets the value of the steadyPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteadyPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="32", dataType=PositiveInteger.class)
    public String getMaxPoolSize();

    /**
     * Sets the value of the maxPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxWaitTimeInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="60000", dataType=NonNegativeInteger.class)
    public String getMaxWaitTimeInMillis();

    /**
     * Sets the value of the maxWaitTimeInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxWaitTimeInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the poolResizeQuantity property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="2", dataType=PositiveInteger.class)
    public String getPoolResizeQuantity();

    /**
     * Sets the value of the poolResizeQuantity property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPoolResizeQuantity(String value) throws PropertyVetoException;

    /**
     * Gets the value of the idleTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="300", dataType=NonNegativeInteger.class)
    public String getIdleTimeoutInSeconds();

    /**
     * Sets the value of the idleTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIdleTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the transactionIsolationLevel property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getTransactionIsolationLevel();

    /**
     * Sets the value of the transactionIsolationLevel property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTransactionIsolationLevel(String value) throws PropertyVetoException;

    /**
     * Gets the value of the isIsolationLevelGuaranteed property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    public String getIsIsolationLevelGuaranteed();

    /**
     * Sets the value of the isIsolationLevelGuaranteed property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIsIsolationLevelGuaranteed(String value) throws PropertyVetoException;

    /**
     * Gets the value of the isConnectionValidationRequired property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getIsConnectionValidationRequired();

    /**
     * Sets the value of the isConnectionValidationRequired property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIsConnectionValidationRequired(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectionValidationMethod property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="auto-commit")
    public String getConnectionValidationMethod();

    /**
     * Sets the value of the connectionValidationMethod property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionValidationMethod(String value) throws PropertyVetoException;

    /**
     * Gets the value of the validationTableName property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getValidationTableName();

    /**
     * Sets the value of the validationTableName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValidationTableName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the failAllConnections property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getFailAllConnections();

    /**
     * Sets the value of the failAllConnections property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFailAllConnections(String value) throws PropertyVetoException;

    /**
     * Gets the value of the nonTransactionalConnections property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getNonTransactionalConnections();

    /**
     * Sets the value of the nonTransactionalConnections property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNonTransactionalConnections(String value) throws PropertyVetoException;

    /**
     * Gets the value of the allowNonComponentCallers property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getAllowNonComponentCallers();

    /**
     * Sets the value of the allowNonComponentCallers property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAllowNonComponentCallers(String value) throws PropertyVetoException;

    /**
     * Gets the value of the validateAtmostOncePeriodInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="0", dataType=NonNegativeInteger.class)
    public String getValidateAtmostOncePeriodInSeconds();

    /**
     * Sets the value of the validateAtmostOncePeriodInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValidateAtmostOncePeriodInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectionLeakTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="0", dataType=NonNegativeInteger.class)
    public String getConnectionLeakTimeoutInSeconds();

    /**
     * Sets the value of the connectionLeakTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionLeakTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectionLeakReclaim property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getConnectionLeakReclaim();

    /**
     * Sets the value of the connectionLeakReclaim property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionLeakReclaim(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectionCreationRetryAttempts property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="0", dataType=NonNegativeInteger.class)
    public String getConnectionCreationRetryAttempts();

    /**
     * Sets the value of the connectionCreationRetryAttempts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionCreationRetryAttempts(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectionCreationRetryIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="10", dataType=NonNegativeInteger.class)
    public String getConnectionCreationRetryIntervalInSeconds();

    /**
     * Sets the value of the connectionCreationRetryIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectionCreationRetryIntervalInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the statementTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="-1", dataType=Integer.class)
    public String getStatementTimeoutInSeconds();

    /**
     * Sets the value of the statementTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatementTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the lazyConnectionEnlistment property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getLazyConnectionEnlistment();

    /**
     * Sets the value of the lazyConnectionEnlistment property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLazyConnectionEnlistment(String value) throws PropertyVetoException;

    /**
     * Gets the value of the lazyConnectionAssociation property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getLazyConnectionAssociation();

    /**
     * Sets the value of the lazyConnectionAssociation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLazyConnectionAssociation(String value) throws PropertyVetoException;

    /**
     * Gets the value of the associateWithThread property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getAssociateWithThread();

    /**
     * Sets the value of the associateWithThread property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAssociateWithThread(String value) throws PropertyVetoException;

    /**
     * Gets the value of the matchConnections property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getMatchConnections();

    /**
     * Sets the value of the matchConnections property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMatchConnections(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxConnectionUsageCount property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="0", dataType=NonNegativeInteger.class)
    public String getMaxConnectionUsageCount();

    /**
     * Sets the value of the maxConnectionUsageCount property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxConnectionUsageCount(String value) throws PropertyVetoException;

    /**
     * Gets the value of the wrapJdbcObjects property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getWrapJdbcObjects();

    /**
     * Sets the value of the wrapJdbcObjects property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWrapJdbcObjects(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     * 
     *              {@link String }
     */
    public void setDescription(String value) throws PropertyVetoException;
    
    /**
        Properties.  This list is likely incomplete as of 21 October 2008.
     */
@PropertiesDesc(
    props={
        @PropertyDesc(name="PortNumber", defaultValue="1527", dataType=Port.class,
            description="Port on which the database server listens for requests"),
            
        @PropertyDesc(name="Password", defaultValue="APP",
            description="Password for connecting to the database"),
            
        @PropertyDesc(name="User", defaultValue="APP",
            description="User name for connecting to the database"),
            
        @PropertyDesc(name="serverName", defaultValue="localhost",
            description="Database server for this connection pool"),
            
        @PropertyDesc(name="DatabaseName", defaultValue="sun-appserv-samples",
            description="Database for this connection pool."),
            
        @PropertyDesc(name="connectionAttributes", defaultValue=";create=true",
            description="connection attributes")
    }
    )
    @Element
    List<Property> getProperty();
}



