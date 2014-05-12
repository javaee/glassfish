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
 * This annotation is placed on fields or on parameters
 * of methods or constructors to indicate that these
 * fields or parameters should come from the configuration
 * instance of the type defined by the {@link ConfiguredBy}
 * annotation on the class.
 * <p>
 * The key field gives the name of the parameter to get from
 * the java bean instance upon which the instance of this service
 * is based.  If the configuration bean is a java bean then
 * a method name starting with &quot;get&quot; and having the
 * key name (with the first letter capitalized) will be invoked
 * to get the value.  if the configuration bean is a map then
 * the value of the key is the value of the key in the map from
 * which to get the value
 * <p>
 * In the case of a field the key field can come from the name
 * of the field (or can be explicitly set, which will override the name
 * of the field).  In the case of a parameter the key field must
 * be filled in with the name of the field on the java bean to
 * use to inject into this parameter
 * 
 * @author jwells
 *
 */
@Retention(RUNTIME)
@Target( { FIELD, PARAMETER })
@InjectionPointIndicator
public @interface Configured {
    /**
     * The name of the field in the java bean or
     * bean-like map to use for injecting into
     * this field or parameter
     * 
     * @return The name of the field to use for
     * injecting into this field or parameter
     */
    public String key() default "";
    
    /**
     * Describes how dynamic a configured field or parameter must be.
     * All parameters of a constructor must be STATIC.
     * All parameters of a method must have the same dynamicity value
     * 
     * @return The dynamicicty of this field or parameter
     */
    public Dynamicity dynamicity() default Dynamicity.STATIC;
    
    /**
     * Describes how dynamic a configured field or parameter should be
     * 
     * @author jwells
     *
     */
    public enum Dynamicity {
        /** This value should not automatically change over the life of the service instance */
        STATIC,
        /** This value can change at any time during the life of the service instance */
        FULLY_DYNAMIC
    }

}
