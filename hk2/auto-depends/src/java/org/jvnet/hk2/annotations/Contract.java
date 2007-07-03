/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.jvnet.hk2.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

import org.jvnet.hk2.component.ComponentManager;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
 * annotation can be looked up by using {@link ComponentManager#getComponent(Class)}
 * (and its family of methods.)
 *
 * <h2>Contract annotation</h2>
 * <p>
 * TBD.
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target({TYPE,ANNOTATION_TYPE})
public @interface Contract {
    
}
