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
package com.sun.appserv.management.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  Annotation for methods that create AMXConfig elements. It may be applied to the class
  to be created in which case it denotes the default way that the class must be instantiated
  (eg generic instantiation).
  It may also be applied to a method which creates that class in which case the method may define
  its own parameter ordering (for the case of overloaded methods).  The former (class annotation)
  is preferred unless there are overloaded methods to be handled; this allows generic creation
  without any method declaration.
  <p>
  See {@link PropertyConfig} for an example of class annotation; see 
  {@link ResourcesConfig#createJDBCConnectionPoolConfig} for an example of method annotation.
  <p>Items that are named <em>must</em> make the key value the first item in paramNames().
  
  See also org.glassfish.api.amx.AMXConfigInfo in glassfish-api.
*/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AMXCreateInfo   
{
    /**
        An ordered list of names in a create method.  A create method in any {@link AMXConfig}
        should be of the form:
        <pre>MyConfig createMyConfig( String p1, String p2, ..., Map<String,String> optional);</pre>
        paramNames[0] corresponds to p1, paramNames[1] corresponds to p2, etc.  The last paramater,
        the optional Map, need not be included, but may be for completeness.
        <p>
        Parameter names should preferentially be the name of the xml attribute to which the
        parameter maps.
     */
    public String[] paramNames() default {};
}
