/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.jvnet.hk2.config.types.Property;
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
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;
import java.util.Properties;

/**
 * Create Custom Resource
 *
 */
@Service(name="create-custom-resource")
@Scoped(PerLookup.class)
@I18n("create.custom.resource")
public class CreateCustomResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(CreateCustomResource.class);

    @Param(name = "restype")
    String resType;

    @Param(name = "factoryclass")
    String factoryClass;

    @Param(optional = true, defaultValue = "true")
    Boolean enabled;

    @Param(optional = true)
    String description;

    @Param(name = "property", optional = true, separator = ':')
    Properties properties;

    @Param(optional = true,
    defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;

    @Param(name = "jndi_name", primary = true)
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

        if (resType == null) {
            report.setMessage(localStrings.getLocalString(
                    "create.custom.resource.noResType",
                    "No type defined for Custom Resource."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (factoryClass == null) {
            report.setMessage(localStrings.getLocalString(
                    "create.custom.resource.noFactoryClassName",
                    "No Factory class name defined for Custom Resource."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                if (((BindableResource) resource).getJndiName().equals(jndiName))
                {
                    report.setMessage(localStrings.getLocalString(
                            "create.custom.resource.duplicate.1",
                            "Resource named {0} already exists.",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            } 
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {

                    CustomResource newResource =
                            param.createChild(CustomResource.class);
                    newResource.setJndiName(jndiName);
                    newResource.setFactoryClass(factoryClass);
                    newResource.setResType(resType);
                    newResource.setEnabled(enabled.toString());
                    if (description != null) {
                        newResource.setDescription(description);
                    } 
                    if (properties != null) {
                        for (java.util.Map.Entry e : properties.entrySet()) {
                            Property prop = newResource.createChild(
                                    Property.class);
                            prop.setName((String) e.getKey());
                            prop.setValue((String) e.getValue());
                            newResource.getProperty().add(prop);
                        }
                    }
                    param.getResources().add(newResource);
                    return newResource;
                }
            }, resources);

            if (!targetServer.isResourceRefExists(jndiName)) {
                targetServer.createResourceRef(enabled.toString(), jndiName);
            }
            report.setMessage(localStrings.getLocalString(
                    "create.custom.resource.success",
                    "Custom Resource {0} created.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString(
                    "create.custom.resource.fail",
                    "Unable to create custom resource {0}.", jndiName) +
                    " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
    }
}
