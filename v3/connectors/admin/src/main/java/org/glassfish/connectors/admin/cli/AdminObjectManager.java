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
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import static org.glassfish.resource.common.ResourceConstants.*;
import org.glassfish.resource.common.ResourceStatus;
import org.jvnet.hk2.config.types.Property;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.*;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
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
@Service (name=ServerTags.ADMIN_OBJECT_RESOURCE)
@Scoped(PerLookup.class)
@I18n("create.admin.object")
public class AdminObjectManager implements ResourceManager{

    @Inject
    Applications applications;

    @Inject
    ConnectorRuntime connectorRuntime;

    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    final private static LocalStringManagerImpl localStrings = 
        new LocalStringManagerImpl(AdminObjectManager.class);

    private String resType = null;
    private String className = null;
    private String raName = null;
    private String enabled = Boolean.TRUE.toString();
    private String jndiName = null;
    private String description = null;

    public AdminObjectManager() {
    }

    public String getResourceType() {
        return ServerTags.ADMIN_OBJECT_RESOURCE;
    }

    public ResourceStatus create(Resources resources, HashMap attrList, 
                                    final Properties props, Server targetServer) 
                                    throws Exception {
        setParams(attrList);
        
        if (jndiName == null) {
            String msg = localStrings.getLocalString("create.admin.object.noJndiName",
                            "No JNDI name defined for administered object.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                if (((BindableResource) resource).getJndiName().equals(jndiName)) {
                    String msg = localStrings.getLocalString("create.admin.object.duplicate",
                            "A resource named {0} already exists.", jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }

        ResourceStatus status = isValidRAName();
        if (status.getStatus() == ResourceStatus.FAILURE) {
            return status;
        }

        status = isValidAdminObject();
        if (status.getStatus() == ResourceStatus.FAILURE) {
            return status;
        }
            
        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {

                    AdminObjectResource newResource = param.createChild(AdminObjectResource.class);
                    newResource.setJndiName(jndiName);
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    newResource.setResAdapter(raName);
                    newResource.setResType(resType);
                    newResource.setClassName(className);
                    newResource.setEnabled(enabled.toString());
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
            Logger.getLogger(AdminObjectManager.class.getName()).log(Level.SEVERE,
                    "Unabled to create administered object", tfe);
            String msg = localStrings.getLocalString("create.admin.object.fail",
                            "Unable to create administered object {0}.", jndiName) +
                            " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        String msg = localStrings.getLocalString(
                "create.admin.object.success",
                "Administered object {0} created.", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
         
    }

    public void setParams(HashMap attrList) {
        resType = (String) attrList.get(RES_TYPE);
        className = (String)attrList.get(ADMIN_OBJECT_CLASS_NAME);
        enabled = (String) attrList.get(ENABLED);
        jndiName = (String) attrList.get(JNDI_NAME);
        description = (String) attrList.get(DESCRIPTION);
        raName = (String) attrList.get(RES_ADAPTER);
    }
    
     //TODO Error checking taken from v2, need to refactor for v3
    private ResourceStatus isValidAdminObject() {
        // Check if the restype is valid -
        // To check this, we need to get the list of admin-object-interface
        // names and then find out if this list contains the restype.
        //boolean isValidAdminObject = true;
         boolean isValidAdminObject = false;

         //if classname is null, check whether the resType is present and only one adminobject must
         //be using that resType
         if (className == null) {

             String[] resTypes;
             try {
                 resTypes = connectorRuntime.getAdminObjectInterfaceNames(raName);
             } catch (ConnectorRuntimeException cre) {
                 Logger.getLogger(AdminObjectManager.class.getName()).log(Level.SEVERE,
                         "Could not find admin-ojbect-interface names (resTypes) from ConnectorRuntime for resource adapter.", cre);
                 String msg = localStrings.getLocalString(
                         "admin.mbeans.rmb.null_ao_intf",
                         "Resource Adapter {0} does not contain any resource type for admin-object. Please specify another res-adapter.",
                         raName) + " " + cre.getLocalizedMessage();
                 return new ResourceStatus(ResourceStatus.FAILURE, msg);
             }
             if (resTypes == null || resTypes.length <= 0) {
                 String msg = localStrings.getLocalString("admin.mbeans.rmb.null_ao_intf",
                         "Resource Adapter {0} does not contain any resource type for admin-object. Please specify another res-adapter.", raName);
                 return new ResourceStatus(ResourceStatus.FAILURE, msg);
             }

             int count = 0;
             for (int i = 0; i < resTypes.length; i++) {
                 if (resTypes[i].equals(resType)) {
                     isValidAdminObject = true;
                     count++;
                 }
             }
             if(count > 1){
                 String msg = localStrings.getLocalString(
                         "admin.mbeans.rmb.multiple_admin_objects.found.for.restype",
                         "Need to specify admin-object classname parameter (--classname) as multiple admin objects " +
                                 "use this resType [ {0} ]",  resType);

                 return new ResourceStatus(ResourceStatus.FAILURE, msg);
             }
         }else{
             try{
                isValidAdminObject = connectorRuntime.hasAdminObject(raName, resType, className);
             } catch (ConnectorRuntimeException cre) {
                 Logger.getLogger(AdminObjectManager.class.getName()).log(Level.SEVERE,
                         "Could not find admin-object-interface names (resTypes) and admin-object-classnames from " +
                                 "ConnectorRuntime for resource adapter.", cre);
                 String msg = localStrings.getLocalString(
                         "admin.mbeans.rmb.ao_intf_impl_check_failed",
                         "Could not determine admin object resource information of Resource Adapter [ {0} ] for" +
                                 "resType [ {1} ] and classname [ {2} ] ",
                         raName, resType, className) + " " + cre.getLocalizedMessage();
                 return new ResourceStatus(ResourceStatus.FAILURE, msg);
             }
         }

         if (!isValidAdminObject) {
            String msg = localStrings.getLocalString("admin.mbeans.rmb.invalid_res_type",
                "Invalid Resource Type: {0}", resType);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        return new ResourceStatus(ResourceStatus.SUCCESS, "");
    }

    private ResourceStatus isValidRAName() {
        //TODO turn on validation.  For now, turn validation off until connector modules ready
        //boolean retVal = false;
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, "");

        if ((raName == null) || (raName.equals(""))) {
            String msg = localStrings.getLocalString("admin.mbeans.rmb.null_res_adapter",
                    "Resource Adapter Name is null.");
            status = new ResourceStatus(ResourceStatus.FAILURE, msg);
        } else {
            // To check for embedded connector module
            // System RA, so don't validate
            if (!raName.equals(DEFAULT_JMS_ADAPTER) && !raName.equals(JAXR_RA_NAME)) {
                // Check if the raName contains double underscore or hash.
                // If that is the case then this is the case of an embedded rar,
                // hence look for the application which embeds this rar,
                // otherwise look for the webconnector module with this raName.

                int indx = raName.indexOf(EMBEDDEDRAR_NAME_DELIMITER);
                if (indx != -1) {
                    String appName = raName.substring(0, indx);
                    Application app = applications.getModule(Application.class, appName);
                    if (app == null) {
                        String msg = localStrings.getLocalString("admin.mbeans.rmb.invalid_ra_app_not_found",
                                "Invalid raname. Application with name {0} not found.", appName);
                        status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                    }
                } else {
                    Application app = applications.getModule(Application.class, raName);
                    if (app == null) {
                        String msg = localStrings.getLocalString("admin.mbeans.rmb.invalid_ra_cm_not_found",
                                "Invalid raname. Connector Module with name {0} not found.", raName);
                        status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                    }
                }
            }
        }

        return status;
    }
}
