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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.util.io.FileUtils;
import org.jvnet.glassfish.api.ContractProvider;
import org.jvnet.glassfish.api.deployment.archive.ArchiveHandler;
import org.jvnet.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import javax.enterprise.deploy.shared.ModuleType;
import java.io.*;


/**
 * this class is responsible for exploding a J2EE archive file into
 * a directory/file based structures that is used by the deployment
 * and app server runtime.
 *
 * @author Jerome Dochez
 */

@Service
public class J2EEModuleExploder extends ModuleExploder implements ContractProvider, PostConstruct {

	private static String validationLevel = null;

    @Inject
    Config config;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    ArchivistFactory archivistFactory;

    public void postConstruct() {
        validationLevel = config.getAdminService().getDasConfig().getDeployXmlValidation();
    }

    /**
     * explode the passed archive file inside the directory
     * <p>
     * This is the original signature for this method.  It now delegates to the other variant
     * but specifies the directory argument as also the directory into which to expand nested jar
     * file contents.  This preserves the original behavior of the method.
     *
     * @param archive the archive
     * @param directory the destination directory
     * @param moduleName the module name for the archive
     * @throws IOException for any exception related to reading and writing
     * @throws IASDeploymentException when deployment descriptors cannot be read
     */
    public void explode(File archive, File directory, String moduleName)
    throws IOException, IASDeploymentException {
        explode(archive, directory, moduleName, false);
    }
    
    /**
     * explode the passed archive file inside the directory
     * @param archive the archive
     * @param directory the destination directory
     * @param moduleName the module name for the archive
     * @param preserveManifest whether to prevent manifests from exploded nested jar files from overwriting the archive's manifest
     * @throws IOException for any exception related to reading and writing
     * @throws IASDeploymentException when deployment descriptors cannot be read
     */
    public void explode(File archive, File directory, String moduleName, boolean preserveManifest)
    throws IOException, IASDeploymentException {
        assert archive != null;
        
        
        ReadableArchive source = archiveFactory.openArchive(archive);
        
        // now copy the archive, let the archivist do the job...
        Archivist archivist;
        try {

            ArchiveHandler handler = archivistFactory.getArchivist(source);
            if (handler == null) {
                String msg = localStrings.getString
                        ("enterprise.deployment.backend.no_archivist_recognized_arch",
                        archive.getAbsolutePath()
                        );
                throw new IASDeploymentException(msg);
            }
            archivist = archivistFactory.getArchivist(handler);
        } catch (IOException ioe) {
            String msg = localStrings.getString
                    ("enterprise.deployment.backend.error_getting_archivist",
                    archive.getAbsolutePath()
                    );
            throw new IASDeploymentException(msg, ioe);
        }
        if (!archivist.getModuleType().equals(ModuleType.EAR)) {
            explodeModule(source, directory, preserveManifest);
        }
    }

    public static Application explodeEar(File source, File destination) throws Exception {
        
        // first explode the ear file
        explodeJar(source, destination);
        
        // now we need to load the application standard deployment descriptor.
        ApplicationArchivist archivist = new ApplicationArchivist();
        archivist.setXMLValidationLevel(validationLevel);
        FileArchive appArchive = new FileArchive();
        appArchive.open(destination.getAbsolutePath());
        
        archivist.setManifest(appArchive.getManifest());
        
        // read the standard deployment descriptors
        Application appDesc;
        if (archivist.hasStandardDeploymentDescriptor(appArchive)) {
            appDesc = archivist.readStandardDeploymentDescriptor(appArchive);
        } else {
            appDesc = Application.createApplication(appArchive,true);
        }

        archivist.setDescriptor(appDesc);
        
        // ok we should now have the list of modules, so we can happily explode them...
        for (ModuleDescriptor<BundleDescriptor> bundle : appDesc.getModules()) {
            
            String moduleName = bundle.getArchiveUri();
            String massagedModuleName =  FileUtils.makeFriendlyFilename(moduleName);
            File archiveFile = new File(destination, moduleName);
            File moduleDir = new File(destination, massagedModuleName);
            explodeJar(archiveFile, moduleDir);
            
            // delete the original module file
            archiveFile.delete();
        }
        
        return appDesc;
    }
}
