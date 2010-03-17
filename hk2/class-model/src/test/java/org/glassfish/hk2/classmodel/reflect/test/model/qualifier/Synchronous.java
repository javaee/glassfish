// Copyright
package org.glassfish.hk2.classmodel.reflect.test.model.qualifier;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Target;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 13, 2010
 * Time: 11:58:14 AM
 * To change this template use File | Settings | File Templates.
 */

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface Synchronous {}
