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

package org.glassfish.webservices.annotation.handlers;

import java.util.Set;
import java.util.StringTokenizer;

import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.Annotation;

import javax.enterprise.deploy.shared.ModuleType;

import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.ProcessingContext;
import org.glassfish.apf.ResultType;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.AnnotationProcessorException;

import org.glassfish.apf.impl.AnnotationUtils;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;

import org.glassfish.apf.context.AnnotationContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;

import javax.xml.namespace.QName;

import org.jvnet.hk2.annotations.Service;

/**
 * This annotation handler is responsible for processing the javax.jws.WebService 
 * annotation type.
 *
 * @author Jerome Dochez
 */
@Service
public class WebServiceProviderHandler extends AbstractHandler implements AnnotationHandler {
    
    /** Creates a new instance of WebServiceHandler */
    public WebServiceProviderHandler() {
    }
        
    public Class<? extends Annotation> getAnnotationType() {
        return javax.xml.ws.WebServiceProvider.class;
    }    

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies() {
        /*Class dependencies[] = { javax.ejb.Stateless.class };
        return dependencies;*/
        return getEjbAndWebAnnotationTypes();
    }
    
    public HandlerProcessingResult processAnnotation(AnnotationInfo annInfo) 
        throws AnnotationProcessorException     
    {
        AnnotatedElementHandler annCtx = annInfo.getProcessingContext().getHandler();
        AnnotatedElement annElem = annInfo.getAnnotatedElement();
        
        // sanity check
        if (!(annElem instanceof Class)) {
            AnnotationProcessorException ape = new AnnotationProcessorException(
                    "@WebServiceProvider can only be specified on TYPE", annInfo);
            annInfo.getProcessingContext().getErrorHandler().error(ape);
            return HandlerProcessingResultImpl.getDefaultResult(getAnnotationType(), ResultType.FAILED);                        
        }             
        // WebServiceProvider MUST implement the provider interface, let's check this
        if (!javax.xml.ws.Provider.class.isAssignableFrom((Class) annElem)) {
            AnnotationProcessorException ape = new AnnotationProcessorException(
                    annElem.toString() + "does not implement the javax.xml.ws.Provider interface", annInfo);
            annInfo.getProcessingContext().getErrorHandler().error(ape);
            return HandlerProcessingResultImpl.getDefaultResult(getAnnotationType(), ResultType.FAILED);                                    
        }
    
        // let's get the main annotation of interest. 
        javax.xml.ws.WebServiceProvider ann = (javax.xml.ws.WebServiceProvider) annInfo.getAnnotation();        
        
        BundleDescriptor bundleDesc = null;
        
        // let's see the type of web service we are dealing with...
        if (annElem.getAnnotation(javax.ejb.Stateless.class)!=null) {
            // this is an ejb !
            EjbContext ctx = (EjbContext) annCtx;
            bundleDesc = ctx.getDescriptor().getEjbBundleDescriptor();
            bundleDesc.setSpecVersion("3.0");
        } else {
             if(annCtx instanceof WebComponentContext) {
                    bundleDesc = ((WebComponentContext)annCtx).getDescriptor().getWebBundleDescriptor();
                } else if ( !(annCtx instanceof WebBundleContext)) {
                    return getInvalidAnnotatedElementHandlerResult(
                            annInfo.getProcessingContext().getHandler(), annInfo);
                }

                bundleDesc = ((WebBundleContext)annCtx).getDescriptor();

                bundleDesc.setSpecVersion("2.5");
        }        

        // For WSProvider, portComponentName is the fully qualified class name
        String portComponentName = ((Class) annElem).getName();
        
        // As per JSR181, the serviceName is either specified in the deployment descriptor
        // or in @WebSErvice annotation in impl class; if neither service name implclass+Service
        String svcName  = ann.serviceName();
        if(svcName == null) {
            svcName = "";
        }

        // Store binding type specified in Impl class
        String userSpecifiedBinding = null;
        javax.xml.ws.BindingType bindingAnn = (javax.xml.ws.BindingType)
                ((Class)annElem).getAnnotation(javax.xml.ws.BindingType.class);
        if(bindingAnn != null) {
            userSpecifiedBinding = bindingAnn.value();
        }
        
        // In case user gives targetNameSpace in the Impl class, that has to be used as
        // the namespace for service, port; typically user will do this in cases where
        // port_types reside in a different namespace than that of server/port.
        // Store the targetNameSpace, if any, in the impl class for later use
        String targetNameSpace = ann.targetNamespace();
        if(targetNameSpace == null) {
            targetNameSpace = "";
        }

        String portName = ann.portName();
        if(portName == null) {
            portName = "";
        }
        
        // Check if the same endpoint is already defined in webservices.xml
        WebServicesDescriptor wsDesc = bundleDesc.getWebServices();
        WebServiceEndpoint endpoint = wsDesc.getEndpointByName(portComponentName);
        WebService newWS;
        if(endpoint == null) {
            // Check if a service with the same name is already present
            // If so, add this endpoint to the existing service
            if (svcName.length()!=0) {
                newWS = wsDesc.getWebServiceByName(svcName);
            } else {
                newWS = wsDesc.getWebServiceByName(((Class)annElem).getSimpleName()+"Service");
            }
            if(newWS==null) {
                newWS = new WebService();
                // service name from annotation
                if (svcName.length()!=0) {
                    newWS.setName(svcName);
                } else {
                    newWS.setName(((Class)annElem).getSimpleName()+"Service");            
                }
                wsDesc.addWebService(newWS);
            }
            endpoint = new WebServiceEndpoint();
            // port-component-name is fully qualified class name
            endpoint.setEndpointName(portComponentName);
            newWS.addEndpoint(endpoint);            
            wsDesc.setSpecVersion(com.sun.enterprise.deployment.node.WebServicesDescriptorNode.SPEC_VERSION);            
        } else {
            newWS = endpoint.getWebService();
        }

        // If wsdl-service is specified in the descriptor, then the targetnamespace
        // in wsdl-service should match the @WebService.targetNameSpace, if any.
        // make that assertion here - and the targetnamespace in wsdl-service, if
        // present overrides everything else
        if(endpoint.getWsdlService() != null) {
            if( (targetNameSpace != null) && (targetNameSpace.length() != 0 ) &&
                (!endpoint.getWsdlService().getNamespaceURI().equals(targetNameSpace)) ) {
                throw new AnnotationProcessorException(
                        "Target Namespace inwsdl-service element does not match @WebService.targetNamespace", 
                        annInfo);
            }
            targetNameSpace = endpoint.getWsdlService().getNamespaceURI();
        }
        
        // Set binding id id @BindingType is specified by the user in the impl class
        if((!endpoint.hasUserSpecifiedProtocolBinding()) &&
                    (userSpecifiedBinding != null) &&
                        (userSpecifiedBinding.length() != 0)){
            endpoint.setProtocolBinding(userSpecifiedBinding);
        }        

        // Use annotated values only if the deployment descriptor equivalent has not been specified        
        if(newWS.getWsdlFileUri() == null) {
            // take wsdl location from annotation
            if (ann.wsdlLocation()!=null && ann.wsdlLocation().length()!=0) {
                newWS.setWsdlFileUri(ann.wsdlLocation());
            }
        }

        annElem = annInfo.getAnnotatedElement();
        
        // we checked that the endpoint implements the provider interface above
        Class clz = (Class) annElem;
        Class serviceEndpointIntf = null;
        for (Class intf : clz.getInterfaces()) {
            if (javax.xml.ws.Provider.class.isAssignableFrom(intf)) {
                serviceEndpointIntf = intf;
                break;
            }
        }
        if (serviceEndpointIntf==null) {
            endpoint.setServiceEndpointInterface("javax.xml.ws.Provider"); 
        } else {
            endpoint.setServiceEndpointInterface(serviceEndpointIntf.getName());
        }

        if (XModuleType.WAR.equals(bundleDesc.getModuleType())) {
            if(endpoint.getServletImplClass() == null) {
                // Set servlet impl class here
                endpoint.setServletImplClass(((Class)annElem).getName());
            }

            // Servlet link name
            WebBundleDescriptor webBundle = (WebBundleDescriptor) bundleDesc;
            if(endpoint.getWebComponentLink() == null) {
                endpoint.setWebComponentLink(portComponentName);
            }
            if(endpoint.getWebComponentImpl() == null) {
                WebComponentDescriptor webComponent = (WebComponentDescriptor) webBundle.
                    getWebComponentByCanonicalName(endpoint.getWebComponentLink());

                // if servlet is not known, we should add it now
                if (webComponent == null) {
                    webComponent = new WebComponentDescriptor();
                    webComponent.setServlet(true);                
                    webComponent.setWebComponentImplementation(((Class) annElem).getCanonicalName());
                    webComponent.setName(endpoint.getEndpointName());
                    webComponent.addUrlPattern("/"+newWS.getName());
                    webBundle.addWebComponentDescriptor(webComponent);
                }
                endpoint.setWebComponentImpl(webComponent);
            }
        } else {
            if(endpoint.getEjbLink() == null) {
                EjbDescriptor[] ejbDescs = ((EjbBundleDescriptor) bundleDesc).getEjbByClassName(((Class)annElem).getName());
                if(ejbDescs.length != 1) {
                    throw new AnnotationProcessorException(
                        "Unable to find matching descriptor for EJB endpoint", 
                        annInfo);                    
                }
                endpoint.setEjbComponentImpl(ejbDescs[0]);
                ejbDescs[0].setWebServiceEndpointInterfaceName(endpoint.getServiceEndpointInterface());
                endpoint.setEjbLink(ejbDescs[0].getName());
            }
        }

        if(endpoint.getWsdlPort() == null) {
            endpoint.setWsdlPort(new QName(targetNameSpace, portName, "ns1"));
        }
        
        if(endpoint.getWsdlService() == null) {
            endpoint.setWsdlService(new QName(targetNameSpace, svcName, "ns1"));
        }
                
        return HandlerProcessingResultImpl.getDefaultResult(getAnnotationType(), ResultType.PROCESSED);
    }
}
