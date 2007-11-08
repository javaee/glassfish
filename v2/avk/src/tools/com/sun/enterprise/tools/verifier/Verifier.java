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
package com.sun.enterprise.tools.verifier;

import java.io.IOException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.List;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.logging.LogDomains;
import com.sun.enterprise.tools.verifier.gui.MainFrame;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * This class is the main class to invoke the verification process. It
 * is directly called by the scripts in AVK and verifier in appserver.
 * The deployment backend invokes verifier in a separate process.
 * Deploytool GUI invokes verifier by calling the verify() and
 * generateReports() APIs.
 */
public class Verifier {

    private static boolean debug = false;
    private static Logger logger = LogDomains.getLogger(
            LogDomains.AVK_VERIFIER_LOGGER);
    /**
     * contains arguments data. It is used throughout the verification framework
     */
    private FrameworkContext frameworkContext = null;


    /**
     * Constructor that does the initialization. It parses and validates
     * the arguments and creates the frameworkContext that is used
     * throughout the verification framework.
     *
     * @param args
     */
    public Verifier(String[] args) {
        StringManagerHelper.setLocalStringsManager(this.getClass());
        frameworkContext = new Initializer(args).getFrameworkContext();
    }

    /**
     * This constructor is called by the deployment backend. The invocation
     * of this method is in the server's process.
     */
    public Verifier() {
        StringManagerHelper.setLocalStringsManager(this.getClass());
        frameworkContext = new FrameworkContext();
        frameworkContext.setUseTimeStamp(true);
        frameworkContext.setOutputDirName(System.getProperty("com.sun.aas.instanceRoot") + // NOI18N
                                                            File.separator +
                                                            "logs" + // NOI18N
                                                            File.separator + 
                                                            "verifier-results"); // NOI18N
    }

    /**
     * Main verifier method
     *
     * @param args Arguments to pass to verifier
     *             returns 0 if successfully verified with ZERO failures & ZERO errors.
     *             returns failure_count+error_count otherwise.
     */
    public static void main(String[] args) throws IOException {
        Verifier verifier = new Verifier(args);
        if (verifier.frameworkContext.isUsingGui()) {
            MainFrame mf = new MainFrame(
                    verifier.frameworkContext.getJarFileName(), true, verifier);
            mf.setSize(800, 600);
            mf.setVisible(true);
        } else {
            LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
            try {
                verifier.verify();
            } catch (Exception e) {
                LogRecord logRecord = new LogRecord(Level.SEVERE,
                        smh.getLocalString(
                                verifier.getClass().getName() +
                                ".verifyFailed", // NOI18N
                                "Could not verify successfully.")); // NOI18N
                logRecord.setThrown(e);
                verifier.frameworkContext.getResultManager().log(logRecord);
            }
            verifier.generateReports();
            int failedCount = verifier.frameworkContext.getResultManager()
                    .getFailedCount() +
                    verifier.frameworkContext.getResultManager().getErrorCount();
            if (failedCount != 0)
                System.exit(failedCount);
        }
    }

    /**
     * This method does the verification by running all the verifier tests
     *
     * @return ResultManager that contains all the test results.
     * @throws IOException
     */
    private ResultManager verify() throws IOException {
        VerificationHandler verificationHandler = 
                        new VerificationHandler(frameworkContext);
        ResultManager resultManager;
        try {
            resultManager = verificationHandler.verifyArchive();
        } finally {
            verificationHandler.cleanup();
        }
        return resultManager;
    }

    /**
     * @param jarFile This method is called from gui MainPanel to run verifier
     *                on selected archive
     * @return ResultManager Object containing all test results
     * @throws IOException
     */
    public ResultManager verify(String jarFile) throws IOException {
        frameworkContext.setJarFileName(jarFile);
        return verify();
    }

    /**
     * Call from deployment backend. This call is in the appserver process.
     * Verifier will run in appserver mode for this invocation.
     * If parameter application is null then this api is equivalent to 
     * invoking a standalone verifier. 
     * Parameter abstractArchive must not be null.
     * 
     * @return status of the invocation. A non zero value will denote a failure.
     * @throws IOException
     */ 
    public int verify(Application application,
                      AbstractArchive abstractArchive,
                      List<String> classPath,
                      File jspOutDir)
            throws IOException {
        boolean originalBoundsChecking = Descriptor.isBoundsChecking();
        Descriptor.setBoundsChecking(false);
        ResultManager rmanager=null;
        frameworkContext.setJspOutDir(jspOutDir);
        frameworkContext.setIsBackend(true);
        VerificationHandler verificationHandler = null;
        try {
            if(application == null) { //can be a standalone connector deployment
                frameworkContext.setJarFileName(abstractArchive.getArchiveUri());
                verificationHandler = new VerificationHandler(frameworkContext); 
            } else
                verificationHandler = new VerificationHandler(frameworkContext,
                                                              application,
                                                              abstractArchive,
                                                              classPath);
            rmanager = verificationHandler.verifyArchive();
        } catch(Exception e) {
            LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
            LogRecord logRecord = 
                    new LogRecord(Level.SEVERE,
                                 smh.getLocalString(getClass().getName() +
                                                   ".verifyFailed", // NOI18N
                                                   "Could not verify successfully.")); // NOI18N
            logRecord.setThrown(e);
            frameworkContext.getResultManager().log(logRecord);
        } finally { // restore the original values
            Descriptor.setBoundsChecking(originalBoundsChecking);
            if(verificationHandler!=null)
                verificationHandler.cleanup();
        }
        generateReports();
        return rmanager.getErrorCount() + rmanager.getFailedCount();
    }

    /**
     * It generates the reports using the ResultManager
     *
     * @throws IOException
     */
    public void generateReports() throws IOException {
        new ReportHandler(frameworkContext).generateAllReports();
    }

    /**
     * checks if verifier is running in debug mode
     * @return debug status
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * debug messages are logged here.
     * @param t
     */
    public static void debug(Throwable t) {
        logger.log(Level.FINEST, "Exception occurred", t);
    }
}
