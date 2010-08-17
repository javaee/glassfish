/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.installer.conf;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.installer.util.GlassFishUtils;
import org.openinstaller.util.ClassUtils;
import org.openinstaller.util.ExecuteCommand;

/** Manages glassfish domain related operations.
 * Operations such as start/stop/delete domain are not exposed
 * through installer hence are not implemented in this class yet.
 * This utility class is invoked when the user chooses to create a
 * domain.
 * Holds reference to Product object to get product wide information.
 *
 * @author sathyan
 */
public class DomainManager {

    /* Reference to Product to obtain installation directory
     * and path to administration script.
     */
    private final Product productRef;
    /* Holds asadmin command output including the command line
     * of the recent runs. Gets overwritten on repeated calls to
     * asadmin commands. This text content will be used to construct
     * summary panel that displays the status/results of user configuration
     * actions.
     */
    private String outputFromRecentRun;
    /* Holds status of the configuration. Currently not used as createDomain
     * returns a valid Domain object when the configuation is successful.
     * Can be used to double check in the calling code to make sure that
     * configuration was indeed successful/failure.
     */
    private boolean domainConfigSuccessful;

    /* Logger related. */
    private static final Logger LOGGER;

    static {
        LOGGER = Logger.getLogger(ClassUtils.getClassName());
    }

    /* @return true/false, the value of the overall configuration status flag. */
    public boolean isDomainConfigSuccessful() {
        return domainConfigSuccessful;
    }

    public DomainManager(Product productRef) {
        this.productRef = productRef;
        outputFromRecentRun = null;
    }

    /* Creates the domain by invoking asadmin's create-domain command.
     * @param domainName, name of the domain to create.
     * @param domainRoot, installation location of the domain.
     * @param instancePort, HTTP port.
     * @param adminPort, Admin Port to get into console.
     * @param saveLogin, default to false.
     * @param checkPorts, default to true.
     * @param adminUser, username for administering the server.
     * @param adminPassword, password to be used for administrator logins.
     * @param runningMode, "DRYRUN"/"REAL" "DRYRUN" mode would just return the
     * commandline and not execute it.
     * @return Domain, the newly created Domain Object, null if the creation fails.
     */
    public Domain createDomain(String domainName, String domainRoot,
            String instancePort, String adminPort,
            boolean saveLogin, boolean checkPorts,
            String adminUser, String adminPassword,
            String runningMode) {


        domainConfigSuccessful = true;

        /* Setup domain attributes. */
        Domain glassfishDomain = new Domain(domainName, domainRoot, instancePort,
                adminPort, saveLogin, checkPorts, adminUser, adminPassword, "changeit");

        /* Setup value for --domainproperties of create-domain command. */
        glassfishDomain.setDomainProperties(GlassFishUtils.getDomainProperties(adminPort, instancePort, glassfishDomain.getGlassfishPortBases()));

        /* Create password file. */
        PasswordFile pFile = new PasswordFile(glassfishDomain.getAdminPassword(),
                glassfishDomain.getMasterPassword(), "asadminTmp");

        /* Update the password file with actual passwords. */
        pFile.setupPasswordFile();

        /* Assemble the create-domain command line. */
        ExecuteCommand asadminExecuteCommand =
                GlassFishUtils.assembleCreateDomainCommand(productRef, glassfishDomain, pFile);

        outputFromRecentRun = "";
        if (asadminExecuteCommand != null) {
            LOGGER.log(Level.INFO, "Creating GlassFish domain");
            LOGGER.log(Level.INFO, "with the following command line");
            outputFromRecentRun += asadminExecuteCommand.expandCommand(asadminExecuteCommand.getCommand()) + "\n";
            LOGGER.log(Level.INFO, outputFromRecentRun);

            if (runningMode.contains("DRYRUN")) {
                /*
                Do not execute the command, this is useful when the clients just
                wanted the actual commandline and not execute the command.*/
                return glassfishDomain;
            }

            try {
                asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
                asadminExecuteCommand.setCollectOutput(true);
                asadminExecuteCommand.execute();
                outputFromRecentRun += asadminExecuteCommand.getAllOutput();
                LOGGER.log(Level.INFO, "Asadmin output: " + outputFromRecentRun);
                // Look for the string failed till asadmin bugs related to stderr are resolved.
                // Ugly/Buggy, but works for now.
                if (outputFromRecentRun.indexOf("failed") != -1) {
                    domainConfigSuccessful = false;
                    glassfishDomain = null;
                }

            } catch (Exception e) {
                LOGGER.log(Level.INFO, "In exception, asadmin output: " + outputFromRecentRun);
                LOGGER.log(Level.INFO, "Exception while creating GlassFish domain: " + e.getMessage());
                glassfishDomain = null;
                domainConfigSuccessful = false;
            }
        } else {
            outputFromRecentRun = "Command Line formation failed.";
            domainConfigSuccessful = false;
            glassfishDomain = null;
        }
        /* Delete the password, it is set to be deleted upon JVM exit. */
        pFile.getPasswordFile().delete();
        return glassfishDomain;
    }

    public boolean deleteDomain(String domainName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stopDomain(String domainName) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    public void startDomain(String domainName,String runningMode) {
         /* Assemble the create-domain command line. */
        ExecuteCommand asadminExecuteCommand =
                GlassFishUtils.assembleStartDomainCommand(productRef, domainName);

        outputFromRecentRun = "";
        if (asadminExecuteCommand != null) {
            LOGGER.log(Level.INFO, "Starting GlassFish domain");
            LOGGER.log(Level.INFO, "with the following command line");
            outputFromRecentRun += asadminExecuteCommand.expandCommand(asadminExecuteCommand.getCommand()) + "\n";
            LOGGER.log(Level.INFO, outputFromRecentRun);

            if (runningMode.contains("DRYRUN")) {
                /*
                Do not execute the command, this is useful when the clients just
                wanted the actual commandline and not execute the command.*/
                return;
            }

            try {
                asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
                asadminExecuteCommand.setCollectOutput(true);
                asadminExecuteCommand.execute();
                outputFromRecentRun += asadminExecuteCommand.getAllOutput();
                LOGGER.log(Level.INFO, "Asadmin output: " + outputFromRecentRun);
                // Look for the string failed till asadmin bugs related to stderr are resolved.
                // Ugly/Buggy, but works for now.
                if (outputFromRecentRun.indexOf("failed") != -1) {
                    domainConfigSuccessful = false;
                }

            } catch (Exception e) {
                LOGGER.log(Level.INFO, "In exception, asadmin output: " + outputFromRecentRun);
                LOGGER.log(Level.INFO, "Exception while starting GlassFish domain: " + e.getMessage());
                domainConfigSuccessful = false;
            }
        } else {
            outputFromRecentRun = "Command Line formation failed.";
            domainConfigSuccessful = false;
        }
    }

    /* Checks to make sure that the domain is running, currently used during
     * cluster creation as create-cluster requires the domain to be up.
     * @param domainName, name of the domain to check.
     * @return true if the domain is running, false if the domain is not running
     * or there are other errors running asadmin list-domains command.
     */
    public boolean isDomainRunning(String domainName) {

        boolean retStatus = false;

        /* Assemble asadmin list-domains command. */
        ExecuteCommand asadminExecuteCommand =
                GlassFishUtils.assembleListDomainsCommand(productRef);

        if (asadminExecuteCommand != null) {
            LOGGER.log(Level.INFO, "Validating presence of GlassFish domain");
            LOGGER.log(Level.INFO, "with the following command line");
            LOGGER.log(Level.INFO,
                    ExecuteCommand.expandCommand(asadminExecuteCommand.getCommand()));
            outputFromRecentRun = asadminExecuteCommand.expandCommand(asadminExecuteCommand.getCommand());


            try {
                asadminExecuteCommand.setOutputType(ExecuteCommand.ERRORS | ExecuteCommand.NORMAL);
                asadminExecuteCommand.setCollectOutput(true);
                asadminExecuteCommand.execute();
                outputFromRecentRun += asadminExecuteCommand.getAllOutput();
                LOGGER.log(Level.INFO, "Asadmin output: " + outputFromRecentRun);
                /* Look for the string "Name:<domainname> Status: Running" in the output. */
                if (outputFromRecentRun.indexOf("Name: " + domainName + " Status: Running") != -1) {
                    retStatus = true;
                }

            } catch (Exception e) {
                LOGGER.log(Level.INFO, "In exception, asadmin output: " + outputFromRecentRun);
                LOGGER.log(Level.INFO, "Exception while creating GlassFish domain: " + e.getMessage());
            }
        } else {
            outputFromRecentRun = "Command Line formation failed.";
            retStatus = false;
        }

        return retStatus;
    }

    /* Caller can get the output of recent asadmin command run. This has to be used
     * along with configSuccessful flag to find out the overall status of configuration.
     * @return String the whole of asadmin recent run's output including the command line.
     */
    public String getOutputFromRecentRun() {
        return this.outputFromRecentRun;
    }

    public boolean isConfigSuccessful() {
        return this.domainConfigSuccessful;
    }
}
