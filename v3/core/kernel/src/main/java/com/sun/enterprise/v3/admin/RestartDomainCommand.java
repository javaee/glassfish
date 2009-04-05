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
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.util.Iterator;
import java.util.Collection;

/**
 * AdminCommand to stop the domain execution which mean shuting down the application
 * server.  Then if the proper watchdog is running on the domain machine, the domain
 * will be restarted
 *
 * @author Byron Nevins
 */
@Service(name="restart-domain")
@Async
@I18n("restart.domain.command")
public class RestartDomainCommand implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(StopDomainCommand.class);

    @Inject
    ModulesRegistry registry;

    /**
     * Restart of the application server :
     *
     * All running services are stopped.
     * LookupManager is flushed.
     *
     * Client code that started us should notice the return value of 10 and restart us.
     */
    public void execute(AdminCommandContext context) {
        // This has to be an asynchronous command so there is no way to directly
        // return an error.  We do our best by logging a SEVERE error...

        if(!isRestartAllowed()) {
            context.getLogger().severe(localStrings.getLocalString(
                    "restart.domain.not_enabled",
                    "The server was not started with a watchdog. Restart is not " +
                    "possible.  Try stopping and then locally starting the server."));
            return;
        }

        context.getLogger().info(localStrings.getLocalString("restart.domain.init","Server restart initiated"));
        Collection<Module> modules = registry.getModules(
                "com.sun.enterprise.osgi-adapter");
        if (modules.size() == 1) {
            final Module mgmtAgentModule = modules.iterator().next();
            mgmtAgentModule.stop();
        } else {
            context.getLogger().warning(modules.size() + " no of primordial modules found");
        }

        /* System.exit() is required.  In java this is the only way to return an
         * exit value.
         * Normally I would use a constant with the value of 10.
         * But this code goes into a jar that is sort of an island -- very few
         * dependencies.  The client code on the other end will also just use 10
         */
        System.exit(10);
    }

    boolean isRestartAllowed() {
        // TODO commandline args are clumped into this one System Property - 
        
        String s = System.getProperty("hk2.startup.context.args");
        return s != null && s.indexOf("-watchdog=true") >= 0;
    }
}
