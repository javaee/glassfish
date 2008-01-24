package com.sun.enterprise.config.serverbeans;

import org.glassfish.config.support.TypedChangeEvent;

import java.beans.VetoableChangeSupport;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

/**
 * A Constrained List is a @Link java.util.List implementation which mutable
 * operations are constrained by the owner of the list.
 *
 * @author Jerome Dochez
 */
public class ConstrainedList<T> extends ArrayList<T> {

    final Object source;
    final String id;
    final VetoableChangeSupport support;

    ConstrainedList(Object source, String id, VetoableChangeSupport support) {
        this.source = source;
        this.id = id;
        this.support = support;
    }

    public boolean add(T object) {
        try {
            support.fireVetoableChange(new TypedChangeEvent(source, id, null, object, TypedChangeEvent.Type.ADD));
        } catch (PropertyVetoException e1) {
            return false;
        }
        return super.add(object);
    }

    public boolean remove(Object object) {
        try {
            support.fireVetoableChange(new TypedChangeEvent(source, id, object, null, TypedChangeEvent.Type.REMOVE));
        } catch (PropertyVetoException e) {
            return false;
        }
        return super.remove(object);
    }

    public Object clone() {
        System.out.println(this + " is being cloned");
        return super.clone();
    }
}
