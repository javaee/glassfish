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
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Injectable;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.ReferenceContainer;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import org.glassfish.quality.ToDo;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * A cluster defines a homogeneous set of server instances that share the same
 * applications, resources, and configuration.
 */
@Configured
@SuppressWarnings("unused")
public interface Cluster extends ConfigBeanProxy, Injectable, PropertyBag, Named, SystemPropertyBag, ReferenceContainer, RefContainer {

    /**
     * Sets the cluster name
     * @param value cluster name
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="name", primary = true)
    public void setName(String value) throws PropertyVetoException;

    /**
     * points to a named config. All server instances in the cluster
     * will share this config.
     *
     * @return a named config name
     */
    @Attribute
    @NotNull
    String getConfigRef();

    /**
     * Sets the value of the configRef property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="config", optional=true)
    void setConfigRef(String value) throws PropertyVetoException;

    /**
     * Gets the value of the heartbeatEnabled property.
     *
     * When "heartbeat-enabled" is set to "true", the GMS services will be
     * started as a lifecycle module in each the application server in the
     * cluster.When "heartbeat-enabled" is set to "false", GMS will not be
     * started and its services will be unavailable. Clusters should function
     * albeit with reduced functionality.
     *
     * @return true | false as a string, null means false
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    String getHeartbeatEnabled();

    /**
     * Sets the value of the heartbeatEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setHeartbeatEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the heartbeatPort property.
     *
     * This is the communication port GMS uses to listen for group  events.
     * This should be a valid port number.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @Min(value=1024)
    @Max(value=49151)
    String getHeartbeatPort();

    /**
     * Sets the value of the heartbeatPort property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setHeartbeatPort(String value) throws PropertyVetoException;

    /**
     * Gets the value of the heartbeatAddress property.
     *
     * This is the address (only multicast supported) at which GMS will
     * listen for group events.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getHeartbeatAddress();

    /**
     * Sets the value of the heartbeatAddress property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setHeartbeatAddress(String value) throws PropertyVetoException;

    /**
     * Gets the value of the serverRef property.
     *
     * List of servers in the cluster
     *
     * @return list of configured {@link ServerRef }
     */
    @Element
    List<ServerRef> getServerRef();

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
    @Element
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal system props" )
    @Param(name="systemproperties",optional=true)
    List<SystemProperty> getSystemProperty();

    /**
     *	Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    @Param(name="properties", optional=true)
    List<Property> getProperty();

    /**
     * Returns the cluster configuration reference
     * @return the config-ref attribute
     */
    @DuckTyped
    String getReference();

    @DuckTyped
    List<Server> getInstances();

    class Duck {
        public static String getReference(Cluster cluster) {
            return cluster.getConfigRef();
        }

        public static List<Server> getInstances(Cluster cluster) {

            Dom clusterDom = Dom.unwrap(cluster);
            Domain domain =
                    clusterDom.getHabitat().getComponent(Domain.class);

            ArrayList<Server> instances = new ArrayList<Server>();
            for (ServerRef sRef : cluster.getServerRef()) {
                instances.add(domain.getServerNamed(sRef.getRef()));
            }
            return instances;
        }
    }

    @Service
    @Scoped(PerLookup.class)
    class Decorator implements CreationDecorator<Cluster> {

        @Param(name="config", optional=true)
        String configRef=null;

        @Param(optional = true)
        String hosts=null;

        @Param(optional = true)
        int haagentport=0;

        @Param(optional = true)
        String haadminpassword=null;

        @Param(optional = true)
        String haadminpasswordfile=null;

        @Param(optional = true)
        String devicesize=null;

        @Param(optional = true)
        String haproperty=null;

        @Param(optional = true)
        String autohadb=null;

        @Param(optional = true)
        String portbase=null;

        @Inject
        Habitat habitat;

        @Inject
        ServerEnvironment env;

        @Inject
        Domain domain;

        /**
         * Decorates the newly CRUD created cluster configuration instance.
         * tasks :
         *      - ensures that it references an existing configuration
         *      - creates a new config from the default-config if no config-ref
         *        was provided.
         *      - check for deprecated parameters.
         *
         * @param context administration command context
         * @param instance newly created configuration element
         * @throws TransactionFailure
         * @throws PropertyVetoException
         */
        @Override
        public void decorate(AdminCommandContext context, final Cluster instance) throws TransactionFailure, PropertyVetoException {
            Logger logger = LogDomains.getLogger(Cluster.class, LogDomains.ADMIN_LOGGER);
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Cluster.class);

            //There should be no instance/config with the same name as the cluster
            if ((domain.getServerNamed(instance.getName()) != null) ||
                    (domain.getConfigNamed(instance.getName()) != null)){
                throw new TransactionFailure(localStrings.getLocalString(
                        "cannotAddDuplicate", "There is an instance {0} already present.", instance.getName()));
            }

            if (configRef==null) {
                Config config = habitat.getComponent(Config.class, "default-config");
                if (config==null) {
                    config = habitat.getAllByContract(Config.class).iterator().next();
                    logger.warning(localStrings.getLocalString(Cluster.class,
                            "Cluster.no_default_config_found",
                            "No default config found, using config {0} as the default config for the cluster {1}",
                            config.getName(), instance.getName()));
                }
                final Config configCopy;
                try {
                    configCopy = (Config) config.deepCopy();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, localStrings.getLocalString(Cluster.class,
                            "Cluster.error_while_copying",
                            "Error while copying the default configuration {0)",
                            e.toString(), e));
                    throw new TransactionFailure(e.toString(),e);
                }


                final String configName = instance.getName() + "-config";
                instance.setConfigRef(configName);

                // needs to be changed to join the transaction of instance
                ConfigSupport.apply(new ConfigCode() {
                    @Override
                    public Object run(ConfigBeanProxy[] w ) throws PropertyVetoException, TransactionFailure {
                        ((Configs) w[0]).getConfig().add(configCopy);
                        ((Config) w[1]).setName(configName);
                        return null;
                    }
                }, domain.getConfigs(), configCopy);
            }  else {

                // cluster using specified config
                Config specifiedConfig = domain.getConfigs().getConfigByName(configRef);
                if (specifiedConfig == null) {
                    throw new TransactionFailure(localStrings.getLocalString(
                            "noSuchConfig", "Configuration {0} does not exist.", configRef));
                }
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
                    instance.getApplicationRef().add(newAppRef);
                }
            }

            if (hosts!=null ||
                    haagentport!=0 ||
                    haadminpassword!=null ||
                    haadminpasswordfile!=null ||
                    devicesize!=null ||
                    haproperty!=null ||
                    autohadb!=null ||
                    portbase!=null
                    ) {
                context.getActionReport().setActionExitCode(ActionReport.ExitCode.WARNING);
                context.getActionReport().setMessage("Obsolete options used.");
            }
        }
    }

    @Service
    @Scoped(PerLookup.class)
    class DeleteDecorator implements DeletionDecorator<Clusters, Cluster> {

        @Inject
        Configs configs;
        
        @Override
        public void decorate(AdminCommandContext context, Clusters parent, Cluster child) throws
                PropertyVetoException, TransactionFailure{
            // check if the config is still in used, otherwise delete it.
            Logger logger = LogDomains.getLogger(Cluster.class, LogDomains.ADMIN_LOGGER);
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Cluster.class);
            final ActionReport report = context.getActionReport();


            String instanceConfig = child.getConfigRef();
            final Config config = configs.getConfigByName(instanceConfig);
            try {
                ConfigSupport.apply(new SingleConfigCode<Configs>() {

                    @Override
                    public Object run(Configs c) throws PropertyVetoException, TransactionFailure {
                        List<Config> configList = c.getConfig();
                        configList.remove(config);
                        return null;
                    }
                }, configs);
            } catch (TransactionFailure ex) {
                logger.log(Level.SEVERE,
                        localStrings.getLocalString("deleteConfigFailed",
                                "Unable to remove config {0}", instanceConfig), ex);
                String msg = ex.getMessage() != null ? ex.getMessage()
                        : localStrings.getLocalString("deleteConfigFailed",
                        "Unable to remove config {0}", instanceConfig);
                report.setMessage(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(ex);
                throw ex;
            }

        }
    }
}

