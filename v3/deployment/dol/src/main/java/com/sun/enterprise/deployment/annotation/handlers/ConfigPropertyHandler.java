/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.annotation.context.RarBundleContext;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.util.LocalStringManagerImpl;

import javax.resource.spi.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.apf.impl.HandlerProcessingResultImpl;
import org.glassfish.apf.*;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jagadish Ramu
 */
@Service
public class ConfigPropertyHandler extends AbstractHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(AbstractHandler.class);

    public Class<? extends Annotation> getAnnotationType() {
        return ConfigProperty.class;
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        ConfigProperty configProperty = (ConfigProperty) element.getAnnotation();

        if (aeHandler instanceof RarBundleContext) {

            RarBundleContext rbc = (RarBundleContext) aeHandler;
            ConnectorDescriptor desc = rbc.getDescriptor();

            String defaultValue = configProperty.defaultValue();
            String description = configProperty.description();
            boolean ignore = configProperty.ignore();
            boolean supportsDynamicUpdates = configProperty.supportsDynamicUpdates();
            boolean confidential = configProperty.confidential();
            
            Class type = configProperty.type();

            if (element.getElementType().equals(ElementType.METHOD)) {

                Method m = (Method) element.getAnnotatedElement();

                Class[] parameters = m.getParameterTypes();
                Class propertyType;
                if (parameters != null) {
                    if (parameters.length == 1) {
                        propertyType = parameters[0];
                    } else {
                        return getFailureResult(element, "more than one parameter for JavaBean setter method : " +
                                "[" + m.getName() + " ] ", true);
                    }
                } else {
                    return getFailureResult(element, "no parameters for JavaBean setter method : " +
                            "[" + m.getName() + " ] ", true);
                }

                if (type.equals(Object.class)) {
                    type = propertyType;
                } else {
                    //check compatibility between annotation type and property-type
                    if (!propertyType.isAssignableFrom(type)) {
                        return getFailureResult(element, "annotation type [" + type + "] and property-type" +
                                " [" + propertyType + "] " +
                                "are not assignment compatible", true);
                    }
                }

                ConnectorConfigProperty ep = getConfigProperty(defaultValue, description, ignore,
                        supportsDynamicUpdates, confidential, type, m.getName().substring(3));


                Class c = m.getDeclaringClass();
                handleConfigPropertyAnnotation(element, desc, ep, c);

            } else if (element.getElementType().equals(ElementType.FIELD)) {
                Field f = (Field) element.getAnnotatedElement();
                Class c = f.getDeclaringClass();
                Class returnType = f.getType();
                if (type.equals(Object.class)) {
                    type = returnType;
                } else {
                    //check compatibility between annotation type and return-type
                    if (!returnType.isAssignableFrom(type)) {
                        return getFailureResult(element, "annotation type [" + type + "] " +
                                "and return-type [" + returnType + "] " +
                                "are not assignment compatible", true);
                    }
                }

                //TODO V3 need to get the defaultValue from the field if its not specified via annotation
                ConnectorConfigProperty ep = getConfigProperty(defaultValue,description, ignore,
                        supportsDynamicUpdates, confidential,  type, f.getName());

                handleConfigPropertyAnnotation(element, desc, ep, c);

            }

        } else {
            return getFailureResult(element, "not a rar bundle context", true);
        }
        return getDefaultProcessedResult();
    }

    private ConnectorConfigProperty getConfigProperty(String defaultValue, String description, boolean ignore,
                                                      boolean supportsDynamicUpdates, boolean confidential,
                                                      Class type, String propertyName) {
        ConnectorConfigProperty ep = new ConnectorConfigProperty();
        //use description if specified
        if (!description.equals("")) {
            ep.setDescription(description);
        }
        //use default value if specified
        if (!defaultValue.equals("")) {
            ep.setValue(defaultValue);
        }
        ep.setType(type.getName());

        ep.setName(propertyName);

        if (!ep.isSetIgnoreCalled()) {
            ep.setIgnore(ignore);
        }
        if (!ep.isSetConfidentialCalled()) {
            ep.setConfidential(confidential);
        }
        if (!ep.isSupportsDynamicUpdates()) {
            ep.setSupportsDynamicUpdates(supportsDynamicUpdates);
        }
        return ep;
    }

    private void handleConfigPropertyAnnotation(AnnotationInfo element,
                                                ConnectorDescriptor desc, ConnectorConfigProperty ep, Class c) {
        if (ResourceAdapter.class.isAssignableFrom(c)
                || c.getAnnotation(Connector.class) != null) {
            processConnector(desc, ep);
        } else if (ManagedConnectionFactory.class.isAssignableFrom(c)
                || c.getAnnotation(ConnectionDefinition.class) != null) {
            processConnectionDefinition(element, desc, ep, c);
        } else if (ActivationSpec.class.isAssignableFrom(c)
                || c.getAnnotation(Activation.class) != null) {
            processActivation(element, desc, ep, c);
        } else if (c.getAnnotation(AdministeredObject.class) != null) {
            //TODO V3 handle "AdministeredObject interface also
            
            handleConfigPropertyForAdministeredObject(element, desc, ep, c);
        }
    }

    private void handleConfigPropertyForAdministeredObject(AnnotationInfo element,
                                                           ConnectorDescriptor desc, ConnectorConfigProperty ep, Class c) {

        if (c.getAnnotation(AdministeredObject.class) != null) {
            AdministeredObject ao = (AdministeredObject) c.getAnnotation(AdministeredObject.class);
            Class[] adminObjectInterfaces = ao.adminObjectInterfaces();
            for (Class adminObjectInterface : adminObjectInterfaces) {
                AdminObject adminObject = desc.getAdminObject(adminObjectInterface.getName(), c.getName());
                if (adminObject != null) {
                    if (!(isConfigDefined(adminObject.getConfigProperties(), ep))) {
                        adminObject.addConfigProperty(ep);
                    }
                } else {
                    // ideally adminObject should not be null as "@AdministeredObject"
                    // should have been handled before @ConfigProperty
                    getFailureResult(element, "could not get adminobject of interface " +
                            "[ " + adminObjectInterface.getName() + " ]" +
                            " and class [ " + c.getName() + " ]", true);
                }
            }
        }
    }

    private void processActivation(AnnotationInfo element, ConnectorDescriptor desc,
                                   ConnectorConfigProperty ep, Class c) {
        if (desc.getInBoundDefined()) {
            //TODO V3 above check is not needed if we are sure that "@Activation" is handled already
            InboundResourceAdapter ira = desc.getInboundResourceAdapter();
            if (c.getAnnotation(Activation.class) != null) {
                Activation activation = (Activation) c.getAnnotation(Activation.class);
                Class[] messageListeners = activation.messageListeners();

                //messageListeners cant be 0 as we ask "@Activation" to be handled before "@ConfigProperty"

                for (Class clz : messageListeners) {
                    if (ira.hasMessageListenerType(clz.getName())) {
                        MessageListener ml = ira.getMessageListener(clz.getName());

                        if (!(isConfigDefined(ml.getConfigProperties(), ep))) {
                            ml.addConfigProperty(ep);
                        }

                    } else {
                        //TODO V3 possible that tha Activation annotation is not handled yet ?
                    }
                }
            } else {
                //TODO V3 how do we handle this case ? as multiple message-listeners can have
                // same activationSpec class ?
                debug("configProperty annotation is not annotated with Activation, but implements ActivationSpec");
            }
        } else {
            //TODO V3 can't happen as we ask "@Activation" to be handled before processing "@ConfigProperty" ?
            getFailureResult(element, "No Inbound RA yet defined ", true);
        }
    }

    private void processConnectionDefinition(AnnotationInfo element, ConnectorDescriptor desc,
                                             ConnectorConfigProperty ep, Class c) {
        //3) TODO V3 how do we make sure that @Connector annotation for this MCF is already processed ?
        //TODO V3 should we handle @connectionDefinitions also ?

        //TODO V3 make sure that the class that implements MCF is annotated with connectionDefinition ?

        //TODO V3 can't we just test for ConnectionDefinition annotation alone ?

        if (desc.getOutBoundDefined()) {
            OutboundResourceAdapter ora = desc.getOutboundResourceAdapter();
            Set connectionDefinitions = ora.getConnectionDefs();
            boolean foundConnectionDefinition = false;
            for (Object o : connectionDefinitions) {
                ConnectionDefDescriptor cd = (ConnectionDefDescriptor) o;
                //TODO V3 is assuming that a connection-defintion with the MCF class as this
                // annotation's MCF class
                // is the one that need to be updated with config properties, correct ?

                if (cd.getManagedConnectionFactoryImpl().equals(c.getName())) {

                    if (!(isConfigDefined(cd.getConfigProperties(), ep))) {
                        cd.addConfigProperty(ep);
                    }

                    foundConnectionDefinition = true;
                    /* TODO V3 it is possible that multiple MCFs with same class, but different
                     connection-factory-interface
                     hence process all connection definitions ? */
                    // break;
                }
            }
            if (!foundConnectionDefinition) {
                // TODO V3 should we create a new connection defintion ? We may not have necessary
                // properties if its not @ConnectionDefinition (MCF alone)

                getFailureResult(element, "could not find connection definition for MCF class " + c.getName(), true);
            }
        } else {
            getFailureResult(element, "Outbound RA is not yet defined", true);
        }
    }

    private void processConnector(ConnectorDescriptor desc, ConnectorConfigProperty ep) {
        //handle the annotation specified on a ResourceAdapter JavaBean
        /* TODO V3
         1) how do we make sure that this ResourceAdapter JavaBean is the one that is
         actually selected by the descriptor
         2) how do we make sure that the connector annotation in question is the one that is
         actually selected ? */

        //make sure that the property is not already specified in DD
        if (!(isConfigDefined(desc.getConfigProperties(), ep))) {
            desc.addConfigProperty(ep);
        }
    }

    private boolean isConfigDefined(Set configProperties, ConnectorConfigProperty ep) {
        boolean result = false;
        for (Object o : configProperties) {
            ConnectorConfigProperty ddEnvProperty = (ConnectorConfigProperty) o;
            if (ddEnvProperty.getName().equals(ep.getName())) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * @return a default processed result
     */
    protected HandlerProcessingResult getDefaultProcessedResult() {
        return HandlerProcessingResultImpl.getDefaultResult(
                getAnnotationType(), ResultType.PROCESSED);
    }


    public Class<? extends Annotation>[] getTypeDependencies() {
        return new Class[]{Connector.class, ConnectionDefinition.class, ConnectionDefinitions.class,
                Activation.class, AdministeredObject.class};
    }

    private void debug(String s) {
        logger.log(Level.INFO, "[ConfigPropertyHandler] " + s);
    }

    private HandlerProcessingResultImpl getFailureResult(AnnotationInfo element, String message, boolean doLog) {
        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();
        result.addResult(getAnnotationType(), ResultType.FAILED);
        if (doLog) {
            Object o = element.getAnnotatedElement();
            String className = null;
            if(o instanceof Field){
                className = ((Field)o).getDeclaringClass().getName();
            }else { //else it can be only METHOD
                className = ((Method)o).getDeclaringClass().getName();
            }
            //TODO V3 logStrings
            logger.log(Level.WARNING, "failed to handle annotation [ " + element.getAnnotation() + " ]" +
                    " on class [ " + className + " ], reason : " + message);
        }
        return result;
    }
}
