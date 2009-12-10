/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.tools.upgrade.common;

import com.sun.enterprise.tools.upgrade.logging.LogService;
import com.sun.enterprise.tools.upgrade.UpgradeToolMain;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * The Commands class is used statically (should probably be
 * a singleton instead) and is not thread safe. It is expected
 * that startDomain will not be called by the GUI while an
 * upgrade is already in progress. In the command line case,
 * this isn't an issue.
 */
public class Commands {

    private static final Logger logger = LogService.getLogger();
    private static final StringManager stringManager =
        StringManager.getManager(Commands.class);
    
    private static boolean errorFound = false;

    public static int startDomain(String domainName, CommonInfoModel cInfo) {
        errorFound = false; // in case this is being rerun
        Credentials c = cInfo.getSource().getDomainCredentials();

        String installRoot = System.getProperty(UpgradeToolMain.AS_DOMAIN_ROOT);
        File installRootF = new File(installRoot);
        File asadminF = new File(installRootF.getParentFile(), "bin/asadmin");
        String asadminScript = asadminF.getAbsolutePath();
        try {
            // this is just for readability in output
            asadminScript = asadminF.getCanonicalPath();
        } catch (IOException e) {
            //- no action needed use absolutePath
        }
        String ext = "";
        String osName = System.getProperty("os.name");
        CommandBuilder cb = new CommandBuilder();
        if (osName.indexOf("Windows") != -1) {
            asadminScript = "cmd /c " + asadminScript;
            ext = ".bat";
        }
        cb.add(asadminScript + ext);
        cb.add("start-domain");

        String masterPassword = c.getMasterPassword();
        if (masterPassword != null && masterPassword.length() > 0) {
            cb.add("--passwordfile ");
            cb.add(c.getPasswordFile());
        }

        cb.add("--upgrade");
        cb.add("--domaindir");
        cb.add(cInfo.getTarget().getInstallDir());

        cb.add(domainName);
        return executeCommand(cb.getCommand());
    }
   
    private static int executeCommand(String commandString) {

        // how long we want to wait for output after process dies
        final long JOIN_TIMEOUT = 4000;

        int exitValue = 0;
        logger.info(stringManager.getString("commands.executingCommandMsg") +
            commandString);

        try {
            // start process and threads to watch output
            Process proc = Runtime.getRuntime().exec(commandString);
            StreamWatcher errWatcher =
                new StreamWatcher(proc.getErrorStream(), "ERR");
            StreamWatcher outWatcher =
                new StreamWatcher(proc.getInputStream(), "OUT");
            errWatcher.start();
            outWatcher.start();

            // wait for everything to finish (it should)
            exitValue = proc.waitFor();
            logger.fine("Return value from process: " + exitValue);
            errWatcher.join(JOIN_TIMEOUT);
            outWatcher.join(JOIN_TIMEOUT);
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                stringManager.getString("upgrade.common.general_exception"), e);

            // if the process didn't return a non-zero yet, lets do it now
            if (exitValue == 0) {
                exitValue = 1;
            }
        }
        return exitValue;
    }

    // can come from more than one stream, only want to output once
    private static synchronized void foundError() {
        if (errorFound) {
            return;
        }
        logger.warning(stringManager.getString("commands.problemFound"));
        errorFound = true;
    }

    /*
     * TODO:
     * This method is not connected to anything yet, but am including
     * it so I don't forget. Proably needs it's own bug filed.
     *
     * This could be connected to the Cancel button in the GUI
     * for stopping the upgrade. Will need a class-level field
     * for the process. Watcher threads should terminate on their
     * own.
     */
    public static void killAsadminProcess() {
//        if (asadminProcess != null) {
//            asadminProcess.destroy();
//        }
    }

    /*
     * Class is used to build up a command line. It is functionally
     * a StringBuilder that adds a space between each String that
     * is appended. Leading and trailing spaces are trimmed when
     * getCommand() is called.
     */
    private static class CommandBuilder {

        private static final String SPACE = " ";
        private final StringBuilder sb = new StringBuilder();

        void add(String s) {
            sb.append(s);
            sb.append(SPACE);
        }

        String getCommand() {
            return sb.toString().trim();
        }
    }

    /*
     * Class used to read process output stream and
     * send to logger. It's up to the tool to log
     * any "starting/stopping" messages around the
     * called process. In the GUI case, logged messages
     * above a certain level also are shown in the
     * progress panel.
     */
    private static class StreamWatcher extends Thread {

        /*
         * This is the pattern to match lines that start with
         * SEVERE:
         */
        private static final Pattern pattern;
        static {
            StringBuilder sb = new StringBuilder();
            sb.append(Level.SEVERE.getLocalizedName());
            sb.append(":.*");
            pattern = Pattern.compile(sb.toString());
        }

        private final BufferedReader reader;

        // don't send the message more than once (or keep parsing the lines)
        private boolean sentError = false;

        public StreamWatcher(InputStream stream, String name) {
            super(name);
            reader = new BufferedReader(new InputStreamReader(stream));
        }

        @Override
        public void run() {
            Matcher matcher;
            try {
                String line = reader.readLine();
                if (line != null) {
                    matcher = pattern.matcher(line);
                    while (line != null) {
                        logger.finer(getName() + ": " + line);
                        if (!sentError) {
                            matcher.reset(line);
                            if (matcher.matches()) {
                                Commands.foundError();
                                sentError = true;
                            }
                        }
                        line = reader.readLine();
                        Thread.sleep(2);
                    }
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE,
                    stringManager.getString("commands.exceptionReadingStream"),
                    t);
            } finally {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    // seriously?
                    logger.log(Level.FINE,
                        "Exception closing reader in StreamWatcher",
                        ioe);
                }
            }
        }

    }

}
