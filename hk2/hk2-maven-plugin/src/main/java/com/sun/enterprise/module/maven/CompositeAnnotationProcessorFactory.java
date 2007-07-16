package com.sun.enterprise.module.maven;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * {@link AnnotationProcessorFactory} that aggregates multiple {@link AnnotationProcessorFactory}s.
 * 
 * @author Kohsuke Kawaguchi
 */
public class CompositeAnnotationProcessorFactory implements AnnotationProcessorFactory {
    private final List<AnnotationProcessorFactory> factories;

    public CompositeAnnotationProcessorFactory(List<AnnotationProcessorFactory> factories) {
        this.factories = factories;
    }

    public CompositeAnnotationProcessorFactory(AnnotationProcessorFactory... factories) {
        this(Arrays.asList(factories));
    }

    public Collection<String> supportedOptions() {
        Set<String> r = new HashSet<String>();
        for (AnnotationProcessorFactory f : factories)
            r.addAll(f.supportedOptions());
        return r;
    }

    public Collection<String> supportedAnnotationTypes() {
        Set<String> r = new HashSet<String>();
        for (AnnotationProcessorFactory f : factories)
            r.addAll(f.supportedAnnotationTypes());
        return r;
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> set, AnnotationProcessorEnvironment annotationProcessorEnvironment) {
        List<AnnotationProcessor> r = new ArrayList<AnnotationProcessor>();
        for (AnnotationProcessorFactory f : factories)
            r.add(f.getProcessorFor(set,annotationProcessorEnvironment));

        return AnnotationProcessors.getCompositeAnnotationProcessor(r);
    }
}
