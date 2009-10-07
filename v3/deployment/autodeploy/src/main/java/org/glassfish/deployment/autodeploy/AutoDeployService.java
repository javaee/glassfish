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
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.Startup;
import org.glassfish.api.Async;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.api.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

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
public class AutoDeployService implements PostStartup, PostConstruct, PreDestroy, ConfigListener {

    @Inject
    DasConfig activeDasConfig;

    @Inject
    Habitat habitat;
    
    private AutoDeployer autoDeployer;
    
    private Timer autoDeployerTimer;
    
    private TimerTask autoDeployerTimerTask;
    
    private String target;
    
    private static final String DAS_TARGET = "server";

    private static final List<String> configPropertyNames = Arrays.asList(
            "autodeploy-enabled", "autodeploy-polling-interval-in-seconds", 
            "autodeploy-verifier-enabled", "autodeploy-jsp-precompilation-enabled"
            );
        
 
    /** Cannot find the resource bundle unless we want until postConstruct to create the logger. */
    private Logger logger;
    
    private static final String DEFAULT_POLLING_INTERVAL_IN_SECONDS = "2";
    private static final String DEFAULT_AUTO_DEPLOY_ENABLED = "true";

    public void postConstruct() {
        logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);
        
        /* Create the auto-deployer right away, even if its use is disabled 
         * currently.  This way any initialization errors will appear early 
         * in the log rather than later if and when the auto-deployer is 
         * enabled.
         */
        String directory = activeDasConfig.getAutodeployDir();
        target = getTarget();
        try {
            autoDeployer = new AutoDeployer(
                    target,
                    directory,
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
            autoDeployerTimer = new Timer("AutoDeployer", true);

            boolean isEnabled = isAutoDeployEnabled();
            int pollingIntervalInSeconds = Integer.valueOf(DEFAULT_POLLING_INTERVAL_IN_SECONDS);
            try {
                pollingIntervalInSeconds = getPollingIntervalInSeconds();
            } catch (NumberFormatException ex) {
                logger.log(
                        Level.WARNING, 
                        "enterprise.deployment.autodeploy.error_parsing_polling_interval", 
                        new Object[] {
                            activeDasConfig.getAutodeployPollingIntervalInSeconds(),
                            ex.getClass().getName(),
                            ex.getLocalizedMessage()
                            }
                    );
            }
            if (isEnabled) {
                startAutoDeployer(pollingIntervalInSeconds);
            }
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
    
    private void logConfig(String title, 
            boolean isEnabled,
            int pollingIntervalInSeconds,
            String directory) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("[AutoDeploy] " + title + ", enabled=" + isEnabled +
                    ", polling interval(seconds)=" + pollingIntervalInSeconds +
                    ", directory=" + directory);
        }
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
    
    private int getPollingIntervalInSeconds() throws NumberFormatException {
        return Integer.parseInt(
                getValue(activeDasConfig.getAutodeployPollingIntervalInSeconds(), 
                DEFAULT_POLLING_INTERVAL_IN_SECONDS));
    }
    
    private void startAutoDeployer(int pollingIntervalInSeconds) {
        long pollingInterval = pollingIntervalInSeconds * 1000L;
        autoDeployerTimer.schedule(
                autoDeployerTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            autoDeployer.run();
                        } catch (Exception ex) {
                            // shoule have been already logged
                            AutoDeployer.sLogger.log(Level.FINE, ex.getMessage(), ex);
                        }
                    }
                }, 
                pollingInterval, 
                pollingInterval);
        logConfig(
                "Started", 
                isAutoDeployEnabled(), 
                pollingIntervalInSeconds, 
                activeDasConfig.getAutodeployDir());
    }

    private void stopAutoDeployer() {
        /*
         * Tell the running autodeployer to stop, then cancel the timer task 
         * and the timer.
         */
        logger.fine("[AutoDeploy] Stopping");
        autoDeployer.cancel(true);
        autoDeployerTimerTask.cancel();
    }
    
    /**
     * Reschedules the autodeployer because a configuration change has altered
     * the frequency.
     */
    private void rescheduleAutoDeployer(int pollingIntervalInSeconds) {
        logger.fine("[AutoDeploy] Restarting...");
        stopAutoDeployer();
        try {
            autoDeployer.waitUntilIdle();
        } catch (InterruptedException e) {
            // XXX OK to glide through here?
        }
        startAutoDeployer(pollingIntervalInSeconds);
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        /*
         * Deal with any changes to the DasConfig that might affect whether
         * the autodeployer should be stopped or started or rescheduled with a
         * different frequency.  Those change are handled here, by this
         * class.
         */
        
       /* Record any events we tried to process but could not. */
        List<UnprocessedChangeEvent> unprocessedEvents = new ArrayList<UnprocessedChangeEvent>();
        
        Boolean newEnabled = null;
        Integer newPollingIntervalInSeconds = null;
        
        for (PropertyChangeEvent event : events) {
            if (event.getSource() instanceof DasConfig) {
                String propName = event.getPropertyName();
                if (configPropertyNames.contains(propName) && event.getOldValue().equals(event.getNewValue())) {
                    logger.fine("[AutoDeploy] Ignoring reconfig of " + propName + 
                            " from " + event.getOldValue() + " to " + event.getNewValue());
                    continue;
                }
                if (propName.equals("autodeploy-enabled")) {
                    /*
                     * Either start the currently stopped autodeployer or stop the
                     * currently running one.
                     */
                    newEnabled = Boolean.valueOf((String) event.getNewValue());
                    logger.fine("[AutoDeploy] Reconfig - enabled changed to " + newEnabled);
                } else if (propName.equals("autodeploy-polling-interval-in-seconds")) {
                    try {
                        newPollingIntervalInSeconds = new Integer((String) event.getNewValue());
                        logger.fine("[AutoDeploy] Reconfig - polling interval (seconds) changed from " 
                                + ((String) event.getOldValue()) + " to " + 
                                newPollingIntervalInSeconds);
                    } catch (NumberFormatException ex) {
                        logger.log(Level.WARNING, "enterprise.deployment.autodeploy.error_processing_config_change", 
                                new Object[] {
                                    propName, 
                                    event.getOldValue(), 
                                    event.getNewValue(), 
                                    ex.getClass().getName(), 
                                    ex.getLocalizedMessage()} );
                    }
                } else if (propName.equals("autodeploy-dir")) {
                    String newDir = (String) event.getNewValue();
                    try {
                        autoDeployer.setDirectory(newDir);
                        logger.fine("[AutoDeploy] Reconfig - directory changed from " + 
                                ((String) event.getOldValue()) + " to " +
                                newDir);
                    } catch (AutoDeploymentException ex) {
                        logger.log(Level.WARNING, "enterprise.deployment.autodeploy.error_processing_config_change",
                                new Object[] {
                                    propName, 
                                    event.getOldValue(), 
                                    event.getNewValue(), 
                                    ex.getClass().getName(), 
                                    ex.getCause().getLocalizedMessage()});
                    }
                } else if (propName.equals("autodeploy-verifier-enabled")) {
                    boolean newVerifierEnabled = Boolean.parseBoolean((String) event.getOldValue());
                    autoDeployer.setVerifierEnabled(newVerifierEnabled);
                    logger.fine("[AutoDeploy] Reconfig - verifierEnabled changed from " + 
                            Boolean.parseBoolean((String) event.getOldValue()) +
                            " to " + newVerifierEnabled);
                } else if (propName.equals("autodeploy-jsp-precompilation-enabled")) {
                    boolean newJspPrecompiled = Boolean.parseBoolean((String) event.getNewValue());
                    autoDeployer.setJspPrecompilationEnabled(newJspPrecompiled);
                    logger.fine("[AutoDeploy] Reconfig - jspPrecompilationEnabled changed from " + 
                            Boolean.parseBoolean((String) event.getOldValue()) +
                            " to " + newJspPrecompiled);
                }
            }
        }
        if (newEnabled != null) {
            if (newEnabled) {
                startAutoDeployer(newPollingIntervalInSeconds == null ? 
                    getPollingIntervalInSeconds() : newPollingIntervalInSeconds);
            } else {
                stopAutoDeployer();
            }
        } else {
            if ((newPollingIntervalInSeconds != null) && isAutoDeployEnabled()) {
                /*
                 * There is no change in whether the autodeployer should be running, only
                 * in how often it should run.  If it is not running now don't
                 * start it.  If it is running now, restart it to use the new
                 * polling interval.
                 */
                rescheduleAutoDeployer(newPollingIntervalInSeconds.intValue());
            }
        }
        return (unprocessedEvents.size() > 0) ? new UnprocessedChangeEvents(unprocessedEvents) : null;
    }
}
