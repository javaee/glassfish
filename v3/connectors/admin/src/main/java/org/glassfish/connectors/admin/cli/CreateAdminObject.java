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
package org.glassfish.connectors.admin.cli;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;
import java.util.Properties;

/**
 * Create Admin Object Command
 * 
 */
@Service(name="create-admin-object")
@Scoped(PerLookup.class)
@I18n("create.admin.object")
public class CreateAdminObject implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateAdminObject.class);    

    @Param(name="restype")
    String resType;

    @Param(name="raname")
    String raName;

    @Param(optional=true, defaultValue="true")
    Boolean enabled;

    @Param(optional=true)
    String description;
    
    @Param(name="property", optional=true)
    Properties properties;
    
    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="jndi_name", primary=true)
    String jndiName;
    
    @Inject
    Resources resources;
    
    @Inject
    Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        Server targetServer = domain.getServerNamed(target);
        
        if (jndiName == null) {
            report.setMessage(localStrings.getLocalString("create.admin.object.noJndiName",
                            "No JNDI name defined for administered object."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof AdminObjectResource) {
                if (((AdminObjectResource) resource).getJndiName().equals(jndiName)) {
                    report.setMessage(localStrings.getLocalString("create.admin.object.duplicate",
                            "An administered object named {0} already exists.", jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        //TODO check if raname is valid
        /*if (!isValidRAName(resources, raName)) {
            report.setMessage(localStrings.getLocalString("create.admin.object.raNotFound",
                "Applications: Config element connector-module {0} is not found", raName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }*/

        //TODO check if restype is valid
        /*if (!isResTypeValid(aName)) {
            report.setMessage(localStrings.getLocalString("create.admin.object.raNotFound",
                "Applications: Config element connector-module {0} is not found", raName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }*/

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {

                    AdminObjectResource newResource = ConfigSupport.createChildOf(param, AdminObjectResource.class);
                    newResource.setJndiName(jndiName);
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    newResource.setResAdapter(raName);
                    newResource.setResType(resType);
                    newResource.setEnabled(enabled.toString());
                    if (properties != null) {
                        for ( java.util.Map.Entry e : properties.entrySet()) {
                            Property prop = ConfigSupport.createChildOf(newResource,
                                Property.class);
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
            report.setMessage(localStrings.getLocalString("create.admin.object.fail",
                            "Unable to create administered object {0}.", jndiName) +
                            " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
        report.setMessage(localStrings.getLocalString("create.admin.object.success",
                "Administered object {0} created.", jndiName));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    //TODO Error checking taken from v2, need to refactor for v3
    /*
    private boolean isResTypeValid(String raName) {
        // Check if the restype is valid -
        // To check this, we need to get the list of admin-object-interface
        // names and then find out if this list contains the restype.
        String[] resTypes = ConnectorRuntime.getRuntime().getAdminObjectInterfaceNames(raName);
        if (resTypes == null || resTypes.length <= 0) {
            throw new Exception(localStrings.getString("admin.mbeans.rmb.null_ao_intf", raName));
        }

        for (int i = 0; i < resTypes.length; i++) {
            if (resTypes[i].equals(resType)) {
                validResType = true;
                break;
            }
        }

        if (!validResType) {
            throw new Exception(localStrings.getString("admin.mbeans.rmb.invalid_res_type", resType));
        }
    }*/

    //TODO Error checking taken from v2, need to refactor for v3
    /*
    private boolean isValidRAName(String raName) throws Exception {
        boolean retVal = false;

        if ((raName == null) || (raName.equals(""))) {
            throw new Exception(localStrings.getString("admin.mbeans.rmb.null_res_adapter"));
        }

        // To check for embedded conenctor module
        if (raName.equals(ConnectorRuntime.DEFAULT_JMS_ADAPTER) || raName.equals(ConnectorRuntime.JAXR_RA_NAME)) {
            // System RA, so don't validate
            retVal = true;
        } else {
            // Check if the raName contains double underscore or hash.
            // If that is the case then this is the case of an embedded rar,
            // hence look for the application which embeds this rar,
            // otherwise look for the webconnector module with this raName.

            ObjectName applnObjName = m_registry.getMbeanObjectName(ServerTags.APPLICATIONS, new String[]{getDomainName()});
            int indx = raName.indexOf(
                    ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER);
            if (indx != -1) {
                String appName = raName.substring(0, indx);
                ObjectName j2eeAppObjName = (ObjectName) getMBeanServer().invoke(applnObjName, "getJ2eeApplicationByName", new Object[]{appName}, new String[]{"java.lang.String"});

                if (j2eeAppObjName == null) {
                    throw new Exception(localStrings.getString("admin.mbeans.rmb.invalid_ra_app_not_found", appName));
                } else {
                    retVal = true;
                }
            } else {
                ObjectName connectorModuleObjName = (ObjectName) getMBeanServer().invoke(applnObjName, "getConnectorModuleByName", new Object[]{raName}, new String[]{"java.lang.String"});

                if (connectorModuleObjName == null) {
                    throw new Exception(localStrings.getString("admin.mbeans.rmb.invalid_ra_cm_not_found", raName));
                } else {
                    retVal = true;
                }
            }
        }
        return retVal;
    }*/
}
