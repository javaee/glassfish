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
    "applications",
    "resources",
    "configs",
    "servers",
    "clusters",
    "nodeAgents",
    "lbConfigs",
    "loadBalancers",
    "systemProperty",
    "property"
}) */
@Configured
public class Domain
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String applicationRoot;
    @Attribute

    protected String logRoot;
    @Attribute

    protected String locale;
    protected Applications applications;
    protected Resources resources;
    @Element(required=true)
    protected Configs configs;
    @Element(required=true)
    protected Servers servers;
    protected Clusters clusters;
    @Element
    protected NodeAgents nodeAgents;
    @Element
    protected LbConfigs lbConfigs;
    @Element
    protected LoadBalancers loadBalancers;
    @Element
    protected List<SystemProperty> systemProperty = new ConstrainedList<SystemProperty>(this, "systemProperty", support);
    protected List<Property> property = new ConstrainedList<Property>(this, "property", support);



    /**
     * Gets the value of the applicationRoot property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getApplicationRoot() {
        return applicationRoot;
    }

    /**
     * Sets the value of the applicationRoot property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setApplicationRoot(String value) throws PropertyVetoException {
        support.fireVetoableChange("applicationRoot", this.applicationRoot, value);

        this.applicationRoot = value;
    }

    /**
     * Gets the value of the logRoot property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLogRoot() {
        return logRoot;
    }

    /**
     * Sets the value of the logRoot property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogRoot(String value) throws PropertyVetoException {
        support.fireVetoableChange("logRoot", this.logRoot, value);

        this.logRoot = value;
    }

    /**
     * Gets the value of the locale property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the value of the locale property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLocale(String value) throws PropertyVetoException {
        support.fireVetoableChange("locale", this.locale, value);

        this.locale = value;
    }

    /**
     * Gets the value of the applications property.
     *
     * @return possible object is
     *         {@link Applications }
     */
    public Applications getApplications() {
        return applications;
    }

    /**
     * Sets the value of the applications property.
     *
     * @param value allowed object is
     *              {@link Applications }
     */
    public void setApplications(Applications value) throws PropertyVetoException {
        support.fireVetoableChange("applications", this.applications, value);

        this.applications = value;
    }

    /**
     * Gets the value of the resources property.
     *
     * @return possible object is
     *         {@link Resources }
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * Sets the value of the resources property.
     *
     * @param value allowed object is
     *              {@link Resources }
     */
    public void setResources(Resources value) throws PropertyVetoException {
        support.fireVetoableChange("resources", this.resources, value);

        this.resources = value;
    }

    /**
     * Gets the value of the configs property.
     *
     * @return possible object is
     *         {@link Configs }
     */
    public Configs getConfigs() {
        return configs;
    }

    /**
     * Sets the value of the configs property.
     *
     * @param value allowed object is
     *              {@link Configs }
     */
    public void setConfigs(Configs value) throws PropertyVetoException {
        support.fireVetoableChange("configs", this.configs, value);

        this.configs = value;
    }

    /**
     * Gets the value of the servers property.
     *
     * @return possible object is
     *         {@link Servers }
     */
    public Servers getServers() {
        return servers;
    }

    /**
     * Sets the value of the servers property.
     *
     * @param value allowed object is
     *              {@link Servers }
     */
    public void setServers(Servers value) throws PropertyVetoException {
        support.fireVetoableChange("servers", this.servers, value);

        this.servers = value;
    }

    /**
     * Gets the value of the clusters property.
     *
     * @return possible object is
     *         {@link Clusters }
     */
    public Clusters getClusters() {
        return clusters;
    }

    /**
     * Sets the value of the clusters property.
     *
     * @param value allowed object is
     *              {@link Clusters }
     */
    public void setClusters(Clusters value) throws PropertyVetoException {
        support.fireVetoableChange("clusters", this.clusters, value);

        this.clusters = value;
    }

    /**
     * Gets the value of the nodeAgents property.
     *
     * @return possible object is
     *         {@link NodeAgents }
     */
    public NodeAgents getNodeAgents() {
        return nodeAgents;
    }

    /**
     * Sets the value of the nodeAgents property.
     *
     * @param value allowed object is
     *              {@link NodeAgents }
     */
    public void setNodeAgents(NodeAgents value) throws PropertyVetoException {
        support.fireVetoableChange("nodeAgents", this.nodeAgents, value);

        this.nodeAgents = value;
    }

    /**
     * Gets the value of the lbConfigs property.
     *
     * @return possible object is
     *         {@link LbConfigs }
     */
    public LbConfigs getLbConfigs() {
        return lbConfigs;
    }

    /**
     * Sets the value of the lbConfigs property.
     *
     * @param value allowed object is
     *              {@link LbConfigs }
     */
    public void setLbConfigs(LbConfigs value) throws PropertyVetoException {
        support.fireVetoableChange("lbConfigs", this.lbConfigs, value);

        this.lbConfigs = value;
    }

    /**
     * Gets the value of the loadBalancers property.
     *
     * @return possible object is
     *         {@link LoadBalancers }
     */
    public LoadBalancers getLoadBalancers() {
        return loadBalancers;
    }

    /**
     * Sets the value of the loadBalancers property.
     *
     * @param value allowed object is
     *              {@link LoadBalancers }
     */
    public void setLoadBalancers(LoadBalancers value) throws PropertyVetoException {
        support.fireVetoableChange("loadBalancers", this.loadBalancers, value);

        this.loadBalancers = value;
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
