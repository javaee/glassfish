/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.connectors.admin.cli;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.glassfish.api.I18n;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import static org.glassfish.resource.common.ResourceConstants.*;
import org.glassfish.resource.common.ResourceStatus;
import org.jvnet.hk2.config.types.Property;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.admin.cli.resources.ResourceManager;


/**
 *
 * @author Jennifer Chou
 */
@Service (name=ServerTags.CONNECTOR_RESOURCE)
@Scoped(PerLookup.class)
@I18n("create.connector.resource")
public class ConnectorResourceManager implements ResourceManager{

    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    final private static LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(ConnectorResourceManager.class);

    private String poolName = null;
    private String enabled = Boolean.TRUE.toString();
    private String jndiName = null;
    private String description = null;
    private String objectType = "user";

    public ConnectorResourceManager() {
    }

    public String getResourceType() {
        return ServerTags.CONNECTOR_RESOURCE;
    }

    public ResourceStatus create(Resources resources, HashMap attrList, 
                                    final Properties props, Server targetServer) 
                                    throws Exception {
        setParams(attrList);
        
        if (jndiName == null) {
            String msg = localStrings.getLocalString("create.connector.resource.noJndiName",
                            "No JNDI name defined for connector resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                if (((BindableResource) resource).getJndiName().equals(jndiName)) {
                    String msg = localStrings.getLocalString("create.connector.resource.duplicate",
                            "A resource named {0} already exists.", jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }/* else if (resource instanceof ResourcePool) {
                if (((ResourcePool) resource).getName().equals(jndiName)) {
                    String msg = localStrings.getLocalString("create.connector.resource.duplicate",
                            "A resource named {0} already exists.", jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }*/
        }

        if (!isConnPoolExists(resources, poolName)) {
            String msg = localStrings.getLocalString("create.connector.resource.connPoolNotFound",
                "Attribute value (pool-name = {0}) is not found in list of connector connection pools.", poolName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
            
        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {

                    ConnectorResource newResource = param.createChild(ConnectorResource.class);
                    newResource.setJndiName(jndiName);
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    newResource.setPoolName(poolName);
                    newResource.setEnabled(enabled.toString());
                    newResource.setObjectType(objectType);
                    if (props != null) {
                        for ( Map.Entry e : props.entrySet()) {
                            Property prop = newResource.createChild(Property.class);
                            prop.setName((String)e.getKey());
                            prop.setValue((String)e.getValue());
                            newResource.getProperty().add(prop);
                        }
                    }
                    param.getResources().add(newResource);
                    return newResource;
                }
            }, resources);

            if (!targetServer.isResourceRefExists( jndiName)) {
                targetServer.createResourceRef( enabled.toString(), jndiName);
            }

        } catch(TransactionFailure tfe) {
            String msg = localStrings.getLocalString("create.connector.resource.fail",
                            "Connector resource {0} create failed ", jndiName) +
                            " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        String msg = localStrings.getLocalString(
                "create.connector.resource.success", "Connector resource {0} created successfully",
                jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
         
    }

    public void setParams(HashMap attrList) {
        poolName = (String) attrList.get(POOL_NAME);
        enabled = (String) attrList.get(ENABLED);
        jndiName = (String) attrList.get(JNDI_NAME);
        description = (String) attrList.get(DESCRIPTION);
        objectType = (String) attrList.get(ServerTags.OBJECT_TYPE);
    }
    
    private boolean isConnPoolExists(Resources resources, String poolName) {
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ConnectorConnectionPool) {
                if (((ConnectorConnectionPool)resource).getName().equals(poolName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
