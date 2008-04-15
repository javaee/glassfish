/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.annotations;

import org.jvnet.hk2.component.CageBuilder;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Annotation indicating that additional processing is performed when
 * the component goes into a habitat.
 *
 * <p>
 * This annotation can be used either as a normal annotation on a component
 * or a meta-annotation. A common usage of this is to put this along with
 * {@link Contract} annotation so that all the implementations of a contract
 * receives some infrastructure service.
 *
 * <p>
 * If used as a normal annotation on the contract type, all the services of this
 * contract is subject to the registration hook processing &mdash; that is,
 * the specified {@link CageBuilder} is invoked whenever those services
 * are entered into the habitat, to be given an opportunity to perform
 * additional work.
 *
 * <p>
 * This can be also used as a meta-annotation.
 * Suppose this annotation is placed on annotation X, which in turn is placed
 * on class Y. In this case, {@link CageBuilder} is invoked for every Ys entered into habitat
 * (again, the common case is where X also has {@link Contract} annotation.)
 *
 * <p>
 * This is the interception point for providing additional infrastructure service
 * for certain kinds of inhabitants. 
 *
 * @author Kohsuke Kawaguchi
 * @see CageBuilder
 */
@Contract
@Documented
@Retention(RUNTIME)
@Target({ANNOTATION_TYPE,TYPE})
@Inherited
public @interface CagedBy {
    /**
     * Designates the {@link CageBuilder} that intercepts incoming inhabitants.
     */
    // this value is captured in metadata so that at runtime
    // we can check the registration hook easily.
    @InhabitantMetadata("cageBuilder")
    // we need to be able to find all components that are caged by certain CageBuilder,
    // hence this index.
    @Index
    Class<? extends CageBuilder> value();
}
