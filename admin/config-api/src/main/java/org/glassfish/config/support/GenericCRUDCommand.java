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
package org.glassfish.config.support;

import org.glassfish.api.admin.*;
import org.glassfish.api.Param;
import org.glassfish.api.I18n;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.*;
import com.sun.hk2.component.InhabitantsFile;
import com.sun.hk2.component.InjectionResolver;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.beans.PropertyVetoException;

/**
 * Not much so far, just to get the APIs figured out.
 */
@Scoped(PerLookup.class)
public class GenericCRUDCommand implements AdminCommand, PostConstruct, CommandModelProvider {

    @Inject
    Habitat habitat;
    
    @Inject
    Inhabitant<GenericCRUDCommand> myself;

    boolean valid=false;

    String commandName;
    Class targetType=null;
    Class<? extends ConfigResolver> resolverType;
    CommandModel model;


    final static Logger logger = LogDomains.getLogger(GenericCRUDCommand.class, LogDomains.ADMIN_LOGGER);

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

        Create create = (Create) targetType.getAnnotation(Create.class);
        resolverType = create.resolver();


        try {
            System.out.println("I create " + targetType.getName() + " instances which gets added to " +
                create.parentType().getName() + " under " + this.elementName(create.parentType(), targetType));
        } catch(ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Cannot load child type", e);
        }
        try {
            model = new GenericCommandModel(targetType);
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

    /**
     * Returns the element name used by the parent to store instances of the child
     *
     * @param parent type of the parent
     * @param child type of the child
     * @return the element name holding child's instances in the parent
     */
    protected String elementName(Class<ConfigBeanProxy> parent, Class<ConfigBeanProxy> child)
        throws ClassNotFoundException {

        DomDocument document = habitat.getComponent(DomDocument.class);
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
                for (ConfigModel subChildModel : subChildrenModels) {
                    if (subChildModel.targetTypeName.equals(child.getName())) {
                        return elementName;
                    }
                }

            }
        }
        return null;
    }

    public void execute(AdminCommandContext context) {

        // inject resolver with command parameters...
        // fake it for now...
        InjectionManager manager = new InjectionManager();
        
        ConfigResolver resolver = habitat.getComponent(resolverType);

        manager.inject(resolver, new InjectionResolver<Param>(Param.class) {

            public Object getValue(Object component, AnnotatedElement annotated, Class type) throws ComponentException {
                return null;
            }

            @Override
            public boolean isOptional(AnnotatedElement annotatedElement, Param param) {
                return param.optional();
            }
        });

        final ConfigBeanProxy target = resolver.resolve(context);

        try {
            ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy> () {
                public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {
                    ConfigBeanProxy child = target.createChild(targetType);
                    // now injects the parameters...

                    return child;
                }
            }, target);
        } catch(TransactionFailure e) {
            e.printStackTrace();
        }
    }

    public CommandModel getModel() {
        return model;
    }

    private final class GenericCommandModel extends CommandModel {

        final HashMap<String, ParamModel> params = new HashMap<String, ParamModel>();
        
        private GenericCommandModel(Class<ConfigBeanProxy> targetType) {
            DomDocument document = habitat.getComponent(DomDocument.class);
            ConfigModel cm = document.buildModel(targetType);
            for (Method m : targetType.getMethods()) {
                ConfigModel.Property prop = cm.toProperty(m);
                if (prop==null) continue;
                String attributeName = prop.xmlName; 
                if (m.isAnnotationPresent(Param.class)) {
                    Param p = m.getAnnotation(Param.class);
                    if (p.name()!=null && !p.name().isEmpty()) {
                        params.put(p.name(), new ParamBasedModel(p.name(), p));
                    } else {
                        if (m.isAnnotationPresent(Attribute.class)) {
                            Attribute attr = m.getAnnotation(Attribute.class);
                            if (attr.value()!=null && !attr.value().isEmpty()) {
                                params.put(attr.value(), new AttributeBasedModel(attr.value(), attr));
                            } else {
                                params.put(attributeName, new AttributeBasedModel(attributeName, attr));
                            }
                        }
                    }
                }
            }
            // now the resolver parameters.
            Create create = targetType.getAnnotation(Create.class);
            if (create==null) return;

            Class<? extends ConfigResolver> resolverType = create.resolver();
            CommandModelImpl classModel = new CommandModelImpl();
            classModel.init(resolverType);

            for (String paramName : classModel.getParametersNames()) {
                params.put(paramName, classModel.getModelFor(paramName));
            }
        }

        public I18n getI18n() {
            return null;
        }

        public String getCommandName() {
            return commandName;
        }

        public ParamModel getModelFor(String paramName) {
            return params.get(paramName);
        }

        public Collection<String> getParametersNames() {
            return params.keySet();
        }

        private final class ParamBasedModel extends ParamModel {
            final String name;
            final Param param;

            private ParamBasedModel(String name, Param param) {
                this.name = name;
                this.param = param;
            }

            public String getName() {
                return name;
            }

            public Param getParam() {
                return param;
            }

            public I18n getI18n() {
                return null;
            }

            public Class getType() {
                return String.class;
            }
        }

        private final class AttributeBasedModel extends ParamModel {
            final String name;
            final Attribute attr;

            private AttributeBasedModel(String name, Attribute attr) {
                this.name = name;
                this.attr=attr;
            }

            public String getName() {
                return name;
            }

            public I18n getI18n() {
                return null;
            }

            public Class getType() {
                return String.class;
            }

            public Param getParam() {
                return new Param() {

                    public Class<? extends Annotation> annotationType() {
                        return Param.class;
                    }

                    public String name() {
                        return name;
                    }

                    public String acceptableValues() {
                        return null;
                    }

                    public boolean optional() {
                        return !attr.key();

                    }

                    public String shortName() {
                        return null;
                    }

                    public boolean primary() {
                        return attr.key();
                    }

                    public String defaultValue() {
                        return attr.defaultValue();
                    }

                    public boolean password() {
                        return false;
                    }

                    public char separator() {
                        return ',';
                    }

                    public boolean multiple() {
                        return false;
                    }
                };
            }
        }
    }
}
