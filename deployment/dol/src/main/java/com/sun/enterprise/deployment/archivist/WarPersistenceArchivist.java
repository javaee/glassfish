package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.PersistenceDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
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
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Dec 8, 2008
 * Time: 12:55:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class WarPersistenceArchivist extends PersistenceArchivist {

    @Override
    public boolean supportsModuleType(XModuleType moduleType) {
        return XModuleType.WAR==moduleType;
    }

    @Override
    public Object open(Archivist main, ReadableArchive archive, RootDeploymentDescriptor descriptor) throws IOException, SAXParseException {

        //
        // TODO: Factor out this code that is a duplicate of the same method in WebArchivist
        //
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "EjbArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    archive.getURI());
        }
        Map<String, ReadableArchive> subArchives = new HashMap<String, ReadableArchive>();
        Enumeration entries = archive.entries();
        final String CLASSES_DIR = "WEB-INF/classes/";
        final String LIB_DIR = "WEB-INF/lib/";
        final String JAR_EXT = ".jar";
        try {
            ReadableArchive libArchive = archive.getSubArchive(LIB_DIR);
            if (libArchive!=null) {
                Enumeration<String> libEntries = libArchive.entries();
                while(libEntries.hasMoreElements()) {
                    String path = libEntries.nextElement();
                    if (path.endsWith(JAR_EXT)) {
                        if(path.indexOf('/') == -1) { // to avoid WEB-INF/lib/foo/bar.jar
                            // this jarFile is directly inside WEB-INF/lib directory
                            try {
                                subArchives.put(LIB_DIR+"/"+path, libArchive.getSubArchive(path));
                            } catch (IOException ioe) {
                                // if there is any problem in opening the
                                // library jar, log the exception and proceed
                                // to the next jar
                                logger.log(Level.SEVERE, ioe.getMessage(), ioe);
                            }
                        } else {
                            if(logger.isLoggable(Level.FINE)) {
                                logger.logp(Level.FINE, "EjbArchivist",
                                        "readPersistenceDeploymentDescriptors",
                                        "skipping {0} as it exists inside a directory in {1}.",
                                        new Object[]{path, LIB_DIR});
                            }
                            continue;
                        }

                    }

                }
            }

            final String pathOfPersistenceXMLInsideClassesDir =
                    CLASSES_DIR+ DescriptorConstants.PERSISTENCE_DD_ENTRY;
            InputStream is = archive.getEntry(pathOfPersistenceXMLInsideClassesDir);
            if (is!=null) {
                is.close();
                subArchives.put(CLASSES_DIR, archive.getSubArchive(CLASSES_DIR));
            }

            /** ToDo : mitesh, debug replacement code above and clean this old version
            while(entries.hasMoreElements()){
                final String nextEntry = String.class.cast(entries.nextElement());
                if(pathOfPersistenceXMLInsideClassesDir.equals(nextEntry)) {
                    subArchives.put(CLASSES_DIR, archive.getSubArchive(CLASSES_DIR));
                } else if (nextEntry.startsWith(LIB_DIR) && nextEntry.endsWith(JAR_EXT)) {
                    String jarFile = nextEntry.substring(LIB_DIR.length(), nextEntry.length()-JAR_EXT.length());
                    if(jarFile.indexOf('/') == -1) { // to avoid WEB-INF/lib/foo/bar.jar
                        // this jarFile is directly inside WEB-INF/lib directory
                        subArchives.put(nextEntry, archive.getSubArchive(nextEntry));
                    } else {
                        if(logger.isLoggable(Level.FINE)) {
                            logger.logp(Level.FINE, "EjbArchivist",
                                    "readPersistenceDeploymentDescriptors",
                                    "skipping {0} as it exists inside a directory in {1}.",
                                    new Object[]{nextEntry, LIB_DIR});
                        }
                        continue;
                    }
                }
            }
             */
            for(Map.Entry<String, ReadableArchive> pathToArchiveEntry : subArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(main, pathToArchiveEntry.getValue(), pathToArchiveEntry.getKey(), descriptor);
            }
        } finally {
            for(Archive subArchive : subArchives.values()) {
                subArchive.close();
            }
        }
        return null;
    }
}
