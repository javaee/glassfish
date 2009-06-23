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
package com.sun.enterprise.tools.verifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.xml.sax.SAXParseException;
import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.ConnectorArchivist;
import com.sun.enterprise.deployment.archivist.EjbArchivist;
import com.sun.enterprise.deployment.archivist.PluggableArchivistsHelper;
import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.deployment.backend.J2EEModuleExploder;
import com.sun.enterprise.deployment.backend.OptionalPkgDependency;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.FileArchiveFactory;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.logging.LogDomains;
import com.sun.enterprise.tools.verifier.apiscan.stdapis.APIRepository;
import com.sun.enterprise.tools.verifier.app.ApplicationVerifier;
import com.sun.enterprise.tools.verifier.appclient.AppClientVerifier;
import com.sun.enterprise.tools.verifier.connector.ConnectorVerifier;
import com.sun.enterprise.tools.verifier.ejb.EjbVerifier;
import com.sun.enterprise.tools.verifier.web.WebVerifier;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.util.JarClassLoader;
import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.enterprise.server.PELaunch;


/**
 * It is responsible for creating the application descriptor,
 * exploding the archive, creating the application classloader,
 * and invoking the appropriate check managers that load and run
 * the verifier tests.
 *
 * @author Vikas Awasthi
 */
public class VerificationHandler {

    private final String TMPDIR = System.getProperty("java.io.tmpdir");
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(
            "yyyyMMddhhmmss"); // NOI18N
    private String explodeDir = TMPDIR + File.separator + "exploded" + // NOI18N
            dateFormatter.format(new Date());
    private LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
    private FrameworkContext frameworkContext = null;
    private Logger logger = LogDomains.getLogger(
            LogDomains.AVK_VERIFIER_LOGGER);
    private Application application = null;
    private ResultManager resultManager = null;
    private Archivist archivist = null;
    private boolean isBackend = false;
    private List<String> classPath = null;
    private final int MAX_WINDOWS_PATH_LIMIT = 248;

    public VerificationHandler(FrameworkContext frameworkContext)
            throws IOException {
        this.frameworkContext = frameworkContext;
        try {
            initStandalone();
        } catch(IOException ioe) {
            cleanup();
            throw ioe;
        } catch(RuntimeException re) {
            cleanup();
            throw re;
        }
    }

    public VerificationHandler(FrameworkContext frameworkContext, 
                               Application application, 
                               AbstractArchive abstractArchive,
                               List<String> classPath) {
        this.frameworkContext = frameworkContext;
        init();
        this.application = application;
        this.frameworkContext.setClassPath(classPath);
        this.frameworkContext.setJarFileName(application.getRegistrationName());
        isBackend = true;
        this.frameworkContext.setApplication(application);
        this.frameworkContext.setAbstractArchive(abstractArchive);
    }

    public ResultManager verifyArchive() { // should we throw exception here
        if(!application.isVirtual()) { // don't run app tests for standalone module
            runVerifier(new ApplicationVerifier(frameworkContext, application));
        }

        for (Iterator itr = application.getEjbBundleDescriptors().iterator();
             itr.hasNext();) {
            EjbBundleDescriptor ejbd = (EjbBundleDescriptor) itr.next();
            runVerifier(new EjbVerifier(frameworkContext, ejbd));
        }

        for (Iterator itr = application.getWebBundleDescriptors().iterator();
             itr.hasNext();) {
            WebBundleDescriptor webd = (WebBundleDescriptor) itr.next();
            runVerifier(new WebVerifier(frameworkContext, webd));
        }

        for (Iterator itr = application.getApplicationClientDescriptors()
                .iterator();
             itr.hasNext();) {
            ApplicationClientDescriptor appClientDescriptor =
                                    (ApplicationClientDescriptor) itr.next();
            runVerifier(new AppClientVerifier(frameworkContext,appClientDescriptor));
        }

        for (Iterator itr = application.getRarDescriptors().iterator();
             itr.hasNext();) {
            ConnectorDescriptor cond = (ConnectorDescriptor) itr.next();
            runVerifier(new ConnectorVerifier(frameworkContext, cond));
        }
        
        return resultManager;
    }
    /**
     * common initialization method for the call from backend and standalone
     * invocation.
     */
    private void init() {
        frameworkContext.setResultManager(resultManager = new ResultManager());
        try {
            APIRepository.Initialize(
                    frameworkContext.getConfigDirStr() + File.separator +
                    "standard-apis.xml"); // NOI18N
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * initialization done for standalone verifier invocation.
     * @throws IOException
     */
    private void initStandalone() throws IOException {
        init();
        logger.log(Level.FINE, getClass().getName() + ".debug.startingLoadJar");
        if (!frameworkContext.isPortabilityMode()) {
            String as_config_dir =
                    System.getProperty("com.sun.aas.installRoot")+File.separator+"config";
            classPath = PELaunch.getServerClassPath(as_config_dir,
                                                    frameworkContext.getDomainDir());
        }

        // initialize /tmp/* directories
        initVerifierTmpDirs();
        String jarFile = frameworkContext.getJarFileName();
        //We must call OptionalPkgDependency.satisfyOptionalPackageDependencies() before explodeArchive,
        //because inside this call, the list of installed optional packages in the system gets initialised.
        //That list is then used inside optionalPackageDependencyLogic() code.
        //It looks to be a bug as ideally this kind of dependency should be taken care of inside
        //OptionalPkgDependency class itself.
        //But any way, we don't have a choice but to make this work around in our code.
        OptionalPkgDependency.satisfyOptionalPackageDependencies();
        archivist = ArchivistFactory.getArchivistForArchive(new File(jarFile));
        if(archivist == null) {
            throw new RuntimeException(
                    smh.getLocalString(getClass().getName() + ".notAJavaEEArchive", // NOI18N
                    "[ {0} ] is not a valid Java EE archive", // NOI18N
                            new Object[]{jarFile}));
        }
        explodeArchive(new File(jarFile));
        checkAndExplodeArchiveInWindowsPlatform(jarFile);
        Descriptor.setBoundsChecking(false);

        try {
            createApplicationDescriptor();
        } catch (IOException e) {
            log("Problem in creating application descriptor", e);
            throw e;
        } catch (SAXParseException se) {
            log("Problem in parsing the xml file. For " +
                    se.getLocalizedMessage() +
                    ", error at line " +
                    se.getLineNumber() +
                    ", column " +
                    se.getColumnNumber(), se);
            IOException ioe = new IOException();
            ioe.initCause(se);
            throw ioe;
        }
        ((Descriptor) application).visit(new ApplicationValidator());
    }

    private void runVerifier(BaseVerifier baseVerifier) {
        try {
            baseVerifier.verify();
        } catch (Exception e) {
            log("Problem in running tests for :" +
                    baseVerifier.getDescriptor().getName(),
                    e);
        }
    }

    private void createApplicationDescriptor() throws IOException,
            SAXParseException {
// the code below is used by the deploytool GUI.            
        PluggableArchivistsHelper defaultArchivists = new PluggableArchivistsHelper();
        defaultArchivists.registerArchivist(new ApplicationArchivist());
        defaultArchivists.registerArchivist(new WebArchivist());
        defaultArchivists.registerArchivist(new EjbArchivist());
        defaultArchivists.registerArchivist(new ConnectorArchivist());
        defaultArchivists.registerArchivist(new AppClientArchivist());
        AbstractArchive abstractArchive =
                new FileArchiveFactory().openArchive(
                        frameworkContext.getExplodedArchivePath());
        frameworkContext.setAbstractArchive(abstractArchive);
        archivist.setPluggableArchivists(defaultArchivists);
        archivist.setXMLValidationLevel("full");
        archivist.setRuntimeXMLValidation(true);
        archivist.setRuntimeXMLValidationLevel("full");
        archivist.setAnnotationProcessingRequested(true);
        archivist.setAnnotationErrorHandler(new VerifierErrorHandler(resultManager));

        String jarName = new File(abstractArchive.getArchiveUri()).getName();
        createApplicationDescriptor0(abstractArchive, jarName);
    }

    private void explodeArchive(File archiveFile) throws IOException {
        if (archiveFile.isDirectory()) {
            frameworkContext.setExplodedArchivePath(
                    archiveFile.getAbsolutePath());
            return;
        }

        String appName = FileUtils.makeFriendlyFileNameNoExtension(
                archiveFile.getName());
        File appDir = new File(new File(explodeDir), appName);
        frameworkContext.setExplodedArchivePath(appDir.getAbsolutePath());

        try {
            ModuleType moduleType = archivist.getModuleType();
            if(ModuleType.EAR.equals(moduleType)) { // EAR file
                // Explode the archive and create a descriptor. This is
                // not complete at this point. It will be filled with all info later.
                application = J2EEModuleExploder.explodeEar(archiveFile, appDir);
            } else if ( ModuleType.EJB.equals(moduleType) ||
                    ModuleType.CAR.equals(moduleType) ||
                    ModuleType.RAR.equals(moduleType) ||
                    ModuleType.WAR.equals(moduleType) ) {
                J2EEModuleExploder.explodeJar(archiveFile, appDir);
            } else
                throw new FileNotFoundException(
                        "Deployment descriptor not found in " +
                        archiveFile.getName());
        } catch (Exception e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }

    // See issue: 6329054. In windows max allowed path length is 248. Check
    // if this length is exceeded during archive explosion. If the length 
    // exceeds the max limit then use %HOMEDRIVE%/temp as the temp directory
    // for exploding the archive. This is done because default temp directory
    // itself consumes a lot of path length. If if file length still exceeds 
    // max limit or if %HOMEDRIVE%/temp does not exist then throw exception
    // giving proper message to the user.
    private void checkAndExplodeArchiveInWindowsPlatform(String jarFile) 
            throws IOException {
        if(!System.getProperty("os.name").toLowerCase().startsWith("win"))
            return;
        
        if(!testFileLength(new File(explodeDir))) {
            File tempDir = new File(System.getProperty("home.drive"),"temp");
            if(!tempDir.exists())
                throw new IOException(smh.getLocalString
                        (getClass().getName() + ".exception1","Maximum Path " +
                        "Length exceeded. The application uses long file names " +
                        "which has exceeded maximum allowed path length in windows." +
                        " Please shorten the file names and then continue. Not " +
                        "able to proceed further as [{0}] does not exist",
                        new Object[]{tempDir.getAbsolutePath()}));
            if(!FileUtil.deleteDir(new File(explodeDir))) {
                logger.log(Level.WARNING, 
                            getClass().getName() + ".explodedirdeleteerror", // NOI18N
                            new Object[] {explodeDir});
            }
            explodeDir = tempDir.getAbsolutePath() + File.separator + "exploded" +
                    dateFormatter.format(new Date());
            explodeArchive(new File(jarFile));
            if(!testFileLength(new File(explodeDir)))
                throw new IOException(smh.getLocalString
                        (getClass().getName() + ".exception","Maximum Path Length " +
                        "exceeded. The application uses long file names which has " +
                        "exceeded maximum allowed path length in windows. Please " +
                        "shorten the file names and then continue."));
        }
    }
    
    /** Maximum allowed path length in windows is 248. Return false if any 
     * exploded file exceeds this length. */
    private boolean testFileLength(File file) throws IOException {
        if(file.getAbsolutePath().length() > MAX_WINDOWS_PATH_LIMIT) {
            logger.log(Level.WARNING, 
                        getClass().getName() + ".maxlength.exceeded", // NOI18N
                        new Object[] {file.getAbsolutePath(), 
                                      file.getAbsolutePath().length(), 
                                      MAX_WINDOWS_PATH_LIMIT});
            return false;
        } else if(file.getCanonicalPath().length() > MAX_WINDOWS_PATH_LIMIT) {
            // absolute paths may have ~1. Since windows behave strangely  
            // both absolute path and canonical path are used
            logger.log(Level.WARNING, 
                        getClass().getName() + ".maxlength.exceeded", // NOI18N
                        new Object[] {file.getCanonicalPath(), 
                                      file.getCanonicalPath().length(), 
                                      MAX_WINDOWS_PATH_LIMIT});
            return false;
        }
        
        if(!file.isDirectory())
            return true;
        
        for (File file1 : file.listFiles()) 
            if(!testFileLength(file1))
                return false;
        return true;
    }

    private boolean initVerifierTmpDirs() throws IOException {
        // Make sure we can create the directory appservResultDir
        File test = new File(explodeDir);
        if (!test.isDirectory() && !test.getAbsoluteFile().mkdirs()) {
            logger.log(Level.SEVERE, getClass().getName() +
                    ".explodedircreateerror", test.getAbsolutePath()); // NOI18N
            throw new IOException(smh.getLocalString(getClass().getName()
                    + ".explodedircreateerror", test.getAbsolutePath())); // NOI18N
        }
        return true;
    }

    public void cleanup() {
        if(!isBackend && application!=null)
            ((JarClassLoader)application.getClassLoader()).done();
        if(!isBackend &&
            !((new File(frameworkContext.getJarFileName())).isDirectory()))
            FileUtil.deleteDir(new File(explodeDir));
    }

    /**
     * This method is used to log exception messges in the error vector of
     * ResultManager object.
     * @param message
     * @param e
     */
    private void log(String message, Exception e) {
        if (message == null) message = "";
        LogRecord logRecord = new LogRecord(Level.SEVERE, message);
        logRecord.setThrown(e);
        frameworkContext.getResultManager().log(logRecord);
    }

    private ClassLoader getEarClassLoader(Application dummyApp) throws IOException {
        List<String> classPath = EJBClassPathUtils.getApplicationClassPath(dummyApp, 
                                        frameworkContext.getExplodedArchivePath());
        return createClassLoaderFromPath(classPath);
    }

    private ClassLoader getModuleClassLoader(ModuleType type) throws IOException {
        String moduleRoot = frameworkContext.getExplodedArchivePath();
        List<String> classPath = EJBClassPathUtils.getModuleClassPath(type, moduleRoot, moduleRoot);
        return createClassLoaderFromPath(classPath);
    }
    
    private ClassLoader createClassLoaderFromPath(List<String> classPath) throws IOException {
        if(!frameworkContext.isPortabilityMode()) {
            classPath.addAll(0, this.classPath);
            frameworkContext.setClassPath(classPath);
        } else {
            String as_lib_root = System.getProperty("com.sun.aas.installRoot")+
                                                    File.separator+
                                                    "lib"+ // NOI18N
                                                    File.separator;
            classPath.add(as_lib_root + "javaee.jar"); // NOI18N
        }

        JarClassLoader jcl = new JarClassLoader();
        // toURI().toURL() takes care of all the escape characters in the 
        // absolutePath. The toURI() method encodes all escape characters. Since
        // EJBClassLoader does not decode these urls here only toURL() is used.
        // Once this issue is fixed in EJBClassloader we can change it to 
        // toURI().toURL()
        for (String path : classPath) 
            jcl.appendURL(new File(path));
        return jcl;
    }

    /**
     * This method populates the missing information in Application object
     * that is already created in {@link #explodeArchive(java.io.File)}.
     * @param abstractArchive
     * @param jarName
     * @throws IOException
     * @throws SAXParseException
     */
    private void createApplicationDescriptor0(AbstractArchive abstractArchive,
                                              String jarName) throws IOException, SAXParseException{
        if (archivist.getModuleType()==ModuleType.EAR) {
                ClassLoader classLoader = getEarClassLoader(application);
                application.setClassLoader(classLoader);
                archivist.setClassLoader(classLoader);
                archivist.setHandleRuntimeInfo(!frameworkContext.isPortabilityMode());
                archivist.readPersistenceDeploymentDescriptors(abstractArchive, application);
                ((ApplicationArchivist) archivist).readModulesDescriptors(application, abstractArchive);
                if(!frameworkContext.isPortabilityMode()) {
                    // this recurssively reads runtime DD for all the modules.
                    archivist.readRuntimeDeploymentDescriptor(abstractArchive, application);
                }
                application.setRegistrationName(jarName);
        } else {
            ClassLoader classLoader = getModuleClassLoader(archivist.getModuleType());
            archivist.setClassLoader(classLoader);
            application = ApplicationArchivist.openArchive(jarName,
                                                    archivist,
                                                    abstractArchive,
                                                    !frameworkContext.isPortabilityMode());
            application.setClassLoader(classLoader);
        }
    }
}
