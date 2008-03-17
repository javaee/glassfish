
package com.sun.appserv.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
    Use this annotation to name parameters on any method.  Intended usage is so that
    create() methods can translate params[idx] to a named value when creating a sub-element.

    @author llc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParamNames {
    /**
        Not all names need be specified (eg the first M of N can be listed),
        but they must be in order.  Names of no interest can be inserted as the empty string.
     */
    public String[] paramNames();
}

