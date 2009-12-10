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

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.*;
import com.sun.logging.LogDomains;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import org.glassfish.webservices.monitoring.Endpoint;
import org.glassfish.webservices.monitoring.WebServiceEngineImpl;
import org.glassfish.webservices.monitoring.WebServiceTesterServlet;
import org.glassfish.api.admin.ServerEnvironment;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.AddressingFeature;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import com.sun.xml.ws.api.server.InstanceResolver;
/**
 * The JAX-WS dispatcher servlet.
 *
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="webservices", probeProviderName="servlet-109")
public class JAXWSServlet extends HttpServlet {

    private static Logger logger = LogDomains.getLogger(JAXWSServlet.class,LogDomains.WEBSERVICES_LOGGER);
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
        synchronized(this) {
            ServletAdapterList list =
                    (ServletAdapterList) getServletContext().getAttribute("ADAPTER_LIST");
            if(list != null) {
                for(ServletAdapter x : list) {
                    x.getEndpoint().dispose();
                }
                getServletContext().removeAttribute("ADAPTER_LIST");
            }
            JAXWSAdapterRegistry.getInstance().removeAdapter(contextRoot);
            /*
            Fix for bug 3932/4052 since the x.getEndpoint().dispose is being
           called above we do not need to call this explicitly
            try {
                (new WsUtil()).doPreDestroy(endpoint, classLoader);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "@PreDestroy lifecycle call failed for service" 
                        + endpoint.getName(), t);
            }*/
            wsEngine_.removeHandler(endpoint);
        }
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException {
        startedEvent(endpoint.getEndpointAddressPath());
        if (("Tester".equalsIgnoreCase(request.getQueryString())) &&
                (!(HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding())))) {
            Endpoint endpt = wsEngine_.getEndpoint(request.getServletPath());
            if (endpt!=null && Boolean.parseBoolean(endpt.getDescriptor().getDebugging())) {
                WebServiceTesterServlet.invoke(request, response,
                        endpt.getDescriptor());
                endedEvent(endpoint.getEndpointAddressPath());
                return;
            }
        }

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
        endedEvent(endpoint.getEndpointAddressPath());
    }

    @Probe(name="startedEvent")
    private void startedEvent(
            @ProbeParam("endpointAddress") String endpointAddress) {

    }

    @Probe(name="endedEvent")
    private void endedEvent(
            @ProbeParam("endpointAddress") String endpointAddress) {

    }

    protected void doGet(HttpServletRequest request,
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
                //TODOBM fixnew WsUtil()).writeInvalidMethodType(response, message);
                return;
            }
            if (endpt!=null && Boolean.parseBoolean(endpt.getDescriptor().getDebugging())) {
                /* ClassLoader loader =  endpt.getDescriptor().getBundleDescriptor().getExtraAttribute("WEBLOADER");
                if (loader != null) {
                    endpt.getDescriptor().getBundleDescriptor().setClassLoader(loader.getClassLoader());
                    endpt.getDescriptor().getBundleDescriptor().removeExtraAttribute("WEBLOADER");
                }*/
                //TODO BM fixe me
                WebServiceTesterServlet.invoke(request, response,
                        endpt.getDescriptor());

                return;
            }
        }

        // If it is not a "Tester request" and it is not a WSDL request,
        // this might be a restful service
        /* TODO BM fix me
       if (!("WSDL".equalsIgnoreCase(request.getQueryString())) &&
              (HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding()))) {
            doPost(request, response);
            return;
        }*/

        // normal WSDL retrieval invocation
        try {
            ServletAdapter targetEndpoint = (ServletAdapter) getEndpointFor(request);
            if (targetEndpoint != null) {
                targetEndpoint.publishWSDL(getServletContext(), request, response);
            } else {
                String message =
                        "Invalid wsdl request " +  request.getRequestURL();
                //(new WsUtil()).writeInvalidMethodType(response, message);
            }
        } catch(Throwable t) {
            ServletException se = new ServletException();
            se.initCause(t);
            throw se;
        }
    }

    private void doInit(ServletConfig servletConfig) throws ServletException {
        String servletName = "unknown";

        try {


            WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
            ComponentEnvManager compEnvManager = wscImpl.getComponentEnvManager();
            JndiNameEnvironment jndiNameEnv = compEnvManager.getCurrentJndiNameEnvironment();
            WebBundleDescriptor webBundle = null;
            if (jndiNameEnv != null && jndiNameEnv instanceof WebBundleDescriptor){
                webBundle = ((WebBundleDescriptor)jndiNameEnv);
            } else {
                throw new WebServiceException("Cannot intialize the JAXWSServlet for " + jndiNameEnv);
            }


            classLoader = Thread.currentThread().getContextClassLoader();
            servletName = servletConfig.getServletName();
            contextRoot = webBundle.getContextRoot();
            WebComponentDescriptor webComponent =
                    webBundle.getWebComponentByCanonicalName(servletName);

            if( webComponent != null ) {
                WebServicesDescriptor webServices = webBundle.getWebServices();
                Collection<WebServiceEndpoint> endpoints =
                        webServices.getEndpointsImplementedBy(webComponent);
                // Only 1 endpoint per servlet is supported, even though
                // data structure implies otherwise. 
                endpoint =  endpoints.iterator().next();

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
            ServletException se = new ServletException();
            se.initCause(t);
            throw se;
        }
    }


    private void registerEndpoint() throws Exception {

        WsUtil wsu = new WsUtil();
        // Complete all the injections that are required
        Class serviceEndpointClass =
                Class.forName(endpoint.getServletImplClass(), true, classLoader);

        // Get the proper binding using BindingID
        String givenBinding = endpoint.getProtocolBinding();

        // Get list of all wsdls and schema
        SDDocumentSource primaryWsdl = null;
        Collection docs = null;
        if(endpoint.getWebService().hasWsdlFile()) {

            //TODO BM figure way for WEB or APP
            /* BaseManager mgr;
            if(endpoint.getBundleDescriptor().getApplication().isVirtual()) {
                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.WEB);
            } else {
                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.APP);
            }*/

            WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
            ServerEnvironment servEnv = wscImpl.getServerEnvironmentImpl();
            String deployedDir = new File(servEnv.getApplicationRepositoryPath().getAbsolutePath(),
                    endpoint.getBundleDescriptor().getApplication().getRegistrationName()).getAbsolutePath();
            
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
        ArrayList<WebServiceFeature> wsFeatures = new ArrayList<WebServiceFeature>();
        // Only if MTOm is enabled create the Binding with the MTOMFeature
        if (mtomEnabled) {
            int mtomThreshold = endpoint.getMtomThreshold() != null ? new Integer(endpoint.getMtomThreshold()):0;
            MTOMFeature mtom = new MTOMFeature(true,mtomThreshold);
            wsFeatures.add(mtom);
        }

        Addressing addressing = endpoint.getAddressing();
        if (addressing != null) {
            AddressingFeature addressingFeature = new AddressingFeature(addressing.isEnabled(),
                    addressing.isRequired(),getResponse(addressing.getResponses()));
            wsFeatures.add(addressingFeature);
        }
        RespectBinding rb = endpoint.getRespectBinding();
        if (rb != null) {
            RespectBindingFeature rbFeature = new RespectBindingFeature(rb.isEnabled());
            wsFeatures.add(rbFeature);
        }
        if (wsFeatures.size()>0){
            binding = BindingID.parse(givenBinding).createBinding(wsFeatures.toArray
                    (new WebServiceFeature[0]));
        } else {
            binding = BindingID.parse(givenBinding).createBinding();
        }

        wsu.configureJAXWSServiceHandlers(endpoint, givenBinding,
                binding);

        // See if it is configured with JAX-WS extension InstanceResolver annotation like
        // @com.sun.xml.ws.developer.servlet.HttpSessionScope or @com.sun.xml.ws.developer.Stateful 
        InstanceResolver ir = InstanceResolver.createFromInstanceResolverAnnotation(serviceEndpointClass);
        //TODO - Implement 109 StatefulInstanceResolver ??
        if ( ir == null) {
            //use our own InstanceResolver that does not call @PostConstuct method before
            //@Resource injections have happened.
            ir = new InstanceResolverImpl(serviceEndpointClass);
        }
        Invoker inv = ir.createInvoker();

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

        //Fix for 6852 Add the ServletAdapter which implements the BoundEndpoint
        // container.addEndpoint(wsep);

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
            container.addEndpoint(adapter);
        }

        registerEndpointUrlPattern(adapter);


    }


    private AddressingFeature.Responses getResponse(String s) {
        if (s != null) {
            return AddressingFeature.Responses.valueOf(AddressingFeature.Responses.class,s);
        } else return AddressingFeature.Responses.ALL;    

    }

    private void registerEndpointUrlPattern(Adapter info) {
        JAXWSAdapterRegistry.getInstance().addAdapter(contextRoot, urlPattern,info);
    }

    private Adapter getEndpointFor(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return JAXWSAdapterRegistry.getInstance().getAdapter(contextRoot, urlPattern, path);
    }
}
