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
            //Get probable archives from root of the ear
            SubArchivePURootScanner earRootScanner = new SubArchivePURootScanner() {

                public String getPathOfSubArchiveToScan() {
                    // We are scanning root of ear.
                    return "";
                }

                public boolean isProbablePuRootJar(String jarName) {
                    return super.isProbablePuRootJar(jarName) &&
                            // component roots are not scanned while scanning ear. They will be handled
                            // while scanning the component.
                            !isComponentJar(jarName,((Application)descriptor).getModules());
                }

                private boolean isComponentJar(String jarName, Set<ModuleDescriptor<BundleDescriptor>> moduleDescriptors) {
                //TODO : This method duplicated code in JPACompositeSniffer. Need to push this code into a common place (May be somewhere in deployment code)
                    boolean isComponentJar = false;
                    for (ModuleDescriptor md : moduleDescriptors) {
                        String archiveUri = md.getArchiveUri();
                        if (jarName.equals(archiveUri)) {
                            isComponentJar = true;
                            break;
                        }
                    }
                    return isComponentJar;
                }

            };
            probablePersitenceArchives = getProbablePersistenceRoots(earArchive, earRootScanner);

            //Geather all jars in lib of ear
            SubArchivePURootScanner libPURootScannerScanner = new SubArchivePURootScanner() {
                String getPathOfSubArchiveToScan() {
                    return LIB_DIR;
                }
            };
            probablePersitenceArchives.putAll(getProbablePersistenceRoots(earArchive, libPURootScannerScanner));

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