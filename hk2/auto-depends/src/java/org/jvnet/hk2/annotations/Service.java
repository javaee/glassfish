/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
