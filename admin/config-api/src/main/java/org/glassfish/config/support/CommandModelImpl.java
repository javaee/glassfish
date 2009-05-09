package org.glassfish.config.support;

import org.glassfish.api.Param;
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
