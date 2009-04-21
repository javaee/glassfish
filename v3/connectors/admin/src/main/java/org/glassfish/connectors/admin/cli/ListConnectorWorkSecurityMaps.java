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
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.config.serverbeans.GroupMap;
import com.sun.enterprise.config.serverbeans.PrincipalMap;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.WorkSecurityMap;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List Connector Work Security Maps
 *
 */
@Service(name="list-connector-work-security-maps")
@Scoped(PerLookup.class)
@I18n("list.connector.work.security.maps")
public class ListConnectorWorkSecurityMaps implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListConnectorWorkSecurityMaps.class);

    @Param(name="securitymap")
    String securityMap;

    @Param(name="resource-adapter-name", primary=true)
    String raName;

    @Inject
    Resources resources;

    @Inject
    WorkSecurityMap[] workSecurityMaps;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            boolean foundWSM = false;
            for (WorkSecurityMap wsm : workSecurityMaps) {
                if (wsm.getName().equals(securityMap) &&
                        wsm.getResourceAdapterName().equals(raName)) {
                    List<PrincipalMap> principalList = wsm.getPrincipalMap();
                    List<GroupMap> groupList = wsm.getGroupMap();

                    report.setMessage(localStrings.getLocalString(
                        "list.connector.work.security.maps.workSecurityMap",
                        "Work security map {0} for resource adapter {1}", securityMap, raName));
                    
                    for (PrincipalMap map : principalList) {
                        final ActionReport.MessagePart part =
                            report.getTopMessagePart().addChild();
                            part.setMessage(localStrings.getLocalString(
                        "list.connector.work.security.maps.eisAndMappedPrincipal",
                        "eis principal={0}, mapped principal={1}",
                                    map.getEisPrincipal(), map.getMappedPrincipal()));
                    }
                    
                    for (GroupMap map : groupList) {
                        final ActionReport.MessagePart part =
                            report.getTopMessagePart().addChild();
                            part.setMessage(localStrings.getLocalString(
                        "list.connector.work.security.maps.eisAndMappedGroup",
                        "eis group={0}, mapped group={1}",
                                    map.getEisGroup(), map.getMappedGroup()));
                    }
                    foundWSM = true;
                    break;
                }
            }

            if (!foundWSM) {
                 report.setMessage(localStrings.getLocalString(
                        "list.connector.work.security.maps.workSecurityMapNotFound",
                        "Work security map {0} for resource adapter {1} not found", securityMap, raName));
            }

        } catch (Exception e) {
            Logger.getLogger(DeleteConnectorWorkSecurityMap.class.getName()).log(Level.SEVERE,
                    "list-connector-work-security-maps failed", e);
            report.setMessage(localStrings.getLocalString("" +
                    "list.connector.work.security.maps.fail",
                    "Unable to list connector work security map {0} for resource adapter {1}", securityMap, raName) + " " +
                    e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
