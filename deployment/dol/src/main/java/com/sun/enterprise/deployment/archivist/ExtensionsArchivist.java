/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.xml.sax.SAXParseException;
import org.jvnet.hk2.annotations.Contract;

import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * An extension archivist is processing extensions deployment descriptors like
 * web services, persistence or even EJB information within a war file.
 *
 * They do not represent a top level archivist, as it is not capable of loading
 * BundleDescriptors directly but require a top level archivist to do so before
 * they can process their own metadata
 *
 * @author Jerome Dochez
 */
@Contract
public abstract class ExtensionsArchivist  {

    protected final Logger logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);

    public abstract DeploymentDescriptorFile getStandardDDFile(RootDeploymentDescriptor descriptor);

    public abstract DeploymentDescriptorFile getConfigurationDDFile(RootDeploymentDescriptor descriptor);

    public abstract XModuleType getModuleType();

    public abstract boolean supportsModuleType(XModuleType moduleType);

    public abstract <T extends RootDeploymentDescriptor> T getDefaultDescriptor();

    public ModuleScanner getScanner() {
        return null;
    }

    public <T extends RootDeploymentDescriptor> void addExtension(RootDeploymentDescriptor root, RootDeploymentDescriptor extension) {
        root.addExtensionDescriptor(extension.getClass(), extension, null);
        extension.setModuleDescriptor(root.getModuleDescriptor());
    }

    public Object open(Archivist main, ReadableArchive archive, RootDeploymentDescriptor descriptor)
            throws IOException, SAXParseException {


        DeploymentDescriptorFile confDD = getStandardDDFile(descriptor);
         if (archive.getURI() != null) {
             confDD.setErrorReportingString(archive.getURI().getSchemeSpecificPart());
         }
         InputStream is = null;
         try {
             is = archive.getEntry(confDD.getDeploymentDescriptorPath());
             if (is == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Deployment descriptor: " +
                                    confDD.getDeploymentDescriptorPath(),
                                    " does not exist in archive: " + 
                                    archive.getURI().getSchemeSpecificPart());
                }

             } else {
                 confDD.setXMLValidation(main.getXMLValidation());
                 confDD.setXMLValidationLevel(main.getXMLValidationLevel());
                 return confDD.read(descriptor, is);
             }
         } finally {
             if (is != null) {
                 is.close();
             }
         }
         return null;
     }

     public Object readRuntimeDeploymentDescriptor(Archivist main, ReadableArchive archive, RootDeploymentDescriptor descriptor)
            throws IOException, SAXParseException {

        DeploymentDescriptorFile confDD = getConfigurationDDFile(descriptor);

        // if this extension archivist has no runtime DD, just return the 
        // original descriptor
        if (confDD == null) {
            return descriptor;
        }

        if (archive.getURI() != null) {
            confDD.setErrorReportingString(archive.getURI().getSchemeSpecificPart());
        }
        InputStream is = null;
        try {
            is = archive.getEntry(confDD.getDeploymentDescriptorPath());
            if (is != null) {
                confDD.setXMLValidation(main.getRuntimeXMLValidation());
                confDD.setXMLValidationLevel(main.getRuntimeXMLValidationLevel());
                return confDD.read(descriptor, is);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }

}
