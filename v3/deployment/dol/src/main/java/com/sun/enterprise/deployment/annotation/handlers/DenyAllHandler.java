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

import com.sun.enterprise.deployment.AuthorizationConstraintImpl;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.WebResourceCollection;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;
import com.sun.enterprise.deployment.util.TypeUtil;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * This handler is responsible for handling the javax.annotation.security.DenyAll.
 *
 * @author Shing Wai Chan
 */
@Service
public class DenyAllHandler extends AbstractCommonAttributeHandler {
    
    public DenyAllHandler() {
    }
    
    /**
     * @return the annoation type this annotation handler is handling
     */
    public Class<? extends Annotation> getAnnotationType() {
        return DenyAll.class;
    }    

    /**
     * Process Annotation with given EjbContexts.
     * @param ainfo
     * @param ejbContexts
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

        if (hasMoreThanOneAccessControlAnnotation(ainfo)) {
            return getDefaultFailedResult();
        }

        Method annMethod = (Method) ainfo.getAnnotatedElement();

        for (EjbContext ejbContext : ejbContexts) {
            EjbDescriptor ejbDesc = ejbContext.getDescriptor();
                
            for (Object next : ejbDesc.getSecurityBusinessMethodDescriptors()) {
                MethodDescriptor md = (MethodDescriptor)next;
                // override by xml
                if (!hasMethodPermissionsFromDD(md, ejbDesc)) {
                    Method m = md.getMethod(ejbDesc);
                    if (TypeUtil.sameMethodSignature(m, annMethod)) {
                        ejbDesc.addPermissionedMethod(
                            MethodPermission.getExcludedMethodPermission(), md);
                    }
                }
            }
        }

        return getDefaultProcessedResult();
    }   

    /**
     * Process Annotation with given WebCompContexts.
     * @param ainfo
     * @param webCompContexts
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebComponentContext[] webCompContexts)
            throws AnnotationProcessorException {

        return processAuthAnnotationOnWebComponentContexts(ainfo, webCompContexts);
    }

    /**
     * Process Annotation with given WebBundleContext.
     * @param ainfo
     * @param webBundleContext
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebBundleContext webBundleContext)
            throws AnnotationProcessorException {

        // this is not a web component
        return getInvalidAnnotatedElementHandlerResult(webBundleContext, ainfo);
    }

    /**
     * @return an array of annotation types this annotation handler would 
     * require to be processed (if present) before it processes it's own 
     * annotation type.
     */
    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAndWebAnnotationTypes();
    }

    @Override
    protected boolean supportTypeInheritance() {
        return true;
    }

    @Override
    protected SecurityConstraint addHttpMethodConstraint(
            Annotation authAnnotation, WebComponentDescriptor webCompDesc,
            String httpMethod, Set<String> urlPatterns) {

        WebBundleDescriptor webBundleDesc = webCompDesc.getWebBundleDescriptor();
        SecurityConstraint securityConstraint =
                createSecurityConstraint(webBundleDesc, urlPatterns, httpMethod);

        if (securityConstraint != null) {
            AuthorizationConstraintImpl ac = new AuthorizationConstraintImpl();
            securityConstraint.setAuthorizationConstraint(ac);
        }
        return securityConstraint;
    }
}
