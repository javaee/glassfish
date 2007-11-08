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

package com.sun.enterprise.ee.cli.commands;

import com.sun.enterprise.cli.commands.GenericCommand;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.i18n.StringManager;
import javax.management.MBeanServerConnection;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.LoadBalancerConfig;
import java.io.File;
import java.util.Vector;

public class ExportLBConfigCommand extends BaseHttpLBCommand {

    private static final String CONFIG_OPTION = "config";
    private static final String LBNAME_OPTION = "lbname";
    private static final StringManager _strMgr = 
            StringManager.getManager(ExportLBConfigCommand.class);

    protected void handleReturnValue(Object retVal) {
        final String exportDirMsg = 
            _strMgr.getString("LBConfigLocation", (String)retVal);
        CLILogger.getInstance().printMessage(exportDirMsg);
    }

    /*
     * Returns the Params from the properties
     * @return params returns params
     */
    protected Object[] getParamsInfo()
        throws CommandException, CommandValidationException
    {
        String configName = getOption(CONFIG_OPTION);
        String lbName = getOption(LBNAME_OPTION);

        checkConfigAndLBNameOptions(configName, lbName);
        //get the config, if lbname is specified
        if (lbName!=null)
        {
            MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(),
                                                    getUser(), getPassword());
            DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
            DomainConfig domainConfig = domainRoot.getDomainConfig();
            LoadBalancerConfig loadBalancerConfig = 
                    domainConfig.getLoadBalancerConfigMap().get(lbName);
            if (loadBalancerConfig==null){
                throw new CommandException( 
                        _strMgr.getString("LoadBalancerConfigNotDefined", 
                                        new Object[] {lbName}));
            }
            configName = loadBalancerConfig.getLbConfigName();
            CLILogger.getInstance().printDebugMessage(
                    "Config name for "+lbName+" is "+configName);
        }
        Vector oprnds    = getOperands();
        String fileName  = null;

        if (oprnds != null && oprnds.size() >= 1) {
            File file = new File((String) oprnds.get(0));
            fileName = file.getPath();
            if (!file.isAbsolute()) {
                fileName = file.getAbsolutePath();
            }
        } 
        CLILogger.getInstance().printDebugMessage("Path = " + fileName);

        return new Object[] {configName, fileName};
    }
}
