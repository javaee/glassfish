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
 * TODO : docs
 *
 * @author Jerome Dochez, Jeff Trent, tbeerbower
 */
@Scope
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
@Documented
@Inherited
@Contract
public @interface RunLevel {

    public static final String RUNLEVEL_VAL_META_TAG  = "runLevelValue";
    public static final String RUNLEVEL_MODE_META_TAG = "runLevelMode";

    // the "kernel" (aka immediate) run level
    public static final int RUNLEVEL_VAL_KERNAL = -1;

    public enum Mode {
        VALIDATING,
        NON_VALIDATING;

        /**
         * Convert an RecordType to an integer.
         *
         * @return the integer
         */
        public int toInt()
        {
            return ordinal();
        }

        /**
         * Convert an integer value to a Mode
         *
         * @param nOrdinal  the ordinal value of a Mode
         *
         * @return the RecordType
         */
        public static Mode fromInt(int nOrdinal)
        {
            return Mode.class.getEnumConstants()[nOrdinal];
        }
    }

    /**
     * Defines the run level.
     *
     * @return the run level
     */
    //@InhabitantMetadata(RUNLEVEL_VAL_META_TAG)
    public int value() default 0;

    public Mode mode() default Mode.VALIDATING;
}
