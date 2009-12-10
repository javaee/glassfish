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
package com.sun.enterprise.v3.admin;

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
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;
import java.util.Properties;

/**
 * Create System Properties Command
 * 
 * Adds or updates one or more system properties of the domain, configuration, 
 * cluster, or server instance
 * 
 * Usage: create-system-properties [--terse=false] [--echo=false] [--interactive=true] 
 * [--host localhost] [--port 4848|4849] [--secure|-s=true] [--user admin_user] 
 * [--passwordfile file_name] [--target target(Default server)] (name=value)[:name=value]*                                                                       
 *
 * @author Jennifer Chou
 * 
 */
@Service(name="create-system-properties")
@Scoped(PerLookup.class)
@I18n("create.system.properties")
public class CreateSystemProperties implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateSystemProperties.class);
    
    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="name_value", primary=true, separator=':')
    Properties properties;

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

        SystemPropertyBag spb;
        if ("domain".equals(target))
            spb = domain;
        else
            spb = domain.getServerNamed(target); //this is ok for now  (config is not a target as far as v3 FCS is concerned -- take it up later)
        if (spb == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            String msg = localStrings.getLocalString("invalid.target.sys.props",
                    "Invalid target:{0}. Valid targets are ''domain'' and a server named ''server'' (default).", target);
            report.setMessage(msg);
            return;
        }
        String sysPropName = "";
        try {            
            for (final Object key : properties.keySet()) {
                final String propName = (String) key;
                sysPropName = propName;
                    
                ConfigSupport.apply(new SingleConfigCode<SystemPropertyBag>() {

                    public Object run(SystemPropertyBag param) throws PropertyVetoException, TransactionFailure {

                        // update existing system properties
                        // sysProperty.setValue(propValue) doesn't work ? so
                        // remove the property and have it recreated with the new value
                        for (SystemProperty sysProperty : param.getSystemProperty()) {
                            if (sysProperty.getName().equals(propName)) {
                                param.getSystemProperty().remove(sysProperty);
                                break;
                            }
                        }
                        
                        // create system-property
                        SystemProperty newSysProp = param.createChild(SystemProperty.class);
                        newSysProp.setName(propName);
                        newSysProp.setValue(properties.getProperty(propName));
                        param.getSystemProperty().add(newSysProp);                    
                        return newSysProp;
                    }
                }, spb);
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            }
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("create.system.properties.failed",
                    "System property {0} creation failed", sysPropName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        } catch(Exception e) {
            report.setMessage(localStrings.getLocalString("create.system.properties.failed",
                    "System property {0} creation failed", sysPropName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
    }
}
