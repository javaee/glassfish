/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Set;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;

import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.provider.ParameterMetaData;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.Param;


/**
 * Resource utilities class. Used by resource templates,
 * <code>TemplateListOfResource</code> and <code>TemplateResource</code>
 *
 * @author Rajeshwar Patil
 */
public class ResourceUtil extends Util {

    /**
     * Adjust the input parameters. In case of POST and DELETE methods, user
     * can provide name, id or DEFAULT parameter for primary parameter(i.e the
     * object to create or delete). This method is used to rename primary
     * parameter name to DEFAULT irrespective of what user provides.
     */
    public void adjustParameters(HashMap<String, String> data) {
        if (data != null) {
            if (!(data.containsKey("DEFAULT"))) {
                boolean isRenamed = renameParameter(data, "name", "DEFAULT");
                if (!isRenamed) {
                    renameParameter(data, "id", "DEFAULT");
                }
            }
        }
    }

    /**
     * Returns the name of the command associated with
     * this resource,if any, for the given operation.
     * @param type the given resource operation
     * @return String the associated command name for the given operation.
     */
    public String getCommand(RestRedirect.OpType type, ConfigBean configBean) {

        Class<? extends ConfigBeanProxy> cbp = null;
        try {
            cbp = (Class<? extends ConfigBeanProxy>)
                configBean.model.classLoaderHolder.get().loadClass(
                    configBean.model.targetTypeName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        RestRedirects restRedirects = cbp.getAnnotation(RestRedirects.class);
        if (restRedirects != null) {
            RestRedirect[] values = restRedirects.value();
            for (RestRedirect r : values) {
                if (r.opType().equals(type)) {
                    return r.commandName();
                }
            }
        }
        return null;
    }


    /**
     * Executes the specified __asadmin command.
     * @param commandName the command to execute
     * @param parameters the command parameters
     * @param habitat the habitat
     * @param logger the logger to use
     * @return ActionReport object with command execute status details.
     */
    public ActionReport runCommand(String commandName,
            HashMap<String, String> parameters, Habitat habitat) {
        CommandRunner cr = habitat.getComponent(CommandRunner.class);
        ActionReport ar = habitat.getComponent(ActionReport.class);
        Properties p = new Properties();
        p.putAll(parameters);

        cr.doCommand(commandName, p, ar);
        return ar;
    }


    /**
    * Executes the specified __asadmin command.
    * @param commandName the command to execute
    * @param parameters the command parameters
    * @param habitat the habitat
    * @param logger the logger to use
    * @return ActionReport object with command execute status details.
    */
    public ActionReport runCommand(String commandName,
           Properties parameters, Habitat habitat) {
       CommandRunner cr = habitat.getComponent(CommandRunner.class);
       ActionReport ar = habitat.getComponent(ActionReport.class);

       cr.doCommand(commandName, parameters, ar);
       return ar;
    }


    /**
     * Constructs and returns the resource method meta-data.
     * @param command the command assocaited with the resource method
     * @param habitat the habitat
     * @param logger the logger to use
     * @return MethodMetaData the meta-data store for the resource method.
     */
    public MethodMetaData getMethodMetaData(String command, Habitat habitat,
            Logger logger) {
        MethodMetaData methodMetaData = new MethodMetaData();

        if (command != null) {
            Collection<CommandModel.ParamModel> params =
                getParamMetaData(command, habitat, logger);
            Iterator<CommandModel.ParamModel> iterator = params.iterator();
            CommandModel.ParamModel paramModel;
            while(iterator.hasNext()) {
                paramModel = iterator.next();
                Param param = paramModel.getParam();

                ParameterMetaData parameterMetaData =
                    getParameterMetaData(paramModel);

                String parameterName =
                    (paramModel.getParam().primary())?"id":paramModel.getName();

                methodMetaData.putParameterMetaData(parameterName,
                    parameterMetaData);
            }
        }

        return methodMetaData;
    }


    /**
     * Constructs and returns the parameter meta-data.
     * @param command the command assocaited with the resource method
     * @param habitat the habitat
     * @param logger the logger to use
     * @return Collection the meta-data for the parameter of the resource method.
     */
    public Collection<CommandModel.ParamModel> getParamMetaData(
            String commandName, Habitat habitat, Logger logger) {
        CommandRunner cr = habitat.getComponent(CommandRunner.class);
        CommandModel cm = cr.getModel(commandName, logger);
        Collection<CommandModel.ParamModel> params = cm.getParameters();
        //print(params);
        return params;
    }


    //removes entries with empty value from the given Map
    void purgeEmptyEntries(HashMap<String, String> data) {
        Set<String> keys = data.keySet();
        Iterator<String> iterator = keys.iterator();
        String key;
        while (iterator.hasNext()) {
            key = iterator.next();
            if (data.get(key).length() < 1) {
                data.remove(key);
                iterator = keys.iterator();
            }
        }
     }


    //Construct parameter meta-data from the model
    private ParameterMetaData getParameterMetaData(CommandModel.ParamModel paramModel) {
        Param param = paramModel.getParam();
        ParameterMetaData parameterMetaData = new ParameterMetaData();

        parameterMetaData.putAttribute("Type", paramModel.getType().toString());
        parameterMetaData.putAttribute("Optional", Boolean.toString(param.optional()));
        parameterMetaData.putAttribute("Default Value", param.defaultValue());
        parameterMetaData.putAttribute("Acceptable Values", param.acceptableValues());
        //parameterMetaData.putAttribute("name1", paramModel.getName());
        //parameterMetaData.putAttribute("Name", param.name());
        //parameterMetaData.putAttribute("I18n", paramModel.getI18n().value());

        return parameterMetaData;
    }


    //rename the given input parameter
    private boolean renameParameter(HashMap<String, String> data,
        String parameterToRename, String newName) {
        if ((data.containsKey(parameterToRename))) {
            String value = data.get(parameterToRename);
            data.remove(parameterToRename);
            data.put(newName, value);
            return true;
        }
        return false;
    }


    //print given parameter meta-data.
    private void print(Collection<CommandModel.ParamModel> params) {
        for (CommandModel.ParamModel pm : params) {
            System.out.println("Command Param: " + pm.getName());
            System.out.println("Command Param Type: " + pm.getType());
            System.out.println("Command Param Name: " + pm.getParam().name());
            System.out.println("Command Param Shortname: " + pm.getParam().shortName());
        }
    }
}