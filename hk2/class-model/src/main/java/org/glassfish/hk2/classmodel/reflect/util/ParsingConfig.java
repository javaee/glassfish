package org.glassfish.hk2.classmodel.reflect.util;

import java.util.Set;

/**
 * Configuration for the parser.
 *
 * @author Jerome Dochez
 */
public interface ParsingConfig {

    /**
     * Returns a list of annotations that denotes dependency injection enabled
     * classes (classes that use dependency injection). A class annotated with
     * one the returned annotation getName will potentially define one or more
     * injection point.
     *
     * @return list of annotations that denote a dependency injection enabled type.
     */
    public Set<String> getInjectionTargetAnnotations();

    /**
     * Returns a list of interfaces that denotes a dependency injection enabled
     * classes. A class implementing one of the interface returned will
     * potentially define one or more injection point.
     *
     * @return list of interfaces that a class can implement that will denote possible
     * use of dependency injection
     */
    public Set<String> getInjectionTargetInterfaces();

    /**
     * Returns a list of annotations that denote an injection point within an
     * dependency injection enabled type. This injection point (representing
     * a dependency) is either a constructor parameter, a field or a method.
     *
     * @return list of annotations denoting injection points (like @Inject).
     */
    public Set<String> getInjectionPointsAnnotations();
}
