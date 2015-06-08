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
package org.glassfish.hk2.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a method of an annotation as providing a value that
 * should be placed into the metadata of a {@link Descriptor}.
 * <p>
 * One downside of using Qualifiers (or values in the Scope annotation) is that
 * in order to get these values the underlying classes must be reified.  In order
 * to relieve the system from having to reify classes to get the data in the
 * scope and qualifier annotations this annotation can be placed on the methods
 * of an annotation to indicate that the values found in the annotation should be
 * placed into the metadata of the descriptor.  Since the metadata of a descriptor
 * can be accessed without classloading the underlying class the descriptor is
 * describing this data can then be accessed without needing to reify the class.
 * <p>
 * This qualifier will be honored whenever the system does automatic analysis of
 * a class (for example, when analyzing a pre-reified class file or object).  It
 * will also be used by the automatic inhabitant generator when analyzing class files
 * marked &#64;Service.  However, if the programmatic API is being used to build up
 * a descriptor file this annotation is not taken into account, and it is hence the
 * responsibility of the user of the programmatic API to fill in the metadata values
 * itself.
 * <p>
 * This annotation can be placed on any method of an annotation marked with
 * {@link javax.inject.Scope} or {@link javax.inject.Qualifier}.  The "toString" of the object returned
 * from that method will be placed in the metadata of the descriptor that is
 * created (unless the object returned is a Class, in which case the name of
 * the Class is used)
 * <p>
 * @see Descriptor ActiveDescriptor
 * 
 * @author jwells
 *
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD})
public @interface Metadata {
    /**
     * This is the key that will be used in the metadata field of the descriptor.
     * Values returned from the methods annotated with this annotation will have
     * their toString called and the result will be added to the metadata key with
     * this value (unless the return type is Class, in which case the name of
     * the class will be used)
     * 
     * @return The key of the metadata field that will be added to
     */
    public String value();

}
