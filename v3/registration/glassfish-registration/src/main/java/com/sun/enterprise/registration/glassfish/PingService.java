/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.registration.glassfish;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import com.sun.enterprise.registration.RegistrationException;
import java.util.Date;
import java.lang.StringBuffer;

import com.sun.pkg.client.Image;
import com.sun.pkg.client.Fmri;
import com.sun.pkg.client.SystemInfo;
import org.glassfish.internal.api.PostStartup;


@Service(name = "PingService")
public class PingService implements PostStartup, PostConstruct {

    @Inject
    Logger logger;
    
    private static final long ONE_DAY =  24 * 60 * 60 * 1000;
    private static final String JVM_OPTION = 
            "com.sun.enterprise.registration.PING_TIMER_INTERVAL";
    private static final long TIMER_INTERVAL = 
            Long.getLong(JVM_OPTION, 7 * 24 * 60) * 60 * 1000; 
    private static final String UC_PING_TIME_STAMP_FILE = ".ping";


    public void postConstruct() {
        if (TIMER_INTERVAL <=0) {
            logger.finer("Ping disabled : " + JVM_OPTION + " <= 0");
            return;                             
        }

        SystemInfo.UpdateCheckFrequency frequency =
                SystemInfo.getUpdateCheckFrequency();
        
        if (frequency.equals(SystemInfo.UpdateCheckFrequency.NEVER)) {
            logger.finer("Ping disabled by Update Center option");
            return;                             
        }
    

        try {
            RegistrationUtil.synchUUID();
         } catch (RegistrationException ex) {
            logger.fine("Ping disabled");
            logger.fine(ex.getMessage());
            return; 
         }
        
        final Timer pingTimer = new Timer("ping", true); //Mark the timer as daemon so that it does not hold up appserver shutdown

        TimerTask pingTask = new TimerTask() {
            public void run() {
                try {
                    Image img = RegistrationUtil.getUpdateCenterImage();
                    img.refreshCatalogs(); // this gets the information from the server

                    List<Image.FmriState> list = img.getInventory(null, false);  // get the list of packages that are installed

                    ArrayList<String> pkgs = 
                            new ArrayList<String>();
                    for (Image.FmriState fs : list) {
                        pkgs.add(fs.fmri.getName());
                    }
                    Image.ImagePlan ip = img.makeInstallPlan(pkgs.toArray(new String[0]));
                    int numUpdates = ip.getProposedFmris().length;
                    int numNewUpdates = 0;
                    long lastPing = getTimeStamp();
                    Date lastPingDate = new Date(getTimeStamp());
                    logger.info("Total number of available updates : " +
                            numUpdates); // FIX i18n
                    if (numUpdates > 0) {
                        StringBuffer sb = new StringBuffer();
                        for (Fmri fmri : ip.getProposedFmris()) {
                            Date pkgDate = fmri.getVersion().getPublishDate();
                            if (pkgDate.after(lastPingDate)) {
                                sb.append(System.getProperty("line.separator"));
                                sb.append(fmri.getName() + " " + fmri.getVersion() +
                                        " " + fmri.getVersion().getPublishDate());
                                numNewUpdates++;
                            }
                        }
                        if (lastPing > 0) {
                            logger.info("Number of available updates since " + 
                                lastPingDate + " : "  + numNewUpdates);
                        }
                        if (numNewUpdates > 0) {
                            if (lastPing > 0) {
                                sb.insert(0, "Available updates since " + lastPingDate + " : " +
                                        System.getProperty("line.separator"));
                            }
                            else {
                                sb.insert(0, "Available updates : " + 
                                        System.getProperty("line.separator"));
                            }
                            logger.info(sb.toString());
                        }
                    }
                } catch (Exception e) {
                    // should the timer schedule be changed in case of
                    // exception?
                    logger.fine(e.getMessage());
                }
                // set the time stamp even in case of failure to ping, 
                // so that next attempt to ping remains startup agnostic.                
                finally { 
                    try {
                        setTimeStamp();
                    } catch (Exception ex) {
                        logger.fine(ex.getMessage());
                    }
                }
            }
        };

        // nextPing is the time after which an initial ping would
        // be attempted during the current server run. A value of 0 means 
        // no delay - i.e. attempt ping immediately.
        long nextPing = 0L;
        try {
            long current = System.currentTimeMillis();
            long lastPing = getTimeStamp();
            //This is to ensure that we do only one ping within a 24 hour 
            // period, regardless of server restarts.
            if (current - lastPing <= ONE_DAY) 
                nextPing = lastPing - current + ONE_DAY;
            if (nextPing < 0)
                nextPing = 0L;
        } catch(Exception ex) {
            logger.fine(ex.getMessage());
            nextPing = 0L;
        }
        

        logger.finer("next ping after : " + nextPing/(60 * 1000) + " minutes");
        // ping after nextPing milliseconds and subsequenlty after TIMER_INTERVAL intervals
        pingTimer.schedule(pingTask, nextPing, TIMER_INTERVAL);
    }

    private void setTimeStamp() throws Exception {
        File f = new File(RegistrationUtil.getRegistrationHome(), 
                UC_PING_TIME_STAMP_FILE);
        if (!f.createNewFile())
            f.setLastModified(System.currentTimeMillis());
    }

    private long getTimeStamp() throws Exception {
        File f = new File(RegistrationUtil.getRegistrationHome(), 
                UC_PING_TIME_STAMP_FILE);
        if (!f.exists())
            return 0L;
        return f.lastModified();        
    }
}

