/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.InjectionPointIndicator;

/**
 * This Injection point indicator can be used for
 * services that have a hierarchical names.  The name space
 * of the name fields of the ActiveDescriptors must form
 * a directed acyclical graph.  For example, this is useful if
 * using a naming scheme based on an XML hierarchy.
 * <p>
 * If the injection point of this annotation is of type
 * {@link ChildIterable} then the generic type of the
 * {@link ChildIterable} must contain the Type
 * of the underlying service, and the {@link ChildIterable}
 * will contain all of the children services whose
 * name starts with the name of the parent ActiveDescriptor
 * appended with the value field of this annotation.
 * <p>
 * If the injection point is NOT a {@link ChildIterable} then
 * the type is as per a normal injection point, but the chosen
 * instance of that type will have a name that starts with the
 * name of the parent ActiveDescriptor appended with the value
 * field of this annotation
 * 
 * @author jwells
 *
 */
@Retention(RUNTIME)
@Target( { FIELD, PARAMETER })
@InjectionPointIndicator
public @interface ChildInject {
    /**
     * The string that will be appended to the
     * name field of the ActiveDescriptor of
     * the parent of this injection point
     * 
     * @return The value to append to the name
     * field of the ActiveDescriptor of the parent
     * of this injection point
     */
    public String value() default "";
    
    /**
     * This field returns the separator that is used to
     * separate heirarchical name fields, for use by the
     * {@link ChildIterable#byKey(String)} method.  This
     * value will be pre-pended to the name given to the
     * {@link ChildIterable#byKey(String)} key parameter
     * @return The separator used to separate a hierachical
     * namespace
     */
    public String separator() default ".";
}
