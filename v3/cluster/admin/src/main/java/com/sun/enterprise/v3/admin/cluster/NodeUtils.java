/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.v3.admin.cluster;

import org.jvnet.hk2.component.Habitat;
import org.glassfish.internal.api.RelativePathResolver;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.NetUtils;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.CommandValidationException;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;

/**
 * Utility methods for operating on Nodes
 *
 * @author Joe Di Pol
 */
public class NodeUtils {

    static final String NODE_DEFAULT_SSH_PORT = "22";
    static final String NODE_DEFAULT_SSH_USER = "${user.name}";
    static final String NODE_DEFAULT_INSTALLDIR =
                                                "${com.sun.aas.installRoot}";

    // Command line option parameter names
    static final String PARAM_NODEHOST = "nodehost";
    static final String PARAM_INSTALLDIR = "installdir";
    static final String PARAM_NODEDIR = "nodedir";
    static final String PARAM_SSHPORT = "sshport";
    static final String PARAM_SSHUSER = "sshuser";
    static final String PARAM_SSHKEYFILE = "sshkeyfile";
    static final String PARAM_SSHPASSWORD = "sshpassword";
    static final String PARAM_SSHKEYPASSPHRASE = "sshkeypassphrase";
    static final String PARAM_TYPE = "type";

    private static final String NL = System.getProperty("line.separator");

    private TokenResolver resolver = null;
    private Logger logger = null;
    private Habitat habitat = null;

    NodeUtils(Habitat habitat, Logger logger) {
        this.logger = logger;
        this.habitat = habitat;

        // Create a resolver that can replace system properties in strings
        Map<String, String> systemPropsMap =
                new HashMap<String, String>((Map)(System.getProperties()));
        resolver = new TokenResolver(systemPropsMap);
    }

    /**
     * Validate all the parameters used to create an ssh node
     * @param map   Map with all parameters used to create an ssh node.
     *              The map values can contain system property tokens.
     * @param sshL  SSHLauncher to test SSH connection. If null no SSH
     *              connection will be tested.
     * @throws CommandValidationException
     */
    void validate(ParameterMap map, SSHLauncher sshL) throws
            CommandValidationException {

        String sshkeyfile = map.getOne(PARAM_SSHKEYFILE);
        if (StringUtils.ok(sshkeyfile)) {
            // User specified a key file. Make sure we get use it
            File kfile = new File(resolver.resolve(sshkeyfile));
            if (! kfile.isAbsolute()) {
                throw new CommandValidationException(
                        Strings.get("key.path.not.absolute",
                        kfile.getPath()));
            }
            if (! kfile.exists()) {
                throw new CommandValidationException(
                        Strings.get("key.path.not.found",
                        kfile.getPath()));
            }
            if (! kfile.canRead() ) {
                throw new CommandValidationException(
                        Strings.get("key.path.not.readable",
                        kfile.getPath(), System.getProperty("user.name")) );
            }
        }

        validatePassword(map.getOne(PARAM_SSHPASSWORD));
        validatePassword(map.getOne(PARAM_SSHKEYPASSPHRASE));

        if (sshL != null) {
            validateSSHConnection(map, sshL);
        }
    }

    private void validatePassword(String p) throws CommandValidationException {

        String expandedPassword = null;

        // Make sure if a password alias is used we can expand it
        if (StringUtils.ok(p)) {
            try {
                expandedPassword = RelativePathResolver.getRealPasswordFromAlias(p);
            } catch (IllegalArgumentException e) {
                throw new CommandValidationException(
                        Strings.get("no.such.password.alias", p));
            } catch (Exception e) {
                throw new CommandValidationException(
                        Strings.get("no.such.password.alias", p),
                        e);
            }

            if (expandedPassword == null) {
                throw new CommandValidationException(
                        Strings.get("no.such.password.alias", p));
            }
        }
    }

    private void validateSSHConnection(ParameterMap map, SSHLauncher sshL) throws
            CommandValidationException {


        String nodehost = map.getOne(PARAM_NODEHOST);
        String installdir = map.getOne(PARAM_INSTALLDIR);
        String nodedir = map.getOne(PARAM_NODEDIR);
        String sshport = map.getOne(PARAM_SSHPORT);
        String sshuser = map.getOne(PARAM_SSHUSER);
        String sshkeyfile = map.getOne(PARAM_SSHKEYFILE);
        String sshpassword = map.getOne(PARAM_SSHPASSWORD);
        String sshkeypassphrase = map.getOne(PARAM_SSHKEYPASSPHRASE);

        // We use the resolver to expand any system properties
        if (! NetUtils.isPortStringValid(resolver.resolve(sshport))) {
            throw new CommandValidationException(Strings.get(
                    "ssh.invalid.port", sshport));
        }

        int port = Integer.parseInt(resolver.resolve(sshport));

        try {
            // sshpassword and sshkeypassphrase may be password alias.
            // Those aliases are handled by sshLauncher
            sshL.validate(resolver.resolve(nodehost),
                          port,
                          resolver.resolve(sshuser),
                          sshpassword,      
                          resolver.resolve(sshkeyfile),
                          sshkeypassphrase, 
                          resolver.resolve(installdir),
                          logger);
        } catch (IOException e) {
            String m1 = e.getMessage();
            String m2 = "";
            Throwable e2 = e.getCause();
            if (e2 != null) {
                m2 = e2.getMessage();
            }
            if (e instanceof FileNotFoundException) {
                logger.warning(StringUtils.cat(": ", m1, m2, sshL.toString()));
                throw new CommandValidationException(StringUtils.cat(NL,
                                            m1, m2));
            } else {
                String msg = Strings.get("ssh.bad.connect", nodehost);
                logger.warning(StringUtils.cat(": ", msg, m1, m2,
                                            sshL.toString()));
                throw new CommandValidationException(StringUtils.cat(NL,
                                            msg, m1, m2));
            }
        }
    }
}
