/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;

import com.sun.grizzly.config.dom.ThreadPool;
import com.sun.enterprise.config.serverbeans.ThreadPools;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;


/**
 * Create Thread Pool Command
 *
 */
@Service(name="create-threadpool")
@Scoped(PerLookup.class)
@I18n("create.threadpool")

public class CreateThreadpool implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new
            LocalStringManagerImpl(CreateThreadpool.class);

    @Param(name="maxthreadpoolsize", optional=true)
    String maxthreadpoolsize;

    @Param(name="minthreadpoolsize", optional=true)
    String minthreadpoolsize;

    @Param(name= "idletimeout", optional=true)
    String idletimeout;

    @Param(name="workqueues", optional=true)
    String workqueues;

    @Param(name="maxqueuesize", optional=true)
    String maxQueueSize;

    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="threadpool_id", primary=true)
    String threadpool_id;

    @Inject
    Configs configs;



    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        List<Config> configList = configs.getConfig();
        Config config = configList.get(0);
        ThreadPools threadPools = config.getThreadPools();

        for (ThreadPool pool: threadPools.getThreadPool()) {
            if (pool.getName().equals(threadpool_id)) {
                report.setMessage(localStrings.getLocalString("create.threadpool.duplicate",
                        "Thread Pool named {0} already exists.", threadpool_id));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        if (workqueues != null) {
            report.setMessage(localStrings.getLocalString("create.threadpool.deprecated.workqueues",
                        "Deprecated Syntax: --workqueues option is deprecated for create-threadpool command."));
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<ThreadPools>() {
                public Object run(ThreadPools param) throws PropertyVetoException, TransactionFailure {
                    ThreadPool newPool = param.createChild(ThreadPool.class);
                    newPool.setName(threadpool_id);
                    newPool.setMaxThreadPoolSize(maxthreadpoolsize);
                    newPool.setMinThreadPoolSize(minthreadpoolsize);
                    newPool.setMaxQueueSize(maxQueueSize);
                    newPool.setIdleThreadTimeoutSeconds(idletimeout);
                    param.getThreadPool().add(newPool);
                    return newPool;
                }
            }, threadPools);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure e) {
            String str = e.getMessage();
            String def = "Creation of: " + threadpool_id + "failed because of: " + str;
            String msg = localStrings.getLocalString("create.threadpool.failed", def, threadpool_id, str);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
