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
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */
@Configured
public class ModuleLogLevels
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String root;
    @Attribute

    protected String server;
    @Attribute

    protected String ejbContainer;
    @Attribute

    protected String cmpContainer;
    @Attribute

    protected String mdbContainer;
    @Attribute

    protected String webContainer;
    @Attribute

    protected String classloader;
    @Attribute

    protected String configuration;
    @Attribute

    protected String naming;
    @Attribute

    protected String security;
    @Attribute

    protected String jts;
    @Attribute

    protected String jta;
    @Attribute

    protected String admin;
    @Attribute

    protected String deployment;
    @Attribute

    protected String verifier;
    @Attribute

    protected String jaxr;
    @Attribute

    protected String jaxrpc;
    @Attribute

    protected String saaj;
    @Attribute

    protected String corba;
    @Attribute

    protected String javamail;
    @Attribute

    protected String jms;
    @Attribute

    protected String connector;
    @Attribute

    protected String jdo;
    @Attribute

    protected String cmp;
    @Attribute

    protected String util;
    @Attribute

    protected String resourceAdapter;
    @Attribute

    protected String synchronization;
    @Attribute

    protected String nodeAgent;
    @Attribute

    protected String selfManagement;
    @Attribute

    protected String groupManagementService;
    @Attribute

    protected String managementEvent;
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the root property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getRoot() {
        if (root == null) {
            return "INFO";
        } else {
            return root;
        }
    }

    /**
     * Sets the value of the root property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRoot(String value) throws PropertyVetoException {
        support.fireVetoableChange("root", this.root, value);

        this.root = value;
    }

    /**
     * Gets the value of the server property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getServer() {
        if (server == null) {
            return "INFO";
        } else {
            return server;
        }
    }

    /**
     * Sets the value of the server property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServer(String value) throws PropertyVetoException {
        support.fireVetoableChange("server", this.server, value);

        this.server = value;
    }

    /**
     * Gets the value of the ejbContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getEjbContainer() {
        if (ejbContainer == null) {
            return "INFO";
        } else {
            return ejbContainer;
        }
    }

    /**
     * Sets the value of the ejbContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbContainer(String value) throws PropertyVetoException {
        support.fireVetoableChange("ejbContainer", this.ejbContainer, value);

        this.ejbContainer = value;
    }

    /**
     * Gets the value of the cmpContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCmpContainer() {
        if (cmpContainer == null) {
            return "INFO";
        } else {
            return cmpContainer;
        }
    }

    /**
     * Sets the value of the cmpContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCmpContainer(String value) throws PropertyVetoException {
        support.fireVetoableChange("cmpContainer", this.cmpContainer, value);

        this.cmpContainer = value;
    }

    /**
     * Gets the value of the mdbContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMdbContainer() {
        if (mdbContainer == null) {
            return "INFO";
        } else {
            return mdbContainer;
        }
    }

    /**
     * Sets the value of the mdbContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMdbContainer(String value) throws PropertyVetoException {
        support.fireVetoableChange("mdbContainer", this.mdbContainer, value);

        this.mdbContainer = value;
    }

    /**
     * Gets the value of the webContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getWebContainer() {
        if (webContainer == null) {
            return "INFO";
        } else {
            return webContainer;
        }
    }

    /**
     * Sets the value of the webContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWebContainer(String value) throws PropertyVetoException {
        support.fireVetoableChange("webContainer", this.webContainer, value);

        this.webContainer = value;
    }

    /**
     * Gets the value of the classloader property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getClassloader() {
        if (classloader == null) {
            return "INFO";
        } else {
            return classloader;
        }
    }

    /**
     * Sets the value of the classloader property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClassloader(String value) throws PropertyVetoException {
        support.fireVetoableChange("classloader", this.classloader, value);

        this.classloader = value;
    }

    /**
     * Gets the value of the configuration property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConfiguration() {
        if (configuration == null) {
            return "INFO";
        } else {
            return configuration;
        }
    }

    /**
     * Sets the value of the configuration property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConfiguration(String value) throws PropertyVetoException {
        support.fireVetoableChange("configuration", this.configuration, value);

        this.configuration = value;
    }

    /**
     * Gets the value of the naming property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getNaming() {
        if (naming == null) {
            return "INFO";
        } else {
            return naming;
        }
    }

    /**
     * Sets the value of the naming property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNaming(String value) throws PropertyVetoException {
        support.fireVetoableChange("naming", this.naming, value);

        this.naming = value;
    }

    /**
     * Gets the value of the security property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSecurity() {
        if (security == null) {
            return "INFO";
        } else {
            return security;
        }
    }

    /**
     * Sets the value of the security property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSecurity(String value) throws PropertyVetoException {
        support.fireVetoableChange("security", this.security, value);

        this.security = value;
    }

    /**
     * Gets the value of the jts property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJts() {
        if (jts == null) {
            return "INFO";
        } else {
            return jts;
        }
    }

    /**
     * Sets the value of the jts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJts(String value) throws PropertyVetoException {
        support.fireVetoableChange("jts", this.jts, value);

        this.jts = value;
    }

    /**
     * Gets the value of the jta property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJta() {
        if (jta == null) {
            return "INFO";
        } else {
            return jta;
        }
    }

    /**
     * Sets the value of the jta property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJta(String value) throws PropertyVetoException {
        support.fireVetoableChange("jta", this.jta, value);

        this.jta = value;
    }

    /**
     * Gets the value of the admin property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getAdmin() {
        if (admin == null) {
            return "INFO";
        } else {
            return admin;
        }
    }

    /**
     * Sets the value of the admin property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdmin(String value) throws PropertyVetoException {
        support.fireVetoableChange("admin", this.admin, value);

        this.admin = value;
    }

    /**
     * Gets the value of the deployment property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDeployment() {
        if (deployment == null) {
            return "INFO";
        } else {
            return deployment;
        }
    }

    /**
     * Sets the value of the deployment property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDeployment(String value) throws PropertyVetoException {
        support.fireVetoableChange("deployment", this.deployment, value);

        this.deployment = value;
    }

    /**
     * Gets the value of the verifier property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getVerifier() {
        if (verifier == null) {
            return "INFO";
        } else {
            return verifier;
        }
    }

    /**
     * Sets the value of the verifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVerifier(String value) throws PropertyVetoException {
        support.fireVetoableChange("verifier", this.verifier, value);

        this.verifier = value;
    }

    /**
     * Gets the value of the jaxr property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJaxr() {
        if (jaxr == null) {
            return "INFO";
        } else {
            return jaxr;
        }
    }

    /**
     * Sets the value of the jaxr property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJaxr(String value) throws PropertyVetoException {
        support.fireVetoableChange("jaxr", this.jaxr, value);

        this.jaxr = value;
    }

    /**
     * Gets the value of the jaxrpc property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJaxrpc() {
        if (jaxrpc == null) {
            return "INFO";
        } else {
            return jaxrpc;
        }
    }

    /**
     * Sets the value of the jaxrpc property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJaxrpc(String value) throws PropertyVetoException {
        support.fireVetoableChange("jaxrpc", this.jaxrpc, value);

        this.jaxrpc = value;
    }

    /**
     * Gets the value of the saaj property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSaaj() {
        if (saaj == null) {
            return "INFO";
        } else {
            return saaj;
        }
    }

    /**
     * Sets the value of the saaj property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSaaj(String value) throws PropertyVetoException {
        support.fireVetoableChange("saaj", this.saaj, value);

        this.saaj = value;
    }

    /**
     * Gets the value of the corba property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCorba() {
        if (corba == null) {
            return "INFO";
        } else {
            return corba;
        }
    }

    /**
     * Sets the value of the corba property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCorba(String value) throws PropertyVetoException {
        support.fireVetoableChange("corba", this.corba, value);

        this.corba = value;
    }

    /**
     * Gets the value of the javamail property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJavamail() {
        if (javamail == null) {
            return "INFO";
        } else {
            return javamail;
        }
    }

    /**
     * Sets the value of the javamail property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJavamail(String value) throws PropertyVetoException {
        support.fireVetoableChange("javamail", this.javamail, value);

        this.javamail = value;
    }

    /**
     * Gets the value of the jms property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJms() {
        if (jms == null) {
            return "INFO";
        } else {
            return jms;
        }
    }

    /**
     * Sets the value of the jms property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJms(String value) throws PropertyVetoException {
        support.fireVetoableChange("jms", this.jms, value);

        this.jms = value;
    }

    /**
     * Gets the value of the connector property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getConnector() {
        if (connector == null) {
            return "INFO";
        } else {
            return connector;
        }
    }

    /**
     * Sets the value of the connector property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnector(String value) throws PropertyVetoException {
        support.fireVetoableChange("connector", this.connector, value);

        this.connector = value;
    }

    /**
     * Gets the value of the jdo property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJdo() {
        if (jdo == null) {
            return "INFO";
        } else {
            return jdo;
        }
    }

    /**
     * Sets the value of the jdo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJdo(String value) throws PropertyVetoException {
        support.fireVetoableChange("jdo", this.jdo, value);

        this.jdo = value;
    }

    /**
     * Gets the value of the cmp property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCmp() {
        if (cmp == null) {
            return "INFO";
        } else {
            return cmp;
        }
    }

    /**
     * Sets the value of the cmp property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCmp(String value) throws PropertyVetoException {
        support.fireVetoableChange("cmp", this.cmp, value);

        this.cmp = value;
    }

    /**
     * Gets the value of the util property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getUtil() {
        if (util == null) {
            return "INFO";
        } else {
            return util;
        }
    }

    /**
     * Sets the value of the util property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUtil(String value) throws PropertyVetoException {
        support.fireVetoableChange("util", this.util, value);

        this.util = value;
    }

    /**
     * Gets the value of the resourceAdapter property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getResourceAdapter() {
        if (resourceAdapter == null) {
            return "INFO";
        } else {
            return resourceAdapter;
        }
    }

    /**
     * Sets the value of the resourceAdapter property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResourceAdapter(String value) throws PropertyVetoException {
        support.fireVetoableChange("resourceAdapter", this.resourceAdapter, value);

        this.resourceAdapter = value;
    }

    /**
     * Gets the value of the synchronization property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSynchronization() {
        if (synchronization == null) {
            return "INFO";
        } else {
            return synchronization;
        }
    }

    /**
     * Sets the value of the synchronization property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSynchronization(String value) throws PropertyVetoException {
        support.fireVetoableChange("synchronization", this.synchronization, value);

        this.synchronization = value;
    }

    /**
     * Gets the value of the nodeAgent property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getNodeAgent() {
        if (nodeAgent == null) {
            return "INFO";
        } else {
            return nodeAgent;
        }
    }

    /**
     * Sets the value of the nodeAgent property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNodeAgent(String value) throws PropertyVetoException {
        support.fireVetoableChange("nodeAgent", this.nodeAgent, value);

        this.nodeAgent = value;
    }

    /**
     * Gets the value of the selfManagement property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSelfManagement() {
        if (selfManagement == null) {
            return "INFO";
        } else {
            return selfManagement;
        }
    }

    /**
     * Sets the value of the selfManagement property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSelfManagement(String value) throws PropertyVetoException {
        support.fireVetoableChange("selfManagement", this.selfManagement, value);

        this.selfManagement = value;
    }

    /**
     * Gets the value of the groupManagementService property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getGroupManagementService() {
        if (groupManagementService == null) {
            return "INFO";
        } else {
            return groupManagementService;
        }
    }

    /**
     * Sets the value of the groupManagementService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGroupManagementService(String value) throws PropertyVetoException {
        support.fireVetoableChange("groupManagementService", this.groupManagementService, value);

        this.groupManagementService = value;
    }

    /**
     * Gets the value of the managementEvent property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getManagementEvent() {
        if (managementEvent == null) {
            return "INFO";
        } else {
            return managementEvent;
        }
    }

    /**
     * Sets the value of the managementEvent property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManagementEvent(String value) throws PropertyVetoException {
        support.fireVetoableChange("managementEvent", this.managementEvent, value);

        this.managementEvent = value;
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
