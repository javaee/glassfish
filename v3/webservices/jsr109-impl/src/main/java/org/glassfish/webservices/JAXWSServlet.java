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

package org.glassfish.webservices;

//import com.sun.enterprise.Switch;
import com.sun.enterprise.deployment.WebServiceEndpoint;
/*import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.util.InjectionManagerImpl;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.webservice.monitoring.WebServiceEngineImpl;
import com.sun.enterprise.webservice.monitoring.WebServiceTesterServlet;*/
import com.sun.logging.LogDomains;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import org.glassfish.webservices.monitoring.WebServiceEngineImpl;

/**
 * The JAX-WS dispatcher servlet.
 *
 */
public class JAXWSServlet extends HttpServlet {

    private static Logger logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);    
    private WebServiceEndpoint endpoint;    
    private String urlPattern;    
    private String contextRoot;
    private WebServiceEngineImpl wsEngine_;
    private ClassLoader classLoader;
    
    public void init(ServletConfig servletConfig) throws ServletException {
        try {
            super.init(servletConfig);
            wsEngine_ = WebServiceEngineImpl.getInstance();
            // Register endpoints here
            doInit(servletConfig);
        } catch (Throwable e) {
            throw new ServletException(e);
        }
    }

    public void destroy() {
        /*synchronized(this) {
            ServletAdapterList list = 
                    (ServletAdapterList) getServletContext().getAttribute("ADAPTER_LIST");
            if(list != null) {
                for(ServletAdapter x : list) {
                    x.getEndpoint().dispose();
                }
                getServletContext().removeAttribute("ADAPTER_LIST");
            }
            JAXWSAdapterRegistry.getInstance().removeAdapter(contextRoot);
            try {
                (new WsUtil()).doPreDestroy(endpoint, classLoader);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "@PreDestroy lifecycle call failed for service" 
                        + endpoint.getName(), t);
            }
            wsEngine_.removeHandler(endpoint);
        }*/
    }

    /*protected void doPost1(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException {

       *//* if (("Tester".equalsIgnoreCase(request.getQueryString())) &&
             (!(HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding())))) {
            Endpoint endpt = wsEngine_.getEndpoint(request.getServletPath());
            if (endpt!=null && Boolean.parseBoolean(endpt.getDescriptor().getDebugging())) {
                WebServiceTesterServlet.invoke(request, response,
                        endpt.getDescriptor());
                return;
            }
        }        *//*
        
        // lookup registered URLs and get the appropriate adapter;
        // pass control to the adapter
        try {
            ServletAdapter targetEndpoint = (ServletAdapter) getEndpointFor(request);
            if (targetEndpoint != null) {
                targetEndpoint.handle(getServletContext(), request, response);
            } else {
                throw new ServletException("Service not found");
            }
        } catch(Throwable t) {
            ServletException se = new ServletException();
            se.initCause(t);
            throw se;
        } 
    }
    */
     protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
        throws ServletException, IOException {
        
     }


   /* protected void doGet1(HttpServletRequest request,
                         HttpServletResponse response)
        throws ServletException, IOException {
        if (("Tester".equalsIgnoreCase(request.getQueryString())) &&
            (!(HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding())))) {
            
            Endpoint endpt = wsEngine_.getEndpoint(request.getServletPath());
            if((endpt != null) && ((endpt.getDescriptor().isSecure()) ||
               (endpt.getDescriptor().getMessageSecurityBinding() != null)  ||
                endpoint.hasSecurePipeline())) {
                String message = endpt.getDescriptor().getWebService().getName() +
                    "is a secured web service; Tester feature is not supported for secured services";
                (new WsUtil()).writeInvalidMethodType(response, message);                
                return;
            }
            if (endpt!=null && Boolean.parseBoolean(endpt.getDescriptor().getDebugging())) {
                Loader loader = (Loader) endpt.getDescriptor().getBundleDescriptor().getExtraAttribute("WEBLOADER");
                if (loader != null) {
                    endpt.getDescriptor().getBundleDescriptor().setClassLoader(loader.getClassLoader());
                    endpt.getDescriptor().getBundleDescriptor().removeExtraAttribute("WEBLOADER");
                }
                WebServiceTesterServlet.invoke(request, response,
                        endpt.getDescriptor());
                return;
            }
        }
        
        // If it is not a "Tester request" and it is not a WSDL request,
        // this might be a restful service
        if (!("WSDL".equalsIgnoreCase(request.getQueryString())) && 
              (HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding()))) {
            doPost(request, response);
            return;
        }
        
        // normal WSDL retrieval invocation
        try {
            ServletAdapter targetEndpoint = (ServletAdapter) getEndpointFor(request);
            if (targetEndpoint != null) {
                targetEndpoint.publishWSDL(getServletContext(), request, response);
            } else {
                String message = 
                "Invalid wsdl request " +  request.getRequestURL();
                (new WsUtil()).writeInvalidMethodType(response, message);
            }
        } catch(Throwable t) {
            ServletException se = new ServletException();
            se.initCause(t);
            throw se;
        } 
    }*/
     private void doInit(ServletConfig servletConfig) throws ServletException {
       //TODO BM move code from commented method back here
   }
    
    /*private void doInit1(ServletConfig servletConfig) throws ServletException {
        String servletName = "unknown";
        
        try {
            InvocationManager invManager = 
                Switch.getSwitch().getInvocationManager();
            ComponentInvocation inv = invManager.getCurrentInvocation();
            Object containerContext = inv.getContainerContext();

            WebBundleDescriptor webBundle = (WebBundleDescriptor)
                Switch.getSwitch().getDescriptorFor(containerContext);
            classLoader = Thread.currentThread().getContextClassLoader();
            servletName = servletConfig.getServletName();
            contextRoot = webBundle.getContextRoot();
            WebComponentDescriptor webComponent = 
                webBundle.getWebComponentByCanonicalName(servletName);

            if( webComponent != null ) {
                WebServicesDescriptor webServices = webBundle.getWebServices();
                Collection endpoints =                     
                    webServices.getEndpointsImplementedBy(webComponent);
                // Only 1 endpoint per servlet is supported, even though
                // data structure implies otherwise. 
                endpoint = (WebServiceEndpoint) endpoints.iterator().next();

                // need to invoke the endpoint lifecylcle 
                if(!(HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding()))) {
                    // Doing this so that restful service are not monitored
                    wsEngine_.createHandler(endpoint);
                }
                registerEndpoint();
            } else {
                throw new ServletException(servletName + " not found");
            }
        } catch(Throwable t) {
            logger.log(Level.WARNING, "Servlet web service endpoint '" +
                       servletName + "' failure", t);
	    t.printStackTrace();
            ServletException se = new ServletException();
            se.initCause(t);
            throw se;
        }        
    }*/

   /* private void registerEndpoint() throws Exception {

        WsUtil wsu = new WsUtil();
        // Complete all the injections that are required
        Class serviceEndpointClass = 
                Class.forName(endpoint.getServletImplClass(), true, classLoader);
        *//*
        Object serviceEndpoint = serviceEndpointClass.newInstance();
        new InjectionManagerImpl().injectInstance(serviceEndpoint);

        // Set webservice context here
        // If the endpoint has a WebServiceContext with @Resource then
        // that has to be used
        WebServiceContextImpl wsc = null;
        WebBundleDescriptor bundle = (WebBundleDescriptor)endpoint.getBundleDescriptor();
        Iterator<ResourceReferenceDescriptor> it = bundle.getResourceReferenceDescriptors().iterator();
        while(it.hasNext()) {
            ResourceReferenceDescriptor r = it.next();            
            if(r.isWebServiceContext()) {
                Iterator<InjectionTarget> iter = r.getInjectionTargets().iterator();
                boolean matchingClassFound = false;
                while(iter.hasNext()) {
                    InjectionTarget target = iter.next();
                    if(endpoint.getServletImplClass().equals(target.getClassName())) {
                        matchingClassFound = true;
                        break;
                    }
                }
                if(!matchingClassFound) {
                    continue;
                }
                try {
                    javax.naming.InitialContext ic = new javax.naming.InitialContext();
                    wsc = (WebServiceContextImpl) ic.lookup("java:comp/env/" + r.getName());
                } catch (Throwable t) {
                    // Swallowed intentionally
                }
            }
        }
        if(wsc == null) {
            wsc = new WebServiceContextImpl();
        }
         *//*

        // Get the proper binding using BindingID
        String givenBinding = endpoint.getProtocolBinding();

        // Get list of all wsdls and schema
        SDDocumentSource primaryWsdl = null;
        Collection docs = null;
        if(endpoint.getWebService().hasWsdlFile()) {
            BaseManager mgr;
            if(endpoint.getBundleDescriptor().getApplication().isVirtual()) {
                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.WEB);
            } else {
                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.APP);
            }
            String deployedDir = 
                mgr.getLocation(endpoint.getBundleDescriptor().getApplication().getRegistrationName());
            File pkgedWsdl = null;
            if(deployedDir != null) {
                if(endpoint.getBundleDescriptor().getApplication().isVirtual()) {
                    pkgedWsdl = new File(deployedDir+File.separator+
                                endpoint.getWebService().getWsdlFileUri());
                } else {
                    pkgedWsdl = new File(deployedDir+File.separator+
                            endpoint.getBundleDescriptor().getModuleDescriptor().getArchiveUri().replaceAll("\\.", "_") +
                            File.separator + endpoint.getWebService().getWsdlFileUri());
                }
            } else {
                pkgedWsdl = new File(endpoint.getWebService().getWsdlFileUrl().getFile());
            }
            if(pkgedWsdl.exists()) {
                //Canonicalize the filename.  Since getWsdlsAndSchemas canonicalizes
                //the filenames of the metatdata documents, JAXWS might get into have
                //trouble detecting common root paths.
                //ie C://foo.wsdl and c://schema.wsdl
                pkgedWsdl = pkgedWsdl.getCanonicalFile();
                
                primaryWsdl = SDDocumentSource.create(pkgedWsdl.toURL());
                docs = wsu.getWsdlsAndSchemas(pkgedWsdl);
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.INFO, "Creating endpoint with packaged WSDL " + 
                            primaryWsdl.getSystemId().toString());
                    logger.log(Level.FINE, "Metadata documents:");
                    for (Object source: docs) {
                        logger.log(Level.FINE, ((SDDocumentSource)source).getSystemId().toString());
                    }
                }
            }
        }

        // Create a Container to pass ServletContext and also inserting the pipe
        JAXWSContainer container = new JAXWSContainer(getServletContext(),
                endpoint);
        
        // Get catalog info
        java.net.URL catalogURL = null;
        File catalogFile = new File(endpoint.getBundleDescriptor().getDeploymentDescriptorDir() +
                File.separator + "jax-ws-catalog.xml");
        if(catalogFile.exists()) {
            catalogURL = catalogFile.toURL();
        }

        // Create Binding and set service side handlers on this binding
        boolean mtomEnabled = wsu.getMtom(endpoint);
        WSBinding binding = null;
        // Only if MTOm is enabled create the Binding with the MTOMFeature
        if (mtomEnabled) {
            MTOMFeature mtom = new MTOMFeature(true);
            binding = BindingID.parse(givenBinding).createBinding(mtom);
        } else {
            binding = BindingID.parse(givenBinding).createBinding();
        }
             
        wsu.configureJAXWSServiceHandlers(endpoint, givenBinding, 
                binding);

        // Create the jaxws2.1 invoker and use this
        Invoker inv;
	if (serviceEndpointClass.getAnnotation(Stateful.class) == null) {
		//use our own InstanceResolver that does not call @PostConstuct method before
		//@Resource injections have happened.
		inv = (new InstanceResolverImpl(serviceEndpointClass)).createInvoker();
	} else {
		//let JAX-WS handle the Stateful WebService case
		//TODO - Implement 109 StatefulInstanceResolver
		inv = null;
	}
        
         
        WSEndpoint wsep = WSEndpoint.create(
                serviceEndpointClass, // The endpoint class
                false, // we do not want JAXWS to process @HandlerChain
                inv, 
                endpoint.getServiceName(), // the service QName
                endpoint.getWsdlPort(), // the port
                container, // Our container with info on security/monitoring pipe 
                binding, // Derive binding
                primaryWsdl, // primary WSDL
                docs, // Collection of imported WSDLs and schema
                catalogURL
                );
        
        container.addEndpoint(wsep);

        // For web components, this will be relative to the web app
        // context root.  Make sure there is a leading slash.
        String uri = endpoint.getEndpointAddressUri();
        urlPattern = uri.startsWith("/") ? uri : "/" + uri;

        // The whole web app should have a single adapter list 
        // This is to enable JAXWS publish WSDLs with proper addresses
        ServletAdapter adapter;
        synchronized(this) {
            ServletAdapterList list = 
                (ServletAdapterList) getServletContext().getAttribute("ADAPTER_LIST");
            if(list == null) {
                list = new ServletAdapterList();
                getServletContext().setAttribute("ADAPTER_LIST", list);
            }
            adapter = 
                list.createAdapter(endpoint.getName(), urlPattern, wsep);
        }

        registerEndpointUrlPattern(adapter);

        *//*
        wsu.doPostConstruct(wsep.getImplementationClass(),
                serviceEndpoint);
         *//*
    }   */

   /* private void registerEndpointUrlPattern(Adapter info) {
        JAXWSAdapterRegistry.getInstance().addAdapter(contextRoot, urlPattern,info);
    }
    
    private Adapter getEndpointFor(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return JAXWSAdapterRegistry.getInstance().getAdapter(contextRoot, urlPattern, path);
    }*/
}
