/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

/**
 * Common metadata properties used by the inhabitant / habitat.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class Constants {

  /**
   * Represents class / contract type name(s).  Only used by Trackers
   * at present.
   */
  public static final String OBJECTCLASS = "objectclass";
  
  /**
   * Service property identifying a service's ranking number.
   * 
   * <p>
   * The service ranking is used by the Framework to determine the <i>natural
   * order</i> of services (when used specifically with the Tracker).
   * 
   * <p>
   * The default ranking is zero (0). A service with a ranking of
   * <code>Integer.MAX_VALUE</code> is very likely to be returned as the
   * default service, whereas a service with a ranking of
   * <code>Integer.MIN_VALUE</code> is very unlikely to be returned.
   * 
   * <p>
   * If the supplied property value is not of type <code>Integer</code>, it is
   * deemed to have a ranking value of zero.
   * 
   * <p>
   * Hk2 manages its meta information as String, but converts to Integer
   * for comparisons.
   */
  public static final String SERVICE_RANKING = "service.ranking";
 
  /**
   * Qualifier (annotation names) used to describe the service.
   */
  public static final String QUALIFIER = "qualifier"; 

  
  /**
   * The executor service names used by Hk2
   */
  public static final String EXECUTOR_INHABITANT_INJECTION_MANAGER = "inhabitant-injection";
  public static final String EXECUTOR_INHABITANT_ACTIVATOR = "inhabitation-activator";
  public static final String EXECUTOR_HABITAT_LISTENERS_AND_TRACKERS = "habitat-listeners";
}
