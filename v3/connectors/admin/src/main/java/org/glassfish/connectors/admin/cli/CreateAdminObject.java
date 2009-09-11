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
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import static org.glassfish.resource.common.ResourceConstants.*;
import org.glassfish.resource.common.ResourceStatus;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.HashMap;

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

    @Param(name="classname", optional=true)
    String className;

    @Param(name="raname")
    String raName;

    @Param(optional=true, defaultValue="true")
    Boolean enabled;

    @Param(optional=true)
    String description;
    
    @Param(name="property", optional=true, separator=':')
    Properties properties;
    
    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="jndi_name", primary=true)
    String jndiName;
    
    @Inject
    Resources resources;
    
    @Inject
    Domain domain;

    @Inject
    private Habitat habitat;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        Server targetServer = domain.getServerNamed(target);
        
        HashMap attrList = new HashMap();
        attrList.put(RES_TYPE, resType);
        attrList.put(ADMIN_OBJECT_CLASS_NAME, className);
        attrList.put(ENABLED, enabled.toString());
        attrList.put(JNDI_NAME, jndiName);
        attrList.put(ServerTags.DESCRIPTION, description);
        attrList.put(RES_ADAPTER, raName);

        ResourceStatus rs;

        try {
            AdminObjectManager adminObjMgr = habitat.getComponent(AdminObjectManager.class);
            rs = adminObjMgr.create(resources, attrList, properties, targetServer);
        } catch(Exception e) {
            Logger.getLogger(CreateAdminObject.class.getName()).log(Level.SEVERE,
                    "Something went wrong in create-admin-object", e);
            String def = "Admin object: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.admin.object.fail",
                    def, jndiName) + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getMessage() != null) {
                report.setMessage(rs.getMessage());
            } else {
                 report.setMessage(localStrings.getLocalString("create.admin.object.fail",
                    "Admin object {0} creation failed", jndiName, ""));
            }
            if (rs.getException() != null)
                report.setFailureCause(rs.getException());
        }
        report.setActionExitCode(ec);
    }
}
