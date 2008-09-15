package com.sun.enterprise.deployment;

import org.jvnet.hk2.annotations.Contract;

import java.lang.annotation.Annotation;

/**
 * Defines a pluggability facility to retrieve annotation types from various
 * containers.
 */
@Contract
public interface AnnotationTypesProvider {

    public Class<? extends Annotation>[] getAnnotationTypes();
}
