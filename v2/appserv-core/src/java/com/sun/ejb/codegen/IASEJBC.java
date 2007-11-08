
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
package com.sun.ejb.codegen;

import java.lang.reflect.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import static com.sun.corba.ee.spi.codegen.Wrapper.*;

import com.sun.ejb.codegen.*;
import com.sun.ejb.EJBUtils;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.backend.DeploymentMode;
import com.sun.enterprise.deployment.backend.WebServiceDeployer; 
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.runtime.IASEjbExtraDescriptors;
import com.sun.enterprise.log.Log;
import com.sun.enterprise.util.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.enterprise.webservice.codegen.JaxRpcCodegenAdapter;
import com.sun.enterprise.webservice.codegen.JaxRpcCodegenFactory;
import com.sun.logging.LogDomains;

/**
 * Handles all ejb related codegen. Also does webservices code generation.
 * 
 *
 * @author Vivek Nagar
 * @author Danny Coward
 * @author Nazrul Islam
 * @author Praveen Joy
 * @author Kenneth Saks
 */
public final class IASEJBC {

    private static final StringManager localStrings =
                                StringManager.getManager(IASEJBC.class);
    private static final Logger _logger = 
                            LogDomains.getLogger(LogDomains.DPL_LOGGER);

    /**
     * This class is only instantiated internally.
     */
    private IASEJBC() { }

    /**
     * Get the java file name based on the specified class name.
     *
     * @param  className   the class name.
     * @param  repository  repository for this application
     *
     * @return the java file name.
     */
    private String getFileName(String className, String repository) {

        return (repository + File.separator 
               + className.replace('.', File.separatorChar) + ".java");
    }

    /**
     * Get the class name based on the specified file name.
     *
     * @param  fileName    the java file name.
     * @param  repository  path to the code generator repository
     *
     * @return the class name.
     */
    private String getClassName(String fileName, String repository) {

        String className = fileName;
        if (className.startsWith(repository))
            className = className.substring(repository.length());

        if (className.indexOf(".java") != -1)
            className = className.substring(0, className.indexOf(".java"));
        else if (className.indexOf(".class") != -1)
            className = className.substring(0, className.indexOf(".class"));

        className = className.replace(File.separatorChar, '.');
        if (className.charAt(0) == '.')
            className = className.substring(1);

        return className;
    }

    /**
     * Create the FileOutputStream for the specified class.
     *
     * @param   fileName     the name of the file
     *
     * @return the output stream.
     *
     * @exception IOException.
     */
    private OutputStream createOutputStream(String fileName)
            throws IOException
    {
        File file    = new File(fileName);
        File parent  = null;

        if ( (parent=file.getParentFile()) != null) 
        {
            if ( !parent.exists() ) 
            {
                parent.mkdirs();
            }
        }

        FileOutputStream out       = new FileOutputStream(fileName);
        BufferedOutputStream bout  = new BufferedOutputStream(out);

        return bout;
    }


    /**
     * Runs the generator and adds the generated file to files
     * 
     * @param    gen    code generator 
     * @param    files  contans newly generated files
     * @param    rep    directory where generator will create new src files
     *
     * @return   file name of generated file
     * @throws   Exception  if an error while generating new src
     */
    private String generateCode(Generator gen, Vector files, File rep)
        throws Exception
    {

        String genClass    = gen.getGeneratedClass();
        String repository  = rep.getCanonicalPath();
        String genFile     = getFileName(genClass, repository);
        
        OutputStream out = createOutputStream(genFile);
        PrintStream ps = new PrintStream(out);
        ((ClassGeneratorFactory)gen).evaluate();
        _sourceCode(ps, null);
        out.close();

        _logger.log(Level.FINE,
                    "[EJBC] Adding to generated files: " + genFile);

        files.addElement(genFile);
    
        return genFile;
    }

    /**
     * Compile all the generated .java files, run rmic on them.
     *
     * @param    classPath         class path for javac & rmic
     * @param    rmicOptions       options for rmic
     * @param    stubClasses  additional classes to be compilled with 
     *                             the other files
     * @param    destDir           destination directory for javac & rmic
     * @param    repository        repository for code generator
     *
     * @exception    GeneratorException  if an error during code generation
     * @exception    IOException         if an i/o error
     */
    private void compileAndRmic(String classPath, List rmicOptions, 
                                Set stubClasses, File destDir,
                                String repository)
        throws GeneratorException, IOException
    {

        if( (stubClasses.size() == 0) ) {
            _logger.log(Level.FINE,  "[EJBC] No code generation required");
            return;
        }

        progress(localStrings.getStringWithDefault(
                                         "generator.compiling_rmi_iiop", 
                                         "Compiling RMI-IIOP code."));

        List options = new ArrayList();
        List fileList = new ArrayList();

        options.addAll(rmicOptions);   

        options.add("-classpath");
        String bigClasspath = System.getProperty("java.class.path")
                            + File.pathSeparator + classPath 
                            + File.pathSeparator + repository;

        options.add(bigClasspath);
        options.add("-d");
        options.add(destDir.toString());

        for(Iterator extraIter = stubClasses.iterator(); 
            extraIter.hasNext();) {
            String next = (String) extraIter.next();
            _logger.log(Level.FINE,"[EJBC] rmic " + next + "...");
            fileList.add(next);
        }

        try {
            RMICompiler rmic = new RMICompiler(options, fileList);
            rmic.setClasspath(bigClasspath);
            rmic.compile();

        } catch(JavaCompilerException e) {
            _logger.log(Level.FINE,"ejbc.codegen_rmi_fail",e);
            String msg = 
                localStrings.getString("generator.rmic_compilation_failed");
            GeneratorException ge = new GeneratorException(msg);
            ge.initCause(e);
            throw ge;
        }

        if (_logger.isLoggable(Level.FINE)){
            StringBuffer sbuf = new StringBuffer();
            for(Iterator it = options.iterator(); it.hasNext(); ) {
                sbuf.append("\n\t").append(it.next());
            }
            for(Iterator it = fileList.iterator(); it.hasNext(); ) {
                sbuf.append("\n\t").append(it.next());
            }
            _logger.log(Level.FINE,"[EJBC] RMIC COMMAND: " + sbuf.toString());
        }        
        return;
    }

    /**
     * Compile .java files.
     *
     * @param    classPath    class path to be used during javac
     * @param    files        actual source files
     * @param    destDir      destination directory for .class files
     * @param    repository   repository for code generator
     * @param    javacOptions options for javac (-g or -O)
     *
     * @exception  GeneratorException  if an error while code compilation
     */
    public static void compileClasses(String classPath, Vector files, 
           File destDir, String repository, List javacOptions) 
        throws GeneratorException {

        List options	= new ArrayList();
        List fileList	= new ArrayList();

        if (files.size() <= 0) {
            return;
        }

        // adds the passed in javac options

        options.addAll(javacOptions);
        options.add("-d");
        options.add(destDir.toString());
        options.add("-classpath");
        options.add(System.getProperty("java.class.path")
                         + File.pathSeparator + classPath 
                         + File.pathSeparator  + repository);

        fileList.addAll(files);

        for(Iterator it = fileList.iterator(); it.hasNext(); )
        {
            String file = (String)it.next();
            _logger.log(Level.FINE,localStrings.getStringWithDefault(
                                    "generator.compile", 
                                    "Compiling {0} ...", new Object[] {file} ));
        }

        if (_logger.isLoggable(Level.FINE)) {
            StringBuffer sbuf = new StringBuffer();
            for ( Iterator it = options.iterator(); it.hasNext();  ) {
                sbuf.append("\n\t").append((String)it.next());
            }
            _logger.log(Level.FINE,"[EJBC] JAVAC COMMAND: " + sbuf.toString());
        }

        long start = System.currentTimeMillis();
        long end = start;

        try {
            JavaCompiler jc = new JavaCompiler(options, fileList);
            jc.compile();
        } catch(JavaCompilerException jce) {
            _logger.log(Level.FINE,"ejbc.codegen_compile_failed", jce);
            String msg = 
                localStrings.getStringWithDefault(
                    "generator.java_complilation_failed",
                    "Compilation failed: {0}",
                new Object[] {jce.getMessage()} );
            GeneratorException ge = new GeneratorException(msg);
            ge.initCause(jce);
            throw ge;
        }

        end = System.currentTimeMillis();
        _logger.log(Level.FINE,"JAVA compile time (" + fileList.size() 
                + " files) = " + (end - start));
    }

    /**
     * Assembles the name of the client jar files into the given vector.
     * 
     * @param    stubClasses  classes that required rmic
     * @param    allClientFiles    vector that contains all client jar files
     * @param    stubsDir          current stubsnskells dir for the app
     */
    private void addGeneratedFiles(Set stubClasses,
                                   Vector allClientFiles, File stubsDir) 
    {
        for (Iterator iter = stubClasses.iterator(); iter.hasNext();) {
            String next = (String) iter.next();
            String stubFile = stubsDir.toString() + File.separator + 
                                GeneratedNames.getStubName(next).replace('.', 
                                File.separatorChar) + ".class";
            allClientFiles.add(stubFile);
        }

        _logger.log(Level.FINE,
                    "[EJBC] Generated client files: " + allClientFiles);
    }   
     
    /** 
     * Constructs the client zip entries.
     *
     * @param    allClientFiles  all client stubs
     * @param    stubsDir        stubs directory for the current app
     *
     * @return   the client zip entries or an empty array if no stubs
     */
    private ZipItem[] getClientZipEntries(Vector allClientFiles, 
                                          File stubsDir) 
        throws IOException, ZipFileException {

        // number of client stubs
        final int CLIENT_SZ = allClientFiles.size();

        ZipItem[] zipEntries = new ZipItem[CLIENT_SZ];

        // string representaion of the stubs dir - please note that 
        // toString is used to convert the file object to string earlier.
        // So, canonical path should not be used here
        String stubsDirPath = stubsDir.toString();

        for (int i=0; i<CLIENT_SZ; i++) {
            String longName = (String) allClientFiles.elementAt(i);
            File file = new File(longName);

            _logger.log(Level.FINE,"[EJBC] stubs - >>"+longName);

            // coverts the file name to a jar entry name
            String entryName = "";
            if (longName.startsWith(stubsDirPath)) {
                entryName = longName.substring(stubsDirPath.length());
                if (entryName.charAt(0) == File.separatorChar) {
                    entryName = entryName.substring(1);
                }
            } else {
                // throw exception
                String msg = 
                    localStrings.getString("generator.unknown_class_prefix");
                throw new RuntimeException(msg);
            }
            // zip entry has forward slashes
            entryName = entryName.replace(File.separatorChar,'/');

            // create the zip entry
            zipEntries[i] = new ZipItem(file, entryName);
        }

        // returns the client stubs
        return zipEntries;
    }

    private Set getRemoteSuperInterfaces(ClassLoader jcl, 
                                         String homeRemoteIntf) 
        throws ClassNotFoundException {
 
        // all super interfaces of home or remote that need to be
        // processed for stubs.
        Set allSuperInterfaces = 
            TypeUtil.getSuperInterfaces(jcl, homeRemoteIntf,"java.rmi.Remote");

        Set remoteSuperInterfaces = new HashSet();

        Iterator iter = allSuperInterfaces.iterator();
        while (iter.hasNext()) {
            String intfName = (String) iter.next();
            Class  intfClass = jcl.loadClass(intfName);
            if ( java.rmi.Remote.class.isAssignableFrom(intfClass) &&
                 !(intfName.equals("javax.ejb.EJBHome")) &&
                 !(intfName.equals("javax.ejb.EJBObject")) ) {              
                remoteSuperInterfaces.add(intfName);           
            }
        }

        return remoteSuperInterfaces;
    }

/**
     * Returns the EJB Remote and Home interfaces that do not correspond
     * to an ejb *within* this application.  These will be used to generate
     * stubs for ejb clients so that no additional packaging is required by
     * the deployer in cases where the target ejb lives in another application.
     * 
     * @param    jcl         class loader for an app or stand alone module
     * @param    app  application to be searched for ejb client classes
     * @param    stubClasses  contains any classes that have
     *    already been identified as needing stubs generated for them.  
     *
     * @exception  IOException             if an i/o error
     * @exception  ClassNotFoundException  if a class is not available in 
     *                                     the class path
     */
    private Set getEjbClientStubClasses(ClassLoader jcl, 
                                   Application application, Set stubClasses)
        throws IOException, ClassNotFoundException
    {

        Set ejbClientStubClasses = new HashSet();
        final String BASE_HOME   = "javax.ejb.EJBHome";
        final String BASE_REMOTE = "javax.ejb.EJBObject";

        Vector ejbRefs = application.getEjbReferenceDescriptors();

        for (int i = 0; i < ejbRefs.size(); i++) {

            EjbReferenceDescriptor next = 
                (EjbReferenceDescriptor) ejbRefs.get(i);

            if( next.isLocal() || next.isEJB30ClientView() ) {
                continue;
            }

            String home   = next.getEjbHomeInterface();
            String remote = next.getEjbInterface();
           
            ejbClientStubClasses.add(home);
            Set homeSuperIntfs = getRemoteSuperInterfaces(jcl, home);
            ejbClientStubClasses.addAll(homeSuperIntfs);
                                                   
            ejbClientStubClasses.add(remote);
            Set remoteSuperIntfs = getRemoteSuperInterfaces(jcl, remote);
            ejbClientStubClasses.addAll(remoteSuperIntfs);                
        }

        return ejbClientStubClasses;
    }

    /**
     * Returns all the classes that require RMI-IIOP stubs.
     * 
     * @param    jcl class loader for an app or stand alone module
     * @param    ejbHomeInterfaces      all home interfaces
     * @param    ejbRemoteInterfaces    all remote interfaces
     * @param    remoteEjbDescriptors   remote ejbs that need stubs generated
     * @return   all classes requiring RMI-IIOPS stubs
     * @exception  IOException             if an i/o error
     * @exception  ClassNotFoundException  if a class is not available in 
     *                                     the class path
     */
    private Set getStubClasses(ClassLoader jcl,
                               Set ejbHomeInterfaces, Set ejbRemoteInterfaces,
                               List remoteEjbDescriptors)
            throws IOException, ClassNotFoundException
    {
      
        Set stubClasses     = new HashSet();
     
        for (Iterator iter = remoteEjbDescriptors.iterator(); iter.hasNext();)
        {

            EjbDescriptor desc = (EjbDescriptor) iter.next();

            String home   = desc.getHomeClassName();
            String remote = desc.getRemoteClassName();

            stubClasses.add(home);
            Set homeSuperIntfs = getRemoteSuperInterfaces(jcl, home);
            stubClasses.addAll(homeSuperIntfs);
                        
                        
            stubClasses.add(remote);
            Set remoteSuperIntfs = getRemoteSuperInterfaces(jcl, remote);
            stubClasses.addAll(remoteSuperIntfs);
            
        }       

        return stubClasses;
    }

    /**
     * Helper method - returns the class path as string with path separator.
     *
     * @param    paths      array of class paths
     * @param    other      additional directory to be added to the class path
     *
     * @return   class path for the given application
     */
    private String getClassPath(String[] paths, File other) {

        StringBuffer sb  = new StringBuffer();

        for (int i=0; i<paths.length; i++) {
            sb.append(paths[i]+File.pathSeparator);
        }

        if (other != null) {
            sb.append(other.toString());
        }

        return sb.toString();
    }

    /**
     * Generates and compiles the necessary impl classes, stubs and skels. 
     *
     * <pre>
     *
     * This method makes the following assumptions:
     *    - the deployment descriptor xmls are registered with Config
     *    - the class paths are registered with Config
     *
     * @@@
     * In case of re-deployment, the following steps should happen before:
     *    - rename the src dir from previous deployment (ex. /app/pet-old)
     *    - rename the stubs dir from previous deployment (ex. /stub/pet-old)
     *    - explode the ear file (ex. /app/petstore)
     *    - register the deployment descriptor xml with config
     *    - register the class path with config
     *
     * After successful completion of this method, the old src and sutbs 
     * directories may be deleted.
     *
     * </pre>
     *
     * @param    ejbcCtx   runtime environment for ejbc   
     *
     * @return   array of the client stubs files as zip items or empty array 
     * 
     * @exception  GeneratorException      if an error while code generation
     * @exception  ClassNotFoundException  if class not available in the 
     *                                     class path to be loaded
     * @exception  IOException             if an i/o error
     * @exception  CmpCompilerException    if an error from CMP compiler
     * @exception  Exception               other exceptions (?)
     */
    public static ZipItem[] ejbc(EjbcContext ejbcCtx)
            throws GeneratorException, ClassNotFoundException, IOException, 
                   CmpCompilerException, Exception
    {
        IASEJBC ejbc = new IASEJBC();
        return ejbc.doCompile(ejbcCtx);
    }

    private ZipItem[] doCompile(EjbcContext ejbcCtx) 
            throws GeneratorException, ClassNotFoundException, IOException, 
                   CmpCompilerException, Exception
    {
        
        // stubs dir for the current deployment 
        File stubsDir = ejbcCtx.getStubsDir();

        // deployment descriptor object representation
        Application application  = ejbcCtx.getDescriptor();

        long startTime = now();
        long time;	// scratchpad variable

        _logger.log(Level.FINE, "ejbc.begin",application.getRegistrationName());

        // class path to be used for this application during javac & rmic
        String classPath = getClassPath(ejbcCtx.getClasspathUrls(), stubsDir);

        // Warning: A class loader is passed in while constructing the 
        //          application object
        final ClassLoader jcl = application.getClassLoader();

        // creates the stubs dir if it does not exist
        if (!stubsDir.exists()) {
            stubsDir.mkdirs();
        }
        // stubs dir is used as repository for code generator 
        final String gnrtrTMP = stubsDir.getCanonicalPath();

        // previous thread context class loader
        final ClassLoader oContextCL = 
            Thread.currentThread().getContextClassLoader();

        // sets the thread context classloader for use by rmic etc.
        if(System.getSecurityManager() == null) {
            Thread.currentThread().setContextClassLoader(jcl);
        } else {
            java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(jcl);
                    return null;
                }
            });
        }


        // ---- CMP --------------------------------------------------------

        if (application.containsCMPEntity()) {
            CmpCompiler cmpc = new CmpCompiler(ejbcCtx);
            cmpc.compile();
        }

        // ---- END OF CMP -------------------------------------------------

        // ---- EJB DEPLOYMENT DESCRIPTORS -------------------------------

        Vector ejbRemoteDeploymentDescriptors  = new Vector();
        Set ejbHomeInterfaces         = new HashSet();
        Set ejbRemoteInterfaces       = new HashSet();
        Set<String> nonSerializableSfulClasses  = new HashSet();
        Set<String> ejb30RemoteBusinessInterfaces  = new HashSet();

        // Open the app using the JarClassLoader to load classes.
        // This allows the descriptors to find generated classes 
        // (e.g. the generated CMP bean class) as soon as they are 
        // compiled below.
        int ejbCount   = 0;
        Iterator iter  = application.getEjbDescriptors().iterator();


        // The main use-case we want to support is the one where existing
        // stand-alone java clients that access ejbs in our appserver
        // through CosNaming need the generated stubs.  We don't want to
        // force them to run rmic themselves so it's better for them
        // just to tell us during the deployment of an ejb client app
        // or ejb app that we should run rmic and put the stubs in the
        // client.jar.  Turning on the deployment-time rmic flag ONLY
        // controls the generation of rmic stubs.  It is independent of the
        // run-time decision about whether to use dynamic RMI stubs.  By
        // default, dynamic stubs will be used in the server, in 
        // the Application Client container, and in stand-alone clients
        // that instantiate our naming service.  If the server has been
        // explicitly configured for static RMI stubs by use of the
        // internal ORB system property, we will always call RMIC.
        boolean generateRmicStubs = 
            ( ejbcCtx.getDeploymentRequest().getGenerateRMIStubs() );
            // ||
	    //  !EJBUtils.getOrbUseDynamicStubs() );

        while (iter.hasNext()) {
            ejbCount++;
            EjbDescriptor next = (EjbDescriptor) iter.next();


            if( next.isLocalBusinessInterfacesSupported() ) {
                for(String nextBusIntfStr : 
                        next.getLocalBusinessClassNames() ) {
                    Class intf = jcl.loadClass(nextBusIntfStr);
                    if(javax.ejb.EJBLocalObject.class.isAssignableFrom(intf)) {
                        throw new GeneratorException("Invalid Local Business "
                           + "Interface " + intf + ". A Local Business " +
                         "interface MUST not extend javax.ejb.EJBLocalObject");
                    }
                }
            }

            if( next.isRemoteInterfacesSupported() ) {
                
                if( generateRmicStubs ) {
                    ejbRemoteDeploymentDescriptors.addElement(next);
                    ejbHomeInterfaces.add(next.getHomeClassName());
                    ejbRemoteInterfaces.add(next.getRemoteClassName());
                } else {
                    _logger.log(Level.FINE, 
                                "Skipping RMI-IIOP STUB generation for"
                                + " " + next.getName());
                }
            }
            
            if( next.isRemoteBusinessInterfacesSupported() ) {

                for(String nextIntf : next.getRemoteBusinessClassNames() ) {
                    // If there's more than one ejb with same
                    // Remote business interface, only generate
                    // the artifacts once.  This will work since
                    // there is nothing bean-specific about the 
                    // generated artifacts. Their only dependency is
                    // the corresponding Remote business interface.
                    if( !ejb30RemoteBusinessInterfaces.contains(nextIntf) ) {
                        ejb30RemoteBusinessInterfaces.add(nextIntf);
                    }
                }
            } 

            if( next.getType().equals(EjbSessionDescriptor.TYPE) &&
                ((EjbSessionDescriptor)next).isStateful() ) {

                Set<String> classNames = new HashSet<String>();
                classNames.add(next.getEjbClassName());
                classNames.addAll(next.getInterceptorClassNames());

                for(String className : classNames) {
                    Class clazz = jcl.loadClass(className);
                    if( !Serializable.class.isAssignableFrom(clazz) ) {
                        // Add for processing. Duplicates will be ignored
                        // by Set.
                        nonSerializableSfulClasses.add(className);
                    }
                }
            }
        }

        // Need to generate Remote 3.0 internal intf/wrappers for
        // EJB 3.0 Remote clients as well.  This will be removed
        // when we move to the new codegen API.  
        Vector ejbRefs = application.getEjbReferenceDescriptors();
        for (int i = 0; i < ejbRefs.size(); i++) {
            EjbReferenceDescriptor next = 
                (EjbReferenceDescriptor) ejbRefs.get(i);
            if( next.isEJB30ClientView() && !next.isLocal() ) {
                String busInterface = next.getEjbInterface();
                if( !ejb30RemoteBusinessInterfaces.contains(busInterface) ) {
                    ejb30RemoteBusinessInterfaces.add(busInterface);
                }
            }
        }

        progress(localStrings.getStringWithDefault
                 ("generator.processing_beans", "Processing beans..."));


        // ---- END OF EJB DEPLOYMENT DESCRIPTORS --------------------------

        // ---- LOCAL HOME & OBJECT ----------------------------------------

        FileArchive dArchive = new FileArchive();
        dArchive.open(gnrtrTMP);
        DeploymentContext context = new DeploymentContext(dArchive,application);

        // Generate code for Remote EJB 30 business interfaces
        Vector remote30Files = new Vector();

        if( EJBUtils.useStaticCodegen() ) {

            // Generic a single generic home interface for this application.
            Generator genericHomeGen = new GenericHomeGenerator
                (context.getClassLoader());
            
            generateCode(genericHomeGen, remote30Files, stubsDir);

            for (String businessIntf : ejb30RemoteBusinessInterfaces) {

                // generate RMI-IIOP version of Remote business interface
                Generator remoteGen = 
                    new RemoteGenerator(context.getClassLoader(),
                                        businessIntf);
                
                generateCode(remoteGen, remote30Files, stubsDir);
                
                Generator clientGen = new Remote30WrapperGenerator
                    (context.getClassLoader(), businessIntf, 
                     remoteGen.getGeneratedClass());
                
                generateCode(clientGen, remote30Files, stubsDir);
            }
            // log completion message 
            if (remote30Files.size() > 0) {
                
                // compile generated Remote business interfaces
                time = now();
                compileClasses(classPath, remote30Files, stubsDir, gnrtrTMP, 
                               ejbcCtx.getJavacOptions());
                ejbcCtx.getTiming().javaCompileTime += (now() - time);
            
                _logger.fine("Done generating Remote business intfs");
            }


            // Generate any serializable sub-classes for EJB 3.0 stateful 
            // session beans and stateful session bean interceptors that 
            // don't implement Serializable. These classes 
            // are not put in the client.jar.
            Vector serializableSfulSubClasses = new Vector();
            for(String className : nonSerializableSfulClasses) {
                Generator serializableSfulGen = 
                    new SerializableBeanGenerator(context.getClassLoader(), 
                                                  className);

                generateCode(serializableSfulGen, serializableSfulSubClasses,
                             stubsDir);
            }

            if( serializableSfulSubClasses.size() > 0 ) {
                // compile generated stateful serializable sub-classes
                time = now();
                compileClasses(classPath, serializableSfulSubClasses, stubsDir,
                               gnrtrTMP, ejbcCtx.getJavacOptions());
                           
                ejbcCtx.getTiming().javaCompileTime += (now() - time);
            
                _logger.fine("Generated Stateful Serializable subclasses");
                        
            }
        }

        // ---- WEB SERVICES -----------------------------------------------
        
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "ejbc.start_jaxrpc_generation", 
                        application.getRegistrationName());
        }
        time = now();

        JaxRpcCodegenFactory jaxrpcFactory = 
            JaxRpcCodegenFactory.newInstance();
        JaxRpcCodegenAdapter jaxrpcAdapter = jaxrpcFactory.getAdapter();
        jaxrpcAdapter.run(ejbcCtx);
        
        ejbcCtx.getTiming().jaxrpcGenerationTime += (now() - time);
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "ejbc.end_jaxrpc_generation", 
                        application.getRegistrationName());
        }      
        
        // this should not be here but in AppDeployer or such but since
        // the archive file is saved at then end of this ejbc process, and 
        // the servlet swith may require to save the DDs, I decided to put 
        // it here until we rework the codegen pluggability.
        WebServiceDeployer deployer = 
            new WebServiceDeployer(ejbcCtx.getDeploymentRequest());
        deployer.doWebServiceDeployment(ejbcCtx.getDescriptor(),  
                                        ejbcCtx.getSrcDir());
        
        
        // ---- END OF WEB SERVICES ---------------------------------------- 

        // ---- RMIC ALL STUB CLASSES --------------------------------------

        Set allStubClasses = new HashSet();

        if( generateRmicStubs ) {
            // stubs classes for ejbs within this app that need rmic
            Set ejbStubClasses = getStubClasses(jcl, ejbHomeInterfaces, 
                  ejbRemoteInterfaces, ejbRemoteDeploymentDescriptors);
            
            // stubs for any J2EE components within the app that are clients
            // of remote ejbs but where the target ejbs are not defined within
            // the app
            Set ejbClientStubClasses =
                getEjbClientStubClasses(jcl, application, ejbStubClasses);
                        
            allStubClasses.addAll(ejbStubClasses);
            allStubClasses.addAll(ejbClientStubClasses);
            
            // Compile and RMIC all Stubs
            
            time = now();
            compileAndRmic(classPath, ejbcCtx.getRmicOptions(), allStubClasses,
                           stubsDir, gnrtrTMP);
            
            ejbcCtx.getTiming().RMICompileTime += (now() - time);
        }

        // ---- END OF RMIC ALL STUB CLASSES -------------------------------
       
        // Create list of all server files and client files 
        Vector allClientFiles = new Vector();

        // assemble the client files
        addGeneratedFiles(allStubClasses, allClientFiles, stubsDir);

        if( remote30Files.size() > 0 ) {
            
            Iterator itr = remote30Files.iterator();
            if (itr != null) {
                for (;itr.hasNext();) {       
                    String file = (String) itr.next();
                    allClientFiles.add(file.replace(".java", ".class"));
                }
            }

        }
                          
        if (jaxrpcAdapter!=null) {
            Iterator itr = jaxrpcAdapter.getListOfBinaryFiles();
            if (itr!=null) {
                for (;itr.hasNext();) {                    
                    allClientFiles.add(itr.next());
                }
            }
        }

        // client zip entries
        ZipItem[] clientStubs = getClientZipEntries(allClientFiles, stubsDir);

        _logger.log(Level.FINE, "ejbc.end", application.getRegistrationName());
        ejbcCtx.getTiming().totalTime = now() - startTime;

        // sets the old thread context classloader back
        // this allows the EJB class loader to be garbage collected
        if(System.getSecurityManager() == null) {
            Thread.currentThread().setContextClassLoader(oContextCL);
        } else {
            java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(oContextCL);
                    return null;
                }
            }
            );
        }

        /*
         *Clean up, releasing the class loader.
         */
        jaxrpcAdapter.done();
        
        return clientStubs;
    }

    private long now()
    {
        return System.currentTimeMillis();
    }

    private void progress(String message) {
            try {
                _logger.log(Level.FINE, message);
            } catch(Throwable t) {
                _logger.log(Level.FINER,"Cannot set status message",t);
            }
    }

}
