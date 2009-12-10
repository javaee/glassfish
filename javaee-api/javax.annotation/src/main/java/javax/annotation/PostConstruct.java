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
 * Copyright 2005-2006 Sun Microsystems, Inc. All Rights Reserved.
 */


package javax.annotation;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * The PostConstruct annotation is used on a method that needs to be executed 
 * after dependency injection is done to perform any initialization. This 
 * method MUST be invoked before the class is put into service. This 
 * annotation MUST be supported on all classes that support dependency 
 * injection. The method annotated with PostConstruct MUST be invoked even 
 * if the class does not request any resources to be injected. Only one 
 * method can be annotated with this annotation. The method on which the 
 * PostConstruct annotation is applied MUST fulfill all of the following 
 * criteria - 
- The method MUST NOT have any parameters except in the case of EJB 
 * interceptors   in which case it takes an InvocationC	ontext object as 
 * defined by the EJB   specification.
 * - The return type of the method MUST be void.
 * - The method MUST NOT throw a checked exception.
 * - The method on which PostConstruct is applied MAY be public, protected, 
 * package private or private.
 * - The method MUST NOT be static except for the application client.
 * - The method MAY be final.
 * - If the method throws an unchecked exception the class MUST NOT be put into   
 * service except in the case of EJBs where the EJB can handle exceptions and 
 * even   recover from them.
 * @since Common Annotations 1.0
 * @see javax.annotation.PreDestroy
 * @see javax.annotation.Resource
 */
@Documented
@Retention (RUNTIME)
@Target(METHOD)
public @interface PostConstruct {
}
