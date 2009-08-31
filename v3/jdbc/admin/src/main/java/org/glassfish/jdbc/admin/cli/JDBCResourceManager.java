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

package org.glassfish.jdbc.admin.cli;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;

import static org.glassfish.resource.common.ResourceConstants.*;
import org.glassfish.resource.common.ResourceStatus;
import com.sun.enterprise.config.serverbeans.ServerTags;
import org.glassfish.api.I18n;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.jvnet.hk2.config.ConfiguredBy;
import org.glassfish.admin.cli.resources.ResourceManager;

/**
 *
 * @author PRASHANTH ABBAGANI
 *
 * The JDBC resource manager allows you to create and delete the config element
 * Will be used by the add-resources, deployment and CLI command
 */
@Service (name=ServerTags.JDBC_RESOURCE)
@I18n("jdbc.resource.manager")
@ConfiguredBy(Resources.class)
public class JDBCResourceManager implements ResourceManager {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(JDBCResourceManager.class);
    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    String jndiName = null;
    String description = null;
    String poolName = null;
    String enabled = Boolean.TRUE.toString();

    public String getResourceType () {
        return ServerTags.JDBC_RESOURCE;
    }

    public ResourceStatus create(Resources resources, HashMap attrList, 
                                    final Properties props, Server targetServer) 
           throws Exception {

        jndiName = (String) attrList.get(JNDI_NAME);
        description = (String) attrList.get(DESCRIPTION);
        poolName = (String) attrList.get(POOL_NAME);
        enabled = (String) attrList.get(ENABLED);

        if (jndiName == null) {
            String msg = localStrings.getLocalString("create.jdbc.resource.noJndiName",
                            "No JNDI name defined for JDBC resource.");
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            return status;
        }
        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                if (((BindableResource) resource).getJndiName().equals(jndiName)) {
                    String msg = localStrings.getLocalString("create.jdbc.resource.duplicate",
                            "A resource named {0} already exists.", jndiName);
                    ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg, true);
                    return status;
                }
            }/* else if (resource instanceof ResourcePool) {
                if (((ResourcePool) resource).getName().equals(jndiName)) {
                    String msg = localStrings.getLocalString("create.jdbc.resource.duplicate",
                            "A resource named {0} already exists.", jndiName);
                    ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg, true);
                    return status;
                }
            }*/
        }

        if (!isConnPoolExists(resources, poolName)) {
            String msg = localStrings.getLocalString("create.jdbc.resource.connPoolNotFound",
                "Attribute value (pool-name = {0}) is not found in list of jdbc connection pools.", poolName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            return status;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {

                    JdbcResource newResource = param.createChild(JdbcResource.class);
                    newResource.setJndiName(jndiName);//jdbcResource.getJndiName());
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    newResource.setPoolName(poolName);
                    newResource.setEnabled(enabled);
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
                targetServer.createResourceRef( enabled, jndiName);
            }

        } catch(TransactionFailure tfe) {
            String msg = localStrings.getLocalString("create.jdbc.resource.fail",
                            "JDBC resource {0} create failed ", jndiName) +
                            " " + tfe.getLocalizedMessage();
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        } /*catch(PropertyVetoException pve) {
            return (localStrings.getLocalString("create.jdbc.resource.fail", "{0} create failed ", id));
        }*/
        String msg = localStrings.getLocalString("create.jdbc.resource.success",
                "JDBC resource {0} created successfully", jndiName);
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, msg);
        return status;
    }
    
    public ResourceStatus delete (Resources resources, final JdbcResource[] jdbcResources,  
            final String jndiName, final Server targetServer) 
            throws Exception {
        
        if (jndiName == null) {
            String msg = localStrings.getLocalString("jdbc.resource.noJndiName",
                            "No JNDI name defined for JDBC resource.");
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            return status;
        }

        // ensure we already have this resource
        if (!isResourceExists(resources, jndiName)) {
            String msg = localStrings.getLocalString("delete.jdbc.resource.notfound",
                    "A JDBC resource named {0} does not exist.", jndiName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            return status;
        }

        try {
            // delete jdbc-resource
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    for (JdbcResource resource : jdbcResources) {
                        if (resource.getJndiName().equals(jndiName)) {
                            return param.getResources().remove(resource);
                        }
                    }
                    // not found
                    return null;
                }
            }, resources) == null) {
                String msg = localStrings.getLocalString("jdbc.resource.deletionFailed", 
                                "JDBC resource {0} delete failed ", jndiName);
                ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                return status;
            }
            
            // delete resource-ref
            targetServer.deleteResourceRef(jndiName);
            
        } catch(TransactionFailure tfe) {
            String msg = localStrings.getLocalString("jdbc.resource.deletionFailed", 
                            "JDBC resource {0} delete failed ", jndiName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }

        String msg = localStrings.getLocalString("jdbc.resource.deleteSuccess",
                "JDBC resource {0} deleted successfully", jndiName);
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, msg);
        return status;
    }
    
    public ArrayList list(JdbcResource[] jdbcResources) {
        ArrayList<String> list = new ArrayList();
        for (JdbcResource r : jdbcResources) {
            list.add(r.getJndiName());
        }
        return list;
    } 
    
    private boolean isResourceExists(Resources resources, String jndiName) {
        
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcResource) {
                if (((JdbcResource) resource).getJndiName().equals(jndiName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isConnPoolExists(Resources resources, String poolName) {
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcConnectionPool) {
                if (((JdbcConnectionPool)resource).getName().equals(poolName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
