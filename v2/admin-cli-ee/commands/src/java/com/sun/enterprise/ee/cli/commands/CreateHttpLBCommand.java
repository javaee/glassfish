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

import com.sun.enterprise.cli.framework.*;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.appserv.management.helper.LBConfigHelper;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.enterprise.ee.admin.lbadmin.mbeans.LbConfigHelper;
import com.sun.appserv.management.ext.lb.LoadBalancer;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.StringTokenizer;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;


public class CreateHttpLBCommand extends BaseHttpLBCommand {

    private static final String TARGET_OPTION = "target";
    private static final String CONFIG_OPTION = "config";
    private static final String AUTO_APPLY_ENABLED_OPTION = "autoapplyenabled";
    private static final String DEVICE_HOST_OPTION = "devicehost";
    private static final String DEVICE_PORT_OPTION = "deviceport";
    private static final String SSL_PROXY_HOST_OPTION = "sslproxyhost";
    private static final String SSL_PROXY_PORT_OPTION = "sslproxyport";
    private static final String PROPERTY_OPTION = "property";
    private static final StringManager _strMgr = 
                            StringManager.getManager(CreateHttpLBCommand.class);
    

    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        if (!validateOptions())
            throw new CommandValidationException("Validation is false");
        
        final String lbName = (String) getOperands().get(0);
        final boolean autoApplyEnabled = 
                        getBooleanOption(AUTO_APPLY_ENABLED_OPTION);
        Map instanceWeights = getInstanceWeightsMap();
        final String target = getOption(TARGET_OPTION);
        try
        {
            MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                                                  getUser(), getPassword());
            DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();

            LBConfigHelper lbconfigHelper = new LBConfigHelper(domainRoot);
            lbconfigHelper.createLoadBalancer(lbName, target, getLBParams(), 
                                                getLBProperties());
            //Now configure the weights for the target cluster
            final boolean isCluster = 
                domainRoot.getDomainConfig().getClusterConfigMap().keySet().contains(target);
            if ( ! isCluster ) 
            {
                //display warning and continue
                _strMgr.getString("WeightCannotApplyToNonCluster", 
                                        new Object[] {target});
            }
            else if (getOption(LBConfigHelper.LB_WEIGHT) != null)
                lbconfigHelper.configureLBWeight(target, instanceWeights);
            //Test the connection if autoapplyenabled is true and 
            // display warning if pinging is unsuccesful
            if (autoApplyEnabled)
            {
                if (!domainRoot.getLoadBalancerMap().get(lbName).testConnection())
                    CLILogger.getInstance().printWarning(_strMgr.getString(
                                                        "CouldNotPingLB", 
                                                        new Object[] {lbName}));
            }
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
						       "CommandSuccessful",
						       new Object[] {name}));
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }        
    }

    
    /*
     * Formulate and Returns the Map of properties
     */
    private Map<String,String> getLBProperties() throws Exception
    {
        String sslHost = getOption(SSL_PROXY_HOST_OPTION);
        String sslPort = getOption(SSL_PROXY_PORT_OPTION);
        
        Map<String,String> properties = new HashMap<String,String>();
        properties.put(PropertiesAccess.PROPERTY_PREFIX + "device-host", 
                        getOption(DEVICE_HOST_OPTION));
        
        final String devicePort = getOption(DEVICE_PORT_OPTION);
        validatePort(devicePort);
        properties.put(PropertiesAccess.PROPERTY_PREFIX + "device-admin-port", 
                        devicePort);
        if (sslHost != null)
            properties.put(PropertiesAccess.PROPERTY_PREFIX + "ssl-proxy-host", 
                            sslHost);
        if (sslPort != null) {
            validatePort(sslPort);
            properties.put(PropertiesAccess.PROPERTY_PREFIX + "ssl-proxy-port", 
                           sslPort);
        }
        
        //Add the properties from --property option
        Properties props = createPropertiesParam(getOption(PROPERTY_OPTION));
        
        if (props != null)
        {
            for (Enumeration e = props.propertyNames() ; e.hasMoreElements() ;) 
            {
                String name = (String) e.nextElement();
                String value = props.getProperty(name);
                properties.put(PropertiesAccess.PROPERTY_PREFIX + name, value);
                CLILogger.getInstance().printDebugMessage(
                        "Property name="+name+", value="+value);
            }        
        }   
        return properties;
    }

    
    /*
     * Formulate and Returns the Map of options
     */
    private Map<String,String> getLBParams() {
        Map<String,String> mLBOptions = new HashMap<String,String>();
        addToOptions(mLBOptions, LBConfigHelper.HTTPS_ROUTING);
        addToOptions(mLBOptions, LBConfigHelper.RESPONSE_TIMEOUT);
        addToOptions(mLBOptions, LBConfigHelper.RELOAD_INTERVAL);
        addToOptions(mLBOptions, LBConfigHelper.MONITOR);
        addToOptions(mLBOptions, LBConfigHelper.ROUTE_COOKIE);
        addToOptions(mLBOptions, LBConfigHelper.HEALTH_CHECKER_URL);
        addToOptions(mLBOptions, LBConfigHelper.HEALTH_CHECKER_INTERVAL);
        addToOptions(mLBOptions, LBConfigHelper.HEALTH_CHECKER_TIMEOUT);
        addToOptions(mLBOptions, LBConfigHelper.AUTO_APPLY_ENABLED);
        addToOptions(mLBOptions, LBConfigHelper.LB_ENABLE_ALL_INSTANCES);
        addToOptions(mLBOptions, LBConfigHelper.LB_ENABLE_ALL_APPLICATIONS);
        addToOptions(mLBOptions, LBConfigHelper.LB_POLICY);
        addToOptions(mLBOptions, LBConfigHelper.LB_POLICY_MODULE);
        return mLBOptions;
    }

    
}
