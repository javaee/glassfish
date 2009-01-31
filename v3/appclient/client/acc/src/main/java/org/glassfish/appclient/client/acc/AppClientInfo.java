/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.annotation.introspection.AppClientPersistenceDependencyAnnotationScanner;
import com.sun.enterprise.deployment.util.AnnotationDetector;
import com.sun.enterprise.loader.EJBClassLoader;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.appclient.client.jws.boot.JWSACCMain;
import org.glassfish.appclient.common.Util;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.xml.sax.SAXParseException;

import javax.persistence.EntityManagerFactory;

/**
 *Represents information about the app client, regardless of what type of
 *archive (jar or directory) it is stored in or what type of module 
 *(app client or nested within an ear) that archive holds.
 *
 *@author tjquinn
 */
@Service
public abstract class AppClientInfo {

    public static final String USER_CODE_IS_SIGNED_PROPERTYNAME = "com.sun.aas.user.code.signed";
    
    private static final String SIGNED_USER_CODE_PERMISSION_TEMPLATE_NAME = "jwsclientSigned.policy";
    
    private static final String UNSIGNED_USER_CODE_PERMISSION_TEMPLATE_NAME = "jwsclientUnsigned.policy";
    
    private static final String CODE_BASE_PLACEHOLDER_NAME = "com.sun.aas.jws.client.codeBase";
    
    /** logger */
    protected Logger _logger;

    @Inject
    protected ArchiveFactory archiveFactory;

    @Inject
    protected Habitat habitat;

    /** abstract representation of the storage location */
    private ReadableArchive appClientArchive = null;

    /** original appclient (file or directory) */
    private File appClientFile = null;

    /** abstract archivist able to operate on the module in the location 
      * (specified by archive */
    private Archivist archivist = null;
    
    /** main class name as the user specified it on the command line */
    protected String mainClassFromCommandLine;
    
    /**
     *main class to be used - could come from the command line or from the 
     *manifest of the selected app client archive 
     */
    protected String mainClassNameToRun = null;

    /** class loader - cached */
    private ClassLoader classLoader = null;
    
    /** indicates if the app client has been launched using Java Web Start */
    protected boolean isJWS = false;
    
    /** access to the localizable strings */
    private static final LocalStringManager localStrings =
            new LocalStringManagerImpl(AppClientInfo.class);

// XXX Mitesh helping to update this
//    private PersistenceUnitLoader.ApplicationInfo puAppInfo;

    /**
     *Creates a new instance of AppClientInfo.  Always invoked from subclasse
     *because AppClientInfo is abstract.
     *<p>
     *Note that any code instantiationg one of the concrete subclasses MUST
     *invoke completeInit after this super.constructor has returned.
     *
     *@param isJWS indicates if ACC has been launched using Java Web Start
     *@param logger the logger to use for messages
     *@param archive the AbstractArchive for the app client module or 
     *               directory being launched
     *@param archivist the Archivist corresponding to the type of module 
     *                 in the archive
     *@param mainClassFromCommandLine the main class name specified on the 
     *       command line (null if not specified)
     */
    public AppClientInfo(boolean isJWS, Logger logger, File appClientFile, 
                         Archivist archivist, String mainClassFromCommandLine) {
        this.isJWS = isJWS;
        _logger = logger;
        this.appClientFile = appClientFile;
        this.archivist = archivist;
        this.mainClassFromCommandLine = mainClassFromCommandLine;
    }

    /**
     *Finishes initialization work.
     *<p>
     *The calling logic that instantiates this object must invoke completeInit
     *after instantiation but before using the object.
     *@throws IOException for errors opening the expanded archive
     *@throws SAXParseException for errors parsing the descriptors in a newly-opened archive
     *@throws ClassNotFoundException if the main class requested cannot be located in the archive
     *@throws URISyntaxException if preparing URIs for the class loader fails
     *
     */
    protected void completeInit(URL[] persistenceURLs) 
        throws IOException, SAXParseException, ClassNotFoundException, 
               URISyntaxException, AnnotationProcessorException, Exception {

        //expand if needed. initialize the appClientArchive
        appClientArchive = expand(appClientFile);

        //Create the class loader to be used for persistence unit checking, 
        //validation, and running the app client.
        classLoader = createClassLoader(appClientArchive, persistenceURLs);

        //Populate the deployment descriptor without validation.
        //Note that validation is done only after the persistence handling
        //has instructed the classloader created above.
        populateDescriptor(appClientArchive, archivist, classLoader);
        
         //If the selected app client depends on at least one persistence unit
         //then handle the P.U. before proceeding.
        if (appClientDependsOnPersistenceUnit(getAppClient())) {
            //@@@check to see if the descriptor is metadata-complet=true
            //if not, we would have loaded classes into the classloader
            //during annotation processing.  we need to hault and ask
            //the user to deploy the application.
            //if (!getAppClient().isFullFlag()) {
            //    throw new RuntimeException("Please deploy your application");
            //}
            handlePersistenceUnitDependency();
        }
        
         //Now that the persistence handling has run and instrumented the class
         //loader - if it had to - it's ok to validate.
        archivist.validate(classLoader);
        
        fixupWSDLEntries();
        
        if (isJWS) {
            grantRequestedPermissionsToUserCode();
        }
    }
    
    /**
     *Returns the app client descriptor to be run.
     *@return the descriptor for the selected app client
     */
    protected ApplicationClientDescriptor getAppClient() {
        return getAppClient(archivist);
    }

    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    protected File createTmpArchiveDir(File forArchive) 
        throws IOException {
        /*
         *Create a temporary file (cannot create temp directories directly),
         *use the unique file name as a directory name.
         */
        String name = forArchive.getName();
        
        File tmpDir = File.createTempFile("acc-" + name, "");
        tmpDir.delete();

        tmpDir.mkdirs();

        /*
         *We will try to delete the directory when we finish with it, but this
         *is a back-up.
         */
        if (!_keepExplodedDir) {
            tmpDir.deleteOnExit();
        }

        return tmpDir;
    }

    /**
     *Closes the instance of AppClientInfo, deleting any temporary directory
     *created and closing the archive.
     *@throws IOException in case of error closing the archive
     */
    protected void close() throws IOException {
        try {
            // XXX Mitesh helping to update this
//            if (puAppInfo != null) {
//                new PersistenceUnitLoaderImpl().unload(puAppInfo);
//                puAppInfo = null;
//            }
            if (appClientArchive != null) {
                appClientArchive.close();
            }
            if (classLoader != null && 
                    classLoader instanceof EJBClassLoader) {
                ((EJBClassLoader) classLoader).done();
            }
        } finally {
            if (deleteAppClientDir()) {
                if (appClientArchive != null) {
                    appClientArchive.delete();
                }
            }
            appClientArchive = null;
        }
    }

    protected boolean deleteAppClientDir() {
        return !_keepExplodedDir;
    }
    
    protected String getLocalString(final String key, final String defaultMessage,
            final Object... args) {
        String result = localStrings.getLocalString(this.getClass(),
                key, defaultMessage, args);
        return result;
    }

    /**
     *Processes persistence unit handling for the ACC.
     */
    private void handlePersistenceUnitDependency() 
            throws URISyntaxException, MalformedURLException {
        // XXX Mitesh helping to update this
//        this.puAppInfo = new ApplicationInfoImpl(this);
//        new PersistenceUnitLoaderImpl().load(puAppInfo);
    }

    /**
     * implementation of
     * {@link com.sun.enterprise.server.PersistenceUnitLoader.ApplicationInfo}.
     */
    private static class ApplicationInfoImpl
            // XXX Mitesh helping to update this
//            implements PersistenceUnitLoader.ApplicationInfo
    {

        private AppClientInfo outer; // the outer object we are associated with

        private ApplicationClientDescriptor appClient;

        public ApplicationInfoImpl(AppClientInfo outer) {
            this.outer = outer;
            appClient = outer.getAppClient();
        }

        public Application getApplication(Habitat habitat) {
            Application application = appClient.getApplication();
            if (application == null) {
                application = Application.createApplication(
                        habitat,
                        appClient.getModuleID(),
                        appClient.getModuleDescriptor());
            }
            return application;
        }

        public InstrumentableClassLoader getClassLoader() {
            return (InstrumentableClassLoader) outer.getClassLoader();
        }

        public String getApplicationLocation() {
            return outer.appClientArchive.getURI().toASCIIString();
        }

        /**
         * @return list of PU that are actually referenced by the
         *         appclient.
         */
        public Collection<? extends PersistenceUnitDescriptor>
                getReferencedPUs() {
            return appClient.findReferencedPUs();
        }

        /**
         * @return list of EMFs that have been loaded for this appclient.
         */
        public Collection<? extends EntityManagerFactory> getEntityManagerFactories() {
            Collection<EntityManagerFactory> emfs =
                    new HashSet<EntityManagerFactory>();

            if (appClient.getApplication() != null) {
                emfs.addAll(appClient.getApplication()
                        .getEntityManagerFactories());
            }
            emfs.addAll(appClient.getEntityManagerFactories());
            return emfs;
        }
    } // end of class ApplicationInfoImpl

    private ClassLoader createClassLoader(ReadableArchive archive, URL[] persistenceURLs)
        throws IOException {
        List<String> paths = getClassPaths(archive);
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        EJBClassLoader loader = new EJBClassLoader(parent);

        final int LIST_SZ = paths.size();
        for (int i=0; i<LIST_SZ; i++) {
            String path = paths.get(i);
            loader.appendURL(new File(path));
        }
        
        if (_logger.isLoggable(Level.FINE)) {
            for (int i = 0; i < paths.size(); i++) {
                _logger.fine("Added path to classloader ==> " + paths.get(i));
            }
        }
        
        for (URL url : persistenceURLs) {
            loader.appendURL(url);
            _logger.fine("Added path to classloader ==> " + url);
        }

        return loader;
    }

    private void deleteFile(File f) {
        if (f.isDirectory()) {
            for (File subFile : f.listFiles()) {
                deleteFile(subFile);
            }
        }
        f.delete();
    }

    /**
     *Reports whether the selected app client depends on a persistence unit
     *or not.
     *@returns true if the app client depends on a persistence unit
     */
    private boolean appClientDependsOnPersistenceUnit(
        ApplicationClientDescriptor acDescr) 
            throws MalformedURLException, ClassNotFoundException, 
                   IOException, URISyntaxException {
            /*
             *If the descriptor contains at least one reference to an entity
             *manager then it definitely depends on a persistence unit.  
             */
            return descriptorContainsPURefcs(acDescr) 
                    || mainClassContainsPURefcAnnotations(acDescr);
    }
    
    /**
     *Reports whether the app client's descriptor shows a dependence on a
     *persistence unit.
     *@param descr the descriptor for the app client in question
     *@returns true if the descriptor shows such a dependency
     */
    private  boolean descriptorContainsPURefcs(
        ApplicationClientDescriptor descr) {
        return ! descr.getEntityManagerFactoryReferenceDescriptors().isEmpty();
    }
    
    /**
     *Reports whether the main class in the archive contains annotations that 
     *refer to persistence units.
     *@return boolean if the main class contains annotations that refer to a pers. unit
     */
    private boolean mainClassContainsPURefcAnnotations(
        ApplicationClientDescriptor acDescr) 
            throws MalformedURLException, ClassNotFoundException, 
                   IOException, URISyntaxException {
        AnnotationDetector annoDetector = 
                    new AnnotationDetector(new AppClientPersistenceDependencyAnnotationScanner());

        //e.g. FROM a.b.Foo or Foo TO a/b/Foo.class or Foo.class
        String mainClassEntryName = 
                acDescr.getMainClassName().replace('.', '/') + ".class";

        return classContainsAnnotation
                (mainClassEntryName, annoDetector, appClientArchive, acDescr);
    }

    /**
     *Adjusts the web services WSDL entries corresponding to where they
     *actually reside.
     */
    private void fixupWSDLEntries() 
        throws URISyntaxException, MalformedURLException, IOException, 
               AnnotationProcessorException {
        ApplicationClientDescriptor ac = getAppClient();
        URI uri = (new File(getAppClientRoot(appClientArchive, ac))).toURI();
        File moduleFile = new File(uri);
        for (Iterator itr = ac.getServiceReferenceDescriptors().iterator();
                    itr.hasNext();) {
            ServiceReferenceDescriptor serviceRef = 
                    (ServiceReferenceDescriptor) itr.next();
            if (serviceRef.getWsdlFileUri()!=null) {
                // In case WebServiceRef does not specify wsdlLocation, we get
                // wsdlLocation from @WebClient in wsimport generated source; 
                // If wsimport was given a local WSDL file, then WsdlURI will
                // be an absolute path - in that case it should not be prefixed
                // with modileFileDir
                String wsdlURI = serviceRef.getWsdlFileUri();
                File wsdlFile = new File(wsdlURI);
                if(wsdlFile.isAbsolute()) {
                    serviceRef.setWsdlFileUrl(wsdlFile.toURI().toURL());
                } else {
                    // This is the case where WsdlFileUri is a relative path
                    // (hence relative to the root of this module or wsimport
                    // was executed with WSDL in HTTP URL form
                    serviceRef.setWsdlFileUrl(getEntryAsUrl(
                        moduleFile, serviceRef.getWsdlFileUri()));
                }
            }
        }
    }

    private static URL getEntryAsUrl(File moduleLocation, String uri)
        throws MalformedURLException, IOException {
        URL url = null;
        try {
            url = new URL(uri);
        } catch(java.net.MalformedURLException e) {
            // ignore
            url = null;
        }
        if (url!=null) {
            return url;
        }
        if( moduleLocation != null ) {
            if( moduleLocation.isFile() ) {
                url = createJarUrl(moduleLocation, uri);
            } else {
                String path = uri.replace('/', File.separatorChar);
                url = new File(moduleLocation, path).toURI().toURL();
            }
        }
        return url;
    }

    private static URL createJarUrl(File jarFile, String entry)
        throws MalformedURLException, IOException {
        return new URL("jar:" + jarFile.toURI().toURL() + "!/" + entry);
    }


    private RootDeploymentDescriptor populateDescriptor(
            ReadableArchive archive, Archivist theArchivist, ClassLoader loader)
        throws IOException, SAXParseException, Exception {

        //@@@ Optimize it later.
        //Here the application.xml is read twice for NestedAppClientInfo.
        //Once already in expand() method.

        theArchivist.setAnnotationProcessingRequested(true);

        //@@@ setting of the classloader would trigger annotation processing 
        //for appclients that have only partial deployment descriptors or no
        //descriptors at all. 
        //Note that the annotation processing is bypassed if the descriptors 
        //are meta-complete=true", which will be the case for anything that is 
        //generated by the backend, i.e. if the appclient being executed here 
        //is a generated jar produced by the appserver, obtained by deploying
        //the original application client and retrieve.
        theArchivist.setClassLoader(loader);

        //open with Archivist./pen(AbstractArchive) to also ensure the
        //validation is not called
        //return archivist.open(archive);
        RootDeploymentDescriptor d = null;
        try {
            d = theArchivist.open(archive);
        } catch (Exception ex) { 
            close(); //make sure there is no junk tmp director left
            throw ex;
        }
        
        //depend on the type of the appclient, additional work needs
        //to be done.
        messageDescriptor(d, theArchivist, archive);

        theArchivist.setDescriptor(d);
        return d;
    }

    /**
     *Granting the appropriate level of permissions to user code, emulating
     *the Java Web Start behavior as required in the JNLP spec.
     *<p>
     *Classes from the user's app client jar are loaded using the EJBClassLoader
     *rather than the Java Web Start class loader.  As a result, Java Web Start
     *cannot grant the appropriate level of permissions to the user code since
     *it is the JWS class loader that does that.  So we need to grant the user
     *code the appropriate level of permissions, based on whether the user's
     *app client jar was signed or not.
     *@param retainTempFiles indicates whether to keep the generated policy file
     */
    private void grantRequestedPermissionsToUserCode() throws IOException, URISyntaxException {
        /*
         *Create a temporary file containing permissions grants.  We will use
         *this temp file to refresh the Policy objects's settings.  The temp
         *file will contain one segment for each element in the class path
         *for the user code (from the expanded generated app client jar). 
         *The permissions granted will be the same for all class path elements,
         *and the specific settings will either be the sandbox permissions
         *as described in the JNLP spec or full permissions.
         */
        boolean userJarIsSigned = Boolean.getBoolean(USER_CODE_IS_SIGNED_PROPERTYNAME);
        
        boolean retainTempFiles = Boolean.getBoolean(AppClientContainer.APPCLIENT_RETAIN_TEMP_FILES_PROPERTYNAME);
        
        /*
         *Use a template to construct each part of the policy file, choosing the
         *template based on whether the user code is signed or not.
         */
        String templateName = (userJarIsSigned ? SIGNED_USER_CODE_PERMISSION_TEMPLATE_NAME : UNSIGNED_USER_CODE_PERMISSION_TEMPLATE_NAME);
        String template = Util.loadResource(JWSACCMain.class, templateName);
        
        /*
         *Create the temporary policy file to write to.
         */
        File policyFile = File.createTempFile("accjws-user", ".policy");
        if ( ! retainTempFiles) {
            policyFile.deleteOnExit();
        }
        
        PrintStream ps = null;
        
        try {
            ps = new PrintStream(policyFile);
        
            Properties p = new Properties();

            /*
             *Use the list of class paths already set up on the EJBClassLoader.
             *Then for each element in the class path, write a part of the policy 
             *file granting the privs specified in the selected template to the code path.
             */
            EJBClassLoader loader = (EJBClassLoader) getClassLoader();
            for (URL classPathElement : loader.getURLs()) {
                /*
                 *Convert the URL into a proper codebase expression suitable for
                 *a grant clause in the policy file.
                 */
                String codeBase = Util.URLtoCodeBase(classPathElement);
                if (codeBase != null) {
                    p.setProperty(CODE_BASE_PLACEHOLDER_NAME, codeBase);
                    String policyPart = Util.replaceTokens(template, p);

                    ps.println(policyPart);
                }
            }

            /*
             *Close the temp file and use it to refresh the current Policy object.
             */
            ps.close();

            JWSACCMain.refreshPolicy(policyFile);

            if ( ! retainTempFiles) {
                policyFile.delete();
            }
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////
    //  The following protected methods are overridden by at least //
    //  one of the sub classes.                                    //
    /////////////////////////////////////////////////////////////////

    /**
     *Expands the contents of the source archive into a temporary
     *directory, using the same format as backend server expansions.
     *@param file an archive file to be expanded
     *@return an opened archive for the expanded directory archive
     *@exception IOException in case of errors during the expansion
     */
    protected abstract ReadableArchive expand(File file)
        throws IOException, Exception;

    protected ApplicationClientDescriptor getAppClient(
        Archivist archivist) {
        return ApplicationClientDescriptor.class.cast(
                    archivist.getDescriptor());
    }

    protected String getAppClientRoot(
        ReadableArchive archive, ApplicationClientDescriptor descriptor) {
        return archive.getURI().toASCIIString();
    }                                        

    protected void messageDescriptor(RootDeploymentDescriptor d, 
        Archivist archivist, ReadableArchive archive)
            throws IOException, AnnotationProcessorException {
        //default behavor: no op
    }

    protected List<String> getClassPaths(ReadableArchive archive) {
        List<String> paths = new ArrayList();
        paths.add(archive.getURI().toASCIIString());
        return paths;
    }

    /**
     *Returns the main class that should be executed.
     *@return the name of the main class to execute when the client starts
     */
    protected String getMainClassNameToRun(ApplicationClientDescriptor acDescr) {
         if (mainClassNameToRun == null) {
             if (mainClassFromCommandLine != null) {
                 mainClassNameToRun = mainClassFromCommandLine;
                 _logger.fine("Main class is " + mainClassNameToRun + " from command line");
             } else {
                 /*
                  *Find out which class to execute from the descriptor.
                  */
                 mainClassNameToRun = getAppClient().getMainClassName();
                 _logger.fine("Main class is " + mainClassNameToRun + " from descriptor");
             }
         }
         return mainClassNameToRun;
    }

    protected boolean classContainsAnnotation(
            String entry, AnnotationDetector detector, 
            ReadableArchive archive, ApplicationClientDescriptor descriptor)
            throws FileNotFoundException, IOException {
        return detector.containsAnnotation(archive, entry);
//        String acRoot = getAppClientRoot(archive, descriptor);
//        String entryLocation = acRoot + File.separator + entry;
//        File entryFile = new File(entryLocation);
//        return detector.containsAnnotation(archive, entry)
//        return detector.containsAnnotation(entryFile);
    }

    public String toString() {
        String lineSep = System.getProperty("line.separator");
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getName() + ": " + lineSep);
        result.append("  isJWS: " + isJWS);
        result.append("  archive file: " + appClientFile.getAbsolutePath() + lineSep);
        result.append("  archive type: " + appClientArchive.getClass().getName() + lineSep);
        result.append("  archivist type: " + archivist.getClass().getName() + lineSep);
        result.append("  main class to be run: " + mainClassNameToRun + lineSep);
        result.append("  temporary archive directory: " + appClientArchive.getURI() + lineSep);
        result.append("  class loader type: " + classLoader.getClass().getName() + lineSep);
       
        return result.toString();
    }

    //for debug purpose
    protected static final boolean _keepExplodedDir = 
            Boolean.getBoolean("appclient.keep.exploded.dir");
}