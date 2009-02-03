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
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import java.util.List;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.amx.AMXConfigInfo;

import org.glassfish.config.support.datatypes.PositiveInteger;
import org.glassfish.config.support.datatypes.NonNegativeInteger;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;

import org.glassfish.quality.ToDo;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */
@AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.TransactionServiceConfig", singleton=true)
@Configured
public interface TransactionService extends ConfigBeanProxy, Injectable, PropertyBag  {

    /**
     * Gets the value of the automaticRecovery property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false")
    public String getAutomaticRecovery();

    /**
     * Sets the value of the automaticRecovery property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAutomaticRecovery(String value) throws PropertyVetoException;

    /**
     * Gets the value of the timeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="0")
    public String getTimeoutInSeconds();

    /**
     * Sets the value of the timeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the txLogDir property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getTxLogDir();

    /**
     * Sets the value of the txLogDir property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTxLogDir(String value) throws PropertyVetoException;

    /**
     * Gets the value of the heuristicDecision property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="rollback")
    public String getHeuristicDecision();

    /**
     * Sets the value of the heuristicDecision property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHeuristicDecision(String value) throws PropertyVetoException;

    /**
     * Gets the value of the retryTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="600")
    public String getRetryTimeoutInSeconds();

    /**
     * Sets the value of the retryTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRetryTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the keypointInterval property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="2048")
    public String getKeypointInterval();

    /**
     * Sets the value of the keypointInterval property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setKeypointInterval(String value) throws PropertyVetoException;
    
    

   /**
        Properties.
     */
@PropertiesDesc(
    props={
        @PropertyDesc(name="oracle-xa-recovery-workaround", defaultValue="true", dataType=Boolean.class,
            description="If true, the Oracle XA Resource workaround is used in transaction recovery"),
            
        @PropertyDesc(name="disable-distributed-transaction-logging", defaultValue="false", dataType=Boolean.class,
            description="If true, disables transaction logging, which might improve performance. " +
                "If the automatic-recovery attribute is set to true , this property is ignored"),
            
        @PropertyDesc(name="xaresource-txn-timeout", defaultValue="120", dataType=PositiveInteger.class,
            description=" Changes the XAResource timeout. In some cases, the XAResource default timeout can cause " +
                "transactions to be aborted, so it is desirable to change it"),
            
        @PropertyDesc(name="pending-txn-cleanup-interval", defaultValue="60", dataType=PositiveInteger.class,
            description="Interval in seconds at which an asynchronous thread checks for pending transactions and completes them"),
            
        @PropertyDesc(name="use-last-agent-optimization", defaultValue="true", dataType=Boolean.class,
            description="Enables last agent optimization, which improves the throughput of transactions. " +
                "If one non-XA resource is used with XA resources in the same transaction, the non XA resource is the last agent"),
            
        @PropertyDesc(name="wait-time-before-recovery-insec", defaultValue="60", dataType=PositiveInteger.class,
            description="Wait time in seconds after which an instance starts the recovery for a dead instance"),
            
        @PropertyDesc(name="db-logging-resource",
            description="db-logging-resource NDI name of the JDBC resource for the database to which transactions are logged")
    }
    )
    @Element
    List<Property> getProperty();
}
