/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Implementation of this abstract class are handling injection resolution
 * for a particular injection annotation {@see Inject}
 *
 * @author Jerome Dochez
 */
public abstract class InjectionResolver<U extends Annotation> {
    public final Class<U> type;

    /**
     * Construct a resolver with a particular injection type
     * @param type the injection annotation type
     */
    public InjectionResolver(Class<U> type) {
        this.type = type;
    }

    /**
     * Returns the setter method responsible for setting the resource identified by the
     * passed annotation on the passed annotated method.
     *
     * This is useful when the annotation is specified on the getter for instance (like
     * Attribute in the config module for instance) while the setter should be used if
     * values must be set using this injection resolver.
     *
     * By default, the setter method is the annotated method.
     *
     * @param annotated the annotated method
     * @param annotation the annotation on the method
     * @return the setter method to use for injecting the annotation identified resource
     */
    public Method getSetterMethod(Method annotated, U annotation) {
        return annotated;
    }

    /**
     * Returns true if the resolution of this injection identified by the
     * passed annotation instance is optional
     * @param annotation the injection metadata
     * @return true if the {@see getValue()} can return null without generating a
     * faulty injection operation
     */
    public boolean isOptional(AnnotatedElement annotated, U annotation) {
        return false;
    }

    /**
     * Returns the value to inject in the field or method of component annotated with
     * the annotated annotation.
     *
     * @param component injection target
     * @param annotated field of method to inject
     * @param type type of the expected return
     * @return the injectable resource
     * @throws ComponentException if the resource cannot be located.
     */
    public abstract Object getValue(Object component, AnnotatedElement annotated, Class type) throws ComponentException;
}