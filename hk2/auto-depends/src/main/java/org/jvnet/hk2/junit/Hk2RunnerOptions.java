package org.jvnet.hk2.junit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jvnet.hk2.component.HabitatFactory;

/**
 * Provide options for tuning the behavior of the Hk2Runner.
 * 
 * @see Hk2Runner
 * 
 * @author Jeff Trent
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface Hk2RunnerOptions {

  /**
   * Flag indicating whether the habitat (and all injections)
   * are recreated after each <code>Test</code>.
   */
  boolean reinitializePerTest() default false;
  
  /**
   * Alternative Habitat Factory from the default 
   */
  Class<? extends HabitatFactory> habitatFactory() default HabitatFactory.class; 
  
}
