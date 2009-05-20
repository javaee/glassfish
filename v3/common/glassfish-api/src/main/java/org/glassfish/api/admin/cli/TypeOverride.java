package org.glassfish.api.admin.cli;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;


/** Indicates the overriding type for a particular option as a function of values of other options. 
 * @author Kedar Mhaswade (km@dev.java.net)
 */

@Retention(RUNTIME)
@Target(FIELD)
public @interface TypeOverride {
  OptionType type();
  String[] options();
  String[] values();
  Condition condition() default Condition.OR;
}