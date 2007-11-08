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
package com.sun.enterprise.ee.admin.event.pluggable;

import com.sun.logging.ee.EELogDomains;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;

import com.sun.enterprise.ee.admin.proxy.InstanceProxy;
import com.sun.enterprise.ee.admin.mbeanapi.ServerRuntimeMBean;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.admin.event.ElementChangeHelper;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.ee.admin.event.EEEventDispatcher;
import com.sun.enterprise.admin.event.pluggable.RestartEventHelper;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Restart event Helper - class providing support for informing
 * server(s) to set its(their) restart required state to true or false.
 * 
 * @author: Satish Viswanatham
 */
public class EERestartEventHelper implements RestartEventHelper{

    // i18n StringManager
    private static Logger _logger     = null;
    
    public EERestartEventHelper() {
    }

    /**
     * Given a config change list, this method figures out if that list contains
     * any non dynamic recofigurable changes. If there is any change which can
     * not be applied dynamically, if sets the restart status on the appropriate
     * target servers.
     *
     * @param ctx        Config context of DAS 
     * @param list       config change list
     */
    public void setRestartRequiredForTarget(ConfigContext ctx, 
            ArrayList configChangeList ) {
   
        try {
            AdminService admServ = AdminService.getAdminService();

            if ((admServ != null) && (admServ.isDas())) {
                Set nonDynSet = ElementChangeHelper.
                    getXPathesForDynamicallyNotReconfigurableElements(
                    configChangeList);

                Iterator iter = nonDynSet.iterator();
                if (iter == null) {
                    getLogger().log(Level.FINE, 
                        "EERestartEventHelper: All changes were dynamically reconfigurable");
                    return;
                }

                Set targetSet = new HashSet();
                TargetBuilder tgtBldr = new TargetBuilder();
                while(iter.hasNext()) {
                    String xpath = (String) iter.next();
                    String target = null;
                    
                    try {
                        target = tgtBldr.getTargetNameForXPath(
                                        xpath,ctx,true);
                        targetSet.add(target);
                    } catch (Exception e) {
                        // ignore if this is not a valid target
                        getLogger().log(Level.INFO, 
                            "eeadmin.no_target_for_xpath", xpath);
                    }
                }

                // resolve the target names into server names
                EEEventDispatcher eeDisp = new EEEventDispatcher();
                Set svrSet = eeDisp.resolveTargets(ctx, targetSet);

                // set restart required to false on those servers
                setRestartRequiredForTargetInternal(ctx, svrSet, true);
            }
        } catch (Throwable t) {
            getLogger().log(Level.INFO,"eeadmin.exception_during_restart_reset",
                       t);
        }

    }

    /**
     * For each physical end point, this method makes a MBean proxy call
     * to the server instance to set the restart required flag.
     *
     * @param  ctx  admin config context
     * @param  targets  set of server instance names
     * @param  restart  true if restart required is needed
     */
    private void setRestartRequiredForTargetInternal(ConfigContext ctx,
            Set  targets, boolean restart) {

        Iterator iter = null;
        if (targets != null) {
            iter = targets.iterator();
        }

        if (iter == null) {
            getLogger().log(Level.FINE, 
                "EERestartEventHelper: No targets found for the configuration changes");
            return;
        }

        while (iter.hasNext()) {
            String inst = (String) iter.next();
            setRestartRequiredRemote(ctx, inst, true);
        }
    }

    /**
     * Invokes the remote server instance MBean proxy.
     * 
     * @param  ctx  admin config context
     * @param  inst  name of the server instance
     * @param  restartFlag  true if restart required is needed
     */
    void setRestartRequiredRemote(ConfigContext ctx, String inst, 
            boolean restartFlag) {

        try {
            // only deals with server instance now
            if (ServerHelper.isAServer(ctx, inst)) {
                ServerRuntimeMBean runtimeMBean =
                    InstanceProxy.getInstanceProxy(inst);
                 runtimeMBean.setRestartRequired(restartFlag);
             } else {
                // FIXME: handle node agent end points
             }
        } catch (Throwable t) {
            getLogger().log(Level.FINE,"eeadmin.setrestart.exception", t);
            // ignore if it can not get status
        }
    }

    private static Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }
}
