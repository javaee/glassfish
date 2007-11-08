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

package com.sun.enterprise.ddgenerator.sun;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;

/**
 * This class is responsible for writing deployment descriptors 
 * after a deployment action has occured to a abstract archive instance.
 *
 * @author  Jerome Dochez
 */
public class DescriptorArchivist {
    
    /** Creates a new instance of DescriptorArchivist */
    public DescriptorArchivist() {
    }
  
    /**
     * writes an application deployment descriptors
     * @param the application object
     * @param the abstract archive
     */
    public void write(Application application, AbstractArchive out) 
        throws IOException 
    {
        if (application.isVirtual()) {
            ModuleDescriptor aModule = (ModuleDescriptor) application.getModules().next();
            Archivist moduleArchivist = ArchivistFactory.getArchivistForType(aModule.getModuleType());
            write(aModule.getDescriptor(), moduleArchivist,  out);
        } else {
            // this is a real application.
            
            // let's start by writing out all submodules deployment descriptors
            for (Iterator modules = application.getModules();modules.hasNext();) {
                ModuleDescriptor aModule = (ModuleDescriptor) modules.next();
                Archivist moduleArchivist = ArchivistFactory.getArchivistForType(aModule.getModuleType());
                if (aModule.getAlternateDescriptor()!=null) {
                    // the application is using alternate deployment descriptors
                    String runtimeDDPath = "sun-" + aModule.getAlternateDescriptor();
                    DeploymentDescriptorFile ddFile = moduleArchivist.getConfigurationDDFile();
                    if (ddFile!=null) {
                        OutputStream os = out.putNextEntry(runtimeDDPath);
                        ddFile.write(aModule.getDescriptor(), os);
                        out.closeEntry();
                    }
                } else {
                    AbstractArchive moduleArchive = out.getEmbeddedArchive(aModule.getArchiveUri());
                    write(aModule.getDescriptor(),  moduleArchivist, moduleArchive);
                }
            }
            
            // now let's write the application runtime descriptor
            ApplicationArchivist archivist = new ApplicationArchivist();
            archivist.setDescriptor(application);
            archivist.writeRuntimeDeploymentDescriptors(out);                        
        }
    }
    
    /**
     * writes a bundle descriptor 
     * @param the bundle descriptor
     * @param the abstract archive
     */
    public void write(BundleDescriptor bundle, AbstractArchive out) 
        throws IOException
    {
        Archivist archivist = ArchivistFactory.getArchivistForArchive(out);
        write(bundle, archivist, out);
    }
    
    /**
     * writes a bundle descriptor
     * @param the bundle descriptor
     * @param the archivist responsible for writing such bundle type
     * @param the archive to write to
     */
    protected void write(BundleDescriptor bundle, Archivist archivist, AbstractArchive out) 
        throws IOException
    {
        
        archivist.setDescriptor(bundle);
        archivist.writeRuntimeDeploymentDescriptors(out);
        
        // now if this is a web bundle descriptor, we may need to rewrite the standard 
        // deployment descriptors if we have web services since the servlet implementation
        // has been switched
        if (bundle.getModuleType().equals(ModuleType.WAR)) {
            WebBundleDescriptor webBundle = (WebBundleDescriptor) bundle;
            if (webBundle.hasWebServices()) {
                archivist.writeStandardDeploymentDescriptors(out);
            }
        }
    }
}
