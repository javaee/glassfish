package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.SAXParseException;

/**
 * PersistenceArchivist for app clients that knows how to scan for PUs in
 * the app client itself as well as in library JARs (or top-level JARs from
 * the containing EAR) that might accompany the app client.
 *
 */
@Service
public class ACCPersistenceArchivist extends PersistenceArchivist {

    
    
    @Inject
    private ProcessEnvironment env;
    
    @Inject
    private ArchiveFactory archiveFactory;

    @Override
    public boolean supportsModuleType(XModuleType moduleType) {
        return (XModuleType.CAR == moduleType) && (env.getProcessType() == ProcessType.ACC) ;
    }

    @Override
    public Object open(Archivist main, ReadableArchive archive, RootDeploymentDescriptor descriptor) throws IOException, SAXParseException {
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "ACCPersistencerArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    archive.getURI());
        }
        
        final Map<String,ReadableArchive> candidatePersistenceArchives =
                new HashMap<String,ReadableArchive>();
        
        /*
         * The descriptor had better be an ApplicationClientDescriptor!
         */
        if ( ! (descriptor instanceof ApplicationClientDescriptor)) {
            return null;
        }
        
        final ApplicationClientDescriptor acDescr = ApplicationClientDescriptor.class.cast(descriptor);
        
        try {
            /*
             * We must scan the app client archive itself.
             */

            candidatePersistenceArchives.put(archive.getURI().toASCIIString(), archive);

            /*
             * If this app client 
             * was deployed as part of an EAR then scan any library JARs and, if the
             * client was also deployed or launched in v2-compatibility mode, any
             * top-level JARs in the EAR.
             * 
             * Exactly how we do this depends on whether this is a deployed client
             * (which will reside in a client download directory) or a non-deployed
             * one (which will reside either as a stand-alone client or within an
             * EAR).
             */
            final Manifest mf = archive.getManifest();
            if (isDeployed(mf)) {
                if ( ! isStandAlone(mf)) {
                    addOtherDeployedScanTargets(archive, mf, candidatePersistenceArchives);
                }
            } else if ( ! isStandAlone(acDescr)) {
                addOtherNondeployedScanTargets(archive, acDescr, candidatePersistenceArchives);
            }

            for (Map.Entry<String, ReadableArchive> pathToArchiveEntry : candidatePersistenceArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(main, 
                        pathToArchiveEntry.getValue(), 
                        pathToArchiveEntry.getKey(), 
                        descriptor);
            }
        } finally {
            for (Map.Entry<String, ReadableArchive> pathToArchiveEntry : candidatePersistenceArchives.entrySet()) {
                
            }
        }
        return null;
    }


    private boolean isStandAlone(final Manifest mf) {
        final Attributes mainAttrs = mf.getMainAttributes();
        final String relativePathToGroupFacade = mainAttrs.getValue(AppClientArchivist.GLASSFISH_GROUP_FACADE);
        return relativePathToGroupFacade == null;
    }

    private boolean isStandAlone(final ApplicationClientDescriptor ac) {
        /*
         * For a non-deployed app (this case), the descriptor for a stand-alone
         * app client has a null application value.
         */
        return (ac.getApplication() == null || ac.isStandalone());
    }

    private boolean isDeployed(final Manifest mf) throws IOException {
        final Attributes mainAttrs = mf.getMainAttributes();
        final String gfClient = mainAttrs.getValue(AppClientArchivist.GLASSFISH_APPCLIENT);
        return gfClient != null;
    }
    
    private void addOtherDeployedScanTargets(
            final ReadableArchive archive,
            final Manifest mf, 
            Map<String,ReadableArchive> candidates) throws IOException {
        
        final Attributes mainAttrs = mf.getMainAttributes();
//        final String classPathExpr = mainAttrs.getValue(Attributes.Name.CLASS_PATH);
        final String otherPUScanTargets = mainAttrs.getValue(
                AppClientArchivist.GLASSFISH_CLIENT_PU_SCAN_TARGETS_NAME);
        
        /*
         * Include library JARs - listed in the facade's Class-Path - and
         * any additional (typically top-level) JARs to be scanned.
         */
        
//        addScanTargetsFromURIList(archive, classPathExpr, candidates);
        addScanTargetsFromURIList(archive, otherPUScanTargets, candidates);
    }
    
    private void addOtherNondeployedScanTargets(final ReadableArchive clientArchive,
            final ApplicationClientDescriptor acDescr,
            final Map<String,ReadableArchive> candidates) {
        
        /*
         * The archive is a non-deployed one.  We know from an earlier check
         * that this is not a stand-alone app client, so we can use the
         * app client archive's parent archive to get to the containing EAR for
         * use in a subarchive scanner.
         */
        final ReadableArchive earArchive = clientArchive.getParentArchive();

        EARBasedPersistenceHelper.addLibraryAndTopLevelCandidates(earArchive,
                acDescr.getApplication(),
                true,
                candidates);
        
        
        
    }
    
    private void addScanTargetsFromURIList(final ReadableArchive archive,
            final String relativeURIList,
            final Map<String,ReadableArchive> candidates) throws IOException {
        if (relativeURIList == null) {
            return;
        }
        final String[] relativeURIs = relativeURIList.split(" ");
        for (String uriText : relativeURIs) {
            final URI scanTargetURI = archive.getURI().resolve(uriText);
            candidates.put(uriText, archiveFactory.openArchive(scanTargetURI));
        }
    }
    
    private class AppClientPURootScanner extends SubArchivePURootScanner {

        private final ReadableArchive clientArchive;
        
        private AppClientPURootScanner(final ReadableArchive clientArchive) {
            this.clientArchive = clientArchive;
        }
        
        @Override
        ReadableArchive getSubArchiveToScan(ReadableArchive parentArchive) {
            return clientArchive;
        }

        /**
         * The superclass requires this implementation, but it is never used
         * because we also override getSubArchiveToScan.
         * 
         * @return
         */
        @Override
        String getPathOfSubArchiveToScan() {
            return null;
        }
    }
}
