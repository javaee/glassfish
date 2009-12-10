/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

/*
 *
 * Copyright 2005-2009 Sun Microsystems, Inc. All Rights Reserved.
 */

package javax.annotation;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * The ManagedBean annotation marks a POJO (Plain Old Java Object) as a
 * ManagedBean.A ManagedBean supports a small set of basic services such as
 * resource injection, lifecycle callbacks and interceptors.
 *
 * @since Common Annotations 1.1
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ManagedBean {
    /**
     * The name of the Managed Bean. Managed Bean names must be unique within a
     * Java EE module. For each named Managed Bean, Java EE containers must make
     * available the following entries in JNDI, using the same naming scheme used
     * for EJB components.
     * <p>
     * In the application namespace: <p>
     * java:app/&lt;module-name&gt;/&lt;bean-name&gt; <p>
     * In the module namespace of the module containing the Managed Bean:
     * <p> java:module/&lt;bean-name&gt;
     *
     */
    public String value() default "";
}
