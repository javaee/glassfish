/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import org.glassfish.hk2.api.ClassAnalyzer;

/**
 * Annotation placed on classes that are to be automatically added
 * to an hk2 {@link org.glassfish.hk2.api.ServiceLocator}.  A service
 * marked with this annotation has the default scope of {@link javax.inject.Singleton},
 * but any other scope annotation placed on the class will override that default.
 * <p>
 * This annotation is read at build time using the hk2-inhabitant-generator
 * and information about the service is placed into a file in the
 * associated jar.  The usual way to get these services into
 * a {@link org.glassfish.hk2.api.ServiceLocator} is to use a
 * {@link org.glassfish.hk2.api.Populator} as provided by the
 * {@link org.glassfish.hk2.api.DynamicConfigurationService#getPopulator()}
 * method.  An easier way to do that is with the
 * {@link org.glassfish.hk2.utilities.ServiceLocatorUtilities#createAndPopulateServiceLocator()}
 * utility.
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 * @see org.glassfish.hk2.api.Factory org.glassfish.hk2.api.ClassAnalyzer
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
     * {@link org.glassfish.hk2.api.ServiceLocator#getService(Class, String, java.lang.annotation.Annotation...)} and
     * similar methods can be used to obtain a service with a particular name.
     *
     * <p>
     * The default value "" indicates that the inhabitant has no name.
     */
    String name() default "";

    /**
     * Additional metadata that goes into the inhabitants file.
     * The value is "key={value},key={value1,value2,...},..." format.
     *
     * This information is accessible from {@link org.glassfish.hk2.api.Descriptor#getMetadata()}.
     *
     * <p>
     * While this is limited in expressiveness, metadata has a performance advantage
     * in it that it can be read without even creating a classloader for this class.
     * For example, this feature is used by the configuration module so that
     * the config file can be read without actually loading the classes. 
     */
    String metadata() default "";
    
    /**
     * The name of the {@link ClassAnalyzer} service that should be used
     * to analyze this class
     * 
     * @return The name of the {@link ClassAnalyzer} service that should
     * be used to analyze this class
     */
    String analyzer() default ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME;
}
