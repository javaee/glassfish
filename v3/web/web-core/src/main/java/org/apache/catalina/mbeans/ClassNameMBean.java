/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.apache.catalina.mbeans;


import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import org.apache.tomcat.util.modeler.BaseModelMBean;


/**
 * <p>A convenience base class for <strong>ModelMBean</strong> implementations
 * where the underlying base class (and therefore the set of supported
 * properties) is different for varying implementations of a standard
 * interface.  For Catalina, that includes at least the following:
 * Connector, Logger, Realm, and Valve.  This class creates an artificial
 * MBean attribute named <code>className</code>, which reports the fully
 * qualified class name of the managed object as its value.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2006/03/12 01:27:02 $
 */

public class ClassNameMBean extends BaseModelMBean {


     // ---------------------------------------------------------- Constructors


     /**
      * Construct a <code>ModelMBean</code> with default
      * <code>ModelMBeanInfo</code> information.
      *
      * @exception MBeanException if the initialize of an object
      *  throws an exception
      * @exception RuntimeOperationsException if an IllegalArgumentException
      *  occurs
      */
     public ClassNameMBean()
         throws MBeanException, RuntimeOperationsException {

         super();

     }


     // ------------------------------------------------------------ Properties


     /**
      * Return the fully qualified Java class name of the managed object
      * for this MBean.
      */
     public String getClassName() {

         return (this.resource.getClass().getName());

     }


 }
