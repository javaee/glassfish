/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.glassfish.hk2.api.AnnotationLiteral;
import org.glassfish.hk2.api.Unqualified;

/**
 * This is an implementation of {@link Unqualified}.  It is here
 * as a convenience for creating instances of this annotation
 * where necessary
 * 
 * @author jwells
 *
 */
public class UnqualifiedImpl extends AnnotationLiteral<Unqualified> implements Unqualified {
    private static final long serialVersionUID = 7982327982416740739L;
    
    private final Class<? extends Annotation>[] value;
    
    /**
     * Makes a copy of the annotation classes values and initializes
     * this {@link Unqualified} annotation with those values
     * 
     * @param value A list of qualifiers that must NOT be on
     * injection point.  A zero-length list indicates that 
     * no qualifier must be present on the matching service
     */
    // @SafeVarargs
    public UnqualifiedImpl(Class<? extends Annotation>... value) {
        this.value = Arrays.copyOf(value, value.length);
    }
    
    /**
     * The set of annotations that must not be associated with
     * the service being injected
     * 
     * @return All annotations that must not be on the injected
     * service.  An empty list indicates that NO annotations must
     * be on the injected service
     */
    @Override
    public Class<? extends Annotation>[] value() {
        return Arrays.copyOf(value, value.length);
    }
    
    @Override
    public String toString() {
        return "UnqualifiedImpl(" + Arrays.toString(value) + "," + System.identityHashCode(this) + ")";
    }
}
