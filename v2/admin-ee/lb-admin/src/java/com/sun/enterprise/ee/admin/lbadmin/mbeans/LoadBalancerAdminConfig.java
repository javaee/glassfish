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
import com.sun.enterprise.ee.admin.lbadmin.writer.LbConfigPublisher;
import com.sun.enterprise.ee.admin.lbadmin.writer.LbConfigWriter;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.logging.ee.EELogDomains;
import java.util.logging.Logger;
import java.util.logging.Level; 
import java.util.List;
import java.util.ArrayList;

import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;

/**
 * MBean representing configuration for the load-balancer element.
 *
 * @author Nandini Ektare
 */
public final class LoadBalancerAdminConfig extends BaseConfigMBean {

    /**
      Applies changes for all the targets - servers/clusters
     */
    public void applyLBChanges() throws MBeanException {
        _logger.log(Level.FINE, 
            "[LoadBalancerAdminConfig] applyLBChanges called " 
            );
        String configName = null;
        String lbName = null;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                                .getAdminConfigContext();
            com.sun.enterprise.config.serverbeans.LoadBalancers lbs =
                    ((Domain)ctx.getRootConfigBean()).
                    getLoadBalancers();
            if(lbs == null)
                throw new MBeanException(new Exception("No Loadbalancers configured"));
            lbName = (String)getAttribute("name");
            com.sun.enterprise.config.serverbeans.LoadBalancer lb =
                    lbs.getLoadBalancerByName(lbName);
            if(lb == null)
                throw new MBeanException(new Exception("No Loadbalancer configured by name " + lbName));
            configName = lb.getLbConfigName();
            LbConfigPublisher lbp = new LbConfigPublisher(ctx, configName, lbName);
            lbp.publish();
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.ExportConfig", configName, lbName));
        } catch (Exception e) {
            String msg = _strMgr.getString("LbExportFailed", 
                        configName, lbName);
            throw new MBeanException(e, msg);
        }
        
    }
                                                                                                                                                             
    /**
      Applies changes for a particular the targets - server/cluster
     */
    public void applyLBChanges(String target) throws MBeanException {
    }
                                                                                                                                                             
    /**
      @return true if there are pending changes for this LB
     */
    public boolean isApplyChangeRequired() throws MBeanException {
        return true;
    }
    
    //------- PRIVATE VARIABLES --------------------
    private static final StringManager _strMgr = 
        StringManager.getManager(HTTPLBAdminConfigMBean.class);

    private static Logger _logger = Logger.getLogger(
			EELogDomains.EE_ADMIN_LOGGER);
    
    private static final StringManagerBase _sMgr =
        StringManagerBase.getStringManager(_logger.getResourceBundleName());
}
