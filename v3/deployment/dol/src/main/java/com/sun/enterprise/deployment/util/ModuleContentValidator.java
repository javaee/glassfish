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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.UserDataConstraint;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * Allows validation of module content that might involve actually
 * reading the bytes themselves from the module.  Called after
 * descriptor has been loaded but before module-specific archivist
 * is closed.
 *
 * @author Kenneth Saks
 */
public class ModuleContentValidator extends DefaultDOLVisitor {

    private ReadableArchive archive_;
    
    // resources...
    private static LocalStringManagerImpl localStrings =
	    new LocalStringManagerImpl(ModuleContentValidator.class);      

    public ModuleContentValidator(ReadableArchive archive) {
        archive_ = archive;
    }

    public void accept(ServiceReferenceDescriptor serviceRef) {
        if( serviceRef.hasWsdlFile() ) {
            String wsdlFileUri = serviceRef.getWsdlFileUri();
            // For JAXWS based clients, the user need not package WSDL and not refer to it
            // in @WebServiceRef; in this case we rely on @WebClient in generated files to get
            // wsdl location; this wsdl location can be URL or absolute path; in these cases
            // ensure that the file is present and return
            URL url = null;
            try {
                url = new URL(wsdlFileUri);
            } catch(java.net.MalformedURLException e) {
                // don't care, will eventuall fail below
            } 
            if (url!=null) {
                if (url.getProtocol().equals("http") || url.getProtocol().equals("https")) 
                    return;
            }
            File tmpFile = new File(wsdlFileUri);
            if(tmpFile.isAbsolute() && tmpFile.exists()) {
                return;
            }
            try {
                InputStream wsdlFileInputStream = 
                    archive_.getEntry(wsdlFileUri); 
                if( wsdlFileInputStream != null ) {
                    wsdlFileInputStream.close();
                } else {
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.util.wsdlfilenotfound",
                           "wsdl file {0} does not exist for service-ref {1}",
                           new Object[] {wsdlFileUri, serviceRef.getName()});
                    DOLUtils.getDefaultLogger().warning(msg);

                } 
            } catch(IOException ioe) {
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.util.wsdlfilenotreadable",
                           "wsdl file {0}  for service-ref {1} cannot be opened : {2}",
                           new Object[] {wsdlFileUri, serviceRef.getName(), ioe.getMessage()});
                    DOLUtils.getDefaultLogger().warning(msg);
                   
            }
        }
        
        if( serviceRef.hasMappingFile() ) {
            String mappingFileUri = serviceRef.getMappingFileUri();
            try {
                InputStream mappingFileInputStream = 
                    archive_.getEntry(mappingFileUri);
                if( mappingFileInputStream != null ) {
                    mappingFileInputStream.close();
                } else {
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.util.mappingfilenotfound",
                           "mapping file {0} does not exist for service-ref {1}",
                           new Object[] {mappingFileUri, serviceRef.getName()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(msg);
                } 
            } catch(IOException ioe) {
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.util.mappingfilenotreadable",
                           "mapping file {0}  for service-ref {1} cannot be opened : {2}",
                           new Object[] {mappingFileUri, serviceRef.getName(), ioe.getMessage()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(ioe);
            }
        }
    }

    public void accept(WebService webService) {
        
        /*try {
            
            String wsdlFileUri = webService.getWsdlFileUri();
            if (!webService.hasWsdlFile()) {
                // no wsdl was specified in the annotation or deployment descritor, 
                //it will be generated at deployment.
                return;
            }
            try {
                URL url = new URL(wsdlFileUri);
                if (url.getProtocol()!=null && !url.getProtocol().equals("file"))
                    return;
            } catch(java.net.MalformedURLException e) {
                // ignore it could be a relative uri
            }
            InputStream wsdlFileInputStream = archive_.getEntry(wsdlFileUri);

            if( wsdlFileInputStream != null ) {
                
                wsdlFileInputStream.close();
                BundleDescriptor bundle = webService.getBundleDescriptor();
                if( !isWsdlContent(wsdlFileUri, bundle) ) {
                    String msg = localStrings.getLocalString(
                        "enterprise.deployment.util.wsdlpackagedinwrongservicelocation",
                        "wsdl file {0} for web service {1} must be packaged in or below {2}",
                        new Object[] {wsdlFileUri, webService.getName(), bundle.getWsdlDir()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(msg);                  
                }
            } else {
                // let's look in the wsdl directory
                String fullFileUri = webService.getBundleDescriptor().getWsdlDir() + "/" + wsdlFileUri;
                wsdlFileInputStream = archive_.getEntry(fullFileUri);

                if( wsdlFileInputStream != null ) {        
                    // found it, let's update the DOL to not have to recalculate this again
                    wsdlFileInputStream.close();
                    webService.setWsdlFileUri(fullFileUri);
                } else {
                    // this time I give up, no idea where this WSDL is
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.util.servicewsdlfilenotfound",
                           "wsdl file {0} does not exist for web service {1}",
                           new Object[] {wsdlFileUri, webService.getName()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(msg);           
                }
            }*/
            //TODO BM connect with ModuleContentLinkers accept
            try {
                 if("1.1".compareTo(webService.getWebServicesDescriptor().getSpecVersion())<0) {

                    Collection<WebServiceEndpoint> endpoints = webService.getEndpoints();
                    for(WebServiceEndpoint ep : endpoints) {
                        if( ep.implementedByWebComponent() ) {
                            updateServletEndpointRuntime(ep);
                        } else {
                            validateEjbEndpoint(ep);
                        }
                    }

                 } else {
                     jaxrpcWebService(webService);

                }
            } catch(Exception e) {
                RuntimeException ge =new RuntimeException(e.getMessage());
                ge.initCause(e);
                throw ge;
            }

       /* } catch(IOException ioe) {
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.util.servicewsdlfilenotreadable",
                           "wsdl file {0}  for service-ref {1} cannot be opened : {2}",
                           new Object[] {webService.getWsdlFileUri(), webService.getName(), ioe.getMessage()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(ioe);
        }
        
        // For JAXRPC-2.0 based webservice, there is no model file
        // XXX - TODO - This check should be changed to checking the version 
        // once the 2.0 DTDs/Schemas are available
        if(webService.getMappingFileUri() == null) {
            return;
        }

        try {
            InputStream mappingFileInputStream = 
                archive_.getEntry(webService.getMappingFileUri());
            if( mappingFileInputStream != null ) {
                mappingFileInputStream.close();
            } else {
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.util.servicemappingfilenotfound",
                           "Web Service mapping file {0} for web service {1} not found",
                           new Object[] {webService.getMappingFileUri(), webService.getName()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(msg);                
            }
        } catch(IOException ioe) {
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.util.servicemappingfilenotreadable",
                           "Web Service mapping file {0} for web service {1} not found {2} ",
                           new Object[] {webService.getMappingFileUri(), webService.getName(), ioe});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(ioe);                
        }*/
    }

    public void updateServletEndpointRuntime(WebServiceEndpoint endpoint) {

            // Copy the value of the servlet impl bean class into
            // the runtime information.  This way, we'll still
            // remember it after the servlet-class element has been
            // replaced with the name of the container's servlet class.
            endpoint.saveServletImplClass();

            WebComponentDescriptor webComp =
                (WebComponentDescriptor) endpoint.getWebComponentImpl();

            WebBundleDescriptor bundle = webComp.getWebBundleDescriptor();
            WebServicesDescriptor webServices = bundle.getWebServices();
            Collection endpoints =
                webServices.getEndpointsImplementedBy(webComp);

            if( endpoints.size() > 1 ) {
                String msg = "Servlet " + endpoint.getWebComponentLink() +
                    " implements " + endpoints.size() + " web service endpoints " +
                    " but must only implement 1";
                throw new IllegalStateException(msg);
            }

            if( endpoint.getEndpointAddressUri() == null ) {
                Set urlPatterns = webComp.getUrlPatternsSet();
                if( urlPatterns.size() == 1 ) {

                    // Set endpoint-address-uri runtime info to uri.
                    // Final endpoint address will still be relative to context root
                    String uri = (String) urlPatterns.iterator().next();
                    endpoint.setEndpointAddressUri(uri);

                    // Set transport guarantee in runtime info if transport
                    // guarantee is INTEGRAL or CONDIFIDENTIAL for any
                    // security constraint with this url-pattern.
                    Collection constraints =
                        bundle.getSecurityConstraintsForUrlPattern(uri);
                    for(Iterator i = constraints.iterator(); i.hasNext();) {
                        SecurityConstraint next = (SecurityConstraint) i.next();

                        UserDataConstraint dataConstraint =
                            next.getUserDataConstraint();
                        String guarantee = (dataConstraint != null) ?
                            dataConstraint.getTransportGuarantee() : null;

                        if( (guarantee != null) &&
                            ( guarantee.equals
                              (UserDataConstraint.INTEGRAL_TRANSPORT) ||
                              guarantee.equals
                              (UserDataConstraint.CONFIDENTIAL_TRANSPORT) ) ) {
                            endpoint.setTransportGuarantee(guarantee);
                            break;
                        }
                    }
                } else {
                    String msg = localStrings.getLocalString(
		    	   "enterprise.deployment.unassignedaddress",
                           "Endpoint {0} has not been assigned an endpoint address\\n " +
                           "and is associated with servlet {1} , which has  {2} urlPatterns",
                           new Object[] {endpoint.getEndpointName(), webComp.getCanonicalName(), urlPatterns.size()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new IllegalStateException(msg);
                }
            }
        }
    

    /**
     * All wsdl files and wsdl imported files live under a well-known
     * wsdl directory. 
     * @param uri module uri
     */
    public boolean isWsdlContent(String uri, BundleDescriptor bundle) {
        String wsdlDir = bundle.getWsdlDir();
        return (uri != null) && uri.startsWith(wsdlDir);
    }


    public void validateEjbEndpoint(WebServiceEndpoint ejbEndpoint) {
        EjbDescriptor ejbDescriptor = ejbEndpoint.getEjbComponentImpl();
        EjbBundleDescriptor bundle = ejbDescriptor.getEjbBundleDescriptor();
        WebServicesDescriptor webServices = bundle.getWebServices();
        Collection endpoints =
                webServices.getEndpointsImplementedBy(ejbDescriptor);
        if( endpoints.size() == 1 ) {
            if( ejbDescriptor.hasWebServiceEndpointInterface() ) {
                if(!ejbEndpoint.getServiceEndpointInterface().equals
                        (ejbDescriptor.getWebServiceEndpointInterfaceName())) {
                    String msg = "Ejb " + ejbDescriptor.getName() +
                            " service endpoint interface does not match " +
                            " port component " + ejbEndpoint.getEndpointName();
                    throw new IllegalStateException(msg);
                }
            } else {
                String msg = "Ejb " + ejbDescriptor.getName() +
                        " must declare <service-endpoint> interface";
                throw new IllegalStateException(msg);
            }
        }
    }

    private void jaxrpcWebService(WebService webService)
            throws Exception {

        if((webService.getWsdlFileUrl() == null) ||
                (webService.getMappingFileUri() == null)) {
            throw new DeploymentException(localStrings.getLocalString(
                    "enterprise.webservice.jaxrpcFilesNotFound",
                    "Service {0} seems to be a JAXRPC based web service but without "+
                            "the mandatory WSDL and Mapping file. Deployment cannot proceed",
                    new Object[] {webService.getName()}));
        }
        /*ModelInfo modelInfo = createModelInfo(webService);
        String args[] = createJaxrpcCompileArgs(true);

        CompileTool wscompile =
                rpcFactory.createCompileTool(System.out, "wscompile");
        wscompileForWebServices = wscompile;
        WsCompile delegate = new WsCompile(wscompile, webService);
        delegate.setModelInfo(modelInfo);
        wscompile.setDelegate(delegate);

        jaxrpc(args, delegate, webService, files);*/
    }


}
