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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.I18n;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import static org.glassfish.resource.common.ResourceConstants.*;
import org.glassfish.resource.common.ResourceStatus;
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;
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
@Service (name=ServerTags.RESOURCE_ADAPTER_CONFIG)
@Scoped(PerLookup.class)
@I18n("create.resource.adapter.config")
public class ResourceAdapterConfigManager implements ResourceManager {

    final private static LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(ResourceAdapterConfigManager.class);

    private String raName = null;
    private String threadPoolIds = null;
    private String objectType = "user";
    private String name = null;

    public String getResourceType() {
        return ServerTags.RESOURCE_ADAPTER_CONFIG;
    }

    public ResourceStatus create(Resources resources, HashMap attrList, 
                                    final Properties props, Server targetServer) 
                                    throws Exception {
        setParams(attrList);
        
        if (raName == null) {
            String msg = localStrings.getLocalString("create.resource.adapter.confignoRAName",
                            "No RA Name defined for resource adapter config.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ResourceAdapterConfig) {
                if (((ResourceAdapterConfig) resource).getResourceAdapterName().equals(raName)) {
                    String msg = localStrings.getLocalString("create.resource.adapter.config.duplicate",
                            "Resource adapter config already exists for RAR", raName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }
            
        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {

                    ResourceAdapterConfig newResource = param.createChild(ResourceAdapterConfig.class);
                    newResource.setResourceAdapterName(raName);
                    if(threadPoolIds != null) {
                        newResource.setThreadPoolIds(threadPoolIds);
                    }
                    newResource.setObjectType(objectType);
                    if (name != null) {
                        newResource.setName(name);
                    }
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

        } catch(TransactionFailure tfe) {
            Logger.getLogger(ResourceAdapterConfigManager.class.getName()).log(Level.SEVERE,
                    "TransactionFailure: create-resource-adapter-config", tfe);
            String msg = localStrings.getLocalString("create.resource.adapter.config.fail",
                            "Unable to create resource adapter config", raName) +
                            " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        String msg = localStrings.getLocalString(
                "create.resource.adapter.config.success", "Resource adapter config {0} created successfully",
                raName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
         
    }

    public void setParams(HashMap attrList) {
        raName = (String) attrList.get(RESOURCE_ADAPTER_CONFIG_NAME);
        name = (String) attrList.get("name");
        threadPoolIds = (String) attrList.get(THREAD_POOL_IDS);
        objectType = (String) attrList.get(ServerTags.OBJECT_TYPE);
    }
}
