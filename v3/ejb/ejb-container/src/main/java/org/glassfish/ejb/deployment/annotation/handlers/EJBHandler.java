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
package org.glassfish.ejb.deployment.annotation.handlers;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.logging.Level;

import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbEntityDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the javax.ejb.EJB
 *
 * @author Shing Wai Chan
 */
@Service
public class EJBHandler extends AbstractResourceHandler {
    
    public EJBHandler() {
    }

    /**
     * @return the annoation type this annotation handler is handling
     */
    public Class<? extends Annotation> getAnnotationType() {
        return EJB.class;
    }

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param ainfo the annotation information
     * @param rcContexts an array of ResourceContainerContext
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {

        EJB ejbAn = (EJB)ainfo.getAnnotation();
        return processEJB(ainfo, rcContexts, ejbAn);
    }


    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param ainfo the annotation information
     * @param rcContexts an array of ResourceContainerContext
     * @param ejbAn
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult processEJB(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts, EJB ejbAn)
            throws AnnotationProcessorException {
        EjbReferenceDescriptor ejbRefs[] = null;

        if (ElementType.FIELD.equals(ainfo.getElementType())) {
            Field f = (Field)ainfo.getAnnotatedElement();
            String targetClassName = f.getDeclaringClass().getName();

            String logicalName = ejbAn.name();

            // applying with default
            if (logicalName.equals("")) {
                logicalName = targetClassName + "/" + f.getName();
            }

            // If specified, beanInterface() overrides field type
            // NOTE that defaultValue is Object.class, not null
            Class beanInterface = (ejbAn.beanInterface() == Object.class) ?
                    f.getType() : ejbAn.beanInterface();

            InjectionTarget target = new InjectionTarget();
            target.setClassName(targetClassName);
            target.setFieldName(f.getName());
            
            ejbRefs = getEjbReferenceDescriptors(logicalName, rcContexts);
            for (EjbReferenceDescriptor ejbRef : ejbRefs) {
                ejbRef.addInjectionTarget(target);

                if (ejbRef.getName().length() == 0) { // a new one
                    processNewEJBAnnotation(ejbRef, beanInterface,
                                            logicalName, ejbAn);
                }
            }
        } else if (ElementType.METHOD.equals(ainfo.getElementType())) {

            Method m = (Method)ainfo.getAnnotatedElement();
            String targetClassName = m.getDeclaringClass().getName();

            String logicalName = ejbAn.name();
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
            Class beanInterface = (ejbAn.beanInterface() == Object.class) ?
                    params[0] : ejbAn.beanInterface();

            InjectionTarget target = new InjectionTarget();
            target.setClassName(targetClassName);
            target.setMethodName(m.getName());
            
            ejbRefs = getEjbReferenceDescriptors(logicalName, rcContexts);
            for (EjbReferenceDescriptor ejbRef : ejbRefs) {

                ejbRef.addInjectionTarget(target);

                if (ejbRef.getName().length() == 0) { // a new one

                    processNewEJBAnnotation(ejbRef, beanInterface,
                                            logicalName, ejbAn);
                }
            }
        } else if( ElementType.TYPE.equals(ainfo.getElementType()) ) {
            // name() and beanInterface() are required elements for 
            // TYPE-level usage
            String logicalName = ejbAn.name();
            Class beanInterface = ejbAn.beanInterface();

            if( "".equals(logicalName) || beanInterface == Object.class ) {
                Class c = (Class) ainfo.getAnnotatedElement();
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.invalidtypelevelejb",
                    "Invalid TYPE-level @EJB with name() = [{0}] and beanInterface = [{1}] in {2}.  Each TYPE-level @EJB must specify both name() and beanInterface().",
                new Object[] { logicalName, beanInterface, c }));
                return getDefaultFailedResult();
            }
                               
            ejbRefs = getEjbReferenceDescriptors(logicalName, rcContexts);
            for (EjbReferenceDescriptor ejbRef : ejbRefs) {
                if (ejbRef.getName().length() == 0) { // a new one

                    processNewEJBAnnotation(ejbRef, beanInterface,
                                            logicalName, ejbAn);
                }
            }
        } 

        return getDefaultProcessedResult();
    }

    /**
     * Return EjbReferenceDescriptors with given name if exists or a new
     * one without name being set.
     * @param logicalName
     * @param rcContexts
     * @return an array of EjbReferenceDescriptor
     */
    private EjbReferenceDescriptor[] getEjbReferenceDescriptors(
            String logicalName, ResourceContainerContext[] rcContexts) {
        EjbReferenceDescriptor ejbRefs[] =
                new EjbReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            EjbReferenceDescriptor ejbRef =
                (EjbReferenceDescriptor)rcContexts[i].getEjbReference(logicalName);
            if (ejbRef == null) {
                ejbRef = new EjbReferenceDescriptor();
                rcContexts[i].addEjbReferenceDescriptor(ejbRef);
            }
            ejbRefs[i] = ejbRef;
        }

        return ejbRefs;
    }

    private void processNewEJBAnnotation(EjbReferenceDescriptor ejbRef,
                                         Class beanInterface, 
                                         String logicalName, EJB annotation) {
        
        ejbRef.setName(logicalName);
        
        String targetBeanType = EjbSessionDescriptor.TYPE;
        if (EJBHome.class.isAssignableFrom(beanInterface) ||
            EJBLocalHome.class.isAssignableFrom(beanInterface)) {
            targetBeanType = processForHomeInterface(ejbRef, beanInterface);
        } else {
            // EJB 3.0 style Business Interface
            ejbRef.setEjbInterface(beanInterface.getName());
            
            if( beanInterface.getAnnotation(Local.class) != null ) {
                ejbRef.setLocal(true);
            } else if( beanInterface.getAnnotation(Remote.class) 
                       != null ) {
                ejbRef.setLocal(false);
            } else {
                // Assume remote for now. We can't know for sure until the
                // post-validation stage.  Even though local business will 
                // probably be more common than remote business, defaulting 
                // to remote business simplies the post-application 
                // validation logic considerably.  See 
                // EjbBundleValidator.accept(EjbReferenceDescriptor) 
                // for more details.
                ejbRef.setLocal(false);
            }
        }
        
        String ejbAnBeanName = annotation.beanName();
        if (ejbAnBeanName != null && ejbAnBeanName.length() > 0) {
            ejbRef.setLinkName(ejbAnBeanName);
        }


        String ejbAnLookup = annotation.lookup();
        if (ejbAnLookup != null && ejbAnLookup.length() > 0) {
            if( !ejbRef.hasLookupName() ) {
                ejbRef.setLookupName(ejbAnLookup);
            }
        }

        
        ejbRef.setType(targetBeanType);
        ejbRef.setMappedName(annotation.mappedName());
        ejbRef.setDescription(annotation.description());
    }

    /**
     * @return targetBeanType
     */
    private String processForHomeInterface(EjbReferenceDescriptor ejbRef,
            Class beanInterface) {

        //XXX assume session bean
        String targetBeanType = EjbSessionDescriptor.TYPE;
        ejbRef.setHomeClassName(beanInterface.getName());

        try {
            // Set bean Interface as well so we have all
            // the info that would have been in an ejb-ref/
            // ejb-local-ref
            Method[] methods = beanInterface.getMethods();
            for (Method m : methods) {
                if (m.getName().equals("create")) {
                    ejbRef.setEjbInterface(m.getReturnType().getName());
                    break;
                }
            }
            // Use existence of findByPrimaryKey method on Home to
            // determine target bean type
            for (Method m : methods) {
                if (m.getName().equals("findByPrimaryKey")) {
                    targetBeanType = EjbEntityDescriptor.TYPE;
                    break;
                }
            }
        } catch(Exception e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, 
                "component intf / ejb type annotation processing error", e);
            }
        }

        ejbRef.setLocal(EJBLocalHome.class.isAssignableFrom(beanInterface));
        return targetBeanType;
    }
}
