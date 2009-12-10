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

package javax.ejb;

import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;

import java.util.concurrent.TimeUnit;

/**
 * Specifies the amount of time in a given time unit that a concurrent 
 * access attempt should block before timing out.  
 * 
 * A value of 0 means concurrent access is not permitted.
 * 
 * A value of -1 means wait indefinitely to acquire a lock.
 * 
 * Values less than -1 are not valid.
 */

@Target({METHOD, TYPE}) 
@Retention(RUNTIME)
public @interface AccessTimeout {

    long value();

    TimeUnit unit() default TimeUnit.MILLISECONDS;

}
