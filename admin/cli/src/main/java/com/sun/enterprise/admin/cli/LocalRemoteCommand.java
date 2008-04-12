/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * A local StopDomain command
 * @author bnevins
 */
abstract class LocalRemoteCommand extends S1ASCommand {
    public Map<String,Map<String,String>> runRemoteCommand(String... args) throws CommandException {
        try {
            if(!validated)
                throw new CommandException(strings.get("internalError", 
                        strings.get("noValidateOptions")));
            
            String[] newArgs = new String[args.length + 2];
            CLILogger.getInstance().pushAndLockLevel(Level.WARNING);
            return new RemoteCommand(args).getServerResponse();
        }
        finally {
            CLILogger.getInstance().popAndUnlockLevel();
        }
    }

    @Override
    public boolean validateOptions() throws CommandValidationException {
        super.validateOptions();
        // Currently all derived classes have the 
        // domain name as the optional operand
        if (!operands.isEmpty()) {
            domainName = (String) operands.firstElement();
        }

        // get domainsDir
        String domaindir = getOption("domaindir");

        if (ok(domaindir)) {
            domainsDir = new File(domaindir);
            if (!domainsDir.isDirectory()) {
                throw new CommandValidationException(
                        strings.get("badDomainsDir", domainsDir));
            }
        }
        getDomainRootDir();
        domainXml = RemoteUtils.getDomainXml(domainRootDir);
        adminPort = RemoteUtils.getAdminPort(domainXml);
        validated = true;
        return true;
    }

    private void getDomainRootDir() throws CommandValidationException {
        if (domainsDir == null) {
            domainsDir = new File(getSystemProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY));
        }

        if (!domainsDir.isDirectory()) {
            throw new CommandValidationException(
                    strings.get("StopDomain.badDomainsDir", domainsDir));
        }

        if (domainName != null) {
            domainRootDir = new File(domainsDir, domainName);
        }
        else {
            domainRootDir = RemoteUtils.getTheOneAndOnlyDomain(domainsDir);
        }

        if (!domainRootDir.isDirectory()) {
            throw new CommandValidationException(
                    strings.get("StopDomain.badDomainDir", domainRootDir));
        }
        domainRootDir = SmartFile.sanitize(domainRootDir);
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
    private File domainsDir;
    private File domainRootDir;
    private String domainName;
    private File domainXml;
    private int adminPort;
    private boolean validated = false;
    private final static LocalStringsImpl strings = new LocalStringsImpl(LocalRemoteCommand.class);
}
