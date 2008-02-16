package org.glassfish.admin.amx;


/**
 * @author llc
 */
public @interface ObjectNameBuilderAnnotation {
    Class<? extends ObjectNameBuilder> builder();
    
    /**
        Any desired hints for the builder.
     */
    String[]  builderHints() default {};
}
