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


import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.node.WebServiceNode;
import com.sun.enterprise.deployment.util.WebServerInfo;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.logging.LogDomains;
import com.sun.tools.ws.util.xml.XmlUtil;
import com.sun.xml.bind.api.JAXBRIContext;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DownloadableArtifacts;
import org.glassfish.webservices.codegen.*;
import org.glassfish.webservices.monitoring.Deployment109ProbeProvider;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.internal.api.JAXRPCCodeGenFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.servlet.SingleThreadModel;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Webservices module deployer. This is loaded from WebservicesContainer
 *
 * @author Bhakti Mehta
 * 
 */
@Service
public class WebServicesDeployer extends JavaEEDeployer<WebServicesContainer,WebServicesApplication> {
    public static final WebServiceDeploymentNotifier deploymentNotifier =
            new WebServiceDeploymentNotifierImpl();

    public static WebServiceDeploymentNotifier getDeploymentNotifier() {
        return deploymentNotifier;
    }

    private Logger logger = LogDomains.getLogger(this.getClass(),LogDomains.WEBSERVICES_LOGGER);

    private ResourceBundle rb = logger.getResourceBundle();

    @Inject
    private ServerEnvironment env;

    @Inject
    private RequestDispatcher dispatcher;

    @Inject(name="server-config")
    private Config config;

    @Inject
    private Habitat habitat;

    @Inject
    private DownloadableArtifacts downloadableArtifacts;

    @Inject
    private ArchiveFactory archiveFactory;



    private Deployment109ProbeProvider probe;

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
   
    public boolean prepare(DeploymentContext dc) {
        try {

            Application app = dc.getModuleMetaData(Application.class);
            if (app==null) {
                // hopefully the DOL gave a good message of the failure...
                logger.severe(format(rb.getString("failed.loading.dd")));
                return false;
            }
            BundleDescriptor bundle = dc.getModuleMetaData(BundleDescriptor.class);

            String moduleCP = getModuleClassPath(dc);
            List<URL> moduleCPUrls = ASClassLoaderUtil.getURLsFromClasspath(moduleCP, File.pathSeparator, null);
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            URLClassLoader newCl = new URLClassLoader(ASClassLoaderUtil.convertURLListToArray(moduleCPUrls), oldCl);
            Thread.currentThread().setContextClassLoader(newCl);
            WebServicesDescriptor wsDesc = bundle.getWebServices();
            for (WebService ws : wsDesc.getWebServices()) {
                if ((new WsUtil()).isJAXWSbasedService(ws)){
                    setupJaxWSServiceForDeployment(dc, ws);
                } else {
                    JAXRPCCodeGenFacade facade= habitat.getByContract(JAXRPCCodeGenFacade.class);
                    if (facade != null) {
                        facade.run(habitat, dc, moduleCP);
                    }  else {
                        throw new DeploymentException(rb.getString("jaxrpc.codegen.fail")) ;
                    }
                }
            }
            doWebServicesDeployment(app,dc);
            populateWsdlFilesForPublish(dc,wsDesc);
            Thread.currentThread().setContextClassLoader(oldCl);
            return true;
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            RuntimeException re = new RuntimeException(ex.getMessage());
            re.initCause(ex);
            throw re;
        }
    }


    protected void setupJaxWSServiceForDeployment(DeploymentContext dc, WebService ws) throws DeploymentException {

        BundleDescriptor bundle = dc.getModuleMetaData(BundleDescriptor.class);

        // for modules this is domains/<domain-name>/j2ee-modules/<module-name>
        // for apps this is domains/<domain-name>/j2ee-apps/<app-name>/<foo_war> (in case of embedded wars)
        //  or domains/<domain-name>/j2ee-apps/<app-name>/<foo_jar> (in case of embedded jars)
        File moduleDir = dc.getSourceDir();

        //For modules this is domains/<domain-name>/generated/xml
        //Check with Hong about j2ee-modules
        File wsdlDir = dc.getScratchDir("xml");
        wsdlDir.mkdirs();


        //For modules this is domains/<domain-name>/generated/xml
        //Check with Hong about j2ee-modules
        File stubsDir = dc.getScratchDir("ejb");
        stubsDir.mkdirs();

        /** TODO BM implement later
         if(!dc.getModuleProps().getProperty("type").equals("web")) {
         String subDirName = DeploymentUtils.getRelativeEmbeddedModulePath(moduleDir.getAbsolutePath(), bundle.getModuleDescriptor().getArchiveUri());

         moduleDir =new File(moduleDir, subDirName);
         wsdlDir =new File( wsdlDir,subDirName);

         }**/

        String webinfLibDir = null;
        if (!XModuleType.WAR.equals(bundle.getModuleType()) &&
                !XModuleType.EJB.equals(bundle.getModuleType())) {
            // unknown module type with @WebService, just ignore...
            return;
        }
        if (XModuleType.WAR.equals(bundle.getModuleType())) {
            webinfLibDir = moduleDir.getAbsolutePath() + File.separator + "WEB-INF"+File.separator+"lib";
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
            } else {
                wsdlFileUri = ws.getWsdlFileUri();
                if(wsdlFileUri.startsWith("/")) {
                    wsdlFile = new File(wsdlFileUri);
                } else {
                    wsdlFile = new File(moduleDir, wsdlFileUri);
                }
                if (!wsdlFile.exists()) {
                    String errorMessage =  format(rb.getString("wsdl.notfound"),
                            ws.getWsdlFileUri(),bundle.getModuleDescriptor().getArchiveUri())  ;
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
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    public Object loadMetaData(Class type, DeploymentContext dc) {
         //Moved the doWebServicesDeployment back to prepare after discussing with
         //Jerome
         //see this bug  https://glassfish.dev.java.net/issues/show_bug.cgi?id=8080
         return true;
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */

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
            logger.log(Level.SEVERE,"Error occured", x);
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

    public void doWebServicesDeployment(Application app, DeploymentContext dc)throws Exception{

        Collection webBundles = new HashSet();
        Collection webServices = new HashSet();

        // First collect all web applications and web service descriptors.
        webBundles.addAll( app.getWebBundleDescriptors() );
        webServices.addAll( app.getWebServiceDescriptors() );

        // swap the deployment descriptors context-root with the one
        // provided in the deployment request.
        if (dc.getAppProps().get("context-root") !=null ) {
            if (app.isVirtual()) {
                String contextRoot = ((String)dc.getAppProps().get("context-root"));
                ((WebBundleDescriptor) webBundles.iterator().next()).setContextRoot(contextRoot);

            }
        }

        // Generate final wsdls for all web services and store them in
        // the application repository directory.
        for(Iterator<WebService> iter = webServices.iterator(); iter.hasNext(); ) {
            WsUtil wsUtil = new WsUtil();
            WebService next = iter.next();

            // For JAXWS services, we rely on JAXWS RI to do WSL gen and publishing
            // For JAXRPC we do it here in 109
            //however it is needed for file publishing for jaxws
            if(wsUtil.isJAXWSbasedService(next) && (!next.hasFilePublishing())) {
                for(WebServiceEndpoint wsep : next.getEndpoints()) {
                    wsep.composeFinalWsdlUrl(wsUtil.getWebServerInfoForDAS().getWebServerRootURL(wsep.isSecure()));
                }
            } else {

                // Even if deployer specified a wsdl file
                // publish location, server can't assume it can access that
                // file system.  Plus, it's cleaner to depend on a file stored
                // within the application repository rather than one directly
                // exposed to the deployer. Name of final wsdl is derived based
                // on the location of its associated module.  This prevents us
                // from having write the module to disk in order to store the
                // modified runtime info.
                URL url = next.getWsdlFileUrl();
                if (url == null ) {
                    File f = new File(dc.getSourceDir(),next.getWsdlFileUri()) ;
                    url = f.toURL();
                }

                // Create the generated WSDL in the generated directory; for that create the directories first
                File genXmlDir =  dc.getScratchDir("xml");
                /*if(!app.isVirtual()) {
                    // Add module name to the generated xml dir for apps
                    String subDirName = next.getBundleDescriptor().getModuleDescriptor().getArchiveUri();
                    genXmlDir = new File(genXmlDir, subDirName.replaceAll("\\.", "_"));
                }*/
                String wsdlFileDir = next.getWsdlFileUri().substring(0, next.getWsdlFileUri().lastIndexOf('/'));
                (new File(genXmlDir, wsdlFileDir)).mkdirs();
                File genWsdlFile = new File(genXmlDir, next.getWsdlFileUri());
                wsUtil.generateFinalWsdl(url, next, wsUtil.getWebServerInfoForDAS(), genWsdlFile);
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
        final WebServiceDeploymentNotifier notifier = getDeploymentNotifier();

        Collection endpoints = web.getWebServices().getEndpoints();
        ClassLoader cl = web.getClassLoader();
        WsUtil wsutil = new WsUtil();

        for(Iterator endpointIter = endpoints.iterator();endpointIter.hasNext();) {

            WebServiceEndpoint nextEndpoint = (WebServiceEndpoint)endpointIter.next();
            WebComponentDescriptor webComp = nextEndpoint.getWebComponentImpl();
            if( !nextEndpoint.hasServletImplClass() ) {
                throw new DeploymentException( format(rb.getString(
                        "enterprise.deployment.backend.cannot_find_servlet"),
                        nextEndpoint.getEndpointName()));
            }
            String servletImplClass = nextEndpoint.getServletImplClass();
            if( !nextEndpoint.getWebService().hasFilePublishing() ) {
                String publishingUri = nextEndpoint.getPublishingUri();
                String publishingUrlPattern =
                        (publishingUri.charAt(0) == '/') ?publishingUri : "/" + publishingUri + "/*";
                webComp.addUrlPattern(publishingUrlPattern);

            }
            try {
                Class servletImplClazz  = cl.loadClass(servletImplClass);
                String containerServlet;
                if(wsutil.isJAXWSbasedService(nextEndpoint.getWebService())) {
                    containerServlet = "org.glassfish.webservices.JAXWSServlet";
                } else {
                    containerServlet =
                    SingleThreadModel.class.isAssignableFrom(servletImplClazz) ?
                    "org.glassfish.webservices.SingleThreadJAXRPCServlet" :
                        "org.glassfish.webservices.JAXRPCServlet";
                }
                webComp.setWebComponentImplementation(containerServlet);
                if (notifier != null) {
                    notifier.notifyDeployed(nextEndpoint);
                }
            } catch(ClassNotFoundException cex) {
                throw new DeploymentException( format(rb.getString(
                        "enterprise.deployment.backend.cannot_find_servlet"),
                        nextEndpoint.getEndpointName()));
            }

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
            WebServerInfo wsi = new WsUtil().getWebServerInfoForDAS();
            URL rootURL = wsi.getWebServerRootURL(nextEndpoint.isSecure());
            URL actualAddress = nextEndpoint.composeEndpointAddress(rootURL);
            //Ommitting the part of generating the wsdl for now
            //I think we need that to set the endpointAddressURL of WebServiceEndpoint
            logger.info(format(rb.getString("enterprise.deployment.endpoint.registration"),
            nextEndpoint.getEndpointName(), actualAddress.toString() ));

        }
    }

    private String format(String key, String ... values){
        return MessageFormat.format(key, (Object [])values);
    }

    public static void moveFile(String sourceFile, String destFile)
    throws IOException {
        FileUtils.copy(sourceFile, destFile);
        new File(sourceFile).delete();
    }

    public void unload(WebServicesApplication container, DeploymentContext context) {
        final WebServiceDeploymentNotifier notifier = getDeploymentNotifier();

        Application app = container.getApplication();
        for(WebService svc : app.getWebServiceDescriptors()) {
            for(WebServiceEndpoint endpoint : svc.getEndpoints()) {
                probe.undeploy(endpoint.getEndpointAddressPath());
                if (notifier != null) {
                    notifier.notifyUndeployed(endpoint);
                }
            }
        }
    }

    public void clean(DeploymentContext dc) {
        BundleDescriptor bundle = dc.getModuleMetaData(BundleDescriptor.class);
        String name = bundle.getApplication().getAppName();
        if (downloadableArtifacts.getArtifacts(name).size()>0) {
            downloadableArtifacts.clearArtifacts(name) ;
        }
    }


    public WebServicesApplication load(WebServicesContainer container, DeploymentContext context) {
        probe = container.getDeploymentProbeProvider();
        Application app = context.getModuleMetaData(Application.class);
        
        for(WebService svc : app.getWebServiceDescriptors()) {
            for(WebServiceEndpoint endpoint : svc.getEndpoints()) {
                probe.deploy(app, endpoint);
            }
        }

        return new WebServicesApplication(context, env, dispatcher, config, habitat);
    }

    /**
     * Populate the wsdl files entries to download (if any) (Only for webservices which
     * use file publishing).
     */
    private void populateWsdlFilesForPublish(
            DeploymentContext dc, WebServicesDescriptor wsDesc) throws IOException {


        for (WebService webService : wsDesc.getWebServices()) {
            if (!webService.hasFilePublishing()) {
                continue;
            }

            copyExtraFilesToGeneratedFolder(dc);
            BundleDescriptor bundle = webService.getBundleDescriptor();

            XModuleType moduleType = bundle.getModuleType();
            //only EAR, WAR and EJB archives could contain wsdl files for publish
            if (!(XModuleType.EAR.equals(moduleType) ||
                    XModuleType.WAR.equals(moduleType) ||
                    XModuleType.EJB.equals(moduleType))) {
                return;
            }

            String moduleName = bundle.getApplication().getAppName();
            File sourceDir = dc.getScratchDir("xml");

            URI clientPublishURI =  null;

            try {
                clientPublishURI =  webService.getClientPublishUrl().toURI();
            } catch (URISyntaxException e) {
                logger.warning(rb.getString("exception.thrown") + e);

            }
            File parent = new File(clientPublishURI);
            
            // Collect the names of all entries in or below the
            // dedicated wsdl directory.
            FileArchive archive = new FileArchive();
            archive.open(sourceDir.toURI());


            Enumeration entries = archive.entries(bundle.getWsdlDir());

            // Strictly speaking, we only need to write the files needed by
            // imports of this web service's wsdl file.  However, it's not
            // worth actual parsing the whole chain just to figure this
            // out.  In the worst case, some unnecessary files under
            // META-INF/wsdl or WEB-INF/wsdl will be written to the publish
            // directory.
            ArrayList<DownloadableArtifacts.FullAndPartURIs> alist = new ArrayList<DownloadableArtifacts.FullAndPartURIs>();
            while(entries.hasMoreElements()) {
                String name = (String) entries.nextElement();
                String wsdlName = name.substring(name.lastIndexOf('/')+1) ;
                URI clientwsdl = new File(parent, wsdlName).toURI();
                alist.add(new DownloadableArtifacts.FullAndPartURIs(new File(sourceDir,name).toURI(),clientwsdl));
            }
            downloadableArtifacts.addArtifacts(moduleName,alist);

        }
    }

    /**
     * This is to be used for filepublishing only. Incase of wsdlImports and wsdlIncludes
     * we need to copy the nested wsdls from applications folder to the generated/xml folder
     *
     */
    private void copyExtraFilesToGeneratedFolder( DeploymentContext context) throws IOException{
        Archivist archivist = habitat.getByContract(Archivist.class);

        ReadableArchive archive = archiveFactory.openArchive(
                context.getSourceDir());

        WritableArchive archive2 = archiveFactory.createArchive(
                context.getScratchDir("xml"));

        // copy the additional webservice elements etc
        archivist.copyExtraElements(archive, archive2);
 
    }

}

   

