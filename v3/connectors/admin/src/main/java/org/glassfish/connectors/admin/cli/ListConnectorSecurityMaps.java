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
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.SecurityMap;
import com.sun.enterprise.config.serverbeans.BackendPrincipal;
import com.sun.enterprise.util.LocalStringManagerImpl;
//import com.sun.enterprise.v3.common.PropsFileActionReporter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List Connector Security Maps
 *
 */
@Service(name="list-connector-security-maps")
@Scoped(PerLookup.class)
@I18n("list.connector.security.maps")
public class ListConnectorSecurityMaps extends ConnectorSecurityMap implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ListConnectorSecurityMaps.class);

    @Param(name="securitymap", optional=true)
    String securityMap;

    @Param(name="verbose", optional=true, defaultValue="false")
    Boolean verbose;

    @Param(name="pool-name", primary=true)
    String poolName;

    @Inject
    ConnectorConnectionPool[] ccPools;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final ActionReport.MessagePart mp = report.getTopMessagePart();

        /* Issue 5918 Used in ManifestManager to keep output sorted */
        //try {
        //    PropsFileActionReporter reporter = (PropsFileActionReporter) report;
        //    reporter.useMainChildrenAttribute(true);
        //} catch(ClassCastException e) {
            // ignore this is not a manifest output.
        //}

        if (!doesPoolNameExist(poolName, ccPools)) {
            report.setMessage(localStrings.getLocalString("create.connector.security.map.noSuchPoolFound",
                    "Specified connector connection pool {0} does not exist. Please specify a valid pool name.",
                    poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (securityMap != null) {
            if (!doesMapNameExist(poolName, securityMap, ccPools)) {
                report.setMessage(localStrings.getLocalString("list.connector.security.maps.securityMapNotFound",
                        "Security map {0} does not exist for connector connection pool {1}. Please give a valid map name.",
                        securityMap, poolName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        try {
            final List<SecurityMap> securityMaps = getAllSecurityMapsForPool(poolName, ccPools);
            if (securityMaps != null && !securityMaps.isEmpty()) {
                if (securityMap == null && verbose) {
                    for (SecurityMap sm : securityMaps) {
                        listSecurityMapDetails(sm, mp);
                    }
                } else if (securityMap == null && !verbose) {
                    //print the map names .....
                    for (SecurityMap sm : securityMaps) {
                        listSecurityMapNames(sm, mp);
                    }
                } else {
                    // map name is not null, verbose is redundant when security map is specified
                    for (SecurityMap sm : securityMaps) {
                        if (sm.getName().equals(securityMap)) {
                            //if (verbose) {
                                listSecurityMapDetails(sm, mp);
                                break;
                            //} else {
                            //    listSecurityMapNames(sm, mp);
                            //    break;
                            //}
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ListConnectorSecurityMaps.class.getName()).log(Level.SEVERE,
                    "list-connector-security-maps failed", e);
            report.setMessage(localStrings.getLocalString("" +
                    "list.connector.security.maps.fail",
                    "Unable to list security map {0} for connector connection pool {1}", securityMap, poolName) + " " +
                    e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void listSecurityMapNames(SecurityMap sm, ActionReport.MessagePart mp) {
        final ActionReport.MessagePart part = mp.addChild();
        part.setMessage(sm.getName());
    }

    private void listSecurityMapDetails(SecurityMap sm, ActionReport.MessagePart mp) {
        List<String> principalList = sm.getPrincipal();
        List<String> groupList = sm.getUserGroup();
        BackendPrincipal bp = sm.getBackendPrincipal();

        final ActionReport.MessagePart partSM = mp.addChild();
        partSM.setMessage(sm.getName());

        final ActionReport.MessagePart partPG = partSM.addChild();
        if (!principalList.isEmpty()) {
            partPG.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.principals","\tPrincipal(s)"));
        }
        if (!groupList.isEmpty()) {
            partPG.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.groups","\tUser Group(s)"));
        }

        for (String principal : principalList) {
            final ActionReport.MessagePart partP = partPG.addChild();
            partP.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.eisPrincipal",
                    "\t\t"+principal, principal));
        }

        for (String group : groupList) {
            final ActionReport.MessagePart partG = partPG.addChild();
            partG.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.eisGroup",
                    "\t\t"+group, group));
        }

        final ActionReport.MessagePart partBP = partPG.addChild();
            partBP.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.backendPrincipal",
                    "\t"+"Backend Principal"));
        final ActionReport.MessagePart partBPU = partBP.addChild();
            partBPU.setMessage(localStrings.getLocalString(
                    "list.connector.security.maps.username",
                    "\t\t"+"User Name = "+bp.getUserName(), bp.getUserName()));
            
        if (bp.getPassword() != null && !bp.getPassword().isEmpty()) {
            final ActionReport.MessagePart partBPP = partBP.addChild();
                partBPP.setMessage(localStrings.getLocalString(
                        "list.connector.security.maps.password",
                        "\t\t"+"Password = "+bp.getPassword(), bp.getPassword()));
        }
        
    }
}
