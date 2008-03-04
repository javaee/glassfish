/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.webservices;


import com.sun.enterprise.deployment.*;
import com.sun.logging.LogDomains;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.deployment.common.DeploymentException;
import org.jvnet.hk2.annotations.Service;

import javax.enterprise.deploy.shared.ModuleType;
import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.tools.ws.util.xml.XmlUtil;


/**
 * Web module deployer. This is loaded from WebservicesContainer
 *
 * @author Bhakti Mehta
 * 
 */
@Service
public class WebServicesDeployer extends SimpleDeployer {


    /**
     * Constructor
     */
    public WebServicesDeployer() {
    }
    

    protected String getModuleType () {
        return "webservices";
    }



    protected RootDeploymentDescriptor getDefaultBundleDescriptor() {
        return null;
    }

    protected void cleanArtifacts(DeploymentContext deploymentContext) throws DeploymentException {
        //TODO BM clean
    }


    

    /**
     * Prepares the application bits for running in the application server.
     * For certain cases, this is exploding the jar file to a format the
     * ContractProvider instance is expecting, generating non portable
     * artifacts and other application specific tasks.
     * Failure to prepare should throw an exception which will cause the overall
     * deployment to fail.
     *
     * @param dc deployment context
     * @return true if the prepare phase was successful
     *
     */
    @Override
    public boolean prepare(DeploymentContext dc) {
        try {
            Application app = dc.getModuleMetaData(Application.class);

            if (app==null) {
                // hopefully the DOL gave a good message of the failure...
                dc.getLogger().severe("Failed to load deployment descriptor, aborting");
                return false;
            }
            generateArtifacts(dc);
           // createClientJar(dc);
            return true;
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            RuntimeException re = new RuntimeException(ex.getMessage());
            re.initCause(ex);
            throw re;
        }
    }




    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {
        Application app = dc.getModuleMetaData(Application.class);
        Set<BundleDescriptor> bundles = app.getBundleDescriptors();
        for(BundleDescriptor bundle : bundles) {
            WebServicesDescriptor wsDesc = bundle.getWebServices();
            for (WebService ws : wsDesc.getWebServices()) {

                // for modules this is domains/<domain-name>/j2ee-modules/<module-name>
                // for apps this is domains/<domain-name>/j2ee-apps/<app-name>/<foo_war> (in case of embedded wars)
                //  or domains/<domain-name>/j2ee-apps/<app-name>/<foo_jar> (in case of embedded jars)
                File moduleDir = dc.getSourceDir();
                File wsdlDir = dc.getScratchDir("xml");

                /** TODO BM implement later
                if(!dc.getProps.getProperty("type").equals("web")) {
                    String subDirName = DeploymentUtils.getRelativeEmbeddedModulePath(moduleDir.getAbsolutePath(), bundle.getModuleDescriptor().getArchiveUri());

                    moduleDir =new File(moduleDir, subDirName);
                    wsdlDir =new File( wsdlDir,subDirName);

                }**/

                File classesDir;
                String webinfLibDir = null;
                if (ModuleType.WAR.equals(bundle.getModuleType())) {
                    classesDir = new File(moduleDir, "WEB-INF"+File.separator+"classes");
                    webinfLibDir = moduleDir.getAbsolutePath() + File.separator + "WEB-INF"+File.separator+"lib";
                } else if (ModuleType.EJB.equals(bundle.getModuleType())) {
                    classesDir = moduleDir;
                } else {
                    // unknown module type with @WebService, just ignore...
                    continue;
                }

                wsdlDir = new File(wsdlDir, bundle.getWsdlDir().replaceAll("/", "\\"+File.separator));

                // Check if catalog file is present, if so get mapped WSDLs
                String wsdlFileUri;
                File wsdlFile;
                try {
                    checkCatalog(bundle, ws, moduleDir);
                } catch (DeploymentException e) {
                    LogDomains.getLogger(LogDomains.WEB_LOGGER).log(Level.SEVERE,"Error in resolving the catalog");
                }
                if (ws.hasWsdlFile()) {
                    // If wsdl file is an http URL, download that WSDL and all embedded relative wsdls, schemas
                    if (ws.getWsdlFileUri().startsWith("http")) {
                        try {
                            downloadWsdlsAndSchemas(ws, new URL(ws.getWsdlFileUri()), wsdlDir);
                        } catch(Exception e) {
                            throw new DeploymentException(e.toString(), e);
                        }
                        wsdlFileUri = ws.getWsdlFileUri().substring(ws.getWsdlFileUri().lastIndexOf("/")+1);
                        wsdlFile = new File(wsdlDir, wsdlFileUri);

                        // at this point, we don't care we got it from and it simplifies
                        // the rest of the deployment process to just think that is was
                        // generated during deployment
                        // ws.setWsdlFileUri(null);

                    } else {
                        wsdlFileUri = ws.getWsdlFileUri();
                        if(wsdlFileUri.startsWith("/")) {
                            wsdlFile = new File(wsdlFileUri);
                        } else {
                            wsdlFile = new File(moduleDir, wsdlFileUri);
                        }
                        if (!wsdlFile.exists()) {
                            throw new DeploymentException("WebService " + ws.getName() + " wsdl file "
                                    + ws.getWsdlFileUri() + " not found in archive "
                                    + bundle.getModuleDescriptor().getArchiveUri());
                        }
                    }

                } else {
                    //make required dirs in case they are not present
                    wsdlFileUri = JAXBRIContext.mangleNameToClassName(ws.getName()) + ".wsdl";
                    wsdlDir.mkdirs();
                    wsdlFile = new File(wsdlDir, wsdlFileUri);
                }
            }
        }
    }

    private void downloadWsdlsAndSchemas(WebService ws, URL httpUrl, File wsdlDir) throws Exception {

    }
    // If catalog file is present, get the mapped WSDL for given WSDL and replace the value in
    // the given WebService object
    private void checkCatalog(BundleDescriptor bundle, WebService ws, File moduleDir)
                            throws DeploymentException {
        // If no catalog file is present, return
        File catalogFile = new File(moduleDir,
                bundle.getDeploymentDescriptorDir() +
                File.separator + "jax-ws-catalog.xml");
        if(!catalogFile.exists()) {
            return;
        }
        resolveCatalog(catalogFile, ws.getWsdlFileUri(), ws);
    }

    public URL resolveCatalog(File catalogFile, String wsdlFile, WebService ws) throws DeploymentException {

        try {


           URL retVal = null;
            // Get an entity resolver
            org.xml.sax.EntityResolver resolver =
                    XmlUtil.createEntityResolver(catalogFile.toURL());
            org.xml.sax.InputSource source = resolver.resolveEntity(null, wsdlFile);
            if(source != null) {
                String mappedEntry = source.getSystemId();
                // For entries with relative paths, Entity resolver always
                // return file://<absolute path
                if(mappedEntry.startsWith("file:")) {
                    File f = new File(mappedEntry.substring(mappedEntry.indexOf(":")+1));
                    if(!f.exists()) {
                        throw new DeploymentException("File " + mappedEntry + " not found");
                    }
                    retVal = f.toURI().toURL();
                    if(ws != null) {
                        ws.setWsdlFileUri(f.getAbsolutePath());
                        ws.setWsdlFileUrl(retVal);
                    }
                } else if(mappedEntry.startsWith("http")) {
                    retVal = new URL(mappedEntry);
                    if(ws != null) {
                        ws.setWsdlFileUrl(retVal);
                    }
                }
            }
            return retVal;

        } catch (Throwable t) {
            throw new DeploymentException("Exception while processing catalog "
                    + catalogFile.getAbsolutePath() + "; Reason " + t.getMessage());
        }
      
    }


    


}
