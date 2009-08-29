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
package org.glassfish.web.admin.cli;

import java.util.List;
import java.util.Properties;
import java.util.Map;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
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

import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpService;
import org.glassfish.api.admin.config.Property;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;

/**
 * Command to create virtual server
 * 
 */
@Service(name="create-virtual-server")
@Scoped(PerLookup.class)
@I18n("create.virtual.server")
public class CreateVirtualServer implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateVirtualServer.class);

    @Param(name="hosts")
    String hosts;
             
    @Param(name="httplisteners", optional=true)
    String httpListeners;

    @Param(name="networklisteners", optional=true)
    String networkListeners;

    @Param(name="defaultwebmodule", optional=true)
    String defaultWebModule;

    @Param(name="state", acceptableValues="on, off", optional=true)
    String state;

    @Param(name="logfile", optional=true)
    String logFile;

    @Param(name="property", optional=true, separator=':')
    Properties properties;

    @Param(name="virtual_server_id", primary=true)
    String virtualServerId;
    
    @Inject
    Configs configs;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        if (networkListeners != null && httpListeners != null) {
            report.setMessage(localStrings.getLocalString("create.virtual.server.both.http.network",
                "Please use only networklisteners", virtualServerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        List <Config> configList = configs.getConfig();
        Config config = configList.get(0);
        HttpService httpService = config.getHttpService();
        
        // ensure we don't already have one of this name
        for (VirtualServer virtualServer: httpService.getVirtualServer()) {
            if (virtualServer.getId().equals(virtualServerId)) {
                report.setMessage(localStrings.getLocalString("create.virtual.server.duplicate",
                        "Virtual Server named {0} already exists.", virtualServerId));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;                    
            }
        }
        
        try {
            ConfigSupport.apply(new SingleConfigCode<HttpService>() {

                public Object run(HttpService param) throws PropertyVetoException, TransactionFailure {
                    boolean docrootAdded = false;
                    boolean accessLogAdded = false;
                    
                    VirtualServer newVirtualServer = param.createChild(VirtualServer.class);
                    newVirtualServer.setId(virtualServerId);
                    newVirtualServer.setHosts(hosts);
                    newVirtualServer.setNetworkListeners(httpListeners);
                    newVirtualServer.setDefaultWebModule(defaultWebModule);
                    newVirtualServer.setState(state);
                    newVirtualServer.setLogFile(logFile);

                    // 1. add properties
                    // 2. check if the access-log and docroot properties have
                    //    been specified. We need to add those with default 
                    //    values if the properties have not been specified.
                    if (properties != null) {
                        for (Map.Entry entry : properties.entrySet()) {
                            Property property = newVirtualServer.createChild(Property.class);
                            String pn = (String)entry.getKey();
                            property.setName(pn);
                            property.setValue((String)entry.getValue());
                            newVirtualServer.getProperty().add(property);
                            if ("docroot".equals(pn))
                                docrootAdded = true;
                            if ("accesslog".equals(pn))
                                accessLogAdded = true;
                        }
                    }
                    if (!docrootAdded) {
                        Property drp = newVirtualServer.createChild(Property.class);
                        drp.setName("docroot");
                        drp.setValue("${com.sun.aas.instanceRoot}/docroot");
                        newVirtualServer.getProperty().add(drp);
                    }
                    
                    if (!accessLogAdded) {
                        Property alp = newVirtualServer.createChild(Property.class);
                        alp.setName("accesslog");
                        alp.setValue("${com.sun.aas.instanceRoot}/logs/access");
                        newVirtualServer.getProperty().add(alp);
                    }
                    
                    param.getVirtualServer().add(newVirtualServer);
                    return newVirtualServer;
                }
            }, httpService);

        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("create.virutal.server.fail", "{0} create failed ", virtualServerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
