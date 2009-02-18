/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sometimes listeners are not interested in receiving all event types, forcing 
 * them in checking the event type before doing any type of processing.
 * 
 * Alternatively, they can use this annotation to restrict the parameter type 
 * or value they really are interested in. In this case, they can restrict
 * to a event type name so that their listener is only called when such event
 * type is dispatched.
 * 
 * @author Jerome Dochez
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestrictTo {

    String value();
}
