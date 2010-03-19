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

import com.sun.hk2.component.InhabitantsFile;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandModelProvider;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.*;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the generic delete command
 *
 * @author Jerome Dochez
 */
@Scoped(PerLookup.class)
public class GenericDeleteCommand extends GenericCrudCommand implements AdminCommand, PostConstruct, CommandModelProvider {

    @Inject
    Logger logger;

    @Inject
    Inhabitant<GenericCreateCommand> myself;

    @Inject
    Habitat habitat;

    String commandName;

    Class<ConfigBeanProxy> targetType=null;
    Class<? extends ConfigResolver> resolverType;
    CommandModel model;
    String elementName;
    Delete delete;    

    @Override
    public CommandModel getModel() {
        return model;
    }

    @Override
    public void postConstruct() {
        // first we need to retrieve our inhabitant.
        System.out.println("Lead " + myself);

        // let's find command name, parent type and such...
        List<String> indexes = myself.metadata().get(InhabitantsFile.INDEX_KEY);
        if (indexes.size()!=1) {
            logger.log(Level.SEVERE, "Inhabitant has more than 1 index " + indexes.get(0));
            return;
        }
        String index = indexes.get(0);
        if (index.indexOf(":")==-1) {
            logger.log(Level.SEVERE, "This is not a named service " + index);
            return;
        }
        commandName = index.substring(index.indexOf(":")+1);
        String targetTypeName = myself.metadata().get(InhabitantsFile.TARGET_TYPE).get(0);

        try {
            targetType = loadClass(targetTypeName);
        } catch(ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Cannot load target type", e);
        }

        delete = targetType.getAnnotation(Delete.class);
        resolverType = delete.resolver();
        try {
            elementName = elementName(delete.parentType(), targetType);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Cannot load child type", e);
        }

        System.out.println("I delete " + targetType.getName() + " instances stored in " +
            delete.parentType().getName() + " under " + elementName);

        try {
            model = new GenericCommandModel(null, delete.resolver(), document, commandName);
            for (String paramName : model.getParametersNames()) {
                CommandModel.ParamModel param = model.getModelFor(paramName);
                System.out.println("I take " + param.getName() + " parameters");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    protected Class loadClass(String type) throws ClassNotFoundException {
        // by default I use the inhabitant class loader
        return myself.type().getClassLoader().loadClass(type);
    }

    @Override
    public void execute(AdminCommandContext context) {
        // inject resolver with command parameters...
        final InjectionManager manager = new InjectionManager();

        ConfigResolver resolver = habitat.getComponent(resolverType);

        manager.inject(resolver, getInjectionResolver());

        final ConfigBeanProxy target = resolver.resolve(context, elementName,  targetType);
        if (target==null) {
            context.logger.severe("Cannot find the target configuration");
            return;
        }
        final ConfigBean child = (ConfigBean) ConfigBean.unwrap(target);

        try {
            ConfigSupport.deleteChild((ConfigBean) child.parent(), child);
        } catch(TransactionFailure e) {
            e.printStackTrace();
        }

    }
}
