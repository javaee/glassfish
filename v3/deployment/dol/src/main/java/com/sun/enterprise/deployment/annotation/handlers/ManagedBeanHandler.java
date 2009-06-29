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
import com.sun.enterprise.deployment.annotation.context.ManagedBeanContext;
import org.glassfish.apf.*;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

import javax.annotation.ManagedBean;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;

@Service
public class ManagedBeanHandler extends AbstractHandler {


    public ManagedBeanHandler() {
    }

    /**
     * @return the annoation type this annotation handler is handling
     */
    public Class<? extends Annotation> getAnnotationType() {
        return ManagedBean.class;
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo element)
        throws AnnotationProcessorException {


        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        if( aeHandler instanceof ManagedBeanContext ) {

            // Ignore @ManagedBean processing during ManagedBean class processing itself
            return getDefaultProcessedResult();
        }

        ManagedBeanDescriptor managedBeanDesc = new ManagedBeanDescriptor();

        ManagedBean resourceAn = (ManagedBean) element.getAnnotation();

        // name() is optional
        String logicalName = resourceAn.value();
        if( !logicalName.equals("")) {
            managedBeanDesc.setName(logicalName);
        }

        Class managedBeanClass = (Class) element.getAnnotatedElement();

        managedBeanDesc.setBeanClassName(managedBeanClass.getName());


        Class[] interceptors = null;


        // For now, just process the javax.interceptor related annotations directly instead
        // of relying on the annotation framework.   All the existing javax.interceptor
        // handlers are very tightly coupled to ejb so it would be more work to abstract those
        // than to just process the annotations directly.

        // TODO refactor javax.interceptor annotation handlers to support both ejb and non-ejb
        // related interceptors

        Annotation interceptorsAnn = getClassAnnotation(managedBeanClass, "javax.interceptor.Interceptors");
        if( interceptorsAnn != null ) {
            try {
                Method m = interceptorsAnn.annotationType().getDeclaredMethod("value");
                interceptors = (Class[]) m.invoke(interceptorsAnn);
            } catch(Exception e) {
                AnnotationProcessorException ape = new AnnotationProcessorException(e.getMessage(), element);
                ape.initCause(e);
                throw ape;
            }
        }

        Method managedBeanAroundInvoke =
            getMethodForMethodAnnotation(managedBeanClass, "javax.interceptor.AroundInvoke");
        if( managedBeanAroundInvoke != null ) {
            // TODO process bean class superclasses for AroundInvoke
            LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
            desc.setLifecycleCallbackClass(managedBeanClass.getName());
            desc.setLifecycleCallbackMethod(managedBeanAroundInvoke.getName());
            managedBeanDesc.addAroundInvokeDescriptor(desc);
        }




        if( aeHandler instanceof ResourceContainerContext ) {
            ((ResourceContainerContext) aeHandler).addManagedBean(managedBeanDesc);            


            // process managed bean class annotations
            ManagedBeanContext managedBeanContext =
                new ManagedBeanContext(managedBeanDesc);
            ProcessingContext procContext = element.getProcessingContext();
            procContext.pushHandler(managedBeanContext);

            procContext.getProcessor().process(
                procContext, new Class[] { managedBeanClass });

            if( interceptors != null ) {

                List<InterceptorDescriptor> classInterceptorChain = new LinkedList<InterceptorDescriptor>();

                for(Class i : interceptors) {

                    InterceptorDescriptor nextInterceptor = new InterceptorDescriptor();
                    nextInterceptor.setInterceptorClassName(i.getName());

                    // Redirect PostConstruct / PreDestroy methods to InterceptorDescriptor
                    // during annotation processing
                    managedBeanContext.setInterceptorMode(nextInterceptor);

                    // Process annotations on interceptor
                    procContext.pushHandler(managedBeanContext);
                    procContext.getProcessor().process(procContext, new Class[] {i});


                    managedBeanContext.unsetInterceptorMode();

                    // Add interceptor to class-leve chain
                    classInterceptorChain.add(nextInterceptor);

                    Method interceptorAroundInvoke =
                        getMethodForMethodAnnotation(i, "javax.interceptor.AroundInvoke");
                    if( interceptorAroundInvoke != null ) {
                        // TODO process superclasses for AroundInvoke
                        LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
                        desc.setLifecycleCallbackClass(i.getName());
                        desc.setLifecycleCallbackMethod(interceptorAroundInvoke.getName());
                        nextInterceptor.addAroundInvokeDescriptor(desc);
                    }

                }
                
                managedBeanDesc.setClassInterceptorChain(classInterceptorChain);

            }
           
        }

        return getDefaultProcessedResult();
    }

    private Annotation getClassAnnotation(Class c, String annotationClassName) {
        for(Annotation next : c.getDeclaredAnnotations()) {

            if( next.annotationType().getName().equals(annotationClassName)) {
                return next;
            }
        }
        return null;
    }

    private Method getMethodForMethodAnnotation(Class c, String annotationClassName) {
        for(Method m : c.getDeclaredMethods()) {
            for(Annotation next : m.getDeclaredAnnotations()) {

                if( next.annotationType().getName().equals(annotationClassName)) {
                    return m;
                }
            }
        }
        return null;
    }




}
