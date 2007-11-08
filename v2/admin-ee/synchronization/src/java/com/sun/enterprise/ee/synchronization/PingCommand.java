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
package com.sun.enterprise.ee.synchronization;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.UndeclaredThrowableException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.proxy.MBeanServerProxy;
import com.sun.enterprise.ee.admin.clientreg.MBeanServerConnectionInfo;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.config.ConfigException;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;
/**
 * Represents a concrete implementation of a command that pings 
 * central repository admin. 
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class PingCommand implements Ping {

    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
                                                                                                                                                             
    private static final StringManagerBase _strMgr =
        StringManagerBase.getStringManager(_logger.getResourceBundleName());
    
    /**
     * Initializes the arguments.
     *
     * @param   dpr   obj that provides access to DAS info
     */
    PingCommand(DASPropertyReader dpr) {
        _dasInfo = dpr;
    }

    /**
     * Pings central repository admin.
     */
    public void execute() {

        try {
            MBeanServerConnectionInfo connInfo = 
                new MBeanServerConnectionInfo(_dasInfo);

            int count = 0;

            // (re) attempts to contact DAS 
            while (count < MAX_RETRY) {
                try {
                    SynchronizationMBean proxy = 
                     (SynchronizationMBean)MBeanServerProxy.getMBeanServerProxy(
                       SynchronizationMBean.class, new ObjectName(MBEAN_NAME), 
                       connInfo);
                    _response = proxy.ping(SERVER_NAME);                           
                    // made contact with DAS
                    break;

                } catch (Exception e) {
                   // JMX exception or exception thrown when wrong DAS contacted
                    _response = getReal(e);
                    Throwable t = ((Exception)_response).getCause();
                    if (t instanceof ConfigException) {
                        // could contact the DAS but wrong DAS contacted
                        String agentId =  System.getProperty(
                            SystemPropertyConstants.SERVER_NAME);
                        String msg = _strMgr.getString(
                            SYNC_NO_MATCHING_AGENT, new Object[] {agentId, 
                            _dasInfo.getHost(),_dasInfo.getPort()});
                        _logger.log(Level.SEVERE, msg);
                        throw new NonMatchingDASContactedException(msg);
                    } else {
                        _logger.log(Level.FINE,
                        _strMgr.getString(SYNC_NO_CONNECTION,
                             _dasInfo.getJMXURL()));
                        Thread.currentThread().sleep(DEFAULT_RETRY_INTV);
                    }
                }
                // failed to contact DAS.
                // Increment the retry count and try again
                ++count;
            }
        } catch (Exception e) {
	    _response = e;
            _logger.log(Level.FINE, SYNC_NO_CONNECTION, _dasInfo.getJMXURL());
        }
    }

    /**
     * Returns the name of this command.
     *
     * @return  the name of this command
     */
    public String getName() {
        return NAME;
    }

    /** 
     * Returns true if central repository admin is alive.
     * 
     * @return  true if central repository admin is alive
     */
    public boolean isAlive() {
        return (_response instanceof Exception) ? false : true;
    }

    /**
     * No-op.
     */
    public Object getResult() {
        return _response;
    }
     private Throwable getReal(final Exception e) {
         Throwable real = null;
         if (exceptionHierarchyIsExpected(e)) {
             real = e.getCause().getCause();
         } else {
             real = e;
         }
         return ( real );
     }

     private boolean exceptionHierarchyIsExpected(final Throwable t) {
         boolean expected = false;
         if (t instanceof UndeclaredThrowableException) {
             Throwable at = t.getCause();
             if (at instanceof AgentException) {
                 at = at.getCause();
                 if (at != null)
                     expected = true;
             }
         }
         return ( expected );
     }

    // ---- VARIABLE(S) - PRIVATE -------------------------------
    private Object _response            = null;
    private DASPropertyReader _dasInfo  = null;
    private static final int MAX_RETRY  = 3;
    private static final String NAME    = "Synchronization-Ping-Command";    
    private static final String MBEAN_NAME        = 
            "com.sun.appserv:type=synchronization,category=config";
    private static final long DEFAULT_RETRY_INTV  = 3000;
    private static final String SERVER_NAME       = 
            System.getProperty(SystemPropertyConstants.SERVER_NAME);
    private static final String SYNC_NO_CONNECTION = 
            "synchronization.no_connection";
    private static final String SYNC_NO_MATCHING_AGENT =
            "synchronization.no_matching_agent";
}
