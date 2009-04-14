package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * Implementation of this abstract class are handling injection resolution
 * for a particular injection annotation {@see Inject}
 *
 * @author Jerome Dochez
 */
public abstract class InjectionResolver<U extends Annotation> {
    public final Class<U> type;

    /**
     * Construct a resolver with a particular injection type
     * @param type the injection annotation type
     */
    public InjectionResolver(Class<U> type) {
        this.type = type;
    }

    /**
     * Returns true if the resolution of this injection identified by the
     * passed annotation instance is optional
     * @param annotation the injection metadata
     * @return true if the {@see getValue()} can return null without generating a
     * faulty injection operation
     */
    public boolean isOptional(U annotation) {
        return false;
    }

    /**
     * Returns the value to inject in the field or method of component annotated with
     * the annotated annotation.
     *
     * @param component injection target
     * @param annotated field of method to inject
     * @param type type of the expected return
     * @return the injectable resource
     * @throws ComponentException if the resource cannot be located.
     */
    public abstract Object getValue(Object component, AnnotatedElement annotated, Class type) throws ComponentException;
}