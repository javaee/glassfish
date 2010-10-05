package org.glassfish.hk2.classmodel.reflect;

import java.util.Map;

/**
 * Models an annotation instance
 *
 * @author Jerome Dochez
 */
public interface AnnotationModel {

    /**
     * Returns the annotation type for this model
     * @return the annotation type
     */
    AnnotationType getType();

    /**
     * Returns the annotated element with this annotation instance
     * @return the annotated element
     */
    AnnotatedElement getElement();


    /**
     * Returns an unmodifiable collection of annotation values.
     *
     * @return collection of value elements of this annotation
     */
    Map<String, Object> getValues();
}
