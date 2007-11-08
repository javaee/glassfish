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

import java.util.logging.Logger;
import java.util.logging.Level;


import com.sun.ejb.base.distributed.EJBTimerBaseAction;
import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.PlannedShutdownAction;
import com.sun.enterprise.ee.cms.core.PlannedShutdownSignal;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.ee.cms.core.SignalAcquireException;
import com.sun.enterprise.ee.cms.core.SignalReleaseException;

import com.sun.logging.LogDomains;

public class EJBTimerPlannedShutdownActionImpl
    extends EJBTimerBaseAction
    implements PlannedShutdownAction
{
    protected static final Logger _logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);
 
    public void consumeSignal(Signal signal) {
        try {
            //Acquire the signal before processing it. This would ensure that
            //the failed server would not be added back to the cluster before the
            //notification action is completed
	        signal.acquire();
                _logger.log(Level.INFO, "Got Planned Shutdown Notification[1]. "
                        + "shutdownNodeID: " + signal.getMemberToken());
            PlannedShutdownSignal psSignal = (PlannedShutdownSignal) signal;
            GMSConstants.shutdownType shutdownType = psSignal.getEventSubType();
            if (shutdownType == GMSConstants.shutdownType.INSTANCE_SHUTDOWN) {
                _logger.log(Level.INFO, "Got Planned Shutdown Notification. "
                        + "shutdownNodeID: " + signal.getMemberToken());
                //Get the member id which has failed
                super.migrateTimers(signal.getMemberToken());
            } else {
                _logger.log(Level.INFO, "Ignoring Group Shutdown Notification");
            }
        } catch (SignalAcquireException e) {
            _logger.log(Level.WARNING,
                    "EJBTimerPlannedShutdownActionImpl: Couldn't acquire signal", e);
        } catch (Exception ex) {
            _logger.log(Level.WARNING,
                    "EJBTimerPlannedShutdownActionImpl: Exception during migrateTimers", ex);
        } finally {
            try {
                signal.release();
            } catch (Exception ex) {
                _logger.log(Level.WARNING,
                        "EJBTimerPlannedShutdownActionImpl: Couldn't release signal", ex);
            }
        }
    }
}
