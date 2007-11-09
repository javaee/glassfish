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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.config.serverbeans;

import org.glassfish.api.admin.*;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Bunch of utility methods for the new serverbeans config api based on jaxb
 */
public final class ConfigBeansUtilities {

    // static methods only
    private ConfigBeansUtilities() {
    }

    public static <T> List<T> getModules(Class<T> type, Applications apps) {
        List<T> modules = new ArrayList<T>();
        for (Object module : apps.getLifecycleModuleOrJ2EeApplicationOrEjbModuleOrWebModuleOrConnectorModuleOrAppclientModuleOrMbeanOrExtensionModule()) {
            if (module.getClass().getName().equals(type.getClass().getName())) {
                modules.add((T) module);
            }
        }
        return modules;
    }

    public static <T> T getModule(Class<T> type, Applications apps, String moduleID) {

        if (moduleID == null) {
            return null;
        }

        for (Object module : apps.getLifecycleModuleOrJ2EeApplicationOrEjbModuleOrWebModuleOrConnectorModuleOrAppclientModuleOrMbeanOrExtensionModule()) {
            if (module.getClass().getName().equals(type.getClass().getName())) {
                Method m;
                try {
                    m = type.getMethod("getName");
                } catch (SecurityException ex) {
                    return null;
                } catch (NoSuchMethodException ex) {
                    return null;
                }
                if (m != null) {
                    try {
                        if (moduleID.equals(m.invoke(module))) {
                            return (T) module;
                        }
                    } catch (IllegalArgumentException ex) {
                        return null;
                    } catch (IllegalAccessException ex) {
                        return null;
                    } catch (InvocationTargetException ex) {
                        return null;
                    }
                }
            }
        }
        return null;

    }

    public static Property getPropertyByName(Object bean, String name) {
        Method m;
        try {
            m = bean.getClass().getMethod("getProperty");
        } catch (SecurityException ex) {
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
        if (m == null) {
            return null;
        }
        List<Property> properties;
        try {
            properties = (List<Property>) m.invoke(bean);
        } catch (IllegalArgumentException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        } catch (InvocationTargetException ex) {
            return null;
        }
        for (Property prop : properties) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }
        return null;
    }

    /**
     * Returns a property value if the bean has properties and one of its
     * properties name is equal to the one passed.
     *
     * @param bean the config-api bean
     * @param name the property name requested
     * @return the property value of null if not found
     */
    public static String getPropertyValueByName(Object bean, String name) {
        Property prop = getPropertyByName(bean, name);
        if (prop != null) {
            return prop.getValue();
        }
        return null;
    }

    /**
     * Get the default value of Format from dtd
     */
    public static String getDefaultFormat() {
        return "%client.name% %auth-user-name% %datetime% %request% %status% %response.length%".trim();
    }

    /**
     * Get the default value of RotationPolicy from dtd
     */
    public static String getDefaultRotationPolicy() {
        return "time".trim();
    }

    /**
     * Get the default value of RotationEnabled from dtd
     */
    public static String getDefaultRotationEnabled() {
        return "true".trim();
    }

    /**
     * Get the default value of RotationIntervalInMinutes from dtd
     */
    public static String getDefaultRotationIntervalInMinutes() {
        return "1440".trim();
    }

    /**
     * Get the default value of QueueSizeInBytes from dtd
     */
    public static String getDefaultQueueSizeInBytes() {
        return "4096".trim();
    }

    /**
     * This method is used to convert a string value to boolean.
     *
     * @return true if the value is one of true, on, yes, 1. Note
     *         that the values are case sensitive. If it is not one of these
     *         values, then, it returns false. A finest message is printed if
     *         the value is null or a info message if the values are
     *         wrong cases for valid true values.
     */
    public static boolean toBoolean(final String value) {
        final String v = (null != value ? value.trim() : value);
        return null != v && (v.equals("true")
                || v.equals("yes")
                || v.equals("on")
                || v.equals("1"));
    }

    /**
     * Execute some logic on one config bean of type T protected by a transaction
     *
     * @param code code to execute
     * @param param config object participating in the transaction
     * @return list of events that represents the modified config elements.
     */
    public static <T extends ConfigBean> List<PropertyChangeEvent> apply(final SingleConfigCode<T> code, T param)
        throws PropertyVetoException {
        
        ConfigBean[] objects = new ConfigBean[1];
        objects[0]=param;
        return apply((new ConfigCode() {
            public boolean run(ConfigBean... objects) throws PropertyVetoException {
                return code.run((T) objects[0]);
            }
        }), objects);
    }
    
    /**
     * Executes some logic on some config beans protected by a transaction.
     *
     * @param code code to execute
     * @param objects config beans participating to the transaction
     */
    public static List<PropertyChangeEvent> apply(ConfigCode code, ConfigBean... objects)
            throws PropertyVetoException {
        
        // the fools think they operate on the "real" object while I am
        // feeding them with deep copies. Only if the transaction succeed
        // will I apply the "changes" to the real ones.
        ConfigBean[] cheapCopies = new ConfigBean[objects.length];
        for (int i=0;i<objects.length;i++) {
            //cheapCopies[i] = SerialClone.clone(objects[i]);
            cheapCopies[i] = (ConfigBean) objects[i].clone();
        }

        // Of course I am not locking the live objects but the copies.
        // if the user try to massage the real objects, he will get
        // a well deserved nasty exception
        Transaction t = new Transaction();
        for (ConfigBean configBean : cheapCopies) {
            if (!configBean.join(t)) {
                t.rollback();
                return null;
            }
        }
        
        List<PropertyChangeEvent> transactionEvents = null;
        try {
            if (code.run(cheapCopies)) {
                try {
                    transactionEvents = t.commit();
                } catch (RetryableException e) {
                    System.out.println("Retryable...");
                    t.rollback();
                    return null;
                }
            } else {
                t.rollback();
                return null;
            }
        } catch (PropertyVetoException e) {
            t.rollback();
            throw e;
        }

        // now apply the changes to the "real objects if this was a success
        if (transactionEvents!=null && transactionEvents.size()>0) {
            // now I need to lock the live copies
            Transaction real = new Transaction();
            for (ConfigBean configBean : objects) {
                if (!configBean.join(real)) {
                    // should I wait and retry ?
                    real.rollback();
                    return null;
                }
            }

            // process to events on the "real" objects
            for (PropertyChangeEvent event : transactionEvents) {
                // the trick is to find the right object on which the event applies.
                int i=0;
                while (i<cheapCopies.length && cheapCopies[i]!=event.getSource())
                    i++;

                if (i<cheapCopies.length) {
                    ConfigBean source = objects[i];
                    try {
                        // TODO : cache those bean info, faster algorithm needed here.
                        BeanInfo info = Introspector.getBeanInfo(source.getClass());
                        for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
                            if (prop.getName().equals(event.getPropertyName())) {
                                try {
                                    try {
                                        // remember that new objects also need to be part of the transaction
                                        ConfigBean configBean = ConfigBean.class.cast(event.getNewValue());
                                        configBean.join(real);
                                    } catch(ClassCastException e) {
                                        // ignore
                                    }
                                    prop.getWriteMethod().invoke(source, event.getNewValue());
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                            }
                        }

                    } catch (IntrospectionException e) {
                        System.out.println("Cannot introspect " + source.getClass() + " : " + e);
                    }


                }                

            }

            try {
                return real.commit();
            } catch (RetryableException e) {
                // should I wait and retry

            }

        }
        return null;
    }

}


