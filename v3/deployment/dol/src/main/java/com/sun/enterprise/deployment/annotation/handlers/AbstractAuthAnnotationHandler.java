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
package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.SecurityConstraintImpl;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.WebResourceCollectionImpl;
import com.sun.enterprise.deployment.annotation.context.*;
import com.sun.enterprise.deployment.util.TypeUtil;
import com.sun.enterprise.deployment.web.AuthorizationConstraint;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.WebResourceCollection;
import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.TransportProtected;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is an abstract class encapsulate generic behaviour of auth annotations,
 * such as @DenyAll, @PermitAll, @RolesAllowed and @TransportProtected.
 *
 * Concrete subclass handlers need to implement the following:
 *     public Class&lt;? extends Annotation&gt; getAnnotationType();
 *     protected void processEjbMethodSecurity(Annotaton authAnnotation,
 *          MethodDescriptor md, EjbDescriptor ejbDesc);
 *     protected void processSecurityConstraint(Annotation authAnnotation,
 *          SecurityConstraint securityConstraint,
 *          WebComponentDescriptor webCompDesc);
 *     protected Classlt;? extends Annotaion&gt;[] relatedAnnotationClasses();
 *      
 * @author Shing Wai Chan
 */
abstract class AbstractAuthAnnotationHandler extends AbstractCommonAttributeHandler
        implements PostProcessor {

    /**
     * This method processes the EJB Security for the given Annotation.
     */
    protected abstract void processEjbMethodSecurity(Annotation authAnnotation,
            MethodDescriptor md, EjbDescriptor ejbDesc);

    /**
     * This method processes the SecurityConstraint for the given Annotation.
     * Derived class may like to override this method if necessary.
     */
    protected abstract void processSecurityConstraint(Annotation authAnnotation,
            SecurityConstraint securityConstraint,
            WebComponentDescriptor webComponentDesc); 

    /**
     * Process Annotation with given EjbContexts.
     * @param ainfo
     * @param ejbContexts
     * @return HandlerProcessingResult
     */
    @Override
    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, EjbContext[] ejbContexts)
            throws AnnotationProcessorException {

        if (!validateAccessControlAnnotations(ainfo)) {
            return getDefaultFailedResult();
        }

        Annotation authAnnotation = ainfo.getAnnotation();
        for (EjbContext ejbContext : ejbContexts) {
            EjbDescriptor ejbDesc = ejbContext.getDescriptor();
                
            if (ElementType.TYPE.equals(ainfo.getElementType())) {
                // postpone the processing at the end
                ejbContext.addPostProcessInfo(ainfo, this);
            } else { // METHOD
                Method annMethod = (Method) ainfo.getAnnotatedElement();
                for (Object next : ejbDesc.getSecurityBusinessMethodDescriptors()) {
                    MethodDescriptor md = (MethodDescriptor)next;
                    // override by xml
                    if (!hasMethodPermissionsFromDD(md, ejbDesc)) {
                        Method m = md.getMethod(ejbDesc);
                        if (TypeUtil.sameMethodSignature(m, annMethod)) {
                            processEjbMethodSecurity(authAnnotation, md, ejbDesc);
                        }
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
    @Override
    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebComponentContext[] webCompContexts)
            throws AnnotationProcessorException {

        if (!validateAccessControlAnnotations(ainfo)) {
            return getDefaultFailedResult();
        }

        Annotation authAnnotation = ainfo.getAnnotation();
        boolean ok = true;
        if (ElementType.TYPE.equals(ainfo.getElementType())) {
            for (WebComponentContext webCompContext : webCompContexts) {
                addAllHttpMethodConstraint(ainfo, webCompContext, authAnnotation);
            }
        } else {
            Method annMethod = (Method) ainfo.getAnnotatedElement();
            if (isValidHttpServletAnnotatedMethod(annMethod)) {
                String httpMethod = annMethod.getName().substring(2).toUpperCase();
                for (WebComponentContext webCompContext : webCompContexts) {
                    processHttpMethodAnnotation(ainfo, webCompContext, authAnnotation, httpMethod);
                }     
            } else {
                ok = false;
            }
        }

        return ((ok)? getDefaultProcessedResult() : getDefaultFailedResult());
    }

    /**
     * Process Annotation with given WebBundleContext.
     * @param ainfo
     * @param webBundleContext
     * @return HandlerProcessingResult
     */
    @Override
    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebBundleContext webBundleContext)
            throws AnnotationProcessorException {

        // this is not a web component
        return getInvalidAnnotatedElementHandlerResult(webBundleContext, ainfo);
    }

    /**
     * This method is for processing security annotation associated to ejb.
     * Dervied class call this method may like to override
     *
     * protected void processEjbMethodSecurity(Annotation authAnnotation,
     *         MethodDescriptor md, EjbDescriptor ejbDesc)
     */
    @Override
    public void postProcessAnnotation(AnnotationInfo ainfo,
            AnnotatedElementHandler aeHandler)
            throws AnnotationProcessorException {

        EjbContext ejbContext = (EjbContext)aeHandler;
        EjbDescriptor ejbDesc = ejbContext.getDescriptor();
        Annotation authAnnotation = ainfo.getAnnotation();

        if (!ejbContext.isInherited() &&
                (ejbDesc.getMethodPermissionsFromDD() == null ||
                ejbDesc.getMethodPermissionsFromDD().size() == 0)) {
            for (MethodDescriptor md : getMethodAllDescriptors(ejbDesc)) {
                processEjbMethodSecurity(authAnnotation, md, ejbDesc);
            }
        } else {
            Class classAn = (Class)ainfo.getAnnotatedElement();
            for (Object next : ejbDesc.getSecurityBusinessMethodDescriptors()) {
                MethodDescriptor md = (MethodDescriptor)next;
                Method m = md.getMethod(ejbDesc);
                // override by existing info
                if (classAn.equals(ejbContext.getDeclaringClass(md)) &&
                        !hasMethodPermissionsFromDD(md, ejbDesc)) {
                    processEjbMethodSecurity(authAnnotation, md, ejbDesc);
                }
            }
        }
    }

    @Override
    protected boolean supportTypeInheritance() {
        return true;
    }

    /**
     * This method returns a list of related annotation types.
     * Those annotations should not be used with the given annotaton type.
     */
    protected Class<? extends Annotation>[] relatedAnnotationTypes() {
        return new Class[0];
    }

    //---------- helper methods ---------

    /**
     * Returns MethodDescriptors representing All for a given EjbDescriptor.
     * @param ejbDesc
     * @return resulting MethodDescriptor
     */
    private Set<MethodDescriptor> getMethodAllDescriptors(
            EjbDescriptor ejbDesc) {
        Set methodAlls = new HashSet();
        if (ejbDesc.isRemoteInterfacesSupported() ||
            ejbDesc.isRemoteBusinessInterfacesSupported()) {
            methodAlls.add(
                    new MethodDescriptor(MethodDescriptor.ALL_METHODS,
                    "", MethodDescriptor.EJB_REMOTE));
            if (ejbDesc.isRemoteInterfacesSupported()) {
                methodAlls.add(
                    new MethodDescriptor(MethodDescriptor.ALL_METHODS,
                    "", MethodDescriptor.EJB_HOME));
            }
        }

        if (ejbDesc.isLocalInterfacesSupported() ||
                ejbDesc.isLocalBusinessInterfacesSupported()) {
            methodAlls.add(
                    new MethodDescriptor(MethodDescriptor.ALL_METHODS,
                    "", MethodDescriptor.EJB_LOCAL));
            if (ejbDesc.isLocalInterfacesSupported()) {
                methodAlls.add(
                    new MethodDescriptor(MethodDescriptor.ALL_METHODS,
                    "", MethodDescriptor.EJB_LOCALHOME));
            }
        }

        if (ejbDesc.isLocalBean()) {
            methodAlls.add(
                    new MethodDescriptor(MethodDescriptor.ALL_METHODS,
                    "", MethodDescriptor.EJB_LOCAL));    
        }

        if (ejbDesc.hasWebServiceEndpointInterface()) {
            methodAlls.add(
                    new MethodDescriptor(MethodDescriptor.ALL_METHODS,
                    "", MethodDescriptor.EJB_WEB_SERVICE));
        }

        return methodAlls;
    }

    /**
     * @param methodDesc
     * @param ejbDesc
     * @return whether the given methodDesc has permission defined in ejbDesc
     */
    private boolean hasMethodPermissionsFromDD(MethodDescriptor methodDesc,
            EjbDescriptor ejbDesc) {
        HashMap methodPermissionsFromDD = ejbDesc.getMethodPermissionsFromDD();
        if (methodPermissionsFromDD != null) {
            Set allMethods = ejbDesc.getMethodDescriptors();
            String ejbClassSymbol = methodDesc.getEjbClassSymbol();
            for (Object mdObjsObj : methodPermissionsFromDD.values()) {
                List mdObjs = (List)mdObjsObj;
                for (Object mdObj : mdObjs) {
                    MethodDescriptor md = (MethodDescriptor)mdObj;
                    for (Object style3MdObj :
                            md.doStyleConversion(ejbDesc, allMethods)) {
                        MethodDescriptor style3Md = (MethodDescriptor)style3MdObj;
                        if (methodDesc.equals(style3Md)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method checks whether annotations are compatible.
     * One cannot have two or more of the @DenyAll, @PermitAll, @RoleAllowed.
     * In addition, one cannot use @DenyAll with @TransportProtected.
     *
     * @param ainfo
     * @return validity
     */
    private boolean validateAccessControlAnnotations(AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        boolean validity = true;
        AnnotatedElement ae = (AnnotatedElement)ainfo.getAnnotatedElement();

        int count = 0;
        boolean hasDenyAll = false;

        count += (ae.isAnnotationPresent(RolesAllowed.class)? 1 : 0);
        if (ae.isAnnotationPresent(DenyAll.class)) {
            count += 1;
            hasDenyAll = true;
        }

        // continue the checking if not already more than one
        if (count < 2 && ae.isAnnotationPresent(PermitAll.class)) {
            count++;
        }

        if (count > 1) {
            log(Level.SEVERE, ainfo,
                localStrings.getLocalString(
                "enterprise.deployment.annotation.handlers.morethanoneauthannotation",
                "One cannot have more than one of @RolesAllowed, @PermitAll, @DenyAll in the same AnnotatedElement."));
            validity = false;
        }

        if (hasDenyAll && ae.isAnnotationPresent(TransportProtected.class)) {
            log(Level.WARNING, ainfo,
                localStrings.getLocalString(
                "enterprise.deployment.annotation.handlers.warningdenyalltransportprotected",
                "One should not have @DenyAll and @TransportProtected together. The @TransportProtected would be ignored."));
        }

        return validity;
    }

    private void validateClassMethodAccessControlAnnotations(
            AnnotationInfo ainfo, SecurityConstraint typeSecConstr)
            throws AnnotationProcessorException {

        if (typeSecConstr == null) {
            return;
        }

        AnnotatedElement ae = (AnnotatedElement)ainfo.getAnnotatedElement();
        AuthorizationConstraint authConstr = typeSecConstr.getAuthorizationConstraint();
        // should not have @DenyAll and @TransportProtected in class and method together
        if ((typeSecConstr.getUserDataConstraint() != null && ae.isAnnotationPresent(DenyAll.class)) ||
                (authConstr != null && (!authConstr.getSecurityRoles().hasMoreElements()) &&
                 ae.isAnnotationPresent(TransportProtected.class))) {

            log(Level.WARNING, ainfo,
                localStrings.getLocalString(
                "enterprise.deployment.annotation.handlers.warningdenyalltransportprotected",
                "One should not have @DenyAll and @TransportProtected together. The @TransportProtected would be ignored."));
        }
    }

    private void processHttpMethodAnnotation(AnnotationInfo ainfo, 
            WebComponentContext webCompContext, Annotation authAnnotation, String httpMethod)
            throws AnnotationProcessorException {

        WebComponentDescriptor webCompDesc = webCompContext.getDescriptor();

        SecurityConstraint typeSecConstr = webCompContext.getTypeSecurityConstraint();
        if (typeSecConstr != null) {
            validateClassMethodAccessControlAnnotations(ainfo, typeSecConstr);
            //there should be only one
            for (WebResourceCollection wrc : typeSecConstr.getWebResourceCollections()) {
                wrc.addHttpMethodOmission(httpMethod);
                addHttpMethodConstraint(authAnnotation,
                        webCompDesc, httpMethod, wrc.getUrlPatterns(), webCompContext);
            }
        } else {
            addHttpMethodConstraint(authAnnotation, webCompDesc,
                    httpMethod, null, webCompContext);
        }
    }

    private void addAllHttpMethodConstraint(AnnotationInfo ainfo,
            WebComponentContext webCompContext, Annotation authAnnotation) 
            throws AnnotationProcessorException {

        Class clazz = (Class)ainfo.getAnnotatedElement();
        WebComponentDescriptor webCompDesc = webCompContext.getDescriptor();
        //leaf class
        if (clazz.getName().equals(webCompContext.getComponentClassName())) {
            // if it exists, then it must be orthogonal and combine in the same security constraint
            SecurityConstraint securityConstraint =
                    webCompContext.getTypeSecurityConstraint();
        
            if (securityConstraint == null) {
                Set<String> nonOverridedUrlPatterns =
                        webCompContext.getNonOverridedUrlPatterns();

                if (nonOverridedUrlPatterns == null) {
                    nonOverridedUrlPatterns = getNonOverridedUrlPatterns(webCompDesc);
                    webCompContext.setNonOverridedUrlPatterns(nonOverridedUrlPatterns);
                }

                securityConstraint =
                    createSecurityConstraint(webCompDesc, nonOverridedUrlPatterns, null);
                webCompContext.setTypeSecurityConstraint(securityConstraint);
            }

            if (securityConstraint != null) {
                processSecurityConstraint(authAnnotation, securityConstraint, webCompDesc);
            }
        } else { // non-leaf
            for (Method method : webCompDesc.getUserDefinedHttpMethods()) {
                if (method.getDeclaringClass().equals(clazz)) {
                    // check no related annotations for the given method
                    boolean exclude = method.isAnnotationPresent(getAnnotationType());
                    if (!exclude) {
                        for (Class annClass : relatedAnnotationTypes()) {
                            if (method.isAnnotationPresent(annClass)) {
                                exclude = true;
                                break;
                            }
                        }
                    }
                    if (!exclude) {
                         String httpMethod = method.getName().substring(2).toUpperCase();
                         processHttpMethodAnnotation(ainfo, webCompContext, authAnnotation, httpMethod);
                    }
                }
            }
        }
    }

    /**
     * Add a http method constraint according to the given data.
     * If urlPatterns is null, then one use the non overrided url patterns.
     * @param authAnnotation
     * @param webCompDesc
     * @param httpMethod
     * @param urlPatterns
     * @param webCompContext
     */
    private void addHttpMethodConstraint(
            Annotation authAnnotation, WebComponentDescriptor webCompDesc,
            String httpMethod, Set<String> urlPatterns,
            WebComponentContext webCompContext) {

        if (urlPatterns == null) {
            urlPatterns = webCompContext.getNonOverridedUrlPatterns();

            if (urlPatterns == null) {
                urlPatterns = getNonOverridedUrlPatterns(webCompDesc);
                webCompContext.setNonOverridedUrlPatterns(urlPatterns);
            }
        }

        SecurityConstraint securityConstraint =
                webCompContext.getMethodSecurityConstraint(httpMethod);

        if (securityConstraint == null) {
            securityConstraint =
                createSecurityConstraint(webCompDesc, urlPatterns, httpMethod);

            if (securityConstraint != null) {
                SecurityConstraint typeSecurityConstraint = webCompContext.getTypeSecurityConstraint();
                //get authorization and user data constraint from type as it may be omitted in the processing
                if (typeSecurityConstraint != null) {
                    securityConstraint.setAuthorizationConstraint(typeSecurityConstraint.getAuthorizationConstraint());
                    securityConstraint.setUserDataConstraint(typeSecurityConstraint.getUserDataConstraint());
                }
                webCompContext.putMethodSecurityConstraint(httpMethod, securityConstraint);
            }
        }

        if (securityConstraint != null) {
            processSecurityConstraint(authAnnotation, securityConstraint, webCompDesc);
        }
    }

    /**
     * In Servlet 3.0, one can put annotations to only standard doXXX methods:
     *     doDelete, doGet, doHead, doOptions, doPost, doPut, doTrace
     * This method will check whether the given method can have security
     * annotation.
     *
     * @param method
     * @return validty of method for security annotations
     */
    private boolean isValidHttpServletAnnotatedMethod(Method method) {
        boolean valid = false;
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        Class<?>[] parameterTypes = method.getParameterTypes();

        String[] names = new String[] { "doDelete", "doGet", "doHead",
            "doOptions", "doPost", "doPut", "doTrace" };
        for (String name : names) {
            if (methodName.equals(name)) {
                valid = true;
                break;
            }
        }

        valid = valid && (void.class.equals(returnType)) &&
            (parameterTypes.length == 2) &&
            (parameterTypes[0].equals(HttpServletRequest.class) &&
                parameterTypes[1].equals(HttpServletResponse.class));

        return valid;
    }

    /**
     * Given a WebComponentDescriptor, find the set of urlPattern which does not have
     * any existing url pattern in SecurityConstraint
     * @param webCompDesc
     * @return a list of url String
     */
    private Set<String> getNonOverridedUrlPatterns(WebComponentDescriptor webCompDesc) {

        Set<String> nonOverridedUrlPatterns = new HashSet<String>(webCompDesc.getUrlPatternsSet());

        WebBundleDescriptor webBundleDesc = webCompDesc.getWebBundleDescriptor();
        Set<String> urlPatterns = webCompDesc.getUrlPatternsSet();

        Enumeration<SecurityConstraint> eSecConstr = webBundleDesc.getSecurityConstraints();
        while (eSecConstr.hasMoreElements()) {
            SecurityConstraint sc = eSecConstr.nextElement();
            for (WebResourceCollection wrc : sc.getWebResourceCollections()) {
                nonOverridedUrlPatterns.removeAll(wrc.getUrlPatterns());
            }
        }

        return nonOverridedUrlPatterns;
    }

    /**
     * Create a SecurityConstriant for given urlPattern and httpMethod.
     * @param webCompDesc
     * @param urlPattern
     * @param httpMethod
     * @return SecurityConstraint
     */
    private SecurityConstraint createSecurityConstraint(
            WebComponentDescriptor webCompDesc, Set<String> urlPatterns,
            String httpMethod) {

        WebBundleDescriptor webBundleDesc = webCompDesc.getWebBundleDescriptor();
        if (urlPatterns.size() == 0) {
            return null;
        }
        
        SecurityConstraint securityConstraint = new SecurityConstraintImpl();
        WebResourceCollectionImpl webResourceColl = new WebResourceCollectionImpl();
        for (String urlPattern: urlPatterns) {
            webResourceColl.addUrlPattern(urlPattern);
        }

        if (httpMethod != null) {
            webResourceColl.addHttpMethod(httpMethod);
        }

        securityConstraint.addWebResourceCollection(webResourceColl);
        webBundleDesc.addSecurityConstraint(securityConstraint);

        return securityConstraint;
    }
}
