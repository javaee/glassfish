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

import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.config.Named;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.InjectionManager;
import org.jvnet.hk2.config.*;

import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic list command implementation.
 *
 * @author Jerome Dochez
 */
public class GenericListCommand  extends GenericCrudCommand implements AdminCommand {

    @Inject
     Habitat habitat;

    Class<? extends CrudResolver> resolverType;
    CommandModel model;
    String elementName;
    Listing listing;


    final static Logger logger = LogDomains.getLogger(GenericCreateCommand.class, LogDomains.ADMIN_LOGGER);

    public void postConstruct() {

        super.postConstruct();

        listing = targetType.getAnnotation(Listing.class);
        resolverType = listing.resolver();
        try {
            elementName = elementName(document, listing.parentType(), targetType);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Cannot load child type", e);
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericCrudCommand.configbean_not_found",
                    "The Config Bean {0} cannot be loaded by the generic command implementation : {1}",
                    listing.parentType(), e.getMessage());
            logger.severe(msg);
            throw new ComponentException(msg, e);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Generic Command configured for creating " + targetType.getName() + " instances which gets added to " +
                listing.parentType().getName() + " under " + elementName);
        }

        try {
            // we only use the resolver parameter as the command parameters.
            model = new GenericCommandModel(null, habitat.getComponent(DomDocument.class), commandName, listing.resolver());
            if (logger.isLoggable(Level.FINE)) {
                for (String paramName : model.getParametersNames()) {
                    CommandModel.ParamModel param = model.getModelFor(paramName);
                    logger.fine("I take " + param.getName() + " parameters");
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

    public void execute(final AdminCommandContext context) {

        final ActionReport result = context.getActionReport();

        // inject resolver with command parameters...
        final InjectionManager manager = new InjectionManager();

        CrudResolver resolver = habitat.getComponent(resolverType);

        manager.inject(resolver, getInjectionResolver());

        final ConfigBeanProxy parentBean = resolver.resolve(context, listing.parentType());
        if (parentBean==null) {
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericCreateCommand.target_object_not_found",
                    "The CrudResolver {0} could not find the configuration object of type {1} where instances of {2} should be added",
                    resolver.getClass().toString(), listing.parentType(), targetType);
            result.failure(logger, msg);
            return;
        }

        Dom parentDom = Dom.unwrap(parentBean);
        for (Dom child : parentDom.nodeElements(elementName)) {
            String key = child.getKey();
            if (key==null) {
                String msg = localStrings.getLocalString(GenericCrudCommand.class,
                        "GenericListCommand.element_has_no_key",
                        "The element {0} has not key attribute",
                        targetType);
                result.failure(logger, msg);
                return;

            }
            context.getActionReport().addSubActionsReport().setMessage(key);
        }
    }

    @Override
     public CommandModel getModel() {
        return model;
    }
}
