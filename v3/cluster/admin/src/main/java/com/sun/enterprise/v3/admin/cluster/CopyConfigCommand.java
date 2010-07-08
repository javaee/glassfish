/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;


import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Properties;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.*;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.GenericCrudCommand;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.*;

/**
 *  This is a remote command that copies a config to a destination config.
 * Usage: copy-config
 	[--systemproperties  (name=value)[:name=value]*]
	source_configuration_name destination_configuration_name
 * @author Bhakti Mehta
 */
@Service(name = "copy-config")
@I18n("copy.config.command")
@Scoped(PerLookup.class)
public final class CopyConfigCommand implements AdminCommand {

    @Param(primary=true, multiple=true)
    List<String> configs;

    @Inject
    Domain domain;

    @Param(optional=true, separator=':')
    String systemproperties;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CopyConfigCommand.class);

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        SystemPropertyBag spb = null;

        if (configs.size() != 2) {
            report.setMessage(localStrings.getLocalString("Config.badConfigNames",
                    "You must specify a source and destination config") + "\n" +
                    localStrings.getLocalString("Config.copyConfigUsage",
                     "Usage copy-config \\n[--systemproperties (name=value)[:name=value]*]\\nsource_configuration_name destination_configuration_name"
       
            ));

            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        final String srcConfig = configs.get(0);
        final String destConfig = configs.get(1);
        //Get the config from the domain
        //does the src config exist
        final Config config = domain.getConfigNamed(srcConfig);
        if (config == null ){
            report.setMessage(localStrings.getLocalString(
                    "Config.noSuchConfig", "Config {0} does not exist.", srcConfig));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        
        //does dest config exist
        Config destinationConfig = domain.getConfigNamed(destConfig);
        if (destinationConfig != null ){
            report.setMessage(localStrings.getLocalString(
                    "Config.configExists", "Config {0} already exists.", destConfig));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //Copy the config
        final String configName = destConfig ;

        try {
            final Config newCopy = (Config) ConfigSupport.apply(new SingleConfigCode<Configs>(){
                @Override
                public Object run(Configs configs ) throws PropertyVetoException, TransactionFailure {
                    final Config destCopy = (Config) config.deepCopy(configs);
                    if (systemproperties != null) {
                        final Properties properties = GenericCrudCommand.convertStringToProperties(systemproperties,':');

                        for (final Object key : properties.keySet()) {
                            final String propName = (String) key;
                            //cannot update a system property so remove it first
                            List<SystemProperty> sysprops = destCopy.getSystemProperty() ;
                            for (SystemProperty sysprop:sysprops) {
                                if (propName.equals(sysprop.getName())) {
                                    sysprops.remove(sysprop);
                                    break;
                                }

                            }

                            //Currently the systemproperties is not working due to this bug 12311
                            SystemProperty newSysProp = destCopy.createChild(SystemProperty.class);
                            newSysProp.setName(propName);
                            newSysProp.setValue(properties.getProperty(propName));
                            destCopy.getSystemProperty().add(newSysProp);
                        }
                    }
                    destCopy.setName(configName);
                    configs.getConfig().add(destCopy);
                    return destCopy;

                }
            }   ,domain.getConfigs());

        } catch (TransactionFailure e) {
            e.printStackTrace();
            report.setMessage(
                localStrings.getLocalString(
                    "Config.copyConfigError",
                    "CopyConfig error") +
                "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

}
