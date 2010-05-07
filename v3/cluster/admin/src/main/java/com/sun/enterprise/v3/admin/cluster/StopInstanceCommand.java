/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.util.Iterator;
import java.util.Collection;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.server.ServerEnvironmentImpl;

/**
 * AdminCommand to stop the instance
 * server.
 *
 * @author Byron Nevins
 */
@Service(name="stop-instance")
@Async
@I18n("stop.instance.command")
public class StopInstanceCommand implements AdminCommand {

    final private static LocalStringsImpl strings = new LocalStringsImpl(StopInstanceCommand.class);
    
    @Inject
    ModulesRegistry registry;

    @Inject
    private ServerEnvironment env;

    @Param(optional=true, defaultValue="true")
    Boolean force;
    

    /**
     * Shutdown of the application server : 
     *
     * All running services are stopped. 
     * LookupManager is flushed.
     */
    public void execute(AdminCommandContext context) {

        if(!env.isInstance()) {
            // This command is asynchronous.  We can't return anything so we just
            // log the error and return
            String msg = strings.get("stop.instance.notAnInstance",
                            env.getRuntimeType().toString());

            context.getLogger().warning(msg);
            return;
        }

        context.getLogger().info(strings.get("stop.instance.init"));
        Collection<Module> modules = registry.getModules(
                "org.glassfish.core.glassfish");
        if (modules.size() == 1) {
            final Module mgmtAgentModule = modules.iterator().next();
            mgmtAgentModule.stop();
        } else {
            context.getLogger().warning(modules.size() + " no of primordial modules found");
        }
        if (force) {
            System.exit(0);
        }
    }
}
