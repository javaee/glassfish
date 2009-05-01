package org.glassfish.deployment.common;

import org.objectweb.asm.*;

import java.util.List;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ReadableArchive;

import com.sun.logging.LogDomains;

/**
 * This class will detect whether an archive contains specified annotations.
 */
public class GenericAnnotationDetector extends AnnotationScanner {

    boolean found = false;
    List<String> annotations = new ArrayList<String>();; 

    final static Logger logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);

    public GenericAnnotationDetector(Class[] annotationClasses) {
        if (annotationClasses != null) {
            for (Class annClass : annotationClasses) {
                annotations.add(Type.getDescriptor(annClass));
            }
        }
    }

    public boolean hasAnnotationInArchive(ReadableArchive archive) {
        scanArchive(archive);
        return found;
    }

    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        if (annotations.contains(s)) {
            found = true;
        }
        return null;
    }

    @Override
    public void scanArchive(ReadableArchive archive) {
        try {
            int crFlags = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                | ClassReader.SKIP_FRAMES;
            Enumeration<String> entries = archive.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement();
                if (entryName.endsWith(".class")) {
                    // scan class files
                    InputStream is = archive.getEntry(entryName);
                    try {
                        ClassReader cr = new ClassReader(is);
                        cr.accept(this, crFlags);
                    } finally {
                        is.close();
                    }
                } else if (entryName.endsWith(".jar") && 
                    entryName.indexOf('/') == -1) {
                    // scan class files inside top level jar
                    try {
                        ReadableArchive jarSubArchive = null;
                        try {
                            jarSubArchive = archive.getSubArchive(entryName);
                            Enumeration<String> jarEntries =
                                jarSubArchive.entries();
                            while (jarEntries.hasMoreElements()) {
                                String jarEntryName = jarEntries.nextElement();
                                if (jarEntryName.endsWith(".class")) {
                                    InputStream is =
                                        jarSubArchive.getEntry(jarEntryName);
                                    try {
                                        ClassReader cr = new ClassReader(is);
                                        cr.accept(this, crFlags);
                                    } finally {
                                        is.close();
                                    }
                                }
                            }
                        } finally {
                            jarSubArchive.close();
                        }
                    } catch (IOException ioe) {
                        logger.warning("Error scan jar entry" + entryName +
                            ioe.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to scan archive for annotations" +
                e.getMessage());
        }
    }
}
