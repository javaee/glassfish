package org.jvnet.hk2.config.generator;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Collection;
import java.util.Set;
import java.util.Collections;

import org.jvnet.hk2.config.Configured;

/**
 * @author Kohsuke Kawaguchi
 */
public class AnnotationProcessorFactoryImpl implements AnnotationProcessorFactory {

    public Collection<String> supportedOptions() {
        return Collections.emptyList();
    }

    public Collection<String> supportedAnnotationTypes() {
        return Collections.singletonList(Configured.class.getName());
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> set, AnnotationProcessorEnvironment annotationProcessorEnvironment) {
        return new AnnotationProcessorImpl(annotationProcessorEnvironment);
    }
}
