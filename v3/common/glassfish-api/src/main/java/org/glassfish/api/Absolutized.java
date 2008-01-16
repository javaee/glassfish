package org.glassfish.api;

import java.io.File;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Target;

/**
 * Used in conjunction with {@link File} or {@link String} that represents a file
 * to indicate that the path is absolutized.
 *
 * <p>
 * Note that using {@link String} to represent a path name is discouraged,
 * because of the lack of type safety.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(SOURCE)
@Documented
@Target({METHOD, PARAMETER, FIELD})
public @interface Absolutized {
}
