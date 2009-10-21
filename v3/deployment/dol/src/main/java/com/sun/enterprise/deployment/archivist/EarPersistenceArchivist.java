package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.Archive;
import org.xml.sax.SAXParseException;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@Service
public class EarPersistenceArchivist extends PersistenceArchivist {

    @Override
    public boolean supportsModuleType(XModuleType moduleType) {
        return XModuleType.EAR==moduleType;
    }


    /**
     * Reads persistence.xml from spec defined pu roots of an ear.
     * Spec defined pu roots are - (1)Non component jars in root of ear (2)jars in lib of ear
     */
    @Override
    public Object open(Archivist main, ReadableArchive earArchive, final RootDeploymentDescriptor descriptor) throws IOException, SAXParseException {

        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "EarArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    earArchive.getURI());
        }


        Map<String, ReadableArchive> probablePersitenceArchives = new HashMap<String,  ReadableArchive>();
        try {
            final Application app = Application.class.cast(descriptor);

            // TO DO: need to compute includeRoot, not hard-code it, in the next invocation.
            EARBasedPersistenceHelper.addLibraryAndTopLevelCandidates(earArchive, app, true /* includeRoot */,
                    probablePersitenceArchives);

            for(Map.Entry<String, ReadableArchive> pathToArchiveEntry : probablePersitenceArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(main, pathToArchiveEntry.getValue(), pathToArchiveEntry.getKey(), descriptor);
            }
        } finally {
            for(Archive subArchive : probablePersitenceArchives.values()) {
                subArchive.close();
            }
        }
        return null;
    }

}