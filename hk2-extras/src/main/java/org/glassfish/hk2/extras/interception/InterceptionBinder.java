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
package org.glassfish.hk2.extras.interception;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is placed on an annotation that can be used
 * to indicate a binding between an interceptor (annotated
 * with {@link Interceptor}) and a class to be intercepted
 * (annotated with {@link Intercepted}).
 * 
 * The annotation on which this annotation is placed must
 * have RUNTIME retention and have a Target of TYPE or
 * METHOD (or ANNOTATION_TYPE for transitive bindings).
 * 
 * When an annotation annotated with this annotation is put
 * on an implementation of
 * {@link org.aopalliance.intercept.MethodInterceptor} or
 * {@link org.aopalliance.intercept.ConstructorInterceptor} and
 * which is also annotated with {@link Interceptor} then it becomes
 * associated with that interceptor.  These interceptors will
 * be called on methods of any service annotated with
 * {@link Intercepted} appropriately.
 * 
 * When an annotation annotated with this annotation is
 * used with an hk2 service marked with {@link Intercepted}
 * it can either be put on the entire class, in which case EVERY method
 * of that class will be intercepted, or it can be placed on individual
 * methods of the service to indicate that only those methods should be
 * intercepted.  If it is placed both at the class level and on individual
 * methods then every method will be intercepted.
 * 
 * Annotations annotated with InterceptionBinder are transitive.  In other
 * words if an annotation is annotated with ANOTHER annotation that is
 * marked with InterceptionBinder then any interceptor marked with the other
 * annotation also applies to any service or method marked with this annotation
 * 
 * @author jwells
 *
 */
@Inherited
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface InterceptionBinder {
}
