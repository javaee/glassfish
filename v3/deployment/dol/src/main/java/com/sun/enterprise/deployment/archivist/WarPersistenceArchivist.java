package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.util.XModuleType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.Archive;
import org.xml.sax.SAXParseException;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;

@Service
public class WarPersistenceArchivist extends PersistenceArchivist {

    @Override
    public boolean supportsModuleType(XModuleType moduleType) {
        return XModuleType.WAR==moduleType;
    }

    @Override
    public Object open(Archivist main, ReadableArchive warArchive, RootDeploymentDescriptor descriptor) throws IOException, SAXParseException {
        final String CLASSES_DIR = "WEB-INF/classes/";

        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "EjbArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    warArchive.getURI());
        }
        Map<String, ReadableArchive> probablePersitenceArchives =  new HashMap<String, ReadableArchive>();
        try {
            SubArchivePURootScanner warLibScanner = new SubArchivePURootScanner() {
                String getPathOfSubArchiveToScan() {
                    return "WEB-INF/lib";
                }
            };
            probablePersitenceArchives = getProbablePersistenceRoots(warArchive, warLibScanner);

            final String pathOfPersistenceXMLInsideClassesDir = CLASSES_DIR+ DescriptorConstants.PERSISTENCE_DD_ENTRY;
            InputStream is = warArchive.getEntry(pathOfPersistenceXMLInsideClassesDir);
            if (is!=null) {
                is.close();
                probablePersitenceArchives.put(CLASSES_DIR, warArchive.getSubArchive(CLASSES_DIR));
            }

            for(Map.Entry<String, ReadableArchive> pathToArchiveEntry : probablePersitenceArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(main, pathToArchiveEntry.getValue(), pathToArchiveEntry.getKey(), descriptor);
            }
        } finally {
            for(Archive probablePersitenceArchive : probablePersitenceArchives.values()) {
                probablePersitenceArchive.close();
            }
        }
        return null;
    }
}
