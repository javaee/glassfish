/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.module.common_impl.LogHelper;

import com.sun.enterprise.universal.collections.ManifestUtils;
import com.sun.enterprise.universal.glassfish.AdminCommandResponse;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;
import java.io.*;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import com.sun.hk2.component.InjectionResolver;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.InjectionManager;
import org.jvnet.hk2.component.UnsatisfiedDepedencyException;
import com.sun.enterprise.universal.GFBase64Decoder;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandModelImpl;
import com.sun.enterprise.v3.common.XMLContentActionReporter;

/**
 * Encapsulates the logic needed to execute a server-side command (for example,  
 * a descendant of AdminCommand) including injection of argument values into the 
 * command.  
 * 
 * @author dochez
 * @author tjquinn
 * @author Bill Shannon
 */
@Service
public class CommandRunnerImpl implements CommandRunner {
    
    public final static LocalStringManagerImpl adminStrings = new LocalStringManagerImpl(CommandRunnerImpl.class);
    public Logger logger = Logger.getLogger(CommandRunnerImpl.class.getName());

    private static final String ASADMIN_CMD_PREFIX = "AS_ADMIN_";
    public final InjectionManager injectionMgr = new InjectionManager();
    
    @Inject
    Habitat habitat;

    @Inject
    ClassLoaderHierarchy clh;

    public void postConstruct() {
         logger = LogDomains.getLogger(CommandRunnerImpl.class, LogDomains.ADMIN_LOGGER);
    }
    /**
     * Returns a uninitialized action report
     * @param name action report type name
     * @return unitialized action report
     */
    public ActionReport getActionReport(String name) {
        return habitat.getComponent(ActionReport.class, name);
    }

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

        doCommand(commandName, parameters, report, null, null);
    }
    
    /**
     * Executes a command by name.
     * <p>
     * The commandName parameter value should correspond to the name of a 
     * command that is a service with that name.
     * @param commandName the command to execute
     * @param parameters name/value pairs to be passed to the command
     * @param report will hold the result of the command's execution
     * @param inboundPayload files uploaded from the client
     * @param outboundPayload files downloaded to the client
     */
    public void doCommand(final String commandName, final Properties parameters,
            final ActionReport report, Payload.Inbound inboundPayload, Payload.Outbound outboundPayload) {

        final AdminCommand handler = getCommand(commandName, report, logger);
        if (handler==null) {
            return;
        }
        CommandModel model = new CommandModelImpl(handler.getClass());
        doCommand(model, handler, parameters, report, inboundPayload, outboundPayload);
    }

    /**
     * Executes the provided command object.
     * @param commandName name of the command (used for logging and reporting)
     * @param command the command service to execute
     * @param parameters name/value pairs to be passed to the command
     * @param report will hold the result of the command's execution
     */
    
    public void doCommand(
            final String commandName, 
            final AdminCommand command, 
            final Properties parameters, 
            final ActionReport report) {
        CommandModel model = new CommandModelImpl(command.getClass());
        doCommand(model, command, parameters, report, null, null);
    }

    public InjectionResolver<Param> getDelegatedResolver(final CommandModel model, final Object parameters) {

        return new InjectionResolver<Param>(Param.class) {

            @Override
            public boolean isOptional(AnnotatedElement element, Param annotation) {
                String name = model.getParamName(annotation, element);
                CommandModel.ParamModel param = model.getModelFor(name);
                return param.getParam().optional();
            }

            @Override
            public Object getValue(Object component, AnnotatedElement target, Class type) throws ComponentException {

                // look for the name in the list of parameters passed.
                if (target instanceof Field) {
                    Field targetField = (Field) target;
                    try {
                        Field sourceField = parameters.getClass().getField(targetField.getName());
                        targetField.setAccessible(true);
                        Object paramValue = sourceField.get(parameters);
/*
                        if (paramValue==null) {
                            return convertStringToObject(target, type, param.defaultValue());
                        }
*/
                        // XXX temp fix, to revisit
                        if (paramValue != null) {
                        checkAgainstAcceptableValues(target, paramValue.toString());
                        }
                        return paramValue;
                    } catch (IllegalAccessException e) {
                    } catch (NoSuchFieldException e) {
                    }
                }
                return null;
            }

        };
    }

    private InjectionResolver<Param> getPropsBasedResolver(final CommandModel model, final Properties parameters) {

       return new InjectionResolver<Param>(Param.class) {

           @Override
            public boolean isOptional(AnnotatedElement element, Param annotation) {
               String name = model.getParamName(annotation, element);
               CommandModel.ParamModel param = model.getModelFor(name);
               return param.getParam().optional();
            }

           @Override
            public Object getValue(Object component, AnnotatedElement target, Class type) throws ComponentException {
                // look for the name in the list of parameters passed.
                Param param = target.getAnnotation(Param.class);
                //String acceptable = param.acceptableValues();
                String paramName = getParamName(param, target);
                if (param.primary()) {
                    // this is the primary parameter for the command
                    String value = parameters.getProperty("DEFAULT");
                    if (value!=null) {
                        // let's also copy this value to the command with a real name.
                        parameters.setProperty(paramName, value);
                        return convertStringToObject(target, type, value);
                    }
                }
                String paramValueStr = getParamValueString(parameters, param,
                                                           target);

                checkAgainstAcceptableValues(target, paramValueStr);
                if (paramValueStr != null) {
                    return convertStringToObject(target, type, paramValueStr);
                }
                //return default value
                return getParamField(component, target);
            }
        };
    }

    public ActionReport doCommand(
        final String commandName,
        final Object parameters,
        final ActionReport report,
        final Payload.Inbound inboundPayload,
        final Payload.Outbound outboundPayload) {

        final AdminCommand command = getCommand(commandName, report, logger);
        if (command==null) {
            return report;
        }
        final CommandModel model = new CommandModelImpl(command.getClass());
        
        InjectionResolver<Param> injectionTarget =  new InjectionResolver<Param>(Param.class) {

            @Override
            public boolean isOptional(AnnotatedElement element, Param annotation) {
                String name = model.getParamName(annotation, element);
                CommandModel.ParamModel param = model.getModelFor(name);
                return param.getParam().optional();
            }

            public Object getValue(Object component, AnnotatedElement target, Class type) throws ComponentException {

                // look for the name in the list of parameters passed.
                Param param = target.getAnnotation(Param.class);
                String acceptable = param.acceptableValues();
                String paramName = getParamName(param, target);
                
                if (target instanceof Field) {
                    Field targetField = (Field) target;
                    try {
                        Field sourceField = parameters.getClass().getField(targetField.getName());
                        targetField.setAccessible(true);
                        Object paramValue = sourceField.get(parameters);
/*
                        if (paramValue==null) {
                            return convertStringToObject(target, type, param.defaultValue());
                        }
*/
                        // XXX temp fix, to revisit 
                        if (paramValue != null) {
                        checkAgainstAcceptableValues(target, paramValue.toString());
                        }
                        return paramValue;
                    } catch (IllegalAccessException e) {
                    } catch (NoSuchFieldException e) {
                    }
                }
                return null;
            }
        };
        return doCommand(model, command, injectionTarget, report, inboundPayload, outboundPayload);

    }

    /**
     * Executes the provided command object.
     * @param model model of the command (used for logging and reporting)
     * @param command the command service to execute
     * @param injector injector capable of populating the command parameters
     * @param report will hold the result of the command's execution
     * @param inboundPayload files uploaded from the client
     * @param outboundPayload files downloaded to the client
     */

    public ActionReport doCommand(
            final CommandModel model,
            final AdminCommand command,
            final InjectionResolver<Param> injector,
            final ActionReport report,
            final Payload.Inbound inboundPayload,
            final Payload.Outbound outboundPayload) {

        report.setActionDescription(model.getCommandName() + " AdminCommand");

        final AdminCommandContext context = new AdminCommandContext(
                LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER),
                report, inboundPayload, outboundPayload);

        LocalStringManagerImpl localStrings = new LocalStringManagerImpl(command.getClass());

        // Let's get the command i18n key
        I18n i18n = model.getI18n();
        String i18n_key = "";
        if (i18n!=null) {
            i18n_key = i18n.value();
        }

        // inject
        try {
            injectionMgr.inject(command, injector);
        } catch (UnsatisfiedDepedencyException e) {
            Param param = e.getAnnotation(Param.class);
            CommandModel.ParamModel paramModel=null;
            for (CommandModel.ParamModel pModel : model.getParameters()) {
                if (pModel.getParam().equals(param)) {
                    paramModel = pModel;
                    break;
                }
            }
            String errorMsg;            
            final String usage = getUsageText(command, model);
            if (paramModel!=null) {
                String paramName = paramModel.getName();
                String paramDesc = getParamDescription(localStrings, i18n_key, paramModel);

                if (param.primary()) {
                    errorMsg = adminStrings.getLocalString("commandrunner.operand.required",
                                                           "Operand required.");
                }
                else if (param.password()) {
                    errorMsg = adminStrings.getLocalString("adapter.param.missing.passwordfile", "{0} command requires the passwordfile parameter containing {1} entry.",
                            model.getCommandName(), paramName);
                }
                else if (paramDesc!=null) {
                    errorMsg = adminStrings.getLocalString("admin.param.missing",
                                                           "{0} command requires the {1} parameter ({2})",
                                                            model.getCommandName(), paramName, paramDesc);

                }
                else {
                    errorMsg = adminStrings.getLocalString("admin.param.missing.nodesc",
                            "{0} command requires the {1} parameter", model.getCommandName(), paramName);
                }
            } else {
                errorMsg = adminStrings.getLocalString("admin.param.missing.nofound",
                       "Cannot find {1} in {0} command model, file a bug", model.getCommandName(), e.getUnsatisfiedName());
            }
            logger.severe(errorMsg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(errorMsg);
            report.setFailureCause(e);
            ActionReport.MessagePart childPart = report.getTopMessagePart().addChild();
            childPart.setMessage(usage);
            return report;
        } catch (ComponentException e) {
            // if the cause is UnacceptableValueException -- we want the message
            // from it.  It is wrapped with a less useful Exception

            Exception exception = e;
            Throwable cause = e.getCause();
            if(cause != null && (cause instanceof UnacceptableValueException || cause instanceof IllegalArgumentException)) {
                // throw away the wrapper.
                exception = (Exception)cause;
            }
            logger.log(Level.SEVERE, "invocation.exception",exception);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(exception.getMessage());
            report.setFailureCause(exception);
            ActionReport.MessagePart childPart = report.getTopMessagePart().addChild();
            childPart.setMessage(getUsageText(command, model));
            return report;
        }

        // We need to set context CL to common CL before executing
        // the command. See issue #5596
        final AdminCommand wrappedComamnd = new AdminCommand() {
            public void execute(AdminCommandContext context) {
                Thread thread = Thread.currentThread();
                ClassLoader origCL = thread.getContextClassLoader();
                ClassLoader ccl = clh.getCommonClassLoader();
                if (origCL != ccl) {
                    try {
                        thread.setContextClassLoader(ccl);
                        command.execute(context);
                    } finally {
                        thread.setContextClassLoader(origCL);
                    }
                } else {
                    command.execute(context);
                }
            }
        };

        // the command may be an asynchronous command, so we need to check
        // for the @Async annotation.
        Async async = command.getClass().getAnnotation(Async.class);
        if (async==null) {
            try {
                wrappedComamnd.execute(context);
            } catch(Throwable e) {
                System.out.println("logger = " + logger);
                logger.log(Level.SEVERE,
                        adminStrings.getLocalString("adapter.exception","Exception in command execution : ", e), e);
                report.setMessage(e.toString());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(e);
            }
        } else {
            Thread t = new Thread() {
                public void run() {
                    try {
                        wrappedComamnd.execute(context);
                    } catch (RuntimeException e) {
                        logger.log(Level.SEVERE,e.getMessage(), e);
                    }
                }
            };
            t.setPriority(async.priority());
            t.start();
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            report.setMessage(
                    adminStrings.getLocalString("adapter.command.launch", "Command {0} was successfully initiated asynchronously.",
                            model.getCommandName()));
        }
        return context.getActionReport();   
    }
        
    /**
     * Executes the provided command object.
     * @param model model of the command 
     * @param command the command service to execute
     * @param parameters name/value pairs to be passed to the command
     * @param report will hold the result of the command's execution
     * @param inboundPayload files uploaded from the client
     * @param outboundPayload files downloaded to the client
     */
    
    public ActionReport doCommand(
            final CommandModel model,
            final AdminCommand command,
            final Properties parameters,
            final ActionReport report,
            final Payload.Inbound inboundPayload,
            final Payload.Outbound outboundPayload) {


        if (parameters.get("help")!=null || parameters.get("Xhelp")!=null) {
            InputStream in = getManPage(model.getCommandName(), command);
            String manPage = encodeManPage(in);

            if(manPage != null && parameters.get("help")!=null) {
                report.getTopMessagePart().addProperty("MANPAGE", manPage);
            }
            else {
                report.getTopMessagePart().addProperty(AdminCommandResponse.GENERATED_HELP, "true");
                getHelp(command, report);
            }
            return report;
        }

        try {
            if (!skipValidation(command)) {
                validateParameters(model, parameters);
            }
        } catch (ComponentException e) {
            // if the cause is UnacceptableValueException -- we want the message
            // from it.  It is wrapped with a less useful Exception

            Exception exception = e;
            Throwable cause = e.getCause();
            if(cause != null && (cause instanceof UnacceptableValueException)) {
                // throw away the wrapper.
                exception = (Exception)cause;
            }
            logger.severe(exception.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(exception.getMessage());
            report.setFailureCause(exception);
            ActionReport.MessagePart childPart = report.getTopMessagePart().addChild();
            childPart.setMessage(getUsageText(command, model));
            return report;
        }

        // initialize the injector.
        InjectionResolver<Param> injectionMgr =  getPropsBasedResolver(model, parameters);
        return doCommand(model, command, injectionMgr, report, inboundPayload, outboundPayload);

    }

    private void checkAgainstAcceptableValues(AnnotatedElement target, String paramValueStr ) {

        Param param = target.getAnnotation(Param.class);
        String acceptable = param.acceptableValues();
        String paramName = getParamName(param, target);
        
        if(ok(acceptable)&& ok(paramValueStr)) {
            String[] ss = acceptable.split(",");
            boolean ok = false;

            for(String s : ss) {
                if(paramValueStr.equals(s.trim())) {
                    ok = true;
                    break;
                }
            }
            if(!ok)
                throw new UnacceptableValueException(
                    adminStrings.getLocalString("adapter.command.unacceptableValue",
                    "Invalid parameter: {0}.  Its value is {1} but it isn''t one of these acceptable values: {2}",
                    paramName,
                    paramValueStr,
                    acceptable));
        }
    }

    protected String getParamDescription(LocalStringManagerImpl localStrings, String i18nKey, CommandModel.ParamModel model) {

        I18n i18n = model.getI18n();
        String paramDesc;
        if (i18n==null) {
            paramDesc = localStrings.getLocalString(i18nKey+"."+model.getName(), "");
        } else {
            paramDesc = localStrings.getLocalString(i18n.value(), "");
        }
        if (paramDesc==null) {
            paramDesc = "";
//            paramDesc = adminStrings.getLocalString("adapter.nodesc", "no description provided");
        }
        return paramDesc;        
    }

        /**
         * get the Param name.  First it checks if the annotated Param
         * includes a the name, if not then get the name from the field.
         *
         * @param - Param class annotation
         * @annotated - annotated element
         * @return the name of the param
         */
    String getParamName(Param param, AnnotatedElement annotated) {
        if (param.name().equals("")) {
            if (annotated instanceof Field) {
                return ((Field) annotated).getName();
            }
            if (annotated instanceof Method) {
                return ((Method) annotated).getName().substring(3).toLowerCase();
            }
        } else if (param.password() == true) {
            return ASADMIN_CMD_PREFIX + param.name().toUpperCase();
        } else {
            return param.name();
        }
        return "";
    }

    
        /**
         * get the param value.  checks if the param (option) value
         * is defined on the command line (URL passed by the client)
         * by calling getPropertiesValue method.  If not, then check
         * for the shortName.  If param value is not given by the
         * shortName (short option) then if the default valu is
         * defined.
         * 
         * @param parameters - parameters from the command line.
         * @param param - from the annotated Param
         * @param target - annotated element
         *
         * @return param value
         */
    String getParamValueString(final Properties parameters,
                               final Param param,
                               final AnnotatedElement target) {
        String paramValueStr = getPropertiesValue(parameters,
                                                  getParamName(param, target),
                                                  true);
        if (paramValueStr == null) {
                //check for shortName
            paramValueStr = parameters.getProperty(param.shortName());
        }
            //if paramValueStr is still null, then check to
            //see if the defaultValue is defined
        if (paramValueStr == null) {
            final String defaultValue = param.defaultValue();
            paramValueStr = (defaultValue.equals(""))?null:defaultValue;
        }
        return paramValueStr;
    }


        /**
         * get the value of the field.  This value is defined in the
         * annotated Param declaration.  For example:
         * <code>
         * @Param(optional=true)
         * String name="server"
         * </code>
         * The Field, name's value, "server" is returned.
         *
         * @param component - command class object
         * @param annotated - annotated element
         *
         * @return the annotated Field value
         */
    Object getParamField(final Object component,
                         final AnnotatedElement annotated) {
        try {
            if (annotated instanceof Field) {
                Field field = (Field)annotated;
                field.setAccessible(true);
                return ((Field) annotated).get(component);
            }
        }
        catch (Exception e) {
                //unable to get the field value, may not be defined
                //return null instead.
            return null;
        }
        return null;
    }

        /**
         * convert the String parameter to the specified type.
         * For example if type is Properties and the String
         * value is: name1=value1:name2=value2:...
         * then this api will convert the String to a Properties
         * class with the values {name1=name2, name2=value2, ...}
         *
         * @param type - the type of class to convert
         * @param paramValStr - the String value to convert
         *
         * @return Object
         */
        Object convertStringToObject(AnnotatedElement target, Class type, String paramValStr) {
            Param param = target.getAnnotation(Param.class);
            Object paramValue = paramValStr;
            if (type.isAssignableFrom(String.class)) {
                paramValue = paramValStr;
            } else if (type.isAssignableFrom(Properties.class)) {
                paramValue = convertStringToProperties(paramValStr, param.separator());
            } else if (type.isAssignableFrom(List.class)) {
                paramValue = convertStringToList(paramValStr, param.separator());
            } else if (type.isAssignableFrom(Boolean.class)) {
                String paramName = getParamName(param, target);
                paramValue = convertStringToBoolean(paramName, paramValStr);
            } else if (type.isAssignableFrom(String[].class)) {
                paramValue = convertStringToStringArray(paramValStr, param.separator());
            } else if (type.isAssignableFrom(File.class)) {
                return new File(paramValStr);
            }
            return paramValue;
        }


    /**
         *  Searches for the property with the specified key in this property list.
         *  The method returns null if the property is not found.
         *  @see java.util.Properties#getProperty(java.lang.String)
         *
         *  @param props - the property to search in
         *  @param key - the property key
         *  @param ignoreCase - true to search the key ignoring case
         *                      false otherwise
         *  @return the value in this property list with the specified key value.
         */
    String getPropertiesValue(final Properties props, final String key,
                              final boolean ignoreCase) {
        GFBase64Decoder base64Decoder = new GFBase64Decoder();
        if (ignoreCase) {
            for (Object propObj : props.keySet()) {
                final String propName = (String)propObj;
                if (propName.equalsIgnoreCase(key)) {
                    try {
                    if (propName.startsWith(ASADMIN_CMD_PREFIX))
                        return new String(base64Decoder.decodeBuffer(
                                props.getProperty(propName)));
                    } catch (IOException e) {
                        // ignore for now. Not much can be done anyway.
                        // todo: improve this error condition reporting
                    }
                    return props.getProperty(propName);
                }
            }
        }
        return props.getProperty(key);
    }

    
    /**
     * Return Command handlers from the lookup or if not found in the lookup,
     * look at META-INF/services implementations and add them to the lookup
     * @param commandName the request handler's command name
     * @param report the reporting facility
     * @return the admin command handler if found
     *
     */
    public AdminCommand getCommand(String commandName, ActionReport report, Logger logger) {

        AdminCommand command = null;
        try {
            command = habitat.getComponent(AdminCommand.class, commandName);
        } catch(ComponentException e) {
           e.printStackTrace();
        }
        if (command==null) {
            String msg;
            
            if(!ok(commandName))
                msg = adminStrings.getLocalString("adapter.command.nocommand", "No command was specified.");
            else {
                msg = adminStrings.getLocalString("adapter.command.notfound", "Command {0} not found", commandName);
                    //set cause to CommandNotFoundException so that asadmin
                    //displays the closest matching commands
                report.setFailureCause(new CommandNotFoundException(msg));
            }
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            LogHelper.getDefaultLogger().info(msg);
        }
        return command;
    }

         /**
          * get the usage-text of the command.
          * check if <command-name>.usagetext is defined in LocalString.properties
          * if defined, then use the usagetext from LocalString.properties else
          * generate the usagetext from Param annotations in the command class.
          *
          * @param command class
          * @param model command model
          *
          * @return usagetext
          */
    String getUsageText(AdminCommand command, CommandModel model) {
        StringBuffer usageText = new StringBuffer();
        I18n i18n = model.getI18n();
        String i18nKey = null;
        
        final LocalStringManagerImpl lsm  = new LocalStringManagerImpl(command.getClass());
        if (i18n!=null) {
            i18nKey = i18n.value();
        }
	String usage;
        if (i18nKey != null &&
		ok(usage = lsm.getLocalString(i18nKey+".usagetext", ""))) {
	    usageText.append(adminStrings.getLocalString("adapter.usage", "Usage: "));
            usageText.append(usage);
	    return usageText.toString();
        }
        else {
            return generateUsageText(model);
        }
    }

         /**
          * generate the usage-text from the annotated Param in the command class
          *
          * @param model command model
          *
          * @return generated usagetext
          */
    private String generateUsageText(CommandModel model) {
        StringBuffer usageText = new StringBuffer();
	usageText.append(adminStrings.getLocalString("adapter.usage", "Usage: "));
        usageText.append(model.getCommandName());
        usageText.append(" ");
        StringBuffer operand = new StringBuffer();
        for (CommandModel.ParamModel pModel : model.getParameters()) {
            final Param param = pModel.getParam();
            final String paramName = pModel.getName();
                //do not want to display password as an option
            if (param.password())
                continue;
            final boolean optional = param.optional();
            final Class<?> ftype = pModel.getType();
            Object fvalue = null;
            String fvalueString = null;
            try {
                fvalue = param.defaultValue();
                if(fvalue != null)
                    fvalueString = fvalue.toString();
            }
            catch(Exception e) {
                // just leave it as null...
            }
            // this is a param.
            if (param.primary()) {
                if (optional) {
                    operand.append("[").append(paramName).append("] ");
                }
                else {
                    operand.append(paramName).append(" ");
                }
                continue;
            }
            if (optional) { usageText.append("["); }
            usageText.append("--").append(paramName);

            if (ok(param.defaultValue())) {
                usageText.append("=").append(param.defaultValue());
                if(optional) { usageText.append("] "); }
                else { usageText.append(" "); }
            }
            else if (ftype.isAssignableFrom(String.class)) {
                    //check if there is a default value assigned
                if (ok(fvalueString)) {
                    usageText.append("=").append(fvalueString);
                    if (optional) { usageText.append("] "); }
                    else { usageText.append(" "); }
                } else {
                    usageText.append("=").append(paramName);
                    if (optional) { usageText.append("] "); }
                    else { usageText.append(" "); }
                }
            }
            else if (ftype.isAssignableFrom(Boolean.class)) {
                // note: There is no defaultValue for this param.  It might
                // hava  value -- but we don't care -- it isn't an official
                // default value.
                    usageText.append("=").append("true|false");
                    if (optional) { usageText.append("] "); }
                    else { usageText.append(" "); }
            }
            else {
                usageText.append("=").append(paramName);
                if (optional) { usageText.append("] "); }
                else { usageText.append(" "); }
            }
        }//for
        usageText.append(operand);
        return usageText.toString();
    }

    public void getHelp(AdminCommand command, ActionReport report) {

        CommandModel model = getModel(command);        
        report.setActionDescription(model.getCommandName() + " help");
        LocalStringManagerImpl localStrings = new LocalStringManagerImpl(command.getClass());
        // Let's get the command i18n key
        I18n i18n = command.getClass().getAnnotation(I18n.class);
        String i18nKey = "";

        if (i18n!=null) {
            i18nKey = i18n.value();
        }
	// XXX - this is a hack for now.  if the request mapped to an
	// XMLContentActionReporter, that means we want the command metadata.
	if (report instanceof XMLContentActionReporter) {
	    getMetadata(command, model, report);
	} else {
	    report.setMessage(model.getCommandName() + " - " + localStrings.getLocalString(i18nKey, ""));
	    report.getTopMessagePart().addProperty("SYNOPSIS", getUsageText(command, model));
	    for (CommandModel.ParamModel param : model.getParameters()) {
		addParamUsage(report, localStrings, i18nKey, param);
	    }
	    report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
	}
    }

    /**
     * Return the metadata for the command.  We translate the parameter
     * and operand information to parts and properties of the ActionReport,
     * which will be translated to XML elements and attributes by the
     * XMLContentActionReporter.
     *
     * @param command the command
     * @param model the CommandModel describing the command
     * @param report	the (assumed to be) XMLContentActionReporter
     */
    private void getMetadata(AdminCommand command, CommandModel model,
	    ActionReport report) {
        LocalStringManagerImpl localStrings =
		new LocalStringManagerImpl(command.getClass());

        // Let's get the command i18n key
        I18n i18n = model.getI18n();
        String i18n_key = "";
        if (i18n!=null) {
            i18n_key = i18n.value();
        }

	ActionReport.MessagePart top = report.getTopMessagePart();
	ActionReport.MessagePart cmd = top.addChild();
	// <command name="name">
	cmd.setChildrenType("command");
	cmd.addProperty("name", model.getCommandName());
	if (model.unknownOptionsAreOperands())
	    cmd.addProperty("unknown-options-are-operands", "true");
	String usage = localStrings.getLocalString(i18n_key + ".usagetext", "");
	if (ok(usage))
	    cmd.addProperty("usage", usage);
	CommandModel.ParamModel primary = null;
	// for each parameter add
	// <option name="name" type="type" short="s" default="default"
	//   acceptable-values="list"/>
	for (CommandModel.ParamModel p : model.getParameters()) {
	    Param param = p.getParam();
	    if (param.primary()) {
		primary = p;
		continue;
	    }
	    ActionReport.MessagePart ppart = cmd.addChild();
	    ppart.setChildrenType("option");
	    ppart.addProperty("name", p.getName());
	    ppart.addProperty("type", typeOf(p));
	    ppart.addProperty("optional", Boolean.toString(param.optional()));
	    String paramDesc = getParamDescription(localStrings, i18n_key, p);
	    if (ok(paramDesc))
		ppart.addProperty("description", paramDesc);
	    if (ok(param.shortName()))
		ppart.addProperty("short", param.shortName());
	    if (ok(param.defaultValue()))
		ppart.addProperty("default", param.defaultValue());
	    if (ok(param.acceptableValues()))
		ppart.addProperty("acceptable-values", param.acceptableValues());
	}

	// are operands allowed?
	if (primary != null) {
	    // for the operand(s), add
	    // <operand type="type" min="0/1" max="1"/>
	    ActionReport.MessagePart primpart = cmd.addChild();
	    primpart.setChildrenType("operand");
	    primpart.addProperty("name", primary.getName());
	    primpart.addProperty("type", typeOf(primary));
	    primpart.addProperty("min",
		    primary.getParam().optional() ? "0" : "1");
	    primpart.addProperty("max", "1");   // XXX - based on array type?
	    String desc = getParamDescription(localStrings, i18n_key, primary);
	    if (ok(desc))
		primpart.addProperty("description", desc);
	}
    }

    /**
     * Map a Java type to one of the types supported by the asadmin client.
     * Currently supported types are BOOLEAN, FILE, PROPERTIES, PASSWORD, and
     * STRING.  (All of which should be defined constants on some class.)
     *
     * @param t the Java type
     * @return	the string representation of the asadmin type
     */
    private static String typeOf(CommandModel.ParamModel p) {
	Class t = p.getType();
	if (t == Boolean.class)
	    return "BOOLEAN";
	else if (t == File.class)
	    return "FILE";
	else if (t == Properties.class)	// XXX - allow subclass?
	    return "PROPERTIES";
	else if (p.getParam().password())
	    return "PASSWORD";
	else
	    return "STRING";
    }

    public InputStream getManPage(String commandName, AdminCommand command) {
        // bnevins -- too bad there is no AdminCommand baseclass.  We could make it
        // do the work but, alas, there is no such thing.
        Class clazz = command.getClass();
        Package pkg = clazz.getPackage();
        String manPage = pkg.getName().replace('.', '/');
        manPage += "/" + commandName + ".1";
        ClassLoader loader = clazz.getClassLoader();
        InputStream in = loader.getResourceAsStream(manPage);
        return in;
    }

    private void addParamUsage(ActionReport report, LocalStringManagerImpl localStrings, String i18nKey, CommandModel.ParamModel model) {
        Param param = model.getParam();
        if (param!=null) {
             // this is a param.
            String paramName = model.getName();
            //do not want to display password in the usage
            if (param.primary())
                return;
            if (param.primary()) {
                //if primary then it's an operand
                report.getTopMessagePart().addProperty(paramName+"_operand", getParamDescription(localStrings, i18nKey, model));
            } else {
                report.getTopMessagePart().addProperty(paramName, getParamDescription(localStrings, i18nKey, model));
            }
        }
    }

    
    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }


    /**
     * validate the paramters with the Param annotation.  If parameter is not defined
     * as a Param annotation then it's an invalid option.  If parameter's key is "DEFAULT"
     * then it's a operand.
     *
     * @param model - command model
     * @param parameters - parameters from URL
     *
     * @throws ComponentException if option is invalid
     */
    void validateParameters(final CommandModel model, final Properties parameters)
        throws ComponentException {
        
        final java.util.Enumeration e = parameters.propertyNames();

        //loop through parameters and make sure they are part of the Param declared field
        for (Object key : parameters.keySet()) {

            //to do, we should validate meta-options differently. 
            if ("DEFAULT".equals(key) || ((String) key).startsWith(ASADMIN_CMD_PREFIX)) {
                continue;
            }

            //check if key is a valid Param Field
            boolean validOption = false;
            //loop through the Param field in the command class
            //if either field name or the param name is equal to
            //key then it's a valid option
            for (CommandModel.ParamModel pModel : model.getParameters()) {
                validOption = pModel.isParamId(key.toString());
                if (validOption)
                    break;
            }
            if (!validOption) {
                throw new ComponentException(" Invalid option: " + key);
            }
        }
    }
    
         /**
         * convert a String to a Boolean
         * null --> true
         * "" --> true
         * case insensitive "true" --> true
         * case insensitive "false" --> false
         * anything else --> throw Exception
         * @param paramName - the name of the param
         * @param s - the String to convert
         * @return Boolean
         */
    Boolean convertStringToBoolean(String paramName, String s) {
        if(!ok(s))
            return true;
        
        if(s.equalsIgnoreCase(Boolean.TRUE.toString()))
            return true;

        if(s.equalsIgnoreCase(Boolean.FALSE.toString()))
            return false;
        
        String msg = adminStrings.getLocalString(
                "adapter.command.unacceptableBooleanValue",
                "Invalid parameter: {0}.  This boolean option must be set " +
                    "(case insensitive) to true or false.  Its value was set to {1}",
                paramName, s);
                
        throw new UnacceptableValueException(msg);
    }

        /**
         * convert a String with the following format to Properties:
         * name1=value1:name2=value2:name3=value3:...
         * The Properties object contains elements:
         * {name1=value1, name2=value2, name3=value3, ...}
         *
         * @param propsString - the String to convert
         * @param sep the separator character
         * @return Properties containing the elements in String
         */
    Properties convertStringToProperties(String propsString, char sep) {
        final Properties properties = new Properties();
        if (propsString != null) {
            ParamTokenizer stoken = new ParamTokenizer(propsString, sep);
            while (stoken.hasMoreTokens()) {
                String token = stoken.nextTokenKeepEscapes();
                final ParamTokenizer nameTok = new ParamTokenizer(token, '=');
                String name = null, value = null;
                if (nameTok.hasMoreTokens())
                    name = nameTok.nextToken();
                if (nameTok.hasMoreTokens())
                    value = nameTok.nextToken();
                if (nameTok.hasMoreTokens() || name == null || value == null)
                    throw new IllegalArgumentException(
                        adminStrings.getLocalString("InvalidPropertySyntax",
                            "Invalid property syntax.", propsString));
                properties.setProperty(name, value);
            }
        }
        return properties;
    }

        /**
         * convert a String with the following format to List<String>:
         * string1:string2:string3:...
         * The List object contains elements: string1, string2, string3, ...
         *
         * @param listString - the String to convert
         * @param sep the separator character
         * @return List containing the elements in String
         */
    List<String> convertStringToList(String listString, char sep) {
        List<String> list = new java.util.ArrayList();
        if (listString != null) {
            final ParamTokenizer ptoken = new ParamTokenizer(listString, sep);
            while (ptoken.hasMoreTokens()) {
                String token = ptoken.nextToken();
                list.add(token);
            }
        }
        return list;
    }

        /**
         * convert a String with the following format to String Array:
         * string1,string2,string3,...
         * The String Array contains: string1, string2, string3, ...
         *
         * @param arrayString - the String to convert
         * @param sep the separator character
         * @return String[] containing the elements in String
         */
    String[] convertStringToStringArray(String arrayString, char sep) {
        final ParamTokenizer paramTok = new ParamTokenizer(arrayString, sep);
        List<String> strs = new ArrayList<String>();
        while (paramTok.hasMoreTokens())
            strs.add(paramTok.nextToken());
        return strs.toArray(new String[strs.size()]);
    }

        /**
         * check if the variable, "skipParamValidation" is defined in the command
         * class.  If defined and set to true, then parameter validation will be
         * skipped from that command.
         * This is used mostly for command referencing.  For example list-applications
         * command references list-components command and you don't want to define
         * the same params from the class that implements list-components. 
         *
         * @param command - AdminCommand class
         * @return true if to skip param validation, else return false.
         */
    boolean skipValidation(AdminCommand command) {
            try {
                final Field f = command.getClass().getDeclaredField("skipParamValidation");
                f.setAccessible(true);
                if (f.getType().isAssignableFrom(boolean.class)) {
                    return f.getBoolean(command);
                }
            } catch (NoSuchFieldException e) {
                return false;
            } catch (IllegalAccessException e) {
                return false;
            }
            //all else return false
            return false;
        }

    // bnevins Apr 8, 2008
    private String encodeManPage(InputStream in) {
        final String eolToken = ManifestUtils.EOL_TOKEN;
        
        try {
            if(in == null)
                return null;

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuilder sb = new StringBuilder();
            
            while((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(eolToken);
            }
            return sb.toString();
        }
        catch (Exception ex) {
            return null;
        }
    }

    public CommandModel getModel(String commandName, Logger logger) {
        AdminCommand command = null;
        try {
            command = habitat.getComponent(AdminCommand.class, commandName);
        } catch(ComponentException e) {
            logger.log(Level.SEVERE, "Cannot instantiate " + commandName, e);
            return null;
        }
        return getModel(command);
    }

    private CommandModel getModel(AdminCommand command) {

        if (command instanceof CommandModelProvider) {
            return  ((CommandModelProvider) command).getModel();
        } else {
            return new CommandModelImpl(command.getClass());
        }
    }


    public void doCommand(CommandBuilder b, ActionReport report, Logger logger) {
        final AdminCommand command = getCommand(b.commandName, report, logger);
        if (command==null) {
            return;
        }
        CommandModel model = getModel(command);
        InjectionResolver<Param> resolver;
        if (b.delegate==null) {
            final Properties parameters = b.paramsAsProperties;
            if (parameters.get("help")!=null || parameters.get("Xhelp")!=null) {
                InputStream in = getManPage(model.getCommandName(), command);
                String manPage = encodeManPage(in);

                if(manPage != null && parameters.get("help")!=null) {
                    report.getTopMessagePart().addProperty("MANPAGE", manPage);
                }
                else {
                    report.getTopMessagePart().addProperty(AdminCommandResponse.GENERATED_HELP, "true");
                    getHelp(command, report);
                }
                return;
            }

            try {
                if (!skipValidation(command)) {
                    validateParameters(model, parameters);
                }
            } catch (ComponentException e) {
                // if the cause is UnacceptableValueException -- we want the message
                // from it.  It is wrapped with a less useful Exception

                Exception exception = e;
                Throwable cause = e.getCause();
                if(cause != null && (cause instanceof UnacceptableValueException)) {
                    // throw away the wrapper.
                    exception = (Exception)cause;
                }
                logger.severe(exception.getMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(exception.getMessage());
                report.setFailureCause(exception);
                ActionReport.MessagePart childPart = report.getTopMessagePart().addChild();
                childPart.setMessage(getUsageText(command, model));
                return;
            }
            resolver = getPropsBasedResolver(model, b.paramsAsProperties);
        } else {
            resolver = getDelegatedResolver(model, b.delegate);
        }
        doCommand(model, command, resolver, report, b.inbound, b.outbound);

    }
}



