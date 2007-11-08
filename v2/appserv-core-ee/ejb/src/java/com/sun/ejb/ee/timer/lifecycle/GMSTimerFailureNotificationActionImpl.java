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

package com.sun.ejb.ee.timer.lifecycle;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.ee.cms.core.FailureNotificationAction;

import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.ee.cms.core.SignalAcquireException;
import com.sun.enterprise.ee.cms.core.SignalReleaseException;

import com.sun.ejb.spi.distributed.DistributedEJBService;
import com.sun.ejb.spi.distributed.DistributedEJBServiceFactory;
import com.sun.ejb.base.distributed.EJBTimerBaseAction;

import com.sun.logging.LogDomains;

/**
 * Implementation of EJB Timer FailureNotificationAction interface
 * add @author: 
 * add @Date:
 * add @version: 
 */
public class GMSTimerFailureNotificationActionImpl 
    extends EJBTimerBaseAction
    implements FailureNotificationAction
{
    protected static final Logger _logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);
 
    public GMSTimerFailureNotificationActionImpl() {
        super();
    }
 
    /**
     * When a server fails the timers that are owned by the failing server should
     * be migrated over to a new server. We will rely on the GMS module to transmit
     * a FailureNotificationSignal to the other working servers. The other servers
     * would try to migrate timers for the failed servers but only one would succeed. 
     * We would rely on the database to provide the locking mechanism 
     * @param signal
     */
    public void consumeSignal(Signal signal) {
        try {
            //Acquire the signal before processing it. This would ensure that
            //the failed server would not be added back to the cluster before the
            //notification action is completed
            signal.acquire();
            _logger.log(Level.INFO, "Got FailureNotification. failedNodeID: "
                    + signal.getMemberToken());
            //Get the member id which has failed
            migrateTimers(signal.getMemberToken());
            //Always Release after completing the processing. This will ensure that
            //it is now safe for the failed server to rejoin the cluster

        } catch (SignalAcquireException e) {
            _logger.log(Level.WARNING,
                    "GMSTimerFailureNotificationActionImpl: Couldn't acquire signal", e);
        } catch (Exception ex) {
            _logger.log(Level.WARNING,
                    "GMSTimerFailureNotificationActionImpl: Exception during migrateTimers", ex);
        } finally {
            try {
                signal.release();
            } catch (Exception ex) {
                _logger.log(Level.WARNING,
                        "GMSTimerFailureNotificationActionImpl: Couldn't release signal", ex);
            }
        }
    } //consumeSignal()

} //GMSTimerFailureNotificationActionImpl{}
