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

/*import com.sun.ejb.Invocation;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.webservice.monitoring.JAXWSEndpointImpl;
*/

/**
 * Runtime dispatch information about one ejb web service
 * endpoint.  This class must support concurrent access,
 * since a single instance will be used for all web
 * service invocations through the same ejb endpoint.
 * <p><b>NOT THREAD SAFE</b>
 * @author Jerome Dochez
 */
public class EjbRuntimeEndpointInfo {

   /* protected Logger logger = LogDomains.getLogger(this.getClass(),LogDomains.WEBSERVICES_LOGGER);

    private ResourceBundle rb = logger.getResourceBundle()   ;
    
    protected final WebServiceEndpoint endpoint;

    protected final StatelessSessionContainer container;

    protected final Object webServiceEndpointServant;
    
    protected final InvocationManager invManager;
    
    // the variables below are access in non-thread-safe ways
    private ServletAdapter adapter = null;
    private ServletAdapterList adapterList = null;
    
    private WebServiceContextImpl wsCtxt = null;
    private boolean handlersConfigured = false;    
    
    protected EjbMessageDispatcher messageDispatcher = null;

    public EjbRuntimeEndpointInfo(WebServiceEndpoint webServiceEndpoint,
                                  StatelessSessionContainer ejbContainer, 
                                  Object servant) {
                                  
        endpoint = webServiceEndpoint;
        container  = ejbContainer;
        webServiceEndpointServant = servant;

        Switch theSwitch = Switch.getSwitch();
        invManager = theSwitch.getInvocationManager();
    }

    public Container getContainer() {
        return container;
    }

    public WebServiceEndpoint getEndpoint() {
        return endpoint;
    }

    public String getEndpointAddressUri() {
        return endpoint.getEndpointAddressUri();
    }

    public WebServiceContext getWebServiceContext() {
        return wsCtxt;
    }

    public Object prepareInvocation(boolean doPreInvoke)
        throws Exception {

        // For proper injection of handlers, we have to configure handler
        // after invManager.preInvoke but the Invocation.contextData has to be set
        // before invManager.preInvoke. So the steps of configuring jaxws handlers and 
        // init'ing jaxws is done here - this sequence is important
        if (adapter==null) {
            synchronized(this) {
                if(adapter == null) {
                    try {
                        // Set webservice context here
                        // If the endpoint has a WebServiceContext with @Resource then
                        // that has to be used
                        Invocation tmpInv = new Invocation();
                        tmpInv.isWebService = true;
                        tmpInv.container = container;                
                        tmpInv.transactionAttribute = Container.TX_NOT_INITIALIZED;
                        invManager.preInvoke(tmpInv);
                        EjbDescriptor ejbDesc = endpoint.getEjbComponentImpl();
                        Iterator<ResourceReferenceDescriptor> it = ejbDesc.getResourceReferenceDescriptors().iterator();
                        while(it.hasNext()) {
                            ResourceReferenceDescriptor r = it.next();            
                            if(r.isWebServiceContext()) {
                                Iterator<InjectionTarget> iter = r.getInjectionTargets().iterator();
                                boolean matchingClassFound = false;
                                while(iter.hasNext()) {
                                    InjectionTarget target = iter.next();
                                    if(ejbDesc.getEjbClassName().equals(target.getClassName())) {
                                        matchingClassFound = true;
                                        break;
                                    }
                                }
                                if(!matchingClassFound) {
                                    continue;
                                }
                                try {
                                    javax.naming.InitialContext ic = new javax.naming.InitialContext();
                                    wsCtxt = (WebServiceContextImpl) ic.lookup("java:comp/env/" + r.getName());
                                } catch (Throwable t) {
                                    // Swallowed intentionally
                                }
                            }
                        }
                        if(wsCtxt == null) {
                            wsCtxt = new WebServiceContextImpl();
                        }
                    } catch (Throwable t) {
                        logger.severe("Cannot initialize endpoint " + endpoint.getName() + " : error is : " + t.getMessage());
                        return null;
                    } finally {
                        invManager.postInvoke(invManager.getCurrentInvocation());                         
                    }
                }
            }
        }
        
        if(doPreInvoke) {
                // We need to split the preInvoke tasks into stages since handlers
                // need access to java:comp/env and method authorization must take
                // place before handlers are run.  Note that the application 
                // classloader was set much earlier when the invocation first arrived
                // so we don't need to set it here.
                Invocation inv = new Invocation();

                // Do the portions of preInvoke that don't need a Method object.
                inv.isWebService = true;
                inv.container = container;                
                inv.transactionAttribute = Container.TX_NOT_INITIALIZED;

                // If the endpoint has at least one handler, method
                // authorization will be performed by a container-provided handler
                // before any application handler handleRequest methods are called.
                // Otherwise, the ejb container will do the authorization.
                inv.securityPermissions =  Container.SEC_NOT_INITIALIZED;

                // AS per latest spec change, the MessageContext object in WebSvcCtxt
                // should be the same one as used in the ejb's interceptors'        
                inv.setContextData(wsCtxt);
                
                // In all cases, the WebServiceInvocationHandler will do the
                // remaining preInvoke tasks : getContext, preInvokeTx, etc.
                invManager.preInvoke(inv);
        }

        // Now process handlers and init jaxws RI
        if(!handlersConfigured && doPreInvoke) {
            synchronized(this) {
                if(!handlersConfigured) {
                    try {
                        WsUtil wsu = new WsUtil();
                        String implClassName = endpoint.getEjbComponentImpl().getEjbClassName();
                        Class clazz = container.getClassLoader().loadClass(implClassName);

                        // Get the proper binding using BindingID
                        String givenBinding = endpoint.getProtocolBinding();

                        // Get list of all wsdls and schema
                        SDDocumentSource primaryWsdl = null;
                        Collection docs = null;
                        if(endpoint.getWebService().hasWsdlFile()) {
                            BaseManager mgr;
                            if(endpoint.getBundleDescriptor().getApplication().isVirtual()) {
                                mgr = DeploymentServiceUtils.getInstanceManager(DeployableObjectType.EJB);
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
                                primaryWsdl = SDDocumentSource.create(pkgedWsdl.toURL());
                                docs = wsu.getWsdlsAndSchemas(pkgedWsdl);
                            }
                        }

                        // Create a Container to pass ServletContext and also inserting the pipe
                        JAXWSContainer container = new JAXWSContainer(null,
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
                        wsu.configureJAXWSServiceHandlers(endpoint, 
                            endpoint.getProtocolBinding(), binding);

                        // Create the jaxws2.1 invoker and use this
                        Invoker inv = new InstanceResolverImpl(clazz).createInvoker();
                        WSEndpoint wsep = WSEndpoint.create(
                                clazz, // The endpoint class
                                false, // we do not want JAXWS to process @HandlerChain
                                new EjbInvokerImpl(clazz, inv, webServiceEndpointServant, wsCtxt), // the invoker
                                endpoint.getServiceName(), // the service QName
                                endpoint.getWsdlPort(), // the port
                                container,
                                binding, // Derive binding
                                primaryWsdl, // primary WSDL
                                docs, // Collection of imported WSDLs and schema
                                catalogURL
                                );

                        String uri = endpoint.getEndpointAddressUri();
                        String urlPattern = uri.startsWith("/") ? uri : "/" + uri;
                        if(urlPattern.indexOf("/", 1) != -1) {
                            urlPattern = urlPattern.substring(urlPattern.indexOf("/", 1));
                        }

                        // All set; Create the adapter
                        if(adapterList == null) {
                            adapterList = new ServletAdapterList();
                        }
                        adapter = adapterList.createAdapter(endpoint.getName(), urlPattern, wsep);
                        handlersConfigured=true;
                    } catch (Throwable t) {
                        logger.severe("Cannot initialize endpoint " + endpoint.getName() + " : error is : " + t.getMessage());
                        t.printStackTrace();
                        adapter = null;                    
                    }
                }
            }
        }
        return adapter;
    }

    *//**
     * Force initialization of the endpoint runtime information  
     * as well as the handlers injection 
     *//*
    public void initRuntimeInfo(ServletAdapterList list) throws Exception{ 
        try { 
            this.adapterList = list;
            prepareInvocation(true); 
        } finally { 
            invManager.postInvoke(invManager.getCurrentInvocation()); 
        } 
         
    } 
     
    *//**
     * Called after attempt to handle message.  This is coded defensively
     * so we attempt to clean up no matter how much progress we made in
     * getImplementor.  One important thing is to complete the invocation
     * manager preInvoke().
     *//*
    public void releaseImplementor() {
        try {
            Invocation inv = (Invocation) invManager.getCurrentInvocation();

            // Only use container version of postInvoke if we got past
            // assigning an ejb instance to this invocation.  This is
            // because the web service invocation does an InvocationManager
            // preInvoke *before* assigning an ejb instance.  So, we need
            // to ensure that InvocationManager.postInvoke is always
            // called.  It was cleaner to keep this logic in this class
            // and WebServiceInvocationHandler rather than change the
            // behavior of BaseContainer.preInvoke and 
            // BaseContainer.postInvoke.

            if( inv != null ) {
                if( inv.ejb != null ) {
                    container.webServicePostInvoke(inv);
                } else {
                    invManager.postInvoke(inv);
                }
            }
        } catch(Throwable t) {
            logger.log(Level.FINE, "", t);
        }

    }
    
    public EjbMessageDispatcher getMessageDispatcher() {
        if (messageDispatcher==null) {
            messageDispatcher = new Ejb3MessageDispatcher();            
        }
        return messageDispatcher;
    }*/

}
