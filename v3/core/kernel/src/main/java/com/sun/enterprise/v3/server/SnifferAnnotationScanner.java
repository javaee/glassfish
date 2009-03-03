package com.sun.enterprise.v3.server;

import org.objectweb.asm.*;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.container.Sniffer;

import com.sun.logging.LogDomains;

public class SnifferAnnotationScanner implements ClassVisitor {

    Map<String, SnifferStatus> annotations = new HashMap<String, SnifferStatus>();

    String className;
    String signature;

    final static Logger logger = LogDomains.getLogger(SnifferAnnotationScanner.class, LogDomains.CORE_LOGGER);

    public void register(Sniffer sniffer, Class[] annotationClasses) {
        SnifferStatus stat = new SnifferStatus(sniffer);
        if (annotationClasses!=null) {
            for (Class annClass : annotationClasses) {
                annotations.put(Type.getDescriptor(annClass), stat);
            }
        }
    }

    public List<Sniffer> getApplicableSniffers() {
        List<Sniffer> appSniffers = new ArrayList<Sniffer>();
        for (String annotationName : annotations.keySet()) {
            SnifferStatus stat = annotations.get(annotationName);
            if (!appSniffers.contains(stat.sniffer) && stat.found) {
                appSniffers.add(stat.sniffer);
            }
        }
        return appSniffers;
    }

    public void visit(int version,
           int access,
           String name,
           String signature,
           String superName,
           String[] interfaces) {

        this.className = name;
        this.signature = signature;
    }

    public void visitSource(String s, String s1) {}

    public void visitOuterClass(String s, String s1, String s2) {

    }

    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        SnifferStatus status = annotations.get(s);
        if (status != null) {
            status.found = true;
        }
        return null;
    }

    public void visitAttribute(Attribute attribute) {

    }

    public void visitInnerClass(String s, String s1, String s2, int i) {

    }

    public FieldVisitor visitField(int i, String s, String s1, String s2, Object o) {
        return null;
    }

    public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
        return null;
    }

    public void visitEnd() {
        
    }

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
                    ClassReader cr = new ClassReader(is);
                    cr.accept(this, crFlags);
                } else if (entryName.endsWith(".jar")) {
                    // scan class files inside jar
                    try {
                        File archiveRoot = new File(archive.getURI());
                        File file = new File(archiveRoot, entryName);
                        JarFile jarFile = new JarFile(file);
                        Enumeration<JarEntry> jarEntries = jarFile.entries();
                        while (jarEntries.hasMoreElements()) {
                            JarEntry entry = jarEntries.nextElement();
                            String jarEntryName = entry.getName();
                            if (jarEntryName.endsWith(".class")) {
                                InputStream is = jarFile.getInputStream(entry);
                                ClassReader cr = new ClassReader(is);
                                cr.accept(this, crFlags);
                            }
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

    private static final class SnifferStatus {
        Sniffer sniffer;
        boolean found;

        SnifferStatus(Sniffer sniffer) {
            this.sniffer = sniffer;
        }
    }
}
