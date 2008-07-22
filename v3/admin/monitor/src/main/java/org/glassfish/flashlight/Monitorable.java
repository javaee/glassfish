/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To designate a class as monitorable so that it is published in the 
 * MonitoringRegistry
 * @author Harpreet Singh
 */
@Target ({ElementType.TYPE,
          ElementType.FIELD,
          ElementType.METHOD})
@Retention (RetentionPolicy.RUNTIME)

public @interface Monitorable {
    String value ();
}
