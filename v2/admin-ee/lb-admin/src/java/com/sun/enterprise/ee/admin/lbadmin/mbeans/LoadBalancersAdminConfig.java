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
package com.sun.enterprise.ee.admin.lbadmin.mbeans;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
	
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.LoadBalancers;
import com.sun.enterprise.config.serverbeans.LoadBalancer;

import com.sun.logging.ee.EELogDomains;
import java.util.logging.Logger;
import java.util.logging.Level; 
import java.util.List;
import java.util.ArrayList;

import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;

/**
 * MBean representing configuration for the load-balancers element.
 *
 * @author Nandini Ektare
 */
public final class LoadBalancersAdminConfig extends BaseConfigMBean {

    /**
     * List of available Loadbalancers will be returned.
     *
     * @param  lbConfigName lb config name
     *
     * @return the list of load balancer names
     *
     * @throws MBeanException   If the operation fails
     */
    public String[] listLoadBalancers(String lbConfigName) 
        throws MBeanException {
        _logger.log(Level.FINE, "[LBAdminMBean] listLBConfigs called" );
        try{
        ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                                    .getAdminConfigContext();
        Domain domain = (Domain)ctx.getRootConfigBean();
        LoadBalancers loadbalancers = ((Domain)ctx.getRootConfigBean())
                                                    .getLoadBalancers();
        if (loadbalancers == null ) {
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.NoLbConfigs"));
            return null;
        }

        String[] names = null;
        List<String> namesList = new ArrayList<String>();
            LoadBalancer[] lbArray = loadbalancers.getLoadBalancer();
            if (lbArray.length == 0) {
                _logger.log(Level.INFO, _sMgr.getString(
                        "http_lb_admin.NoLbConfigs"));
                return null;
            }
            for (LoadBalancer lb : lbArray) {
                String configName = lb.getLbConfigName();
                if(lbConfigName == null || lbConfigName.equals("")
                        || lbConfigName.equals(configName))
                    namesList.add(lb.getName());
            }
            names = new String[namesList.size()];
            names = namesList.toArray(names);
            return names;
        }catch(Exception e){
            throw new MBeanException(e);
        }
    }

    //------- PRIVATE VARIABLES --------------------
    private static final StringManager _strMgr = 
        StringManager.getManager(LoadBalancersAdminConfig.class);

    private static Logger _logger = Logger.getLogger(
			EELogDomains.EE_ADMIN_LOGGER);
    
    private static final StringManagerBase _sMgr =
        StringManagerBase.getStringManager(_logger.getResourceBundleName());
}