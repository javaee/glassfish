package org.jvnet.hk2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that the object should be created by using the given factory component,
 * instead of calling the constructor.
 *
 * <p>
 * If specified, the factory component is activated, and
 * {@link org.jvnet.hk2.component.Factory#getObject()} is used to obtain the instance,
 * instead of the default action, which is to call the constructor.
 * <p>
 * The resource injection and extraction happens like it normally
 * does, after the factory returns the object.
 *
 * @author Kohsuke Kawaguchi
 * @see FactoryFor
 */
// this needed to be separated from @Service so that @Configured can use this
@Retention(RUNTIME)
@Target(TYPE)
public @interface Factory {
    Class<? extends org.jvnet.hk2.component.Factory> value();
}
