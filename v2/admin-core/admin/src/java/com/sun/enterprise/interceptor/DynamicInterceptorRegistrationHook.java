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
package com.sun.enterprise.interceptor;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;

/**
    Allows registration of MBeans dynamically for a restricted set of MBeanServer methods.
    These include:<br/>
    <ul>
    <li>isRegistered</li>
    <li>getClassLoaderFor</li>
    </ul>
    Other methods such as get/setAttribute(s) and invoke() are not supported.
 */
public interface DynamicInterceptorRegistrationHook
{
    /**
        Called when an MBean is not registered.  If desired, the MBean may be registered.
        Note that nothing precludes another thread from registering the specified MBean at any time,
        and thus registrationHook() could be called with the MBean already registered.
        @param MBeanServer  the MBeanServer in which the missing MBean should be registered
        @param ObjectName   the ObjectName of the non-registered MBean
        @return true if the MBean is now presnt.
     */
    public boolean   registrationHook( final MBeanServer server, final ObjectName objectName );
};








