/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.concurrent.config;

import org.jvnet.hk2.config.*;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;

import javax.validation.constraints.Min;
import javax.validation.Payload;
import java.beans.PropertyVetoException;

/**
 * Concurrency managed executor service resource base definition
 */

@Configured
public interface ManagedExecutorServiceBase extends ConfigBeanProxy,
        Resource, BindableResource, Payload, ConcurrencyResource {

    /**
     * Gets the value of the threadPriority property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue=""+Thread.NORM_PRIORITY, dataType=Integer.class)
    @Min(value=0)
    String getThreadPriority();


    /**
     * Sets the value of the threadPriority property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setThreadPriority(String value) throws PropertyVetoException;

    /**
     * Gets the value of the longRunningTasks property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue="false", dataType=Boolean.class)
    String getLongRunningTasks();

    /**
     * Sets the value of the longRunningTasks property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setLongRunningTasks(String value) throws PropertyVetoException;
    
    /**
     * Gets the value of the hungAfterSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value=0)
    String getHungAfterSeconds();

    /**
     * Sets the value of the hungAfterSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setHungAfterSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the corePoolSize property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value=0)
    String getCorePoolSize();

    /**
     * Sets the value of the coreSize property.
     *
     * @param value allowed object is {@link String }
     */
    void setCorePoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the keepAlivesSeconds property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "60", dataType = Integer.class)
    @Min(value=0)
    String getKeepAliveSeconds();

    /**
     * Sets the value of the keepAliveSeconds property.
     *
     * @param value allowed object is {@link String }
     */
    void setKeepAliveSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the threadLifetimeSeconds property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value=0)
    String getThreadLifetimeSeconds();

    /**
     * Sets the value of the threadLifetimeSeconds property.
     *
     * @param value allowed object is {@link String }
     */
    void setThreadLifetimeSeconds(String value) throws PropertyVetoException;
}
