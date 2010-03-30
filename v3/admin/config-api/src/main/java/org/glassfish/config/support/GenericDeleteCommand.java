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
package org.glassfish.config.support;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandModelProvider;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.*;

import java.util.logging.Level;

/**
 * Implementation of the generic delete command
 *
 * @author Jerome Dochez
 */
@Scoped(PerLookup.class)
public class GenericDeleteCommand extends GenericCrudCommand implements AdminCommand, PostConstruct, CommandModelProvider {

    @Inject
    Habitat habitat;

    Class<? extends CrudResolver> resolverType;
    CommandModel model;
    String elementName;
    Delete delete;    
    
    @Override
    public CommandModel getModel() {
        return model;
    }
       
    @Override
    public void postConstruct() {

        super.postConstruct();
        delete = targetType.getAnnotation(Delete.class);
        resolverType = delete.resolver();
        try {
            elementName = elementName(document, delete.parentType(), targetType);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Cannot load child type", e);
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericCrudCommand.configbean_not_found",
                    "The Config Bean {0} cannot be loaded by the generic command implementation : {1}",
                    delete.parentType(), e.getMessage());
            logger.severe(msg);
            throw new ComponentException(msg, e);         
        }

        if (logger.isLoggable(level)) {
            logger.log(level, "Generic Command configured for deleting " + targetType.getName() + " instances stored in " +
               delete.parentType().getName() + " under " + elementName);
        }        

        try {
            model = new GenericCommandModel(null, document, commandName, delete.resolver());
            if (logger.isLoggable(level)) {
                for (String paramName : model.getParametersNames()) {
                    CommandModel.ParamModel param = model.getModelFor(paramName);
                    logger.log(level, "I take " + param.getName() + " parameters");
                }
            }
        } catch(Exception e) {
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericCreateCommand.command_model_exception",
                    "Exception while creating the command model for the generic command {0} : {1}",
                    commandName, e.getMessage());
            logger.severe(msg);
            throw new ComponentException(msg, e);
        }
    }

    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport result = context.getActionReport();
        // inject resolver with command parameters...
        final InjectionManager manager = new InjectionManager();

        CrudResolver resolver = habitat.getComponent(resolverType);

        manager.inject(resolver, getInjectionResolver());

        final ConfigBeanProxy target = resolver.resolve(context, targetType);
        if (target==null) {
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericDeleteCommand.target_object_not_found",
                    "The CrudResolver {0} could not find the configuration object of type {1} where instances of {2} should be removed",
                    resolver.getClass().toString(), delete.parentType(), targetType);
            result.failure(logger, msg);
            return;
        }
        final ConfigBean child = (ConfigBean) ConfigBean.unwrap(target);

        try {
            ConfigSupport.deleteChild((ConfigBean) child.parent(), child);
        } catch(TransactionFailure e) {
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericDeleteCommand.transaction_exception",
                    "Exception while deleting the configuration {0} :{1}",
                    child.typeName(), e.getMessage());
            result.failure(logger, msg);
        }

    }
}
