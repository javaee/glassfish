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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.admin;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.JavaClassRunner;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

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

@Service(name="restart-domain")
@Async
@I18n("restart.domain.command")

public class RestartDomainCommand implements AdminCommand {
    @Inject
    ModulesRegistry registry;

    /** version which will use injection */
    public RestartDomainCommand()
    {
    }
    
    /** version which will not use injection */
    public RestartDomainCommand( final ModulesRegistry registryIn )
    {
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
    public void execute(AdminCommandContext context) {
        try {
            init(context);

            if(!verbose) {
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
                context.getLogger().warning(modules.size() + " no of primordial modules found");
            
        }
        catch(Exception e) {
            context.getLogger().severe("Got an exception trying to restart: " + e);
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
        String s = System.getProperty("hk2.startup.context.args");
        Reader reader = new StringReader(s);
        props = new Properties();
        props.load(reader);
        verbose = Boolean.parseBoolean(props.getProperty("-verbose", "false"));
        logger.info(strings.get("restart.domain.init"));
    }

    private void reincarnate() {
        try {
            if(setupReincarnationWithAsadmin() || setupReincarnationWithOther())
                doReincarnation();
            else
                logger.severe(strings.get("restart.domain.noStartupInfo", 
                        strings.get("restart.domain.asadminError"),
                        strings.get("restart.domain.nonAsadminError") ));
        }
        catch(RDCException rdce) {
            // already logged...
        }
        catch(Exception e) {
            logger.severe(strings.get("restart.domain.internalError", e));
        }

    }

    private void doReincarnation() throws RDCException {
        try {
            // TODO JavaClassRunner is very simple and primitive.
            // Feel free to beef it up...

            String[] props = normalProps;

            if(Boolean.parseBoolean(System.getenv("AS_SUPER_DEBUG")))
                props = debuggerProps;  // very very difficult to debug this stuff otherwise!

            new JavaClassRunner(classpath, props, classname, args);
        }
        catch(Exception e) {
            logger.severe(strings.get("restart.domain.jvmError", e));
            throw new RDCException();
        }
    }

    private boolean setupReincarnationWithAsadmin() throws RDCException{
        classpath   = props.getProperty("-asadmin-classpath");
        classname   = props.getProperty("-asadmin-classname");
        argsString  = props.getProperty("-asadmin-args");

        return verify("restart.domain.asadminError");
    }

    private boolean setupReincarnationWithOther() throws RDCException {

        classpath   = props.getProperty("-startup-classpath");
        classname   = props.getProperty("-startup-classname");
        argsString  = props.getProperty("-startup-args");

        return verify("restart.domain.nonAsadminError");
    }

    private boolean verify(String errorStringKey) throws RDCException {
        // Either asadmin or non-asadmin startup params have been set -- check them!
        // THREE possible returns:
        // 1) true
        // 2) false
        // 3) RDCException
        if(classpath == null && classname == null && argsString == null) {
            return false;
        }

        // now that at least one is set -- demand that ALL OF THEM be set...
        if(!ok(classpath) || !ok(classname) || argsString == null) {
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

    private Properties      props;
    private Logger          logger;
    private boolean         verbose;
    private String          classpath;
    private String          classname;
    private String          argsString;
    private String[]        args;

    /////////////             static variables               ///////////////////

    private static final String             magicProperty = "-DAS_RESTART=true";
    private static final String[]           normalProps = { magicProperty };
    private static final LocalStringsImpl   strings = new LocalStringsImpl(RestartDomainCommand.class);
    private static final boolean            debug   = Boolean.parseBoolean(System.getenv("AS_DEBUG"));
    private static final String[]           debuggerProps =
    {
        magicProperty,
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1323" };
}
