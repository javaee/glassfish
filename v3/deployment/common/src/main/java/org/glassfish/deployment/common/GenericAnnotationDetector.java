package org.glassfish.deployment.common;

import org.objectweb.asm.*;

import java.util.List;
import java.util.ArrayList;

import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * This class will detect whether an archive contains specified annotations.
 */
public class GenericAnnotationDetector extends AnnotationScanner {

    boolean found = false;
    List<String> annotations = new ArrayList<String>();; 

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
}
