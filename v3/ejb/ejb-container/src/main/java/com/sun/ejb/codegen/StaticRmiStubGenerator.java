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

import java.lang.reflect.Method;
import java.io.*;
import java.util.*;
import com.sun.ejb.EJBUtils;

import static java.lang.reflect.Modifier.*;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.loader.util.ASClassLoaderUtil;
import com.sun.enterprise.deployment.util.TypeUtil;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.Habitat;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;

import org.glassfish.api.deployment.DeployCommandParameters;


/**
 * This class is used to generate the RMI-IIOP version of a 
 * remote business interface.
 */

public class StaticRmiStubGenerator {

    private static final LocalStringManagerImpl localStrings =
	    new LocalStringManagerImpl(StaticRmiStubGenerator.class);

     private static final Logger _logger =
                LogDomains.getLogger(StaticRmiStubGenerator.class, LogDomains.EJB_LOGGER);

     private static final String ORG_OMG_STUB_PREFIX  = "org.omg.stub.";


    /**
     * This class is only instantiated internally.
     */
    public StaticRmiStubGenerator() { }

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
     * @param    deploymentCtx
     *
     * @return   array of the client stubs files as zip items or empty array
     *
     */
    public void /* ZipItem[] */ ejbc(Habitat h, DeploymentContext deploymentCtx) throws Exception {

        // stubs dir for the current deployment
        File stubsDir = deploymentCtx.getScratchDir("ejb");

        // deployment descriptor object representation
        EjbBundleDescriptor ejbBundle  = deploymentCtx.getModuleMetaData(EjbBundleDescriptor.class);

        long startTime = now();
        long time;	// scratchpad variable

        // class path to be used for this application during javac & rmic
        String classPath =  ASClassLoaderUtil.getModuleClassPath(h, deploymentCtx);

        // getClassPath(ejbcCtx.getClasspathUrls(), stubsDir);

        // Warning: A class loader is passed in while constructing the
        //          application object
        final ClassLoader jcl = ejbBundle.getClassLoader();

        // stubs dir is used as repository for code generator
        final String gnrtrTMP = stubsDir.getCanonicalPath();

        // ---- EJB DEPLOYMENT DESCRIPTORS -------------------------------

        // The main use-case we want to support is the one where existing
        // stand-alone java clients that access ejbs hosted in our appserver
        // directly through CosNaming need the generated stubs.  We don't want to
        // force them to run rmic themselves so it's better for them
        // just to tell us during the deployment of an ejb client app
        // or ejb app that we should run rmic and put the stubs in the
        // client.jar.  Turning on the deployment-time rmic flag ONLY
        // controls the generation of rmic stubs.  Dynamic stubs will be used
        // in the server, in the Application Client container, and in
        // stand-alone clients that instantiate our naming provider.  
        DeployCommandParameters dcp =
                deploymentCtx.getCommandParameters(DeployCommandParameters.class);
        boolean generateRmicStubs = dcp.generatermistubs;


        //progress(localStrings.getStringWithDefault
          //       ("generator.processing_beans", "Processing beans..."));


        // ---- END OF EJB DEPLOYMENT DESCRIPTORS --------------------------

        // ---- LOCAL HOME & OBJECT ----------------------------------------

        /**         TODO
        FileArchive dArchive = new FileArchive();
        dArchive.open(gnrtrTMP);
        DeploymentContext context = new DeploymentContext(dArchive,application);

         */

        // Generate code for Remote EJB 30 business interfaces
        Vector remote30Files = new Vector();

        // ---- RMIC ALL STUB CLASSES --------------------------------------

        Set allStubClasses = new HashSet();

        if( generateRmicStubs ) {

            // stubs classes for ejbs within this app that need rmic
            Set ejbStubClasses = getStubClasses(jcl, ejbBundle);


            allStubClasses.addAll(ejbStubClasses);

            // Compile and RMIC all Stubs

            time = now();

            //compileAndRmic(classPath, ejbcCtx.getRmicOptions(), allStubClasses,
              //             stubsDir, gnrtrTMP);

            //ejbcCtx.getTiming().RMICompileTime += (now() - time);
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



        /**                   TODO
        // client zip entries
        ZipItem[] clientStubs = getClientZipEntries(allClientFiles, stubsDir);

        _logger.log(Level.FINE, "ejbc.end", application.getRegistrationName());
        ejbcCtx.getTiming().totalTime = now() - startTime;

         */


        return;
    }

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
    private void rmic(String classPath, List rmicOptions,
                                Set stubClasses, File destDir,
                                String repository)
        throws GeneratorException, IOException
    {

        if( (stubClasses.size() == 0) ) {
            _logger.log(Level.FINE,  "[EJBC] No code generation required");
            return;
        }

        /**
        progress(localStrings.getStringWithDefault(
                                         "generator.compiling_rmi_iiop",
                                         "Compiling RMI-IIOP code."));
         */

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

        /** TODO

        try {

             * RMICompiler rmic = new RMICompiler(options, fileList);
            rmic.setClasspath(bigClasspath);
            rmic.compile();


        } catch(JavaCompilerException e) {
            _logger.log(Level.FINE,"ejbc.codegen_rmi_fail",e);
             String msg =
                "generator.rmic_compilation_failed";
            GeneratorException ge = new GeneratorException(msg);
            ge.initCause(e);
            throw ge;
        }

         */

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
                                getStubName(next).replace('.',
                                File.separatorChar) + ".class";
            allClientFiles.add(stubFile);
        }

        _logger.log(Level.FINE,
                    "[EJBC] Generated client files: " + allClientFiles);
    }

      
    private String getStubName(String fullName) {

        String className = fullName;
        String packageName = "";

        int lastDot = fullName.lastIndexOf('.');
        if (lastDot != -1) {
            className   = fullName.substring(lastDot+1, fullName.length());
            packageName = fullName.substring(0, lastDot+1);
        }

        String stubName = packageName + "_" + className + "_Stub";

		if(isSpecialPackage(fullName))
            stubName = ORG_OMG_STUB_PREFIX + stubName;

        return stubName;
    }

    private boolean isSpecialPackage(String name)
	{
		// these package names are magic.  RMIC puts any home/remote stubs
		// into a different directory in these cases.
		// 4845896  bnevins, April 2003

		// this is really an error.  But we have enough errors. Let's be forgiving
		// and not allow a NPE out of here...
		if(name == null)
			return false;

		// Licensee bug 4959550
		// if(name.startsWith("com.sun.") || name.startsWith("javax."))
		if(name.startsWith("javax.")) {
			return true;
        }

		return false;
	}

    /**
     * Constructs the client zip entries.
     *
     * @param    allClientFiles  all client stubs
     * @param    stubsDir        stubs directory for the current app
     *
     * @return   the client zip entries or an empty array if no stubs
     */
    /**    TODO
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
     **/

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


    private Set getStubClasses(ClassLoader jcl,
                              EjbBundleDescriptor ejbBundle)
            throws IOException, ClassNotFoundException
    {

        Set stubClasses     = new HashSet();

        for (Iterator iter = ejbBundle.getEjbs().iterator(); iter.hasNext();)
        {

            EjbDescriptor desc = (EjbDescriptor) iter.next();

            if( desc.isRemoteInterfacesSupported() ) {

                String home   = desc.getHomeClassName();
                String remote = desc.getRemoteClassName();

                stubClasses.add(home);
                Set homeSuperIntfs = getRemoteSuperInterfaces(jcl, home);
                stubClasses.addAll(homeSuperIntfs);


                stubClasses.add(remote);
                Set remoteSuperIntfs = getRemoteSuperInterfaces(jcl, remote);
                stubClasses.addAll(remoteSuperIntfs);

            }

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

