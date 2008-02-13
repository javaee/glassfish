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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.admin;

import com.sun.enterprise.module.impl.Utils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.InjectionManager;
import org.jvnet.hk2.component.UnsatisfiedDepedencyException;

/**
 * Encapsulates the logic needed to execute a server-side command (for example,  
 * a descendant of AdminCommand) including injection of argument values into the 
 * command.  
 * 
 * @author dochez
 * @author tjquinn
 */
@Service
public class CommandRunner {
    
    public final static LocalStringManagerImpl adminStrings = new LocalStringManagerImpl(CommandRunner.class);
    public final static Logger logger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    @Inject
    Habitat habitat;

    /**
     * Executes a command by name.
     * <p>
     * The commandName parameter value should correspond to the name of a 
     * command that is a service with that name.
     * @param commandName the command to execute
     * @param parameters name/value pairs to be passed to the command
     * @param report will hold the result of the command's execution
     */
    public void doCommand(final String commandName, final Properties parameters, final ActionReport report) {

        final AdminCommand handler = getCommand(commandName, report, logger);
        if (handler==null) {
            return;
        }
        doCommand(commandName, handler, parameters, report);
    }

    /**
     * Executes the provided command object.
     * @param commandName name of the command (used for logging and reporting)
     * @param handler the command service to execute
     * @param parameters name/value pairs to be passed to the command
     * @param report will hold the result of the command's execution
     */
    public void doCommand(
            final String commandName, 
            final AdminCommand command, 
            final Properties parameters, 
            final ActionReport report) {
        
        if (parameters.size()==1 && parameters.get("help")!=null) {
            usage(commandName, command, report);
            return;
        }
        report.setActionDescription(commandName + " AdminCommand");

        final AdminCommandContext context = new AdminCommandContext(
                LogDomains.getLogger(LogDomains.ADMIN_LOGGER),
                report, parameters);                                                 

        // initialize the injector.
        InjectionManager injectionMgr =  new InjectionManager<Param>() {

            @Override
            protected boolean isOptional(Param annotation) {
                return annotation.optional();
            }

            protected Object getValue(Object component, AnnotatedElement target, Class type) throws ComponentException {
                // look for the name in the list of parameters passed.
                Param param = target.getAnnotation(Param.class);
                if (param.primary()) {
                    // this is the primary parameter for the command
                    String value = parameters.getProperty("DEFAULT");
                    if (value!=null) {
                        // let's also copy this value to the command with a real name.
                        parameters.setProperty(getParamName(param, target), value);
                        return value;
                    }
                }
                return parameters.get(getParamName(param, target));
            }
        };

        LocalStringManagerImpl localStrings = new LocalStringManagerImpl(command.getClass());

        // Let's get the command i18n key
        I18n i18n = command.getClass().getAnnotation(I18n.class);
        String i18n_key = "";
        if (i18n!=null) {
            i18n_key = i18n.value();
        }

        // inject
        try {
            injectionMgr.inject(command, Param.class);
        } catch (UnsatisfiedDepedencyException e) {
            Param param = e.getUnsatisfiedElement().getAnnotation(Param.class);
            String paramName = getParamName(param, e.getUnsatisfiedElement());
            String paramDesc = getParamDescription(localStrings, i18n_key, paramName, e.getUnsatisfiedElement());

            String errorMsg;
            if (paramDesc!=null) {
                errorMsg = adminStrings.getLocalString("admin.param.missing",
                        "{0} command requires the {1} parameter : {2}", commandName, paramName, paramDesc);
            } else {
                errorMsg = adminStrings.getLocalString("admin.param.missing.nodesc",
                        "{0} command requires the {1} parameter", commandName, paramName);
            }
            logger.severe(errorMsg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(errorMsg);
            report.setFailureCause(e);
            return;
        } catch (ComponentException e) {
            logger.severe(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
            report.setFailureCause(e);
        }

        // the command may be an asynchronous command, so we need to check
        // for the @Async annotation.
        Async async = command.getClass().getAnnotation(Async.class);
        if (async==null) {
            try {
                command.execute(context);
            } catch(Throwable e) {
                logger.log(Level.SEVERE,
                        adminStrings.getLocalString("adapter.exception","Exception in command execution : ", e), e);
            }
        } else {
            Thread t = new Thread() {
                public void run() {
                    try {
                        command.execute(context);
                    } catch (RuntimeException e) {
                        logger.log(Level.SEVERE,e.getMessage(), e);
                    }
                }
            };
            t.setPriority(async.priority());
            t.start();
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            report.setMessage(
                    adminStrings.getLocalString("adapter.command.launch", "{0} launch successful", commandName));
        }
    }

    protected String getParamDescription(LocalStringManagerImpl localStrings, String i18nKey, String paramName, AnnotatedElement annotated) {

        I18n i18n = annotated.getAnnotation(I18n.class);
        String paramDesc;
        if (i18n==null) {
            paramDesc = localStrings.getLocalString(i18nKey+"."+paramName, null);
        } else {
            paramDesc = localStrings.getLocalString(i18n.value(), null);
        }
        if (paramDesc==null) {
            paramDesc = adminStrings.getLocalString("adapter.nodesc", "no description provided");
        }
        return paramDesc;        
    }

    protected String getParamName(Param param, AnnotatedElement annotated) {
        if (param.name().equals("")) {
            if (annotated instanceof Field) {
                return ((Field) annotated).getName();
            }
            if (annotated instanceof Method) {
                return ((Method) annotated).getName().substring(3).toLowerCase();
            }
        } else {
            return param.name();
        }
        return "";
    }
    /**
     * Return Command handlers from the lookup or if not found in the lookup,
     * look at META-INF/services implementations and add them to the lookup
     * @param commandName the request handler's command name
     * @param report the reporting facility
     * @return the admin command handler if found
     *
     */
    private AdminCommand getCommand(String commandName, ActionReport report, Logger logger) {

        AdminCommand command = null;
        try {
            command = habitat.getComponent(AdminCommand.class, commandName);
        } catch(ComponentException e) {
           e.printStackTrace();
        }
        if (command==null) {
            String msg = adminStrings.getLocalString("adapter.command.notfound", "Command {0} not found", commandName);
            report.setMessage(msg);
            Utils.getDefaultLogger().info(msg);
        }
        return command;
    }

    public void usage(String commandName, AdminCommand command, ActionReport report) {
        
        report.setActionDescription(commandName + " help");
        LocalStringManagerImpl localStrings = new LocalStringManagerImpl(command.getClass());

        // Let's get the command i18n key
        I18n i18n = command.getClass().getAnnotation(I18n.class);
        String i18nKey = "";
        if (i18n!=null) {
            i18nKey = i18n.value();
        }
        report.setMessage(localStrings.getLocalString(i18nKey, null));

        for (Field f : command.getClass().getDeclaredFields()) {
            addParamUsage(report, localStrings, i18nKey, f);
        }
        for (Method m : command.getClass().getDeclaredMethods()) {
            addParamUsage(report, localStrings, i18nKey, m);
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void addParamUsage(ActionReport report, LocalStringManagerImpl localStrings, String i18nKey, AnnotatedElement annotated) {

        Param param = annotated.getAnnotation(Param.class);
        if (param!=null) {
            // this is a param.
            String paramName = getParamName(param, annotated);
            report.getTopMessagePart().addProperty(paramName, getParamDescription(localStrings, i18nKey, paramName, annotated));
        }
    }
    

}

