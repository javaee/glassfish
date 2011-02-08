/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgicdi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * A CDI (JSR-299) Qualifier that indicates a reference to a
 * Service in the OSGi service registry that needs to be injected 
 * into a Bean/Java EE Component.
 *  
 * A Java EE component developer uses this annotation to indicate that the
 * injection point needs to be injected with an OSGi service and can also 
 * provide additional meta-data to aid in service discovery.
 * 
 * If this qualifier annotates an injection point, the 
 * <code>OSGiServiceExtension</code> discovers and instantiates
 * the service implementing the service interface type of the injection point,
 * and makes it available for injection to that injection point.
 * 
 * @author Sivakumar Thyagarajan
 */ 
@Qualifier
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RetentionPolicy.RUNTIME)

public @interface OSGiService {
    /**
     * Determines if the OSGi service that is to be injected refers to a 
     * dynamic instance of the service or is statically bound to
     * the service implementation discovered at the time of injection.
     * 
     * If the value of this annotation element is true, a proxy to the service
     * interface is returned to the client. When the service is used, an active 
     * instance of the service at that point in time is used. If a service 
     * instance that was obtained earlier has gone away 
     * (deregistered by the service provider or stopped), then a new instance 
     * of the service is obtained from the OSGi service registry. This is
     * ideal for stateless and/or idempotent services or service implementations
     * whose lifecycle may be shorter than the client's lifecycle. 
     * 
     * If the value of this annotation element is false, an instance of the service
     * is obtained from the service registry at the time of injection and provided
     * to the client. If the service implementation provider deregisters the obtained
     * service or the service instance is stopped, no attempt is made to get 
     * another instance of the service and a <code>ServiceUnavailableException</code>
     * is thrown on method invocation. This is ideal for stateful or contextual 
     * services and for references to service implementations whose lifecycle 
     * is well-known and is known to be greater than the lifecycle of the client. 
     */
   boolean dynamic() default false;
   
   /**
    * Service discovery criteria. The string provided must match the Filter 
    * syntax specified in the OSGi Core Specification.
    */
   String serviceCriteria() default ""; 
   
   /**
    * Waits, for the specified milliseconds, for at least one service that 
    * matches the criteria specified to be available in the OSGi Service 
    * registry.
    *  
    * 0 indicates indefinite wait.
    * -1 indicates that the service is returned immediately if available 
    * or a null is returned if not available.
    */
   int waitTimeout() default -1;
}
