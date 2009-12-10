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

package org.glassfish.api.admin;

import org.glassfish.api.Param;
import org.glassfish.api.I18n;

import java.util.Collection;
import java.util.ArrayList;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sun.hk2.component.InjectionResolver;

/**
 * Model for an administrative command
 *
 * @author Jerome Dochez
 */
public abstract class CommandModel {

    public abstract String getCommandName();

    public abstract I18n getI18n();

    public abstract ParamModel getModelFor(String paramName);

    public abstract Collection<String> getParametersNames();

    public Collection<ParamModel> getParameters() {
        ArrayList<ParamModel> copy = new ArrayList<ParamModel>();
        for (String name : getParametersNames()) {
            copy.add(getModelFor(name));
        }
        return copy;
    }


        /**
         * get the Param name.  First it checks if the annotated Param
         * includes a the name, if not then get the name from the field.
         *
         * @param param class annotation
         * @param annotated annotated field or method
         * @return the name of the param
         */
    public String getParamName(Param param, AnnotatedElement annotated) {
        if (param.name().equals("")) {
            if (annotated instanceof Field) {
                return ((Field) annotated).getName();
            }
            if (annotated instanceof Method) {
                return ((Method) annotated).getName().substring(3).toLowerCase();
            }
        } else if (param.password()) {
            return ASADMIN_CMD_PREFIX + param.name().toUpperCase();
        } else {
            return param.name();
        }
        return "";
    }    

    public abstract class ParamModel {

        public abstract String getName();

        public abstract Param getParam();

        public abstract I18n getI18n();

        public abstract Class getType();

        public boolean isParamId(String key) {
            if (getParam().primary()) {
                return "DEFAULT".equals(key) || getName().equals(key);
            }
            if (getParam().password()) {
                return key.startsWith(ASADMIN_CMD_PREFIX);
            }
            return getName().equals(key) || getParam().shortName().equals(key);
        }

    }

    /**
     * Should an unknown option be considered an operand by asadmin?
     */
    public boolean unknownOptionsAreOperands() {
	return false;	// default implementation
    }

    private static final String ASADMIN_CMD_PREFIX = "AS_ADMIN_";
    
}
