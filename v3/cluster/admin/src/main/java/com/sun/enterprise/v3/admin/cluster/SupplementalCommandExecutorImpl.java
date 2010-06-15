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
 *
 */
package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.hk2.component.InjectionResolver;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.common.util.admin.UnacceptableValueException;
import org.jvnet.hk2.component.*;
import org.glassfish.common.util.admin.CommandModelImpl;
import org.glassfish.config.support.GenericCrudCommand;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An executor that executes Supplemental commands means for current command
 *
 * @author Vijay Ramachandran
 */
@Service(name="SupplementalCommandExecutorImpl")
public class SupplementalCommandExecutorImpl implements SupplementalCommandExecutor {

    @Inject
    private Domain domain;

    @Inject
    private ExecutorService threadExecutor;

    @Inject
    private Habitat habitat;

    @Inject
    private ServerEnvironment serverEnv;

    private static final LocalStringManagerImpl strings =
                        new LocalStringManagerImpl(SupplementalCommandExecutor.class);

    private Map<String, List<SupplementalCommand>> supplementalCommandsMap = null;

    public ActionReport.ExitCode execute(String commandName, Supplemental.Timing time,
                                         AdminCommandContext context, InjectionResolver<Param> injector) {
        //TODO : Use the executor service to parallelize this
        ActionReport.ExitCode finalResult = ActionReport.ExitCode.SUCCESS;
        ActionReport originalReport = context.getActionReport();
        if(!getSupplementalCommandsList().isEmpty() && getSupplementalCommandsList().containsKey(commandName)) {
            List<SupplementalCommand> cmds = getSupplementalCommandsList().get(commandName);
            for(SupplementalCommand aCmd : cmds) {
                if( (serverEnv.isDas() && aCmd.whereToRun().contains(RuntimeType.DAS)) ||
                    (serverEnv.isInstance() && aCmd.whereToRun().contains(RuntimeType.INSTANCE)) ) {
                    if( (time.equals(Supplemental.Timing.Before) && aCmd.toBeExecutedBefore()) ||
                        (time.equals(Supplemental.Timing.After) && aCmd.toBeExecutedAfter()) ) {
                        ActionReport subReport = originalReport.addSubActionsReport();
                        ActionReport.ExitCode result = FailurePolicy.applyFailurePolicy(aCmd.onFailure(),
                                inject(aCmd, injector, subReport));
                        if(!result.equals(ActionReport.ExitCode.SUCCESS)) {
                            if(finalResult.equals(ActionReport.ExitCode.SUCCESS))
                                finalResult = result;
                            continue;
                        }
                        context.setActionReport(subReport);
                        aCmd.execute(context);
                        if(subReport.hasFailures()) {
                            result = FailurePolicy.applyFailurePolicy(aCmd.onFailure(), ActionReport.ExitCode.FAILURE);
                        } else if(subReport.hasWarnings()) {
                            result = FailurePolicy.applyFailurePolicy(aCmd.onFailure(), ActionReport.ExitCode.WARNING);
                        }
                        if(!result.equals(ActionReport.ExitCode.SUCCESS)) {
                            if(finalResult.equals(ActionReport.ExitCode.SUCCESS))
                                finalResult = result;
                        }
                    }
                }
            }
        }
        context.setActionReport(originalReport);
        return finalResult;
    }

    /**
     * Get list of all supplemental commands, map it to various commands and cache htis list
     */
    private Map<String, List<SupplementalCommand>> getSupplementalCommandsList() {
        if(supplementalCommandsMap == null) {
            synchronized(this) {
                if(supplementalCommandsMap == null) {
                    supplementalCommandsMap = new ConcurrentHashMap<String, List<SupplementalCommand>>();
                    Collection<Inhabitant<? extends Supplemental>> supplementals = habitat.getInhabitants(Supplemental.class);
                    if(!supplementals.isEmpty()) {
                        Iterator<Inhabitant<? extends Supplemental>> iter = supplementals.iterator();
                        while(iter.hasNext()) {
                            Inhabitant<? extends Supplemental> inh = iter.next();
                            MultiMap<String, String> map = inh.metadata();
                            String commandName = map.getOne("target");
                            AdminCommand cmdObject = (AdminCommand) inh.get();
                            Supplemental ann = cmdObject.getClass().getAnnotation(Supplemental.class);
                            SupplementalCommand cmd = new SupplementalCommand(cmdObject, ann.on(), ann.ifFailure());
                            if(supplementalCommandsMap.containsKey(commandName)) {
                                supplementalCommandsMap.get(commandName).add(cmd);
                            } else {
                                ArrayList<SupplementalCommand> cmdList = new ArrayList<SupplementalCommand>();
                                cmdList.add(cmd);
                                supplementalCommandsMap.put(commandName, cmdList);
                            }
                        }
                    }
                }
            }
        }
        return supplementalCommandsMap;
    }

    private ActionReport.ExitCode inject(SupplementalCommand cmd,InjectionResolver<Param> injector, ActionReport subActionReport) {

        ActionReport.ExitCode result = ActionReport.ExitCode.SUCCESS;
        try {
            new InjectionManager().inject(cmd.command, injector);
        } catch (Exception e) {
            result = ActionReport.ExitCode.FAILURE;
            subActionReport.setActionExitCode(result);
            subActionReport.setMessage(e.getMessage());
            subActionReport.setFailureCause(e);
        }
        return result;
    }

    private class SupplementalCommand {
        private AdminCommand command;
        private Supplemental.Timing timing;
        private FailurePolicy failurePolicy;
        private List<RuntimeType> whereToRun = new ArrayList<RuntimeType>();

        public SupplementalCommand(AdminCommand cmd, Supplemental.Timing time, FailurePolicy onFail) {
            command = cmd;
            timing = time;
            failurePolicy = onFail;
            org.glassfish.api.admin.Cluster ann =
                    cmd.getClass().getAnnotation(org.glassfish.api.admin.Cluster.class);
            if( ann == null) {
                whereToRun.add(RuntimeType.DAS);
                whereToRun.add(RuntimeType.INSTANCE);
            } else {
                if(ann.value().length == 0) {
                    whereToRun.add(RuntimeType.DAS);
                    whereToRun.add(RuntimeType.INSTANCE);
                } else {
                    for(RuntimeType t : ann.value()) {
                        whereToRun.add(t);
                    }
                }
            }
        }

        public void execute(AdminCommandContext ctxt) {
            command.execute(ctxt);
        }

        public boolean toBeExecutedBefore() {
            return timing.equals(Supplemental.Timing.Before);
        }

        public boolean toBeExecutedAfter() {
            return timing.equals(Supplemental.Timing.After);
        }

        public FailurePolicy onFailure() {
            return failurePolicy;
        }

        public List<RuntimeType> whereToRun() {
            return whereToRun;
        }
    }
}
