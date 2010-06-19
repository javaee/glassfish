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
 * @author Prashanth Abbagani
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

    private String jndiName = null;
    private String description = null;
    private String poolName = null;
    private String enabled = Boolean.TRUE.toString();

    public String getResourceType () {
        return ServerTags.JDBC_RESOURCE;
    }

    public ResourceStatus create(Resources resources, HashMap attributes, final Properties properties,
                                 Server targetServer, boolean requiresNewTransaction) throws Exception {

        setAttributes(attributes);

        if (jndiName == null) {
            String msg = localStrings.getLocalString("create.jdbc.resource.noJndiName",
                            "No JNDI name defined for JDBC resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                if (((BindableResource) resource).getJndiName().equals(jndiName)) {
                    String msg = localStrings.getLocalString("create.jdbc.resource.duplicate",
                            "A resource named {0} already exists.", jndiName);
                    return  new ResourceStatus(ResourceStatus.FAILURE, msg, true);
                }
            }
        }

        if (!isConnPoolExists(resources, poolName)) {
            String msg = localStrings.getLocalString("create.jdbc.resource.connPoolNotFound",
                "Attribute value (pool-name = {0}) is not found in list of jdbc connection pools.", poolName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            return status;
        }

        if (requiresNewTransaction) {
            try {
                ConfigSupport.apply(new SingleConfigCode<Resources>() {

                    public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                        return createResource(param, properties);
                    }
                }, resources);

                if (!targetServer.isResourceRefExists(jndiName)) {
                    targetServer.createResourceRef(enabled, jndiName);
                }
            } catch (TransactionFailure tfe) {
                String msg = localStrings.getLocalString("create.jdbc.resource.fail",
                        "JDBC resource {0} create failed ", jndiName) +
                        " " + tfe.getLocalizedMessage();
                ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                status.setException(tfe);
                return status;
            }
        } else {
            createResource(resources, properties);
        }
        String msg = localStrings.getLocalString("create.jdbc.resource.success",
                "JDBC resource {0} created successfully", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }

    private void setAttributes(HashMap attributes) {
        jndiName = (String) attributes.get(JNDI_NAME);
        description = (String) attributes.get(DESCRIPTION);
        poolName = (String) attributes.get(POOL_NAME);
        enabled = (String) attributes.get(ENABLED);
    }

    private JdbcResource createResource(Resources param, Properties properties) throws PropertyVetoException, TransactionFailure {
        JdbcResource newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }

    private JdbcResource createConfigBean(Resources param, Properties properties) throws PropertyVetoException, TransactionFailure {
        JdbcResource jdbcResource = param.createChild(JdbcResource.class);
        jdbcResource.setJndiName(jndiName);
        if (description != null) {
            jdbcResource.setDescription(description);
        }
        jdbcResource.setPoolName(poolName);
        jdbcResource.setEnabled(enabled);
        if (properties != null) {
            for ( Map.Entry e : properties.entrySet()) {
                Property prop = jdbcResource.createChild(Property.class);
                prop.setName((String)e.getKey());
                prop.setValue((String)e.getValue());
                jdbcResource.getProperty().add(prop);
            }
        }
        return jdbcResource;
    }

    public Resource createConfigBean(final Resources resources, HashMap attributes, final Properties properties) throws Exception{
        setAttributes(attributes);
        return createConfigBean(resources, properties);
    }

    public ResourceStatus delete (Resources resources, final JdbcResource[] jdbcResources,  
            final String jndiName, final Server targetServer) 
            throws Exception {
        
        if (jndiName == null) {
            String msg = localStrings.getLocalString("jdbc.resource.noJndiName",
                            "No JNDI name defined for JDBC resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        // ensure we already have this resource
        if (!isResourceExists(resources, jndiName)) {
            String msg = localStrings.getLocalString("delete.jdbc.resource.notfound",
                    "A JDBC resource named {0} does not exist.", jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
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
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
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
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }
    
    public ArrayList list(JdbcResource[] jdbcResources) {
        ArrayList<String> list = new ArrayList<String>();
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
