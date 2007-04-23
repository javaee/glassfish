/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.jvnet.hk2.annotations;

import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.Scope;
import org.jvnet.hk2.component.Factory;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker interface for service implementation. A service is defined by 
 * an interface marked with the {@link Contract} annotation. Each service
 * implementation must be marked with the @Service interface and 
 * implement the service interface. 
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Service {

    /**
     * Returns name of the service 
     * @return name of the service
     */
    String name() default "";

    /**
     * Indicates the scope that this implementation is tied to.
     */
    Class<? extends Scope> scope() default PerLookup.class;

    /**
     * If this implementation is created from a factory
     * (instead of calling the default constructor), then specify
     * the factory class.
     *
     * <p>
     * If specified, the factory component is activated, and
     * {@link Factory#getObject()} is used to obtain the instance,
     * instead of the default action, which is to call the constructor.
     * <p>
     * The resource injection and extraction happens like it normally
     * does, after the factory returns the object.
     */
    Class<? extends Factory> factory() default Factory.class;
}
