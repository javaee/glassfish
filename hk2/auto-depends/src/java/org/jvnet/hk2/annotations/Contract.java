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

import org.jvnet.hk2.component.Habitat;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Marker annotation to allow implementations to be discovered from
 * the contract they implement.
 *
 * <p>
 * There are two usages of this annotation.
 *
 * <h2>Contract interface</h2>
 * <p>
 * This annotation can be placed on interfaces and abstract classes <tt>T</tt>,
 * and derived classes that are assignable to <tt>T</tt> with {@link Service}
 * annotation can be looked up by using {@link Habitat#getByContract(Class)}
 * (and its family of methods.)
 *
 * <h2>Contract annotation</h2>
 * <p>
 * This annotation can be used as a meta-annotation on another annotation (let's say X),
 * along with {@link Index} annotation on one of its property. What happens
 * in this case is that when X is placed on classes, those classes will be
 * marked as implementations of the contract. 
 *
 * @author Jerome Dochez
 * @see Index
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE,ANNOTATION_TYPE})
public @interface Contract {
    
}
