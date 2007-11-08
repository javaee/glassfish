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
package com.sun.enterprise.ee.admin.lbadmin.lifecycle;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.appserv.management.ext.lb.LoadBalancer;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContextEventListener;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.management.ext.lb.LoadBalancerImpl;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.logging.ee.EELogDomains;
import java.util.List;
import java.util.logging.Level;

import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * This class listens to the element change event 
 *
 * @author Harsha R A
 * @since  Appserver 9.0
 */
public class LoadbalancerAdminEventListener implements ConfigContextEventListener{

    /**
     * Default constructor.
     */
    public LoadbalancerAdminEventListener() { 
    }

    /**
     * before config add, delete, set, update or flush. type is in ev
     */
    public void postAccessNotification(ConfigContextEvent ev) 
    {
    }

    /**
     * after config add, delete, set, update or flush. type is in ev
     */

    public void preChangeNotification(ConfigContextEvent ev) {
    }

    /**
     * before config add, delete, set, update or flush. type is in ev
     */

    public void preAccessNotification(ConfigContextEvent ev) {
    }

   /**
    * after config add, delete, set, update or flush. type is in ev
     * applies the changes to the applicable load balancers
     */
    public void postChangeNotification(ConfigContextEvent ev) {
        String choice = ev.getChoice();
        if(choice == null)
            return;
        if(!choice.equals("ADD") && !choice.equals("DELETE")
        && !choice.equals("UPDATE") && !choice.equals("SET") )
            return;
        try {
            //ignore the events that are generated as part of this method call
            ConfigContext evctx = ev.getConfigContext();
            List<ConfigChange> changes = evctx.getConfigChangeList();
            for(ConfigChange change : changes) {
                String xpath = change.getXPath();
                if(xpath!=null && 
                     xpath.indexOf(LoadBalancerImpl.LAST_APPLIED_PROPERTY)!= -1)
                    return;
            }
        
            //get all the load balancers
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
            .getAdminConfigContext();
            com.sun.enterprise.config.serverbeans.LoadBalancers loadbalancers =
                    ((Domain)ctx.getRootConfigBean()).
                    getLoadBalancers();
            if(loadbalancers == null)
                return;
            com.sun.enterprise.config.serverbeans.LoadBalancer[] lbs =
                    loadbalancers.getLoadBalancer();
            if(lbs == null)
                return;
            
            //loop through the load balancers
            for(com.sun.enterprise.config.serverbeans.LoadBalancer lb : lbs){
                if(lb == null)
                    continue;
                if(!lb.isAutoApplyEnabled())
                    continue;
                String lbName = lb.getName();
                
                //get the AMX mbean for this load balancer
                ObjectName loadBalancerObjName = null;
                try{
                    loadBalancerObjName =
                            new ObjectName(LoadBalancerImpl.LOADBALANCER_OBJECT_NAME+lbName);
                } catch ( MalformedObjectNameException e ){
                    if(_logger.isLoggable(Level.FINE))
                        e.printStackTrace();
                    continue;
                }
                LoadBalancer loadBalancer =
                        (LoadBalancer) ProxyFactory.getInstance(
                        MBeanServerFactory.getMBeanServer()).getProxy(loadBalancerObjName);
                
                //apply changes if required
                if(loadBalancer.isApplyChangeRequired()) {
                    loadBalancer.applyLBChanges();
                    _logger.info(_sMgr.getString("http_lb_admin_applychanges_done",lbName));
                }   
            }
        }catch(Exception e){
            Throwable rootException = ExceptionUtil.getRootCause(e);

            _logger.warning(_sMgr.getString("http_lb_admin_applychanges_notdone",rootException.getLocalizedMessage()));
            if(_logger.isLoggable(Level.FINE))
                e.printStackTrace();
        }
        
    }

    // ---- VARIABLES - PRIVATE --------------------------------------------
    private static Logger _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
    private static final StringManagerBase _sMgr =
        StringManagerBase.getStringManager(_logger.getResourceBundleName());

}
