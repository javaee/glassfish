/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.glassfish.hk2.RunLevelDefaultScope;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines a run/start level.
 * 
 * @author Jerome Dochez, Jeff Trent
 */
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
@Documented
@Inherited
@Contract
public @interface RunLevel {

    public static final String META_SCOPE_TAG = "runLevelScope";
    public static final String META_VAL_TAG = "runLevel";
    
    // the "kernel" (aka immediate) run level
    public static final int KERNEL_RUNLEVEL = -1;

    // TODO: problem compiling (w/ Sun(?))
//    // the default scope
//    public static final Class<?> DEFAULT_SCOPE = RunLevelDefaultScope.class;

    /**
     * Defines the run level scope in which this RunLevel applies.
     * <p/>
     * 
     * The run level scope is any type used to segregate the
     * application / system namespace.
     * <p/>
     * 
     * @return the run level scope type this annotation value applies
     */
    @InhabitantMetadata(META_SCOPE_TAG)
    Class<?> runLevelScope() default RunLevelDefaultScope.class;

    /**
     * Defines the run level.
     *
     * @return the run level
     */
    @InhabitantMetadata(META_VAL_TAG)
    int value() default 0;
    
    /**
     * Determines whether strict constraint rules should be followed.
     * 
     * <p/>
     * When set to true these rules apply:
     * <li> The RunLevelService is the one responsible for instantiation a service once the proper run level is reached during startup.
     * <li> The RunLevelService will release the service during shutdown.
     * <li> A non-RunLevel service cannot depend (i.e., be injected) with a RunLevel service.
     * <li> A RunLevel service with scope X cannot depend on a RunLevel service with scope Y when X != Y
     * <li> A RunLevel service having run level value of N can not depend (i.e., be injected) with a RunLevel service having value M when M > N. 
     * 
     * <p>
     * When set to false these rules apply:
     * <li> The RunLevelService will not manage / release the service.
     * <li> Any demand for the service will cause the service to be instantiated.
     * 
     * @return true if strict constraint rules should be followed
     */
    boolean strict() default true;
}
