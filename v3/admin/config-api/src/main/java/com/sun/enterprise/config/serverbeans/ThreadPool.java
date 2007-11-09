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


/**
 *
 */

/* @XmlType(name = "") */
@Configured
public class ThreadPool
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String threadPoolId;
    @Attribute

    protected String minThreadPoolSize;
    @Attribute

    protected String maxThreadPoolSize;
    @Attribute

    protected String idleThreadTimeoutInSeconds;
    @Attribute

    protected String numWorkQueues;



    /**
     * Gets the value of the threadPoolId property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getThreadPoolId() {
        return threadPoolId;
    }

    /**
     * Sets the value of the threadPoolId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setThreadPoolId(String value) throws PropertyVetoException {
        support.fireVetoableChange("threadPoolId", this.threadPoolId, value);

        this.threadPoolId = value;
    }

    /**
     * Gets the value of the minThreadPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMinThreadPoolSize() {
        if (minThreadPoolSize == null) {
            return "0";
        } else {
            return minThreadPoolSize;
        }
    }

    /**
     * Sets the value of the minThreadPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMinThreadPoolSize(String value) throws PropertyVetoException {
        support.fireVetoableChange("minThreadPoolSize", this.minThreadPoolSize, value);

        this.minThreadPoolSize = value;
    }

    /**
     * Gets the value of the maxThreadPoolSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMaxThreadPoolSize() {
        if (maxThreadPoolSize == null) {
            return "200";
        } else {
            return maxThreadPoolSize;
        }
    }

    /**
     * Sets the value of the maxThreadPoolSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxThreadPoolSize(String value) throws PropertyVetoException {
        support.fireVetoableChange("maxThreadPoolSize", this.maxThreadPoolSize, value);

        this.maxThreadPoolSize = value;
    }

    /**
     * Gets the value of the idleThreadTimeoutInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getIdleThreadTimeoutInSeconds() {
        if (idleThreadTimeoutInSeconds == null) {
            return "120";
        } else {
            return idleThreadTimeoutInSeconds;
        }
    }

    /**
     * Sets the value of the idleThreadTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIdleThreadTimeoutInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("idleThreadTimeoutInSeconds", this.idleThreadTimeoutInSeconds, value);

        this.idleThreadTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the numWorkQueues property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getNumWorkQueues() {
        if (numWorkQueues == null) {
            return "1";
        } else {
            return numWorkQueues;
        }
    }

    /**
     * Sets the value of the numWorkQueues property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNumWorkQueues(String value) throws PropertyVetoException {
        support.fireVetoableChange("numWorkQueues", this.numWorkQueues, value);

        this.numWorkQueues = value;
    }



}
