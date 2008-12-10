package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.PersistenceDeploymentDescriptorFile;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.xml.sax.SAXParseException;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Dec 8, 2008
 * Time: 12:34:02 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class PersistenceArchivist extends ExtensionsArchivist {

    protected final Logger logger = LogDomains.getLogger(PersistenceArchivist.class, LogDomains.DPL_LOGGER);
    
    public DeploymentDescriptorFile getStandardDDFile(RootDeploymentDescriptor descriptor) {
        return new PersistenceDeploymentDescriptorFile();
    }

    public DeploymentDescriptorFile getConfigurationDDFile(RootDeploymentDescriptor descriptor) {
        return null; 
    }

    public XModuleType getModuleType() {
        return XModuleType.Persistence;
    }

    public boolean supportsModuleType(XModuleType moduleType) {
        return (XModuleType.CAR == moduleType || XModuleType.EJB == moduleType);
    }

    @Override
    public Object open(Archivist main, ReadableArchive archive, RootDeploymentDescriptor descriptor)
            throws IOException, SAXParseException {

        return readPersistenceDeploymentDescriptor(main, archive, "", descriptor);
    }

    protected PersistenceUnitsDescriptor readPersistenceDeploymentDescriptor(Archivist main,
            ReadableArchive subArchive, String puRoot, RootDeploymentDescriptor descriptor)
            throws IOException, SAXParseException {

        final String subArchiveURI = subArchive.getURI().getSchemeSpecificPart();
        if (logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "Archivist",
                    "readPersistenceDeploymentDescriptor",
                    "PURoot = [{0}] subArchive = {1}",
                    new Object[]{puRoot, subArchiveURI});
        }
        if (descriptor.getPersistenceUnitsDescriptor(puRoot) != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINE, "Archivist",
                        "readPersistenceDeploymentDescriptor",
                        "PU has been already read for = {0}",
                        subArchiveURI);
            }
            return null;
        }
        PersistenceUnitsDescriptor persistenceUnitsDescriptor =
                    PersistenceUnitsDescriptor.class.cast(super.open(main, subArchive, descriptor));

        if (persistenceUnitsDescriptor!=null) {
            descriptor.addPersistenceUnitsDescriptor(puRoot,
                    persistenceUnitsDescriptor);
        }

        return persistenceUnitsDescriptor;
    }    
}
