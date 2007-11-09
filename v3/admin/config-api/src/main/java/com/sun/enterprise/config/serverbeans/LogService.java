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
import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "moduleLogLevels",
    "property"
}) */
@Configured
public class LogService
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String file;
    @Attribute

    protected String useSystemLogging;
    @Attribute

    protected String logHandler;
    @Attribute

    protected String logFilter;
    @Attribute

    protected String logToConsole;
    @Attribute

    protected String logRotationLimitInBytes;
    @Attribute

    protected String logRotationTimelimitInMinutes;
    @Attribute

    protected String alarms;
    @Attribute

    protected String retainErrorStatisticsForHours;
    @Element
    protected ModuleLogLevels moduleLogLevels;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the file property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFile(String value) throws PropertyVetoException {
        support.fireVetoableChange("file", this.file, value);

        this.file = value;
    }

    /**
     * Gets the value of the useSystemLogging property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getUseSystemLogging() {
        if (useSystemLogging == null) {
            return "false";
        } else {
            return useSystemLogging;
        }
    }

    /**
     * Sets the value of the useSystemLogging property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUseSystemLogging(String value) throws PropertyVetoException {
        support.fireVetoableChange("useSystemLogging", this.useSystemLogging, value);

        this.useSystemLogging = value;
    }

    /**
     * Gets the value of the logHandler property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLogHandler() {
        return logHandler;
    }

    /**
     * Sets the value of the logHandler property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogHandler(String value) throws PropertyVetoException {
        support.fireVetoableChange("logHandler", this.logHandler, value);

        this.logHandler = value;
    }

    /**
     * Gets the value of the logFilter property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLogFilter() {
        return logFilter;
    }

    /**
     * Sets the value of the logFilter property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogFilter(String value) throws PropertyVetoException {
        support.fireVetoableChange("logFilter", this.logFilter, value);

        this.logFilter = value;
    }

    /**
     * Gets the value of the logToConsole property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLogToConsole() {
        if (logToConsole == null) {
            return "false";
        } else {
            return logToConsole;
        }
    }

    /**
     * Sets the value of the logToConsole property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogToConsole(String value) throws PropertyVetoException {
        support.fireVetoableChange("logToConsole", this.logToConsole, value);

        this.logToConsole = value;
    }

    /**
     * Gets the value of the logRotationLimitInBytes property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLogRotationLimitInBytes() {
        if (logRotationLimitInBytes == null) {
            return "500000";
        } else {
            return logRotationLimitInBytes;
        }
    }

    /**
     * Sets the value of the logRotationLimitInBytes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogRotationLimitInBytes(String value) throws PropertyVetoException {
        support.fireVetoableChange("logRotationLimitInBytes", this.logRotationLimitInBytes, value);

        this.logRotationLimitInBytes = value;
    }

    /**
     * Gets the value of the logRotationTimelimitInMinutes property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLogRotationTimelimitInMinutes() {
        if (logRotationTimelimitInMinutes == null) {
            return "0";
        } else {
            return logRotationTimelimitInMinutes;
        }
    }

    /**
     * Sets the value of the logRotationTimelimitInMinutes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogRotationTimelimitInMinutes(String value) throws PropertyVetoException {
        support.fireVetoableChange("logRotationTimelimitInMinutes", this.logRotationTimelimitInMinutes, value);

        this.logRotationTimelimitInMinutes = value;
    }

    /**
     * Gets the value of the alarms property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAlarms() {
        if (alarms == null) {
            return "false";
        } else {
            return alarms;
        }
    }

    /**
     * Sets the value of the alarms property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAlarms(String value) throws PropertyVetoException {
        support.fireVetoableChange("alarms", this.alarms, value);

        this.alarms = value;
    }

    /**
     * Gets the value of the retainErrorStatisticsForHours property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getRetainErrorStatisticsForHours() {
        if (retainErrorStatisticsForHours == null) {
            return "5";
        } else {
            return retainErrorStatisticsForHours;
        }
    }

    /**
     * Sets the value of the retainErrorStatisticsForHours property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRetainErrorStatisticsForHours(String value) throws PropertyVetoException {
        support.fireVetoableChange("retainErrorStatisticsForHours", this.retainErrorStatisticsForHours, value);

        this.retainErrorStatisticsForHours = value;
    }

    /**
     * Gets the value of the moduleLogLevels property.
     *
     * @return possible object is
     *         {@link ModuleLogLevels }
     */
    public ModuleLogLevels getModuleLogLevels() {
        return moduleLogLevels;
    }

    /**
     * Sets the value of the moduleLogLevels property.
     *
     * @param value allowed object is
     *              {@link ModuleLogLevels }
     */
    public void setModuleLogLevels(ModuleLogLevels value) throws PropertyVetoException {
        support.fireVetoableChange("moduleLogLevels", this.moduleLogLevels, value);

        this.moduleLogLevels = value;
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
