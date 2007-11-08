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
package com.sun.enterprise.ee.admin.event;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import com.sun.enterprise.admin.event.EventDispatcher;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.AdminContext;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.config.ConfigException;

/**
 * Dispatches an event to a target. This version of the dispatcher 
 * can forward event to remote server instances. Each server instance
 * will have an Admin Event Multicaster. This will work as a fat pipe 
 * between the instances.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class EEEventDispatcher implements EventDispatcher {

    /**
     * Dispatches the event to the event target.
     *
     * @param  event  event to be sent
     * @return result from each end point of the event target
     */
    public AdminEventResult dispatch(AdminEvent event) {

        // Throws an exception if an event gets into an infinite loop. 
        // This should never happen.
        if (!event.isValidHopCount()) {
            String msg = _strMgr.getString("invalid.event.hop.count");
                throw new RuntimeException(msg);
        }

        AdminEventResult result = null;

        try { 
            String t = event.getTargetDestination();
            assert (t != null);

            EndPoint[] endPoints = resolveTarget(t);

            if (endPoints != null) {
                DispatchMgr mgr = new DispatchMgr(event, endPoints);

                // increments the number of times this event has been forwarded
                event.incrementHopCount();

                result = mgr.forward();
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING,"eeadmin.eedispatcher.exception",e);
            if (result != null) {
                result.setResultCode(result.ERROR);
                if (event != null) {
                    result.addException(event.getTargetDestination(), e);
                }
            }
        }

        return result;
    }

    /**
     * Returns the logger of this instance.
     *
     * @return  logger for this instance
     */
    private static Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }

    /**
     * Resolves the given set of targets to zero or more server names.
     *
     * @param   targets     targets to be resolved
     * @return  zero or more server names
     */
    public Set resolveTargets(ConfigContext ctx, Set targets) 
        throws ConfigException {

        // get the iterator on the targets
        Iterator iter = null;
        if (targets != null) {
            iter = targets.iterator();
        }

        // return null if there are not target
        if (iter == null) {
            getLogger().log(Level.FINE, 
                "No targets passed for EEDispatcher.resolveTarget");
            return null;
        }

        // create a result server name set
        Set svrSet = new HashSet();
        
        while (iter.hasNext()) {

            // get the next target name
            String tgtName = (String)iter.next();

            // get the end points in that target
            EndPoint[] epts = resolveTarget(tgtName);
            
            // if this target does not have end points, continue to next target
            if (epts == null) {
                getLogger().log(Level.FINE, 
                    " EEEventDispatcher: No servers exist in target " + 
                    tgtName);
                continue;
            }

            // get the servers information of the end points
            // and adding to the set
            for (int svrIdx=0; svrIdx<epts.length; svrIdx++) {
                String inst = epts[svrIdx].getHost();

                svrSet.add(inst);
            }
        }
        return svrSet;
    }

    /**
     * Resolves the given target to zero or more end points.
     *
     * @param   t  target of the event
     * @return  resolved end points 
     */
    EndPoint[] resolveTarget(String t) throws ConfigException {

        EndPoint[] ePoints  = null;
        ConfigContext ctx   = null;

        
        if ( AdminService.getAdminService() != null ) {
            AdminContext aCtx=AdminService.getAdminService().getAdminContext();
            ctx = aCtx.getAdminConfigContext();
        } else {
            ctx = ApplicationServer.getServerContext().getConfigContext();
        }
        
        if ( isCluster(t, ctx) ) { // cluster destination

            Server[] servers = 
                ServerHelper.getServersInCluster(ctx, t);

            ePoints = TargetHelperBase.createEndPoints(servers, ctx);

        } else if ( isServer(t, ctx) ) { // server destination

            Server server = 
                ServerHelper.getServerByName(ctx, t);

            ePoints = new EndPoint[1];
            ePoints[0] = new EndPoint(server, ctx);

        } else if ( isConfig(t, ctx) ) {  // config destination

            Server[] servers=ServerHelper.getServersReferencingConfig(ctx, t); 
            ePoints = TargetHelperBase.createEndPoints(servers, ctx);

        } else if (TargetHelperBase.isResourceTarget(t)) { // resource

            ResourceTargetHelper rth = new ResourceTargetHelper(t, ctx);
            ePoints = rth.getEndPoints();

        } else if (TargetHelperBase.isApplicationTarget(t)) { // application

            ApplicationTargetHelper ath = new ApplicationTargetHelper(t, ctx);
            ePoints = ath.getEndPoints();

        } else {  // domain destination

            //throw new UnsupportedOperationException();
            Domain domain = ConfigAPIHelper.getDomainConfigBean(ctx);
            Server[] servers=domain.getServers().getServer();
            ePoints = TargetHelperBase.createEndPoints(servers, ctx);
        }

        return ePoints;
    }

    /**
     * Returns true if the target is a cluster.
     *
     * @param  target  target for this event
     * @param  ctx     config context 
     * @return true if target is a cluster
     */
    private boolean isCluster(String target, ConfigContext ctx) {
        try {
            return ClusterHelper.isACluster(ctx, target);
        } catch (ConfigException e) {
            return false;
        }

    }

    /**
     * Returns true if the target is a config.
     *
     * @param  target  target for this event
     * @param  ctx     config context
     * @param  true if target is a config
     */
    private boolean isConfig(String target, ConfigContext ctx) {
        try {
            return ConfigAPIHelper.isAConfig(ctx, target);
        } catch (ConfigException e) {
            return false;
        }
    }

    /**
     * Returns trus if the given target is a server.
     *
     * @param  target  target for this event
     * @param  ctx     config context 
     * @return true if the target is a server
     */
    private boolean isServer(String target, ConfigContext ctx) {
        try {
            return ServerHelper.isAServer(ctx, target);
        } catch (ConfigException e) {
            return false;
        }
    }

    // ---- INSTANCE VARIABLES - PRIVATE -------------------------------
    private static Logger _logger     = null;             
    private static final StringManager _strMgr = 
        StringManager.getManager(EEEventDispatcher.class);
}
