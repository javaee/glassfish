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
package com.sun.enterprise.admin.event;

import com.sun.logging.LogDomains;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;

import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventResult;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.admin.event.ElementChangeHelper;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Restart event Helper - class providing support for informing
 * server(s) to set its(their) restart required state to true or false.
 *
 * @author: Satish Viswanatham
 */
public class RRPersistenceHelper{

    // i18n StringManager
    private static final Logger _logger     = 
                        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    
    public RRPersistenceHelper() {
    }

    /**
     * Set the restart required status in the given instance name
     * if the result code is not success
     *
     * @param evt        Admin Event that is being sent 
     * @param result     Result of the Admin Event
     */
    public void setRestartRequiredForServer(AdminEvent event, 
        AdminEventResult result ) {

        if (AdminService.getAdminService() == null) {
            // This instance does not have admin service return null
            return;
        }

        String resCode = null;
        // the follows tests this is server or DAS
        try {
            if (result != null) {
                resCode = result.getResultCode();
            }
            if ((resCode == null) || 
                (!resCode.equals(AdminEventResult.SUCCESS)) ){

                if (event != null) {
                    setRestartRequired( event.getInstanceName(), true);
                }
            }
        } catch (Throwable t) {
           _logger.log(Level.INFO, "event.exception_during_restart_reset",t);
        }

    }

    /**
     * Set the restart required status in the given instance name
     *
     * @param inst       Local server instance name 
     * @param restart    boolen value for restart required flag
     */
    public void setRestartRequired(String inst, boolean restart) {

        try {
            RMIClient client = AdminChannel.getRMIClient(inst);
            if (client == null) {
                _logger.log(Level.INFO, "event.rmi_client_not_found");
            } else {
                client.setRestartNeeded(restart); 
            }
        } catch (Throwable t) {
            _logger.log(Level.INFO,
                "event.exception_during_restart_reset", t);
        }
    }
    
    /**
     * Set the restart required status in the current instance.
     * Server Runtime MBean calls this method to set the restart 
     * required state from DAS. 
     *
     * @param restart    boolen value for restart required flag
     */
    public void setRestartRequired(boolean restart) {
        String inst =  null;
        ServerContext svrCtx = ApplicationServer.getServerContext();
        if (svrCtx !=null) {
            inst = svrCtx.getInstanceName();
        }
        setRestartRequired(inst, restart);
    }

}
