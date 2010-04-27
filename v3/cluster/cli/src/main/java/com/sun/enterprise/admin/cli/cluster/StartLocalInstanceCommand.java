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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.universal.xml.MiniXmlParserException;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;

import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.admin.cli.StartServerCommand;
/**
 * Start a local server instance.
 */
@Service(name = "start-local-instance")
@Scoped(PerLookup.class)
public class StartLocalInstanceCommand extends LocalInstanceCommand implements StartServerCommand {
    @Param(optional = true, defaultValue = "false")
    private boolean verbose;

    @Param(optional = true, defaultValue = "false")
    private boolean debug;

    @Param(optional = true, defaultValue = "false")
    private boolean upgrade;


    @Param(name = "instance_name", primary = true, optional = false)
    private String instanceName0;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartLocalInstanceCommand.class);

    @Override
    public GFLauncherFactory.ServerType getType() {
         return GFLauncherFactory.ServerType.instance;
    }

    @Override
    protected void validate()
                        throws CommandException, CommandValidationException {
      
        if(ok(instanceName0))
            instanceName = instanceName0;
        else
            throw new CommandValidationException(strings.get("Instance.badInstanceName"));

        // call this AFTER the above!  validate() calls initInstance() which
        // will use instanceName.

        super.validate(); // sets all the dirs
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        System.out.println("" + this);

        try {
            createLauncher();
            // this can be slow, 500 msec,
            // with --passwordfile option it is ~~ 18 msec
            String mpv = getMasterPassword();
            info.addSecurityToken(MASTER_PASSWORD, mpv);

            // launch returns very quickly if verbose is not set
            // if verbose is set then it returns after the domain dies
            launcher.launch();

            if (verbose || upgrade) { // we can potentially loop forever here...
                while (launcher.getExitValue() == CLIConstants.RESTART_EXIT_VALUE) {
                    logger.printMessage(strings.get("restart"));

                    if (CLIConstants.debugMode)
                        System.setProperty(CLIConstants.WALL_CLOCK_START_PROP,
                                            "" + System.currentTimeMillis());
                    launcher.relaunch();
                }
                return launcher.getExitValue();
            } else {
                //waitForDAS(info.getAdminPorts());
                //report();
                return SUCCESS;
            }
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }    /**
     * Create a launcher for the instance specified by arguments to
     * this command.  The launcher is for a server of the specified type.
     * Sets the launcher and info fields.
     */
    public void createLauncher()
                        throws GFLauncherException, MiniXmlParserException {
            launcher = GFLauncherFactory.getInstance(getType());
            info = launcher.getInfo();
            info.setInstanceName(instanceName);
            info.setInstanceRootDir(instanceDir);
            info.setVerbose(verbose || upgrade);
            info.setDebug(debug);
            info.setUpgrade(upgrade);

            info.setRespawnInfo(programOpts.getClassName(),
                            programOpts.getClassPath(),
                            programOpts.getProgramArguments());

            launcher.setup();
    }

    public String toString() {
        return ObjectAnalyzer.toStringWithSuper(this);
    }
    private static final String MASTER_PASSWORD = "AS_ADMIN_MASTERPASSWORD";
    private GFLauncherInfo info;
    private GFLauncher launcher;
}
