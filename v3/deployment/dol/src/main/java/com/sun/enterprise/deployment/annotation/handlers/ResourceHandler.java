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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * This handler is responsible for handling the javax.annotation.Resource
 *
 */
@Service
public class ResourceHandler extends AbstractResourceHandler {

    // Map of all @Resource types that map to env-entries and their
    // corresponding types.  
    private static final Map<Class, Class> envEntryTypes;

    static {

        envEntryTypes = new HashMap<Class, Class>();

        envEntryTypes.put(String.class, String.class);

        envEntryTypes.put(Class.class, Class.class);

        envEntryTypes.put(Character.class, Character.class);
        envEntryTypes.put(Character.TYPE, Character.class);
        envEntryTypes.put(char.class, Character.class);

        envEntryTypes.put(Byte.class, Byte.class);
        envEntryTypes.put(Byte.TYPE, Byte.class);
        envEntryTypes.put(byte.class, Byte.class);

        envEntryTypes.put(Short.class, Short.class);
        envEntryTypes.put(Short.TYPE, Short.class);
        envEntryTypes.put(short.class, Short.class);

        envEntryTypes.put(Integer.class, Integer.class);
        envEntryTypes.put(Integer.TYPE, Integer.class);
        envEntryTypes.put(int.class, Integer.class);

        envEntryTypes.put(Long.class, Long.class);        
        envEntryTypes.put(Long.TYPE, Long.class);        
        envEntryTypes.put(long.class, Long.class);        

        envEntryTypes.put(Boolean.class, Boolean.class);
        envEntryTypes.put(Boolean.TYPE, Boolean.class);
        envEntryTypes.put(boolean.class, Boolean.class);

        envEntryTypes.put(Double.class, Double.class);
        envEntryTypes.put(Double.TYPE, Double.class);
        envEntryTypes.put(double.class, Double.class);

        envEntryTypes.put(Float.class, Float.class);
        envEntryTypes.put(Float.TYPE, Float.class);
        envEntryTypes.put(float.class, Float.class);

    }
        
    public ResourceHandler() {
    }

    /**
     * @return the annoation type this annotation handler is handling
     */
    public Class<? extends Annotation> getAnnotationType() {
        return Resource.class;
    }

    /**
     * This entry point is used both for a single @EJB and iteratively
     * from a compound @EJBs processor.
     */
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {

        Resource resourceAn = (Resource)ainfo.getAnnotation();
        return processResource(ainfo, rcContexts, resourceAn);
    }

    protected HandlerProcessingResult processResource(AnnotationInfo ainfo,
                                   ResourceContainerContext[] rcContexts, 
                                   Resource resourceAn)
        throws AnnotationProcessorException {

        ResourceReferenceDescriptor resourceRefs[] = null;

        if (ElementType.FIELD.equals(ainfo.getElementType())) {
            Field f = (Field)ainfo.getAnnotatedElement();
            String targetClassName = f.getDeclaringClass().getName();

            String logicalName = resourceAn.name();

            // applying with default 
            if (logicalName.equals("")) {
                logicalName = targetClassName + "/" + f.getName();
            }

            // If specified, beanInterface() overrides parameter type
            // NOTE that default value is Object.class, not null
            Class resourceType = (resourceAn.type() == Object.class) ?
                    f.getType() : resourceAn.type();

            DescriptorInfo descriptorInfo = getDescriptors
                (resourceType, logicalName, rcContexts, resourceAn);
                 
            InjectionTarget target = new InjectionTarget();
            target.setFieldName(f.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);
            
            for (EnvironmentProperty desc : descriptorInfo.descriptors) {            
                desc.addInjectionTarget(target);
                    
                if (desc.getName().length() == 0) { // a new one
                    processNewAnnotation(desc, descriptorInfo.dependencyType,
                                         descriptorInfo.resourceType,
                                         logicalName, resourceAn);
                } else if (desc.getInjectResourceType() == null) {              
                    // if the optional resource type is not set, 
                    // set it using the resource type of field/method
                    desc.setInjectResourceType(
                        descriptorInfo.resourceType.getName());
                }
            }
        } else if (ElementType.METHOD.equals(ainfo.getElementType())) {

            Method m = (Method)ainfo.getAnnotatedElement();
            String targetClassName = m.getDeclaringClass().getName();

            String logicalName = resourceAn.name();
            if( logicalName.equals("") ) {
                // Derive javabean property name.
                String propertyName = 
                        getInjectionMethodPropertyName(m, ainfo);

                // prefixing with fully qualified type name 
                logicalName = targetClassName + "/" + propertyName;
            }

            validateInjectionMethod(m, ainfo);

            Class[] params = m.getParameterTypes();
            // If specified, beanInterface() overrides parameter type
            // NOTE that default value is Object.class, not null
            Class resourceType = (resourceAn.type() == Object.class) ?
                    params[0] : resourceAn.type();


            DescriptorInfo descriptorInfo = getDescriptors
                (resourceType, logicalName, rcContexts, resourceAn);

            InjectionTarget target = new InjectionTarget();
            target.setMethodName(m.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);

            for (EnvironmentProperty desc : descriptorInfo.descriptors) {
                desc.addInjectionTarget(target);
                    
                if (desc.getName().length() == 0) { // a new one
                    processNewAnnotation(desc, 
                                         descriptorInfo.dependencyType,
                                         descriptorInfo.resourceType,
                                         logicalName, resourceAn);
                }
            }

        } else if( ElementType.TYPE.equals(ainfo.getElementType()) ) {
            // name() and type() are required for TYPE-level @Resource
            String logicalName = resourceAn.name();
            Class resourceType = resourceAn.type();

            if( "".equals(logicalName) || resourceType == Object.class ) {
                Class c = (Class) ainfo.getAnnotatedElement();
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.invalidtypelevelresource",                
                    "Invalid TYPE-level @Resource with name() = [{0}] and type = [{1}] in {2}. Each TYPE-level @Resource must specify both name() and type().",
                    new Object[] { logicalName, resourceType, c }));
                return getDefaultFailedResult();
            }

            DescriptorInfo descriptorInfo = getDescriptors
                (resourceType, logicalName, rcContexts, resourceAn);

            for (EnvironmentProperty desc : descriptorInfo.descriptors) {
                    
                if (desc.getName().length() == 0) { // a new one
                    processNewAnnotation(desc,
                                         descriptorInfo.dependencyType,
                                         descriptorInfo.resourceType,
                                         logicalName, resourceAn);
                }
            }
        } 

        return getDefaultProcessedResult();
    }

    private class DescriptorInfo {

        public EnvironmentProperty[] descriptors;
        public DependencyType dependencyType;
        public Class resourceType;
    }


    private enum DependencyType {
        ENV_ENTRY,
        RESOURCE_REF,
        MESSAGE_DESTINATION_REF,
        RESOURCE_ENV_REF
    }



    private DescriptorInfo getDescriptors(Class resourceType,
        String logicalName, ResourceContainerContext[] rcContexts, Resource resourceAn) {
            
        DescriptorInfo descriptorInfo = new DescriptorInfo();
        descriptorInfo.dependencyType = DependencyType.RESOURCE_REF;
        descriptorInfo.resourceType = resourceType;

        Class webServiceContext = null;
        try {

            WSDolSupport support  = Globals.getDefaultHabitat().getComponent(WSDolSupport.class);
            if (support!=null) {
                webServiceContext = support.getType("javax.xml.ws.WebServiceContext");
            }
        }   catch(Exception e) {
            // we don't care, either we don't have the class, ot the bundled is not installed
        }
        if( (resourceType.getName().equals("javax.jms.Queue")) ||
            (resourceType.getName().equals("javax.jms.Topic") )) {
            descriptorInfo.descriptors = 
                getMessageDestinationReferenceDescriptors
                (logicalName, rcContexts);
            descriptorInfo.dependencyType = 
                DependencyType.MESSAGE_DESTINATION_REF;
        } else if ( resourceType == javax.sql.DataSource.class ||
                    resourceType.getName().equals("javax.jms.ConnectionFactory") ||
                    resourceType.getName().equals("javax.jms.QueueConnectionFactory") ||
                    resourceType.getName().equals("javax.jms.TopicConnectionFactory") ||
                    resourceType == webServiceContext ||
                    resourceType.getName().equals("javax.mail.Session") || 
                    resourceType.getName().equals("java.net.URL") ||
                    resourceType.getName().equals("javax.resource.cci.ConnectionFactory")
                    || resourceType == org.omg.CORBA_2_3.ORB.class || 
                    resourceType == org.omg.CORBA.ORB.class || 
                    resourceType.getName().equals("javax.jms.XAConnectionFactory") ||
                    resourceType.getName().equals("javax.jms.XAQueueConnectionFactory") ||
                    resourceType.getName().equals("javax.jms.XATopicConnectionFactory") ) {
            descriptorInfo.descriptors = getResourceReferenceDescriptors
                (logicalName, rcContexts);
            descriptorInfo.dependencyType = DependencyType.RESOURCE_REF;
        } else if( envEntryTypes.containsKey(resourceType) || resourceType.isEnum()) {
            descriptorInfo.descriptors = getEnvironmentPropertyDescriptors
                (logicalName, rcContexts, resourceAn);
            descriptorInfo.dependencyType = DependencyType.ENV_ENTRY;
            // Get corresponding class type.  This does the appropriate
            // mapping for primitives.  For everything else, the type is
            // unchanged.
            descriptorInfo.resourceType = envEntryTypes.get(resourceType);
            if (descriptorInfo.resourceType == null) {
                // subclass of Enum case
                descriptorInfo.resourceType = resourceType;
            }
        } else {
            descriptorInfo.descriptors =
                getJmsDestinationReferenceDescriptors
                (logicalName, rcContexts);
            descriptorInfo.dependencyType = DependencyType.RESOURCE_ENV_REF;
        }
        return descriptorInfo;
    }

    /**
     * Return ResourceReferenceDescriptors with given name if exists or a new
     * one without name being set.
     * @param logicalName
     * @param rcContexts
     * @return an array of ResourceReferenceDescriptor
     */
    private ResourceReferenceDescriptor[] getResourceReferenceDescriptors(
            String logicalName, ResourceContainerContext[] rcContexts) {
        ResourceReferenceDescriptor resourceRefs[] =
                new ResourceReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            ResourceReferenceDescriptor resourceRef =
                rcContexts[i].getResourceReference(logicalName);
            if (resourceRef == null) {
                resourceRef = new ResourceReferenceDescriptor();
                rcContexts[i].addResourceReferenceDescriptor(resourceRef);
            }
            resourceRefs[i] = resourceRef;
        }

        return resourceRefs;
    }

    /**
     * Return MessageDestinationReferenceDescriptors with given name 
     * if exists or a new one without name being set.
     * @param logicName
     * @param rcContexts
     * @return an array of message destination reference descriptors
     */
    private MessageDestinationReferenceDescriptor[] 
        getMessageDestinationReferenceDescriptors
        (String logicName, ResourceContainerContext[] rcContexts) {
            
        MessageDestinationReferenceDescriptor msgDestRefs[] =
                new MessageDestinationReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            MessageDestinationReferenceDescriptor msgDestRef =
                rcContexts[i].getMessageDestinationReference(logicName);
            if (msgDestRef == null) {
               msgDestRef = new MessageDestinationReferenceDescriptor();
               rcContexts[i].addMessageDestinationReferenceDescriptor(
                   msgDestRef);
            }
            msgDestRefs[i] = msgDestRef;
        }

        return msgDestRefs;
    }

    /**
     * Return JmsDestinationReferenceDescriptors with given name
     * if exists or a new one without name being set.
     * @param logicName
     * @param rcContexts
     * @return an array of resource env reference descriptors
     */
    private JmsDestinationReferenceDescriptor[]
        getJmsDestinationReferenceDescriptors
        (String logicName, ResourceContainerContext[] rcContexts) {

        JmsDestinationReferenceDescriptor jmsDestRefs[] =
                new JmsDestinationReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            JmsDestinationReferenceDescriptor jmsDestRef =
                rcContexts[i].getJmsDestinationReference(logicName);
            if (jmsDestRef == null) {
               jmsDestRef = new JmsDestinationReferenceDescriptor();
               rcContexts[i].addJmsDestinationReferenceDescriptor(
                   jmsDestRef);
            }
            jmsDestRefs[i] = jmsDestRef;
        }

        return jmsDestRefs;
    }

    /**
     * Return EnvironmentProperty descriptors with given name 
     * if exists or a new one without name being set.
     * @param logicalName
     * @param rcContexts
     * @return an array of EnvironmentProperty descriptors
     */
    private EnvironmentProperty[] getEnvironmentPropertyDescriptors
        (String logicalName, ResourceContainerContext[] rcContexts, Resource annotation) {
            
        Collection<EnvironmentProperty> envEntries =
            new ArrayList<EnvironmentProperty>();

        for (int i = 0; i < rcContexts.length; i++) {
            EnvironmentProperty envEntry =
                rcContexts[i].getEnvEntry(logicalName);
            // For @Resource declarations that map to env-entries, if there
            // is no corresponding deployment descriptor entry that has a
            // value and no lookup(), it's treated as if the declaration doesn't exist.
            // A common case is that the @Resource is applied to a field
            // with a default value which was not overridden by the deployer.
            if (envEntry != null) {
                envEntries.add(envEntry);
            } else {
                envEntry = new EnvironmentProperty();
                envEntries.add(envEntry);
                rcContexts[i].addEnvEntryDescriptor(envEntry);
            }
        }

        return envEntries.toArray(new EnvironmentProperty[] {});
    }

    private void processNewAnnotation(EnvironmentProperty desc,
                                      DependencyType dependencyType,
                                      Class resourceType, 
                                      String logicalName, Resource annotation){

        desc.setName(logicalName);
        if (desc.getDescription() == null || desc.getDescription().length() == 0) {
            desc.setDescription(annotation.description());
        }
        
        if( dependencyType == DependencyType.ENV_ENTRY ) {

            desc.setType(resourceType.getName());
            desc.setMappedName(annotation.mappedName());
            desc.setLookupName(getResourceLookupValue(annotation));

        } else if( dependencyType == DependencyType.MESSAGE_DESTINATION_REF ) {

            MessageDestinationReferenceDescriptor msgDestRef =
                (MessageDestinationReferenceDescriptor) desc;
            msgDestRef.setDestinationType(resourceType.getName());
            msgDestRef.setMappedName(annotation.mappedName());
            msgDestRef.setLookupName(getResourceLookupValue(annotation));
        } else if( dependencyType == DependencyType.RESOURCE_ENV_REF ) {

            JmsDestinationReferenceDescriptor jmsDestRef =
                (JmsDestinationReferenceDescriptor) desc;
            jmsDestRef.setRefType(resourceType.getName());
            jmsDestRef.setMappedName(annotation.mappedName());
            jmsDestRef.setLookupName(getResourceLookupValue(annotation));
        } else if( dependencyType == DependencyType.RESOURCE_REF ) {
            
            desc.setType(resourceType.getName());
        
            ResourceReferenceDescriptor resRef = (ResourceReferenceDescriptor)
                desc;

            String authType = 
                (annotation.authenticationType() ==
                 Resource.AuthenticationType.CONTAINER) ?
                ResourceReferenceDescriptor.CONTAINER_AUTHORIZATION :
                ResourceReferenceDescriptor.APPLICATION_AUTHORIZATION;
            
            resRef.setAuthorization(authType);
            
            String sharable = annotation.shareable() ?
                ResourceReferenceDescriptor.RESOURCE_SHAREABLE :
                ResourceReferenceDescriptor.RESOURCE_UNSHAREABLE;
            
            resRef.setSharingScope(sharable);
            resRef.setMappedName(annotation.mappedName());
            resRef.setLookupName(getResourceLookupValue(annotation));
        }
        
        return;
    }

    private String getResourceLookupValue(Resource annotation) {

        String lookupValue = "";
        try {
            lookupValue = annotation.lookup();
        } catch(NoSuchMethodError nsme) {
           // Probably means lib endorsed dir is not set and an older version of Resource
           // is being picked up from JDK.  Don't treat this as a fatal error.
        }

        return lookupValue;

    }

}
