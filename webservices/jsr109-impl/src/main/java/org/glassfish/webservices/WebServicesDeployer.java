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
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.web.WebDeployer;
import com.sun.enterprise.web.WebApplication;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Module;
import com.sun.logging.LogDomains;
import com.sun.tools.ws.spi.WSToolsObjectFactory;
import com.sun.tools.ws.util.xml.XmlUtil;
import com.sun.xml.bind.api.JAXBRIContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.container.Container;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
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
import javax.servlet.ServletException;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Web module deployer. This is loaded from WebservicesContainer
 *
 * @author Bhakti Mehta
 * 
 */
@Service
public class WebServicesDeployer extends WebDeployer {

    protected Logger logger = Logger.getLogger(WebServicesDeployer.class.getName());

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
                dc.getLogger().severe(localStrings.getLocalString("failed.loading.dd",
                        "Failed to load deployment descriptor, aborting"));
                return false;
            }
            WebBundleDescriptor wbd = (WebBundleDescriptor) app.getStandaloneBundleDescriptor();
            if (!wbd.getSpecVersion().equals("2.5") ){
                super.generateArtifacts(dc);
            } else {
                generateArtifacts(dc);
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
                            throw new DeploymentException(localStrings.getLocalString("wsdl.notfound",
                                    "WebService wsdl file not found in archive", ws.getName()
                                    , ws.getWsdlFileUri(),  bundle.getModuleDescriptor().getArchiveUri()));
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
                            throw new DeploymentException(localStrings.getLocalString("impl.notfound",
                                    "WebService {0} implementation {1} not found in archive {2}" , ws.getName()
                                    , implClassName ,bundle.getModuleDescriptor().getArchiveUri()));
                    }

                    if (implClass!=null) {
                        if(implClass.getAnnotation(javax.xml.ws.WebServiceProvider.class) != null) {
                            // if we already found a jaxrpcendpoint, flag error since we do not support jaxws+jaxrpc endpoint
                            // in the same service
                            if(jaxrpcEndPtFound) {
                                throw new DeploymentException(localStrings.getLocalString("jaxws-jaxrpc.error",
                                        "WebService {0} has a JAXWS and a JAXRPC endpoint; this is not supported now",
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
                                throw new DeploymentException(localStrings.getLocalString("jaxws-jaxrpc.error",
                                        "WebService {0} has a JAXWS and a JAXRPC endpoint; this is not supported now",
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
                                    logger.log(Level.WARNING,
                                       localStrings.getLocalString("wsgen.failed.cont",
                                               "wsgen failed- proceeding under the assumption" +
                                                " that the user packaged all required objects properly"));
                                }
                            }
                            try {
                                endpoint.getWebService().setWsdlFileUrl(wsdlFile.toURI().toURL());
                            } catch(java.net.MalformedURLException mue) {
                                throw new DeploymentException(localStrings.getLocalString("wsgen.failed","WSGEN Failed") , mue);
                            }
                            logger.log(Level.INFO, localStrings.getLocalString("wsgen.success","wsgen successful"));
                        } else {
                            // this is a jaxrpc endpoint
                            // if we already found a jaxws endpoint, flag error since we do not support jaxws+jaxrpc endpoint
                            // in the same service
                            if(jaxwsEndPtFound) {
                                throw new DeploymentException(localStrings.getLocalString("jaxws-jaxrpc.error",
                                        "WebService {0} has a JAXWS and a JAXRPC endpoint; this is not supported now",
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
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    @Override
    public MetaData getMetaData() {

        List<ModuleDefinition> apis = new ArrayList<ModuleDefinition>();       
        Module module = modulesRegistry.makeModuleFor("org.glassfish:javax.javaee",null);
        if (module!=null) {
            apis.add(module.getModuleDefinition());
        }

        String[] otherExportedPackages = new String[] {
                 "org.glassfish.webservices:jsr109-impl",
                 "org.glassfish.web:webtier" ,
                 "com.sun.xml.ws:webservices-rt",
                 "com.sun.tools.ws:webservices-tools",
                 "javax.xml:webservices-api"

                 };

        for (String otherExportedPackage : otherExportedPackages) {
            module = modulesRegistry.makeModuleFor(otherExportedPackage, null);
            if (module != null) {
                apis.add(module.getModuleDefinition());
            } else {
                //module is null
                logger.log(Level.WARNING,localStrings.getLocalString("module.load.error","Error loading the module {0}",otherExportedPackage));
            }
        }

        return new MetaData(false, apis.toArray(new ModuleDefinition[apis.size()]), new Class[] { Application.class }, null );
    }
    private void downloadWsdlsAndSchemas(WebService ws, URL httpUrl, File wsdlDir) throws Exception {
        // First make required directories and download this wsdl file
        wsdlDir.mkdirs();
        String fileName = httpUrl.toString().substring(httpUrl.toString().lastIndexOf("/")+1);
        File toFile = new File(wsdlDir.getAbsolutePath()+File.separator+fileName);
        downloadFile(httpUrl, toFile);

        // Get a list of wsdl and schema relative imports in this wsdl
        Collection<Import> wsdlRelativeImports = new HashSet();
        Collection<Import> schemaRelativeImports = new HashSet();
        Collection<Import> wsdlIncludes = new HashSet();
        Collection<Import> schemaIncludes = new HashSet();
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
            downloadWsdlsAndSchemas(ws, new URL(urlWithoutFileName+"/"+next.getLocation()), newWsdlDir);
        }
        return;
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
            throw new DeploymentException(localStrings.getLocalString("catalog.error",
                    "Exception while processing catalog {0} Reason " + t.getMessage(),catalogFile.getAbsolutePath()));
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
     *@param wsdlRelativeImports outupt param in which wsdl relative imports
     * will be added
     *
     *@param schemaRelativeImports outupt param in which schema relative
     * imports will be added
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
            logger.log(Level.SEVERE,localStrings.getLocalString("parsing.error","Parsing error  line {0}, uri {1}",
                spe.getLineNumber() ,spe.getSystemId()));
            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null) {
                x = spe.getException();
            }
            x.printStackTrace();
        } catch (Exception sxe) {
            logger.log(Level.SEVERE, localStrings.getLocalString("wsdl.parsing.error","Error parsing WSDL {0}"
                    , sxe.getMessage()));
        } finally {
            try {
                if(is != null) {
                    is.close();
                }
            } catch (IOException io) {
                logger.log(Level.FINE, io.getMessage());
            }
        }
    }

    private void procesSchemaImports(Document document, Collection schemaImportCollection) throws SAXException,
            ParserConfigurationException, IOException, SAXParseException {
        NodeList schemaImports =
                document.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
        addImportsAndIncludes(schemaImports, schemaImportCollection, "namespace", "schemaLocation");
    }

    private void procesWsdlImports(Document document, Collection wsdlImportCollection) throws SAXException,
            ParserConfigurationException, IOException, SAXParseException {
        NodeList wsdlImports =
                document.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "import");
        addImportsAndIncludes(wsdlImports, wsdlImportCollection, "namespace", "location");
    }

    private void procesSchemaIncludes(Document document, Collection schemaIncludeCollection) throws SAXException,
            ParserConfigurationException, IOException, SAXParseException {
        NodeList schemaIncludes =
                document.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
        addImportsAndIncludes(schemaIncludes, schemaIncludeCollection, null, "schemaLocation");
    }

    private void procesWsdlIncludes(Document document, Collection wsdlIncludesCollection) throws SAXException,
            ParserConfigurationException, IOException, SAXParseException {
        NodeList wsdlIncludes =
                document.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "include");
        addImportsAndIncludes(wsdlIncludes, wsdlIncludesCollection, null, "location");
    }

    private void addImportsAndIncludes(NodeList list, Collection result,
                        String namespace, String location) throws SAXException,
                        ParserConfigurationException, IOException, SAXParseException {
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
        return;
    }

    private String getWsgenClassPath(File classesDir, String webinfLibDir,
                                     String appLibDirPath, String moduleDir, Application app,DeploymentContext dc) throws DeploymentException {
        // First thing in the classpath is modules' classes directory
        String classpath = classesDir.getAbsolutePath();

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
            ClassLoader classloader = dc.getClassLoader()   ;
            if (classloader instanceof URLClassLoader) {
               URL[] urls =((URLClassLoader)classloader).getURLs();
            }
        } catch (Exception e) {
            throw new DeploymentException(localStrings.getLocalString("exception.manifest",
                    "Exception : {0} when trying to process MANIFEST file under {1}",e.getMessage() , moduleDir));
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch(IOException t) {
                    logger.log(Level.FINE, t.getMessage());
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
                Iterator fileSetIter = com.sun.enterprise.deployment.util.FileUtil.getAllFilesUnder(dir, new JarFilter(), false).iterator();
                while(fileSetIter.hasNext()) {
                    cp+=(File.pathSeparator+((File)fileSetIter.next()).getAbsolutePath());
                }
            }
        } catch (IOException ioex) {
            throw new DeploymentException(localStrings.getLocalString("io.exception",
                    "IOException : {0} when trying to get list of files under {1}",ioex.getMessage()
                    , dirName));
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


        Thread.currentThread().setContextClassLoader(dc.getClassLoader())      ;
        ArrayList argsList = new ArrayList();
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
        String[] wsgenargs = (String[]) argsList.toArray(new String[0]);
        try {
            return wsTools.wsgen(System.out, wsgenargs);
        } catch (Exception e ) {
            e.printStackTrace();
            throw new RuntimeException (localStrings.getLocalString("wsgen.rtexception",
                    "Exception occured in the wsgen process {0}",e));

        }
    }

   
}
