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

import org.glassfish.api.amx.AMXConfigInfo;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/* @XmlType(name = "", propOrder = {
    "moduleLogLevels",
    "property"
}) */
@AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.LogServiceConfig", singleton=true)
@Configured
public interface LogService extends ConfigBeanProxy, Injectable  {

    /**
     * Gets the value of the file property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getFile();

    /**
     * Sets the value of the file property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFile(String value) throws PropertyVetoException;

    /**
     * Gets the value of the useSystemLogging property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false")
    public String getUseSystemLogging();

    /**
     * Sets the value of the useSystemLogging property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUseSystemLogging(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logHandler property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getLogHandler();

    /**
     * Sets the value of the logHandler property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogHandler(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logFilter property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getLogFilter();

    /**
     * Sets the value of the logFilter property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogFilter(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logToConsole property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false")
    public String getLogToConsole();

    /**
     * Sets the value of the logToConsole property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogToConsole(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logRotationLimitInBytes property.
     *
     * @return possible object is
     *         {@link String }
     */
//    @Attribute (defaultValue="500000")
    @Attribute (defaultValue="0")
    @Min(value=1)
    public String getLogRotationLimitInBytes();

    /**
     * Sets the value of the logRotationLimitInBytes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogRotationLimitInBytes(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logRotationTimelimitInMinutes property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="0")
    @Min(value=0)
    @Max(value=14400)
    public String getLogRotationTimelimitInMinutes();

    /**
     * Sets the value of the logRotationTimelimitInMinutes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogRotationTimelimitInMinutes(String value) throws PropertyVetoException;

    /**
     * Gets the value of the alarms property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false")
    public String getAlarms();

    /**
     * Sets the value of the alarms property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAlarms(String value) throws PropertyVetoException;

    /**
     * Gets the value of the retainErrorStatisticsForHours property.
     *
     * @return possible object is
     *         {@link String }
     */
//    @Attribute (defaultValue="5")
    @Attribute (defaultValue="0")
    @Min(value=5)
    @Max(value=500)
    public String getRetainErrorStatisticsForHours();

    /**
     * Sets the value of the retainErrorStatisticsForHours property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRetainErrorStatisticsForHours(String value) throws PropertyVetoException;

    /**
     * Gets the value of the moduleLogLevels property.
     *
     * @return possible object is
     *         {@link ModuleLogLevels }
     */
    @Element
    public ModuleLogLevels getModuleLogLevels();

    /**
     * Sets the value of the moduleLogLevels property.
     *
     * @param value allowed object is
     *              {@link ModuleLogLevels }
     */
    public void setModuleLogLevels(ModuleLogLevels value) throws PropertyVetoException;




}
