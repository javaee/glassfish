package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model a annotation instance
 */
public class AnnotationModelImpl implements AnnotationModel {
    
    final AnnotationType type;
    final AnnotatedElement element;
    private final Map<String, Object> values = new HashMap<String, Object>();

    public AnnotationModelImpl(AnnotatedElement element, AnnotationType type) {
        this.type = type;
        this.element = element;
    }
    
    @Override
    public String toString() {
      return "AnnotationModel:" + type + "-" + element;
    }

    public void addValue(String name, Object value) {
        values.put(name, value);
    }

    @Override
    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public AnnotationType getType() {
        return type;
    }

    @Override
    public AnnotatedElement getElement() {
        return element;
    }
}
