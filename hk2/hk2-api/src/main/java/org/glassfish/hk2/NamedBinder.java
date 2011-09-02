/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2;

import java.lang.annotation.Annotation;

/**
 * Provides a means to more fully describe a binding that has already been
 * named, or where the name has been assumed to be left blank.
 * 
 * <p/>
 * This is a builder like pattern, where each method more fully builds up
 * a binding description. The builder takes the caller through phases of
 * the build process. This process actually begins in the {@link Binder}
 * class. Once the Binder is bound to a name (or the name is assumed null),
 * a NamedBinder provides a means to add annotations here. Once this phase
 * is completed, a {@link ResolvedBinder} is produced once a target is
 * provided - the target is the actual implementation strategy behing the
 * binding (e.g., class name to load reflectively, a factory, etc.).
 * 
 * @author Jerome Dochez, Jeff Trent
 */
public interface NamedBinder<U>  {

    /**
     * Append annotation to the binding. The annotation should be
     * annotated with {@link javax.inject.Qualifier} annotation
     * 
     * @param annotation the annotation to append
     * @return this instances, with additional annotations appended to it
     */
    NamedBinder<U> annotatedWith(Class<? extends Annotation> annotation);

    /**
     * Have this instance resolve to a particular target implementation
     * class name.
     * 
     * @param className the class name target to resolve to
     * @return a ResolvedBinder
     */
    ResolvedBinder<U> to(String className);

    /**
     * Resolve this binder to a particular target implementation
     * class type. The implementation type will be instantiated
     * depending on its {@kink Scope} when this binding is requested
     * for injection or through explicit lookup.
     * 
     * @param <T> TODO javadoc.
     * @param serviceClass the class type target to resolve to
     * @return a ResolvedBinder
     */
    <T extends U> ResolvedBinder<T> to(Class<? extends T> serviceClass);

    /**
     * Resolve this binder to a particular parameterized type.
     * The implementation type will be instantiated
     * depending on its {@kink Scope} when this binding is requested
     * for injection or through explicit lookup.
     * 
     * @param <T> TODO javadoc.
     * @param typeLiteral the type literal target to resolve to
     * @return a ResolvedBinder
     */
    <T extends U> ResolvedBinder<T> to(TypeLiteral<T> typeLiteral);

    /**
     * Have this instance resolve to a particular singleton implementation
     * instance. In this case the {@link Scope} is assumed to be singleton. 
     * 
     * @param <T> TODO javadoc.
     * @param instance the instance used to satisfy this binding requests.
     */
    <T extends U> void toInstance(T instance);

    /**
     * Have this instance resolve to a particular target factory
     * implementation.
     * 
     * @param <T> TODO javadoc.
     * @param factory the factory target to resolve to
     * @return a ResolvedBinder
     */
    <T extends U> ResolvedBinder<T> toFactory(Factory<T> factory);
    
    /**
     * Have this instance resolve to a particular target factory
     * class type.
     * 
     * @param <T> TODO javadoc.
     * @param factoryType the factory class type target to resolve to
     * @return a ResolvedBinder
     */
    <T extends U> ResolvedBinder<T> toFactory(Class<? extends Factory<? extends T>> factoryType);

    /**
     * Have this instance resolve to a particular target type literal factory.
     * 
     * @param <T> TODO javadoc.
     * @param factoryType the type literal factory to resolve to
     * @return a ResolvedBinder
     */
    <T extends U> ResolvedBinder<T> toFactory(TypeLiteral<? extends Factory<? extends T>> factoryType);

}
