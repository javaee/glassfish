package org.glassfish.deployment.common;

import org.objectweb.asm.*;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.glassfish.api.deployment.archive.ReadableArchive;

import com.sun.logging.LogDomains;

public class AnnotationScanner implements ClassVisitor {

    protected String className;
    protected String signature;

    final static Logger logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);

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
                    try {
                        ClassReader cr = new ClassReader(is);
                        cr.accept(this, crFlags);
                    } catch(Exception e) {
                        logger.log(Level.WARNING, "Exception while scanning " +
                                entryName, e);
                    } finally {
                        is.close();
                    }
                } else if (entryName.endsWith(".jar")) {
                    // scan class files inside jar
                    try {
                        File archiveRoot = new File(archive.getURI());
                        File file = new File(archiveRoot, entryName);
                        JarFile jarFile = new JarFile(file);
                        try {
                            Enumeration<JarEntry> jarEntries = jarFile.entries();
                            while (jarEntries.hasMoreElements()) {
                                JarEntry entry = jarEntries.nextElement();
                                String jarEntryName = entry.getName();
                                if (jarEntryName.endsWith(".class")) {
                                    InputStream is = jarFile.getInputStream(entry);
                                    try {
                                        ClassReader cr = new ClassReader(is);
                                        cr.accept(this, crFlags);
                                    } catch(Exception e) {
                                        logger.log(Level.FINE,
                                                "Exception while scanning " +
                                                        entryName, e);
                                    } finally {
                                        is.close();
                                    }
                                }
                            }
                        } finally {
                            jarFile.close();
                        }
                    } catch (IOException ioe) {
                        logger.warning("Error scan jar entry" + entryName +
                            ioe.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to scan archive for annotations", e);
        }
    }
}
