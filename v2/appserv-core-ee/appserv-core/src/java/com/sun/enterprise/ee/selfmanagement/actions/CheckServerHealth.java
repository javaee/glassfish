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

/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package com.sun.enterprise.ee.selfmanagement.actions;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.logging.LogDomains;


import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CheckServerHealth implements Runnable {
    
    /** Logger for self management service */
    private static Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    /** Server instance to check */
    private Instance instance = null;
    
        
    public CheckServerHealth(Instance value) {
        instance = value;
    }
    
    public void run() {
        Boolean isHealthy = false;
        try {
            ArrayList<HttpListener> enabledListeners = new ArrayList<HttpListener>();
          
            Server serv = instance.getServer();
            ConfigContext cfgCtx = InstanceHangAction.configCtx;
            Config cfg = ServerHelper.getConfigForServer(cfgCtx, serv.getName());
        
            // get http-listeners on this server
            HttpService httpSrv = cfg.getHttpService();
            HttpListener[] listeners = httpSrv.getHttpListener();
            for (HttpListener listener : listeners) {
                if (listener.isEnabled()) {
                    enabledListeners.add(listener);
                    //count++;          
                }
            }
        
            int size = enabledListeners.size();
            
            ExecutorService exSrv = Executors.newFixedThreadPool(size);
            ArrayList<Future<Boolean>> checkTasks = new ArrayList<Future<Boolean>>(size);
            int timeout = instance.getTimeout();
            int i = 0;
        
            for (HttpListener listener : enabledListeners) {
                Callable<Boolean> checkListener = new CheckListenerHealth(serv,
                                                                       listener,
                                                                     timeout);
                Future<Boolean> task = exSrv.submit(checkListener);
                checkTasks.add(task);
            }
        
            // wait for status from the checks
            for (Future<Boolean> task : checkTasks) {
                isHealthy = task.get();
                if (isHealthy) {
                    //found atleast one healthy listener on the instance
                    if (instance.getClusterName()!=null) {
                        _logger.log(Level.INFO,"sgmt.instancehang_clserverhealthy",
                                    new Object[]{serv.getName(),instance.getClusterName()});
                        
                    } else {
                        _logger.log(Level.INFO,"sgmt.instancehang_serverhealthy",
                                    serv.getName());
                    }
                    break;
                }  
            }
        
            if(!isHealthy) {
                String cName = instance.getClusterName();
                if (cName!=null) {
                    //clustered server
                    _logger.log(Level.WARNING,"sgmt.instancehang_clserverunhealthy",
                                new Object[]{cName,serv.getName()});
                } else {
                    //standalone server
                    _logger.log(Level.WARNING,"sgmt.instancehang_serverunhealthy",
                                serv.getName());
                }
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            //being optimistic, as check could not be done?
            isHealthy=true;
        } catch (RejectedExecutionException ex) {
            //being optimistic, as check could not be done
            isHealthy=true;
        } catch (ConfigException ex) {
            //nop
        }
        
        // the state is accordingly
        instance.setHealthy(isHealthy);
    }
}
