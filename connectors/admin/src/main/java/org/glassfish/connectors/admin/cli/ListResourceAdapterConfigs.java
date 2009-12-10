/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.config.types.Property;


/**
 * List Resource Adapter Configs command
 *
 */
@Service(name="list-resource-adapter-configs")
@Scoped(PerLookup.class)
@I18n("list.resource.adapter.configs")
public class ListResourceAdapterConfigs implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListResourceAdapterConfigs.class);    

    @Param(name="raname", optional=true)
    String raName;

    @Param(name="verbose", optional=true, defaultValue="false")
    Boolean verbose;

    @Inject
    ResourceAdapterConfig[] resources;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        try {
            HashMap<String, List<Property>> raMap = new HashMap();
            boolean raExists = false;
            for (ResourceAdapterConfig r : resources) {
                if (raName != null && !raName.isEmpty()) {
                    if (r.getResourceAdapterName().equals(raName)) {
                        raMap.put(raName, r.getProperty());
                        raExists = true;
                        break;
                    }
                } else {
                    raMap.put(r.getResourceAdapterName(), r.getProperty());
                }
            }
            if (raName != null && !raName.isEmpty() && !raExists) {
                report.setMessage(localStrings.getLocalString("delete.resource.adapter.config.notfound",
                        "Resource adapter {0} not found.", raName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            if (raMap.isEmpty()) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(localStrings.getLocalString("nothingToList",
                    "Nothing to list."));
            } else {
                /**
                  get the properties if verbose=true. Otherwise return the name.
                 */
                if (verbose) {
                    for (Entry<String, List<Property>> raEntry : raMap.entrySet()) {
                        final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                        part.setMessage(raEntry.getKey());
                        for (Property prop : raEntry.getValue()) {
                            final ActionReport.MessagePart propPart = part.addChild();
                            propPart.setMessage("\t" + prop.getName() + "=" + prop.getValue());
                        }
                    }
                } else {
                    for (String ra : raMap.keySet()) {
                        final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                        part.setMessage(ra);
                    }
                }
            }
        } catch (Exception e) {
            String failMsg = localStrings.getLocalString("list.resource.adapter.configs.fail",
                    "Unable to list resource adapter configs.");
            Logger.getLogger(ListResourceAdapterConfigs.class.getName()).log(Level.SEVERE,
                    failMsg, e);
            report.setMessage(failMsg + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
