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
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.Element;
import java.util.List;

import java.beans.PropertyVetoException;
import java.io.Serializable;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;

/**
 *
 */

/* @XmlType(name = "") */
@org.glassfish.api.amx.AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.ThreadPoolConfig")
@Configured
public interface ThreadPool extends ConfigBeanProxy, PropertyBag, Injectable  {

    /**
     * Gets the value of the threadPoolId property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true, key=true)
    public String getThreadPoolId();

    /**
     * Sets the value of the threadPoolId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setThreadPoolId(String value) throws PropertyVetoException;

    /**
     * Gets the value of the minThreadPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="0")
    public String getMinThreadPoolSize();

    /**
     * Sets the value of the minThreadPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMinThreadPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxThreadPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="200")
    public String getMaxThreadPoolSize();

    /**
     * Sets the value of the maxThreadPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxThreadPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the threadIncrement property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="10")
    public String getThreadIncrement();

    /**
     * Sets the value of the threadIncrement property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setThreadIncrement(String value);

        /**
     * Gets the value of the idleThreadTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="120")
    public String getIdleThreadTimeoutInSeconds();

    /**
     * Sets the value of the idleThreadTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIdleThreadTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the numWorkQueues property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1")
    public String getNumWorkQueues();

    /**
     * Sets the value of the numWorkQueues property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNumWorkQueues(String value) throws PropertyVetoException;

    /**
     * Gets the max number of tasks, which could be queued on the thread pool.
     *
     * @return the max number of tasks, which could be queued on the thread pool.
     *         possible object is {@link String }
     */
    @Attribute (defaultValue="-1")
    public String	getMaxQueueSize();

    /**
     * Gets the max number of tasks, which could be queued on the thread pool.
     *
     * @param value the max number of tasks, which could be queued on the
     *        thread pool.
     */
    public void	setMaxQueueSize( String value );

    /**
     * Gets the custom {@link ThreadPool} implementation class name
     *
     * @return {@link ThreadPool} implementation class name
     */
    @Attribute
    public String getClassname();

    /**
     * Sets the custom {@link ThreadPool} implementation class name
     *
     * @param classname {@link ThreadPool} implementation class name
     */
    public void setClassname(String classname);

    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Override
    @Element
    List<Property> getProperty();
}
