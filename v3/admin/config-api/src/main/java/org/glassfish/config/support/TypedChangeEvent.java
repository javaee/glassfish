package org.glassfish.config.support;

import java.beans.PropertyChangeEvent;

/**
 * Simple extension to java beans events to support notification for adding/removing indexed properties.
 * 
 */
public class TypedChangeEvent extends PropertyChangeEvent {

    public enum Type {ADD, REMOVE, CHANGE};

    final Type type;

    public TypedChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
        type=Type.CHANGE;
    }

    public TypedChangeEvent(Object source, String propertyName, Object oldValue, Object newValue, Type type) {
        super(source, propertyName, oldValue, newValue);
        this.type=type;
    }

    public Type getType() {
        return type;
    }

}
