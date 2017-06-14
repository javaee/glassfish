/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.config.support;

import org.glassfish.api.I18n;
import org.jvnet.hk2.config.GenerateServiceFromMethod;
import org.jvnet.hk2.config.GeneratedServiceName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * List command column annotation.
 *
 * This annotation works with the Listing annotation to provide additional 
 * information about columns in the output. The annotation can be placed on 
 * any method that takes no arguments and returns a type that can be converted to
 * a String, including DuckTyped methods.
 * 
 * @author Tom Mueller
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface ListingColumn {
    /** 
     * Determines the order of the columns from left to right. The "key" attribute
     * is assigned order value 0. Higher order values are for columns further
     * to the right. 
     */
    int order() default GenericListCommand.ColumnInfo.NONKEY_ORDER;
    
    /**
     * Returns the header for the column.  The calculate dvalue is the method
     * name converted to XML form, e.g., getSomeAttr is SOME-ATTR
     */
    String header() default ""; 
     
    /**
     * Determines whether a column should be excluded from the output. The default
     * is false. 
     */
    boolean exclude() default false;
    
    /** 
     * Determines whether a column should be included in the --long output by
     * default.  The default is true.
     */
    boolean inLongByDefault() default true;
}
