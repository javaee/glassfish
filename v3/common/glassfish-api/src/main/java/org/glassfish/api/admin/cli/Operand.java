package org.glassfish.api.admin.cli;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/** Indicates an operand of a command. When you need multiple operand, you should annotate an array field with this
 *  annotation type. 
 * @author Kedar Mhaswade(km@dev.java.net)
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Operand {
  OptionType type() default OptionType.STRING;
  boolean required() default false;
}