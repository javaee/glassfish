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
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.List;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "clusterRefOrServerRef",
    "property"
}) */
@org.glassfish.api.amx.AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.LBConfig")
@Configured
public interface LbConfig extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true, key=true)
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the responseTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="60")
    public String getResponseTimeoutInSeconds();

    /**
     * Sets the value of the responseTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResponseTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the httpsRouting property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false")
    public String getHttpsRouting();

    /**
     * Sets the value of the httpsRouting property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHttpsRouting(String value) throws PropertyVetoException;

    /**
     * Gets the value of the reloadPollIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="60")
    public String getReloadPollIntervalInSeconds();

    /**
     * Sets the value of the reloadPollIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReloadPollIntervalInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the monitoringEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false")
    public String getMonitoringEnabled();

    /**
     * Sets the value of the monitoringEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMonitoringEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the routeCookieEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true")
    public String getRouteCookieEnabled();

    /**
     * Sets the value of the routeCookieEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRouteCookieEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the clusterRefOrServerRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clusterRefOrServerRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClusterRefOrServerRef().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ClusterRef }
     * {@link ServerRef }
     */
    @Element("*")    
    public List<Ref> getClusterRefOrServerRef();
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Override
    @Element
    List<Property> getProperty();
}
