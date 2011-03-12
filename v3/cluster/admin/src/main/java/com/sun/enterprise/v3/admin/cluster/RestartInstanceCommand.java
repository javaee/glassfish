/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.admin.remote.ServerRemoteAdminCommand;
import com.sun.enterprise.admin.util.*;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.util.StringUtils;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.*;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;

/**
 *
 * @author bnevins
 */
@Service(name = "restart-instance")
@Scoped(PerLookup.class)
@CommandLock(CommandLock.LockType.NONE) // don't prevent _synchronize-files
@I18n("restart.instance.command")
@ExecuteOn(RuntimeType.DAS)
public class RestartInstanceCommand implements AdminCommand {
    @Override
    public void execute(AdminCommandContext context) {
        try {
            helper = new RemoteInstanceCommandHelper(habitat);
            report = context.getActionReport();
            logger = context.getLogger();
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

            // Each of the methods below immediately returns if there has been an error
            // This is just to avoid a ton of error-checking in this top-level method
            // i.e. it's for readability.

            if (!env.isDas())
                setError(Strings.get("restart.instance.notDas", env.getRuntimeType().toString()));

            prepare();

            if(!isInstanceRestartable())
                setError(Strings.get("restart.notRestartable", instanceName));

            setOldPid();
            logger.fine("Restart-instance old-pid = " + oldPid);
            callInstance();
            waitForRestart();

            if (!isError()) {
                String msg = Strings.get("restart.instance.success", instanceName);
                logger.info(msg);
                report.setMessage(msg);
            }
        }
        catch (CommandException ex) {
            setError(Strings.get("restart.instance.racError", instanceName,
                    ex.getLocalizedMessage()));
        }
    }

    private void prepare() {
        if (isError())
            return;

        if (!StringUtils.ok(instanceName)) {
            setError(Strings.get("stop.instance.noInstanceName"));
            return;
        }

        instance = helper.getServer(instanceName);

        if (instance == null) {
            setError(Strings.get("stop.instance.noSuchInstance", instanceName));
            return;
        }

        host = instance.getAdminHost();

        if (host == null) {
            setError(Strings.get("stop.instance.noHost", instanceName));
            return;
        }
        port = helper.getAdminPort(instance);

        if (port < 0) {
            setError(Strings.get("stop.instance.noPort", instanceName));
            return;
        }
        logger.finer(ObjectAnalyzer.toString(this));
    }

    /**
     * return null if all went OK...
     *
     */
    private void callInstance() throws CommandException {
        if (isError())
            return;

        String cmdName = "_restart-instance";

        RemoteAdminCommand rac = createRac(cmdName);
        // notice how we do NOT send in the instance's name as an operand!!
        ParameterMap map = new ParameterMap();

        if (debug != null)
            map.add("debug", debug);

        rac.executeCommand(map);
    }

    private boolean isInstanceRestartable() throws CommandException {
        if (isError())
            return false;

        String cmdName = "_get-runtime-info";

        RemoteAdminCommand rac = createRac(cmdName);
        rac.executeCommand(new ParameterMap());
        Map<String, String> atts = rac.getAttributes();

        if (atts != null) {
            String val = atts.get("restartable_value");

            if (val != null && val.equals("false"))
                return false;
        }
        return true;
    }

    private void waitForRestart() {
        if (isError())
            return;

        long deadline = System.currentTimeMillis() + WAIT_TIME_MS;

        while (System.currentTimeMillis() < deadline) {
            try {
                String newpid = getPid();

                // when the next statement is true -- the server has restarted.
                if (StringUtils.ok(newpid) && !newpid.equals(oldPid)) {
                    logger.fine("Restarted instance pid = " + newpid);
                    return;
                }
            }
            catch (Exception e) {
                // ignore.  This is normal!
            }
        }
        setError(Strings.get("restart.instance.timeout", instanceName));
    }

    private RemoteAdminCommand createRac(String cmdName) throws CommandException {
        // I wonder why the signature is so unwieldy?
        // hiding it here...
        return new ServerRemoteAdminCommand(habitat, cmdName, host,
                port, false, "admin", null, logger);
    }

    private void setError(String s) {
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setMessage(s);
    }

    private boolean isError() {
        return report.getActionExitCode() == ActionReport.ExitCode.FAILURE;
    }

    private void setOldPid() throws CommandException {
        if (isError())
            return;

        oldPid = getPid();

        if (!StringUtils.ok(oldPid))
            setError(Strings.get("restart.instance.nopid", instanceName));
    }

    private String getPid() throws CommandException {
        String cmdName = "_get-runtime-info";
        RemoteAdminCommand rac = createRac(cmdName);
        rac.executeCommand(new ParameterMap());
        Map<String, String> map = rac.getAttributes();
        return map.get("pid_value");
    }
    @Inject
    InstanceStateService stateSvc;
    @Inject
    private Habitat habitat;
    @Inject
    private ServerEnvironment env;
    @Param(optional = false, primary = true)
    private String instanceName;
    // no default value!  We use the Boolean as a tri-state.
    @Param(name = "debug", optional = true)
    private String debug;
    private Logger logger;
    private RemoteInstanceCommandHelper helper;
    private ActionReport report;
    private String errorMessage = null;
    private final static long WAIT_TIME_MS = 600000; // 10 minutes
    private Server instance;
    private String host;
    private int port;
    private String oldPid;
}
