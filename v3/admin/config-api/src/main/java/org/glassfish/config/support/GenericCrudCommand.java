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

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.hk2.component.InhabitantsFile;
import com.sun.hk2.component.InjectionResolver;
import com.sun.logging.LogDomains;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandModelProvider;
import org.glassfish.common.util.admin.ParamTokenizer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InjectionManager;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.*;
import org.jvnet.tiger_types.Types;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * services pertinent to generic CRUD command implementations
 *
 * @author Jerome Dochez
 *
 */
public abstract class GenericCrudCommand implements CommandModelProvider, PostConstruct {
    
    private InjectionResolver<Param> injector;

    @Inject
    DomDocument document;

    @Inject
    Inhabitant<?> myself;    

    final protected static Logger logger = LogDomains.getLogger(GenericCrudCommand.class, LogDomains.ADMIN_LOGGER);
    final protected static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(GenericCrudCommand.class);

    String commandName;
    Class<ConfigBeanProxy> targetType=null;
    protected final Level level = Level.INFO;

    public void postConstruct() {
        List<String> indexes = myself.metadata().get(InhabitantsFile.INDEX_KEY);
        if (indexes.size()!=1) {
            StringBuffer sb = new StringBuffer();
            for (String index : indexes) {
                sb.append(index).append(" ");
            }
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericCrudCommand.too_many_indexes",
                    "The metadata for this generic implementation has more than one index {0}",
                    sb.toString());
            logger.severe(msg);
            throw new ComponentException(msg);
        }
        String index = indexes.get(0);
        if (index.indexOf(":")==-1) {
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericCrudCommand.unamed_service",
                    "The service {0} is un-named, for generic command, the service name is the command name and must be provided",
                    index);
            logger.severe(msg);
            throw new ComponentException(msg);            
        }
        commandName = index.substring(index.indexOf(":")+1);
        String targetTypeName = myself.metadata().get(InhabitantsFile.TARGET_TYPE).get(0);
        if (logger.isLoggable(level)) {
            logger.log(level,"Generic method targeted type is " + targetTypeName);
        }

        try {
            targetType = loadClass(targetTypeName);
        } catch(ClassNotFoundException e) {
            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                    "GenericCrudCommand.configbean_not_found",
                    "The Config Bean {0} cannot be loaded by the generic command implementation : {1}",
                    targetTypeName, e.getMessage());
            logger.severe(msg);
            throw new ComponentException(msg, e);
        }

    }

    /**
     * we need to have access to the injector instance that has all the parameters context 
     * @param injector the original command injector
     */
    // todo : would be lovely to replace this with some smart injection...
    public void setInjectionResolver(InjectionResolver<Param> injector) {
        this.injector = injector;
    }

    public InjectionResolver<Param> getInjectionResolver() {
        final InjectionResolver<Param> delegate = injector;
        return new InjectionResolver<Param>(Param.class) {
            @Override
            public Object getValue(Object component, AnnotatedElement annotated, Class type) throws ComponentException {
                if (type.isAssignableFrom(List.class)) {
                    final List<ConfigBeanProxy> values;
                    try {
                        if (annotated instanceof Method) {
                            values = (List<ConfigBeanProxy>) ((Method) annotated).invoke(component);
                        } else if (annotated instanceof Field) {
                            values = (List<ConfigBeanProxy>) ((Field) annotated).get(component);
                        } else {
                            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                                    "GenericCrudCommand.invalid_type",
                                    "Invalid annotated type {0} passed to InjectionResolver:getValue()",
                                    annotated.getClass().toString());
                            logger.severe(msg);
                            throw new ComponentException(msg);
                        }
                    } catch (IllegalAccessException e) {
                        String msg = localStrings.getLocalString(GenericCrudCommand.class,
                                "GenericCrudCommand.invocation_failure",
                                "Failure {0} while getting List<?> values from component",
                                e.getMessage());
                        logger.severe(msg);
                        throw new ComponentException(msg, e);
                    } catch (InvocationTargetException e) {
                        String msg = localStrings.getLocalString(GenericCrudCommand.class,
                                "GenericCrudCommand.invocation_failure",
                                "Failure {0} while getting List<?> values from component",
                                e.getMessage());
                        logger.severe(msg);
                        throw new ComponentException(msg, e);
                    }
                    Object value = delegate.getValue(component, annotated, type);
                    if (value==null) {
                        if (logger.isLoggable(level)) {
                            logger.log(level, "Value of " + annotated.toString() + " is null");
                        }
                        return null;
                    }
                    final Class<? extends ConfigBeanProxy> itemType = Types.erasure(Types.getTypeArgument(
                            annotated instanceof Method?
                            ((Method) annotated).getGenericReturnType():((Field) annotated).getGenericType(), 0));
                    if (logger.isLoggable(level)) {
                        logger.log(level, "Found that List<?> really is a List<" + itemType.toString() + ">");
                    }
                    if (itemType==null) {
                            String msg = localStrings.getLocalString(GenericCrudCommand.class,
                                    "GenericCrudCommand.nongeneric_type",
                                    "The List type returned by {0} must be a generic type",
                                    annotated.toString());
                            logger.severe(msg);
                            throw new ComponentException(msg);
                    }
                    if (!ConfigBeanProxy.class.isAssignableFrom(itemType)) {
                        String msg = localStrings.getLocalString(GenericCrudCommand.class,
                                "GenericCrudCommand.wrong_type",
                                "The generic type {0} is not supported, only List<? extends ConfigBeanProxy> is",
                                annotated.toString());
                        logger.severe(msg);
                        throw new ComponentException(msg);
                        
                    }
                    Properties props = convertStringToProperties(value.toString(), ':');
                    if (logger.isLoggable(level)) {
                        for (Map.Entry<Object, Object> entry : props.entrySet()) {
                            logger.log(level, "Subtype " + itemType + " key:" + entry.getKey() + " value:" + entry.getValue());
                        }
                    }
                    final BeanInfo beanInfo;
                    try {
                        beanInfo = Introspector.getBeanInfo(itemType);
                    } catch (IntrospectionException e) {
                        String msg = localStrings.getLocalString(GenericCrudCommand.class,
                                "GenericCrudCommand.introspection_failure",
                                "Failure {0} while instrospecting {1} to find all getters and setters",
                                e.getMessage(), itemType.getName());
                        logger.severe(msg);
                        throw new ComponentException(msg, e);
                    }
                    for (final Map.Entry<Object, Object> entry : props.entrySet()) {
                        ConfigBeanProxy child = (ConfigBeanProxy) component;
                        try {
                            ConfigBeanProxy cc = child.createChild(itemType);
                            new InjectionManager().inject(cc, itemType, new InjectionResolver<Attribute>(Attribute.class) {

                                @Override
                                public boolean isOptional(AnnotatedElement annotated, Attribute annotation) {
                                    return true;    
                                }

                                @Override
                                public Method getSetterMethod(Method annotated, Attribute annotation) {
                                    // Attribute annotation are always annotated on the getter, we need to find the setter
                                    // variant.
                                    for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                                        if (pd.getReadMethod().equals(annotated)) {
                                            return pd.getWriteMethod();
                                        }
                                    }
                                    return annotated;
                                }



                                @Override
                                public Object getValue(Object component, AnnotatedElement annotated, Class type) throws ComponentException {
                                    String name = annotated.getAnnotation(Attribute.class).value();
                                    if (name==null || name.length()==0) {

                                        // maybe there is a better way to do this...
                                        name = ((Method) annotated).getName().substring(3);

                                        if (name.equalsIgnoreCase("name") || name.equalsIgnoreCase("key")) {
                                            return entry.getKey();
                                        }
                                        if (name.equalsIgnoreCase("value")) {
                                            return entry.getValue();
                                        }
                                    }
                                    return null;
                                };
                            });
                            values.add(cc);
                        } catch (TransactionFailure transactionFailure) {

                        }

                    }
                    return null;
                }
                return delegate.getValue(component, annotated, type);
            }
        };
        
    }

    protected Class loadClass(String type) throws ClassNotFoundException {
        // by default I use the inhabitant class loader
        return myself.type().getClassLoader().loadClass(type);
    }    

    private static final String ASADMIN_CMD_PREFIX = "AS_ADMIN_";
    


    /**
     * Convert a String with the following format to Properties:
     * name1=value1:name2=value2:name3=value3:...
     * The Properties object contains elements:
     * {name1=value1, name2=value2, name3=value3, ...}
     *
     * @param propsString the String to convert
     * @param sep the separator character
     * @return Properties containing the elements in String
     */
    static Properties convertStringToProperties(String propsString, char sep) {
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
                    throw new IllegalArgumentException("TODO : i18n : Invalid property syntax." + propsString);
                        //strings.getLocalString("InvalidPropertySyntax",
                        //    "Invalid property syntax.", propsString));
                properties.setProperty(name, value);
            }
        }
        return properties;
    }

    /**
     * Returns the element name used by the parent to store instances of the child
     *
     * @param document the dom document this configuration element lives in.
     * @param parent type of the parent
     * @param child type of the child
     * @return the element name holding child's instances in the parent
     * @throws ClassNotFoundException when subclasses cannot be loaded
     */
    public static String elementName(DomDocument document, Class<?> parent, Class<?> child)
        throws ClassNotFoundException {

        ConfigModel cm = document.buildModel(parent);
        for (String elementName : cm.getElementNames()) {
            ConfigModel.Property prop = cm.getElement(elementName);
            if (prop instanceof ConfigModel.Node) {
                ConfigModel childCM = ((ConfigModel.Node) prop).getModel();
                String childTypeName = childCM.targetTypeName;
                if (childTypeName.equals(child.getName())) {
                    return elementName;
                }
                // check the inheritance hierarchy
                List<ConfigModel> subChildrenModels = document.getAllModelsImplementing(
                        childCM.classLoaderHolder.get().loadClass(childTypeName));
                if (subChildrenModels!=null) {
                    for (ConfigModel subChildModel : subChildrenModels) {
                        if (subChildModel.targetTypeName.equals(child.getName())) {
                            return elementName;
                        }
                    }
                }

            }
        }
        return null;
    }
 
}
