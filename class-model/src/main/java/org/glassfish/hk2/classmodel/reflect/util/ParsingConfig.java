/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.classmodel.reflect.util;

import java.util.Set;

/**
 * Filters the parsing activity to only deeply parse classes that are either
 * annotated with an annotation returned by {@link #getAnnotationsOfInterest}
 * or implements/subclass a type returned by {@link #getTypesOfInterest}.
 *
 * A class identified to be deeply parsed will contain all the metadata about
 * its members like fields, methods as well as annotations on those.
 *
 * @author Jerome Dochez
 */
public interface ParsingConfig {

    /**
     * Returns a list of annotations that should trigger an exhaustive visit
     * of the annotated type.
     *
     * @return list of annotations that triggers an exhaustive scanning of the
     * annotated type
     */
    Set<String> getAnnotationsOfInterest();

    /**
     * Returns a list of types (classes or interfaces) that a type must either
     * subclass or implement to trigger an exhaustive scanning
     *
     * @return list of types that will trigger an exhaustive scanning.
     */
    Set<String> getTypesOfInterest();

    /**
     * Returns true if unannotated fields and methods should be part of the
     * model returned.
     *
     * @return true if unannotated fields and methods will be accessible from
     * the returned {@link org.glassfish.hk2.classmodel.reflect.Types} model.
     */
    boolean modelUnAnnotatedMembers();
}
