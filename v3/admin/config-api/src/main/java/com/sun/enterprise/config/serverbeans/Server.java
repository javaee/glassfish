/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.config.support.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.constraints.Min;
/**
 *
 * Java EE Application Server Configuration
 *
 * Each Application Server instance is a Java EE compliant container. One
 * server instance is specially designated as the Administration Server in SE/EE
 *
 * User applications cannot be deployed to an Administration Server instance
 */
@Configured
@SuppressWarnings("unused")
public interface Server extends ConfigBeanProxy, Injectable, PropertyBag, Named, SystemPropertyBag, ReferenceContainer, RefContainer {

    @Param(name="name", primary = true)
    public void setName(String value) throws PropertyVetoException;


    /**
     * Gets the value of the configRef property.
     *
     * Points to a named config. Needed for stand-alone servers. If server
     * instance is part of a cluster, then it points to the cluster config
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getConfigRef();

    /**
     * Sets the value of the configRef property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "config", optional=true)
    void setConfigRef(String value) throws PropertyVetoException;

    /**
     * Gets the value of the nodeAgentRef property.
     *
     * SE/EE only. Specifies name of node agent where server instance is hosted
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getNodeAgentRef();

    /**
     * Sets the value of the nodeAgentRef property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "nodeagent")
    void setNodeAgentRef(String value) throws PropertyVetoException;

    /**
     * Gets the value of the lbWeight property.
     *
     * Each server instance in a cluster has a weight, which may be used to
     * represent the relative processing capacity of that instance. Default
     * weight is 100 for every instance. Weighted load balancing policies will
     * use this weight while load balancing requests within the cluster.
     * It is the responsibility of the administrator to set the relative weights
     * correctly, keeping in mind deployed hardware capacity
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="100")
    @Min(value=1)
    String getLbWeight();

    /**
     * Sets the value of the lbWeight property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setLbWeight(String value) throws PropertyVetoException;


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
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal system properties" )
    @Element
    @Param(name="systemproperties", optional = true)
    List<SystemProperty> getSystemProperty();

    @DuckTyped
    String getReference();

    @DuckTyped
    ResourceRef getResourceRef(String name);

    @DuckTyped
    boolean isResourceRefExists(String refName);

    @DuckTyped
    void deleteResourceRef(String name) throws TransactionFailure;

    @DuckTyped
    void createResourceRef(String enabled, String refName) throws TransactionFailure;

    @DuckTyped
    ApplicationRef getApplicationRef(String appName);


    class Duck {
        public static String getReference(Server server) {
            return server.getConfigRef();
        }

        public static ApplicationRef getApplicationRef(Server server, 
            String appName) {
            for (ApplicationRef appRef : server.getApplicationRef()) {
                if (appRef.getRef().equals(appName)) {
                    return appRef;
                }
            }
            return null;
        }

        public static ResourceRef getResourceRef(Server server, String refName) {
            for (ResourceRef ref : server.getResourceRef()) {
                if (ref.getRef().equals(refName)) {
                    return ref;
                }
            }
            return null;
        }

        public static boolean isResourceRefExists(Server server, String refName) {
            return getResourceRef(server, refName)!=null;
        }

        public static void deleteResourceRef(Server server, String refName) throws TransactionFailure {
            final ResourceRef ref = getResourceRef(server, refName);
            if (ref!=null) {
               ConfigSupport.apply(new SingleConfigCode<Server>() {

                    public Object run(Server param) {
                        return param.getResourceRef().remove(ref);
                        }
               }, server);
            }
        }

        public static void createResourceRef(Server server, final String enabled, final String refName) throws TransactionFailure {

            ConfigSupport.apply(new SingleConfigCode<Server>() {

                    public Object run(Server param) throws PropertyVetoException, TransactionFailure {

                        ResourceRef newResourceRef = param.createChild(ResourceRef.class);
                        newResourceRef.setEnabled(enabled);
                        newResourceRef.setRef(refName);
                        param.getResourceRef().add(newResourceRef);
                        return newResourceRef;
                    }
                }, server);
        }
    }
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    @Param(name="properties", optional = true)
    List<Property> getProperty();

    @Service
    class Decorator implements CreationDecorator<Server> {
        @Param(name="cluster", optional=true)
        String clusterName = null;

        @Inject
        Domain domain;
        
        @Override
        public void decorate(AdminCommandContext context, Server instance) throws TransactionFailure, PropertyVetoException {
            Logger logger = LogDomains.getLogger(Cluster.class, LogDomains.ADMIN_LOGGER);
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Server.class);
            String configRef = instance.getConfigRef();

            // cluster instance using cluster config
            if (clusterName != null) {
                if (configRef != null) {
                    throw new TransactionFailure(localStrings.getLocalString(
                            "Server.cannotSpecifyBothConfigAndCluster",
                            "A configuration name and cluster name cannot both be specified."));
                }
                boolean clusterExists = false;
                Clusters clusters = domain.getClusters();
                if (clusters != null) {
                    for (Cluster cluster : clusters.getCluster()) {
                        if (clusterName.equals(cluster.getName())) {
                            instance.setConfigRef(cluster.getConfigRef());
                            clusterExists = true;
                            break;
                        }
                    }
                }
                clusterName = null; // workaround - initialize to null, otherwise it keeps the name value
                if (!clusterExists) {
                    throw new TransactionFailure(localStrings.getLocalString(
                            "noSuchCluster", "Cluster {0} does not exist.", clusterName));
                }
            }

            // instance using specified config
            if (configRef != null) {
                Config specifiedConfig = domain.getConfigs().getConfigByName(configRef);
                if (specifiedConfig == null) {
                    throw new TransactionFailure(localStrings.getLocalString(
                            "noSuchConfig", "Configuration {0} does not exist.", configRef));
                }
            }

            //stand-alone instance using default-config if config not specified
            if (configRef == null && clusterName == null) {
                Config defaultConfig = domain.getConfigs().getConfigByName("default-config");

                final Config configCopy;
                try {
                    configCopy = (Config) defaultConfig.deepCopy();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, localStrings.getLocalString(Server.class,
                    "Cluster.error_while_copying",
                    "Error while copying the default configuration {0)",
                    e.toString(), e));
                    throw new TransactionFailure(e.toString(),e);
                }


                final String configName = instance.getName()+"-config";
                instance.setConfigRef(configName);

                ConfigSupport.apply(new ConfigCode() {
                    @Override
                    public Object run(ConfigBeanProxy[] w ) throws PropertyVetoException, TransactionFailure {
                        ((Configs) w[0]).getConfig().add(configCopy);
                        ((Config) w[1]).setName(configName);
                        return null;
                    }
                }, domain.getConfigs(), configCopy);
            }

            for (Resource resource : domain.getResources().getResources()) {
                if (resource.getObjectType().equals("system-all")) {
                    String name=null;
                    if (resource instanceof BindableResource) {
                        name = ((BindableResource) resource).getJndiName();
                    }
                    if (resource instanceof Named) {
                        name = ((Named) resource).getName();
                    }
                    if (name==null) {
                        throw new TransactionFailure("Cannot add un-named resources to the new server instance");
                    }
                    ResourceRef newResourceRef = instance.createChild(ResourceRef.class);
                    newResourceRef.setRef(name);
                    instance.getResourceRef().add(newResourceRef);
                }
            }
            for (Application application : domain.getApplications().getApplications()) {
                if (application.getObjectType().equals("system-all")) {
                    ApplicationRef newAppRef = instance.createChild(ApplicationRef.class);
                    newAppRef.setRef(application.getName());
                    // todo : what about virtual-servers ?
                    instance.getApplicationRef().add(newAppRef);
                }
            }
        }
    }
}
