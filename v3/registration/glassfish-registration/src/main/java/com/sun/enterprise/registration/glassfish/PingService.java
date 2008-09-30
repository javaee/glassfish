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
import org.glassfish.api.Startup;
import org.glassfish.api.Async;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;

import com.sun.enterprise.registration.RegistrationException;

import com.sun.pkg.client.Image;
import com.sun.pkg.client.Fmri;
import com.sun.pkg.client.Manifest;
import com.sun.pkg.client.SystemInfo;
import com.sun.pkg.client.Version;

/* Service to attempt transfer of tags to the central stclient servicetag
 * repository
 * 
 */

@Service(name = "PingService")
@Async
public class PingService implements Startup, PostConstruct {

    @Inject
    Logger logger;
    
    private static final long TIMER_INTERVAL = 
            Long.getLong("com.sun.enterprise.registration.PING_TIMER_INTERVAL", 7* 24 * 60 * 60 * 1000);

    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    public void postConstruct() {
        
         try {
            RegistrationUtil.synchUUID();
         } catch (RegistrationException ex) {
                logger.fine(ex.getMessage());
                //logger.finer(ex);
             return; 
         }

        final Timer pingTimer = new Timer("ping", true); //Mark the timer as daemon so that it does not hold up appserver shutdown

        TimerTask pingTask = new TimerTask() {
            public void run() {
                try {
                    Image img = RegistrationUtil.getUpdateCenterImage();
                    img.refreshCatalogs(); // this gets the information from the server

                    List<Image.FmriState> list = img.getInventory(null, false);  // get the list of packages that are installed

                    ArrayList<String> pkgs = new ArrayList<String>();
                    for (Image.FmriState fs : list) {
                        pkgs.add(fs.fmri.getName());
                    }
                    Image.ImagePlan ip = img.makeInstallPlan(pkgs.toArray(new String[0]));
                    int numUpdates = ip.getProposedFmris().length;
                    logger.info("Number of available updates : " +
                            numUpdates); // FIX i18n
                    if (numUpdates > 0) {
                        logger.info("Available updates : ");                    
                        for (Fmri fmri : ip.getProposedFmris()) {
                            logger.info(fmri.toString());
                        }
                    }
                } catch (Exception e) {
                    // should the timer schedule be changed in case of
                    // exception?
                    logger.info(e.getMessage());
                }
            }
        };
        pingTimer.schedule(pingTask, 0L, TIMER_INTERVAL);
    }
}

