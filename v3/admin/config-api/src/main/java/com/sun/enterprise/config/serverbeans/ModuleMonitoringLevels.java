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

import java.beans.PropertyVetoException;
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */
@org.glassfish.admin.amx.AMXConfigInfo( amxInterface=com.sun.appserv.management.config.ModuleMonitoringLevelsConfig.class, singleton=true)
@Configured
public interface ModuleMonitoringLevels extends ConfigBeanProxy, Injectable  {

    /**
     * Gets the value of the threadPool property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getThreadPool();

    /**
     * Sets the value of the threadPool property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setThreadPool(String value) throws PropertyVetoException;

    /**
     * Gets the value of the orb property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getOrb();

    /**
     * Sets the value of the orb property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setOrb(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ejbContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getEjbContainer();

    /**
     * Sets the value of the ejbContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the webContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getWebContainer();

    /**
     * Sets the value of the webContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWebContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the transactionService property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getTransactionService();

    /**
     * Sets the value of the transactionService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTransactionService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the httpService property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getHttpService();

    /**
     * Sets the value of the httpService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHttpService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jdbcConnectionPool property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJdbcConnectionPool();

    /**
     * Sets the value of the jdbcConnectionPool property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJdbcConnectionPool(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectorConnectionPool property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getConnectorConnectionPool();

    /**
     * Sets the value of the connectorConnectionPool property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectorConnectionPool(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectorService property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getConnectorService();

    /**
     * Sets the value of the connectorService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnectorService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jmsService property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJmsService();

    /**
     * Sets the value of the jmsService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJmsService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jvm property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJvm();

    /**
     * Sets the value of the jvm property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJvm(String value) throws PropertyVetoException;

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
    @Element("property")
    public List<Property> getProperty();



}
