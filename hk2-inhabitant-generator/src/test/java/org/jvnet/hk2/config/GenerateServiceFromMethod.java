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

package org.jvnet.hk2.config;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This is here because the hk2-inhabitant-generator does not have a dependency on
 * the configuration subsystem, though it does parse some configuration subsystem
 * annotations.  These annotations are here so as to be able to write test classes
 * that contain these annotations.
 * 
 * @author jwells
 */
@Documented
@Retention(RUNTIME)
@Target( ANNOTATION_TYPE )
public @interface GenerateServiceFromMethod {
    /**
     * This is the key in the metadata that will contain the actual type of the List return type of the
     * method where the user-supplied annotation has been placed
     */
    public final static String METHOD_ACTUAL = "MethodListActual";
    
    /**
     * This is the key in the metadata that will contain the name of the method where the user-supplied
     * annotation has been placed
     */
    public final static String METHOD_NAME = "MethodName";
    
    /**
     * This is the key in the metadata that will contain the fully qualified class name of the class marked
     * {@link Configured} that contains this annotation
     */
    public final static String PARENT_CONFIGURED = "ParentConfigured";
    
    /**
     * This must have the fully qualified class name of the implementation that is to be used in the
     * generated descriptor
     * 
     * @return The fully qualified class name of the implementation
     */
    public String implementation();
    
    /**
     * The set of fully qualified class names of the advertised contracts that are to be used in
     * the generated descriptor.  Note that the implementation class is not automatically added
     * to this list
     * 
     * @return The fully qualified class names of the advertised contracts the generated descriptor
     * should take
     */
    public String[] advertisedContracts();
    
    /**
     * The scope that the descriptor should take.  Defaults to PerLookup
     * 
     * @return The fully qualified class names of the scope the descriptor should take
     */
    public String scope() default "org.glassfish.hk2.api.PerLookup";
}
