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
import com.sun.enterprise.util.io.FileUtils;
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
public interface Node extends ConfigBeanProxy, Injectable, Named, ReferenceContainer, RefContainer {

    /**
     * Sets the node name
     * @param value node name
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="name", primary = true)
    public void setName(String value) throws PropertyVetoException;
    
    /**
     * points to a named host. 
     *
     * @return a named host name
     */

    @Attribute
    String getNodeHost();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="nodehost")
    void setNodeHost(String value) throws PropertyVetoException;

    /**
     * points to a named host.
     *
     * @return a named host name
     */

    @Attribute
    String getNodeHome();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="nodehome")
    void setNodeHome(String value) throws PropertyVetoException;

    @Element
    SshConnector getSshConnector();

    void setSshConnector(SshConnector connector);

    @Service
    @Scoped(PerLookup.class)
    class Decorator implements CreationDecorator<Node> {
        @Param(name="sshport",optional=true)
        String sshPort="-1";
        @Param(name="sshnodehost",optional=true)
        String sshHost=null;


        @Param (name="sshuser", optional=true)
        String sshuser=null;

        @Param (name="sshkeyfile", optional=true)
        String sshkeyfile;

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
        public void decorate(AdminCommandContext context, final Node instance) throws TransactionFailure, PropertyVetoException {


            Logger logger = LogDomains.getLogger(Node.class, LogDomains.ADMIN_LOGGER);
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Node.class);
            SshConnector sshC = instance.createChild(SshConnector.class);
            if (sshPort != "-1" )
                sshC.setSshPort(sshPort);
            else
                sshC.setSshPort("22");
            if (sshHost != null)
                sshC.setSshHost(sshHost);
            
            if (sshuser != null || sshkeyfile != null) {
                SshAuth sshA = sshC.createChild(SshAuth.class);
                if (sshuser != null)
                    sshA.setUserName(sshuser);
                if (sshkeyfile != null)
                    sshA.setKeyfile(sshkeyfile);
                sshC.setSshAuth(sshA);
            }
            instance.setSshConnector(sshC);
        }
    }

        @Service
    @Scoped(PerLookup.class)
    class DeleteDecorator implements DeletionDecorator<Nodes, Node> {
        @Inject
        private Domain domain;

        @Inject
        Nodes nodes;

        @Inject
        Servers servers;

        @Inject
        private ServerEnvironment env;

        @Override
        public void decorate(AdminCommandContext context, Nodes parent, Node child) throws
                PropertyVetoException, TransactionFailure{
            Logger logger = LogDomains.getLogger(Node.class, LogDomains.ADMIN_LOGGER);
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Node.class);
            final ActionReport report = context.getActionReport();
            String nodeName = child.getName();
            
            if (nodeName.equals("localhost"))  { // can't delete localhost node
                final String msg = localStrings.getLocalString(
                 "Node.localhost",
                 "Cannot remove Node {0}. ",child.getName() );
                 logger.log(Level.SEVERE, msg);
                return;
            }

            List<Node> nodeList = nodes.getNode();
            List<Server> serverList=servers.getServer();
            //check if node is referenced in an instance
            if (serverList.size() > 0) {
                for (Server server: serverList){
                    if (nodeName.equals(server.getNode())){
                       final String msg = localStrings.getLocalString(
                        "Node.referencedByInstance",
                        "Node {0} referenced in server instance {1}.  Remove instance before removing node."
                        ,child.getName() ,server.getName() );
                        logger.log(Level.SEVERE, msg);
                        return;
                    }
                }
            }

            nodeList.remove(child);

        }
    }

}
