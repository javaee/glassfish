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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a required resource for a component. 
 * The HK2 runtime injects all instance variables annotated with 
 * @Inject as well as setter methods.
 <p>
 * Examples:<br>
<pre>
        @Inject("GlassfishMBeanServer")
        MBeanServer mbeanserver;
        
        @Inject  // unnamed
        MBeanServer mbeanserver;
</pre>
 *
 * @see org.jvnet.hk2.annotations.Extract
 * @see org.jvnet.hk2.component.Habitat
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target({METHOD,FIELD})
public @interface Inject {
    /**
     * Returns the name.
     * @return name of the resource
     */
    public String name() default "";

    /**
     * When true, it is not an error if not present.
     * When false, a failure occurs when not present.
     @return true if optional, false otherwise
     */
    public boolean optional() default false;
}
