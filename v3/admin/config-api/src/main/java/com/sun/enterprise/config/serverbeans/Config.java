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
    "httpService",
    "iiopService",
    "adminService",
    "connectorService",
    "webContainer",
    "ejbContainer",
    "mdbContainer",
    "jmsService",
    "logService",
    "securityService",
    "transactionService",
    "monitoringService",
    "diagnosticService",
    "javaConfig",
    "availabilityService",
    "threadPools",
    "alertService",
    "groupManagementService",
    "managementRules",
    "systemProperty",
    "property"
}) */
@Configured
public class Config
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String name;
    @Attribute

    protected String dynamicReconfigurationEnabled;
    @Element(required=true)
    protected HttpService httpService;
    @Element(required=true)
    protected IiopService iiopService;
    @Element(required=true)
    protected AdminService adminService;
    @Element
    protected ConnectorService connectorService;
    @Element(required=true)
    protected WebContainer webContainer;
    @Element(required=true)
    protected EjbContainer ejbContainer;
    @Element(required=true)
    protected MdbContainer mdbContainer;
    @Element
    protected JmsService jmsService;
    @Element(required=true)
    protected LogService logService;
    @Element(required=true)
    protected SecurityService securityService;
    @Element(required=true)
    protected TransactionService transactionService;
    @Element(required=true)
    protected MonitoringService monitoringService;
    @Element
    protected DiagnosticService diagnosticService;
    @Element(required=true)
    protected JavaConfig javaConfig;
    @Element
    protected AvailabilityService availabilityService;
    @Element(required=true)
    protected ThreadPools threadPools;
    @Element
    protected AlertService alertService;
    @Element
    protected GroupManagementService groupManagementService;
    @Element
    protected ManagementRules managementRules;
    @Element
    protected List<SystemProperty> systemProperty = new ConstrainedList<SystemProperty>(this, "systemProperty", support);
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException {
        support.fireVetoableChange("name", this.name, value);

        this.name = value;
    }

    /**
     * Gets the value of the dynamicReconfigurationEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDynamicReconfigurationEnabled() {
        if (dynamicReconfigurationEnabled == null) {
            return "true";
        } else {
            return dynamicReconfigurationEnabled;
        }
    }

    /**
     * Sets the value of the dynamicReconfigurationEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDynamicReconfigurationEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("dynamicReconfigurationEnabled", this.dynamicReconfigurationEnabled, value);

        this.dynamicReconfigurationEnabled = value;
    }

    /**
     * Gets the value of the httpService property.
     *
     * @return possible object is
     *         {@link HttpService }
     */
    public HttpService getHttpService() {
        return httpService;
    }

    /**
     * Sets the value of the httpService property.
     *
     * @param value allowed object is
     *              {@link HttpService }
     */
    public void setHttpService(HttpService value) throws PropertyVetoException {
        support.fireVetoableChange("httpService", this.httpService, value);

        this.httpService = value;
    }

    /**
     * Gets the value of the iiopService property.
     *
     * @return possible object is
     *         {@link IiopService }
     */
    public IiopService getIiopService() {
        return iiopService;
    }

    /**
     * Sets the value of the iiopService property.
     *
     * @param value allowed object is
     *              {@link IiopService }
     */
    public void setIiopService(IiopService value) throws PropertyVetoException {
        support.fireVetoableChange("iiopService", this.iiopService, value);

        this.iiopService = value;
    }

    /**
     * Gets the value of the adminService property.
     *
     * @return possible object is
     *         {@link AdminService }
     */
    public AdminService getAdminService() {
        return adminService;
    }

    /**
     * Sets the value of the adminService property.
     *
     * @param value allowed object is
     *              {@link AdminService }
     */
    public void setAdminService(AdminService value) throws PropertyVetoException {
        support.fireVetoableChange("adminService", this.adminService, value);

        this.adminService = value;
    }

    /**
     * Gets the value of the connectorService property.
     *
     * @return possible object is
     *         {@link ConnectorService }
     */
    public ConnectorService getConnectorService() {
        return connectorService;
    }

    /**
     * Sets the value of the connectorService property.
     *
     * @param value allowed object is
     *              {@link ConnectorService }
     */
    public void setConnectorService(ConnectorService value) throws PropertyVetoException {
        support.fireVetoableChange("connectorService", this.connectorService, value);

        this.connectorService = value;
    }

    /**
     * Gets the value of the webContainer property.
     *
     * @return possible object is
     *         {@link WebContainer }
     */
    public WebContainer getWebContainer() {
        return webContainer;
    }

    /**
     * Sets the value of the webContainer property.
     *
     * @param value allowed object is
     *              {@link WebContainer }
     */
    public void setWebContainer(WebContainer value) throws PropertyVetoException {
        support.fireVetoableChange("webContainer", this.webContainer, value);

        this.webContainer = value;
    }

    /**
     * Gets the value of the ejbContainer property.
     *
     * @return possible object is
     *         {@link EjbContainer }
     */
    public EjbContainer getEjbContainer() {
        return ejbContainer;
    }

    /**
     * Sets the value of the ejbContainer property.
     *
     * @param value allowed object is
     *              {@link EjbContainer }
     */
    public void setEjbContainer(EjbContainer value) throws PropertyVetoException {
        support.fireVetoableChange("ejbContainer", this.ejbContainer, value);

        this.ejbContainer = value;
    }

    /**
     * Gets the value of the mdbContainer property.
     *
     * @return possible object is
     *         {@link MdbContainer }
     */
    public MdbContainer getMdbContainer() {
        return mdbContainer;
    }

    /**
     * Sets the value of the mdbContainer property.
     *
     * @param value allowed object is
     *              {@link MdbContainer }
     */
    public void setMdbContainer(MdbContainer value) throws PropertyVetoException {
        support.fireVetoableChange("mdbContainer", this.mdbContainer, value);

        this.mdbContainer = value;
    }

    /**
     * Gets the value of the jmsService property.
     *
     * @return possible object is
     *         {@link JmsService }
     */
    public JmsService getJmsService() {
        return jmsService;
    }

    /**
     * Sets the value of the jmsService property.
     *
     * @param value allowed object is
     *              {@link JmsService }
     */
    public void setJmsService(JmsService value) throws PropertyVetoException {
        support.fireVetoableChange("jmsService", this.jmsService, value);

        this.jmsService = value;
    }

    /**
     * Gets the value of the logService property.
     *
     * @return possible object is
     *         {@link LogService }
     */
    public LogService getLogService() {
        return logService;
    }

    /**
     * Sets the value of the logService property.
     *
     * @param value allowed object is
     *              {@link LogService }
     */
    public void setLogService(LogService value) throws PropertyVetoException {
        support.fireVetoableChange("logService", this.logService, value);

        this.logService = value;
    }

    /**
     * Gets the value of the securityService property.
     *
     * @return possible object is
     *         {@link SecurityService }
     */
    public SecurityService getSecurityService() {
        return securityService;
    }

    /**
     * Sets the value of the securityService property.
     *
     * @param value allowed object is
     *              {@link SecurityService }
     */
    public void setSecurityService(SecurityService value) throws PropertyVetoException {
        support.fireVetoableChange("securityService", this.securityService, value);

        this.securityService = value;
    }

    /**
     * Gets the value of the transactionService property.
     *
     * @return possible object is
     *         {@link TransactionService }
     */
    public TransactionService getTransactionService() {
        return transactionService;
    }

    /**
     * Sets the value of the transactionService property.
     *
     * @param value allowed object is
     *              {@link TransactionService }
     */
    public void setTransactionService(TransactionService value) throws PropertyVetoException {
        support.fireVetoableChange("transactionService", this.transactionService, value);

        this.transactionService = value;
    }

    /**
     * Gets the value of the monitoringService property.
     *
     * @return possible object is
     *         {@link MonitoringService }
     */
    public MonitoringService getMonitoringService() {
        return monitoringService;
    }

    /**
     * Sets the value of the monitoringService property.
     *
     * @param value allowed object is
     *              {@link MonitoringService }
     */
    public void setMonitoringService(MonitoringService value) throws PropertyVetoException {
        support.fireVetoableChange("monitoringService", this.monitoringService, value);

        this.monitoringService = value;
    }

    /**
     * Gets the value of the diagnosticService property.
     *
     * @return possible object is
     *         {@link DiagnosticService }
     */
    public DiagnosticService getDiagnosticService() {
        return diagnosticService;
    }

    /**
     * Sets the value of the diagnosticService property.
     *
     * @param value allowed object is
     *              {@link DiagnosticService }
     */
    public void setDiagnosticService(DiagnosticService value) throws PropertyVetoException {
        support.fireVetoableChange("diagnosticService", this.diagnosticService, value);

        this.diagnosticService = value;
    }

    /**
     * Gets the value of the javaConfig property.
     *
     * @return possible object is
     *         {@link JavaConfig }
     */
    public JavaConfig getJavaConfig() {
        return javaConfig;
    }

    /**
     * Sets the value of the javaConfig property.
     *
     * @param value allowed object is
     *              {@link JavaConfig }
     */
    public void setJavaConfig(JavaConfig value) throws PropertyVetoException {
        support.fireVetoableChange("javaConfig", this.javaConfig, value);

        this.javaConfig = value;
    }

    /**
     * Gets the value of the availabilityService property.
     *
     * @return possible object is
     *         {@link AvailabilityService }
     */
    public AvailabilityService getAvailabilityService() {
        return availabilityService;
    }

    /**
     * Sets the value of the availabilityService property.
     *
     * @param value allowed object is
     *              {@link AvailabilityService }
     */
    public void setAvailabilityService(AvailabilityService value) throws PropertyVetoException {
        support.fireVetoableChange("availabilityService", this.availabilityService, value);

        this.availabilityService = value;
    }

    /**
     * Gets the value of the threadPools property.
     *
     * @return possible object is
     *         {@link ThreadPools }
     */
    public ThreadPools getThreadPools() {
        return threadPools;
    }

    /**
     * Sets the value of the threadPools property.
     *
     * @param value allowed object is
     *              {@link ThreadPools }
     */
    public void setThreadPools(ThreadPools value) throws PropertyVetoException {
        support.fireVetoableChange("threadPools", this.threadPools, value);

        this.threadPools = value;
    }

    /**
     * Gets the value of the alertService property.
     *
     * @return possible object is
     *         {@link AlertService }
     */
    public AlertService getAlertService() {
        return alertService;
    }

    /**
     * Sets the value of the alertService property.
     *
     * @param value allowed object is
     *              {@link AlertService }
     */
    public void setAlertService(AlertService value) throws PropertyVetoException {
        support.fireVetoableChange("alertService", this.alertService, value);

        this.alertService = value;
    }

    /**
     * Gets the value of the groupManagementService property.
     *
     * @return possible object is
     *         {@link GroupManagementService }
     */
    public GroupManagementService getGroupManagementService() {
        return groupManagementService;
    }

    /**
     * Sets the value of the groupManagementService property.
     *
     * @param value allowed object is
     *              {@link GroupManagementService }
     */
    public void setGroupManagementService(GroupManagementService value) throws PropertyVetoException {
        support.fireVetoableChange("groupManagementService", this.groupManagementService, value);

        this.groupManagementService = value;
    }

    /**
     * Gets the value of the managementRules property.
     *
     * @return possible object is
     *         {@link ManagementRules }
     */
    public ManagementRules getManagementRules() {
        return managementRules;
    }

    /**
     * Sets the value of the managementRules property.
     *
     * @param value allowed object is
     *              {@link ManagementRules }
     */
    public void setManagementRules(ManagementRules value) throws PropertyVetoException {
        support.fireVetoableChange("managementRules", this.managementRules, value);

        this.managementRules = value;
    }

    /**
     * Gets the value of the systemProperty property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the systemProperty property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSystemProperty().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link SystemProperty }
     */
    public List<SystemProperty> getSystemProperty() {
        return this.systemProperty;
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
