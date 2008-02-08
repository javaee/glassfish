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
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import org.glassfish.deployment.autodeploy.AutoDeployer.AutodeploymentStatus;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Manages retrying of autodeployed files in case a file is copied slowly.
 * <p>
 * If a file is copied into the autodeploy directory slowly, it can appear there
 * before the copy has finished, causing the attempt to autodeploy it to fail.
 * This class encapsulates logic to retry such files on successive loops through
 * the autodeployer thread, reporting failure only if the candidate file remains
 * stable in size and cannot be opened after a period of time which defaults below
 * and is configurable using the config property name defined below.
 * <p>
 * The main public entry point is the testFileAsArchive method, which accepts a file and
 * if it is not a directory tries to open it as an archive (a zip file).  When AutoDeployer tries
 * and fails to open such a file as an archive, it records
 * that fact internally.  The manager adds to or updates
 * a map that describes all such failed files.  If AutoDeployer previously
 * noted failures to open the file and the file's size has been stable
 * for a configurable period of time, then the manager concludes that the
 * file is not simply a slow-copying file but is truly invalid.  In that case
 * it throws an exception.  When a file fails to open as a zip file the first time,
 * or if it has failed to open before but its size has changed within the configurable
 * time, then the manager returns a null to indicate that we need to wait before trying
 * to process the file.  Once a file opens successfully, any record of that file is removed
 * from the map.
 *
 * @author tjquinn
 */
@Service
public class AutodeployRetryManager implements PostConstruct {
        
    /**
     *Specifies the default value for the retry limit.
     */
    private final int RETRY_LIMIT_DEFAULT = 30; // 30 seconds

    /** Maps an invalid File to its corresponding Info object. */
    private HashMap<File,Info> invalidFiles = new HashMap<File,Info>();

    private static final Logger sLogger=LogDomains.getLogger(LogDomains.DPL_LOGGER);
    private static final LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(AutodeployRetryManager.class);

    @Inject
    private DasConfig activeDasConfig;
    
    private int timeout = RETRY_LIMIT_DEFAULT;

    public void postConstruct() {
        setTimeout();
    }
    
    /**
     *Tests to see if the file can be opened as an archive.
     *<p>
     *This method will not be needed if the main autodeployer uses
     *the deployment facility.  This is because the deployment facility
     *method invocations use an archive, so the openFileAsArchive method
     *will be used instead of this one.
     *@param file spec to be tested
     *@return whether the file was opened as an archive (DEPLOY_SUCCESS), 
     *   was not but should be tried later (DEPLOY_PENDING), or was
     *   not and should not be tried later (DEPLOY_FAILURE).
     *@throws AutoDeploymentException if an error occurred closing the archive
     */
    AutodeploymentStatus testFileAsArchive(String file) throws AutoDeploymentException {
        AutodeploymentStatus result;
        try {
            File inFile = new File(file);
            if (!inFile.isDirectory()) {
                // either a j2ee jar file or a directory or a .class file
                if (file.endsWith(".class")) {
                    return AutodeploymentStatus.SUCCESS;
                }
                
                /*
                 * The file is not a directory and not a .class file.  It should
                 * be a file archive.  An inexpensive way to see if it might be
                 * valid is to try to open it as a zip file.  
                 */
                ZipFile zipFile = openFileAsZipFile(inFile);
                if (zipFile != null) {
                    result = AutodeploymentStatus.SUCCESS;
                    zipFile.close();
                } else {
                    result = AutodeploymentStatus.PENDING;
                }
            } else {
                /*
                 * The file is a directory.  Try deploying it because there is 
                 * no way other than that of finding out if the directory is
                 * complete.  And even then it might not be if files in 
                 * subdirectories are added later.
                 */
                
                // XXX Need to allow failed deployments (as for directories) to trigger monitoring and retry of directories

                result = AutodeploymentStatus.SUCCESS;
            }
        } catch (AutoDeploymentException ade) {
            result = AutodeploymentStatus.FAILURE;
        } catch (IOException ioe) {
            String msg = localStrings.getLocalString(
                    "enterprise.deployment.autodeploy.error_closing_archive", 
                    "error closing file {0} after testing it as an archive");
            throw new AutoDeploymentException(msg, ioe);
        }
        return result;
    }

    /**
     *Opens the specified file as an archive and tracks files that have
     *failed to open as archives.
     *@param f the file to try opening as an archive
     *@return the AbstractArchive for the file (null if it shoudl be tried later)
     *@throws AutoDeploymenException if the manager has been unable to open
     *the file as an archive past the configured expiration time
     */
    private ZipFile openFileAsZipFile(File f) throws AutoDeploymentException {
        ZipFile zipFile = null;
        try {
            /*
             Try to open the file as an archive and then remove the file, if
             *it is present, from the map tracking invalid files.
             */
            zipFile = new ZipFile(f);

            recordSuccessfulOpen(f);

        } catch (IOException ioe) {
            String errorMsg = null;
            /*
             *If the archive variable was not assigned a non-null, then the file
             *is not a valid archive.
             */
            if (zipFile == null) {
                boolean failedPreviously = recordFailedOpen(f);
                if ( ! failedPreviously) {
                    Info info = get(f);
                    errorMsg = localStrings.getLocalString(
                            "enterprise.deployment.autodeploy.error_opening_start_retry", 
                            "error opening {0} as JAR; may be slow copy; starting retry until {1}",
                            f, 
                            new Date(info.retryExpiration).toString());
                    sLogger.log(Level.INFO, errorMsg);
                }
            }
        }
        return zipFile;
    }


    /**
     *Retrieves the Info object describing the specified file.
     *@param File for which the Info object is requested
     *@return Info object for the specified file
     *null if the file is not recorded as invalid
     */
    Info get(File file) {
        Info info = (Info) invalidFiles.get(file);
        return info;
    }

    /**
     *Indicates whether the AutoDeployer should try opening the specified
     *file as an archive.
     *<p>
     *The file should be opened if this retry manager has no information
     *about the file or if information is present and the file size is
     *unchanged from the previous failure to open.
     *@return if the file should be opened as an archive
     */
    boolean shouldOpen(File file) {
        boolean result = true; // default is true in case the file is not being monitored
        String msg = null;
        boolean loggable = sLogger.isLoggable(Level.FINE);
        Info info = (Info) invalidFiles.get(file);
        if (info != null) {
            result = info.shouldOpen();
            if (loggable) {
                if (result) {
                    msg = localStrings.getLocalString(
                            "enterprise.deployment.autodeploy.try_stable_length", 
                            "file {0} has stable length so it should open as a JAR",
                            file.getAbsolutePath());
                } else {
                    msg = localStrings.getLocalString(
                            "enterprise.deployment.autodeploy.no_try_unstable_length", 
                            "file {0} has an unstable length of {1}; do not retry yet",
                            file.getAbsolutePath(), String.valueOf(file.length()));
                }
            }
            info.update();
        } else {
            if (loggable) {
                msg = localStrings.getLocalString(
                        "enterprise.deployment.autodeploy.try_not_monitored", 
                        "file {0} should be opened as an archive because it is not being monitored as a slowly-growing file",
                        file.getAbsolutePath());
            }
        }
        if (loggable) {
            sLogger.log(Level.FINE, msg);
        }
        return result;
    }

    boolean recordFailedDeployment(File file) throws AutoDeploymentException {
        return recordFailedOpen(file);
    }
    
    boolean recordSuccessfulDeployment(File file) {
        return recordSuccessfulOpen(file);
    }
    
    boolean recordSuccessfulUndeployment(File file) {
        return endMonitoring(file);
    }
    
    boolean recordFailedUndeployment(File file) {
        return endMonitoring(file);
    }
    
    boolean endMonitoring(File file) {
        return (invalidFiles.remove(file) != null);
    }
    
    /**
     *Records the fact that the autodeployer tried but failed to open this file
     *as an archive.
     *@param File the file that could not be interpreted as a legal archive
     *@return true if the file was previously recognized as an invalid one
     *@throws AutoDeploymentException if the file should no longer be retried
     */
    private boolean recordFailedOpen(File file) throws AutoDeploymentException {
        boolean fileAlreadyPresent;
        /*
         *Try to map the file to an existing Info object for it.
         */
        Info info = get(file);
        if ( ! (fileAlreadyPresent = (info != null))) {
            /*
             *This file was not previously noted as invalid.  Create a new
             *entry and add it to the map.
             */
            info = createInfo(file);
            invalidFiles.put(file, info);
            if (sLogger.isLoggable(Level.FINE)) {
                String msg = localStrings.getLocalString(
                        "enterprise.deployment.autodeploy.begin_monitoring", 
                        "will monitor {0} waiting for its size to be stable size until {1}",
                        file.getAbsolutePath(), new Date(info.retryExpiration).toString());
                sLogger.log(Level.FINE, msg);
            }
        } else {
            /*
             *The file has previously been recorded as invalid.  Update
             *the recorded info.
             */
            info.update();

            /*
             *If the file is still eligible for later retries, just return.
             *If the file size has been stable too long, then conclude that
             *the file is just an invalid archive and throw an exception
             *to indicate that.
             */
            boolean loggable = sLogger.isLoggable(Level.FINE);
            if ( ! info.hasRetryPeriodExpired()) {
                /*
                 *Just log that the file is still being monitored.
                 */
                if (loggable) {
                    String msg = localStrings.getLocalString(
                            "enterprise.deployment.autodeploy.continue_monitoring", 
                            "file {0} remains eligible for monitoring until {1}",
                            file.getAbsolutePath(), new Date(info.retryExpiration).toString());
                    sLogger.log(Level.FINE, msg);
                }
            } else {
                /*
                 *Log that monitoring of this file will end, remove the file from
                 *the map, and throw an exception
                 *with the same message.
                 */
                String msg = localStrings.getLocalString(
                        "enterprise.deployment.autodeploy.abort_monitoring",
                        "File {0} is no longer eligible for retry; its size has been stable for {1} second{1,choice,0#s|1#|1<s} but it is still unrecognized as an archive",
                        file.getAbsolutePath(), 
                        timeout);
                if (loggable) {
                    sLogger.log(Level.FINE, msg);
                }
                invalidFiles.remove(file);
                throw new AutoDeploymentException(msg);
            }
        }
        return fileAlreadyPresent;
    }

    /**
     *Marks a file as successfully opened and no longer subject to retry.
     *@param File that is no longer invalid
     *@return true if the file had been previously recorded as invalid
     */
    private boolean recordSuccessfulOpen(File file) {
        if (sLogger.isLoggable(Level.FINE)) {
            String msg = localStrings.getLocalString(
                    "enterprise.deployment.autodeploy.end_monitoring", 
                    "File {0} opened successfully; no need to monitor it further",
                    file.getAbsolutePath());
            sLogger.log(Level.FINE, msg);
        }
        return (invalidFiles.remove(file)) != null;
    }
    
    private void setTimeout() {
        int newTimeout = timeout;
        String timeoutText = activeDasConfig.getAutodeployRetryTimeout();
        if (timeoutText == null || timeoutText.equals("")) {
            timeout = RETRY_LIMIT_DEFAULT;
            return;
        }
        try {
            int configuredTimeout = Integer.parseInt(timeoutText);
            if (configuredTimeout > 1000) {
                /*
                 * User probably thought the configured value was in milliseconds
                 * instead of seconds.  
                 */
                sLogger.warning(localStrings.getLocalString(
                        "enterprise.deployment.autodeploy.configured_timeout_large",
                        "Configured timeout value of {0} second{0,choice,0#s|1#|1<s} will be used but seems very large",
                        configuredTimeout));
                newTimeout = configuredTimeout;
            } else if (configuredTimeout <= 0) {
                sLogger.warning(localStrings.getLocalString(
                        "enterprise.deployment.autodeploy.configured_timeout_small",
                        "Configured timeout value of {0} second{0,choice,0#s|1#|1<s} is too small; using previous value of {1} second{1,choice,0#s|1#|1<s}",
                        configuredTimeout,
                        timeout));
            } else {
                newTimeout = configuredTimeout;
            }
        } catch (NumberFormatException ex) {
            sLogger.warning(localStrings.getLocalString(
                    "enterprise.deployment.autodeploy.configured_timeout_invalid",
                    "Could not convert configured timeout value of \"{0}\" to a number; using previous value of {1} second{1,choice,0#s|1#|1<s}",
                    timeoutText,
                    timeout));
        }
        timeout = newTimeout;
    }
    
    /**
     * Factory method that creates a new Info object for a given file.
     * @param f the file of interest
     * @return the new Info object
     */
    private Info createInfo(File f) {
        if (f.isDirectory()) {
            return new DirectoryInfo(f);
        } else {
            return new JarInfo(f);
        }
    }
        
    /**
     *Records pertinent information about a file judged to be invalid - that is,
     *unrecognizable as a legal archive:
     *<ul>
     *<li>the file object,
     *<li>the length of the file at the most recent check of the file,
     *<li>the time after which no more retries should be attempted.
     *</ul>
     */
    private abstract class Info {
        
        /** File recorded in this Info instance */
        protected File file = null;

        /** Timestamp after which all retries on this file should stop. */
        protected long retryExpiration = 0;

        /**
         *Creates a new instance of the Info object for the specified file.
         *@param File to be recorded
         */
        public Info(File file) {
            this.file = file;
            update();
        }

        /**
         * Reports whether the file represented by this Info object should be
         * opened now, even though past opens have failed.
         * @return
         */
        protected abstract boolean shouldOpen();

        /**
         * Updates whatever data is used to monitor changes to the file and
         * returns whether or not changes have been detected.
         * @return whether changes have been detected
         */
        protected abstract boolean update();
        
        /**
         *Reports whether the retry period for this file has expired.
         *<p>
         *@return if the file should remain as a candidate for retry
         */
        private boolean hasRetryPeriodExpired() {
            return (System.currentTimeMillis() > retryExpiration);
        }
        
        /**
         * Delays the time when retries for this file will expire.
         */
        protected void postponeRetryExpiration() {
            retryExpiration = System.currentTimeMillis() + timeout * 1000;
        }

    }
    
    private class JarInfo extends Info {

        /** File length the previous time this file was reported as
         * invalid. */
        private long recordedLength = 0;

        public JarInfo(File f) {
            super(f);
        }

        @Override
        protected boolean shouldOpen() {
            return (file.length() == recordedLength);            
        }

        /**
         *Updates the Info object with the file's length and recomputes (if
         *appropriate) the retry due date and the expiration.
         */
        @Override
        protected boolean update() {
            boolean hasChanged;
            long currentLength = file.length();
            if (hasChanged = (recordedLength != currentLength)) {
                /*
                 *The file's size has changed.  Reset the time for this
                 *file's expiration.
                 */
                postponeRetryExpiration();
            }
            /*
             *In all cases, update the recorded length with the file's
             *actual current length.
             */
            recordedLength = currentLength;
            return hasChanged;
        }

    }
    
    private class DirectoryInfo extends Info {
        
        private long whenScanned = 0;
        
        public DirectoryInfo(File f) {
            super(f);
        }

        @Override
        protected boolean shouldOpen() {
            /*
             * For directories we have no way of knowing - without trying to
             * deploy it - whether it represents a valid archive.  So as far
             * as we know from here, directories should always be opened.
             */
            return true;
        }

        @Override
        protected boolean update() {
            /*
             * For a directory, scan all its files for one that's newer than 
             * the last time we checked.
             */
            
            /*
             * Record the start time of the scan rather than the finish time
             * so we don't inadvertently miss files that were changed 
             * during the scan.
             */
            long newWhenScanned = System.currentTimeMillis();
            boolean hasChanged = isNewerFile(file, whenScanned);
            if (hasChanged) {
                postponeRetryExpiration();
            }
            whenScanned = newWhenScanned;
            return hasChanged;
        }
        
        /**
         * Reports whether the specified file is newer (or contains a newer
         * file) than the timestamp.
         * @param f the file to check
         * @param timestamp moment to compare to
         * @return true if the file is newer or contains a newer file than timestamp
         */
        private boolean isNewerFile(File f, long timestamp) {
            boolean aFileIsNewer = (f.lastModified() > timestamp);
            if ( ! aFileIsNewer) {
                if (f.isDirectory()) {
                    for (File containedFile : f.listFiles()) {
                        if (aFileIsNewer = isNewerFile(containedFile, timestamp)) {
                            break;
                        }
                    }
                }
            }
            return aFileIsNewer;
        }
    }
}