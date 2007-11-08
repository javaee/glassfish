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

import java.io.File;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.logging.LogDomains;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbCMPEntityDescriptor;
import com.sun.enterprise.deployment.backend.DeploymentUtils;

import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.deployment.runtime.IASEjbExtraDescriptors;
import com.sun.enterprise.deployment.IASEjbCMPEntityDescriptor;
import com.sun.enterprise.deployment.runtime.IASPersistenceManagerDescriptor;


/**
 * Generates concrete impls for CMP beans in an archive. 
 *
 * @author Nazrul Islam
 * @since  JDK 1.4
 */
class CmpCompiler {

    /**
     * Constructor!
     *
     * @param   ctx   object encapsulating ejbc runtime information
     */
    CmpCompiler(EjbcContext ctx) {
        this._ejbcCtx = ctx;
    }

    /**
     * Generates the concrete impls for all CMPs in the application.
     *
     * @throws GeneratorException  if this exception was thrown while generating concrete impls
     * @throws CmpCompilerException  if any other error occurs while generating concrete impls
     */
    void compile() throws CmpCompilerException, GeneratorException {
        
        // deployment descriptor object representation for the archive
        Application application = null;

        // deployment descriptor object representation for each module
        EjbBundleDescriptor bundle = null;

        // ejb name
        String beanName = null; 

        // GeneratorException message if any
        StringBuffer generatorExceptionMsg = null; 

        try {
            // scratchpad variable
            long time; 

            // stubs dir for the current deployment 
            File stubsDir = this._ejbcCtx.getStubsDir();

            application = this._ejbcCtx.getDescriptor();

            _logger.log(Level.FINE, "ejbc.processing_cmp", 
                        application.getRegistrationName());

            Vector cmpFiles = new Vector();
            final ClassLoader jcl = application.getClassLoader();

            // For each Bundle  descriptor generate concrete class.
            Iterator bundleItr = 
                application.getEjbBundleDescriptors().iterator();

            while ( bundleItr.hasNext() ) {

                bundle = (EjbBundleDescriptor)bundleItr.next();

                if (!bundle.containsCMPEntity()) {
                    continue;
                }
                
                // If it is a stand alone module then the srcDir is 
                // the ModuleDirectory
                String archiveUri = (!application.isVirtual()) ?
                    DeploymentUtils.getEmbeddedModulePath(
                            this._ejbcCtx.getSrcDir().getCanonicalPath(),
                            bundle.getModuleDescriptor().getArchiveUri()):
                    this._ejbcCtx.getSrcDir().getCanonicalPath();

                if (com.sun.enterprise.util.logging.Debug.enabled) {
                    _logger.log(Level.FINE,"[CMPC] Module Dir name is "
                            + archiveUri);
                }

                String generatedXmlsPath = (!application.isVirtual()) ?
                    DeploymentUtils.getEmbeddedModulePath(
                            this._ejbcCtx.getDeploymentRequest().
                            getGeneratedXMLDirectory().getCanonicalPath(), 
                            bundle.getModuleDescriptor().getArchiveUri()):
                    this._ejbcCtx.getDeploymentRequest().
                        getGeneratedXMLDirectory().getCanonicalPath();

                if (com.sun.enterprise.util.logging.Debug.enabled) {
                    _logger.log(Level.FINE,"[CMPC] Generated XML Dir name is "
                            + generatedXmlsPath);
                }

                IASPersistenceManagerDescriptor pmDesc = 
                    bundle.getPreferredPersistenceManager();

                String  generatorName = null;
                if (null == pmDesc) {
                    generatorName = IASPersistenceManagerDescriptor.PM_CLASS_GENERATOR_DEFAULT; 

                } else {
                    generatorName = pmDesc.getPersistenceManagerClassGenerator();

                    // Backward compatability:
                    // Support existing settings that have the old name.
                    if (generatorName.equals(
			IASPersistenceManagerDescriptor.PM_CLASS_GENERATOR_DEFAULT_OLD)) {

                        generatorName = IASPersistenceManagerDescriptor.PM_CLASS_GENERATOR_DEFAULT;
                    }
                }

                CMPGenerator gen = null;

                try {
		    Class generator = getClass().getClassLoader().loadClass(generatorName);
                    gen = (CMPGenerator)generator.newInstance();

                } catch (Throwable e) {
                    String msg = localStrings.getString("cmpc.cmp_generator_class_error",
                            application.getRegistrationName(), 
                            bundle.getModuleDescriptor().getArchiveUri());
                    _logger.log(Level.SEVERE, msg, e);
                    generatorExceptionMsg = addGeneratorExceptionMessage(msg, 
                            generatorExceptionMsg);

                    continue;
                }

                try {
                    time = now();
                    gen.init(bundle, _ejbcCtx, archiveUri, generatedXmlsPath);
                    this._ejbcCtx.getTiming().cmpGeneratorTime += (now() - time);
    			
                    Iterator ejbs=bundle.getEjbs().iterator();
    
                    while ( ejbs.hasNext() ) {
    
                        EjbDescriptor desc = (EjbDescriptor) ejbs.next();
                        beanName = desc.getName();

                        if (com.sun.enterprise.util.logging.Debug.enabled) {
                            _logger.log(Level.FINE,"[CMPC] Ejb Class Name: "
                                               + desc.getEjbClassName());
                        }
    
                        if ( desc instanceof IASEjbCMPEntityDescriptor ) {
    
                            // generate concrete CMP class implementation
                            IASEjbCMPEntityDescriptor entd = 
                                (IASEjbCMPEntityDescriptor)desc;
    
                            if (com.sun.enterprise.util.logging.Debug.enabled) {
                                _logger.log(Level.FINE,
                                    "[CMPC] Home Object Impl name  is "
                                    + entd.getLocalHomeImplClassName());
                            }
    
                            // generate persistent class
                            entd.setClassLoader(jcl);
    				
                            try {
                                time = now();
                                gen.generate(entd, stubsDir, stubsDir);
                                this._ejbcCtx.getTiming().cmpGeneratorTime += 
                                                                (now() - time);
    
                            } catch (GeneratorException e) {
                                String msg = e.getMessage();
                                _logger.log(Level.WARNING, msg);
                                generatorExceptionMsg = addGeneratorExceptionMessage(
                                        msg, generatorExceptionMsg);
                            } 

                        /* WARNING: IASRI 4683195
                         * JDO Code failed when there was a relationship involved
                         * because it depends upon the orginal ejbclasname and hence
                         * this code is shifted to just before the Remote Impl is
                         * generated.Remote/Home Impl generation depends upon this
                         * value
                         */
    
                        } else if (desc instanceof EjbCMPEntityDescriptor ) {
                                //RI code here
                        }

                    } // end while ejbs.hasNext()

                    beanName = null;

                    time = now();
                    Collection col = gen.cleanup();
                    this._ejbcCtx.getTiming().cmpGeneratorTime += (now() - time);
    
                    for (Iterator fileIter=col.iterator();fileIter.hasNext();) {
                        File file=(File)fileIter.next();
                        String fileName=file.getPath();
                        _logger.log(Level.FINE,"[CMPC] File name is "+fileName);
                        cmpFiles.addElement(fileName);
                    }
                    
                } catch (GeneratorException e) {
                    String msg = e.getMessage();
                    _logger.log(Level.WARNING, msg);
                    generatorExceptionMsg = addGeneratorExceptionMessage(msg, 
                            generatorExceptionMsg);
                } 

            } // end of bundle

            bundle = null;

            if (generatorExceptionMsg == null) {
                // class path for javac
                String classPath = 
                    getClassPath(this._ejbcCtx.getClasspathUrls(), stubsDir);

                time = now();
                IASEJBC.compileClasses(classPath, cmpFiles, stubsDir, 
                                   stubsDir.getCanonicalPath(), 
                                   this._ejbcCtx.getJavacOptions());

                this._ejbcCtx.getTiming().javaCompileTime += (now() - time);

                _logger.log(Level.FINE, "ejbc.done_processing_cmp", 
                        application.getRegistrationName());
             }

        } catch (GeneratorException e) {
            _logger.log(Level.WARNING, e.getMessage());
            throw e;

        } catch (Throwable e) {
            String eType = e.getClass().getName();
            String appName = application.getRegistrationName();
            String exMsg = e.getMessage();

            String msg = null;
            if (bundle == null) {
                // Application or compilation error
                msg = localStrings.getString("cmpc.cmp_app_error",
                    eType, appName, exMsg);
            } else {
                String bundleName = bundle.getModuleDescriptor().getArchiveUri();
                if (beanName == null) {
                    // Module processing error
                    msg = localStrings.getString("cmpc.cmp_module_error",
                        eType, appName, bundleName, exMsg);
                } else {
                    // CMP bean generation error
                    msg = localStrings.getString("cmpc.cmp_bean_error",
                        new Object[] {eType, beanName, appName, bundleName, exMsg});
                }
            }

            _logger.log(Level.SEVERE, msg, e);

            throw new CmpCompilerException(msg);
        }

        if (generatorExceptionMsg != null) {
            // We already logged each separate part.
            throw new GeneratorException(generatorExceptionMsg.toString());
        }
    }

    /**
     * Helper method. Returns the current system time. 
     *
     * @return  current system time
     */
    private long now() {
		return System.currentTimeMillis();
	}

    /**
     * Returns the name of the ejb module for the given bundle descriptor.
     *
     * <xmp>
     *    ejb-jar-ic_jar  (from ConverterApp example)
     * </xmp>
     *
     * @param    bundle  ejb bundle descriptor
     * @return           the name of the ejb dir module
     */
    private static String getModuleDirName(EjbBundleDescriptor bundle)
    {
        String archieveuri = bundle.getModuleDescriptor().getArchiveUri();
        return FileUtils.makeFriendlyFilename(archieveuri);
    }

    /**
     * Helper method - returns the class path as string with path separator.
     *
     * @param    paths      array of class paths
     * @param    other      additional directory to be added to the class path
     *
     * @return   class path for the given application
     */
    private static String getClassPath(String[] paths, File other) {

        StringBuffer sb  = new StringBuffer();

        for (int i=0; i<paths.length; i++) {
            sb.append(paths[i]+File.pathSeparator);
        }

        if (other != null) {
            sb.append(other.toString());
        }

        return sb.toString();
    }

    /** Adds GeneratorException message to the buffer.
     *
     * @param	msg 	the message text to add to the buffer.
     * @param	buf	the buffer to use.
     * @return	the new or updated buffer.
     */
    private StringBuffer addGeneratorExceptionMessage(String msg, StringBuffer buf) {
        StringBuffer rc = buf;
        if (rc == null) 
            rc = new StringBuffer(msg);
        else 
            rc.append('\n').append(msg);

        return rc;
    }

    // ---- VARIABLE(S) - PRIVATE --------------------------------------
    private EjbcContext _ejbcCtx         = null;
    private static final Logger _logger  = 
                LogDomains.getLogger(LogDomains.DPL_LOGGER);
    private static final StringManager localStrings =
                    StringManager.getManager(CmpCompiler.class);

}
