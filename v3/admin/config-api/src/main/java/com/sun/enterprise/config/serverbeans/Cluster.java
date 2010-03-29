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

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.config.support.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.Injectable;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.ReferenceContainer;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import org.glassfish.quality.ToDo;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "serverRef",
    "resourceRef",
    "applicationRef",
    "systemProperty",
    "property"
}) */

@Configured
/**
 * SE/EE Cluster configuration. A cluster defines a homogeneous set of server
 * instances that share the same applications, resources, and configuration.                                                
 *
 */
@Create(value="create-cluster", parentType=Clusters.class, resolver= TypeResolver.class, decorator=Cluster.Decorator.class)
@Delete(value="delete-cluster", parentType=Clusters.class, resolver= TypeAndNameResolver.class)
public interface Cluster extends ConfigBeanProxy, Injectable, PropertyBag, Named, SystemPropertyBag, ReferenceContainer {

    @Param(name="name", primary = true)
    public void setName(String value) throws PropertyVetoException;
    

    /**
     * Gets the value of the configRef property.
     *
     * Points to a named config. All server instances in the cluster will
     * share this config.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    String getConfigRef();

    /**
     * Sets the value of the configRef property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    @Param(name="config-ref")
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
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    String getHeartbeatEnabled();

    /**
     * Sets the value of the heartbeatEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
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
     */
    void setHeartbeatAddress(String value) throws PropertyVetoException;

    /**
     * Gets the value of the serverRef property.
     *
     * List of servers in the cluster
     * 
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serverRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServerRef().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ServerRef }
     */
    @Element
    List<ServerRef> getServerRef();

    /**
     * Gets the value of the resourceRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceRef().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceRef }
     */
    @Element
    List<ResourceRef> getResourceRef();

    /**
     * Gets the value of the applicationRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applicationRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplicationRef().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ApplicationRef }
     */
    @Element
    List<ApplicationRef> getApplicationRef();

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
    	Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    @Param(name="properties", optional=true)
    List<Property> getProperty();
    
    @DuckTyped
    String getReference();

    class Duck {
        public static String getReference(Cluster cluster) {
            return cluster.getConfigRef();
        }
    }

    @Service
    class Decorator implements CreationDecorator {

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
        
        @Override
        public void decorate(AdminCommandContext context, Object instance) throws TransactionFailure, PropertyVetoException {
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
}
