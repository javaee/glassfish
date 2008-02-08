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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.deployment.autodeploy;

import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.logging.LogDomains;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.Startup;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;

/**
 * A service wrapper around the autodeployer.
 * <p>
 * The module system will start this service during GlassFish start-up.  In turn
 * it will start the actual autodeployer to run periodically.
 * <p>
 * Note that some values used by the service are known when the class is first
 * started.  Others can be configured dynamically.  The first type are initialized
 * during postConstruct.  The others will trigger the delivery of config change
 * events to which we respond and, as needed, stop or reschedule the timer task.
 * 
 * @author tjquinn
 */
@Service
@Scoped(Singleton.class)
public class AutoDeployService implements Startup, PostConstruct, PreDestroy {

    @Inject
    DasConfig activeDasConfig;

    @Inject
    Habitat habitat;
    
    private AutoDeployer autoDeployer;
    
    private Timer autoDeployerTimer;
    
    private TimerTask autoDeployerTimerTask;
    
    private String target;
    
    private static final String DAS_TARGET = "server";
    
    private static final Logger logger = LogDomains.getLogger(LogDomains.DPL_LOGGER);
    
    private static final String DEFAULT_POLLING_INTERVAL_IN_SECONDS = "2";
    private static final String DEFAULT_AUTO_DEPLOY_ENABLED = "true";

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    public void postConstruct() {
        /*
         * Create the auto-deployer right away, even if its use is disabled 
         * currently.  This way any initialization errors will appear early 
         * in the log rather than later if and when the auto-deployer is 
         * enabled.
         */
        target = getTarget();
        try {
            autoDeployer = new AutoDeployer(
                    target,
                    activeDasConfig.getAutodeployDir(),
                    getDefaultVirtualServer(),
                    Boolean.parseBoolean(activeDasConfig.getAutodeployJspPrecompilationEnabled()),
                    Boolean.parseBoolean(activeDasConfig.getAutodeployVerifierEnabled()),
                    true /* renameOnSuccess */,
                    true /* force deployment */,
                    true /* enabled when autodeployed */,
                    habitat
                    );
             /*
             * Also create the timer and the timer task, reusing them as needed as
             * we need to stop and restart the task.
             */
            autoDeployerTimer = new Timer("AutoDeployer");

            autoDeployerTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            autoDeployer.run();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };

            if (isAutoDeployEnabled()) {
                startAutoDeployer();
            }
                logger.fine("[AutoDeployer] Service start-up complete");
        } catch (AutoDeploymentException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e); 
        }

    }

    public void preDestroy() {
        stopAutoDeployer();
    }

    static String getValue(String value, String defaultValue) {
        return (value == null || value.equals("")) ? defaultValue : value;
    }
    

    private String getTarget() {
        // XXX should this also be configurable ?
        return DAS_TARGET;
    }
    
    private String getDefaultVirtualServer() {
        // XXX write this? Or should DeployCommand take care of it on behalf of all code that uses DeployCommand?
        return null;
    }
    
    private boolean isAutoDeployEnabled() {
        return Boolean.parseBoolean(
                getValue(activeDasConfig.getAutodeployEnabled(),
                DEFAULT_AUTO_DEPLOY_ENABLED));
    }
    
    private long getPollingIntervalInSeconds() {
        return Integer.parseInt(
                getValue(activeDasConfig.getAutodeployPollingIntervalInSeconds(), 
                DEFAULT_POLLING_INTERVAL_IN_SECONDS));
    }
    
    private void startAutoDeployer() {
        long pollingInterval = getPollingIntervalInSeconds() * 1000;
        autoDeployerTimer.schedule(
                autoDeployerTimerTask, 
                pollingInterval, 
                pollingInterval);
        logger.fine("[AutoDeployer] Started, monitoring " + 
                    activeDasConfig.getAutodeployDir() + " every " +
                    getPollingIntervalInSeconds()
                    );
    }

    private void stopAutoDeployer() {
        /*
         * Tell the running autodeployer to stop, then cancel the timer task 
         * and the timer.
         */
        logger.fine("[AutoDeployer] Stopping");
        autoDeployer.cancel(true);
        autoDeployerTimerTask.cancel();
        autoDeployerTimer.cancel();
    }
    
    /**
     * Reschedules the autodeployer because a configuration change has altered
     * the frequency.
     */
    private void rescheduleAutoDeployer() {
        logger.fine("[AutoDeployer] Restarting...");
        stopAutoDeployer();
        try {
            autoDeployer.waitUntilIdle();
        } catch (InterruptedException e) {
            // XXX OK to glide through here?
        }
        startAutoDeployer();
    }
}
