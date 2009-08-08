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

package com.sun.enterprise.admin.cli.commands;

import java.io.*;
import java.util.*;
import java.net.Socket;
import java.util.logging.Level;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.admin.cli.remote.DASUtils;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.ValidOption;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;

/**
 * The stop-domain command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
public class StopDomainCommand extends CLICommand {
    private File domainsDir;
    private File domainRootDir;
    private String domainName;
    private File domainXml;

    private final static long WAIT_FOR_DAS_TIME_MS = 60000;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StopDomainCommand.class);

    /**
     * Constructor.
     */
    public StopDomainCommand(String name, ProgramOptions programOpts,
            Environment env)
            throws CommandException, CommandValidationException {
        super(name, programOpts, env);
    }

    /**
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, "domaindir", '\0', "STRING", false, null);
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "domain_name";
        operandType = "STRING";
        operandMin = 0;
        operandMax = 1;

        processProgramOptions();
    }

    /**
     * The validate method validates that the type and quantity of
     * parameters and operands matches the requirements for this
     * command.  The validate method supplies missing options from
     * the environment.  It also supplies passwords from the password
     * file or prompts for them if interactive.
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException {
        super.validate();

        // get domainName
        if (!operands.isEmpty()) {
            domainName = (String)operands.get(0);
        }

        // get domainsDir
        String domaindir = getOption("domaindir");

        if (ok(domaindir)) {
            domainsDir = new File(domaindir);
            if (!domainsDir.isDirectory()) {
                throw new CommandValidationException(
                        strings.get("StopDomain.badDomainsDir", domainsDir));
            }
        }
    }

    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        getDomainRootDir();
        getDomainXml();
        domainRootDir = SmartFile.sanitize(domainRootDir);
        Integer[] ports = null;

        try {
            MiniXmlParser parser = new MiniXmlParser(domainXml);
            Set<Integer> portsSet = parser.getAdminPorts();
            ports = portsSet.toArray(new Integer[portsSet.size()]);
        } catch (MiniXmlParserException ex) {
            throw new CommandValidationException(
                    strings.get("StopDomain.parserError", ex), ex);
        }

        // TODO -- it would be nice to know if it worked!
        // If so use other port numbers

        int adminPort = ports[0];
        programOpts.setPort(adminPort);

        // Verify that the DAS is running and reachable
        if (!DASUtils.pingDASQuietly(programOpts, env)) {
            // by definition this is not an error
            // https://glassfish.dev.java.net/issues/show_bug.cgi?id=8387
            logger.printWarning(strings.get("StopDomain.dasNotRunning"));
            return 0;
        }

        // run the remote stop-domain command and throw away the output
        RemoteCommand cmd = new RemoteCommand("stop-domain", programOpts, env);
        cmd.executeAndReturnOutput("stop-domain");
        waitForDeath(adminPort);
        return 0;
    }

    private void getDomainRootDir() throws CommandValidationException {
        if (domainsDir == null) {
            domainsDir = new File(getSystemProperty(
                            SystemPropertyConstants.DOMAINS_ROOT_PROPERTY));
        }

        if (!domainsDir.isDirectory()) {
            throw new CommandValidationException(
                    strings.get("StopDomain.badDomainsDir", domainsDir));
        }

        if (domainName != null) {
            domainRootDir = new File(domainsDir, domainName);
        } else {
            domainRootDir = getTheOneAndOnlyDomain(domainsDir);
        }

        if (!domainRootDir.isDirectory()) {
            throw new CommandValidationException(
                    strings.get("StopDomain.badDomainDir", domainRootDir));
        }
    }

    private void getDomainXml() throws CommandValidationException {
        // root-dir/config/domain.xml
        domainXml = new File(domainRootDir, "config/domain.xml");

        if (!domainXml.canRead()) {
            throw new CommandValidationException(
                    strings.get("StopDomain.noDomainXml", domainXml));
        }
    }

    /**
     * It either throws an Exception or returns a valid directory
     * @param parent
     * @return
     * @throws com.sun.enterprise.cli.framework.CommandValidationException
     */
    private File getTheOneAndOnlyDomain(File parent)
            throws CommandValidationException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = parent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        if (files == null || files.length == 0) {
            throw new CommandValidationException(
                    strings.get("noDomainDirs", parent));
        }

        if (files.length > 1) {
            throw new CommandValidationException(
                    strings.get("StopDomain.tooManyDomainDirs", parent));
        }

        return files[0];
    }

    private void waitForDeath(int adminPort) throws CommandException {
        // 1) it's impossible to use the logger to print anything without \n
        // 2) The Logger is set to WARNING right now to kill the version
        //    messages
        // that's why I'm writing to stderr

        long startWait = System.currentTimeMillis();
        System.err.print(strings.get("StopDomain.WaitDASDeath") + " ");
        boolean alive = true;

        while (!timedOut(startWait)) {
            if (!pingPort(adminPort)) {
                alive = false;
                break;
            }
            try {
                Thread.sleep(100);
                System.err.print(".");
            } catch (InterruptedException ex) {
                // don't care
            }
        }

        System.err.println();

        if (alive) {
            throw new CommandException(strings.get("StopDomain.DASNotDead",
                    (WAIT_FOR_DAS_TIME_MS / 1000)));
        }
    }

    /**
     * This is no substitute for DASUtils.pingDAS() -- that command guarantees
     * that DAS is at the other end of the port.  This is a quick check that can
     * be used after verifying DAS is in fact listening on the port.
     * I ran into a problem where stop-domain hangs on pingDAS() after it
     * shuts down, waiting on a network timeout.  This ping is fast!
     *
     * @param port the port to ping
     * @return true if the server socket is reachable
     */
    private boolean pingPort(int port) {
        try {
            String host = null;
            new Socket(host, port);
            return true;
        } catch (Exception ex) {
            logger.printDebugMessage(
                strings.get("pingPort got Exception: " + ex));  // XXX
            return false;
        }
    }

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
}
