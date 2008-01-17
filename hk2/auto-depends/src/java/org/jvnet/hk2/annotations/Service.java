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

import com.sun.hk2.component.InhabitantsFile;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Marker interface for service implementation. A service is defined by 
 * an interface marked with the {@link Contract} annotation. Each service
 * implementation must be marked with the @Service interface and 
 * implement the service interface. 
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 * @see Factory
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@InhabitantAnnotation("default")
public @interface Service {

    /**
     * Name of the service.
     *
     * <p>
     * {@link Habitat#getComponent(Class, String)} and similar methods can be used
     * to obtain a service with a particular name. All the named services
     * are still available through {@link Habitat#getAllByContract(Class)}.
     *
     * <p>
     * The default value "" indicates that the inhabitant is anonymous.
     */
    @Index
    String name() default "";

    /**
     * Additional metadata that goes into the inhabitants file.
     * The value is "key=value,key=value,..." format. See {@link InhabitantsFile}
     * for more details.
     *
     * This information is accessilbe from {@link Inhabitant#metadata()}.
     *
     * <p>
     * While this is limited in expressiveness, metadata has a performance advantage
     * in it that it can be read without even creating a classloader for this class.
     * For example, this feature is used by the configuration module so that
     * the config file can be read without actually loading the classes. 
     */
    String metadata() default "";
}
