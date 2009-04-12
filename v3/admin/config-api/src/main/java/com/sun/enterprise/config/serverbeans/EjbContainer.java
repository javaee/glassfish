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
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.amx.AMXConfigInfo;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;
import org.glassfish.quality.ToDo;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "ejbTimerService",
    "property"
}) */
@AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.EJBContainerConfig", singleton=true)
@Configured
public interface EjbContainer extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the steadyPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="32")
    String getSteadyPoolSize();

    /**
     * Sets the value of the steadyPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSteadyPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the poolResizeQuantity property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="16")
    String getPoolResizeQuantity();

    /**
     * Sets the value of the poolResizeQuantity property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPoolResizeQuantity(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="64")
    String getMaxPoolSize();

    /**
     * Sets the value of the maxPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMaxPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cacheResizeQuantity property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="32")
    String getCacheResizeQuantity();

    /**
     * Sets the value of the cacheResizeQuantity property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setCacheResizeQuantity(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxCacheSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="512")
    String getMaxCacheSize();

    /**
     * Sets the value of the maxCacheSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMaxCacheSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the poolIdleTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="600")
    String getPoolIdleTimeoutInSeconds();

    /**
     * Sets the value of the poolIdleTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPoolIdleTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cacheIdleTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="600")
    String getCacheIdleTimeoutInSeconds();

    /**
     * Sets the value of the cacheIdleTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setCacheIdleTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the removalTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="5400")
    String getRemovalTimeoutInSeconds();

    /**
     * Sets the value of the removalTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setRemovalTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the victimSelectionPolicy property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="nru")
    String getVictimSelectionPolicy();

    /**
     * Sets the value of the victimSelectionPolicy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setVictimSelectionPolicy(String value) throws PropertyVetoException;

    /**
     * Gets the value of the commitOption property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="B")
    String getCommitOption();

    /**
     * Sets the value of the commitOption property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setCommitOption(String value) throws PropertyVetoException;

    /**
     * Gets the value of the sessionStore property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getSessionStore();

    /**
     * Sets the value of the sessionStore property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSessionStore(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ejbTimerService property.
     *
     * @return possible object is
     *         {@link EjbTimerService }
     */
    @Element
    EjbTimerService getEjbTimerService();

    /**
     * Sets the value of the ejbTimerService property.
     *
     * @param value allowed object is
     *              {@link EjbTimerService }
     */
    void setEjbTimerService(EjbTimerService value) throws PropertyVetoException;
    
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
