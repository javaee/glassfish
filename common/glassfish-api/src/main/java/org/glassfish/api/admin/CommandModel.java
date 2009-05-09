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

    private static final String ASADMIN_CMD_PREFIX = "AS_ADMIN_";
    
}
