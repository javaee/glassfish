/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.runlevel;


import org.glassfish.hk2.api.Metadata;
import org.jvnet.hk2.annotations.Contract;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Defines a run/start level.
 *
 * @author jdochez, jtrent, tbeerbower
 */
@Scope
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
@Documented
@Inherited
@Contract
public @interface RunLevel {
    // ----- Constants ------------------------------------------------------

    /**
     * The metadata key for run level value.  If this value is
     * present it MUST match the value of this annotation.  If
     * this value is set then the system will not have to reify the
     * descriptor in order to determine its level
     */
    public static final String RUNLEVEL_VAL_META_TAG  = "runLevelValue";

    /**
     * The metadata key for run level mode.  If this value is
     * present is MUST match the mode of this annotation.  If
     * this value is set then the system will not have to reify the
     * descriptor in order to determine its mode
     */
    public static final String RUNLEVEL_MODE_META_TAG = "runLevelMode";

    /**
     * The initial run level.
     */
    public static final int RUNLEVEL_VAL_INITIAL = -2;

    /**
     * The immediate run level.  Services set to this run level will be
     * activated immediately.
     */
    public static final int RUNLEVEL_VAL_IMMEDIATE = -1;

    /**
     * Services set to have a non-validating run level mode will be
     * activated by their associated run level service or through
     * injection into another service.  These services will not be
     * checked during activation which means that the service can be
     * activated prior to the run level service reaching the run level.
     * The run level serves only as a fail safe for activation.
     */
    public static final int RUNLEVEL_MODE_NON_VALIDATING = 0;

    /**
     * Services set to have a validating run level mode will be activated
     * and deactivated by their associated run level service but may also
     * be activated through injection into another service.  The current
     * run level of the associated run level service will be checked
     * during activation of these services to ensure that the service
     * is being activated in at an appropriate run level.
     */
    public static final int RUNLEVEL_MODE_VALIDATING = 1;


    // ----- Elements -------------------------------------------------------

    /**
     * Defines the run level.
     *
     * @return the run level
     */
    @Metadata(RUNLEVEL_VAL_META_TAG)
    public int value() default 0;

    /**
     * Defines the run level mode.
     *
     * @return the mode
     */
    @Metadata(RUNLEVEL_MODE_META_TAG)
    public int mode() default RUNLEVEL_MODE_VALIDATING;
}
