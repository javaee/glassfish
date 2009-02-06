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
import com.sun.enterprise.module.*;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.util.WebServerInfo;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.web.WebDeployer;
import com.sun.logging.LogDomains;
import com.sun.tools.ws.spi.WSToolsObjectFactory;
import com.sun.tools.ws.util.xml.XmlUtil;
import com.sun.xml.bind.api.JAXBRIContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DeploymentUtils;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.enterprise.deploy.shared.ModuleType;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.MessageFormat;


/**
 * Web module deployer. This is loaded from WebservicesContainer
 *
 * @author Bhakti Mehta
 * 
 */
@Service
public class WebServicesDeployer extends WebDeployer {

    protected Logger logger = LogDomains.getLogger(this.getClass(),LogDomains.WEBSERVICES_LOGGER);

    private ResourceBundle rb = logger.getResourceBundle()   ;


   private final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(WebServicesDeployer.class);

    
    /**
     * Constructor
     */
    public WebServicesDeployer() {
    }
    
    protected String getModuleType () {
        return "webservices";
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
                logger.severe(format(rb.getString("failed.loading.dd"),"foo","bar"));

                return false;
            }
            WebBundleDescriptor wbd = (WebBundleDescriptor) app.getStandaloneBundleDescriptor();
            if (!wbd.getSpecVersion().equals("2.5") || (!wbd.hasWebServices())){
                super.generateArtifacts(dc);
            } else {
                generateArtifacts(dc);
                doWebServicesDeployment(app,dc) ;
                saveAppDescriptor(dc);
            }
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

                //For modules this is domains/<domain-name>/generated/xml
                //Check with Hong about j2ee-modules
                File wsdlDir = dc.getScratchDir("xml");
                wsdlDir.mkdir();


                //For modules this is domains/<domain-name>/generated/xml
                //Check with Hong about j2ee-modules
                File stubsDir = dc.getScratchDir("ejb");
                stubsDir.mkdir();
                
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
                    logger.log(Level.SEVERE,"Error in resolving the catalog");
                }
                if (ws.hasWsdlFile()) {
                    // If wsdl file is an http URL, download that WSDL and all embedded relative wsdls, schemas
                    if (ws.getWsdlFileUri().startsWith("http")) {
                        try {
                            downloadWsdlsAndSchemas( new URL(ws.getWsdlFileUri()), wsdlDir);
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
                            String errorMessage =  format(rb.getString("wsdl.notfound"),ws.getWsdlFileUri(),bundle.getModuleDescriptor().getArchiveUri())  ;
                            logger.severe(errorMessage);
                            throw new DeploymentException(errorMessage);

                        }
                    }

                } else {
                    //make required dirs in case they are not present
                    wsdlFileUri = JAXBRIContext.mangleNameToClassName(ws.getName()) + ".wsdl";
                    wsdlDir.mkdirs();
                    wsdlFile = new File(wsdlDir, wsdlFileUri);
                }
                for (WebServiceEndpoint endpoint : ws.getEndpoints()) {

                    String implClassName;
                    boolean jaxwsEndPtFound = false;
                    boolean jaxrpcEndPtFound = false;
                    if (endpoint.implementedByEjbComponent()) {
                        implClassName = endpoint.getEjbComponentImpl().getEjbClassName();
                    } else {
                        implClassName = endpoint.getWebComponentImpl().getWebComponentImplementation();
                    }

                    // check this is NOT a provider interface
                    Class implClass;
                    try {
                        implClass = dc.getClassLoader().loadClass(implClassName);
                    } catch(Exception e) {
                            throw new DeploymentException(format(rb.getString("impl.notfound"),
                                     ws.getName()
                                    , implClassName ,bundle.getModuleDescriptor().getArchiveUri()));
                    }

                    if (implClass!=null) {
                        if(implClass.getAnnotation(javax.xml.ws.WebServiceProvider.class) != null) {
                            // if we already found a jaxrpcendpoint, flag error since we do not support jaxws+jaxrpc endpoint
                            // in the same service
                            if(jaxrpcEndPtFound) {
                                throw new DeploymentException(format(rb.getString("jaxws-jaxrpc.error"),
                                         ws.getName()  ));
                            }
                            //This is a JAXWS endpoint with @WebServiceProvider
                            //Do not run wsgen for this endpoint
                            jaxwsEndPtFound = true;
                            continue;
                        }
                        if(implClass.getAnnotation(javax.jws.WebService.class) != null) {

                            // if we already found a jaxrpcendpoint, flag error since we do not support jaxws+jaxrpc endpoint
                            // in the same service
                            if(jaxrpcEndPtFound) {
                                throw new DeploymentException(format(rb.getString("jaxws-jaxrpc.error"),
                                        ws.getName()  ));
                            }
                            // This is a JAXWS endpoint with @WebService; Invoke wsgen
                            jaxwsEndPtFound = true;

                            String wsgenClassPath = getWsgenClassPath(classesDir, webinfLibDir,
                                dc.getSourceDir().getAbsolutePath()+File.separator+app.getLibraryDirectory(),
                                moduleDir.getAbsolutePath(),app,dc);
                            QName servicename = endpoint.getServiceName();
                            File newstubsdir = new File(stubsDir,servicename.getLocalPart());
                            boolean stubsdircreated = newstubsdir.mkdir();
                           // stubsdircreated?newstubsdir.getCanonicalPath():stubsDir;
                            Thread.currentThread().setContextClassLoader(dc.getClassLoader())  ;
                            boolean wsgenDone =
                                runWsGen(implClassName, wsdlFile.exists(), wsgenClassPath,
                                    stubsDir, wsdlDir, endpoint.getServiceName(), endpoint.getWsdlPort(),dc);
                            if(!wsgenDone) {
                                // wsgen failed; if WSDL file were present, just throw a warning
                                // assuming that the user would have packaged everything
                                if(!wsdlFile.exists()) {
                                    throw new DeploymentException("WSGEN FAILED");
                                } else {
                                    logger.warning(rb.getString("wsgen.failed.cont"));

                                }
                            }
                            try {
                                endpoint.getWebService().setWsdlFileUrl(wsdlFile.toURI().toURL());
                            } catch(java.net.MalformedURLException mue) {
                                throw new DeploymentException(rb.getString("wsgen.failed") , mue);
                            }
                            logger.info(rb.getString("wsgen.success"));
                        } else {
                            // this is a jaxrpc endpoint
                            // if we already found a jaxws endpoint, flag error since we do not support jaxws+jaxrpc endpoint
                            // in the same service
                            if(jaxwsEndPtFound) {
                                throw new DeploymentException(format(rb.getString("jaxws-jaxrpc.error"),
                                                ws.getName()  ));
                            }
                            // Set spec version to 1.1 to indicate later the wscompile should be run
                            // We do this here so that jaxrpc endpoint having J2EE1.4 or JavaEE5
                            // descriptors will work properly
                            jaxrpcEndPtFound = true;
                            ws.getWebServicesDescriptor().setSpecVersion("1.1");
                        }
                    }
                }
            }
        }
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    @Override
    public MetaData getMetaData() {
        return new MetaData(false, null, new Class[] {Application.class});
    }
    private void downloadWsdlsAndSchemas( URL httpUrl, File wsdlDir) throws Exception {
        // First make required directories and download this wsdl file
        wsdlDir.mkdirs();
        String fileName = httpUrl.toString().substring(httpUrl.toString().lastIndexOf("/")+1);
        File toFile = new File(wsdlDir.getAbsolutePath()+File.separator+fileName);
        downloadFile(httpUrl, toFile);

        // Get a list of wsdl and schema relative imports in this wsdl
        HashSet<Import> wsdlRelativeImports = new HashSet<Import>();
        HashSet<Import> schemaRelativeImports = new HashSet<Import>();
        HashSet<Import> wsdlIncludes = new HashSet<Import>();
        HashSet<Import> schemaIncludes = new HashSet<Import>();
        parseRelativeImports(httpUrl, wsdlRelativeImports, wsdlIncludes,
                schemaRelativeImports, schemaIncludes);
        wsdlRelativeImports.addAll(wsdlIncludes);
        schemaRelativeImports.addAll(schemaIncludes);

        // Download all schema relative imports
        String urlWithoutFileName = httpUrl.toString().substring(0, httpUrl.toString().lastIndexOf("/"));
        for(Import next : schemaRelativeImports) {
            String location = next.getLocation();
            location = location.replaceAll("/", "\\"+File.separator);
            if(location.lastIndexOf(File.separator) != -1) {
                File newDir = new File(wsdlDir.getAbsolutePath()+File.separator+
                location.substring(0, location.lastIndexOf(File.separator)));
                newDir.mkdirs();
            }
            downloadFile(new URL(urlWithoutFileName+"/"+next.getLocation()),
                        new File(wsdlDir.getAbsolutePath()+File.separator+location));
        }

        // Download all wsdl relative imports
        for(Import next : wsdlRelativeImports) {
            String newWsdlLocation = next.getLocation();
            newWsdlLocation = newWsdlLocation.replaceAll("/",  "\\"+File.separator);
            File newWsdlDir;
            if(newWsdlLocation.lastIndexOf(File.separator) != -1) {
                newWsdlDir = new File(wsdlDir.getAbsolutePath() + File.separator +
                newWsdlLocation.substring(0, newWsdlLocation.lastIndexOf(File.separator)));
            } else {
                newWsdlDir = wsdlDir;
            }
            downloadWsdlsAndSchemas( new URL(urlWithoutFileName+"/"+next.getLocation()), newWsdlDir);
        }

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
                        throw new DeploymentException(format(rb.getString("catalog.resolver.error"),mappedEntry));
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
            throw new DeploymentException(format(rb.getString("catalog.error"),
                     t.getMessage(),catalogFile.getAbsolutePath()));
        }
      
    }

     public void downloadFile(URL httpUrl, File toFile) throws Exception {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            if(!toFile.createNewFile()) {
                throw new Exception(localStrings.getLocalString("filecreation.error",
                        "Unable to create new File", toFile.getAbsolutePath()));
            }
            is = httpUrl.openStream();

            os = new FileOutputStream(toFile, true);
            int readCount;
            byte[] buffer = new byte[10240]; // Read 10KB at a time
            while(true) {
                readCount = is.read(buffer, 0, 10240);
                if(readCount != -1) {
                    os.write(buffer, 0, readCount);
                } else {
                    break;
                }
            }
        } finally {
            if(is != null) {
                is.close();
            }
            if(os != null) {
                os.flush();
                os.close();
            }
        }
    }

    /**
     * Collect all relative imports from a web service's main wsdl document.
     *
     *@param wsdlFileUrl
     * @param wsdlRelativeImports outupt param in which wsdl relative imports
     * will be added
     *
     *@param schemaRelativeImports outupt param in which schema relative
     * imports will be added
     * @param schemaIncludes output param in which schema includes will be added
     */
    public void parseRelativeImports(URL wsdlFileUrl,
                                      Collection wsdlRelativeImports,
                                      Collection wsdlIncludes,
                                      Collection schemaRelativeImports,
                                       Collection schemaIncludes)
        throws Exception {

        // We will use our little parser rather than using JAXRPC's heavy weight WSDL parser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        InputStream is = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            is = wsdlFileUrl.openStream();
            Document document = builder.parse(is);
            procesSchemaImports(document, schemaRelativeImports);
            procesWsdlImports(document, wsdlRelativeImports);
            procesSchemaIncludes(document, schemaIncludes);
            procesWsdlIncludes(document, wsdlIncludes);
        } catch (SAXParseException spe) {
            // Error generated by the parser
            logger.severe(format(rb.getString("parsing.error"),
                   "" + spe.getLineNumber() ,spe.getSystemId()));
            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null) {
                x = spe.getException();
            }
            x.printStackTrace();
        } catch (Exception sxe) {
            logger.severe(format(rb.getString("wsdl.parsing.error"), sxe.getMessage()));
        } finally {
            try {
                if(is != null) {
                    is.close();
                }
            } catch (IOException io) {
                logger.fine( io.getMessage());
            }
        }
    }

    private void procesSchemaImports(Document document, Collection schemaImportCollection) throws SAXException,
            ParserConfigurationException, IOException {
        NodeList schemaImports =
                document.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
        addImportsAndIncludes(schemaImports, schemaImportCollection, "namespace", "schemaLocation");
    }

    private void procesWsdlImports(Document document, Collection wsdlImportCollection) throws SAXException,
            ParserConfigurationException, IOException {
        NodeList wsdlImports =
                document.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "import");
        addImportsAndIncludes(wsdlImports, wsdlImportCollection, "namespace", "location");
    }

    private void procesSchemaIncludes(Document document, Collection schemaIncludeCollection) throws SAXException,
            ParserConfigurationException, IOException {
        NodeList schemaIncludes =
                document.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
        addImportsAndIncludes(schemaIncludes, schemaIncludeCollection, null, "schemaLocation");
    }

    private void procesWsdlIncludes(Document document, Collection wsdlIncludesCollection) throws SAXException,
            ParserConfigurationException, IOException {
        NodeList wsdlIncludes =
                document.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "include");
        addImportsAndIncludes(wsdlIncludes, wsdlIncludesCollection, null, "location");
    }

    private void addImportsAndIncludes(NodeList list, Collection result,
                        String namespace, String location) throws SAXException,
                        ParserConfigurationException, IOException {
        for(int i=0; i<list.getLength(); i++) {
            String givenLocation = null;
            Node element = list.item(i);
            NamedNodeMap attrs = element.getAttributes();
            Node n= attrs.getNamedItem(location);
            if(n != null) {
                givenLocation = n.getNodeValue();
            }
            if((givenLocation == null) ||
                ((givenLocation != null) && givenLocation.startsWith("http"))) {
                continue;
            }
            Import imp = new Import();
            imp.setLocation(givenLocation);
            if(namespace != null) {
                n = attrs.getNamedItem(namespace);
                if(n != null) {
                    imp.setNamespace(n.getNodeValue());
                }
            }
            result.add(imp);
        }

    }

    private String getWsgenClassPath(File classesDir, String webinfLibDir,
                                     String appLibDirPath, String moduleDir, Application app,DeploymentContext dc) throws DeploymentException {
        // First thing in the classpath is modules' classes directory
        String classpath = classesDir.getAbsolutePath();
        /**
         * JAXWS uses the System.getProperty(java.class.path) to pass on to apt during wsgen
         * In V2 this would have
         * tools.jar, webservices-rt and webservices-api.jar and webservices-tools.jar so there was no issue
         * In V3 the apt cannot see JSR 250, JAXB api and JAXWS apis so I have to pass them
         * explicitly to apt using the classpath option
         * This will be changed after prelude once I move to asm as it is not thoroughly tested right now
         */

        WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
        ModulesRegistry modulesRegistry = wscImpl.getModulesRegistry();
        Collection<Module> modules1 = modulesRegistry.getModules();
        Iterator it= modules1.iterator();

        while(it.hasNext()){
            Module m = (Module) it.next();
            String name = m.getName();
            if (name.equals("com.sun.xml.ws") || name.equals("com.sun.xml.bind") ){
                ModuleDefinition modDef= m.getModuleDefinition();
                java.net.URI[] location = modDef.getLocations();
                classpath+=(File.pathSeparator + new File(location[0]).getAbsolutePath())  ;

            }
        }

        // Next add the Jar files in WEB-INF/lib, if any
        if(webinfLibDir != null) {
            classpath = addJarsToClassPath(classpath, webinfLibDir);
        }

        // Next add the jar files in the EAR level lib directory
        if(appLibDirPath != null) {
            classpath = addJarsToClassPath(classpath, appLibDirPath);
            classpath = addJarsToClassPath(classpath,new File(appLibDirPath).getParentFile().getAbsolutePath());
        }

        //Add expanded modules to classpath
        Set <ModuleDescriptor<BundleDescriptor>> modulesSet = app.getModules();
        Iterator <ModuleDescriptor<BundleDescriptor>> modules ;
        for ( modules = modulesSet.iterator() ; modules.hasNext();) {

            ModuleDescriptor <BundleDescriptor> md =  modules.next();

            String moduleUri = md.getArchiveUri();
            String parentPath = new File(appLibDirPath).getParentFile(
            ).getAbsolutePath();
            String moduleRoot = DeploymentUtils.getEmbeddedModulePath(
                    parentPath, moduleUri);

            classpath = addModuleDirsToClassPath(classpath,moduleRoot);
        }

        // Now add the classpath elements in the modules Manifest entries
        FileInputStream is = null;
        try {
            File mfFile = new File(moduleDir+File.separator+"META-INF"+
                    File.separator+"MANIFEST.MF");
            if(mfFile.exists()) {
                is = new FileInputStream(mfFile);
                Manifest ms = new Manifest(is);
                Attributes attrMap = ms.getMainAttributes();
                String mfCp = attrMap.getValue(Attributes.Name.CLASS_PATH);
                if(mfCp != null && mfCp.length() != 0) {
                    StringTokenizer strTok = new StringTokenizer(mfCp, " \t");
                    while(strTok.hasMoreTokens()) {
                        String givenCP = strTok.nextToken();
                        // Append moduleDir to all relative classPaths
                        if(!givenCP.startsWith(File.separator)) {
                            //Fix for 2629
                            // Based on J2EE spec the referenced jars
                            // in Classpath
                            // must be relative to the referencing jars
                            givenCP = new File(moduleDir).getParent()+File.separator+givenCP;
                        }
                        classpath+=(File.pathSeparator+givenCP);
                    }
                }
            }

        } catch (Exception e) {
            throw new DeploymentException(format(rb.getString("exception.manifest"),
                   e.getMessage() , moduleDir));
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch(IOException t) {
                    logger.fine( t.getMessage());
                }
            }
        }
        return classpath;
    }

    class JarFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".jar"));
        }
    }

    private String addJarsToClassPath(String cp, String dirName) throws DeploymentException {
        try {
            File dir = new File(dirName);
            if(dir.exists()) {
                Iterator fileSetIter = FileUtils.getAllFilesUnder(dir, new JarFilter(), false).iterator();
                while(fileSetIter.hasNext()) {
                    cp+=(File.pathSeparator+((File)fileSetIter.next()).getAbsolutePath());
                }
            }
        } catch (IOException ioex) {
            throw new DeploymentException(format(rb.getString("io.exception"),
                    ioex.getMessage() , dirName));
        }
        return cp;
    }



    private String addModuleDirsToClassPath(String cp, String dirName)  {
        File dir = new File(dirName);
        if (dir.exists()) {
            cp+=File.pathSeparator+ dir.getAbsolutePath();

        }

        return cp;
    }

    private boolean runWsGen(String implClass, boolean skipGenWsdl, String classPath, File stubsDir,
                             File wsdlDir, QName sQname, QName port,DeploymentContext dc) {


       // Thread.currentThread().setContextClassLoader(dc.getClassLoader())      ;
        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add("-cp");
        argsList.add(classPath);
        argsList.add("-keep");
        if(!skipGenWsdl) {
            argsList.add("-wsdl");
            argsList.add("-r");
            argsList.add(wsdlDir.getAbsolutePath());
            argsList.add("-servicename");
            argsList.add(sQname.toString());
            argsList.add("-portname");
            argsList.add(port.toString());
        }


        argsList.add("-d");
        argsList.add(stubsDir.getAbsolutePath());
        argsList.add("-Xdonotoverwrite");
        argsList.add(implClass);
        WSToolsObjectFactory wsTools = WSToolsObjectFactory.newInstance();
        String[] wsgenargs = argsList.toArray(new String[0]);
        try {
            return wsTools.wsgen(System.out, wsgenargs);
        } catch (Exception e ) {
            e.printStackTrace();
            throw new RuntimeException (format(rb.getString("wsgen.rtexception"),
                    e.getMessage()));

        }
    }

    public void doWebServicesDeployment(Application app, DeploymentContext dc)throws Exception{

        Collection webBundles = new HashSet();
        Collection webServices = new HashSet();


        // First collect all web applications and web service descriptors.
        webBundles.addAll( app.getWebBundleDescriptors() );
        webServices.addAll( app.getWebServiceDescriptors() );


        // swap the deployment descriptors context-root with the one
        // provided in the deployment request.
        if (dc.getProps().get("context-root") !=null ) {
            if (app.isVirtual()) {
                String contextRoot = ((String)dc.getProps().get("context-root"));
                ((WebBundleDescriptor) webBundles.iterator().next()).setContextRoot(contextRoot);

            }
        }
        // Swap the application written servlet implementation class for
        // one provided by the container.  The original class is stored
        // as runtime information since it will be used as the servant at
        // dispatch time.

        for(Iterator<WebBundleDescriptor> iter = webBundles.iterator(); iter.hasNext(); ) {
            doWebServiceDeployment(iter.next());
        }
    }

    public void doWebServiceDeployment(WebBundleDescriptor web) throws DeploymentException, MalformedURLException {
        /**
         * Combining code from <code>com.sun.enterprise.deployment.backend.WebServiceDeployer</code>
         * in v2
         */
        Collection endpoints = web.getWebServices().getEndpoints();

        for(Iterator endpointIter = endpoints.iterator();endpointIter.hasNext();) {

            WebServiceEndpoint nextEndpoint = (WebServiceEndpoint)endpointIter.next();
            WebComponentDescriptor webComp = nextEndpoint.getWebComponentImpl();

            if( !nextEndpoint.hasServletImplClass() ) {
                throw new DeploymentException( format(rb.getString(
                        "enterprise.deployment.backend.cannot_find_servlet"),
                        nextEndpoint.getEndpointName()));

            }


            /*if( !nextEndpoint.getWebService().hasFilePublishing() ) {
            // @@@ add security attributes as well????
                String publishingUri = nextEndpoint.getPublishingUri();
                String publishingUrlPattern =publishingUri.charAt(0) == '/') ?publishingUri : "/" + publishingUri + "*//*";
                webComp.addUrlPattern(publishingUrlPattern);

             }*/

            String containerServlet = "org.glassfish.webservices.JAXWSServlet";
            webComp.setWebComponentImplementation(containerServlet);


            /**
             * Now trying to figure the address from <code>com.sun.enterprise.webservice.WsUtil.java</code>
             */
            // Get a URL for the root of the webserver, where the host portion
            // is a canonical host name.  Since this will be used to compose the
            // endpoint address that is written into WSDL, it's better to use
            // hostname as opposed to IP address.
            // The protocol and port will be based on whether the endpoint
            // has a transport guarantee of INTEGRAL or CONFIDENTIAL.
            // If yes, https will be used.  Otherwise, http will be used.
            WebServerInfo wsi = new WsUtil().getWebServerInfo();
            URL rootURL = wsi.getWebServerRootURL(nextEndpoint.isSecure());

            URL actualAddress = nextEndpoint.composeEndpointAddress(rootURL);
            //Ommitting the part of generating the wsdl for now
            //I think we need that to set the endpointAddressURL of WebServiceEndpoint
            logger.info(format(rb.getString("enterprise.deployment.endpoint.registration"),

                          nextEndpoint.getEndpointName(), actualAddress.toString() ));

           
            

        }

    }

    private String format(String key, String ... values){
        return MessageFormat.format(key,values);
    }

}

   

