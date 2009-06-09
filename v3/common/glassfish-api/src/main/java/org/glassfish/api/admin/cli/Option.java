package org.glassfish.api.admin.cli;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/** Indicates that the field is an option for a command. From a command line stand-point, the type of an option is
 *  of essence and it specifies how a particular option should be interpreted. It's use is both in getting syntactical
 *  information and during special handling of the values of options.
 * 
 * @author Kedar Mhaswade (km@dev.java.net)
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Option {


    /* defaults to the field name */
    String name() default "";

    char symbol() default '\u0000';

    OptionType type() default OptionType.STRING;

    boolean required() default false;

    boolean repeats() default false;

    String defaultValue() default ""; // the value in the field declaration overrides

    String[] legalValues() default {};

}