/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.registration.glassfish;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.internal.api.PostStartup;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;
import com.sun.enterprise.registration.RegistrationException;
import com.sun.enterprise.registration.impl.SysnetRegistrationService;



/* Service to attempt transfer of tags to the central stclient servicetag
 * repository
 * 
 */
@Service(name = "SysnetTransferService")
public class TransferService implements PostStartup, PostConstruct {

    @Inject
    Logger logger;

    private static final long TIMER_INTERVAL = 
            Long.getLong("com.sun.enterprise.registration.TRANSFER_TIMER_INTERVAL", 7 * 24  * 60) * 60 * 1000;

    public int priority() {
        return Thread.MIN_PRIORITY;
    }

    public void postConstruct() {
        if (TIMER_INTERVAL < 0)
            return;
        final SysnetRegistrationService srs =
                new SysnetRegistrationService(
                RegistrationUtil.getServiceTagRegistry());
        if (srs.isRegistrationEnabled()) {
            final Timer registrationTimer = new Timer("registration", true); //Mark the timer as daemon so that it does not hold up appserver shutdown

            TimerTask registrationTask = new TimerTask() {

                public void run() {
                    try {
                        srs.transferEligibleServiceTagsToSysNet();
                        // Transfer was succseeful cancel the timer thread
                        registrationTimer.cancel();
                    } catch (RegistrationException e) {
                        //Log exception.  
                        logger.info(e.getMessage());
                    }
                }
            };
            registrationTimer.schedule(registrationTask, 0L, TIMER_INTERVAL);
        }
    }
}
