/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package javax.ejb;

import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Indicates a dependency on the local, no-interface, or remote view of an Enterprise
 * Java Bean.
 *
 *
 * Either beanName() or lookup() can be used to resolve the EJB dependency
 * to its target session bean component.  It is an error to specify values for
 * both beanName() and lookup().
 *
 * If no explicit linking information is provided and there is only one session
 * bean within the same application that exposes the matching client view type,
 * by default the EJB dependency resolves to that session bean.
 *
 */

@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface EJB {

    /**
     * The logical name of the ejb reference within the declaring component's
     * (e.g., java:comp/env) environment.
     */
    String name() default "";

    String description() default "";

    /**
     * The ejb-name of the Enterprise Java Bean to which this reference 
     * is mapped.  Only applicable if the target EJB is defined within the 
     * same application or stand-alone module as the declaring component.
     */
    String beanName() default "";

    /**
     * Holds one of the following types of the target EJB :
     *  [ Local business interface, bean class (for no-interface view),
     *    Remote business interface, 
     *    Local Home interface, Remote Home interface ]
     *  
     */
    Class beanInterface() default Object.class;

    /**
      * The product specific name of the EJB component to which this
      * ejb reference should be mapped.  This mapped name is often a
      * global JNDI name, but may be a name of any form. 
      * 
      * Application servers are not required to support any particular 
      * form or type of mapped name, nor the ability to use mapped names. 
      * The mapped name is product-dependent and often installation-dependent. 
      * No use of a mapped name is portable. 
      */ 
    String mappedName() default "";

    /**
      * A portable lookup string containing the JNDI name for the target EJB component. 
      *
      */ 
    String lookup() default "";
}
