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

package org.glassfish.config.support;

import org.glassfish.api.Param;
import org.glassfish.api.UnknownOptionsAreOperands;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.CommandModel;
import org.jvnet.hk2.annotations.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Model for an administrative command
 *
 * @author Jerome Dochez
 */
public class CommandModelImpl extends CommandModel {

    // use a LinkedHashMap so params appears in the order they are declared in the class.
    final Map<String, CommandModel.ParamModel> params = new LinkedHashMap<String, ParamModel>();
    final String commandName;
    final I18n i18n;
    private boolean dashOk = false;

    public CommandModelImpl(Class<? extends AdminCommand> commandType) {

        commandName = commandType.getAnnotation(Service.class).name();
        i18n = commandType.getAnnotation(I18n.class);
        init(commandType);
    }

    CommandModelImpl() {
        commandName = null;
        i18n=null;
    }

    void init(Class commandType) {

        Class currentClazz = commandType;
        while(currentClazz!=null) {

            for (Field f : currentClazz.getDeclaredFields()) {
                add(f);
            }

            for (Method m : currentClazz.getDeclaredMethods()) {
                add(m);
            }

	    if (currentClazz.isAnnotationPresent(UnknownOptionsAreOperands.class))
		dashOk = true;
            currentClazz = currentClazz.getSuperclass();
        }
    }

    public I18n getI18n() {
        return i18n;
    }

    public String getCommandName() {
        return commandName;
    }

    public CommandModel.ParamModel getModelFor(String paramName) {
        return params.get(paramName);
    }

    public Collection<String> getParametersNames() {
        return params.keySet();
    }

    /**
     * Should an unknown option be considered an operand by asadmin?
     */
    @Override
    public boolean unknownOptionsAreOperands() {
	return dashOk;
    }

    private void add(AnnotatedElement e) {
        if (e.isAnnotationPresent(Param.class)) {
            ParamModel model = new ParamModelImpl(e);
            if (!params.containsKey(model.getName())) {
                params.put(model.getName(), model);
            }
        }
    }

    class ParamModelImpl extends ParamModel {

        final String    name;
        final Param     param;
        final I18n      i18n;
        final Class     type;

        ParamModelImpl(AnnotatedElement e) {
            Param p = e.getAnnotation(Param.class);
            name = getParamName(p, e);
            param = p;
            i18n = e.getAnnotation(I18n.class);
            if (e instanceof Method) {
                type = ((Method) e).getReturnType();
            } else if (e instanceof Field) {
                type = ((Field) e).getType();
            } else {
                type = String.class;
            }
        }


        public String getName() {
            return name;
        }

        public Param getParam() {
            return param;
        }

        public I18n getI18n() {
            return i18n;
        }

        public Class getType() {
            return type;
        }
    }
}
