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

package com.sun.enterprise.deployment.backend;

import java.io.File;
import java.io.OutputStream;
import java.io.BufferedOutputStream;

import java.util.Iterator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import java.net.URL;
import javax.servlet.SingleThreadModel;

import javax.xml.ws.http.HTTPBinding;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.io.WebDeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.WebServerInfo;
import com.sun.enterprise.deployment.util.VirtualServerInfo;

import com.sun.enterprise.webservice.WsUtil;

import com.sun.enterprise.diagnostics.util.FileUtils;

import com.sun.enterprise.util.i18n.StringManager;
/**
 * This class is responsible for handling all generation/process
 * that has to happen during web services deployment
 *
 * @author  Jerome Dochez
 */
public class WebServiceDeployer {
    
    private static StringManager localStrings =
        StringManager.getManager( WebServiceDeployer.class );
    
    private DeploymentRequest request;
        
    /** Creates a new instance of WebServiceDeployer */
    public WebServiceDeployer(DeploymentRequest request) {
        this.request = request;
    }
    
    /**
     * We need to replace the servlet-class for each servlet endpoint
     * with a pre-written servlet that has access to the container and
     * the JAXRPC runtime.  The original servlet-class was already saved
     * in the runtime information by j2eec.
     *
     * Also, generate the final WSDL for each web service by filling in
     * the endpoint address field, and publish to client if necessary.
     *
     */
    public void doWebServiceDeployment(Application app, File appDir) throws Exception {

        ClassLoader loader = app.getClassLoader();
        Collection webBundles = new HashSet();
        Collection webServices = new HashSet();

        // First collect all web applications and web service descriptors.
        webBundles.addAll( app.getWebBundleDescriptors() );
        webServices.addAll( app.getWebServiceDescriptors() );
        
        // swap the deployment descriptors context-root with the one 
        // provided in the deployment request.
        if (request.getContextRoot()!=null) {
            if (app.isVirtual()) {
                ((WebBundleDescriptor) webBundles.iterator().next()).setContextRoot(request.getContextRoot());
            }
        }
        
        // Swap the application written servlet implementation class for
        // one provided by the container.  The original class is stored
        // as runtime information since it will be used as the servant at
        // dispatch time.
        for(Iterator<WebBundleDescriptor> iter = webBundles.iterator(); iter.hasNext(); ) {
            doWebServiceDeployment(iter.next(), appDir);
        }
        
        // Generate final wsdls for all web services and store them in
        // the application repository directory.
        for(Iterator<WebService> iter = webServices.iterator(); iter.hasNext(); ) {
            WsUtil wsUtil = new WsUtil();
            WebService next = iter.next();
            
            // Endpoint with HTTP bindings need not have WSDLs; In which case
            // there is no need for WSDL publishing
            if( (next.getWsdlFileUrl() == null) &&
                 (next.getMappingFileUri() == null) ) {
                for(WebServiceEndpoint wsep : next.getEndpoints()) {
                    /*if(!(HTTPBinding.HTTP_BINDING.equals(wsep.getProtocolBinding()))) {
                        throw new Exception(
                            localStrings.getStringWithDefault(
                            "enterprise.webservice.noWsdlError",
                            "Service {0} has an endpoint with non-HTTP binding but there is no WSDL; Deployment cannot proceed",
                            new Object[] {next.getName()}));                   
                    }*/
                    wsep.composeFinalWsdlUrl(wsUtil.getWebServerInfo(request).getWebServerRootURL(wsep.isSecure()));
                }
                continue;
            }

            URL clientPublishLocation = next.getClientPublishUrl();

            // Even if deployer specified a wsdl file 
            // publish location, server can't assume it can access that 
            // file system.  Plus, it's cleaner to depend on a file stored
            // within the application repository rather than one directly
            // exposed to the deployer. Name of final wsdl is derived based
            // on the location of its associated module.  This prevents us
            // from having write the module to disk in order to store the
            // modified runtime info.
            URL url = next.getWsdlFileUrl();
            
            // Create the generated WSDL in the generated directory; for that create the directories first
            File genXmlDir = request.getGeneratedXMLDirectory();
            if(request.isApplication()) {
                // Add module name to the generated xml dir for apps
                String subDirName = next.getBundleDescriptor().getModuleDescriptor().getArchiveUri();
                genXmlDir = new File(genXmlDir, subDirName.replaceAll("\\.", "_"));
            }
            File genWsdlFile = null;
            
            if (!next.hasWsdlFile()) {                
                // no wsdl file was specified at deployment or it was an http location
                // we must have downloaded it or created one when 
                // deploying into the generated directory directly. pick it up from there, 
                // but generate it into a temp 
                genWsdlFile = new File(url.toURI());
                genWsdlFile = File.createTempFile("gen_","", genWsdlFile.getParentFile());
            } else {
                String wsdlFileDir = next.getWsdlFileUri().substring(0, next.getWsdlFileUri().lastIndexOf('/'));
                (new File(genXmlDir, wsdlFileDir)).mkdirs();
                genWsdlFile = new File(genXmlDir, next.getWsdlFileUri());
            }
            wsUtil.generateFinalWsdl(url, next, wsUtil.getWebServerInfo(request), genWsdlFile); 
            
            if (!next.hasWsdlFile()) {
                // Two renaming operations followed by a delete
                // are required because, on windows, a File.delete and 
                // a File.renameTo are not foolproof
                File finalName = new File(url.toURI());
                File tmpName = new File(genWsdlFile.getAbsolutePath() + ".TMP");
                // Rename wsgen generated / downloaded WSDL to .TMP
                boolean renameDone = finalName.renameTo(tmpName);
                if(!renameDone) {
                    // On windows rename operation fails occassionaly;
                    // so use the iostream way to do the rename
                    FileUtils.moveFile(finalName.getAbsolutePath(), tmpName.getAbsolutePath());
                }
                // Rename soap:address fixed WSDL to wsgen generated WSDL
                renameDone = genWsdlFile.renameTo(finalName);
                if(!renameDone) {
                    // On windows rename operation fails occassionaly;
                    // so use the iostream way to do the rename
                    FileUtils.moveFile(genWsdlFile.getAbsolutePath(), finalName.getAbsolutePath());
                }
                // Remove the original WSDL file
                tmpName.delete();
            }
        }                
    }  
    
    /**
     * We need to replace the servlet-class for each servlet endpoint
     * with a pre-written servlet that has access to the container and
     * the JAXRPC runtime.  The original servlet-class was already saved
     * in the runtime information by j2eec.
     *
     * Also, generate the final WSDL for each web service by filling in
     * the endpoint address field, and publish to client if necessary.
     *
     */
    public void doWebServiceDeployment(WebBundleDescriptor web, File outDir) throws Exception {
        
        Collection endpoints = web.getWebServices().getEndpoints();
        ClassLoader loader = web.getClassLoader();
        
        boolean writeModule = !endpoints.isEmpty();
        
        for(Iterator endpointIter = endpoints.iterator();
            endpointIter.hasNext();) {
            
            WebServiceEndpoint nextEndpoint = (WebServiceEndpoint)
            endpointIter.next();
            WebComponentDescriptor webComp = nextEndpoint.getWebComponentImpl();
            String servletImplClass = nextEndpoint.getServletImplClass();
            if( !nextEndpoint.hasServletImplClass() ) {
                throw new Exception(
                localStrings.getStringWithDefault(
                "enterprise.deployment.backend.cannot_find_servlet",
                "Runtime settings error.  Cannot find servlet-impl-class for endpoint {0} ",
                new Object[] {nextEndpoint.getEndpointName()}));
            }
            
            if( !nextEndpoint.getWebService().hasFilePublishing() ) {
                // @@@ add security attributes as well????
                String publishingUri = nextEndpoint.getPublishingUri();
                String publishingUrlPattern =
                (publishingUri.charAt(0) == '/') ?
                publishingUri : "/" + publishingUri + "/*";
                webComp.addUrlPattern(publishingUrlPattern);
            }
            
            Class servletImplClazz  = loader.loadClass(servletImplClass);
            String containerServlet;
            // For versions above 1.1, use JAXWSServlet
            if("1.1".compareTo(web.getWebServices().getSpecVersion())<0) {
                containerServlet = "com.sun.enterprise.webservice.JAXWSServlet";                                    
            } else {
                containerServlet =
                SingleThreadModel.class.isAssignableFrom(servletImplClazz) ?
                "com.sun.enterprise.webservice.SingleThreadJAXRPCServlet" :
                    "com.sun.enterprise.webservice.JAXRPCServlet";
            }
            webComp.setWebComponentImplementation(containerServlet);
        }
    }
}
