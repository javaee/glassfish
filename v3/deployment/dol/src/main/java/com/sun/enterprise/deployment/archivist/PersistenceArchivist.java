package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.PersistenceDeploymentDescriptorFile;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Set;

public abstract class PersistenceArchivist extends ExtensionsArchivist {
    protected static final String JAR_EXT = ".jar";
    protected static final char SEPERATOR_CHAR = '/';
    protected static final String LIB_DIR = "lib";


    private static final Logger st_logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);
    protected final Logger logger = st_logger;

    public DeploymentDescriptorFile getStandardDDFile(RootDeploymentDescriptor descriptor) {
        return new PersistenceDeploymentDescriptorFile();
    }

    public DeploymentDescriptorFile getConfigurationDDFile(RootDeploymentDescriptor descriptor) {
        return null; 
    }

    public XModuleType getModuleType() {
        return XModuleType.Persistence;
    }

    @Override
    // Need to be overridden in derived class to define module type it supports
    public abstract boolean supportsModuleType(XModuleType moduleType);

    @Override
    public Object open(Archivist main, ReadableArchive archive, RootDeploymentDescriptor descriptor)
            throws IOException, SAXParseException {
        String puRoot = getPuRoot(archive);
        readPersistenceDeploymentDescriptor(main, archive, puRoot, descriptor);
        return null;  // return null so that the descritor does not get added twice to extensions
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
        if (descriptor.getExtensionsDescriptors(PersistenceUnitsDescriptor.class, puRoot) != null) {
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

            persistenceUnitsDescriptor.setParent(descriptor);
            persistenceUnitsDescriptor.setPuRoot(puRoot);            
            descriptor.addExtensionDescriptor(PersistenceUnitsDescriptor.class,persistenceUnitsDescriptor, puRoot);
        }

        return persistenceUnitsDescriptor;
    }

    public <T extends RootDeploymentDescriptor> T getDefaultDescriptor() {
        return null;
    }

    /**
     * Gets probable persitence roots from given parentArchive using given subArchiveRootScanner
     * @param parentArchive the parentArchive within which probable persitence roots need to be scanned
     * @param subArchivePURootScanner the scanner instance used for the scan
     * @see com.sun.enterprise.deployment.archivist.EarPersistenceArchivist.SubArchivePURootScanner
     * @return Map of puroot path to probable puroot archive.
     */
    protected static Map<String, ReadableArchive> getProbablePersistenceRoots(ReadableArchive parentArchive, SubArchivePURootScanner subArchivePURootScanner) {
        Map<String, ReadableArchive> probablePersitenceArchives = new HashMap<String, ReadableArchive>();
        ReadableArchive  archiveToScan = subArchivePURootScanner.getSubArchiveToScan(parentArchive);
        if(archiveToScan != null) { // The subarchive exists
            Enumeration<String> entries = archiveToScan.entries();
            String puRootPrefix = subArchivePURootScanner.getPurRootPrefix();
            while(entries.hasMoreElements()) {
                String entry = entries.nextElement();
                if(subArchivePURootScanner.isProbablePuRootJar(entry)) {
                    ReadableArchive puRootArchive = getSubArchive(archiveToScan, entry, false /* expect entry to be present */);
                    if(puRootArchive != null) {
                        String puRoot = puRootPrefix + entry;
                        probablePersitenceArchives.put(puRoot, puRootArchive);
                    }
                }
            }

        }
        return probablePersitenceArchives;
    }

    private static ReadableArchive getSubArchive(ReadableArchive parentArchive, String path, boolean expectAbscenceOfSubArchive) {
        ReadableArchive returnedArchive = null;
        try {
            returnedArchive = parentArchive.getSubArchive(path);
        } catch (IOException ioe) {
            // if there is any problem in opening the subarchive, and the subarchive is expected to be present, log the exception
            if(!expectAbscenceOfSubArchive) {
                st_logger.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }
        return returnedArchive;
    }

    protected String getPuRoot(ReadableArchive archive) {
        // getPuRoot() is called from Open() of this class to define pu root. This is default implementation to keep compiler happy.
        // It is assumed that subclasses not overriding open() (which are currently ACCPersitenceArchivist and ServerSidePersitenceArchivist)
        // would override this method.
        assert false;
        return null;
    }

    protected static abstract class SubArchivePURootScanner {
        abstract String getPathOfSubArchiveToScan();

        ReadableArchive getSubArchiveToScan(ReadableArchive parentArchive) {
            String pathOfSubArchiveToScan = getPathOfSubArchiveToScan();
            return (pathOfSubArchiveToScan == null || pathOfSubArchiveToScan.isEmpty()) ? parentArchive :
                    getSubArchive(parentArchive, pathOfSubArchiveToScan, true /*It is possible that lib does not exist for a given ear */);
        }

        String getPurRootPrefix() {
            String pathOfSubArchiveToScan = getPathOfSubArchiveToScan();
            return (pathOfSubArchiveToScan == null || pathOfSubArchiveToScan.isEmpty()) ? pathOfSubArchiveToScan : pathOfSubArchiveToScan + SEPERATOR_CHAR;
        }

        boolean isProbablePuRootJar(String jarName) {
            // all jars in root of subarchive are probable pu roots
            return isJarEntry(jarName) && checkIsInRootOfArchive(jarName, getPathOfSubArchiveToScan());
        }

        private boolean checkIsInRootOfArchive(String path, String parentArchivePath) {
            boolean inRootOfArchive = true;
            if (path.indexOf('/') != -1) {
                inRootOfArchive = false;
                if (st_logger.isLoggable(Level.FINE)) {
                    st_logger.logp(Level.FINE, "PersistenceArchivist",
                            "readPersistenceDeploymentDescriptors",
                            "skipping {0} as it exists inside a directory in {1}.",
                            new Object[]{path, parentArchivePath});
                }
            }
            return inRootOfArchive;
        }

        private boolean isJarEntry(String path) {
            return path.endsWith(JAR_EXT);
        }

    }

    
}
