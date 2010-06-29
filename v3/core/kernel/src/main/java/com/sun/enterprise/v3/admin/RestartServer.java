/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.JavaClassRunner;
import org.glassfish.api.admin.AdminCommandContext;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * For non-verbose mode:
 * Stop this server, spawn a new JVM that will wait for this JVM to die.  The new JVM then starts the server again.
 *
 * For verbose mode:
 * We want the asadmin console itself to do the respawning -- so just return a 10 from
 * System.exit().  This tells asadmin to restart.
 *
 * @author Byron Nevins
 */
public class RestartServer {

    protected final void setRegistry(final ModulesRegistry registryIn) {
        registry = registryIn;
    }

    /**
     * Restart of the application server :
     *
     * All running services are stopped.
     * LookupManager is flushed.
     *
     * Client code that started us should notice the return value of 10 and restart us.
     */
    protected final void doExecute(AdminCommandContext context) {
        try {
            // unfortunately we can't rely on constructors with HK2...
            if(registry == null)
                throw new NullPointerException(new LocalStringsImpl(getClass())
                        .get("restart.server.internalError", "registry was not set"));

            init(context);

            if (!verbose) {
                // do it now while we still have the Logging service running...
                reincarnate();
            }
            // else we just return 10 from System.exit()

            Collection<Module> modules = registry.getModules(
                    "com.sun.enterprise.osgi-adapter");
            if (modules.size() == 1) {
                final Module mgmtAgentModule = modules.iterator().next();
                mgmtAgentModule.stop();
            }
            else
                context.getLogger().warning(strings.get("restart.server.badNumModules", modules.size()));

        }
        catch (Exception e) {
            context.getLogger().severe(strings.get("restart.server.failure", e));
        }

        System.exit(10);
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /////////               ALL PRIVATE BELOW               ////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    private void init(AdminCommandContext context) throws IOException {
        logger = context.getLogger();
        String s = System.getProperty(com.sun.enterprise.glassfish.bootstrap.Constants.ARGS_PROP);
        Reader reader = new StringReader(s);
        props = new Properties();
        props.load(reader);
        verbose = Boolean.parseBoolean(props.getProperty("-verbose", "false"));
        logger.info(strings.get("restart.server.init"));
    }

    private void reincarnate() {
        try {
            if (setupReincarnationWithAsadmin() || setupReincarnationWithOther())
                doReincarnation();
            else
                logger.severe(strings.get("restart.server.noStartupInfo",
                        strings.get("restart.server.asadminError"),
                        strings.get("restart.server.nonAsadminError")));
        }
        catch (RDCException rdce) {
            // already logged...
        }
        catch (Exception e) {
            logger.severe(strings.get("restart.server.internalError", e));
        }

    }

    private void doReincarnation() throws RDCException {
        try {
            // TODO JavaClassRunner is very simple and primitive.
            // Feel free to beef it up...

            String[] props = normalProps;

            if (Boolean.parseBoolean(System.getenv("AS_SUPER_DEBUG")))
                props = debuggerProps;  // very very difficult to debug this stuff otherwise!

            new JavaClassRunner(classpath, props, classname, args);
        }
        catch (Exception e) {
            logger.severe(strings.get("restart.server.jvmError", e));
            throw new RDCException();
        }
    }

    private boolean setupReincarnationWithAsadmin() throws RDCException {
        classpath = props.getProperty("-asadmin-classpath");
        classname = props.getProperty("-asadmin-classname");
        argsString = props.getProperty("-asadmin-args");

        return verify("restart.server.asadminError");
    }

    private boolean setupReincarnationWithOther() throws RDCException {

        classpath = props.getProperty("-startup-classpath");
        classname = props.getProperty("-startup-classname");
        argsString = props.getProperty("-startup-args");

        return verify("restart.server.nonAsadminError");
    }

    private boolean verify(String errorStringKey) throws RDCException {
        // Either asadmin or non-asadmin startup params have been set -- check them!
        // THREE possible returns:
        // 1) true
        // 2) false
        // 3) RDCException
        if (classpath == null && classname == null && argsString == null) {
            return false;
        }

        // now that at least one is set -- demand that ALL OF THEM be set...
        if (!ok(classpath) || !ok(classname) || argsString == null) {
            logger.severe(strings.get(errorStringKey));
            throw new RDCException();
        }

        args = argsString.split(",,,");

        return true;
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    // We use this simply to tell the difference between fatal errors and other
    // non-fatal conditions.
    private static class RDCException extends Exception {
    }
    ModulesRegistry registry;
    private Properties props;
    private Logger logger;
    private boolean verbose;
    private String classpath;
    private String classname;
    private String argsString;
    private String[] args;
    private static final LocalStringsImpl strings = new LocalStringsImpl(RestartServer.class);
    /////////////             static variables               ///////////////////
    private static final String magicProperty = "-DAS_RESTART=true";
    private static final String[] normalProps = {magicProperty};
    private static final String[] debuggerProps = {
        magicProperty,
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1323"};
}
