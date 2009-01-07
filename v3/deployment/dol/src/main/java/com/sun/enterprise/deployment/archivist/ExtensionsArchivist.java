package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.apf.Scanner;
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

    public Scanner getScanner() {
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
                    logger.logp(Level.FINE, "Archivist",
                            "readPersistenceDeploymentDescriptor",
                            "{0} does not contain {1}, so it is not a {2} module",
                            new Object[]{archive.getURI(),
                                    confDD.getDeploymentDescriptorPath(),
                                    getModuleType()});
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
}
